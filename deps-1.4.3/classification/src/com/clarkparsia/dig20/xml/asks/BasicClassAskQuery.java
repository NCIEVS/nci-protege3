package com.clarkparsia.dig20.xml.asks;

import static com.clarkparsia.dig20.asks.AskQueryFactory.equivalentClasses;
import static com.clarkparsia.dig20.asks.AskQueryFactory.isSatisfiable;
import static com.clarkparsia.dig20.asks.AskQueryFactory.unrecognized;

import org.coode.owl.owlxmlparser.AbstractOWLDescriptionElementHandler;
import org.coode.owl.owlxmlparser.OWLXMLParserException;
import org.semanticweb.owl.model.OWLDescription;
import org.xml.sax.Attributes;

import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.xml.SAXHandlerParent;

public class BasicClassAskQuery extends AbstractBaseAskQuery<OWLDescription> {

	protected OWLDescription	desc;

	public BasicClassAskQuery(SAXHandlerParent<AsksQuerySAXHandler> parent, AskQueryType t,
			Attributes attributes) {
		super( parent, t, attributes );
	}

	public AskQuery getQuery() {
		switch ( type ) {
		case EQUIVALENT_CLASSES:
			return equivalentClasses( id, desc );
		case IS_SATISFIABLE:
			return isSatisfiable( id, desc );
		default:
			return unrecognized( id );
		}
	}

	@Override
	public void handleChild(AbstractOWLDescriptionElementHandler handler)
			throws OWLXMLParserException {
		desc = handler.getOWLObject();
		isComplete = true;
	}
}