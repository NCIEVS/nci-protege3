package edu.stanford.smi.protegex.server_changes.postprocess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.bmir.protegex.chao.change.api.Annotation_Change;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.change.api.Composite_Change;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Timestamp;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.impl.DefaultTimestamp;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protegex.owl.model.NamespaceUtil;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.server_changes.PostProcessorManager;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;

public class AnnotationCombiner implements PostProcessor {
    public static String COMPOSITE_ANNOTATION_CHANGE = "Annotation change";
    
    private PostProcessorManager postProcessorManager;
    private Map<RemoteSession, Annotation_Change> lastAnnotationBySession
                                    = new HashMap<RemoteSession, Annotation_Change>();
    private Map<RemoteSession, Composite_Change> lastAnnotationTransactionBySession
                                    = new HashMap<RemoteSession, Composite_Change>();

    public void initialize(PostProcessorManager postProcessorManager) {
        this.postProcessorManager = postProcessorManager;
    }

    /*
     * Combine Annotations.
     */
    public void addChange(Change aChange) {
        RemoteSession session = postProcessorManager.getCurrentSession();
        
        if (aChange instanceof Composite_Change && 
                aChange.getContext() != null && 
                aChange.getContext().startsWith(COMPOSITE_ANNOTATION_CHANGE)) {
            return;
        }
        
        Composite_Change transaction = lastAnnotationTransactionBySession.remove(session);
        Annotation_Change previous_annotation = lastAnnotationBySession.remove(session);

        if (aChange instanceof Annotation_Change
                && !postProcessorManager.getTransactionState().inTransaction()) {
            Annotation_Change annotation = (Annotation_Change) aChange;

            lastAnnotationBySession.put(session, annotation);
            if (previous_annotation == null) {
                return;
            }

            Ontology_Component applyTo = annotation.getApplyTo();
            Ontology_Component property = annotation.getAssociatedProperty();

            if (applyTo.equals(previous_annotation.getApplyTo()) &&
                    property.equals(previous_annotation.getAssociatedProperty())) {

                if (transaction == null) {
                    List<Change> annotations = new ArrayList<Change>();
                    annotations.add(previous_annotation);
                    annotations.add(annotation);
                    String applyToName = NamespaceUtil.getPrefixedName((OWLModel)postProcessorManager.getKb(), applyTo.getCurrentName());
                    String propertyName = NamespaceUtil.getPrefixedName((OWLModel)postProcessorManager.getKb(), property.getCurrentName());
                    transaction = ServerChangesUtil.createTransactionChange(postProcessorManager, 
                                                                            applyTo, 
                                                                            COMPOSITE_ANNOTATION_CHANGE +
                                                                                " for " + applyToName + ", property: " + propertyName, 
                                                                            annotations);
                }
                else {
                    KnowledgeBase changesKb = postProcessorManager.getChangesKb();
                    Collection<Change> annotations = new ArrayList<Change>(transaction.getSubChanges());
                    annotations.add(annotation);
                    transaction.setSubChanges(annotations);
                    Timestamp timestamp = DefaultTimestamp.getTimestamp(changesKb);
                    transaction.setTimestamp(timestamp);
                }
                lastAnnotationTransactionBySession.put(session, transaction);
            }
        }

    }
}
