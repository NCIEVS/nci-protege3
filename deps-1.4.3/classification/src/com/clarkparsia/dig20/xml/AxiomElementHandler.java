/**
 * 
 */
package com.clarkparsia.dig20.xml;

import org.coode.owl.owlxmlparser.AbstractOWLAxiomElementHandler;
import org.coode.owl.owlxmlparser.OWLXMLParserException;
import org.semanticweb.owl.model.OWLAxiom;
import org.xml.sax.SAXException;

public class AxiomElementHandler extends DefaultOWLXMLParentElementHandler<OWLAxiom> {

	private OWLAxiom								axiom;
	private SAXHandlerParent<AxiomElementHandler>	parent;

	public AxiomElementHandler(SAXHandlerParent<AxiomElementHandler> parent) {
		this.parent = parent;
	}

	public OWLAxiom getAxiom() {
		if( axiom == null )
			throw new NullPointerException();

		return axiom;
	}

	public OWLAxiom getOWLObject() throws OWLXMLParserException {
		return axiom;
	}

	@Override
	public void handleChild(AbstractOWLAxiomElementHandler handler) {
		assert axiom == null : "Multiple handleChild calls";

		axiom = handler.getOWLObject();
		isComplete = true;
	}

	@Override
	protected void notifyParent() throws SAXException {
		parent.handleChild( this );
	}
}