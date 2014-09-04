package com.clarkparsia.dig20;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.semanticweb.owl.model.OWLAxiom;

/**
 * <p>
 * Title: Abstract Change Directive
 * </p>
 * <p>
 * Description: Common implementation for tells and retractions
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Mike Smith
 */
public abstract class AbstractChangeDirective {

	private final OWLAxiom[]	axioms;
	private final String		id;
	private final URI			uri;

	public AbstractChangeDirective(String id, URI uri, Collection<? extends OWLAxiom> axioms) {
		if( id == null )
			throw new NullPointerException();
		if( axioms == null )
			throw new NullPointerException();
		if( uri == null )
			throw new NullPointerException();

		this.id = id;
		this.uri = uri;
		if( axioms instanceof Set ) {
			this.axioms = axioms.toArray( new OWLAxiom[0] );
		}
		else {
			// Eliminate duplicate axioms
			this.axioms = new LinkedHashSet<OWLAxiom>( axioms ).toArray( new OWLAxiom[0] );
		}
	}

	public Collection<OWLAxiom> getAxioms() {
		return Collections.unmodifiableList( Arrays.asList( axioms ) );
	}

	public String getId() {
		return id;
	}

	public URI getURI() {
		return uri;
	}

	protected String toString(String base) {
		return String.format( "%s(%s,%s,%s)", base, id, Arrays.toString(axioms), uri );
	}
}
