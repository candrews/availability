package com.integralblue.availability.service.impl;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.integralblue.availability.model.FreeBusyResponse;
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
	
	
}
