package com.clarkparsia.explanation.test;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParserFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mindswap.pellet.owlapi.PelletReasonerFactory;
import org.semanticweb.owl.inference.OWLClassReasoner;
import org.semanticweb.owl.inference.OWLReasonerFactory;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.explanation.ExplainQuery;
import com.clarkparsia.dig20.explanation.ExplanationsResponse;
import com.clarkparsia.dig20.responses.Response;
import com.clarkparsia.dig20.xml.ResponsesDefaultHandler;
import com.clarkparsia.dig20.xml.test.DigParserTests.AsksDocHandler;
import com.clarkparsia.explanation.BlackBoxExplanation;
import com.clarkparsia.explanation.ExplanationGenerator;
import com.clarkparsia.explanation.GlassBoxExplanation;
import com.clarkparsia.explanation.HSTExplanationGenerator;
import com.clarkparsia.explanation.TransactionAwareSingleExpGen;
import com.clarkparsia.owlapi.OWL;
import com.clarkparsia.owlapi.OntologyUtils;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Evren Sirin
 */
@RunWith(Parameterized.class)
public class FileBasedExplanationTest {
	protected static final Logger				log			= Logger
																	.getLogger( FileBasedExplanationTest.class.getName() );

	protected static final OWLOntologyManager	manager		= OntologyUtils.getOWLOntologyManager();

	public static final String					base		= "test/data";

	private static final PelletReasonerFactory	PELLET		= new PelletReasonerFactory();
	private static final GlassBoxExplanation	GLASS_BOX	= new GlassBoxExplanation( manager );
	private static final BlackBoxExplanation	BLACK_BOX	= new BlackBoxExplanation( manager );

	@Parameters
	public static Collection<Object[]> getParameters() {
		Collection<Object[]> parameters = new ArrayList<Object[]>();
		for( int i = 1; true; i++ ) {
			String index = String.format( "%03d", i );
			File inputFile = new File( base + "/Input" + index + ".rdf" );
			File asksFile = new File( base + "/Asks" + index + ".xml" );
			File responsesFile = new File( base + "/Responses" + index + ".xml" );

			if( !inputFile.exists() )
				break;

			parameters.add( new Object[] { inputFile, asksFile, responsesFile } );
		}

		return parameters;
	}

	public static void main(String args[]) throws Exception {
		if( args.length != 2 ) {
			printUsage(System.out);			
		}
		
		TransactionAwareSingleExpGen singleExpGen = null;		
		String method = args[0];
		if( method.equals( "black" ) )
			singleExpGen = new BlackBoxExplanation( manager );
		else if( method.equals( "glass" ) )
			singleExpGen = new GlassBoxExplanation( manager );
		else
			throw new IllegalArgumentException( "Explanation method " + method
					+ " is not recognized" );
		
		String index = String.format( "%03d", Integer.parseInt( args[1] ) );
		File inputFile = new File( base + "/Input" + index + ".rdf" );
		File asksFile = new File( base + "/Asks" + index + ".xml" );
		File responsesFile = new File( base + "/Responses" + index + ".xml" );

		FileBasedExplanationTest testCase = new FileBasedExplanationTest( inputFile, asksFile, responsesFile );

		testCase.testExplanations( singleExpGen );
	}

	private static void printUsage(PrintStream ps) {
		ps.println( "Usage:" );
		ps.println( "\t" + FileBasedExplanationTest.class.getName() + " (black|glass) testIndex" );
	}

	private File							asksFile;
	private ExplanationGenerator			expGen;
	private File							inputFile;
	private OWLOntology						ontology;
	private OWLClassReasoner				reasoner;
	private OWLReasonerFactory				reasonerFactory = PELLET;
	private File							responsesFile;

	public FileBasedExplanationTest(File inputFile, File asksFile, File responsesFile) {
		this.inputFile = inputFile;
		this.asksFile = asksFile;
		this.responsesFile = responsesFile;
	}

	@Test
	public void testGlassBox() throws Exception {
		log.fine( "Running GlassBox" );
		testExplanations( GLASS_BOX );
	}
	
	@Test
	public void testBlackBox() throws Exception {
		log.fine( "Running BlackBox" );
		testExplanations( BLACK_BOX );
	}
	
	public void testExplanations(TransactionAwareSingleExpGen singleExp) throws Exception {
		configure( singleExp );
		
		ExplanationTester tester = new ExplanationTester( expGen );

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware( true );

		AsksDocHandler asksHandler = new AsksDocHandler();
		factory.newSAXParser().parse( asksFile.getAbsolutePath(), asksHandler );

		ResponsesDefaultHandler responsesHandler = new ResponsesDefaultHandler();
		factory.newSAXParser().parse( responsesFile.getAbsolutePath(), responsesHandler );

		for( AskQuery q : asksHandler.getQueries() ) {
			if( q instanceof ExplainQuery ) {
				ExplainQuery eQ = (ExplainQuery) q;
				if( log.isLoggable( Level.FINE ) )
					log.fine( "Explain query: " +  q.getId() + " " + eQ.getOWLObject() );

				Response r = responsesHandler.getResponse( q.getId() );
				if( r == null )
					throw new NullPointerException();
				if( r instanceof ExplanationsResponse ) {
					ExplanationsResponse eR = (ExplanationsResponse) r;
					tester.testExplanations( eQ.getOWLObject(), eQ.getMax(), eR.getExplanations() );
				}
			}
			else
				throw new UnsupportedOperationException();
		}
	}

	public void configure(TransactionAwareSingleExpGen singleExpGen) throws Exception {
		if( log.isLoggable( Level.FINE ) )
			log.fine( "File: " + inputFile );
		
		ontology = manager.loadOntology( inputFile.toURI() );
		reasoner = reasonerFactory.createReasoner( manager );
		reasoner.loadOntologies( Collections.singleton( ontology ) );

		reasoner.getSubClasses( OWL.Thing );

		HSTExplanationGenerator hst = new HSTExplanationGenerator( singleExpGen );

		hst.setReasonerFactory( reasonerFactory );
		hst.setReasoner( reasoner );
		hst.setOntology( ontology );

		expGen = hst;
	}

}
