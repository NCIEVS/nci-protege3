package edu.stanford.smi.protegex.server_changes.listeners.owl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import edu.stanford.bmir.protegex.chao.change.api.Annotation_Change;
import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.bmir.protegex.chao.change.api.Property_Value;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Property;
import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.framestore.LocalClassificationFrameStore;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.PostProcessorManager;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;

public class OwlChangesFrameListener extends FrameAdapter {
    private PostProcessorManager postProcessorManager;
    private ChangeFactory factory;

    public OwlChangesFrameListener(OWLModel owlModel) {
        postProcessorManager = ChangesProject.getPostProcessorManager(owlModel);
        factory = new ChangeFactory(postProcessorManager.getChangesKb());
    }

    @Override
    public void ownSlotValueChanged(final FrameEvent event) {
        final Frame f = event.getFrame();
        final Slot slot = event.getSlot();
        if (LocalClassificationFrameStore.isLocalClassificationProperty(slot)) {
        	return;
        }
        postProcessorManager.submitChangeListenerJob(new Runnable() {
            public void run() {
                OWLModel owlModel = (OWLModel) f.getKnowledgeBase();
                if (f instanceof Cls) {
                    Cls c = (Cls) f;
                    if (slot instanceof RDFProperty && ((RDFProperty) slot).isAnnotationProperty()) {
                        handleAnnotation(c, (RDFProperty) slot, event);
                    } else if (slot.equals(owlModel.getSystemFrames().getOwlDisjointWithProperty())) {
                        handleOwlDisjoint(c, slot, event);
                    } else if (!isFramesSystemSlot(slot) && !slot.equals(owlModel.getSystemFrames().getRdfTypeProperty())) {
                        handleInstanceSlotValueChange((Instance) f, slot, event);
                    }
                } else if (f instanceof Instance) {
                    if (!isFramesSystemSlot(slot) && !slot.equals(owlModel.getSystemFrames().getRdfTypeProperty())) {
                        handleInstanceSlotValueChange((Instance) f, slot, event);
                    }
                }
            }
         });
    }

    private boolean isFramesSystemSlot(Slot slot) {
        if (!slot.isSystem()) {
            return false;
        }
        return !slot.getName().startsWith("http://"); //not a very good test, but it should work    			
    }

    private void handleAnnotation(Cls c, RDFProperty prop, FrameEvent event) {
        String cText = c.getBrowserText();
        Slot s = event.getSlot();
        String sName = s.getBrowserText();
        ArrayList oldSlotValues = (ArrayList) event.getOldValues();
        Collection newSlotValues = c.getOwnSlotValues(prop);
        StringBuffer context = new StringBuffer();
        if (newSlotValues == null && oldSlotValues == null || newSlotValues.equals(oldSlotValues)) {
            return;
        }
        Ontology_Component applyTo = postProcessorManager.getOntologyComponent(c, true);
        Ontology_Property ontologyAnnotation = (Ontology_Property) postProcessorManager.getOntologyComponent(s, true);

        if (newSlotValues == null || newSlotValues.isEmpty()) {
            context.append("Annotation removed: ");
            context.append(sName);
            context.append(" from class: ");
            context.append(cText);
            context.append(" (old value:");
            context.append(CollectionUtilities.toString(oldSlotValues));
            context.append(")");
            Annotation_Change change = factory.createAnnotation_Removed(null);
            change.setAssociatedProperty(ontologyAnnotation);
            change.setAction("Annotation Removed");
            postProcessorManager.finalizeChange(change, applyTo, context.toString());
        }//Annotation deleted
        else if (oldSlotValues == null || oldSlotValues.isEmpty()) {
            context.append("Annotation added: ");
            context.append(sName);
            context.append(": ");
            context.append("'");
            context.append(CollectionUtilities.toString(newSlotValues));
            context.append("'");
            context.append(" to class: ");
            context.append(cText);
            Annotation_Change change = factory.createAnnotation_Added(null);
            change.setAssociatedProperty(ontologyAnnotation);
            change.setAction("Annotation Added");
            postProcessorManager.finalizeChange(change, applyTo, context.toString());
        } else {
            context.append("Annotation modified: ");
            context.append("annotation ");
            context.append(sName);
            context.append(" for class: ");
            context.append(cText);
            context.append(" set to: ");
            context.append(newSlotValues);
            Annotation_Change change = factory.createAnnotation_Modified(null);
            change.setAssociatedProperty(ontologyAnnotation);
            change.setAction("Annotation Modified");
            postProcessorManager.finalizeChange(change, applyTo, context.toString());
        }
    }

    private void handleOwlDisjoint(Cls c, Slot slot, FrameEvent event) {
        String cText = c.getBrowserText();
        ArrayList oldSlotValues = (ArrayList) event.getOldValues();
        StringBuffer context = new StringBuffer();
        Collection newSlotValues = c.getOwnSlotValues(slot);
        Collection deleted = new HashSet(oldSlotValues);
        deleted.removeAll(newSlotValues);
        Collection added = new HashSet(newSlotValues);
        added.removeAll(oldSlotValues);
        if (added.isEmpty() && deleted.isEmpty()) {
            return;
        }
        if (!added.isEmpty()) {
            context.append("Add disjoint class(es): ");
            context.append(CollectionUtilities.toString(added));
        }
        if (!deleted.isEmpty()) {
            context.append("Remove disjoint class(es): ");
            context.append(CollectionUtilities.toString(deleted));
        }
        context.append(" to: ");
        context.append(cText);

        ServerChangesUtil.createChangeStd(postProcessorManager, factory.createDisjointClass_Added(null), c, context
                .toString());
    }

    private void handleInstanceSlotValueChange(Instance i, Slot slot, FrameEvent event) {
        String iText = i.getBrowserText();
        String ownSName = slot.getBrowserText();
        String newSlotValue = CollectionUtilities.toString(i.getOwnSlotValues(event.getSlot()));
        ArrayList oldValues = (ArrayList) event.getArgument2();
        StringBuffer context = new StringBuffer();
        context.append("Property: ");
        context.append(ownSName);
        context.append(" for instance: ");
        context.append(iText);
        context.append(" set to: ");
        context.append(newSlotValue);
        context.append("(Old values:");
        context.append(CollectionUtilities.toString(oldValues));
        context.append(")");
        Property_Value change = factory.createProperty_Value(null);
        change.setAction("Property Value Changed");
        ServerChangesUtil.createChangeStd(postProcessorManager, change, i, context.toString());
    }

}
