package com.clarkparsia.protege.exceptions;

/**
 * <p>
 * Title: Unexpected Type Exception
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
public class UnexpectedTypeException extends ConversionException {

	private static final long serialVersionUID = -48223209240015705L;

	public UnexpectedTypeException(String theMessage) {
		super(theMessage);
	}

	public UnexpectedTypeException(Object theObject) {
		super("Unexpected input type: "
				+ ((theObject != null) ? theObject.getClass().getName() : "null"));
	}

	public UnexpectedTypeException(String theMessage, Object theObject) {
		super(theMessage + " input type: "
				+ ((theObject != null) ? theObject.getClass().getName() : "null"));
	}
	
	public UnexpectedTypeException(Class<?> theExtended, Object theObject) {
		super("Unexpected specialization of " + theExtended.getName()
				+ ((theObject != null) ? theObject.getClass().getName() : "null"));
	}
}
