package com.clarkparsia.dig20.asks;

public abstract class BaseAskQuery implements AskQuery {

	private String	id;

	BaseAskQuery(String id) {
		if (id == null)
			throw new NullPointerException();
		
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if( obj instanceof BaseAskQuery ) {
			BaseAskQuery q = (BaseAskQuery) obj;
			return id.equals( q.getId() );
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
		return "?[" + id + "]";
	}
}
