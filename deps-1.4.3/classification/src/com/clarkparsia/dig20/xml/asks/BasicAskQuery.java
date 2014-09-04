package com.clarkparsia.dig20.xml.asks;

import static com.clarkparsia.dig20.asks.AskQueryFactory.consistentkb;
import static com.clarkparsia.dig20.asks.AskQueryFactory.classifykb;
import static com.clarkparsia.dig20.asks.AskQueryFactory.namedClasses;
import static com.clarkparsia.dig20.asks.AskQueryFactory.namedDataProperties;
import static com.clarkparsia.dig20.asks.AskQueryFactory.namedIndividuals;
import static com.clarkparsia.dig20.asks.AskQueryFactory.namedObjectProperties;
import static com.clarkparsia.dig20.asks.AskQueryFactory.unrecognized;

import org.xml.sax.Attributes;

import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.xml.SAXHandlerParent;

public class BasicAskQuery extends AbstractBaseAskQuery<Object> {

	public BasicAskQuery(SAXHandlerParent<AsksQuerySAXHandler> parent, AskQueryType t,
			Attributes attr) {
		super( parent, t, attr );
		isComplete = true;
	}

	public AskQuery getQuery() {
		switch ( type ) {
		case CONSISTENT_KB:
			return consistentkb( id );
		case CLASSIFY_KB:
			return classifykb( id );
		case NAMED_CLASSES:
			return namedClasses( id );
		case NAMED_DATAPROPERTIES:
			return namedDataProperties( id );
		case NAMED_OBJECTPROPERTIES:
			return namedObjectProperties( id );
		case NAMED_INDIVIDUALS:
			return namedIndividuals( id );
		default:
			return unrecognized( id );
		}
	}
}
