package com.integralblue.availability.model.slack;

import java.util.Map;

import org.springframework.ui.Model;

public class SlackMessageModelFactory {
	public static <T extends SlackMessage> T get(Model controllerModel, Class<T> asClass) {
		if (asClass == null)
			throw new IllegalArgumentException("asClass can't be null.");
		
		Map<String, Object> values = controllerModel.asMap(); 
		if (asClass == SlashSlackMessage.class) {
			SlashSlackMessage msg = SlashSlackMessage.builder()
					.token(values.getOrDefault("token", "").toString())
					.teamId(values.getOrDefault("team_id", "").toString())
					.teamDomain(values.getOrDefault("team_domain", "").toString())
					.channelId(values.getOrDefault("channel_id", "").toString())
					.channelName(values.getOrDefault("channel_name", "").toString())
					.userName(values.getOrDefault("user_name", "").toString())
					.command(values.getOrDefault("command", "").toString())
					.build();
			msg.setText(values.getOrDefault("text", "").toString());
			msg.setUserId(values.getOrDefault("user_id", "").toString());
			return msg;
		}
		
		/*
		 * token=gIkuvaNzQIHg97ATvDxqgjtO
team_id=T0001
team_domain=example
channel_id=C2147483705
channel_name=test
user_id=U2147483697
user_name=Steve
command=/weather
text=94070
		 */
		throw new IllegalArgumentException(asClass.getCanonicalName() + " is not handled by this factory.");
	}
}
