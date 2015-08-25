package com.integralblue.availability.controller;

import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.integralblue.availability.model.slack.SlackMessageModelFactory;
import com.integralblue.availability.model.slack.SlashSlackMessage;

@Controller
@Slf4j
public class SlackController {
	@RequestMapping(value="/slack/availability",method=RequestMethod.GET,produces=MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> getAvailability(@RequestParam Map<String,String> allRequestParams) {
		log.debug("Incoming Slack request: " + allRequestParams.toString());
		Optional<SlashSlackMessage> msg = SlackMessageModelFactory.getSlackMessage(allRequestParams);
		if (!msg.isPresent())
			//TODO: thymeleaf automatically intercepts exceptions and tries to redirect to an error template page
			//		exceptions should be allowed to be thrown in this controller; thymeleaf will probably have to be disabled
			//		at some point for this controller/url pattern
			return ResponseEntity.badRequest().body("");
		
//		SlackSession session = SlackSessionFactory.createWebSocketSlackSession(authToken);
		return ResponseEntity.ok("Hello world! " + msg.get());
	}
}
