package edu.stanford.smi.protegex.changes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreePath;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultAnnotation;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.util.AnnotationCreationComparator;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.ui.HeaderComponent;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.SelectableTable;
import edu.stanford.smi.protege.util.ViewAction;
import edu.stanford.smi.protege.widget.AbstractTabWidget;
import edu.stanford.smi.protegex.changes.action.AnnotationShowAction;
import edu.stanford.smi.protegex.changes.listeners.ChangesListener;
import edu.stanford.smi.protegex.changes.ui.ChangeMenu;
import edu.stanford.smi.protegex.changes.ui.ColoredTableCellRenderer;
import edu.stanford.smi.protegex.changes.ui.CreateChAOProjectDialog;
import edu.stanford.smi.protegex.changes.ui.Filter;
import edu.stanford.smi.protegex.changes.ui.JTreeTable;
import edu.stanford.smi.protegex.server_changes.ChangesProject;

/**
 * Change Management Tab widget
 *
 */
public class ChangesTab extends AbstractTabWidget {
	// Main UI tables
	public static final String HEADERCOMP_NAME_CHANGE_VIEWER = "Change Viewer";
	public static final String HEADERCOMP_NAME_ANNOTATE_VIEWER = "Annotation Viewer";
	public static final String LABELCOMP_NAME_CHANGE_HIST = "Change History";
	public static final String LABELCOMP_NAME_ANNOTATIONS = "Annotations";
	public static final String LABELCOMP_NAME_ASSOC_CHANGES = "Associated Changes";
	public static final String LABELCOMP_NAME_DETAIL_CHANGES = "Detailed Changes";
	public static final String ACTION_NAME_CREATE_ANNOTATE = "Create Annotation";
	public static final String ACTION_NAME_REMOVE_ANNOTATE = "Remove Annotation";
	public static final String ACTION_NAME_EDIT_ANNOTATE = "Edit Annotation";
	public static final String FILTER_NAME_DETAIL_VIEW = "Detailed View";
	public static final String FILTER_NAME_SUMMARY_VIEW = "Summary View";

	// Search Panel
	private static final String SEARCH_PANEL_TITLE = "Search";
	private static final String SEARCH_PANEL_BUTTON_GO = "Go";
	private static final String SEARCH_PANEL_BUTTON_CLEAR = "Clear";

	private static final String ANNOT_PANEL_TITLE = "Create Annotation";

	private static final String CHANGES_TAB_NAME = "Changes";

	private KnowledgeBase changes_kb;
	private KnowledgeBase currentKB;

	private SelectableTable annotationsTable;
	private SelectableTable annotationChangesTable;

	private JComboBox annotationTypes;
	private ChangeTableModel annotationChangesTableModel;

	private AnnotationTableModel annotationsTableModel;

	private Instance instToEdit;
	private String OWL_KB_INDICATOR = "OWL";

	private ChangeMenu changesMenu;
	private RemoveInstanceAction removeAnnotationAction;
	private EditInstanceAction editAnnotationAction;
	private AddInstanceAction addAnnotationAction;

	private JTreeTable changesTreeTable;
	private ChangeTreeTableModel changesTreeTableModel;

	private ChangesListener changesListener;

	private LabeledComponent changesLabledComponent;


	private JComboBox columnSearchComboBox;
	private JTextField searchTextField;


	public boolean kbInOwl(KnowledgeBase kb) {
		int index = kb.getClass().getName().indexOf(OWL_KB_INDICATOR);
		return index > 0;
	}

	public KnowledgeBase getChangesKB() {
		return changes_kb;
	}

	public void initialize() {
		setLabel(CHANGES_TAB_NAME);

		currentKB = getKnowledgeBase();
		changes_kb = ChAOKbManager.getChAOKb(currentKB);

		if (changes_kb == null) {
			if (currentKB.getProject().isMultiUserClient()) {
				ModalDialog.showMessageDialog(this,
						"The Changes Tab could not find the annotation/changes knowledge base\n" +
						"associated to this project. One possible reason is that the\n" +
						"annotations/changes knowledge base was not configured on the server.\n" +
						"Please check the configuration of the project on the server side.\n" +
						"Changes Tab will not work at the current time.",
						"No annotation/changes knowledge base", ModalDialog.MODE_CLOSE);
				return;
			}

			CreateChAOProjectDialog dialog = new CreateChAOProjectDialog(currentKB);
			dialog.showDialog();
			changes_kb = dialog.getChangesKb();

			if (changes_kb == null) {
				ModalDialog.showMessageDialog(this,
						"Could not find or create the changes and annotations\n" +
						"ontology. Changes Tab will not work in this session.", "No ChAO");
				return;
			}
		}

		if (!ChangesProject.isInitialized(getProject())) {
		    ChangesProject.initialize(getProject());
		}
		changes_kb = ChAOKbManager.getChAOKb(currentKB);

		initTables();
		loadExistingData();

		changesListener = new ChangesListener(changes_kb, this);
		changes_kb.addFrameListener(changesListener);

		buildGUI();
	}

