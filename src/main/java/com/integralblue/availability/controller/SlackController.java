package com.integralblue.availability.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.integralblue.availability.model.SlackMessage;

@Controller
public class SlackController {
	@RequestMapping(value="/slack/availability",method=RequestMethod.GET,produces=MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> getAvailability(@RequestBody(required=true) SlackMessage slackRequest) {
		return ResponseEntity.ok("Hello world!");
	}
}
