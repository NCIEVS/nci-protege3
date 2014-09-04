package com.clarkparsia.protege.exceptions;

/**
 * <p>
 * Title: Conversion Exception
 * </p>
 * <p>
 * Description:
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
public class ConversionException extends Exception {

	private static final long serialVersionUID = 1568427894687084737L;

	public ConversionException(String theMessage) {
		super(theMessage);
	}

	public ConversionException(Throwable theCause) {
		super(theCause);
	}
	
	public ConversionException(String theMessage, Throwable theCause) {
		super(theMessage, theCause);
	}
}
