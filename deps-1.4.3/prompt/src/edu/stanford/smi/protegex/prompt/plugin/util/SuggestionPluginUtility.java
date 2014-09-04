package edu.stanford.smi.protegex.prompt.plugin.util;

import java.util.Collection;

import javax.swing.AbstractAction;

import edu.stanford.smi.protegex.prompt.plugin.SelectionListener;
import edu.stanford.smi.protegex.prompt.ui.ActionListPane;
import edu.stanford.smi.protegex.prompt.ui.MappingTabComponent;
import edu.stanford.smi.protegex.prompt.ui.SuggestionListPane;

public class SuggestionPluginUtility implements SelectableContainer {
	public void addHeaderButton(AbstractAction headerButton) {
		SuggestionListPane todoPane = (SuggestionListPane) MappingTabComponent.getTodoPane();

		todoPane.addHeaderButton(headerButton);
	}

	public void addSelectionListener(SelectionListener selectionListener) {
		SuggestionListPane todoPane = (SuggestionListPane) MappingTabComponent.getTodoPane();
		ActionListPane actionPane = todoPane.getTodoList();

		actionPane.addSelectionListener(selectionListener);
	}

	public void addMappingCreationListener() {

	}

	public Collection getList() {
		SuggestionListPane todoPane = (SuggestionListPane) MappingTabComponent.getTodoPane();
		ActionListPane actionPane = todoPane.getTodoList();

		return actionPane.getActionList();
	}

	public void updateList(Collection list) {
		SuggestionListPane todoPane = (SuggestionListPane) MappingTabComponent.getTodoPane();
		ActionListPane actionPane = todoPane.getTodoList();

		actionPane.changeList(list);
	}
}
