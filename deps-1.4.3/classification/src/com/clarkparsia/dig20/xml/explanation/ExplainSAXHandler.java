package com.clarkparsia.dig20.xml.explanation;

import static com.clarkparsia.dig20.explanation.ExplanationFactory.explain;

import org.coode.owl.owlxmlparser.AbstractOWLAxiomElementHandler;
import org.coode.owl.owlxmlparser.OWLXMLParserException;
import org.semanticweb.owl.model.OWLAxiom;
import org.xml.sax.Attributes;

import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.xml.SAXHandlerParent;
import com.clarkparsia.dig20.xml.asks.AbstractBaseAskQuery;
import com.clarkparsia.dig20.xml.asks.AskQueryType;
import com.clarkparsia.dig20.xml.asks.AsksQuerySAXHandler;

public class ExplainSAXHandler extends AbstractBaseAskQuery<OWLAxiom> {

	private OWLAxiom	axiom;
	private Integer		max;

	public ExplainSAXHandler(SAXHandlerParent<AsksQuerySAXHandler> parent, AskQueryType t,
			Attributes attr) {
		super( parent, null, attr );
		String s = attr.getValue( ExplanationVocabulary.NEXP_ATTRIBUTE );
		max = (s == null)
			? null
			: Integer.parseInt( s );
	}

	public AskQuery getQuery() {
		return (max == null)
			? explain( id, axiom )
			: explain( id, axiom, max );
	}

	@Override
	public void handleChild(AbstractOWLAxiomElementHandler handler) throws OWLXMLParserException {
		axiom = handler.getOWLObject();
		isComplete = true;
	}
}
