package com.clarkparsia.protege.change.listener;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;

import com.clarkparsia.protege.change.ChangeList;
import com.clarkparsia.protege.change.ChangeLog;
import com.clarkparsia.protege.change.Converter;
import com.clarkparsia.protege.exceptions.ConversionException;
import com.clarkparsia.protege.exceptions.IncompleteInputException;
import com.clarkparsia.protege.exceptions.UnexpectedTypeException;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLAnonymousClass;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLDataRange;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNAryLogicalClass;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLNames;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.OWLProperty;
import edu.stanford.smi.protegex.owl.model.OWLRestriction;
import edu.stanford.smi.protegex.owl.model.RDFList;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSDatatype;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.model.event.PropertyValueAdapter;
import edu.stanford.smi.protegex.owl.model.triplestore.Tuple;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Sept 18, 2007
 *
 * @author Mike Smith <msmith@clarkparsia.com>
 */
public class ProtegeChangePropertyValueListener extends PropertyValueAdapter {
    private final static Logger LOGGER = Log.getLogger(ProtegeChangePropertyValueListener.class);
    
	/**
	 * Used to track changes in the current and parent transactions since the
	 * previous commit
	 */
	private final Map<RDFResource,Set<RDFProperty>> mChangesSinceCommit;
	private int transactionDepth;	
    private final boolean mIgnoreMultipleChanges;
    
    private final Converter mConverter;
    private final ChangeLog mChangeLog;
    private final OWLModel mModel;
    
    private final OWLNamedClass mOwlDataProperty;
    private final OWLNamedClass mOwlObjectProperty;

    private final RDFSNamedClass mOwlFunctionalProperty;
    private final RDFSNamedClass mOwlInverseFunctionalProperty;
    private final RDFSNamedClass mOwlSymmetricProperty;
    private final RDFSNamedClass mOwlTransitiveProperty;
    
    private final RDFProperty mRdfFirst;
    private final RDFProperty mRdfRest;
    private final RDFProperty mRdfType;
    private final RDFProperty mRdfsRange;
    private final RDFProperty mRdfsSubClassOf;
    private final RDFProperty mOwlDisjointWith;
    private final RDFProperty mOwlEquivalentClass;
    private final RDFProperty mOwlIntersectionOf;
    private final RDFProperty mOwlUnionOf;
    private final RDFProperty mOwlSomeValues;
    private final RDFProperty mOwlAllValues;
    private final RDFProperty mOwlOnProperty;
    
    private final Map<RDFProperty,Class<?>> mPropertiesToTrack;

    public ProtegeChangePropertyValueListener(Converter theConverter, ChangeLog theLog, OWLModel theModel) {
        mConverter = theConverter;
        mChangeLog = theLog;
        mModel = theModel;

        mOwlDataProperty = mModel.getOWLDatatypePropertyClass();
        mOwlObjectProperty = mModel.getOWLObjectPropertyClass();

        mOwlFunctionalProperty = mModel.getOWLFunctionalPropertyClass();
        mOwlInverseFunctionalProperty = mModel.getOWLInverseFunctionalPropertyClass();
        mOwlSymmetricProperty = mModel.getRDFSNamedClass(OWLNames.Cls.SYMMETRIC_PROPERTY);
        mOwlTransitiveProperty = mModel.getRDFSNamedClass(OWLNames.Cls.TRANSITIVE_PROPERTY);
        
        mRdfFirst = mModel.getRDFFirstProperty();
        mRdfRest = mModel.getRDFRestProperty();
        mRdfType = mModel.getRDFTypeProperty();
        mRdfsSubClassOf = mModel.getRDFSSubClassOfProperty();
        mRdfsRange = mModel.getRDFSRangeProperty();
        mOwlDisjointWith = mModel.getOWLDisjointWithProperty();
        mOwlEquivalentClass = mModel.getOWLEquivalentClassProperty();
        mOwlIntersectionOf = mModel.getOWLIntersectionOfProperty();
        mOwlUnionOf = mModel.getOWLUnionOfProperty();
        mOwlSomeValues = mModel.getRDFProperty(OWLNames.Slot.SOME_VALUES_FROM);
        mOwlAllValues = mModel.getRDFProperty(OWLNames.Slot.ALL_VALUES_FROM);
        mOwlOnProperty = mModel.getRDFProperty(OWLNames.Slot.ON_PROPERTY);
        
        mPropertiesToTrack = new HashMap<RDFProperty, Class<?>>();
        mPropertiesToTrack.put(mOwlEquivalentClass, RDFSClass.class);
        mPropertiesToTrack.put(mOwlDisjointWith, RDFSClass.class);
        mPropertiesToTrack.put(mRdfsSubClassOf, RDFSClass.class);
        mPropertiesToTrack.put(mOwlIntersectionOf, RDFSClass.class);
        mPropertiesToTrack.put(mOwlUnionOf, RDFSClass.class);
        mPropertiesToTrack.put(mOwlSomeValues, RDFSClass.class);
        mPropertiesToTrack.put(mOwlAllValues, RDFSClass.class);
        
        mPropertiesToTrack.put(mRdfsRange, RDFProperty.class);
        mPropertiesToTrack.put(mRdfType, RDFProperty.class);
        
        mPropertiesToTrack.put(mRdfFirst, RDFList.class);
        mPropertiesToTrack.put(mRdfRest, RDFList.class);
        
        mChangesSinceCommit = new HashMap<RDFResource,Set<RDFProperty>>();
        transactionDepth = 0;

        // Prior to 1.4 flag was true for database projects
        mIgnoreMultipleChanges = false;
    }