	private void buildGUI() {
		// Create menu item
		changesMenu = new ChangeMenu(getKnowledgeBase(), changes_kb);
		JMenuBar menuBar = getMainWindowMenuBar();
		menuBar.add (changesMenu);

		annotationsTable.addMouseListener(new AnnotationShowAction(annotationsTable, annotationsTableModel, changes_kb.getProject()));
		JScrollPane scroll = ComponentFactory.createScrollPane(changesTreeTable);
		JScrollPane scroll2 = ComponentFactory.createScrollPane(annotationsTable);
		JScrollPane scroll3 = ComponentFactory.createScrollPane(annotationChangesTable);

		changesLabledComponent = new LabeledComponent(LABELCOMP_NAME_CHANGE_HIST, scroll,true);
		changesLabledComponent.setFooterComponent(initSearchPanel());

		changesLabledComponent.doLayout();
		changesLabledComponent.addHeaderSeparator();
		addAnnotationAction = new AddInstanceAction(changesLabledComponent, ACTION_NAME_CREATE_ANNOTATE);
		addAnnotationAction.setEnabled(false);

		changesLabledComponent.setHeaderComponent(initAnnotPanel(), BorderLayout.EAST);
		changesLabledComponent.addHeaderButton(addAnnotationAction);

		changesLabledComponent.addHeaderButton(new ViewAction("View change details", null) {
			@Override
			public void onView() {
				TreePath[] selectedTreePaths = changesTreeTable.getTree().getSelectionPaths();

				for (TreePath treePath : selectedTreePaths) {
					Object lastPathComp = treePath.getLastPathComponent();
					try {
						if (lastPathComp instanceof ChangeTreeTableNode) {
							Change change = ((ChangeTreeTableNode)lastPathComp).getChange();
							changes_kb.getProject().show(((AbstractWrappedInstance)change).getWrappedProtegeInstance());
						}
					} catch (Exception e) {
						Log.getLogger().warning("Error at getting change table row " + treePath);
					}
				}
			}
		});


		LabeledComponent annotLC = new LabeledComponent(LABELCOMP_NAME_ANNOTATIONS, scroll2, true);
		annotLC.doLayout();
		annotLC.addHeaderSeparator();

		editAnnotationAction = new EditInstanceAction(ACTION_NAME_EDIT_ANNOTATE);
		editAnnotationAction.setEnabled(false);
		annotLC.addHeaderButton(editAnnotationAction);

		removeAnnotationAction = new RemoveInstanceAction(ACTION_NAME_REMOVE_ANNOTATE);
		removeAnnotationAction.setEnabled(false);
		annotLC.addHeaderButton(removeAnnotationAction);

		LabeledComponent assocLC = new LabeledComponent(LABELCOMP_NAME_ASSOC_CHANGES, scroll3, true);
		assocLC.doLayout();
		assocLC.addHeaderSeparator();

		assocLC.addHeaderButton(new ViewAction("View Change", annotationChangesTable) {
			@Override
			public void onView() {
				int[] selRows = annotationChangesTable.getSelectedRows();
				for (int selRow : selRows) {
					Instance instance = (Instance) annotationChangesTableModel.getObjInRow(selRow);
					changes_kb.getProject().show(instance);
				}
			}
		});

		JSplitPane splitPanel = ComponentFactory.createTopBottomSplitPane(false);
		splitPanel.setResizeWeight(0.75);
		splitPanel.setDividerLocation(0.75);
		splitPanel.setTopComponent(annotLC);
		splitPanel.setBottomComponent(assocLC);

		HeaderComponent changeView = new HeaderComponent(HEADERCOMP_NAME_CHANGE_VIEWER, null, changesLabledComponent);
		HeaderComponent annotView = new HeaderComponent(HEADERCOMP_NAME_ANNOTATE_VIEWER, null, splitPanel);

		JSplitPane splitPanelBig = ComponentFactory.createTopBottomSplitPane(false);
		splitPanelBig.setTopComponent(changeView);
		splitPanelBig.setBottomComponent(annotView);
		splitPanelBig.setResizeWeight(0.5);
		splitPanelBig.setDividerLocation(0.5);

		add(splitPanelBig);

		changesTreeTable.getTree().expandPath(changesTreeTableModel.getRootPath());
	}

