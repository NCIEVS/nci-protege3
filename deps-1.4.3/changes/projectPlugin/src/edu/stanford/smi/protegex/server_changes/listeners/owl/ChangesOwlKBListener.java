package edu.stanford.smi.protegex.server_changes.listeners.owl;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.smi.protege.event.KnowledgeBaseAdapter;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.PostProcessorManager;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;


public class ChangesOwlKBListener extends KnowledgeBaseAdapter {
    private static final Logger log = Log.getLogger(ChangesOwlKBListener.class);

    private PostProcessorManager changes_db;
    private ChangeFactory factory;

    public ChangesOwlKBListener(OWLModel om) {
        changes_db = ChangesProject.getPostProcessorManager(om);
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
        final Cls cls = event.getCls();
        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createCreatedChange(changes_db, factory.createClass_Created(null), cls, cls.getName());
            }
        });
	}

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.event.KnowledgeBaseListener#clsDeleted(edu.stanford.smi.protege.event.KnowledgeBaseEvent)
     */
    @Override
	public void clsDeleted(KnowledgeBaseEvent event) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("In deleted class listener");
        }
        if (event.isReplacementEvent()) {
            return;
        }
        final String deletedClsName = event.getOldName();
        final Frame deletedFrame = event.getCls();
        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createDeletedChange(changes_db, factory.createClass_Deleted(null), deletedFrame, deletedClsName);
            }
         });
    }


	/* (non-Javadoc)
	 * @see edu.stanford.smi.protege.event.KnowledgeBaseListener#frameNameChanged(edu.stanford.smi.protege.event.KnowledgeBaseEvent)
	 */
	@Override
	public void frameNameChanged(final KnowledgeBaseEvent event) {
        final String oldName = event.getOldName();

        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createNameChange(changes_db, event.getFrame(), oldName, event.getNewFrame().getName());
            }
         });
	}

	/* (non-Javadoc)
	 * @see edu.stanford.smi.protege.event.KnowledgeBaseListener#instanceCreated(edu.stanford.smi.protege.event.KnowledgeBaseEvent)
	 */
	@Override
	public void instanceCreated(KnowledgeBaseEvent event) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("In created instance listener");
        }
        if (event.isReplacementEvent()) {
            return;
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
        if (log.isLoggable(Level.FINE)) {
            log.fine("In deleted instance listener");
        }
        if (event.isReplacementEvent()) {
            return;
        }
        final String name = event.getOldName();
        final Frame frame = event.getFrame();
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
        final Frame prop = event.getFrame();
        final String propName = prop.getName();
        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createCreatedChange(changes_db, factory.createProperty_Created(null), prop, prop.getName());
            }
         });
	}

	/* (non-Javadoc)
	 * @see edu.stanford.smi.protege.event.KnowledgeBaseListener#slotDeleted(edu.stanford.smi.protege.event.KnowledgeBaseEvent)
	 */
	@Override
	public void slotDeleted(KnowledgeBaseEvent event) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("In deleted slot listener");
        }
        if (event.isReplacementEvent()) {
            return;
        }
        final String propName = event.getOldName();
        final Frame frame = event.getSlot();
        if (event.getSlot() instanceof RDFProperty) {
            changes_db.submitChangeListenerJob(new Runnable() {
                    public void run() {
                        ServerChangesUtil.createDeletedChange(changes_db,
                                                              factory.createProperty_Deleted(null),
                                                              frame,
                                                              propName);
                    }
                });

        }
	}


}
