package com.clarkparsia.dig20.asks;

import org.semanticweb.owl.model.OWLObject;

public abstract class ObjectQuery<T extends OWLObject> extends BaseAskQuery {

	protected T	t;

	public ObjectQuery(String id, T t) {
		super( id );
		if( t == null )
			throw new NullPointerException();
		this.t = t;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if( !super.equals( obj ) )
			return false;
		if( obj instanceof ObjectQuery ) {
			ObjectQuery q = (ObjectQuery) obj;
			return t.equals( q.getOWLObject() );
		}
		return false;
	}

	public T getOWLObject() {
		return t;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime * super.hashCode() + t.hashCode();
	}
}
