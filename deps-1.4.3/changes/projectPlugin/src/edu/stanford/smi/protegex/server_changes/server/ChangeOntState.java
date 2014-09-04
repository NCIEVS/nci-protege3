package edu.stanford.smi.protegex.server_changes.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.server.framestore.background.ServerCachedState;

public class ChangeOntState implements ServerCachedState {
    private Map<Slot, String> traversalSlots;
    
    private Frame previous;
    private List<Slot> traversed = new ArrayList<Slot>();
    
    private ChangeOntState() {
        ;
    }
    
    public ChangeOntState(Map<Slot, String> traversalSlots) {
        this.traversalSlots = traversalSlots;
    }
    
    public boolean validTransition(Slot slot, Frame toFrame) {
        if (toFrame.equals(previous) || !traversalSlots.containsKey(slot)) {
            return false;
        }
        return !traversed.contains(slot);
    }
    
    public ChangeOntState makeTransition(Slot slot, Frame toFrame) {
        ChangeOntState newState = new ChangeOntState();
        newState.previous  = toFrame;
        newState.traversed = new ArrayList<Slot>(traversed);
        newState.traversed.add(slot);
        newState.traversalSlots = traversalSlots;
        return newState;
    }
    
    public String toString() {
        String traversedNames[] = new String[traversed.size()];
        int counter = 0;
        for (Slot slot : traversed) {
            traversedNames[counter++] = traversalSlots.get(slot);
        }
        return "COState[Previous = " + (previous == null ? null : previous.getFrameID()) 
                    + "traversed = " + traversedNames + "]";
    }
    
    

}