	@Override
	public void browserTextChanged(RDFResource theResource) {
		LOGGER.finer("PropertyValueListener: browser text changed");
	}

	@Override
	public void nameChanged(RDFResource theResource, String theOldName) {
		LOGGER.fine("PropertyValueListener: name changed");
		if (mIgnoreMultipleChanges && mChangeLog.isNewResource(theResource))
			return;
		
		// Strategic decision to let any conversion problems pop all the way to
		// this level. If 30 axioms are queued, and 1 fails none will be
		// applied. This is *more* likely to cause the problem to be noted and
		// reported, instead of ignored.
		try {
			ChangeList aList = buildChangedAxioms(theResource, theOldName);
			mChangeLog.queueChanges(aList);
		} catch( ConversionException e ) {
			LOGGER
					.log(
							Level.SEVERE,
							"Error affecting reasoner synchronization: failure attempting to queue rename changes for axiom for resource previously named '"
									+ theOldName + "'", e);
		}
    }

    private ChangeList buildIndividualChangedAxioms(OWLIndividual theProtegeInd, URI theOldURI) throws ConversionException {
        OWLDataFactory aFactory = mConverter.getFactory();

        ChangeList aChanges = new ChangeList();

        org.semanticweb.owl.model.OWLIndividual aOldOWLInd = aFactory.getOWLIndividual(theOldURI);

        Collection aDifferent = theProtegeInd.getDifferentFrom();
        if (aDifferent.size() > 0) {
            aChanges.axiomAdded(mConverter.createDifferentFromAxiom(theProtegeInd, aDifferent));
            aChanges.axiomRemoved(aFactory.getOWLDifferentIndividualsAxiom(createIndividualSet(aDifferent, aOldOWLInd)));
        }

        Collection aSames = theProtegeInd.getSameAs();
        if (aSames.size() > 0) {
            aChanges.axiomAdded(mConverter.createSameIndividualsAxiom(theProtegeInd, aSames));
            aChanges.axiomRemoved(aFactory.getOWLSameIndividualsAxiom(createIndividualSet(aSames, aOldOWLInd)));
        }

        for (Object aType : theProtegeInd.getRDFTypes()) {
            aChanges.axiomAdded(mConverter.createClassAssertionAxiom(theProtegeInd, (RDFSClass) aType));
            aChanges.axiomRemoved(aFactory.getOWLClassAssertionAxiom(aOldOWLInd, mConverter.convertClass( (RDFSClass) aType)));
        }

        return aChanges;
    }

    private ChangeList buildClassChangedAxioms(RDFSClass theProtegeClass, URI theOldURI) throws ConversionException {
        OWLDataFactory aFactory = mConverter.getFactory();

        ChangeList aChanges = new ChangeList();

        org.semanticweb.owl.model.OWLClass aOldOWLClass = aFactory.getOWLClass(theOldURI);

		aChanges.axiomAdded(mConverter.createDeclarationAxiom(theProtegeClass));
        aChanges.axiomRemoved(aFactory.getOWLDeclarationAxiom(aOldOWLClass));
        
        // TODO: what about depending classes?
        // TODO: same as?

        Collection aEquivs = theProtegeClass.getEquivalentClasses();
        if (aEquivs.size() > 0) {	
            aChanges.axiomAdded(aFactory.getOWLEquivalentClassesAxiom(createDescriptionSet(aEquivs, theProtegeClass)));
            aChanges.axiomRemoved(aFactory.getOWLEquivalentClassesAxiom(createDescriptionSet(aEquivs, aOldOWLClass)));
        }

        Collection aSupers = theProtegeClass.getSuperclasses(false);
        if (aSupers.size() > 0) {
            for (Object aObj : aSupers) {
                RDFSClass aSuper = (RDFSClass) aObj;
                aChanges.axiomAdded(mConverter.createSubClassAxiom(theProtegeClass, aSuper));
                aChanges.axiomRemoved(aFactory.getOWLSubClassAxiom(aOldOWLClass,
                                                                   mConverter.convertClass(aSuper)));
            }
        }
        
        if (theProtegeClass instanceof OWLClass) {
            OWLClass aProtegeOWLClass = (OWLClass) theProtegeClass;

            Collection aDisjoints = aProtegeOWLClass.getDisjointClasses();
            if (aDisjoints.size() > 0) {
                aChanges.axiomAdded(mConverter.createDisjointClassesAxiom(aProtegeOWLClass, aDisjoints));
                aChanges.axiomRemoved(aFactory.getOWLDisjointClassesAxiom(createDescriptionSet(aDisjoints, aOldOWLClass)));
            }
        }

        return aChanges;
    }

