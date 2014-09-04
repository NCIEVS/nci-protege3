package com.clarkparsia.explanation.io.html;

import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owl.model.OWLAntiSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLAxiomAnnotationAxiom;
import org.semanticweb.owl.model.OWLCardinalityRestriction;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLConstantAnnotation;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataComplementOf;
import org.semanticweb.owl.model.OWLDataExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataOneOf;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLDataRangeFacetRestriction;
import org.semanticweb.owl.model.OWLDataRangeRestriction;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataSubPropertyAxiom;
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
import org.semanticweb.owl.model.OWLNaryBooleanDescription;
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
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyAnnotationAxiom;
import org.semanticweb.owl.model.OWLPropertyExpression;
import org.semanticweb.owl.model.OWLQuantifiedRestriction;
import org.semanticweb.owl.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLUnaryPropertyAxiom;
import org.w3c.dom.Element;

import com.clarkparsia.explanation.io.html.utils.DescriptionSorter;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Apr 23, 2007 9:43:26 AM
 *
 * @author Michael Grove <mhgrove@hotmail.com>
 */
public class HTMLManchesterSyntaxObjectRenderer extends HTMLObjectStrikeRenderer {
    protected enum Keyword {
        AND("and", "purple", 3), // either &and; or &cap;
        OR("or", "purple", 3),   // either &or; or &cup;
        ONLY("only", "purple", 3),
        SOME("some", "purple", 3),
        EXACTLY("exactly", "purple", 3),
        NOT("not", "purple", 3),
        OPEN_BRACE("{", "orange", 3),
        CLOSE_BRACE("}", "orange", 3),
        VALUE("value", "purple", 3),
        MIN("min", "purple", 3),
        MAX("max", "purple", 3),
        //SUB_CLASS_OF("&#8838;", "green", 3, "Symbol"),
        //SUB_PROPERTY_OF("&#8838;", "green", 3, "Symbol"),
        //EQUIVALENT_PROPERTIES("&#8801;", "green", 3),
        //EQUIVALENT_CLASSES("&#8801;", "green", 3),
        SUB_CLASS_OF("subClassOf", "green", 3, "Symbol"),
        SUB_PROPERTY_OF("subPropertyOf", "green", 3, "Symbol"),
        EQUIVALENT_PROPERTY("equivalentProperty", "green", 3),
        EQUIVALENT_CLASS("equivalentClass", "green", 3),
        EQUIVALENT_PROPERTIES("EquivalentProperties", "green", 3),
        EQUIVALENT_CLASSES("EquivalentClasses", "green", 3),
        ANNOTATION("annotation", "green", 3),
        LABEL("label", "green", 3),
        COMMENT("comment", "green", 3),
        INVERSE_OF("inverseOf", "green", 3),
        INVERSE("inv", "green", 3),
        NOT_RELATIONSHIP("notObjectRelationship", "green", 3),
        DISJOINT_PROPERTY("propertyDisjointWith", "green", 3),
        DISJOINT_CLASS("disjointWith", "green", 3),
        DISJOINT_PROPERTIES("DisjointProperties", "green", 3),
        DISJOINT_CLASSES("DisjointClasses", "green", 3),
        DISJOINT_UNION("disjointUnionOf", "green", 3),
        SYMMETRIC("symmetric", "green", 3),
        DECLARATION("declaration", "green", 3),
        ONTOLOGY_ANNOTATION("ontologyAnnotation", "green", 3),
        ENTITY_ANNOTATION_AXIOM("entityAnnotationAxiom", "green", 3),
        ANTISYMMETRIC_PROPERTY("AntiSymmetric", "green", 3),
        REFLEXIVE_PROPERTY("Reflexive", "green", 3),
        OBJECT_RELATIONSHIP("ObjectRelationship", "green", 3),
        RANGE("range", "green", 3),
        DOMAIN("domain", "green", 3),
        TYPE("type", "green", 3),
        IRREFLEXIVE("Irreflexive", "green", 3),
        TRANSITIVE("Transitive", "green", 3),
        DIFFERENT_INDIVIDUAL("differentFrom", "green", 3),
        DIFFERENT_INDIVIDUALS("DifferentIndividuals", "green", 3),
        DATA_RELATIONSHIP("DataRelationship", "green", 3),
        INVERSE_FUNCTIONAL("InverseFunctional", "green", 3),
        SAME_INDIVIDUAL("sameAs", "green", 3),
        SAME_INDIVIDUALS("SameIndividual", "green", 3),
        IMPORTS("imports", "green", 3),
        SELF("self", "green", 3),
        FACET_RESTRICTION("facetRestriction", "green", 3),
        DATA_RANGE_RESTRICTION("dataRangeRestriction", "green", 3),
        FUNCTIONAL("Functional", "green", 3);

/*
        public static String FORALL = "<font face='Symbol'><a href='"+owl.getAllValuesFrom()+"' style=\"text-decoration: none\">&#8704;</a></font>";
        public static String EXISTS = "<font face=\"Symbol\"><a href='"+owl.getSomeValuesFrom()+"' style=\"text-decoration: none\">&#8707;</a></font>";
        public static String MEMBEROF = "<font face=\"Verdana\">.</font>";
        public static String GREATEQU = "<font face=\"Symbol\"><a href='"+owl.getMaxCardinality()+"' style=\"text-decoration: none\">&#8805;</a></font>";
        public static String LESSEQU = "<font face=\"Symbol\"><a href='"+owl.getMinCardinality()+"' style=\"text-decoration: none\">&#8804;</a></font>";
        public static String EQU = "<a href='"+owl.getCardinality()+"' style=\"text-decoration: none\">=</a>";
        public static String EQUIVALENT = "<font face=\"Symbol\">&#8801;</font>";
        public static String INTERSECTION = "<font face=\"Symbol\">&#8745;</font>";
        public static String UNION = "<font face=\"Symbol\">&#8746;</font>";
        public static String COMPLEMENT = "&not;&nbsp;";
        public static String SUBSET = "<font face=\"Symbol\">&#8838;</font>";
        public static String SUPERSET = "<font face=\"Symbol\">&#8839;</font>";
        public static String DISJOINT = "<font face=\"Symbol\">&#8800;</font>";

    public static String MIN_INC = "<font face=\"Symbol\"><a href='"+owl.OWL+"minInclusive' style=\"text-decoration: none\">&#8805;</a></font>";
    public static String MIN_EXC = "<font face=\"Symbol\"><a href='"+owl.OWL+"minExclusive' style=\"text-decoration: none\">&gt;</a></font>";
    public static String MAX_INC = "<font face=\"Symbol\"><a href='"+owl.OWL+"maxInclusive' style=\"text-decoration: none\">&#8804;</a></font>";
    public static String MAX_EXC = "<font face=\"Symbol\"><a href='"+owl.OWL+"maxExclusive' style=\"text-decoration: none\">&lt;</a></font>";
 */
        int mSize;
        String mColor;
        String mLabel;
        String mFace;
        String mStyleClass;

