package com.integralblue.availability.model.slack;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
@Builder
public class SlashSlackMessage extends SlackMessage {
	String token;
	String teamId;
	String teamDomain;
	String channelId;
	String channelName;
	String userName;
	String command;
}
