/*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui.diffUI;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protegex.prompt.ProjectsAndKnowledgeBases;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.FrameStatus;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.ResultTable;

public class DiffViewSetUp {
 
	private KnowledgeBase _kb1;
	private KnowledgeBase _kb2;
	private Project _p1;
	private Project _p2;
	private ResultTable _diffTable;
	private boolean _standAloneTreeView = false;
	private FrameStatus _frameStatuses;

	//use this non-static version of the class when you use DiffTreeView outside of the Prompt tab
	public DiffViewSetUp (Project p1, Project p2, ResultTable table) {
		_p1 = p1;
		_p2 = p2;
		_kb1 = p1.getKnowledgeBase();
		_kb2 = p2.getKnowledgeBase();
		_diffTable = table;
		_standAloneTreeView = true;
		PromptTab.getPromptDiff().setViewSetUp (this);
		_frameStatuses = new FrameStatus(_kb1, _kb2, _diffTable);
		
		PromptTab.getPromptDiff().setViewSetUp (this);
	}
	
	public DiffViewSetUp(){
		_kb1 = PromptTab.getPromptDiff().getKb1();
		_kb2 = PromptTab.getPromptDiff().getKb2();
		_p1 = ProjectsAndKnowledgeBases.getProject(_kb1);
		_p2 = ProjectsAndKnowledgeBases.getProject(_kb2);
		_diffTable = PromptTab.getPromptDiff().getResultsTable();
		
		PromptTab.getPromptDiff().setViewSetUp (this);
		_frameStatuses = new FrameStatus(_kb1, _kb2, _diffTable);
		PromptTab.getPromptDiff().setViewSetUp (this);
	}
	
	public Project getProject1 () {
		return _p1;
	}

	public Project getProject2 () {
		return _p2;
	}

	public KnowledgeBase getKb1 () {
		return _kb1;
	}

	public KnowledgeBase getKb2 () {
		return _kb2;
	}
	
	public ResultTable getResultsTable () {
		return _diffTable;
	}


	public boolean isStandAloneTreeView() {
		return _standAloneTreeView;
	}

	public void setStandAloneTreeView(boolean b) {
		_standAloneTreeView = b;
	}

	public FrameStatus getFrameStatuses() {
		return _frameStatuses;
	}

}
