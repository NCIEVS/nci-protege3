package edu.stanford.smi.protegex.prompt.ui.diffUI;

/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.MutableSelectable;
import edu.stanford.smi.protege.util.Selectable;
import edu.stanford.smi.protege.util.SelectionEvent;
import edu.stanford.smi.protege.util.SelectionListener;
import edu.stanford.smi.protegex.prompt.ProjectsAndKnowledgeBases;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.event.PromptListenerManager;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.TableRow;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.TableRowComparator;
import edu.stanford.smi.protegex.prompt.ui.TabComponent;
import edu.stanford.smi.protegex.prompt.util.Util;
import edu.stanford.smi.protegex.server_changes.prompt.AuthorManagement;

public class DiffTabComponent extends TabComponent {
	private static final Logger log = Log.getLogger(DiffTabComponent.class);

	private static DiffTableView _tableView;
	private static DiffTreeView _treeView;
	private static PromptDiffUserView _userView;
	private Collection<Frame> selection = new HashSet<Frame>();
	private JTabbedPane mainPane;
	List<TableRow> results;

	public DiffTabComponent(Dimension size, int pluginPerspectiveType) {
		super(size, pluginPerspectiveType);
		PromptListenerManager.fireDiffUIBuilt(mainPane);
		PromptTab.getPromptDiff().fireUIBuilt(mainPane);
	}

	protected JComponent createContentPane() {
	    results = filterNonDisplayedRows(PromptTab.getPromptDiff().getResults());
		KnowledgeBase old_kb = PromptTab.getPromptDiff().getKb1();
		KnowledgeBase new_kb = PromptTab.getPromptDiff().getKb2();
		mainPane = ComponentFactory.createTabbedPane(true);
		new DiffViewSetUp(ProjectsAndKnowledgeBases.getProject(old_kb), ProjectsAndKnowledgeBases.getProject(new_kb), PromptTab.getPromptDiff().getResultsTable());

		_tableView = new DiffTableView(results);
		mainPane.addTab("Table view", _tableView);

		_treeView = new DiffTreeView();
		mainPane.addTab("Tree view", _treeView);

		AuthorManagement authorManagement = PromptTab.getPromptDiff().getAuthorManagement();
		if (authorManagement != null) {
			_userView = new PromptDiffUserView(PromptTab.getPromptDiff(), results);
			mainPane.addTab("Users", _userView);
		}
		setupSelectionListeners();
		mainPane.setSelectedComponent(_treeView);
		return mainPane;
	}
	
	public static List<TableRow> filterNonDisplayedRows(List<TableRow> rows) {
	    List<TableRow> newRows = new ArrayList<TableRow>();
	    for (TableRow row : rows) {
	        edu.stanford.smi.protege.model.Frame f1 = row.getF1Value();
	        edu.stanford.smi.protege.model.Frame f2 = row.getF2Value();

	        if (!Util.displayFrameInDiffTable (f1)) continue;
	        if (!Util.displayFrameInDiffTable (f2)) continue;

	        if (row.getMappingLevel() == TableRow.MAPPING_LEVEL_UNCHANGED) continue; // we used to remove these from the _diffTable
	        if (f1 != null && f1.isIncluded() && f2 != null && f2.isIncluded() && !PromptTab.getPromptDiff().showIncluded()) continue;
	        newRows.add(row);
	    }
	    return newRows;
	}

	private void setupSelectionListeners() {

		SelectionListener selectionListener = new SelectionListener() {

			public void selectionChanged(SelectionEvent event) {
				Component c = mainPane.getSelectedComponent();
				if (log.isLoggable(Level.FINE)) {
					log.fine("Selection may have changed.  Current tab component = " + c);
					log.fine("Selection List = ");
				}
				if (selection != null && c instanceof Selectable) {
					selection.clear();
					for (Object o : ((Selectable) c).getSelection()) {
						if (o instanceof Frame) {
							if (log.isLoggable(Level.FINE)) {
								log.fine("\t" + o);
							}
							selection.add((Frame) o);
						}
					}
				}
				if (log.isLoggable(Level.FINE)) {
					log.fine("End of selection list.");
				}
			}

		};
		_tableView.addSelectionListener(selectionListener);
		_treeView.addSelectionListener(selectionListener);
		if (_userView != null) {
			_userView.addSelectionListener(selectionListener);
		}
		mainPane.addChangeListener(new ChangeListener() {

			@SuppressWarnings("unchecked")
			public void stateChanged(ChangeEvent e) {
				Component c = mainPane.getSelectedComponent();
				if (c instanceof MutableSelectable) {
					if (log.isLoggable(Level.FINE)) {
						log.fine("May have switched tabs.  Current tab = " + c);
						if (selection == null || selection.isEmpty()) {
							log.fine("selection is empty");
						} else {
							log.fine("Selection list");
							for (Object o : selection) {
								log.fine("\t" + o);
							}
							log.fine("End of selection list");
						}
					}
					((MutableSelectable) c).setSelection(selection != null ? new ArrayList(selection) : null);
				}
			}

		});

	}

	public String toString() {

		return "DiffTabComponent";
	}

	public void reset() {
	    Collections.sort(results, new TableRowComparator());
		(_tableView).reset();
		if (_treeView != null) {
		    _treeView.reloadRHS();
		}
		if (_userView != null) {
		    _userView.reset();
		}
	}
	
	public void setTableViewTitle(String title) {
		int index = mainPane.indexOfComponent(_tableView);
		mainPane.setTitleAt(index, title);
	}

	public DiffTreeView getTreeView() {
		return _treeView;
	}

	public PromptDiffUserView getUserView() {
		return _userView;
	}

}
