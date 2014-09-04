package com.clarkparsia.dig20.client.async;

/**
 * <p>
 * Title: Dig Asynchronous Operation Interface
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
public interface DigAsynchronousOperation extends Runnable {

	public void accept(DigAsynchronousOperationVisitor visitor);

}
