package com.clarkparsia.dig20.client;

import static com.clarkparsia.dig20.asks.AskQueryFactory.classifykb;
import static com.clarkparsia.dig20.asks.AskQueryFactory.equivalentClasses;
import static com.clarkparsia.dig20.asks.AskQueryFactory.isEquivalentClassTo;
import static com.clarkparsia.dig20.asks.AskQueryFactory.subClasses;
import static com.clarkparsia.dig20.asks.AskQueryFactory.superClasses;
import static com.clarkparsia.dig20.explanation.ExplanationFactory.explain;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyChangeListener;
import org.semanticweb.owl.model.OWLOntologyChangeVisitor;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.RemoveAxiom;
import org.semanticweb.owl.model.SetOntologyURI;

import com.clarkparsia.dig20.RetractAxioms;
import com.clarkparsia.dig20.TellAxioms;
import com.clarkparsia.dig20.asks.AskQuery;
import com.clarkparsia.dig20.asks.AskQueryFactory;
import com.clarkparsia.dig20.asks.ObjectHierarchyQuery;
import com.clarkparsia.dig20.asks.SubClassesQuery;
import com.clarkparsia.dig20.asks.SuperClassesQuery;
import com.clarkparsia.dig20.exceptions.DigClientException;
import com.clarkparsia.dig20.exceptions.ErrorResponseException;
import com.clarkparsia.dig20.exceptions.ProtocolErrorException;
import com.clarkparsia.dig20.explanation.ExplainQuery;
import com.clarkparsia.dig20.explanation.ExplanationsResponse;
import com.clarkparsia.dig20.responses.BooleanAskResult;
import com.clarkparsia.dig20.responses.ErrorResponse;
import com.clarkparsia.dig20.responses.OkResponse;
import com.clarkparsia.dig20.responses.Response;
import com.clarkparsia.dig20.responses.ResponseVisitor;
import com.clarkparsia.dig20.responses.SynonymsAskResult;

/**
 * <p>
 * Title: DigReasoner
 * </p>
 * <p>
 * Description: "DIG 2" client implementation of OWLAPI OWLReasoner interface
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007, 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Mike Smith
 */
public class DigReasoner implements OWLReasoner, OWLOntologyChangeListener {

	private static class SynonymClassAdapter implements ResponseVisitor {

		Set<Set<OWLClass>>	col	= new HashSet<Set<OWLClass>>();
		ErrorResponse		error;

		public Set<Set<OWLClass>> getSynonyms() throws ErrorResponseException {
			if( error != null )
				throw new ErrorResponseException( error );
			return col;
		}

		public void reset() {
			col = new HashSet<Set<OWLClass>>();
			error = null;
		}

		public void visit(BooleanAskResult r) {
			throw new UnsupportedOperationException();
		}

		public void visit(ErrorResponse r) {
			error = r;
		}

		public void visit(ExplanationsResponse r) {
			throw new UnsupportedOperationException();
		}

		public void visit(OkResponse r) {
			throw new UnsupportedOperationException();
		}

		public void visit(SynonymsAskResult r) {

			for( List<? extends OWLObject> l : r.getOWLObjects() ) {
				Set<OWLClass> clses = new HashSet<OWLClass>();
				for( OWLObject o : l ) {
					if( o instanceof OWLClass ) {
						clses.add( (OWLClass) o );
					}
					else
						throw new UnsupportedOperationException();
				}
				if( !clses.isEmpty() )
					col.add( clses );
			}
		}
	}

	final public static int							SUBS_INDEX		= 1;

	final public static int							SUPERS_INDEX	= 0;

	/**
	 * Concurrent access guarded by {@code this}
	 */
	final private List<OWLAxiom>					additions;
	/**
	 * Concurrent access guarded by {@code this}
	 */
	private int										changeId;
	/**
	 * ThreadLocal used to force thread confinement
	 */
	final private ThreadLocal<SynonymClassAdapter>	clsAdapter;
	/**
	 * Thread safe object, concurrent access acceptable
	 */
	final private DefaultDigClient					digClient;
	/**
	 * Immuatable object, concurrent access acceptable
	 */
	final private URI								kbURI;
	/**
	 * Immutable object, concurrent access acceptable
	 */
	final private OWLClass							owlNothing;
	/**
	 * Immutable object, concurrent access acceptable
	 */
	final private OWLClass							owlThing;
	/**
	 * Concurrent access guarded by {@code this}
	 */
	final private List<OWLAxiom>					removals;
	

