package com.clarkparsia.dig20.xml;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.xml.asks.AskQueryType;
import com.clarkparsia.dig20.xml.asks.AsksQuerySAXHandler;
import com.clarkparsia.dig20.xml.asks.AsksQuerySAXHandlerFactory;

public class AsksElementHandler extends StackableHandler implements
		SAXHandlerParent<AsksQuerySAXHandler> {

	private SAXHandlerParent<AsksElementHandler>	parent	= null;
	private List<AskQuery>							queries	= new ArrayList<AskQuery>();
	final private URI								uri;

	public AsksElementHandler(SAXHandlerParent<AsksElementHandler> parent, Attributes atts,
			Locator locator) throws SAXParseException {
		this.parent = parent;
		final String uriString = atts.getValue( DigVocabulary.URI_ATTRIBUTE );
		if( uriString == null )
			throw new SAXParseException( "Missing KB uri attribute", locator );
		try {
			uri = new URI( uriString );
		} catch( URISyntaxException e ) {
			throw new SAXParseException( "Invalid URI for KB uri attribute", locator, e );
		}

		if( locator != null )
			setDocumentLocator( locator );
	}

	public List<AskQuery> getQueries() {
		return queries;
	}

	public URI getURI() {
		return uri;
	}

	public void handleChild(AsksQuerySAXHandler child) throws SAXException {

		if( this.child != child )
			throw new RuntimeException( "Child does not match argument" );

		queries.add( child.getQuery() );

		this.child = null;
	}

	@Override
	public ContentHandler newChild(String uri, String localName, String name, Attributes atts)
			throws SAXException {
		AskQueryType q = AskQueryType.get( uri + localName );
		if( q == null )
			throw new SAXParseException( "Unexpected Element " + uri + localName, locator );

		return AsksQuerySAXHandlerFactory.theInstance().create( this, q, atts );
	}

	@Override
	public void notifyParent() throws SAXException {
		parent.handleChild( this );
	}

}