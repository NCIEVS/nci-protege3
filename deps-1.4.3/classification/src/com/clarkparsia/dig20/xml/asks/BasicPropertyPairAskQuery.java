package com.clarkparsia.dig20.xml.asks;

import static com.clarkparsia.dig20.asks.AskQueryFactory.unrecognized;

import java.util.List;

import org.coode.owl.owlxmlparser.AbstractOWLObjectPropertyElementHandler;
import org.coode.owl.owlxmlparser.OWLDataPropertyElementHandler;
import org.coode.owl.owlxmlparser.OWLXMLParserException;
import org.semanticweb.owl.model.OWLPropertyExpression;
import org.xml.sax.Attributes;

import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.xml.SAXHandlerParent;

public class BasicPropertyPairAskQuery extends AbstractBaseAskQuery<List<OWLPropertyExpression<?, ?>>> {

	private int						nFound	= 0;
	private OWLPropertyExpression<?, ?>[]	pair	= new OWLPropertyExpression[2];

	public BasicPropertyPairAskQuery(SAXHandlerParent<AsksQuerySAXHandler> parent, AskQueryType t,
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
	public void handleChild(AbstractOWLObjectPropertyElementHandler handler)
			throws OWLXMLParserException {
		pair[nFound++] = handler.getOWLObject();
		isComplete = (nFound == 2);
	}

	@Override
	public void handleChild(OWLDataPropertyElementHandler handler) throws OWLXMLParserException {
		pair[nFound++] = handler.getOWLObject();
		isComplete = (nFound == 2);
	}
}
