package com.integralblue.availability.model.slack;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class SlashSlackMessage {
	@NonNull String userId;
	@NonNull String text;
	@NonNull String token;
	@NonNull String userName;
	@NonNull String command;
	String teamId;
	String teamDomain;
	String channelId;
	String channelName;
}