/**
 * 
 */
package com.clarkparsia.dig20.xml;

import java.util.ArrayList;
import java.util.List;

import org.coode.owl.owlxmlparser.AbstractOWLAxiomElementHandler;
import org.coode.owl.owlxmlparser.OWLElementHandler;
import org.coode.owl.owlxmlparser.OWLXMLParserException;
import org.semanticweb.owl.model.OWLAxiom;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class AxiomsElementHandler extends DefaultOWLXMLParentElementHandler<List<OWLAxiom>>
		implements OWLElementHandler<List<OWLAxiom>> {

	private List<OWLAxiom>							axioms	= new ArrayList<OWLAxiom>();
	private SAXHandlerParent<AxiomsElementHandler>	parent;

	public AxiomsElementHandler(SAXHandlerParent<AxiomsElementHandler> parent, Attributes atts,
			Locator locator) throws SAXParseException {
		this.parent = parent;

		if( locator != null )
			setDocumentLocator( locator );
	}

	public List<OWLAxiom> getAxioms() {
		return axioms;
	}

	public List<OWLAxiom> getOWLObject() throws OWLXMLParserException {
		return axioms;
	}

	@Override
	public void handleChild(AbstractOWLAxiomElementHandler handler) {
		axioms.add( handler.getOWLObject() );
	}

	@Override
	protected void notifyParent() throws SAXException {
		parent.handleChild( this );
	}
}