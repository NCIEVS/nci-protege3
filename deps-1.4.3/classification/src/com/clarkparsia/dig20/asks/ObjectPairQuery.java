package com.clarkparsia.dig20.asks;

import org.semanticweb.owl.model.OWLObject;

public abstract class ObjectPairQuery<T extends OWLObject> extends BaseAskQuery {

	protected T	t;
	protected T	u;

	public ObjectPairQuery(String id, T t, T u) {
		super( id );
		if( (t == null) || (u == null) )
			throw new NullPointerException();
		this.t = t;
		this.u = u;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if( !super.equals( obj ) )
			return false;

		if( obj instanceof ObjectPairQuery ) {
			ObjectPairQuery q = (ObjectPairQuery) obj;
			return ((t.equals( q.getFirst() ) && u.equals( q.getSecond() )) || (t.equals( q
					.getSecond() ) && u.equals( q.getFirst() )));
		}

		return false;
	}

	public T getFirst() {
		return t;
	}

	public T getSecond() {
		return u;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		return prime * (prime * super.hashCode() + t.hashCode()) + u.hashCode();
	}
}
