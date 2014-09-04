package edu.stanford.bmir.protegex.chao.util;

import java.util.Comparator;

import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Timestamp;

public class AnnotationCreationComparator implements Comparator<Annotation> {

    public int compare(Annotation arg0, Annotation arg1) {
        Annotation a0 = arg0;
        Annotation a1 = arg1;

        Timestamp timestamp0 = a0.getCreated();
        Timestamp timestamp1 = a1.getCreated();

        if (timestamp0 == null) {
        	return 1;
        }

        return timestamp0.compareTimestamp(timestamp1);
    }

}
