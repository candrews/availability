package com.integralblue.availability.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.integralblue.availability.model.CalendarEvent;
import com.integralblue.availability.model.Availability;
import com.integralblue.availability.model.FreeBusyStatus;
import com.integralblue.availability.model.Room;
import com.integralblue.availability.model.RoomList;
import com.integralblue.availability.properties.ExchangeConnectionProperties;
import com.integralblue.availability.service.AvailabilityService;

import lombok.NonNull;
import lombok.SneakyThrows;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.availability.AvailabilityData;
import microsoft.exchange.webservices.data.core.enumeration.misc.error.ServiceError;
import microsoft.exchange.webservices.data.core.enumeration.property.LegacyFreeBusyStatus;
import microsoft.exchange.webservices.data.core.exception.service.remote.ServiceResponseException;
import microsoft.exchange.webservices.data.core.response.AttendeeAvailability;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.misc.availability.AttendeeInfo;
import microsoft.exchange.webservices.data.misc.availability.AvailabilityOptions;
import microsoft.exchange.webservices.data.misc.availability.GetUserAvailabilityResults;
import microsoft.exchange.webservices.data.misc.availability.TimeWindow;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.availability.Suggestion;
import microsoft.exchange.webservices.data.property.complex.availability.TimeSuggestion;

@Service
public class ExchangeAvailabilityService implements AvailabilityService {
	@Autowired
	private ExchangeConnectionProperties exchangeConnectionProperties;

	@Override
	@SneakyThrows
	public Optional<Availability> getAvailability(@NonNull String emailAddress, @NonNull Date start, @NonNull Date end) {
		Assert.isTrue(! start.after(end), "start must not be after end");
		final ExchangeService exchangeService = getExchangeService();
		final List<AttendeeInfo> attendees = Arrays.asList(new AttendeeInfo[] { new AttendeeInfo(emailAddress) });

		final AvailabilityOptions availabilityOptions = new AvailabilityOptions();
		availabilityOptions.setMeetingDuration(30);

		// minimum time frame allowed by API is 24 hours
		final GetUserAvailabilityResults results = exchangeService.getUserAvailability(attendees,
				new TimeWindow(start, end.before(DateUtils.addDays(start, 1))?DateUtils.addDays(start, 1):end), AvailabilityData.FreeBusyAndSuggestions,availabilityOptions);

		Assert.isTrue(results.getAttendeesAvailability().getCount() == 1);
		AttendeeAvailability attendeeAvailability;
		try {
			attendeeAvailability = results.getAttendeesAvailability().getResponseAtIndex(0);
			attendeeAvailability.throwIfNecessary();
		} catch (ServiceResponseException e) {
			if (e.getErrorCode() == ServiceError.ErrorMailRecipientNotFound) {
				return Optional.empty();
			} else {
				throw e;
			}
		}
		FreeBusyStatus statusAtStart = FreeBusyStatus.FREE;
		final List<CalendarEvent> calendarEvents = new ArrayList<>();
		for (final microsoft.exchange.webservices.data.property.complex.availability.CalendarEvent calendarEvent : attendeeAvailability.getCalendarEvents()) {
			if(start.compareTo(calendarEvent.getEndTime()) < 0 && calendarEvent.getStartTime().compareTo(start) <= 0){
				switch (calendarEvent.getFreeBusyStatus()) {
				case Busy:
					statusAtStart = FreeBusyStatus.BUSY;
					break;
				case Free:
					// do nothing
					break;
				case NoData:
					// do nothing
					break;
				case OOF:
					// do nothing
					break;
				case Tentative:
					if(statusAtStart == FreeBusyStatus.FREE){
						statusAtStart = FreeBusyStatus.TENTATIVE;
					}
					break;
				}
			}
			
			if(start.compareTo(calendarEvent.getEndTime()) < 0 && calendarEvent.getStartTime().compareTo(end) < 0){
				calendarEvents.add(
						CalendarEvent.builder()
						.start(calendarEvent.getStartTime())
						.end(calendarEvent.getEndTime())
						.status(legacyFreeBusyStatusToFreeBusyStatus(calendarEvent.getFreeBusyStatus()))
						.location(calendarEvent.getDetails()==null?null:calendarEvent.getDetails().getLocation())
						.subject(calendarEvent.getDetails()==null?null:calendarEvent.getDetails().getSubject())
					.build());
			}
		}
		
		Date nextFree = null;
		for(final Suggestion suggestion : results.getSuggestions()){
			for(final TimeSuggestion timeSuggestion : suggestion.getTimeSuggestions()){
				if(nextFree==null || nextFree.after(timeSuggestion.getMeetingTime())){
					nextFree = timeSuggestion.getMeetingTime();
				}
			}
		}

		return Optional.of(Availability.builder().statusAtStart(statusAtStart).nextFree(nextFree).calendarEvents(Collections.unmodifiableList(calendarEvents)).build());
	}

	@SneakyThrows
	private ExchangeService getExchangeService() {
		final ExchangeService exchangeService = new ExchangeService();
		exchangeService.setCredentials(new WebCredentials(exchangeConnectionProperties.getCredentials().getUsername(), exchangeConnectionProperties.getCredentials().getPassword(),exchangeConnectionProperties.getCredentials().getDomain()));
		exchangeService.setUrl(exchangeConnectionProperties.getUri());
		return exchangeService;
	}

	@SneakyThrows
	@Override
	public Set<RoomList> getRoomLists() {
		final Set<RoomList> roomLists = new HashSet<>();
		final ExchangeService exchangeService = getExchangeService();
		for(EmailAddress emailAddress : exchangeService.getRoomLists()){
			roomLists.add(RoomList.builder().emailAddress(emailAddress.getAddress()).name(emailAddress.getName()).build());
		}
		return Collections.unmodifiableSet(roomLists);
	}

	@SneakyThrows
	@Override
	public Optional<Set<Room>> getRooms(@NonNull String roomListEmailAddress) {
		final Set<Room> roomLists = new HashSet<>();
		final ExchangeService exchangeService = getExchangeService();
		for(EmailAddress emailAddress : exchangeService.getRooms(new EmailAddress(roomListEmailAddress))){
			roomLists.add(Room.builder().emailAddress(emailAddress.getAddress()).name(emailAddress.getName()).build());
		}
		return Optional.of(Collections.unmodifiableSet(roomLists));
	}
	
	private static FreeBusyStatus legacyFreeBusyStatusToFreeBusyStatus(@NonNull LegacyFreeBusyStatus legacyFreeBusyStatus){
		switch(legacyFreeBusyStatus){
		case Busy:
			return FreeBusyStatus.BUSY;
		case Free:
			return FreeBusyStatus.FREE;
		case Tentative:
			return FreeBusyStatus.TENTATIVE;
		default:
			return FreeBusyStatus.FREE;
		}
	}
}
