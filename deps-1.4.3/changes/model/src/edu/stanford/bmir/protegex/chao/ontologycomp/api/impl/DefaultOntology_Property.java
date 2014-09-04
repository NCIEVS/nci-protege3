package edu.stanford.bmir.protegex.chao.ontologycomp.api.impl;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultAnnotation;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.change.api.impl.DefaultChange;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Property;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Slot;

/**
 * Generated by Protege (http://protege.stanford.edu).
 * Source Class: Ontology_Property
 *
 * @version generated on Mon Aug 18 21:08:59 GMT-08:00 2008
 */
public class DefaultOntology_Property extends DefaultOntology_Component
         implements Ontology_Property {

    public DefaultOntology_Property(Instance instance) {
        super(instance);
    }


    public DefaultOntology_Property() {
    }

    // Slot associatedAnnotations

    @Override
	public Collection<Annotation> getAssociatedAnnotations() {
        Collection protegeValues = getWrappedProtegeInstance().getOwnSlotValues(getAssociatedAnnotationsSlot());
        Collection<Annotation> values = new ArrayList<Annotation>();
        Cls cls = getKnowledgeBase().getCls("Annotation");
        for (Object object : protegeValues) {
            if (object instanceof Instance && ((Instance)object).hasType(cls)) {
                values.add(new DefaultAnnotation((Instance)object));
            }
        }
        return values;
    }


    @Override
	public Slot getAssociatedAnnotationsSlot() {
        final String name = "associatedAnnotations";
        return getKnowledgeBase().getSlot(name);
    }


    @Override
	public boolean hasAssociatedAnnotations() {
        return hasSlotValues(getAssociatedAnnotationsSlot());
    }


    @Override
	public void addAssociatedAnnotations(Annotation newAssociatedAnnotations) {
        addSlotValue(getAssociatedAnnotationsSlot(), newAssociatedAnnotations);
    }


    @Override
	public void removeAssociatedAnnotations(Annotation oldAssociatedAnnotations) {
        removeSlotValue(getAssociatedAnnotationsSlot(), oldAssociatedAnnotations);
    }


    @Override
	public void setAssociatedAnnotations(Collection<? extends Annotation> newAssociatedAnnotations) {
        setSlotValues(getAssociatedAnnotationsSlot(), newAssociatedAnnotations);
    }

    // Slot changes

    @Override
	public Collection<Change> getChanges() {
        Collection protegeValues = getWrappedProtegeInstance().getOwnSlotValues(getChangesSlot());
        Collection<Change> values = new ArrayList<Change>();
        Cls cls = getKnowledgeBase().getCls("Change");
        for (Object object : protegeValues) {
            if (object instanceof Instance && ((Instance)object).hasType(cls)) {
                values.add(new DefaultChange((Instance)object));
            }
        }
        return values;
    }


    @Override
	public Slot getChangesSlot() {
        final String name = "changes";
        return getKnowledgeBase().getSlot(name);
    }


    @Override
	public boolean hasChanges() {
        return hasSlotValues(getChangesSlot());
    }


    @Override
	public void addChanges(Change newChanges) {
        addSlotValue(getChangesSlot(), newChanges);
    }


    @Override
	public void removeChanges(Change oldChanges) {
        removeSlotValue(getChangesSlot(), oldChanges);
    }


    @Override
	public void setChanges(Collection<? extends Change> newChanges) {
        setSlotValues(getChangesSlot(), newChanges);
    }

    // Slot currentName

    @Override
	public String getCurrentName() {
        return (String) getWrappedProtegeInstance().getOwnSlotValue(getCurrentNameSlot());
    }


    @Override
	public Slot getCurrentNameSlot() {
        final String name = "currentName";
        return getKnowledgeBase().getSlot(name);
    }


    @Override
	public boolean hasCurrentName() {
        return hasSlotValues(getCurrentNameSlot());
    }


    @Override
	public void setCurrentName(String newCurrentName) {
        setSlotValue(getCurrentNameSlot(), newCurrentName);
    }
}
