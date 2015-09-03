package com.integralblue.availability.properties;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.integralblue.availability.properties.ExchangeConnectionProperties.Credentials;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@SuppressWarnings("unused")
@Component
@ConfigurationProperties(prefix="application")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicationProperties {
	/**
	 * The application's base URL
	 */
	URI baseUri;
}
