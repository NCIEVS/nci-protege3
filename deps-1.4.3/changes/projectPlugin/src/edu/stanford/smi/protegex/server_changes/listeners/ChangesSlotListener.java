package edu.stanford.smi.protegex.server_changes.listeners;

import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.smi.protege.event.SlotAdapter;
import edu.stanford.smi.protege.event.SlotEvent;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protegex.server_changes.PostProcessorManager;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;


public class ChangesSlotListener extends SlotAdapter{
    private PostProcessorManager changes_db;
    private ChangeFactory factory;

    public ChangesSlotListener(KnowledgeBase kb) {
        changes_db = ChangesProject.getPostProcessorManager(kb);
        factory = new ChangeFactory(changes_db.getChangesKb());
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.event.SlotListener#templateSlotClsAdded(edu.stanford.smi.protege.event.SlotEvent)
     */
    @Override
    public void templateSlotClsAdded(SlotEvent event) {
        if (event.getArgument() instanceof Cls) {
            final Cls theCls = event.getCls();
            final Slot theSlot = event.getSlot();

            final StringBuffer context = new StringBuffer();
            context.append("Added template slot: ");
            context.append(theSlot.getBrowserText());
            context.append(" to: ");
            context.append(theCls.getBrowserText());

            changes_db.submitChangeListenerJob(new Runnable() {
                    public void run() {
                        ServerChangesUtil.createChangeWithSlot(changes_db, factory.createTemplateSlot_Added(null), theCls, context.toString(), theSlot);
                    }
                });
        }
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.event.SlotListener#templateSlotClsRemoved(edu.stanford.smi.protege.event.SlotEvent)
     */
    @Override
    public void templateSlotClsRemoved(SlotEvent event) {
        if (event.getArgument() instanceof Cls) {
            final Cls theCls = event.getCls();
            final Slot theSlot = event.getSlot();
            //String name = changes_db.getPossiblyDeletedBrowserText(theCls);
            final String name = theCls.getName();
            final StringBuffer context = new StringBuffer();
            context.append("Removed template slot: ");
            context.append(theSlot.getBrowserText());
            context.append(" from: ");
            context.append(name);

            changes_db.submitChangeListenerJob(new Runnable() {
                    public void run() {
                        ServerChangesUtil.createChangeWithSlot(changes_db, factory.createTemplateSlot_Removed(null), theCls, context.toString(), theSlot);
                    }
                });
        }
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.event.SlotListener#directSubslotAdded(edu.stanford.smi.protege.event.SlotEvent)
     */
    @Override
    public void directSubslotAdded(SlotEvent event) {
        if (event.getArgument() instanceof Slot) {
            final Slot eventSlot = (Slot) event.getArgument();
            final String context = "Direct Subslot Added: " + eventSlot.getBrowserText();

            changes_db.submitChangeListenerJob(new Runnable() {
                    public void run() {
                        ServerChangesUtil.createChangeStd(changes_db, factory.createSubproperty_Added(null), eventSlot, context);
                    }
                });
        }
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.event.SlotListener#directSubslotRemoved(edu.stanford.smi.protege.event.SlotEvent)
     */
    @Override
    public void directSubslotRemoved(SlotEvent event) {
        if (event.getArgument() instanceof Slot) {
            final Slot eventSlot = (Slot) event.getArgument();
            //String name = changes_db.getPossiblyDeletedBrowserText(eventSlot);
            final String name = event.getSubslot().getName(); //TT -maybe null pointer
            final String context = "Direct Subslot Removed: " + name;

            changes_db.submitChangeListenerJob(new Runnable() {
                    public void run() {
                        ServerChangesUtil.createChangeStd(changes_db, factory.createSubproperty_Removed(null), eventSlot, context);
                    }
                });
        }
    }


    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.event.SlotListener#directSuperslotAdded(edu.stanford.smi.protege.event.SlotEvent)
     */
    @Override
    public void directSuperslotAdded(SlotEvent event) {
        if (event.getArgument() instanceof Slot) {
            final Slot eventSlot = (Slot) event.getArgument();
            final String context = "Direct Superslot Added: " + eventSlot.getBrowserText();

            changes_db.submitChangeListenerJob(new Runnable() {
                    public void run() {
                        ServerChangesUtil.createChangeStd(changes_db, factory.createSuperproperty_Added(null), eventSlot, context);
                    }
                });
        }
    }

    /* (non-Javadoc)
     * @see edu.stanford.smi.protege.event.SlotListener#directSuperslotRemoved(edu.stanford.smi.protege.event.SlotEvent)
     */
    @Override
    public void directSuperslotRemoved(SlotEvent event) {
        if (event.getArgument() instanceof Slot) {
            final Slot eventSlot = (Slot) event.getArgument();
            final String context = "Direct Superslot Removed: " + eventSlot.getBrowserText();

            changes_db.submitChangeListenerJob(new Runnable() {
                    public void run() {
                        ServerChangesUtil.createChangeStd(changes_db, factory.createSuperproperty_Removed(null), eventSlot, context);
                    }
                });
        }
    }
}
