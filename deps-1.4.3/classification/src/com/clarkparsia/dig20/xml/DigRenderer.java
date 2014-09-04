package com.clarkparsia.dig20.xml;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.coode.owlapi.owlxml.renderer.OWLXMLObjectRenderer;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLOntology;

import com.clarkparsia.dig20.DigVisitor;
import com.clarkparsia.dig20.asks.AskQuery;
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
import com.clarkparsia.dig20.explanation.ExplanationsResponse;
import com.clarkparsia.dig20.responses.BooleanAskResult;
import com.clarkparsia.dig20.responses.ErrorResponse;
import com.clarkparsia.dig20.responses.OkResponse;
import com.clarkparsia.dig20.responses.SynonymsAskResult;
import com.clarkparsia.dig20.xml.asks.AskQueryType;
import com.clarkparsia.dig20.xml.explanation.ExplanationVocabulary;
import com.clarkparsia.dig20.xml.responses.ResponsesVocabulary;
import com.clarkparsia.explanation.io.ExplanationRenderer;

public class DigRenderer implements ExplanationRenderer, DigVisitor {
	private static final OWLOntology	ontology	= DigWriter.getEmptyOntology();

	private String						documentTag	= "AxiomExplanations";
	protected OWLXMLObjectRenderer		renderer;
	protected DigWriter					xeWriter;

	public DigRenderer() {
	}

	public void endRendering() throws OWLException {
		xeWriter.endDocument();
		xeWriter = null;
		renderer = null;
	}

	public String getDocumentTag() {
		return documentTag;
	}

	public void render(URI uri, Collection<? extends AskQuery> queries) {
		xeWriter.writeStartElement( DigVocabulary.ASKS.getURI() );
		xeWriter.writeAttribute( DigVocabulary.URI_ATTRIBUTE, uri.toASCIIString() );
		for( AskQuery q : queries )
			q.accept( this );
		xeWriter.writeEndElement();
	}

	public void render(OWLAxiom axiom, Set<Set<OWLAxiom>> explanations) throws OWLException,
			IOException {
		xeWriter.writeStartElement( DigVocabulary.EXPLANATIONS.getURI() );

		xeWriter.writeStartElement( DigVocabulary.AXIOM.getURI() );
		axiom.accept( renderer );
		xeWriter.writeEndElement();

		for( Set<OWLAxiom> explanation : explanations ) {
			xeWriter.writeStartElement( DigVocabulary.AXIOMS.getURI() );
			for( OWLAxiom a : explanation )
				a.accept( renderer );
			xeWriter.writeEndElement();
		}

		xeWriter.writeEndElement();
	}

	public void renderAxioms(String id, URI uri, Collection<? extends OWLAxiom> axioms) {
		xeWriter.writeStartElement( DigVocabulary.AXIOMS.getURI() );
		xeWriter.writeAttribute( DigVocabulary.ID_ATTRIBUTE, id );
		xeWriter.writeAttribute( DigVocabulary.URI_ATTRIBUTE, uri.toASCIIString() );
		for( OWLAxiom a : axioms ) {
			a.accept( renderer );
		}
		xeWriter.writeEndElement();
	}

	public void renderRetractions(String id, URI uri, Collection<? extends OWLAxiom> axioms) {
		xeWriter.writeStartElement( DigVocabulary.RETRACTIONS.getURI() );
		xeWriter.writeAttribute( DigVocabulary.ID_ATTRIBUTE, id );
		xeWriter.writeAttribute( DigVocabulary.URI_ATTRIBUTE, uri.toASCIIString() );
		for( OWLAxiom a : axioms ) {
			a.accept( renderer );
		}
		xeWriter.writeEndElement();
	}

	public void setDocumentTag(String documentTag) {
		this.documentTag = documentTag;
	}

	public void startRendering(Writer writer) throws OWLException, IOException {
		xeWriter = DigWriter.create( writer );
		xeWriter.startDocument( documentTag );
		renderer = new OWLXMLObjectRenderer( ontology, xeWriter );
	}

