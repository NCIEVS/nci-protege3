package com.clarkparsia.protege.change;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAnnotation;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataOneOf;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLDeclarationAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLPropertyExpression;
import org.semanticweb.owl.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owl.vocab.XSDVocabulary;

import com.clarkparsia.explanation.io.html.HTMLObjectRenderer;
import com.clarkparsia.explanation.io.html.utils.OWLAxiomSideVisitor;
import com.clarkparsia.protege.exceptions.ConversionException;
import com.clarkparsia.protege.exceptions.IncompleteInputException;
import com.clarkparsia.protege.exceptions.UnexpectedTypeException;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLAllValuesFrom;
import edu.stanford.smi.protegex.owl.model.OWLCardinality;
import edu.stanford.smi.protegex.owl.model.OWLCardinalityBase;
import edu.stanford.smi.protegex.owl.model.OWLComplementClass;
import edu.stanford.smi.protegex.owl.model.OWLDataRange;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLEnumeratedClass;
import edu.stanford.smi.protegex.owl.model.OWLHasValue;
import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
import edu.stanford.smi.protegex.owl.model.OWLMaxCardinality;
import edu.stanford.smi.protegex.owl.model.OWLMinCardinality;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNAryLogicalClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.OWLQuantifierRestriction;
import edu.stanford.smi.protegex.owl.model.OWLRestriction;
import edu.stanford.smi.protegex.owl.model.OWLSomeValuesFrom;
import edu.stanford.smi.protegex.owl.model.OWLUnionClass;
import edu.stanford.smi.protegex.owl.model.RDFList;
import edu.stanford.smi.protegex.owl.model.RDFObject;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSDatatype;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;

