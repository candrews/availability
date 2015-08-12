package com.integralblue.availability.service;

import java.util.Optional;
import java.util.Set;

import com.integralblue.availability.model.FreeBusyResponse;
import com.integralblue.availability.model.RoomList;

public interface AggregateAvailabilityService {
	/**
	 * @param username
	 * @return empty optional if the given username was not found
	 */
	Optional<FreeBusyResponse> getAvailability(String username);
	
	Set<RoomList> getRoomLists();
}
