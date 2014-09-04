package edu.stanford.smi.protegex.server_changes;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.bmir.protegex.chao.change.api.Class_Created;
import edu.stanford.bmir.protegex.chao.change.api.Class_Deleted;
import edu.stanford.bmir.protegex.chao.change.api.Composite_Change;
import edu.stanford.bmir.protegex.chao.change.api.Created_Change;
import edu.stanford.bmir.protegex.chao.change.api.Deleted_Change;
import edu.stanford.bmir.protegex.chao.change.api.Individual_Created;
import edu.stanford.bmir.protegex.chao.change.api.Individual_Deleted;
import edu.stanford.bmir.protegex.chao.change.api.Name_Changed;
import edu.stanford.bmir.protegex.chao.change.api.Property_Created;
import edu.stanford.bmir.protegex.chao.change.api.Property_Deleted;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.OntologyComponentFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Property;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.User;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.code.generator.wrapping.OntologyJavaMappingUtil;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.NamespaceUtil;
import edu.stanford.smi.protegex.owl.model.RDFProperty;

public class ServerChangesUtil {
    private static final Logger log = Log.getLogger(ServerChangesUtil.class);

    public static Change createChangeStd(PostProcessorManager changes_db,
                                         Change change,
                                         Frame applyTo,
                                         String context) {
        Ontology_Component frame = changes_db.getOntologyComponent(applyTo, true);
        if (change.getAction() == null) {
        	change.setAction(((AbstractWrappedInstance)change).getWrappedProtegeInstance().getDirectType().getName());
        }
        changes_db.finalizeChange(change, frame, context);
        return change;
    }

    public static Created_Change createCreatedChange(PostProcessorManager changes_db,
                                                     Created_Change change,
                                                     Frame applyTo,
                                                     String name) {
        String context = "";
        if (change instanceof Class_Created) {
        	context =  "Class";
        } else if (change instanceof Property_Created) {
        	context = "Property";
        } else if (change instanceof Individual_Created) {
        	context = "Individual";
        }

        context = context + " Create: " + NamespaceUtil.getLocalName(name);
        change.setAction(((AbstractWrappedInstance)change).getWrappedProtegeInstance().getDirectType().getName());

        Ontology_Component oc = changes_db.getOntologyComponent(applyTo, true);
        oc.setCurrentName(name);
        change.setCreationName(name);
        changes_db.finalizeChange(change, oc, context);
        return change;
    }

    public static Deleted_Change createDeletedChange(PostProcessorManager changes_db,
                                                     Deleted_Change change,
                                                     Frame frame,
                                                     String name) {
        String context = "";       
        if (change instanceof Class_Deleted) {
        	context =  "Class";
        } else if (change instanceof Property_Deleted) {
        	context = "Property";
        } else if (change instanceof Individual_Deleted) {
        	context = "Individual";
        }       
        
        context = context + " Delete: " + NamespaceUtil.getLocalName(name);

        change.setAction(((AbstractWrappedInstance)change).getWrappedProtegeInstance().getDirectType().getName());
        Ontology_Component applyTo = changes_db.getOntologyComponent(frame, true);
        applyTo.setCurrentName(null);

        change.setDeletionName(name);
        changes_db.finalizeChange(change, applyTo, context);
        return change;
    }

    public static Name_Changed createNameChange(PostProcessorManager changes_db,
                                                Frame applyTo,
                                                String oldName,
                                                String newName) {

        StringBuffer context = new StringBuffer();
        context.append("Name change from '");
        context.append(NamespaceUtil.getLocalName(oldName));
        context.append("' to '");
        context.append(NamespaceUtil.getLocalName(newName));
        context.append("'");

        Ontology_Component applyToOc = changes_db.getOntologyComponent(applyTo, true);
        applyToOc.setCurrentName(newName);

        Name_Changed change = new ChangeFactory(changes_db.getChangesKb()).createName_Changed(null);
        change.setAction(((AbstractWrappedInstance)change).getWrappedProtegeInstance().getDirectType().getName());
        change.setOldName(oldName);
        change.setNewName(newName);
        changes_db.finalizeChange(change, applyToOc, context.toString());
        return change;
    }

    public static Composite_Change createTransactionChange(PostProcessorManager changes_db,
                                                           Frame applyTo,
                                                           String context,
                                                           List<Change> changes) {
        Ontology_Component oc = changes_db.getOntologyComponent(applyTo, true);
        return createTransactionChange(changes_db, oc, context, changes);
    }

    public static Composite_Change createTransactionChange(PostProcessorManager changes_db,
                                                           Ontology_Component applyTo,
                                                           String context,
                                                           List<Change> changes) {
    	Composite_Change transaction = new ChangeFactory(changes_db.getChangesKb()).createComposite_Change(null);
        transaction.setSubChanges(changes);
        transaction.setAction(((AbstractWrappedInstance)transaction).getWrappedProtegeInstance().getDirectType().getName());
        changes_db.finalizeChange(transaction, applyTo, context);
        return transaction;
    }

