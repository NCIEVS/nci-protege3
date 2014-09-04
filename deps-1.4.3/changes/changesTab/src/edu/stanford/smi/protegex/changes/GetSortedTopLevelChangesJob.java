package edu.stanford.smi.protegex.changes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.bmir.protegex.chao.util.ChangeDateComparator;
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.ProtegeJob;

public class GetSortedTopLevelChangesJob extends ProtegeJob {
    private static final long serialVersionUID = -1146361145804033402L;

    public GetSortedTopLevelChangesJob(KnowledgeBase changes_kb) {
        super(changes_kb);
    }

    @Override
    public List<Change> run() throws ProtegeException {
        KnowledgeBase changes_kb = getKnowledgeBase();
        ChangeFactory factory = new ChangeFactory(changes_kb);
        List<Change> changes = (List<Change>) factory.getAllChangeObjects(true);
        List<Change> top_level_changes = new ArrayList<Change>();
        for (Object o : changes) {
            Change change = (Change) o;
            if (!ChangeProjectUtil.isRoot(change) && change.getPartOfCompositeChange() == null) {
                top_level_changes.add(change);
            }
        }
        Collections.sort(top_level_changes, new ChangeDateComparator(changes_kb));
        return top_level_changes;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<Change> execute() throws ProtegeException {
        return (List) super.execute();
    }
}
