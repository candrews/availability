package com.integralblue.availability.service.impl;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import com.integralblue.availability.service.SlackMessageService;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;

@ConditionalOnBean(SlackSession.class)
@Service
public class SlackBotServiceImpl {
	@Autowired
	private SlackSession slackSession;
	
	@Autowired
	private SlackMessageService slackMessageService;
	
	@PostConstruct
	public void afterPropertiesSet(){
		slackSession.addMessagePostedListener( (SlackMessagePosted event, SlackSession session) -> {
			// don't pay any attention to messages we sent to avoid infinite loops
			if(!event.getSender().getId().equals(session.sessionPersona().getId())){
				if(StringUtils.startsWithIgnoreCase(event.getMessageContent(),"<@" + session.sessionPersona().getId() + ">")){
					// the message is addressed to this bot
					String message = event.getMessageContent().substring(session.sessionPersona().getId().length()+3);
					if(message.startsWith(":")){
						message = message.substring(1);
					}
					message = message.trim();
					session.sendMessage(event.getChannel(), slackMessageService.respondToMessage(event.getSender(), message), null);
				}else if(event.getChannel().isDirect()){
					// this is a direct message, so the bot should reply
					session.sendMessage(event.getChannel(), slackMessageService.respondToMessage(event.getSender(), event.getMessageContent()), null);
				}else if(StringUtils.containsIgnoreCase(event.getMessageContent(),"<@" + session.sessionPersona().getId() + ">")){
					// this bot is mentioned, so it should say something
					session.sendMessage(event.getChannel(), "How can I help you? You can send messages to @" + session.sessionPersona().getUserName() +  " or open a direct message chat. For help, say '@" + session.sessionPersona().getUserName() +  "' or send a direct message with the text 'help'", null);
				}
			}
		});
	}
}
