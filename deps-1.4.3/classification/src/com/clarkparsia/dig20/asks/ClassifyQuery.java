package com.clarkparsia.dig20.asks;

public class ClassifyQuery extends BaseAskQuery {

	public ClassifyQuery(String id) {
		super( id );
	}

	public void accept(AskQueryVisitor v) {
		v.visit( this );
	}

	@Override
	public boolean equals(Object obj) {
		if( !super.equals( obj ) )
			return false;

		return (obj instanceof ClassifyQuery);
	}

	@Override
	public String toString() {
		return super.toString() + "classify";
	}

}
