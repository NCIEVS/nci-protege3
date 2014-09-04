package edu.stanford.smi.protegex.server_changes.postprocess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.change.api.Composite_Change;
import edu.stanford.bmir.protegex.chao.change.api.Created_Change;
import edu.stanford.bmir.protegex.chao.change.api.Name_Changed;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protegex.owl.model.NamespaceUtil;
import edu.stanford.smi.protegex.server_changes.PostProcessorManager;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;

public class JoinCreateAndNameChange implements PostProcessor {
    private PostProcessorManager postProcessorManager;
    /*
     * When a component has just been created this map determines the change that caused the change.
     * This is used in the slightly tricky code that creates a transaction around sequential create +
     * name change operations.
     */
    private Map<RemoteSession, Created_Change> lastCreateBySession = new HashMap<RemoteSession, Created_Change>();
    private Map<RemoteSession, Composite_Change> lastCompositeCreateBySession = new HashMap<RemoteSession, Composite_Change>();

    public void initialize(PostProcessorManager postProcessorManager) {
        this.postProcessorManager = postProcessorManager;
    }

    /*
     * This code listens for the following sequence of changes within a session:
     *    A Created Change
     *       ...
     *    Several changes and then
     *    
     *    A Composite Change whose first change is the Created Change above 
     *    and whose name is created and whose applyTo is correct.
     *    
     *    A NameChange
     *    
     *    If it finds these then it rolls up a new composite change which contains the composite change
     *    and the name change.
     */
    public void addChange(Change aChange) {
        RemoteSession session = postProcessorManager.getCurrentSession();
        
        Composite_Change lastCompositeCreate = lastCompositeCreateBySession.remove(session);
        
        if (aChange instanceof Created_Change
                && aChange.getApplyTo() != null
                && postProcessorManager.getTransactionState().inTransaction()) {
            lastCreateBySession.put(session, (Created_Change) aChange);
        }
        else if (aChange instanceof Composite_Change) {
            Created_Change lastCreatedChange = lastCreateBySession.remove(session);
            Composite_Change potentialCreateTransaction = (Composite_Change) aChange;
            Collection<Change> subChanges = potentialCreateTransaction.getSubChanges();

            if (lastCreatedChange != null &&
                    subChanges.size() != 0 &&
                    CollectionUtilities.getFirstItem(subChanges).equals(lastCreatedChange) &&
                    potentialCreateTransaction.getContext() != null &&
                    potentialCreateTransaction.getContext().startsWith("Create") &&
                    lastCreatedChange.getApplyTo() != null && 
                    lastCreatedChange.getApplyTo().equals(potentialCreateTransaction.getApplyTo())) {
                lastCompositeCreateBySession.put(session, potentialCreateTransaction);
            }
        }
        else if (aChange instanceof Name_Changed && 
                lastCompositeCreate != null &&
                lastCompositeCreate.getApplyTo().equals(aChange.getApplyTo())) {
            Name_Changed nameChange = (Name_Changed) aChange;
            String newName = nameChange.getNewName();
            Ontology_Component created = lastCompositeCreate.getApplyTo();
            List<Change> changes = new ArrayList<Change>();
            changes.add(lastCompositeCreate);
            changes.add(nameChange);
            StringBuffer sb = new StringBuffer("Create ");
            sb.append(created.getComponentType());
            sb.append(" ");
            sb.append(NamespaceUtil.getLocalName(newName));
            ServerChangesUtil.createTransactionChange(postProcessorManager,
                                                      created,
                                                      sb.toString(),
                                                      changes);
        }
    }
}
