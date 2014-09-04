package com.clarkparsia.dig20.xml.responses;

import static com.clarkparsia.dig20.responses.BooleanAskResult.result;
import static com.clarkparsia.dig20.responses.ErrorResponse.error;
import static com.clarkparsia.dig20.responses.OkResponse.ok;
import static com.clarkparsia.dig20.responses.SynonymsAskResult.result;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.semanticweb.owl.model.OWLObject;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.clarkparsia.dig20.responses.ErrorResponse;
import com.clarkparsia.dig20.responses.Response;
import com.clarkparsia.dig20.xml.SAXHandlerParent;
import com.clarkparsia.dig20.xml.StackableHandler;

public class ResponseElementHandler extends StackableHandler {

	private enum ChildType {

		BOOLEAN(ResponsesVocabulary.Elements.BOOLEAN), OK(ResponsesVocabulary.Elements.OK),
		ERROR(ResponsesVocabulary.Elements.ERROR), SYNONYMS(ResponsesVocabulary.Elements.SYNONYMS);

		private final URI	uri;

		private ChildType(ResponsesVocabulary.Elements e) {
			this.uri = e.getURI();
		}

		public static ChildType get(String uri) {
			for( ChildType t : values() ) {
				if( t.uri.toString().equals( uri ) )
					return t;
			}
			return null;
		}
	}

	private boolean										b;
	private ErrorResponse								error;
	private String										id;
	private boolean										isBool;
	private boolean										isComplete;
	private List<List<OWLObject>>						objects;
	private SAXHandlerParent<ResponseElementHandler>	parent;
	private ChildType									childType;

	public ResponseElementHandler(SAXHandlerParent<ResponseElementHandler> parent, Attributes atts) {
		this.parent = parent;
		this.id = atts.getValue( ResponsesVocabulary.ID_ATTRIBUTE );

		if( this.id == null )
			throw new RuntimeException( new SAXParseException(
					"No query identifier attribute found.", locator ) );

		error = null;
		isComplete = false;
	}

	public Response getResponse() {
		if( error != null )
			return error;
		
		if (childType == null)
			childType = ChildType.SYNONYMS;

		switch ( childType ) {
		case BOOLEAN:
			return result( id, b );
		case OK:
			return ok( id );
		case SYNONYMS:
			if( objects == null )
				objects = Collections.emptyList();
			return result( id, objects );
		default:
			throw new IllegalStateException();
		}

	}

	@Override
	public ContentHandler newChild(String uri, String localName, String qName, Attributes attributes)
			throws SAXException {

		if( isComplete )
			throw new SAXParseException( "Unexpected additional child element", locator );

		childType = ChildType.get( uri + localName );
		if (childType == null)
			throw new SAXParseException( "Unexpected Element " + uri + localName, locator );

		switch ( childType ) {
		case BOOLEAN:
			return new BooleanElementHandler( new SAXHandlerParent<BooleanElementHandler>() {
				public void handleChild(BooleanElementHandler child) throws SAXException {
					if( ResponseElementHandler.this.child != child )
						throw new SAXException( "Child does not match argument" );
					b = child.getBoolean();
					ResponseElementHandler.this.child = null;
				}
			} );

		case OK:
			return new OkElementHandler( new SAXHandlerParent<OkElementHandler>() {
				public void handleChild(OkElementHandler child) throws SAXException {
					if( ResponseElementHandler.this.child != child )
						throw new SAXException( "Child does not match argument" );
					ResponseElementHandler.this.child = null;
				}
			} );

		case SYNONYMS:
			return new SynonymsElementHandler( new SAXHandlerParent<SynonymsElementHandler>() {
				public void handleChild(SynonymsElementHandler child) throws SAXException {
					if( ResponseElementHandler.this.child != child )
						throw new SAXException( "Child does not match argument" );
					if( objects == null )
						objects = new ArrayList<List<OWLObject>>();
					objects.add( child.getObjects() );
					ResponseElementHandler.this.child = null;
				}
			} );

		case ERROR:
			return new MessageHandler( new SAXHandlerParent<MessageHandler>() {
				public void handleChild(MessageHandler child) throws SAXException {
					if( ResponseElementHandler.this.child != child )
						throw new SAXException( "Child does not match argument" );
					ResponseElementHandler.this.error = error( ResponseElementHandler.this.id,
							child.getCode(), child.getContent() );
					ResponseElementHandler.this.isComplete = true;
					ResponseElementHandler.this.child = null;
				}
			}, attributes );

		default:
			throw new SAXParseException( "Unexpected Element " + uri + localName, locator );
		}
	}

	@Override
	public void notifyParent() throws SAXException {
		parent.handleChild( this );
	}
}