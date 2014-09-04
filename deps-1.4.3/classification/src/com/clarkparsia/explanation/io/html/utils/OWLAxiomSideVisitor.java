package com.clarkparsia.explanation.io.html.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.owl.model.OWLAntiSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLAxiomAnnotationAxiom;
import org.semanticweb.owl.model.OWLAxiomVisitor;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLConstantAnnotation;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataComplementOf;
import org.semanticweb.owl.model.OWLDataExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataOneOf;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLDataRangeFacetRestriction;
import org.semanticweb.owl.model.OWLDataRangeRestriction;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataSubPropertyAxiom;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDeclarationAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owl.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLDisjointUnionAxiom;
import org.semanticweb.owl.model.OWLEntityAnnotationAxiom;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owl.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owl.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLImportsDeclaration;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectAnnotation;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectOneOf;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyChainSubPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectPropertyInverse;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLObjectSelfRestriction;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectSubPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLObjectVisitor;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyAnnotationAxiom;
import org.semanticweb.owl.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLTypedConstant;
import org.semanticweb.owl.model.OWLUntypedConstant;
import org.semanticweb.owl.model.SWRLAtomConstantObject;
import org.semanticweb.owl.model.SWRLAtomDVariable;
import org.semanticweb.owl.model.SWRLAtomIVariable;
import org.semanticweb.owl.model.SWRLAtomIndividualObject;
import org.semanticweb.owl.model.SWRLBuiltInAtom;
import org.semanticweb.owl.model.SWRLClassAtom;
import org.semanticweb.owl.model.SWRLDataRangeAtom;
import org.semanticweb.owl.model.SWRLDataValuedPropertyAtom;
import org.semanticweb.owl.model.SWRLDifferentFromAtom;
import org.semanticweb.owl.model.SWRLObjectPropertyAtom;
import org.semanticweb.owl.model.SWRLRule;
import org.semanticweb.owl.model.SWRLSameAsAtom;

/**
 * An "updated" version of the OWLObjectContainer visitor class from SWOOP.  Mainly just up-porting the previous
 * code to the latest OWL-API.  This is pretty much verbatim from what is in SWOOP, with changes to match
 * API changes, so the mileage of this code will vary; I suspect Aditya's approach was not complete and that
 * this won't catch all the cases, and I've purposely left out some coverage (notably SWRL, annotations)
 * since we wont be using that stuff in the near future.  Possible known issues are marked with to do comments
 */
public class OWLAxiomSideVisitor implements OWLAxiomVisitor, OWLObjectVisitor {
    /**
     * The objects which appear on the left hand side of the axiom
     */
    private List<OWLObject> mLeftHandSide = new ArrayList<OWLObject>();

    /**
     * The objects which appear on the right hand side of the axiom
     */
    private List<OWLObject> mRightHandSide = new ArrayList<OWLObject>();

    /**
     * The list of object used in this axioms
     */
    private List<OWLObject> mObjects = new ArrayList<OWLObject>();

    /**
     * Reset the state of the visitor
     */
    public void reset() {
        mLeftHandSide.clear();
        mRightHandSide.clear();

        mObjects.clear();
    }

    /**
     * Return the collection of objects found in the LHS of the axiom
     * @return the collection of concepts on the LHS of the axioms
     */
    public List<OWLObject> getLHS() {
        if (mLeftHandSide.size() == 0 && mRightHandSide.size() == 0 && mObjects.size() > 0) {
            // this is for the case when we've tried to gather the sides for, say, intersectionOf, which isn't an axiom
            // so we don't add its components to one side or the other, so this will pretend like the objects are all
            // on the left side.  I *think* this is ok, Aditya's code performs this in an adhoc manner in several
            // different spots of the code.  so we'll at least get the same behavior w/o repeated code.
            return Collections.unmodifiableList(mObjects);
        }
        else {
            return Collections.unmodifiableList(mLeftHandSide);
        }
    }

    /**
     * Return the collection of concepts found in the RHS of the axiom
     * @return the concepts on the RHS
     */
    public List<OWLObject> getRHS() {
        return Collections.unmodifiableList(mRightHandSide);
    }

    public void visit(OWLSubClassAxiom theOWLSubClassAxiom) {
        theOWLSubClassAxiom.getSubClass().accept(this);
        mLeftHandSide.addAll(mObjects);
        mObjects.clear();

        theOWLSubClassAxiom.getSuperClass().accept(this);
        mRightHandSide.addAll(mObjects);
        mObjects.clear();
    }

