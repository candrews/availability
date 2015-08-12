package com.integralblue.availability.service.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.integralblue.availability.model.FreeBusyResponse;
import com.integralblue.availability.model.FreeBusyStatus;
import com.integralblue.availability.properties.ExchangeConnectionProperties;
import com.integralblue.availability.service.AvailabilityService;

import lombok.NonNull;
import lombok.SneakyThrows;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.availability.AvailabilityData;
import microsoft.exchange.webservices.data.core.enumeration.misc.error.ServiceError;
import microsoft.exchange.webservices.data.core.exception.service.remote.ServiceResponseException;
import microsoft.exchange.webservices.data.core.response.AttendeeAvailability;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.misc.availability.AttendeeInfo;
import microsoft.exchange.webservices.data.misc.availability.AvailabilityOptions;
import microsoft.exchange.webservices.data.misc.availability.GetUserAvailabilityResults;
import microsoft.exchange.webservices.data.misc.availability.TimeWindow;
import microsoft.exchange.webservices.data.property.complex.availability.Suggestion;
import microsoft.exchange.webservices.data.property.complex.availability.TimeSuggestion;

@Service
public class ExchangeAvailabilityService implements AvailabilityService {
	@Autowired
	private ExchangeConnectionProperties exchangeConnectionProperties;

	@Override
	@SneakyThrows
	public Optional<FreeBusyResponse> getAvailability(@NonNull String username) {
		final ExchangeService exchangeService = getExchangeService();
		final List<AttendeeInfo> attendees = Arrays.asList(new AttendeeInfo[] { new AttendeeInfo(username) });

		// minimum time frame allowed by API is 24 hours
		final Date start = new Date();
		final Date end = DateUtils.addDays(new Date(), 1);

		final AvailabilityOptions availabilityOptions = new AvailabilityOptions();
		availabilityOptions.setMeetingDuration(30);
		
		final GetUserAvailabilityResults results = exchangeService.getUserAvailability(attendees,
				new TimeWindow(start, end), AvailabilityData.FreeBusyAndSuggestions,availabilityOptions);

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
		final FreeBusyStatus status = aggregateStatus(attendeeAvailability.getCalendarEvents());
		
		Date nextFree = null;
		for(final Suggestion suggestion : results.getSuggestions()){
			for(final TimeSuggestion timeSuggestion : suggestion.getTimeSuggestions()){
				if(nextFree==null || nextFree.after(timeSuggestion.getMeetingTime())){
					nextFree = timeSuggestion.getMeetingTime();
				}
			}
		}

		return Optional.of(FreeBusyResponse.builder().freeBusyStatus(status).nextFree(nextFree).build());
	}

	private FreeBusyStatus aggregateStatus(Collection<microsoft.exchange.webservices.data.property.complex.availability.CalendarEvent> calendarEvents) {
		final Date now = new Date();
		FreeBusyStatus aggregateStatus = FreeBusyStatus.FREE;
		for (final microsoft.exchange.webservices.data.property.complex.availability.CalendarEvent calendarEvent : calendarEvents) {
			if(now.after(calendarEvent.getStartTime()) && now.before(calendarEvent.getEndTime())){
				switch (calendarEvent.getFreeBusyStatus()) {
				case Busy:
					return FreeBusyStatus.BUSY;
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
					aggregateStatus = FreeBusyStatus.TENTATIVE;
				}
			}
		}
		return aggregateStatus;
	}

	@SneakyThrows
	private ExchangeService getExchangeService() {
		final ExchangeService exchangeService = new ExchangeService();
		exchangeService.setCredentials(new WebCredentials(exchangeConnectionProperties.getCredentials().getUsername(), exchangeConnectionProperties.getCredentials().getPassword(),exchangeConnectionProperties.getCredentials().getDomain()));
		exchangeService.setUrl(exchangeConnectionProperties.getUri());
		return exchangeService;
	}
}
