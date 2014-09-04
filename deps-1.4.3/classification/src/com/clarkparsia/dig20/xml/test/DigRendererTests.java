package com.clarkparsia.dig20.xml.test;

import static com.clarkparsia.dig20.asks.AskQueryFactory.consistentkb;
import static com.clarkparsia.dig20.asks.AskQueryFactory.equivalentClasses;
import static com.clarkparsia.dig20.asks.AskQueryFactory.isSatisfiable;
import static com.clarkparsia.dig20.asks.AskQueryFactory.isSubClassOf;
import static com.clarkparsia.dig20.asks.AskQueryFactory.namedClasses;
import static com.clarkparsia.dig20.asks.AskQueryFactory.namedDataProperties;
import static com.clarkparsia.dig20.asks.AskQueryFactory.namedIndividuals;
import static com.clarkparsia.dig20.asks.AskQueryFactory.namedObjectProperties;
import static com.clarkparsia.dig20.asks.AskQueryFactory.subClasses;
import static com.clarkparsia.dig20.asks.AskQueryFactory.superClasses;
import static com.clarkparsia.dig20.explanation.ExplanationFactory.explain;
import static com.clarkparsia.dig20.explanation.ExplanationsResponse.explanation;
import static com.clarkparsia.dig20.explanation.ExplanationsResponse.explanations;
import static com.clarkparsia.dig20.responses.BooleanAskResult.result;
import static com.clarkparsia.dig20.responses.ErrorResponse.error;
import static com.clarkparsia.dig20.responses.SynonymsAskResult.result;
import static com.clarkparsia.dig20.responses.SynonymsAskResult.synonyms;
import static com.clarkparsia.owlapi.OWL.Class;
import static com.clarkparsia.owlapi.OWL.not;
import static com.clarkparsia.owlapi.OWL.subClassOf;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.responses.Response;
import com.clarkparsia.dig20.xml.DigRenderer;
import com.clarkparsia.dig20.xml.DigVocabulary;
import com.clarkparsia.dig20.xml.responses.ResponsesVocabulary;

public class DigRendererTests {

	private static final String	DATA_BASEDIR	= "test/data/dig20/";
	public static boolean		DEBUG_OUTPUT	= true;
	private static final String	KBURI_BASE		= "tag:clarkparsia.com,2009:dig20:test:";

	private static boolean childMatches(Element e, Element listParent) {

		NodeList list = listParent.getElementsByTagName( e.getNodeName() );

		for( int i = 0; i < list.getLength(); i++ ) {
			Node n = list.item( i );
			if( elementEquals( e, (Element) n ) )
				return true;
		}

		return false;
	}

	private static boolean documentEquals(Document d0, Document d1) {
		return elementEquals( d0.getDocumentElement(), d1.getDocumentElement() )
				&& elementEquals( d1.getDocumentElement(), d0.getDocumentElement() );
	}

	private static boolean elementEquals(Element e0, Element e1) {

		if( !e0.getNodeName().equals( e1.getNodeName() ) )
			return false;

		NodeList childNodes = e0.getChildNodes();
		for( int i = 0; i < childNodes.getLength(); i++ ) {
			Node child = childNodes.item( i );
			if( child instanceof Element ) {
				if( !childMatches( (Element) child, e1 ) )
					return false;
			}
		}

		NamedNodeMap attributeMap = e0.getAttributes();
		for( int i = 0; i < attributeMap.getLength(); i++ ) {
			Attr attr = (Attr) attributeMap.item( i );
			String value = e1.getAttribute( attr.getNodeName() );
			if( value == null || !value.equals( attr.getValue() ) )
				return false;
		}

		return true;
	}

	private static void testRenderEquals(String filename, URI uri, AskQuery... queries) {
		testRenderEquals( filename, toXML( uri, queries ) );
	}

