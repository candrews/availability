package com.integralblue.availability.model.slack.parser;

import java.util.List;

import javax.validation.constraints.NotNull;

public class IsobarSlashSlackParsingStrategy extends SlashSlackParsingStrategy {
	@Override
	public String getParsingIdentifier() {
		return "isobar";
	}
	
	@Override
	public String getUserEmailAddresses(@NotNull ParsableSlackMessage message) {
		assertOneParameter(message);
		return message.getParameters().get().get(0) + "@isobar.com";
	}

	@Override
	public SlackCommand getIntention(ParsableSlackMessage message) {
		assertAtLeastOneParameter(message);
		List<String> params = message.getParameters().get();
		
		if (params.get(0).startsWith("@"))
			return SlackCommand.USER_STATUS;
		else if (params.get(0).equalsIgnoreCase("rooms"))
			return SlackCommand.ROOM_STATUS_BY_OFFICE;
		else
			return SlackCommand.UNKNOWN;
	}

	@Override
	public String getRoomLocation(@NotNull ParsableSlackMessage message) {
		assertAtLeastOneParameter(message);
		switch (message.getParameters().get().get(0).trim().toLowerCase()) {
			case "bos":
			case "boston":
				return "Boston";
			case "nyc":
			case "new york":
			case "new york city":
				return "New York";
			case "chicago":
				return "Chicago";
			case "gtm":
			case "goto":
			case "gotomeeting":
			case "goto meeting":
				return "GTM";
			case "lyn":
			case "lymn":
				return "Lynn";
			default:
				return "Other";
		}
	}
}
