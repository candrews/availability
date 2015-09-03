package com.integralblue.availability.controller;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.method.annotation.UriComponentsBuilderMethodArgumentResolver;
import org.springframework.web.util.UriComponentsBuilder;

import com.integralblue.availability.model.Availability;
import com.integralblue.availability.model.FreeBusyStatus;
import com.integralblue.availability.model.Room;
import com.integralblue.availability.model.slack.SlashSlackMessage;
import com.integralblue.availability.properties.ApplicationProperties;
import com.integralblue.availability.properties.SlackProperties;
import com.integralblue.availability.service.AvailabilityService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class SlackController {
	@Autowired
	protected SlackProperties slackProperties;
	
	@Autowired
	protected ApplicationProperties applicationProperties;
	
	@Autowired
	@Qualifier("slackAvailabilityService")
	private AvailabilityService availabilityService;
	
	private static final Pattern ROOM_LIST_PATTERN = Pattern.compile("rooms (.+?)\\s*", Pattern.CASE_INSENSITIVE);
	private static final Pattern OTHER_PATTERN = Pattern.compile("(.+?)\\s*", Pattern.CASE_INSENSITIVE);
	
	@RequestMapping(value="/slack/availability",method=RequestMethod.GET,produces=MediaType.TEXT_PLAIN_VALUE)
	@ResponseBody
	public String getAvailability(@Valid SlashSlackMessage message, Model model) {
		log.debug("Incoming Slack request: {}", message.toString());
		if (!message.getToken().equals(slackProperties.getSlashCommandToken())) {
			throw new AccessDeniedException("Token in request " + message.getToken() + " did not match configured token.");
		}
		
		try {
			final Matcher roomListMatcher = ROOM_LIST_PATTERN.matcher(message.getText());
			if(roomListMatcher.matches()){
				return getTextForRoomList(roomListMatcher.group(1));
			}
			final Matcher otherMatcher = OTHER_PATTERN.matcher(message.getText());
			if(otherMatcher.matches()){
				String emailAddress;
				if(otherMatcher.group(1).startsWith("@")){
					// TODO look up the given slack handle using the slack API to get the email address
					emailAddress = otherMatcher.group(1).substring(1) + "@isobar.com";
				}else{
					emailAddress = otherMatcher.group(1);
				}
				return getTextForUserStatus(emailAddress);
			}
			return "Unknown command";
		} catch (Exception e) {
			throw new RuntimeException("Error while trying to process message: " + message, e);
		}
	}

	private String getTextForRoomList(String roomList) {
		return availabilityService.getRoomListAvailability(roomList)
				.map(roomToOptionalAvailability -> "*" + linkToRoomList(roomList) + "*\n"
						+ roomToOptionalAvailability.entrySet().stream()
								.map(entry -> "> " + optionalAvailabilityToEmoji(entry.getValue())
										+ linkToRoom(entry.getKey()) + ": "
										+ entry.getValue()
												.map(availability -> availability.getStatusAtStart().toString()
														+ (availability.getStatusAtStart() == FreeBusyStatus.FREE ? ""
																: (" Next available at " + availability.getNextFree())))
												.orElse("does not exist (error)"))
								.collect(Collectors.joining("\n")))
				.orElse("room list " + roomList + " was not found");
	}
	
	private String optionalAvailabilityToEmoji(Optional<Availability> optionalAvailability) {
		return optionalAvailability.map(availability -> {
			switch (availability.getStatusAtStart()) {
			case BUSY:
				return ":no_entry:";
			case FREE:
				return ":white_check_mark:";
			case TENTATIVE:
				return ":question:";
			default:
				throw new IllegalStateException();
			}
		}).orElse(":exclamation:");
	}

	private String getTextForUserStatus(String user) {
		return availabilityService.getAvailability(user, new Date(), new Date()).map(availability -> {
			String returnText = optionalAvailabilityToEmoji(Optional.of(availability)) + linkToUser(user) + " is " + availability.getStatusAtStart() + ".";
			Date nextAvailable = availability.getNextFree();
			if (availability.getStatusAtStart() != FreeBusyStatus.FREE) {
				// TODO format the nextAvailable time to be in the requesting user's time zone
				returnText+="Next available at " + nextAvailable + ".";
			}
			return returnText;
		}).orElse("user " + user + " not found");
	}
	
	private String linkToRoomList(String roomList) {
		return "<"
				+ UriComponentsBuilder.fromUri(applicationProperties.getBaseUri()).path("list/{roomList}/availability").buildAndExpand(Collections.singletonMap("roomList", roomList)).toUriString()
				+ "|" + roomList + ">";
	}

	private String linkToUser(String email) {
		return "<"
				+ UriComponentsBuilder.fromUri(applicationProperties.getBaseUri()).path("user/{user}/availability").buildAndExpand(Collections.singletonMap("user", email)).toUriString()
				+ "|" + email + ">";
	}

	private String linkToRoom(Room room) {
		return "<"
				+ UriComponentsBuilder.fromUri(applicationProperties.getBaseUri()).path("user/{user}/availability").buildAndExpand(Collections.singletonMap("user", room.getEmailAddress())).toUriString()
				+ "|" + room.getName() + ">";
	}
}
