package com.integralblue.availability.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.integralblue.availability.model.Availability;
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
	
	/** Get the availability for each provided email address
	 * @param emailAddresses
	 * @param startDate
	 * @param endDate
	 * @return map of email address to availability. The map's keys will be the same as the provided email addresses
	 */
	Map<String, Optional<Availability>> getAvailability(List<String> emailAddresses, Date startDate, Date endDate);
	
	/** Get the room lists
	 * @return
	 */
	Set<RoomList> getRoomLists();
	
	/** Get the rooms in a room list
	 * @param roomListEmailAddress
	 * @return empty optional if the given roomListEmailAddress was not found
	 */
	Optional<Set<Room>> getRooms(String roomListEmailAddress);
	
	Optional<Map<Room, Optional<Availability>>> getRoomListAvailability(String roomListEmailAddress);
}
