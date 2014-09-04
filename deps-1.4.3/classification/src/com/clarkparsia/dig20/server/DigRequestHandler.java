package com.clarkparsia.dig20.server;

import static com.clarkparsia.dig20.explanation.ExplanationsResponse.explanations;
import static com.clarkparsia.dig20.responses.BooleanAskResult.result;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_NOT_IMPLEMENTED;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_NOT_IMPLEMENTED_QUERY_ATTRIBUTE;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_NOT_IMPLEMENTED_QUERY_TYPE;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_UNDEFINED_CLASS;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_UNDEFINED_DATATYPE;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_UNDEFINED_DATA_PROPERTY;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_UNDEFINED_INDIVIDUAL;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_UNDEFINED_KB;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_UNDEFINED_OBJECT_PROPERTY;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_UNKNOWN;
import static com.clarkparsia.dig20.responses.ErrorResponse.ERROR_CODE_UNRECOGNIZED_BASE;
import static com.clarkparsia.dig20.responses.ErrorResponse.error;
import static com.clarkparsia.dig20.responses.SynonymsAskResult.result;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.mindswap.pellet.owlapi.PelletReasonerFactory;
import org.mindswap.pellet.owlapi.Reasoner;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.inference.OWLReasonerBase;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLAxiomChange;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDeclarationAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEntityVisitor;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLRuntimeException;
import org.semanticweb.owl.model.RemoveAxiom;
import org.semanticweb.owl.util.OWLEntityCollector;

import com.clarkparsia.dig20.HTTPConstants;
import com.clarkparsia.dig20.QueryProcessor;
import com.clarkparsia.dig20.RetractAxioms;
import com.clarkparsia.dig20.TellAxioms;
import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.asks.AskQueryVisitor;
import com.clarkparsia.dig20.asks.ClassifyQuery;
import com.clarkparsia.dig20.asks.ConsistentQuery;
import com.clarkparsia.dig20.asks.EquivalentClassesQuery;
import com.clarkparsia.dig20.asks.IsEquivalentClassToQuery;
import com.clarkparsia.dig20.asks.IsSatisfiableQuery;
import com.clarkparsia.dig20.asks.IsSubClassOfQuery;
import com.clarkparsia.dig20.asks.NamedClassesQuery;
import com.clarkparsia.dig20.asks.NamedDataPropertiesQuery;
import com.clarkparsia.dig20.asks.NamedIndividualsQuery;
import com.clarkparsia.dig20.asks.NamedObjectPropertiesQuery;
import com.clarkparsia.dig20.asks.SubClassesQuery;
import com.clarkparsia.dig20.asks.SuperClassesQuery;
import com.clarkparsia.dig20.asks.UnrecognizedQuery;
import com.clarkparsia.dig20.explanation.ExplainQuery;
import com.clarkparsia.dig20.responses.ErrorResponse;
import com.clarkparsia.dig20.responses.OkResponse;
import com.clarkparsia.dig20.responses.Response;
import com.clarkparsia.dig20.xml.DigRenderer;
import com.clarkparsia.dig20.xml.DigServerDefaultHandler;
import com.clarkparsia.dig20.xml.responses.ResponsesVocabulary;
import com.clarkparsia.explanation.GlassBoxExplanation;
import com.clarkparsia.explanation.HSTExplanationGenerator;
import com.clarkparsia.explanation.MultipleExplanationGenerator;
import com.clarkparsia.explanation.SatisfiabilityConverter;
import com.clarkparsia.modularity.IncrementalClassifier;
import com.clarkparsia.modularity.OntologyDiff;
import com.clarkparsia.modularity.graph.GraphBasedModuleExtractor;
import com.clarkparsia.modularity.io.IncrementalClassifierPersistence;
import com.clarkparsia.pellet.utils.MultiMapUtils;

/**
 * <p>
 * Title: HTTPExplanationHandler
 * </p>
 * <p>
 * Description: HTTP request handler for server side explanation generator.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Mike Smith
 */
public class DigRequestHandler extends AbstractHttpHandler {
	
	public static boolean APPLY_CHANGES_IMMEDIATELY = true;
    
    static {
		if (System.getProperty("clarkparsia.server.changes.immediate") == null)
			APPLY_CHANGES_IMMEDIATELY = false;
		else
			APPLY_CHANGES_IMMEDIATELY = true;
    }

