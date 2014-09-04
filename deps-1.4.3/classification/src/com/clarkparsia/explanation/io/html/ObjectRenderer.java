package com.clarkparsia.explanation.io.html;

import java.util.Set;

import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLAxiom;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Jun 19, 2007 12:05:41 PM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public interface ObjectRenderer {
    /**
     * Render this object
     * @param object the object to render
     * @return the String rendering of the given object
     */
    public String render( OWLObject object );

    /**
     * Reset the state of this renderer
     */
    public void reset();

    /**
     * Update the state of the renderer with the axiom being explained, and its explanation set.
     * @param theAxiom the axiom being explained
     * @param theExplanation the explanation set
     */
    public void update(OWLAxiom theAxiom, Set<OWLAxiom> theExplanation);
}
