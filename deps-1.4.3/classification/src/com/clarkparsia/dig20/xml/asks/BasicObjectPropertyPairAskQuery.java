package com.clarkparsia.dig20.xml.asks;

import static com.clarkparsia.dig20.asks.AskQueryFactory.unrecognized;

import java.util.List;

import org.coode.owl.owlxmlparser.AbstractOWLObjectPropertyElementHandler;
import org.coode.owl.owlxmlparser.OWLXMLParserException;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.xml.sax.Attributes;

import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.xml.SAXHandlerParent;

public class BasicObjectPropertyPairAskQuery extends
		AbstractBaseAskQuery<List<OWLObjectPropertyExpression>> {

	private int								nFound	= 0;
	private OWLObjectPropertyExpression[]	pair	= new OWLObjectPropertyExpression[2];

	public BasicObjectPropertyPairAskQuery(SAXHandlerParent<AsksQuerySAXHandler> parent,
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
	public void handleChild(AbstractOWLObjectPropertyElementHandler handler)
			throws OWLXMLParserException {
		pair[nFound++] = handler.getOWLObject();
		isComplete = (nFound == 2);
	}
}
