package com.clarkparsia.explanation.io.html;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;

import java.net.URI;

import java.util.Set;

import java.io.Writer;
import java.io.StringWriter;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;

import org.semanticweb.owl.model.OWLTypedConstant;
import org.semanticweb.owl.model.OWLUntypedConstant;
import org.semanticweb.owl.model.SWRLAtom;
import org.semanticweb.owl.model.SWRLAtomConstantObject;
import org.semanticweb.owl.model.SWRLAtomDVariable;
import org.semanticweb.owl.model.SWRLAtomIVariable;
import org.semanticweb.owl.model.SWRLAtomIndividualObject;
import org.semanticweb.owl.model.SWRLAtomObject;
import org.semanticweb.owl.model.SWRLBuiltInAtom;
import org.semanticweb.owl.model.SWRLClassAtom;
import org.semanticweb.owl.model.SWRLDataRangeAtom;
import org.semanticweb.owl.model.SWRLDataValuedPropertyAtom;
import org.semanticweb.owl.model.SWRLDifferentFromAtom;
import org.semanticweb.owl.model.SWRLObjectPropertyAtom;
import org.semanticweb.owl.model.SWRLRule;
import org.semanticweb.owl.model.SWRLSameAsAtom;
import org.semanticweb.owl.model.OWLAxiom;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Apr 23, 2007 9:16:24 AM
 *
 * @author Michael Grove <mhgrove@hotmail.com>
 */
public abstract class BaseHTMLObjectRenderer implements HTMLObjectRenderer {
    /**
     * Style classes for controlling how the rendering looks.
     */
    private String mLiteralStyleClass = null;
    private String mDatatypeStyleClass = null;
    private String mConstantStyleClass = null;
    private String mDataPropertyStyleClass = null;
    private String mObjectPropertyStyleClass = null;
    private String mIndividualStyleClass = null;
    private String mClassStyleClass = null;

    /**
     * The axiom we are explaining which this object will be used to render
     */
    private OWLAxiom mExplainedAxiom;

    /**
     * The list of axioms we're going to render as an explanation with this renderer
     */
    private Set<OWLAxiom> mExplanationSet;

    /**
     * The DOM
     */
    private Document mDOM;

    /**
     * The current DOM element
     */
    protected Element mElement;

    public BaseHTMLObjectRenderer() {
        reset();
    }