	public DigReasoner(URL digServer, URI kbURI, OWLOntologyManager manager) {
		digClient = new DefaultDigClient( digServer );
		this.kbURI = kbURI;
		additions = new ArrayList<OWLAxiom>();
		removals = new ArrayList<OWLAxiom>();
		owlThing = manager.getOWLDataFactory().getOWLThing();
		owlNothing = manager.getOWLDataFactory().getOWLNothing();

		clsAdapter = new ThreadLocal<SynonymClassAdapter>() {

			@Override
			protected SynonymClassAdapter initialValue() {
				return new SynonymClassAdapter();
			}
		};
		
		changeId = 0;
	}

	private String getChangeId() {
		return "changeid-" + (changeId++);
	}

	private boolean booleanResponse(AskQuery q) throws OWLReasonerException {
		Response r = digClient.getResponse( kbURI, q );
		if( r == null )
			throw new ProtocolErrorException( "Unable to parse server response" );
		if( r instanceof BooleanAskResult ) {
			BooleanAskResult bar = (BooleanAskResult) r;
			return bar.getBoolean();
		}
		else if( r instanceof ErrorResponse )
			throw new ErrorResponseException( (ErrorResponse) r );
		else
			throw new ProtocolErrorException( "Boolean server response expected, not received" );
	}

	public void classify() throws OWLReasonerException {
		synchronize();
		noOpQuery( classifykb() );
	}

	public void clearOntologies() throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public void dispose() throws OWLReasonerException {
	}

	private Set<Set<OWLAxiom>> explanationResponse(ExplainQuery q) throws OWLReasonerException {
		Response r = digClient.getResponse( kbURI, q );
		if( r == null )
			throw new ProtocolErrorException( "Unable to parse server response" );
		if( r instanceof ExplanationsResponse ) {
			ExplanationsResponse er = (ExplanationsResponse) r;
			return er.getExplanations();
		}
		else if( r instanceof ErrorResponse )
			throw new ErrorResponseException( (ErrorResponse) r );
		else
			throw new ProtocolErrorException( "Explanations server response expected, not received" );
	}

	public Set<Set<OWLClass>> getAncestorClasses(OWLDescription clsC) throws OWLReasonerException {
		synchronize();
		return namedClassesFilter( superClasses( clsC, /* direct = */false ) );
	}

	public Set<Set<OWLDataProperty>> getAncestorProperties(OWLDataProperty property)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Set<Set<OWLObjectProperty>> getAncestorProperties(OWLObjectProperty property)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Map<OWLDataProperty, Set<OWLConstant>> getDataPropertyRelationships(
			OWLIndividual individual) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Set<Set<OWLClass>> getDescendantClasses(OWLDescription clsC) throws OWLReasonerException {
		synchronize();
		return namedClassesFilter( subClasses( clsC, /* direct = */false ) );
	}

	public Set<Set<OWLDataProperty>> getDescendantProperties(OWLDataProperty property)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Set<Set<OWLObjectProperty>> getDescendantProperties(OWLObjectProperty property)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Set<Set<OWLDescription>> getDomains(OWLDataProperty property)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Set<Set<OWLDescription>> getDomains(OWLObjectProperty property)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Set<OWLClass> getEquivalentClasses(OWLDescription clsC) throws OWLReasonerException {
		synchronize();
		Set<Set<OWLClass>> resp = namedClassesFilter( equivalentClasses( clsC ) );
		if( resp.size() == 0 )
			return Collections.emptySet();
		else if( resp.size() > 1 )
			throw new ProtocolErrorException( "Unexpected synonym sets in equivalent class query" );
		return resp.iterator().next();
	}

