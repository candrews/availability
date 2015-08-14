package com.integralblue.availability.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mustache.web.MustacheViewResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private ThymeleafViewResolver viewResolver;

	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		MappingJackson2JsonView mappingJackson2JsonView = new MappingJackson2JsonView();
		mappingJackson2JsonView.setObjectMapper(objectMapper);
		registry.enableContentNegotiation(true, mappingJackson2JsonView);
		registry.viewResolver(viewResolver);
	}

}
