package edu.stanford.smi.protegex.server_changes.listeners;

import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.SystemFrames;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protegex.server_changes.PostProcessorManager;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;


public class ChangesFrameListener extends FrameAdapter {
	private PostProcessorManager changes_db;
	private ChangeFactory factory;


	public ChangesFrameListener(KnowledgeBase kb) {
		changes_db = ChangesProject.getPostProcessorManager(kb);
		factory = new ChangeFactory(changes_db.getChangesKb());
	}

	@Override
	public void ownSlotValueChanged(FrameEvent event) {

		final Frame frame = event.getFrame();
		final String frameName = frame.getBrowserText();
		final KnowledgeBase kb = frame.getKnowledgeBase();
		final SystemFrames systemFrames = kb.getSystemFrames();

		final Slot ownSlot = event.getSlot();
		final String newSlotValue = CollectionUtilities.toString(frame.getOwnSlotValues(event.getSlot()));

		final StringBuffer context = new StringBuffer();

		if(ownSlot.equals(systemFrames.getDocumentationSlot())) {

			if(newSlotValue.equals("")) {
				context.append("Removed documentation from ");
				context.append(frameName);
                changes_db.submitChangeListenerJob(new Runnable() {
                        public void run() {
                            ServerChangesUtil.createChangeStd(changes_db, factory.createDocumentation_Removed(null), frame, context.toString());
                        }
                    });
			}
			else {
				context.append("Added documentation: ");
				context.append(newSlotValue);
				context.append(" to: ");
				context.append(frameName);

                changes_db.submitChangeListenerJob(new Runnable() {
                        public void run() {
                            ServerChangesUtil.createChangeStd(changes_db, factory.createDocumentation_Added(null), frame, context.toString());
                        }
                    });
			}

		} else if (ownSlot.equals(systemFrames.getMaximumValueSlot())) {

			context.append("Maximum value for: ");
			context.append(frameName);
			context.append(" set to: ");
			context.append(newSlotValue);

            changes_db.submitChangeListenerJob(new Runnable() {
                    public void run() {
                        ServerChangesUtil.createChangeStd(changes_db, factory.createMaximum_Value(null), frame, context.toString());
                    }
                });
        
		} else if (ownSlot.equals(systemFrames.getMinimumValueSlot())){

			context.append("Minimum value for: ");
			context.append(frameName);
			context.append(" set to: ");
			context.append(newSlotValue);

            changes_db.submitChangeListenerJob(new Runnable() {
                    public void run() {
                        ServerChangesUtil.createChangeStd(changes_db, factory.createMinimum_Value(null), frame, context.toString());
                    }
                });

		} else if (ownSlot.equals(systemFrames.getMinimumCardinalitySlot())){

			if(!newSlotValue.equals("")){
				context.append("Minimum cardinality for: ");
				context.append(frameName);
				context.append(" set to: ");
				context.append(newSlotValue);

                changes_db.submitChangeListenerJob(new Runnable() {
                        public void run() {
                            ServerChangesUtil.createChangeStd(changes_db, factory.createMinimum_Cardinality(null), frame, context.toString());
                        }
                    });
			}
		} else if (ownSlot.equals(systemFrames.getMaximumCardinalitySlot())){

			if (newSlotValue.equals("")){
				context.append(frameName);
				context.append(" can take multiple values");

                changes_db.submitChangeListenerJob(new Runnable() {
                        public void run() {
                            ServerChangesUtil.createChangeStd(changes_db, factory.createMaximum_Cardinality(null), frame, context.toString());
                        }
                    });

			}
			else {
				context.append("Maximum cardinality for: ");
				context.append(frameName);
				context.append(" set to: ");
				context.append(newSlotValue);

                changes_db.submitChangeListenerJob(new Runnable() {
                        public void run() {
                            ServerChangesUtil.createChangeStd(changes_db, factory.createMaximum_Cardinality(null), frame, context.toString());
                        }
                    });
			}
		} else if (ownSlot.isSystem()) {
			//TT: do nothing, it is handled somewhere else (hopefully!)
		} else {

			context.append("Set value for ");
			context.append(ownSlot.getBrowserText());
			context.append(" for ");
			context.append(frameName);
			context.append(" to: ");
			context.append(newSlotValue);

            changes_db.submitChangeListenerJob(new Runnable() {
                    public void run() {
                        ServerChangesUtil.createChangeStd(changes_db, factory.createProperty_Value(null), frame, context.toString());
                    }
                });
		}
	}

}
