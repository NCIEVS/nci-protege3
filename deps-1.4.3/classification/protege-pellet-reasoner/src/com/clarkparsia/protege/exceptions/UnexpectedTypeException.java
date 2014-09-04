package com.clarkparsia.protege.exceptions;

public class UnexpectedTypeException extends ConversionException {

	private static final long serialVersionUID = -48223209240015705L;

	public UnexpectedTypeException(String theMessage) {
		super(theMessage);
	}

	public UnexpectedTypeException(Object theObject) {
		super("Unexpected input type: " + theObject.getClass().getName());
	}

	public UnexpectedTypeException(String theMessage, Object theObject) {
		super(theMessage + " input type: " + theObject.getClass().getName());
	}
}
