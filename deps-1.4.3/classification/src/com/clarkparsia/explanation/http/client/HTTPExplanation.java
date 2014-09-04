package com.clarkparsia.explanation.http.client;

import static com.clarkparsia.dig20.explanation.ExplanationFactory.explain;

import java.net.URI;
import java.net.URL;
import java.util.Set;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLSubClassAxiom;

import com.clarkparsia.dig20.QueryProcessor;
import com.clarkparsia.dig20.client.DefaultDigClient;
import com.clarkparsia.dig20.exceptions.ErrorResponseException;
import com.clarkparsia.dig20.explanation.ExplainQuery;
import com.clarkparsia.dig20.explanation.ExplanationsResponse;
import com.clarkparsia.dig20.responses.BooleanAskResult;
import com.clarkparsia.dig20.responses.ErrorResponse;
import com.clarkparsia.dig20.responses.OkResponse;
import com.clarkparsia.dig20.responses.Response;
import com.clarkparsia.dig20.responses.ResponseVisitor;
import com.clarkparsia.dig20.responses.SynonymsAskResult;
import com.clarkparsia.explanation.ExplanationGenerator;

/**
 * <p>
 * Title: HTTPExplanation
 * </p>
 * <p>
 * Description: Client side of an http client/server based explanation
 * generator.
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
public class HTTPExplanation implements ExplanationGenerator {

	private static class ExplanationCollector implements ResponseVisitor {

		private Set<Set<OWLAxiom>>	exp;
		private ErrorResponse		error;

		public Set<Set<OWLAxiom>> getExplanations() throws ErrorResponseException {
			if( error != null )
				throw new ErrorResponseException( error );
			return exp;
		}

		public void visit(BooleanAskResult r) {
			throw new UnsupportedOperationException();
		}

		public void visit(ErrorResponse r) {
			error = r;
		}

		public void visit(ExplanationsResponse r) {
			exp = r.getExplanations();
		}

		public void visit(OkResponse r) {
			throw new UnsupportedOperationException();
		}

		public void visit(SynonymsAskResult r) {
			throw new UnsupportedOperationException();
		}

	}

	private final QueryProcessor	digClient;
	private final URI				kbURI;

	private OWLOntologyManager	manager	= null;

	public HTTPExplanation(OWLOntologyManager manager, URL server, URI kbURI) {
		this.manager = manager;
		this.digClient = new DefaultDigClient( server );
		this.kbURI = kbURI;
	}

	public Set<OWLAxiom> getExplanation(OWLDescription unsatClass) {

		Set<Set<OWLAxiom>> explanations = getExplanations( unsatClass, 1 );

		return explanations.isEmpty()
			? null
			: explanations.iterator().next();
	}

	private Set<Set<OWLAxiom>> getExplanations(OWLAxiom a, int max) throws ErrorResponseException {

		ExplainQuery q = (max == 0)
			? explain( a )
			: explain( a, max );

		Response r = digClient.getResponse( kbURI, q );
		if( r == null )
			return null;

		ExplanationCollector collector = new ExplanationCollector();
		r.accept( collector );

		return collector.getExplanations();
	}

	public Set<Set<OWLAxiom>> getExplanations(OWLDescription unsatClass) throws ErrorResponseException {
		return getExplanations( unsatClass, 0 );
	}

	public Set<Set<OWLAxiom>> getExplanations(OWLDescription unsatClass, int maxExplanations) throws ErrorResponseException {
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLSubClassAxiom axiom = factory.getOWLSubClassAxiom( unsatClass, factory.getOWLNothing() );
		return getExplanations( axiom, maxExplanations );
	}
}