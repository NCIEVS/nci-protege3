package edu.stanford.smi.protegex.owlx.examples.javaDemo.model.impl;

import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFIndividual;
import edu.stanford.smi.protegex.owlx.examples.javaDemo.model.Product;

/**
 * Generated by Protege-OWL  (http://protege.stanford.edu/plugins/owl).
 * Source OWL Class: http://www.owl-ontologies.com/javaDemo.owl#Product
 *
 * @version generated on Mon Feb 21 10:30:53 EST 2005
 */
public class DefaultProduct extends DefaultRDFIndividual
        implements Product {

    public DefaultProduct(OWLModel owlModel, FrameID id) {
        super(owlModel, id);
    }


    public DefaultProduct() {
    }

    // Property http://www.owl-ontologies.com/javaDemo.owl#price


    public float getPrice() {
        return getPropertyValueLiteral(getPriceProperty()).getFloat();
    }


    public RDFProperty getPriceProperty() {
        final String uri = "http://www.owl-ontologies.com/javaDemo.owl#price";
        final String name = getOWLModel().getResourceNameForURI(uri);
        return getOWLModel().getRDFProperty(name);
    }


    public boolean hasPrice() {
        return getPropertyValueCount(getPriceProperty()) > 0;
    }


    public void setPrice(float newPrice) {
        setPropertyValue(getPriceProperty(), new Float(newPrice));
    }
}
