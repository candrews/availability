package com.integralblue.availability.model;

import lombok.Builder;
import lombok.Value;

/** A room list is a container for a list of rooms
 */
@Value
@Builder
public class RoomList {
	String name;
	String emailAddress;
}
