package org.uu.nl.goldenagents.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.uu.nl.goldenagents.exceptions.AgentNotFoundException;
import org.uu.nl.goldenagents.exceptions.InvalidIdException;
import org.uu.nl.goldenagents.netmodels.angular.ApiError;
import org.uu.nl.goldenagents.util.StacktraceFilterElements;
import org.uu.nl.net2apl.core.platform.Platform;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;

@ControllerAdvice
public class ErrorController {

	@Value("${debug}")
	private boolean debug;

	// Swallow errors?
	@Value("${stacktrace.swallow}")
	private boolean swallow;

	// Filter stacktraces that are not relevant? See
	@Value("${stacktrace.filter}")
	private boolean filter;
	
	@ExceptionHandler(AgentNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ApiError handleAgentNotFoundException(AgentNotFoundException ex, WebRequest request) {
		logError(ex, request);
		return new ApiError(
				new Date(), 
				ex.getMessage(), 
				request.getDescription(false), 
				debug ? stackTraceToString(ex.getStackTrace()) : null);
	}
	
	@ExceptionHandler(InvalidIdException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleInvalidIdException(InvalidIdException ex, WebRequest request) {
		logError(ex, request);
		return new ApiError(
				new Date(), 
				ex.getMessage(), 
				request.getDescription(false), 
				debug ? stackTraceToString(ex.getStackTrace()) : null);
	}
	
	@ExceptionHandler(IOException.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public ApiError handleIOException(IOException ex, WebRequest request) {
		logError(ex, request);
		return new ApiError(
				new Date(), 
				ex.getMessage(), 
				request.getDescription(false), 
				debug ? stackTraceToString(ex.getStackTrace()) : null);
	}
	
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ApiError handleGenericException(Exception ex, WebRequest request) {
		logError(ex, request);
		return new ApiError(
				new Date(), 
				ex.getMessage(), 
				request.getDescription(false), 
				debug ? stackTraceToString(ex.getStackTrace()) : null);
	}

	private void logError(Exception ex, WebRequest request) {
		if(!swallow) {
			StackTraceElement[] elements =
					filter ? StacktraceFilterElements.filter(ex.getStackTrace()) : ex.getStackTrace();

			Class c;
			try {
				StackTraceElement firstElem = elements.length > 0 ? elements[0] : ex.getStackTrace()[0];
				c = Class.forName(firstElem.getClassName());
			} catch (ClassNotFoundException | IndexOutOfBoundsException e) {
				c = ErrorController.class;
			}
			Platform.getLogger().log(c, String.format(
					"Exception %s handled by SPRING for request %s", ex.getMessage(), request.getDescription(false)));
			ex.setStackTrace(elements);
			Platform.getLogger().log(c, Level.INFO, ex);
		}
	}
	
	private String[] stackTraceToString(StackTraceElement[] trace) {
		return Arrays.stream(trace).map(stack -> stack.toString()).toArray(String[]::new);
	}
}
