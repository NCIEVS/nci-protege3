package com.clarkparsia.dig20.exceptions;

import java.io.IOException;

/**
 * <p>
 * Title: Dig Client POST IO Exception
 * </p>
 * <p>
 * Description: Exception indication exceptional behavior at network I/O layer,
 * prior to completing HTTP POST
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
public class DigClientPostIOException extends DigClientException {

	private static final long	serialVersionUID	= -7945337781754520221L;

	public DigClientPostIOException(IOException e) {
		super( "Failed to complete POST operation", e );
	}
}