    public void visit(OWLNegativeObjectPropertyAssertionAxiom theOWLNegativeObjectPropertyAssertionAxiom) {
        // TODO: is this right?
        mLeftHandSide.add(theOWLNegativeObjectPropertyAssertionAxiom.getProperty());

        mRightHandSide.add(theOWLNegativeObjectPropertyAssertionAxiom.getSubject());
        mRightHandSide.add(theOWLNegativeObjectPropertyAssertionAxiom.getObject());
    }

    public void visit(OWLAntiSymmetricObjectPropertyAxiom theOWLAntiSymmetricObjectPropertyAxiom) {
        theOWLAntiSymmetricObjectPropertyAxiom.getProperty().accept(this);
        mLeftHandSide.addAll(mObjects);
    }

    public void visit(OWLReflexiveObjectPropertyAxiom theOWLReflexiveObjectPropertyAxiom) {
        theOWLReflexiveObjectPropertyAxiom.getProperty().accept(this);
        mLeftHandSide.addAll(mObjects);
    }

    public void visit(OWLDisjointClassesAxiom theOWLDisjointClassesAxiom) {
        visitSet(theOWLDisjointClassesAxiom.getDescriptions());
    }

    public void visit(OWLDataPropertyDomainAxiom theOWLDataPropertyDomainAxiom) {
        theOWLDataPropertyDomainAxiom.getProperty().accept(this);
        mLeftHandSide.addAll(mObjects);

        mObjects.clear();

        theOWLDataPropertyDomainAxiom.getDomain().accept(this);
        mRightHandSide.addAll(mObjects);
    }

    public void visit(OWLImportsDeclaration theOWLImportsDeclaration) {
        // TODO: what to do with imports?
    }

    public void visit(OWLAxiomAnnotationAxiom theOWLAxiomAnnotationAxiom) {
        // TODO: handle annotations
    }

    public void visit(OWLObjectPropertyDomainAxiom theOWLObjectPropertyDomainAxiom) {
        theOWLObjectPropertyDomainAxiom.getProperty().accept(this);
        mLeftHandSide.addAll(mObjects);

        mObjects.clear();

        theOWLObjectPropertyDomainAxiom.getDomain().accept(this);
        mRightHandSide.addAll(mObjects);
    }

    public void visit(OWLEquivalentObjectPropertiesAxiom theOWLEquivalentObjectPropertiesAxiom) {
    	visitSet(theOWLEquivalentObjectPropertiesAxiom.getProperties());
    }

    public void visit(OWLNegativeDataPropertyAssertionAxiom theOWLNegativeDataPropertyAssertionAxiom) {
        // TODO: is this right?
        mLeftHandSide.add(theOWLNegativeDataPropertyAssertionAxiom.getProperty());

        mRightHandSide.add(theOWLNegativeDataPropertyAssertionAxiom.getSubject());
        mRightHandSide.add(theOWLNegativeDataPropertyAssertionAxiom.getObject());
    }

    public void visit(OWLDifferentIndividualsAxiom theOWLDifferentIndividualsAxiom) {
    	visitSet(theOWLDifferentIndividualsAxiom.getIndividuals());
    }

    public void visit(OWLDisjointDataPropertiesAxiom theOWLDisjointDataPropertiesAxiom) {
    	visitSet(theOWLDisjointDataPropertiesAxiom.getProperties());
    }

    public void visit(OWLDisjointObjectPropertiesAxiom theOWLDisjointObjectPropertiesAxiom) {
    	visitSet(theOWLDisjointObjectPropertiesAxiom.getProperties()) ;
    }

    public void visit(OWLObjectPropertyRangeAxiom theOWLObjectPropertyRangeAxiom) {
        theOWLObjectPropertyRangeAxiom.getProperty().accept(this);
        mLeftHandSide.addAll(mObjects);

        mObjects.clear();

        theOWLObjectPropertyRangeAxiom.getRange().accept(this);
        mRightHandSide.addAll(mObjects);
    }

    public void visit(OWLObjectPropertyAssertionAxiom theOWLObjectPropertyAssertionAxiom) {
        // TODO: is this right?
        mLeftHandSide.add(theOWLObjectPropertyAssertionAxiom.getProperty());

        mRightHandSide.add(theOWLObjectPropertyAssertionAxiom.getSubject());
        mRightHandSide.add(theOWLObjectPropertyAssertionAxiom.getObject());
    }

    public void visit(OWLFunctionalObjectPropertyAxiom theOWLFunctionalObjectPropertyAxiom) {
        theOWLFunctionalObjectPropertyAxiom.getProperty().accept(this);
        mLeftHandSide.addAll(mObjects);
    }

