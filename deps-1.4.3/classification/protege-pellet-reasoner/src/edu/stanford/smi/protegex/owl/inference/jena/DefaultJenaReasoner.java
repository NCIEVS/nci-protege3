package edu.stanford.smi.protegex.owl.inference.jena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.jena.Jena;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.vocabulary.OWL;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.database.OWLDatabaseModel;
import edu.stanford.smi.protegex.owl.inference.protegeowl.ProtegeOWLReasoner;
import edu.stanford.smi.protegex.owl.inference.protegeowl.task.protegereasoner.UpdateInconsistentClassesTask;
import edu.stanford.smi.protegex.owl.inference.reasoner.AbstractProtegeReasoner;
import edu.stanford.smi.protegex.owl.inference.reasoner.exception.ProtegeReasonerException;
import edu.stanford.smi.protegex.owl.jena.creator.JenaCreator;
import edu.stanford.smi.protegex.owl.jena.protege2jena.Protege2Jena;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.OWLProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSDatatype;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;
import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSLiteral;
import edu.stanford.smi.protegex.owl.model.triplestore.TripleStore;
import edu.stanford.smi.protegex.owl.model.triplestore.TripleStoreModel;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLNames;


public class DefaultJenaReasoner extends AbstractProtegeReasoner implements JenaReasoner {

	protected OntModel ontModel;
	protected OntModelSpec ontModelSpec;

	public DefaultJenaReasoner() {
		super();	
		this.ontModelSpec = getDefaultOntModelSpec();		
	}

	public DefaultJenaReasoner(OntModelSpec ontModelSpec) {
		super();
		this.ontModelSpec = ontModelSpec;		
	}

	@Override
	public void initialize() {
		ontModel = getOntModel();
	}

	public void reset(){
		if (ontModel != null) {
			ontModel.close();
			ontModel = null;
		}
	}
	
	@Override
	public void rebind() {	
		super.rebind();
		ontModel = getOntModel();
	}
	
	
	public ValidityReport getValidityReport() throws ProtegeReasonerException {
		return getOntModel().validate();		
	}
	
	
	
	@Override
	public void computeInconsistentConcepts() throws ProtegeReasonerException {
		UpdateInconsistentClassesTask task = new UpdateInconsistentClassesTask(this);						
		performTask(task);	
	}
	
	
	@Override
	public Set<OWLClass> getAncestorClasses(OWLClass owlClass) throws ProtegeReasonerException {

		OntClass cls = (OntClass) getResource(owlClass).as(OntClass.class);				
		return getOWLClassSet(cls.listSuperClasses(false));
	}

	@Override
	public Set<OWLClass> getDescendantClasses(OWLClass owlClass) throws ProtegeReasonerException {
		OntClass cls = (OntClass) getResource(owlClass).as(OntClass.class);

		return getOWLClassSet(cls.listSubClasses(false));
	}

	@Override
	public Set<OWLClass> getEquivalentClasses(OWLClass owlClass) throws ProtegeReasonerException {

		OntClass cls = (OntClass) getResource(owlClass).as(OntClass.class);

		return getOWLClassSet(cls.listEquivalentClasses());
	}



	public Set<OWLClass> getInconsistentClasses()
	throws ProtegeReasonerException {
		OntClass owlNothing = getOntModel().getOntClass(OWL.Nothing.getURI());

		return getOWLClassSet(owlNothing.listEquivalentClasses()); 
	}

	@Override
	public Collection<OWLIndividual> getIndividualsBelongingToClass(OWLClass owlClass)
	throws ProtegeReasonerException {
		Set<OWLIndividual> individuals = new HashSet<OWLIndividual>();

		try {
			OntClass cls = (OntClass) getResource(owlClass).as(OntClass.class);

			individuals = getOWLIndividualsSet(cls.listInstances());
		} catch (Exception e) {
			throw(new ProtegeReasonerException(e.getMessage(), e));
		}

		return individuals;
	}


	@Override
	public Collection<OWLClass> getSuperclasses(OWLClass owlClass) throws ProtegeReasonerException {
		Set<OWLClass> superClses = new HashSet<OWLClass>();

		try {
			OntClass cls = (OntClass) getResource(owlClass).as(OntClass.class);
			superClses = getOWLClassSet(cls.listSuperClasses(true));
		} catch (Exception e) {			
			throw(new ProtegeReasonerException(e.getMessage(), e));			
		}

		return superClses;		
	}

