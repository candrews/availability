package com.integralblue.availability.controller;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.integralblue.availability.model.RoomList;
import com.integralblue.availability.service.AvailabilityService;

@Controller
public class RoomListsController {
	@Autowired
	@Qualifier("exchangeAvailabilityService")
	private AvailabilityService availabilityService;
	
	@RequestMapping("/roomLists")
	public String roomLists(){
		Set<RoomList> roomLists = availabilityService.getRoomLists();
		return "";
	}
}