	private JPanel initAnnotPanel() {
		JPanel annotPanel = ComponentFactory.createPanel();
		JLabel annotLabel = new JLabel(ANNOT_PANEL_TITLE);
		String[] annotFields = {	AnnotationTableModel.TYPE_COMMENT,
				AnnotationTableModel.TYPE_EXPLANATION,
				AnnotationTableModel.TYPE_EXAMPLE,
				AnnotationTableModel.TYPE_QUESTION,
				AnnotationTableModel.TYPE_ADVICE,
				AnnotationTableModel.TYPE_SEEALSO,
		};

		annotationTypes = new JComboBox(annotFields);
		annotationTypes.setSelectedIndex(0);


		annotPanel.add(annotLabel);
		annotPanel.add(annotationTypes);
		return annotPanel;
	}

	private JPanel initSearchPanel() {
		JPanel searchPanel = ComponentFactory.createPanel();
		JLabel searchLabel = new JLabel(SEARCH_PANEL_TITLE);
		String[] searchFields = {	ChangeTableColumn.CHANGE_COLNAME_AUTHOR.getName(),
				ChangeTableColumn.CHANGE_COLNAME_ACTION.getName(),
				ChangeTableColumn.CHANGE_COLNAME_DESCRIPTION.getName(),
				ChangeTableColumn.CHANGE_COLNAME_CREATED.getName()
		};

		columnSearchComboBox = new JComboBox(searchFields);
		columnSearchComboBox.setSelectedIndex(0);

		searchTextField = new JTextField(40);
		searchTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					onSearch();
				}
			}
		});
		//does not work
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				searchTextField.requestFocusInWindow();				
			}			
		});		
		
		JButton searchButton = new JButton(SEARCH_PANEL_BUTTON_GO);
		searchButton.setMnemonic(KeyEvent.VK_G);
		ActionListener searchExecute = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSearch();
			}	
		};
		searchButton.addActionListener(searchExecute);

		JButton clearButton = new JButton(SEARCH_PANEL_BUTTON_CLEAR);
		clearButton.setMnemonic(KeyEvent.VK_L);
		ActionListener searchClear = new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				changesTreeTableModel.setFilter(null);
				refreshTables(null);
				columnSearchComboBox.setSelectedIndex(0);
				searchTextField.setText("");
				searchTextField.setBackground(Color.WHITE);
			}	
		};
		clearButton.addActionListener(searchClear);

		searchPanel.add(searchLabel);
		searchPanel.add(columnSearchComboBox);
		searchPanel.add(searchTextField);
		searchPanel.add(searchButton);
		searchPanel.add(clearButton);

		searchPanel.setLayout(new FlowLayout());

		return searchPanel;
	}


	protected void onSearch() {
		String text = searchTextField.getText();
		if (text != null && text.length() == 0) { 
			text = null; 
			searchTextField.setBackground(Color.WHITE);
		} else {
			searchTextField.setBackground(Color.YELLOW);
			text = text.trim();
			if (!text.endsWith("*")) {
				text = text + "*";
			}
		}		
		String selectedItem = (String)columnSearchComboBox.getSelectedItem();		
		Filter filter = text == null ? null : new Filter(ChangeTableColumn.getColumnFromName(selectedItem), text);
		changesTreeTableModel.setFilter(filter);
		refreshTables(filter);
	}
	
	private void initTables() {
		// Create Tables
		annotationChangesTableModel = new ChangeTableModel(changes_kb);

		annotationsTableModel = new AnnotationTableModel(changes_kb);
		changesTreeTableModel = new ChangeTreeTableModel(changes_kb);

		annotationChangesTable = new SelectableTable();
		annotationChangesTable.setModel(annotationChangesTableModel);
		annotationsTable = new SelectableTable();
		annotationsTable.setModel(annotationsTableModel);
		changesTreeTable = new JTreeTable(changesTreeTableModel);
		changesTreeTable.addNotify();

		ComponentFactory.configureTable(annotationsTable);
		ComponentFactory.configureTable(annotationChangesTable);

		annotationsTable.setShowGrid(false);
		annotationsTable.setIntercellSpacing(new Dimension(0, 0));
		annotationsTable.setColumnSelectionAllowed(false);
		annotationsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		annotationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		annotationChangesTable.setShowGrid(false);
		annotationChangesTable.setIntercellSpacing(new Dimension(0, 0));
		annotationChangesTable.setColumnSelectionAllowed(false);
		annotationChangesTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		annotationChangesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		annotationChangesTable.setDefaultRenderer(Object.class, new ColoredTableCellRenderer());

		changesTreeTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		changesTreeTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		ListSelectionModel lsm = annotationsTable.getSelectionModel();
		lsm.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()){
					return;
				}

				ListSelectionModel lsm = (ListSelectionModel)e.getSource();
				if(!lsm.isSelectionEmpty()) {
					removeAnnotationAction.setEnabled(true);
					editAnnotationAction.setEnabled(true);
					int selectedRow = lsm.getMinSelectionIndex();
					String instName = annotationsTableModel.getInstanceName(selectedRow);
					Instance selectedInst = changes_kb.getInstance(instName);
					//fishy
					Collection<Change> changes = new ArrayList<Change>();
					for (AnnotatableThing thing : new DefaultAnnotation(selectedInst).getAnnotates()) {
						if (thing instanceof Change) {
							changes.add((Change)thing);
						}
					}
					annotationChangesTableModel.setChanges(changes);
				}
			}
		});

		ListSelectionModel tlsm = changesTreeTable.getSelectionModel();
		tlsm.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()){
					return;
				}

				ListSelectionModel lsm = (ListSelectionModel)e.getSource();
				if(!lsm.isSelectionEmpty()) {
					addAnnotationAction.setEnabled(true);

				}
			}
		});
	}


	public void refreshTables(Filter filter) {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		String searchText = searchTextField.getText();
		searchTextField.setText(searchText + " (searching...)");
		searchTextField.revalidate(); searchTextField.repaint();
		try {
			changesTreeTableModel = new ChangeTreeTableModel(changes_kb);
			changesTreeTableModel.setFilter(filter);
			changesTreeTable = new JTreeTable(changesTreeTableModel);
			changesTreeTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
			changesTreeTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);				
			changesLabledComponent.setCenterComponent(ComponentFactory.createScrollPane(changesTreeTable));

			annotationChangesTableModel = new ChangeTableModel(changes_kb);
			annotationChangesTable.setModel(annotationChangesTableModel);

			annotationsTableModel = new AnnotationTableModel(changes_kb);
			annotationsTable.setModel(annotationsTableModel);	

			loadExistingData();
		} finally {		
			setCursor(Cursor.getDefaultCursor());
			searchTextField.setText(searchText);
			searchTextField.revalidate(); searchTextField.repaint();
		}
	}

	private void loadExistingData() {
		Collection<Annotation> annotateInsts = new AnnotationFactory(changes_kb).getAllAnnotationObjects(true);
		loadChanges();
		loadAnnotations(annotateInsts);
	}

	private void loadChanges() {
		for (Change aInst : ChangeProjectUtil.getSortedTopLevelChanges(changes_kb)) {
			changesTreeTableModel.addChangeData(aInst);
		}
	}

	public void createChange(Change aChange) {
		changesTreeTableModel.addChangeData(aChange);
		changesMenu.setEnabledLastChange(true);
		changesMenu.setChange(aChange);
	}

	public void modifyChange(Change aChange, Slot slot, List oldValues) {
		changesTreeTableModel.update(aChange, slot, oldValues);
	}

	private void loadAnnotations(Collection<Annotation> annotateInsts) {
		List<Annotation> annotationList = new ArrayList<Annotation>(annotateInsts);
		Collections.sort(annotationList, new AnnotationCreationComparator());

		for (Object element : annotationList) {
			Annotation aInst = (Annotation) element;
			annotationsTableModel.addAnnotationData(aInst);
		}
	}

	public void updateAnnotationTable() {
		annotationsTableModel.update();
	}

	public void createAnnotationItemInTable(Annotation annotateInst) {
		String body = annotateInst.getBody();
		if (body == null || body.length() == 0) {
			changes_kb.deleteInstance(((AbstractWrappedInstance)annotateInst).getWrappedProtegeInstance());
		}
		else{
			//annotateInst = createUtil.updateAnnotation(annotateInst);
			annotationsTableModel.addAnnotationData(annotateInst);
		}
	}

	public String getTimeStamp() {
		Date currTime = new Date();

		String datePattern = "MM/dd/yyyy HH:mm:ss zzz";
		SimpleDateFormat format = new SimpleDateFormat(datePattern);
		String time = format.format(currTime);

		return time;
	}

	public static void main(String[] args) {
		edu.stanford.smi.protege.Application.main(args);
	}

	public class AddInstanceAction extends AbstractAction {

		Component myComp;

		public AddInstanceAction(Component c, String prompt) {
			super(prompt, Icons.getCreateClsNoteIcon());
			myComp = c;
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent arg0) {
			final Annotation annotateInst;

			if (changesTreeTable.getSelectedRowCount() > 0) {
				final Collection<Change> chngInstSelected = new ArrayList();

				TreePath[] selectedTreePaths = changesTreeTable.getTree().getSelectionPaths();

				for (TreePath treePath : selectedTreePaths) {
					Object lastPathComp = treePath.getLastPathComponent();
					try {
						if (lastPathComp instanceof ChangeTreeTableNode) {
							Change change = ((ChangeTreeTableNode)lastPathComp).getChange();
							chngInstSelected.add(change);
						}
					} catch (Exception e) {
						Log.getLogger().warning("Error at getting change table row " + treePath);
					}
				}

				String annotTypeName = (String)annotationTypes.getSelectedItem();

				annotateInst = ChangeProjectUtil.createAnnotation(changes_kb, annotTypeName, chngInstSelected);

				//this does not work in the client
				//ChangesDb changesDb = ChangesProject.getChangesDb(currentKB);
				//annotateInst = changesDb.createAnnotation(annotType);
				//changesDb.finalizeAnnotation(annotateInst, chngInstSelected, "");

				JFrame edit = changes_kb.getProject().show(((AbstractWrappedInstance)annotateInst).getWrappedProtegeInstance());

				edit.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent arg0) {
						createAnnotationItemInTable(annotateInst);
					}
				});
				edit.setVisible(true);
			}
		}
	}

	@Override
	public void dispose() {
		//TODO: This will be reimplemented once we will have a start/stop model for the ChangesProject
		// This is just a quick fix

		//remove the menu item
		JMenuBar menuBar = getMainWindowMenuBar();
		if (changesMenu != null) {
			menuBar.remove(changesMenu);
		}

		if (changes_kb != null && changes_kb.getFrameStoreManager() != null) {
			changes_kb.removeFrameListener(changesListener);
		}

		//changes project will be disposed in the ChAOKbManager

		super.dispose();
	}


	public class RemoveInstanceAction extends AbstractAction {

		public RemoveInstanceAction(String prompt) {
			super(prompt, Icons.getDeleteClsNoteIcon());
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent arg0) {
			int numSelect = annotationsTable.getSelectedRowCount();

			if (numSelect > 1) {
				annotationsTableModel.removeAnnotationData(annotationsTable.getSelectedRows());
			} else if (numSelect == 1) {
				String delName = annotationsTableModel.getInstanceName(annotationsTable.getSelectedRow());
				Instance instToDel = changes_kb.getInstance(delName);
				changes_kb.deleteInstance(instToDel);
				annotationsTableModel.removeAnnotationData(annotationsTable.getSelectedRow());
			}

			annotationsTable.clearSelection();
			removeAnnotationAction.setEnabled(false);
			editAnnotationAction.setEnabled(false);
		}
	}

	public class EditInstanceAction extends AbstractAction {
		public EditInstanceAction(String prompt) {
			super(prompt, Icons.getClsNoteIcon());
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			int numSelect = annotationsTable.getSelectedRowCount();

			if (numSelect == 1) {
				String instEditName = annotationsTableModel.getInstanceName(annotationsTable.getSelectedRow());

				instToEdit = changes_kb.getInstance(instEditName);

				JFrame edit = changes_kb.getProject().show(instToEdit);
				edit.addWindowListener(new WindowListener() {
					public void windowOpened(WindowEvent arg0) {
					}

					public void windowClosing(WindowEvent arg0) {
					}

					public void windowClosed(WindowEvent arg0) {
						annotationsTableModel.editAnnotationData(instToEdit, annotationsTable.getSelectedRow());
						removeAnnotationAction.setEnabled(false);
						editAnnotationAction.setEnabled(false);
					}

					public void windowIconified(WindowEvent arg0) {
					}

					public void windowDeiconified(WindowEvent arg0) {
					}

					public void windowActivated(WindowEvent arg0) {
					}

					public void windowDeactivated(WindowEvent arg0) {
					}
				});

				edit.setVisible(true);
			}
		}
	}


}