        Keyword(String theLabel, String theStyleClass) {
            mLabel = theLabel;
            mStyleClass = theStyleClass;
        }

        Keyword(String theLabel, String theColor, int theSize) {
            this(theLabel, theColor, theSize, null);
        }

        Keyword(String theLabel, String theColor, int theSize, String theFace) {
            mColor = theColor;
            mSize = theSize;
            mLabel = theLabel;
            mFace = theFace;
        }

        String getStyleClass() { return mStyleClass; }
        String getColor() { return mColor; }
        int getSize() { return mSize; }
        String getLabel() { return mLabel; }
        String getFace() { return mFace; }
    }

    protected Element createKeywordElement(Keyword theKeyword) {
        if (theKeyword.getStyleClass() != null)
            return applyFormatting(mElement.getOwnerDocument().createTextNode(theKeyword.getLabel()),
                                   theKeyword.getStyleClass());
        else return createKeywordElement(theKeyword.getLabel(), theKeyword.getColor(), theKeyword.getSize(), null);
    }
    
    /**
     * Render an n-ary axiom with special handling for the binary case.
     * 
     * @param set objects to be rendered
     * @param binary keyword used for binary case
     * @param nary keyword used for n-ary case
     */
    protected void visitNaryAxiom(Set<? extends OWLObject> set, Keyword binary, Keyword nary) {
    	Set<? extends OWLObject> sortedSet = DescriptionSorter.toSortedSet( set );
		Iterator<? extends OWLObject> aIter = sortedSet.iterator();

    	if( set.size() == 2 ) {
    		aIter.next().accept( this );
    		insertSpace();
    		append(createKeywordElement(binary));
    		insertSpace();
    		aIter.next().accept( this );
    	}
    	else {
    		append(createKeywordElement(nary));
    		append("(");
    		aIter.next().accept( this );
    		while (aIter.hasNext()) {
    			insertSpace();
    			aIter.next().accept( this );    				
    		}
    		append(")");
    	}
    }
    
