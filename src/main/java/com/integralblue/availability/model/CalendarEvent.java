package com.integralblue.availability.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class CalendarEvent {
	String id;
	String subject;
	String location;
	Date start;
	Date end;
	
	@NotNull @NonNull FreeBusyStatus status;
}
