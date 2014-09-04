package edu.stanford.smi.protegex.owl.jena.creator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.ontology.OntClass;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protegex.owl.model.OWLAnonymousClass;
import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

/*
 * This class does not get copied to the trunk.
 */
public class BigJenaCreator extends JenaCreator {

	/*
	 * forReasoning = false inferred = true
	 */
	@SuppressWarnings("unchecked")
	public BigJenaCreator(OWLModel owlModel, Collection targetClses,
			ProgressDisplay progressDisplay) {
		super(owlModel, false, true, targetClses, progressDisplay);
	}

	@Override
	protected void addSuperclasses(RDFSNamedClass rdfsClass, OntClass ontClass) {
		if (rdfsClass instanceof OWLNamedClass) {
			OWLNamedClass namedCls = (OWLNamedClass) rdfsClass;
			addInferredInheritedAnonymousSuperclasses(namedCls);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void createAdditionalAnonymousSuperclassesOfIncludedClass(
			OWLNamedClass namedCls) {
		OntClass ontClass = getOntModel().getOntClass(namedCls.getURI());
		for (Iterator it = namedCls.getSuperclasses(true).iterator(); it
				.hasNext();) {
			Cls superCls = (Cls) it.next();
			if (superCls instanceof OWLAnonymousClass && !superCls.isIncluded()) {
				OntClass superClass = getOntClass((OWLAnonymousClass) superCls);
				if (superCls.hasDirectSuperclass(namedCls)) {
					ontClass.addEquivalentClass(superClass);
				} else {
					ontClass.addSuperClass(superClass);
					addInferredInheritedAnonymousSuperclasses(namedCls);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void addInferredInheritedAnonymousSuperclasses(
			OWLNamedClass namedCls) {

		Set<RDFSClass> inheritedInferredRestrictions = new HashSet<RDFSClass>();
		// add what is asserted at this class, but not the equivalent classes
		// NCI request
		inheritedInferredRestrictions.addAll(namedCls.getPureSuperclasses());

		ArrayList<RDFSClass> inferredSuperclasses = new ArrayList<RDFSClass>();
		// get the closure of inferred superclasses (recursive)
		// this will include also the inferred equivalent classes
		getInferredSuperclasses(namedCls, inferredSuperclasses);

		Collection<RDFSClass> allSuperclasses = new HashSet<RDFSClass>(namedCls
				.getSuperclasses(true));
		allSuperclasses.addAll(inferredSuperclasses);
		allSuperclasses.remove(namedCls);

		for (RDFSClass superClass : allSuperclasses) {
			if (!(superClass instanceof OWLNamedClass)) {
				continue;
			}
			
			Collection<RDFSClass> superclassesOfInfSuperclass = superClass
					.getSuperclasses(true);

			for (RDFSClass superOfInfSuperCls : superclassesOfInfSuperclass) {
				if (!(superOfInfSuperCls instanceof OWLAnonymousClass)) {
					continue;
				} else if (superOfInfSuperCls instanceof OWLIntersectionClass) {
					OWLIntersectionClass intClass = (OWLIntersectionClass) superOfInfSuperCls;
					Collection<RDFSClass> ops = intClass.getOperands();
					for (RDFSClass op : ops) {
						if (op instanceof OWLNamedClass) {
							continue;
						}
						addIfNotMember(inheritedInferredRestrictions, op);
						
						
					}

				} else {
					addIfNotMember(inheritedInferredRestrictions, superOfInfSuperCls);
				}
			}
		}

		OntClass ontClass = getOntModel().getOntClass(namedCls.getURI());

		for (RDFSClass superCls : inheritedInferredRestrictions) {
			OntClass ontSuperClass = getOntClass(superCls);
			ontClass.addSuperClass(ontSuperClass);
		}
	}
	
	private void addIfNotMember(Set<RDFSClass> inheritedInferredRestrictions, RDFSClass op) {
		Iterator<RDFSClass> it = inheritedInferredRestrictions.iterator();
		boolean found = false;
		while (it.hasNext()) {
			RDFSClass fc = it.next();
			if (fc.equalsStructurally(op)) {
				found = true;
				break;
			}
		}
		if (!found) {
			inheritedInferredRestrictions.add(op);
		}
		
	}

	@SuppressWarnings("unchecked")
	private void getInferredSuperclasses(OWLNamedClass aClass,
			List<RDFSClass> infSuperclasses) {
		Collection infDirectSuperclasses = aClass.getInferredSuperclasses();

		for (Iterator iterator = infDirectSuperclasses.iterator(); iterator
				.hasNext();) {
			RDFSClass infDirectSuperclass = (RDFSClass) iterator.next();

			if (!infSuperclasses.contains(infDirectSuperclass)
					&& infDirectSuperclass instanceof OWLNamedClass) {
				infSuperclasses.add(infDirectSuperclass);
				getInferredSuperclasses((OWLNamedClass) infDirectSuperclass,
						infSuperclasses);
			}
		}
	}

}
