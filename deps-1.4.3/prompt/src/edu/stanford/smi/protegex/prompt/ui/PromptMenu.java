/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.prompt.ui;

import javax.swing.AbstractAction;
import javax.swing.JMenu;

import edu.stanford.smi.protegex.prompt.ui.action.ConfigureAction;
import edu.stanford.smi.protegex.prompt.ui.action.DiffIconDialogAction;
import edu.stanford.smi.protegex.prompt.ui.action.PerspectivesMenuAction;
import edu.stanford.smi.protegex.prompt.ui.action.PluginManagerDialogAction;
import edu.stanford.smi.protegex.prompt.ui.action.RemoveSuffixesAction;
import edu.stanford.smi.protegex.prompt.ui.action.RunMappingInterpreterAction;

public class PromptMenu extends JMenu {
	protected AbstractAction _removeSuffixes = new RemoveSuffixesAction();
	protected AbstractAction _configure = new ConfigureAction();
	protected AbstractAction _diffIconDialog = new DiffIconDialogAction();
	protected AbstractAction _runMappingInterpreter = new RunMappingInterpreterAction();
	protected AbstractAction _pluginManagerDialog = new PluginManagerDialogAction();
	protected JMenu _perspectivesMenu = new PerspectivesMenuAction();

	public PromptMenu() {
		super("Prompt");

		add(_runMappingInterpreter);
		add(_removeSuffixes);
		add(_configure);
		add(_diffIconDialog);
		add(_pluginManagerDialog);
		add(_perspectivesMenu);

		_runMappingInterpreter.setEnabled(false);
		_removeSuffixes.setEnabled(false);
		_configure.setEnabled(false);
		_diffIconDialog.setEnabled(false);
		_pluginManagerDialog.setEnabled(true);
	}

	public void setRemoveSuffixesEnabled(boolean value) {
		_removeSuffixes.setEnabled(value);
	}

	public void setConfigureEnabled(boolean value) {
		_configure.setEnabled(value);
	}

	public void setDiffIconDialogEnabled(boolean value) {
		_diffIconDialog.setEnabled(true);
	}

	public void setRunMappingInterpreterEnabled(boolean value) {
		_runMappingInterpreter.setEnabled(true);
	}

}
