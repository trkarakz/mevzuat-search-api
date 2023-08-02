package com.evrim.mevzuat.search.api.aspect;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.evrim.common.dto.security.AuthResultWithCompanyDto;
import com.evrim.common.entity.Log;
import com.evrim.common.logging.Loggable;
import com.evrim.common.security.AuthenticationService;
import com.evrim.common.util.JsonUtil;
import com.evrim.mevzuat.search.api.service.LogService;

/**
 * Intercept service methods and prints performance and info logging according to @Loggable annotation
 * 
 * If target class or target method have @Loggable annotation
 * 		@Loggable.logInfo prints info logging (on method begin and on method exit)
 * 
 * 		@Loggable.logPerformance prints performance logging
 * 
 * <strong> Note </strong> : Method annotation override the class annotation
 *
 */
@Aspect
@Component("loggingAspect")
public class LoggingAspect {
	private static final Logger LOG = LoggerFactory.getLogger("Logger");
	
	@Autowired
	AuthenticationService authenticationService;
	
	@Autowired
	LogService logService;
	
	@Around("inServiceLayer()")
	public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getStaticPart().getSignature();
		
		Loggable loggable = null;
		
		// try to get Loggable annotation
		try {
			// get loggabble annotation from method
			loggable = joinPoint.getTarget().getClass().getMethod(signature.getMethod().getName(), signature.getParameterTypes()).getAnnotation(Loggable.class);
			
			// if method does not have Loggable, try to get from class
			if(loggable == null)
				loggable = joinPoint.getTarget().getClass().getAnnotation(Loggable.class);
			
		} catch (Throwable e) {
			LOG.error("Loggable annotation coult not get",e);
		}
		
		if(loggable == null) {
			return  joinPoint.proceed();
		}
		
		// Perf4J stopwatch for performance logging
		StopWatch stopWatch = new Slf4JStopWatch();
		
		AuthResultWithCompanyDto authResult = authenticationService.getAuthenticatedUser();
		String userEmail = authResult==null?"mevzuat-search-api":authResult.getAuthResult().getEmail();
		
		long startTime = Calendar.getInstance().getTimeInMillis();
		long duration = 0L;

		try {
			String paramStr = "";
			
			if(loggable.logInfo())
				paramStr = JsonUtil.toJson(joinPoint.getArgs());
			
			if(loggable.logInfo())
				LOG.debug("MevzuatSearhService Begin:{}, Params:{}", joinPoint.getSignature().toShortString(), paramStr);
			
			if(loggable.logPerformance())
				stopWatch.start();
			
			Object methodResult = joinPoint.proceed();
			
			duration = Calendar.getInstance().getTimeInMillis() - startTime;

			if(loggable.logInfo())
				LOG.debug("MevzuatSearhService End:{}, Duration:{}", 
						joinPoint.getSignature().toShortString(),  duration);
			
			if(loggable.logPerformance())
				stopWatch.stop(joinPoint.getSignature().toShortString()+"-SUCCESS");
			
			
			if(loggable.logInfo()) {
				String boundedParamStr = paramStr.substring(0, Math.min(400, paramStr.length()));
				
				Log log = new Log(userEmail, "mevzuat-search-api", joinPoint.getSignature().toShortString(), 
						boundedParamStr, null, new Date(), "", duration);
				
				logService.save(log);
			}

			return methodResult;
		} catch (Throwable e) {
			
			if(loggable.logInfo())
				LOG.error("MevzuatSearhService Error:{}, User:{}, Error:{}, Duration:{}",joinPoint.getSignature().toShortString(), userEmail, e.getMessage(), Calendar.getInstance().getTimeInMillis() - startTime, e);
			
			if(loggable.logPerformance())
				stopWatch.stop(joinPoint.getSignature().toShortString()+"-ERROR");
			
			Log log = new Log(userEmail, "mevzuat-search-api", joinPoint.getSignature().toShortString(), 
					JsonUtil.toJson(joinPoint.getArgs()), StringUtils.substring(ExceptionUtils.getRootCauseMessage(e), 0, 500), 
					new Date(), "", duration);
			
			logService.save(log);

			throw e;
		}

	}

	@Pointcut("execution(* com.evrim.mevzuat.search.api.service..*(..))")
	public void inServiceLayer() {
	}
	
}
