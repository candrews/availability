package com.integralblue.availability;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.web.WebAppConfiguration;

import com.integralblue.availability.AvailabilityApplication;

import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AvailabilityApplication.class)
@WebAppConfiguration
@TestPropertySource(locations="classpath:test.properties")
public class AvailabilityApplicationTests {

	@Test
	public void contextLoads() {
	}

}
