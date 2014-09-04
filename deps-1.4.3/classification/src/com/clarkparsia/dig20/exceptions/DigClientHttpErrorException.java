package com.clarkparsia.dig20.exceptions;

/**
 * <p>
 * Title: Dig Client HTTP Exception
 * </p>
 * <p>
 * Description: Exception indication exceptional behavior at HTTP layer
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Mike Smith
 */
public class DigClientHttpErrorException extends DigClientException {

	private static final long	serialVersionUID	= -9189143119730448584L;

	final private boolean		retry;

	public DigClientHttpErrorException(String message, boolean retry) {
		super( message );
		this.retry = retry;
	}

	public DigClientHttpErrorException(String message, Throwable cause, boolean retry) {
		super( message, cause );
		this.retry = retry;
	}

	public boolean getRetry() {
		return retry;
	}
}
