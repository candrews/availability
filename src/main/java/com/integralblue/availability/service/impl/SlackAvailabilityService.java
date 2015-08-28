package com.integralblue.availability.service.impl;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.integralblue.availability.model.Availability;
import com.integralblue.availability.model.FreeBusyStatus;
import com.integralblue.availability.model.Room;
import com.integralblue.availability.model.RoomList;
import com.integralblue.availability.properties.ExchangeConnectionProperties;
import com.integralblue.availability.service.AvailabilityService;

@Service
public class SlackAvailabilityService implements AvailabilityService {
	@Autowired
	@Qualifier("exchangeAvailabilityService")
	private AvailabilityService exchangeAvailabilityService;
	
	@Override
	public Optional<Availability> getAvailability(String emailAddress,
			Date startDate, Date endDate) {
		return exchangeAvailabilityService.getAvailability(emailAddress, startDate, endDate);
	}

	@Override
	public Set<RoomList> getRoomLists() {
		return exchangeAvailabilityService.getRoomLists();
	}

	@Override
	public Optional<Set<Room>> getRooms(String roomListEmailAddress) {
		return exchangeAvailabilityService.getRooms(roomListEmailAddress);
	}

	@Override
	public Map<Room, FreeBusyStatus> getRoomsStatus(String roomListEmailAddress, Date fromDate, Date toDate) {
		return exchangeAvailabilityService.getRoomsStatus(roomListEmailAddress, fromDate, toDate);
	}
}