	@Override
	public Collection<OWLClass> getSubclasses(OWLClass owlClass) throws ProtegeReasonerException {
		Set<OWLClass> subClses = new HashSet<OWLClass>();

		try {
			OntClass cls = (OntClass) getResource(owlClass).as(OntClass.class);
			subClses = getOWLClassSet(cls.listSubClasses(true));
		} catch (Exception e) {			
			throw(new ProtegeReasonerException(e.getMessage(), e));			
		}

		return subClses;	
	}

	@Override	
	public Collection<OWLClass> getIndividualDirectTypes(OWLIndividual owlIndividual)	throws ProtegeReasonerException {
		Set<OWLClass> types = new HashSet<OWLClass>();

		try {
			Individual ind = (Individual) getResource(owlIndividual).as(Individual.class);			
			types = getOWLClassSet(ind.listRDFTypes(true));
		} catch (Exception e) {			
			throw(new ProtegeReasonerException(e.getMessage(), e));			
		}

		return types;	
	}
	
	@Override	
	public Collection<OWLClass> getIndividualTypes(OWLIndividual owlIndividual)	throws ProtegeReasonerException {
		Set<OWLClass> types = new HashSet<OWLClass>();

		try {
			Individual ind = (Individual) getResource(owlIndividual).as(Individual.class);			
			types = getOWLClassSet(ind.listRDFTypes(false));
		} catch (Exception e) {			
			throw(new ProtegeReasonerException(e.getMessage(), e));			
		}

		return types;	
	}
	
	@Override
	public Collection<OWLProperty> getSubProperties(OWLProperty property) throws ProtegeReasonerException {
		Set<OWLProperty> props = new HashSet<OWLProperty>();

		try {
			OntProperty cls = (OntProperty) getResource(property).as(OntProperty.class);
			props = getOWLPropertiesSet(cls.listSubProperties(true));
		} catch (Exception e) {			
			throw(new ProtegeReasonerException(e.getMessage(), e));			
		}

		return props;
	}
	
	@Override
	public Collection<OWLProperty> getSuperProperties(OWLProperty property)	throws ProtegeReasonerException {
		Set<OWLProperty> props = new HashSet<OWLProperty>();

		try {
			OntProperty cls = (OntProperty) getResource(property).as(OntProperty.class);
			props = getOWLPropertiesSet(cls.listSuperProperties(true));
		} catch (Exception e) {			
			throw(new ProtegeReasonerException(e.getMessage(), e));			
		}

		return props;
	}
	
	
	@Override
	public Collection<OWLProperty> getDescendantProperties(OWLProperty property) throws ProtegeReasonerException {
		Set<OWLProperty> props = new HashSet<OWLProperty>();

		try {
			OntProperty cls = (OntProperty) getResource(property).as(OntProperty.class);
			props = getOWLPropertiesSet(cls.listSubProperties(false));
		} catch (Exception e) {			
			throw(new ProtegeReasonerException(e.getMessage(), e));			
		}

		return props;
	}
	
	@Override
	public Collection<OWLProperty> getAncestorProperties(OWLProperty property)	throws ProtegeReasonerException {
		Set<OWLProperty> props = new HashSet<OWLProperty>();

		try {
			OntProperty cls = (OntProperty) getResource(property).as(OntProperty.class);
			props = getOWLPropertiesSet(cls.listSuperProperties(false));
		} catch (Exception e) {			
			throw(new ProtegeReasonerException(e.getMessage(), e));			
		}

		return props;
	}
	
	

	@Override
	public boolean isSatisfiable(OWLClass owlClass)	throws ProtegeReasonerException {
		boolean isSatisfiable = false;

		try {
			OntClass cls = (OntClass) getResource(owlClass).as(OntClass.class);			
			OntClass owlNothing = ontModel.getOntClass(OWL.Nothing.getURI());			
			isSatisfiable = !cls.hasSuperClass(owlNothing);				
		} catch (Exception e) {
			throw(new ProtegeReasonerException(e.getMessage(), e));
		}

		return isSatisfiable;		
	}

