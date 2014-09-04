package edu.stanford.smi.protegex.prompt.ui.action;

import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.mapping.*;

public class RunMappingInterpreterAction extends AbstractAction {
	private Project _sourceProject = null;
	private Project _targetProject = null;
	private Project _mappingProject = null;
	
	public RunMappingInterpreterAction() {
		super("Run mapping interpreter");
	}

	public void actionPerformed(ActionEvent ae) {
		int confirm = ModalDialog.showMessageDialog(PromptTab.getMainWindow(),
				  "This operation will delete current instances in the target knowledge base " + ProjectsAndKnowledgeBases.getMappingTargetKnowledgeBaseInMerging().getPrettyName(), ModalDialog.MODE_OK_CANCEL);

		createProjects();
		switch(confirm) {
	  	case ModalDialog.OPTION_OK:
				WaitCursor wait = new WaitCursor(PromptTab.getMainWindow());
				wait.show();
				VKBCInterpreter vkbcInterpreter = new VKBCInterpreter(_sourceProject, _targetProject, _mappingProject);
				vkbcInterpreter.invoke();

				//TODO: take care of multiple use of the same target
//				String targetPrjFilePath = targetPrj.getProjectURI().getPath();
//				_mappingPanel.getTargetKB().close();
//				_mappingPanel.getTargetKB().loadProject(targetPrjFilePath, false);
//				_mappingPanel.reloadTargetKBComponent();

				wait.hide();
				ModalDialog.showMessageDialog(PromptTab.getMainWindow(),"New instances loaded in the target knowledge base");
				break;

	  	case ModalDialog.OPTION_CANCEL:
				break;
			}

  	}
	
	private void createProjects () {
		if (_sourceProject != null) return;
		_sourceProject = ProjectsAndKnowledgeBases.getMappingSourceKnowledgeBaseInMerging().getProject();
		_targetProject = ProjectsAndKnowledgeBases.getMappingTargetKnowledgeBaseInMerging().getProject();
		_mappingProject = ProjectsAndKnowledgeBases.getFirstMappingKnowledgeBaseInMerging().getProject();
	}

}