/**
 * Title: A conversion utility for changing between Protege OWL API objects and Manchester OWLAPI objects.<br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Aug 22, 2007 2:43:53 PM
 * 
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class Converter {
	private final Logger LOGGER = Log.getLogger(Converter.class);

    /**
     * Factory used to create axioms
     */
    private OWLDataFactory mFactory;

    /**
     * Map from Frames to their URI's
     */
    private Map<Frame, String> mURICache;

    private Map<FrameID, OWLDescription> mAnonClsCache;
	private Map<FrameID, OWLDescription> mFirstMap;
	private Map<FrameID, FrameID> mRestMap;

    public Converter() {
		this(OWLManager.createOWLOntologyManager().getOWLDataFactory());
	}

	public Converter(OWLDataFactory theFactory) {
		mFactory = theFactory;

		mURICache = new HashMap<Frame, String>();
		mAnonClsCache = new HashMap<FrameID, OWLDescription>();
		mFirstMap = new HashMap<FrameID, OWLDescription>();
		mRestMap = new HashMap<FrameID, FrameID>();
    }

    /**
     * Returns the OWLDataFactory used by this converter
     * @return the OWLDataFactory
     */
    public OWLDataFactory getFactory() {
		return mFactory;
	}

    /**
     * Resets the state of the converter by clearing all the cached values
     */
    public void reset() {
		mAnonClsCache.clear();
		mFirstMap.clear();
		mRestMap.clear();
		LOGGER.fine("Converter caches cleared");
    }

    /**
     * Returns the URI of the given resource
     * @param theResource the resource to get the URI for
     * @return the URI of the resource
     * @throws ConversionException if the URI for the resource is not a valid URI
     */
    private URI getURI(RDFResource theResource) throws ConversionException {
		if (theResource == null)
			throw new NullPointerException("Cannot get URI for null resource");

		String aURIStr = mURICache.get( theResource );
		
		if (aURIStr == null)
			aURIStr = theResource.getURI();
		
		try {
			URI aURI = new URI( aURIStr );
			return aURI;
		} catch( URISyntaxException use ) {
			LOGGER.fine( "URI syntax exception caught getting resource URI: "
					+ use.getMessage() );
			throw new ConversionException( use );
		}
	}
    
    public void cacheAnonCls(RDFSClass theClass) throws ConversionException {
    	if (mAnonClsCache.containsKey(theClass.getFrameID()))
    		return;
    	
    	OWLDescription aDesc = convertClass(theClass);
    	mAnonClsCache.put(theClass.getFrameID(), aDesc);
    	if (LOGGER.isLoggable(Level.FINE))
    		LOGGER.fine("Cached anonymous class description: " + aDesc);
    }
    
    public void cacheRestrictionWithFiller(OWLRestriction theClass, OWLObjectPropertyExpression theProperty, OWLDescription theFiller) throws ConversionException {
    	OWLDescription aDescription = null;
    	 if (theClass instanceof OWLSomeValuesFrom)
 			aDescription = mFactory.getOWLObjectSomeRestriction(theProperty, theFiller);
 		else if (theClass instanceof OWLAllValuesFrom)
 			aDescription = mFactory.getOWLObjectAllRestriction(theProperty, theFiller);
    	 
		if (aDescription != null) {
			mAnonClsCache.put(theClass.getFrameID(), aDescription);

			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("Cached restriction: " + aDescription);
		}
		else {
			LOGGER.warning("Cannot cache unsupported restriction: " + theClass);
		}
    }
	
	public void cacheNaryClass(RDFSClass theClass, Collection<OWLDescription> theDescriptions) {
		OWLDescription aDescription = null;
		if(theDescriptions.size() == 1) 
			aDescription = theDescriptions.iterator().next();
		else if (theClass instanceof OWLIntersectionClass)
			aDescription = mFactory.getOWLObjectIntersectionOf(new HashSet<OWLDescription>(theDescriptions));
		else if (theClass instanceof OWLUnionClass)
			aDescription = mFactory.getOWLObjectUnionOf(new HashSet<OWLDescription>(theDescriptions));		

		if (aDescription != null) {
			mAnonClsCache.put(theClass.getFrameID(), aDescription);

			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("Cached anonymous class description: " + aDescription);
		}
		else {
			LOGGER.warning("Cannot cache unknown nary class description: " + theClass);
		}
	}
	
	public void cacheListFirst(FrameID theFrameID, RDFSClass theClass) throws ConversionException {
		OWLDescription aDescription = convertClass(theClass);
		if (aDescription == null)
			throw new ConversionException("Class conversion failed, unable to save list first");
		mFirstMap.put(theFrameID, aDescription);
    	if (LOGGER.isLoggable(Level.FINE))
    		LOGGER.fine("Added to rdf:first cache:" + theFrameID + ":" + aDescription);
	}

	public void cacheListRest(FrameID theList, FrameID theRest) {
		mRestMap.put(theList, theRest);
    	if (LOGGER.isLoggable(Level.FINE))
    		LOGGER.fine("Added to rdf:rest cache:" + theList + ":" + theRest);
	}
	
	public boolean isCached(RDFList theList) {
		return mFirstMap.containsKey(theList.getFrameID()) && mRestMap.containsKey(theList.getFrameID());
	}
	
	public void cacheList(RDFList theList) throws ConversionException {
		final FrameID aNilId = theList.getOWLModel().getRDFNil().getFrameID();
		final Set<FrameID> aVisitLog = new HashSet<FrameID>();
		RDFList aCurrent = theList;
		FrameID aCurrentID = theList.getFrameID();
		
		/*
		 * Iterate over the list until it ends, but beware of cycles that would
		 * cause infinite looping problems.
		 */
		while( !aCurrentID.equals(aNilId) ) {
			
			if (!aVisitLog.add(aCurrentID)) {
		    	if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine("Cycle detected in list, terminating conversion after " + aVisitLog.size()
					                + " elements");
				break;
			}

			if (!mFirstMap.containsKey(aCurrentID)) {
				if (aCurrent == null)
					aCurrent = (RDFList) theList.getOWLModel().getFrame(aCurrentID);
				if (aCurrent == null)
					throw new IncompleteInputException(
							"rdf:List is incomplete (rdf:List resource missing)");

				Object aObj = aCurrent.getFirst();
				if (aObj == null) 
					throw new IncompleteInputException("rdf:List is incomplete (rdf:first object missing) skipping element");
				else {
					if (aObj instanceof RDFSClass) {
						RDFSClass aCls = (RDFSClass) aObj;
						cacheListFirst(aCurrentID, aCls);
					}
					else if (LOGGER.isLoggable(Level.FINE))
						LOGGER.fine("Ignoring non-class member of rdf:List: " + aObj.getClass().getName());
				}
			}

			// Get rest from cache if possible
			FrameID aRestId = mRestMap.get(aCurrentID);
			if (aRestId != null) {
				aCurrentID = aRestId;
				aCurrent = null;
			}
			else {
				aCurrent = aCurrent.getRest();
				if (aCurrent == null) {
					aCurrentID = aNilId;
				}
				else {
					aCurrentID = aCurrent.getFrameID();
				}
			}

		}
	}

    /**
     * Convert an RDFList of RDFSClass objects to a list of OWLDescription objects
     * @param theList the RDFList of RDFSClass'es to convert
     * @return a copy of the list, with each element in the list converted to its corresponding OWLDescription
     * @throws ConversionException thrown if the list parameter contains list elements that are not of type RDFSClass
     */
    public List<OWLDescription> convertClassList(RDFList theList) throws ConversionException {

		List<OWLDescription> aReturn = new ArrayList<OWLDescription>();
		final FrameID aNilId = theList.getOWLModel().getRDFNil().getFrameID();
		final Set<FrameID> aVisitLog = new HashSet<FrameID>();
		RDFList aCurrent = theList;
		FrameID aCurrentID = theList.getFrameID();

		/*
		 * Iterate over the list until it ends, but beware of cycles that would
		 * cause infinite looping problems.
		 */
		while( !aCurrentID.equals(aNilId) ) {
			
			if (!aVisitLog.add(aCurrentID)) {
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine("Cycle detected in list, terminating conversion after " + aVisitLog.size()
					                + " elements");
				break;
			}

			// Get first from cache if possible
			OWLDescription aDescription = mFirstMap.get(aCurrentID);
			if (aDescription != null) {
				aReturn.add(aDescription);				
			} else {
				if (aCurrent == null)
					aCurrent = (RDFList) theList.getOWLModel().getFrame(aCurrentID);
				if (aCurrent == null)
					throw new IncompleteInputException(
							"rdf:List is incomplete (rdf:List resource missing)");

				Object aObj = aCurrent.getFirst();
				if (aObj == null) {
					throw new IncompleteInputException("rdf:List incomplete (rdf:first object missing)");
				}
				else {
					if (aObj instanceof RDFSClass) {
						RDFSClass aCls = (RDFSClass) aObj;
						aDescription = convertClass(aCls);
						if (aDescription == null)
							throw new ConversionException("Failed to convert list element");
						aReturn.add(aDescription);
					}
					else {
						LOGGER.warning("rdf:List contains non-class skipping element: "
								+ aObj.getClass().getName());
					}
				}
			}


			// Get rest from cache if possible
			FrameID aRestId = mRestMap.get(aCurrentID);
			if (aRestId != null) {
				aCurrentID = aRestId;
				aCurrent = null;
			}
			else {
				aCurrent = aCurrent.getRest();
				if (aCurrent == null) {
					aCurrentID = aNilId;
				}
				else {
					aCurrentID = aCurrent.getFrameID();
				}
			}

		}

		return aReturn;
	}

    /**
     * Track the URI for the given RDF Resource
     * @param theURI the uri
     * @param theFrame the resource which is identified by the given URI
     */
    public void cacheURIForResource(RDFResource theResource) {
		if (!mURICache.containsKey(theResource) && !theResource.isAnonymous()) {
			final String aURI = theResource.getURI();
			
			/*
			 * The anonymous check depends on a bnode identifier which will be
			 * gone if frame has been deleted already. See
			 * DefaultFrame.getName()
			 */
			if (aURI.contains("<<deleted>>")) {
				LOGGER.warning("Attempt to cache URI of deleted frame aborted");
				return;
			}
			
			mURICache.put(theResource, aURI);
	    	if (LOGGER.isLoggable(Level.FINE))
	    		LOGGER.fine("Cached URI for resource: " + aURI + ":" + theResource);
		}
	}

    /**
     * Convert the list of protege RDFSClass objects into a list of OWL-API OWLDescriptions
     * @param theProtegeList the list of Protege RDFSClass objects to convert
     * @return a list of equivalent OWLDescription objects
     * @throws ConversionException thrown when there is an error converting one of the individual list elements
     */
    public Set<OWLDescription> convertListOfClasses(Collection<? extends RDFSClass> theProtegeList)
			throws ConversionException {
		Set<OWLDescription> aDescSet = new HashSet<OWLDescription>();

		for (RDFSClass aClass : theProtegeList) {
			aDescSet.add(convertClass(aClass));
		}

		return aDescSet;
	}

    /**
     * Convert the list of Protege Individuals to a list of OWL-API OWLIndividuals
     * @param theProtegeList the list of Protege Individual objects
     * @return a list of equivalent OWLIndividual boejcts
     * @throws ConversionException throw when there is an error converting one of the individual list elements
     */
    public Set<OWLIndividual> convertListOfIndividuals(
			Collection<edu.stanford.smi.protegex.owl.model.OWLIndividual> theProtegeList)
			throws ConversionException {
		Set<OWLIndividual> aIndSet = new HashSet<OWLIndividual>();

		for (edu.stanford.smi.protegex.owl.model.OWLIndividual aInd : theProtegeList) {
			aIndSet.add(convertIndividual(aInd));
		}

		return aIndSet;
	}

    /**
     * Given two parameters, where one is the sub class of the other, construct an OWL-API subclass axiom to represent
     * this relationship
     * @param theSubClass the subclass
     * @param theSuperClass the super class
     * @return an OWL-API subclass axiom whose objects are the (converted) parameters
     * @throws ConversionException if there is an error converting either of the parameters to OWL-API objects
     */
    public OWLAxiom createSubClassAxiom(RDFSClass theSubClass, RDFSClass theSuperClass)
			throws ConversionException {
		return mFactory.getOWLSubClassAxiom(convertClass(theSubClass), convertClass(theSuperClass));
	}

	public OWLDisjointClassesAxiom createDisjointClassesAxiom(RDFSClass theCls,
			RDFSClass theOtherCls) throws ConversionException {
		return createDisjointClassesAxiom(theCls, Arrays.asList(theOtherCls));
	}

	public OWLDisjointClassesAxiom createDisjointClassesAxiom(RDFSClass theCls,
			Collection theDisjoints) throws ConversionException {
		Set<OWLDescription> aClassSet = convertListOfClasses(theDisjoints);
		aClassSet.add(convertClass(theCls));

		return mFactory.getOWLDisjointClassesAxiom(aClassSet);
	}

	public OWLAxiom createSameIndividualsAxiom(
			edu.stanford.smi.protegex.owl.model.OWLIndividual theRes, Collection theSame)
			throws ConversionException {
		Set<OWLIndividual> aIndSet = convertListOfIndividuals(theSame);
		aIndSet.add(convertIndividual(theRes));

		return mFactory.getOWLSameIndividualsAxiom(aIndSet);
	}

    /**
     * Create a OWL-API different from axiom which represents the different from relationship of the individual to the
     * list of other individuals
     * @param theRes the protege individual
     * @param theDifferent the other individuals it is different from
     * @return an OWL-API different from axiom where the operands are the results of the conversion of the parameters
     * @throws ConversionException thrown when there was an error converting any of the parameters to OWL-API objects
     */
    public OWLAxiom createDifferentFromAxiom(
			edu.stanford.smi.protegex.owl.model.OWLIndividual theRes, Collection theDifferent)
			throws ConversionException {
		Set<OWLIndividual> aIndSet = convertListOfIndividuals(theDifferent);
		aIndSet.add(convertIndividual(theRes));

		return mFactory.getOWLDifferentIndividualsAxiom(aIndSet);
	}

	public OWLAxiom createSubPropertyAxiom(RDFProperty theSubProperty, RDFProperty theProperty)
			throws ConversionException {
		OWLPropertyExpression aProp = convertProperty(theProperty);
		OWLPropertyExpression aSubProp = convertProperty(theSubProperty);

		if (aProp instanceof OWLObjectPropertyExpression
				&& aSubProp instanceof OWLObjectPropertyExpression)
			return mFactory.getOWLSubObjectPropertyAxiom((OWLObjectPropertyExpression) aSubProp,
					(OWLObjectPropertyExpression) aProp);
		else if (aProp instanceof OWLDataPropertyExpression
				&& aSubProp instanceof OWLDataPropertyExpression)
			return mFactory.getOWLSubDataPropertyAxiom((OWLDataPropertyExpression) aSubProp,
					(OWLDataPropertyExpression) aProp);
		else {
			final String aErrorMessage = aProp + " and " + aSubProp
					+ " are not of the same type, either "
					+ "object or data, cannot construct appropriate sub property axiom";

			LOGGER.fine(aErrorMessage);
			throw new UnexpectedTypeException(aErrorMessage);
		}
	}

	public OWLAxiom createClassAssertionAxiom(RDFResource theInstance, RDFSClass theClass)
			throws ConversionException {
		return mFactory.getOWLClassAssertionAxiom(convertIndividual(theInstance),
				convertClass(theClass));
	}

	public OWLObjectPropertyRangeAxiom createObjectPropertyRangeAxiom(RDFProperty theProperty,
			RDFSClass theClass) throws ConversionException {
		OWLPropertyExpression aProp = convertProperty(theProperty);
		if (aProp instanceof OWLObjectPropertyExpression)
			return mFactory.getOWLObjectPropertyRangeAxiom((OWLObjectPropertyExpression) aProp,
					convertClass(theClass));
		else throw new UnexpectedTypeException("Object range passed with non-object property",
				aProp);
	}

	public OWLAxiom createEquivalentPropertiesAxiom(RDFProperty theProp, RDFProperty theEquivProp)
			throws ConversionException {
		OWLPropertyExpression aProp = convertProperty(theProp);
		OWLPropertyExpression aEqProp = convertProperty(theEquivProp);

		if (aProp instanceof OWLObjectPropertyExpression) {
			Set<OWLObjectPropertyExpression> aSet = new HashSet<OWLObjectPropertyExpression>();
			aSet.add((OWLObjectPropertyExpression) aProp);
			aSet.add((OWLObjectPropertyExpression) aEqProp);

			return mFactory.getOWLEquivalentObjectPropertiesAxiom(aSet);
		}
		else if (aProp instanceof OWLDataPropertyExpression) {
			Set<OWLDataPropertyExpression> aSet = new HashSet<OWLDataPropertyExpression>();
			aSet.add((OWLDataPropertyExpression) aProp);
			aSet.add((OWLDataPropertyExpression) aEqProp);

			return mFactory.getOWLEquivalentDataPropertiesAxiom(aSet);
		}
		else {
			final String aErrorMessage = "cannot create equivalent properties axiom between "
					+ aProp + " and " + aEqProp;

			LOGGER.fine(aErrorMessage);
			throw new UnexpectedTypeException(aErrorMessage);
		}
	}

	public OWLDataPropertyRangeAxiom createDataPropertyRangeAxiom(RDFProperty theProperty,
			RDFResource theRange) throws ConversionException {
		OWLPropertyExpression aProp = convertProperty(theProperty);
		if (aProp instanceof OWLDataPropertyExpression) {
			if (theRange instanceof OWLDataRange)
				return mFactory.getOWLDataPropertyRangeAxiom((OWLDataPropertyExpression) aProp,
						convertOWLDataRange((OWLDataRange) theRange));
			else if (theRange instanceof RDFSDatatype)
				return mFactory.getOWLDataPropertyRangeAxiom((OWLDataPropertyExpression) aProp,
						mFactory.getOWLDataType(getURI(theRange)));
			else throw new UnexpectedTypeException("Data property range type unexpected", theRange);
		}
		else {
			String aErrorMessage = aProp + " is not an data property, but used with datatype range";

			LOGGER.fine(aErrorMessage);
			throw new UnexpectedTypeException(aErrorMessage);
		}
	}

	public OWLEquivalentClassesAxiom createEquivalentClassesAxiom(RDFSClass theClass,
			RDFSClass theOtherClass) throws ConversionException {
		return mFactory.getOWLEquivalentClassesAxiom(convertListOfClasses(Arrays
				.asList(new RDFSClass[] { theClass, theOtherClass })));
	}

	public OWLObject convert(RDFObject theObject) throws ConversionException {
		if (theObject instanceof RDFSClass) {
			return convertClass((RDFSClass) theObject);
		}
		else if (theObject instanceof RDFProperty) {
			return convertProperty((RDFProperty) theObject);
		}
		else if (theObject instanceof RDFSLiteral) {
			return convertLiteral((RDFSLiteral) theObject);
		}
		else if (theObject instanceof RDFResource) {
			return convertIndividual((RDFResource) theObject);
		}
		else {
			LOGGER.fine("Unexpected type of RDFObject passed for conversion: "
					+ theObject.getClass().getName());
			throw new UnexpectedTypeException(RDFObject.class, theObject);
		}
	}

	public OWLDeclarationAxiom createDeclarationAxiom(RDFObject theObject)
			throws ConversionException {
		OWLObject aEntity = convert(theObject);
		return getFactory().getOWLDeclarationAxiom((OWLEntity) aEntity);
	}

	public OWLAxiom createFunctionalAxiom(RDFProperty theProperty) throws ConversionException {
		if (theProperty instanceof OWLDatatypeProperty)
			return getFactory().getOWLFunctionalDataPropertyAxiom(
					convertDataProperty((OWLDatatypeProperty) theProperty));
		else if (theProperty instanceof OWLObjectProperty)
			return getFactory().getOWLFunctionalObjectPropertyAxiom(
					convertObjectProperty((OWLObjectProperty) theProperty));
		else throw new UnexpectedTypeException(RDFProperty.class, theProperty);
	}

	public OWLInverseFunctionalObjectPropertyAxiom createInverseFunctionalAxiom(
			OWLObjectProperty theProperty) throws ConversionException {
		return getFactory().getOWLInverseFunctionalObjectPropertyAxiom(
				convertObjectProperty(theProperty));
	}

	public OWLSymmetricObjectPropertyAxiom createSymmetricAxiom(OWLObjectProperty theProperty)
			throws ConversionException {
		return getFactory().getOWLSymmetricObjectPropertyAxiom(convertObjectProperty(theProperty));
	}

	public OWLTransitiveObjectPropertyAxiom createTransitiveAxiom(OWLObjectProperty theProperty)
			throws ConversionException {
		return getFactory().getOWLTransitiveObjectPropertyAxiom(convertObjectProperty(theProperty));
	}

	public OWLAxiom createDomainAxiom(RDFSClass theClass, RDFProperty theProperty) throws ConversionException {
		OWLPropertyExpression aProp = convertProperty(theProperty);

		// TODO: what about annotation properties?
		if (aProp instanceof OWLObjectPropertyExpression)
			return mFactory.getOWLObjectPropertyDomainAxiom((OWLObjectPropertyExpression) aProp,
					convertClass(theClass));
		else if (aProp instanceof OWLDataPropertyExpression)
			return mFactory.getOWLDataPropertyDomainAxiom((OWLDataPropertyExpression) aProp,
					convertClass(theClass));
		else throw new UnexpectedTypeException(OWLPropertyExpression.class, aProp);
	}

	public OWLAxiom createDomainAxiom(Collection<RDFSClass> theList, RDFProperty theProperty) throws ConversionException {
		if (theList.isEmpty())
			return null;
		else if (theList.size() == 1)
			return createDomainAxiom(theList.iterator().next(), theProperty);

		OWLPropertyExpression aProp = convertProperty(theProperty);

		// TODO: what about annotation properties?
		if (aProp instanceof OWLObjectPropertyExpression)
			return mFactory.getOWLObjectPropertyDomainAxiom((OWLObjectPropertyExpression) aProp,
					mFactory.getOWLObjectUnionOf(convertListOfClasses(theList)));
		else if (aProp instanceof OWLDataPropertyExpression)
			return mFactory.getOWLDataPropertyDomainAxiom((OWLDataPropertyExpression) aProp,
					mFactory.getOWLObjectUnionOf(convertListOfClasses(theList)));
		else throw new UnexpectedTypeException(OWLPropertyExpression.class, aProp);
	}

	public OWLConstant convertLiteral(RDFSLiteral theLiteral) throws ConversionException {
		if (theLiteral.getDatatype() != null) {
			return mFactory.getOWLTypedConstant(theLiteral.getString(), mFactory.getOWLDataType(getURI(theLiteral.getDatatype())));
		}
		else if (theLiteral.getLanguage() != null) {
			return mFactory.getOWLUntypedConstant(theLiteral.getString(), theLiteral.getLanguage());
		}
		else {
			return mFactory.getOWLUntypedConstant(theLiteral.getString());
		}
	}

	public OWLDescription convertNaryLogicalClass(OWLNAryLogicalClass theClass)
			throws ConversionException {
		
		OWLDescription aDesc;

		if (theClass instanceof OWLIntersectionClass) {
			aDesc = convertIntersection((OWLIntersectionClass) theClass);
		}
		else if (theClass instanceof OWLUnionClass) {
			aDesc = convertUnion((OWLUnionClass) theClass);
		}
		else throw new UnexpectedTypeException(OWLNAryLogicalClass.class, theClass);

		return aDesc;
	}

	public OWLDescription convertIntersection(OWLIntersectionClass theClass)
			throws ConversionException {
		Collection<? extends RDFSClass> aOperand = theClass.getOperands();
		if (aOperand.isEmpty()) {
			throw new IncompleteInputException("Empty intersection: " + theClass);
		}
		else {
			Set<OWLDescription> aList = convertListOfClasses(aOperand);
			if (aList.size() == 1) {			
				OWLDescription aClass = aList.iterator().next();
				LOGGER.warning("owl:intersectionOf is supposed to have at least 2 elements but contains only 1: " + aClass);
				return aClass;
			}
			else {
				return mFactory.getOWLObjectIntersectionOf(aList);
			}
		}
	}

	public OWLDescription convertUnion(OWLUnionClass theClass)
			throws ConversionException {
		Collection<? extends RDFSClass> aOperand = theClass.getOperands();
		if (aOperand.isEmpty())
			throw new IncompleteInputException("Empty union: " + theClass);
		return mFactory.getOWLObjectUnionOf(convertListOfClasses(aOperand));
	}

	public OWLDescription convertComplement(OWLComplementClass theComplement)
			throws ConversionException {
		// TODO: data complements?

        return mFactory.getOWLObjectComplementOf(convertClass(theComplement.getComplement()));
	}

	public OWLDescription convertOneOf(OWLEnumeratedClass theOneOf) throws ConversionException {
		return mFactory.getOWLObjectOneOf(convertListOfIndividuals(theOneOf.getOneOf()));
	}

	public OWLDescription convertClass(RDFSClass theClass) throws ConversionException {

		OWLDescription aCachedDesc = mAnonClsCache.get(theClass.getFrameID());
		if (aCachedDesc != null) return aCachedDesc;

		if (theClass.isAnonymous()) {
			if (theClass instanceof edu.stanford.smi.protegex.owl.model.OWLRestriction) {
				return convertRestriction((edu.stanford.smi.protegex.owl.model.OWLRestriction) theClass);
			}
			else if (theClass instanceof OWLNAryLogicalClass) {
				return convertNaryLogicalClass((OWLNAryLogicalClass) theClass);
			}
			else if (theClass instanceof OWLComplementClass) {
				return convertComplement((OWLComplementClass) theClass);
			}
			else if (theClass instanceof OWLEnumeratedClass) {
				return convertOneOf((edu.stanford.smi.protegex.owl.model.OWLEnumeratedClass) theClass);
			}
			else {
				LOGGER.fine("throwing illegal arg from convert class");
				throw new UnexpectedTypeException("NYI - anon - " + theClass + " "
						+ theClass.getClass());
			}
		}
		else {
			URI aURI = getURI(theClass);
			return mFactory.getOWLClass(aURI);
		}
	}

	public OWLDescription convertRestriction(
			edu.stanford.smi.protegex.owl.model.OWLRestriction theRest) throws ConversionException {
		OWLDescription aReturn;
		
		if (theRest.getOnProperty() == null)
			throw new IncompleteInputException(
					"Unable to convert restriction: restricted property is null");

		if (theRest instanceof OWLCardinalityBase)
			aReturn = convertCardinalityRestriction((OWLCardinalityBase) theRest);
		else if (theRest instanceof OWLHasValue)
			aReturn = convertHasValueRestriction((OWLHasValue) theRest);
		else if (theRest instanceof OWLQuantifierRestriction)
			aReturn = convertQuantifierRestriction((OWLQuantifierRestriction) theRest);
		else throw new UnexpectedTypeException(
				edu.stanford.smi.protegex.owl.model.OWLRestriction.class, theRest);

		return aReturn;
	}

	public OWLDescription convertHasValueRestriction(OWLHasValue theValueRest)
			throws ConversionException {
		OWLDescription aReturn = null;

		final Object value = theValueRest.getHasValue();

		if (value instanceof RDFResource) {
			aReturn = mFactory.getOWLObjectValueRestriction(
					(OWLObjectPropertyExpression) convertProperty(theValueRest.getOnProperty()),
					convertIndividual((RDFResource) value));
		}
		else if (value instanceof RDFSLiteral) {
			aReturn = mFactory.getOWLDataValueRestriction(
					(OWLDataPropertyExpression) convertProperty(theValueRest.getOnProperty()),
					convertLiteral((RDFSLiteral) value));
		}
		else if (value instanceof Boolean) {
			aReturn = mFactory.getOWLDataValueRestriction(
					(OWLDataPropertyExpression) convertProperty(theValueRest.getOnProperty()),
					mFactory.getOWLTypedConstant(value.toString(), mFactory
							.getOWLDataType(XSDVocabulary.BOOLEAN.getURI())));
		}
		else if (value instanceof Float) {
			aReturn = mFactory.getOWLDataValueRestriction(
					(OWLDataPropertyExpression) convertProperty(theValueRest.getOnProperty()),
					mFactory.getOWLTypedConstant(value.toString(), mFactory
							.getOWLDataType(XSDVocabulary.FLOAT.getURI())));
		}
		else if (value instanceof Integer) {
			aReturn = mFactory.getOWLDataValueRestriction(
					(OWLDataPropertyExpression) convertProperty(theValueRest.getOnProperty()),
					mFactory.getOWLTypedConstant(value.toString(), mFactory
							.getOWLDataType(XSDVocabulary.INTEGER.getURI())));
		}
		else if (value instanceof String) {
			aReturn = mFactory.getOWLDataValueRestriction(
					(OWLDataPropertyExpression) convertProperty(theValueRest.getOnProperty()),
					mFactory.getOWLUntypedConstant(value.toString()));
		}

		return aReturn;
	}

	public OWLDescription convertCardinalityRestriction(OWLCardinalityBase theCardRest)
			throws ConversionException {
		OWLDescription aReturn;
		OWLPropertyExpression aPropExpr = convertProperty(theCardRest.getOnProperty());

		if (theCardRest instanceof OWLCardinality) {
			OWLCardinality aCard = (OWLCardinality) theCardRest;

			if (aPropExpr instanceof OWLDataPropertyExpression) {
				aReturn = mFactory.getOWLDataExactCardinalityRestriction(
						(OWLDataPropertyExpression) aPropExpr, aCard.getCardinality());
			}
			else if (aPropExpr instanceof OWLObjectPropertyExpression) {
				aReturn = mFactory.getOWLObjectExactCardinalityRestriction(
						(OWLObjectPropertyExpression) aPropExpr, aCard.getCardinality());
			}
			else throw new UnexpectedTypeException(OWLCardinality.class, aPropExpr);
		}
		else if (theCardRest instanceof OWLMinCardinality) {
			OWLMinCardinality aMinCard = (OWLMinCardinality) theCardRest;

			if (aPropExpr instanceof OWLDataPropertyExpression) {
				aReturn = mFactory.getOWLDataMinCardinalityRestriction(
						(OWLDataPropertyExpression) aPropExpr, aMinCard.getCardinality());
			}
			else if (aPropExpr instanceof OWLObjectPropertyExpression) {
				aReturn = mFactory.getOWLObjectMinCardinalityRestriction(
						(OWLObjectPropertyExpression) aPropExpr, aMinCard.getCardinality());
			}
			else throw new UnexpectedTypeException(OWLMinCardinality.class, aPropExpr);
		}
		else if (theCardRest instanceof OWLMaxCardinality) {
			OWLMaxCardinality aMaxCard = (OWLMaxCardinality) theCardRest;

			if (aPropExpr instanceof OWLDataPropertyExpression) {
				aReturn = mFactory.getOWLDataMaxCardinalityRestriction(
						(OWLDataPropertyExpression) aPropExpr, aMaxCard.getCardinality());
			}
			else if (aPropExpr instanceof OWLObjectPropertyExpression) {
				aReturn = mFactory.getOWLObjectMaxCardinalityRestriction(
						(OWLObjectPropertyExpression) aPropExpr, aMaxCard.getCardinality());
			}
			else throw new UnexpectedTypeException(OWLMaxCardinality.class, aPropExpr);
		}
		else throw new UnexpectedTypeException(OWLCardinalityBase.class, theCardRest);

		return aReturn;
	}

	public OWLDataOneOf convertOWLDataRange(
			edu.stanford.smi.protegex.owl.model.OWLDataRange theDataRange)
			throws ConversionException {

		RDFList aList = theDataRange.getOneOf();
		Set<OWLConstant> aConsts = new HashSet<OWLConstant>();
		for (Object aObj : aList.getValueLiterals()) {
			RDFSLiteral aLit = (RDFSLiteral) aObj;
			aConsts.add(convertLiteral(aLit));
		}
		return mFactory.getOWLDataOneOf(aConsts);
	}

	public OWLDescription convertQuantifierRestriction(
			edu.stanford.smi.protegex.owl.model.OWLQuantifierRestriction theQuant) throws ConversionException {
		OWLDescription aReturn = null;

		OWLPropertyExpression aPropExpr = convertProperty(theQuant.getOnProperty());

		boolean isAll = true;

		if (theQuant instanceof edu.stanford.smi.protegex.owl.model.OWLSomeValuesFrom) {
			isAll = false;
		}

		if (aPropExpr instanceof OWLDataPropertyExpression) {
			if (theQuant.getFiller() instanceof RDFList) {
				Set<OWLConstant> aConsts = new HashSet<OWLConstant>();

				RDFList aList = (RDFList) theQuant.getFiller().as(RDFList.class);
				for (Object aObj : aList.getValueLiterals()) {
					RDFSLiteral aLit = (RDFSLiteral) aObj;

					aConsts.add(convertLiteral(aLit));
				}

				if (isAll) {
					aReturn = mFactory.getOWLDataAllRestriction(
							(OWLDataPropertyExpression) aPropExpr, mFactory
									.getOWLDataOneOf(aConsts));
				}
				else {
					aReturn = mFactory.getOWLDataSomeRestriction(
							(OWLDataPropertyExpression) aPropExpr, mFactory
									.getOWLDataOneOf(aConsts));
				}
			}
			else {
				if (isAll) {
					aReturn = mFactory.getOWLDataAllRestriction(
							(OWLDataPropertyExpression) aPropExpr, mFactory.getOWLDataType(getURI(theQuant.getFiller())));
				}
				else {
					aReturn = mFactory.getOWLDataSomeRestriction(
							(OWLDataPropertyExpression) aPropExpr, mFactory.getOWLDataType(getURI(theQuant.getFiller())));
				}
			}
		}
		else if (aPropExpr instanceof OWLObjectPropertyExpression) {
			if (isAll) {
				aReturn = mFactory.getOWLObjectAllRestriction(
						(OWLObjectPropertyExpression) aPropExpr, (OWLDescription) convert(theQuant
								.getFiller()));
			}
			else {
				aReturn = mFactory.getOWLObjectSomeRestriction(
						(OWLObjectPropertyExpression) aPropExpr, (OWLDescription) convert(theQuant
								.getFiller()));
			}
		}

		return aReturn;
	}

	public OWLIndividual convertIndividual(RDFResource theResource) throws ConversionException {
		final URI aUri = getURI(theResource);

		if (theResource.isAnonymous())
			return mFactory.getOWLAnonymousIndividual(aUri);
		else return mFactory.getOWLIndividual(aUri);
	}

	public OWLDataProperty convertDataProperty(OWLDatatypeProperty theProperty) throws ConversionException {
		return mFactory.getOWLDataProperty(getURI(theProperty));
	}

	public org.semanticweb.owl.model.OWLObjectProperty convertObjectProperty(
			OWLObjectProperty theProperty) throws ConversionException {
		return mFactory.getOWLObjectProperty(getURI(theProperty));
	}

	public OWLPropertyExpression convertProperty(RDFProperty theProperty)
			throws ConversionException {
		if (theProperty instanceof edu.stanford.smi.protegex.owl.model.OWLProperty) {
			// TODO: what about annotation properties?
			if (theProperty instanceof OWLObjectProperty)
				return convertObjectProperty((OWLObjectProperty) theProperty);
			else if (theProperty instanceof OWLDatatypeProperty)
				return convertDataProperty((OWLDatatypeProperty) theProperty);
			else {
				throw new UnexpectedTypeException(
						edu.stanford.smi.protegex.owl.model.OWLProperty.class, theProperty);
			}
		}
		else throw new UnexpectedTypeException(RDFProperty.class, theProperty);
	}

    /**
     * Convert the OWL-API OWLClass into its equivalent Protege OWLClass relative to the specified Protege model
     * @param theClass the class to convert
     * @param theModel the protege model to convert the class relative to
     * @return the equivalant Protege OWLClass
     */
    public edu.stanford.smi.protegex.owl.model.OWLNamedClass toProtegeClass(OWLClass theClass,
			OWLModel theModel) {
			return theModel.getOWLNamedClass(theModel.getResourceNameForURI(theClass.getURI()
				.toString()));
	}

    /**
     * Convert the OWL-API OWLIndividual into its equivalent protege Individual relative to the specified protege model
     * @param theInd the individual to convert
     * @param theModel the protege model to convert relative to
     * @return the equivalent Protege Individual
     */
    public edu.stanford.smi.protegex.owl.model.OWLIndividual toProtegeIndividual(
			OWLIndividual theInd, OWLModel theModel) {
		return theModel.getOWLIndividual(theInd.getURI().toString());
	}

    private static final String TAB = "&nbsp;&nbsp;&nbsp;&nbsp;";

    private static String render(HTMLObjectRenderer theRend, DefaultMutableTreeNode theNode, int theIndent) {
        StringBuilder aRenderedView = new StringBuilder();

        for (int i = 0; i < theIndent; i++) aRenderedView.append(TAB);

        aRenderedView.append(" * ").append(theRend.render( (OWLObject) theNode.getUserObject())).append("<br>\n");
        theRend.reset();

        for (int aIndex = 0; aIndex < theNode.getChildCount(); aIndex++) {
            aRenderedView.append(render(theRend, (DefaultMutableTreeNode) theNode.getChildAt(aIndex), theIndent+1));
        }

        return aRenderedView.toString();
    }

    private static void collectSides(DefaultMutableTreeNode theNode, OWLAxiomSideVisitor theVisitor) {

        ((OWLObject) theNode.getUserObject()).accept(theVisitor);

        for (int aIndex = 0; aIndex < theNode.getChildCount(); aIndex++) {
            collectSides((DefaultMutableTreeNode) theNode.getChildAt(aIndex), theVisitor);
        }
    }
    

	// ------------- new methods --------------------

	public OWLAxiom createPropertyInverseAxiom(OWLObjectProperty theProperty, OWLObjectProperty theInverseProperty) throws ConversionException {
		return mFactory.getOWLInverseObjectPropertiesAxiom(convertObjectProperty(theProperty),
				convertObjectProperty(theInverseProperty));
	}

	public OWLAxiom createPropertyValueAxiom(edu.stanford.smi.protegex.owl.model.OWLIndividual theIndividual,
			RDFProperty theProperty, Object theValue) throws ConversionException {

		if (theValue instanceof RDFResource) {
			//this will also treat RDFProperties
			org.semanticweb.owl.model.OWLObjectProperty objProp = mFactory.getOWLObjectProperty(getURI(theProperty));
			return mFactory.getOWLObjectPropertyAssertionAxiom(convertIndividual(theIndividual),
					objProp, convertIndividual((RDFResource)theValue));
		} else {
			OWLConstant constant = convertValue(theIndividual.getOWLModel(), theValue);
			//this will also treat RDFProperties
			OWLDataProperty dataProp = mFactory.getOWLDataProperty(getURI(theProperty));
			
			return mFactory.getOWLDataPropertyAssertionAxiom(convertIndividual(theIndividual), dataProp, constant);
		}
	}


	protected OWLConstant convertValue(OWLModel owlModel, Object theValue) throws ConversionException {
		OWLConstant value = null;
		
		if (theValue instanceof String) {
			return mFactory.getOWLUntypedConstant((String)theValue);
		} else if (theValue instanceof RDFSLiteral) {
			return convertLiteral((RDFSLiteral)theValue);
		} else {
			RDFSLiteral literal = null;
			try {
				literal = owlModel.asRDFSLiteral(theValue);
			} catch (Exception e) {
				throw new ConversionException("Could not convert value " + theValue);
			}
			
			if (literal != null) {
				value = convertLiteral((RDFSLiteral)literal);
			} else {
				throw new UnexpectedTypeException(theValue);
			}
		}
		
		return value;
	}
	
	
	
	public OWLAxiom createAnnotationAxiom(RDFResource theResource,
			RDFProperty theAnnotationProperty, Object theValue) throws ConversionException {

		if (!theAnnotationProperty.isAnnotationProperty()) {
			throw new UnexpectedTypeException(theAnnotationProperty);
		}

		if (theValue instanceof RDFResource) {
			OWLAnnotation anno = mFactory.getOWLObjectAnnotation(getURI(theAnnotationProperty), convertIndividual((RDFResource) theValue));
			return mFactory.getOWLEntityAnnotationAxiom((OWLEntity)convert(theResource), anno);			
		} else if (theValue instanceof String) {
			OWLConstant value = mFactory.getOWLUntypedConstant((String)theValue);
			OWLAnnotation anno = mFactory.getOWLConstantAnnotation(getURI(theAnnotationProperty), value);
			return mFactory.getOWLEntityAnnotationAxiom((OWLEntity)convert(theResource), anno);
		} else if (theValue instanceof RDFSLiteral) {
			OWLConstant value = convertLiteral((RDFSLiteral)theValue);
			OWLAnnotation anno = mFactory.getOWLConstantAnnotation(getURI(theAnnotationProperty), value);
			return mFactory.getOWLEntityAnnotationAxiom((OWLEntity)convert(theResource), anno);			
		} else {
			throw new UnexpectedTypeException(theValue);
		}
	}

	
	public OWLDifferentIndividualsAxiom convertDifferentIndividualsAxiom(Collection<edu.stanford.smi.protegex.owl.model.OWLIndividual> individuals)
		throws ConversionException {
		Set<OWLIndividual> differents = convertListOfIndividuals(individuals);
		
		return mFactory.getOWLDifferentIndividualsAxiom(differents);
	}
	
	
	public FrameID getAnonymousFrameID(OWLDescription owlDesc) {
		//find a better way for the reverse lookup
		for (Iterator iterator = mAnonClsCache.keySet().iterator(); iterator.hasNext();) {
			FrameID frameID = (FrameID) iterator.next();
			if (mAnonClsCache.get(frameID).equals(owlDesc)) {
				return frameID;
			}
		}

		return null;
	}
}
