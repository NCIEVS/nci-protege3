/*
 * Author(s): Sean Falconer (seanf@uvic.ca)
  * 
*/

package edu.stanford.smi.protegex.prompt.ui.action;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.ui.plugin.PromptPluginManagerDialog;

public class PluginManagerDialogAction extends AbstractAction {
	private static final long serialVersionUID = -8035030298518979513L;

	public PluginManagerDialogAction() {
		super("Plugin manager");
	}
	
	public void actionPerformed(ActionEvent e) {
		JFrame window = PromptTab.getMainWindow();
		PromptPluginManagerDialog dialog = new PromptPluginManagerDialog((Frame) window, "Prompt Plugin Manager", false);
		dialog.setSize(500, 300);
		dialog.setLocationRelativeTo(window);
		dialog.setVisible(true);
	}	
}