    protected void visitUnaryPropertyAxiom(OWLUnaryPropertyAxiom<?> theAxiom, Keyword keyword) {
    	Element aParent = preVisit();
    	
        append(createKeywordElement(keyword));
        insertSpace();
        append("(");
        theAxiom.getProperty().accept(this);
        append(")");
        
        postVisit(aParent, theAxiom);
    }
    
    protected void visitBooleanDescription(OWLNaryBooleanDescription theDescription, Keyword theKeyword) {
    	Element aParent = preVisit();
    	
        append("(");

        Iterator<OWLDescription> aIter = DescriptionSorter.toSortedSet(theDescription.getOperands()).iterator();
        aIter.next().accept( this );
        
        while (aIter.hasNext()) {
        	insertSpace();
            append(createKeywordElement(theKeyword));
            insertSpace();
            
            aIter.next().accept( this );
        }

        append(")");
        
        postVisit(aParent, theDescription);
    }

    protected void visitQuantifiedRestriction(OWLQuantifiedRestriction<?, ?> theRestriction, Keyword theKeyword) {
    	visitRestriction(theRestriction.getProperty(), theKeyword, theRestriction.getFiller());
    }
    
    protected void visitCardinalityRestriction(OWLCardinalityRestriction<?, ?> theRestriction, Keyword theKeyword) {
    	if (theRestriction.isQualified())
    		visitRestriction(theRestriction.getProperty(), theKeyword, theRestriction.getCardinality(), theRestriction.getFiller());
    	else
    		visitRestriction(theRestriction.getProperty(), theKeyword, theRestriction.getCardinality());
    }
    	
    protected void visitRestriction(OWLPropertyExpression<?, ?> theProperty, Keyword theKeyword, Object... theArgs ) {
        append("(");

        theProperty.accept(this);
        insertSpace();
        append(createKeywordElement(theKeyword));
        insertSpace();
        for (Object aObject : theArgs) {
			if (aObject instanceof OWLObject) {
				((OWLObject) aObject).accept(this);
			}
			else {
				append(aObject.toString());
			}
		}

        append(")");
    	
    }
    
    public void visit(OWLObjectOneOf theDescription) {
        Element aParent = preVisit();

        append(createKeywordElement(Keyword.OPEN_BRACE));
        for (OWLIndividual ind : DescriptionSorter.toSortedSet(theDescription.getIndividuals())) {
            insertSpace();
            ind.accept(this);
        }
        insertSpace();
        append(createKeywordElement(Keyword.CLOSE_BRACE));

        postVisit(aParent, theDescription);
    }

    public void visit(OWLObjectIntersectionOf theDescription) {
        visitBooleanDescription(theDescription, Keyword.AND);
    }

    public void visit(OWLObjectUnionOf theDescription) {
    	visitBooleanDescription(theDescription, Keyword.OR);
    }

    public void visit(OWLObjectComplementOf theDescription) {
        Element aParent = preVisit();

        append("(");
        append(createKeywordElement(Keyword.NOT));
        insertSpace();
        theDescription.getOperand().accept(this);
        append(")");

        postVisit(aParent, theDescription);
    }

    public void visit(OWLObjectSomeRestriction theDescription) {
        Element aParent = preVisit();

        visitQuantifiedRestriction(theDescription, Keyword.SOME);

        postVisit(aParent, theDescription);
    }

    public void visit(OWLObjectAllRestriction theDescription) {
        Element aParent = preVisit();

        visitQuantifiedRestriction(theDescription, Keyword.ONLY);

        postVisit(aParent, theDescription);
    }

    public void visit(OWLObjectValueRestriction theDescription) {
        Element aParent = preVisit();

        visitRestriction(theDescription.getProperty(), Keyword.VALUE, theDescription.getValue());

        postVisit(aParent, theDescription);
    }

    public void visit(OWLObjectMinCardinalityRestriction theDescription) {
        Element aParent = preVisit();

        visitCardinalityRestriction(theDescription, Keyword.MIN);

        postVisit(aParent, theDescription);
    }

    public void visit(OWLObjectExactCardinalityRestriction theDescription) {
        Element aParent = preVisit();

        visitCardinalityRestriction(theDescription, Keyword.EXACTLY);

        postVisit(aParent, theDescription);
    }