	@Override
	public int getSubsumptionRelationship(OWLClass cls1, OWLClass cls2)	throws ProtegeReasonerException {	
		try {
			OntClass ontcls1 = (OntClass) getResource(cls1).as(OntClass.class);
			OntClass ontcls2 = (OntClass) getResource(cls2).as(OntClass.class);

			if (ontcls1.hasEquivalentClass(ontcls2)) {
				return ProtegeOWLReasoner.CLS1_EQUIVALENT_TO_CLS2;
			}

			if (ontcls1.hasSuperClass(ontcls2)) {
				return ProtegeOWLReasoner.CLS1_SUBSUMED_BY_CLS2;
			}

			if (ontcls1.hasSuperClass(ontcls2)) {
				return ProtegeOWLReasoner.CLS1_SUBSUMED_BY_CLS2;
			}

			return ProtegeOWLReasoner.NO_SUBSUMPTION_RELATIONSHIP;
		} catch (Exception e) {
			throw(new ProtegeReasonerException(e.getMessage(), e));
		}
	}

	@Override
	public boolean isSubsumedBy(OWLClass cls1, OWLClass cls2) throws ProtegeReasonerException {
		try {
			OntClass ontcls1 = (OntClass) getResource(cls1).as(OntClass.class);
			OntClass ontcls2 = (OntClass) getResource(cls2).as(OntClass.class);

			return (ontcls1.hasSuperClass(ontcls2));
		} catch (Exception e) {
			throw(new ProtegeReasonerException(e.getMessage(), e));
		}
	}


	@Override
	public Collection<OWLIndividual> getRelatedIndividuals(OWLIndividual subject,
			OWLObjectProperty objectProperty) throws ProtegeReasonerException {

		try {
			Individual ind = (Individual) getResource(subject).as(Individual.class);
			OntProperty prop = (OntProperty) getResource(objectProperty).as(OntProperty.class);

			return getOWLIndividualsSet(ind.listPropertyValues(prop));
		} catch (Exception e) {
			throw new ProtegeReasonerException(e.getMessage(), e);
		}		
	}

	@Override
	public Collection getRelatedValues(OWLIndividual subject, OWLDatatypeProperty datatypeProperty)
	throws ProtegeReasonerException {

		try {
			Individual ind = (Individual) getResource(subject).as(Individual.class);
			OntProperty prop = (OntProperty) getResource(datatypeProperty).as(OntProperty.class);

			return getPropertyValues(ind.listPropertyValues(prop));
		} catch (Exception e) {
			throw new ProtegeReasonerException(e.getMessage(), e);
		}
	}



	public boolean hasType(OWLIndividual owlIndividual, OWLClass owlClass) throws ProtegeReasonerException {
		boolean hasType = false;

		try {
			Individual ind = (Individual) getResource(owlIndividual).as(Individual.class);
			OntClass cls = (OntClass) getResource(owlClass).as(OntClass.class);
			hasType = ind.hasRDFType(cls);			
		} catch (Exception e) {
			throw new ProtegeReasonerException(e.getMessage(), e);
		}

		return hasType;
	}

	// ************************** MANAGEMENT *************************************


	public void dispose() {
		if (ontModel != null) {
			ontModel.close();
			ontModel = null;
		}
		super.dispose();
	}


	protected OntModel getOntModel() {
		if (ontModel != null) {
			return ontModel;
		}

		Collection<TripleStore> exportedTripleStores = getExportedTripleStores();

		long t0 = System.currentTimeMillis();

		//special handling for database case. Improve it later
		if (owlModel instanceof OWLDatabaseModel) {
			ontModel = createDBOntModel();
		} else {
			ontModel = Protege2Jena.createOntModel(owlModel, ontModelSpec, exportedTripleStores);
		}

		Log.getLogger().info("Created ont model in " + (System.currentTimeMillis() - t0) + " ms");

		return ontModel;		
	}


	protected OntModel createDBOntModel() {
		  JenaCreator creator = new JenaCreator(owlModel, false, null, null);
		  OntModel ontModel = creator.createOntModel(getDefaultOntModelSpec());
		  
		  return ontModel;
	}
	
