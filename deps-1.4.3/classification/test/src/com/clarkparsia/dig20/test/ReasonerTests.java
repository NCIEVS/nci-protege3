package com.clarkparsia.dig20.test;

import static com.clarkparsia.owlapi.OWL.Class;
import static com.clarkparsia.owlapi.OWL.Nothing;
import static com.clarkparsia.owlapi.OWL.Thing;
import static com.clarkparsia.owlapi.OWL.subClassOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.RemoveAxiom;

import com.clarkparsia.dig20.client.DigReasoner;
import com.clarkparsia.dig20.client.async.DigAsynchronousOperation;
import com.clarkparsia.dig20.client.async.DigAsynchronousOperationListener;
import com.clarkparsia.dig20.client.async.DigOntologyChangeOperation;
import com.clarkparsia.dig20.exceptions.DigClientException;
import com.clarkparsia.dig20.exceptions.ErrorResponseException;

public class ReasonerTests {

	private static final String	base	= "http://protege.stanford.edu/plugins/owl/owl-library/koala.owl#";
	private static URI			kbURI;
	private static final String	path	= "test/data/koala.owl";
	private static TestServer	server	= TestServer.theInstance();

	public static AddAxiom add(OWLAxiom a) {
		return new AddAxiom( null, a );
	}

	public static <T> List<T> list(T... ts) {
		return Arrays.asList( ts );
	}

	public static RemoveAxiom remove(OWLAxiom a) {
		return new RemoveAxiom( null, a );
	}

	public static <T> Set<T> set(T... ts) {
		return new HashSet<T>( Arrays.asList( ts ) );
	}

	@BeforeClass
	public static void startDigServer() {
		kbURI = server.start( path );
	}

	@AfterClass
	public static void stopDigServer() {
		server.stop();
	}

	public static <T> void testSetSetEqual(Set<? extends Set<? extends T>> expected,
			Set<? extends Set<? extends T>> actual) {
		assertTrue( actual.equals( expected ) );
	}

	final Set<OWLClass>	nothings;

	private DigReasoner	reasoner	= new DigReasoner( server.URL(), kbURI, OWLManager
											.createOWLOntologyManager() );

	final Set<OWLClass>	unsats;

