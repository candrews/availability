package com.integralblue.availability.service;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

import com.integralblue.availability.model.Availability;
import com.integralblue.availability.model.RoomList;

public interface AggregateAvailabilityService {
	/**
	 * @param emailAddress
	 * @param start
	 * @param end
	 * @return empty optional if the given emailAddress was not found
	 */
	Optional<Availability> getAvailability(String emailAddress, Date start, Date end);
	
	Set<RoomList> getRoomLists();
}
