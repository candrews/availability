package com.integralblue.availability.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.integralblue.availability.model.slack.SlackMessageModelFactory;
import com.integralblue.availability.model.slack.SlashSlackMessage;

@RestController
public class SlackController {
	@RequestMapping(value="/slack/availability",method=RequestMethod.GET,produces=MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> getAvailability(Model model) {
		SlashSlackMessage msg = SlackMessageModelFactory.get(model, SlashSlackMessage.class);
		return ResponseEntity.ok("Hello world! " + msg.toString());
	}
}