	private static class UndefinedEntityErrorProducer implements OWLEntityVisitor {

		private ErrorResponse	error;
		private String			id;

		public ErrorResponse makeErrorResponse(AskQuery q, OWLEntity e) {
			id = q.getId();
			e.accept( this );
			return error;
		}

		public void visit(OWLClass cls) {
			error = error( id, ERROR_CODE_UNDEFINED_CLASS, cls.getURI().toString() );
		}

		public void visit(OWLDataProperty property) {
			error = error( id, ERROR_CODE_UNDEFINED_DATA_PROPERTY, property.getURI().toString() );
		}

		public void visit(OWLDataType dataType) {
			error = error( id, ERROR_CODE_UNDEFINED_DATATYPE, dataType.getURI().toString() );
		}

		public void visit(OWLIndividual individual) {
			error = error( id, ERROR_CODE_UNDEFINED_INDIVIDUAL, individual.getURI().toString() );
		}

		public void visit(OWLObjectProperty property) {
			error = error( id, ERROR_CODE_UNDEFINED_OBJECT_PROPERTY, property.getURI().toString() );
		}

	}

	private class DefaultQueryProcessor implements QueryProcessor, AskQueryVisitor {

		private Response						r;
		private UndefinedEntityErrorProducer	undefinedEntityErrorProducer;

		public DefaultQueryProcessor() {
			undefinedEntityErrorProducer = new UndefinedEntityErrorProducer();
		}

		private boolean allEntitiesDefined(AskQuery q, OWLObject... os) throws OWLReasonerException {
			for( OWLEntity e : getEntities( os ) ) {
				if( !isDefined( generator.getReasoner(), e ) ) {
					r = undefinedEntityErrorProducer.makeErrorResponse( q, e );
					return false;
				}
			}
			return true;
		}

		private void handleError(Exception e, AskQuery q) {
			r = error( q.getId(), ERROR_CODE_UNKNOWN, e.getMessage() );
			log.log( Level.WARNING, "Returning unknown error response", e );
		}

		public Response getResponse(URI uri, AskQuery q) {
			if( !kbURI.equals( uri ) ) {
				return error( q.getId(), ERROR_CODE_UNDEFINED_KB, String.format(
						"KB identifier from client (%s) does not match server KB identifier (%s)",
						uri, kbURI ) );
			}
			
			q.accept( this );
			return r;
		}

		public void visit(ConsistentQuery q) {
			r = error( q.getId(), ERROR_CODE_NOT_IMPLEMENTED );
		}

		public void visit(ClassifyQuery q) {
			try {
				applyChanges();
				clsReasoner.classify();
				r = result( q.getId(), true );
			} catch (Exception e) {
				handleError( e, q );
			}
		}

		public void visit(EquivalentClassesQuery q) {
			OWLDescription d = q.getOWLObject();
			try {
				if( !allEntitiesDefined( q, d ) )
					return;
				Set<OWLClass> eq = clsReasoner.getEquivalentClasses( d );
				Set<Set<OWLClass>> ret;
				if( eq.isEmpty() )
					ret = Collections.emptySet();
				else
					ret = Collections.singleton( eq );
				r = result( q.getId(), ret );
			} catch( OWLReasonerException e ) {
				handleError( e, q );
			}
		}

		public void visit(ExplainQuery q) {
			OWLDescription desc = converter.convert( q.getOWLObject() );

			int maxExplanations = q.getAll()
				? 0
				: q.getMax();

			try {
				if( !allEntitiesDefined( q, desc ) )
					return;
				Set<Set<OWLAxiom>> axioms = generator.getExplanations( desc, maxExplanations );
				r = explanations( q.getId(), axioms );
			} catch( OWLRuntimeException e ) {
				handleError( e, q );
			} catch( OWLException e ) {
				handleError( e, q );
			}
		}

		public void visit(IsEquivalentClassToQuery q) {
			try {
				final OWLDescription first = q.getFirst();
				final OWLDescription second = q.getSecond();
				if( !allEntitiesDefined( q, first, second ) )
					return;
				r = result( q.getId(), clsReasoner.isEquivalentClass( first, second ) );
			} catch( OWLReasonerException e ) {
				handleError( e, q );
			}
		}

