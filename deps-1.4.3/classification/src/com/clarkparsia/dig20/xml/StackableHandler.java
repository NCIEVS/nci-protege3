package com.clarkparsia.dig20.xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public abstract class StackableHandler implements ContentHandler {

	protected ContentHandler	child;
	protected Locator			locator;

	public void characters(char[] ch, int start, int length) throws SAXException {
		if( child != null )
			child.characters( ch, start, length );
	}

	public void endDocument() throws SAXException {
		if( child != null )
			throw new SAXParseException( "Unexpected end of document", locator );
	}

	public void endElement(String uri, String localName, String name) throws SAXException {
		if( child != null )
			child.endElement( uri, localName, name );
		else
			notifyParent();
	}

	public void endPrefixMapping(String prefix) throws SAXException {
		if( child != null )
			child.endPrefixMapping( prefix );
	}

	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		if( child != null )
			child.characters( ch, start, length );
	}

	public abstract void notifyParent() throws SAXException;

	public abstract ContentHandler newChild(String uri, String localName, String name,
			Attributes atts) throws SAXException;

	public void startElement(String uri, String localName, String name, Attributes atts)
			throws SAXException {
		if( child != null )
			child.startElement( uri, localName, name, atts );
		else {
			child = newChild( uri, localName, name, atts );
			if( locator != null )
				child.setDocumentLocator( locator );
		}

	}

	public void processingInstruction(String target, String data) throws SAXException {
		if( child != null )
			child.processingInstruction( target, data );
	}

	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
		if( child != null )
			child.setDocumentLocator( locator );
	}

	public void skippedEntity(String name) throws SAXException {
		if( child != null )
			child.skippedEntity( name );
	}

	public void startDocument() throws SAXException {
	}

	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		if( child != null )
			child.startPrefixMapping( prefix, uri );
	}
}