    public void visit(OWLObjectSubPropertyAxiom theOWLObjectSubPropertyAxiom) {
        theOWLObjectSubPropertyAxiom.getSubProperty().accept(this);
        mLeftHandSide.addAll(mObjects);

        mObjects.clear();

        theOWLObjectSubPropertyAxiom.getSuperProperty().accept(this);
        mRightHandSide.addAll(mObjects);
    }

    public void visit(OWLDisjointUnionAxiom theOWLDisjointUnionAxiom) {
    	theOWLDisjointUnionAxiom.getOWLClass().accept(this);
        mLeftHandSide.addAll(mObjects);
        mObjects.clear();
        
        for (OWLDescription aDesc : theOWLDisjointUnionAxiom.getDescriptions()) {
            aDesc.accept(this);
            mRightHandSide.addAll(mObjects);
            mObjects.clear();
        }
    }

    public void visit(OWLDeclarationAxiom theOWLDeclarationAxiom) {
        theOWLDeclarationAxiom.getEntity().accept(this);
        mLeftHandSide.addAll(mObjects);
    }

    public void visit(OWLEntityAnnotationAxiom theOWLEntityAnnotationAxiom) {
        // TODO: handle annotations
    }

    public void visit(OWLOntologyAnnotationAxiom theOWLOntologyAnnotationAxiom) {
        // TODO: handle annotations
    }

    public void visit(OWLSymmetricObjectPropertyAxiom theOWLSymmetricObjectPropertyAxiom) {
        theOWLSymmetricObjectPropertyAxiom.getProperty().accept(this);
        mLeftHandSide.addAll(mObjects);
    }

    public void visit(OWLDataPropertyRangeAxiom theOWLDataPropertyRangeAxiom) {
        theOWLDataPropertyRangeAxiom.getProperty().accept(this);
        mLeftHandSide.addAll(mObjects);

        mObjects.clear();

        theOWLDataPropertyRangeAxiom.getRange().accept(this);
        mRightHandSide.addAll(mObjects);
    }

    public void visit(OWLFunctionalDataPropertyAxiom theOWLFunctionalDataPropertyAxiom) {
        theOWLFunctionalDataPropertyAxiom.getProperty().accept(this);
        mLeftHandSide.addAll(mObjects);
    }

    public void visit(OWLEquivalentDataPropertiesAxiom theOWLEquivalentDataPropertiesAxiom) {
        visitSet(theOWLEquivalentDataPropertiesAxiom.getProperties());    
    }

    public void visit(OWLClassAssertionAxiom theOWLClassAssertionAxiom) {
        // TODO: is this correct?
        mLeftHandSide.add(theOWLClassAssertionAxiom.getIndividual());

        mRightHandSide.add(theOWLClassAssertionAxiom.getDescription());
    }

    public void visit(OWLEquivalentClassesAxiom theOWLEquivalentClassesAxiom) {
    	visitSet(theOWLEquivalentClassesAxiom.getDescriptions());
    }

    public void visit(OWLDataPropertyAssertionAxiom theOWLDataPropertyAssertionAxiom) {
        // TODO: is this right?
        mLeftHandSide.add(theOWLDataPropertyAssertionAxiom.getProperty());

        mRightHandSide.add(theOWLDataPropertyAssertionAxiom.getSubject());
        mRightHandSide.add(theOWLDataPropertyAssertionAxiom.getObject());
    }

    public void visit(OWLTransitiveObjectPropertyAxiom theOWLTransitiveObjectPropertyAxiom) {
        theOWLTransitiveObjectPropertyAxiom.getProperty().accept(this);
        mLeftHandSide.addAll(mObjects);
    }

    public void visit(OWLIrreflexiveObjectPropertyAxiom theOWLIrreflexiveObjectPropertyAxiom) {
        theOWLIrreflexiveObjectPropertyAxiom.getProperty().accept(this);
        mLeftHandSide.addAll(mObjects);
    }

    public void visit(OWLDataSubPropertyAxiom theOWLDataSubPropertyAxiom) {
        theOWLDataSubPropertyAxiom.getSubProperty().accept(this);
        mLeftHandSide.addAll(mObjects);

        mObjects.clear();

        theOWLDataSubPropertyAxiom.getSuperProperty().accept(this);
        mRightHandSide.addAll(mObjects);
    }

    public void visit(OWLInverseFunctionalObjectPropertyAxiom theOWLInverseFunctionalObjectPropertyAxiom) {
        theOWLInverseFunctionalObjectPropertyAxiom.getProperty().accept(this);

        mLeftHandSide.addAll(mObjects);
    }

