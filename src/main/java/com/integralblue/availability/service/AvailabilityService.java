package com.integralblue.availability.service;

import java.util.Optional;
import java.util.Set;

import com.integralblue.availability.model.FreeBusyResponse;
import com.integralblue.availability.model.Room;
import com.integralblue.availability.model.RoomList;

public interface AvailabilityService {
	/**
	 * @param username
	 * @return empty optional if the given username was not found
	 */
	Optional<FreeBusyResponse> getAvailability(String username);
	
	/** Get the room lists
	 * @return
	 */
	Set<RoomList> getRoomLists();
	
	/** Get the rooms in a room list
	 * @param roomListEmailAddress
	 * @return
	 */
	Set<Room> getRooms(String roomListEmailAddress);
}