    public static Change createChangeWithSlot(PostProcessorManager changes_db,
                                              Change change,
                                              Frame applyTo,
                                              String context,
                                              Slot slot) {
        Ontology_Component oc = changes_db.getOntologyComponent(applyTo, true);
        change.setAction(((AbstractWrappedInstance)change).getWrappedProtegeInstance().getDirectType().getName());
        Ontology_Component s =  changes_db.getOntologyComponent(slot, true);
        ((AbstractWrappedInstance)change).getWrappedProtegeInstance().setDirectOwnSlotValue
        	(new ChangeFactory(changes_db.getChangesKb()).getAssociatedPropertySlot(), ((AbstractWrappedInstance)s).getWrappedProtegeInstance());

        changes_db.finalizeChange(change, oc, context);
        return change;
    }

    public static Change createChangeWithProperty(PostProcessorManager changes_db,
                                                  Change change,
                                                  Frame applyTo,
                                                  String context,
                                                  RDFProperty property) {
        Ontology_Component oc = changes_db.getOntologyComponent(applyTo, true);
        change.setAction(((AbstractWrappedInstance)change).getWrappedProtegeInstance().getDirectType().getName());
        Ontology_Property s = (Ontology_Property) changes_db.getOntologyComponent(property, true);
        ((AbstractWrappedInstance)change).getWrappedProtegeInstance().setDirectOwnSlotValue
        	(new ChangeFactory(changes_db.getChangesKb()).getAssociatedPropertySlot(), s);

        changes_db.finalizeChange(change, oc, context);
        return change;
    }

    public static Ontology_Component getOntologyComponent(Frame frame) {
        return OntologyComponentCache.getOntologyComponent(frame);
    }

    public static Ontology_Component getOntologyComponent(Frame frame, boolean create) {
        return OntologyComponentCache.getOntologyComponent(frame, create);
    }
    
    
    public static Ontology_Component getOntologyComponentFromChao(KnowledgeBase changes_kb, Frame frame, boolean create) {
    	if (changes_kb == null) { return null; };
    	Ontology_Component oc = getOntologyComponentFromChao(changes_kb, frame.getName());
    	if (oc != null) {
    		return oc;
    	}

        if (create) {
        	OntologyComponentFactory factory = new OntologyComponentFactory(changes_kb);
        	if (frame instanceof Cls) {
				oc = factory.createOntology_Class(null);
			} else if (frame instanceof Slot) {
				oc = factory.createOntology_Property(null);
			} else {
				oc = factory.createOntology_Individual(null);
			}
            if (!frame.isDeleted()) {
                oc.setCurrentName(frame.getName());
            }
        }
        return oc;
    }

    public static Ontology_Component getOntologyComponentFromChao(KnowledgeBase changes_kb, String name) {
    	if (log.isLoggable(Level.FINE)) {
    		log.fine("Get Ontology Component from ChAO (expensive): " + name);
    	}
    	OntologyComponentFactory factory = new OntologyComponentFactory(changes_kb);
    	//could search also just for the prefixed name - if in compatibility mode..
    	Collection<Frame> ocFrames = changes_kb.getFramesWithValue(factory.getCurrentNameSlot(), null, false, name);
    	if (ocFrames.size() > 0) {
    		Frame ocFrame = CollectionUtilities.getFirstItem(ocFrames);
    		//get matching frames can be case insensitive, so make sure you got the right frame..
    		if (ocFrame.getOwnSlotValue(factory.getCurrentNameSlot()).equals(name)) {
    			return OntologyJavaMappingUtil.getSpecificObject(changes_kb, (Instance)ocFrame, Ontology_Component.class);
    		}
    	}
    	return null;
    }
    
    public static User getUser(KnowledgeBase changes_kb, String name) {
    	OntologyComponentFactory factory = new OntologyComponentFactory(changes_kb);
    	for (Frame frame : changes_kb.getMatchingFrames(factory.getNameSlot(), null, false, name, -1)) {
    		if (frame.getOwnSlotValue(factory.getNameSlot()).equals(name)) {
    			return OntologyJavaMappingUtil.getSpecificObject(changes_kb, (Instance) frame, User.class);
    		}
    	}
    	return null;
    }
    
    public static Collection<Ontology_Component> getAnnotatedOntologyComponents(Annotation annotation) {
    	return getAnnotatedOntologyComponents(annotation, new HashSet<AnnotatableThing>());
    }
    
    private static Collection<Ontology_Component> getAnnotatedOntologyComponents(Annotation annotation, Set<AnnotatableThing> visited) {
    	Collection<AnnotatableThing> annotedThings = annotation.getAnnotates();
        Set<Ontology_Component> annotatedOcs = new HashSet<Ontology_Component>();
        if (!visited.contains(annotation)) {
            visited.add(annotation);
            for (AnnotatableThing annotatableThing : annotedThings) {
                if (annotatableThing.canAs(Ontology_Component.class)) {
                    annotatedOcs.add(annotatableThing.as(Ontology_Component.class));
                } else if (annotatableThing.canAs(Annotation.class)) {
                    annotatedOcs.addAll(getAnnotatedOntologyComponents(annotatableThing.as(Annotation.class), visited));
                }
            }
        }
        return annotatedOcs;
    }
 }