    private ChangeList buildPropertyChangedAxioms(RDFProperty theProtegeProp, URI theOldURI)
			throws ConversionException {

		boolean aObjPropBool;
		OWLDataFactory aFactory = mConverter.getFactory();

		ChangeList aChanges = new ChangeList();

		org.semanticweb.owl.model.OWLObjectProperty aOldAsObjP = null;
		org.semanticweb.owl.model.OWLDataProperty aOldAsDataP = null;

		if (theProtegeProp instanceof OWLObjectProperty) {
			aOldAsObjP = aFactory.getOWLObjectProperty(theOldURI);
			aObjPropBool = true;
			aChanges.axiomRemoved(aFactory.getOWLDeclarationAxiom(aOldAsObjP));
		}
		else if (theProtegeProp instanceof OWLDatatypeProperty) {
			aOldAsDataP = aFactory.getOWLDataProperty(theOldURI);
			aObjPropBool = false;
			aChanges.axiomRemoved(aFactory.getOWLDeclarationAxiom(aOldAsDataP));
		}
		else throw new UnexpectedTypeException(RDFProperty.class, theProtegeProp);

		aChanges.axiomAdded(mConverter.createDeclarationAxiom(theProtegeProp));

		Collection aSupers = theProtegeProp.getSuperproperties(false);
		if (aSupers.size() > 0) {
			for (Object aObj : aSupers) {
				RDFProperty aSuper = (RDFProperty) aObj;

				aChanges.axiomAdded(mConverter.createSubPropertyAxiom(theProtegeProp, aSuper));
				aChanges.axiomRemoved(aObjPropBool ? aFactory.getOWLSubObjectPropertyAxiom(
						aOldAsObjP,
						(org.semanticweb.owl.model.OWLObjectPropertyExpression) mConverter
								.convertProperty(aSuper)) : aFactory.getOWLSubDataPropertyAxiom(
						aOldAsDataP,
						(org.semanticweb.owl.model.OWLDataPropertyExpression) mConverter
								.convertProperty(aSuper)));
			}
		}

		Collection aDomains = theProtegeProp.getDomains(false);
		if (aDomains.size() > 0) {
			for (Object aObj : aDomains) {
				RDFSClass aDomain = (RDFSClass) aObj;

				if (!aDomain.equals(mModel.getOWLThingClass())) {
					aChanges.axiomAdded(mConverter.createDomainAxiom(aDomain, theProtegeProp));
					aChanges.axiomRemoved(aObjPropBool ? aFactory.getOWLObjectPropertyDomainAxiom(
							aOldAsObjP, mConverter.convertClass(aDomain)) : aFactory
							.getOWLDataPropertyDomainAxiom(aOldAsDataP, mConverter
									.convertClass(aDomain)));
				}
			}
		}

		Collection aRanges = theProtegeProp.getRanges(false);
		if (aRanges.size() > 0) {
			for (Object aObj : aRanges) {
				OWLAxiom aAddedAxiom, aRemovedAxiom;

				if (aObjPropBool) {
					RDFSClass aRange = (RDFSClass) aObj;
					aAddedAxiom = mConverter.createObjectPropertyRangeAxiom(theProtegeProp, aRange);
					aRemovedAxiom = aFactory.getOWLObjectPropertyRangeAxiom(aOldAsObjP, mConverter
							.convertClass(aRange));
				}
				else {
					RDFResource aRange = (RDFResource) aObj;
					aAddedAxiom = mConverter.createDataPropertyRangeAxiom(theProtegeProp, aRange);
					if (aRange instanceof OWLDataRange)
						aRemovedAxiom = aFactory.getOWLDataPropertyRangeAxiom(aOldAsDataP,
								mConverter.convertOWLDataRange((OWLDataRange) aRange));
					else if (aRange instanceof RDFSDatatype)
						aRemovedAxiom = aFactory.getOWLDataPropertyRangeAxiom(aOldAsDataP, aFactory
								.getOWLDataType(URI.create(aRange.getURI())));
					else throw new UnexpectedTypeException(RDFResource.class, aRange);
				}

				aChanges.axiomAdded(aAddedAxiom);
				aChanges.axiomRemoved(aRemovedAxiom);
			}
		}

		Collection aEquivs = theProtegeProp.getEquivalentProperties();
		if (aEquivs.size() > 0) {
			for (Object aObj : aEquivs) {
				RDFProperty aEquivProp = (RDFProperty) aObj;

				aChanges.axiomAdded(mConverter.createEquivalentPropertiesAxiom(theProtegeProp,
						aEquivProp));

				OWLAxiom aRemovedAxiom = null;
				if (aObjPropBool) {
					Set<org.semanticweb.owl.model.OWLObjectPropertyExpression> aSet = new HashSet<org.semanticweb.owl.model.OWLObjectPropertyExpression>();
					aSet.add((org.semanticweb.owl.model.OWLObjectPropertyExpression) mConverter
							.convertProperty(aEquivProp));
					aSet.add(aOldAsObjP);

					aRemovedAxiom = aFactory.getOWLEquivalentObjectPropertiesAxiom(aSet);
				}
				else {
					Set<org.semanticweb.owl.model.OWLDataPropertyExpression> aSet = new HashSet<org.semanticweb.owl.model.OWLDataPropertyExpression>();
					aSet.add((org.semanticweb.owl.model.OWLDataPropertyExpression) mConverter
							.convertProperty(aEquivProp));
					aSet.add(aOldAsDataP);

					aRemovedAxiom = aFactory.getOWLEquivalentDataPropertiesAxiom(aSet);
				}

				if (aRemovedAxiom != null) aChanges.axiomRemoved(aRemovedAxiom);
			}
		}

		if (theProtegeProp.isFunctional()) {
			if (aObjPropBool) {
				aChanges
						.axiomAdded(aFactory
								.getOWLFunctionalObjectPropertyAxiom((org.semanticweb.owl.model.OWLObjectPropertyExpression) mConverter
										.convertProperty(theProtegeProp)));
				aChanges.axiomRemoved(aFactory.getOWLFunctionalObjectPropertyAxiom(aOldAsObjP));
			}
			else {
				aChanges
						.axiomAdded(aFactory
								.getOWLFunctionalDataPropertyAxiom((org.semanticweb.owl.model.OWLDataPropertyExpression) mConverter
										.convertProperty(theProtegeProp)));
				aChanges.axiomRemoved(aFactory.getOWLFunctionalDataPropertyAxiom(aOldAsDataP));
			}
		}

		if (theProtegeProp instanceof OWLProperty) {
			OWLProperty aProtegeOWLProp = (OWLProperty) theProtegeProp;

			if (aProtegeOWLProp.isInverseFunctional()) {
				aChanges
						.axiomAdded(aFactory
								.getOWLInverseFunctionalObjectPropertyAxiom((org.semanticweb.owl.model.OWLObjectPropertyExpression) mConverter
										.convertProperty(aProtegeOWLProp)));
				aChanges.axiomRemoved(aFactory
						.getOWLInverseFunctionalObjectPropertyAxiom(aOldAsObjP));
			}

			if (aProtegeOWLProp.isObjectProperty()) {
				final OWLObjectProperty aProtegeOWLObjP = (OWLObjectProperty) aProtegeOWLProp;
				if (aProtegeOWLObjP.isSymmetric()) {
					aChanges
							.axiomAdded(aFactory
									.getOWLSymmetricObjectPropertyAxiom((org.semanticweb.owl.model.OWLObjectPropertyExpression) mConverter
											.convertProperty(aProtegeOWLProp)));
					aChanges.axiomRemoved(aFactory.getOWLSymmetricObjectPropertyAxiom(aOldAsObjP));
				}

				if (aProtegeOWLObjP.isTransitive()) {
					aChanges
							.axiomAdded(aFactory
									.getOWLTransitiveObjectPropertyAxiom((org.semanticweb.owl.model.OWLObjectPropertyExpression) mConverter
											.convertProperty(aProtegeOWLProp)));
					aChanges.axiomRemoved(aFactory.getOWLTransitiveObjectPropertyAxiom(aOldAsObjP));
				}
			}
		}

		// TODO: Handle name changes for all property value assertions
		// presumably this is where this property is used for instance data
		/*
		 * Iterator aSubjIter =
		 * theProtegeProp.getOWLModel().listSubjects(theProtegeProp); while
		 * (aSubjIter.hasNext()) { Object aSubj = aSubjIter.next();
		 *
		 */

		return aChanges;
	}

