package com.clarkparsia.dig20.explanation;

import org.semanticweb.owl.model.OWLAxiom;

public class ExplanationFactory {

	private static final String	baseId	= "explain-qid-";
	private static int			id		= 0;

	private static String getId() {
		return baseId + (id++);
	}

	public static ExplainQuery explain(OWLAxiom a) {
		return explain( getId(), a );
	}

	public static ExplainQuery explain(OWLAxiom a, int max) {
		return explain( getId(), a, max );
	}

	public static ExplainQuery explain(String id, OWLAxiom a) {
		return new ExplainQuery( id, a );
	}

	public static ExplainQuery explain(String id, OWLAxiom a, int max) {
		return new ExplainQuery( id, a, max );
	}
}
