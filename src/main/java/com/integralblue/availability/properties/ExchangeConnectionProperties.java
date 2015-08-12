package com.integralblue.availability.properties;

import java.net.URI;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Component
@ConfigurationProperties(prefix="exchange")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExchangeConnectionProperties {

	@Getter
	@Setter
	@ToString
	@EqualsAndHashCode
	public static class Credentials {
		String domain;
		
		@NotBlank
		String username;
		
		@NotBlank
		String password;
	}

	@NestedConfigurationProperty
	@NotNull
	Credentials credentials;

	@NotNull
	URI uri;
}
