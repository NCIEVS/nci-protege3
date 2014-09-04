package com.clarkparsia.dig20.asks;

import org.semanticweb.owl.model.OWLObject;

public abstract class ObjectPairHierarchyQuery<T extends OWLObject> extends BaseAskQuery {

	protected Boolean	direct;
	protected T			sub;
	protected T			sup;

	public ObjectPairHierarchyQuery(String id, T t, T u) {
		this( id, t, u, null );
	}

	public ObjectPairHierarchyQuery(String id, T sub, T sup, Boolean direct) {
		super( id );
		if( (sub == null) || (sup == null) )
			throw new NullPointerException();
		this.sub = sub;
		this.sup = sup;
		this.direct = direct;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if( !super.equals( obj ) )
			return false;

		if( obj instanceof ObjectPairHierarchyQuery ) {
			ObjectPairHierarchyQuery q = (ObjectPairHierarchyQuery) obj;
			if( direct == null ) {
				if( q.getDirect() != null )
					return false;
			}
			else if( !direct.equals( q.getDirect() ) )
				return false;

			return (sub.equals( q.getSub() ) && sup.equals( q.getSuper() ));
		}

		return false;
	}

	public Boolean getDirect() {
		return direct;
	}

	public T getSub() {
		return sub;
	}

	public T getSuper() {
		return sup;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime * (prime * super.hashCode() + sub.hashCode()) + sup.hashCode();
		return (direct == null)
			? result
			: prime * result + direct.hashCode();
	}
}
