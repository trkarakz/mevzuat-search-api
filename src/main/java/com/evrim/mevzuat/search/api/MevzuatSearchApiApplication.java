package com.evrim.mevzuat.search.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"com.evrim.webgumruk", "com.evrim.mevzuat.search.api", "com.evrim.common"})
@EntityScan(basePackages = {"com.evrim.common.entity", "com.evrim.mevzuat.search.api.entity"})
@EnableJpaRepositories(basePackages = {"com.evrim.mevzuat.search.api.repository"})
@EnableAspectJAutoProxy
@EnableAsync
@EnableScheduling
public class MevzuatSearchApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MevzuatSearchApiApplication.class, args);
	}
	
	@Bean
    public MessageSource messageSource() { 
    	ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:/locale/");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }  
}
