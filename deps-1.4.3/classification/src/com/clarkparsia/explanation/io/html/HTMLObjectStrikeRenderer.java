package com.clarkparsia.explanation.io.html;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.clarkparsia.explanation.io.html.utils.OWLAxiomSideVisitor;

/**
 * Title: HTMLObjectStrikeRenderer<br>
 * Description: A implementation of an algorithm to strike out the irrelevant parts of an axiom explanation using
 * a simple method of counting the concepts used in the explanation set.<br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Mar 4, 2008 10:50:02 AM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public abstract class HTMLObjectStrikeRenderer extends BaseHTMLObjectRenderer {

    /**
     * The collections of concepts on the RHS and LHS of the axioms in the explanation set.
     */
    private List<OWLObject> mRHS;
    private List<OWLObject> mLHS;

    /**
     * Whether or not strike formatting should be used
     */
    private boolean mStrikeEnabled = true;
    
    /**
     * Whether or not irrelevant parts should be grayed out
     */
    private boolean mLowlightEnabled = true;

    /**
     * Create a new HTMLObjectStrikeRenderer
     */
    public HTMLObjectStrikeRenderer() {
        mRHS = new ArrayList<OWLObject>();
        mLHS = new ArrayList<OWLObject>();
    }

    /**
     * Set whether or not strike formatting is enabled
     * @param theEnable true to enable, false to disable
     */
    public void setStrikeEnabled(boolean theEnable) {
        mStrikeEnabled = theEnable;
    }

    /**
     * Return whether or not lowlighting is enabled
     * @return true if its enabled, false otherwise
     */
    public boolean isLowlightEnabled() {
        return mLowlightEnabled;
    }

    /**
     * Set whether or not lowlighting is enabled
     * @param theEnable true to enable, false to disable
     */
    public void setLowlightEnabled(boolean theEnable) {
    	mLowlightEnabled = theEnable;
    }

    /**
     * Return whether or not strike formatting is enabled
     * @return true if its enabled, false otherwise
     */
    public boolean isStrikeEnabled() {
        return mStrikeEnabled;
    }

    /**
     * Update the state of the renderer, notifying it that its going to render a new axiom and provide it the list
     * of concepts used on the LHS and RHS of the axioms in the explanation
     * @param theExplained the axiom which is about to be explained
     * @param theExplanationSet the set of axioms which make up the explanation
     */
    public void update(OWLAxiom theExplained, Set<OWLAxiom> theExplanationSet) {
        super.update(theExplained, theExplanationSet);

        mRHS = new ArrayList<OWLObject>();
        mLHS = new ArrayList<OWLObject>();

        OWLAxiomSideVisitor aVisitor = new OWLAxiomSideVisitor();

        for (OWLAxiom aAxiom : getExplanationSet()) {
            aAxiom.accept(aVisitor);
        }

        mLHS.addAll(aVisitor.getLHS());
        mRHS.addAll(aVisitor.getRHS());

        // count the axiom we are explaining too
        aVisitor.reset();

        getAxiomBeingExplained().accept(aVisitor);

        mLHS.addAll(aVisitor.getLHS());
        mRHS.addAll(aVisitor.getRHS());
    }

    /**
     * Return whether or not the parameter is part of the axiom we are explaining
     * @param theObj the OWLObject to look for in the axiom being explained
     * @return true if it is in the axiom being explained, false otherwise
     */
    private boolean usedInAxiomBeingExplained(OWLObject theObj) {
        OWLAxiomSideVisitor aVisitor = new OWLAxiomSideVisitor();
        getAxiomBeingExplained().accept(aVisitor);

        List<OWLObject> aConcepts = new ArrayList<OWLObject>(aVisitor.getLHS());
        aConcepts.addAll(aVisitor.getRHS());

        return aConcepts.contains(theObj);
    }

    /**
     * Count the number of times the parameter concept(s) are used in the explanation.
     * @param theObj the concept to search for
     * @return the number of times it's been used.
     */
    private int count(OWLObject theObj) {
        int aCount = 0;

        OWLAxiomSideVisitor aVisitor = new OWLAxiomSideVisitor();
        theObj.accept(aVisitor);

        List<OWLObject> aConcepts = new ArrayList<OWLObject>();
        aConcepts.addAll(mLHS);
        aConcepts.addAll(mRHS);

        for (OWLObject aObj : aConcepts) {
            if (aVisitor.getLHS().contains(aObj) ||
                aVisitor.getRHS().contains(aObj)) {
                aCount++;
            }
        }

        return aCount;
    }

    /**
     * Apply the strike formatting to this node, if applicable
     * @param theElem the HTML node to format
     * @param theObj the object represented by the Node
     * @return the formatted node
     */
    @Override
    protected Element applyFormatting(Node theElem, OWLObject theObj) {
        boolean aUsingStrike = true;

        if (mLowlightEnabled && theObj != null) {

            OWLAxiomSideVisitor aVisitor = new OWLAxiomSideVisitor();
            theObj.accept(aVisitor);

            HashSet<OWLObject> aConcepts = new HashSet<OWLObject>();
            aConcepts.addAll(aVisitor.getLHS());
            aConcepts.addAll(aVisitor.getRHS());

            // don't strike out the axiom being explained, and don't strike out the only axiom in the explanation
            if ((getAxiomBeingExplained() != null && theObj.equals(getAxiomBeingExplained())) ||
                getExplanationSet() == null ||
                (getExplanationSet().size() == 1 && theObj.equals(getExplanationSet().iterator().next()))) {
                aUsingStrike = false;
            }
            else {
                // for the axiom, go over the concepts used in the axiom, if all of them are referred to only once,
                // ie only in *this* axiom, its probably not important to the overall explanation, so we can strike
                // it out.
                for (OWLObject aObj : aConcepts) {
                    if (count(aObj) > 1 || usedInAxiomBeingExplained(aObj)) {
                        aUsingStrike = false;
                        break;
                    }
                }
            }

            // this heuristic is apparently wrong, so lets not do it anymore =)
//            if (theObj instanceof OWLDataAllRestriction || theObj instanceof OWLObjectAllRestriction) {
//                OWLQuantifiedRestriction aRest = (OWLQuantifiedRestriction) theObj;
//
//                // if this is an all restriction, and the property is used multiple times, but the filler only once,
//                // we can strike out this axiom
//                if (count(aRest.getFiller()) <= 1 && count(aRest.getProperty()) > 1) {
//                    aUsingStrike = true;
//                }
//            }
        }
        else {
            aUsingStrike = false;
        }

        Node aNode = theElem;

        if (aUsingStrike) {            
            // this grey font should override the color on any font tags contained by this, so lets strip the color
            // attributes from child nodes
            setColor(theElem, "gray");
        }

        return super.applyFormatting(aNode, theObj);
    }

    /**
     * Strip the color declarations off all the child font elements
     * @param theNode the parent node which we want to strip it and its children of font.color attributes.
     */
    private void setColor(Node theNode, String theColor) {
        if (theNode.getNodeName().equals("font") && theNode.getAttributes().getNamedItem("color") != null) {
        	((Element)theNode).setAttribute( "color", theColor );
        	
        }
        else if(theNode.getNodeType() == Node.TEXT_NODE) {
        	Node aParent = theNode.getParentNode();
            Element aFontElem = theNode.getOwnerDocument().createElement("font");

            aFontElem.setAttribute("color", "gray");
            aParent.replaceChild(aFontElem, theNode);
            aFontElem.appendChild(theNode);
        }
        else {
	        NodeList aList = theNode.getChildNodes();
	        for (int i = 0; i < aList.getLength(); i++) {
	            setColor(aList.item(i), theColor);
	        }
        }
    }
}
