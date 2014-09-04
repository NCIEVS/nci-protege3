package com.clarkparsia.dig20.xml.asks;

import static com.clarkparsia.dig20.asks.AskQueryFactory.unrecognized;

import org.coode.owl.owlxmlparser.AbstractOWLDescriptionElementHandler;
import org.coode.owl.owlxmlparser.OWLIndividualElementHandler;
import org.coode.owl.owlxmlparser.OWLXMLParserException;
import org.semanticweb.owl.model.OWLDescription;
import org.xml.sax.Attributes;

import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.xml.SAXHandlerParent;

public class InstantiationTestAskQuery extends BasicIndividualAskQuery {

	private OWLDescription	desc;

	public InstantiationTestAskQuery(SAXHandlerParent<AsksQuerySAXHandler> parent, AskQueryType t,
			Attributes attributes) {
		super( parent, t, attributes );
	}

	public AskQuery getQuery() {
		switch ( type ) {
		default:
			return unrecognized( id );
		}
	}

	@Override
	public void handleChild(AbstractOWLDescriptionElementHandler handler)
			throws OWLXMLParserException {
		desc = handler.getOWLObject();
		isComplete = (ind != null) && (desc != null);
	}

	@Override
	public void handleChild(OWLIndividualElementHandler handler) throws OWLXMLParserException {
		super.handleChild( handler );
		isComplete = (ind != null) && (desc != null);
	}
}
