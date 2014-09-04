/**
 * 
 */
package com.clarkparsia.dig20.xml;

import java.util.Collection;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.helpers.DefaultHandler;

import com.clarkparsia.dig20.responses.Response;
import com.clarkparsia.dig20.xml.responses.ResponsesElementHandler;
import com.clarkparsia.dig20.xml.responses.ResponsesVocabulary;

public class ResponsesDefaultHandler extends DefaultHandler implements
		SAXHandlerParent<ResponsesElementHandler> {

	private ResponsesElementHandler	child;
	private Locator					locator;

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if( child != null )
			child.characters( ch, start, length );
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		if( child != null )
			child.endElement( uri, localName, name );
	}

	public Collection<Response> getResponses() {
		return child.getResponses();
	}

	public Response getResponse(String id) {
		return child.getResponse( id );
	}

	public void handleChild(ResponsesElementHandler child) throws SAXException {
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
		if( child != null )
			child.setDocumentLocator( locator );
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes atts)
			throws SAXException {
		if( child == null ) {
			if( (uri + localName).equals( ResponsesVocabulary.Elements.RESPONSES.toString() ) ) {
				child = new ResponsesElementHandler( this );
				if( locator != null )
					child.setDocumentLocator( locator );
			}
			else
				throw new SAXNotRecognizedException();
		}
		else {
			child.startElement( uri, localName, name, atts );
		}
	}
}