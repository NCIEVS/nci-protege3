package com.clarkparsia.dig20.xml.asks;

import static com.clarkparsia.dig20.asks.AskQueryFactory.subClasses;
import static com.clarkparsia.dig20.asks.AskQueryFactory.superClasses;
import static com.clarkparsia.dig20.asks.AskQueryFactory.unrecognized;

import org.xml.sax.Attributes;

import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.xml.SAXHandlerParent;

public class ClassHierarchyAskQuery extends BasicClassAskQuery {

	private Boolean	direct;

	public ClassHierarchyAskQuery(SAXHandlerParent<AsksQuerySAXHandler> p, AskQueryType t,
			Attributes attributes) {
		super( p, t, attributes );
		String s = attributes.getValue( AskQueryType.DIRECT_ATTR );
		direct = (s == null)
			? Boolean.FALSE
			: Boolean.parseBoolean( s );
	}

	public AskQuery getQuery() {
		switch ( type ) {
		case SUBCLASSES:
			return subClasses( id, desc, direct );
		case SUPERCLASSES:
			return superClasses( id, desc, direct );
		default:
			return unrecognized( id );
		}
	}
}