package com.integralblue.availability.model.slack;

import javax.validation.constraints.NotNull;

import com.integralblue.availability.model.slack.parser.ParsableSlackMessage;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class SlashSlackMessage implements ParsableSlackMessage {
	@NotNull @NonNull String userId;
	@NotNull @NonNull String token;
	@NotNull @NonNull String userName;
	@NotNull @NonNull String command;
	String text;
	String teamId;
	String teamDomain;
	String channelId;
	String channelName;
	
	public void setChannel_name(String channelName){
		this.channelName=channelName;
	}

	public void setUser_id(@NotNull String userId) {
		this.userId = userId;
	}

	public void setUser_name(@NotNull String userName) {
		this.userName = userName;
	}

	public void setTeam_id(String teamId) {
		this.teamId = teamId;
	}

	public void setTeam_domain(String teamDomain) {
		this.teamDomain = teamDomain;
	}

	public void setChannel_id(String channelId) {
		this.channelId = channelId;
	}
	
	
}
