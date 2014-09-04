package com.clarkparsia.protege.exceptions;

/**
 * <p>
 * Title: Incomplete Input Exception
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
public class IncompleteInputException extends ConversionException {

	private static final long serialVersionUID = 6336763065183743707L;

	public IncompleteInputException(String theMessage) {
		super(theMessage);
	}

}
