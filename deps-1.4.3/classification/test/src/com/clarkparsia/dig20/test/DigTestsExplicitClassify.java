package com.clarkparsia.dig20.test;

import static com.clarkparsia.dig20.asks.AskQueryFactory.isSatisfiable;
import static com.clarkparsia.dig20.asks.AskQueryFactory.classifykb;
import static com.clarkparsia.dig20.asks.AskQueryFactory.subClasses;
import static com.clarkparsia.dig20.asks.AskQueryFactory.superClasses;
import static com.clarkparsia.dig20.explanation.ExplanationFactory.explain;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_UNDEFINED_CLASS;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_UNDEFINED_KB;
import static com.clarkparsia.owlapi.OWL.Class;
import static com.clarkparsia.owlapi.OWL.DataProperty;
import static com.clarkparsia.owlapi.OWL.Nothing;
import static com.clarkparsia.owlapi.OWL.Thing;
import static com.clarkparsia.owlapi.OWL.domain;
import static com.clarkparsia.owlapi.OWL.subClassOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLObject;

import com.clarkparsia.dig20.RetractAxioms;
import com.clarkparsia.dig20.TellAxioms;
import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.client.DefaultDigClient;
import com.clarkparsia.dig20.exceptions.DigClientException;
import com.clarkparsia.dig20.exceptions.DigClientHttpErrorException;
import com.clarkparsia.dig20.responses.BooleanAskResult;
import com.clarkparsia.dig20.responses.ErrorResponse;
import com.clarkparsia.dig20.responses.Response;
import com.clarkparsia.dig20.responses.SynonymsAskResult;
import com.clarkparsia.dig20.server.DigRequestHandler;
import com.clarkparsia.owlapi.OWL;

public class DigTestsExplicitClassify {

	private static final String	base	= "http://protege.stanford.edu/plugins/owl/owl-library/koala.owl#";
	private static URI			kbURI;
	private static final String	path	= "test/data/koala.owl";
	private static TestServer	server	= TestServer.theInstance();

	private static boolean applyChangesImmediately;
	
	@BeforeClass
	public static void startDigServer() {
		kbURI = server.start( path );
		applyChangesImmediately = DigRequestHandler.APPLY_CHANGES_IMMEDIATELY;
		DigRequestHandler.APPLY_CHANGES_IMMEDIATELY = false;
	}

	@AfterClass
	public static void stopDigServer() {
		server.stop();
		DigRequestHandler.APPLY_CHANGES_IMMEDIATELY = applyChangesImmediately;
	}

	private static Set<OWLObject> synonyms(OWLObject... objects) {
		return new HashSet<OWLObject>( Arrays.asList( objects ) );
	}

	private static void testSynonymsEqual(
			Collection<? extends Collection<? extends OWLObject>> actual,
			Set<? extends OWLObject>... expected) {
		Set<Set<OWLObject>> aSet = new HashSet<Set<OWLObject>>();
		for( Collection<? extends OWLObject> c : actual ) {
			aSet.add( new HashSet<OWLObject>( c ) );
		}

		assertTrue( aSet.size() == expected.length );
		assertTrue( aSet.containsAll( Arrays.asList( expected ) ) );
	}

	private DefaultDigClient		client;
	private final Set<OWLObject>	unsats	= synonyms( Nothing, Class( base + "Koala" ),
													Class( base + "Quokka" ), Class( base
															+ "KoalaWithPhD" ) );

