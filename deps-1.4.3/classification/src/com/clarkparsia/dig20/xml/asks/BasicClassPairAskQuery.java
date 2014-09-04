package com.clarkparsia.dig20.xml.asks;

import static com.clarkparsia.dig20.asks.AskQueryFactory.isEquivalentClassTo;
import static com.clarkparsia.dig20.asks.AskQueryFactory.unrecognized;

import java.util.List;

import org.coode.owl.owlxmlparser.AbstractOWLDescriptionElementHandler;
import org.coode.owl.owlxmlparser.OWLXMLParserException;
import org.semanticweb.owl.model.OWLDescription;
import org.xml.sax.Attributes;

import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.xml.SAXHandlerParent;

public class BasicClassPairAskQuery extends AbstractBaseAskQuery<List<OWLDescription>> {

	private int					nFound	= 0;
	protected OWLDescription[]	pair	= new OWLDescription[2];

	public BasicClassPairAskQuery(SAXHandlerParent<AsksQuerySAXHandler> parent, AskQueryType t,
			Attributes attributes) {
		super( parent, t, attributes );
	}

	public AskQuery getQuery() {
		switch ( type ) {
		case IS_EQUIVALENT_CLASS_TO:
			return isEquivalentClassTo( id, pair[0], pair[1] );
		default:
			return unrecognized( id );
		}
	}

	@Override
	public void handleChild(AbstractOWLDescriptionElementHandler handler)
			throws OWLXMLParserException {
		pair[nFound++] = handler.getOWLObject();
		isComplete = (nFound == 2);
	}
}
