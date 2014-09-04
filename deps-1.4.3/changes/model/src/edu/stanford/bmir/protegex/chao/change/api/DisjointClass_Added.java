package edu.stanford.bmir.protegex.chao.change.api;

import java.util.Collection;

import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Timestamp;

/**
 * Generated by Protege (http://protege.stanford.edu).
 * Source Class: DisjointClass_Added
 *
 * @version generated on Mon Aug 18 21:13:43 GMT-08:00 2008
 */
public interface DisjointClass_Added extends Class_Change {

    // Slot action

    String getAction();

    boolean hasAction();

    void setAction(String newAction);


    // Slot applyTo

    Ontology_Component getApplyTo();

    boolean hasApplyTo();

    void setApplyTo(Ontology_Component newApplyTo);


    // Slot associatedAnnotations

    Collection<Annotation> getAssociatedAnnotations();

    boolean hasAssociatedAnnotations();

    void addAssociatedAnnotations(Annotation newAssociatedAnnotations);

    void removeAssociatedAnnotations(Annotation oldAssociatedAnnotations);

    void setAssociatedAnnotations(Collection<? extends Annotation> newAssociatedAnnotations);


    // Slot author

    String getAuthor();

    boolean hasAuthor();

    void setAuthor(String newAuthor);


    // Slot context

    String getContext();

    boolean hasContext();

    void setContext(String newContext);


    // Slot partOfCompositeChange

    Composite_Change getPartOfCompositeChange();

    boolean hasPartOfCompositeChange();

    void setPartOfCompositeChange(Composite_Change newPartOfCompositeChange);


    // Slot timestamp

    Timestamp getTimestamp();

    boolean hasTimestamp();

    void setTimestamp(Timestamp newTimestamp);

    void delete();
}
