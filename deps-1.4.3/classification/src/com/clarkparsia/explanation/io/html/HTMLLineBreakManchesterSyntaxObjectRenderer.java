package com.clarkparsia.explanation.io.html;

import java.util.Iterator;

import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLNaryBooleanDescription;
import org.semanticweb.owl.model.OWLObject;
import org.w3c.dom.Element;

import com.clarkparsia.explanation.io.html.utils.DescriptionSorter;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Dec 6, 2007 2:03:48 PM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class HTMLLineBreakManchesterSyntaxObjectRenderer extends HTMLManchesterSyntaxObjectRenderer {

    public String render( OWLObject object ) {
        reset();
        object.accept( this );
        newLine();
        return getRendering();
    }

    @Override
    protected void visitBooleanDescription(OWLNaryBooleanDescription theDescription, Keyword theKeyword) {
    	Element aParent = preVisit();
    	
        append("(");

        Iterator<OWLDescription> aIter = DescriptionSorter.toSortedSet(theDescription.getOperands()).iterator();
        aIter.next().accept( this );
        
        while (aIter.hasNext()) {
        	newLine();
            append(createKeywordElement(theKeyword));
            insertSpace();
            
            aIter.next().accept( this );
        }

        append(")");
        
        postVisit(aParent, theDescription);
    }

	private void newLine() {
        append(mElement.getOwnerDocument().createElement("br"));
    }
}
