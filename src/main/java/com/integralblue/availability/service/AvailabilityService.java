package com.integralblue.availability.service;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.integralblue.availability.model.Availability;
import com.integralblue.availability.model.FreeBusyStatus;
import com.integralblue.availability.model.Room;
import com.integralblue.availability.model.RoomList;

public interface AvailabilityService {
	/**
	 * @param emailAddress
	 * @param startDate
	 * @param endDate
	 * @return empty optional if the given emailAddress was not found
	 */
	Optional<Availability> getAvailability(String emailAddress, Date startDate, Date endDate);
	
	/** Get the room lists
	 * @return
	 */
	Set<RoomList> getRoomLists();
	
	/** Get the rooms in a room list
	 * @param roomListEmailAddress
	 * @return
	 */
	Optional<Set<Room>> getRooms(String roomListEmailAddress);
	
	Map<Room, FreeBusyStatus> getCurrentRoomsStatus(String roomListEmailAddress);
}