		public void visit(IsSatisfiableQuery q) {
			OWLDescription d = q.getOWLObject();
			try {
				if( !allEntitiesDefined( q, d ) )
					return;
				r = result( q.getId(), clsReasoner.isSatisfiable( d ) );
			} catch( OWLReasonerException e ) {
				handleError( e, q );
			}
		}

		public void visit(IsSubClassOfQuery q) {

			if( Boolean.TRUE.equals( q.getDirect() ) ) {
				r = error( q.getId(), ERROR_CODE_NOT_IMPLEMENTED_QUERY_ATTRIBUTE );
				return;
			}

			try {
				final OWLDescription sub = q.getSub();
				final OWLDescription sup = q.getSuper();
				if( !allEntitiesDefined( q, sub, sup ) )
					return;
				r = result( q.getId(), clsReasoner.isSubClassOf( sub, sup ) );
			} catch( OWLReasonerException e ) {
				handleError( e, q );
			}
		}

		public void visit(NamedClassesQuery q) {
			r = error( q.getId(), ERROR_CODE_NOT_IMPLEMENTED_QUERY_TYPE );
		}

		public void visit(NamedDataPropertiesQuery q) {
			r = error( q.getId(), ERROR_CODE_NOT_IMPLEMENTED_QUERY_TYPE );
		}

		public void visit(NamedIndividualsQuery q) {
			r = error( q.getId(), ERROR_CODE_NOT_IMPLEMENTED_QUERY_TYPE );
		}

		public void visit(NamedObjectPropertiesQuery q) {
			r = error( q.getId(), ERROR_CODE_NOT_IMPLEMENTED_QUERY_TYPE );
		}

		public void visit(SubClassesQuery q) {
			OWLDescription d = q.getOWLObject();
			try {
				if( !allEntitiesDefined( q, d ) )
					return;
				Set<Set<OWLClass>> subs = (Boolean.TRUE.equals( q.getDirect() ))
					? clsReasoner.getSubClasses( d )
					: clsReasoner.getDescendantClasses( d );
				r = result( q.getId(), subs );
			} catch( OWLReasonerException e ) {
				handleError( e, q );
			}

		}

		public void visit(SuperClassesQuery q) {
			OWLDescription d = q.getOWLObject();
			try {
				if( !allEntitiesDefined( q, d ) )
					return;
				Set<Set<OWLClass>> subs = (Boolean.TRUE.equals( q.getDirect() ))
					? clsReasoner.getSuperClasses( d )
					: clsReasoner.getAncestorClasses( d );
				r = result( q.getId(), subs );
			} catch( OWLReasonerException e ) {
				handleError( e, q );
			}
		}

		public void visit(UnrecognizedQuery q) {
			r = error( q.getId(), ERROR_CODE_UNRECOGNIZED_BASE );
		}
	}

	private static class DefinedEntityTester implements OWLEntityVisitor {

		private OWLReasonerException	e;
		private boolean					isDefined;
		private OWLReasonerBase			reasoner;

		public boolean isDefined() throws OWLReasonerException {
			if( e != null )
				throw e;
			return isDefined;
		}

		public void reset(OWLReasonerBase reasoner) {
			this.reasoner = reasoner;
			e = null;
		}

		public void visit(OWLClass cls) {
			try {
				isDefined = reasoner.isDefined( cls );
			} catch( OWLReasonerException e ) {
				this.e = e;
			}
		}

		public void visit(OWLDataProperty property) {
			try {
				isDefined = reasoner.isDefined( property );
			} catch( OWLReasonerException e ) {
				this.e = e;
			}
		}

		public void visit(OWLDataType dataType) {
			throw new UnsupportedOperationException();
		}

		public void visit(OWLIndividual individual) {
			try {
				isDefined = reasoner.isDefined( individual );
			} catch( OWLReasonerException e ) {
				this.e = e;
			}
		}

		public void visit(OWLObjectProperty property) {
			try {
				isDefined = reasoner.isDefined( property );
			} catch( OWLReasonerException e ) {
				this.e = e;
			}
		}
	}
	