	@SuppressWarnings("unchecked")
	@Test
	public void addSubClassTest() throws DigClientException {
		OWLClass sup = Class( base + "University" );
		OWLClass subClassOf = Class( base + "BadUniversity" );
		testSynonymResponse( kbURI, subClasses( sup, true ), unsats );

		Collection<TellAxioms> tells = Collections.singleton( new TellAxioms( "tell", kbURI,
				Collections.singleton( subClassOf( subClassOf, sup ) ) ) );
		Collection<RetractAxioms> retractions = Collections.emptySet();
		client.applyChanges( tells, retractions );
		testSynonymResponse( kbURI, subClasses( sup, true ), unsats );
		testBooleanResponse( kbURI, classifykb(), true );
		testSynonymResponse( kbURI, subClasses( sup, true ), synonyms( subClassOf ) );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void directSubClassesTest() {
		testSynonymResponse( kbURI, subClasses( Class( base + "Habitat" ), true ),
				synonyms( Class( base + "University" ) ), synonyms( Class( base + "Forest" ) ) );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void directSuperClassesTest() {
		testSynonymResponse( kbURI, superClasses( Class( base + "Rainforest" ), true ),
				synonyms( Class( base + "Forest" ) ) );
	}

	@Test(expected = DigClientHttpErrorException.class)
	public void exceptionOnRelativeURITest() throws DigClientException {

		Collection<TellAxioms> tells = Collections.singleton( new TellAxioms( "tell", kbURI,
				Collections.singleton( subClassOf( Class( "../Habitat" ), OWL.Thing ) ) ) );
		Collection<RetractAxioms> retractions = Collections.emptySet();

		client.applyChanges( tells, retractions );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void indirectSubClassesTest() {
		testSynonymResponse( kbURI, subClasses( Class( base + "Habitat" ), false ),
				synonyms( Class( base + "University" ) ), synonyms( Class( base + "Forest" ) ),
				synonyms( Class( base + "DryEucalyptForest" ) ), synonyms( Class( base
						+ "Rainforest" ) ), unsats );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void indirectSuperClassesTest() {

		testSynonymResponse( kbURI, superClasses( Class( base + "Rainforest" ), false ),
				synonyms( Class( base + "Forest" ) ), synonyms( Class( base + "Habitat" ) ),
				synonyms( Thing ) );

		testSynonymResponse( kbURI, superClasses( Class( base + "Rainforest" ) ),
				synonyms( Class( base + "Forest" ) ), synonyms( Class( base + "Habitat" ) ),
				synonyms( Thing ) );
	}

	@Before
	public void reload() throws MalformedURLException {
		client = new DefaultDigClient( server.URL() );
		server.reload();
	}

	@Test
	public void removeDomainTest() throws DigClientException {

		OWLClass koala = Class( base + "Koala" );
		testBooleanResponse( kbURI, isSatisfiable( koala ), false );

		Collection<TellAxioms> tells = Collections.emptySet();
		Collection<RetractAxioms> retractions = Collections.singleton( new RetractAxioms(
				"retract", kbURI, Collections.singleton( domain( DataProperty( base
						+ "isHardWorking" ), Class( base + "Person" ) ) ) ) );
		client.applyChanges( tells, retractions );

		testBooleanResponse( kbURI, isSatisfiable( koala ), false );
		testBooleanResponse( kbURI, classifykb(), true );
		testBooleanResponse( kbURI, isSatisfiable( koala ), true );
	}

	private void testBooleanResponse(URI u, AskQuery q, boolean expected) {
		BooleanAskResult r = (BooleanAskResult) client.getResponse( u, q );
		assertTrue( r.getBoolean() == expected );
	}

	private void testErrorResponse(URI u, AskQuery q, int code) {
		Response r = client.getResponse( u, q );
		assertTrue( r instanceof ErrorResponse );
		ErrorResponse e = (ErrorResponse) r;
		assertTrue( e.getCode() == code );
	}

	private void testSynonymResponse(URI u, AskQuery q, Set<? extends OWLObject>... expected) {
		SynonymsAskResult r = (SynonymsAskResult) client.getResponse( u, q );
		testSynonymsEqual( r.getOWLObjects(), expected );
	}

	@Test
	public void undefinedClassInExplain() {
		OWLClass notPresent = Class( base + "NotAClass" );
		testErrorResponse( kbURI, explain( subClassOf( notPresent, Nothing ) ),
				ERROR_CODE_UNDEFINED_CLASS );
	}

	@Test
	public void undefinedClassInSubclass() {
		OWLClass notPresent = Class( base + "NotAClass" );
		testErrorResponse( kbURI, subClasses( notPresent ), ERROR_CODE_UNDEFINED_CLASS );
	}

	@Test
	public void undefinedKbInAsks() {
		final URI u = URI.create( kbURI.toASCIIString() + "/xxx" );
		testErrorResponse( u, isSatisfiable( Class( base + "Koala" ) ), ERROR_CODE_UNDEFINED_KB );
	}

	@Test
	public void undefinedKbInRetraction() throws DigClientException {
		final URI u = URI.create( kbURI.toASCIIString() + "/xxx" );
		Collection<TellAxioms> tells = Collections.emptySet();
		Collection<RetractAxioms> retractions = Collections.singleton( new RetractAxioms(
				"retract", u, Collections.singleton( domain(
						DataProperty( base + "isHardWorking" ), Class( base + "Person" ) ) ) ) );
		Collection<Response> responses = client.applyChanges( tells, retractions );
		assertEquals( 1, responses.size() );

		Response r = responses.iterator().next();
		assertTrue( r instanceof ErrorResponse );
		ErrorResponse e = (ErrorResponse) r;
		assertEquals( ERROR_CODE_UNDEFINED_KB, e.getCode() );
	}
}
