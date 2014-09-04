package com.clarkparsia.protege.change;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Before;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLAxiomChange;

import com.clarkparsia.protege.reasoner.CustomReasonerProjectPlugin;

/**
 * <p>
 * Title: ChangeTrackingTests
 * </p>
 * <p>
 * Description: Unit tests for ontology change tracking infrastructure
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Mike Smith
 */
public abstract class AbstractChangeTrackingTester extends AbstractBaseProjectTester {

	public static ChangeList add(OWLAxiom theAxiom) {
		ChangeList aChangeList = new ChangeList();
		aChangeList.axiomAdded(theAxiom);
		return aChangeList;
	}
	
	/**
	 * A non-order dependent assertEquals
	 */
	public static void assertEqualChanges(ChangeList theExpected, ChangeList theActual) {
		List<OWLAxiomChange> theExpectedList = new ArrayList<OWLAxiomChange>(theExpected.getChanges());
		List<OWLAxiomChange> theActualList = new ArrayList<OWLAxiomChange>(theActual.getChanges());
		
		Collections.sort(theExpectedList, changeComparator);
		Collections.sort(theActualList, changeComparator);
		
		assertEquals(theExpectedList, theActualList);
	}

	private static class ChangeComparator implements Comparator<OWLAxiomChange> {
		public int compare(OWLAxiomChange o1, OWLAxiomChange o2) {
			boolean aFirstAdd = ChangeList.isAddition(o1);
			int aFirstHash = o1.getAxiom().hashCode();
			boolean aSecondAdd = ChangeList.isAddition(o2);
			int aSecondHash = o2.getAxiom().hashCode();

			if (aFirstAdd) {
				if (aSecondAdd) {
					if (aFirstHash == aSecondHash)
						return 0;
					else if (aFirstHash < aSecondHash)
						return -1;
					else return 1;
				}
				else {
					return -1;
				}
			}
			else {
				if (aSecondAdd) {
					return 1;
				}
				else {
					if (aFirstHash == aSecondHash)
						return 0;
					else if (aFirstHash < aSecondHash)
						return -1;
					else return 1;
				}
			}
		}
	}
	
	private static ChangeComparator changeComparator = new ChangeComparator();
	
	public static ChangeList remove(OWLAxiom theAxiom) {
		ChangeList aChangeList = new ChangeList();
		aChangeList.axiomRemoved(theAxiom);
		return aChangeList;
	}
	
	public static ChangeList changeList(ChangeList...theChanges) {
		ChangeList aChangeList = new ChangeList();
		for (ChangeList aList : theChanges) {
			aChangeList.add(aList);
		}
		return aChangeList;
	}
	
	public AbstractChangeTrackingTester(boolean useDatabase) {
		super("ChangeTrackingTests", useDatabase);
	}

	@Before
	public void configureReasonerSync() {
        owlModel.getProject().setClientInformation(CustomReasonerProjectPlugin.KEY_TRACK_CHANGES, true);
        CustomReasonerProjectPlugin.trackChanges(owlModel.getProject(), true);

		owlModel.getProject().setClientInformation(CustomReasonerProjectPlugin.KEY_SYNCH,
												   CustomReasonerProjectPlugin.VALUE_QUERY);
	}

	protected org.semanticweb.owl.model.OWLClass cls(String s) {
		return UtilityObjectFactory.cls(baseURI + s);
	}

	protected org.semanticweb.owl.model.OWLObjectProperty objectProperty(String p) {
		return UtilityObjectFactory.objectProperty(baseURI + p);
	}
}