    /**
     * Render the current DOM as a string
     * @return the DOM rendered as a string
     */
    private String renderDOM() {
        try {
            Writer aOutput = new StringWriter();

            Source aSource = new DOMSource(mElement);
            StreamResult aStreamResult = new StreamResult(aOutput);

            Transformer aTransformer = TransformerFactory.newInstance().newTransformer();

            aTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            aTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
            aTransformer.setOutputProperty(OutputKeys.METHOD, "html");
            aTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            aTransformer.setOutputProperty(OutputKeys.STANDALONE, "no");

            aTransformer.transform(aSource, aStreamResult);

            aOutput.close();

            return aOutput.toString().replaceAll("&amp;nbsp;", "&nbsp;");
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @inheritDoc
     */
    public void update(OWLAxiom theExplained, Set<OWLAxiom> theExplanationSet) {
        mExplainedAxiom = theExplained;
        mExplanationSet = theExplanationSet;
    }

    /**
     * @inheritDoc
     */
    public String render( OWLObject object ) {
        reset();
        object.accept( this );
        mDOM.appendChild(mElement);
        return getRendering();
    }

    /**
     * @inheritDoc
     */
    public void reset() {
        try {
            mDOM = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            mElement = mDOM.createElement("span");
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns the axiom being explained that this renderer will be used to render
     * @return the axiom being explained
     */
    public OWLAxiom getAxiomBeingExplained() {
        return mExplainedAxiom;
    }

    /**
     * Returns the explanation set for the axiom being explained, this renderer is being used (or will be used) to
     * render this set of axioms
     * @return the explanation set
     */
    public Set<OWLAxiom> getExplanationSet() {
        return mExplanationSet;
    }

    /**
     * Append a text node representing a non-breaking space
     */
    protected void insertSpace() {
        append(mDOM.createTextNode("&nbsp;"));
    }

    /**
     * @inheritDoc
     */

    public String getRendering() {
        return renderDOM();
    }

    /**
     * @inheritDoc
     */
    public Element getRenderedElement() {
        return mElement;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String toString() {
        return getRendering();
    }

    /**
     * @inheritDoc
     */
    public String getLiteralStyleClass() {
        return mLiteralStyleClass;
    }

     /**
     * @inheritDoc
     */
    public void setLiteralStyleClass(String theClass) {
        mLiteralStyleClass = theClass;
    }

    /**
     * @inheritDoc
     */
    public String getDatatypeStyleClass() {
        return mDatatypeStyleClass;
    }

   /**
     * @inheritDoc
     */
    public void setDatatypeStyleClass(String theClass) {
        mDatatypeStyleClass = theClass;
    }

    /**
     * @inheritDoc
     */
    public String getConstantStyleClass() {
        return mConstantStyleClass;
    }

    /**
     * @inheritDoc
     */
    public void setConstantStyleClass(String theClass) {
        mConstantStyleClass = theClass;
    }

    /**
     * @inheritDoc
     */
    public String getClassStyleClass() {
        return mClassStyleClass;
    }

    /**
     * @inheritDoc
     */
    public void setClassStyleClass(String theClass) {
        mClassStyleClass = theClass;
    }

    /**
     * @inheritDoc
     */
    public String getIndividualStyleClass() {
        return mIndividualStyleClass;
    }

    /**
     * @inheritDoc
     */
    public void setIndividualStyleClass(String theClass) {
        mIndividualStyleClass = theClass;
    }

    /**
     * @inheritDoc
     */
    public String getObjectPropertyStyleClass() {
        return mObjectPropertyStyleClass;
    }

    /**
     * @inheritDoc
     */
    public void setObjectPropertyStyleClass(String theClass) {
        mObjectPropertyStyleClass = theClass;
    }

    /**
     * @inheritDoc
     */
    public String getDataPropertyStyleClass() {
        return mDataPropertyStyleClass;
    }

    /**
     * @inheritDoc
     */
    public void setDataPropertyStyleClass(String theClass) {
        mDataPropertyStyleClass = theClass;
    }

    /**
     * Applies formatting to the specified Node
     * @param theElem the Node to apply the formatting to
     * @param theObj the object which the Node represents the rendering of
     * @return the element with formatting applied
     */
    protected Element applyFormatting(Node theElem, OWLObject theObj) {
        String aClass = null;

        if (theObj instanceof OWLClass) {
            aClass = getClassStyleClass();
        }
        else if (theObj instanceof OWLObjectProperty) {
            aClass = getObjectPropertyStyleClass();
        }
        else if (theObj instanceof OWLDataProperty) {
            aClass = getDataPropertyStyleClass();
        }
        else if (theObj instanceof OWLIndividual) {
            aClass = getIndividualStyleClass();
        }
        else if (theObj instanceof OWLTypedConstant) {
            aClass = getLiteralStyleClass();
        }
        else if (theObj instanceof OWLDataType) {
            aClass = getDatatypeStyleClass();
        }
        else if (theObj instanceof OWLUntypedConstant) {
            aClass = getConstantStyleClass();
        }

        return applyFormatting(theElem, aClass);
    }

    /**
     * Applies formatting to the specified node
     * @param theElem the Node to apply the formatting to
     * @param theClass the style class to apply
     * @return the node, with formatting
     */
    protected Element applyFormatting(Node theElem, String theClass) {

        if (theClass != null || !(theElem instanceof Element)) {
            Element aElem = mDOM.createElement("span");

            if (theClass != null) {
                aElem.setAttribute("class", theClass);
            }
            
            aElem.appendChild(theElem);

            return aElem;
        }
        else {
            return (Element) theElem;
        }
    }

    /**
     * Create a link (anchor tag) element for the given URI using the shortName of the URI as the text of the tag
     * @param theURI the href for the link
     * @return a link element for the URI
     */
    protected Element createLink(URI theURI) {
        Element aLink = mElement.getOwnerDocument().createElement("a");

        aLink.setAttribute("href", theURI.toString());
        aLink.appendChild(aLink.getOwnerDocument().createTextNode(shortForm(theURI)));

        return aLink;
    }

    public void visit(OWLClass theOWLClass) {
        mElement.appendChild(applyFormatting(createLink(theOWLClass.getURI()), theOWLClass));
    }

    public void visit(OWLObjectProperty theProperty) {
        append(applyFormatting(createLink(theProperty.getURI()), theProperty));
    }

    public void visit(OWLDataProperty theProperty) {
        append(applyFormatting(createLink(theProperty.getURI()), theProperty));
    }

    public void visit(OWLIndividual theIndividual) {
        append(applyFormatting(createLink(theIndividual.getURI()), theIndividual));
    }

    public void visit(OWLTypedConstant node) {

        Element aParent = preVisit();

        String aLiteralValue = "\"" + node.getLiteral() + "\"^^";

        append(mDOM.createTextNode(aLiteralValue));

        node.getDataType().accept(this);

        postVisit(aParent, node);
    }

    public void visit(OWLDataType node) {
        append(applyFormatting(mElement.getOwnerDocument().createTextNode(node.getURI().getFragment()), node));
    }

    public void visit(OWLUntypedConstant node) {
        StringBuilder aBuilder = new StringBuilder();

        aBuilder.append("\"").append(node.getLiteral()).append("\"");
        if (node.hasLang()) {
            aBuilder.append("@").append(node.getLang());
        }

        append(applyFormatting(mElement.getOwnerDocument().createTextNode(aBuilder.toString()), node));
    }

    /*
     * this is all the SWRL rendering stuff that we'll provide some defaults for using evren's concise format stuff
     */

    /**
     * @inheritDoc
     */
    public void visit(SWRLRule rule) {
        Element aParent = preVisit();

        append(createKeywordElement("Rule", "green", 3, null));
        append("(");

        if (!rule.isAnonymous()) {
            append(createLink(rule.getURI()) );
            insertSpace();
        }

        append(createKeywordElement("antecedent", "green", 3, null));
        append("(");
        for (SWRLAtom<?> at : rule.getBody()) {
            at.accept(this);
            append(" ");
        }
        append(") ");

        append(createKeywordElement("consequent", "green", 3, null));
        append("(");
        for (SWRLAtom<?> at : rule.getHead()) {
            at.accept(this);
            append(" ");
        }
        append(")");

        append(")");

        postVisit(aParent, rule);
    }

    public void visit(SWRLDifferentFromAtom node) {
        Element aParent = preVisit();

        append(createKeywordElement("differentFromAtom", "green", 3, null));
        append("(");
        node.getFirstArgument().accept(this);
        append(" ");
        node.getSecondArgument().accept(this);
        append(")");

        postVisit(aParent, node);
    }

    public void visit(SWRLSameAsAtom node) {
        Element aParent = preVisit();

        append(createKeywordElement("sameAsAtom", "green", 3, null));
        append("(");
        node.getFirstArgument().accept(this);
        append(" ");
        node.getSecondArgument().accept(this);
        append(")");

        postVisit(aParent, node);
    }

    public void visit(SWRLObjectPropertyAtom node) {
        Element aParent = preVisit();

        node.getPredicate().accept(this);
        append("(");
        node.getFirstArgument().accept(this);
        append(" ");
        node.getSecondArgument().accept(this);
        append(")");

        postVisit(aParent, node);
    }

    public void visit(SWRLDataValuedPropertyAtom node) {
        Element aParent = preVisit();

        node.getPredicate().accept(this);
        append("(");
        node.getFirstArgument().accept(this);
        append(" ");
        node.getSecondArgument().accept(this);
        append(")");

        postVisit(aParent, node);
    }

    public void visit(SWRLBuiltInAtom node) {
        Element aParent = preVisit();

        append(createLink(node.getPredicate().getURI()));
        append("(");
        for (SWRLAtomObject arg : node.getArguments()) {
            arg.accept(this);
            append(" ");
        }
        append(")");

        postVisit(aParent, node);
    }

    public void visit(SWRLAtomDVariable node) {
        append("?");
        append(createLink(node.getURI()));
    }


    public void visit(SWRLAtomIVariable node) {
        append("?");
        append(createLink(node.getURI()));
    }


    public void visit(SWRLAtomIndividualObject node) {
        Element aParent = preVisit();

        node.getIndividual().accept(this);

        postVisit(aParent, node);
    }


    public void visit(SWRLAtomConstantObject node) {
        Element aParent = preVisit();

        node.getConstant().accept(this);

        postVisit(aParent, node);
    }

    public void visit(SWRLClassAtom node) {
        Element aParent = preVisit();

        node.getPredicate().accept(this);
        append("(");
        node.getArgument().accept(this);
        append(")");

        postVisit(aParent, node);
    }

    public void visit(SWRLDataRangeAtom node) {
        Element aParent = preVisit();

        node.getPredicate().accept(this);
        append("(");
        node.getArgument().accept(this);
        append(")");

        postVisit(aParent, node);
    }

    /**
     * Create a DOM element object for a keyword with the specified formatting
     * @param theText the text of the keyword
     * @param theColor the color to render the text in
     * @param theSize the size of the font to render, or -1 to use the default size
     * @param theFace the font face, or null for the default
     * @return the keyword element rendered as a DOM Element
     */
    protected Element createKeywordElement(String theText, String theColor, int theSize, String theFace) {
        if (theFace == null)
            theFace = "monospaced";

        Element aElem = mDOM.createElement("font");

        aElem.setAttribute("face", theFace);
        aElem.setAttribute("color", theColor);

        if (theSize != -1) {
            aElem.setAttribute("size", "" + theSize);
        }

        aElem.appendChild(aElem.getOwnerDocument().createTextNode(theText));

        return aElem;
    }

    /**
     * Append the string as a Text node to the current DOM element
     * @param theString the string to append
     * @return returns the current DOM element with the string appended
     */
    protected Element append(String theString) {
        return append(mDOM.createTextNode(theString));
    }

    /**
     * Append the specified node to the current DOM element
     * @param theNode the Node to append
     * @return the current DOM element with the Node appended
     */
    protected Element append(Node theNode) {
        mElement.appendChild(theNode);

        return mElement;
    }

    /**
     * Return the short form (local name) for a URI identifier
     * @param theURI the URI
     * @return the local name part of the URI identifier
     */
    protected String shortForm(URI theURI) {
        String fragment = theURI.getFragment();
        if (fragment != null) {
            return fragment;
        }
        int lastSlashIndex = theURI.getPath().lastIndexOf('/');
        if (lastSlashIndex != -1) {
            return theURI.getPath().substring(lastSlashIndex + 1, theURI.getPath().length());
        }
        return theURI.toString();
    }

    /**
     * Sets up the current DOM element for rendering.  This should be called by any visit method <em>BEFORE</em>
     * rendering takes place.  Returns a reference to the old current node; this reference should
     * be kept and passed to the postVisit method.<br><br>
     * <code>
     * public void visit(OWLEquivalentClassesAxiom theAxiom) {
     *     Element aParent = preVisit();
     *     // to rendering tasks here
     *     postVisit(aParent, theAxiom);
     * }
     * </code>
     * @return a reference to the old current DOM element
     */
    protected Element preVisit() {
        Element aParent = mElement;
        mElement = aParent.getOwnerDocument().createElement("span");

        return aParent;
    }

    /**
     * Resets the current DOM element, after attaching the results of the rendering to the DOM tree.  Formatting
     * is applied to the element before it is appended to the tree.  This should be called in tandem with
     * {@link #preVisit preVisit()}.
     * @param theParent the old current DOM element, as returned from preVisit()
     * @param theObj the object which was just rendered
     * @see #preVisit()
     */
    protected void postVisit(Element theParent, OWLObject theObj) {
        mElement = applyFormatting(mElement, theObj);
        theParent.appendChild(mElement);
        mElement = theParent;
    }
}