    private ChangeList buildChangedAxioms(RDFResource theResource, String theOldName) throws ConversionException {

    	final String aString = theResource.getOWLModel().getURIForResourceName(theOldName);
		if (aString == null)
			throw new IncompleteInputException("Unable to get old URI for renamed resource");

		URI aOldURI;
		try {
			aOldURI = URI.create(aString);
		} catch( IllegalArgumentException e ) {
			throw new ConversionException(e);
		}

		// TODO: Ignore changes to annotation properties

		ChangeList aChanges = new ChangeList();

		if (theResource instanceof OWLIndividual) {
			aChanges.add(buildIndividualChangedAxioms((OWLIndividual) theResource, aOldURI));
		}
		else if (theResource instanceof RDFSClass) {
			aChanges.add(buildClassChangedAxioms((RDFSClass) theResource, aOldURI));
		}
		else if (theResource instanceof RDFProperty) {
			aChanges.add(buildPropertyChangedAxioms((RDFProperty) theResource, aOldURI));
		}
		else throw new UnexpectedTypeException(RDFResource.class, theResource);

        return aChanges;
    }

    private Set<OWLDescription> createDescriptionSet(Collection theList, RDFSClass theClass)
			throws ConversionException {
		
    	Set<OWLDescription> aSet = mConverter.convertListOfClasses(theList);
		if (theClass != null) aSet.add(mConverter.convertClass(theClass));

		return aSet;
	}

    private Set<OWLDescription> createDescriptionSet(Collection theList, OWLDescription theClass) throws ConversionException {

		Set<OWLDescription> aSet = mConverter.convertListOfClasses(theList);
		if (theClass != null) aSet.add(theClass);

		return aSet;
	}

    private Set<org.semanticweb.owl.model.OWLIndividual> createIndividualSet(Collection theList,
			org.semanticweb.owl.model.OWLIndividual theInd) throws ConversionException {

    	Set<org.semanticweb.owl.model.OWLIndividual> aSet = mConverter
				.convertListOfIndividuals(theList);

		if (theInd != null) aSet.add(theInd);

		return aSet;
	}

