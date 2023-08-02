package com.evrim.mevzuat.search.api.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import redis.clients.jedis.JedisPoolConfig;

@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {
	
	private static final Logger LOG = LoggerFactory.getLogger(RedisConfig.class);	

	@Value("${cache.hostname}")
	private String redisHostName;

	@Value("${cache.port}")
	private int redisPort;

	@Value("${cache.password}")
	private String redisPassword;

	@Value("${cache.default.timeout}")
	private long defaultCacheTimeout;

	@Value("${cache.auth.name}")
	private String authCacheName;
	
	@Value("${cache.auth.timeout}")
	private long authCacheTimeout;

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public JedisPoolConfig jedisPoolConfig() {
	    JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
	    jedisPoolConfig.setMaxTotal(128);
	    return jedisPoolConfig;
	}
	
	@Bean
	public JedisConnectionFactory jedisConnectionFactory() {
		JedisConnectionFactory factory = new JedisConnectionFactory();
		factory.setHostName(redisHostName);
		factory.setPort(redisPort);
		factory.setPassword(redisPassword);
		factory.setUsePool(true);
		factory.setPoolConfig(jedisPoolConfig());
		
		LOG.debug("redis connection factory - {}, {}", redisHostName, redisPort);
		
		return factory;
	}

	@Bean
	public RedisTemplate<Object, Object> redisTemplate() {
		RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<Object, Object>();
		redisTemplate.setConnectionFactory(jedisConnectionFactory());
		
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(new StringRedisSerializer());
		
		return redisTemplate;
	}

	@Bean
	public RedisCacheManager cacheManager() {
		RedisCacheManager redisCacheManager = new RedisCacheManager(redisTemplate());

		// configure ttls
		redisCacheManager.setDefaultExpiration(defaultCacheTimeout);
		
		Map<String, Long> expires = new HashMap<String, Long>();
		expires.put(authCacheName, authCacheTimeout);
		
		redisCacheManager.setExpires(expires);

		return redisCacheManager;
	}
	
}