	public Set<OWLDataProperty> getEquivalentProperties(OWLDataProperty property)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Set<OWLObjectProperty> getEquivalentProperties(OWLObjectProperty property)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Set<Set<OWLAxiom>> getExplanations(OWLAxiom axiom, int max) throws OWLReasonerException {
		synchronize();
		return explanationResponse( explain(axiom, max) );
	}

	public Set<OWLClass> getInconsistentClasses() throws OWLReasonerException {
		return getEquivalentClasses( owlNothing );
	}

	public Set<OWLIndividual> getIndividuals(OWLDescription clsC, boolean direct)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Set<Set<OWLObjectProperty>> getInverseProperties(OWLObjectProperty property)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Set<OWLOntology> getLoadedOntologies() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Map<OWLObjectProperty, Set<OWLIndividual>> getObjectPropertyRelationships(
			OWLIndividual individual) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Set<OWLDataRange> getRanges(OWLDataProperty property) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Set<OWLDescription> getRanges(OWLObjectProperty property) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Set<OWLIndividual> getRelatedIndividuals(OWLIndividual subject,
			OWLObjectPropertyExpression property) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Set<OWLConstant> getRelatedValues(OWLIndividual subject,
			OWLDataPropertyExpression property) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public URL getServerURL() {
		return digClient.getServerURL();
	}

	public Map<OWLDescription, Set<Set<OWLClass>>> getSubClasses(
			Collection<? extends OWLDescription> clses) throws OWLReasonerException {

		Map<OWLDescription, Set<Set<OWLClass>>> ret = new HashMap<OWLDescription, Set<Set<OWLClass>>>();

		synchronize();
		Map<String, SubClassesQuery> queries = new HashMap<String, SubClassesQuery>();
		for( OWLDescription clsC : clses ) {
			SubClassesQuery q = subClasses( clsC, /* direct = */true );
			queries.put( q.getId(), q );
		}
		Collection<Response> responses = digClient.getResponses( kbURI, queries.values() );
		for( Response r : responses ) {
			String id = r.getId();
			if( r instanceof ErrorResponse ) {
				throw new ErrorResponseException( (ErrorResponse) r );
			}
			SubClassesQuery q = queries.get( id );
			if( q == null )
				throw new ProtocolErrorException( "Response id does not match any query id: " + id );

			final SynonymClassAdapter adapter = clsAdapter.get();
			adapter.reset();
			r.accept( adapter );
			ret.put( q.getOWLObject(), adapter.getSynonyms() );
		}

		return ret;
	}

	public Set<Set<OWLClass>> getSubClasses(OWLDescription clsC) throws OWLReasonerException {
		synchronize();
		return namedClassesFilter( subClasses( clsC, /* direct = */true ) );
	}

	public Set<Set<OWLDataProperty>> getSubProperties(OWLDataProperty property)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Set<Set<OWLObjectProperty>> getSubProperties(OWLObjectProperty property)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/**
	 * Batch query to get super and sub classes for a list of classes
	 * 
	 * @param clses
	 * @return
	 * @throws OWLReasonerException
	 */
	public Map<OWLDescription, Set<Set<OWLClass>>[]> getSubSuperClasses(
			Collection<? extends OWLDescription> clses) throws OWLReasonerException {

		Map<OWLDescription, Set<Set<OWLClass>>[]> ret = new HashMap<OWLDescription, Set<Set<OWLClass>>[]>();

		synchronize();
		Map<String, ObjectHierarchyQuery<OWLDescription>> queries = new HashMap<String, ObjectHierarchyQuery<OWLDescription>>();
		for( OWLDescription clsC : clses ) {
			SubClassesQuery subQ = subClasses( clsC, /* direct = */true );
			queries.put( subQ.getId(), subQ );
			SuperClassesQuery supQ = superClasses( clsC, /* direct = */true );
			queries.put( supQ.getId(), supQ );
		}
		Collection<Response> responses = digClient.getResponses( kbURI, queries.values() );
		for( Response r : responses ) {
			String id = r.getId();
			if( r instanceof ErrorResponse ) {
				throw new ErrorResponseException( (ErrorResponse) r );
			}
			ObjectHierarchyQuery<OWLDescription> q = queries.get( id );
			if( q == null )
				throw new ProtocolErrorException( "Response id does not match any query id: " + id );

			final SynonymClassAdapter adapter = clsAdapter.get();
			adapter.reset();
			r.accept( adapter );
			final Set<Set<OWLClass>> synonyms = adapter.getSynonyms();
			final OWLDescription d = q.getOWLObject();
			Set<Set<OWLClass>>[] retValue = ret.get( d );
			if( retValue == null ) {
				retValue = new Set[2];
				ret.put( d, retValue );
			}
			if( q instanceof SubClassesQuery ) {
				retValue[SUBS_INDEX] = synonyms;
			}
			else if( q instanceof SuperClassesQuery ) {
				retValue[SUPERS_INDEX] = synonyms;
			}
			else
				throw new IllegalStateException( "Unexpected query type for query id: " + id + " "
						+ q.getClass().getCanonicalName() );
		}

		return ret;
	}