    @Override
	public void propertyValueChanged(RDFResource theResource, RDFProperty theProperty,
			Collection theOldValues) {
		if (mIgnoreMultipleChanges && changedThisTransaction(theResource, theProperty)) {
			LOGGER
					.fine("PropertyValueListener: property value change already seen in this transaction, skipping");
			return;
		}
		
		Class<?> expectedResourceType = mPropertiesToTrack.get(theProperty);
		
		if (expectedResourceType == null) {		
	    	if (LOGGER.isLoggable(Level.FINER))
	    		LOGGER.finer("PropertyValueListener: ignoring irrelevant property value changed " + theResource + ":" + theProperty + ":" + theOldValues );
	    	return;
		}
		

		if (!expectedResourceType.isInstance(theResource)) {		
	    	if (LOGGER.isLoggable(Level.FINE))
	    		LOGGER.fine("PropertyValueListener: ignoring property value change with unexpected type " + theResource + ":" + theProperty + ":" + theOldValues );
	    	return;
		}
    	
		if (LOGGER.isLoggable(Level.FINE))
    		LOGGER.fine("PropertyValueListener: process property value change " + theResource + ":" + theProperty + ":" + theOldValues );
		
		if (theResource instanceof RDFSClass) {
			if (mOwlEquivalentClass.equals(theProperty))
				handleEquivalentClassChange((RDFSClass) theResource, theOldValues);
			else if (mOwlDisjointWith.equals(theProperty))
				handleDisjointWithChange((RDFSClass) theResource, theOldValues);
			else if (mRdfsSubClassOf.equals(theProperty))
				handleSubClassOfChange((RDFSClass) theResource, theOldValues);
			else if (mOwlIntersectionOf.equals(theProperty) || mOwlUnionOf.equals(theProperty) )
				handleNaryChange((RDFSClass) theResource, theProperty, theOldValues);
			else if (mOwlSomeValues.equals(theProperty) || mOwlAllValues.equals(theProperty) )
				handleRestrictionChange((RDFSClass) theResource, theProperty, theOldValues);
		}
		else if (theResource instanceof RDFProperty) {
			final RDFProperty theSubjectProperty = (RDFProperty)theResource;

			// Avoid range and type changes for system slots (e.g., rdf:value)
			if (theSubjectProperty.isSystem()) {
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine("Ignoring property value change with system property subject: "
							+ theSubjectProperty + "," + theProperty);
			} else if (mRdfsRange.equals(theProperty))
				handleRangeChange(theSubjectProperty, theOldValues);
			else if (mRdfType.equals(theProperty))
				handlePropertyTypeChange(theSubjectProperty, theOldValues);
		}
		else if ((theResource instanceof RDFList)) {
			if (mRdfFirst.equals(theProperty))
				handleListFirstChange(theResource, theOldValues);
			else if (mRdfRest.equals(theProperty)) handleListRestChange((RDFList) theResource, theOldValues);
		}
	}
    
    private void computeValueChanges(RDFResource theSubject, RDFProperty thePredicate,
			Collection<Object> theOldObjects, Set<Object> theAdditions, Set<Object> theRemovals) {
		Collection<Object> aNew = theSubject.getPropertyValues(thePredicate);
		if (theAdditions != null) {
			theAdditions.addAll(aNew);
			theAdditions.removeAll(theOldObjects);
		}
		if (theRemovals != null) {
			theRemovals.addAll(theOldObjects);
			theRemovals.removeAll(aNew);
		}
    }
    
    private void handlePropertyTypeChange(RDFProperty theProperty, Collection<Object> theOldValues) {
		Set<Object> aAdditions = new HashSet<Object>();
		Set<Object> aRemoves = new HashSet<Object>();
		computeValueChanges(theProperty, mRdfType, theOldValues, aAdditions, aRemoves);

		if (!aAdditions.isEmpty() || !aRemoves.isEmpty()) {
			ChangeList aChangeList = new ChangeList();
			try {
				for (boolean aAdd : new Boolean[] { true, false }) {
					for (Object aObj : aAdd ? aAdditions : aRemoves) {
						OWLAxiom aAxiom = null;
						if (mOwlFunctionalProperty.equals(aObj))
							aAxiom = mConverter.createFunctionalAxiom(theProperty);
						else if (mOwlDataProperty.equals(aObj))
							aAxiom = mConverter.createDeclarationAxiom(theProperty);
						else if (theProperty instanceof OWLObjectProperty) {
							OWLObjectProperty aObjectProperty = (OWLObjectProperty) theProperty;
							if (mOwlObjectProperty.equals(aObj))
								aAxiom = mConverter.createDeclarationAxiom(aObjectProperty);
							else if (mOwlInverseFunctionalProperty.equals(aObj))
								aAxiom = mConverter.createInverseFunctionalAxiom(aObjectProperty);
							else if (mOwlSymmetricProperty.equals(aObj))
								aAxiom = mConverter.createSymmetricAxiom(aObjectProperty);
							else if (mOwlTransitiveProperty.equals(aObj))
								aAxiom = mConverter.createTransitiveAxiom(aObjectProperty);
							else throw new RuntimeException("Unexpected type for property: "
									+ theProperty + "/" + aObj);
						}
						if (aAxiom == null) {
							if (theProperty.isAnnotationProperty()) {
								if (LOGGER.isLoggable(Level.FINE)) {
									LOGGER.fine("Ignoring type change on annotation property: "
											+ theProperty);
								}
							}
							else throw new NullPointerException("Property type conversion failed");
						}
						else {
							if (aAdd)
								aChangeList.axiomAdded(aAxiom);
							else aChangeList.axiomRemoved(aAxiom);
						}
					}
				}
				if (!aChangeList.isEmpty()) mChangeLog.queueChanges(aChangeList);
			} catch( ConversionException e ) {
				LOGGER
						.log(
								Level.SEVERE,
								"Error affecting reasoner synchronization: failure attempting to convert rdf:type values change for property: "
										+ theProperty, e);
			}
		}
	}
    