	public ReasonerTests() {
		unsats = set( Class( base + "Koala" ), Class( base + "KoalaWithPhD" ), Class( base + "Quokka" ) );
		nothings = new HashSet<OWLClass>( unsats );
		nothings.add( Nothing );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void asyncClassDeleteTest() throws ExecutionException, InterruptedException,
			OWLException {

		final OWLClass forest = Class( base + "Forest" );
		final OWLClass euc = Class( base + "DryEucalyptForest" );
		final OWLClass rainforest = Class( base + "Rainforest" );

		testSetSetEqual( set( set( rainforest ), set( euc ) ), reasoner.getSubClasses( forest ) );

		ExecutorService executor = Executors.newSingleThreadExecutor();
		DigAsynchronousOperation op = new DigOntologyChangeOperation(
				new DigAsynchronousOperationListener() {

					public void failure(DigAsynchronousOperation op) {
					}

					public void failure(DigAsynchronousOperation op, DigClientException t) {
						throw new RuntimeException( t );
					}

					public void failure(DigAsynchronousOperation op, ErrorResponseException e) {
						throw new RuntimeException( e );
					}

					public void success(DigAsynchronousOperation op) {
					}

				}, reasoner, list( remove( subClassOf( rainforest, forest ) ) ) );

		Future<DigAsynchronousOperation> f = executor.submit( op, op );
		f.get();

		testSetSetEqual( set( set( euc ) ), reasoner.getSubClasses( forest ) );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void classDeleteTest() throws OWLException {

		final OWLClass forest = Class( base + "Forest" );
		final OWLClass euc = Class( base + "DryEucalyptForest" );
		final OWLClass rainforest = Class( base + "Rainforest" );

		testSetSetEqual( set( set( rainforest ), set( euc ) ), reasoner.getSubClasses( forest ) );

		reasoner.ontologiesChanged( list( remove( subClassOf( rainforest, forest ) ) ) );

		testSetSetEqual( set( set( euc ) ), reasoner.getSubClasses( forest ) );

		assertFalse( reasoner.getSubClasses( Thing ).contains( set( rainforest ) ) );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getAncestorClassesTest() throws OWLReasonerException {
		testSetSetEqual( set( set( Class( base + "Forest" ) ), set( Class( base + "Habitat" ) ),
				set( Thing ) ), reasoner.getAncestorClasses( Class( base + "Rainforest" ) ) );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getDescendantClassesTest() throws OWLReasonerException {
		testSetSetEqual( set( set( Class( base + "University" ) ), set( Class( base + "Forest" ) ),
				set( Class( base + "DryEucalyptForest" ) ), set( Class( base + "Rainforest" ) ), set(
						Nothing, Class( base + "Koala" ), Class( base + "Quokka" ), Class( base
								+ "KoalaWithPhD" ) ) ), reasoner.getDescendantClasses( Class( base
				+ "Habitat" ) ) );
	}

	@Test
	public void getEquivalentClassesTest() throws OWLReasonerException {
		assertTrue( reasoner.getEquivalentClasses( Class( base + "Koala" ) ).equals(
				set( Nothing, Class( base + "Quokka" ), Class( base + "KoalaWithPhD" ) ) ) );
		assertTrue( reasoner.getEquivalentClasses( Class( base + "Female" ) ).equals(
				Collections.emptySet() ) );
	}

	@Test
	public void getInconsistentClassesTest() throws OWLReasonerException {
		assertTrue( reasoner.getInconsistentClasses().equals(
				set( Class( base + "Koala" ), Class( base + "Quokka" ), Class( base + "KoalaWithPhD" ) ) ) );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getSubClassesBatchTest() throws OWLReasonerException {
		Map<OWLDescription, Set<Set<OWLClass>>> ret = reasoner.getSubClasses( set( Class( base
				+ "Habitat" ), Class( base + "Student" ) ) );

		testSetSetEqual( set( set( Class( base + "University" ) ), set( Class( base + "Forest" ) ) ),
				ret.get( Class( base + "Habitat" ) ) );
		testSetSetEqual( set( set( Class( base + "GraduateStudent" ) ), set( Class( base
				+ "MaleStudentWith3Daughters" ) ) ), ret.get( Class( base + "Student" ) ) );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getSubClassesTest() throws OWLReasonerException {
		testSetSetEqual( set( set( Class( base + "University" ) ), set( Class( base + "Forest" ) ) ),
				reasoner.getSubClasses( Class( base + "Habitat" ) ) );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getSubSuperClassesBatchTest() throws OWLReasonerException {
		Map<OWLDescription, Set<Set<OWLClass>>[]> ret = reasoner.getSubSuperClasses( set( Class( base
				+ "Habitat" ), Class( base + "Student" ) ) );

		testSetSetEqual( set( set( Thing ) ),
				ret.get( Class( base + "Habitat" ) )[DigReasoner.SUPERS_INDEX] );
		testSetSetEqual( set( set( Class( base + "University" ) ), set( Class( base + "Forest" ) ) ),
				ret.get( Class( base + "Habitat" ) )[DigReasoner.SUBS_INDEX] );

		testSetSetEqual( set( set( Class( base + "Person" ) ) ),
				ret.get( Class( base + "Student" ) )[DigReasoner.SUPERS_INDEX] );
		testSetSetEqual( set( set( Class( base + "GraduateStudent" ) ), set( Class( base
				+ "MaleStudentWith3Daughters" ) ) ),
				ret.get( Class( base + "Student" ) )[DigReasoner.SUBS_INDEX] );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getSuperClassesBatchTest() throws OWLReasonerException {
		Map<OWLDescription, Set<Set<OWLClass>>> ret = reasoner.getSuperClasses( set( Class( base
				+ "Habitat" ), Class( base + "Student" ) ) );

		testSetSetEqual( set( set( Thing ) ), ret.get( Class( base + "Habitat" ) ) );
		testSetSetEqual( set( set( Class( base + "Person" ) ) ), ret.get( Class( base + "Student" ) ) );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getSuperClassesTest() throws OWLReasonerException {
		testSetSetEqual( set( set( Class( base + "Forest" ) ) ), reasoner.getSuperClasses( Class( base
				+ "Rainforest" ) ) );
	}

	@Test
	public void isEquivalentClassTests() throws OWLReasonerException {
		assertTrue( reasoner.isEquivalentClass( Class( base + "Koala" ), Class( base + "Quokka" ) ) );
		assertFalse( reasoner.isEquivalentClass( Class( base + "Male" ), Class( base + "Female" ) ) );
	}

	@Test
	public void isSubClassOfTests() throws OWLReasonerException {
		assertTrue( reasoner.isSubClassOf( Class( base + "Rainforest" ), Class( base + "Habitat" ) ) );
		assertFalse( reasoner.isSubClassOf( Class( base + "Male" ), Class( base + "Habitat" ) ) );
	}

	@Before
	public void reload() throws MalformedURLException {
		server.reload();
	}
}