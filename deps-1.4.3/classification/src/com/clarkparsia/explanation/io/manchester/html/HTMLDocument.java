package com.clarkparsia.explanation.io.manchester.html;

import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description: A convenience class to make generating HTML easier and cleaner
 * by the use of DOM.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Evren Sirin
 */
public class HTMLDocument {
	public static void main(String[] args) throws Exception {
		HTMLDocument html = new HTMLDocument();

		html.append( "Hello," ).space().append( "world!" );

		html.serialize( new PrintWriter( System.out ) );		
	}

	public static String shortForm(URI theURI) {
		String fragment = theURI.getFragment();
		if( fragment != null ) {
			return fragment;
		}
		int lastSlashIndex = theURI.getPath().lastIndexOf( '/' );
		if( lastSlashIndex != -1 ) {
			return theURI.getPath().substring( lastSlashIndex + 1, theURI.getPath().length() );
		}
		return theURI.toString();
	}

	private Document	document;
	
	private Element		html;
	private Element		body;

	private Element		currentElement;

	private Node		previousNode;

	public HTMLDocument() {
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch( ParserConfigurationException e ) {
			throw new RuntimeException( e );
		}

		html = document.createElement( "html" );
		body = document.createElement( "body" );

		document.appendChild( html );
		html.appendChild( body );

		currentElement = body;
	}

	public HTMLDocument append(String text) {
		Node textNode = document.createTextNode( text );
		currentElement.appendChild( textNode );
		previousNode = textNode;

		return this;
	}

	public HTMLDocument br() {
		return start( "br" ).end();
	}

	public HTMLDocument end() {
		previousNode = currentElement;
		currentElement = (Element) currentElement.getParentNode();

		if( currentElement == null )
			throw new IllegalStateException( "No element to end!" );

		return this;
	}

	public HTMLDocument end(String tag) {
		while( !currentElement.getTagName().equals( tag ) ) {
			currentElement = (Element) currentElement.getParentNode();

			if( currentElement == null )
				throw new IllegalStateException( "Trying to end a non-existent tag: " + tag );
		}

		return this;
	}

	public HTMLDocument entity(String entity) {
		Node entityNode = document.createEntityReference( entity );
		currentElement.appendChild( entityNode );
		previousNode = entityNode;

		return this;
	}

	public Element getBodyElement() {
		return body;
	}

	public Element getCurrentElement() {
		return currentElement;
	}

	public Document getDocument() {
		return document;
	}

	public Element getHTMLElement() {
		return html;
	}

	public Node getPreviousElement() {
		return previousNode;
	}

	public HTMLDocument hr() {
		return start( "hr" ).end();
	}

	public HTMLDocument link(URI href) {
		return link( href, shortForm( href ) );
	}

	public HTMLDocument link(URI href, String text) {
		start( "a" );
		setAttribute( "href", href.toString() );
		append( text );
		end();

		return this;
	}

	/**
	 * @param writer
	 */
	public void serialize(Writer writer) {
		try {
			Source aSource = new DOMSource( document );
			StreamResult aStreamResult = new StreamResult( writer );

			Transformer aTransformer = TransformerFactory.newInstance().newTransformer();

			aTransformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
			aTransformer.setOutputProperty( OutputKeys.INDENT, "yes" );
			aTransformer.setOutputProperty( OutputKeys.METHOD, "html" );
			aTransformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );

			aTransformer.transform( aSource, aStreamResult );

			writer.flush();
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	public HTMLDocument setAttribute(String name, String value) {
		currentElement.setAttribute( name, value );

		return this;
	}

	public HTMLDocument space() {
		return entity( "nbsp" );
	}

	public HTMLDocument start(String tag) {
		previousNode = currentElement;

		Element newElement = document.createElement( tag );
		currentElement.appendChild( newElement );
		currentElement = newElement;

		return this;
	}
}
