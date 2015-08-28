package com.integralblue.availability.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.integralblue.availability.model.Availability;
import com.integralblue.availability.model.FreeBusyStatus;
import com.integralblue.availability.model.Room;
import com.integralblue.availability.model.slack.SlackMessageModelFactory;
import com.integralblue.availability.model.slack.SlashSlackMessage;
import com.integralblue.availability.model.slack.parser.ParsableSlackMessage;
import com.integralblue.availability.model.slack.parser.SlackCommand;
import com.integralblue.availability.model.slack.parser.SlashSlackParsingStrategy;
import com.integralblue.availability.properties.SlackProperties;
import com.integralblue.availability.service.AvailabilityService;

@Controller
@Slf4j
public class SlackController {
	@Autowired
	protected SlackProperties slackProperties;
	
	@Autowired
	@Qualifier("slackAvailabilityService")
	private AvailabilityService availabilityService;
	
	@Autowired
	List<SlashSlackParsingStrategy> slackParsingStrategies;
	private final Map<String, SlashSlackParsingStrategy> parsingStrategies = new HashMap<String, SlashSlackParsingStrategy>();

	@PostConstruct
	private void afterPropertiesSet() {
		for (SlashSlackParsingStrategy s : slackParsingStrategies)
			parsingStrategies.put(s.getParsingIdentifier(), s);
	}
	
	@RequestMapping(value="/slack/availability",method=RequestMethod.GET,produces=MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> getAvailability(@RequestParam Map<String,String> allRequestParams) {
		log.debug("Incoming Slack request: " + allRequestParams.toString());
		if (StringUtils.isEmpty(slackProperties.getParsingStrategyIdentifier()) || StringUtils.isEmpty(slackProperties.getSlashCommandToken())) {
			log.error("Parsing strategy and token properties must not be empty if making Slack requests. Check .properties files.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
		}
			
		Optional<SlashSlackMessage> msg = SlackMessageModelFactory.getSlackMessage(allRequestParams);
		if (!msg.isPresent()) {
			//TODO: thymeleaf automatically intercepts exceptions and tries to redirect to an error template page
			//		exceptions should be allowed to be thrown in this controller; thymeleaf will probably have to be disabled
			//		at some point for this controller/url pattern
			return ResponseEntity.badRequest().body("");
		} else if (!msg.get().getToken().equals(slackProperties.getSlashCommandToken())) {
			log.debug("Token in request " + msg.get().getToken() + " did not match configured token.");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("");
		}
		
		try {
			ParsableSlackMessage message = msg.get();
			SlashSlackParsingStrategy parser = this.parsingStrategies.get(slackProperties.getParsingStrategyIdentifier());
			if (parser == null)
				throw new IllegalArgumentException("A SlashSlackParsingStrategy of type " + slackProperties.getParsingStrategyIdentifier() + " does not exist.");
			
			String returnText = "";
			final List<String> messages = new ArrayList<>();
			SlackCommand behavior = parser.getIntention(message);
			
			if (behavior == SlackCommand.USER_STATUS) {
				returnText = getTextForUserStatus(message, parser);
			} else if (behavior == SlackCommand.ROOM_STATUS_BY_OFFICE) {
				returnText = getTextForRoomStatusByOffice(message, parser, messages);
			} else if (behavior == SlackCommand.UNKNOWN){
				returnText = "I don't understand _" + message.getParameters().get() + "_. Right now just mention the user, like @user.name";
			} else {
				throw new IllegalArgumentException("All SlackCommands must be handled.");
			}
			return ResponseEntity.ok(returnText);
		} catch (Exception e) {
			log.error("Error while trying to process " + msg.get().getCommand() + " " + msg.get().getText(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
		}
	}

	private String getTextForRoomStatusByOffice(ParsableSlackMessage message,
			SlashSlackParsingStrategy parser, final List<String> messages) {
		String returnText = "";
		String office = parser.getRoomLocation(message);
		//this logic of passing just the office name will likely need to change
		//once we have actual room lists. as of right now the list of rooms are
		//hard-coded in a properties file by office, i.e. the boston office or nyc office
		Map<Room, FreeBusyStatus> rooms = availabilityService.getRoomsStatus(office.toLowerCase(), new Date(), new Date());
		messages.add("*" + office + "*\n");
		rooms.forEach((room,status) -> {
			messages.add("> " + room.getName() + ": " + status.toString() + "\n");
		});
		
		for (String s : messages) {
			returnText += s;
		}
		return returnText;
	}

	private String getTextForUserStatus(ParsableSlackMessage message,
			SlashSlackParsingStrategy parser) {
		String returnText;
		String email = parser.getUserEmailAddresses(message);
		Optional<Availability> availability = availabilityService.getAvailability(email, new Date(), new Date());
		Date nextAvailable = availability.get().getNextFree();
		if (availability.get().getStatusAtStart() == FreeBusyStatus.BUSY)
			returnText = email + " is busy, but will be available at " + nextAvailable + ".";
		else
			returnText = email + " is not busy.";
		return returnText;
	}
}
