package com.evrim.mevzuat.search.api.config;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebSecurityExpressionRoot;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.evrim.common.security.StatelessAuthenticationFilter;
import com.evrim.common.security.TokenAuthenticationService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	TokenAuthenticationService tokenAuthenticationService;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
	    http
	    .csrf().disable()
	    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	    .and()
	    .authorizeRequests()
	        .anyRequest().authenticated();
	    
	    http.addFilterBefore(new StatelessAuthenticationFilter(tokenAuthenticationService), BasicAuthenticationFilter.class);
	}
	
	@Bean
	public AuthenticationEntryPoint unauthorizedEntryPoint() {
		return (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.expressionHandler(new DefaultWebSecurityExpressionHandler() {
	        @Override
	        protected SecurityExpressionOperations createSecurityExpressionRoot(Authentication authentication, FilterInvocation fi) {
	            WebSecurityExpressionRoot root = (WebSecurityExpressionRoot) super.createSecurityExpressionRoot(authentication, fi);
	            root.setDefaultRolePrefix(""); //remove the prefix ROLE_
	            return root;
	        }
	    });
	}
}
