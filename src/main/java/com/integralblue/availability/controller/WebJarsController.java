package com.integralblue.availability.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerMapping;
import org.webjars.RequireJS;
import org.webjars.WebJarAssetLocator;

import com.integralblue.availability.NotFoundException;

@Controller
public class WebJarsController {
	private final WebJarAssetLocator assetLocator = new WebJarAssetLocator();
	
	@ResponseBody
	@RequestMapping(value="/webjarslocator/{webjar}/**",method=RequestMethod.GET)
	public Resource locateWebjarAsset(@PathVariable String webjar, HttpServletRequest request) {
	    try {
	    	String mvcPrefix = "/webjarslocator/" + webjar + "/"; // This prefix must match the mapping path!
	    	String mvcPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
	        String fullPath = assetLocator.getFullPath(webjar, mvcPath.substring(mvcPrefix.length()));
	        return new ClassPathResource(fullPath);
	    } catch (Exception e) {
	       throw new NotFoundException();
	    }
	}
	
	@ResponseBody
	@RequestMapping(value = "/webjars.js", produces = "application/javascript",method=RequestMethod.GET)
	public String webjarjs() {
	    return RequireJS.getSetupJavaScript("/webjars/");
	}
}

