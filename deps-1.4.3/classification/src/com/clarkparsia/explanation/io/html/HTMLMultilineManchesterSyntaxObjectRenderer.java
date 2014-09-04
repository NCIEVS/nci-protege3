package com.clarkparsia.explanation.io.html;

import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLNaryBooleanDescription;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLPropertyExpression;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.w3c.dom.Element;

import com.clarkparsia.explanation.io.html.utils.DescriptionSorter;
import com.clarkparsia.explanation.io.html.utils.DomUtils;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Apr 26, 2007 8:07:23 AM
 *
 * @author Michael Grove <mhgrove@hotmail.com>
 */
public class HTMLMultilineManchesterSyntaxObjectRenderer extends HTMLManchesterSyntaxObjectRenderer {

    public String render( OWLObject object ) {
        reset();

        object.accept( this );

        return getRendering();
    }
        
    @Override
    protected void visitNaryAxiom(Set<? extends OWLObject> set, Keyword binary, Keyword nary) {
        Element aTable = DomUtils.createTable(mElement);

        Element aRow = DomUtils.createRow(aTable);

        Element aCell = DomUtils.createCell(aRow);        

        Element aOldElem = mElement;

        mElement = aCell;
        
    	Set<? extends OWLObject> sortedSet = DescriptionSorter.toSortedSet( set );
		Iterator<? extends OWLObject> aIter = sortedSet.iterator();

    	if (set.size() == 2) {
    		aIter.next().accept( this );
    		mElement = DomUtils.createCell(aRow);
    		insertSpace();
    		append(createKeywordElement(binary));
    		insertSpace();
    		mElement = DomUtils.createCell(aRow);
    		aIter.next().accept( this );
    	}
    	else {
    		append(createKeywordElement(nary));
    		insertSpace();
    		append("(");
    		
    		Element aCurrentRow = aRow;
    		
    		// put the first one on the same line as we started on
            mElement = DomUtils.createCell(aCurrentRow);
    		aIter.next().accept( this );
    		
    		while (aIter.hasNext()) {
                // each subsequent one gets its own line
                aCurrentRow = DomUtils.createRow(aTable);

                // this is a spacer so each part of the eq axiom lines up to the right of the keyword string
                DomUtils.createCell(aCurrentRow);
                
                // start the cell to put this part of the axiom in
                mElement = DomUtils.createCell(aCurrentRow);
                
    			aIter.next().accept( this ); 
    		}
    		
            // append the closing paren for this axiom
    		mElement = DomUtils.createCell(aCurrentRow);
    		mElement.setAttribute("valign", "bottom");
            append(" )");
    	}
    	
    	mElement = aOldElem;
    }

    protected void visitBooleanDescription(OWLNaryBooleanDescription theDescription, Keyword theKeyword) {
        Element aTable = DomUtils.createTable(mElement);

        Element aRow = DomUtils.createRow(aTable);

        Element aCell = DomUtils.createCell(aRow, 2);

        Element aOldElem = mElement;

        mElement = aCell;

        Iterator<OWLDescription> aIter = DescriptionSorter.toSortedSet(theDescription.getOperands()).iterator();
        aIter.next().accept(this);
        
        while (aIter.hasNext()) {
            aRow = DomUtils.createRow(aTable);

            mElement = DomUtils.createCell(aRow);            
            append(createKeywordElement(theKeyword));
            insertSpace();
            
            mElement = DomUtils.createCell(aRow);
            aIter.next().accept(this);
        }

        mElement = aOldElem;
    }

	@Override
	public void visit(OWLSubClassAxiom theAxiom) {
        Element aTable = DomUtils.createTable(mElement);

        Element aRow = DomUtils.createRow(aTable);

        Element aCell = DomUtils.createCell(aRow);        

        Element aOldElem = mElement;

        mElement = aCell;

		theAxiom.getSubClass().accept( this );
		mElement = DomUtils.createCell(aRow);
		insertSpace();
		append(createKeywordElement(Keyword.SUB_CLASS_OF));
		insertSpace();
		mElement = DomUtils.createCell(aRow);
		theAxiom.getSuperClass().accept( this );
		
		mElement = aOldElem;
	}

	@Override
	protected void visitRestriction(OWLPropertyExpression<?, ?> theProperty, Keyword theKeyword,
			Object... theArgs) {
        Element aTable = DomUtils.createTable(mElement);

        Element aRow = DomUtils.createRow(aTable);

        Element aOldElem = mElement;

        mElement = DomUtils.createCell(aRow);

//		append("(");
        theProperty.accept(this);
        
        mElement = DomUtils.createCell(aRow);
        insertSpace();
        append(createKeywordElement(theKeyword));
        insertSpace();
        
        for (Object aObject : theArgs) {
            mElement = DomUtils.createCell(aRow);

			if (aObject instanceof OWLObject) {
				((OWLObject) aObject).accept(this);
			}
			else {
				append(aObject.toString());
			}
		}

//        append(")");
		
		mElement = aOldElem;
	}
}
