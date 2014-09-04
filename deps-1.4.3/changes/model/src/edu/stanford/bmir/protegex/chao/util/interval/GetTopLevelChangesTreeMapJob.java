package edu.stanford.bmir.protegex.chao.util.interval;

import java.util.List;
import java.util.TreeMap;

import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.ProtegeJob;
import edu.stanford.smi.protegex.changes.ChangeProjectUtil;

public class GetTopLevelChangesTreeMapJob extends ProtegeJob {
    private static final long serialVersionUID = -8678035299685334982L;

    public GetTopLevelChangesTreeMapJob(KnowledgeBase changesKb) {
        super(changesKb);
    }

    @Override
    public TreeMap<SimpleTime, Change> run() throws ProtegeException {
        KnowledgeBase changes_kb = getKnowledgeBase();
        ChangeFactory factory = new ChangeFactory(changes_kb);
        List<Change> changes = (List<Change>) factory.getAllChangeObjects(true);
        TreeMap<SimpleTime, Change> changeMap = new TreeMap<SimpleTime, Change>();
        for (Object o : changes) {
            Change change = (Change) o;
            if (!ChangeProjectUtil.isRoot(change) && change.getPartOfCompositeChange() == null
                    && change.getTimestamp().hasDate()) {
                changeMap.put(new SimpleTime(change.getTimestamp()), change);
            }
        }
        return changeMap;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public TreeMap<SimpleTime, Change> execute() throws ProtegeException {
        return (TreeMap) super.execute();
    }
    
}
