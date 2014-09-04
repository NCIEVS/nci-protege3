package edu.stanford.smi.protegex.changes.changesKBViewTab;

import java.awt.Container;
import java.util.Collection;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.DirectInstancesList;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SelectableList;
import edu.stanford.smi.protege.widget.AbstractTabWidget;
import edu.stanford.smi.protege.widget.ClsesAndInstancesTab;
import edu.stanford.smi.protegex.server_changes.ChangesProject;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class ChangesKBViewTab extends AbstractTabWidget {
	//TODO: Clean up this class

	ClsesAndInstancesTab ciTab;
	Project changesProject;

	public void initialize() {
		setLabel("ChangesKBViewTab");

		changesProject = getChangesProject();
		buildGUI();
	}

	public void buildGUI() {
		if (changesProject == null) {
			return;
		}

		ciTab = new ClsesAndInstancesTab();
		ciTab.setup(getDescriptor(), changesProject);
		ciTab.initialize();

		//getClsTree().setCellRenderer(new FrameIDRenderer());
		//adjustDirectInstanceRenderer();

		add(ciTab);
	};


	public Project getChangesProject() {
		if (changesProject != null) {
			return changesProject;
		}

		KnowledgeBase changesKB = null;

		if (getProject() != null) {
			changesKB = ChAOKbManager.getChAOKb(getKnowledgeBase());
		}

		if (changesKB != null) {
			changesProject = changesKB.getProject();
		}

		return changesProject;
	}


	public static boolean isSuitable(Project p, Collection errors) {
		try {
			KnowledgeBase kb = ChangesProject.getChangesKB(p.getKnowledgeBase());
			if (kb == null) {
				errors.add("Needs Changes ontology");
				return false;
			}

			return true;
		} catch (Exception e) {
			errors.add("Needs Changes ontology");
			return false;
		}
	}


	private void adjustDirectInstanceRenderer() {
		try {
			//ugly way of getting the instance list, because there are no getter methods in the superclass
			DirectInstancesList dirList = (DirectInstancesList)((Container)((Container)((Container)getComponent(0)).getComponent(2)).getComponent(1)).getComponent(1);
			((SelectableList)dirList.getSelectable()).setCellRenderer(new FrameIDRenderer());

		} catch (Exception e) {
			Log.getLogger().warning("Error at setting browser slot " + e.getMessage());
		}
	}

	private final class FrameIDRenderer extends FrameRenderer {
		@Override
		public void load(Object value) {
			super.load(value);
		}
	}


	@Override
	public void dispose() {
		if (getProject().isMultiUserClient() && changesProject != null) {
			try {
				//FIXME: CHAOKBManager should take care of this
				//changesProject.dispose();
			} catch (Exception e) {
				Log.getLogger().warning("Errors at disposing changes project " + changesProject + " of project " + changesProject);
			}
		}
		//TODO: remove the listeners
		super.dispose();
	}


}
