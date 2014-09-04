package com.clarkparsia.dig20.xml;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Map;

import org.coode.owlapi.owlxml.renderer.OWLXMLWriter;
import org.coode.xml.XMLWriter;
import org.coode.xml.XMLWriterFactory;
import org.coode.xml.XMLWriterNamespaceManager;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyFactory;
import org.semanticweb.owl.model.OWLOntologyFormat;
import org.semanticweb.owl.model.OWLRuntimeException;
import org.semanticweb.owl.util.NamespaceUtil;
import org.semanticweb.owl.vocab.Namespaces;
import org.semanticweb.owl.vocab.OWLXMLVocabulary;

import uk.ac.manchester.cs.owl.EmptyInMemOWLOntologyFactory;

import com.clarkparsia.dig20.xml.asks.AskQueryType;
import com.clarkparsia.dig20.xml.explanation.ExplanationVocabulary;
import com.clarkparsia.dig20.xml.responses.ResponsesVocabulary;

public class DigWriter extends OWLXMLWriter {

	private XMLWriter					writer;

	private static final OWLOntology	emptyOntology;
	private static final NamespaceUtil  namespaceUtil;

	static {
		OWLOntologyFactory.OWLOntologyCreationHandler creationHandler = new OWLOntologyFactory.OWLOntologyCreationHandler() {
			public void ontologyCreated(OWLOntology ontology) {
			}

			public void setOntologyFormat(OWLOntology ontology, OWLOntologyFormat format) {
			}
		};
		URI ont = URI.create( "http://example.org/XMLExplanationWriter/" );
		try {
			emptyOntology = new EmptyInMemOWLOntologyFactory().createOWLOntology( ont, ont,
					creationHandler );
		} catch( OWLException e ) {
			throw new OWLRuntimeException( e );
		}
		namespaceUtil = new NamespaceUtil();
		namespaceUtil.setPrefix( DigVocabulary.DIG20_NS, "dig20" );
		namespaceUtil.setPrefix( AskQueryType.ASKS_NS, "asks" );
		namespaceUtil.setPrefix( ResponsesVocabulary.RESPONSES_NS, "responses" );
		namespaceUtil.setPrefix( ExplanationVocabulary.EXPLANATIONS_NS, "explanations" );
	}

	public static OWLOntology getEmptyOntology() {
		return emptyOntology;
	}

	public static DigWriter create(Writer writer) {
		XMLWriterNamespaceManager nsmgr = new XMLWriterNamespaceManager( DigVocabulary.DIG20_NS );
		for (Map.Entry<String, String> e : namespaceUtil.getNamespace2PrefixMap().entrySet()) {
			nsmgr.setPrefix( e.getValue(), e.getKey() );
		}

		return new DigWriter( writer, nsmgr );
	}

	public DigWriter(Writer writer, XMLWriterNamespaceManager xmlWriterNamespaceManager)
			throws OWLRuntimeException {
		super( writer, xmlWriterNamespaceManager, getEmptyOntology() );

		this.writer = XMLWriterFactory.getInstance().createXMLWriter( writer,
				xmlWriterNamespaceManager, /* xmlBase */
				"" );
	}

	public void startDocument(String rootElementName) throws IOException {
		writer.startDocument( rootElementName );
	}

	public void endDocument() {
		try { 
			writer.endDocument();
		} catch( IOException e ) {
			throw new OWLRuntimeException( e );
		}
	}

	public void writeStartElement(URI name) {
		try {
			writer.writeStartElement( name.toString() );
		} catch( IOException e ) {
			throw new OWLRuntimeException( e );
		}
	}

	public void writeEndElement() {
		try {
			writer.writeEndElement();
		} catch( IOException e ) {
			throw new OWLRuntimeException( e );
		}
	}

	public void writeDatatypeAttribute(URI datatype) {
		try {
			writer.writeAttribute( OWLXMLVocabulary.DATATYPE.getURI().toString(), datatype
					.toString() );
		} catch( IOException e ) {
			throw new OWLRuntimeException( e );
		}
	}

	public void writeNameAttribute(URI uri) {
		try {
			String value = uri.toString();
			String attName = Namespaces.OWL2XML + "URI";
			if( value.startsWith( writer.getXMLBase() ) ) {
				writer.writeAttribute( attName, value.substring( writer.getXMLBase().length(),
						value.length() ) );
			}
			else {
				writer.writeAttribute( attName, value );
			}
		} catch( IOException e ) {
			throw new OWLRuntimeException( e );
		}
	}

	public void writeCardinalityAttribute(int cardinality) {
		try {
			writer.writeAttribute( Namespaces.OWL2XML + "cardinality", Integer
					.toString( cardinality ) );
		} catch( IOException e ) {
			throw new OWLRuntimeException( e );
		}
	}

	public void writeTextContent(String text) {
		try {
			writer.writeTextContent( text );
		} catch( IOException e ) {
			throw new OWLRuntimeException( e );
		}
	}

	public void writeFacetAttribute(URI facetURI) {
		try {
			writer.writeAttribute( OWLXMLVocabulary.DATATYPE_FACET.getURI().toString(), facetURI
					.toString() );
		} catch( IOException e ) {
			throw new OWLRuntimeException( e );
		}
	}

	public void writeAnnotationURIAttribute(URI uri) {
		try {
			writer.writeAttribute( OWLXMLVocabulary.ANNOTATION_URI.toString(), uri.toString() );
		} catch( IOException e ) {
			throw new OWLRuntimeException( e );
		}
	}

	public void writeAttribute(String attr, String val) {
		try {
			writer.writeAttribute( attr, val );
		} catch( IOException e ) {
			throw new OWLRuntimeException( e );
		}
	}
}