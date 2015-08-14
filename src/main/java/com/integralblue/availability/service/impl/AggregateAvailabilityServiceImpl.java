package com.integralblue.availability.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.integralblue.availability.model.Availability;
import com.integralblue.availability.model.RoomList;
import com.integralblue.availability.service.AggregateAvailabilityService;
import com.integralblue.availability.service.AvailabilityService;

import lombok.NonNull;

@Service
public class AggregateAvailabilityServiceImpl implements AggregateAvailabilityService {
	@Autowired
	private Collection<AvailabilityService> availabilityServices;

	@Override
	public Optional<Availability> getAvailability(@NonNull final String emailAddress, @NonNull Date start, @NonNull Date end) {
		Assert.isTrue(! start.after(end), "start must not be after end");
		for(final AvailabilityService availabilityService : availabilityServices){
			Optional<Availability> optionalAvailability = availabilityService.getAvailability(emailAddress, start, end);
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
