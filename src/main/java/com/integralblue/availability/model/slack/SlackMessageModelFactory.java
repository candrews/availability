package com.integralblue.availability.model.slack;

import java.util.Map;
import java.util.Optional;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SlackMessageModelFactory {
	public static Optional<SlashSlackMessage> getSlackMessage(@NonNull Map<String, String> requestParams) {
		try {
			SlashSlackMessage msg = new SlashSlackMessage(
					requestParams.get("user_id"),
					requestParams.get("text"),
					requestParams.get("token"),
					requestParams.get("user_name"),
					requestParams.get("command"),
					requestParams.get("team_id"),
					requestParams.get("team_domain"),
					requestParams.get("channel_id"),
					requestParams.get("channel_name"));
			return Optional.of(msg);
		} catch (NullPointerException e) {
			log.error("When making a SlackMessage; a required field was null: " + e.getMessage());
			return Optional.empty();
		}
	}
}
