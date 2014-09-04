package com.clarkparsia.dig20.xml.responses;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.clarkparsia.dig20.xml.SAXHandlerParent;

public class BooleanElementHandler extends DefaultHandler {

	private StringBuilder							allChars	= new StringBuilder();
	private boolean									b;
	private Locator									locator;
	private SAXHandlerParent<BooleanElementHandler>	parent;

	public BooleanElementHandler(SAXHandlerParent<BooleanElementHandler> parent) {
		this.parent = parent;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		allChars.append( ch, start, length );
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		b = Boolean.parseBoolean( allChars.toString() );
		parent.handleChild( this );
	}

	public boolean getBoolean() {
		return b;
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attr)
			throws SAXException {
		throw new SAXParseException( "Unexpected child element", locator );
	}
}