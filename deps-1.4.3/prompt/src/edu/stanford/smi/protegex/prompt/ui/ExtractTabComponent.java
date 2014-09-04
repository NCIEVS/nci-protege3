/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt.ui;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protegex.prompt.PromptTab;

public class ExtractTabComponent extends TabComponent {
	private JComponent _workingView;
	private TraversalDirectivesKbBrowser _traversalDirectivesKbBrowser = null;

	public ExtractTabComponent(Dimension size, int pluginPerspectiveType) {
		super(size, pluginPerspectiveType);
	}

	protected JComponent createContentPane() {
		JComponent mainPane;
		_workingView = super.createContentPane();
		if (PromptTab.makeViewsExplicit()) {
			JTabbedPane tabbedPane = ComponentFactory.createTabbedPane(true);
			_traversalDirectivesKbBrowser = new TraversalDirectivesKbBrowser();
			tabbedPane.addTab("Extract", _workingView);
			tabbedPane.addTab("Vew definitions", _traversalDirectivesKbBrowser);
			tabbedPane.setSelectedComponent(_workingView);
			mainPane = tabbedPane;
		} else {
			mainPane = _workingView;
		}
		return mainPane;
	}

}
