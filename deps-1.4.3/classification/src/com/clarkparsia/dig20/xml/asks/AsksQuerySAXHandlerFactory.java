package com.clarkparsia.dig20.xml.asks;

import org.xml.sax.Attributes;

import com.clarkparsia.dig20.xml.AsksElementHandler;
import com.clarkparsia.dig20.xml.explanation.ExplainSAXHandler;

public class AsksQuerySAXHandlerFactory {

	private static AsksQuerySAXHandlerFactory	instance;

	static {
		instance = new AsksQuerySAXHandlerFactory();
	}

	public static AsksQuerySAXHandlerFactory theInstance() {
		return instance;
	}

	public AsksQuerySAXHandler create(AsksElementHandler parent, AskQueryType q,
			Attributes attributes) {
		switch ( q ) {
		case CONSISTENT_KB:
		case CLASSIFY_KB:
		case NAMED_CLASSES:
		case NAMED_INDIVIDUALS:
		case NAMED_DATAPROPERTIES:
		case NAMED_OBJECTPROPERTIES:
		case TRANSITIVE_PROPERTIES:
		case SYMMETRIC_PROPERTIES:
		case ANTISYMMETRIC_PROPERTIES:
		case FUNCTIONAL_PROPERTIES:
		case INVERSE_FUNCTIONAL_PROPERTIES:
		case REFLEXIVE_PROPERTIES:
		case IRREFLEXIVE_PROPERTIES:
			return new BasicAskQuery( parent, q, attributes );
		case IS_SATISFIABLE:
		case EQUIVALENT_CLASSES:
		case DISJOINT_CLASSES:
			return new BasicClassAskQuery( parent, q, attributes );
		case SUBCLASSES:
		case SUPERCLASSES:
			return new ClassHierarchyAskQuery( parent, q, attributes );
		case IS_SUBCLASS_OF:
			return new ClassHierarchyTestAskQuery( parent, q, attributes );
		case IS_EQUIVALENT_CLASS_TO:
		case IS_DISJOINT_CLASS_WITH:
			return new BasicClassPairAskQuery( parent, q, attributes );
		case INSTANCES:
			return new ClassInstancesAskQuery( parent, q, attributes );
		case TYPES:
			return new InstanceTypesAskQuery( parent, q, attributes );
		case IS_INSTANCE_OF:
			return new InstantiationTestAskQuery( parent, q, attributes );
		case SYNONYMS:
		case DIFFERENT_INDIVIDUALS:
			return new BasicIndividualAskQuery( parent, q, attributes );
		case IS_SYNONYM_FOR:
		case IS_DIFFERENT_FROM:
			return new BasicIndividualPairAskQuery( parent, q, attributes );
		case SUBPROPERTIES:
		case SUPERPROPERTIES:
			return new PropertyHierarchyAskQuery( parent, q, attributes );
		case IS_SUBPROPERTY_OF:
		case IS_EQUIVALENT_PROPERTY_TO:
		case IS_DISJOINT_PROPERTY_WITH:
			return new BasicPropertyPairAskQuery( parent, q, attributes );
		case EQUIVALENT_PROPERTIES:
		case DISJOINT_PROPERTIES:
			return new BasicPropertyAskQuery( parent, q, attributes );
		case OBJECT_PROPERTY_INSTANCES:
			return new ObjectPropertyInstancesAskQuery( parent, q, attributes );
		case DATA_PROPERTY_INSTANCES:
			return new DataPropertyInstancesAskQuery( parent, q, attributes );
		case RANGE_CLASSES:
			return new RangeClassesAskQuery( parent, q, attributes );
		case IS_RANGE_CLASS_OF:
			return new RangeClassTestAskQuery( parent, q, attributes );
		case RANGE_DATATYPE:
			return new RangeDataTypeAskQuery( parent, q, attributes );
		case DOMAIN_CLASSES:
			return new DomainClassesAskQuery( parent, q, attributes );
		case IS_DOMAIN_CLASS_OF:
			return new DomainClassTestAskQuery( parent, q, attributes );
		case INVERSE_PROPERTIES:
		case IS_TRANSITIVE_PROPERTY:
		case IS_SYMMETRIC_PROPERTY:
		case IS_ANTISYMMETRIC_PROPERTY:
		case IS_FUNCTIONAL_PROPERTY:
		case IS_INVERSE_FUNCTIONAL_PROPERTY:
		case IS_REFLEXIVE_PROPERTY:
		case IS_IRREFLEXIVE_PROPERTY:
			return new BasicObjectPropertyAskQuery( parent, q, attributes );
		case IS_INVERSE_PROPERTY_OF:
			return new BasicObjectPropertyPairAskQuery( parent, q, attributes );
		case EXPLAIN:
			return new ExplainSAXHandler( parent, q, attributes );
		default:
			throw new RuntimeException( "Unsupported ask query" );
		}
	}
}
