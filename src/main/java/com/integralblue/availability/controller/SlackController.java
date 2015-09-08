package com.integralblue.availability.controller;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.integralblue.availability.model.slack.SlashSlackMessage;
import com.integralblue.availability.properties.SlackProperties;
import com.integralblue.availability.service.SlackMessageService;
import com.ullink.slack.simpleslackapi.SlackSession;

import lombok.extern.slf4j.Slf4j;

@ConditionalOnProperty(value = "slack.slashCommandToken")
@Controller
@Slf4j
public class SlackController {
	@Autowired
	private SlackProperties slackProperties;
	
	@Autowired
	private SlackMessageService slackMessageService;
	
	@Autowired(required=false)
	private SlackSession slackSession;
	
	@RequestMapping(value="/slack/availability",method=RequestMethod.GET,produces=MediaType.TEXT_PLAIN_VALUE)
	@ResponseBody
	public String getAvailability(@Valid SlashSlackMessage message, Model model) {
		log.debug("Incoming Slack request: {}", message.toString());
		if (!message.getToken().equals(slackProperties.getSlashCommandToken())) {
			throw new AccessDeniedException("Token in request " + message.getToken() + " did not match configured token.");
		}
		return slackMessageService.respondToMessage(Optional.of(slackSession).map(slackSession -> slackSession.findUserById(message.getUserId())), message.getText());
	}
}
