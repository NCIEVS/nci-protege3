package com.clarkparsia.dig20.explanation;

import org.semanticweb.owl.model.OWLAxiom;

import com.clarkparsia.dig20.asks.AskQueryVisitor;
import com.clarkparsia.dig20.asks.ObjectQuery;

public class ExplainQuery extends ObjectQuery<OWLAxiom> {

	/**
	 * Maximum number of explanations requested (0 if all)
	 */
	private int	max;

	public ExplainQuery(String id, OWLAxiom t) {
		super( id, t );
		max = 0;
	}

	public ExplainQuery(String id, OWLAxiom t, int max) {
		super( id, t );
		if( max < 1 )
			throw new IllegalArgumentException( "max must be positive integer" );
		this.max = max;
	}

	public void accept(AskQueryVisitor v) {
		v.visit( this );
	}

	@Override
	public boolean equals(Object obj) {
		if( !super.equals( obj ) )
			return false;

		if( obj instanceof ExplainQuery ) {
			ExplainQuery q = (ExplainQuery) obj;
			return (max == q.getMax());
		}

		return false;
	}

	public boolean getAll() {
		return (max == 0);
	}

	public int getMax() {
		return max;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		return prime * super.hashCode() + max;
	}

	@Override
	public String toString() {
		return super.toString() + "[nexp=" + ((max == 0)
			? "all"
			: max) + "](" + t.toString() + ")";
	}
}
