package com.clarkparsia.dig20.client.async;

import com.clarkparsia.dig20.client.DigReasoner;
import com.clarkparsia.dig20.exceptions.DigClientException;
import com.clarkparsia.dig20.exceptions.ErrorResponseException;

/**
 * <p>
 * Title: Abstract Dig Asynchronous Operation
 * </p>
 * <p>
 * Description: Abstract base class for all classes implementing
 * {@link DigAsynchronousOperation}. It provides listener notification and
 * {@link DigReasoner} field
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
public abstract class AbstractDigAsynchronousOperation implements DigAsynchronousOperation {

	final protected DigReasoner						digReasoner;
	final private DigAsynchronousOperationListener	listener;

	public AbstractDigAsynchronousOperation(DigAsynchronousOperationListener listener,
			DigReasoner digReasoner) {
		this.listener = listener;
		this.digReasoner = digReasoner;
	}

	protected void notifyFailure(DigClientException e) {
		listener.failure( this, e );
	}

	protected void notifyFailure(ErrorResponseException e) {
		listener.failure( this, e );
	}

	protected void notifyFailure() {
		listener.failure( this );
	}

	protected void notifySuccess() {
		listener.success( this );
	}
}
