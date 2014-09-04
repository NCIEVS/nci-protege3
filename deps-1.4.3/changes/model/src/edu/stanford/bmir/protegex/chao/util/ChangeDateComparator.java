package edu.stanford.bmir.protegex.chao.util;

import java.util.Comparator;

import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Timestamp;
import edu.stanford.smi.protege.model.KnowledgeBase;

public class ChangeDateComparator implements Comparator<Change> {

    public ChangeDateComparator(KnowledgeBase changes_kb) {
        ;
    }

    public int compare(Change o1, Change o2) {
    	if (o1 == null) {
    		return -1;
    	}

    	if (o2 == null) {
    		return 1;
    	}

        Change c1 = o1;
        Change c2 = o2;

        //fishy
        Timestamp t1 = c1.getTimestamp();
        Timestamp t2 = c2.getTimestamp();

        if (t1 == null) {
        	return -1;
        }

        return t1.compareTimestamp(t2);
    }
}