	private static class ChangeRecorder {
		private Map<OWLOntology,Set<OWLAxiom>> additions;
		private Map<OWLOntology,Set<OWLAxiom>> removals;
		
		private ChangeRecorder() {
			additions = new HashMap<OWLOntology, Set<OWLAxiom>>();
			removals = new HashMap<OWLOntology, Set<OWLAxiom>>();
		}
		
		public void recordAddition(OWLOntology ont, OWLAxiom axiom) {
			if (MultiMapUtils.remove(removals, ont, axiom)) {
				if (log.isLoggable(Level.FINE))
					log.fine("Cancel previous removal: " + axiom);
			}
			else if (ont.containsAxiom(axiom)) {
				if (log.isLoggable(Level.FINE))
					log.fine("Ignore redundant addition: " + axiom);
			}
			else {
				if (log.isLoggable(Level.FINE))
					log.fine("Record addition: " + axiom);
				MultiMapUtils.add(additions, ont, axiom);
			}
		}
		
		public void recordRemoval(OWLOntology ont, OWLAxiom axiom) {
			if (MultiMapUtils.remove(additions, ont, axiom)) {
				if (log.isLoggable(Level.FINE))
					log.fine("Cancel previous addition: " + axiom);
			}
			else if (!ont.containsAxiom(axiom)) {
				if (log.isLoggable(Level.FINE))
					log.fine("Ignore redundant removal: " + axiom);
			}
			else {
				if (log.isLoggable(Level.FINE))
					log.fine("Record removal: " + axiom);
				MultiMapUtils.add(removals, ont, axiom);
			}
		}

		public List<OWLAxiomChange> getChanges() {
			List<OWLAxiomChange> changes = new ArrayList<OWLAxiomChange>();
			for (Entry<OWLOntology, Set<OWLAxiom>> e : additions.entrySet()) {
				OWLOntology ont = e.getKey();
	            for (OWLAxiom axiom : e.getValue()) {
	            	changes.add(new AddAxiom(ont, axiom));
                }
            }
			for (Entry<OWLOntology, Set<OWLAxiom>> e : removals.entrySet()) {
				OWLOntology ont = e.getKey();
	            for (OWLAxiom axiom : e.getValue()) {
	            	changes.add(new RemoveAxiom(ont, axiom));
                }
            }
			return changes;
		}

		public Collection<OWLAxiom> getRemovals() {
			List<OWLAxiom> changes = new ArrayList<OWLAxiom>();
			for (Set<OWLAxiom> ontRemovals : removals.values()) {
	            for (OWLAxiom axiom : ontRemovals) {
	            	changes.add(axiom);
                }
            }
			return changes;
		}

		public void clear() {
			additions.clear();
			removals.clear();
		}
	}

	private static final Logger			log					= Logger
																	.getLogger( DigRequestHandler.class
																			.getName() );
	private static final long			serialVersionUID	= 995707381622491870L;

	/**
	 * Concurrent access guarded by synchronizing on {@link #tester}
	 */
	private static DefinedEntityTester	tester				= new DefinedEntityTester();

	private static boolean isDefined(OWLReasonerBase reasoner, OWLEntity e)
			throws OWLReasonerException {
		synchronized( tester ) {
			tester.reset( reasoner );
			e.accept( tester );
			return tester.isDefined();
		}
	}

	/**
	 * Concurrent access guarded by synchronizing on this
	 */
	private IncrementalClassifier			clsReasoner;
	/**
	 * Concurrent access guarded by synchronizing on {@link #collector}
	 */
	private OWLEntityCollector				collector;
	/**
	 * Concurrent access guarded by synchronizing on this
	 */
	private SatisfiabilityConverter			converter;
	/**
	 * Concurrent access guarded by synchronizing on this
	 */
	private GlassBoxExplanation				singleExplanation;
	/**
	 * Concurrent access guarded by synchronizing on this
	 */
	private MultipleExplanationGenerator	generator;
	/**
	 * Concurrent access guarded by synchronizing on this
	 */
	private DefaultQueryProcessor			processor;
	/**
	 * Concurrent access guarded by synchronizing on {@link #parser}
	 */
	private SAXParser						parser;
	/**
	 * Concurrent access guarded by synchronizing on this
	 */
	private URI								kbURI;
	/**
	 * Concurrent access guarded by synchronizing on this
	 */
	private ChangeRecorder					changeTracker;
	
