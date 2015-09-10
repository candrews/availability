package com.integralblue.availability.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.integralblue.availability.model.Availability;
import com.integralblue.availability.model.FreeBusyStatus;
import com.integralblue.availability.model.Room;
import com.integralblue.availability.properties.ApplicationProperties;
import com.integralblue.availability.service.AvailabilityService;
import com.integralblue.availability.service.SlackMessageService;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SlackMessageServiceImpl implements SlackMessageService {
	
	@Autowired
	protected ApplicationProperties applicationProperties;
	
	@Autowired
	@Qualifier("slackAvailabilityService")
	private AvailabilityService availabilityService;
	
	@Autowired(required=false)
	private SlackSession slackSession;
	
	private static final Pattern ROOM_LIST_PATTERN = Pattern.compile("rooms (.+?)\\s*", Pattern.CASE_INSENSITIVE);
	
	private static final String DISPLAY_AVAILABILITY_NOT_TODAY_FORMAT_PATTERN = "h:mm a 'on' EEE";
	private static final String DISPLAY_AVAILABILITY_TODAY_FORMAT_PATTERN = "h:mm a";
	
	private static final Pattern SLACK_AT_USER_REFERENCE = Pattern.compile("@(.+)");
	private static final Pattern SLACK_BRACKETED_USER_REFERENCE = Pattern.compile("<@(.+)>");
	private static final Pattern EMAIL_ADDRESS = Pattern.compile("([^<>@\"'|]+@[^<>@\"'|]+)");
	private static final Pattern MAILTO_EMAIL_ADDRESS = Pattern.compile("<mailto:([^<>@\"'|]+@[^<>@\"'|]+)\\|.*?>");

	@Override
	public String respondToMessage(Optional<SlackUser> optionalMessageSender, @NonNull String message) {
		final TimeZone timeZone = optionalMessageSender.map(s -> {
			return TimeZone.getTimeZone(s.getTimeZone());
		}).orElse(TimeZone.getDefault());
		try {
			if("help".equalsIgnoreCase(message)){
				return "Determine a user, room, or list of rooms availability:\n" + 
						"You can send any of these commands by direct messaging the bot directly, sending an @ message to the bot, or using the /avail command:\n" +
						"`rooms [list name]` Get the availability for all rooms in a given list\n" +
						"`list of [emailaddress] or [@slackusername]` Get the availability of a room or user by email address or slack name\n" +
						"for example:\n" +
						"`rooms boston` will tell you the availability of all rooms in the boston list\n" +
						"`@bob.jones bill.gates@microsoft.com` will tell you the availability of Bob and Bill";
			}
			StringBuilder ret = new StringBuilder();
			ret.append("All times are in your timezone (" + timeZone.getDisplayName() + ")\n");
			final Matcher roomListMatcher = ROOM_LIST_PATTERN.matcher(message);
			if(roomListMatcher.matches()){
				ret.append(getTextForRoomList(roomListMatcher.group(1), timeZone));
				return ret.toString();
			}
			List<String> emailAddresses = new ArrayList<>();
			for(String s : message.split(" ")){
				final Matcher slackAtUserReferenceMatcher = SLACK_AT_USER_REFERENCE.matcher(s);
				if(slackAtUserReferenceMatcher.matches()){
					// convert the Slack @ reference to an email address
					if(slackSession==null){
						ret.append("Sorry, but this integration is not configured to be able to look up slack references");
					}else{
						SlackUser slackUser = slackSession.findUserByUserName(slackAtUserReferenceMatcher.group(1));
						if(slackUser == null){
							ret.append("No slack user was found with the username `" + slackAtUserReferenceMatcher.group(1) + "`\n");
						}else{
							emailAddresses.add(slackUser.getUserMail());
						}
					}
					continue;
				}
				final Matcher slackBracketedUserReferenceMatcher = SLACK_BRACKETED_USER_REFERENCE.matcher(s);
				if(slackBracketedUserReferenceMatcher.matches()){
					// convert the Slack <@> reference to an email address
					if(slackSession==null){
						ret.append("Sorry, but this integration is not configured to be able to look up slack references");
					}else{
						SlackUser slackUser = slackSession.findUserById(slackBracketedUserReferenceMatcher.group(1));
						if(slackUser == null){
							ret.append("No slack user was found with the userid `" + slackBracketedUserReferenceMatcher.group(1) + "`\n");
						}else{
							emailAddresses.add(slackUser.getUserMail());
						}
					}
					continue;
				}
				final Matcher emailAddressMatcher = EMAIL_ADDRESS.matcher(s);
				if(emailAddressMatcher.matches()){
					emailAddresses.add(emailAddressMatcher.group(1));
					continue;
				}
				final Matcher mailtoEmailAddressMatcher = MAILTO_EMAIL_ADDRESS.matcher(s);
				if(mailtoEmailAddressMatcher.matches()){
					emailAddresses.add(mailtoEmailAddressMatcher.group(1));
					continue;
				}
				ret.append("`" + s + "` is not an email address or slack @ reference\n");
			}
			if(! emailAddresses.isEmpty()){
				ret.append(getTextForUsersStatus(emailAddresses, timeZone));
			}
			return ret.toString();
		} catch (Exception e) {
			log.error("Error while trying to process message: {}", message, e);
			return "Error: `" + e.getLocalizedMessage() + "`";
		}
	}

	private String getTextForRoomList(String roomList, TimeZone timeZone) {
		return availabilityService.getRoomListAvailability(roomList)
				.map(roomToOptionalAvailability -> "*" + linkToRoomList(roomList) + "*\n"
						+ roomToOptionalAvailability.entrySet().stream()
								.map(entry -> "> " + optionalAvailabilityToEmoji(entry.getValue())
										+ linkToRoom(entry.getKey()) + ": "
										+ entry.getValue()
												.map(availability -> availability.getStatusAtStart().toString()
														+ (availability.getStatusAtStart() == FreeBusyStatus.FREE ? ""
																: (" Next available at " + formatNextFree(availability.getNextFree(), timeZone))))
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

	private String getTextForUsersStatus(List<String> emailAddresses, TimeZone timeZone) {
		return availabilityService.getAvailability(emailAddresses, new Date(), new Date()).entrySet().stream()
				.map(entry -> entry.getValue().map(availability -> {
					String returnText = optionalAvailabilityToEmoji(Optional.of(availability))
							+ linkToUser(entry.getKey()) + " is " + availability.getStatusAtStart() + ".";
					Date nextAvailable = availability.getNextFree();
					if (availability.getStatusAtStart() != FreeBusyStatus.FREE) {
						returnText += " Next available at " + formatNextFree(nextAvailable, timeZone) + ".";
					}
					return returnText;
				}).orElse("user " + entry.getKey() + " not found")).collect(Collectors.joining("\n"));
	}
	
	private String formatNextFree(Date nextFree, TimeZone timeZone){
		DateFormat displayDateFormat;
		if(LocalDateTime.ofInstant(nextFree.toInstant(), timeZone.toZoneId()).toLocalDate().equals(LocalDate.now(timeZone.toZoneId()))){
			displayDateFormat = new SimpleDateFormat(DISPLAY_AVAILABILITY_TODAY_FORMAT_PATTERN);
		}else{
			displayDateFormat = new SimpleDateFormat(DISPLAY_AVAILABILITY_NOT_TODAY_FORMAT_PATTERN);
		}
		displayDateFormat.setTimeZone(timeZone);
		return displayDateFormat.format(nextFree);
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
