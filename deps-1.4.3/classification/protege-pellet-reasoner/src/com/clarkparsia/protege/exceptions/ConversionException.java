package com.clarkparsia.protege.exceptions;

public class ConversionException extends Exception {

	private static final long serialVersionUID = 1568427894687084737L;

	public ConversionException(String theMessage) {
		super(theMessage);
	}

	public ConversionException(Throwable theCause) {
		super(theCause);
	}
}
