

/*
 * Contributor(s): Abhita Chugh abhita@stanford.edu
*/


package edu.stanford.smi.protegex.prompt.ui.diffUI;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.promptDiff.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;

public class DiffCompareDialog{
	protected DiffClsesPanel _clsesPanel;
    protected Instance _currentInstance;
	protected DiffTreeView _treeView;
	
	private ResultTable _diffTable;

	
	public DiffCompareDialog(JFrame parent,Instance instance, DiffTreeView treeView, DiffClsesPanel clsesPanel){
		_currentInstance = instance;
		_treeView = treeView;
		_clsesPanel = clsesPanel;
		_diffTable = PromptTab.getPromptDiff().getResultsTable();
        

		ModalDialog.showDialog(parent, getCompareInstancesPanel(), "Compare Versions", ModalDialog.MODE_CLOSE);
		
		
		
	}
	
	private JComponent createInstancesPane(){
		InstanceDisplay _instanceDisplayImage = new InstanceDisplay(ProjectsAndKnowledgeBases.getProject(PromptTab.getPromptDiff().getKb2()));
		_instanceDisplayImage.setInstance(_currentInstance);
		InstanceDisplay _instanceDisplaySource = new InstanceDisplay(ProjectsAndKnowledgeBases.getProject(PromptTab.getPromptDiff().getKb1()));
	    _instanceDisplaySource.setInstance((Instance)_diffTable.getSoleSource(_currentInstance));
	    
	    JSplitPane instancesPane = ComponentFactory.createLeftRightSplitPane();
		instancesPane.setDividerLocation(500);
		_instanceDisplaySource.setBorder(BorderFactory.createEtchedBorder());
		_instanceDisplayImage.setBorder(BorderFactory.createEtchedBorder());
		instancesPane.setLeftComponent(_instanceDisplaySource);
		instancesPane.setRightComponent(_instanceDisplayImage);
		
		return instancesPane;
		
	}
	
	
	private DiffTablePanel createDiffTablePanel(){
	 	DiffTablePanel _diffTablePanel;
		_diffTablePanel = new DiffTablePanel(_treeView, _clsesPanel, _diffTable);
		
		Collection rows = _diffTable.getRows(_currentInstance);
		if (rows != null && !rows.isEmpty())
		  _diffTablePanel.setRow((TableRow)CollectionUtilities.getFirstItem(rows),false);
	    
		return _diffTablePanel;
	}
	
	public JComponent getCompareInstancesPanel(){
	  	JSplitPane pane = ComponentFactory.createTopBottomSplitPane ();
		pane.setTopComponent(createInstancesPane());
		
		JComponent diffTable = new JPanel(new BorderLayout());
		JScrollPane scrollPane = ComponentFactory.createScrollPane();
		diffTable.add(scrollPane, BorderLayout.CENTER);
		scrollPane.setViewportView(createDiffTablePanel());
		
		pane.setBottomComponent(diffTable);
		pane.setOneTouchExpandable(true);
		pane.setDividerLocation(500);  
		return pane; 
	}


	
	

}
