package com.clarkparsia.dig20.xml;

import java.net.URI;
import java.net.URISyntaxException;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p>
 * Title: Extended Axioms Element Handler
 * </p>
 * <p>
 * Description: Handler for axioms element requiring id and uri attributes
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Mike Smith
 */
public class ExtendedAxiomsElementHandler extends AxiomsElementHandler {

	final private String											id;
	final private SAXHandlerParent<ExtendedAxiomsElementHandler>	parent;
	final private URI												uri;

	public ExtendedAxiomsElementHandler(SAXHandlerParent<ExtendedAxiomsElementHandler> parent,
			Attributes atts, Locator locator) throws SAXParseException {
		super( null, atts, locator );

		this.parent = parent;

		final String uriString = atts.getValue( DigVocabulary.URI_ATTRIBUTE );
		if( uriString == null )
			throw new SAXParseException( "Missing KB uri attribute", locator );
		try {
			uri = new URI( uriString );
		} catch( URISyntaxException e ) {
			throw new SAXParseException( "Invalid URI for KB uri attribute", locator, e );
		}

		id = atts.getValue( DigVocabulary.ID_ATTRIBUTE );
		if( id == null )
			throw new SAXParseException( "Missing axioms id attribute", locator );
	}

	public String getId() {
		return id;
	}

	public URI getURI() {
		return uri;
	}

	@Override
	protected void notifyParent() throws SAXException {
		parent.handleChild( this );
	}

}