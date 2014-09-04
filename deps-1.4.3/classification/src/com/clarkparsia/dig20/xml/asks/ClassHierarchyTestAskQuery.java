package com.clarkparsia.dig20.xml.asks;

import static com.clarkparsia.dig20.asks.AskQueryFactory.isSubClassOf;
import static com.clarkparsia.dig20.asks.AskQueryFactory.unrecognized;

import org.xml.sax.Attributes;

import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.xml.SAXHandlerParent;

public class ClassHierarchyTestAskQuery extends BasicClassPairAskQuery {

	private Boolean	direct;

	public ClassHierarchyTestAskQuery(SAXHandlerParent<AsksQuerySAXHandler> parent, AskQueryType t,
			Attributes attributes) {
		super( parent, t, attributes );
		String s = attributes.getValue( AskQueryType.DIRECT_ATTR );
		direct = (s == null)
			? Boolean.FALSE
			: Boolean.parseBoolean( s );
	}

	public AskQuery getQuery() {
		switch ( type ) {
		case IS_SUBCLASS_OF:
			return isSubClassOf( id, pair[0], pair[1], direct );
		default:
			return unrecognized( id );
		}
	}
}