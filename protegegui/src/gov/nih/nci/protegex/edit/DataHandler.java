package gov.nih.nci.protegex.edit;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import gov.nih.nci.protegex.panel.EditPanel;
import gov.nih.nci.protegex.tree.TreeItem;
import gov.nih.nci.protegex.tree.TreeItems;
import gov.nih.nci.protegex.tree.TreePanel;
import gov.nih.nci.protegex.util.SemanticTypeUtil;

import javax.swing.JComponent;

/**
 * @Author: NGIT, Kim Ong; Iris Guo
 * 
 * @author Bob Dionne
 * 
 */
public class DataHandler {
	private OWLWrapper wrapper;

	private NCIEditFilter filter;

	//foo
	private NCIEditTab tab;

	private OWLModel owlModel = null;

	public enum Status {
		FAILURE, SUCCESSFUL, IGNORE;

		public static Status toStatus(boolean successful) {
			return successful ? Status.SUCCESSFUL : Status.FAILURE;
		}
	};

	public DataHandler(NCIEditTab tab) {
		this.tab = tab;
		this.owlModel = tab.getOWLModel();
		this.wrapper = tab.getOWLWrapper();
		this.filter = tab.getFilter();
	}

	public Status processData(Cls cls, JComponent panel) {
		if (cls == null) {
			return Status.FAILURE;
		}
		boolean retval = wrapper.isEditable((OWLNamedClass) cls);
		if (!retval) {
			filter.setErrorMessage(cls.getBrowserText() + " is not editable.");
			return Status.FAILURE;
		}

		filter.setErrorMessage("");

		TreeItems init_state = null;
		TreeItems curr_state = null;

		if (panel instanceof TreePanel) {

			init_state = ((TreePanel) panel).getInitialState();
			curr_state = ((TreePanel) panel).getCurrentState();

		} else if (panel instanceof EditPanel) {
			init_state = ((EditPanel) panel).getInitialState();
			curr_state = ((EditPanel) panel).getFinalState();

		}

		try {
			DataHandler.Status res = processData(panel, cls, init_state, curr_state);
			tab.recordHistory(NCIEditTab.EVSHistoryAction.MODIFY, (OWLNamedClass) cls, "");
			return res;
			
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public Status canCreateData(String name, TreeItems state_vec) {

		filter.setCurrState(state_vec);
		filter.setInitialState(state_vec);

		if (!filter.run())
			return Status.FAILURE;

		if (wrapper.codeSlotExists && !filter.runCheckXMLNCNameCompliance(name))
			return Status.FAILURE;
		if (!filter.canAdd(state_vec, state_vec))
			return Status.FAILURE;

		return Status.SUCCESSFUL;

	}

	public boolean createCls(String name, TreeItems state_vec,
			boolean generatecode) {
        
        long t0 = System.currentTimeMillis();

		
		

		try {
			owlModel.beginTransaction("Create class " + name, name);
			
			OWLNamedClass cls = wrapper.createCls(name, generatecode);
			if (cls == null)
				return false;
			
			TreeItems equiv_items_2 = new TreeItems();
			TreeItems finalState = new TreeItems();

			for (int i = 0; i < state_vec.size(); i++) {
				TreeItem item = (TreeItem) state_vec.elementAt(i);
				if (item.getIsDefining()) {
					equiv_items_2.add(item);
				} else {
					finalState.add(item);
				}
			}

			RDFSClass newDefinition = null;
			if (equiv_items_2.size() == 1) {
				TreeItem item = (TreeItem) equiv_items_2.elementAt(0);
				newDefinition = item.getCls();
			}

			else if (equiv_items_2.size() > 1) // intersection class
			{
				OWLIntersectionClass temp_newDefinition = owlModel
						.createOWLIntersectionClass();
				for (int i = 0; i < equiv_items_2.size(); i++) {
					TreeItem item = (TreeItem) equiv_items_2.elementAt(i);
					RDFSClass operand = (RDFSClass) item.getCls();
					Log.getLogger().warning(
							"Creating clone of " + operand.getBrowserText());
					temp_newDefinition.addOperand(operand.createClone());
				}
				newDefinition = (RDFSClass) temp_newDefinition;
			}

			// tab.removeFromListenedToClses((OWLNamedClass) cls);
			if (newDefinition != null) {
				wrapper.addEquivalentClass((OWLNamedClass) cls, newDefinition);
			}

			// /////////////////////////////////////////////////////////////////
			// add equivalent class, if there is one

			try {
				if (!finalState.addItems(wrapper, cls)) {
					// Remove event lister (KLO, 06/30/06)
					// tab.addToListenedToClses((OWLNamedClass) cls);

					// 042707
					owlModel.rollbackTransaction();
					return false;
				}
			} catch (Exception ex) {
				owlModel.rollbackTransaction();
				// OWLUI.handleError(owlModel, ex);
				throw new RuntimeException(ex);
			}

			owlModel.commitTransaction();
			
			tab.recordHistory(NCIEditTab.EVSHistoryAction.CREATE, (OWLNamedClass) cls, "");
            
            Log.getLogger().info(
                    "Synchronization between local copy and ontology (ms): "
                            + (System.currentTimeMillis() - t0));
			return true;

		} catch (Exception e) {

			owlModel.rollbackTransaction();
			// OWLUI.handleError(owlModel, e);
			throw new RuntimeException(e);
			// return false;
		}
	}

	public Status canSaveData(JComponent panel, Cls cls) {

		if (cls == null) {
			return Status.FAILURE;
		}
		boolean retval = wrapper.isEditable((OWLNamedClass) cls);
		if (!retval) {
			filter.setErrorMessage(cls.getBrowserText() + " is not editable.");
			return Status.FAILURE;
		}
		
		boolean isRetired = wrapper.isRetired((OWLNamedClass) cls);
		if (isRetired) {
			if (tab.isActionAllowed(tab.EDIT_RETIRED)) {
				return Status.SUCCESSFUL;
			} else {
				filter.setErrorMessage("retired concepts are not editable.");
				return Status.FAILURE;
			}
		}

		filter.setErrorMessage("");

		TreeItems init_state = null;
		TreeItems curr_state = null;

		if (panel instanceof TreePanel) {

			init_state = ((TreePanel) panel).getInitialState();
			curr_state = ((TreePanel) panel).getCurrentState();

		} else if (panel instanceof EditPanel) {
			init_state = ((EditPanel) panel).getInitialState();
			curr_state = ((EditPanel) panel).getFinalState();

		}

		filter.setCurrState(curr_state);
		filter.setInitialState(init_state);

		if (!filter.run())
			return Status.FAILURE;

		// TODO: Bob, this needs to get out of here
		if (!SemanticTypeUtil.isPropertySet(curr_state)
				&& !SemanticTypeUtil.notSet_Continue(tab, cls))
			return Status.IGNORE;

		TreeItems inserted_items = init_state.getDiffs(curr_state);

		if (!filter.canAdd(inserted_items, init_state)) {
			Log.getLogger().warning(
					"Cannot add " + inserted_items + " to " + cls);
			return Status.FAILURE;
		}
		
		TreeItems deleted_items = curr_state.getDiffs(init_state);

		if (!filter.canDelete(deleted_items, init_state)) {
			Log.getLogger().warning(
					"Cannot delete " + deleted_items + " from " + cls);
			return Status.FAILURE;
		}
		
		

		return Status.SUCCESSFUL;

	}
	
	public Status checkDupsPT(JComponent panel, Cls cls) {

		

		filter.setErrorMessage("");

		TreeItems init_state = null;
		TreeItems curr_state = null;

		if (panel instanceof TreePanel) {

			init_state = ((TreePanel) panel).getInitialState();
			curr_state = ((TreePanel) panel).getCurrentState();

		} else if (panel instanceof EditPanel) {
			init_state = ((EditPanel) panel).getInitialState();
			curr_state = ((EditPanel) panel).getFinalState();

		}

		filter.setCurrState(curr_state);
		filter.setInitialState(init_state);

		if (!filter.runDupPT())
			return Status.FAILURE;

		
		

		return Status.SUCCESSFUL;

	}
	
	// New implementation of the processData method, which minimizes the cloning
	// at saving.
	private Status processData(JComponent parent, Cls cls,
			TreeItems initialState0, TreeItems finalState0) {

		TreeItems inserted_items = initialState0.getDiffs(finalState0);
		TreeItems deleted_items = finalState0.getDiffs(initialState0);
		
		TreeItems mod_delete_items = initialState0.getMods(finalState0);
		
		TreeItems mod_insert_items = finalState0.getMods(initialState0);

		// If no change was made, then don't do anything
		// TreeItems updated_vec = isLangUpdated(initialState0, finalState0);

		boolean success = true;
		long t0;

		t0 = System.currentTimeMillis();

		try {
			owlModel.beginTransaction("Save diffs for " + cls.getBrowserText(),
					cls.getName());
			
			success = success
			& inserted_items.addInsertedItems(wrapper,
					(OWLNamedClass) cls);
			success = success
			& deleted_items.removeDeletedItems(wrapper,
					(OWLNamedClass) cls);
			
            // now check mods
			
			success = success 
			& mod_delete_items.removeDeletedItems(wrapper, (OWLNamedClass) cls);
			
			
			
			success = success 
			& mod_insert_items.addInsertedItems(wrapper, (OWLNamedClass) cls);
			
			
			
			

			Log.getLogger().info(
					"Synchronization between local copy and ontology (ms): "
							+ (System.currentTimeMillis() - t0));
			// 111506
			// tab.addToListenedToClses((OWLNamedClass) cls);
			if (success) {
				owlModel.commitTransaction();
			} else {
				owlModel.rollbackTransaction();
			}
			return Status.toStatus(success);
			// return success;
		} catch (Exception e) {
			
			owlModel.rollbackTransaction();
			Log.getLogger().warning("Error at saving " + cls);
			// OWLUI.handleError(owlModel, e);
			throw new RuntimeException(e);
			// success = false;
			// return false;
		}
	}

}