    private void handleListFirstChange(RDFResource theResource, Collection<Object> theOldValues) {
		Collection aNewValues = theResource.getPropertyValues(mRdfFirst);
		if (aNewValues.isEmpty()) {
			Iterator<Object> aItr = theOldValues.iterator();
			if (aItr.hasNext()) {
				Object aObj = aItr.next();
				if (aObj instanceof RDFSClass) {
					if (aItr.hasNext()) {
						LOGGER
								.severe("Error affecting reasoner synchronization: multiple first elements in list");
						return;
					}
					LOGGER.fine("PropertyValueListener: Saving class list first element");
					RDFSClass aCls = (RDFSClass) aObj;
					try {
						mConverter.cacheListFirst(theResource.getFrameID(), aCls);
					} catch( ConversionException e ) {
						LOGGER
								.log(
										Level.SEVERE,
										"Error affecting reasoner synchronization: failure attempting to store rdf:List first change",
										e);
					}
				}
			}
		}
	}

    private Tuple getReference(Object obj) {
    	Iterator<?> aIt = mModel.listReferences(obj,Integer.MAX_VALUE);
		return getElement(aIt, Tuple.class);
    }

    private OWLClass getRootReference(OWLClass cls) {
    	OWLClass curr = cls;
    	while (curr != null && curr.isAnonymous()) {
	    	Iterator<?> aIt = mModel.listReferences(curr,Integer.MAX_VALUE);
			Tuple tuple = getElement(aIt, Tuple.class);
			RDFProperty theProperty = tuple.getPredicate();
			if (mOwlEquivalentClass.equals(theProperty) ||
				mOwlDisjointWith.equals(theProperty) ||
				mRdfsSubClassOf.equals(theProperty)) {
				break;	
			}
			RDFResource theSubject = tuple.getSubject();
			if (!theSubject.isAnonymous() || !(theSubject instanceof OWLClass))
				break;
				curr = (OWLClass) theSubject;
    	}
		return curr;
    }
    
    private <T> T getValue(RDFResource theResource, RDFProperty theProperty, Class<T> theType) {
    	Collection aValues = theResource.getPropertyValues(theProperty);
		return getElement(aValues, theType);
    }
    
    private <T> T getElement(Collection<?> coll, Class<T> type) {
    	return getElement(coll.iterator(), type);
    }
    
