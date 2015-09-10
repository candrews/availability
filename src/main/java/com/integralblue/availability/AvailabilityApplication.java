package com.integralblue.availability;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class AvailabilityApplication {
	static {
		Security.addProvider(new BouncyCastleProvider());
	}

    public static void main(String[] args) {
        SpringApplication.run(AvailabilityApplication.class, args);
    }
}
