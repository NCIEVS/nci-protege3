package com.clarkparsia.dig20.responses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.semanticweb.owl.model.OWLObject;

public class SynonymsAskResult extends BaseAskResult {

	public static SynonymsAskResult result(String id,
			List<? extends List<? extends OWLObject>> synonyms) {
		return new SynonymsAskResult( id, synonyms );
	}

	public static SynonymsAskResult result(String id, List<? extends OWLObject>... synonyms) {
		return new SynonymsAskResult( id, Arrays.asList( synonyms ) );
	}

	public static SynonymsAskResult result(String id,
			Set<? extends Set<? extends OWLObject>> synonyms) {
		List<List<OWLObject>> outer = new ArrayList<List<OWLObject>>();
		for( Set<? extends OWLObject> s : synonyms ) {
			outer.add( new ArrayList<OWLObject>( s ) );
		}
		return new SynonymsAskResult( id, outer );
	}

	public static List<OWLObject> synonyms(OWLObject... objects) {
		return Arrays.asList( objects );
	}

	private List<? extends List<? extends OWLObject>>	objects;

	public SynonymsAskResult(String id, List<? extends List<? extends OWLObject>> objects) {
		super( id );
		this.objects = objects;
	}

	public void accept(ResponseVisitor v) {
		v.visit( this );
	}

	@Override
	public boolean equals(Object obj) {
		if( !super.equals( obj ) )
			return false;

		if( obj instanceof SynonymsAskResult ) {
			SynonymsAskResult r = (SynonymsAskResult) obj;
			return objects.equals( r.getOWLObjects() );
		}

		return false;
	}

	public List<? extends List<? extends OWLObject>> getOWLObjects() {
		return objects;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime * super.hashCode() + objects.hashCode();
	}
	
	@Override
	public String toString() {
		return super.toString() + "(" + objects + ")";
	}
}
