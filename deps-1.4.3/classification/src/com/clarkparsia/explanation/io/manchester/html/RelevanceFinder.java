package com.clarkparsia.explanation.io.manchester.html;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLCardinalityRestriction;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataComplementOf;
import org.semanticweb.owl.model.OWLDataExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataOneOf;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataRangeFacetRestriction;
import org.semanticweb.owl.model.OWLDataRangeRestriction;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLNaryBooleanDescription;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectOneOf;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyInverse;
import org.semanticweb.owl.model.OWLObjectSelfRestriction;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLObjectVisitor;
import org.semanticweb.owl.model.OWLPropertyExpression;
import org.semanticweb.owl.model.OWLQuantifiedRestriction;
import org.semanticweb.owl.model.OWLTypedConstant;
import org.semanticweb.owl.model.OWLUntypedConstant;
import org.semanticweb.owl.util.OWLEntityCollector;
import org.semanticweb.owl.util.OWLObjectVisitorAdapter;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Evren Sirin
 */
public class RelevanceFinder extends OWLObjectVisitorAdapter implements OWLObjectVisitor {
	public static enum Relevance {
		RELEVANT, PARTLY_RELEVANT, IRRELEVANT
	}

	private Map<OWLEntity, Integer>	occurrenceCount;

	private Relevance				relevance;
	
	private Set<OWLObject>			irrelevantObjects;
	
	private OWLEntityCollector 		entityCounter = new OWLEntityCollector() {
		@Override
		public void visit(OWLClass desc) {
			incrementCount( desc );
		}

		@Override
		public void visit(OWLDataProperty property) {
			incrementCount( property );
		}

		@Override
		public void visit(OWLDataType dataType) {
			incrementCount( dataType );
		}

		@Override
		public void visit(OWLIndividual individual) {
			incrementCount( individual );
		}

		@Override
		public void visit(OWLObjectProperty property) {
			incrementCount( property );
		}			
	};

	public RelevanceFinder(OWLAxiom axiom, Set<OWLAxiom> explanation) {
		occurrenceCount = new HashMap<OWLEntity,Integer>();
		
		countOccurrences( axiom, explanation );
	}
	
	public boolean isRelevant(OWLObject object) {
		reset();
		
		checkRelevance( object );
		
		return !irrelevantObjects.contains( object );
	}
	
	private void incrementCount(OWLEntity e) {
		Integer count = occurrenceCount.get( e );
		if( count == null )
			occurrenceCount.put( e, 1 );
		else
			occurrenceCount.put( e, count + 1 );
	}
	
	private void countOccurrences(OWLAxiom axiom, Set<OWLAxiom> explanation) {
		entityCounter.reset();
		axiom.accept( entityCounter );		
		
		for( OWLAxiom a : explanation ) {
			a.accept( entityCounter );			
		}
	}
	
	private void checkRelevance(OWLObject o) {
		relevance = null;
		
		o.accept( this );
		
		if( relevance == Relevance.IRRELEVANT )
			irrelevantObjects.add( o );
	}

