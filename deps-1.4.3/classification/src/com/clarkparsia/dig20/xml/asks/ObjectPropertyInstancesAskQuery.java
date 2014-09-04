package com.clarkparsia.dig20.xml.asks;

import static com.clarkparsia.dig20.asks.AskQueryFactory.unrecognized;

import org.coode.owl.owlxmlparser.AbstractOWLObjectPropertyElementHandler;
import org.coode.owl.owlxmlparser.OWLIndividualElementHandler;
import org.coode.owl.owlxmlparser.OWLXMLParserException;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.xml.sax.Attributes;

import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.xml.SAXHandlerParent;

public class ObjectPropertyInstancesAskQuery extends BasicIndividualPairAskQuery {

	private Boolean						mostSpecific;
	private OWLObjectPropertyExpression	p;
	private Boolean						transitivity;

	public ObjectPropertyInstancesAskQuery(SAXHandlerParent<AsksQuerySAXHandler> parent,
			AskQueryType t, Attributes attributes) {
		super( parent, t, attributes );
		String s = attributes.getValue( AskQueryType.ASKS_NS, AskQueryType.MOST_SPECIFIC_ATTR );
		mostSpecific = (s == null)
			? Boolean.FALSE
			: Boolean.parseBoolean( s );
		s = attributes.getValue( AskQueryType.ASKS_NS, AskQueryType.TRANSITIVITY_ATTR );
		transitivity = (s == null)
			? Boolean.TRUE
			: Boolean.parseBoolean( s );
	}

	public AskQuery getQuery() {
		switch ( type ) {
		default:
			return unrecognized( id );
		}
	}

	@Override
	public void handleChild(AbstractOWLObjectPropertyElementHandler handler)
			throws OWLXMLParserException {
		p = handler.getOWLObject();
		isComplete = (nFound == 2) && (p != null);
	}

	@Override
	public void handleChild(OWLIndividualElementHandler handler) throws OWLXMLParserException {
		super.handleChild( handler );
		isComplete = (nFound == 2) && (p != null);
	}
}
