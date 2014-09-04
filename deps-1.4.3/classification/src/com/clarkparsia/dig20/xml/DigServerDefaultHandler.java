package com.clarkparsia.dig20.xml;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.model.OWLAxiom;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.helpers.DefaultHandler;

import com.clarkparsia.dig20.RetractAxioms;
import com.clarkparsia.dig20.TellAxioms;
import com.clarkparsia.dig20.asks.AskQuery;

public class DigServerDefaultHandler extends DefaultHandler {

	private ContentHandler				child;
	private boolean						inDoc	= false;
	private Locator						locator;
	private Map<URI, List<AskQuery>>	queries;
	private List<RetractAxioms>			retractions;
	private List<TellAxioms>			tells;

	public DigServerDefaultHandler() {
		queries = new HashMap<URI, List<AskQuery>>();
	}

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

	public List<TellAxioms> getTells() {
		if( tells != null )
			return Collections.unmodifiableList( tells );
		else
			return Collections.emptyList();
	}

	public Set<URI> getKbURIs() {
		return Collections.unmodifiableSet( queries.keySet() );
	}

	public List<AskQuery> getQueries(URI kbURI) {
		List<AskQuery> l = queries.get( kbURI );
		if( l != null )
			return Collections.unmodifiableList( l );
		else
			return Collections.emptyList();
	}
	
	public int getQueryCount() {
		int count = 0;
		for (List<AskQuery> q : queries.values()) {
			count += q.size();
		}
		return count;
	}

	public List<RetractAxioms> getRetractions() {
		if( retractions != null )
			return Collections.unmodifiableList( retractions );
		else
			return Collections.emptyList();
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
		if( child != null )
			child.setDocumentLocator( locator );
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes)
			throws SAXException {
		if( child != null )
			child.startElement( uri, localName, name, attributes );
		else {
			final String full = uri + localName;
			if( !inDoc ) {
				if( full.equals( DigVocabulary.DIG.toString() ) )
					inDoc = true;
				else
					throw new SAXNotRecognizedException( full );
			}
			else {

				if( full.equals( DigVocabulary.AXIOMS.toString() ) ) {

					child = new ExtendedAxiomsElementHandler(
							new SAXHandlerParent<ExtendedAxiomsElementHandler>() {

								public void handleChild(ExtendedAxiomsElementHandler child)
										throws SAXException {
									assert DigServerDefaultHandler.this.child == child;
									if( tells == null )
										tells = new ArrayList<TellAxioms>();
									tells.add( new TellAxioms( child.getId(), child.getURI(), child
											.getAxioms() ) );
									DigServerDefaultHandler.this.child = null;
								}

							}, attributes, locator );

				}
				else if( full.equals( DigVocabulary.RETRACTIONS.toString() ) ) {

					child = new ExtendedAxiomsElementHandler(
							new SAXHandlerParent<ExtendedAxiomsElementHandler>() {

								public void handleChild(ExtendedAxiomsElementHandler child)
										throws SAXException {
									assert DigServerDefaultHandler.this.child == child;
									if( retractions == null )
										retractions = new ArrayList<RetractAxioms>();
									retractions.add( new RetractAxioms( child.getId(), child
											.getURI(), child.getAxioms() ) );
									DigServerDefaultHandler.this.child = null;
								}

							}, attributes, locator );

				}
				else if( full.equals( DigVocabulary.ASKS.toString() ) ) {

					child = new AsksElementHandler( new SAXHandlerParent<AsksElementHandler>() {

						public void handleChild(AsksElementHandler child) throws SAXException {
							assert DigServerDefaultHandler.this.child == child;
							URI u = child.getURI();
							List<AskQuery> l = queries.get( u );
							if( l == null ) {
								l = new ArrayList<AskQuery>();
								queries.put( u, l );
							}
							l.addAll( child.getQueries() );
							DigServerDefaultHandler.this.child = null;
						}

					}, attributes, locator );

				}
				else
					throw new SAXNotRecognizedException( full );

				if( locator != null )
					child.setDocumentLocator( locator );
			}
		}
	}
}
