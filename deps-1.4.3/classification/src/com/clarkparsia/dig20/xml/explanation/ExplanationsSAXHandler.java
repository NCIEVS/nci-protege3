package com.clarkparsia.dig20.xml.explanation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owl.model.OWLAxiom;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.clarkparsia.dig20.explanation.ExplanationsResponse;
import com.clarkparsia.dig20.xml.AxiomsElementHandler;
import com.clarkparsia.dig20.xml.DigVocabulary;
import com.clarkparsia.dig20.xml.SAXHandlerParent;
import com.clarkparsia.dig20.xml.StackableHandler;
import com.clarkparsia.dig20.xml.responses.ResponsesVocabulary;

public class ExplanationsSAXHandler extends StackableHandler implements
		SAXHandlerParent<AxiomsElementHandler> {

	private List<List<OWLAxiom>>						explanations;
	private String										id;
	private SAXHandlerParent<ExplanationsSAXHandler>	parent;

	public ExplanationsSAXHandler(SAXHandlerParent<ExplanationsSAXHandler> parent, Attributes atts) {
		this.parent = parent;
		this.id = atts.getValue( ResponsesVocabulary.ID_ATTRIBUTE );

		if( this.id == null )
			throw new RuntimeException( new SAXParseException(
					"No query identifier attribute found.", locator ) );

		explanations = new ArrayList<List<OWLAxiom>>();
	}

	public void handleChild(AxiomsElementHandler child) throws SAXException {
		if( this.child != child )
			throw new SAXException( "Child does not match argument" );
		explanations.add( child.getAxioms() );
		this.child = null;
	}

	@Override
	public ContentHandler newChild(String uri, String localName, String name, Attributes atts)
			throws SAXException {
		if( (uri + localName).equals( DigVocabulary.AXIOMS.toString() ) )
			return new AxiomsElementHandler( this, atts, locator );
		else
			throw new SAXParseException( "Unexpected Element " + uri + localName, locator );
	}

	@Override
	public void notifyParent() throws SAXException {
		parent.handleChild( this );
	}

	public ExplanationsResponse getResponse() {
		Set<Set<OWLAxiom>> exps = new HashSet<Set<OWLAxiom>>();
		for( List<OWLAxiom> e : explanations )
			exps.add( new HashSet<OWLAxiom>( e ) );
		return ExplanationsResponse.explanations( id, exps );
	}
}