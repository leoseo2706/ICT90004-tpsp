package com.core.tpsp.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

public class TpspException extends RuntimeException {
	
private static final long serialVersionUID = 7374076822728601201L;
	
	@Getter
	private int code;
	
	public TpspException(String msg) {
		super(msg);
		this.code = HttpStatus.INTERNAL_SERVER_ERROR.value();
	}

	public TpspException(int code, String msg) {
		super(msg);
		this.code = code;
	}

}