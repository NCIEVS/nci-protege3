package com.clarkparsia.dig20.xml.asks;

import static com.clarkparsia.dig20.asks.AskQueryFactory.unrecognized;

import org.xml.sax.Attributes;

import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.xml.SAXHandlerParent;

public class RangeClassesAskQuery extends BasicObjectPropertyAskQuery {

	private Boolean	mostSpecific;

	public RangeClassesAskQuery(SAXHandlerParent<AsksQuerySAXHandler> parent, AskQueryType t,
			Attributes attributes) {
		super( parent, t, attributes );
		String s = attributes.getValue( AskQueryType.ASKS_NS, AskQueryType.MOST_SPECIFIC_ATTR );
		mostSpecific = (s == null)
			? Boolean.FALSE
			: Boolean.parseBoolean( s );
	}

	public AskQuery getQuery() {
		switch ( type ) {
		default:
			return unrecognized( id );
		}
	}
}
