package com.evrim.mevzuat.search.api.config;

import java.text.ParseException;
import java.time.LocalDate;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.evrim.common.util.DateUtil;

@Configuration
@EnableWebMvc
public class WebMvcConfig extends WebMvcConfigurerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(getStringToDateConverter());
	}

	public Converter<String, LocalDate> getStringToDateConverter() {
		return new Converter<String, LocalDate>() {

			@Override
			public LocalDate convert(String source) {
				try {
					return DateUtil.asLocalDate(DateUtils.parseDate(source, new String[] { "yyyy-MM-dd" }));
				} catch (ParseException e) {
					logger.error("Converter error", e);
					return null;
				}
			}
		};
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {

		registry.addResourceHandler("swagger-ui.html")
				.addResourceLocations("classpath:/META-INF/resources/");

		registry.addResourceHandler("/webjars/**")
				.addResourceLocations("classpath:/META-INF/resources/webjars/");

	}
}