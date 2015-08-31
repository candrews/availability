package com.integralblue.availability.model.slack.parser;

import java.util.List;
import java.util.Optional;

public interface ParsableSlackMessage {
	Optional<List<String>> getParameters();
	String getCommand();
}
