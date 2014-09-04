package com.clarkparsia.explanation.io.html;

import java.util.Set;
import java.util.Iterator;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyAnnotationAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLAntiSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLImportsDeclaration;
import org.semanticweb.owl.model.OWLAxiomAnnotationAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectSubPropertyAxiom;
import org.semanticweb.owl.model.OWLDisjointUnionAxiom;
import org.semanticweb.owl.model.OWLDeclarationAxiom;
import org.semanticweb.owl.model.OWLEntityAnnotationAxiom;
import org.semanticweb.owl.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owl.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLDataSubPropertyAxiom;
import org.semanticweb.owl.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyChainSubPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLObjectMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectSelfRestriction;
import org.semanticweb.owl.model.OWLObjectOneOf;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataComplementOf;
import org.semanticweb.owl.model.OWLDataOneOf;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataRangeRestriction;
import org.semanticweb.owl.model.OWLDataRangeFacetRestriction;
import org.semanticweb.owl.model.OWLObjectPropertyInverse;
import org.semanticweb.owl.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLConstantAnnotation;
import org.semanticweb.owl.model.OWLObjectAnnotation;
import org.w3c.dom.Element;

import com.clarkparsia.explanation.io.html.utils.DescriptionSorter;

/**
 * Title: <br>
* Description: <br>
* Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
* Created: Apr 18, 2007 11:28:01 AM
*
* @author Michael Grove <mhgrove@hotmail.com>
*/
public class HTMLAbstractSyntaxObjectRenderer extends HTMLObjectStrikeRenderer {

    /**
     * Style class if you wish to override the default styling of keywords.
     */
    private String mKeywordStyle = null;

    public HTMLAbstractSyntaxObjectRenderer() {
        super();
    }

    public HTMLAbstractSyntaxObjectRenderer(String theKeywordStyle) {
        mKeywordStyle = theKeywordStyle;
    }

    /**
     * Style this keyword using either the default styling, or a specific style class if one is specified and
     * return the resulting DOM object
     * @param theKeyword the keyword to style
     * @return the resulting styled DOM object
     */
    protected Element createKeywordElement(String theKeyword) {
        if (mKeywordStyle != null)
            return applyFormatting(mElement.getOwnerDocument().createTextNode(theKeyword), theKeyword);
        else return createKeywordElement(theKeyword, "green", 3, null);
    }

    public void visit(OWLOntology ontology) {
        Element aParent = preVisit();

        append(createKeywordElement("Ontology"));
        append("(");
        append(ontology.getURI().toString());
        append(")");

        postVisit(aParent, ontology);
    }

    public void visit(OWLOntologyAnnotationAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("OntologyAnnotation"));
        append("(");
        theAxiom.getAnnotation().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLSubClassAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("SubClassOf"));
        append("(");
        theAxiom.getSubClass().accept(this);
        insertSpace();
        theAxiom.getSuperClass().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLNegativeObjectPropertyAssertionAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("NotObjectRelationship"));
        append("(");
        theAxiom.getProperty().accept(this);
        append("(");
        theAxiom.getSubject().accept(this);
        insertSpace();
        theAxiom.getObject().accept(this);
        append(")");
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLAntiSymmetricObjectPropertyAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("AntiSymmetricObjectProperty"));
        append("(");
        theAxiom.getProperty().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLReflexiveObjectPropertyAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("ReflexiveObjectProperty"));
        append("(");
        theAxiom.getProperty().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLDisjointClassesAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("DisjointClasses"));
        append("(");
        boolean first = true;
        for (OWLDescription desc : DescriptionSorter.toSortedSet(theAxiom.getDescriptions())) {
           if( first )
                first = false;
            else
                insertSpace();
            desc.accept(this);
        }
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLDataPropertyDomainAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("DataPropertyDomain"));
        append("(");
        theAxiom.getProperty().accept(this);
        insertSpace();
        theAxiom.getDomain().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLImportsDeclaration theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("Imports"));
        append("(");
        append(theAxiom.getSubject().getURI().toString());
        append(" -> ");
        append(theAxiom.getImportedOntologyURI().toString());
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLAxiomAnnotationAxiom theAxiom) {
        Element aParent = preVisit();

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLObjectPropertyDomainAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("ObjectPropertyDomain"));
        append("(");
        theAxiom.getProperty().accept(this);
        insertSpace();
        theAxiom.getDomain().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLEquivalentObjectPropertiesAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("EquivalentObjectProperties"));
        append("(");
        for (OWLObjectPropertyExpression prop : DescriptionSorter.toSortedSet(theAxiom.getProperties())) {
            insertSpace();
            prop.accept(this);
        }
        append(" )");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLNegativeDataPropertyAssertionAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("NotDataRelationship"));
        append("(");
        theAxiom.getProperty().accept(this);
        append("(");
        theAxiom.getSubject().accept(this);
        insertSpace();
        theAxiom.getObject().accept(this);
        append(")");
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLDifferentIndividualsAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("DifferentIndividuals"));
        append("(");
        for (OWLIndividual ind : theAxiom.getIndividuals()) {
            insertSpace();
            ind.accept(this);
        }
        append(" )");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLDisjointDataPropertiesAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("DisjointDataProperties"));
        append("(");
        for (OWLDataPropertyExpression prop : DescriptionSorter.toSortedSet(theAxiom.getProperties())) {
            insertSpace();
            prop.accept(this);
        }
        append(" )");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLDisjointObjectPropertiesAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("DisjointObjectProperties"));
        append("(");
        for (OWLObjectPropertyExpression prop : DescriptionSorter.toSortedSet(theAxiom.getProperties())) {
            insertSpace();
            prop.accept(this);
        }
        append(" )");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLObjectPropertyRangeAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("Range"));
        append("(");
        theAxiom.getProperty().accept(this);
        insertSpace();
        theAxiom.getRange().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLObjectPropertyAssertionAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("ObjectRelationship"));
        append("(");
        theAxiom.getProperty().accept(this);
        append("(");
        theAxiom.getSubject().accept(this);
        insertSpace();
        theAxiom.getObject().accept(this);
        append(")");
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLFunctionalObjectPropertyAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("Functional"));
        append("(");
        theAxiom.getProperty().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLObjectSubPropertyAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("SubProperty"));
        append("(");
        theAxiom.getSubProperty().accept(this);
        insertSpace();
        theAxiom.getSuperProperty().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLDisjointUnionAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("DisjointUnion"));
        append("(");
        theAxiom.getOWLClass().accept(this);
        insertSpace();
        for (OWLDescription desc : DescriptionSorter.toSortedSet(theAxiom.getDescriptions())) {
            insertSpace();
            desc.accept(this);
        }
        append(" )");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLDeclarationAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("Declaration"));
        append("(");
        theAxiom.getEntity().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLEntityAnnotationAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("EntityAnnotationAxiom"));
        append("(");
        theAxiom.getSubject().accept(this);
        insertSpace();
        theAxiom.getAnnotation().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLSymmetricObjectPropertyAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("Symmetric"));
        append("(");
        theAxiom.getProperty().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLDataPropertyRangeAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("DataPropertyRange"));
        append("(");
        theAxiom.getProperty().accept(this);
        insertSpace();
        theAxiom.getRange().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLFunctionalDataPropertyAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("FunctionalDataProperty"));
        append("(");
        theAxiom.getProperty().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLEquivalentDataPropertiesAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("EquivalentDataProperties"));
        append("(");
        for (OWLDataPropertyExpression prop : DescriptionSorter.toSortedSet(theAxiom.getProperties())) {
            insertSpace();
            prop.accept(this);
        }
        append(" )");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLClassAssertionAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("Type"));
        append("(");
        theAxiom.getIndividual().accept(this);
        insertSpace();
        theAxiom.getDescription().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLEquivalentClassesAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("EquivalentClasses"));
        append("(");
        Set<OWLDescription> descriptions = theAxiom.getDescriptions();
        if( descriptions.size() == 2 ) {
            Iterator<OWLDescription> i = descriptions.iterator();
            OWLDescription c1 = i.next();
            OWLDescription c2 = i.next();
            if( c1.isAnonymous() && !c2.isAnonymous() ) {
                c2.accept( this );
                insertSpace();
                c1.accept( this );
            }
            else {
                c1.accept( this );
                insertSpace();
                c2.accept( this );
            }
        }
        else {
            boolean first = true;
            for (OWLDescription desc : DescriptionSorter.toSortedSet(descriptions)) {
                if( first )
                    first = false;
                else
                    insertSpace();
                desc.accept(this);
            }
        }
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLDataPropertyAssertionAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("DataRelationship"));
        append("(");
        theAxiom.getProperty().accept(this);
        append("(");
        theAxiom.getSubject().accept(this);
        insertSpace();
        theAxiom.getObject().accept(this);
        append(")");
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLTransitiveObjectPropertyAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("Transitive"));
        append("(");
        theAxiom.getProperty().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLIrreflexiveObjectPropertyAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("Irreflexive"));
        append("(");
        theAxiom.getProperty().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLDataSubPropertyAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("DataSubProperty"));
        append("(");
        theAxiom.getSubProperty().accept(this);
        insertSpace();
        theAxiom.getSuperProperty().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLInverseFunctionalObjectPropertyAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("InverseFunctional"));
        append("(");
        theAxiom.getProperty().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLSameIndividualsAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("SameIndividuals"));
        append("(");
        for (OWLIndividual ind : theAxiom.getIndividuals()) {
            insertSpace();
            ind.accept(this);
        }
        append(" )");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLObjectPropertyChainSubPropertyAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("SubPropertyOf"));
        append("(");
        append("(");
        for (OWLObjectPropertyExpression prop : theAxiom.getPropertyChain()) {
            insertSpace();
            prop.accept(this);
        }
        append(" )");
        insertSpace();
        theAxiom.getSuperProperty().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLObjectIntersectionOf theDesc) {
        Element aParent = preVisit();

        append(createKeywordElement("And"));
        append("(");
        boolean first = true;
        for (OWLDescription op : DescriptionSorter.toSortedSet(theDesc.getOperands())) {
            if( first )
                first = false;
            else
                insertSpace();
            op.accept(this);
        }
        append(")");

        postVisit(aParent, theDesc);
    }

    public void visit(OWLObjectUnionOf theDesc) {
        Element aParent = preVisit();

        append(createKeywordElement("Or"));
        append("(");
        boolean first = true;
        for (OWLDescription op : DescriptionSorter.toSortedSet(theDesc.getOperands())) {
            if( first )
                first = false;
            else
                insertSpace();
            op.accept(this);
        }
        append(")");

        postVisit(aParent, theDesc);
    }

    public void visit(OWLObjectComplementOf theDesc) {
        Element aParent = preVisit();

        append(createKeywordElement("Not"));
        append("(");
        theDesc.getOperand().accept(this);
        append(")");

        postVisit(aParent, theDesc);
    }

    public void visit(OWLObjectSomeRestriction theDesc) {
        Element aParent = preVisit();

        append(createKeywordElement("Some"));
        append("(");
        theDesc.getProperty().accept(this);
        insertSpace();
        theDesc.getFiller().accept(this);
        append(")");

        postVisit(aParent, theDesc);
    }

    public void visit(OWLObjectAllRestriction theDesc) {
        Element aParent = preVisit();

        append(createKeywordElement("All"));
        append("(");
        theDesc.getProperty().accept(this);
        insertSpace();
        theDesc.getFiller().accept(this);
        append(")");

        postVisit(aParent, theDesc);
    }

    public void visit(OWLObjectValueRestriction theDesc) {
        Element aParent = preVisit();

        append(createKeywordElement("Value"));
        append("(");
        theDesc.getProperty().accept(this);
        insertSpace();
        theDesc.getValue().accept(this);
        append(")");

        postVisit(aParent, theDesc);
    }

    public void visit(OWLObjectMinCardinalityRestriction theDesc) {
        Element aParent = preVisit();

        append(createKeywordElement("Min"));
        append("(");
        append(""+theDesc.getCardinality());
        insertSpace();
        theDesc.getProperty().accept(this);
        insertSpace();
        theDesc.getFiller().accept(this);
        append(")");

        postVisit(aParent, theDesc);
    }

    public void visit(OWLObjectExactCardinalityRestriction theDesc) {
        Element aParent = preVisit();

        append(createKeywordElement("Exact"));
        append("(");
        append(""+theDesc.getCardinality());
        insertSpace();
        theDesc.getProperty().accept(this);
        insertSpace();
        theDesc.getFiller().accept(this);
        append(")");

        postVisit(aParent, theDesc);
    }

    public void visit(OWLObjectMaxCardinalityRestriction theDesc) {
        Element aParent = preVisit();

        append(createKeywordElement("Max"));
        append("(");
        append(""+theDesc.getCardinality());
        insertSpace();
        theDesc.getProperty().accept(this);
        insertSpace();
        theDesc.getFiller().accept(this);
        append(")");

        postVisit(aParent, theDesc);
    }

    public void visit(OWLObjectSelfRestriction theDesc) {
        Element aParent = preVisit();

        append(createKeywordElement("Self"));
        append("(");
        theDesc.getProperty().accept(this);
        append(")");

        postVisit(aParent, theDesc);
    }

    public void visit(OWLObjectOneOf theDesc) {
        Element aParent = preVisit();

        append(createKeywordElement("Enumeration"));
        append("(");
        for (OWLIndividual ind : DescriptionSorter.toSortedSet(theDesc.getIndividuals())) {
            insertSpace();
            ind.accept(this);
        }
        append(" )");

        postVisit(aParent, theDesc);
    }

    public void visit(OWLDataSomeRestriction theDesc) {
        Element aParent = preVisit();

        append(createKeywordElement("DataSome"));
        append("(");
        theDesc.getProperty().accept(this);
        insertSpace();
        theDesc.getFiller().accept(this);
        append(")");

        postVisit(aParent, theDesc);
    }

    public void visit(OWLDataAllRestriction theDesc) {
        Element aParent = preVisit();

        append(createKeywordElement("DataAll"));
        append("(");
        theDesc.getProperty().accept(this);
        insertSpace();
        theDesc.getFiller().accept(this);
        append(")");

        postVisit(aParent, theDesc);
    }

    public void visit(OWLDataValueRestriction theDesc) {
        Element aParent = preVisit();

        append(createKeywordElement("DataValue"));
        append("(");
        theDesc.getProperty().accept(this);
        insertSpace();
        theDesc.getValue().accept(this);
        append(")");

        postVisit(aParent, theDesc);
    }

    public void visit(OWLDataMinCardinalityRestriction theDesc) {
        Element aParent = preVisit();

        append(createKeywordElement("DataMin"));
        append("(");
        append(""+theDesc.getCardinality());
        insertSpace();
        theDesc.getProperty().accept(this);
        insertSpace();
        theDesc.getFiller().accept(this);
        append(")");

        postVisit(aParent, theDesc);
    }

    public void visit(OWLDataExactCardinalityRestriction theDesc) {
        Element aParent = preVisit();

        append(createKeywordElement("DataExact"));
        append("(");
        append(""+theDesc.getCardinality());
        insertSpace();
        theDesc.getProperty().accept(this);
        insertSpace();
        theDesc.getFiller().accept(this);
        append(")");

        postVisit(aParent, theDesc);
    }

    public void visit(OWLDataMaxCardinalityRestriction theDesc) {
        Element aParent = preVisit();

        append(createKeywordElement("DataMax"));
        append("(");
        append(""+theDesc.getCardinality());
        insertSpace();
        theDesc.getProperty().accept(this);
        insertSpace();
        theDesc.getFiller().accept(this);
        append(")");

        postVisit(aParent, theDesc);
    }

    public void visit(OWLDataComplementOf theNode) {
        Element aParent = preVisit();

        append(createKeywordElement("NotDataRange"));
        append("(");
        theNode.getDataRange().accept(this);
        append(")");

        postVisit(aParent, theNode);
    }

    public void visit(OWLDataOneOf theNode) {
        Element aParent = preVisit();

        append(createKeywordElement("DataEnumeration"));
        append("(");
        for (OWLConstant con : theNode.getValues()) {
            insertSpace();
            con.accept(this);
        }
        append(" )");

        postVisit(aParent, theNode);
    }

    public void visit(OWLDataRangeRestriction theNode) {
        Element aParent = preVisit();

        append(createKeywordElement("DataRangeRestriction"));
        append("(");
        theNode.getDataRange().accept(this);
        for (OWLDataRangeFacetRestriction restriction : theNode.getFacetRestrictions()) {
            insertSpace();
            restriction.accept(this);
        }
        append(")");

        postVisit(aParent, theNode);
    }

    public void visit(OWLDataRangeFacetRestriction theNode) {
        Element aParent = preVisit();

        append(createKeywordElement("facetRestriction"));
        append("(");
        append(theNode.getFacet().toString());
        insertSpace();
        theNode.getFacetValue().accept(this);
        append(")");

        postVisit(aParent, theNode);
    }

    public void visit(OWLObjectPropertyInverse theProperty) {
        Element aParent = preVisit();

        append(createKeywordElement("InverseOf"));
        append("(");
        theProperty.getInverse().accept(this);
        append(")");

        postVisit(aParent, theProperty);
    }

    public void visit(OWLInverseObjectPropertiesAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement("InverseProperties"));
        append("(");
        theAxiom.getFirstProperty().accept(this);
        append(" ");
        theAxiom.getSecondProperty().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLConstantAnnotation theAnnotation) {
        Element aParent = preVisit();

        if (theAnnotation.isLabel()) {
            append(createKeywordElement("Label"));
            append("(");
        }
        else if (theAnnotation.isComment()) {
            append(createKeywordElement("Comment"));
            append("(");
        }
        else {
            append(createKeywordElement("Annotation"));
            append("(");
            append(createLink(theAnnotation.getAnnotationURI()));
        }
        append(" ");
        theAnnotation.getAnnotationValue().accept(this);
        append(")");

        postVisit(aParent, theAnnotation);
    }

    public void visit(OWLObjectAnnotation theAnnotation) {
        Element aParent = preVisit();

        append(createKeywordElement("Annotation"));
        append("(");
        append(createLink(theAnnotation.getAnnotationURI()));
        append(" ");
        theAnnotation.getAnnotationValue().accept(this);
        append(")");

        postVisit(aParent, theAnnotation);
    }
}