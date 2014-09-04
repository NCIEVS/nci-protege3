package com.clarkparsia.dig20.xml;

import java.net.URI;

import org.coode.owl.owlxmlparser.AbstractOWLAxiomElementHandler;
import org.coode.owl.owlxmlparser.AbstractOWLDataRangeHandler;
import org.coode.owl.owlxmlparser.AbstractOWLDescriptionElementHandler;
import org.coode.owl.owlxmlparser.AbstractOWLObjectPropertyElementHandler;
import org.coode.owl.owlxmlparser.OWLAnnotationElementHandler;
import org.coode.owl.owlxmlparser.OWLConstantElementHandler;
import org.coode.owl.owlxmlparser.OWLDataPropertyElementHandler;
import org.coode.owl.owlxmlparser.OWLDatatypeFacetRestrictionElementHandler;
import org.coode.owl.owlxmlparser.OWLElementHandler;
import org.coode.owl.owlxmlparser.OWLIndividualElementHandler;
import org.coode.owl.owlxmlparser.OWLSubObjectPropertyChainElementHandler;
import org.coode.owl.owlxmlparser.OWLXMLParserException;
import org.coode.owl.owlxmlparser.OWLXMLParserHandler;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class DefaultOWLXMLParentElementHandler<O> extends DefaultHandler implements
		OWLElementHandler<O> {

	static private OWLOntology			emptyOnt;
	static private OWLOntologyManager	manager;
	static {
		manager = OWLManager.createOWLOntologyManager();
		try {
			emptyOnt = manager.createOntology( URI
					.create( "http://clarkparsia.com/dig20/xml/empty-ont" ) );
		} catch( OWLOntologyCreationException e ) {
			throw new RuntimeException( e );
		}
	}

	private int							childCount	= 0;

	/**
	 * Flag used to indicate if complete data has been parsed from child
	 * elements. If flag is {@code true} and additional child elements are
	 * encountered a {@code SAXParseException} will be thrown.
	 */
	protected boolean					isComplete	= false;

	protected Locator					locator;

	protected OWLXMLParserHandler		owlParser;

	public DefaultOWLXMLParentElementHandler() {
		this.owlParser = new OWLXMLParserHandler( manager, emptyOnt, this );
	}

	public void attribute(String localName, String value) throws OWLXMLParserException {
		throw new IllegalStateException();
	}

	@Override
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		if( childCount == 0 ) {
			// TODO: check that characters are whitespace
		}
		else {
			owlParser.characters( arg0, arg1, arg2 );
		}
	}

	public void endElement() throws OWLXMLParserException {
		throw new IllegalStateException();
	}

	protected abstract void notifyParent() throws SAXException;

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		if( childCount != 0 ) {
			childCount--;
			owlParser.endElement( uri, localName, name );
		}
		else {
			super.endElement( uri, localName, name );
			notifyParent();
		}
	}

	public String getText() {
		throw new IllegalStateException();
	}

	public void handleChars(char[] chars, int start, int length) throws OWLXMLParserException {
		throw new IllegalStateException();
	}

	public void handleChild(AbstractOWLAxiomElementHandler handler) throws OWLXMLParserException {
		unexpected( "Axiom" );
	}

	public void handleChild(AbstractOWLDataRangeHandler handler) throws OWLXMLParserException {
		unexpected( "DataRange" );
	}

	public void handleChild(AbstractOWLDescriptionElementHandler handler)
			throws OWLXMLParserException {
		unexpected( "Description" );
	}

	public void handleChild(AbstractOWLObjectPropertyElementHandler handler)
			throws OWLXMLParserException {
		unexpected( "ObjectProperty" );
	}

	public void handleChild(OWLAnnotationElementHandler handler) throws OWLXMLParserException {
		unexpected( "Annotation" );
	}

	public void handleChild(OWLConstantElementHandler handler) throws OWLXMLParserException {
		unexpected( "Constant" );
	}

	public void handleChild(OWLDataPropertyElementHandler handler) throws OWLXMLParserException {
		unexpected( "DataProperty" );
	}

	public void handleChild(OWLDatatypeFacetRestrictionElementHandler handler)
			throws OWLXMLParserException {
		unexpected( "Datatype Facet" );
	}

	public void handleChild(OWLIndividualElementHandler handler) throws OWLXMLParserException {
		unexpected( "Individual" );
	}

	public void handleChild(OWLSubObjectPropertyChainElementHandler handler)
			throws OWLXMLParserException {
		unexpected( "SubObjectPropertyChain" );
	}

	public boolean isTextContentPossible() {
		return false;
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		super.setDocumentLocator( locator );
		this.locator = locator;
		owlParser.setDocumentLocator( locator );
	}

	@SuppressWarnings("unchecked")
	public void setParentHandler(OWLElementHandler handler) throws OWLXMLParserException {
		throw new IllegalStateException();
	}

	public void startElement(String name) throws OWLXMLParserException {
		throw new IllegalStateException();
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes)
			throws SAXException {
		super.startElement( uri, localName, name, attributes );

		if( isComplete )
			throw new SAXParseException( "Unexpected child element found", locator );

		childCount++;
		owlParser.startElement( uri, localName, name, attributes );
	}

	private void unexpected(String s) throws OWLXMLParserException {
		throw new OWLXMLParserException( locator.getLineNumber(), new SAXParseException(
				"Child OWL " + s + " element unexpected", locator ) );
	}
}