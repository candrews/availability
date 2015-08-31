package com.integralblue.availability.controller;

import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.integralblue.availability.NotFoundException;
import com.integralblue.availability.model.Availability;
import com.integralblue.availability.service.AvailabilityService;

import lombok.NonNull;

@Controller
public class AvailabilityController {
	private static final String DEFAULT_FREE_URL = "/images/free.png";
	private static final String DEFAULT_BUSY_URL = "/images/busy.png";
	private static final String DEFAULT_TENTATIVE_URL = "/images/tentative.png";
	
	@Autowired
	@Qualifier("exchangeAvailabilityService")
	private AvailabilityService availabilityService;
	
	@RequestMapping(value="/user/{emailAddresses}/availability",method=RequestMethod.GET,produces=MediaType.TEXT_HTML_VALUE)
	public String getAvailabilityView(Model model, @PathVariable("emailAddresses") String[] emailAddresses){
		model.addAttribute("emailAddresses", Arrays.asList(emailAddresses));
		return "availability";
	}
	
	@ResponseBody
	@RequestMapping(value="/user/{emailAddress}/availability",method=RequestMethod.GET,produces=MediaType.APPLICATION_JSON_VALUE)
	public Availability getAvailability(@PathVariable String emailAddress, @RequestParam(value="start") @DateTimeFormat(iso=ISO.DATE_TIME) Date start, @RequestParam(value="end") @DateTimeFormat(iso=ISO.DATE_TIME) Date end, @RequestParam Integer timezoneOffset){
		Assert.isTrue(!start.after(end), "start cannot be after end");
		start = DateUtils.addMinutes(start, timezoneOffset);
		end = DateUtils.addMinutes(end, timezoneOffset);
		return availabilityService.getAvailability(emailAddress, start, end).orElseThrow(NotFoundException::new);
	}
	
	@RequestMapping(value="/user/{emailAddress}/availability/redirect",method=RequestMethod.GET)
	public ResponseEntity<Void> getAvailability(@NonNull HttpServletResponse response, @PathVariable String emailAddress, @RequestParam(required=false,value="free",defaultValue=DEFAULT_FREE_URL) String freeUrl, @RequestParam(value="busy",defaultValue=DEFAULT_BUSY_URL) String busyUrl, @RequestParam(value="tentative",defaultValue=DEFAULT_TENTATIVE_URL) String tentativeUrl, @RequestParam(value="date",required=false) Optional<Date> date){
		final Optional<Availability> optionalAvailability = availabilityService.getAvailability(emailAddress, date.orElse(new Date()), date.orElse(new Date()));
		if(optionalAvailability.isPresent()){
			String url;
			switch(optionalAvailability.get().getStatusAtStart()){
			case FREE:
				url=freeUrl;
				break;
			case BUSY:
				url=busyUrl;
				break;
			case TENTATIVE:
				url=tentativeUrl;
				break;
			default:
				throw new IllegalStateException();
			}
			return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(URI.create(url)).build();
		}else{
			throw new NotFoundException();
		}
	}
}
