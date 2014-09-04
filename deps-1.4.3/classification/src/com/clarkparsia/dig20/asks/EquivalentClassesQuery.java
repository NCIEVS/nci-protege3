package com.clarkparsia.dig20.asks;

import org.semanticweb.owl.model.OWLDescription;

public class EquivalentClassesQuery extends ObjectQuery<OWLDescription> {

	public EquivalentClassesQuery(String id, OWLDescription t) {
		super( id, t );
	}

	public void accept(AskQueryVisitor v) {
		v.visit( this );
	}

	@Override
	public boolean equals(Object obj) {
		return (super.equals( obj ) && (obj instanceof EquivalentClassesQuery));
	}

	@Override
	public String toString() {
		return super.toString() + "equivalentClasses" + "(" + t.toString() + ")";
	}
}