	private PersistenceManager				persistenceManager;
	
	public DigRequestHandler() {
		clear();

		processor = new DefaultQueryProcessor();

		converter = new SatisfiabilityConverter( getOntologyManager().getOWLDataFactory() );

		collector = new OWLEntityCollector();
		collector.setCollectDataTypes( false );

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware( true );

		try {
			parser = factory.newSAXParser();
		} catch( Exception e ) {
			log.log( Level.SEVERE, "Failed to create a SAX parser", e );
			throw new RuntimeException( e );
		}
	}

	protected void clear() {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		GlassBoxExplanation newSingleExplanation = new GlassBoxExplanation( manager );
		MultipleExplanationGenerator newGenerator = new HSTExplanationGenerator(
				newSingleExplanation );
		newGenerator.setReasonerFactory( new PelletReasonerFactory() );

		Reasoner reasoner = new Reasoner( manager );
		newGenerator.setReasoner( reasoner );

		GraphBasedModuleExtractor extractor = new GraphBasedModuleExtractor();
		IncrementalClassifier newClassifier = new IncrementalClassifier( manager, reasoner,
				extractor );
		manager.removeOntologyChangeListener( newClassifier );
		
		ChangeRecorder newChangeTracker = new ChangeRecorder();

		synchronized( this ) {
			singleExplanation = newSingleExplanation;
			generator = newGenerator;
			clsReasoner = newClassifier;
			changeTracker = newChangeTracker;
		}
	}
	
	public synchronized long getClassificationTimer(String name) {
		return clsReasoner.timers.getTimerTotal(name);
	}	
	
	public synchronized OWLOntologyManager getOntologyManager() {
		return generator.getOntologyManager();
	}

	private Set<OWLEntity> getEntities(OWLObject... os) {
		synchronized( collector ) {
			collector.reset();
			for( OWLObject o : os ) {
				o.accept( collector );
			}
			return collector.getObjects();
		}
	}
	
	private void parseRequest(HttpRequest request, DigServerDefaultHandler handler) throws Exception {
		final int bufSize = 4096;
		/*
		 * It is necessary to override the close method so that the stream
		 * isn't closed by the sax parser and the opportunity to rewind and
		 * log an error is lost.
		 */
		BufferedInputStream in = new BufferedInputStream( request.getInputStream(), bufSize );
		in.mark( bufSize );
		BufferedInputStream wrapped = new BufferedInputStream( in ) {
			@Override
			public void close() throws IOException {
			}
		};
		synchronized( parser ) {
			try {
				parser.parse( wrapped, handler );
			} catch( Exception e ) {
				try {
					in.reset();
					BufferedReader br = new BufferedReader( new InputStreamReader( in ) );
					StringBuffer sbuf = new StringBuffer();
					char buf[] = new char[1024];
					int nread;
					while( (nread = br.read( buf )) > 0 ) {
						sbuf.append( buf, 0, nread );
					}
					br.close();
					log.log( Level.WARNING, "Exception caught while parsing content:\n"
							+ sbuf.toString() );
					throw e;
				} catch( IOException f ) {
					log.log( Level.WARNING,
							"Exception caught during parse, but unable to rewind input stream" );
					throw e;
				}
			} finally {
				in.close();
			}
		}
		
		if (log.isLoggable(Level.FINE))
			log.fine(String.format("Tells: %d Retractions: %d Queries: %d",
					handler.getTells().size(), handler.getRetractions().size(),
					handler.getQueryCount()));
		
		if (log.isLoggable(Level.FINE)) {
			for (TellAxioms t : handler.getTells()) {
				log.fine(t.toString());
			}
		}

		if (log.isLoggable(Level.FINE)) {
			for (RetractAxioms r : handler.getRetractions()) {
				log.fine(r.toString());
			}
		}

		for (URI u : handler.getKbURIs()) {
			List<AskQuery> qs = handler.getQueries(u);
			log.finer("Asks (" + u + "," + qs.size() + ")");
			if (log.isLoggable(Level.FINER)) {
				for (AskQuery a : qs) {
					log.finer(a.toString());
				}
			}
		}		
	}
	
