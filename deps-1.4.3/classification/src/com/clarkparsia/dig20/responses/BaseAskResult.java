package com.clarkparsia.dig20.responses;

public abstract class BaseAskResult implements Response {

	private String	id;

	public BaseAskResult(String id) {
		if (id == null)
			throw new NullPointerException();
		
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if( obj instanceof BaseAskResult ) {
			BaseAskResult r = (BaseAskResult) obj;
			return id.equals( r.getId() );
		}
		return false;
	}

	public String getId() {
		return id;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return "=[" + id + "]";
	}
}
