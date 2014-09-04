package com.clarkparsia.protege.change;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

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

import com.clarkparsia.protege.exceptions.ConversionException;
import com.clarkparsia.protege.exceptions.IncompleteInputException;
import com.clarkparsia.protege.exceptions.UnexpectedTypeException;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.util.Log;
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
import edu.stanford.smi.protegex.owl.model.OWLUnionClass;
import edu.stanford.smi.protegex.owl.model.RDFList;
import edu.stanford.smi.protegex.owl.model.RDFObject;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSDatatype;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Aug 22, 2007 2:43:53 PM
 * 
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class Converter {
	private final Logger LOGGER = Log.getLogger(Converter.class);

	private OWLDataFactory mFactory;
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

	public OWLDataFactory getFactory() {
		return mFactory;
	}

	public void reset() {
		mAnonClsCache.clear();
		mFirstMap.clear();
		mRestMap.clear();
	}

	private URI getURI(RDFResource theResource) throws ConversionException {
		if (theResource == null) throw new NullPointerException();

		final String aString = theResource.getURI();
		if (aString == null) throw new IncompleteInputException("Unable to get resource URI");

		try {
			return URI.create(aString);
		}
		catch (IllegalArgumentException e) {
			throw new ConversionException(e);
		}
	}

	public void cacheAnonCls(RDFSClass theClass, OWLDescription theDescription) {
		mAnonClsCache.put(theClass.getFrameID(), theDescription);
	}

	public void cacheIntersection(RDFSClass theClass, Collection<OWLDescription> theDescriptions) {
		cacheAnonCls(theClass, mFactory.getOWLObjectIntersectionOf(new HashSet<OWLDescription>(
				theDescriptions)));
	}

	public void cacheListFirst(FrameID theFrameID, RDFSClass theClass) throws ConversionException {
		OWLDescription aDescription = convertClass(theClass);
		if (aDescription == null)
			throw new NullPointerException("Class conversion failed, unable to save list first");
		mFirstMap.put(theFrameID, aDescription);
	}

	public void cacheListRest(FrameID theList, FrameID theRest) {
		if (theRest == null) theRest = theList;
		mRestMap.put(theList, theRest);
	}

	public List<OWLDescription> convertClassList(RDFList theList) throws ConversionException {

		List<OWLDescription> aReturn = new ArrayList<OWLDescription>();
		final FrameID aNilId = theList.getOWLModel().getRDFNil().getFrameID();
		RDFList aCurrent = theList;
		FrameID aCurrentID = theList.getFrameID();

		while( !aCurrentID.equals(aNilId) ) {

			OWLDescription aDescription = mFirstMap.get(aCurrentID);
			if (aDescription == null) {
				Object aObj = aCurrent.getFirst();
				if (aObj instanceof RDFSClass) {
					RDFSClass aCls = (RDFSClass) aObj;
					aDescription = convertClass(aCls);
				}
				else throw new UnexpectedTypeException("List element not an RDFSClass", aObj);
			}

			if (aDescription == null)
				throw new NullPointerException("Failed to convert first list element");

			aReturn.add(aDescription);

			aCurrent = aCurrent.getRest();
			aCurrentID = aCurrent.getFrameID();
		}

		return aReturn;
	}

	public void cacheURIForFrame(String theURI, Frame theFrame) {
		mURICache.put(theFrame, theURI);
	}

	public Set<OWLDescription> convertListOfClasses(Collection<RDFSClass> theProtegeList)
	throws ConversionException {
		Set<OWLDescription> aDescSet = new HashSet<OWLDescription>();

		for (RDFSClass aClass : theProtegeList) {
			aDescSet.add(convertClass(aClass));
		}

		return aDescSet;
	}

	public Set<OWLIndividual> convertListOfIndividuals(
			Collection<edu.stanford.smi.protegex.owl.model.OWLIndividual> theProtegeList)
			throws ConversionException {
		Set<OWLIndividual> aIndSet = new HashSet<OWLIndividual>();

		for (edu.stanford.smi.protegex.owl.model.OWLIndividual aInd : theProtegeList) {
			aIndSet.add(convertIndividual(aInd));
		}

		return aIndSet;
	}

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

	public OWLAxiom createDifferentFromAxiom(
			edu.stanford.smi.protegex.owl.model.OWLIndividual theRes, Collection theDifferent)
	throws ConversionException {
		Set<OWLIndividual> aIndSet = convertListOfIndividuals(theDifferent);
		aIndSet.add(convertIndividual(theRes));

		return mFactory.getOWLDifferentIndividualsAxiom(aIndSet);
	}

	public OWLAxiom createSubPropertyAxiom(RDFProperty theProperty, RDFProperty theSubProperty)
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
			else throw new UnexpectedTypeException(theRange);
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
		} else if (theObject instanceof RDFResource) {
			return convertIndividual((RDFResource) theObject);
		}
		else {
			LOGGER.fine("Unexpected type of RDFObject passed for conversion: "
					+ theObject.getClass().getName());
			throw new UnexpectedTypeException(theObject);
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
		else throw new UnexpectedTypeException(theProperty);
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
		else throw new UnexpectedTypeException(aProp);
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
		else throw new UnexpectedTypeException(aProp);
	}

	public OWLConstant convertLiteral(RDFSLiteral theLiteral) throws ConversionException {
		if (theLiteral.getLanguage() != null) {
			return mFactory.getOWLUntypedConstant(theLiteral.getString(), theLiteral.getLanguage());
		} else	if (theLiteral.getDatatype() != null) {
			return mFactory.getOWLTypedConstant(theLiteral.getString(), mFactory.getOWLDataType(getURI(theLiteral.getDatatype())));
		} else {
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
		else throw new UnexpectedTypeException(theClass);

		return aDesc;
	}

	public OWLDescription convertIntersection(OWLIntersectionClass theClass)
	throws ConversionException {
		return mFactory.getOWLObjectIntersectionOf(convertListOfClasses(theClass.getOperands()));
	}

	public OWLDescription convertUnion(OWLUnionClass theClass)
	throws ConversionException {
		return mFactory.getOWLObjectUnionOf(convertListOfClasses(theClass.getOperands()));
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
			String aURI = theClass.getURI();

			if (mURICache.containsKey(theClass)) {
				aURI = mURICache.get(theClass);
			}

			return mFactory.getOWLClass(URI.create(aURI));
		}
	}

	public OWLDescription convertRestriction(
			edu.stanford.smi.protegex.owl.model.OWLRestriction theRest) throws ConversionException {
		OWLDescription aReturn;

		if (theRest instanceof OWLCardinalityBase)
			aReturn = convertCardinalityRestriction((OWLCardinalityBase) theRest);
		else if (theRest instanceof OWLHasValue)
			aReturn = convertHasValueRestriction((OWLHasValue) theRest);
		else if (theRest instanceof OWLQuantifierRestriction)
			aReturn = convertQuantifierRestriction((OWLQuantifierRestriction) theRest);
		else throw new UnexpectedTypeException(theRest);

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
			else throw new UnexpectedTypeException(aPropExpr);
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
			else throw new UnexpectedTypeException(aPropExpr);
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
			else throw new UnexpectedTypeException(aPropExpr);
		}
		else throw new UnexpectedTypeException(theCardRest);

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
			else throw new UnexpectedTypeException(theProperty);
		}
		else throw new UnexpectedTypeException(theProperty);
	}

	public edu.stanford.smi.protegex.owl.model.OWLNamedClass toProtegeClass(OWLClass theClass,
			OWLModel theModel) {
		return theModel.getOWLNamedClass(theModel.getResourceNameForURI(theClass.getURI()
				.toString()));
	}

	public edu.stanford.smi.protegex.owl.model.OWLIndividual toProtegeIndividual(
			OWLIndividual theInd, OWLModel theModel) {
		return theModel.getOWLIndividual(theInd.getURI().toString());
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
	
	
	// ================ SWRL conversion =========================
	
	

}