	/**
	 * Records but does not apply changes. 
	 * 
	 * @param handler 
	 * @param responses
	 */
	private synchronized void recordChanges(DigServerDefaultHandler handler, List<Response> responses) {
		Set<OWLOntology> onts = generator.getReasoner().getLoadedOntologies();

		for( TellAxioms t : handler.getTells() ) {
			final String id = t.getId();
			if( kbURI.equals( t.getURI() ) ) {
				for( OWLAxiom a : t.getAxioms() ) {
					for( OWLOntology o : onts ) {
						changeTracker.recordAddition(o, a);
					}
				}
				responses.add( new OkResponse( id ) );
			}
			else {
				responses
						.add( new ErrorResponse(
								id,
								ErrorResponse.ERROR_CODE_UNDEFINED_KB,
								String
										.format(
												"KB identifier from client (%s) does not match server KB identifier (%s)",
												t.getURI(), kbURI ) ) );
			}
		}

		for( RetractAxioms r : handler.getRetractions() ) {
			final String id = r.getId();
			if( kbURI.equals( r.getURI() ) ) {
				for( OWLAxiom a : r.getAxioms() ) {
					for( OWLOntology o : onts ) {
						changeTracker.recordRemoval(o, a);
					}
				}
				responses.add( new OkResponse( id ) );
			}
			else {
				responses
						.add( new ErrorResponse( id, ErrorResponse.ERROR_CODE_UNDEFINED_KB ) );
			}
		}
	}
	
	private synchronized void applyChanges() throws OWLOntologyChangeException {			
		List<OWLAxiomChange> changes = changeTracker.getChanges();
		
		if (log.isLoggable(Level.FINE))
			log.fine(String.format("Applying %d changes", changes.size()));

		if (log.isLoggable(Level.FINER)) {
			for (OWLAxiomChange change : changes) {
				log.finer(change.toString());	
			}			
		}
		
		if( changes.size() > 0 ) {
			/*
			 * The HST algorithm used for explanations depends on the
			 * reasoner not being a listener, here it needs to be a
			 * listener, so it is one, but only temporarily.
			 */
			OWLOntologyManager m = generator.getOntologyManager();
			m.addOntologyChangeListener( clsReasoner );
			m.applyChanges( changes );
			m.removeOntologyChangeListener( clsReasoner );

			/*
			 * If no logical axioms reference an entity, that entity
			 * should not be in the KB. Pellet doesn't always get that
			 * right, so this block checks if the entity should be
			 * removed and, if so, forces a reload.
			 */
			{
				boolean reloaded = false;
				for( OWLAxiom a : changeTracker.getRemovals() ) {
					for( OWLEntity e : getEntities( a ) ) {

						boolean annotationOnly = true;
						Set<OWLAxiom> remaining = generator.getOntology()
								.getReferencingAxioms( e );
						for( OWLAxiom b : remaining ) {
							if( b.isLogicalAxiom() || (b instanceof OWLDeclarationAxiom) ) {
								annotationOnly = false;
								break;
							}
						}

						if( annotationOnly ) {
							boolean defined;
							try {
								defined = isDefined( generator.getReasoner(), e );
							} catch( OWLReasonerException ex ) {
								log
										.log(
												Level.SEVERE,
												"Exception caught testing if entity is defined, forcing reload",
												ex );
								defined = true;
							}
							if( defined ) {
								((Reasoner) generator.getReasoner()).refresh();
								reloaded = true;
								log.log( Level.FINE,
										"Reloaded KB to cause removal of entity", e );
								break;
							}
						}
					}
					if( reloaded )
						break;
				}
			}
		}
		
		changeTracker.clear();
	}
	
	private synchronized void answerQueries(DigServerDefaultHandler handler, List<Response> responses) throws OWLOntologyChangeException {
		for( URI u : handler.getKbURIs() ) {
			for( AskQuery q : handler.getQueries( u ) ) {
				responses.add( processor.getResponse( u, q ) );
			}
		}
	}

