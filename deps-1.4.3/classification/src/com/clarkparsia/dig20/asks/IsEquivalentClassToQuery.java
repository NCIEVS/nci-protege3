package com.clarkparsia.dig20.asks;

import org.semanticweb.owl.model.OWLDescription;

public class IsEquivalentClassToQuery extends ObjectPairQuery<OWLDescription> {

	public IsEquivalentClassToQuery(String id, OWLDescription t, OWLDescription u) {
		super( id, t, u );
	}

	public void accept(AskQueryVisitor v) {
		v.visit( this );
	}

	@Override
	public boolean equals(Object obj) {
		return (super.equals( obj ) && (obj instanceof IsEquivalentClassToQuery));
	}

	@Override
	public String toString() {
		return super.toString() + "isEquivalentClassTo" + "(" + t.toString() + "," + u.toString()
				+ ")";
	}
}