    public void visit(OWLSameIndividualsAxiom theOWLSameIndividualsAxiom) {
        visitSet(theOWLSameIndividualsAxiom.getIndividuals());    
    }

    public void visit(OWLObjectPropertyChainSubPropertyAxiom theOWLObjectPropertyChainSubPropertyAxiom) {
        for (OWLObjectPropertyExpression aProp : theOWLObjectPropertyChainSubPropertyAxiom.getPropertyChain()) {
            aProp.accept(this);
        }
        mLeftHandSide.addAll(mObjects);

        mObjects.clear();

        theOWLObjectPropertyChainSubPropertyAxiom.getSuperProperty().accept(this);
        mRightHandSide.addAll(mObjects);
    }

    public void visit(OWLInverseObjectPropertiesAxiom theOWLInverseObjectPropertiesAxiom) {
        theOWLInverseObjectPropertiesAxiom.getFirstProperty().accept(this);
        mLeftHandSide.addAll(mObjects);

        mObjects.clear();

        theOWLInverseObjectPropertiesAxiom.getSecondProperty().accept(this);
        mRightHandSide.addAll(mObjects);
    }

    public void visit(OWLOntology theOWLOntology) {
        mObjects.add(theOWLOntology);
    }

    public void visit(OWLClass theOWLClass) {
        mObjects.add(theOWLClass);
    }

    public void visit(OWLObjectIntersectionOf theOWLObjectIntersectionOf) {
        for (OWLDescription aDesc : theOWLObjectIntersectionOf.getOperands()) {
            aDesc.accept(this);
        }
    }

    public void visit(OWLObjectUnionOf theOWLObjectUnionOf) {
        for (OWLDescription aDesc : theOWLObjectUnionOf.getOperands()) {
            aDesc.accept(this);
        }
    }

    public void visit(OWLObjectComplementOf theOWLObjectComplementOf) {
        theOWLObjectComplementOf.getOperand().accept(this);
    }

    public void visit(OWLObjectSomeRestriction theOWLObjectSomeRestriction) {
        theOWLObjectSomeRestriction.getFiller().accept(this);
        theOWLObjectSomeRestriction.getProperty().accept(this);
    }

    public void visit(OWLObjectAllRestriction theOWLObjectAllRestriction) {
        theOWLObjectAllRestriction.getFiller().accept(this);
        theOWLObjectAllRestriction.getProperty().accept(this);
    }

    public void visit(OWLObjectValueRestriction theOWLObjectValueRestriction) {
        theOWLObjectValueRestriction.getProperty().accept(this);
        theOWLObjectValueRestriction.getValue().accept(this);
    }

    public void visit(OWLObjectMinCardinalityRestriction theOWLObjectMinCardinalityRestriction) {
        theOWLObjectMinCardinalityRestriction.getFiller().accept(this);
        theOWLObjectMinCardinalityRestriction.getProperty().accept(this);
    }

    public void visit(OWLObjectExactCardinalityRestriction theOWLObjectExactCardinalityRestriction) {
        theOWLObjectExactCardinalityRestriction.getFiller().accept(this);
        theOWLObjectExactCardinalityRestriction.getProperty().accept(this);
    }

    public void visit(OWLObjectMaxCardinalityRestriction theOWLObjectMaxCardinalityRestriction) {
        theOWLObjectMaxCardinalityRestriction.getFiller().accept(this);
        theOWLObjectMaxCardinalityRestriction.getProperty().accept(this);
    }

    public void visit(OWLObjectSelfRestriction theOWLObjectSelfRestriction) {
        theOWLObjectSelfRestriction.getProperty().accept(this);
    }

    public void visit(OWLObjectOneOf theOWLObjectOneOf) {
        for (OWLIndividual aInd : theOWLObjectOneOf.getIndividuals()) {
            aInd.accept(this);
        }
    }

    public void visit(OWLDataSomeRestriction theOWLDataSomeRestriction) {
        theOWLDataSomeRestriction.getFiller().accept(this);
        theOWLDataSomeRestriction.getProperty().accept(this);
    }

    public void visit(OWLDataAllRestriction theOWLDataAllRestriction) {
        theOWLDataAllRestriction.getFiller().accept(this);
        theOWLDataAllRestriction.getProperty().accept(this);
    }

    public void visit(OWLDataValueRestriction theOWLDataValueRestriction) {
        theOWLDataValueRestriction.getProperty().accept(this);
        theOWLDataValueRestriction.getValue().accept(this);
    }

