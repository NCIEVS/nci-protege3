package com.clarkparsia.explanation.io.html;

import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultMutableTreeNode;

import java.util.Set;
import java.util.HashSet;
import java.io.IOException;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLException;
import org.w3c.dom.Element;
import com.clarkparsia.explanation.io.html.utils.DomUtils;
import com.clarkparsia.explanation.io.html.utils.ExplanationSorter;

/**
 * Title: SortedHTMLExplanationRenderer<br>
 * Description: An HTML explanation renderer that sorts the axioms in an explanation and displays them with some
 * (optional) indenting for easier reading.<br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Mar 5, 2008 9:49:21 AM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 * @see ExplanationSorter
 */
public class SortedHTMLExplanationRenderer extends HTMLExplanationRenderer {
    /**
     * If you want to show the root (the axiom being explained) in the explanation, set to true, otherwise, if you want
     * it hidden, use false.
     */
    private boolean mShowRoot = false;

    /**
     * The number of spaces we want to indent each subsequent (child) axiom by
     */
    private int mIndentSize = 4;

    /**
     * Whether or not to use the indenting
     */
    private boolean mIsIndenting = true;

    /**
     * Create a new SortedHTMLExplanationRenderer
     * @param theRenderer the object renderer to use
     */
    public SortedHTMLExplanationRenderer(HTMLObjectRenderer theRenderer) {
        super(theRenderer);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void render(OWLAxiom theAxiom, Set<Set<OWLAxiom>> theExplanations) throws OWLException, IOException {
        getRenderer().update(theAxiom, new HashSet<OWLAxiom>());

        super.render(theAxiom, theExplanations);
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void renderSingleExplanation(String theHeader, Set<OWLAxiom> theAxioms) throws OWLException, IOException {
        // overrides the default rendering of a single explanation set by sorting it first, and then renders it in a
        // tree-like manner
        TreeModel aModel = ExplanationSorter.sort(getExplainedAxiom(), theAxioms);

        getRenderer().update(getExplainedAxiom(), theAxioms);

        sortedRender(theHeader, (DefaultMutableTreeNode) aModel.getRoot(), 0);
	}

    /**
     * Return whether or not indenting is enabled
     * @return true if indented rendering is enabled, false otherwise
     */
    public boolean isIndenting() {
        return mIsIndenting;
    }

    /**
     * Set whether or not to indent the axioms when rendering
     * @param theBool true to turn on indenting, false otherwise
     */
    public void setIsIndenting(boolean theBool){
        mIsIndenting = theBool;
    }

    /**
     * Return the number of spaces used to indent axioms
     * @return the indent size
     */
    public int getIndentSize() {
        return mIndentSize;
    }

    /**
     * Set the number of spaces used when indenting
     * @param theSize the new indent size
     */
    public void setIndentSize(int theSize) {
        mIndentSize = theSize;
    }

    /**
     * Whether or not to show the explanation set root, which is the axiom being explained
     * @return true if the root will be rendered, false otherwise
     */
    public boolean isShowRoot() {
        return mShowRoot;
    }

    /**
     * Set whether or not to show the explanation set root, which is the axiom being explained.
     * @param theShow true to show the root during rendering, false otherwise
     */
    public void setShowRoot(boolean theShow) {
        mShowRoot = theShow;
    }

    /**
     * Sorted rendering of the explanation set
     * @param theHeader the header string, someting like 1) to prefix the axiom rendering with
     * @param theNode the node to render, the result of getUserObject on this node is the OWLAxiom we're going to render
     * @param theIndent the current indent size
     */
    private void sortedRender(String theHeader, DefaultMutableTreeNode theNode, int theIndent) {
        String aTab = "";
        for (int i = 0; i < getIndentSize(); i++) aTab += "&nbsp;";

        String aIndentStr = "";
        for (int i = 0; i < theIndent; i++) aIndentStr += aTab;

        OWLAxiom aAxiom = (OWLAxiom) theNode.getUserObject();

        // always show non-root explanations, and only show the root explanation (which is the axiom we
        // are explaining) if mShowRoot is turned on.
        if ((mShowRoot && theNode.getParent() == null) || theNode.getParent() != null) {
            Element aRow = DomUtils.createRow(mElement);
            Element aCell = DomUtils.createCell(aRow);
            aCell.setAttribute("align", "right");
            aCell.appendChild(mElement.getOwnerDocument().createTextNode(theHeader));

            aCell = DomUtils.createCell(aRow);

            if (isIndenting()) {
                aCell.appendChild(aCell.getOwnerDocument().createTextNode(aIndentStr));
                aCell.appendChild(aCell.getOwnerDocument().createTextNode(" * "));
            }

            getRenderer().render(aAxiom);
            aCell.appendChild(aCell.getOwnerDocument().adoptNode(getRenderer().getRenderedElement()));
            theHeader = "";
        }

        for (int aIndex = 0; aIndex < theNode.getChildCount(); aIndex++) {
            sortedRender(theHeader, (DefaultMutableTreeNode)theNode.getChildAt(aIndex), theIndent+1);
            theHeader = "";
        }
    }
}
