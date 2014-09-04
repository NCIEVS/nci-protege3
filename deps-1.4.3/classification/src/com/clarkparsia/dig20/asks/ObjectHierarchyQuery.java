package com.clarkparsia.dig20.asks;

import org.semanticweb.owl.model.OWLObject;

public abstract class ObjectHierarchyQuery<T extends OWLObject> extends BaseAskQuery {

	protected Boolean	direct;
	protected T			t;

	public ObjectHierarchyQuery(String id, T t) {
		this( id, t, null );
	}

	public ObjectHierarchyQuery(String id, T t, Boolean direct) {
		super( id );
		if( t == null )
			throw new NullPointerException();
		this.t = t;
		this.direct = direct;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if( !super.equals( obj ) )
			return false;

		if( obj instanceof ObjectHierarchyQuery ) {
			ObjectHierarchyQuery q = (ObjectHierarchyQuery) obj;
			if( direct == null ) {
				if( q.getDirect() != null )
					return false;
			}
			else if( !direct.equals( q.getDirect() ) )
				return false;

			return t.equals( q.getOWLObject() );
		}

		return false;
	}

	public Boolean getDirect() {
		return direct;
	}

	public T getOWLObject() {
		return t;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime * super.hashCode() + t.hashCode();
		return (direct == null)
			? result
			: prime * result + direct.hashCode();
	}
}
