package com.integralblue.availability.service;

import java.util.Optional;

import com.integralblue.availability.model.FreeBusyResponse;

public interface AvailabilityService {
	/**
	 * @param username
	 * @return empty optional if the given username was not found
	 */
	Optional<FreeBusyResponse> getAvailability(String username);
}