	public void visit(BooleanAskResult r) {
		xeWriter.writeStartElement( ResponsesVocabulary.Elements.RESPONSE.getURI() );
		xeWriter.writeAttribute( ResponsesVocabulary.ID_ATTRIBUTE, r.getId() );
		xeWriter.writeStartElement( ResponsesVocabulary.Elements.BOOLEAN.getURI() );
		xeWriter.writeTextContent( Boolean.toString( r.getBoolean() ) );
		xeWriter.writeEndElement();
		xeWriter.writeEndElement();

	}

	public void visit(ConsistentQuery q) {
		xeWriter.writeStartElement( AskQueryType.CONSISTENT_KB.getURI() );
		xeWriter.writeAttribute( AskQueryType.ID_ATTR, q.getId() );
		xeWriter.writeEndElement();
	}

	public void visit(ClassifyQuery q) {
		xeWriter.writeStartElement( AskQueryType.CLASSIFY_KB.getURI() );
		xeWriter.writeAttribute( AskQueryType.ID_ATTR, q.getId() );
		xeWriter.writeEndElement();
	}

	public void visit(EquivalentClassesQuery q) {
		xeWriter.writeStartElement( AskQueryType.EQUIVALENT_CLASSES.getURI() );
		xeWriter.writeAttribute( AskQueryType.ID_ATTR, q.getId() );
		q.getOWLObject().accept( renderer );
		xeWriter.writeEndElement();
	}

	public void visit(ExplainQuery q) {
		xeWriter.writeStartElement( ExplanationVocabulary.Elements.EXPLAIN.getURI() );
		xeWriter.writeAttribute( AskQueryType.ID_ATTR, q.getId() );
		if( !q.getAll() )
			xeWriter.writeAttribute( ExplanationVocabulary.NEXP_ATTRIBUTE, Integer.toString( q
					.getMax() ) );
		q.getOWLObject().accept( renderer );
		xeWriter.writeEndElement();
	}

	public void visit(ExplanationsResponse r) {
		xeWriter.writeStartElement( ExplanationVocabulary.Elements.EXPLANATIONS.getURI() );
		xeWriter.writeAttribute( ResponsesVocabulary.ID_ATTRIBUTE, r.getId() );
		for( Set<OWLAxiom> s : r.getExplanations() ) {
			xeWriter.writeStartElement( DigVocabulary.AXIOMS.getURI() );
			for( OWLAxiom a : s )
				a.accept( renderer );
			xeWriter.writeEndElement();
		}
		xeWriter.writeEndElement();
	}

	public void visit(IsEquivalentClassToQuery q) {
		xeWriter.writeStartElement( AskQueryType.IS_EQUIVALENT_CLASS_TO.getURI() );
		xeWriter.writeAttribute( AskQueryType.ID_ATTR, q.getId() );
		q.getFirst().accept( renderer );
		q.getSecond().accept( renderer );
		xeWriter.writeEndElement();
	}

	public void visit(IsSatisfiableQuery q) {
		xeWriter.writeStartElement( AskQueryType.IS_SATISFIABLE.getURI() );
		xeWriter.writeAttribute( AskQueryType.ID_ATTR, q.getId() );
		q.getOWLObject().accept( renderer );
		xeWriter.writeEndElement();
	}

	public void visit(IsSubClassOfQuery q) {
		xeWriter.writeStartElement( AskQueryType.IS_SUBCLASS_OF.getURI() );
		xeWriter.writeAttribute( AskQueryType.ID_ATTR, q.getId() );
		Boolean direct = q.getDirect();
		if( direct != null )
			xeWriter.writeAttribute( AskQueryType.DIRECT_ATTR, direct.toString() );
		q.getSub().accept( renderer );
		q.getSuper().accept( renderer );
		xeWriter.writeEndElement();
	}

