package com.clarkparsia.dig20.xml.asks;

import org.coode.owl.owlxmlparser.OWLXMLParserException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.clarkparsia.dig20.xml.DefaultOWLXMLParentElementHandler;
import com.clarkparsia.dig20.xml.SAXHandlerParent;

/**
 * <p>
 * Title: AbstractBaseAskQuery
 * </p>
 * <p>
 * Description: Implementation of shared functionality for ask queries that
 * require OWLAPI for parsing OWL/XML child elements
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
public abstract class AbstractBaseAskQuery<O> extends DefaultOWLXMLParentElementHandler<O>
		implements AsksQuerySAXHandler {

	protected String								id;
	protected SAXHandlerParent<AsksQuerySAXHandler>	parent;
	protected AskQueryType							type;

	public AbstractBaseAskQuery(SAXHandlerParent<AsksQuerySAXHandler> parent, AskQueryType t,
			Attributes attr) {
		this.parent = parent;
		this.type = t;
		this.id = attr.getValue( AskQueryType.ID_ATTR );

		if( this.id == null )
			throw new RuntimeException( new SAXParseException(
					"No query identifier attribute found.", locator ) );
	}

	@Override
	protected void notifyParent() throws SAXException {
		parent.handleChild( this );
	}

	public O getOWLObject() throws OWLXMLParserException {
		throw new UnsupportedOperationException();
	}
}
