package com.clarkparsia.dig20.xml.asks;

import static com.clarkparsia.dig20.xml.explanation.ExplanationVocabulary.EXPLANATIONS_NS;

import java.net.URI;

import com.clarkparsia.dig20.xml.explanation.ExplanationVocabulary;

/**
 * <p>
 * Title: AskQueryType
 * </p>
 * <p>
 * Description: Enumeration of element names that may appear as direct children
 * of the DIG 2.0 asks element
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Mike Smith
 */
public enum AskQueryType {
	
	// Based on (including ordering & grouping) http://www.informatik.uni-ulm.de/ki/Weithoener/dig/asks.xsd
	
	CONSISTENT_KB("consistentKB"),
	
	CLASSIFY_KB("classifyKB"),
	
	NAMED_CLASSES("namedClasses"),
	NAMED_INDIVIDUALS("namedIndividuals"),
	NAMED_DATAPROPERTIES("namedDataProperties"),
	NAMED_OBJECTPROPERTIES("namedObjectProperties"),
	
	IS_SATISFIABLE("isSatisfiable"),
	
	SUBCLASSES("subClasses"),
	SUPERCLASSES("superClasses"),
	IS_SUBCLASS_OF("isSubClassOf"),
	
	EQUIVALENT_CLASSES("equivalentClasses"),
	IS_EQUIVALENT_CLASS_TO("isEquivalentClassTo"),
	DISJOINT_CLASSES("disjointClasses"),
	IS_DISJOINT_CLASS_WITH("isDisjointClassWith"),
	
	INSTANCES("instances"),
	TYPES("types"),
	IS_INSTANCE_OF("isInstanceOf"),
	
	SYNONYMS("synonyms"),
	IS_SYNONYM_FOR("isSynonymFor"),
	DIFFERENT_INDIVIDUALS("differentIndividuals"),
	IS_DIFFERENT_FROM("isDifferentFrom"),
	
	SUBPROPERTIES("subProperties"),
	SUPERPROPERTIES("superProperties"),
	IS_SUBPROPERTY_OF("isSubPropertyOf"),
	
	EQUIVALENT_PROPERTIES("equivalentProperties"),
	IS_EQUIVALENT_PROPERTY_TO("isEquivalentPropertyTo"),
	DISJOINT_PROPERTIES("disjointProperties"),
	IS_DISJOINT_PROPERTY_WITH("isDisjointPropertyWith"),
	
	OBJECT_PROPERTY_INSTANCES("objectPropertyInstances"),
	DATA_PROPERTY_INSTANCES("dataPropertyInstances"),
	
	RANGE_CLASSES("rangeClasses"),
	IS_RANGE_CLASS_OF("isRangeClassOf"),
	RANGE_DATATYPE("rangeDataType"),
	DOMAIN_CLASSES("domainClasses"),
	IS_DOMAIN_CLASS_OF("isDomainClassOf"),
	
	INVERSE_PROPERTIES("inverseProperties"),
	IS_INVERSE_PROPERTY_OF("isInversePropertyOf"),
	TRANSITIVE_PROPERTIES("transitiveProperties"),
	IS_TRANSITIVE_PROPERTY("isTransitiveProperty"),
	SYMMETRIC_PROPERTIES("symmetricProperties"),
	IS_SYMMETRIC_PROPERTY("isSymmetricProperty"),
	ANTISYMMETRIC_PROPERTIES("antiSymmetricProperties"),
	IS_ANTISYMMETRIC_PROPERTY("isAntiSymmetricProperty"),
	FUNCTIONAL_PROPERTIES("functionalProperties"),
	IS_FUNCTIONAL_PROPERTY("isFunctionalProperty"),
	INVERSE_FUNCTIONAL_PROPERTIES("inverseFunctionalProperties"),
	IS_INVERSE_FUNCTIONAL_PROPERTY("isInverseFunctionalProperty"),
	REFLEXIVE_PROPERTIES("reflexiveProperties"),
	IS_REFLEXIVE_PROPERTY("isReflexiveProperty"),
	IRREFLEXIVE_PROPERTIES("irreflexiveProperties"),
	IS_IRREFLEXIVE_PROPERTY("isIrreflexiveProperty"),
	
	EXPLAIN(EXPLANATIONS_NS, ExplanationVocabulary.Elements.EXPLAIN.getShortName());

	final static public String	ASKS_NS				= "http://dl.kr.org/dig/lang/asks-schema#";
	final static public String	ID_ATTR				= "id";
	final static public String	DIRECT_ATTR			= "direct";
	final static public String	MOST_SPECIFIC_ATTR	= "mostSpecific";
	final static public String	TRANSITIVITY_ATTR	= "transitivity";

	private URI					uri;
	private String				shortName;

	AskQueryType(String name) {
		this( ASKS_NS, name );
	}

	AskQueryType(String ns, String name) {
		this.uri = URI.create( ns + name );
		shortName = name;
	}

	public String getShortName() {
		return shortName;
	}

	public URI getURI() {
		return uri;
	}

	public String toString() {
		return uri.toString();
	}

	static public AskQueryType get(String uri) {
		for( AskQueryType q : AskQueryType.values() ) {
			if( q.toString().equals( uri ) )
				return q;
		}
		return null;
	}
}