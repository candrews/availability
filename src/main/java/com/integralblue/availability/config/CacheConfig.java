package com.integralblue.availability.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.cache.CacheBuilder;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
    	final GuavaCacheManager guavaCacheManager = new GuavaCacheManager();
    	guavaCacheManager.setCacheBuilder(CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES));
    	return guavaCacheManager;
    }
}
