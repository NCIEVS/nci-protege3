package com.clarkparsia.explanation.io.manchester.html;

import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.util.URIShortFormProvider;

import com.clarkparsia.explanation.io.manchester.DescriptionSorter;
import com.clarkparsia.explanation.io.manchester.Keyword;

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
public class HTMLManchesterSyntaxHidingObjectRenderer extends HTMLManchesterSyntaxRelevantObjectRenderer {
	/**
	 * @param writer
	 */
	public HTMLManchesterSyntaxHidingObjectRenderer(HTMLBlockWriter writer, URIShortFormProvider shortFormProvider) {
		super( writer, shortFormProvider );
	}

	@Override
	protected void write(OWLObject object) {
		if( isRelevant( object ) )
			super.write( object );
		else
			super.write( "..." );
	}

	@Override
	protected void writeNaryKeyword(Keyword theKeyword, Set<? extends OWLObject> theObjects) {
		boolean hasIrrelevantParts = false;

		theObjects = DescriptionSorter.toSortedSet( theObjects );
		
		Iterator<? extends OWLObject> aIter = theObjects.iterator();

		if( isSmartIndent() )
			writer.startBlock();

		boolean first = true;
		
		while( aIter.hasNext() ) {
			OWLObject aObject = aIter.next();

			if( !isRelevant( aObject ) ) {
				hasIrrelevantParts = true;
			}
			else {
				if( !first ) {					
					if( isWrapLines() )
						writeNewLine();
					else
						writeSpace();
										
					if( theKeyword != null ) {
						write( theKeyword );
						writeSpace();
					}
				}
				else {
					first = false;
				}
	
				write( aObject );
			}
		}

		if( hasIrrelevantParts ) {
			if( isWrapLines() )
				writeNewLine();
			else
				writeSpace();
			
			write( theKeyword );
			writeSpace();
			write( "..." );
		}
		
		if( isSmartIndent() )
			writer.endBlock();
	}
}