    private <T> T getElement(Iterator<?> iterator, Class<T> type) {
    	if (iterator.hasNext()) {
    		Object obj = iterator.next();
    		if (type.isInstance(obj)) {
    			if (iterator.hasNext())
    				LOGGER.severe("Error affecting reasoner synchronization: multiple first elements in list");
    			return type.cast(obj);
    		}
    	}
    	
    	return null;
    }

    
    private void handleListRestChange(RDFList aList, Collection<Object> theOldValues) {
		try {
			RDFList aOldRest = getElement(theOldValues, RDFList.class);
			RDFList aNewRest = getValue(aList, mRdfRest, RDFList.class);
			
			RDFList aStartList = aList.getStart();
			Tuple aTuple = getReference(aStartList);		
			if (aTuple == null)
				return;	
			
			RDFResource aSubject = aTuple.getSubject();
			if (!(aTuple.getSubject() instanceof OWLNAryLogicalClass))
				return;
			
			mConverter.cacheListRest(aList.getFrameID(), aOldRest == null ? null : aOldRest.getFrameID());
			
			List<OWLDescription> aOldList = mConverter.convertClassList(aStartList);		
	
			mConverter.cacheListRest(aList.getFrameID(), aNewRest == null ? null : aNewRest.getFrameID());
			
			List<OWLDescription> aNewList = mConverter.convertClassList(aStartList);
			
			handleNaryChange((OWLNAryLogicalClass) aSubject, aOldList, aNewList);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error affecting reasoner synchronization: " +
					"failure attempting to handle rdf:List rest change", e);
		}
		
	}
    
	private void handleNaryChange(RDFSClass theCls, RDFProperty theProperty, Collection<Object> theOldValues) {
		final RDFList aNilList = mModel.getRDFNil();
		
		try {
			Set<Object> aRemoves = new HashSet<Object>();
			computeValueChanges(theCls, theProperty, theOldValues, null, aRemoves);		
			
			RDFList aOldRDFList = getElement(aRemoves, RDFList.class);
			RDFList aNewRDFList = getValue(theCls, theProperty, RDFList.class);
			
			List<OWLDescription> aOldList = mConverter.convertClassList(aOldRDFList != null ? aOldRDFList : aNilList);
			List<OWLDescription> aNewList = null;
			try {
				aNewList = mConverter.convertClassList(aNewRDFList != null ? aNewRDFList : aNilList);
			} catch (IncompleteInputException e) {
				LOGGER.fine("Ignoring n-ary class change: The list is incomplete");
				return;
			}
			
			handleNaryChange((OWLNAryLogicalClass) theCls, aOldList, aNewList);
		} catch (ConversionException e) {
			LOGGER.log(Level.SEVERE, "Unable to handle " + theProperty + " change", e);
		}
	}
	
	private void handleNaryChange(OWLNAryLogicalClass aCls, List<OWLDescription> aOldList, List<OWLDescription> aNewList) throws ConversionException {
		OWLAnonymousClass aRootExpression = aCls.getExpressionRoot();			
		
		OWLDescription aOldDescription = null;
		OWLDescription aNewDescription = null;

		if (aOldList != null) {
			mConverter.cacheNaryClass(aCls, aOldList);
			aOldDescription = mConverter.convertClass(aRootExpression);
		}

		if (aNewList != null) {
			mConverter.cacheNaryClass(aCls, aNewList);		
			aNewDescription = mConverter.convertClass(aRootExpression);
		}		
		
		Tuple aTuple =  getReference(aRootExpression);
		if (aTuple == null) {
			LOGGER.fine("Ignoring n-ary class change: Expression not connected to any axiom");
			return;
		}
		
		RDFResource aResource = aTuple.getSubject();
		RDFProperty aProperty = aTuple.getPredicate();
		ChangeList aChangeList = new ChangeList();
		if (mOwlEquivalentClass.equals(aProperty)) {
			if (aOldDescription != null)
				aChangeList.axiomRemoved( mConverter.getFactory().getOWLEquivalentClassesAxiom(
						mConverter.convertClass((RDFSClass) aResource), aOldDescription));
			if (aNewDescription != null)
				aChangeList.axiomAdded(mConverter.getFactory().getOWLEquivalentClassesAxiom(
						mConverter.convertClass((RDFSClass) aResource), aNewDescription));
		}
		else if (mRdfsSubClassOf.equals(aProperty)) {
			if (aOldDescription != null)
				aChangeList.axiomRemoved( mConverter.getFactory().getOWLSubClassAxiom(
						mConverter.convertClass((RDFSClass) aResource), aOldDescription));
			if (aNewDescription != null)
				aChangeList.axiomAdded(mConverter.getFactory().getOWLSubClassAxiom(
						mConverter.convertClass((RDFSClass) aResource), aNewDescription));
		}
		
		mChangeLog.queueChanges(aChangeList);
			
	}
	
	private void handleRestrictionChange(RDFSClass theCls, RDFProperty theProperty, Collection<Object> theOldValues) {
		try {
			Set<Object> aRemoves = new HashSet<Object>();
			computeValueChanges(theCls, theProperty, theOldValues, null, aRemoves);		
			
			OWLClass aOldValue = getElement(aRemoves, OWLClass.class);
			OWLClass aNewValue = getValue(theCls, theProperty, OWLClass.class);
			
			if (aOldValue == null || aNewValue == null) {
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine(String.format(
					                "%s value of %s is null, skipping restriction that will be handled elsewhere: ",
					                (aNewValue == null ? "New" : "Old"), theProperty.getLocalName()));
				return;
			}

			OWLObjectProperty aRestrProperty = getValue(theCls, mOwlOnProperty, OWLObjectProperty.class);
			OWLObjectPropertyExpression aProp = (OWLObjectPropertyExpression) mConverter.convertProperty(aRestrProperty); 
			
			OWLDescription aOldDescription =  mConverter.convertClass(aOldValue);
			OWLDescription aNewDescription =  mConverter.convertClass(aNewValue);
			
				
			handleRestrictionChange((OWLRestriction) theCls, aProp, aOldDescription, aNewDescription);
		}
        catch (Exception e) {
			LOGGER.log(Level.SEVERE,
							"Error affecting reasoner synchronization: failure to handle change on restriction: "
									+ theProperty.toString(), e);
        }
	}
	
	private void handleRestrictionChange(OWLRestriction aCls, OWLObjectPropertyExpression theProperty, OWLDescription aOldValue, OWLDescription aNewValue)
	                throws ConversionException {
		OWLAnonymousClass aRootExpression = aCls.getExpressionRoot();

		OWLDescription aOldDescription = null;
		OWLDescription aNewDescription = null;

		if (aOldValue != null) {
			mConverter.cacheRestrictionWithFiller(aCls, theProperty, aOldValue);
			aOldDescription = mConverter.convertClass(aRootExpression);
		}

		if (aNewValue != null) {
			mConverter.cacheRestrictionWithFiller(aCls, theProperty, aNewValue);
			aNewDescription = mConverter.convertClass(aRootExpression);
		}

		Tuple aTuple = getReference(aRootExpression);
		if (aTuple == null) {
			LOGGER.fine("Ignoring restriction change: Expression not connected to any axiom");
			return;
		}

		RDFResource aResource = aTuple.getSubject();
		RDFProperty aProperty = aTuple.getPredicate();
		ChangeList aChangeList = new ChangeList();
		if (mOwlEquivalentClass.equals(aProperty)) {
			if (aOldDescription != null)
				aChangeList.axiomRemoved(mConverter.getFactory().getOWLEquivalentClassesAxiom(
				                mConverter.convertClass((RDFSClass) aResource), aOldDescription));
			if (aNewDescription != null)
				aChangeList.axiomAdded(mConverter.getFactory().getOWLEquivalentClassesAxiom(
				                mConverter.convertClass((RDFSClass) aResource), aNewDescription));
		}
		else if (mRdfsSubClassOf.equals(aProperty)) {
			if (aOldDescription != null)
				aChangeList.axiomRemoved(mConverter.getFactory().getOWLSubClassAxiom(
				                mConverter.convertClass((RDFSClass) aResource), aOldDescription));
			if (aNewDescription != null)
				aChangeList.axiomAdded(mConverter.getFactory().getOWLSubClassAxiom(
				                mConverter.convertClass((RDFSClass) aResource), aNewDescription));
		}

		mChangeLog.queueChanges(aChangeList);

	}	

    private void handleSubClassOfChange(RDFSClass theSubClass, Collection<Object> theOldValues) {
		Set<Object> aAdditions = new HashSet<Object>();
		Set<Object> aRemoves = new HashSet<Object>();
		computeValueChanges(theSubClass, mRdfsSubClassOf, theOldValues, aAdditions, aRemoves);

		ChangeList aChangeList = new ChangeList();
		for (boolean aAdd : new boolean[] { true, false }) {
			for (Object aObj : aAdd ? aAdditions : aRemoves) {
				if (aObj instanceof RDFSClass) {
					try {
						if (aAdd)
							aChangeList.axiomAdded(mConverter.createSubClassAxiom(theSubClass,
									(RDFSClass) aObj));
						else aChangeList.axiomRemoved(mConverter.createSubClassAxiom(theSubClass,
								(RDFSClass) aObj));
					}
					catch (ConversionException e) {
						LOGGER.log(Level.SEVERE,
								"Error affecting reasoner synchronization: failure to "
										+ (aAdd ? "add" : "remove") + " subclass axiom subClass: "
										+ theSubClass.toString() + " superClass: " + aObj, e);
					}
				}
			}
		}
		mChangeLog.queueChanges(aChangeList);
	}
    
	private void handleRangeChange(RDFProperty theProperty, Collection<Object> theOldValues) {

		Set<Object> aAdditions = new HashSet<Object>();
		Set<Object> aRemoves = new HashSet<Object>();
		computeValueChanges(theProperty, mRdfsRange, theOldValues, aAdditions, aRemoves);

		if (!aAdditions.isEmpty() || !aRemoves.isEmpty()) {
			try {
				ChangeList aChangeList = new ChangeList();
				for (boolean aAdd : new Boolean[] { true, false }) {
					for (Object aObj : aAdd ? aAdditions : aRemoves) {

						OWLAxiom aAxiom;

						if (aObj instanceof RDFSClass) {
							aAxiom = mConverter.createObjectPropertyRangeAxiom(theProperty,
									(RDFSClass) aObj);
						}
						else if (aObj instanceof RDFResource) {
							aAxiom = mConverter.createDataPropertyRangeAxiom(theProperty,
									(RDFResource) aObj);
						}
						else throw new UnexpectedTypeException(aObj);

						if (aAdd)
							aChangeList.axiomAdded(aAxiom);
						else aChangeList.axiomRemoved(aAxiom);
					}
				}
				mChangeLog.queueChanges(aChangeList);
			} catch( ConversionException e ) {
				LOGGER.log(Level.SEVERE,
						"Error affecting reasoner synchronization: failure to handle range change for property: "
								+ theProperty.toString(), e);
			}
		}
	}
	
	private void handleDisjointWithChange(RDFSClass theClass, Collection<Object> theOldValues) {
		Set<Object> aAdditions = new HashSet<Object>();
		Set<Object> aRemoves = new HashSet<Object>();
		computeValueChanges(theClass, mOwlDisjointWith, theOldValues, aAdditions, aRemoves);

		if (!aAdditions.isEmpty() || !aRemoves.isEmpty()) {
			try {
				ChangeList aChangeList = new ChangeList();
				for (boolean aAdd : new Boolean[] { true, false }) {
					for (Object aObj : aAdd ? aAdditions : aRemoves) {
						if (aObj instanceof RDFSClass) {
							OWLDisjointClassesAxiom aAxiom = mConverter.createDisjointClassesAxiom(
									theClass, (RDFSClass) aObj);
							if (aAdd)
								aChangeList.axiomAdded(aAxiom);
							else aChangeList.axiomRemoved(aAxiom);
						}
						else throw new UnexpectedTypeException(aObj);
					}
				}
				mChangeLog.queueChanges(aChangeList);
			} catch( ConversionException e ) {
				LOGGER.log(Level.SEVERE,
						"Error affecting reasoner synchronization: failure to handle owl:disjointWith change for class: "
								+ theClass.toString(), e);
			}
		}
	}

	private void handleEquivalentClassChange(RDFSClass theClass, Collection<Object> theOldValues) {
		Set<Object> aAdditions = new HashSet<Object>();
		Set<Object> aRemoves = new HashSet<Object>();
		computeValueChanges(theClass, mOwlEquivalentClass, theOldValues, aAdditions, aRemoves);

		if (!aAdditions.isEmpty() || !aRemoves.isEmpty()) {
			try {
				ChangeList aChangeList = new ChangeList();
				for (boolean aAdd : new Boolean[] { true, false }) {
					for (Object aObj : aAdd ? aAdditions : aRemoves) {
						if (aObj instanceof RDFSClass) {
							OWLEquivalentClassesAxiom aAxiom = mConverter
									.createEquivalentClassesAxiom(theClass, (RDFSClass) aObj);
							if (aAdd)
								aChangeList.axiomAdded(aAxiom);
							else aChangeList.axiomRemoved(aAxiom);
						}
						else throw new UnexpectedTypeException(aObj);
					}
				}
				mChangeLog.queueChanges(aChangeList);
			} catch( ConversionException e ) {
				LOGGER
						.log(
								Level.SEVERE,
								"Error affecting reasoner synchronization: failure to handle owl:equivalentClass change for class: "
										+ theClass.toString(), e);
			}
		}
	}
	
	@Override
	public void visibilityChanged(RDFResource theResource) {
		LOGGER.fine("PropertyValueListener: visibility changed");
	}

	private boolean changedThisTransaction(RDFResource theResource, RDFProperty theProperty) {
		if (transactionDepth == 0)
			return false;
		
		Set<RDFProperty> existing = mChangesSinceCommit.get(theResource);
		if (existing == null) {
			existing = new HashSet<RDFProperty>();
			mChangesSinceCommit.put(theResource, existing);
		}

		return !existing.add(theProperty);
	}

	public void endTransaction() {
		transactionDepth--;
		if (transactionDepth == 0) mChangesSinceCommit.clear();
	}
	
	public void startTransaction() {
		transactionDepth++;
	}	
}