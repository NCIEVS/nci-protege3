package com.clarkparsia.explanation.io.html;

import javax.xml.parsers.DocumentBuilderFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Set;
import java.util.HashSet;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.clarkparsia.explanation.io.ExplanationRenderer;
import com.clarkparsia.explanation.io.html.utils.DomUtils;


/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Apr 17, 2007 3:12:56 PM
 *
 * @author Michael Grove <mhgrove@hotmail.com>
 */
public class HTMLExplanationRenderer implements ExplanationRenderer {

    /**
     * Write the rendered output to here
     */
    private PrintWriter mWriter;

    /**
     * The current axiom being explained.
     */
    private OWLAxiom mExplainedAxiom;

    /**
     * The renderer being used to render individual axioms in the explanation
     */
    private HTMLObjectRenderer mRenderer;

    /**
     * The explanation set for the axiom being explained
     */
    private Set<Set<OWLAxiom>> mExplanations;

    /**
     * What's the DOM man?
     */
    private Document mDOM;

    /**
     * The current DOM element
     */
    protected Element mElement;

    /**
     * Create a new HTML explanation renderer with the given object renderer for rendering the individual axioms in
     * the explanations
     * @param theRenderer the renderer to use
     */
    public HTMLExplanationRenderer(HTMLObjectRenderer theRenderer) {
        mRenderer = theRenderer;
    }

    /**
     * @inheritDoc
     */
    public void startRendering(Writer theWriter) {
        mWriter = theWriter instanceof PrintWriter
            ? (PrintWriter) theWriter
            : new PrintWriter( theWriter );
    }

    /**
     * @inheritDoc
     */
    public void endRendering() {
        mWriter.flush();
        mWriter.close();
    }

    /**
     * @inheritDoc
     */
    public void render(OWLAxiom theAxiom, Set<Set<OWLAxiom>> theExplanations) throws OWLException, IOException {
        mExplainedAxiom = theAxiom;
        mExplanations = theExplanations;

        getRenderer().update(getExplainedAxiom(), new HashSet<OWLAxiom>());

        try {
            mDOM = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        Element aHTML = mDOM.createElement("html");
        Element aBody = mDOM.createElement("body");

        mDOM.appendChild(aHTML);
        aHTML.appendChild(aBody);
        Element aTable = DomUtils.createTable(aBody);

        Element aRow, aCell;
        
        aRow = DomUtils.createRow(aTable);
        aCell = DomUtils.createCell(aRow, 2);
        aCell.appendChild(mDOM.createTextNode("Axiom: "));

        aRow = DomUtils.createRow(aTable);
        aCell = DomUtils.createCell(aRow);
        aCell = DomUtils.createCell(aRow);
 	    mElement = aCell;
        mRenderer.render(theAxiom);
        mElement.appendChild(mDOM.adoptNode(mRenderer.getRenderedElement()));

		int expSize = theExplanations.size();

		String header = (expSize > 1)
			? "Explanations (" + expSize + "):"
			: "Explanation:";

	    aRow = DomUtils.createRow(aTable);
	    aCell = DomUtils.createCell(aRow, 2);
	    aCell.appendChild(mDOM.createTextNode(header));
//        aCell.appendChild(mDOM.createElement("br"));

        mElement = aTable;
        
		if( expSize == 0 ) {
		    aRow = DomUtils.createRow(aTable);
		    aCell = DomUtils.createCell(aRow, 2);
            aCell.appendChild(mDOM.createTextNode("AXIOM IS NOT ENTAILED!"));
	        aCell.appendChild(mDOM.createElement("br"));
		}
		else if( expSize == 1 ) {
            Set<OWLAxiom> explanation = theExplanations.iterator().next();
			renderSingleExplanation( "", explanation );
		}
		else {
			renderMultipleExplanations( theExplanations );
		}

        System.out.println(DomUtils.renderElement(aHTML));
        mWriter.println(DomUtils.renderElement(aHTML));
    }

	protected void renderMultipleExplanations(Set<Set<OWLAxiom>> explanations) throws OWLException, IOException {
        Element aParent = mElement;

        int count = 1;
		for( Set<OWLAxiom> exp : explanations ) {
			mElement = aParent;
			renderSingleExplanation((count++) + ")&nbsp;", exp);
			
			if (count>1 && count<explanations.size()) {
				Element aRow = DomUtils.createRow(aParent);
                Element aCell = DomUtils.createCell(aRow, 2);                
                aCell.appendChild(aCell.getOwnerDocument().createTextNode("&nbsp;"));
                aCell.appendChild(aCell.getOwnerDocument().createElement("br"));
                aCell.appendChild(aCell.getOwnerDocument().createElement("hr"));
                aCell.appendChild(aCell.getOwnerDocument().createTextNode("&nbsp;"));
                aCell.appendChild(aCell.getOwnerDocument().createElement("br"));
			}
		}
	}

	protected void renderSingleExplanation(String theHeader, Set<OWLAxiom> theAxioms) throws OWLException, IOException {
        getRenderer().update(getExplainedAxiom(), theAxioms);

        boolean first = true;

        Element aParent = mElement;

        for (OWLAxiom axiom : theAxioms) {
			if( first )
				first = false;
			else
				theHeader = "";

            Element aRow = DomUtils.createRow(aParent);
            Element aCell = DomUtils.createCell(aRow);
            aCell.setAttribute("align", "right");
            aCell.appendChild(aCell.getOwnerDocument().createTextNode(theHeader));

            aCell = DomUtils.createCell(aRow);

            mRenderer.render(axiom);
            aCell.appendChild(mDOM.adoptNode(mRenderer.getRenderedElement()));

		}
	}

    /**
     * Return the renderer being used
     * @return the current object renderer
     */
    public HTMLObjectRenderer getRenderer() {
        return mRenderer;
    }

    /**
     * Return the axiom being explained, and renderered, by this renderer
     * @return the axioms being explained, or null if it has not been initialized yet
     */
    protected OWLAxiom getExplainedAxiom() {
        return mExplainedAxiom;
    }

    /**
     * Return the explanation sets for the axiom being explained, and rendered, by this renderer
     * @return the explanation sets, or null if it has not been initialized yet.
     */
    protected Set<Set<OWLAxiom>> getExplanations() {
        return mExplanations;
    }
}
