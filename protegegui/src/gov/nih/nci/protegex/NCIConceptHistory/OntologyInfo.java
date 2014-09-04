package edu.stanford.smi.protegex.NCIConceptHistory;

import java.util.Collection;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.NamespaceUtil;
import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * This class is duplicated in the NCIEVS History which is unfortunate.
 * 
 * @author tredmond
 *
 */

public class OntologyInfo {
    public static final String SPLIT_FROM_SLOT_NAME = "Split_From";
    public static final String MERGE_INTO_SLOT_NAME = "Merge_Into";
    public static final String OLD_PARENT           = "OLD_PARENT";
    
    public static final String PRERETIRED_CONCEPTS_CLASS_NAME = "Preretired_Concepts";
    public static final String PRERETIRED_CLASSES_CLASS_NAME  = "Preretired_Classes";
    public static final String RETIRED_CONCEPTS_CLASS_NAME    = "Retired_Concepts";
    public static final String CODE_SLOT_NAME                 = "code";
    public static final String RDFS_SUBCLASS_SLOT             = "rdfs:subClassOf";

    
    private KnowledgeBase kb;
    
    public OntologyInfo(KnowledgeBase kb) {
        this.kb = kb;
    }
    
    public KnowledgeBase getKnowledgeBase() {
        return kb;
    }
    
    public String getFullName(String s) {
    	return NamespaceUtil.getFullName((OWLModel) kb, s);
    	
    }
    
    private Slot codeSlot;
    
   
    
    public Slot getCodeSlot() {
        if (codeSlot == null) {
            codeSlot = kb.getSlot(getFullName(CODE_SLOT_NAME));
        }
        return codeSlot;
    }
    
    private Slot oldParent;
    
    public Slot getOldParentSlot() {
        if (oldParent == null) {
            oldParent = kb.getSlot(getFullName(OLD_PARENT));
        }
        return oldParent;
    }
    
    private Slot mergeIntoSlot;
    
    public Slot getMergeIntoSlot() {
        if (mergeIntoSlot == null) {
            mergeIntoSlot = kb.getSlot(getFullName(MERGE_INTO_SLOT_NAME));
        }
        return mergeIntoSlot;
    }
    
    private Slot splitFromSlot;
    
    public Slot getSplitFromSlot() {
        if (splitFromSlot == null) {
            splitFromSlot = kb.getSlot(getFullName(SPLIT_FROM_SLOT_NAME));
        }
        return splitFromSlot;
    }
    
    
    
    
    private Cls retiredConcepts;
    
    public Cls getRetiredConcepts() {
        if (retiredConcepts == null) {
            retiredConcepts = kb.getCls(getFullName(RETIRED_CONCEPTS_CLASS_NAME));
        }
        return retiredConcepts;
    }
    
    
    public String getCode(Frame frame) {
        if (frame == null) {
            return null;
        }
        Object value = frame.getDirectOwnSlotValue(getCodeSlot());
        if (value == null) {
            return null;
        }
        else if (!(value instanceof String)) {
            Log.getLogger().warning("Ontology object " + frame.getBrowserText() + " has non-string code");
            return null;
        }
        return (String) value;
    }
    
    public Cls getClsFromCode(String code) {
        Collection frames = kb.getFramesWithValue(getCodeSlot(), null, false, code);
        if (frames == null) {
            return null;
        }
        else if (frames.size() > 1)  {
            Log.getLogger().warning("More than one object with the same code " + code);
        }
        for (Object o : frames) {
            if (o instanceof Cls) {
                return (Cls) o;
            }
        }
        return null;
    }

}
