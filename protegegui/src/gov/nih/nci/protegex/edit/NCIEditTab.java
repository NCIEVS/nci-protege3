/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License");  you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/u
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * The Original Code is Protege-2000.
 *
 * The Initial Developer of the Original Code is Stanford University. Portions
 * created by Stanford University are Copyright (C) 2001.  All Rights Reserved.
 *
 * Protege-2000 was developed by Stanford Medical Informatics
 * (http://www.smi.stanford.edu) at the Stanford University School of Medicine
 * with support from the National Library of Medicine, the National Science
 * Foundation, and the Defense Advanced Research Projects Agency.  Current
 * information about Protege can be obtained at http://protege.stanford.edu
 *
 * This code was developed by Northrop Grumman Information Technology (NGIT)
 * Contributor(s): Kim Ong, Iris Guo
 */

package gov.nih.nci.protegex.edit;

import static gov.nih.nci.protegex.tree.TreePanel.PanelType.TYPE_PRERETIRE;
import static gov.nih.nci.protegex.tree.TreePanel.PanelType.TYPE_RETIRE;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.plugin.PluginUtilities;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.impl.UnbackedOperationImpl;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.Selectable;
import edu.stanford.smi.protege.util.SelectionEvent;
import edu.stanford.smi.protege.util.SelectionListener;
import edu.stanford.smi.protege.widget.AbstractTabWidget;
import edu.stanford.smi.protegex.NCIEVSHistory.EVSHistory;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFSNames;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.dialogs.ModalDialogFactory;
import edu.stanford.smi.protegex.owl.ui.navigation.NavigationHistoryTabWidget;
import edu.stanford.smi.protegex.owl.ui.widget.OWLToolTipGenerator;
import edu.stanford.smi.protegex.owl.ui.widget.OWLUI;
import gov.nih.nci.protegex.dialog.CustomizedAnnotationData;
import gov.nih.nci.protegex.dialog.EditTabPreferences;
import gov.nih.nci.protegex.dialog.RefactorNamespaceDialog;
import gov.nih.nci.protegex.panel.BatchPanel;
import gov.nih.nci.protegex.panel.BatchProcessingPanel;
import gov.nih.nci.protegex.panel.ClonePanel;
import gov.nih.nci.protegex.panel.ConceptChangedListener;
import gov.nih.nci.protegex.panel.EditPanel;
import gov.nih.nci.protegex.panel.MergePanel;
import gov.nih.nci.protegex.panel.NCIDoublePanel;
import gov.nih.nci.protegex.panel.PanelDirty;
import gov.nih.nci.protegex.panel.PartonomyPanel;
import gov.nih.nci.protegex.panel.PreMergePanel;
import gov.nih.nci.protegex.panel.PreRetirePanel;
import gov.nih.nci.protegex.panel.ReportWriterPanel;
import gov.nih.nci.protegex.panel.RetiredPanel;
import gov.nih.nci.protegex.panel.SplitPanel;
import gov.nih.nci.protegex.panel.WorkflowPanel;
import gov.nih.nci.protegex.tree.TreeItem;
import gov.nih.nci.protegex.tree.TreePanel;
import gov.nih.nci.protegex.tree.TreePanel.PanelType;
import gov.nih.nci.protegex.util.ComplexPropertyParser;
import gov.nih.nci.protegex.util.Config;
import gov.nih.nci.protegex.util.EventListener;
import gov.nih.nci.protegex.util.MsgDialog;
import gov.nih.nci.protegex.util.UIUtil;
import gov.nih.nci.protegex.workflow.NCIWorkflowTab;
import gov.nih.nci.protegex.workflow.SuggestionDialog;

// import gov.nih.nci.protegex.workflow.NCIWorkflowTab.TabbedPaneChangeHandler;

/**
 * Major TODOs from Tim/Tania/Gilberto/Sherri meetings:
 * 
 * 1. misuse local versus remote creation of objects
 * 
 * 2. no consistent usage pattern for transactions
 * 
 * 3. validation logic should all occur outside nested transactions
 * 
 * 4. should be completely rewritten (Tania)
 * 
 * 
 * @author Bob Dionne
 * 
 */
public class NCIEditTab extends AbstractTabWidget implements ActionListener,
		NavigationHistoryTabWidget {
	
	public static void main(String[] args) {
		edu.stanford.smi.protege.Application.main(args);
	}

	public boolean displayHostResource(RDFResource resource) {
		if (resource instanceof OWLNamedClass) {
			_clsesPanel.setSelectedCls((Cls) resource);
			return true;

		}
		return false;
	}

	public Selectable getNestedSelectable() {
		return this._clsesPanel;
	}

	private Logger logger = Log.getLogger(getClass());

	public static final long serialVersionUID = 123456792L;

	

	private OWLWrapper wrapper;

	private DataHandler datahandler = null;

	private NCIEditFilter editfilter;

	static OWLModel owlModel = null;

	private RDFSNamedClass selectedInstance;

	private NCIClsesPanel _clsesPanel;

	private ArrayList<NCIDoublePanel> doublePanels = new ArrayList<NCIDoublePanel>();

	private TreeItem copied_item;

	public static Config config = null;

	private boolean canDelete = false;

	private static String username = null;
	
	

	// /////////////////////////////////////////////////
	// EditPanel tabbed component selected
	// /////////////////////////////////////////////////
	private int prevTabSelection = -1;

	private int currTabSelection = -1;

	private HashMap<Operation, Boolean> permissionHashMap = null;

	private Vector<Operation> action_vec = null;

	public Vector<String> customizedAnnotationKeys;

	public final static Operation EDIT_READ = new UnbackedOperationImpl(
			"EditRead", "editRead");

	public final static Operation EDIT_WRITE = new UnbackedOperationImpl(
			"EditWrite", "EditWrite");

	public final static Operation EDIT_BASIC = new UnbackedOperationImpl(
			"EditBasicRead", "EditBasicRead");
	
	public final static Operation CHANGE_NAMESPACE = new UnbackedOperationImpl(
			"ChangeNamespace", "ChangeNamespace");

	public final static Operation EDIT_PROPERTIES = new UnbackedOperationImpl(
			"EditPropertiesRead", "EditPropertiesRead");

	public final static Operation EDIT_RELATIONS = new UnbackedOperationImpl(
			"EditRelationsRead", "EditRelationsRead");

	public final static Operation GENERATE_REPORT = new UnbackedOperationImpl(
			"WriteReport", "WriteReport");

	public final static Operation MERGE = new UnbackedOperationImpl("Merge",
			"Merge");

	public final static Operation COPY = new UnbackedOperationImpl("NCICopy",
			"NCICopy");

	public final static Operation PRE_MERGE = new UnbackedOperationImpl(
			"PreMerge", "PreMerge");

	public final static Operation PRE_RETIRE = new UnbackedOperationImpl(
			"PreRetire", "PreRetire");

	public final static Operation RETIRE = new UnbackedOperationImpl("Retire",
			"Retire");

	public final static Operation SPLIT = new UnbackedOperationImpl("Split",
			"Split");

	public final static Operation DELETE = new UnbackedOperationImpl("Delete",
			"Delete");

	public final static Operation LOAD_BATCH = new UnbackedOperationImpl(
			"LoadBatch", "LoadBatch");

	public final static Operation EDIT_BATCH = new UnbackedOperationImpl(
			"EditBatch", "EditBatch");

	public final static Operation PARTONOMY_TREE = new UnbackedOperationImpl(
			"Partonomize", "Prtonomize");

	public final static Operation EDIT_RETIRED = new UnbackedOperationImpl(
			"EditRetiredClass", "EditRetiredClass");

	public enum EVSHistoryAction {
		CREATE("create"), MODIFY("modify"), RETIRE("retire"), MERGE("merge"), SPLIT(
				"split");

		private final String myName; // for debug only

		private EVSHistoryAction(String name) {
			myName = name;
		}

		public String toString() {
			return myName;
		}
	}

	public void showSuggestionDialog(String s1, Collection clses) {

		String tdesc = s1;
		

		RDFProperty codeSlot = owlModel.getRDFProperty(wrapper.codeSlotName);

		Iterator it = clses.iterator();
		ArrayList<String> codes = new ArrayList<String>();
		ArrayList<String> names = new ArrayList<String>();
		while (it.hasNext()) {
			OWLNamedClass oc = (OWLNamedClass) it.next();
			String code = "";
			if (this.wrapper.byCode()) {
				code = oc.getLocalName();
				codes.add(code);
				
			} else {
				code = (String) oc.getPropertyValue(codeSlot);
				if (code != null) {
					codes.add(code);
					
				}
				
				
			}
			names.add(oc.getBrowserText());
			if (clses.size() == 1) {

				tdesc = oc.getBrowserText() + " (" + code + ") needs updating.";

			}
			

		}

		new SuggestionDialog(tdesc, codes, names);
	}
	
	public void showRefactorNamespaceDialog(Collection clses) {
		OWLNamedClass ocls = (OWLNamedClass) clses.iterator().next();
		String defNameSpacePrefix = this.getSMWPrefix(getOWLModel().getNamespaceManager().getDefaultNamespace());
		new RefactorNamespaceDialog(this, ocls,this.getOWLModel().getNamespaceManager(), defNameSpacePrefix);
		
	}
	
	private boolean disableHistory = false;

	public void recordHistory(EVSHistoryAction action, OWLNamedClass cls,
			String ref) {
		
		if (disableHistory) {
			return;
		}

		if (getProject().isMultiUserClient()) {
		// now write EVS History
		String code = wrapper.getCode(cls);
		String name = cls.getName();
		String recop = action.toString();
		String reference = ref;

		String[] record = { code, name, recop, reference };
		(new EVSHistory(getKnowledgeBase())).recordHistory(record);
		}

	}

	// swing stuff

	private EventListener eventListener = null;

	private JPanel leftComponent;

	private JPanel textAreaPanel;

	private Collection selection;

	private JPanel tabbedPanel;

	private JTabbedPane tabbedPane;

	private JComponent mainSplitter;

	private SplitPanel splitPanel = null;

	private PreMergePanel preMergePanel = null;

	private MergePanel mergePanel = null;

	private ClonePanel clonePanel = null;

	private PreRetirePanel preRetirePanel;

	private RetiredPanel retirePanel;

	private EditPanel editPanel = null;

	private PartonomyPanel partonomyPanel;

	private WorkflowPanel workflowPanel = null;

	JMenu editTabMenu = new JMenu("Edit Tab");
	
	public void dispose() {
		super.dispose();
		this.getMainWindowMenuBar().remove(this.editTabMenu);
	}

	public void close() {
		logger.info("Prop parser called " + ComplexPropertyParser.cnt
				+ " times");
		logger.info("Time spent in " + ComplexPropertyParser.tim_pars + " ms");
		wrapper.close();
		owlModel = null;
		if (listen != null) {
			UIUtil.getTabbedPane().removeChangeListener(listen);
		}
	}

	public static String getPlainString(String s) {
		String[] languages = owlModel.getUsedLanguages();
		if (s.indexOf("~#") == -1)
			return s;

		for (int i = 0; i < languages.length; i++) {
			if (s.startsWith("~#" + languages[i] + " "))
				return s.substring(5);
		}

		return s.substring(2);
	}

	private void initializePermissionHashMap() {
		action_vec = new Vector<Operation>();
		permissionHashMap = new HashMap<Operation, Boolean>();		
		
		if (owlModel.getProject().isMultiUserClient()) {
			logger.info("Checking user privilege from metaproject...");
			// loadPropertyFile("nci.properties", false);

			

			permissionHashMap.put(EDIT_READ, new Boolean(RemoteClientFrameStore
					.isOperationAllowed(owlModel, EDIT_READ)));
			permissionHashMap.put(EDIT_WRITE, new Boolean(
					RemoteClientFrameStore.isOperationAllowed(owlModel,
							EDIT_WRITE)));
			
			permissionHashMap.put(CHANGE_NAMESPACE, new Boolean(
					RemoteClientFrameStore.isOperationAllowed(owlModel,
							CHANGE_NAMESPACE)));

			permissionHashMap.put(EDIT_BASIC, new Boolean(
					RemoteClientFrameStore.isOperationAllowed(owlModel,
							EDIT_BASIC)));
			permissionHashMap.put(EDIT_PROPERTIES, new Boolean(
					RemoteClientFrameStore.isOperationAllowed(owlModel,
							EDIT_PROPERTIES)));
			permissionHashMap.put(EDIT_RELATIONS, new Boolean(
					RemoteClientFrameStore.isOperationAllowed(owlModel,
							EDIT_RELATIONS)));
			permissionHashMap.put(GENERATE_REPORT, new Boolean(
					RemoteClientFrameStore.isOperationAllowed(owlModel,
							GENERATE_REPORT)));
			permissionHashMap.put(MERGE, new Boolean(RemoteClientFrameStore
					.isOperationAllowed(owlModel, MERGE)));
			permissionHashMap.put(COPY, new Boolean(RemoteClientFrameStore
					.isOperationAllowed(owlModel, COPY)));
			permissionHashMap.put(PRE_MERGE, new Boolean(RemoteClientFrameStore
					.isOperationAllowed(owlModel, PRE_MERGE)));
			permissionHashMap.put(PRE_RETIRE, new Boolean(
					RemoteClientFrameStore.isOperationAllowed(owlModel,
							PRE_RETIRE)));
			permissionHashMap.put(RETIRE, new Boolean(RemoteClientFrameStore
					.isOperationAllowed(owlModel, RETIRE)));
			permissionHashMap.put(SPLIT, new Boolean(RemoteClientFrameStore
					.isOperationAllowed(owlModel, SPLIT)));
			permissionHashMap.put(LOAD_BATCH, new Boolean(
					RemoteClientFrameStore.isOperationAllowed(owlModel,
							LOAD_BATCH)));
			permissionHashMap.put(EDIT_BATCH, new Boolean(
					RemoteClientFrameStore.isOperationAllowed(owlModel,
							EDIT_BATCH)));
			permissionHashMap.put(PARTONOMY_TREE, new Boolean(
					RemoteClientFrameStore.isOperationAllowed(owlModel,
							PARTONOMY_TREE)));

			permissionHashMap.put(EDIT_BASIC, new Boolean(
					RemoteClientFrameStore.isOperationAllowed(owlModel,
							EDIT_BASIC)));
			permissionHashMap.put(DELETE, new Boolean(RemoteClientFrameStore
					.isOperationAllowed(owlModel, DELETE)));
			permissionHashMap.put(EDIT_RETIRED, new Boolean(
					RemoteClientFrameStore.isOperationAllowed(owlModel,
							EDIT_RETIRED)));
		} else {
			// loadPropertyFile("nci.properties", false); // set user to local
			// user

			

			permissionHashMap.put(EDIT_READ, Boolean.TRUE);
			permissionHashMap.put(EDIT_WRITE, Boolean.TRUE);

			permissionHashMap.put(EDIT_BASIC, Boolean.TRUE);
			permissionHashMap.put(EDIT_PROPERTIES, Boolean.TRUE);
			permissionHashMap.put(EDIT_RELATIONS, Boolean.TRUE);

			permissionHashMap.put(GENERATE_REPORT, Boolean.TRUE);
			permissionHashMap.put(MERGE, Boolean.TRUE);
			permissionHashMap.put(COPY, Boolean.TRUE);
			permissionHashMap.put(PRE_MERGE, Boolean.TRUE);
			permissionHashMap.put(PRE_RETIRE, Boolean.TRUE);
			permissionHashMap.put(RETIRE, Boolean.TRUE);

			permissionHashMap.put(SPLIT, Boolean.TRUE);

			permissionHashMap.put(LOAD_BATCH, Boolean.TRUE);
			permissionHashMap.put(EDIT_BATCH, Boolean.TRUE);
			permissionHashMap.put(PARTONOMY_TREE, Boolean.TRUE);
			
			//permissionHashMap.put(CHANGE_NAMESPACE, Boolean.TRUE);

			permissionHashMap.put(DELETE, Boolean.FALSE);
			permissionHashMap.put(EDIT_RETIRED, Boolean.FALSE);
		}

		action_vec.add(EDIT_BASIC);
		action_vec.add(SPLIT);
		action_vec.add(PRE_MERGE);
		action_vec.add(MERGE);
		action_vec.add(PRE_RETIRE);
		action_vec.add(RETIRE);
		action_vec.add(GENERATE_REPORT);
		action_vec.add(LOAD_BATCH);
		action_vec.add(EDIT_BATCH);
		action_vec.add(PARTONOMY_TREE);
		action_vec.add(COPY);
		action_vec.add(DELETE);

	}

	public boolean isActionAllowed(Operation action) {
		if (permissionHashMap == null)
			return false;
		if (!permissionHashMap.containsKey(action)) {
			return false;
		}
		Boolean allowed = (Boolean) permissionHashMap.get(action);
		return allowed.booleanValue();
	}

	public OWLWrapper getWrapper() {
		return wrapper;
	}

	public NCIEditFilter getFilter() {
		return editfilter;
	}

	public EventListener getEventListener() {
		return eventListener;
	}

	public PartonomyPanel getPartonomyPanel() {
		return partonomyPanel;
	}

	private BatchPanel batchProcessingPanel = null;

	public BatchPanel getBatchProcessPanel() {
		return batchProcessingPanel;
	}

	public static String getUserName() {
		return username;
	}

	public RDFProperty getPropertySlot(String name) {
		return config.getPropertySlot(name);

	}
	
	public boolean byCode() {
		return wrapper.byCode();
	}
	
	public boolean useNCIRules() {
		return config.getUseRules().equalsIgnoreCase("nci");
	}
	
	public Vector<String> getComplexProperties() {
		return config.getComplexProperties();
	}
	
	public Vector<String> getNonTransferProps() {
		return config.getNonTransferProps();
	}
	
	public boolean isComplexProp(String s) {
		return config.getComplexProperties().contains(s);
	}

	public boolean isReadOnlyProperty(String name) {
		return config.isReadOnlyProperty(name);

	}
	
	public boolean isRequiredProperty(String name) {
		return config.getRequiredProperties().contains(name);

	}
	
	public String getSMWPrefix(String base) {
		return config.getSMWPrefix(base);
	}
	
	

	private boolean loadConfigurationFile(String filename) {
		config = new Config(filename, wrapper);

		Vector<String> requiredProperties = config.getRequiredProperties();

		Vector<String> noneditable_vec = config.getNonEditableProperties();


		String msg = "";
		for (int i = 0; i < requiredProperties.size(); i++) {
			String propertyname = (String) requiredProperties.elementAt(i);
			try {
				RDFProperty slot = wrapper.getRDFProperty(propertyname);
				String slotName = slot == null ? "null" : slot
						.getBrowserText();
				logger.fine("Checking property: " + propertyname + " ("
						+ slotName + ") ...");
				if (slot == null) {
					logger.warning("\tWARNING: Property: " + propertyname
							+ " is not found.");
					msg = msg + propertyname + "\n";
				}
			} catch (Exception ex) {
				logger.warning("WARNING: Property: " + propertyname
						+ " is not found.");
				msg = msg + propertyname + "\n";
			}
		}

		// TODO: WTF????
		for (int i = 0; i < noneditable_vec.size(); i++) {
			String cls_name = (String) noneditable_vec.elementAt(i);
			OWLNamedClass cls = owlModel.getOWLNamedClass(cls_name);
			String className = cls == null ? "null" : cls.getLocalName();
			logger.fine("Checking class: " + cls_name + " (" + className
					+ ") ...");

			if (getOWLModel().getRDFSNamedClass(cls_name) == null) {
				logger.warning("\tWARNING: Class: " + cls_name
						+ " is not found.");
				msg = msg + cls_name + "\n";
			}
		}

		if (msg.compareTo("") != 0
				&& msg.compareTo(wrapper.codeSlotName + "\n") != 0) {
			MsgDialog.warning(this, "Missing data elements: \n" + msg);
			return false;
		}
		return true;
	}

	public String getDefaultUser() {
		try {
			return System.getProperty("user.name");

		} catch (Exception e) {
			logger.warning("Couldn't obtain the default user "
					+ e.getLocalizedMessage());
			return null;
		}
	}

	private void createEditFilter() {

		editfilter = new NCIEditFilter(this, config.getAuthorities());
		canDelete = false;
	}

	protected NCIClsesPanel createClsesPanel() {
		createEditFilter();
		final NCIClsesPanel panel = new NCIClsesPanel(this, getProject(),
				canDelete);

		panel.addSelectionListener(new SelectionListener() {
			public void selectionChanged(SelectionEvent event) {
				if (event.getSelectable().getSelection().isEmpty()
						|| event.getSelectable().getSelection().size() > 1) {

				} else {
					if (transmitSelection(null)) {
						// ok
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								panel.setSelectedCls(selectedInstance);

							}
						});
					} else {					

					}

				}

			}
		});
		return panel;
	}

	protected JComponent createClsesSplitter() {
		_clsesPanel = createClsesPanel();
		return _clsesPanel;
	}

	public JScrollPane getScrollPane(PanelType type, PanelType subtype) {
		if (type == TYPE_PRERETIRE) {
			return this.preRetirePanel.getScrollPane();
		} else if (type == TYPE_RETIRE) {
			return this.retirePanel.getScrollPane();
		}

		Iterator iterator = doublePanels.iterator();
		while (iterator.hasNext()) {
			NCIDoublePanel doublePanel = (NCIDoublePanel) iterator.next();
			JScrollPane jsp = null;
			if ((jsp = doublePanel.getScrollPane(type, subtype)) != null)
				return jsp;
		}
		return null;
	}

	public SplitPanel getSplitPanel() {
		if (splitPanel != null) {

		} else {
			splitPanel = new SplitPanel(this);

		}
		return splitPanel;

	}

	public PreMergePanel getPreMergePanel() {
		if (preMergePanel != null) {

		} else {
			preMergePanel = new PreMergePanel(this, editfilter);

		}
		return preMergePanel;

	}

	public MergePanel getMergePanel() {
		if (mergePanel != null) {

		} else {
			mergePanel = new MergePanel(this);

		}
		return mergePanel;

	}

	public PreRetirePanel getPreRetirePanel() {

		if (this.preRetirePanel != null) {

		} else {
			preRetirePanel = new PreRetirePanel(this);
		}

		return preRetirePanel;

	}

	public RetiredPanel getRetirePanel() {

		if (this.retirePanel != null) {

		} else {
			retirePanel = new RetiredPanel(this);
		}

		return retirePanel;

	}

	public ClonePanel getClonePanel() {
		if (clonePanel != null) {

		} else {
			clonePanel = new ClonePanel(this);

		}
		return clonePanel;

	}

	private JPanel createTabbedPane() {
		JPanel panel = new JPanel(false);
		ImageIcon icon = new ImageIcon("images/middle.gif");
		tabbedPane = new JTabbedPane();

		editPanel = new EditPanel(this, getSelectedInstance(), getOWLModel(),
				"", true);

		tabbedPane.addTab("Edit", icon, editPanel, "");
		editPanel.disableAll();

		if (isPanelVisible("Split")) {
			if (canEnable("Split")) {
			addPanel("Split");
			} else {
				prefs.putBoolean("Split", false);
			}
		}

		if (isPanelVisible("Merge")) {
			if (canEnable("Merge")) {
			addPanel("Merge");
			} else {
				prefs.putBoolean("Merge", false);
			}
		}

		if (isPanelVisible("Retire")) {
			if (canEnable("Retire")) {
			addPanel("Retire");
			} else {
				prefs.putBoolean("Retire", false);
			}
		}

		tabbedPane.addTab("Report Writer", icon, getReportPanel(), "");

		tabbedPane.addTab("Batch Processing", icon, getBatchProcessingPanel(),
				"");

		tabbedPane.addTab("Partonomy Tree", icon, getPartonomyTreePanel(), "");

		if (isPanelVisible("Clone")) {
			addPanel("Clone");
		}

		// Add the tabbed pane to this panel.
		panel.setLayout(new GridLayout(1, 1));
		panel.add(tabbedPane);

		tabbedPane.setSelectedIndex(0);

		tabbedPane.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent evt) {

				if (selectedInstance == null) {
					tabbedPane.setSelectedIndex(prevTabSelection);
					return;
				}

				RDFSNamedClass cls = (RDFSNamedClass) selectedInstance;
				if (cls.getPrefixedName().compareTo("owl:Thing") == 0
						|| cls.getPrefixedName().compareTo("rdfs:Class") == 0
						|| cls.getPrefixedName().compareTo("rdf:Property") == 0) {
					tabbedPane.setSelectedIndex(prevTabSelection);
					return;
				}

				JTabbedPane pane = (JTabbedPane) evt.getSource();
				currTabSelection = pane.getSelectedIndex();

				if (currTabSelection < 0)
					return;

				if (currTabSelection != prevTabSelection) // tab changed
				{
					PanelDirty comp = (PanelDirty) pane
							.getComponentAt(prevTabSelection);

					if (comp.isDirty()) {

						int option = MsgDialog
								.yesOrNo(NCIEditTab.this, pane
										.getTitleAt(prevTabSelection)
										+ " Panel",
										"No data will be saved. Do you want to continue?");
						if (option == MsgDialog.NO_OPTION) {
							tabbedPane.setSelectedIndex(prevTabSelection);
							return;
						} else {
							// comp.reset();
						}
					}

					comp.reset();

					prevTabSelection = currTabSelection;
					clearListenedToClses();

					if (currTabSelection == 0 && editPanel != null)
						transmitSelection(null);
				}
			}
		});

		return panel;
	}

	public void addWorkFlowPanel() {
		ImageIcon icon = new ImageIcon("images/middle.gif");
		workflowPanel = new WorkflowPanel(this);
		tabbedPane.addTab("Workflow", icon, workflowPanel, "");
		doublePanels.add(workflowPanel);
	}

	public WorkflowPanel getWorkflowPanel() {
		for (NCIDoublePanel dp : doublePanels) {
			if (dp instanceof WorkflowPanel) {
				return (WorkflowPanel) dp;
			}

		}
		return null;
	}

	public JTabbedPane getTabbedPane() {
		return tabbedPane;
	}

	protected Component getBatchProcessingPanel() {

		if (batchProcessingPanel != null) {

		} else {
			batchProcessingPanel = new BatchProcessingPanel(this);
		}

		return (Component) batchProcessingPanel;

	}

	protected Component getPartonomyTreePanel() {

		if (this.partonomyPanel != null) {

		} else {
			partonomyPanel = new PartonomyPanel(this, getOWLModel(), wrapper);
		}

		return this.partonomyPanel;

	}

	protected Component getReportPanel() {

		return new ReportWriterPanel(this);

	}

	private JPanel createExtensionPane() {
		tabbedPanel = createTabbedPane();
		prevTabSelection = 0;
		currTabSelection = 0;

		Box box = new Box(BoxLayout.Y_AXIS);
		box.add(tabbedPanel);

		JPanel pan = new JPanel(new BorderLayout());
		pan.add(box);

		return pan;

	}

	private JComponent createMainSplitter() {

		JSplitPane pane = createLeftRightSplitPane("ClsesTab.left_right", 425);
		pane.setLeftComponent(createClsesSplitter());

		leftComponent = createExtensionPane();
		pane.setRightComponent(leftComponent);

		return pane;
	}

	public void initialize() {
		long tim = System.currentTimeMillis();
				
		OWLUI.setOWLToolTipGenerator(new OWLToolTipGenerator() {

			public String getToolTipText(RDFSClass aClass) {
				return null;
			}

			public String getToolTipText(RDFProperty prop) {
				return null;
			}

			public String getToolTipText(RDFResource res) {
				return null;
			}
			
		});
		
		owlModel = getOWLModel();
		
		if (owlModel.getProject().isMultiUserClient()) {
            username = owlModel.getProject().getLocalUser();
        } else {
            username = owlModel.getProject().getUserName();
        }

		if (owlModel == null) {
			logger.severe("WARNING: OWLModel is null.");
			System.exit(0);

		}
		
		wrapper = OWLWrapper.createInstance(getOWLModel());
		wrapper.setUserName(username);
		
		

		initializePermissionHashMap();

		File pluginsDirectory = PluginUtilities.getPluginsDirectory();
		logger.config("pluginsDirectory: "
				+ (pluginsDirectory == null ? "not configured"
						: pluginsDirectory.getAbsolutePath()));

		setIcon(Icons.getClsesIcon());
		setLabel("NCI Editor");
		setShortDescription("NCI Editor");

		// Instantiate an OWLWrapper

		loadConfigurationFile("ncitab.xml");
		
		wrapper.setConfig(config);

		wrapper.initRoots();

		mainSplitter = createMainSplitter();

		add(mainSplitter);
		setInitialSelection();

		// setClsTree
		setClsTree(_clsesPanel.getClsesTree());

		logger.config("User: " + username);

		// Instantiate EventListener
		eventListener = new EventListener(this);

		// Instantiate a DataHandler
		datahandler = new DataHandler(this);

		

		installMenu();

		ensureClassSelected((OWLNamedClass) wrapper.getSelectableRoots()
				.iterator().next());

		logger.fine("initialization took : "
				+ (System.currentTimeMillis() - tim) + " ms");
	}

	private ChangeListener listen = null;

	public void addChangeListener() {
		if (listen != null) {
		} else {
			listen = new ChangeListener() {
				public void stateChanged(ChangeEvent e) {

					if (!UIUtil.tabHasFocus(NCIEditTab.class.getSimpleName())) {
						OWLNamedClass c = (OWLNamedClass) getSelectedInstance();
						if (c != null) {
							logger.fine("Ive lost focus " + c.getBrowserText());
							clearListenedToClses();
						}

					} else {

						OWLNamedClass c = (OWLNamedClass) getSelectedInstance();
						if (c != null) {

							logger.fine("Ive gained focus "
									+ c.getBrowserText());
							transmitSelection(c);

						}

					}

				}
			};

			UIUtil.getTabbedPane().addChangeListener(listen);
		}

	}

	private void installMenu() {
		JMenuItem preferencesItem = new JMenuItem("Preferences");
		preferencesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String comm = e.getActionCommand();
				if (comm.equalsIgnoreCase("Preferences")) {
					getEditTabPreferences();
				}
			}
		});

		this.editTabMenu.add(preferencesItem);
		
		getMainWindowMenuBar().add(editTabMenu);
	}

	public void addPanel(String pname) {
		// split 1, premerge2, merge3, preretire4, retire5, copy10
		if (pname.equalsIgnoreCase("Split")) {

			ImageIcon icon = new ImageIcon("images/middle.gif");
			tabbedPane.insertTab("Split", icon, getSplitPanel(), "", 1);
			doublePanels.add(splitPanel);

		}

		if (pname.equalsIgnoreCase("Merge")) {

			int prem = findMergeIndex();

			ImageIcon icon = new ImageIcon("images/middle.gif");

			tabbedPane
					.insertTab("PreMerge", icon, getPreMergePanel(), "", prem);
			doublePanels.add(preMergePanel);

			if (isActionAllowed(MERGE)) {

				tabbedPane.insertTab("Merge", icon, getMergePanel(), "",
						prem + 1);
				doublePanels.add(mergePanel);
			}
		}

		if (pname.equalsIgnoreCase("Retire")) {

			int pret = findRetireIndex();

			ImageIcon icon = new ImageIcon("images/middle.gif");
			tabbedPane.insertTab("PreRetire", icon, getPreRetirePanel(), "",
					pret);

			if (isActionAllowed(RETIRE)) {

				tabbedPane.insertTab("Retire", icon, getRetirePanel(), "",
						pret + 1);

			}

		}

		if (pname.equalsIgnoreCase("Clone")) {
			ImageIcon icon = new ImageIcon("images/middle.gif");

			int cint = findCloneIndex();

			tabbedPane.insertTab("Copy", icon, getClonePanel(), "", cint);
			doublePanels.add(clonePanel);

		}

	}

	private int findMergeIndex() {
		int prem = 2;
		if (!isPanelVisible("Split")) {
			prem = 1;

		}
		return prem;

	}

	private int findRetireIndex() {

		int pret = 1;
		if (isPanelVisible("Split")) {
			pret += 1;
		}
		if (isPanelVisible("Merge")) {
			pret += 1;
			if (isActionAllowed(MERGE)) {
				pret += 1;
			}
		}

		return pret;

	}

	private int findCloneIndex() {
		int cint = 3;

		if (isPanelVisible("Split")) {
			cint += 1;
		}
		if (isPanelVisible("Merge")) {
			cint += 1;
			if (isActionAllowed(MERGE)) {
				cint += 1;
			}
		}

		if (isPanelVisible("Retire")) {
			cint += 1;
			if (isActionAllowed(RETIRE)) {
				cint += 1;
			}
		}

		return cint;
	}

	public void removePanel(String pname) {
		if (pname.equalsIgnoreCase("Split")) {

			tabbedPane.remove(1);
			doublePanels.remove(splitPanel);

		}

		if (pname.equalsIgnoreCase("Merge")) {

			int mind = findMergeIndex();
			tabbedPane.remove(mind);
			doublePanels.remove(preMergePanel);

			if (isActionAllowed(MERGE)) {
				tabbedPane.remove(mind);
				doublePanels.remove(mergePanel);

			}
		}

		if (pname.equalsIgnoreCase("Retire")) {

			int mind = findRetireIndex();
			tabbedPane.remove(mind);
			doublePanels.remove(preRetirePanel);

			if (isActionAllowed(RETIRE)) {
				tabbedPane.remove(mind);
				doublePanels.remove(retirePanel);

			}
		}

		if (pname.equalsIgnoreCase("Clone")) {

			int mind = findCloneIndex();
			tabbedPane.remove(mind);
			doublePanels.remove(clonePanel);

		}

	}

	private String[] configurablePanels = { "Split", "Merge", "Retire", "Clone" };

	public String[] getConfigurablePanels() {
		return configurablePanels;
	}
	
	private boolean checkPropsExist(String[] props) {
		for (int i = 0; i < props.length; i++) {
			if (wrapper.getRDFProperty(props[i]) == null) {
				return false;
				
			}
		}
		return true;
		
	}
	
	
	
	
	public boolean canEnable(String panelName) {
		if (panelName.equals("Split")) {
			String[] SPLIT_PANEL = {SPLITFROM};
			return checkPropsExist(SPLIT_PANEL);
		} else if (panelName.equals("Merge")) {
			String[] MERGE_PANEL = {SCOPENOTE, EDITORIALNOTE, MERGERETIRE, MERGESURVIVING,
					this.MERGETO};			
			return checkPropsExist(MERGE_PANEL);
		} else if (panelName.equals("Retire")) {
			String[] RETIRE_PANEL = {SCOPENOTE, EDITORIALNOTE, PREDEPRECATIONASSOC,
					PREDEPRECATIONCHILDCONCEPT, PREDEPRECATIONPARENTCONCEPT, PREDEPRECATIONROLE,
					PREDEPRECATIONSOURCEASSOC, PREDEPRECATIONSOURCEROLE, CONCEPTSTATUS};
			return checkPropsExist(RETIRE_PANEL);
		} else {
		return true;
		}
	}

	public void setPanelEnabled(String pname, boolean value) {

		if (value) {
			// check if already enabled
			if (isPanelVisible(pname)) {

			} else {
				addPanel(pname);
			}
		} else {
			if (isPanelVisible(pname)) {
				removePanel(pname);
			}
		}
		prefs.putBoolean(pname, value);

	}

	public boolean isPanelVisible(String panelName) {
		return prefs.getBoolean(panelName, true);
	}

	private Preferences prefs = Preferences
			.userNodeForPackage(NCIEditTab.class);

	public void getEditTabPreferences() {
		EditTabPreferences panel = new EditTabPreferences(this,
				NCIWorkflowTab.WIKI_URL, NCIWorkflowTab.WIKI_NS_PREFIX);
		ProtegeUI.getModalDialogFactory().showDialog(
				ProtegeUI.getTopLevelContainer(owlModel.getProject()), panel,
				"Edit Tab Preferences", ModalDialogFactory.MODE_CLOSE);
		try {
			if (this.workflowPanel != null) {
				NCIWorkflowTab.WIKI_URL = panel.getURL();
				NCIWorkflowTab.WIKI_NS_PREFIX = panel.getNSPrefix();
			}

			if (panel.getRequiresReloadUI()) {
				panel.ok();
				// ProtegeUI.reloadUI(owlModel.getProject());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			// Ignore possible exception on closed KB
		}
	}

	public DataHandler getDataHandler() {
		return datahandler;
	}

	private void setInitialSelection() {
		if (_clsesPanel != null) {
			transmitSelection(null);
		}
	}

	public OWLModel getOWLModel() {
		return (OWLModel) getProject().getKnowledgeBase();
	}

	public static OWLModel getActiveOWLModel() {
		return owlModel;
	}

	public String getCurrSelection() {
		Collection selection = _clsesPanel.getSelection();
		if (selection.size() == 1) {
			return wrapper.getInternalName((Cls) selectedInstance);
		}
		return null;
	}

	public boolean transmitSelection(Cls clas) {
		// 090606
		if (tabbedPane.getSelectedIndex() == 0
				&& getEditPanel().isSaveButtonEnabled()
				&& selectedInstance != null) {

			// To do #1:
			// Need to check if the focused concept has been changed, if it has
			// not, then do not pop-up the following message.

			if (selectedInstance == (Cls) CollectionUtilities
					.getFirstItem(_clsesPanel.getSelection())) {
				return true;
			}

			int ans = MsgDialog.yesOrNo(this,
					"Focused class has been modified. Continue?");
			if (ans == MsgDialog.NO_OPTION)
				return false;
		}

		Collection selection = null;

		// Instance selectedInstance = null;
		selectedInstance = null;

		if (clas == null) {
			selection = _clsesPanel.getSelection();
			if (selection.size() > 0) {
				selectedInstance = (RDFSNamedClass) CollectionUtilities
						.getFirstItem(selection);
			}
		} else {
			selectedInstance = (RDFSNamedClass) clas;
		}

		OWLModel owlModel = getOWLModel();
		if (selectedInstance != null) {
			if (// selectedInstance.equals(owlModel.getRootCls()) ||
			selectedInstance.equals(owlModel.getOWLNothing())
					|| selectedInstance.equals(owlModel
							.getRDFSNamedClass(RDFSNames.Cls.LITERAL))
					|| selectedInstance.equals(((KnowledgeBase) owlModel)
							.getCls(Model.Cls.DIRECTED_BINARY_RELATION))
					|| selectedInstance.equals(owlModel.getRDFListClass())
					|| selectedInstance.equals(owlModel
							.getRDFUntypedResourcesClass())) {
				selectedInstance = null;
			}

			if (tabbedPane.getSelectedIndex() != 0) {
				return false;
			}

			if (editPanel != null) // edit panel
			{
				editPanel.updateAll();
				if (tabbedPane.getSelectedIndex() == 0) {
					if (eventListener != null && selectedInstance != null) {
						clearListenedToClses();
						OWLNamedClass selectedCls = (OWLNamedClass) selectedInstance;
						if (!wrapper.isEditable(selectedCls)) {
							getEditPanel().disableAll();
							return true;
						} else {
							getEditPanel().enableAddButtons();
						}
						addToListenedToClses((OWLNamedClass) selectedInstance,
								editPanel);
					}
				}
			}
		}
		return true;
	}

	void updateTextAreaPanelCaption(String title) {
		textAreaPanel.setBorder(BorderFactory.createTitledBorder(title));
	}

	public boolean checkNoSavedContinueMsg() {
		int ans = MsgDialog.yesOrNo(this,
				"No data will be saved. Do you want to continue?");
		return ans == MsgDialog.NO_OPTION;
	}

	public void actionPerformed(ActionEvent e) {
		selection = _clsesPanel.getSelection();
		selectedInstance = (RDFSNamedClass) CollectionUtilities
				.getFirstItem(selection);
	}

	public void writeToFile(JTextArea textarea, File exportFile) {

		try {
			BufferedWriter buffWriter = new BufferedWriter(new FileWriter(
					exportFile));
			String s = textarea.getText();
			buffWriter.write(s);
			buffWriter.close();
			String text = "Output file " + exportFile + " generated.";
			ModalDialog.showMessageDialog(null, text);
		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Exception caught", e);
			String text = "Unable to save to file " + exportFile + ".";
			ModalDialog.showMessageDialog(null, text);
		}
	}

	public boolean printable(String line) {
		return wrapper.printable(line);
	}

	public void writeToFile(BufferedWriter buffWriter, Vector v) {

		try {
			int length = 0;
			String conceptDetail = "";
			if (v != null) {
				length = v.size();
			}

			for (int i = 0; i < length; i++) {
				conceptDetail = (String) v.elementAt(i);
				if (printable(conceptDetail)) {
					buffWriter.write(conceptDetail);
				}
			}
		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Exception caught", e);
		}
	}

	public void updateNoteProperties(TreePanel panel, RDFSClass retiringCls) {
		JTree edit_tree = panel.getTree();

		String prop_name = NCIEditTab.EDITORIALNOTE;
		Vector prop_values = panel.getPropertyValues(prop_name);
		if (prop_values != null) {
			for (int i = 0; i < prop_values.size(); i++) {
				String prop_value = (String) prop_values.elementAt(i);
				int pos = prop_value.indexOf("|");
				if (pos != -1) {
					panel.deleteProperty(prop_name, prop_value);
					prop_value = prop_value.substring(pos + 1);
					panel.addProperty(prop_name, prop_value, null);
					edit_tree.repaint();
				}
			}
		}

		prop_name = NCIEditTab.SCOPENOTE;
		prop_values = panel.getPropertyValues(prop_name);
		if (prop_values != null) {
			for (int i = 0; i < prop_values.size(); i++) {
				String prop_value = (String) prop_values.elementAt(i);
				int pos = prop_value.indexOf("|");
				if (pos != -1) {
					panel.deleteProperty(prop_name, prop_value);
					prop_value = prop_value.substring(pos + 1);
					panel.addProperty(prop_name, prop_value, null);
					edit_tree.repaint();
				}
			}
		}
	}

	public void attributes2Properties(TreePanel panel, RDFSClass retiringCls) {
		JTree edit_tree = panel.getTree();

		Collection col = panel.getDirectSuperClses();
		Iterator iter = col.iterator();
		while (iter.hasNext()) {
			Cls sup = (Cls) iter.next();
			if (sup instanceof OWLNamedClass) {
				if (wrapper.getInternalName(sup).compareTo(
						NCIEditTab.PREMERGED_CONCEPTS) != 0
						&& wrapper.getInternalName(sup).compareTo(
								NCIEditTab.PRERETIRED_CONCEPTS) != 0) {
					panel.addProperty(NCIEditTab.PREDEPRECATIONPARENTCONCEPT,
							wrapper.getInternalName(sup), sup);
				}
				panel.deleteParent(sup);
			}

			edit_tree.repaint();
		}

		// roles --> OLD_ROLE

		Vector roles = panel.getRestrictions();
		for (int i = 0; i < roles.size(); i++) {
			RDFSClass r = (RDFSClass) roles.elementAt(i);
			panel.deleteRestriction(r);
			panel.addProperty(NCIEditTab.PREDEPRECATIONROLE,
					r.getBrowserText(), null);
			edit_tree.repaint();
		}

		Vector assocs = panel.getAssociations();
		for (int i = 0; i < assocs.size(); i++) {
			RDFSClass assoc = (RDFSClass) assocs.elementAt(i);
			panel.deleteAssociation(assoc);
			panel.addProperty(NCIEditTab.PREDEPRECATIONASSOC, assoc
					.getBrowserText(), null);
			edit_tree.repaint();
		}

		// OLD_STATE
		String stateString = panel.getPropertyValue("hasType");

		if (stateString != null && stateString.compareTo("primitive") == 0) {
			panel.deleteProperty("hasType", stateString, null);
			panel.addProperty("hasType", "defined", null);
			panel
					.addProperty(NCIEditTab.PREDEPRECATIONSTATE, stateString,
							null);
		}

		panel.getTree().repaint();

	}

	public OWLWrapper getOWLWrapper() {
		return wrapper;
	}

	public void deleteNoteProperties(TreePanel panel) {
		String prop_name = NCIEditTab.EDITORIALNOTE;
		panel.getPropertyValue(prop_name);
		if (panel.deleteProperty(prop_name)) {
			panel.deleteProperty(prop_name);
		}

		prop_name = NCIEditTab.SCOPENOTE;
		panel.getPropertyValue(prop_name);
		if (panel.deleteProperty(prop_name)) {
			panel.deleteProperty(prop_name);
		}
	}

	public void showError(String conceptname) {
		String title = "Unable to save " + conceptname;
		String msg = editfilter.getErrorMessage();
		if (msg.length() > 0)
			MsgDialog.warning(this, title, msg);
		else
			MsgDialog.warning(this, title);
	}

	public void showError(TreePanel panel) {
		showError(panel.getDisplayName());
	}

	public TreeItem getCopyItem() {
		return copied_item;
	}

	public void setCopyItem(TreeItem copied_item) {
		this.copied_item = copied_item;
	}

	public void setSelectedInstance(OWLNamedClass instance) {
		selectedInstance = instance;
	}

	public Instance getSelectedInstance() {
		return selectedInstance;
	}

	public EditPanel getEditPanel() {
		return editPanel;
	}

	public NCIClsesPanel getClassPanel() {
		return _clsesPanel;
	}

	public TreePanel createTreePanel() {
		TreePanel panel = new TreePanel(this, null, getOWLModel());
		return panel;
	}

	// 120506
	public EditPanel createEditPanel(Cls cls) {
		EditPanel panel = new EditPanel(this, cls, getOWLModel(),
				"AdvancedQuery", true);
		return panel;
	}

	public void ensureClassSelected(Cls cls) {
		if (cls != null && !_clsesPanel.getSelection().contains(cls)) {
			_clsesPanel.clearSelection();
			_clsesPanel.setSelectedCls(cls);
		}
	}
	
	public void setClassSelectedNew(Cls cls) {
		if (cls != null) {
			_clsesPanel.clearSelection();
			_clsesPanel.setSelectedCls(cls);
		}
	}

	public void clearListenedToClses() {

		eventListener.clearListenedToClses();
	}

	// TODO: seems like wrong class is getting events during new edit scenario
	public void addToListenedToClses(OWLNamedClass cls, ConceptChangedListener l) {

		if (cls != null) {
			eventListener.addToListenedToClses(cls, l);
		}
	}

	public void removeFromListenedToClses(OWLNamedClass cls,
			ConceptChangedListener l) {

		if (cls != null) {
			eventListener.removeFromListenedToClses(cls, l);
		}
	}

	public void showConceptExistError(String name) {
		MsgDialog.ok(this, "Concept " + name + " already exists.");
	}

	public void showInvalidConceptNameError(String name) {
		MsgDialog.ok(this, "Concept " + name + " is invalid.");
	}

	public void showDataError(String errorMessage) {
		MsgDialog.ok(this, errorMessage);
	}

	//public String getClsName(String name) {
		//return config.getClsName(name);
	//}

	public String getBrowserText(Cls cls) {
		return wrapper.getInternalName(cls);
	}

	public DataHandler.Status saveConcept(TreePanel panel) {
		return saveConcept(panel, null);
	}

	public DataHandler.Status saveConcept(TreePanel panel, String clsName) {
		try {
			if (clsName == null || clsName.compareTo("") == 0) {
				Cls cls = panel.getInstance();
				if (cls == null) {
					System.out
							.println("Error saving concept data -- cls is null");
					return DataHandler.Status.FAILURE;
				}

				return datahandler.processData(cls, panel);
			} else {
				Cls cls = wrapper.getCls(clsName);
				if (cls != null)
					return datahandler.processData(cls, panel);
				return DataHandler.Status.toStatus(datahandler.createCls(
						clsName, panel.getCurrentState(), false));
			}

		} catch (Exception e) {
			// Log.getLogger().log(Level.WARNING, "Exception caught", e);
			OWLUI.handleError(owlModel, e);
		}
		return DataHandler.Status.FAILURE;
	}

	public static CustomizedAnnotationData getCustomizedAnnotationData(
			String annotation_name) {

		return config.getCustomizedAnnotationData(annotation_name);

	}
	
	public static String getSerializedCustomizedAnnotationData(String annotation_name, ArrayList<String> values) {
		CustomizedAnnotationData cad = getCustomizedAnnotationData(annotation_name);
		return cad.formatValues(values);
	}

	public Cls getSelectedCls() {
		Collection selection = _clsesPanel.getSelection();
		// Instance selectedInstance = null;
		Instance selectedInstance = null;
		if (selection.size() == 1) {
			selectedInstance = (Instance) CollectionUtilities
					.getFirstItem(selection);
			return (Cls) selectedInstance;
		}
		return null;
	}

	public void initializeEditPanel() {
		ensureClassSelected((OWLNamedClass) wrapper.getSelectableRoots()
				.iterator().next());
		// ensureClassSelected(owlModel.getOWLThingClass());
		transmitSelection(null);
	}

	// some public strings accessed from many places, so static

	public static final String SUBCLASSOF = "rdfs:subClassOf";
	
	

	public static String PRERETIRED_CONCEPTS = "";

	public static String RETIRED_CONCEPTS = "";

	public static String CODE = "";

	public static String SPLITFROM = "";

	public static String PREMERGED_CONCEPTS = "";

	public static String CURATORIALAUTHORITY = "";

	public static String CONCEPTSTATUS = "";

	public static String PREDEPRECATIONSTATE = "";

	public static String PREDEPRECATIONSOURCEROLE = "";

	public static String PREDEPRECATIONROLE = "";

	public static String PREDEPRECATIONSOURCEASSOC = "";

	public static String PREDEPRECATIONASSOC = "";

	public static String PREDEPRECATIONPARENTCONCEPT = "";

	public static String PREDEPRECATIONCHILDCONCEPT = "";

	public static String EDITORIALNOTE = "";

	public static String SCOPENOTE = "";

	public static String DEFINITION = "";
	
	
	

	

	public static void fixComplexName(String name) {
		String s = name.substring(name.indexOf(":") + 1);

		if (s.compareTo(DEFINITION) == 0) {
			DEFINITION = name;
		}


	}

	public static String MERGERETIRE = "";

	public static String MERGETO = "";

	public static String MERGESURVIVING = "";

	public static String PREFLABEL = "";

	public static String RDFLABEL = "rdfs:label";

	public static String ALTLABEL = "";
	
	
}
