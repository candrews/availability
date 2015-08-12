package com.integralblue.availability.model;

import java.util.Date;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Builder
@Value
public class FreeBusyResponse {
	@NonNull FreeBusyStatus freeBusyStatus;
	Date nextFree;
}
