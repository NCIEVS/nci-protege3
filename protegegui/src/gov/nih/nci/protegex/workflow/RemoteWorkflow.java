package gov.nih.nci.protegex.workflow;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.plugin.AbstractProjectPlugin;
import edu.stanford.smi.protege.ui.ProjectMenuBar;
import edu.stanford.smi.protege.ui.ProjectToolBar;
import edu.stanford.smi.protege.ui.ProjectView;



public class RemoteWorkflow extends AbstractProjectPlugin {
	

	public void afterCreate(Project p) {
		///System.out.println("afterCreate");
	}
	
	public void afterSave(Project p) {
		// Log.enter(this, "beforeSave", p);
	}
	
	
	

	

	public void afterLoad(Project p) {
		//System.out.println("afterLoad");

	}

	public void afterShow(ProjectView view, ProjectToolBar toolBar,
			ProjectMenuBar menuBar) {

	}

	public void beforeSave(Project p) {
		// Log.enter(this, "beforeSave", p);
	}

	

	public void beforeHide(ProjectView view, ProjectToolBar toolBar,
			ProjectMenuBar menuBar) {
		// Log.enter(this, "beforeHide", view, toolBar, menuBar);
	}

	public void beforeClose(Project p) {
		// Log.enter(this, "beforeClose", p);
	}

	public String getName() {
		return "My Project Plugin Tester";
	}

	public void dispose() {
		System.out.println("dispose");
		// Log.enter(this, "dispose");
	}
	


}
