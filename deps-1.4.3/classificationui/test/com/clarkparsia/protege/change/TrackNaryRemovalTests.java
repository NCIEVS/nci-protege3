package com.clarkparsia.protege.change;

import static com.clarkparsia.protege.change.UtilityObjectFactory.collection;
import static com.clarkparsia.protege.change.UtilityObjectFactory.equivalent;
import static com.clarkparsia.protege.change.UtilityObjectFactory.intersectionOf;
import static com.clarkparsia.protege.change.UtilityObjectFactory.some;
import static com.clarkparsia.protege.change.UtilityObjectFactory.sub;
import static com.clarkparsia.protege.change.UtilityObjectFactory.unionOf;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDescription;

import com.clarkparsia.protege.reasoner.CustomReasonerProjectPlugin;

import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLNAryLogicalClass;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;

/**
 * Tests for removing an operand from an nary class (intersection or union). Various different
 * scenarios are tested: removing from the beginning middle or end of the list, n-ary class
 * nested inside another expression, expression an equiavlent class or subclass axiom.
 * 
 * There is a single test function that does all the work based on test parameters, The class
 * is using a mixture of parametrized runs with specific functions that hard code some parameters.
 * Using only parametrized runs makes it hard to see which case failed especially when the number
 * of parameters are high.  
 * 
 * @author Evren Sirin
 */
@RunWith(Parameterized.class)
public class TrackNaryRemovalTests extends AbstractChangeTrackingTester {
	private boolean useNesting;
	private boolean useSubClassAxiom;
	
	@Parameters
	public static Collection<Object[]> getCases() {
		Collection<Object[]> cases = new ArrayList<Object[]>();
				
		int numberOfFlags = 3;
		// there are two possible values for each flag 
		int numberOfCases = (int) Math.pow(2, numberOfFlags);
		
		// fancy way to get all combination of boolean values, easier than
		// copy/paste if the umber of flags in the constructor changes later
		for (int i = 0; i < numberOfCases; i++) {
			Boolean[] currCase = new Boolean[numberOfFlags];
			for (int flag = 0; flag < numberOfFlags; flag++) {
				currCase[flag] = (i & (1 << flag)) >> flag == 1;
			}
			cases.add(currCase);
		}
		return cases;
	}
	
	public TrackNaryRemovalTests(boolean useNesting, boolean useSubClassAxiom, boolean useDatabase) {
		super(useDatabase);
		
		this.useNesting = useNesting;
		this.useSubClassAxiom = useSubClassAxiom;
	}
	
	@Test
	public void removeFirstOperandFromBinaryIntersection() {
		removeOperandFromNaryClass(0, 2, true);
	}
	
	@Test
	public void removeLastOperandFromBinaryIntersection() {
		removeOperandFromNaryClass(1, 2, true);
	}
	
	@Test
	public void removeFirstOperandFromBinaryUnion() {
		removeOperandFromNaryClass(0, 2, false);
	}
	
	@Test
	public void removeLastOperandFromBinaryUnion() {
		removeOperandFromNaryClass(1, 2, false);
	}
	
	@Test
	public void removeFirstOperandFromIntersection() {
		removeOperandFromNaryClass(0, 3, true);
	}
	
	@Test
	public void removeMiddleOperandFromIntersection() {
		removeOperandFromNaryClass(1, 3, true);
	}
	
	@Test
	public void removeLastOperandFromIntersection() {
		removeOperandFromNaryClass(2, 3, true);
	}	
	
	@Test
	public void removeFirstOperandFromUnion() {
		removeOperandFromNaryClass(0, 3, false);
	}
	@Test
	public void removeMiddleOperandFromUnion() {
		removeOperandFromNaryClass(1, 3, false);
	}
	
	@Test
	public void removeLastOperandFromUnion() {
		removeOperandFromNaryClass(2, 3, false);
	}
	
	public void removeOperandFromNaryClass(int indexToRemove, int listSize, boolean useIntersection) {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLNamedClass[] B = new OWLNamedClass[listSize];
		for (int i = 0; i < listSize; i++) {
			B[i] = owlModel.createOWLNamedClass("B" + i);
		}
			
		OWLNAryLogicalClass aClass = useIntersection
			? owlModel.createOWLIntersectionClass(collection(B))
			: owlModel.createOWLUnionClass(collection(B));		
			
		OWLClass aRootClass = aClass;	
			
		if (useNesting) {
			OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
			aRootClass = owlModel.createOWLSomeValuesFrom(p, aClass);
		}

		if (useSubClassAxiom)
			A.addSuperclass(aRootClass);
		else
			A.addEquivalentClass(aRootClass);

		aChangeLog.clear();

		aClass.removeOperand(B[indexToRemove]);
		
		OWLDescription[] aOldList = new OWLDescription[listSize]; 
		for (int i = 0; i < listSize; i++)
			aOldList[i] = cls("B" + i);		
		
		OWLDescription[] aNewList = remove(aOldList, indexToRemove);
		
		OWLDescription aOldDescription = createNaryClass(aOldList, useIntersection);
		OWLDescription aNewDescription = createNaryClass(aNewList, useIntersection);
		
		if (useNesting) {
			aOldDescription = some(objectProperty("p"), aOldDescription);
			aNewDescription = some(objectProperty("p"), aNewDescription);
		}
		
		OWLAxiom aOldAxiom = useSubClassAxiom ? sub(cls("A"), aOldDescription) : equivalent(cls("A"), aOldDescription);
		OWLAxiom aNewAxiom = useSubClassAxiom ? sub(cls("A"), aNewDescription) : equivalent(cls("A"), aNewDescription);
		
		assertEquals(changeList(remove(aOldAxiom), add(aNewAxiom)), aChangeLog.getChanges());
	}
	
	private OWLDescription[] remove(OWLDescription[] aOldList, int indexToRemove) {
		int listSize = aOldList.length;
		OWLDescription[] aNewList = new OWLDescription[listSize-1];
		if (indexToRemove > 0)
			System.arraycopy(aOldList, 0, aNewList, 0, indexToRemove);
		if (indexToRemove != listSize - 1)
			System.arraycopy(aOldList, indexToRemove + 1, aNewList, indexToRemove, listSize - indexToRemove - 1);
		return aNewList;
	}
	
	private OWLDescription createNaryClass(OWLDescription[] list, boolean createIntersection) {
		if (list.length == 1)
			return list[0];
		return createIntersection ? intersectionOf(list) : unionOf(list);
	}
}