package com.integralblue.availability.controller;

import java.util.Date;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.integralblue.availability.NotFoundException;
import com.integralblue.availability.service.AvailabilityService;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import springfox.documentation.annotations.ApiIgnore;

/**
 * This controller returns event data in the format specified by FullCalendar.
 * The output of this controller should *only* be used by FullCalendar - it is not intended for use by anything else and is not considered part of the API.
 *
 */
@Controller
@ApiIgnore
public class FullCalendarDataController {
	
	/** @see <a href="http://fullcalendar.io/docs/event_data/Event_Object/">Event Object</a>
	 */
	@Value @Builder
	
	private static class FullCalendarEvent {
		String id;
		@NotNull @NonNull String title;
		boolean allDay;
		@NotNull @NonNull Date start;
		@NotNull @NonNull Date end;
		String url;
		String resourceId;
	}
	
	@Autowired
	@Qualifier("exchangeAvailabilityService")
	private AvailabilityService availabilityService;

	@ResponseBody
	@RequestMapping(value="/user/{emailAddress}/availability/fullcalendar",method=RequestMethod.GET,produces=MediaType.APPLICATION_JSON_VALUE)
	public FullCalendarEvent[] getFullCalendarEvents(@PathVariable String emailAddress, @RequestParam(value="start") @DateTimeFormat(iso=ISO.DATE) Date startDate, @RequestParam(value="end") @DateTimeFormat(iso=ISO.DATE) Date endDate){
		return availabilityService.getAvailability(emailAddress, startDate, endDate)
				.map(availability -> 
					availability.getCalendarEvents().stream().map(calendarEvent ->
						FullCalendarEvent.builder().resourceId(emailAddress).start(calendarEvent.getStart()).end(calendarEvent.getEnd()).id(calendarEvent.getId()).title(Optional.ofNullable(calendarEvent.getSubject()).orElse("(unknown)")).build()).toArray(FullCalendarEvent[]::new))
				.orElseThrow(NotFoundException::new);
	}
	
}
