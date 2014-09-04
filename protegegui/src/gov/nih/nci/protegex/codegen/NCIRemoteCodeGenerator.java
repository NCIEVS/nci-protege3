/**
 * 
 */
package gov.nih.nci.protegex.codegen;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.plugin.AbstractProjectPlugin;
import edu.stanford.smi.protege.exception.ProtegeIOException;
import edu.stanford.smi.protege.ui.ProjectMenuBar;
import edu.stanford.smi.protege.ui.ProjectToolBar;
import edu.stanford.smi.protege.ui.ProjectView;

import java.io.File;

/**
 * @author Manual Re Count
 * 
 */
public class NCIRemoteCodeGenerator extends AbstractProjectPlugin {

	// TODO: check for props fiel and throw exception to console if missing
	public void afterCreate(Project p) {
		//System.out.println("afterCreate");
		

	}

	public void afterSave(Project p) {
		// Log.enter(this, "beforeSave", p);
	}

	public void afterLoad(Project p) {
		//System.out.println("afterLoadFoo");
		/** TODO: client has no props file so this needs to be changed
		try {
			File props = new File("codegen.properties");
			if (!props.exists()) {
				throw new ProtegeIOException(
						"Unable to find props file for Code Generator");
			}
		} catch (NullPointerException e) {
			System.out
					.println("This will happen if the code 5 lines above is written wrong");
			e.printStackTrace();
		}
		**/
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
		//System.out.println("dispose");
		// Log.enter(this, "dispose");
	}

}
