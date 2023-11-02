package com.dws.challenge.exception;

import org.springframework.http.HttpStatus;

public class OverDraftException extends RuntimeException {

	private final String errorCode;

	private final HttpStatus httpStatus;

	public OverDraftException(String message, String errorCode) {
		super(message);

		this.errorCode = errorCode;
		this.httpStatus = HttpStatus.BAD_REQUEST;
	}
}
