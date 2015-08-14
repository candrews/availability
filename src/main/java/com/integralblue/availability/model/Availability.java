package com.integralblue.availability.model;

import java.util.Date;
import java.util.List;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Builder
@Value
public class Availability {
	@NonNull FreeBusyStatus statusAtStart;
	Date nextFree;
	List<CalendarEvent> calendarEvents;
}
