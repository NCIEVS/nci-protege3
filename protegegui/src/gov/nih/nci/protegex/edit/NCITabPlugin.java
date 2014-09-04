/**
 * 
 */
package gov.nih.nci.protegex.edit;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.ui.ProjectMenuBar;
import edu.stanford.smi.protege.ui.ProjectToolBar;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.plugin.*;
import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;
import gov.nih.nci.protegex.util.UIUtil;
import gov.nih.nci.protegex.workflow.*;

import java.util.HashMap;
import java.util.Set;
import java.util.ArrayList;

/**
 * @author Bob Dionne
 * 
 */
public class NCITabPlugin extends AbstractProjectPlugin {

	// TODO: check for props fiel and throw exception to console if missing
	public void afterCreate(Project p) {
		// System.out.println("afterCreate");

	}

	public void afterSave(Project p) {
		// Log.enter(this, "beforeSave", p);
	}

	public void afterLoad(Project p) {
		// System.out.println("afterLoad");

	}

	/**
	 * This project plugin was added solely to disable the menu items based on a
	 * config file. The config file is loaded by NCIEditTab, and perhaps it
	 * ought to be done here, I'm not sure. Apparently other pluging will
	 * restore menu items so it's best done in the afterShow method and still
	 * depends on the other plugins having been called first.
	 * 
	 * This is not pretty but it works. -- Bob Dionne
	 */
	public void afterShow(ProjectView view, ProjectToolBar toolBar,
			ProjectMenuBar menuBar) {

		HashMap<String, ArrayList<String>> disMenus = NCIEditTab.config
				.getDisableMenus();

		Set<String> keys = disMenus.keySet();

		for (String k : keys) {
			ArrayList<String> subs = disMenus.get(k);
			for (String ss : subs) {
				disableMenuItems(k, ss);
			}
		}

		// add workflow panel to EditTab
		// if workflow tab is enabled
		NCIEditTab tab = (NCIEditTab) UIUtil.getTab(NCIEditTab.class
				.getName());
		tab.addChangeListener();
		NCIWorkflowTab wftab = (NCIWorkflowTab) UIUtil
				.getTab(NCIWorkflowTab.class.getName());
		if (wftab != null) {
			wftab.addChangeListener();
			tab.addWorkFlowPanel();
			NCIWorkflowTab.WIKI_URL = NCIEditTab.config.getSmwBaseUrl();
			NCIWorkflowTab.WIKI_TOP_PAGE = NCIEditTab.config.getSmwTopLevelPage();
		}

	}

	private void disableMenuItems(String menu, String submenu) {

		ResourceKey resourceKey_Menu = key(menu);
		ResourceKey resourceKey_SubMenu = key(submenu);
		if (resourceKey_Menu == null || resourceKey_SubMenu == null) {
			System.out
					.println("resourceKey_Menu == null || resourceKey_SubMenu == null");
			return;
		}

		try {
			JMenuBar menuBar = ProjectManager.getProjectManager()
					.getCurrentProjectMenuBar();
			disableMenuItem((ProjectMenuBar) menuBar, resourceKey_Menu,
					resourceKey_SubMenu, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ResourceKey key(String s) {
		return new ResourceKey(s);
	}

	private void disableMenuItem(ProjectMenuBar menuBar, ResourceKey menuKey,
			ResourceKey resourceKey, boolean removeTrailingSeparator) {
		String menuName = LocalizedText.getText(menuKey);
		String itemName = LocalizedText.getText(resourceKey);
		JMenu projectMenu = menuBar.getMenu(0);

		for (int i = 1; !projectMenu.getText().equals(menuName); i++) {
			projectMenu = menuBar.getMenu(i);
		}

		for (int i = 0; i < projectMenu.getItemCount(); i++) {
			JMenuItem item = projectMenu.getItem(i);

			if (item != null && itemName.equals(item.getText())) {

				projectMenu.remove(i);

			} else if (item != null) {
				// do nothing
			}
		}
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
		// System.out.println("dispose");
		// Log.enter(this, "dispose");
	}

}
