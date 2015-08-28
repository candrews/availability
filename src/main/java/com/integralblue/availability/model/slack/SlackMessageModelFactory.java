package com.integralblue.availability.model.slack;

import java.util.Map;
import java.util.Optional;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SlackMessageModelFactory {
	public static Optional<SlashSlackMessage> getSlackMessage(@NonNull Map<String, String> requestParams) {
		try {
			SlashSlackMessage msg = SlashSlackMessage.builder()
					.userId(requestParams.get("user_id"))
					.text(requestParams.get("text"))
					.token(requestParams.get("token"))
					.userName(requestParams.get("user_name"))
					.command(requestParams.get("command"))
					.teamId(requestParams.get("team_id"))
					.teamDomain(requestParams.get("team_domain"))
					.channelId(requestParams.get("channel_id"))
					.channelName(requestParams.get("channel_name"))
					.build();
			return Optional.of(msg);
		} catch (NullPointerException e) {
			log.error("When making a SlackMessage; a required field was null: " + e.getMessage());
			return Optional.empty();
		}
	}
}