	protected OntModelSpec getDefaultOntModelSpec() {
		OntDocumentManager docManager = new OntDocumentManager();
		docManager.setProcessImports(false);

		OntModelSpec ontModelSpec = new OntModelSpec(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		ontModelSpec.setDocumentManager(docManager);

		return ontModelSpec;
	}


	//exclude swrl triple stores
	protected Collection<TripleStore> getExportedTripleStores() {
		TripleStoreModel tsModel = owlModel.getTripleStoreModel();

		Collection<TripleStore> tsColl = new ArrayList<TripleStore>(tsModel.getTripleStores());

		removeTripleStore(tsColl, SWRLNames.SWRL_IMPORT);
		removeTripleStore(tsColl, SWRLNames.SWRLB_IMPORT);
		removeTripleStore(tsColl, SWRLNames.SWRL_ALT_IMPORT);
		removeTripleStore(tsColl, SWRLNames.SWRLB_ALT_IMPORT);

		return tsColl;
	}


	private void removeTripleStore(Collection<TripleStore> tsColl, String tsName) {
		TripleStoreModel tsModel = owlModel.getTripleStoreModel();
		TripleStore ts = tsModel.getTripleStore(tsName);

		if (ts == null) {
			return;
		}

		tsColl.remove(ts);
		Log.getLogger().info("Removed triplestore " + ts + " from exported triplestores");		
	}


	protected Resource getResource(RDFResource resource) {

		if (!resource.isAnonymous()) {
			String uri = owlModel.getURIForResourceName(resource.getName());
			return getOntModel().getResource(uri);
		} else {
			String anonId = AnonId.create(resource.getName()).toString();
			return getOntModel().getResource(anonId);
		}		
	}

	//TODO: check this
	protected Collection getPropertyValues(NodeIterator propertyValuesIt) {
		Collection values = new ArrayList();

		if (propertyValuesIt.hasNext()) {
			while (propertyValuesIt.hasNext()) {
				RDFNode next = propertyValuesIt.nextNode();
				if (next.isLiteral()) {
					Literal literal = (Literal) next;
					String datatypeUri = literal.getDatatypeURI();
					if (datatypeUri == null) {
						values.add(literal.getString());
					} else {
						RDFSDatatype datatype = owlModel.getRDFSDatatypeByURI(datatypeUri);

						if (datatype.equals(XSDDatatype.XSDstring) && literal.getLanguage().length() == 0) {
							values.add(literal.getString());
						} else {					
							RDFSLiteral lit = DefaultRDFSLiteral.create(owlModel, literal.getLexicalForm(), datatype);
							values.add(lit);
						}
					}
				}
			}
		}

		return values;
	}


	protected <T> Set<T> getRDFResourceSet(Iterator<?> i) {
		Set<T> owlEntities = new HashSet<T>();

		if(i.hasNext()) {
			while (i.hasNext()) {
				Object next = i.next();
				if (next instanceof Resource) {
					RDFResource owlEntity = getProtegeOWLEntity((Resource) next);

					if (owlEntity == null) {						
						Log.getLogger().warning("Can't find OWL resource: " + next);
					} else { // do a try catch?
						try {
							owlEntities.add((T)owlEntity);
						} catch (Exception e) {
							Log.getLogger().warning("Found resource with wrong type: " + owlEntity);
						}

					}
				} else {	        	
					Log.getLogger().warning("Found resource with wrong type: " + next);
				}
			}
		}       

		return owlEntities;
	}



	protected Set<OWLClass> getOWLClassSet(Iterator<?> i) {    	
		return getRDFResourceSet(i); 
	}


	protected Set<OWLIndividual> getOWLIndividualsSet(Iterator<?> i) {    	    	
		return getRDFResourceSet(i);    
	}


	protected Set<OWLProperty> getOWLPropertiesSet(Iterator<?> i) {    	    	
		return getRDFResourceSet(i);    
	}



	private RDFResource getProtegeOWLEntity(Resource resource) {

		if (resource.isAnon()) {
			String name = resource.getId().toString();
			return owlModel.getRDFResource(name);
		}
		return owlModel.getRDFResource(resource.getURI());
	}

	//to be removed
	public static void printIterator(Iterator i, String header) {
		System.out.println(header);
		for(int c = 0; c < header.length(); c++)
			System.out.print("=");
		System.out.println();

		if(i.hasNext()) {
			while (i.hasNext()) 
				System.out.println( i.next() );
		}       
		else
			System.out.println("<EMPTY>");

		System.out.println();        
	}

	public OntModel getJenaModel() throws ProtegeReasonerException {		
		return getOntModel();
	}

	public Reasoner getJenaReasoner() throws ProtegeReasonerException {
		return getOntModel().getSpecification().getReasoner();	
	}

	public static String getReasonerName() {
		return "Jena " + Jena.VERSION;
	}

}
