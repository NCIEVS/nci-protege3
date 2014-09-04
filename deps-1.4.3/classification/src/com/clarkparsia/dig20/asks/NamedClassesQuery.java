package com.clarkparsia.dig20.asks;

public class NamedClassesQuery extends BaseAskQuery {

	public NamedClassesQuery(String id) {
		super( id );
	}

	public void accept(AskQueryVisitor v) {
		v.visit( this );
	}

	@Override
	public boolean equals(Object obj) {
		if( !super.equals( obj ) )
			return false;

		return (obj instanceof NamedClassesQuery);
	}

}
