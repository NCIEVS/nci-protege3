package com.clarkparsia.dig20.asks;

import org.semanticweb.owl.model.OWLDescription;

public class SubClassesQuery extends ObjectHierarchyQuery<OWLDescription> {

	public SubClassesQuery(String id, OWLDescription t) {
		super( id, t );
	}

	public SubClassesQuery(String id, OWLDescription t, Boolean direct) {
		super( id, t, direct );
	}

	public void accept(AskQueryVisitor v) {
		v.visit( this );
	}

	@Override
	public boolean equals(Object obj) {
		return (super.equals( obj ) && (obj instanceof SubClassesQuery));
	}

	@Override
	public String toString() {
		return super.toString() + "subClasses" + ((direct != null)
			? "[direct=" + direct.toString() + "]("
			: "(") + t.toString() + ")";
	}

}