	public Map<OWLDescription, Set<Set<OWLClass>>> getSuperClasses(
			Collection<? extends OWLDescription> clses) throws OWLReasonerException {

		Map<OWLDescription, Set<Set<OWLClass>>> ret = new HashMap<OWLDescription, Set<Set<OWLClass>>>();

		synchronize();
		Map<String, SuperClassesQuery> queries = new HashMap<String, SuperClassesQuery>();
		for( OWLDescription clsC : clses ) {
			SuperClassesQuery q = superClasses( clsC, /* direct = */true );
			queries.put( q.getId(), q );
		}
		Collection<Response> responses = digClient.getResponses( kbURI, queries.values() );
		for( Response r : responses ) {
			String id = r.getId();
			if( r instanceof ErrorResponse ) {
				throw new ErrorResponseException( (ErrorResponse) r );
			}
			SuperClassesQuery q = queries.get( id );
			if( q == null )
				throw new ProtocolErrorException( "Response id does not match any query id: " + id );

			final SynonymClassAdapter adapter = clsAdapter.get();
			adapter.reset();
			r.accept( adapter );
			ret.put( q.getOWLObject(), adapter.getSynonyms() );
		}

		return ret;
	}

	public Set<Set<OWLClass>> getSuperClasses(OWLDescription clsC) throws OWLReasonerException {
		synchronize();
		return namedClassesFilter( superClasses( clsC, /* direct = */true ) );
	}

	public Set<Set<OWLDataProperty>> getSuperProperties(OWLDataProperty property)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Set<Set<OWLObjectProperty>> getSuperProperties(OWLObjectProperty property)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Set<Set<OWLClass>> getTypes(OWLIndividual individual, boolean direct)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public boolean hasDataPropertyRelationship(OWLIndividual subject,
			OWLDataPropertyExpression property, OWLConstant object) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public boolean hasObjectPropertyRelationship(OWLIndividual subject,
			OWLObjectPropertyExpression property, OWLIndividual object) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public boolean hasType(OWLIndividual individual, OWLDescription type, boolean direct)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public boolean isAntiSymmetric(OWLObjectProperty property) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public boolean isClassified() throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public boolean isConsistent(OWLOntology ontology) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public boolean isDefined(OWLClass cls) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public boolean isDefined(OWLDataProperty prop) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public boolean isDefined(OWLIndividual ind) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public boolean isDefined(OWLObjectProperty prop) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public boolean isEquivalentClass(OWLDescription clsC, OWLDescription clsD)
			throws OWLReasonerException {
		synchronize();
		AskQuery q = isEquivalentClassTo( clsC, clsD );
		return booleanResponse( q );
	}

