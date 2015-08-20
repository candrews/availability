package com.integralblue.availability.model.slack;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class SlackMessage {
	String userId;
	String text;
}
