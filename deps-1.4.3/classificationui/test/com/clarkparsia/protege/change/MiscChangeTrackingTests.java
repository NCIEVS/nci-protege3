package com.clarkparsia.protege.change;

import static com.clarkparsia.protege.change.UtilityObjectFactory.collection;
import static com.clarkparsia.protege.change.UtilityObjectFactory.declaration;
import static com.clarkparsia.protege.change.UtilityObjectFactory.disjoint;
import static com.clarkparsia.protege.change.UtilityObjectFactory.equivalent;
import static com.clarkparsia.protege.change.UtilityObjectFactory.functional;
import static com.clarkparsia.protege.change.UtilityObjectFactory.intersectionOf;
import static com.clarkparsia.protege.change.UtilityObjectFactory.list;
import static com.clarkparsia.protege.change.UtilityObjectFactory.min;
import static com.clarkparsia.protege.change.UtilityObjectFactory.range;
import static com.clarkparsia.protege.change.UtilityObjectFactory.sub;
import static com.clarkparsia.protege.change.UtilityObjectFactory.top;
import static com.clarkparsia.protege.change.UtilityObjectFactory.*;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.clarkparsia.protege.reasoner.CustomReasonerProjectPlugin;

import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protegex.owl.model.OWLAllValuesFrom;
import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
import edu.stanford.smi.protegex.owl.model.OWLMinCardinality;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.OWLSomeValuesFrom;
import edu.stanford.smi.protegex.owl.model.OWLUnionClass;
import edu.stanford.smi.protegex.owl.model.framestore.OWLFrameStore;

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
public class MiscChangeTrackingTests extends AbstractChangeTrackingTester {
	
	public MiscChangeTrackingTests(boolean useDatabase) {
		super(useDatabase);
	}

	@Before
	public void configureReasonerSync() {
        owlModel.getProject().setClientInformation(CustomReasonerProjectPlugin.KEY_TRACK_CHANGES, true);
        CustomReasonerProjectPlugin.trackChanges(owlModel.getProject(), true);

		owlModel.getProject().setClientInformation(CustomReasonerProjectPlugin.KEY_SYNCH,
												   CustomReasonerProjectPlugin.VALUE_QUERY);
	}

	/**
	 * Add a class as added when using the "New" button on the NCI Edit tab,
	 * edit subtab. Known to fail in r526. Updated for changes in edit-tab
	 * r1359, r1850
	 */
	@Test
	public void addNamedClassLikeNCIEditTab() {

		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass aSuper = owlModel.createOWLNamedClass("A");

		aChangeLog.clear();

		owlModel.beginTransaction("Create class");
		OWLNamedClass cls = owlModel.createOWLNamedClass("B");

		cls.addSuperclass(aSuper);
		if (cls.isSubclassOf(owlModel.getOWLThingClass()))
			cls.removeSuperclass(owlModel.getOWLThingClass());

		owlModel.commitTransaction();

		assertEqualChanges(changeList(add(declaration(cls("B"))), add(sub(cls("B"), cls("A")))), aChangeLog.getChanges());
	}
	
	/**
	 * Test addition of equivalent class, when outside a transaction (i.e., no
	 * additional cleanup is possible). Prior to r9172 of protege-owl svn, the
	 * changelist also included sub(A,B) sub(B,A)
	 */
	@Test
	public void addEquivalentNamedClassWithoutTransaction() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLNamedClass B = owlModel.createOWLNamedClass("B");

		aChangeLog.clear();

		A.addEquivalentClass(B);

