package com.clarkparsia.explanation.io.manchester.html;

import java.net.URI;

import org.semanticweb.owl.util.URIShortFormProvider;

import com.clarkparsia.explanation.io.manchester.Keyword;
import com.clarkparsia.explanation.io.manchester.ManchesterSyntaxObjectRenderer;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 *
 * @author Evren Sirin
 */
public class HTMLManchesterSyntaxObjectRenderer extends ManchesterSyntaxObjectRenderer {
	private URIShortFormProvider shortFormProvider = null;
	
	/**
	 * @param writer
	 */
	public HTMLManchesterSyntaxObjectRenderer(HTMLBlockWriter writer, URIShortFormProvider shortFormProvider) {
    	super( writer );
    	
    	this.shortFormProvider = shortFormProvider;
    }

	/**
	 * {@inheritDoc}
	 */
    protected void write(Keyword keyword) {
    	write( "<font" );
    	write( " face=\"" + keyword.getFace() );
    	write( "\" color=\"" + keyword.getColor() );
    	write( "\" size=\"" + keyword.getSize() );
    	write( "\">" );
		write( keyword.getLabel() );
		write( "</font>" );
	}  

	/**
	 * {@inheritDoc}
	 */
    protected void write(URI uri) {
    	write( "<a" );
    	write( " href='" + uri + "'");
    	write( ">" );
    	write( shortForm( uri ) );
    	write( "</a>" );
    }    

	/**
	 * {@inheritDoc}
	 */
    protected void writeSpace() {
    	write( "&nbsp;" );
    }

	@Override
    protected String shortForm(URI theURI) {
		return shortFormProvider.getShortForm( theURI );
    }
}
