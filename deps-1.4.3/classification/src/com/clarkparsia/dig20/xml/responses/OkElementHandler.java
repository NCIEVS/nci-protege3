package com.clarkparsia.dig20.xml.responses;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.clarkparsia.dig20.xml.SAXHandlerParent;

public class OkElementHandler extends DefaultHandler {

	private Locator								locator;
	private SAXHandlerParent<OkElementHandler>	parent;

	public OkElementHandler(SAXHandlerParent<OkElementHandler> parent) {
		this.parent = parent;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		parent.handleChild( this );
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