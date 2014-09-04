package com.clarkparsia.dig20.exceptions;

import java.io.IOException;

/**
 * <p>
 * Title: Dig Client Response IO Exception
 * </p>
 * <p>
 * Description: Exception indication exceptional behavior at network I/O layer,
 * after completing HTTP POST but before response completed
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
public class DigClientResponseIOException extends DigClientException {

	private static final long	serialVersionUID	= -8797942155131368118L;

	public DigClientResponseIOException(IOException e) {
		super( "Failure getting response to POST operation", e );
	}
}
