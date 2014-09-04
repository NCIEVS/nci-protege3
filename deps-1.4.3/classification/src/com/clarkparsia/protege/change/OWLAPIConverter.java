package com.clarkparsia.protege.change;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.inference.OWLReasonerAdapter;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntologyStorageException;
import org.semanticweb.owl.model.OWLPropertyExpression;
import org.semanticweb.owl.model.OWLTypedConstant;
import org.semanticweb.owl.model.OWLUntypedConstant;
import org.semanticweb.owl.model.UnknownOWLOntologyException;

import com.clarkparsia.protege.exceptions.ConversionException;

import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.util.Disposable;
import edu.stanford.smi.protege.util.ErrorHandler;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.inference.protegeowl.log.ReasonerLogRecord;
import edu.stanford.smi.protegex.owl.inference.protegeowl.log.ReasonerLogRecordFactory;
import edu.stanford.smi.protegex.owl.inference.protegeowl.log.ReasonerLogger;
import edu.stanford.smi.protegex.owl.inference.util.ReasonerUtil;
import edu.stanford.smi.protegex.owl.model.OWLAllDifferent;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.OWLProperty;
import edu.stanford.smi.protegex.owl.model.ProtegeNames;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSLiteral;


/*
 * Based on the code from -r16255 http://smi-protege.stanford.edu/repos/protege/protege-pellet-reasoner/trunk
 */

