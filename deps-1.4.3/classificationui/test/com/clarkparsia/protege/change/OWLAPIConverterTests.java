package com.clarkparsia.protege.change;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;
import org.semanticweb.owl.model.OWLLogicalAxiom;
import org.semanticweb.owl.model.OWLOntology;

import com.clarkparsia.owlapi.OWL;
import com.clarkparsia.protege.exceptions.ConversionException;

import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;

public class OWLAPIConverterTests extends AbstractBaseProjectTester {

	public OWLAPIConverterTests(boolean useDatabase) {
		super("OWLAPIConverterTests", useDatabase);
	}

	private void assertAxioms(OWLLogicalAxiom... expectedAxioms) {
		try {
			OWLAPIConverter converter = new OWLAPIConverter(owlModel, OWL.manager);
			Set<OWLLogicalAxiom> actualAxioms = converter.convert().getLogicalAxioms();

			for (OWLLogicalAxiom expectedAxiom : expectedAxioms) {
				actualAxioms.contains(expectedAxiom);
				assertTrue("Missing " + expectedAxiom, actualAxioms.remove(expectedAxiom));
			}
			assertTrue("Unexpected: " + actualAxioms.toString(), actualAxioms.isEmpty());
		}
		catch (ConversionException e) {
			throw new RuntimeException(e);
		}
		finally {
			for (OWLOntology ont : OWL.manager.getOntologies()) {
				OWL.manager.removeOntology(ont.getURI());
			}
		}
	}

	@Test
	public void convertSubClassOf() throws ConversionException {
		OWLNamedClass aFirst = owlModel.createOWLNamedClass("First");
		OWLNamedClass aSecond = owlModel.createOWLNamedClass("Second");

		aFirst.addSuperclass(aSecond);

		assertAxioms(OWL.subClassOf(OWL.Class(aFirst.getURI()), OWL.Thing), OWL.subClassOf(OWL.Class(aSecond.getURI()),
		                OWL.Thing), OWL.subClassOf(OWL.Class(aFirst.getURI()), OWL.Class(aSecond.getURI())));
	}

	@Test
	public void convertEquivalentClasses() throws ConversionException {
		OWLNamedClass aFirst = owlModel.createOWLNamedClass("First");
		OWLNamedClass aSecond = owlModel.createOWLNamedClass("Second");

		aFirst.addEquivalentClass(aSecond);

		assertAxioms(OWL.subClassOf(OWL.Class(aFirst.getURI()), OWL.Thing), OWL.subClassOf(OWL.Class(aSecond.getURI()),
		                OWL.Thing), OWL.equivalentClasses(OWL.Class(aFirst.getURI()), OWL.Class(aSecond.getURI())));
	}

	@Test
	public void convertDisjointClasses() throws ConversionException {
		OWLNamedClass aFirst = owlModel.createOWLNamedClass("First");
		OWLNamedClass aSecond = owlModel.createOWLNamedClass("Second");

		aFirst.addDisjointClass(aSecond);

		assertAxioms(OWL.subClassOf(OWL.Class(aFirst.getURI()), OWL.Thing), OWL.subClassOf(OWL.Class(aSecond.getURI()),
		                OWL.Thing), OWL.disjointClasses(OWL.Class(aFirst.getURI()), OWL.Class(aSecond.getURI())));
	}

	@Test
	public void convertSubObjectPropertyOf() throws ConversionException {
		OWLObjectProperty aFirst = owlModel.createOWLObjectProperty("First");
		OWLObjectProperty aSecond = owlModel.createOWLObjectProperty("Second");

		aFirst.addSuperproperty(aSecond);

		assertAxioms(OWL.subPropertyOf(OWL.ObjectProperty(aFirst.getURI()), OWL.ObjectProperty(aSecond.getURI())));
	}

	@Test
	public void convertSubDataPropertyOf() throws ConversionException {
		OWLDatatypeProperty aFirst = owlModel.createOWLDatatypeProperty("First");
		OWLDatatypeProperty aSecond = owlModel.createOWLDatatypeProperty("Second");

		aFirst.addSuperproperty(aSecond);

		assertAxioms(OWL.subPropertyOf(OWL.DataProperty(aFirst.getURI()), OWL.DataProperty(aSecond.getURI())));
	}

	@Test
	public void convertEquivalentObjectProperty() throws ConversionException {
		OWLObjectProperty aFirst = owlModel.createOWLObjectProperty("First");
		OWLObjectProperty aSecond = owlModel.createOWLObjectProperty("Second");

		aFirst.addEquivalentProperty(aSecond);

		assertAxioms(OWL.factory.getOWLEquivalentObjectPropertiesAxiom(OWL.ObjectProperty(aFirst.getURI()), OWL
		                .ObjectProperty(aSecond.getURI())));
	}

	@Test
	public void convertEquivalentDataProperty() throws ConversionException {
		OWLDatatypeProperty aFirst = owlModel.createOWLDatatypeProperty("First");
		OWLDatatypeProperty aSecond = owlModel.createOWLDatatypeProperty("Second");

		aFirst.addEquivalentProperty(aSecond);

		assertAxioms(OWL.factory.getOWLEquivalentDataPropertiesAxiom(OWL.DataProperty(aFirst.getURI()), OWL
		                .DataProperty(aSecond.getURI())));
	}
}
