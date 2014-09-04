package edu.stanford.smi.protegex.changes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.bmir.protegex.chao.change.api.Deleted_Change;
import edu.stanford.bmir.protegex.chao.change.api.Name_Changed;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.OntologyComponentFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Timestamp;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.impl.DefaultTimestamp;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.code.generator.wrapping.OntologyJavaMappingUtil;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.ProtegeJob;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;

public class ChangeProjectUtil {

	/**
	 * Deletes all annotation instances from the Changes&Annotations kb
	 * @param changesKb - the Changes&Annotations kb
	 */
	public static void deleteAllAnnotations(KnowledgeBase changesKb) {
		AnnotationFactory factory = new AnnotationFactory(changesKb);
		Cls annotationCls = factory.getAnnotationClass();

		if (annotationCls == null) {
			return;
		}

		Collection<Annotation> annotations = factory.getAllAnnotationObjects(true);

		for (Annotation annotation : annotations) {
			Timestamp created = annotation.getCreated();
			Timestamp modified = annotation.getModified();

			((AbstractWrappedInstance)annotation).getWrappedProtegeInstance().delete();

			if (created != null) {
				((AbstractWrappedInstance)created).getWrappedProtegeInstance().delete();
			}

			if (modified != null) {
				((AbstractWrappedInstance)modified).getWrappedProtegeInstance().delete();
			}
		}
	}

	/**
	 * Deletes all change instances from the Changes&Annotations kb
	 * @param changesKb - the Changes&Annotations kb
	 */
	public static void deleteAllChanges(KnowledgeBase changesKb) {
		ChangeFactory factory = new ChangeFactory(changesKb);
		Cls changeCls = factory.getChangeClass();

		if (changeCls == null) {
			return;
		}

		Collection<Change> changes = factory.getAllChangeObjects(true);

		for (Change change : changes) {
			Timestamp timestamp = change.getTimestamp();

			((AbstractWrappedInstance)change).getWrappedProtegeInstance().delete();

			if (timestamp != null) {
				((AbstractWrappedInstance)timestamp).getWrappedProtegeInstance().delete();
			}
		}
	}

	public static String getActionDisplay(Change aInst) {
		String actionStr = aInst.getAction();
		//This should not be the case
		if (actionStr == null) {
			return "No Action";
		}
		return actionStr.replace('_', ' ');
	}

	/*
	 * Hopefully the definition of root will change or - better - go away.
	 * These two methods need to be synchronized with ChangesDb.createRootChange
	 * while we are figuring this out.
	 */

	public static boolean isRoot(Change change) {
		return change.getApplyTo() == null;
	}

	public static void logAnnotatableThing(String msg, Logger log, Level level, AnnotatableThing aInst) {
		if (!log.isLoggable(level)) {
			return;
		}
		ChangeProjectUtil.logAnnotatableThing(msg, log, level, aInst, ((AbstractWrappedInstance)aInst).getWrappedProtegeInstance().getDirectType());
	}

	public static void logAnnotatableThing(String msg, Logger log, Level level, AnnotatableThing aInst, Cls cls) {
		if (!log.isLoggable(level)) {
			return;
		}
		log.log(level, msg);
		if (aInst instanceof Change) {
			Change change = (Change) aInst;
			log.log(level, "\tAction = " + change.getAction());
			log.log(level, "\tApplyTo = " + change.getApplyTo());
			log.log(level, "\tAuthor = " + change.getAuthor());
			log.log(level, "\tContext = " + change.getContext());
			if (change.getTimestamp() != null) {
				log.log(level, "\tCreated = " + change.getTimestamp().getDate());
			}
			else {
				log.log(level, "\tNo timestamp");
			}
		}
		log.log(level, "\tDirect type = " + cls);
		log.log(level, "\tFrame = " + aInst);
	}


	@Deprecated
	public static Annotation createAnnotation(KnowledgeBase changesKb, String protegeClsName, Collection<? extends AnnotatableThing> annotatableThings) {
		Annotation annotation = OntologyJavaMappingUtil.createObject(changesKb, null, protegeClsName, Annotation.class);
		annotation.setAnnotates(annotatableThings);
		Timestamp now = DefaultTimestamp.getTimestamp(changesKb);
		annotation.setCreated(now);
		annotation.setModified(now);
		annotation.setAuthor(changesKb.getUserName());
		return annotation;
	}


	@SuppressWarnings("unchecked")
	public static List<Change> getSortedTopLevelChanges(KnowledgeBase changes_kb) {
		ProtegeJob job = new GetSortedTopLevelChangesJob(changes_kb);
		return (List<Change>) job.execute();
	}

    @SuppressWarnings("unchecked")
    public static List<Change> getSortedChanges(KnowledgeBase changes_kb) {
        ProtegeJob job = new GetSortedChangesJob(changes_kb);
        return (List<Change>) job.execute();
    }

	public static Collection<Change> removeRoots(Collection<Change> changes) {
		Collection<Change> roots = new ArrayList<Change>();
		for (Change change : changes) {
			if (isRoot(change)) {
				roots.add(change);
			}
		}
		changes.removeAll(roots);
		return changes;
	}

    public static Ontology_Component getOntologyComponentByInitialName(KnowledgeBase changes_kb, String name) {
        Ontology_Component firstTry = getOntologyComponentByFinalName(changes_kb, name);
        if (firstTry != null && firstTry.getCurrentName().equals(name)) {
            return firstTry;
        }
        Collection<Frame> matchingFrames;

        OntologyComponentFactory ocFactory = new OntologyComponentFactory(changes_kb);
        ChangeFactory cFactory = new ChangeFactory(changes_kb);

        matchingFrames = changes_kb.getMatchingFrames(ocFactory.getDeletionNameSlot(),
                                                      null, false, name, -1);
        if (matchingFrames != null) {
            for (Frame frame : matchingFrames) {
            	if (!(frame instanceof Instance)) {	continue; }
            	Deleted_Change deleted_Change = OntologyJavaMappingUtil.getSpecificObject(changes_kb,
            			(Instance)frame, Deleted_Change.class);
                if (deleted_Change != null) {
                    Ontology_Component secondTry = deleted_Change.getApplyTo();
                    if (secondTry.getInitialName().equals(name)) {
                        return secondTry;
                    }
                }
            }
        }
        matchingFrames = changes_kb.getMatchingFrames(ocFactory.getOldNameSlot(),
                                                      null, false, name, -1);
        if (matchingFrames != null) {
            for (Frame frame : matchingFrames) {
            	if (!(frame instanceof Instance)) {	continue; }
            	Name_Changed name_Change = OntologyJavaMappingUtil.getSpecificObject(changes_kb,
            			(Instance)frame, Name_Changed.class);
                if (name_Change != null) {
                	Ontology_Component thirdTry = name_Change.getApplyTo();
                    if (thirdTry.getInitialName().equals(name)) {
                        return thirdTry;
                    }
                }
            }
        }
        return null;
    }

    public static Ontology_Component getOntologyComponentByFinalName(KnowledgeBase changes_kb, String name) {
    	return ServerChangesUtil.getOntologyComponentFromChao(changes_kb, name);
    }



}
