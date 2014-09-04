package edu.stanford.bmir.protegex.chao.util.interval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.smi.protege.event.ProjectAdapter;
import edu.stanford.smi.protege.event.ProjectEvent;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protegex.changes.ChangeProjectUtil;
import edu.stanford.smi.protegex.server_changes.model.AbstractChangeListener;

public class TimeIntervalCalculator {
    private static Map<KnowledgeBase, TimeIntervalCalculator> instanceMap = new HashMap<KnowledgeBase, TimeIntervalCalculator>();

    private ProjectAdapter projectListener = new CleanupListener();
    private AbstractChangeListener changeListener;
    
    private KnowledgeBase changesKb;
    private TreeMap<SimpleTime, Change> sortedChangesMap = new TreeMap<SimpleTime, Change>();
    
    private TimeIntervalCalculator(KnowledgeBase changesKb) {
        this.changesKb = changesKb;
        
        sortedChangesMap = new GetTopLevelChangesTreeMapJob(changesKb).execute();

        changesKb.getProject().addProjectListener(projectListener); 

        changeListener = new UpdateChangesListener();
        changesKb.addFrameListener(changeListener);
        if (changesKb.getProject().isMultiUserServer()) {
            ServerFrameStore.requestEventDispatch(changesKb);
        }
    }
    
    public static TimeIntervalCalculator get(KnowledgeBase changesKb) {
        TimeIntervalCalculator t;
        synchronized (TimeIntervalCalculator.class) {
            t = instanceMap.get(changesKb);
        }
        if (t == null) {
            t = new TimeIntervalCalculator(changesKb);
            TimeIntervalCalculator existingCalculator;
            synchronized (TimeIntervalCalculator.class) {
                existingCalculator = instanceMap.get(changesKb);
                if (existingCalculator == null) {
                    instanceMap.put(changesKb, t);
                }
            }
            if (existingCalculator != null) {
                t.dispose();
                t = existingCalculator;
            }
        }
        return t;
    }
    
    public Collection<Change> getTopLevelChanges() {
        synchronized (changesKb) {
            return new ArrayList<Change>(sortedChangesMap.values());
        }
    }
   
    public Collection<Change> getTopLevelChangesBefore(Date d) {
        synchronized (changesKb) {
            return new ArrayList<Change>(sortedChangesMap.headMap(new SimpleTime(d)).values());
        }
    }
    
    public Collection<Change> getTopLevelChangesAfter(Date d) {
        synchronized (changesKb) {
            return new ArrayList<Change>(sortedChangesMap.tailMap(new SimpleTime(d)).values());
        }
    }

    public Collection<Change> getTopLevelChanges(Date start, Date end) {
        List<Change> changes = new ArrayList<Change>();
        for (Change change : getTopLevelChangesAfter(start)) {
            if (change.getTimestamp().getDateParsed().compareTo(end) >= 0) {
                break;
            }
            changes.add(change);
        }
        return changes;
    }
    
    public void dispose() {
        synchronized (TimeIntervalCalculator.class) {
            instanceMap.remove(changesKb);
        }
        changesKb.getProject().removeProjectListener(projectListener);
        changesKb.removeFrameListener(changeListener);
    }
    
    private static class CleanupListener extends ProjectAdapter {
        
        public void projectClosed(ProjectEvent event) {
            Project changesProject = (Project) event.getSource();
            KnowledgeBase changesKb = changesProject.getKnowledgeBase();
            TimeIntervalCalculator  t;
            synchronized (TimeIntervalCalculator.class) {
                t = instanceMap.get(changesKb);
            }
            if (t != null) {
                t.dispose();
            }
        }
    }
    
    private class UpdateChangesListener extends AbstractChangeListener {
        private Slot partOfCompositeChangeSlot;
        
        public UpdateChangesListener() {
            super(changesKb);
            partOfCompositeChangeSlot = new ChangeFactory(changesKb).getPartOfCompositeChangeSlot();
        }
        
        @Override
        public void addChange(Change change) {
            if (!ChangeProjectUtil.isRoot(change) && change.getPartOfCompositeChange() == null
                    && change.getTimestamp().hasDate()) {
                sortedChangesMap.put(new SimpleTime(change.getTimestamp()), change);
            }
        }

        @Override
        public void modifyChange(Change change, Slot slot, List oldValues) {
            if (slot.equals(partOfCompositeChangeSlot) &&
                    change.hasPartOfCompositeChange()) {
                sortedChangesMap.remove(new SimpleTime(change.getTimestamp()));
            }
            else if (slot.equals(partOfCompositeChangeSlot)) {
                sortedChangesMap.put(new SimpleTime(change.getTimestamp()), change);
            }
        }
        
        @Override
        public void addAnnotation(Annotation annotation) {

        }
    }
}
