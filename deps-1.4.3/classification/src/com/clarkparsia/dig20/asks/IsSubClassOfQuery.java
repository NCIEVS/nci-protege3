package com.clarkparsia.dig20.asks;

import org.semanticweb.owl.model.OWLDescription;

public class IsSubClassOfQuery extends ObjectPairHierarchyQuery<OWLDescription> {

	public IsSubClassOfQuery(String id, OWLDescription sub, OWLDescription sup) {
		super( id, sub, sup );
	}

	public IsSubClassOfQuery(String id, OWLDescription sub, OWLDescription sup, Boolean direct) {
		super( id, sub, sup, direct );
	}

	public void accept(AskQueryVisitor v) {
		v.visit( this );
	}

	@Override
	public boolean equals(Object obj) {
		return (super.equals( obj ) && (obj instanceof IsSubClassOfQuery));
	}

	@Override
	public String toString() {
		return super.toString() + "isSubClassOf" + ((direct != null)
			? "[direct=" + direct.toString() + "]("
			: "(") + sub.toString() + "," + sup.toString() + ")";
	}
}
