package com.integralblue.availability.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.integralblue.availability.model.FreeBusyResponse;
import com.integralblue.availability.model.RoomList;
import com.integralblue.availability.service.AggregateAvailabilityService;
import com.integralblue.availability.service.AvailabilityService;

import lombok.NonNull;

@Service
public class AggregateAvailabilityServiceImpl implements AggregateAvailabilityService {
	@Autowired
	private Collection<AvailabilityService> availabilityServices;

	@Override
	public Optional<FreeBusyResponse> getAvailability(@NonNull final String username) {
		for(final AvailabilityService availabilityService : availabilityServices){
			Optional<FreeBusyResponse> optionalAvailability = availabilityService.getAvailability(username);
			if(optionalAvailability.isPresent()){
				return optionalAvailability;
			}
		}
		return Optional.empty();
	}

	@Override
	public Set<RoomList> getRoomLists() {
		final Set<RoomList> roomLists = new HashSet<>();
		for(final AvailabilityService availabilityService : availabilityServices){
			roomLists.addAll(availabilityService.getRoomLists());
		}
		return Collections.unmodifiableSet(roomLists);
	}
	
	
}