    public void visit(OWLObjectMaxCardinalityRestriction theDescription) {
        Element aParent = preVisit();

        visitCardinalityRestriction(theDescription, Keyword.MAX);

        postVisit(aParent, theDescription);
    }

    public void visit(OWLOntology ontology) {
        Element aParent = preVisit();

        append(ontology.getURI().toString());

        postVisit(aParent, ontology);
    }

    public void visit(OWLDataSomeRestriction theDescription) {
        Element aParent = preVisit();

        visitQuantifiedRestriction(theDescription, Keyword.SOME);

        postVisit(aParent, theDescription);
    }

    public void visit(OWLDataAllRestriction theDescription) {
        Element aParent = preVisit();

        visitQuantifiedRestriction(theDescription, Keyword.ONLY);

        postVisit(aParent, theDescription);
    }

    public void visit(OWLDataValueRestriction theDescription) {
        Element aParent = preVisit();

        visitRestriction(theDescription.getProperty(), Keyword.VALUE, theDescription.getValue());

        postVisit(aParent, theDescription);
    }

    public void visit(OWLDataMinCardinalityRestriction theDescription) {
        Element aParent = preVisit();

        visitCardinalityRestriction(theDescription, Keyword.MIN);

        postVisit(aParent, theDescription);
    }

    public void visit(OWLDataExactCardinalityRestriction theDescription) {
        Element aParent = preVisit();

        visitCardinalityRestriction(theDescription, Keyword.EXACTLY);
        
        postVisit(aParent, theDescription);
    }

    public void visit(OWLDataMaxCardinalityRestriction theDescription) {
        Element aParent = preVisit();

        visitCardinalityRestriction(theDescription, Keyword.MAX);

        postVisit(aParent, theDescription);
    }

    public void visit(OWLDataComplementOf theDescription) {
        Element aParent = preVisit();

        append(createKeywordElement(Keyword.NOT));
        insertSpace();
        append("(");
        theDescription.getDataRange().accept(this);
        append(")");

        postVisit(aParent, theDescription);
    }

    public void visit(OWLDataOneOf theDescription) {
        Element aParent = preVisit();

        append(createKeywordElement(Keyword.OPEN_BRACE));
        for (OWLConstant ind : DescriptionSorter.toSortedSet(theDescription.getValues())) {
            insertSpace();
            ind.accept(this);
        }
        insertSpace();
        append(createKeywordElement(Keyword.CLOSE_BRACE));

        postVisit(aParent, theDescription);
    }

    public void visit(OWLDataRangeRestriction theRestriction) {
        Element aParent = preVisit();

        append(createKeywordElement(Keyword.DATA_RANGE_RESTRICTION));
        insertSpace();
        append("(");
        theRestriction.getDataRange().accept(this);
        for (OWLDataRangeFacetRestriction restriction : theRestriction.getFacetRestrictions()) {
            insertSpace();
            restriction.accept(this);
        }
        append(")");

        postVisit(aParent, theRestriction);
    }

    public void visit(OWLDataRangeFacetRestriction theRestriction) {
        Element aParent = preVisit();

        append(createKeywordElement(Keyword.FACET_RESTRICTION));
        insertSpace();
        append("(");
        append(theRestriction.getFacet().toString());
        insertSpace();
        theRestriction.getFacetValue().accept(this);
        append(")");

        postVisit(aParent, theRestriction);
    }

    public void visit(OWLObjectSelfRestriction theRestriction) {
        Element aParent = preVisit();

        visitRestriction(theRestriction.getProperty(), Keyword.SELF);

        postVisit(aParent, theRestriction);
    }

    /////////////////////////////////////////////////
    /////////////////////////////////////////////////
    /////////////////////////////////////////////////

