package com.clarkparsia.dig20.xml.responses;

import java.util.ArrayList;
import java.util.List;

import org.coode.owl.owlxmlparser.AbstractOWLDescriptionElementHandler;
import org.coode.owl.owlxmlparser.AbstractOWLObjectPropertyElementHandler;
import org.coode.owl.owlxmlparser.OWLDataPropertyElementHandler;
import org.coode.owl.owlxmlparser.OWLIndividualElementHandler;
import org.coode.owl.owlxmlparser.OWLXMLParserException;
import org.semanticweb.owl.model.OWLObject;
import org.xml.sax.SAXException;

import com.clarkparsia.dig20.xml.DefaultOWLXMLParentElementHandler;
import com.clarkparsia.dig20.xml.SAXHandlerParent;

public class SynonymsElementHandler extends DefaultOWLXMLParentElementHandler<List<OWLObject>> {

	private List<OWLObject>								objects	= new ArrayList<OWLObject>();
	private SAXHandlerParent<SynonymsElementHandler>	parent;

	public SynonymsElementHandler(SAXHandlerParent<SynonymsElementHandler> parent) {
		this.parent = parent;
	}

	public List<OWLObject> getObjects() {
		return objects;
	}

	public List<OWLObject> getOWLObject() throws OWLXMLParserException {
		return objects;
	}

	@Override
	public void handleChild(AbstractOWLDescriptionElementHandler handler)
			throws OWLXMLParserException {
		objects.add( handler.getOWLObject() );
	}

	@Override
	public void handleChild(AbstractOWLObjectPropertyElementHandler handler)
			throws OWLXMLParserException {
		objects.add( handler.getOWLObject() );
	}

	@Override
	public void handleChild(OWLDataPropertyElementHandler handler) throws OWLXMLParserException {
		objects.add( handler.getOWLObject() );
	}

	@Override
	public void handleChild(OWLIndividualElementHandler handler) throws OWLXMLParserException {
		objects.add( handler.getOWLObject() );
	}

	@Override
	protected void notifyParent() throws SAXException {
		parent.handleChild( this );
	}
}
