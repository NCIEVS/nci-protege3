package com.clarkparsia.dig20.xml.responses;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.clarkparsia.dig20.responses.Response;
import com.clarkparsia.dig20.xml.SAXHandlerParent;
import com.clarkparsia.dig20.xml.StackableHandler;
import com.clarkparsia.dig20.xml.explanation.ExplanationVocabulary;
import com.clarkparsia.dig20.xml.explanation.ExplanationsSAXHandler;

public class ResponsesElementHandler extends StackableHandler {

	SAXHandlerParent<ResponsesElementHandler>	parent;
	Map<String, Response>						responseMap	= new HashMap<String, Response>();

	public ResponsesElementHandler(SAXHandlerParent<ResponsesElementHandler> parent) {
		this.parent = parent;
	}

	public Collection<Response> getResponses() {
		return responseMap.values();
	}

	public Response getResponse(String id) {
		return responseMap.get( id );
	}

	@Override
	public ContentHandler newChild(String uri, String localName, String name, Attributes atts)
			throws SAXException {
		if( (uri + localName).equals( ResponsesVocabulary.Elements.RESPONSE.toString() ) ) {
			return new ResponseElementHandler( new SAXHandlerParent<ResponseElementHandler>() {
				public void handleChild(ResponseElementHandler child) throws SAXException {
					if( ResponsesElementHandler.this.child != child )
						throw new SAXException( "Child does not match argument" );
					Response r = child.getResponse();
					responseMap.put( r.getId(), r );
					ResponsesElementHandler.this.child = null;
				}
			}, atts );
		}
		else if( (uri + localName).equals( ExplanationVocabulary.Elements.EXPLANATIONS.toString() ) ) {
			return new ExplanationsSAXHandler( new SAXHandlerParent<ExplanationsSAXHandler>() {
				public void handleChild(ExplanationsSAXHandler child) throws SAXException {
					if( ResponsesElementHandler.this.child != child )
						throw new SAXException( "Child does not match argument" );
					Response r = child.getResponse();
					responseMap.put( r.getId(), r );
					ResponsesElementHandler.this.child = null;
				}
			}, atts );
		}
		else
			throw new SAXParseException( "Unexpected Element " + uri + localName, locator );
	}

	@Override
	public void notifyParent() throws SAXException {
		parent.handleChild( this );
	}

}
