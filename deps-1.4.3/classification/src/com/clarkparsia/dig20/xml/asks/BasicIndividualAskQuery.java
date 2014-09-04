package com.clarkparsia.dig20.xml.asks;

import static com.clarkparsia.dig20.asks.AskQueryFactory.unrecognized;

import org.coode.owl.owlxmlparser.OWLIndividualElementHandler;
import org.coode.owl.owlxmlparser.OWLXMLParserException;
import org.semanticweb.owl.model.OWLIndividual;
import org.xml.sax.Attributes;

import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.xml.SAXHandlerParent;

public class BasicIndividualAskQuery extends AbstractBaseAskQuery<OWLIndividual> {

	protected OWLIndividual	ind;

	public BasicIndividualAskQuery(SAXHandlerParent<AsksQuerySAXHandler> parent, AskQueryType t,
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
	public void handleChild(OWLIndividualElementHandler handler) throws OWLXMLParserException {
		ind = handler.getOWLObject();
		isComplete = true;
	}

}
