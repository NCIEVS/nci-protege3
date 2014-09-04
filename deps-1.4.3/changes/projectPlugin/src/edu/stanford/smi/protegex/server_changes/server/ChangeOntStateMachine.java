package edu.stanford.smi.protegex.server_changes.server;

import java.util.HashMap;
import java.util.Map;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.server.framestore.background.ServerCacheStateMachine;
import edu.stanford.smi.protege.server.framestore.background.ServerCachedState;

public class ChangeOntStateMachine implements ServerCacheStateMachine {
    public static final String[] slotNames = {"applyTo", "changes", "annotates", 
        "associatedAnnotations", "created", "modified",  "timestamp"};
    
    private KnowledgeBase changes_kb;
    private Map<Slot, String> traversalSlots;
    
    public ChangeOntStateMachine(KnowledgeBase changes_kb) {
        this.changes_kb = changes_kb;
        traversalSlots = new HashMap<Slot, String>();
        for (String  slotName : slotNames) {
            traversalSlots.put(changes_kb.getSlot(slotName), slotName);
        }
    }

    public ServerCachedState getInitialState() {
        return new ChangeOntState(traversalSlots);
    }

    public ServerCachedState nextState(ServerCachedState state, Frame before, Slot slot, Frame after) {
        if (!(state instanceof ChangeOntState)) {
            return null;
        }
        ChangeOntState costate = (ChangeOntState) state;
        if (!(costate.validTransition(slot, after))) {
            return null;
        }
        return costate.makeTransition(slot, after);
    }

}
