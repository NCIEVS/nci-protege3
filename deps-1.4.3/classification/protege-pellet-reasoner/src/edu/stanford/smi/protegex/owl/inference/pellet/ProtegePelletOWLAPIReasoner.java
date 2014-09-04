package edu.stanford.smi.protegex.owl.inference.pellet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.mindswap.pellet.KnowledgeBase;
import org.mindswap.pellet.Pellet;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLPropertyExpression;

import edu.stanford.smi.protegex.owl.inference.reasoner.exception.ProtegeReasonerException;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.owlapi.converter.OWLAPIConverter;
import edu.stanford.smi.protegex.owl.owlapi.converter.exception.ConversionException;

public class ProtegePelletOWLAPIReasoner extends AbstractProtegePelletReasoner {

	private OWLOntologyManager owlOntologyManager;

	private OWLOntology owlApiOntology;

	private OWLAPIConverter owlApiConverter;

	private Reasoner pelletReasoner;


	public ProtegePelletOWLAPIReasoner() {
		owlOntologyManager = OWLManager.createOWLOntologyManager();
		pelletReasoner = new Reasoner(owlOntologyManager);
	}
	

	public OWLOntology getOwlApiOntology() throws ProtegeReasonerException {
		if (owlApiOntology == null) {			
			try {	
				owlApiConverter = new OWLAPIConverter(owlModel, owlOntologyManager);
				owlApiConverter.setConvertAnnotations(false);
				
				owlApiOntology = owlApiConverter.convert();				
				
				pelletReasoner.setOntology(owlApiOntology);	

			} catch (ConversionException e) {
				throw new ProtegeReasonerException("Could not convert Protege-OWL model to OWL API model", e);
			}			
		}
		
		return owlApiOntology;
	}
	
	public OWLAPIConverter getOWLAPIConverter() {
		if (owlApiConverter == null) {
			rebind();
		}
		
		return owlApiConverter;
	}
	
	public Reasoner getPelletReasoner() {
		return pelletReasoner;
	}

	public KnowledgeBase getPelletKB() {
		return pelletReasoner.getKB();
	}
	
	public void reset() {
		if (owlApiConverter != null) {
			owlApiConverter.dispose();
		} 
		if (pelletReasoner != null) {
			//TT: is this needed?
			pelletReasoner.clearOntologies();
		}
		owlApiOntology = null;
	}
	

	@Override
	public void rebind() {
		super.rebind(); // this will call reset() and setOWLModel()
		try {
			owlApiOntology = getOwlApiOntology();
		} catch (ProtegeReasonerException e) {
			//how to handle here the exception?
			throw new RuntimeException(e);
		}
	}


	@Override
	public void dispose() {
		reset();		
		pelletReasoner.dispose();
		super.dispose();
	}
	
	public static String getReasonerName() {
		return "Pellet " + Pellet.getVersionInfo().getVersionString() + " (direct)";
	}
	
	
	// **************************************
	
	@Override
	public Collection<OWLClass> getSuperclasses(OWLClass owlClass) throws ProtegeReasonerException {
		Collection<OWLClass> classes = new HashSet<OWLClass>();
		
		try {
			classes = getOWLAPIConverter().getOWLClassSetFromEquivSet(pelletReasoner.getSuperClasses(owlApiConverter.getOWLAPIClass(owlClass)));
		} catch (ConversionException e) {
			throw new ProtegeReasonerException("Conversion exception " + owlClass, e);
		}
		
		return classes;
	}
	
	@Override
	public boolean isSatisfiable(OWLClass owlClass) throws ProtegeReasonerException {
		try {
			return pelletReasoner.isSatisfiable(getOWLAPIConverter().getOWLAPIClass(owlClass));
		} catch (ConversionException e) {
			throw new ProtegeReasonerException("Conversion exception " + owlClass, e);
		}
	}
	
	@Override
	public boolean isDisjointTo(OWLClass cls1, OWLClass cls2) throws ProtegeReasonerException {
		try {
			return pelletReasoner.isDisjointWith(getOWLAPIConverter().getOWLAPIClass(cls1), getOWLAPIConverter().getOWLAPIClass(cls2));
		} catch (ConversionException e) {
			throw new ProtegeReasonerException("Conversion exception", e);
		}
	}
	
	@Override
	public boolean isSubsumedBy(OWLClass subclass, OWLClass superclass) throws ProtegeReasonerException {
		try {
			return pelletReasoner.isSubClassOf(getOWLAPIConverter().getOWLAPIClass(subclass), getOWLAPIConverter().getOWLAPIClass(superclass));
		} catch (ConversionException e) {
			throw new ProtegeReasonerException("Conversion exception", e);
		}
	}
	
	@Override
	public Collection<OWLClass> getEquivalentClasses(OWLClass owlClass)
			throws ProtegeReasonerException {
		Collection<OWLClass> classes = new HashSet<OWLClass>();
		
		try {
			classes = getOWLAPIConverter().getOWLClassSet(pelletReasoner.getEquivalentClasses((getOWLAPIConverter().getOWLAPIClass(owlClass))));
		} catch (ConversionException e) {
			throw new ProtegeReasonerException("Conversion exception " + owlClass, e);
		}
		
		return classes;
	}
	