	public void handle(String pathInContext, String pathParams, HttpRequest request,
			HttpResponse response) {

		final String clientInfo = "(" + request.getHttpConnection().getRemoteAddr() + ":"
				+ request.getHttpConnection().getRemotePort() + ")";
		request.setHandled( true );

		try {
			DigServerDefaultHandler handler = new DigServerDefaultHandler();

			parseRequest(request, handler);

			if( log.isLoggable( Level.FINE ) ) 
				log.fine( "Successful parse for client " + clientInfo );
			
			List<Response> responses = new ArrayList<Response>();			

			recordChanges(handler, responses);
			
			if (APPLY_CHANGES_IMMEDIATELY)
				applyChanges();
			
			answerQueries(handler, responses);

			response.setStatus( HttpResponse.__200_OK );
			response.setContentType( HTTPConstants.RESPONSE_CONTENT_TYPE );

			DigRenderer renderer = new DigRenderer();

			renderer.setDocumentTag( ResponsesVocabulary.Elements.RESPONSES.toString() );
			renderer.startRendering( new OutputStreamWriter( response.getOutputStream() ) );

			for( Response r : responses ) {
				r.accept( renderer );
			}

			if( log.isLoggable( Level.FINER ) ) {
				log.finer( "Responses (" + responses.size() + ")" );
				for( Response r : responses ) {
					log.finer( r.toString() );
				}
			}

			renderer.endRendering();

		} catch( Exception e ) {
			log.log( Level.SEVERE, "Failed to respond to request for client " + clientInfo, e );
			response.setStatus( HttpResponse.__500_Internal_Server_Error, "Caught Exception: "
					+ e.getLocalizedMessage() );
		}

		try {
			response.commit();

			if( log.isLoggable( Level.FINE ) )
				log.fine( "Successful response for client " + clientInfo );

		} catch( IOException e ) {
			log.log( Level.SEVERE, "Error committing HTTP response for client " + clientInfo, e );
		}
	}

	public Collection<OWLOntology> getLoadedOntologies() {
		return clsReasoner.getLoadedOntologies();
	}
	
	public void setPersistenceManager( PersistenceManager persistenceManager ) {
		this.persistenceManager = persistenceManager;
	}
	
	public synchronized void loadOntology(OWLOntology ontology, URI kbURI) {
		try {
			this.kbURI = kbURI;
			
			ontology = initializeClsReasoner( ontology );
						
			generator.setOntology( ontology );
						
		} catch( OWLException e ) {
			throw new RuntimeException( e );
		}
	}
	
	private static final double INCREMENTAL_UPDATE_FEASIBILITY_CUTOFF = 0.25;
	
	/**
	 * Estimates whether an incremental update to the classifier is feasible after the classifier was restored from a file,
	 * and the underlying ontology was changed in the meanwhile.
	 * 
	 * The current algorithm simply assumes that if the number of changes is greater than a specified cut-off ratio, then the update becomes unreasonable. 
	 * 
	 * @param diff the changes to the ontology.
	 * @param restoredAxiomsCount the number of total axioms in the ontology for the restored classifier
	 * @return true if the update is deemed feasible, false otherwise
	 */
	private boolean isIncrementalUpdateFeasible( OntologyDiff diff, int restoredAxiomsCount ) {
		if( restoredAxiomsCount > 0 ) {
			return ( (double) diff.getDiffCount() / (double) restoredAxiomsCount ) <= INCREMENTAL_UPDATE_FEASIBILITY_CUTOFF;
		}
		
		// this case will only happen, if the restored classifier did not contain any axioms; in such a case
		// update does not make any sense -- it is equivalent to performing a build from scratch
		return false;
	}
	
	/**
	 * Updates the reasoner with a set of changes to the ontology. 
	 * 
	 * @param classifier the classifier to be update
	 * @param ontologyDiff the changes to the ontology (with respect to the state of the ontology when the reasoner was created, or updated the last time)
	 * @throws OWLException if an error should occur while updating the reasoner
	 */
	private void updateClsReasoner( IncrementalClassifier classifier, OntologyDiff ontologyDiff ) throws OWLException {
		Collection<OWLOntology> ontologies = classifier.getLoadedOntologies();
		
		if( ontologies.isEmpty() ) {
			throw new IllegalArgumentException( "The classifier for the update does not have any ontologies loaded!" );			
		}
		
		OWLOntology ontology = ontologies.iterator().next();
		
		classifier.ontologiesChanged(new LinkedList<OWLOntologyChange>( ontologyDiff.getChanges( ontology ) ) ); 
	}
	
