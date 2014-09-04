package com.clarkparsia.protege.change;

import static com.clarkparsia.protege.change.UtilityObjectFactory.collection;
import static com.clarkparsia.protege.change.UtilityObjectFactory.equivalent;
import static com.clarkparsia.protege.change.UtilityObjectFactory.intersectionOf;
import static com.clarkparsia.protege.change.UtilityObjectFactory.min;
import static com.clarkparsia.protege.change.UtilityObjectFactory.unionOf;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.semanticweb.owl.model.OWLDescription;

import com.clarkparsia.protege.reasoner.CustomReasonerProjectPlugin;

import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLNAryLogicalClass;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Evren Sirin
 */
public class TrackNaryCreationTests extends AbstractChangeTrackingTester {

	public TrackNaryCreationTests(boolean useDatabase) {
		super(useDatabase);
	}
	
	@Test
	public void createBinaryIntersection() {
		createNaryClass(2, true);
	}
	@Test
	public void createTernaryIntersection() {
		createNaryClass(3, true);
	}
	
	@Test
	public void createBinaryUnion() {
		createNaryClass(2, false);
	}
	@Test
	public void createTernaryUnion() {
		createNaryClass(3, false);
	}
	
	public void createNaryClass(int theListSize, boolean theUseIntersection) {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		
		OWLClass[] aClsList = new OWLClass[theListSize];
		for (int i = 0; i < theListSize; i++) {
			aClsList[i] = owlModel.createOWLMinCardinality(p, i);
		}

		aChangeLog.clear();
		
		OWLNAryLogicalClass aClass = theUseIntersection
			? owlModel.createOWLIntersectionClass(collection(aClsList))
			: owlModel.createOWLUnionClass(collection(aClsList));		
			
		A.addEquivalentClass(aClass);

		OWLDescription[] aList = new OWLDescription[theListSize]; 
		for (int i = 0; i < theListSize; i++) {
			aList[i] = min(objectProperty("p"), i);
		}
		
		if (theUseIntersection)
			assertEquals(changeList(add(equivalent(cls("A"),
					intersectionOf(aList)))), aChangeLog.getChanges());
		else
			assertEquals(changeList(add(equivalent(cls("A"), unionOf(aList)))),
					aChangeLog.getChanges());
	}
}