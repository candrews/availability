package com.integralblue.availability.controller;

import java.net.URI;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.integralblue.availability.model.FreeBusyResponse;
import com.integralblue.availability.service.AggregateAvailabilityService;

import lombok.NonNull;

@Controller
public class AvailabilityController {
	private static final String DEFAULT_FREE_URL = "/images/free.png";
	private static final String DEFAULT_BUSY_URL = "/images/busy.png";
	private static final String DEFAULT_TENTATIVE_URL = "/images/tentative.png";
	
	@Autowired
	private AggregateAvailabilityService aggregateAvailabilityService;
	
	@RequestMapping(value="/user/{username}/availability", produces=MediaType.APPLICATION_JSON_VALUE,method=RequestMethod.GET)
	public ResponseEntity<FreeBusyResponse> getAvailability(@PathVariable String username){
		final Optional<FreeBusyResponse> optionalAvailability = aggregateAvailabilityService.getAvailability(username);
		if(optionalAvailability.isPresent()){
			return new ResponseEntity<FreeBusyResponse>(optionalAvailability.get(), HttpStatus.OK);
		}else{
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@RequestMapping(value="/user/{username}/availability",method=RequestMethod.GET)
	public ResponseEntity<Void> getAvailability(@NonNull HttpServletResponse response, @PathVariable String username, @RequestParam(required=false,value="free") String freeUrl, @RequestParam(required=false,value="busy") String busyUrl, @RequestParam(required=false,value="tentative") String tentativeUrl ){
		final Optional<FreeBusyResponse> optionalAvailability = aggregateAvailabilityService.getAvailability(username);
		if(optionalAvailability.isPresent()){
			String url;
			switch(optionalAvailability.get().getFreeBusyStatus()){
			case FREE:
				url=response.encodeURL(StringUtils.isEmpty(freeUrl)?DEFAULT_FREE_URL:freeUrl);
				break;
			case BUSY:
				url=response.encodeURL(StringUtils.isEmpty(busyUrl)?DEFAULT_BUSY_URL:busyUrl);
				break;
			case TENTATIVE:
				url=response.encodeURL(StringUtils.isEmpty(tentativeUrl)?DEFAULT_TENTATIVE_URL:tentativeUrl);
				break;
			default:
				throw new IllegalStateException();
			}
			return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(URI.create(url)).build();
		}else{
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
}
