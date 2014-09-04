package com.clarkparsia.dig20;

import java.net.URI;
import java.util.Collection;

import org.semanticweb.owl.model.OWLAxiom;

/**
 * <p>
 * Title: Retract Axioms
 * </p>
 * <p>
 * Description:
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
public class RetractAxioms extends AbstractChangeDirective {

	public RetractAxioms(String id, URI uri, Collection<? extends OWLAxiom> axioms) {
		super( id, uri, axioms );
	}

	@Override
	public String toString() {
		return super.toString( "Retract" );
	}

}
