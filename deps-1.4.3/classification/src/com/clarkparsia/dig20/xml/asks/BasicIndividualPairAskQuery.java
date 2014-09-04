package com.clarkparsia.dig20.xml.asks;

import static com.clarkparsia.dig20.asks.AskQueryFactory.unrecognized;

import java.util.List;

import org.coode.owl.owlxmlparser.OWLIndividualElementHandler;
import org.coode.owl.owlxmlparser.OWLXMLParserException;
import org.semanticweb.owl.model.OWLIndividual;
import org.xml.sax.Attributes;

import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.xml.SAXHandlerParent;

public class BasicIndividualPairAskQuery extends AbstractBaseAskQuery<List<OWLIndividual>> {

	protected int			nFound	= 0;
	private OWLIndividual[]	pair	= new OWLIndividual[2];

	public BasicIndividualPairAskQuery(SAXHandlerParent<AsksQuerySAXHandler> parent,
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
	public void handleChild(OWLIndividualElementHandler handler) throws OWLXMLParserException {
		pair[nFound++] = handler.getOWLObject();
		isComplete = (nFound == 2);
	}
}
