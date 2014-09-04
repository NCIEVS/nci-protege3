package com.clarkparsia.explanation.io.manchester.html;

import java.awt.Color;
import java.net.URI;

import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.util.URIShortFormProvider;

import com.clarkparsia.explanation.io.manchester.Keyword;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Evren Sirin
 */
public class HTMLManchesterSyntaxRelevantObjectRenderer extends HTMLManchesterSyntaxObjectRenderer {
	public static final Color	DEFAULT_LOWLIGHT_COLOR	= new Color( 202, 202, 202 );

	private Color				lowlightColor			= DEFAULT_LOWLIGHT_COLOR;
	private RelevanceFinder		relevanceFinder			= null;
	protected boolean			isRelevant				= true;

	/**
	 * @param writer
	 */
	public HTMLManchesterSyntaxRelevantObjectRenderer(HTMLBlockWriter writer, URIShortFormProvider shortFormProvider) {
		super( writer, shortFormProvider );
	}

	public String getLowlightColorString() {
		return Integer.toHexString( lowlightColor.getRGB() & 0x00ffffff );
	}

	public Color getLowlightColor() {
		return lowlightColor;
	}

	public void setLowlightColor(Color lowlightColor) {
		if( lowlightColor == null )
			throw new NullPointerException( "Color cannot be null" );

		this.lowlightColor = lowlightColor;
	}

	public RelevanceFinder getRelevanceFinder() {
		return relevanceFinder;
	}

	public void setRelevanceFinder(RelevanceFinder relevanceFinder) {
		this.relevanceFinder = relevanceFinder;
	}

	private boolean isLowlighting() {
		return lowlightColor != null && relevanceFinder != null;
	}

	protected boolean isRelevant(OWLObject object) {
		return relevanceFinder == null || relevanceFinder.isRelevant( object );
	}

	@Override
	protected void write(OWLObject object) {
		boolean previousRelevance = isRelevant;

		// if relevance is previously set to false it means this object is part
		// of an irrelevant object and thus should be irrelevant. no need to
		// check the relevance again
		if( previousRelevance )
			isRelevant = isRelevant( object ) || !isLowlighting();

		super.write( object );

		// reset the relevance to previous setting
		isRelevant = previousRelevance;
	}

	@Override
	protected void write(Keyword keyword) {
		write( "<font" );
		write( " face=\"" + keyword.getFace() );
		if( !isRelevant )
			write( "\" color=\"" + getLowlightColorString() );
		else
			write( "\" color=\"" + keyword.getColor() );
		write( "\" size=\"" + keyword.getSize() );
		write( "\">" );
		write( keyword.getLabel() );
		write( "</font>" );
	}

	@Override
	protected void write(URI uri) {
		write( "<a" );
		write( " href='" + uri + "'" );
		write( ">" );
		if( !isRelevant ) {
			write( "<font" );
			write( " color=\"" + getLowlightColorString() );
			write( "\">" );
		}
		write( shortForm( uri ) );
		if( !isRelevant ) {
			write( "</font>" );
		}
		write( "</a>" );
	}
}
