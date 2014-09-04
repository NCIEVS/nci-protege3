/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt.ui.action;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.ui.diffUI.*;

public class DiffIconDialogAction extends AbstractAction {

		public DiffIconDialogAction() {
			super("Icons and fonts for comparison....");
		}

		public void actionPerformed(ActionEvent ae) {
			JFrame window = PromptTab.getMainWindow();
			DiffIconDialog dialog = new DiffIconDialog((Frame) window, "PromptDiff: Icons", false);
			dialog.setSize(500, 300);
			dialog.setLocationRelativeTo(window);
			dialog.setVisible(true);
		}
}
	