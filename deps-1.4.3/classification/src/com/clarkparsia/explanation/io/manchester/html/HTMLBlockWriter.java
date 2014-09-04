package com.clarkparsia.explanation.io.manchester.html;

import java.io.Writer;
import java.util.ArrayList;

import com.clarkparsia.explanation.io.manchester.BlockWriter;
import com.clarkparsia.explanation.io.manchester.TextBlockWriter;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description: Concrete implementation of {@link BlockWriter} for HTML output.
 * Due to differences in font sizes the strategy of printing spaces as in
 * {@link TextBlockWriter} do not work for HTML output.
 * </p>
 * <p>
 * This implementation uses a rather crude strategy of tracking the actual HTML
 * printed in one line and prints the exact same HTML but in an invisible. There
 * are two different strategies for printing invisible output. The first is
 * surrounding the output with a span element whose CSS attribute visibility is
 * set to hidden. This will hide everything inside that span but will still take
 * same exact amount of space. Unfortunately, this CSS attribute is not
 * supported in every HTML viewer including the default Java widgets so there is
 * a even more hacky solution to surround output with a font tag where the color
 * is set to the background color. The font color of inner elements are
 * overridden by a simple string substitution operation. Note that, one
 * drawback of this option is that selecting part of the explanation might 
 * highlight the invisible parts. The solution for this is to use the
 * <code>JTextPane.setHighlighter(null)</code> call. 
 * </p>
 * </p>
 * <b>IMPORTANT:</b> This class is not intended for general HTML output and
 * would fail miserably under certain conditions. Using tables and images in the
 * output are some examples. Unfortunately, any alternative strategy for
 * alignment (e.g. using nested HTML tables) has even more disadvantages. Some
 * of the hackiness in this implementation can be overcome by using the
 * {@link HTMLDocument} class and generating HTML through DOM elements.
 * <p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Evren Sirin
 */
public class HTMLBlockWriter extends BlockWriter {
	/**
	 * The strings 
	 */
	private ArrayList<String>	blockPrefixes				= new ArrayList<String>();
	private StringBuffer		currentLine					= null;
	private boolean				visibilityStyleSupported	= true;
	private String				backgroundColor				= "white";
	private boolean				offTheRecord				= false;

	/**
	 * @param out
	 */
	public HTMLBlockWriter(Writer out) {
		super( out, " " );
	}

	protected void startNewLine() {
		if( newLine ) {
			newLine = false;

			if( !blockPrefixes.isEmpty() ) {
				currentLine = new StringBuffer( blockPrefixes.get( blockPrefixes.size() - 1 ) );
				offTheRecord = true;
				if( isVisibilityStyleSupported() ) {
					super.print( "<span visibility='hidden'>" );
					super.print( currentLine );
					super.print( "</span>" );

				}
				else {
					super.print( "<font color='" );
					super.print( getBackgroundColor() );
					super.print( "'>" );
					super.print( currentLine );
					super.print( "</font>" );
				}
				offTheRecord = false;
			}
			else {
				currentLine = new StringBuffer();
			}
		}
	}

	public void clearBlocks() {
		blockPrefixes.clear();
	}

	public void startBlock() {
		String line = currentLine.toString();

		if( !isVisibilityStyleSupported() ) {
			line = line.replaceAll( "color=", "hidecolor=" );
			line = line.replaceAll( "href=", "hidehref=" );
		}

		blockPrefixes.add( line );
	}

	public void endBlock() {
		blockPrefixes.remove( blockPrefixes.size() - 1 );
	}

	public void println() {
		if( offTheRecord ) {
			super.println();
			return;
		}

		offTheRecord = true;
		println( "<br>" );
		offTheRecord = false;

		newLine = true;
	}

	@Override
	public void write(char[] buf, int off, int len) {
		super.write( buf, off, len );

		if( !offTheRecord )
			currentLine.append( buf, off, len );
	}

	@Override
	public void write(int c) {
		super.write( c );

		if( !offTheRecord )
			currentLine.append( c );
	}

	@Override
	public void write(String s, int off, int len) {
		super.write( s, off, len );

		if( !offTheRecord )
			currentLine.append( s, off, len );
	}
	
	@Override
	public void printSpace() {
		super.print( "&nbsp;" );
	}

	public boolean isVisibilityStyleSupported() {
		return visibilityStyleSupported;
	}

	public void setVisibilityStyleSupported(boolean visibilityStyleSupported) {
		this.visibilityStyleSupported = visibilityStyleSupported;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
}
