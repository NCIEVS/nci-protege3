package com.clarkparsia.dig20.exceptions;

import org.semanticweb.owl.inference.OWLReasonerException;

public class ProtocolErrorException extends OWLReasonerException {

	private static final long	serialVersionUID	= -1380116542224792071L;

	public ProtocolErrorException(String message) {
		super( message );
	}
}