    public void visit(OWLSubClassAxiom theAxiom) {
        Element aParent = preVisit();

        theAxiom.getSubClass().accept(this);
        insertSpace();        
        append(createKeywordElement(Keyword.SUB_CLASS_OF));
        insertSpace();
        theAxiom.getSuperClass().accept(this);

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLNegativeObjectPropertyAssertionAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement(Keyword.NOT_RELATIONSHIP));
        insertSpace();
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
    	visitUnaryPropertyAxiom(theAxiom,Keyword.ANTISYMMETRIC_PROPERTY);
    }

    public void visit(OWLReflexiveObjectPropertyAxiom theAxiom) {
    	visitUnaryPropertyAxiom(theAxiom, Keyword.REFLEXIVE_PROPERTY);    
    }

    public void visit(OWLDisjointClassesAxiom theAxiom) {
        Element aParent = preVisit();

        visitNaryAxiom(theAxiom.getDescriptions(), Keyword.DISJOINT_CLASS, Keyword.DISJOINT_CLASSES);

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLDataPropertyDomainAxiom theAxiom) {
        Element aParent = preVisit();

        theAxiom.getProperty().accept(this);
        insertSpace();
        append(createKeywordElement(Keyword.DOMAIN));
        insertSpace();
        theAxiom.getDomain().accept(this);

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLImportsDeclaration theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement(Keyword.IMPORTS));
        insertSpace();
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

        theAxiom.getProperty().accept(this);
        insertSpace();
        append(createKeywordElement(Keyword.DOMAIN));
        insertSpace();
        theAxiom.getDomain().accept(this);

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLEquivalentObjectPropertiesAxiom theAxiom) {
        Element aParent = preVisit();

        visitNaryAxiom(theAxiom.getProperties(), Keyword.EQUIVALENT_PROPERTY, Keyword.EQUIVALENT_PROPERTIES);

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLNegativeDataPropertyAssertionAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement(Keyword.NOT_RELATIONSHIP));
        insertSpace();
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

        visitNaryAxiom(theAxiom.getIndividuals(), Keyword.DIFFERENT_INDIVIDUAL, Keyword.DIFFERENT_INDIVIDUALS);

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLDisjointDataPropertiesAxiom theAxiom) {
        Element aParent = preVisit();

        visitNaryAxiom(theAxiom.getProperties(), Keyword.DISJOINT_PROPERTY, Keyword.DISJOINT_PROPERTIES);

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLDisjointObjectPropertiesAxiom theAxiom) {
        Element aParent = preVisit();

        visitNaryAxiom(theAxiom.getProperties(), Keyword.DISJOINT_PROPERTY, Keyword.DISJOINT_PROPERTIES);

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLObjectPropertyRangeAxiom theAxiom) {
        Element aParent = preVisit();

        theAxiom.getProperty().accept(this);
        insertSpace();
        append(createKeywordElement(Keyword.RANGE));
        insertSpace();
        theAxiom.getRange().accept(this);

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLObjectPropertyAssertionAxiom theAxiom) {
        Element aParent = preVisit();

        theAxiom.getSubject().accept(this);
        insertSpace();
        theAxiom.getProperty().accept(this);
        insertSpace();
        theAxiom.getObject().accept(this);

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLFunctionalObjectPropertyAxiom theAxiom) {
    	visitUnaryPropertyAxiom(theAxiom, Keyword.FUNCTIONAL);
    }

    public void visit(OWLObjectSubPropertyAxiom theAxiom) {
        Element aParent = preVisit();

        theAxiom.getSubProperty().accept(this);
        insertSpace();
        append(createKeywordElement(Keyword.SUB_PROPERTY_OF));
        insertSpace();
        theAxiom.getSuperProperty().accept(this);

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLDisjointUnionAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement(Keyword.DISJOINT_UNION));
        insertSpace();
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

        append(createKeywordElement(Keyword.DECLARATION));
        insertSpace();
        append("(");
        theAxiom.getEntity().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLEntityAnnotationAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement(Keyword.ENTITY_ANNOTATION_AXIOM));
        insertSpace();
        append("(");
        theAxiom.getSubject().accept(this);
        insertSpace();
        theAxiom.getAnnotation().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLOntologyAnnotationAxiom theAxiom) {
        Element aParent = preVisit();

        append(createKeywordElement(Keyword.ONTOLOGY_ANNOTATION));
        insertSpace();
        append("(");
        theAxiom.getAnnotation().accept(this);
        append(")");

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLSymmetricObjectPropertyAxiom theAxiom) {
    	visitUnaryPropertyAxiom(theAxiom, Keyword.SYMMETRIC);
    }

    public void visit(OWLDataPropertyRangeAxiom theAxiom) {
        Element aParent = preVisit();

        theAxiom.getProperty().accept(this);
        insertSpace();
        append(createKeywordElement(Keyword.RANGE));
        insertSpace();
        theAxiom.getRange().accept(this);

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLFunctionalDataPropertyAxiom theAxiom) {
    	visitUnaryPropertyAxiom(theAxiom,Keyword.FUNCTIONAL);
    }

    public void visit(OWLEquivalentDataPropertiesAxiom theAxiom) {
        Element aParent = preVisit();

        visitNaryAxiom(theAxiom.getProperties(), Keyword.EQUIVALENT_PROPERTY, Keyword.EQUIVALENT_PROPERTIES);

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLClassAssertionAxiom theAxiom) {
        Element aParent = preVisit();

        theAxiom.getIndividual().accept(this);
        insertSpace();
        append(createKeywordElement(Keyword.TYPE));
        insertSpace();
        theAxiom.getDescription().accept(this);

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLEquivalentClassesAxiom theAxiom) {
        Element aParent = preVisit();

        visitNaryAxiom(theAxiom.getDescriptions(), Keyword.EQUIVALENT_CLASS, Keyword.EQUIVALENT_CLASSES);

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLDataPropertyAssertionAxiom theAxiom) {
        Element aParent = preVisit();

        theAxiom.getSubject().accept(this);
        insertSpace();
        theAxiom.getProperty().accept(this);
        insertSpace();
        theAxiom.getObject().accept(this);

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLTransitiveObjectPropertyAxiom theAxiom) {
        visitUnaryPropertyAxiom(theAxiom, Keyword.TRANSITIVE);
    }

    public void visit(OWLIrreflexiveObjectPropertyAxiom theAxiom) {
    	visitUnaryPropertyAxiom(theAxiom, Keyword.IRREFLEXIVE);
    }

    public void visit(OWLDataSubPropertyAxiom theAxiom) {
        Element aParent = preVisit();

        theAxiom.getSubProperty().accept(this);
        insertSpace();
        append(createKeywordElement(Keyword.SUB_PROPERTY_OF));
        insertSpace();
        theAxiom.getSuperProperty().accept(this);

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLInverseFunctionalObjectPropertyAxiom theAxiom) {
    	visitUnaryPropertyAxiom(theAxiom, Keyword.INVERSE_FUNCTIONAL);
    }

    public void visit(OWLSameIndividualsAxiom theAxiom) {
        Element aParent = preVisit();

        visitNaryAxiom(theAxiom.getIndividuals(), Keyword.SAME_INDIVIDUAL, Keyword.SAME_INDIVIDUALS);

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLObjectPropertyChainSubPropertyAxiom theAxiom) {
        Element aParent = preVisit();

        append("[");
        for (OWLObjectPropertyExpression prop : theAxiom.getPropertyChain()) {
            insertSpace();
            prop.accept(this);
        }
        append(" ]");
        insertSpace();
        append(createKeywordElement(Keyword.SUB_PROPERTY_OF));
        insertSpace();        
        theAxiom.getSuperProperty().accept(this);

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLInverseObjectPropertiesAxiom theAxiom) {
        Element aParent = preVisit();

        theAxiom.getFirstProperty().accept(this);
    	insertSpace();
    	append(createKeywordElement(Keyword.INVERSE_OF));
        insertSpace();
        theAxiom.getSecondProperty().accept(this);

        postVisit(aParent, theAxiom);
    }

    public void visit(OWLObjectPropertyInverse theInverse) {
        Element aParent = preVisit();

        append(createKeywordElement(Keyword.INVERSE));
        append("(");
        theInverse.getInverse().accept(this);
        append(")");

        postVisit(aParent, theInverse);
    }

    public void visit(OWLObjectAnnotation theAnnotation) {
        Element aParent = preVisit();

        append(createKeywordElement(Keyword.ANNOTATION));
        insertSpace();
        append("(");
        append(createLink(theAnnotation.getAnnotationURI()));
        insertSpace();
        theAnnotation.getAnnotationValue().accept(this);
        append(")");

        postVisit(aParent, theAnnotation);
    }

    public void visit(OWLConstantAnnotation theAnnotation) {
        Element aParent = preVisit();

        if (theAnnotation.isLabel()) {
            append(createKeywordElement(Keyword.LABEL));
            insertSpace();
            append("(");
        }
        else if (theAnnotation.isComment()) {
            append(createKeywordElement(Keyword.COMMENT));
            insertSpace();
            append("(");
        }
        else {
            append(createKeywordElement(Keyword.ANNOTATION));
            insertSpace();
            append("(");
            append(createLink(theAnnotation.getAnnotationURI()));
        }
        append(" ");
        theAnnotation.getAnnotationValue().accept(this);
        append(")");

        postVisit(aParent, theAnnotation);
    }
}
