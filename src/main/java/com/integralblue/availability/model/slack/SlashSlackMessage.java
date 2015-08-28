package com.integralblue.availability.model.slack;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.integralblue.availability.model.slack.parser.ParsableSlackMessage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class SlashSlackMessage implements ParsableSlackMessage {
	@NonNull String userId;
	@NonNull String token;
	@NonNull String userName;
	@NonNull String command;
	String text;
	String teamId;
	String teamDomain;
	String channelId;
	String channelName;
	@Override
	public Optional<List<String>> getParameters() {
		return Optional.ofNullable(Arrays.asList(getText().split("\\s")));
	}
}