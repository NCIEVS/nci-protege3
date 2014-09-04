package edu.stanford.smi.protegex.prompt.ui.diffUI;

//import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;

import javax.swing.*;

//import edu.stanford.smi.protege.action.*;
//import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protege.ui.*;

import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.ui.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class DiffDirectInstancesList extends DirectInstancesList {
	ResultTable _diffTable;

	AcceptorRejector _acceptorRejector;

	DiffTreeView _treeView;

	Cls _cls;

	public DiffDirectInstancesList(Project project, DiffTreeView treeView) {
		super(project);
		_treeView = treeView;
		_diffTable = treeView.getDiffTable();
		_acceptorRejector = PromptTab.getPromptDiff().getAcceptorRejector();

		SelectableList list = getList();
		list.setCellRenderer(new DiffFrameRenderer(_diffTable));

	}

	public void setCls(Cls cls) {

		_cls = cls;

		ArrayList clsList = new ArrayList();
		if (cls == null) {
			setClses(clsList);
			return;
		}

		clsList.add(cls);
		setClses(clsList);
		addDeletedInstances(cls);

	}

	public void select(Instance instance) {
		if (instance.getDirectType() != _cls) {
			return;
		}

		SelectableList list = getList();
		list.setSelectedValue(instance, true);
	}

	public void addDeletedInstances(Cls cls) {

		Collection deletedInstances = Util.getDeletedInstances(cls, _diffTable);

		SelectableList list = getList();
		SimpleListModel listModel = (SimpleListModel) list.getModel();
		listModel.addValues(deletedInstances);
	}

	protected void addButtons(Action viewAction, LabeledComponent c) {
		super.addButtons(viewAction, c);
		c.removeAllHeaderButtons();
		c.addHeaderButton(createReferencersAction());
		c.addHeaderButton(new AcceptInstanceAction("Accept Instance Change"));
		c.addHeaderButton(new RejectInstanceAction("Reject Instance Change"));
	}

	protected SelectableList getList() {
		return (SelectableList) getDragComponent();
	}

	private class AcceptInstanceAction extends AllowableAction {

		public AcceptInstanceAction(String prompt) {
			super(prompt, ComponentUtilities.loadImageIcon(TabComponent.class,
					"images/OK.gif"), getList());
		}

		public void actionPerformed(ActionEvent evt) {
			Collection selection = getSelection();
			for (Iterator iter = selection.iterator(); iter.hasNext();) {
				Instance instance = (Instance) iter.next();
				_acceptorRejector.acceptInstance(_cls, instance);
				ProjectsAndKnowledgeBases.getAcceptChangesKb().addToKB(
						AcceptChangesKnowledgeBase.ACCEPT_INSTANCE,
						_cls.getName(), null, null, instance.getName());
			}
			_treeView.reloadRHS();
		}

		public void onSelectionChange() {
			setAllowed(isAllowed());
		}

		public boolean isAllowed() {
			boolean allowed = false;

			if (_cls == null)
				return allowed;

			TableRow row = (TableRow) CollectionUtilities
					.getFirstItem(_diffTable.getRows(_cls));
			String operationValue = row.getOperationValue();

			if (row.isChangeAccepted()
					|| (!row.isChanged()
							&& operationValue != TableRow.OPERATION_ADD && operationValue != TableRow.OPERATION_DELETE)) {
				allowed = true;
			}

			return allowed;
		}
	}

	private class RejectInstanceAction extends AllowableAction {

		public RejectInstanceAction(String prompt) {
			super(prompt, ComponentUtilities.loadImageIcon(TabComponent.class,
					"images/Cancel.gif"), getList());
		}

		public void actionPerformed(ActionEvent evt) {
			Collection selection = getSelection();
			for (Iterator iter = selection.iterator(); iter.hasNext();) {
				Instance instance = (Instance) iter.next();
				_acceptorRejector.rejectInstance(_cls, instance);
			}
			_treeView.reloadRHS();
		}

		public void onSelectionChange() {
			setAllowed(isAllowed());
		}

		public boolean isAllowed() {
			boolean allowed = false;

			if (_cls == null)
				return allowed;

			TableRow row = (TableRow) CollectionUtilities
					.getFirstItem(_diffTable.getRows(_cls));
			String operationValue = row.getOperationValue();

			if (row.isChangeAccepted()
					|| (!row.isChanged()
							&& operationValue != TableRow.OPERATION_ADD && operationValue != TableRow.OPERATION_DELETE)) {
				allowed = true;
			}

			return allowed;
		}
	}

}
