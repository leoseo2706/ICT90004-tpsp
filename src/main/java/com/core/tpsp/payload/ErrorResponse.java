package com.core.tpsp.payload;

import java.util.Date;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {
	
	private Date timestamp;
	private int status;
	private String error;
	private Object message;

	public ErrorResponse(int status, String error, Object message) {
		super();
		this.timestamp = new Date();
		this.status = status;
		this.error = error;
		this.message = message;
	}

	public ErrorResponse(Object message) {
		super();
		this.status = HttpStatus.OK.value();
		this.timestamp = new Date();
		this.message = message;
	}

}
