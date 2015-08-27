package com.integralblue.availability.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

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
import com.integralblue.availability.model.RoomList;
import com.integralblue.availability.model.slack.SlackMessageModelFactory;
import com.integralblue.availability.model.slack.SlashSlackMessage;
import com.integralblue.availability.service.AvailabilityService;

@Controller
@Slf4j
public class SlackController {
	private static final String TOKEN = "S2KjclFypcEsHpeJU0rbWCY6";
	
	@Autowired
	@Qualifier("slackAvailabilityService")
	private AvailabilityService availabilityService;
	
	@RequestMapping(value="/slack/availability",method=RequestMethod.GET,produces=MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> getAvailability(@RequestParam Map<String,String> allRequestParams) {
		log.debug("Incoming Slack request: " + allRequestParams.toString());
		Optional<SlashSlackMessage> msg = SlackMessageModelFactory.getSlackMessage(allRequestParams);
		if (!msg.isPresent())
			//TODO: thymeleaf automatically intercepts exceptions and tries to redirect to an error template page
			//		exceptions should be allowed to be thrown in this controller; thymeleaf will probably have to be disabled
			//		at some point for this controller/url pattern
			return ResponseEntity.badRequest().body("");
		else if (!msg.get().getToken().equals(TOKEN))
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("");
		
		try {
			SlashSlackMessage smsg = msg.get();
			String parameter = smsg.getText();
			String returnText = "";
			final List<String> messages = new ArrayList<>();
			if (parameter.startsWith("@")) {
				parameter = parameter.substring(1) + "@isobar.com";
				Optional<Availability> availability = availabilityService.getAvailability(parameter, new Date(), new Date());
				Date nextAvailable = availability.get().getNextFree();
				if (availability.get().getStatusAtStart() == FreeBusyStatus.BUSY)
					returnText = parameter + " is busy, but will be available at " + nextAvailable + ".";
				else
					returnText = parameter + " is not busy.";
			} else if (parameter.startsWith("rooms")) {
				Set<RoomList> rooms = new HashSet<>();
				String x = "bos.morphine@isobar.com,bos.boston@isobar.com,bos.mighty@isobar.com";
				String[] x2 = x.split(",");
				List<String> r = Arrays.asList(x2);
				for (String r2 : r) {
					RoomList rl = RoomList.builder().emailAddress(r2).name("Room Name: " + r2).build();
					rooms.add(rl);
				}
				Map<RoomList, FreeBusyStatus> boston = new HashMap<>(),
						chicago = new HashMap<>(), nyc = new HashMap<>()
						, lynda = new HashMap<>(), gtm = new HashMap<>(),
						other = new HashMap<>();
				for (RoomList room : rooms) {
					
					Map<RoomList, FreeBusyStatus> addTo;
					if (room.getEmailAddress().startsWith("bos."))
						addTo = boston;
					else if (room.getEmailAddress().startsWith("chi.cf."))
						addTo = chicago;
					else if (room.getEmailAddress().startsWith("nyc.cf."))
						addTo = nyc;
					else if (room.getEmailAddress().startsWith("lyn."))
						addTo = lynda;
					else if (room.getEmailAddress().startsWith("gtm."))
						addTo = gtm;
					else
						addTo = other;
					FreeBusyStatus status = availabilityService.getAvailability(room.getEmailAddress(), new Date(), new Date()).get().getStatusAtStart();
					addTo.put(room,  status);
				}
				
				messages.add("*Boston*\n");
				boston.forEach((room,status) -> {
					String roomName = room.getName().replaceAll("bos.", "").replaceAll("@roundarchisobar.com", "").replaceAll("@isobar.com", "");
					messages.add("> " + roomName + ": " + status.toString() + "\n");
				});
				messages.add("*Chicago*\n");
				chicago.forEach((room,status) -> {
					String roomName = room.getName().replaceAll("chi.cf.", "").replaceAll("@roundarchisobar.com", "").replaceAll("@isobar.com", "");
					messages.add("> " + roomName + ": " + status.toString() + "\n");
				});
				messages.add("*NYC*\n");
				nyc.forEach((room,status) -> {
					String roomName = room.getName().replaceAll("nyc.cf.", "").replaceAll("@roundarchisobar.com", "").replaceAll("@isobar.com", "");
					messages.add("> " + roomName + ": " + status.toString() + "\n");
				});
				messages.add("*Lynda*\n");
				lynda.forEach((room,status) -> {
					String roomName = room.getName().replaceAll("lyn.", "").replaceAll("@roundarchisobar.com", "").replaceAll("@isobar.com", "");
					roomName += "Room ";
					messages.add("> " + roomName + ": " + status.toString() + "\n");
				});
				messages.add("*GTM*\n");
				gtm.forEach((room,status) -> {
					String roomName = room.getName().replaceAll("gtm.", "").replaceAll("@roundarchisobar.com", "").replaceAll("@isobar.com", "");
					roomName += "GTM ";
					messages.add("> " + roomName + ": " + status.toString() + "\n");
				});
				messages.add("*Other*\n");
				gtm.forEach((room,status) -> {
					String roomName = room.getName().replaceAll("@roundarchisobar.com", "").replaceAll("@isobar.com", "");
					messages.add("> " + roomName + ": " + status.toString() + "\n");
				});
				for (String s : messages) {
					returnText += s;
				}
			} else {
				returnText = "I don't understand _" + parameter + "_. Right now just mention the user, like @alex.beardsley";
			}
			return ResponseEntity.ok(returnText);
		} catch (Exception e) {
			log.error("Error while trying to process " + msg.get().getCommand() + " " + msg.get().getText(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
		}
	}
}
