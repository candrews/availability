package com.integralblue.availability.model.slack.parser;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public abstract class SlashSlackParsingStrategy {
	public abstract String getUserEmailAddresses(ParsableSlackMessage message);
	public abstract String getRoomLocation(ParsableSlackMessage message);
	public abstract SlackCommand getIntention(ParsableSlackMessage message);
	public abstract String getParsingIdentifier();
	
	protected void assertOneParameter(ParsableSlackMessage message) {
		if (message.getParameters().isPresent())
			throw new IllegalArgumentException("Expected at least one message parameter but there were none; " + message.toString());
		List<String> params = message.getParameters().get();
		if (params.size() != 1)
			throw new IllegalArgumentException("Expected only 1 message parameter, got " + params.size() + "; " + message.toString());
	}
	
	protected void assertAtLeastOneParameter(ParsableSlackMessage message) {
		if (message.getParameters().isPresent())
			throw new IllegalArgumentException("Expected at least one message parameter but there were none; " + message.toString());
		if (message.getParameters().get().isEmpty())
			throw new IllegalArgumentException("Expected at least one message parameter, got none; " + message.toString());
	}
}