	/**
	 * Attempts to restore the class reasoner from a file.
	 * 
	 * @param ontology the current ontology (for comparison purposes to determine whether the state of the saved classifier is up-to-date); the ontology passed may
	 * be null (in such a case any acceptable saved state of the classifier will be considered up-to-date)
	 * @return the classifier restored to a file and updated to the current state of the ontology (if provided), or null (if it was impossible to restore classifier
	 * or infeasible to update)
	 * @throws OWLException if an update of the classifier should fail
	 * @throws IOException if an I/O error should occur during the restoring of the classifier
	 */
	private IncrementalClassifier restoreClsReasoner( OWLOntology ontology ) throws OWLException, IOException {
		IncrementalClassifier restoredClsReasoner = null;
		
		if( ( persistenceManager != null ) && persistenceManager.isRestoreNeeded() ) {
			persistenceManager.setRestoreNeeded( false );
			restoredClsReasoner = persistenceManager.restore( ontology );
			
			if ( restoredClsReasoner != null ) {
				if ( restoredClsReasoner.isClassified() ) {
					persistenceManager.setCurrentStatePersisted( true );
				} else {
					restoredClsReasoner.classify();
					persistenceManager.setCurrentStatePersisted( false );
				}
			}			
		}
		
		return restoredClsReasoner;
	}
	
	/**
	 * Initializes the current class reasoner by either loading an ontology to it, or by loading a previously saved
	 * state of the class reasoner (if one exists). If the previously saved state of the class reasoner corresponds to a different version of the ontology
	 * (there were changes to the ontology since the state was saved), the reasoner may be incrementally updated with these changes (if feasible).
	 * 
	 * @param ontology the current version of the ontology or null if the ontology was not loaded. (Null is only allowed if a reasoner is to be restored from
	 * a saved state.)
	 * @return the current ontology after the classifier is restored (if an ontology was passed to this method, it is the same ontology; otherwise
	 * it is the ontology from the restored classifier)
	 * @throws OWLException if an exception should occur while updating the classifier with the changes to the ontology
	 * @throws IllegalArgumentException if no ontology has been provided and there is no acceptable state of the classifier available
	 */
	private OWLOntology initializeClsReasoner( OWLOntology ontology ) throws OWLException {
		IncrementalClassifier restoredClsReasoner = null;
		
		if( ( persistenceManager != null ) && persistenceManager.isRestoreNeeded() ) {
			try {				
				restoredClsReasoner = restoreClsReasoner( ontology );				
			} catch( IOException e ) {
				e.printStackTrace();
			}						
		}

		if ( restoredClsReasoner != null ) {
			clsReasoner = restoredClsReasoner;
			
			Collection<OWLOntology> loadedOntologies = clsReasoner.getLoadedOntologies();
			
			if( !loadedOntologies.isEmpty() ) {
				ontology = loadedOntologies.iterator().next();
			}
			
			generator.setReasoner( clsReasoner.getReasoner() );
		} else {
			if ( ontology == null ) {
				throw new IllegalArgumentException( "There is no ontology provided and the persisted classifier could not be restored" );
			}
			
			clsReasoner.loadOntologies( Collections.singleton( ontology ) );
			
			if ( persistenceManager != null ) {
				persistenceManager.setCurrentStatePersisted( false );
			}
		}	
		
		return ontology;
	}
	
	public synchronized void prepareTaxonomy() {
		try {
			clsReasoner.classify();

			// This forces loading the second pellet reasoner
			singleExplanation.getAltReasoner();
		} catch( OWLReasonerException e ) {
			throw new RuntimeException( e );
		}
	}
	
	public void setMultiThreadedClassification( boolean multiThreaded ) {
		clsReasoner.setMultiThreaded( multiThreaded );
	}
	
	public boolean isMultiThreadedClassification() {
		return clsReasoner.isMultiThreaded();
	}
	
	public void saveClassifier( OutputStream outputStream ) throws IOException {
		// FIXME I am not sure whether synchronizing here is a good idea. Persistence
		// may take a minute or two, and nobody will be able to access the classifier during that time
		synchronized ( this ) {
			IncrementalClassifierPersistence.save( clsReasoner, outputStream );			
		}
	}
}