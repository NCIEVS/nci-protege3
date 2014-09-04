/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
 *            Abhita Chugh (abhita@stanford.edu)
  *
*/
package edu.stanford.smi.protegex.server_changes.prompt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.util.AbstractSelectableComponent;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Selectable;
import edu.stanford.smi.protege.util.SelectableList;
import edu.stanford.smi.protege.util.SimpleListModel;
import edu.stanford.smi.protege.util.ViewAction;
import edu.stanford.smi.protegex.server_changes.ChangesProject;

public class UserConceptList extends AbstractSelectableComponent implements Selectable {
    private KnowledgeBase old_kb, new_kb;
    private JTable changesTable;

	private Collection<String> _userList = new ArrayList<String>();
	private SelectableList _noConflictList, _conflictList;
	private Set<Ontology_Component> _noConflictConcepts = new HashSet<Ontology_Component> ();
	private Set<Ontology_Component> _conflictConcepts = new HashSet<Ontology_Component> ();

    private AuthorManagement authorManagement;

    private boolean showAllChangesInTable = false;

    public UserConceptList (KnowledgeBase old_kb, KnowledgeBase new_kb) {
        super ();
        this.old_kb = old_kb;
        this.new_kb = new_kb;

    }

    public void initialize() {
        buildGUI();

        /* these tweedledee-tweedledum listeners are a bit funky... */
        _noConflictList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (_noConflictList.getSelectedValue() != null) {
                    updateChangeTable((Ontology_Component) _noConflictList.getSelectedValue());
                    _conflictList.clearSelection();
                }
                notifySelectionListeners();
            }
        });

        _conflictList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (_conflictList.getSelectedValue() != null) {
                    updateChangeTable((Ontology_Component) _conflictList.getSelectedValue());
                    _noConflictList.clearSelection();
                }
                notifySelectionListeners();
            }
        });
    }

    public void updateChangeTable(Ontology_Component ontologyComponent) {
		((ChangesTableModel)changesTable.getModel()).setOntologyComponent(ontologyComponent);
	}

	private void buildGUI() {
    	setLayout(new BorderLayout());

    	JComponent changeTablePanel = createChangesTable();

		JPanel changesPanel = new JPanel(new BorderLayout());
		changesPanel.add(createConceptLists(), BorderLayout.CENTER);

		JSplitPane topBottomSplitPane = ComponentFactory.createTopBottomSplitPane(changesPanel, changeTablePanel);
		changeTablePanel.setMinimumSize(new Dimension(0, 50));
		changeTablePanel.setPreferredSize(new Dimension(100, 200));
		topBottomSplitPane.setDividerLocation(50 + topBottomSplitPane.getInsets().bottom);

		add(topBottomSplitPane, BorderLayout.CENTER);
	}

	protected JComponent createChangesTable() {
		changesTable = ComponentFactory.createSelectableTable(null);

		changesTable.setModel(new ChangesTableModel(null));

		for (int i = 0; i < changesTable.getModel().getColumnCount(); i++) {
			ComponentUtilities.addColumn(changesTable, new FrameRenderer());
		}

		LabeledComponent labeledComponent = new LabeledComponent("Changes of selected ontology component", new JScrollPane(changesTable));

		labeledComponent.addHeaderButton(new ViewAction("View change", (Selectable)changesTable) {
			@Override
			public void onView() {
				for (int i = 0; i < changesTable.getSelectedRows().length; i++) {
					Change change = ((ChangesTableModel)changesTable.getModel()).getChange(changesTable.getSelectedRows()[i]);
					view(change);
				}
			}

			private void view(Change change) {
				if (change != null) {
					ChangesProject.getChangesProj(new_kb).show(((AbstractWrappedInstance)change).getWrappedProtegeInstance());
				}
			}

		});

		labeledComponent.setHeaderComponent(getHeaderCenterComponent("Show also subchanges", showAllChangesInTable, new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				onShowAllChanges(e.getSource());
			}
		}));

		return labeledComponent;
	}

	public void setUserList (Collection<String> newUsers) {
        _userList = newUsers;
        setConceptList ();
    }

    public void setAuthorManagement(AuthorManagement authorManagement) {
        this.authorManagement = authorManagement;
    }

	private JComponent createConceptLists() {
		JPanel result = new JPanel ();
		result.setLayout (new GridLayout (0, 2, 10, 0));

		_noConflictList = createConceptList (_noConflictConcepts);
		_noConflictList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_conflictList = createConceptList (_conflictConcepts);
		_conflictList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		LabeledComponent labeledComponent1 = new LabeledComponent ("Ontology components changed by one user", ComponentFactory.createScrollPane (_noConflictList));
		LabeledComponent labeledComponent2 = new LabeledComponent ("Ontology components changed by multiple users", ComponentFactory.createScrollPane (_conflictList));

		labeledComponent1.addHeaderButton(new ViewAction("View changed ontology component", _noConflictList) {
			@Override
			public void onView(Object o) {
				Ontology_Component ontoComp = (Ontology_Component) o;
				ChangesProject.getChangesProj(new_kb).show(((AbstractWrappedInstance)ontoComp).getWrappedProtegeInstance());
			}
		});

		labeledComponent2.addHeaderButton(new ViewAction("View changed ontology component", _conflictList) {
			@Override
			public void onView(Object o) {
				Ontology_Component ontoComp = (Ontology_Component) o;
				ChangesProject.getChangesProj(new_kb).show(((AbstractWrappedInstance)ontoComp).getWrappedProtegeInstance());
			}
		});

		result.add(labeledComponent1);
		result.add(labeledComponent2);

		return result;
	}

	private void setConceptList () {
	    _noConflictConcepts.clear();
	    _conflictConcepts.clear();
	    for (String user : _userList) {
	        _noConflictConcepts.addAll(authorManagement.getFilteredUnConflictedFrames(user));
	        _conflictConcepts.addAll(authorManagement.getFilteredConflictedFrames(user));
	    }
	    ((SimpleListModel)_noConflictList.getModel()).setValues(_noConflictConcepts);
	    ((SimpleListModel)_conflictList.getModel()).setValues (_conflictConcepts);
	}

	private SelectableList createConceptList (Collection concepts) {
		SelectableList list = ComponentFactory.createSelectableList(null, false);

		list.setCellRenderer(new ChangeTabRenderer(old_kb, new_kb));
		((SimpleListModel)list.getModel()).setValues(concepts);

		return list;
	}

	private JComponent getHeaderCenterComponent(String text, boolean selected, ItemListener itemListener) {
		final JCheckBox showAllChanges = ComponentFactory.createCheckBox(text);
		showAllChanges.setSelected(selected);

		Font font = showAllChanges.getFont();
        showAllChanges.setFont(font.deriveFont(Font.BOLD, font.getSize()));
        showAllChanges.setForeground(new Color(140, 140, 140));
		showAllChanges.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(showAllChanges, BorderLayout.EAST);

		if (itemListener != null) {
			showAllChanges.addItemListener(itemListener);
		}

		return panel;
	}


	protected void onShowAllChanges(Object source) {
		JCheckBox cb = (JCheckBox) source;

		showAllChangesInTable = cb.isSelected();
		((ChangesTableModel)changesTable.getModel()).setShowAllChanges(showAllChangesInTable);
	}

	public Set<Ontology_Component> getSelection() {
	    Set<Ontology_Component> result = new HashSet<Ontology_Component>();
	    for (Object o : _conflictList.getSelection()) {
	        result.add((Ontology_Component) o);
	    }
	    for (Object o : _noConflictList.getSelection()) {
	        result.add((Ontology_Component) o);
	    }
	    return result;
	}

	public void clearSelection() {
	    _conflictList.clearSelection();
	    _noConflictList.clearSelection();
	}

    public void setSelection(Collection<Ontology_Component> unconflicted, Collection<Ontology_Component> conflicted) {
        ComponentUtilities.setSelectedValues(_noConflictList, unconflicted);
        ComponentUtilities.setSelectedValues(_conflictList, conflicted);
    }

}