/**
 * Class for converting a Protege-OWL model into a OWL-API model.
 * The conversion of the DL-part of the Protege-OWL model is complete. (If not, then please report the bugs!)
 * See Limitation section for more details on things that are not converted.
 * <p>
 * <b>Limitations </b>
 * <li> Simple annotation properties that do not have a value at an entity are not converted.
 *  Datatype and object annotation properties that do not have a value at an entity are
 *  converted as datatype or object properties, respectively.  </li>
 * <li> Meta-model entities are not converted (meta-classes, meta-properties, etc.) </li>
 * </p>
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class OWLAPIConverter implements Disposable {
	private ErrorHandler<ConversionException> errorHandler;
		
	private OWLOntologyManager owlManager;
	private Converter converter;
	private OWLModel owlModel;
	private OWLOntology _ont;
		
	private boolean convertAnnotation = true;

	private List<RDFProperty> ignoredProperties = new ArrayList<RDFProperty>();	

	public OWLAPIConverter(OWLModel owlModel) {
		this(owlModel, OWLManager.createOWLOntologyManager());
	}
	
	public OWLAPIConverter(OWLModel owlModel, OWLOntologyManager manager) {
		this.owlModel = owlModel;
		this.owlManager = manager;
		this.converter = new Converter(owlManager.getOWLDataFactory());		
		this.errorHandler = new OWLAPIConverterErrorHandler();
		this.convertAnnotation = true;
		initIgnoredPropertiesList();
	}
		

	public void setErrorHandler(ErrorHandler<ConversionException> errorHandler) {
		this.errorHandler = errorHandler;
	}
	
	public boolean isConvertAnnotations() {
		return convertAnnotation;
	}
	
	public void setConvertAnnotations(boolean convertAnnotationProperties) {
		this.convertAnnotation = convertAnnotationProperties;
	}

	protected OWLOntology createTopOntology() throws ConversionException {
		String ontologyName= owlModel.getTripleStoreModel().getTopTripleStore().getName();
		try {
			_ont = owlManager.createOntology(URI.create(ontologyName));
		} catch (OWLOntologyCreationException e) {
			errorHandler.fatalError(new ConversionException("Could not create ontology " + ontologyName, e));
		}

		return _ont;		
	}


	public OWLOntology convert() throws ConversionException {		
		this._ont = createTopOntology();

		//TODO: convert deprecated classes and properties
		//TODO: convert imports
		
		convertClasses(_ont);		
		convertProperties(_ont);
		convertIndividuals(_ont);		
		convertAllDifferent(_ont);
		
		if (convertAnnotation == true) {
			convertOntologyAnnotations(_ont);		
		}

		return _ont;
	}


	protected void convertClasses(OWLOntology ont) throws ConversionException {
		for (Iterator iterator = ReasonerUtil.getInstance().getNamedClses(owlModel).iterator(); iterator.hasNext();) {
			OWLNamedClass owlClass = (OWLNamedClass) iterator.next();

			convertClass(ont, owlClass);
		}		
	}


	protected void convertClass(OWLOntology ont, OWLClass owlClass) throws ConversionException {
		//convert declaration		
		try {
			addAxiom(ont, converter.createDeclarationAxiom(owlClass));
		} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
			errorHandler.error(new ConversionException("Cannot convert class " + owlClass, e));
		}

		//convert equivalent classes
		for (Iterator iterator = owlClass.getEquivalentClasses().iterator(); iterator.hasNext();) {
			OWLClass equivClass = (OWLClass) iterator.next();
			try {
				addAxiom(ont, converter.createEquivalentClassesAxiom(owlClass, equivClass));
			} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
				errorHandler.error(new ConversionException("Cannot convert equivalent classes of " + owlClass, e));
			}			
		}

		//convert subclass-of
		for (Iterator iterator = owlClass.getPureSuperclasses().iterator(); iterator.hasNext();) {
			//rdfs:Class will be treated as owl:Class - maybe we should change this
			RDFSClass superClass = (RDFSClass) iterator.next(); 
			try {
				addAxiom(ont, converter.createSubClassAxiom(owlClass, superClass));
			} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
				errorHandler.error(new ConversionException("Cannot convert subclasses of " + owlClass, e));
			}			
		}

		//convert disjoint					
		Collection<RDFSClass> disjClasses = owlClass.getDisjointClasses();
		
		for (RDFSClass disjClass : disjClasses) {
			try {
				addAxiom(ont, converter.createDisjointClassesAxiom(owlClass, disjClass));
			} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
				errorHandler.error(new ConversionException("Cannot convert disjoint classes of " + owlClass, e));
			}
		}					
			
		if (convertAnnotation == true) {
			convertAnnotationProperties(ont, owlClass);
		}

	}

	protected void convertProperties(OWLOntology ont) throws ConversionException {
		for (Iterator iterator = ReasonerUtil.getInstance().getObjectProperties(owlModel).iterator(); iterator.hasNext();) {
			RDFProperty prop = (RDFProperty) iterator.next();
			
			if (!(prop instanceof OWLObjectProperty)) {
				errorHandler.warning(new ConversionException("Found object property: " + prop.getName() + " with unexpected Java type " + prop.getClass().getSimpleName() + 
						".\nOntology property type(s): " + prop.getProtegeTypes()));				
			} else {			
				convertObjectProperty(ont, (OWLObjectProperty) prop);
			}
		}

		for (Iterator iterator = ReasonerUtil.getInstance().getDataTypeProperties(owlModel).iterator(); iterator.hasNext();) {
			RDFProperty prop = (RDFProperty) iterator.next();
			
			if (!(prop instanceof OWLDatatypeProperty)) {
				errorHandler.warning(new ConversionException("Found datatype property: " + prop.getName() + " with unexpected Java type " + prop.getClass().getSimpleName() +
						".\nOntology property type(s): " + prop.getProtegeTypes()));		
			} else {			
				convertDatatypeProperty(ont, (OWLDatatypeProperty) prop);
			}
			
		}
		//FIXME: how to convert annotation properties? Could not find in OWLAPI a way to create them

	}


	protected void convertObjectProperty(OWLOntology ont, OWLObjectProperty prop) throws ConversionException {		
		try {
			addAxiom(ont, converter.createDeclarationAxiom(prop));
		} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
			errorHandler.error(new ConversionException("Cannot convert " + prop, e));
		}

		//sub-properties
		convertSubProperties(ont, prop);

		//domain		
		try {
			addAxiom(ont, converter.createDomainAxiom(prop.getDomains(false), prop));
		} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
			errorHandler.error(new ConversionException("Cannot convert domain of " + prop, e));
		}

		//range
		for (Iterator iterator = prop.getRanges(false).iterator(); iterator.hasNext();) {
			RDFSClass range = (RDFSClass) iterator.next();			
			try {
				addAxiom(ont, converter.createObjectPropertyRangeAxiom(prop, range));
			} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
				errorHandler.error(new ConversionException("Cannot convert range " + range + " of " + prop, e));
			}
		}

		//inverse
		OWLObjectProperty invProp = (OWLObjectProperty) prop.getInverseProperty();
		if (invProp != null) {
			try {
				addAxiom(ont, converter.createPropertyInverseAxiom(prop, invProp));
			} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
				errorHandler.error(new ConversionException("Cannot convert the inverse property of " + prop, e));	
			}
		}

		//functional
		if (prop.isFunctional()) {			
			try {
				addAxiom(ont, converter.createFunctionalAxiom(prop));
			} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
				errorHandler.error(new ConversionException("Cannot convert: " + prop + " is functional", e));
			}
		}

		//symmetric
		if (prop.isSymmetric()) {
			try {
				addAxiom(ont, converter.createSymmetricAxiom(prop));
			} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
				errorHandler.error(new ConversionException("Cannot convert: " + prop + " is symmetric", e));
			}
		}

		//transitive
		if (prop.isTransitive()) {
			try {
				addAxiom(ont, converter.createTransitiveAxiom(prop));
			} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
				errorHandler.error(new ConversionException("Cannot convert: " + prop + " is transitive", e));
			}
		}

		//inv functional
		if (prop.isInverseFunctional()) {
			try {
				addAxiom(ont, converter.createInverseFunctionalAxiom(prop));
			} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
				errorHandler.error(new ConversionException("Cannot convert: " + prop + " is inverse functional", e));
			}
		}

		//equiv prop
		for (Iterator iterator = prop.getEquivalentProperties().iterator(); iterator.hasNext();) {
			RDFProperty equivProp = (RDFProperty) iterator.next();
			try {
				addAxiom(ont, converter.createEquivalentPropertiesAxiom((OWLObjectProperty)prop, equivProp));
			} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
				errorHandler.error(new ConversionException("Cannot convert the equivalent properties of " + prop, e));
			}
		}

		if (convertAnnotation == true) {
			convertAnnotationProperties(ont, prop);
		}
	}

	protected void convertDatatypeProperty(OWLOntology ont, OWLProperty prop) throws ConversionException {		
		try {
			addAxiom(ont, converter.createDeclarationAxiom(prop));
		} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
			errorHandler.error(new ConversionException("Cannot convert " + prop, e));
		}

		convertSubProperties(ont, prop);

		//domain		
		try {
			addAxiom(ont, converter.createDomainAxiom(prop.getDomains(false), prop));
		} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
			errorHandler.error(new ConversionException("Cannot convert domain of " + prop, e));		
		}

		//range
		for (Iterator iterator = prop.getRanges(false).iterator(); iterator.hasNext();) {
			RDFResource range = (RDFResource) iterator.next();			
			try {
				addAxiom(ont, converter.createDataPropertyRangeAxiom(prop, range));
			} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
				errorHandler.error(new ConversionException("Cannot convert range " + range + " of " + prop, e));			
			}
		}

		//functional
		if (prop.isFunctional()) {			
			try {
				addAxiom(ont, converter.createFunctionalAxiom(prop));
			} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
				errorHandler.error(new ConversionException("Cannot convert: " + prop + " is functional", e));
			}
		}
		
		//equiv prop
		for (Iterator iterator = prop.getEquivalentProperties().iterator(); iterator.hasNext();) {
			RDFProperty equivProp = (RDFProperty) iterator.next();
			try {
				addAxiom(ont, converter.createEquivalentPropertiesAxiom((OWLDatatypeProperty)prop, equivProp));
			} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
				errorHandler.error(new ConversionException("Cannot convert the equivalent properties of " + prop, e));
			}
		}

		if (convertAnnotation == true) {
			convertAnnotationProperties(ont, prop);
		}
	}


	protected void convertSubProperties(OWLOntology ont, OWLProperty prop) throws ConversionException {
		for (Iterator iterator = prop.getSuperproperties(false).iterator(); iterator.hasNext();) {
			RDFProperty superProp = (RDFProperty) iterator.next();
			try {
				addAxiom(ont, converter.createSubPropertyAxiom(prop, superProp));
			} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
				errorHandler.error(new ConversionException("Cannot convert subproperty axiom " +
						superProp + ", " + prop));
			}
		}

	}


	protected void convertIndividuals(OWLOntology ont) throws ConversionException {
		for (Iterator iterator = ReasonerUtil.getInstance().getIndividuals(owlModel).iterator(); iterator.hasNext();) {		
			OWLIndividual individual = (OWLIndividual) iterator.next();
			convertIndividual(ont, individual);
		}	
	}


	protected void convertIndividual(OWLOntology ont, OWLIndividual individual) throws ConversionException {
		//types
		for (Iterator iterator = individual.getRDFTypes().iterator(); iterator.hasNext();) {
			RDFSClass type = (RDFSClass) iterator.next();
			try {
				addAxiom(ont, converter.createClassAssertionAxiom(individual, type));
			} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
				errorHandler.error(new ConversionException("Cannot convert " + individual, e));
			}
		}
		
		//same as
		Collection sameAs = individual.getSameAs();
		if (sameAs != null && sameAs.size() > 0) {
			try {
				addAxiom(ont, converter.createSameIndividualsAxiom(individual, sameAs));			
			} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
				errorHandler.error(new ConversionException("Cannot convert sameAs of " + individual, e));
			}
		}
		
		//differentFrom
		Collection diffFrom = individual.getDifferentFrom();
		if (diffFrom != null && diffFrom.size() > 0) {
			try {
				addAxiom(ont, converter.createDifferentFromAxiom(individual, diffFrom));			
			} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
				errorHandler.error(new ConversionException("Cannot convert sameAs of " + individual, e));
			}
		}

		//property values
		for (Iterator iterator = individual.getRDFProperties().iterator(); iterator.hasNext();) {
			RDFProperty prop = (RDFProperty) iterator.next();
			Collection values = individual.getPropertyValues(prop);

			//is there another property except the rdf:type that should be filtered out?
			if (values != null && !prop.isAnnotationProperty() && !isIgnoredProperty(prop)) {
				for (Iterator iterator2 = values.iterator(); iterator2.hasNext();) {
					Object value = (Object) iterator2.next();
					try {
						addAxiom(ont, converter.createPropertyValueAxiom(individual, prop, value));
					} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
						errorHandler.error(new ConversionException("Cannot convert " + (individual == null ? "(null)" : individual.getName()) + "." +
								(prop == null ? "(null)" : prop.getName()) + " = " + value, e));
					}
				}
			}			
		}	
		
		if (convertAnnotation == true) {
			convertAnnotationProperties(ont, individual);
		}
	}


	protected void convertAnnotationProperties(OWLOntology ont, RDFResource resource) throws ConversionException {

		for (Iterator iterator = resource.getRDFProperties().iterator(); iterator.hasNext();) {
			RDFProperty prop = (RDFProperty) iterator.next();

			if (prop.isAnnotationProperty()) {
				for (Iterator iterator2 = resource.getPropertyValues(prop).iterator(); iterator2.hasNext();) {
					Object value = (Object) iterator2.next();
					try {
						addAxiom(ont, converter.createAnnotationAxiom(resource, prop, value));
					} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
						errorHandler.error(new ConversionException("Cannot convert annotation of " + resource +
								"." + prop + " = " + value, e));					
					}					
				}
			}
		}		
	}

	protected void convertOntologyAnnotations(OWLOntology ont) throws ConversionException {
		edu.stanford.smi.protegex.owl.model.OWLOntology pOntology = owlModel.getDefaultOWLOntology();		
		convertAnnotationProperties(ont, pOntology);

	}
	
	
	protected void convertAllDifferent(OWLOntology ont) throws ConversionException {
		Collection allDifferents = owlModel.getOWLAllDifferents();
		
		for (Iterator iterator = allDifferents.iterator(); iterator.hasNext();) {
			OWLAllDifferent allDifferent = (OWLAllDifferent) iterator.next();
			try {
				addAxiom(ont, converter.convertDifferentIndividualsAxiom((Collection<OWLIndividual>) allDifferent.getDistinctMembers()));
			} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
				errorHandler.error(new ConversionException("Cannot convert owl:AllDifferent " + allDifferent, e));
			}
		}		
	}
	

	public void save(URI uri) {		
		try {
			owlManager.saveOntology(_ont, uri);
		} catch (UnknownOWLOntologyException e) {
			throw new RuntimeException(e);
		} catch (OWLOntologyStorageException e) {
			throw new RuntimeException(e);
		}		
	}

	protected void addAxiom(OWLOntology ont, OWLAxiom axiom) throws ConversionException {
		if (axiom == null) {
			return;
		}
		AddAxiom addAxiom = new AddAxiom(ont, axiom);
		try {
			owlManager.applyChange(addAxiom);
		} catch (OWLOntologyChangeException e) {
			errorHandler.error(new ConversionException("Cannot add axiom " + axiom + " to ontology " + ont, e));		
		}	
	}

	
	// **************************** simple conversions *********************************


	public OWLDescription getOWLAPIClass(RDFSClass rdfsClass) throws ConversionException {
		OWLDescription desc = null;
		
		try {			
			desc = converter.convertClass(rdfsClass);
		} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
			errorHandler.error(new ConversionException("Cannot convert " + rdfsClass, e));			
		}
		
		return desc;		
	}
	
	public org.semanticweb.owl.model.OWLIndividual getOWLAPIIndividual(RDFResource resource) throws ConversionException {
		org.semanticweb.owl.model.OWLIndividual ind = null;
		
		try {			
			ind = converter.convertIndividual(resource);
		} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
			errorHandler.error(new ConversionException("Cannot convert " + resource, e));			
		}
		
		return ind;
	}
	
	public OWLPropertyExpression getOWLProperty(RDFProperty prop) throws ConversionException {
		OWLPropertyExpression propExp = null;
		
		try {
			propExp = converter.convertProperty(prop);
		} catch (com.clarkparsia.protege.exceptions.ConversionException e) {
			errorHandler.error(new ConversionException("Cannot convert " + prop, e));	
		}
		
		return propExp;
	}
		

	protected <T1, T2 extends OWLObject> Set<T1> getRDFResourceSetOfSets(Set<Set<T2>> owlDescSet ) throws ConversionException {
		Set<T2> flattenedSet = OWLReasonerAdapter.flattenSetOfSets(owlDescSet);
		return getRDFResourceSet(flattenedSet);
	}
	
	
	protected <T1, T2 extends OWLObject> Set<T1> getRDFResourceSet(Set<T2> owlDescSet) throws ConversionException {
		Set<T1> owlEntities = new HashSet<T1>();

		Iterator<T2> i = owlDescSet.iterator();

		if(i.hasNext()) {
			while (i.hasNext()) {
				Object next = i.next();

				RDFResource owlEntity = getProtegeOWLEntity((T2) next);

				if (owlEntity == null) {						
					Log.getLogger().warning("Can't find OWL resource: " + next);
				} else {
					try {
						owlEntities.add((T1)owlEntity);
					} catch (Exception e) {
						//TODO: throw exceptions
						Log.getLogger().warning("Found resource with wrong type: " + owlEntity);
					}
				}
			}
		}

		return owlEntities;
	}



	public Set<OWLClass> getOWLClassSetFromEquivSet(Set<Set<org.semanticweb.owl.model.OWLClass>> owlDescSet ) throws ConversionException {    	
		return getRDFResourceSetOfSets(owlDescSet); 
	}

	public Set<OWLClass> getOWLClassSet(Set<org.semanticweb.owl.model.OWLClass> owlDescSet) throws ConversionException {    	
		return getRDFResourceSet(owlDescSet); 
	}


	public Set<OWLIndividual> getOWLIndividualsSetFromEquivSet(Set<Set<org.semanticweb.owl.model.OWLIndividual>> owlDescSet ) throws ConversionException {    	    	
		return getRDFResourceSetOfSets(owlDescSet);    
	}

	
	public Set<OWLIndividual> getOWLIndividualsSet(Set<org.semanticweb.owl.model.OWLIndividual> owlDescSet ) throws ConversionException {    	    	
		return getRDFResourceSet(owlDescSet);    
	}

	
	public Set<OWLProperty> getOWLPropertiesSetFromEquivSet(Set<Set<OWLObject>> owlDescSet ) throws ConversionException {    	    	
		return getRDFResourceSetOfSets(owlDescSet);    
	}
	
	
	public RDFResource getProtegeOWLEntity(OWLObject owlObject) throws ConversionException {

		if (owlObject instanceof OWLNamedObject) {
			OWLNamedObject owlNamedObj = (OWLNamedObject) owlObject;
			return owlModel.getRDFResource(owlModel.getResourceNameForURI(owlNamedObj.getURI().toString()));			
		} else if (owlObject instanceof OWLDescription) {
			OWLDescription owlDesc = (OWLDescription) owlObject;			
			if (owlDesc.isAnonymous()) {
				FrameID fid = converter.getAnonymousFrameID(owlDesc);
				if (fid != null) {
					return (RDFResource) owlModel.getFrame(fid);
				}
			}		
		} 
		
		// for any other case
		//TODO: implement later:
		errorHandler.error(new ConversionException("Cannot convert " + owlObject + " to Protege OWL object." +
					" Unexpected type: " +owlObject));
		
		return null;
						
	}
	
	public Collection getLiterals(Set<OWLConstant> owlConstantSet) {
		ArrayList values = new ArrayList();
			
		for (OWLConstant constant : owlConstantSet) {
			values.add(getLiteral(constant));
		}
		
		return values;
	}
	
	protected Object getLiteral(OWLConstant owlConstant) {
		
		if (owlConstant.isTyped()) {
			URI datatypeUri = ((OWLTypedConstant)owlConstant).getDataType().getURI();
			return DefaultRDFSLiteral.create(owlModel, owlConstant.getLiteral(), owlModel.getRDFSDatatypeByURI(datatypeUri.toString()));
			
		} else {
			OWLUntypedConstant untypedConst = (OWLUntypedConstant) owlConstant;
			String lang = untypedConst.getLang();
			String text = untypedConst.getLiteral();
			
			if (lang != null && lang.length() > 0) {				
				return DefaultRDFSLiteral.create(owlModel, text, lang);
			}
			
			return text;
		}		
	}
	
	
	// **************************** end conversions ***************************
	
	public void dispose() {
		converter.reset();
		owlManager.removeOntology(_ont.getURI());
		//TODO: other clean-up?
	}
	
	// *************************** utility ***********************************
	
	protected void initIgnoredPropertiesList() {
		//protege system slots
		ignoredProperties.add(owlModel.getRDFProperty(ProtegeNames.Slot.INFERRED_SUBCLASSES));
		ignoredProperties.add(owlModel.getRDFProperty(ProtegeNames.Slot.INFERRED_SUBCLASSES));
		ignoredProperties.add(owlModel.getRDFProperty(ProtegeNames.Slot.INFERRED_TYPE));
		ignoredProperties.add(owlModel.getRDFProperty(ProtegeNames.Slot.CLASSIFICATION_STATUS));
		
		//the following properties are added explicitly
		ignoredProperties.add(owlModel.getRDFTypeProperty());
		ignoredProperties.add(owlModel.getOWLDifferentFromProperty());
		ignoredProperties.add(owlModel.getOWLSameAsProperty());
	}
	
	protected boolean isIgnoredProperty(RDFProperty property) {
		return ignoredProperties.contains(property);
	}
	
	
	// ************************* error handling ******************************
	
	private class OWLAPIConverterErrorHandler implements ErrorHandler<ConversionException>{

		public void error(ConversionException e) throws ConversionException {
			Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
			displayLogMessage(e, false);
		}

		public void fatalError(ConversionException e) throws ConversionException {
			displayLogMessage(e, false);
			throw e;
		}

		public void warning(ConversionException e) throws ConversionException {
			Log.getLogger().log(Level.WARNING, e.getMessage(), e);
			displayLogMessage(e, true);
		}	
		
		//FIXME: This should not be here, move to GUI code
		private void displayLogMessage(final ConversionException e, final boolean isWarning) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                	// Post an error log record
                    ReasonerLogger logger = ReasonerLogger.getInstance();
                    ReasonerLogRecordFactory factory = ReasonerLogRecordFactory.getInstance();
                    ReasonerLogRecord log = isWarning ? factory.createWarningMessageLogRecord(null, e.getMessage(), null) :
                    	factory.createErrorMessageLogRecord(e.getMessage(), null);
                    logger.postLogRecord(log);
                }
            });		
		}
	}
	
	public OWLOntologyManager getOWLOntologyManager(){
		return owlManager;
	}

}
