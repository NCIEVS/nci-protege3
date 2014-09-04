package com.clarkparsia.dig20.xml.asks;

import static com.clarkparsia.dig20.asks.AskQueryFactory.unrecognized;

import org.coode.owl.owlxmlparser.OWLConstantElementHandler;
import org.coode.owl.owlxmlparser.OWLDataPropertyElementHandler;
import org.coode.owl.owlxmlparser.OWLIndividualElementHandler;
import org.coode.owl.owlxmlparser.OWLXMLParserException;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.xml.sax.Attributes;

import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.xml.SAXHandlerParent;

public class DataPropertyInstancesAskQuery extends BasicIndividualAskQuery {

	private OWLDataPropertyExpression	p;
	private OWLConstant					v;

	public DataPropertyInstancesAskQuery(SAXHandlerParent<AsksQuerySAXHandler> parent,
			AskQueryType t, Attributes attributes) {
		super( parent, t, attributes );
	}

	public AskQuery getQuery() {
		switch ( type ) {
		default:
			return unrecognized( id );
		}
	}

	@Override
	public void handleChild(OWLConstantElementHandler handler) throws OWLXMLParserException {
		v = handler.getOWLObject();
		isComplete = (ind != null) && (p != null) && (v != null);
	}

	@Override
	public void handleChild(OWLDataPropertyElementHandler handler) throws OWLXMLParserException {
		p = handler.getOWLObject();
		isComplete = (ind != null) && (p != null) && (v != null);
	}

	@Override
	public void handleChild(OWLIndividualElementHandler handler) throws OWLXMLParserException {
		super.handleChild( handler );
		isComplete = (ind != null) && (p != null) && (v != null);
	}
}