	public void visit(NamedClassesQuery q) {
		xeWriter.writeStartElement( AskQueryType.NAMED_CLASSES.getURI() );
		xeWriter.writeAttribute( AskQueryType.ID_ATTR, q.getId() );
		xeWriter.writeEndElement();
	}

	public void visit(NamedDataPropertiesQuery q) {
		xeWriter.writeStartElement( AskQueryType.NAMED_DATAPROPERTIES.getURI() );
		xeWriter.writeAttribute( AskQueryType.ID_ATTR, q.getId() );
		xeWriter.writeEndElement();
	}

	public void visit(NamedIndividualsQuery q) {
		xeWriter.writeStartElement( AskQueryType.NAMED_INDIVIDUALS.getURI() );
		xeWriter.writeAttribute( AskQueryType.ID_ATTR, q.getId() );
		xeWriter.writeEndElement();
	}

	public void visit(NamedObjectPropertiesQuery q) {
		xeWriter.writeStartElement( AskQueryType.NAMED_OBJECTPROPERTIES.getURI() );
		xeWriter.writeAttribute( AskQueryType.ID_ATTR, q.getId() );
		xeWriter.writeEndElement();
	}

	public void visit(OkResponse r) {
		xeWriter.writeStartElement( ResponsesVocabulary.Elements.RESPONSE.getURI() );
		xeWriter.writeAttribute( ResponsesVocabulary.ID_ATTRIBUTE, r.getId() );
		xeWriter.writeStartElement( ResponsesVocabulary.Elements.OK.getURI() );
		xeWriter.writeEndElement();
		xeWriter.writeEndElement();
	}

	public void visit(SubClassesQuery q) {
		xeWriter.writeStartElement( AskQueryType.SUBCLASSES.getURI() );
		xeWriter.writeAttribute( AskQueryType.ID_ATTR, q.getId() );
		Boolean direct = q.getDirect();
		if( direct != null )
			xeWriter.writeAttribute( AskQueryType.DIRECT_ATTR, direct.toString() );
		q.getOWLObject().accept( renderer );
		xeWriter.writeEndElement();
	}

	public void visit(SuperClassesQuery q) {
		xeWriter.writeStartElement( AskQueryType.SUPERCLASSES.getURI() );
		xeWriter.writeAttribute( AskQueryType.ID_ATTR, q.getId() );
		Boolean direct = q.getDirect();
		if( direct != null )
			xeWriter.writeAttribute( AskQueryType.DIRECT_ATTR, direct.toString() );
		q.getOWLObject().accept( renderer );
		xeWriter.writeEndElement();
	}

	public void visit(SynonymsAskResult r) {
		xeWriter.writeStartElement( ResponsesVocabulary.Elements.RESPONSE.getURI() );
		xeWriter.writeAttribute( ResponsesVocabulary.ID_ATTRIBUTE, r.getId() );
		for( List<? extends OWLObject> l : r.getOWLObjects() ) {
			xeWriter.writeStartElement( ResponsesVocabulary.Elements.SYNONYMS.getURI() );
			for( OWLObject o : l )
				o.accept( renderer );
			xeWriter.writeEndElement();
		}
		xeWriter.writeEndElement();
	}

	public void visit(UnrecognizedQuery q) {
		throw new UnsupportedOperationException();
	}

	public void visit(ErrorResponse r) {
		xeWriter.writeStartElement( ResponsesVocabulary.Elements.RESPONSE.getURI() );
		xeWriter.writeAttribute( ResponsesVocabulary.ID_ATTRIBUTE, r.getId() );
		xeWriter.writeStartElement( ResponsesVocabulary.Elements.ERROR.getURI() );
		xeWriter
				.writeAttribute( ResponsesVocabulary.CODE_ATTRIBUTE, Integer.toString( r.getCode() ) );
		final String msg = r.getContent();
		if( msg.length() > 0 )
			xeWriter.writeTextContent( msg );
		xeWriter.writeEndElement();
		xeWriter.writeEndElement();
	}

	public void writeAttribute(String attr, String value) {
		xeWriter.writeAttribute( attr, value );
	}
}