	public void reset() {
		relevance = null;
		irrelevantObjects = new HashSet<OWLObject>();
	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(OWLClass c) {
		visitEntity( c );
	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(OWLDataProperty p) {
		visitEntity( p );
	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(OWLDataType d) {
		visitEntity( d );
	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(OWLIndividual i) {
		visitEntity( i );
	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(OWLObjectProperty p) {
		visitEntity( p );
	}

	private void visitEntity(OWLEntity e) {
		Integer count = occurrenceCount.get( e );
		if( count != null && count > 1 )
			relevance = Relevance.RELEVANT;
		else
			relevance = Relevance.IRRELEVANT;
	}

	public void visit(OWLDataAllRestriction theDescription) {
		visitQuantifiedRestriction( theDescription );
	}

	public void visit(OWLDataComplementOf theDescription) {
		checkRelevance( theDescription.getDataRange() );
	}

	public void visit(OWLDataExactCardinalityRestriction theDescription) {
		visitCardinalityRestriction( theDescription );
	}

	public void visit(OWLDataMaxCardinalityRestriction theDescription) {
		visitCardinalityRestriction( theDescription );
	}

	public void visit(OWLDataMinCardinalityRestriction theDescription) {
		visitCardinalityRestriction( theDescription );
	}

	public void visit(OWLDataOneOf theDescription) {
		relevance = Relevance.RELEVANT;
	}

	public void visit(OWLDataRangeFacetRestriction theRestriction) {
		relevance = Relevance.RELEVANT;
	}

	public void visit(OWLDataRangeRestriction theRestriction) {
		relevance = Relevance.RELEVANT;
	}

	public void visit(OWLDataSomeRestriction theDescription) {
		visitQuantifiedRestriction( theDescription );
	}

	public void visit(OWLDataValueRestriction theDescription) {
		visitRestriction( theDescription.getProperty(), theDescription.getValue() );
	}

	public void visit(OWLObjectAllRestriction theDescription) {
		visitQuantifiedRestriction( theDescription );
	}

	public void visit(OWLObjectComplementOf theDescription) {
		theDescription.getOperand().accept( this );
	}

	public void visit(OWLObjectExactCardinalityRestriction theDescription) {
		visitCardinalityRestriction( theDescription );
	}

	public void visit(OWLObjectIntersectionOf theDescription) {
		visitBooleanDescription( theDescription );
	}

	public void visit(OWLObjectMaxCardinalityRestriction theDescription) {
		visitCardinalityRestriction( theDescription );
	}

	public void visit(OWLObjectMinCardinalityRestriction theDescription) {
		visitCardinalityRestriction( theDescription );
	}

	public void visit(OWLObjectOneOf theDescription) {
		boolean allRelevant = true;
		boolean allIrrelevant = true;

		for( OWLIndividual aIndividual : theDescription.getIndividuals() ) {
			checkRelevance( aIndividual );
			switch ( relevance ) {
			case RELEVANT:
				allIrrelevant = false;
				break;
			case IRRELEVANT:
				allRelevant = false;
				break;
			case PARTLY_RELEVANT:
				allRelevant = false;
				allIrrelevant = false;
			}
		}

		if( allRelevant )
			relevance = Relevance.RELEVANT;
		else if( allIrrelevant )
			relevance = Relevance.IRRELEVANT;
		else
			relevance = Relevance.PARTLY_RELEVANT;
	}

	public void visit(OWLObjectPropertyInverse theInverse) {
		theInverse.getInverse().accept( this );
	}

	public void visit(OWLObjectSelfRestriction theRestriction) {

		visitRestriction( theRestriction.getProperty(), null );

	}

	public void visit(OWLObjectSomeRestriction theDescription) {
		visitQuantifiedRestriction( theDescription );
	}

	public void visit(OWLObjectUnionOf theDescription) {
		visitBooleanDescription( theDescription );
	}

	public void visit(OWLObjectValueRestriction theDescription) {
		visitRestriction( theDescription.getProperty(), theDescription.getValue() );
	}

	public void visit(OWLTypedConstant node) {
		relevance = Relevance.RELEVANT;
	}

	public void visit(OWLUntypedConstant node) {
		relevance = Relevance.RELEVANT;
	}

	protected void visitBooleanDescription(OWLNaryBooleanDescription theDescription) {
		boolean allRelevant = true;
		boolean allIrrelevant = true;

		for( OWLDescription aDescription : theDescription.getOperands() ) {
			checkRelevance( aDescription );
			switch ( relevance ) {
			case RELEVANT:
				allIrrelevant = false;
				break;
			case IRRELEVANT:
				allRelevant = false;
				break;
			case PARTLY_RELEVANT:
				allRelevant = false;
				allIrrelevant = false;
			}
		}

		if( allRelevant )
			relevance = Relevance.RELEVANT;
		else if( allIrrelevant )
			relevance = Relevance.IRRELEVANT;
		else
			relevance = Relevance.PARTLY_RELEVANT;
	}

	protected void visitCardinalityRestriction(OWLCardinalityRestriction<?, ?> theRestriction) {
		if( theRestriction.isQualified() )
			visitRestriction( theRestriction.getProperty(), theRestriction.getFiller() );
		else
			visitRestriction( theRestriction.getProperty(), null );
	}

	protected void visitQuantifiedRestriction(OWLQuantifiedRestriction<?, ?> theRestriction) {
		visitRestriction( theRestriction.getProperty(), theRestriction.getFiller() );
	}

	protected void visitRestriction(OWLPropertyExpression<?, ?> theProperty, OWLObject theArg) {
		checkRelevance( theProperty );

		if( relevance == Relevance.RELEVANT && theArg != null ) {
			checkRelevance( theArg );
			
			relevance = Relevance.RELEVANT;
		}
	}
}
