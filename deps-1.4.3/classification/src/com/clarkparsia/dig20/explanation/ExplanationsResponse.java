package com.clarkparsia.dig20.explanation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.model.OWLAxiom;

import com.clarkparsia.dig20.responses.BaseAskResult;
import com.clarkparsia.dig20.responses.ResponseVisitor;

public class ExplanationsResponse extends BaseAskResult {

	public static Set<? extends OWLAxiom> explanation(OWLAxiom... axioms) {
		return new HashSet<OWLAxiom>( Arrays.asList( axioms ) );
	}

	public static ExplanationsResponse explanations(String id,
			Set<? extends OWLAxiom>... explanations) {
		return explanations( id,
				new HashSet<Set<? extends OWLAxiom>>( Arrays.asList( explanations ) ) );
	}

	public static ExplanationsResponse explanations(String id,
			Set<? extends Set<? extends OWLAxiom>> explanations) {
		return new ExplanationsResponse( id, explanations );
	}

	Set<Set<OWLAxiom>>	explanations;

	public ExplanationsResponse(String id, Set<? extends Set<? extends OWLAxiom>> explanations) {
		super( id );
		if( explanations == null )
			throw new NullPointerException();
		this.explanations = new HashSet<Set<OWLAxiom>>();
		for( Set<? extends OWLAxiom> s : explanations ) {
			this.explanations.add( new HashSet<OWLAxiom>( s ) );
		}
	}

	public void accept(ResponseVisitor v) {
		v.visit( this );
	}

	@Override
	public boolean equals(Object obj) {
		if( !super.equals( obj ) )
			return false;

		if( obj instanceof ExplanationsResponse ) {
			ExplanationsResponse r = (ExplanationsResponse) obj;
			return explanations.equals( r.getExplanations() );
		}

		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime * super.hashCode() + explanations.hashCode();
	}
	
	public Set<Set<OWLAxiom>> getExplanations() {
		return explanations;
	}
}
