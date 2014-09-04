package com.clarkparsia.dig20.client.async;

import com.clarkparsia.dig20.exceptions.DigClientException;
import com.clarkparsia.dig20.exceptions.ErrorResponseException;

/**
 * <p>
 * Title: Dig Asynchronous Operation Listener Interface
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
public interface DigAsynchronousOperationListener {

	public void failure(DigAsynchronousOperation op);

	public void failure(DigAsynchronousOperation op, DigClientException t);

	public void failure(DigAsynchronousOperation op, ErrorResponseException e);

	public void success(DigAsynchronousOperation op);

}
