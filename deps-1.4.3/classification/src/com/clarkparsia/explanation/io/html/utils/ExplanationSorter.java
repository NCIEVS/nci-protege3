package com.clarkparsia.explanation.io.html.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;

import org.semanticweb.owl.model.OWLAxiom;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Mar 4, 2008 10:49:46 AM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class ExplanationSorter {
	private static final Logger	log	= Logger.getLogger( ExplanationSorter.class.getName() );
	
	/**
	 * <p>
	 * Different types of matches one can have between two axioms in the
	 * explanation. THe naming convention for these types indicate which part of
	 * the first (current) axiom matches which part of the second axioms. For
	 * example, RHS_LHS means the right-hand side of first axiom has common
	 * elements with the left-hand side of second axiom.
	 * </p>
	 * <p>
	 * The ordering of types in the enumeration is based on the preference
	 * relation, i.e. the first type is more preferred than the second one.
	 * Reordering types will generate different sorting results.
	 * </p>
	 */
	private static enum AxiomMatch { 
		RHS_LHS, LHS_RHS, LHS_LHS, RHS_RHS, NONE 
	}
	
	/**
	 * We indent a subsequent axiom only if the degree of a match is above
	 * certain threshold.
	 */
	private static AxiomMatch INDENT_THRESHOLD = AxiomMatch.RHS_LHS;

    /**
	 * Sort a list of axioms which are the explanation for a specific axiom by
	 * pairing up axioms which use common concepts. We start with the given root
	 * axiom and find the most preferable match as defined by the AxiomMatch
	 * type. Axioms are returned in a tree format to easier understanding;
	 * certain matches will have a parent/child relationship indicating a strong
	 * relationship between two axioms.
	 * 
	 * @param theRoot
	 *            the axiom being explained
	 * @param theAxioms
	 *            the explanation set
	 * @return the explanation set sorted as a tree structure
	 */
    public static TreeModel sort(OWLAxiom theRoot, Collection<OWLAxiom> theAxioms) {
        // make a copy so we can modify the list
        Collection<OWLAxiom> aAxiomsCopy = new ArrayList<OWLAxiom>(theAxioms);

        // current (tree) node
        MutableTreeNode aNode = new DefaultMutableTreeNode(theRoot);

        // current node data
        OWLAxiom aNodeAxiom = theRoot;

        // our sorted list of explanation axioms
        TreeModel aModel = new DefaultTreeModel(aNode);

        // visitor to get the list of things in the left and right hand side of the current axiom
        OWLAxiomSideVisitor aNodeVisitor = new OWLAxiomSideVisitor();

        // visitor to get the LHS/RHS for the current comparison axiom
        OWLAxiomSideVisitor aCurrAxiomVisitor = new OWLAxiomSideVisitor();

        do {
        	// find (at most) one candidate for each match type
        	EnumMap<AxiomMatch, OWLAxiom> aSelectedAxioms = new EnumMap<AxiomMatch, OWLAxiom>(AxiomMatch.class);

            // get LHS/RHS side for the current axiom
            aNodeVisitor.reset();
            aNodeAxiom.accept(aNodeVisitor);
            
            for( OWLAxiom aAxiom : aAxiomsCopy ) {
                // get the LHS/RHS for the current comparison axiom
                aCurrAxiomVisitor.reset();
                aAxiom.accept(aCurrAxiomVisitor);
                
                // compute the match type
                AxiomMatch aMatch = AxiomMatch.NONE;
                if (!Collections.disjoint(aNodeVisitor.getRHS(), aCurrAxiomVisitor.getLHS())) {
                	aMatch = AxiomMatch.RHS_LHS;	
                }
                else if (!Collections.disjoint(aNodeVisitor.getLHS(), aCurrAxiomVisitor.getRHS())) {
                	aMatch = AxiomMatch.LHS_RHS;	
                }
                else if (!Collections.disjoint(aNodeVisitor.getLHS(), aCurrAxiomVisitor.getLHS())) {
                	aMatch = AxiomMatch.LHS_LHS;	
                }
                else if (!Collections.disjoint(aNodeVisitor.getRHS(), aCurrAxiomVisitor.getRHS())) {
                	aMatch = AxiomMatch.RHS_RHS;	
                }
                
                // if we don't have a match for this type store it
                if (!aSelectedAxioms.containsKey(aMatch))
                	aSelectedAxioms.put( aMatch, aAxiom );
			}
            
            // an EnumMap is guaranteed to return the entries in their ordinal
			// order so the first entry will give the most preferred match type
            Map.Entry<AxiomMatch,OWLAxiom> aSelectedEntry = aSelectedAxioms.entrySet().iterator().next();
            
            // get the axiom and match type
            OWLAxiom aSelectedAxiom = aSelectedEntry.getValue();
            AxiomMatch aSelectedMatch = aSelectedEntry.getKey();            
            
            // decide if we want to indent
            boolean indent = aSelectedMatch.compareTo( INDENT_THRESHOLD ) <= 0;
            boolean hasParent = aNode.getParent() != null; 
            MutableTreeNode parent = hasParent && !indent ?
            	((MutableTreeNode)aNode.getParent()) : aNode;

            MutableTreeNode aNewNode = new DefaultMutableTreeNode(aSelectedAxiom);

            parent.insert(aNewNode, parent.getChildCount());

            aNode = aNewNode;
            aNodeAxiom = aSelectedAxiom;
            
            // this axiom is processed so remove it
            aAxiomsCopy.remove(aSelectedAxiom);            
        }
        while (!aAxiomsCopy.isEmpty());

        // for debugging, print the sorted list
        if( log.isLoggable( Level.FINE ) )
        	logTree((DefaultMutableTreeNode)aModel.getRoot(), 0);

        return aModel;
    }

    private static void logTree(DefaultMutableTreeNode theNode, int theIndent) {
        String aIndentStr = "";
        for (int i = 0; i < theIndent; i++) aIndentStr += "\t";

        log.fine(aIndentStr + theNode.getUserObject());

        for (int aIndex = 0; aIndex < theNode.getChildCount(); aIndex++) {
        	logTree((DefaultMutableTreeNode)theNode.getChildAt(aIndex), theIndent+1);
        }
    }
}