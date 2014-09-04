package edu.stanford.smi.protegex.server_changes.listeners.owl;

import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.smi.protege.event.ClsEvent;
import edu.stanford.smi.protegex.owl.model.NamespaceUtil;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.event.ClassAdapter;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.PostProcessorManager;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;

public class OwlChangesClassListener extends ClassAdapter {
    private OWLModel om;
    private PostProcessorManager changes_db;
    private ChangeFactory factory;

    public OwlChangesClassListener(OWLModel om) {
        this.om = om;
        changes_db = ChangesProject.getPostProcessorManager(om);
        factory = new ChangeFactory(changes_db.getChangesKb());
    }

	@Override
	public void instanceAdded(RDFSClass clz, final RDFResource inst, ClsEvent event) {
	    if (event.isReplacementEvent()) {
	        return;
	    }
		final String instText = inst.getBrowserText();
		final StringBuffer context = new StringBuffer();
		context.append("Instance Added: ");
		context.append(instText);
		context.append(" (instance of: " );
		context.append(clz.getBrowserText() );
		context.append(")");

        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createChangeStd(changes_db, factory.createIndividual_Added(null), inst, context.toString());
            }
         });
	}

	@Override
	public void instanceRemoved(RDFSClass clz, final RDFResource inst, ClsEvent event) {
        if (event.isReplacementEvent()) {
            return;
        }
        //String instText = changesDb.getPossiblyDeletedBrowserText(arg1);            
        final StringBuffer context = new StringBuffer();
        context.append("Instance Removed: ");
        context.append(NamespaceUtil.getLocalName(inst.getName()));
        context.append(" (instance of: ");
        context.append(clz.getBrowserText());
        context.append(")");

        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createChangeStd(changes_db, factory.createIndividual_Removed(null), inst, context.toString());
            }
         });
	}

	@Override
	public void addedToUnionDomainOf(RDFSClass arg0, RDFProperty arg1) {
	}

	@Override
	public void removedFromUnionDomainOf(RDFSClass arg0, RDFProperty arg1) {
	}

	@Override
	public void subclassAdded(RDFSClass cls, final RDFSClass subcls, ClsEvent event) {
	    if (event.isReplacementEvent()) {
	        return;
	    }
		final StringBuffer context = new StringBuffer();
		context.append("Subclass Added: ");
		context.append(subcls.getBrowserText());
		context.append(" (added to: ");
		context.append(cls.getBrowserText());
		context.append(")");

        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createChangeStd(changes_db, factory.createSubclass_Added(null), subcls, context.toString());
            }
         });
	}

	@Override
	public void subclassRemoved(RDFSClass supercls, final RDFSClass cls, ClsEvent event) {
        if (event.isReplacementEvent()) {
            return;
        }
        //String clsName = changes_db.getPossiblyDeletedBrowserText(arg1);
        final StringBuffer context = new StringBuffer();
        context.append("Subclass Removed: ");
        context.append(cls.getBrowserText());
        context.append(" (removed from: ");
        context.append(supercls.getBrowserText());
        context.append(")");

        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createChangeStd(changes_db, factory.createSubclass_Removed(null), cls, context.toString());
            }
         });
	}

	@Override
	public void superclassAdded(RDFSClass cls, final RDFSClass supercls) {		
		final StringBuffer context = new StringBuffer();
		context.append("Superclass Added: ");
		context.append(supercls.getBrowserText());
		context.append(" (added to: ");
		context.append(cls.getBrowserText());
		context.append(")");

        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createChangeStd(changes_db, factory.createSuperclass_Added(null), supercls, context.toString());
            }
         });
	}

	@Override
	public void superclassRemoved(RDFSClass cls, final RDFSClass supercls) {
		//String clsName = changes_db.getPossiblyDeletedBrowserText(arg1);
		final StringBuffer context = new StringBuffer();
		context.append("Superclass Removed: ");
		context.append(supercls.getBrowserText());
		context.append(" (removed from: ");
		context.append(cls.getBrowserText());
		context.append(")");

        changes_db.submitChangeListenerJob(new Runnable() {
            public void run() {
                ServerChangesUtil.createChangeStd(changes_db, factory.createSuperclass_Removed(null), supercls, context.toString());
            }
         });
	}
}