	@Override
	public Collection<OWLClass> getAncestorClasses(OWLClass owlClass) throws ProtegeReasonerException {
		Collection<OWLClass> classes = new HashSet<OWLClass>();
		
		try {
			classes = getOWLAPIConverter().getOWLClassSetFromEquivSet(pelletReasoner.getAncestorClasses(getOWLAPIConverter().getOWLAPIClass(owlClass)));
		} catch (ConversionException e) {
			throw new ProtegeReasonerException("Conversion exception " + owlClass, e);
		}
		
		return classes;
	}
	
	@Override
	public Collection<OWLClass> getDescendantClasses(OWLClass owlClass)
			throws ProtegeReasonerException {
		Collection<OWLClass> classes = new HashSet<OWLClass>();
		
		try {
			classes = getOWLAPIConverter().getOWLClassSetFromEquivSet(pelletReasoner.getDescendantClasses(getOWLAPIConverter().getOWLAPIClass(owlClass)));
		} catch (ConversionException e) {
			throw new ProtegeReasonerException("Conversion exception " + owlClass, e);
		}
		
		return classes;
	}
	
	@Override
	public Collection<OWLClass> getSubclasses(OWLClass owlClass) throws ProtegeReasonerException {
		Collection<OWLClass> classes = new HashSet<OWLClass>();
		
		try {
			classes = getOWLAPIConverter().getOWLClassSetFromEquivSet(pelletReasoner.getSubClasses(getOWLAPIConverter().getOWLAPIClass(owlClass)));
		} catch (ConversionException e) {
			throw new ProtegeReasonerException("Conversion exception " + owlClass, e);
		}
		
		return classes;
	}
	
	
	@Override
	public Collection<OWLClass> getIndividualDirectTypes(OWLIndividual individual) throws ProtegeReasonerException {
		Collection<OWLClass> types = new HashSet<OWLClass>();
		
		try {
			types = getOWLAPIConverter().getOWLClassSetFromEquivSet(pelletReasoner.getTypes(getOWLAPIConverter().getOWLAPIIndividual(individual), true));
		} catch (ConversionException e) {
			throw new ProtegeReasonerException("Conversion exception " + individual, e);
		}
		
		return types;
	}
	
	@Override
	public Collection<OWLClass> getIndividualTypes(OWLIndividual individual) throws ProtegeReasonerException {
		Collection<OWLClass> types = new HashSet<OWLClass>();
		
		try {
			types = getOWLAPIConverter().getOWLClassSetFromEquivSet(pelletReasoner.getTypes(getOWLAPIConverter().getOWLAPIIndividual(individual), false));
		} catch (ConversionException e) {
			throw new ProtegeReasonerException("Conversion exception " + individual, e);
		}
		
		return types;
	}
	
	@Override
	public Collection<OWLIndividual> getIndividualsBelongingToClass(OWLClass owlClass) throws ProtegeReasonerException {
		Collection<OWLIndividual> indvs = new HashSet<OWLIndividual>();
		
		try {
			indvs = getOWLAPIConverter().getOWLIndividualsSet(pelletReasoner.getIndividuals(getOWLAPIConverter().getOWLAPIClass(owlClass), false));
		} catch (ConversionException e) {
			throw new ProtegeReasonerException("Conversion exception at getting individuals of " + owlClass, e);
		}
		
		return indvs;
	}
	
	
	@Override
	public Collection getRelatedValues(OWLIndividual subject, OWLDatatypeProperty datatypeProperty)
			throws ProtegeReasonerException {
		
		Collection values = new ArrayList();
		
		try {
			values = getOWLAPIConverter().getLiterals(pelletReasoner.getRelatedValues(getOWLAPIConverter().getOWLAPIIndividual(subject),
					(OWLDataPropertyExpression) getOWLAPIConverter().getOWLProperty(datatypeProperty)));
		} catch (ConversionException e) {
			throw new ProtegeReasonerException("Conversion exception at getting related values of " + 
					subject + " property " + datatypeProperty, e);
		}
		
		
		return values;
	}
	
	@Override
	public Collection<OWLIndividual> getRelatedIndividuals(OWLIndividual subject,
			OWLObjectProperty objectProperty) throws ProtegeReasonerException {
		
		Set<OWLIndividual> values = new HashSet<OWLIndividual>();
		 
		try {
			org.semanticweb.owl.model.OWLIndividual ind = getOWLAPIConverter().getOWLAPIIndividual(subject);
			OWLPropertyExpression prop = getOWLAPIConverter().getOWLProperty(objectProperty);
			values = getOWLAPIConverter().getOWLIndividualsSet(pelletReasoner.getRelatedIndividuals(ind, (OWLObjectPropertyExpression) prop));
			
		} catch (ConversionException e) {
			throw new ProtegeReasonerException("Conversion exception at getting related individuals of " + 
					subject + " property " + objectProperty, e);
		}
		
		return values;
	}
	
	@Override
	public Collection<OWLClass> getSuperclassesOfIntersection(OWLClass[] clses)
			throws ProtegeReasonerException {
		// TODO Auto-generated method stub
		return super.getSuperclassesOfIntersection(clses);
	}
	
	// ******************************** Conversions *****************************************

}
