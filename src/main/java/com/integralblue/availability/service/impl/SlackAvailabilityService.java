package com.integralblue.availability.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.integralblue.availability.service.AvailabilityService;

import lombok.experimental.Delegate;

@Service
public class SlackAvailabilityService implements AvailabilityService {
	@Autowired
	@Qualifier("exchangeAvailabilityService")
	@Delegate
	private AvailabilityService exchangeAvailabilityService;
}
