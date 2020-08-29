package com.core.tpsp.exception;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.core.tpsp.payload.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class TpspControllerAdvice {

	@ExceptionHandler(TpspException.class)
	public @ResponseBody ErrorResponse handle(TpspException e, HttpServletResponse response) {
		log.error("Custom error received: {}", e);
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		return new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
				e.getClass().getSimpleName(), e.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public @ResponseBody ErrorResponse handle(Exception e, HttpServletResponse response) {
		log.error("Common error received: {}", e);
		response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getClass().getSimpleName(),
				e.getMessage());
	}
}
