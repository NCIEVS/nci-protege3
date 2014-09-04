package com.clarkparsia.dig20.responses;

import com.clarkparsia.dig20.RetractAxioms;
import com.clarkparsia.dig20.TellAxioms;

/**
 * <p>
 * Title: OK Response
 * </p>
 * <p>
 * Description: Response used to acknowledge change directives (
 * {@link TellAxioms}, {@link RetractAxioms}
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Mike Smith
 */
public class OkResponse implements Response {

	public static OkResponse ok(String id) {
		return new OkResponse( id );
	}

	private final String	id;

	public OkResponse(String id) {
		this.id = id;
	}

	public void accept(ResponseVisitor v) {
		v.visit( this );
	}

	@Override
	public boolean equals(Object obj) {
		if( !super.equals( obj ) )
			return false;
		if( obj instanceof OkResponse ) {
			OkResponse other = (OkResponse) obj;
			return id.equals( other.getId() );
		}
		return false;
	}

	public String getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 37;
		return prime * super.hashCode() + id.hashCode();
	}

	@Override
	public String toString() {
		return "OK[" + id + "]";
	}
}