    public void visit(OWLDataMinCardinalityRestriction theOWLDataMinCardinalityRestriction) {
        theOWLDataMinCardinalityRestriction.getFiller().accept(this);
        theOWLDataMinCardinalityRestriction.getProperty().accept(this);
    }

    public void visit(OWLDataExactCardinalityRestriction theOWLDataExactCardinalityRestriction) {
        theOWLDataExactCardinalityRestriction.getFiller().accept(this);
        theOWLDataExactCardinalityRestriction.getProperty().accept(this);
    }

    public void visit(OWLDataMaxCardinalityRestriction theOWLDataMaxCardinalityRestriction) {
        theOWLDataMaxCardinalityRestriction.getProperty().accept(this);
        theOWLDataMaxCardinalityRestriction.getFiller().accept(this);
    }

    public void visit(OWLDataType theOWLDataType) {
        mObjects.add(theOWLDataType);
    }

    public void visit(OWLDataComplementOf theOWLDataComplementOf) {
        theOWLDataComplementOf.getDataRange().accept(this);
    }

    public void visit(OWLDataOneOf theOWLDataOneOf) {
        for (OWLConstant aValue : theOWLDataOneOf.getValues()) {
            aValue.accept(this);
        }
    }

    public void visit(OWLDataRangeRestriction theOWLDataRangeRestriction) {
        theOWLDataRangeRestriction.getDataRange().accept(this);
        for (OWLDataRangeFacetRestriction aRest : theOWLDataRangeRestriction.getFacetRestrictions()) {
            aRest.accept(this);
        }
    }

    public void visit(OWLTypedConstant theOWLTypedConstant) {
        mObjects.add(theOWLTypedConstant);
    }

    public void visit(OWLUntypedConstant theOWLUntypedConstant) {
        mObjects.add(theOWLUntypedConstant);
    }

    public void visit(OWLDataRangeFacetRestriction theOWLDataRangeFacetRestriction) {
        theOWLDataRangeFacetRestriction.getFacetValue().accept(this);
    }

    public void visit(org.semanticweb.owl.model.OWLObjectProperty theOWLObjectProperty) {
        mObjects.add(theOWLObjectProperty);
    }

    public void visit(OWLObjectPropertyInverse theOWLObjectPropertyInverse) {
        mObjects.add(theOWLObjectPropertyInverse);
    }

    public void visit(OWLDataProperty theOWLDataProperty) {
        mObjects.add(theOWLDataProperty);
    }

    public void visit(OWLIndividual theOWLIndividual) {
        mObjects.add(theOWLIndividual);
    }

    // TODO: handle annotations
    public void visit(OWLObjectAnnotation theOWLObjectAnnotation) {}
    public void visit(OWLConstantAnnotation theOWLConstantAnnotation) {}

    // TODO: track the SWRL stuff?
    public void visit(SWRLRule theSWRLRule) {}
    public void visit(SWRLClassAtom theSWRLClassAtom) {}
    public void visit(SWRLDataRangeAtom theSWRLDataRangeAtom) {}
    public void visit(SWRLObjectPropertyAtom theSWRLObjectPropertyAtom) {}
    public void visit(SWRLDataValuedPropertyAtom theSWRLDataValuedPropertyAtom) {}
    public void visit(SWRLBuiltInAtom theSWRLBuiltInAtom) {}
    public void visit(SWRLAtomDVariable theSWRLAtomDVariable) {}
    public void visit(SWRLAtomIVariable theSWRLAtomIVariable) {}
    public void visit(SWRLAtomIndividualObject theSWRLAtomIndividualObject) {}
    public void visit(SWRLAtomConstantObject theSWRLAtomConstantObject) {}
    public void visit(SWRLSameAsAtom theSWRLSameAsAtom) {}
    public void visit(SWRLDifferentFromAtom theSWRLDifferentFromAtom) {}

    /**
	 * Visit the objects in a set after sorting the elements. In the sorted
	 * results the first element is assumed to be the left-hand-side of the
	 * axiom and the rest is right-hand side.
	 * 
	 * @param theSet
	 */
    private void visitSet(Set<? extends OWLObject> theSet) {
    	Iterator<? extends OWLObject> i = DescriptionSorter.toSortedSet(theSet).iterator();
    	
        i.next().accept(this);
        mLeftHandSide.addAll(mObjects);
        mObjects.clear();

        while (i.hasNext()) {
        	i.next().accept(this);
            mRightHandSide.addAll(mObjects);
            mObjects.clear();        	
        }
    }
}

