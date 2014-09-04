package com.clarkparsia.dig20.asks;

import org.semanticweb.owl.model.OWLDescription;

public class AskQueryFactory {

	private static final String	baseId	= "ask-qid-";
	private static int			nextId	= 0;

	public static ConsistentQuery consistentkb() {
		return consistentkb( getId() );
	}

	public static ConsistentQuery consistentkb(String id) {
		return new ConsistentQuery( id );
	}

	public static ClassifyQuery classifykb() {
		return classifykb( getId() );
	}

	public static ClassifyQuery classifykb(String id) {
		return new ClassifyQuery( id );
	}
	
	public static EquivalentClassesQuery equivalentClasses(OWLDescription desc) {
		return new EquivalentClassesQuery( getId(), desc );
	}

	public static EquivalentClassesQuery equivalentClasses(String id, OWLDescription desc) {
		return new EquivalentClassesQuery( id, desc );
	}

	private static String getId() {
		return baseId + (nextId++);
	}

	public static IsEquivalentClassToQuery isEquivalentClassTo(OWLDescription d0, OWLDescription d1) {
		return isEquivalentClassTo( getId(), d0, d1 );
	}

	public static IsEquivalentClassToQuery isEquivalentClassTo(String id, OWLDescription d0,
			OWLDescription d1) {
		return new IsEquivalentClassToQuery( id, d0, d1 );
	}

	public static IsSatisfiableQuery isSatisfiable(OWLDescription desc) {
		return isSatisfiable( getId(), desc );
	}

	public static IsSatisfiableQuery isSatisfiable(String id, OWLDescription desc) {
		return new IsSatisfiableQuery( id, desc );
	}

	public static IsSubClassOfQuery isSubClassOf(OWLDescription sub, OWLDescription sup) {
		return isSubClassOf( getId(), sub, sup );
	}

	public static IsSubClassOfQuery isSubClassOf(OWLDescription sub, OWLDescription sup,
			Boolean direct) {
		return isSubClassOf( getId(), sub, sup, direct );
	}

	public static IsSubClassOfQuery isSubClassOf(String id, OWLDescription sub, OWLDescription sup) {
		return new IsSubClassOfQuery( id, sub, sup );
	}

	public static IsSubClassOfQuery isSubClassOf(String id, OWLDescription sub, OWLDescription sup,
			Boolean direct) {
		return new IsSubClassOfQuery( id, sub, sup, direct );
	}

	public static NamedClassesQuery namedClasses() {
		return namedClasses( getId() );
	}

	public static NamedClassesQuery namedClasses(String id) {
		return new NamedClassesQuery( id );
	}

	public static NamedDataPropertiesQuery namedDataProperties() {
		return namedDataProperties( getId() );
	}

	public static NamedDataPropertiesQuery namedDataProperties(String id) {
		return new NamedDataPropertiesQuery( id );
	}

	public static NamedIndividualsQuery namedIndividuals() {
		return namedIndividuals( getId() );
	}

	public static NamedIndividualsQuery namedIndividuals(String id) {
		return new NamedIndividualsQuery( id );
	}

	public static NamedObjectPropertiesQuery namedObjectProperties() {
		return namedObjectProperties( getId() );
	}

	public static NamedObjectPropertiesQuery namedObjectProperties(String id) {
		return new NamedObjectPropertiesQuery( id );
	}

	public static SubClassesQuery subClasses(OWLDescription desc) {
		return subClasses( getId(), desc );
	}

	public static SubClassesQuery subClasses(OWLDescription desc, Boolean direct) {
		return subClasses( getId(), desc, direct );
	}

	public static SubClassesQuery subClasses(String id, OWLDescription desc) {
		return new SubClassesQuery( id, desc );
	}

	public static SubClassesQuery subClasses(String id, OWLDescription desc, Boolean direct) {
		return new SubClassesQuery( id, desc, direct );
	}

	public static SuperClassesQuery superClasses(OWLDescription desc) {
		return superClasses( getId(), desc );
	}

	public static SuperClassesQuery superClasses(OWLDescription desc, Boolean direct) {
		return superClasses( getId(), desc, direct );
	}

	public static SuperClassesQuery superClasses(String id, OWLDescription desc) {
		return new SuperClassesQuery( id, desc );
	}

	public static SuperClassesQuery superClasses(String id, OWLDescription desc, Boolean direct) {
		return new SuperClassesQuery( id, desc, direct );
	}

	public static UnrecognizedQuery unrecognized(String id) {
		return new UnrecognizedQuery( id );
	}
}
