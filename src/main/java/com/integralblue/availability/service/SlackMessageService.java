package com.integralblue.availability.service;

import com.ullink.slack.simpleslackapi.SlackUser;

/**
 * Responsible for accepting a slack message, performing an action, and generating a response
 *
 */
public interface SlackMessageService {
	String respondToMessage(SlackUser messageSender, String message);
}