		assertEquals(changeList(add(equivalent(cls("A"), cls("B")))), aChangeLog.getChanges());

	}

	/**
	 * Test addition of equivalent class, when wrapped in a transaction (i.e.,
	 * cleanup is possible)
	 */
	@Test
	public void addEquivalentNamedClassWithTransaction() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLNamedClass B = owlModel.createOWLNamedClass("B");

		aChangeLog.clear();

		owlModel.beginTransaction("Adding equivalent class");
		A.addEquivalentClass(B);
		owlModel.commitTransaction();

		assertEquals(changeList(add(equivalent(cls("A"), cls("B")))), aChangeLog.getChanges());
	}

	/**
	 * Test included to be sure adding and removing classes are symmetric (see
	 * {@link #removeNamedClass()}). Note includes declaration and A [=
	 * owl:Thing.
	 */
	@Test
	public void addNamedClass() {

		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		owlModel.createOWLNamedClass("A");

		assertEquals(changeList(add(declaration(cls("A"))), add(sub(cls("A"), top()))), aChangeLog
				.getChanges());
	}
	
	@Test
	public void addNamedSuperClass() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLNamedClass B = owlModel.createOWLNamedClass("B");

		aChangeLog.clear();
		
		B.addSuperclass(A);
		
		assertEquals(changeList(add(sub(cls("B"), cls("A")))), aChangeLog.getChanges());
	}

	/**
	 * Added to check that removal is symmetric with adding equivalent
	 * restriction (see {@link #addEquivalentRestriction()}
	 */
	@Test
	public void removeEquivalentRestriction() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		OWLMinCardinality card = owlModel.createOWLMinCardinality(p, 1);

		A.addEquivalentClass(card);

		aChangeLog.clear();

		A.removeEquivalentClass(card);

		assertEquals(changeList(remove(equivalent(cls("A"), min(objectProperty("p"), 1)))),
				aChangeLog.getChanges());

		aChangeLog.clear();
	}

	/**
	 * Test removal of equivalent class axiom where one description is an
	 * intersection. Fails in r506
	 */
	@Test
	public void removeEquivalentIntersectionWithTransaction() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		OWLObjectProperty q = owlModel.createOWLObjectProperty("q");


		OWLMinCardinality aPCard = owlModel.createOWLMinCardinality(p, 3);
		OWLMinCardinality aQCard = owlModel.createOWLMinCardinality(q, 5);
		OWLIntersectionClass anIntersection = owlModel.createOWLIntersectionClass(collection(aPCard, aQCard));

		A.addEquivalentClass(anIntersection);

		aChangeLog.clear();
		
		owlModel.beginTransaction("Remove an equivalent class");
		A.removeEquivalentClass(anIntersection);
		owlModel.commitTransaction();
		
		assertEquals(changeList(remove(equivalent(cls("A"), intersectionOf(min(
				objectProperty("p"), 3), min(objectProperty("q"), 5))))), aChangeLog.getChanges());
	}
	
	@Test
	public void removeDisjointNamedClass() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLNamedClass B = owlModel.createOWLNamedClass("B");
		A.addDisjointClass(B);

		aChangeLog.clear();

		A.removeDisjointClass(B);

		assertEquals(changeList(remove(disjoint(cls("A"), cls("B")))), aChangeLog.getChanges());
	}

	@Test
	public void removeDisjointIntersection() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		OWLObjectProperty q = owlModel.createOWLObjectProperty("q");
		OWLMinCardinality aPCard = owlModel.createOWLMinCardinality(p, 3);
		OWLMinCardinality aQCard = owlModel.createOWLMinCardinality(q, 5);
		OWLIntersectionClass anIntersection = owlModel.createOWLIntersectionClass(collection(aPCard, aQCard));

		A.addDisjointClass(anIntersection);
		
		aChangeLog.clear();

		A.removeDisjointClass(anIntersection);
		
		assertEquals(changeList(remove(disjoint(cls("A"), intersectionOf(min(
				objectProperty("p"), 3), min(objectProperty("q"), 5))))), aChangeLog
				.getChanges());
	}
	
	@Test
	public void removeDisjointRestriction() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		OWLMinCardinality aPCard = owlModel.createOWLMinCardinality(p, 3);

		A.addDisjointClass(aPCard);

		aChangeLog.clear();
		
		A.removeDisjointClass(aPCard);

		assertEquals(changeList(remove(disjoint(cls("A"), min(objectProperty("p"), 3)))), aChangeLog
				.getChanges());
	}
	
	/**
	 * Test removal of equivalent class axiom where one description is an
	 * intersection. Fails in r506, r530, prior to change in protege-owl r10269.
	 */
	@Test
	public void removeEquivalentIntersectionWithoutTransaction() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		OWLObjectProperty q = owlModel.createOWLObjectProperty("q");


		OWLMinCardinality aPCard = owlModel.createOWLMinCardinality(p, 3);
		OWLMinCardinality aQCard = owlModel.createOWLMinCardinality(q, 5);
		OWLIntersectionClass anIntersection = owlModel.createOWLIntersectionClass(collection(aPCard, aQCard));

		A.addEquivalentClass(anIntersection);

		aChangeLog.clear();
		
		A.removeEquivalentClass(anIntersection);
		
		assertEquals(changeList(remove(equivalent(cls("A"), intersectionOf(min(
				objectProperty("p"), 3), min(objectProperty("q"), 5))))), aChangeLog.getChanges());
	}
	
	/**
	 * Test removal of equivalent class, when outside a transaction (i.e., no
	 * cleanup is possible). Prior to protege-owl r10269, the expected results
	 * were different -- this test demonstrated the need for a transaction
	 * because otherwise we see the same edits in two directions.
	 */
	@Test
	public void removeEquivalentNamedClassWithoutTransaction() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLNamedClass B = owlModel.createOWLNamedClass("B");
		A.addEquivalentClass(B);

		aChangeLog.clear();

		A.removeEquivalentClass(B);

		assertEqualChanges(changeList(remove(equivalent(cls("A"), cls("B")))), aChangeLog
				.getChanges());
	}

	/**
	 * Test removal of equivalent class, when wrapped in a transaction (i.e.,
	 * cleanup is possible)
	 */
	@Test
	public void removeEquivalentNamedClassWithTransaction() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLNamedClass B = owlModel.createOWLNamedClass("B");
		A.addEquivalentClass(B);

		aChangeLog.clear();

		owlModel.beginTransaction("Adding equivalent class");
		A.removeEquivalentClass(B);
		owlModel.commitTransaction();

		assertEquals(changeList(remove(equivalent(cls("A"), cls("B")))), aChangeLog
				.getChanges());
	}
	
	@Test
	public void removeFunctionality() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		p.addProtegeType(owlModel.getOWLFunctionalPropertyClass());
		
		aChangeLog.clear();
		
		p.removeProtegeType(owlModel.getOWLFunctionalPropertyClass());
		
		assertEquals(changeList(remove(functional(objectProperty("p")))),
				aChangeLog.getChanges());
	}

	@Test
	public void removeFunctionalObjectProperty() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		p.addProtegeType(owlModel.getOWLFunctionalPropertyClass());

		aChangeLog.clear();

		p.delete();

		assertEqualChanges(changeList(remove(declaration(objectProperty("p"))),
				remove(functional(objectProperty("p")))), aChangeLog.getChanges());
	}

	/**
	 * Test included to be sure adding and removing classes are symmetric (see
	 * {@link #addNamedClass()}). Note includes declaration and A [=
	 * owl:Thing.
	 */
	@Test
	public void removeNamedClass() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");

		aChangeLog.clear();

		A.delete();

		assertEqualChanges(changeList(remove(declaration(cls("A"))), remove(sub(cls("A"), top()))),
				aChangeLog.getChanges());
	}

	@Test
	public void removeNamedSuperClass() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLNamedClass B = owlModel.createOWLNamedClass("B");
		B.addSuperclass(A);

		aChangeLog.clear();

		B.removeSuperclass(A);

		assertEquals(changeList(remove(sub(cls("B"), cls("A")))), aChangeLog.getChanges());
	}


	/**
	 * Test addition of a restriction equivalent to a named class. Tests cleanup
	 * of subs that are temporarily added, then removed. Fails in r467
	 */
	@Test
	public void addEquivalentRestriction() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		OWLMinCardinality card = owlModel.createOWLMinCardinality(p, 1);

		aChangeLog.clear();

		A.addEquivalentClass(card);

		assertEquals(changeList(add(equivalent(cls("A"), min(objectProperty("p"), 1)))), aChangeLog
				.getChanges());

		aChangeLog.clear();
	}
	
	@Test
	public void addFunctionality() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		
		aChangeLog.clear();
		
		p.addProtegeType(owlModel.getOWLFunctionalPropertyClass());
		
		assertEquals(changeList(add(functional(objectProperty("p")))),
				aChangeLog.getChanges());
	}
	
	@Test
	public void addDisjointNamedClass() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLNamedClass B = owlModel.createOWLNamedClass("B");

		aChangeLog.clear();

		A.addDisjointClass(B);

		assertEquals(changeList(add(disjoint(cls("A"), cls("B")))), aChangeLog.getChanges());
	}

	@Test
	public void addDisjointIntersection() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		OWLObjectProperty q = owlModel.createOWLObjectProperty("q");

		aChangeLog.clear();

		OWLMinCardinality aPCard = owlModel.createOWLMinCardinality(p, 3);
		OWLMinCardinality aQCard = owlModel.createOWLMinCardinality(q, 5);
		OWLIntersectionClass anIntersection = owlModel.createOWLIntersectionClass(collection(aPCard, aQCard));

		A.addDisjointClass(anIntersection);

		assertEquals(changeList(add(disjoint(cls("A"), intersectionOf(min(
				objectProperty("p"), 3), min(objectProperty("q"), 5))))), aChangeLog
				.getChanges());
	}
	
	@Test
	public void addDisjointRestriction() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");

		aChangeLog.clear();

		OWLMinCardinality aPCard = owlModel.createOWLMinCardinality(p, 3);

		A.addDisjointClass(aPCard);

		assertEquals(changeList(add(disjoint(cls("A"), min(objectProperty("p"), 3)))), aChangeLog
				.getChanges());
	}

	/**
	 * Test addition of equivalent class axiom where one description is an
	 * intersection
	 */
	@Test
	public void addEquivalentIntersection() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		OWLObjectProperty q = owlModel.createOWLObjectProperty("q");

		aChangeLog.clear();

		OWLMinCardinality aPCard = owlModel.createOWLMinCardinality(p, 3);
		OWLMinCardinality aQCard = owlModel.createOWLMinCardinality(q, 5);
		OWLIntersectionClass anIntersection = owlModel.createOWLIntersectionClass(collection(aPCard, aQCard));

		A.addEquivalentClass(anIntersection);
		assertEquals(changeList(add(equivalent(cls("A"), intersectionOf(min(
				objectProperty("p"), 3), min(objectProperty("q"), 5))))), aChangeLog
				.getChanges());

	}

	/**
	 * Test addition of an object property. Tests that trivial domain axiom is
	 * not added. Fails in r468.
	 */
	@Test
	public void addObjectProperty() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		owlModel.createOWLObjectProperty("p");

		assertEquals(changeList(add(declaration(objectProperty("p")))),
				aChangeLog.getChanges());
	}
	
	/**
	 * Test transition from a single class range to multiple descriptions in a
	 * union. Based on behavior of
	 * edu.stanford.smi.protegex.owl.ui.properties.range.UnionRangeClassesTableModel
	 */
	@Test
	public void addPropertyRangeUnion() {

		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLNamedClass B = owlModel.createOWLNamedClass("B");
		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		p.setUnionRangeClasses(list(A));
		
		aChangeLog.clear();
		
		owlModel.beginTransaction("Change range");
		p.setUnionRangeClasses(list(A, B));
		owlModel.commitTransaction();
		
		assertEqualChanges(changeList(remove(range(objectProperty("p"), cls("A"))), add(range(
				objectProperty("p"), unionOf(cls("A"), cls("B"))))), aChangeLog.getChanges());
	}
	
	/**
	 * Test addition of a restriction as a superclass of a named class.
	 */
	@Test
	public void addSuperClassRestriction() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		OWLMinCardinality card = owlModel.createOWLMinCardinality(p, 1);

		aChangeLog.clear();

		A.addSuperclass(card);

		assertEquals(changeList(add(sub(cls("A"), min(objectProperty("p"), 1)))), aChangeLog
				.getChanges());

		aChangeLog.clear();
	}

	@Test
	public void modifySuperClassRestriction() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLNamedClass B = owlModel.createOWLNamedClass("B");
		OWLNamedClass C = owlModel.createOWLNamedClass("C");
		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		OWLSomeValuesFrom some = owlModel.createOWLSomeValuesFrom(p, B);

		A.addSuperclass(some);

		aChangeLog.clear();
		
		some.setFiller(C);

		assertEquals(changeList(remove(sub(cls("A"), some(objectProperty("p"), cls("B")))), add(sub(cls("A"), some(
		                objectProperty("p"), cls("C"))))), aChangeLog.getChanges());

		aChangeLog.clear();
	}
	
	@Test
	public void modifySuperClassRestrictionInsideIntersection() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLNamedClass B = owlModel.createOWLNamedClass("B");
		OWLNamedClass C = owlModel.createOWLNamedClass("C");
		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		OWLSomeValuesFrom some = owlModel.createOWLSomeValuesFrom(p, B);
		OWLAllValuesFrom all = owlModel.createOWLAllValuesFrom(p, B);

		OWLIntersectionClass anIntersection = owlModel.createOWLIntersectionClass(collection(some, all));

		A.addEquivalentClass(anIntersection);

		aChangeLog.clear();
		
		all.setFiller(C);

		assertEquals(changeList(remove(equivalent(cls("A"), intersectionOf(some(objectProperty("p"), cls("B")), all(
		                objectProperty("p"), cls("B"))))), add(equivalent(cls("A"), intersectionOf(some(
		                objectProperty("p"), cls("B")), all(objectProperty("p"), cls("C")))))), aChangeLog.getChanges());

		aChangeLog.clear();
	}
	
	@Test
	public void modifySuperClassRestrictionWithIntersection() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLNamedClass B = owlModel.createOWLNamedClass("B");
		OWLNamedClass C = owlModel.createOWLNamedClass("C");
		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		OWLIntersectionClass anIntersection = owlModel.createOWLIntersectionClass(collection(B, C));
		OWLSomeValuesFrom some = owlModel.createOWLSomeValuesFrom(p, anIntersection);

		A.addEquivalentClass(some);

		aChangeLog.clear();
		
		OWLUnionClass aUnion = owlModel.createOWLUnionClass(collection(B, C));
		some.setFiller(aUnion);

		assertEquals(changeList(remove(equivalent(cls("A"), some(objectProperty("p"),
		                intersectionOf(cls("B"), cls("C"))))), add(equivalent(cls("A"), some(objectProperty("p"),
		                unionOf(cls("B"), cls("C")))))), aChangeLog.getChanges());

		aChangeLog.clear();
	}
	
	/**
	 * Test addition of object property sub/super relationship. Fails in r489
	 * due to argument ordering.
	 */
	@Test
	public void addSuperPropertyRelation() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		OWLObjectProperty q = owlModel.createOWLObjectProperty("q");

		aChangeLog.clear();

		q.addSuperproperty(p);

		assertEquals(changeList(add(sub(objectProperty("q"), objectProperty("p")))), aChangeLog.getChanges());		
	}

	@Test
	public void removeObjectProperty() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		
		aChangeLog.clear();
		
		p.delete();
		
		assertEquals(changeList(remove(declaration(objectProperty("p")))),
				aChangeLog.getChanges());
	}
	
	/**
	 * Test removal of the root class in a class hierarchy. Tests that sub
	 * classes not otherwise reachable and the path to them are deleted. Fails
	 * in r483.
	 */
	@Test
	public void removeParentClass() {

		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);
		
		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLNamedClass B = owlModel.createOWLNamedClass("B");
		OWLNamedClass C = owlModel.createOWLNamedClass("C");
		B.addSuperclass(A);
		B.removeSuperclass(owlModel.getOWLThingClass());
		C.addSuperclass(B);
		C.removeSuperclass(owlModel.getOWLThingClass());

		aChangeLog.clear();

		A.delete();

		assertEqualChanges(changeList(remove(declaration(cls("A"))), remove(sub(cls("A"), top())),
				remove(declaration(cls("B"))), remove(sub(cls("B"), cls("A"))),
				remove(declaration(cls("C"))), remove(sub(cls("C"), cls("B")))), aChangeLog
				.getChanges());

	}

	/**
	 * Test removal of the root class in a property hierarchy.  Fails in r486.
	 */
	@Test
	@Ignore("Known to fail r486")
	public void removeParentProperty() {

		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);
		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		OWLObjectProperty q = owlModel.createOWLObjectProperty("q");
		OWLObjectProperty r = owlModel.createOWLObjectProperty("r");
		
		r.addSuperproperty(q);
		q.addSuperproperty(p);

		aChangeLog.clear();

		p.delete();

		assertEqualChanges(changeList(remove(declaration(objectProperty("p"))),
				remove(declaration(objectProperty("q"))), remove(sub(objectProperty("q"), objectProperty("p"))),
				remove(declaration(objectProperty("r"))), remove(sub(objectProperty("r"), objectProperty("q")))),
				aChangeLog.getChanges());

	}
	
	/**
	 * Test transition from a range from multiple descriptions in a union to
	 * single class. See also {@link #addPropertyRangeUnion()} for symmetry.
	 * Fails in r546 in database projects due to empty union in removed axiom.
	 * See behavior in {@link OWLFrameStore#deleteRDFListsThatArePropertyValues}
	 * and {@link OWLFrameStore#deleteAnonymousTree}
	 */
	@Test
	@Ignore("Fails in r546: Empty union in removed range axiom")
	public void removePropertyRangeUnion() {

		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLNamedClass B = owlModel.createOWLNamedClass("B");
		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		p.setUnionRangeClasses(list(A, B));

		aChangeLog.clear();

		owlModel.beginTransaction("Change range");
		p.setUnionRangeClasses(list(A));
		owlModel.commitTransaction();

		assertEqualChanges(changeList(add(range(objectProperty("p"), cls("A"))), remove(range(
				objectProperty("p"), unionOf(cls("A"), cls("B"))))), aChangeLog.getChanges());
	}
	
	/**
	 * Test removal of object property sub/super relationship. Fails in r489 due
	 * to argument ordering.
	 */
	@Test
	public void removeSuperPropertyRelation() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		OWLObjectProperty q = owlModel.createOWLObjectProperty("q");

		q.addSuperproperty(p);

		aChangeLog.clear();

		q.removeSuperproperty(p);

		assertEquals(changeList(remove((sub(objectProperty("q"), objectProperty("p"))))),
				aChangeLog.getChanges());
	}
	
	/**
	 * Known to be problematic because the cached copy of the superclass is
	 * cleared prior being dereferenced.
	 */
	@Test
	@Ignore("Known to fail if outside transaction")
	public void removeUnionSuperClassWithoutTransaction() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		OWLObjectProperty q = owlModel.createOWLObjectProperty("q");

		OWLMinCardinality aPCard = owlModel.createOWLMinCardinality(p, 3);
		OWLMinCardinality aQCard = owlModel.createOWLMinCardinality(q, 5);
		OWLUnionClass aUnionCls = owlModel.createOWLUnionClass(collection(aPCard, aQCard));

		A.addSuperclass(aUnionCls);

		aChangeLog.clear();

		A.removeSuperclass(aUnionCls);

		assertEquals(changeList(remove(sub(cls("A"), unionOf(min(objectProperty("p"), 3), min(
				objectProperty("q"), 5))))), aChangeLog.getChanges());
	}
	
	@Test
	public void removeUnionSuperClassWithTransaction() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLNamedClass A = owlModel.createOWLNamedClass("A");
		OWLObjectProperty p = owlModel.createOWLObjectProperty("p");
		OWLObjectProperty q = owlModel.createOWLObjectProperty("q");

		OWLMinCardinality aPCard = owlModel.createOWLMinCardinality(p, 3);
		OWLMinCardinality aQCard = owlModel.createOWLMinCardinality(q, 5);
		OWLUnionClass aUnionCls = owlModel.createOWLUnionClass(collection(aPCard, aQCard));

		A.addSuperclass(aUnionCls);

		aChangeLog.clear();

		owlModel.beginTransaction("Remove union superclass");
		A.removeSuperclass(aUnionCls);
		owlModel.commitTransaction();

		assertEquals(changeList(remove(sub(cls("A"), unionOf(min(objectProperty("p"), 3), min(
				objectProperty("q"), 5))))), aChangeLog.getChanges());
	}
	
	/**
	 * Test rename of an object property. Failed in r544.
	 */
	@SuppressWarnings("deprecation")
	@Test
	@Ignore("Frame rename disallowed in Protege trunk")
	public void renameObjectProperty() {
		ChangeLog aChangeLog = CustomReasonerProjectPlugin.getChangeLog(ServerFrameStore.getCurrentSession(), owlModel);

		OWLObjectProperty p = owlModel.createOWLObjectProperty(null);
		p.setDirectOwnSlotValue(owlModel.getSystemFrames().getNameSlot(), "p");

		assertEquals(changeList(add(declaration(objectProperty("p")))),
				aChangeLog.getChanges());
	}
}
