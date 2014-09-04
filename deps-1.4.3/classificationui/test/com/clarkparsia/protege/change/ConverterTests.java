package com.clarkparsia.protege.change;

import static com.clarkparsia.protege.change.UtilityObjectFactory.constant;
import static com.clarkparsia.protege.change.UtilityObjectFactory.hasValue;
import static com.clarkparsia.protege.change.UtilityObjectFactory.intersectionOf;
import static com.clarkparsia.protege.change.UtilityObjectFactory.list;
import static com.clarkparsia.protege.change.UtilityObjectFactory.not;
import static com.clarkparsia.protege.change.UtilityObjectFactory.set;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.clarkparsia.protege.exceptions.ConversionException;

import edu.stanford.smi.protegex.owl.model.OWLComplementClass;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLHasValue;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFList;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

/**
 * Title: ConverterTests<br>
 * Description: Test Protege to/from OWLAPI converter<br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * 
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class ConverterTests extends AbstractBaseProjectTester {

	private Converter mConverter;

	public ConverterTests(boolean useDatabase) {
		super("ConverterTests", useDatabase);
	}

	private org.semanticweb.owl.model.OWLClass cls(String s) {
		return UtilityObjectFactory.cls(baseURI + s);
	}

	@Test
	public void convertClassCollection() throws ConversionException {
		RDFSNamedClass aFirst = owlModel.createRDFSNamedClass("First");
		RDFSNamedClass aSecond = owlModel.createRDFSNamedClass("Second");
		RDFSNamedClass aThird = owlModel.createRDFSNamedClass("Third");

		assertEquals(set(cls("First"), cls("Second"), cls("Third")), mConverter
				.convertListOfClasses(list(aFirst, aSecond, aThird)));
	}

	@Test
	public void convertComplementedNamedClass() throws ConversionException {
		RDFSNamedClass aProtegeCls = owlModel.createRDFSNamedClass("TestClass");
		OWLComplementClass aComp = owlModel.createOWLComplementClass(aProtegeCls);

		assertEquals(not(cls("TestClass")), mConverter.convertComplement(aComp));
	}

	@Test
	public void convertDataHasValue() throws ConversionException {
		OWLDatatypeProperty aDataProp = owlModel.createOWLDatatypeProperty("testDp");
		OWLHasValue aHasValue = owlModel.createOWLHasValue(aDataProp, "testString");

		assertEquals(hasValue(dataProperty("testDp"), constant("testString")), mConverter
				.convertHasValueRestriction(aHasValue));
	}

	@Test
	public void convertDataPropertyGeneral() throws ConversionException {
		OWLDatatypeProperty aDataProp = owlModel.createOWLDatatypeProperty("testDp");

		assertEquals(dataProperty("testDp"), mConverter.convert(aDataProp));
	}

	@Test
	public void convertDataPropertySpecific() throws ConversionException {
		OWLDatatypeProperty aDataProp = owlModel.createOWLDatatypeProperty("testDp");

		assertEquals(dataProperty("testDp"), mConverter.convertDataProperty(aDataProp));
	}

	@Test
	public void convertIndividualCollection() throws ConversionException {
		OWLIndividual aOne = owlModel.getOWLThingClass().createOWLIndividual("one");
		OWLIndividual aTwo = owlModel.getOWLThingClass().createOWLIndividual("two");
		OWLIndividual aThree = owlModel.getOWLThingClass().createOWLIndividual("three");

		assertEquals(set(individual("one"), individual("two"), individual("three")), mConverter
				.convertListOfIndividuals(list(aOne, aTwo, aThree)));
	}

	@Test
	public void convertNamedClassGeneral() throws ConversionException {
		RDFSNamedClass aProtegeCls = owlModel.createRDFSNamedClass("TestClass");

		assertEquals(cls("TestClass"), mConverter.convertClass(aProtegeCls));
	}

	@Test
	public void convertNamedClassList() throws ConversionException {
		RDFSNamedClass aFirst = owlModel.createRDFSNamedClass("First");
		RDFSNamedClass aSecond = owlModel.createRDFSNamedClass("Second");
		RDFSNamedClass aThird = owlModel.createRDFSNamedClass("Third");
		RDFList aList = owlModel.createRDFList(list(aFirst, aSecond, aThird).iterator());

		assertEquals(list(cls("First"), cls("Second"), cls("Third")), mConverter
				.convertClassList(aList));
	}

	@Test
	public void convertNamedClassSpecific() throws ConversionException {
		RDFSNamedClass aProtegeCls = owlModel.createRDFSNamedClass("TestClass");

		assertEquals(cls("TestClass"), mConverter.convert(aProtegeCls));
	}

	@Test
	public void convertNamedIndividualGeneral() throws ConversionException {
		OWLIndividual aInd = owlModel.getOWLThingClass().createOWLIndividual("testInd");

		assertEquals(individual("testInd"), mConverter.convert(aInd));
	}

	@Test
	public void convertNamedIndividualSpecific() throws ConversionException {
		OWLIndividual aInd = owlModel.getOWLThingClass().createOWLIndividual("testInd");

		assertEquals(individual("testInd"), mConverter.convertIndividual(aInd));
	}

	@Test
	public void convertObjectHasValue() throws ConversionException {
		OWLObjectProperty aObjProp = owlModel.createOWLObjectProperty("testOp");
		OWLIndividual aInd = owlModel.getOWLThingClass().createOWLIndividual("testInd");
		OWLHasValue aHasValue = owlModel.createOWLHasValue(aObjProp, aInd);

		assertEquals(hasValue(objectProperty("testOp"), individual("testInd")), mConverter
				.convertHasValueRestriction(aHasValue));
	}

	@Test
	public void convertObjectIntersection() throws ConversionException {
		RDFSNamedClass aFirst = owlModel.createRDFSNamedClass("First");
		RDFSNamedClass aSecond = owlModel.createRDFSNamedClass("Second");
		RDFSNamedClass aThird = owlModel.createRDFSNamedClass("Third");
		OWLIntersectionClass aInt = owlModel.createOWLIntersectionClass(list(aFirst, aSecond,
				aThird));

		assertEquals(intersectionOf(cls("First"), cls("Second"), cls("Third")), mConverter
				.convertIntersection(aInt));
	}

	@Test
	public void convertSingletonObjectIntersection() throws ConversionException {
		RDFSNamedClass aFirst = owlModel.createRDFSNamedClass("First");
		OWLIntersectionClass aInt = owlModel.createOWLIntersectionClass(list(aFirst));

		assertEquals(cls("First"), mConverter.convertIntersection(aInt));
	}

	@Test
	public void convertObjectPropertyGeneral() throws ConversionException {
		OWLObjectProperty aObjProp = owlModel.createOWLObjectProperty("testOp");

		assertEquals(objectProperty("testOp"), mConverter.convert(aObjProp));
	}

	@Test
	public void convertObjectPropertySpecific() throws ConversionException {
		OWLObjectProperty aObjProp = owlModel.createOWLObjectProperty("testOp");

		assertEquals(objectProperty("testOp"), mConverter.convertObjectProperty(aObjProp));
	}

	private org.semanticweb.owl.model.OWLDataProperty dataProperty(String s) {
		return UtilityObjectFactory.dataProperty(baseURI + s);
	}

	private org.semanticweb.owl.model.OWLIndividual individual(String s) {
		return UtilityObjectFactory.individual(baseURI + s);
	}

	private org.semanticweb.owl.model.OWLObjectProperty objectProperty(String s) {
		return UtilityObjectFactory.objectProperty(baseURI + s);
	}

	@Before
	public void resetConverter() {
		mConverter = new Converter();
	}
}