	private static void testRenderEquals(String filename, byte[] data) {

		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document d = parser.parse( new ByteArrayInputStream( data ) );
			d.normalizeDocument();

			if( DEBUG_OUTPUT ) {
				System.err.println( "\n---" );
				Transformer t = TransformerFactory.newInstance().newTransformer();
				t.transform( new DOMSource( d ), new StreamResult( System.err ) );
				System.err.println( "\n---" );
			}

			Document e = parser.parse( new FileInputStream( filename ) );
			e.normalizeDocument();

			assertTrue( documentEquals( d, e ) );
		} catch( Exception e ) {
			throw new RuntimeException( e );
		}

	}

	private static void testRenderEquals(String filename, Response... queries) {
		testRenderEquals( filename, toXML( queries ) );
	}

	private static byte[] toXML(URI uri, AskQuery... queries) {

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			DigRenderer renderer = new DigRenderer();
			renderer.setDocumentTag( DigVocabulary.ASKS.toString() );
			renderer.startRendering( new OutputStreamWriter( out ) );
			renderer.writeAttribute( DigVocabulary.URI_ATTRIBUTE, uri.toASCIIString() );

			for( AskQuery q : queries )
				q.accept( renderer );

			renderer.endRendering();

			return out.toByteArray();
		} catch( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	private static byte[] toXML(Response... responses) {

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			DigRenderer renderer = new DigRenderer();
			renderer.setDocumentTag( ResponsesVocabulary.Elements.RESPONSES.toString() );
			renderer.startRendering( new OutputStreamWriter( out ) );

			for( Response r : responses )
				r.accept( renderer );

			renderer.endRendering();

			return out.toByteArray();
		} catch( Exception e ) {
			throw new RuntimeException( e );
		}

	}

	private static URI uri(String u) {
		return URI.create( u );
	}

	@Test
	public void askClassHierarchyTests() {
		testRenderEquals( DATA_BASEDIR + "ask-classhierarchy.xml", uri( KBURI_BASE
				+ "ask-classhierarchy" ), superClasses( "1", Class( "A" ) ), superClasses( "2",
				not( Class( "B" ) ), true ), subClasses( "3", Class( "A" ) ), subClasses( "4",
				not( Class( "B" ) ), false ), isSubClassOf( "5", Class( "A" ), Class( "B" ) ),
				isSubClassOf( "6", not( Class( "A" ) ), Class( "B" ), true ) );
	}

	@Test
	public void askConsistentTest() {
		testRenderEquals( DATA_BASEDIR + "ask-consistentkb.xml", uri( KBURI_BASE
				+ "ask-consistentkb" ), consistentkb( "1" ) );
	}

	@Test
	public void askEquivalentClassesTest() {
		testRenderEquals( DATA_BASEDIR + "ask-equivalentclasses.xml", uri( KBURI_BASE
				+ "ask-equivalentclasses" ), equivalentClasses( "1", Class( "A" ) ) );
	}

	@Test
	public void askExplainTest() {
		testRenderEquals( DATA_BASEDIR + "ask-explain.xml", uri( KBURI_BASE + "ask-explain" ),
				explain( "1", subClassOf( Class( "A" ), Class( "B" ) ) ), explain( "2", subClassOf(
						not( Class( "A" ) ), Class( "C" ) ), 5 ) );
	}

	@Test
	public void askNamedEntitiesTests() {
		testRenderEquals( DATA_BASEDIR + "ask-namedentities.xml", uri( KBURI_BASE
				+ "ask-namedentities" ), namedClasses( "A" ), namedIndividuals( "B" ),
				namedDataProperties( "C" ), namedObjectProperties( "D" ) );
	}

	@Test
	public void askSatisfiableTests() {
		testRenderEquals( DATA_BASEDIR + "ask-satisfiable.xml",
				uri( KBURI_BASE + "ask-satisfiable" ), isSatisfiable( "1", Class( "A" ) ),
				isSatisfiable( "2", not( Class( "B" ) ) ) );

	}

	@Test
	public void responseBooleanTest() {
		testRenderEquals( DATA_BASEDIR + "response-boolean.xml", result( "1", true ), result( "2",
				false ) );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void responseErrorTest() {
		testRenderEquals( DATA_BASEDIR + "response-error.xml", error( "1", 400,
				"Useful Error Message" ), error( "2", 500 ) );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void responseExplanationsTest() {
		testRenderEquals( DATA_BASEDIR + "response-explanations.xml", explanations( "1",
				explanation( subClassOf( Class( "A" ), Class( "B" ) ) ) ), explanations( "2",
				explanation( subClassOf( Class( "A" ), not( Class( "B" ) ) ) ), explanation(
						subClassOf( Class( "A" ), Class( "C" ) ), subClassOf( Class( "C" ),
								not( Class( "B" ) ) ) ) ) );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void responseSynonymsTest() {
		testRenderEquals( DATA_BASEDIR + "response-synonyms.xml", result( "1", synonyms(
				Class( "A" ), Class( "B" ) ), synonyms( Class( "C" ) ) ), result( "2",
				synonyms( not( Class( "D" ) ) ) ), result( "3" ) );
	}
}