	public boolean isFunctional(OWLDataProperty property) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public boolean isFunctional(OWLObjectProperty property) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public boolean isInverseFunctional(OWLObjectProperty property) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public boolean isIrreflexive(OWLObjectProperty property) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public boolean isRealised() throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public boolean isReflexive(OWLObjectProperty property) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public boolean isSatisfiable(OWLDescription description) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public boolean isSubClassOf(OWLDescription clsC, OWLDescription clsD)
			throws OWLReasonerException {
		synchronize();
		AskQuery q = AskQueryFactory.isSubClassOf( clsC, clsD );
		return booleanResponse( q );
	}

	public boolean isSymmetric(OWLObjectProperty property) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public boolean isTransitive(OWLObjectProperty property) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public void loadOntologies(Set<OWLOntology> ontologies) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	private Set<Set<OWLClass>> namedClassesFilter(AskQuery q) throws OWLReasonerException {
		Response r = digClient.getResponse( kbURI, q );
		if( r == null )
			throw new ProtocolErrorException( "Unable to parse server response" );

		final SynonymClassAdapter adapter = clsAdapter.get();
		adapter.reset();
		r.accept( adapter );
		return adapter.getSynonyms();
	}

	private void noOpQuery(AskQuery q) {
		digClient.getResponse( kbURI, q );
	}

	public synchronized void ontologiesChanged(List<? extends OWLOntologyChange> changes)
			throws OWLException {
		for( OWLOntologyChange c : changes ) {
			c.accept( new OWLOntologyChangeVisitor() {

				public void visit(AddAxiom change) {
					additions.add( change.getAxiom() );
				}

				public void visit(RemoveAxiom change) {
					removals.add( change.getAxiom() );
				}

				public void visit(SetOntologyURI change) {
					throw new UnsupportedOperationException();
				}
			} );
		}
	}

	public void realise() throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public void setServerURL(URL theURL) {
		digClient.setServerURL( theURL );
	}

	public synchronized void synchronize() throws OWLReasonerException {
		if( !additions.isEmpty() || !removals.isEmpty() ) {

			int nDirectives = 0;

			Collection<TellAxioms> tells;
			String tellId;
			if( !additions.isEmpty() ) {
				tellId = getChangeId();
				tells = Collections.singleton( new TellAxioms( tellId, kbURI,
						new ArrayList<OWLAxiom>( additions ) ) );
				additions.clear();
				nDirectives++;
			}
			else {
				tells = Collections.emptySet();
				tellId = null;
			}

			Collection<RetractAxioms> retractions;
			String retractId;
			if( !removals.isEmpty() ) {
				retractId = getChangeId();
				retractions = Collections.singleton( new RetractAxioms( retractId, kbURI,
						new ArrayList<OWLAxiom>( removals ) ) );
				removals.clear();
				nDirectives++;
			}
			else {
				retractions = Collections.emptySet();
				retractId = null;
			}

			Collection<Response> responses = digClient.applyChanges( tells, retractions );
			// FIXME: All below could be moved out of the synchronized block
			boolean tellOk = (tellId == null);
			boolean retractOk = (retractId == null);
			for( Response r : responses ) {
				String rId = r.getId();
				if( tellId != null && tellId.equals( rId ) ) {
					if( tellOk )
						throw new ProtocolErrorException( "Multiple responses to axiom addition" );
					else {
						if( r instanceof OkResponse )
							tellOk = true;
						else if( r instanceof ErrorResponse )
							throw new ErrorResponseException( (ErrorResponse) r );
						else
							throw new ProtocolErrorException(
									"Unexpected response to axiom addition: " + r );
					}
				}
				else if( retractId != null && retractId.equals( rId ) ) {
					if( retractOk )
						throw new ProtocolErrorException( "Multiple responses to axiom retraction" );
					else {
						if( r instanceof OkResponse )
							retractOk = true;
						else if( r instanceof ErrorResponse )
							throw new ErrorResponseException( (ErrorResponse) r );
						else
							throw new ProtocolErrorException(
									"Unexpected response to axiom retraction: " + r );
					}
				}
				else
					throw new ProtocolErrorException(
							"Unexpected Dig response, not matching any change directive: " + r );
			}
		}

	}

	public void unloadOntologies(Set<OWLOntology> ontologies) throws OWLReasonerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
}
