package edu.stanford.smi.protegex.server_changes.listeners;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.smi.protege.event.KnowledgeBaseAdapter;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.server_changes.PostProcessorManager;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;

public class ChangesKBListener extends KnowledgeBaseAdapter {
    private final static Logger log = Log.getLogger(ChangesKBListener.class);

    private PostProcessorManager changes_db;
    private ChangeFactory factory;

    public ChangesKBListener(KnowledgeBase kb) {
        changes_db = ChangesProject.getPostProcessorManager(kb);
        factory = new ChangeFactory(changes_db.getChangesKb());
    }
	/* (non-Javadoc)
	 * @see edu.stanford.smi.protege.event.KnowledgeBaseListener#clsCreated(edu.stanford.smi.protege.event.KnowledgeBaseEvent)
	 */
    @Override
	public void clsCreated(KnowledgeBaseEvent event) {
        if (event.isReplacementEvent()) {
            return;
        }
        final Cls createdCls = event.getCls();
        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createCreatedChange(changes_db, factory.createClass_Created(null), createdCls, createdCls.getName());
            }
         });
    }

	/* (non-Javadoc)
	 * @see edu.stanford.smi.protege.event.KnowledgeBaseListener#clsDeleted(edu.stanford.smi.protege.event.KnowledgeBaseEvent)
	 */
	@Override
	public void clsDeleted(KnowledgeBaseEvent event) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("In class deleted listener");
        }
        if (event.isReplacementEvent()) {
            return;
        }
        final String oldName = event.getOldName();
        final Frame frame = event.getCls();
        changes_db.submitChangeListenerJob(new Runnable() {
                public void run() {
                    ServerChangesUtil.createDeletedChange(changes_db, factory.createClass_Deleted(null), frame, oldName);
                }
            });
	}

	/* (non-Javadoc)
	 * @see edu.stanford.smi.protege.event.KnowledgeBaseListener#frameNameChanged(edu.stanford.smi.protege.event.KnowledgeBaseEvent)
	 */
	@Override
	public void frameNameChanged(KnowledgeBaseEvent event) {
	    final String oldName = event.getOldName();
        final Frame frame = event.getFrame();
	    final String newName = event.getNewFrame().getName();

        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createNameChange(changes_db, frame, oldName, newName);
            }
         });
	}

	/* (non-Javadoc)
	 * @see edu.stanford.smi.protege.event.KnowledgeBaseListener#instanceCreated(edu.stanford.smi.protege.event.KnowledgeBaseEvent)
	 */
	@Override
	public void instanceCreated(KnowledgeBaseEvent event) {
	    if (event.isReplacementEvent()) {
	        return;
	    }
	    if (log.isLoggable(Level.FINE)) {
	        log.fine("In created instance listener");
	    }
	    final Frame frame = event.getFrame();
        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createCreatedChange(changes_db, factory.createIndividual_Created(null), frame, frame.getName());
            }
         });
	}

	/* (non-Javadoc)
	 * @see edu.stanford.smi.protege.event.KnowledgeBaseListener#instanceDeleted(edu.stanford.smi.protege.event.KnowledgeBaseEvent)
	 */
	@Override
	public void instanceDeleted(KnowledgeBaseEvent event) {
	    if (event.isReplacementEvent()) {
	        return;
	    }
        if (log.isLoggable(Level.FINE)) {
            log.fine("In deleted instance listener");
        }
        final Frame frame = event.getFrame();
        final String name = event.getOldName();
        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createDeletedChange(changes_db, factory.createIndividual_Deleted(null), frame, name);
            }
         });
	}

	/* (non-Javadoc)
	 * @see edu.stanford.smi.protege.event.KnowledgeBaseListener#slotCreated(edu.stanford.smi.protege.event.KnowledgeBaseEvent)
	 */
	@Override
	public void slotCreated(KnowledgeBaseEvent event) {
	    if (event.isReplacementEvent()) {
	        return;
	    }
		final Slot createdSlot = event.getSlot();
        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createCreatedChange(changes_db, factory.createProperty_Created(null), createdSlot, createdSlot.getName());
            }
         });
	}

	/* (non-Javadoc)
	 * @see edu.stanford.smi.protege.event.KnowledgeBaseListener#slotDeleted(edu.stanford.smi.protege.event.KnowledgeBaseEvent)
	 */
	@Override
	public void slotDeleted(KnowledgeBaseEvent event) {
	    if (event.isReplacementEvent()) {
	        return;
	    }
        final String oldName = event.getOldName();
        final Frame frame = event.getSlot();
        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createDeletedChange(changes_db, factory.createProperty_Deleted(null), frame, oldName);
            }
         });
	}
}
