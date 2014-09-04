package com.clarkparsia.dig20.exceptions;

import org.semanticweb.owl.inference.OWLReasonerException;

/**
 * <p>
 * Title: Dig Client Exception
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
public class DigClientException extends OWLReasonerException {

	private static final long	serialVersionUID	= 7885722470126478142L;

	public DigClientException(String message) {
		super( message );
	}

	public DigClientException(Throwable cause) {
		super( cause );
	}

	public DigClientException(String message, Throwable cause) {
		super( message, cause );
	}

}
