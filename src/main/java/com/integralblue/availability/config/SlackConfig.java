package com.integralblue.availability.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.integralblue.availability.properties.SlackProperties;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;

@Configuration
public class SlackConfig {
	
	@Autowired
	private SlackProperties slackProperties;
	
	@ConditionalOnProperty(value = "slack.apiToken")
	@Bean(destroyMethod="disconnect")
	public SlackSession slackSession() throws IOException{
		final SlackSession slackSession = SlackSessionFactory.createWebSocketSlackSession(slackProperties.getApiToken());
		slackSession.connect();
		return slackSession;
	}

}
