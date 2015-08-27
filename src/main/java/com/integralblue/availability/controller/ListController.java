package com.integralblue.availability.controller;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.integralblue.availability.NotFoundException;
import com.integralblue.availability.service.AvailabilityService;

@Controller
public class ListController {
	
	@Autowired
	@Qualifier("exchangeAvailabilityService")
	private AvailabilityService availabilityService;
	
	@RequestMapping(value="/list/{listName}/availability",method=RequestMethod.GET,produces=MediaType.TEXT_HTML_VALUE)
	public String getAvailabilityView(Model model, @PathVariable("listName") String listName){
		return availabilityService.getRooms(listName)
			.map(rooms ->
				rooms.stream().map(room ->
					room.getEmailAddress()).collect(Collectors.joining(",","forward:/user/","/availability")))
			.orElseThrow(NotFoundException::new);
	}
}
