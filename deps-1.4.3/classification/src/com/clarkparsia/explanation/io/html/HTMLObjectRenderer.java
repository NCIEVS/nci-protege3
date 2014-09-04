package com.clarkparsia.explanation.io.html;

import java.util.Set;
import java.net.URI;

import org.semanticweb.owl.model.OWLObjectVisitor;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLObject;
import org.w3c.dom.Element;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Mar 6, 2008 11:09:23 AM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public interface HTMLObjectRenderer extends ObjectRenderer, OWLObjectVisitor {

    /**
     * Return the style class used for literal values
     * @return the style class, or null if one is not specified
     */
    public String getLiteralStyleClass();

    /**
     * Set the style class for literal values
     * @param theClass the new style class for literal values, or null to use the default
     */
    public void setLiteralStyleClass(String theClass);

    /**
     * Get the style class used for datatype values
     * @return the style class, or null if one is not specified
     */
    public String getDatatypeStyleClass();

    /**
     * Set the style class for datatype values
     * @param theClass the new style class, or null to use the default
     */
    public void setDatatypeStyleClass(String theClass);

    /**
     * Get the style class used for constant values
     * @return the style class, or null if one is not specified
     */
    public String getConstantStyleClass();

    /**
     * Set the style class for constant values
     * @param theClass the new style class, or null to use the default
     */
    public void setConstantStyleClass(String theClass);

    /**
     * Get the style class used for OWL classes
     * @return the style class, or null if one is not specified
     */
    public String getClassStyleClass();

    /**
     * Set the style class for OWL classes
     * @param theClass the new style class, or null to use the default
     */
    public void setClassStyleClass(String theClass);

    /**
     * Get the style class used for individuals
     * @return the style class, or null if one is not specified
     */
    public String getIndividualStyleClass();

    /**
     * Set the style class used for individuals
     * @param theClass the new style class, or null to use the default
     */
    public void setIndividualStyleClass(String theClass);

    /**
     * Get the style class used for object properties
     * @return the style class, or null if one is not specified
     */
    public String getObjectPropertyStyleClass();

    /**
     * Set the style class used for object properties
     * @param theClass the new style class, or null to use the default
     */
    public void setObjectPropertyStyleClass(String theClass);

    /**
     * Get the style class used for datatype properties
     * @return the style class, or null if one is not specified
     */
    public String getDataPropertyStyleClass();

    /**
     * Set the style class used for datatype properties
     * @param theClass the new style class, or null to use the default
     */
    public void setDataPropertyStyleClass(String theClass);

    /**
     * Return the current rendered value of the current object, i.e. the last thing passed to the render method
     * @return the rendered text
     */
    public String getRendering();

    /**
     * Return the current object rendered as a DOM Element
     * @return the current object rendered as a DOM element
     */
    public Element getRenderedElement();

    /**
     * Return the axiom being explained by the parent renderer
     * @return the axiom being explained, or null if it is not yet set
     * @see HTMLExplanationRenderer
     */
    public OWLAxiom getAxiomBeingExplained();

    /**
     * Return the explanation set for the axiom being explained by the parent renderer
     * @return the explanation set, or null if it is not yet set.
     * @see HTMLExplanationRenderer
     * @see #getAxiomBeingExplained()
     */
    public Set<OWLAxiom> getExplanationSet();
}
