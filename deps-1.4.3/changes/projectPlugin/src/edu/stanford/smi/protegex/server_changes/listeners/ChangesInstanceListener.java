package edu.stanford.smi.protegex.server_changes.listeners;

import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.smi.protege.event.InstanceEvent;
import edu.stanford.smi.protege.event.InstanceListener;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protegex.server_changes.PostProcessorManager;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;

public class ChangesInstanceListener implements InstanceListener{
    private PostProcessorManager changes_db;
    private ChangeFactory factory;

    public ChangesInstanceListener(KnowledgeBase kb) {
        changes_db = ChangesProject.getPostProcessorManager(kb);
        factory = new ChangeFactory(changes_db.getChangesKb());
    }
    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.event.InstanceListener#directTypeAdded(edu.stanford.smi.protege.event.InstanceEvent)
     */
    public void directTypeAdded(InstanceEvent event) {

        final String directType = event.getInstance().getDirectType().getBrowserText();

        final Instance inst = event.getInstance();
        final String instName = inst.getBrowserText();

        final StringBuffer context = new StringBuffer();
        context.append("Direct Type Added: ");
        context.append(directType);
        context.append(" (added to: ");
        context.append(instName);
        context.append(")");

        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createChangeStd(changes_db, factory.createDirectType_Added(null), inst, context.toString());
            }
         });
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.event.InstanceListener#directTypeRemoved(edu.stanford.smi.protege.event.InstanceEvent)
     */
    public void directTypeRemoved(final InstanceEvent event) {
        final String directTypeText = event.getInstance().getDirectType().getBrowserText();
        final String instText = event.getInstance().getBrowserText();

        final StringBuffer context = new StringBuffer();
        context.append("Direct Type Removed: ");
        context.append(directTypeText);
        context.append(" (removed from: ");
        context.append(instText);
        context.append(")");

        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createChangeStd(changes_db, factory.createDirectType_Removed(null), event.getInstance(), context.toString());
            }
         });
    }
}
