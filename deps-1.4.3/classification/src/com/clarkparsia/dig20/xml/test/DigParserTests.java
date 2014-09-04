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
import static com.clarkparsia.owlapi.OWL.not;
import static com.clarkparsia.owlapi.OWL.subClassOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Test;
import org.semanticweb.owl.model.OWLClass;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.helpers.DefaultHandler;

import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.responses.Response;
import com.clarkparsia.dig20.xml.AsksElementHandler;
import com.clarkparsia.dig20.xml.DigVocabulary;
import com.clarkparsia.dig20.xml.ResponsesDefaultHandler;
import com.clarkparsia.dig20.xml.SAXHandlerParent;
import com.clarkparsia.owlapi.OWL;

public class DigParserTests {

	public static class AsksDocHandler extends DefaultHandler implements
			SAXHandlerParent<AsksElementHandler> {

		private AsksElementHandler	child;
		private List<AskQuery>		queries	= new ArrayList<AskQuery>();
		private URI					kbURI;
		private Locator				locator;

		@Override
		public void endElement(String uri, String localName, String name) throws SAXException {
			if( child != null )
				child.endElement( uri, localName, name );
		}

		public URI getURI() {
			return kbURI;
		}

		public List<AskQuery> getQueries() {
			return queries;
		}

		public void handleChild(AsksElementHandler child) throws SAXException {
			queries.addAll( child.getQueries() );
			kbURI = child.getURI();
			this.child = null;
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
				if( (uri + localName).equals( DigVocabulary.ASKS.toString() ) ) {
					child = new AsksElementHandler( this, atts, locator );
				}
				else
					throw new SAXNotRecognizedException();
			}
			else {
				child.startElement( uri, localName, name, atts );
			}
		}
	}

	private static final String	DATA_BASEDIR	= "test/data/dig20/";
	private static final String	BASE_URI;
	private static final String	KBURI_BASE;

	static {
		File f = new File( DATA_BASEDIR );
		BASE_URI = f.toURI().toString();
		KBURI_BASE = "tag:clarkparsia.com,2009:dig20:test:";
	}

	private static OWLClass Class(String localName) {
		return OWL.Class( BASE_URI + localName );
	}

	private static URI parseAsksFile(String filename, List<AskQuery> queries)
			throws ParserConfigurationException, SAXException, IOException {
		if( queries == null )
			throw new NullPointerException();
		if( !queries.isEmpty() )
			throw new IllegalArgumentException();

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware( true );
		SAXParser parser = factory.newSAXParser();

		AsksDocHandler handler = new AsksDocHandler();
		parser.parse( filename, handler );
		List<AskQuery> asks = handler.getQueries();
		queries.addAll( asks );
		return handler.getURI();
	}

	private static Collection<Response> parseResponsesFile(String filename)
			throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware( true );
		SAXParser parser = factory.newSAXParser();

		ResponsesDefaultHandler handler = new ResponsesDefaultHandler();
		parser.parse( filename, handler );
		Collection<Response> responses = handler.getResponses();
		return responses;
	}

	private static void testParseEquals(String filename, URI kbURI, AskQuery... expected) {
		try {
			List<AskQuery> asks = new ArrayList<AskQuery>();
			URI parsedURI = parseAsksFile( filename, asks );
			assertEquals( kbURI, parsedURI );
			testParseEqualsUnordered( asks, expected );
		} catch( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	private static void testParseEquals(String filename, Response... expected) {
		try {
			testParseEqualsUnordered( parseResponsesFile( filename ), expected );
		} catch( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	private static <T> void testParseEqualsUnordered(Collection<T> actual, T... expected) {
		assertEquals( expected.length, actual.size() );
		assertTrue( actual.containsAll( Arrays.asList( expected ) ) );
	}

	@Test
	public void askClassHierarchyTests() {

		testParseEquals( DATA_BASEDIR + "ask-classhierarchy.xml", uri( KBURI_BASE
				+ "ask-classhierarchy" ), superClasses( "1", Class( "A" ), false ), superClasses(
				"2", not( Class( "B" ) ), true ), subClasses( "3", Class( "A" ), false ),
				subClasses( "4", not( Class( "B" ) ), false ), isSubClassOf( "5", Class( "A" ),
						Class( "B" ), false ), isSubClassOf( "6", not( Class( "A" ) ),
						Class( "B" ), true ) );
	}

	@Test
	public void askConsistentTest() {
		testParseEquals( DATA_BASEDIR + "ask-consistentkb.xml", uri( KBURI_BASE
				+ "ask-consistentkb" ), consistentkb( "1" ) );
	}

	@Test
	public void askEquivalentClassesTest() {
		testParseEquals( DATA_BASEDIR + "ask-equivalentclasses.xml", uri( KBURI_BASE
				+ "ask-equivalentclasses" ), equivalentClasses( "1", Class( "A" ) ) );
	}

	@Test
	public void askExplainTest() {
		testParseEquals( DATA_BASEDIR + "ask-explain.xml", uri( KBURI_BASE + "ask-explain" ),
				explain( "1", subClassOf( Class( "A" ), Class( "B" ) ) ), explain( "2", subClassOf(
						not( Class( "A" ) ), Class( "C" ) ), 5 ) );
	}

	@Test
	public void askNamedEntitiesTests() {

		testParseEquals( DATA_BASEDIR + "ask-namedentities.xml", uri( KBURI_BASE
				+ "ask-namedentities" ), namedClasses( "A" ), namedIndividuals( "B" ),
				namedDataProperties( "C" ), namedObjectProperties( "D" ) );
	}

	@Test
	public void askSatisfiableTests() {

		testParseEquals( DATA_BASEDIR + "ask-satisfiable.xml",
				uri( KBURI_BASE + "ask-satisfiable" ), isSatisfiable( "1", Class( "A" ) ),
				isSatisfiable( "2", not( Class( "B" ) ) ) );
	}

	@Test
	public void responseBooleanTests() {

		testParseEquals( DATA_BASEDIR + "response-boolean.xml", result( "1", true ), result( "2",
				false ) );
	}

	@Test
	public void responseErrorTest() {
		testParseEquals( DATA_BASEDIR + "response-error.xml", error( "1", 400,
				"Useful Error Message" ), error( "2", 500 ) );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void responseExplanationsTests() {
		testParseEquals( DATA_BASEDIR + "response-explanations.xml", explanations( "1",
				explanation( subClassOf( Class( "A" ), Class( "B" ) ) ) ), explanations( "2",
				explanation( subClassOf( Class( "A" ), not( Class( "B" ) ) ) ), explanation(
						subClassOf( Class( "A" ), Class( "C" ) ), subClassOf( Class( "C" ),
								not( Class( "B" ) ) ) ) ) );
	}

	@Test
	public void responseEmptyExplanationsTests() {
		testParseEquals( DATA_BASEDIR + "response-empty-explanations.xml", explanations( "1" ) );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void responseSynonymsTests() {

		testParseEquals( DATA_BASEDIR + "response-synonyms.xml", result( "1", synonyms(
				Class( "A" ), Class( "B" ) ), synonyms( Class( "C" ) ) ), result( "2",
				synonyms( not( Class( "D" ) ) ) ), result( "3" ) );
	}

	private static URI uri(String u) {
		return URI.create( u );
	}
}
