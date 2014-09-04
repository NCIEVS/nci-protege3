package com.clarkparsia.explanation.io.manchester.html;

import java.awt.Color;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.util.SimpleURIShortFormProvider;
import org.semanticweb.owl.util.URIShortFormProvider;

import com.clarkparsia.explanation.io.html.utils.ExplanationSorter;
import com.clarkparsia.explanation.io.manchester.ManchesterSyntaxExplanationRenderer;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Evren Sirin
 */
public class HTMLManchesterSyntaxExplanationRenderer extends ManchesterSyntaxExplanationRenderer {
	public enum IrrelevantPartHandling {
		SHOW, LOWLIGHT, HIDE
	}

	private boolean					visibilityStyleSupported	= false;
	private IrrelevantPartHandling	irrelevantParts				= IrrelevantPartHandling.SHOW;
	private Color					lowlightColor				= null;
	private boolean					sortingEnabled				= true;
	private boolean					indentingEnabled			= false;
	private int						indentSize					= 3;
	private String					indentPrefix				= "*";
	private URIShortFormProvider shortFormProvider = new SimpleURIShortFormProvider();	

	public HTMLManchesterSyntaxExplanationRenderer() {
	}

	public void endRendering() {
		writer.println( "</td></tr></table>" );
		writer.print( "</body>" );
		writer.print( "</html>" );

		super.endRendering();
	}

	public IrrelevantPartHandling getIrrelevantParts() {
		return irrelevantParts;
	}

	public Color getLowlightColor() {
		return lowlightColor;
	}

	public boolean isVisibilityStyleSupported() {
		return visibilityStyleSupported;
	}	public String getIndentPrefix() {
		return indentPrefix;
	}

	public int getIndentSize() {
		return indentSize;
	}

	public boolean isIndentingEnabled() {
		return indentingEnabled;
	}

	public boolean isSortingEnabled() {
		return sortingEnabled;
	}

	@Override
	protected void renderSingleExplanation(Set<OWLAxiom> explanation) throws OWLException,
			IOException {
		if( irrelevantParts != IrrelevantPartHandling.SHOW ) {
			HTMLManchesterSyntaxRelevantObjectRenderer r = (HTMLManchesterSyntaxRelevantObjectRenderer) renderer;

			r.setRelevanceFinder( new RelevanceFinder( getCurrentAxiom(), explanation ) );
		}
		
		writer.printSpace();
		writer.printSpace();
		writer.printSpace();

		writer.startBlock();

		if( isSortingEnabled() )
			renderSortedExplanation( explanation );
		else
			renderUnsortedExplanation( explanation );

		writer.endBlock();
		writer.println();
	}

	protected void renderUnsortedExplanation(Set<OWLAxiom> explanation) throws OWLException,
			IOException {
		for( OWLAxiom a : explanation ) {
			a.accept( renderer );
			writer.println();
		}
	}

	protected void renderSortedExplanation(Set<OWLAxiom> explanation) throws OWLException,
			IOException {
		TreeModel aModel = ExplanationSorter.sort( getCurrentAxiom(), explanation );

		renderExplanationTree( (DefaultMutableTreeNode) aModel.getRoot() );
	}

	private void renderExplanationTree(DefaultMutableTreeNode theNode) {
		OWLAxiom aAxiom = (OWLAxiom) theNode.getUserObject();
		
		boolean printNode = (theNode.getParent() != null);
		boolean indentNode = printNode && isIndentingEnabled()
				&& (theNode.getParent().getParent() != null);
		boolean indentChildren = isIndentingEnabled() && printNode;		
		
		if( printNode ) {
			if( indentNode ) {
				writer.print( indentPrefix );
				writer.printSpace();
			}
			aAxiom.accept( renderer );
			writer.println();
		}

		if( theNode.getChildCount() > 0 ) {
			if( indentChildren ) {
				for( int i = 0; i < indentSize; i++ ) {
					writer.printSpace();
				}
				writer.startBlock();
			}

			for( int aIndex = 0; aIndex < theNode.getChildCount(); aIndex++ ) {
				renderExplanationTree( (DefaultMutableTreeNode) theNode.getChildAt( aIndex ) );
			}

			if( indentChildren ) {
				writer.endBlock();
			}
		}
	}

	public void setIrrelevantParts(IrrelevantPartHandling irrelevantParts) {
		this.irrelevantParts = irrelevantParts;
	}

	public void setLowlightColor(Color lowlightColor) {
		this.lowlightColor = lowlightColor;
	}

	public void setVisibilityStyleSupported(boolean visibilityStyleSupported) {
		this.visibilityStyleSupported = visibilityStyleSupported;
	}

	public void setIndentingEnabled(boolean indentingEnabled) {
		this.indentingEnabled = indentingEnabled;
	}

	public void setIndentPrefix(String indentPrefix) {
		this.indentPrefix = indentPrefix;
	}

	public void setIndentSize(int indentSize) {
		this.indentSize = indentSize;
	}

	public void setSortingEnabled(boolean sortingEnabled) {
		this.sortingEnabled = sortingEnabled;
	}

	public void startRendering(Writer w) {
		HTMLBlockWriter htmlWriter = new HTMLBlockWriter( w );
		htmlWriter.setVisibilityStyleSupported( visibilityStyleSupported );

		writer = htmlWriter;
		
		switch( irrelevantParts ) {
		case SHOW:
			renderer = new HTMLManchesterSyntaxObjectRenderer( htmlWriter, shortFormProvider );
			break;
			
		case LOWLIGHT:
			renderer = new HTMLManchesterSyntaxRelevantObjectRenderer( htmlWriter, shortFormProvider );
			if( lowlightColor != null )
				((HTMLManchesterSyntaxRelevantObjectRenderer) renderer).setLowlightColor( lowlightColor );
			break;
			
		case HIDE:
			renderer = new HTMLManchesterSyntaxHidingObjectRenderer( htmlWriter, shortFormProvider );
			break;

		default:
			throw new RuntimeException();
		}
		
		renderer.setWrapLines( isWrapLines() );
		renderer.setSmartIndent( isSmartIndent() );

		writer.print( "<html>" );
		writer.print( "<body>" );
		writer.print( "<table><tr><td nowrap>" );
	}

	public URIShortFormProvider getShortFormProvider() {
    	return shortFormProvider;
    }

	public void setShortFormProvider(URIShortFormProvider shortFormProvider) {
    	this.shortFormProvider = shortFormProvider;
    }
}
