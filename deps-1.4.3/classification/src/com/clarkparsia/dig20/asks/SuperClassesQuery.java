package com.clarkparsia.dig20.asks;

import org.semanticweb.owl.model.OWLDescription;

public class SuperClassesQuery extends ObjectHierarchyQuery<OWLDescription> {

	public SuperClassesQuery(String id, OWLDescription t) {
		super( id, t );
	}

	public SuperClassesQuery(String id, OWLDescription t, Boolean direct) {
		super( id, t, direct );
	}

	public void accept(AskQueryVisitor v) {
		v.visit( this );
	}

	@Override
	public boolean equals(Object obj) {
		return (super.equals( obj ) && (obj instanceof SuperClassesQuery));
	}

	@Override
	public String toString() {
		return super.toString() + "superClasses" + ((direct != null)
			? "[direct=" + direct.toString() + "]("
			: "(") + t.toString() + ")";
	}
}
