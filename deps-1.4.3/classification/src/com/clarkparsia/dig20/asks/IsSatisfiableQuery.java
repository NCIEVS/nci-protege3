package com.clarkparsia.dig20.asks;

import org.semanticweb.owl.model.OWLDescription;

public class IsSatisfiableQuery extends ObjectQuery<OWLDescription> {

	public IsSatisfiableQuery(String id, OWLDescription t) {
		super( id, t );
	}

	public void accept(AskQueryVisitor v) {
		v.visit( this );
	}

	@Override
	public boolean equals(Object obj) {
		return (super.equals( obj ) && (obj instanceof IsSatisfiableQuery));
	}
}
