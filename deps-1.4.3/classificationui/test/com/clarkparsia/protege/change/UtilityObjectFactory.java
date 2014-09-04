package com.clarkparsia.protege.change;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDeclarationAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectSubPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLUntypedConstant;

/**
 * <p>
 * Title: UtilityObjectFactory
 * </p>
 * <p>
 * Description: Class of static methods used to create objects and make unit
 * tests more readable
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Mike Smith
 */
public class UtilityObjectFactory {

	private static OWLDataFactory dataFactory;

	static {
		dataFactory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
	}

	public static org.semanticweb.owl.model.OWLClass cls(String s) {
		return dataFactory.getOWLClass(URI.create(s));
	}

	public static <T> Collection<T> collection(T... ts) {
		return Arrays.asList(ts);
	}

	public static OWLUntypedConstant constant(String s) {
		return dataFactory.getOWLUntypedConstant(s);
	}

	public static org.semanticweb.owl.model.OWLDataProperty dataProperty(String s) {
		return dataFactory.getOWLDataProperty(URI.create(s));
	}

	public static OWLDeclarationAxiom declaration(OWLEntity e) {
		return dataFactory.getOWLDeclarationAxiom(e);
	}
	
	public static OWLDisjointClassesAxiom disjoint(OWLDescription... clses) {
		return dataFactory.getOWLDisjointClassesAxiom(new HashSet<OWLDescription>(Arrays
				.asList(clses)));
	}

	public static OWLFunctionalObjectPropertyAxiom functional(org.semanticweb.owl.model.OWLObjectProperty p) {
		return dataFactory.getOWLFunctionalObjectPropertyAxiom(p);
	}

	public static OWLEquivalentClassesAxiom equivalent(OWLDescription... clses) {
		return dataFactory.getOWLEquivalentClassesAxiom(new HashSet<OWLDescription>(Arrays
				.asList(clses)));
	}

	public static OWLDataValueRestriction hasValue(org.semanticweb.owl.model.OWLDataProperty p,
			org.semanticweb.owl.model.OWLConstant c) {
		return dataFactory.getOWLDataValueRestriction(p, c);
	}

	public static OWLObjectValueRestriction hasValue(org.semanticweb.owl.model.OWLObjectProperty p,
			org.semanticweb.owl.model.OWLIndividual i) {
		return dataFactory.getOWLObjectValueRestriction(p, i);
	}

	public static org.semanticweb.owl.model.OWLIndividual individual(String s) {
		return dataFactory.getOWLIndividual(URI.create(s));
	}

	public static OWLObjectIntersectionOf intersectionOf(OWLDescription... ds) {
		return dataFactory
				.getOWLObjectIntersectionOf(new HashSet<OWLDescription>(Arrays.asList(ds)));
	}
	
	public static <T> List<T> list(T... ts) {
		return Arrays.asList(ts);
	}

	public static OWLObjectMinCardinalityRestriction min(OWLObjectPropertyExpression p, int value) {
		return dataFactory.getOWLObjectMinCardinalityRestriction(p, value);
	}
	
	public static OWLObjectSomeRestriction some(OWLObjectPropertyExpression p, OWLDescription d) {
		return dataFactory.getOWLObjectSomeRestriction(p, d);
	}
	
	public static OWLObjectAllRestriction all(OWLObjectPropertyExpression p, OWLDescription d) {
		return dataFactory.getOWLObjectAllRestriction(p, d);
	}

	public static Collection<OWLAxiom> noAxioms() {
		return Collections.emptySet();
	}

	public static OWLObjectComplementOf not(OWLDescription d) {
		return dataFactory.getOWLObjectComplementOf(d);
	}

	public static org.semanticweb.owl.model.OWLObjectProperty objectProperty(String s) {
		return dataFactory.getOWLObjectProperty(URI.create(s));
	}
	
	public static OWLObjectPropertyRangeAxiom range(org.semanticweb.owl.model.OWLObjectProperty p,
			OWLDescription d) {
		return dataFactory.getOWLObjectPropertyRangeAxiom(p, d);
	}

	public static <T> Set<T> set(T... ts) {
		return new HashSet<T>(Arrays.asList(ts));
	}

	public static OWLSubClassAxiom sub(OWLDescription sub, OWLDescription sup) {
		return dataFactory.getOWLSubClassAxiom(sub, sup);
	}

	public static OWLObjectSubPropertyAxiom sub(OWLObjectPropertyExpression sub,
			OWLObjectPropertyExpression sup) {
		return dataFactory.getOWLSubObjectPropertyAxiom(sub, sup);
	}

	public static org.semanticweb.owl.model.OWLClass top() {
		return dataFactory.getOWLThing();
	}

	public static OWLObjectUnionOf unionOf(OWLDescription... ds) {
		return dataFactory.getOWLObjectUnionOf(new HashSet<OWLDescription>(Arrays.asList(ds)));
	}
}
