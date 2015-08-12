package com.integralblue.availability.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Room {
	String name;
	String emailAddress;
}
