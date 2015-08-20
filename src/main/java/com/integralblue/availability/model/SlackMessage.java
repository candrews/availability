package com.integralblue.availability.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SlackMessage {
	String token;
	String teamId;
	String teamDomain;
	String channelId;
	String channelName;
	String userId;
	String userName;
	String command;
	String text;
}
