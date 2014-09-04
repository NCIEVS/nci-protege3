package edu.stanford.smi.protegex.server_changes.listeners;

import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.smi.protege.event.ClsAdapter;
import edu.stanford.smi.protege.event.ClsEvent;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protegex.server_changes.PostProcessorManager;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;

public class ChangesClsListener extends ClsAdapter{
    private PostProcessorManager changes_db;
    private ChangeFactory factory;

    public ChangesClsListener(KnowledgeBase kb) {
        changes_db = ChangesProject.getPostProcessorManager(kb);
        factory = new ChangeFactory(changes_db.getChangesKb());
    }

	/* (non-Javadoc)
	 * @see edu.stanford.smi.protege.event.ClsListener#directInstanceAdded(edu.stanford.smi.protege.event.ClsEvent)
	 */
	@Override
	public void directInstanceAdded(ClsEvent event) {
	    if (event.isReplacementEvent()) {
	        return;
	    }
		final Instance addedInst = event.getInstance();
		final Cls clsOfInst = event.getCls();

		final StringBuffer context = new StringBuffer();
		context.append("Added Instance: ");
		context.append(addedInst.getBrowserText());
		context.append(" (instance of ");
		context.append(clsOfInst.getBrowserText());
		context.append(")");

        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createChangeStd(changes_db, factory.createIndividual_Added(null), addedInst, context.toString());
            }
         });
	}

	/* (non-Javadoc)
	 * @see edu.stanford.smi.protege.event.ClsListener#directInstanceRemoved(edu.stanford.smi.protege.event.ClsEvent)
	 */
	@Override
	public void directInstanceRemoved(ClsEvent event) {
	    if (event.isReplacementEvent()) {
	        return;
	    }
		final Instance removedInst = event.getInstance();
		final Cls clsOfInst = event.getCls();

		final StringBuffer context = new StringBuffer();
		context.append("Removed Instance: ");
		context.append(removedInst.getBrowserText());
		context.append(" (instance of ");
		context.append(clsOfInst.getBrowserText());
		context.append(")");

        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createChangeStd(changes_db, factory.createIndividual_Removed(null), removedInst, context.toString());
            }
         });
	}

	/* (non-Javadoc)
	 * @see edu.stanford.smi.protege.event.ClsListener#directSubclassAdded(edu.stanford.smi.protege.event.ClsEvent)
	 */
	@Override
	public void directSubclassAdded(ClsEvent event) {
	    if (event.isReplacementEvent()) {
	        return;
	    }
		final Cls subClass = event.getSubclass();
		final Cls superClass = event.getCls();

		final StringBuffer context = new StringBuffer();
		context.append("Added subclass: ");
		context.append(subClass.getBrowserText());
		context.append(" (subclass of ");
		context.append(superClass.getBrowserText());
		context.append(")");

        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createChangeStd(changes_db, factory.createSubclass_Added(null), subClass, context.toString());
            }
         });
	}


	/* (non-Javadoc)
	 * @see edu.stanford.smi.protege.event.ClsListener#directSubclassRemoved(edu.stanford.smi.protege.event.ClsEvent)
	 */
	@Override
	public void directSubclassRemoved(ClsEvent event) {
	    if (event.isReplacementEvent()) {
	        return;
	    }
		final Cls subClass = event.getSubclass();
		final Cls superClass = event.getCls();

		final StringBuffer context = new StringBuffer();
		context.append("Removed subclass: ");
		context.append(subClass.getBrowserText());
		context.append(" (subclass of ");
		context.append(superClass.getBrowserText());
		context.append(")");

        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createChangeStd(changes_db, factory.createSubclass_Removed(null), subClass, context.toString());
            }
         });
	}

	/* (non-Javadoc)
	 * @see edu.stanford.smi.protege.event.ClsListener#directSuperclassAdded(edu.stanford.smi.protege.event.ClsEvent)
	 */
	@Override
	public void directSuperclassAdded(ClsEvent event) {
	    if (event.isReplacementEvent()) {
	        return;
	    }
		final Cls subClass = event.getSubclass();
		final Cls superClass = event.getCls();

		final StringBuffer context = new StringBuffer();
		context.append("Added superclass: ");
		context.append(subClass.getBrowserText());
		context.append(" (subclass of ");
		context.append( superClass.getBrowserText());
		context.append(")");

        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createChangeStd(changes_db, factory.createSuperclass_Added(null), subClass, context.toString());
            }
         });
	}

	/* (non-Javadoc)
	 * @see edu.stanford.smi.protege.event.ClsListener#directSuperclassRemoved(edu.stanford.smi.protege.event.ClsEvent)
	 */
	@Override
	public void directSuperclassRemoved(ClsEvent event) {
	    if (event.isReplacementEvent()) {
	        return;
	    }
		final Cls subClass = event.getSubclass();
		final Cls superClass = event.getCls();

		final StringBuffer context = new StringBuffer();
		context.append("Removed superclass: ");
		context.append(subClass.getBrowserText());
		context.append(" (subclass of ");
		context.append(superClass.getBrowserText());
		context.append(")");

        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createChangeStd(changes_db, factory.createSubclass_Removed(null), subClass, context.toString());
            }
         });
	}

}
