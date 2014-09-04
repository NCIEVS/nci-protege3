package edu.stanford.smi.protegex.prompt.ui.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.plugin.PluginFacade;
import edu.stanford.smi.protegex.prompt.plugin.PluginManager;
import edu.stanford.smi.protegex.prompt.plugin.model.PromptPlugin;
import edu.stanford.smi.protegex.prompt.plugin.model.PromptUIPerspective;

public class PerspectivesMenuAction extends JMenu {
	private int pluginType;

	public PerspectivesMenuAction() {
		super("Perspectives");

		add(new AbstractAction("*Default") {
			public void actionPerformed(ActionEvent e) {
				changePerspective("Default");
			}
		});
	}

	/**
	 * Loads the perspectives for the given pluginType.
	 * 
	 * @param pluginType
	 */
	public void loadPerspectives(int pluginType) {
		this.pluginType = pluginType;

		PromptUIPerspective defaultPerspective = PluginFacade.getDefaultPerspective(pluginType);
		List<PromptPlugin> plugins = PluginManager.getInstance().getPlugins(pluginType);
		for (PromptPlugin plugin : plugins) {
			add(new AbstractAction(plugin.getPluginName()) {
				public void actionPerformed(ActionEvent e) {
					changePerspective(this.getValue(AbstractAction.NAME).toString());
				}
			});

			if (defaultPerspective != null && defaultPerspective.equals(plugin)) {
				JMenuItem menuItem = getMenuItem(plugin.getPluginName());
				if (menuItem != null) {
					menuItem.setText("*" + plugin.getPluginName());
				}
				//JMenuItem defaultMenuItem = getMenuItem("*Default");
				//defaultMenuItem.setText("Default");
			}
		}
	}

	/**
	 * Changes the Prompt view perspective to the plugin based on the pluginName.
	 * 
	 * @param pluginName
	 */
	private void changePerspective(String pluginName) {
		JMenuItem menuItem = getMenuItem(pluginName);
		if (menuItem == null) {
			return;
		}

		menuItem.setText("*" + pluginName);

		PromptUIPerspective promptPlugin = (PromptUIPerspective) PluginManager.getInstance().getPlugin(pluginType, pluginName);
			PluginFacade.setDefaultPerspective(pluginType, pluginName);

		PromptTab.getTabComponent().setPerspective(promptPlugin);
	}

	private JMenuItem getMenuItem(String pluginName) {
		JMenuItem foundMenuItem = null;
		for (int i = 0; i < this.getMenuComponentCount(); i++) {
			JMenuItem menuItem = (JMenuItem) this.getMenuComponent(i);
			if (menuItem.getText() != null && menuItem.getText().equals(pluginName)) {
				foundMenuItem = menuItem;
			} else if (menuItem.getText().charAt(0) == '*') {
				menuItem.setText(menuItem.getText().substring(1));
				PromptPlugin promptPlugin = PluginManager.getInstance().getPlugin(pluginType, menuItem.getText());
				if (promptPlugin != null) {
					((PromptUIPerspective) promptPlugin).beforeClose();
				}
				if (menuItem.getText() != null && menuItem.getText().equals(pluginName)) {
					foundMenuItem = menuItem;
			}
		}
		}

		return foundMenuItem;
	}
}
