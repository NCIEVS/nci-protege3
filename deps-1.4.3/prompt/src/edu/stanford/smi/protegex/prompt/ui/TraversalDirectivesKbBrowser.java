/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt.ui;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protege.widget.*;
import edu.stanford.smi.protegex.prompt.*;

public class TraversalDirectivesKbBrowser extends InstancesTab {
	private TraversalDirectivesKnowledgeBase _traversalDirectivesKb = PromptTab.getTraversalDirectivesKb ();
	private KnowledgeBase _kb = _traversalDirectivesKb.getKnowledgeBase();
	private Project _project = _traversalDirectivesKb.getProject();

	private InstanceDisplay _instanceDisplay;
	private TraversalDirectivesInstancesList _directInstancesList;

	public TraversalDirectivesKbBrowser () {
		initialize();
	}
	
	public KnowledgeBase getKnowledgeBase() {
		return _kb;
	}
	public Project getProject() {
		return _project;
	}

	public void initialize() {
		add(createInstanceSplitter());
//		_directInstancesList.setShowDisplaySlotPanel (false);
	}

	private JComponent createInstanceSplitter() {
		JSplitPane pane = ComponentFactory.createLeftRightSplitPane();
		pane.setDividerLocation(200);
		_instanceDisplay = (InstanceDisplay) createInstanceDisplay();
		pane.setLeftComponent(createInstancesPanel());
//		_instanceDisplay.setEnabled(false);
		pane.setRightComponent(_instanceDisplay);
		return pane;
	}

	private JComponent createInstancesPanel() {
		JPanel panel = ComponentFactory.createPanel();
		panel.setLayout(new BorderLayout());
		// panel.add(createClsDisplay(), BorderLayout.NORTH);
		panel.add(createDirectInstancesList(), BorderLayout.CENTER);
		return panel;
	}

	protected JComponent createDirectInstancesList() {
		_directInstancesList = new TraversalDirectivesInstancesList(getProject());
		Cls traversalDirectivesCls = PromptTab.getTraversalDirectivesKb().getTraversalDirectiveCls();
		_directInstancesList.setClses (CollectionUtilities.createCollection(traversalDirectivesCls));
		_directInstancesList.addSelectionListener(new SelectionListener() {
			public void selectionChanged(SelectionEvent event) {
				setInstanceDisplaySelection ();
			}
		});
		setInstanceDisplaySelection ();
		return _directInstancesList;
	}
	
	private void setInstanceDisplaySelection () {
		Collection selection = _directInstancesList.getSelection();
		Instance selectedInstance;
		if (selection.size() == 1) {
			selectedInstance = (Instance) CollectionUtilities.getFirstItem(selection);
		} else {
			selectedInstance = null;
		}
          	_instanceDisplay.setInstance(selectedInstance);
	}





	public String toString () {

	  return "TraversalDirectivesKbBrowser";
	}

}
