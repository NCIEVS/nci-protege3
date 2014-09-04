/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 *                 Abhita Chugh abhita@stanford.edu
 *                 Kyle Bruck kbruck@stanford.edu
 *                 Sean Falconer seanf@uvic.ca
 */

package edu.stanford.smi.protegex.prompt;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.plugin.PluginUtilities;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.ui.SlotPairRenderer;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.widget.AbstractTabWidget;
import edu.stanford.smi.protegex.prompt.actionLists.InitialToDoList;
import edu.stanford.smi.protegex.prompt.actionLists.SuggestionsAndConflicts;
import edu.stanford.smi.protegex.prompt.event.DiffListener;
import edu.stanford.smi.protegex.prompt.event.PromptAdapter;
import edu.stanford.smi.protegex.prompt.event.PromptEvent;
import edu.stanford.smi.protegex.prompt.event.PromptListener;
import edu.stanford.smi.protegex.prompt.event.PromptListenerManager;
import edu.stanford.smi.protegex.prompt.operation.Operation;
import edu.stanford.smi.protegex.prompt.plugin.AlgorithmProgressMonitor;
import edu.stanford.smi.protegex.prompt.plugin.PluginFacade;
import edu.stanford.smi.protegex.prompt.plugin.PluginManager;
import edu.stanford.smi.protegex.prompt.plugin.model.MappingStoragePlugin;
import edu.stanford.smi.protegex.prompt.promptDiff.PromptDiff;
import edu.stanford.smi.protegex.prompt.ui.AlgorithmProgressDialog;
import edu.stanford.smi.protegex.prompt.ui.ExtractTabComponent;
import edu.stanford.smi.protegex.prompt.ui.FrameInMergingRenderer;
import edu.stanford.smi.protegex.prompt.ui.InitializationScreen;
import edu.stanford.smi.protegex.prompt.ui.MappingTabComponent;
import edu.stanford.smi.protegex.prompt.ui.MergingTabComponent;
import edu.stanford.smi.protegex.prompt.ui.PromptMenu;
import edu.stanford.smi.protegex.prompt.ui.SlotPairInMergingRenderer;
import edu.stanford.smi.protegex.prompt.ui.TabComponent;
import edu.stanford.smi.protegex.prompt.ui.action.PerspectivesMenuAction;
import edu.stanford.smi.protegex.prompt.ui.diffUI.DiffTabComponent;
import edu.stanford.smi.protegex.prompt.ui.diffUI.DiffTableView;
import edu.stanford.smi.protegex.prompt.ui.event.PromptClsListener;
import edu.stanford.smi.protegex.prompt.ui.event.PromptFrameListener;
import edu.stanford.smi.protegex.prompt.ui.event.PromptKnowledgeBaseListener;
import edu.stanford.smi.protegex.prompt.util.DummyFrame;
import edu.stanford.smi.protegex.prompt.util.Queue;
import edu.stanford.smi.protegex.prompt.util.Util;

public class PromptTab extends AbstractTabWidget {
	private static final String PROMPT_PLUGIN_DIRECTORY = "edu.stanford.smi.protegex.prompt";

	static TabComponent _PromptTab = null;
	static PromptTab _this;
	static private int _operationsInProgress = 0;
	static private Queue _queue = new Queue();
	static private boolean _keepInQueue = false;

	// static private int _queueLevels = 0;

	static private boolean _analyze = true;

	static private int _operationsCount = 1; // the number of operations executed so far;

	private static boolean _mergingHasBeenSetUp = false;
	static private PromptClsListener _clsListener = new PromptClsListener();
	static private PromptFrameListener _frameListener = new PromptFrameListener();
	static private PromptMenu _menu = new PromptMenu();
	
	public static boolean initializeSilently = false;

	public static String _projectDIR = null;

	private static boolean _kbInOWL = false;

	static private boolean _makeViewsExplicit = true;

	static private int _mode = PromptModes.DIFF_MODE;

	static public int getMode() {
		return _mode;
	}

	static private boolean _conflictsPaneActive = false;
	static private boolean _suggestionsPaneActive = false;

	/** the progress dialog box, shown when Prompt is processing a task */
	private static AlgorithmProgressDialog _progressDialog = null;

	/** the todo list alg/thread */
	private static InitialToDoList _todoList;

	/** the prompt diff alg/thread */
	private static PromptDiff _promptDiff;

	/** the listeners to attach to prompt diff when it is created */
	private static Collection<DiffListener> promptDiffListeners = new ArrayList<DiffListener>();

	private static InitializationScreen initializationScreen;

	public PromptTab() {
		// initialize the plugin manager so that Prompt plugin's get loaded
		PluginManager pm = PluginManager.getInstance();
		// read the plugin configuration file
		PluginFacade.readPluginConfig();
		
		_this = this;
	}

	public void initialize() {
		Log.getLogger().info("Prompt version 3.0");
		Log.getLogger().info("Prompt version date: August 8, 2006");
		setLabel("Prompt");

		setLayout(new BorderLayout());

		setFrameRenderers();

		Properties properties = System.getProperties();
		if (properties.getProperty("prompt.project1") != null) {
			initialize(properties.getProperty("prompt.project1"), properties.getProperty("prompt.project2"), getMode(properties.getProperty("prompt.mode")));
		}
	}

	@Override
	public boolean canClose() {
		PromptListenerManager.fireBeforeClose();
		return super.canClose();
	}
	
	@Override
	public void addNotify() {
		super.addNotify();
		initializationScreen = new InitializationScreen(); 
		add(initializationScreen, BorderLayout.CENTER);

		JMenuBar menuBar = getMainWindowMenuBar();
		menuBar.add(_menu);
		if (_PromptTab != null) {
			_PromptTab.setBackground(getMainWindow().getBackground());
		}
	}

	public void initialize(String project1, String project2, int mode) {
		_mode = mode;
		Project currentProject = ProjectManager.getProjectManager().getCurrentProject();
		setUpMerging(new String[] { project1, project2, currentProject.getProjectName() }, new String[] { getAlias(project1), getAlias(project2), getAlias(currentProject.getProjectName()) }, -1, true);
	}

	private static int getMode(String property) {
		if (property.equals("MERGING")) {
			return PromptModes.MERGING_MODE;
		}

		if (property.equals("MAPPING")) {
			return PromptModes.MAPPING_MODE;
		}

		if (property.equals("DIFF")) {
			return PromptModes.DIFF_MODE;
		} else {
			Log.getLogger().warning("Unknown mode: " + property);
			return PromptModes.MERGING_MODE;
		}
	}

	private String getAlias(String fileName) {
		int begin = fileName.lastIndexOf(File.separatorChar);
		if (begin == -1) {
			begin = 0;
		}
		String alias = fileName.substring(begin + 1);
		return alias.substring(0, alias.indexOf('.'));
	}

	private void setFrameRenderers() {
		try {
			FrameRenderer.setPrototypeInstance(new FrameInMergingRenderer());
			SlotPairRenderer.setPrototypeInstance(new SlotPairInMergingRenderer());
		} catch (NoSuchMethodError e) {
			Log.getLogger().info("setPrototype not implemented.");
		}
	}

	private void setDefaultFrameRenderers() {
		FrameRenderer.setPrototypeInstance(new FrameRenderer());
		SlotPairRenderer.setPrototypeInstance(new SlotPairRenderer());
	}

	public static void setCompareSources(boolean compareSources) {
		_analyze = compareSources;
	}

	/**
	 * Displays a new progress dialog box.
	 * 
	 * @param progressMonitor The progress monitor.
	 */
	public static void displayProgressMonitor(AlgorithmProgressMonitor progressMonitor) {
		JFrame mainFrame = PromptTab.getMainWindow();
		if(mainFrame != null) {
			_progressDialog = new AlgorithmProgressDialog(mainFrame, progressMonitor);
			_progressDialog.setResizable(false);
			_progressDialog.setLocationRelativeTo(mainFrame);
			_progressDialog.setVisible(true);
		}
	}

	public static boolean setUpMerging(String[] projectFiles, String[] projectPrettyNames, int preferredOntologyIndex, boolean initializeSilently) {
		PromptTab.initializeSilently = initializeSilently;
		
		if (!ProjectsAndKnowledgeBases.setUpNewProjects(projectFiles, projectPrettyNames)) {
			return false;
		}

		setPreferredOntology(preferredOntologyIndex);
		setUpList();

		return true;
	}

	private void setUpUI() {
		if(!PromptTab.initializeSilently) {
			removeAll();
		}
		
			if (diff()) {
				_PromptTab = new DiffTabComponent(getSize(), PluginManager.PLUGIN_UI_DIFF_PERSPECTIVE);
			} else if (extracting()) {
				_PromptTab = new ExtractTabComponent(getSize(), PluginManager.PLUGIN_UI_EXTRACT_PERSPECTIVE);
			} else if (mapping()) {
				_PromptTab = new MappingTabComponent(getSize(), PluginManager.PLUGIN_UI_MAP_PERSPECTIVE);
			} else {
				_PromptTab = new MergingTabComponent(getSize(), PluginManager.PLUGIN_UI_MERGE_PERSPECTIVE);
			}
	
		if(!PromptTab.initializeSilently) {
			PerspectivesMenuAction menuAction = getPerspectivesMenuAction();
			if (diff()) {
				PluginManager.getInstance().fireAfterLoad(PluginManager.PLUGIN_UI_DIFF);
				menuAction.loadPerspectives(PluginManager.PLUGIN_UI_DIFF_PERSPECTIVE);
			} else if (merging()) {
				PluginManager.getInstance().fireAfterLoad(PluginManager.PLUGIN_UI_MERGE);
				menuAction.loadPerspectives(PluginManager.PLUGIN_UI_MERGE_PERSPECTIVE);
			} else if (mapping()) {
				PluginManager.getInstance().fireAfterLoad(PluginManager.PLUGIN_UI_MAP);
				menuAction.loadPerspectives(PluginManager.PLUGIN_UI_MAP_PERSPECTIVE);
			} else {
				PluginManager.getInstance().fireAfterLoad(PluginManager.PLUGIN_UI_EXTRACT);
				menuAction.loadPerspectives(PluginManager.PLUGIN_UI_EXTRACT_PERSPECTIVE);
			}
		}

		PromptListenerManager.fireUIBuilt(_PromptTab);
		
		if(!PromptTab.initializeSilently) {
			addTabComponent();
		}
	}

	private PerspectivesMenuAction getPerspectivesMenuAction() {
		for (int i = 1; i < _menu.getItemCount(); i++) {
			if (_menu.getItem(i) instanceof PerspectivesMenuAction) {
				return (PerspectivesMenuAction) _menu.getItem(i);
			}
		}
		return null;
	}

	private static void setPreferredOntology(int preferredOntologyIndex) {
		if (preferredOntologyIndex != -1) {
			Preferences.setPreferredOntology(ProjectsAndKnowledgeBases.getKnowledgeBase(preferredOntologyIndex));
		} else
		//	if (ProjectsAndKnowledgeBases.mappingProjectDefined ()) 
		{
			Preferences.setPreferredOntology(ProjectsAndKnowledgeBases.getKnowledgeBase(ProjectsAndKnowledgeBases.MAPPING_TARGET_INDEX));
		}
	}

	private void addTabComponent() {
		setLayout(new BorderLayout());
		add(_PromptTab, BorderLayout.CENTER);
		revalidate();
		repaint();
		revalidate();
		repaint();
		addListeners();
	}

	static public boolean kbInOWL() {
		return _kbInOWL;
	}

	static public void setKbInOWL(boolean value) {
		_kbInOWL = value;
	}

//	static public void setMappingKb (MappingKnowledgeBase mappingKb) {
//	_mappingKb = mappingKb;	
//	}	
//	
	static private void addListeners() {
		if (diff()) {
			return;
		}
		ProjectManager.getProjectManager().getCurrentProject().getKnowledgeBase().addKnowledgeBaseListener(new PromptKnowledgeBaseListener());
		// this is a hack: if the project is large, we cannot add cls listeners to all frames.
		// therefore, simply don't do it for now
		// assume that if the project is small enough to analyze, it is small enough to add listeners
		if (!_analyze) {
			return;
		}

		Collection allFrames = Util.getFrames(ProjectManager.getProjectManager().getCurrentProject().getKnowledgeBase());
		Iterator i = allFrames.iterator();

		while (i.hasNext()) {
			Frame next = (Frame) i.next();
			if (!Util.isSystem(next)) {
				if (next instanceof Cls) {
					((Cls) next).addClsListener(_clsListener);
				} else {
					next.addFrameListener(_frameListener);
				}
			}
		}
	}

	/**
	 * This method sets up a task listener so that the UI thread can be notified when the algorithm
	 * thread is completed.
	 */
	private static void prepareTaskListener() {
		PromptListenerManager.addListener(new PromptAdapter() {
			public void UIBuilt(TabComponent promptTab, PromptEvent event) {}

			public void taskComplete(PromptEvent event, boolean interrupted) {
				// if the task has been interrupted, kill the running thread
				if (interrupted) {
					try {
						if (merging() || mapping() || extracting()) {
							_todoList.stop();
						} else if (diff()) {
							_promptDiff.stop();
						}
						_progressDialog.disposeProgressMonitor();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					if (_progressDialog != null) {
						// close the progress dialog
						_progressDialog.disposeProgressMonitor();
					}

					KnowledgeBaseInMerging kbMerged = ProjectsAndKnowledgeBases.getTargetKnowledgeBaseInMerging();
					Mappings.initialWhatBecameOfItBindings(kbMerged, ProjectsAndKnowledgeBases.getMappingSource1KnowledgeBaseInMerging());
					Mappings.initialWhatBecameOfItBindings(kbMerged, ProjectsAndKnowledgeBases.getMappingSource2KnowledgeBaseInMerging());

					_mergingHasBeenSetUp = true;
					
					_this.setUpUI();

					if (merging() || mapping() || extracting()) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								DummyFrame.createDummyFrameCls(ProjectsAndKnowledgeBases.getTargetKnowledgeBase());
								if ((merging() || mapping()) && ProjectsAndKnowledgeBases.mappingProjectDefined()) {
									MappingStoragePlugin[] mappingStoragePlugins = ProjectsAndKnowledgeBases.getMappingStoragePlugins();
									if (mappingStoragePlugins != null) {
										for (int i = 0; i < mappingStoragePlugins.length; i++) {
											if (mappingStoragePlugins[i].equals(PluginManager.getInstance().getStoragePluginWithSavedMappings())) {
												mappingStoragePlugins[i].performSavedMerges();
											}
										}
									}
								}
								if (ProjectsAndKnowledgeBases.viewDefinitionsProjectDefined()) {
									ProjectsAndKnowledgeBases.getTraversalDirectivesKb().executeSavedTraversalDirectives();
								}
								if (merging()) {
									if (getTabComponent() != null) {
										((MergingTabComponent) getTabComponent()).revalidateTarget();
									}
								}
							}
						});
					}

					PromptListenerManager.fireInitializationDone();
					Statistics.printStatistics();
					Log.getLogger().info("Done!" + new Date());
					initializationScreen.setStartButtonEnabled(true);
				}
			}
		});
	}

	private static void setUpList() {
		prepareTaskListener();	
		try {
			if (diff()) {			
				_promptDiff = new PromptDiff();
				for (DiffListener listener : promptDiffListeners) {
					_promptDiff.addDiffListener(listener);
				}
				_promptDiff.runDiff(true);
				_promptDiff.start();
			} else {
				if (_analyze) {
					_todoList = new InitialToDoList(ProjectsAndKnowledgeBases.getMappingSource1(), ProjectsAndKnowledgeBases.getMappingSource2());
					// start the algorithm thread
					_todoList.start();
				} else {
					PromptListenerManager.fireTaskComplete(false);
				}
			}	
		} catch (Throwable t) {
			Log.getLogger().log(Level.SEVERE, "Errors at running PromptDiff", t);
		} 		
	}

	public static void addDiffListener(DiffListener listener) {
		promptDiffListeners.add(listener);
	}

	public static void algorithmComplete() {
		KnowledgeBaseInMerging kbMerged = ProjectsAndKnowledgeBases.getTargetKnowledgeBaseInMerging();
		Mappings.initialWhatBecameOfItBindings(kbMerged, ProjectsAndKnowledgeBases.getMappingSource1KnowledgeBaseInMerging());
		Mappings.initialWhatBecameOfItBindings(kbMerged, ProjectsAndKnowledgeBases.getMappingSource2KnowledgeBaseInMerging());

		_mergingHasBeenSetUp = true;
		if (merging() || mapping() || extracting()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					DummyFrame.createDummyFrameCls(ProjectsAndKnowledgeBases.getTargetKnowledgeBase());
					if (merging() || mapping()) {
						MappingStoragePlugin[] mappingStoragePlugins = ProjectsAndKnowledgeBases.getMappingStoragePlugins();
						if (mappingStoragePlugins != null) {
							for (int i = 0; i < mappingStoragePlugins.length; i++) {
								mappingStoragePlugins[i].performSavedMerges();
							}
						}
					}
					if (ProjectsAndKnowledgeBases.viewDefinitionsProjectDefined()) {
						ProjectsAndKnowledgeBases.getTraversalDirectivesKb().executeSavedTraversalDirectives();
					}
					if (merging()) {
						((MergingTabComponent) getTabComponent()).revalidateTarget();
					}
				}
			});
		}

		_this.setUpUI();

		PromptListenerManager.fireInitializationDone();
		Statistics.printStatistics();
		Log.getLogger().info("Done!" + new Date());
	}

	public static javax.swing.JFrame getMainWindow() {
		if(_this != null) {
			return (javax.swing.JFrame) ComponentUtilities.getFrame(_this);
		}
		return null;
	}

	public static TabComponent getTabComponent() {
		return _PromptTab;
	}

	public static boolean processingOperation() {
		return (_operationsInProgress == 0) ? false : true;
	}

	public static void addToQueue(Operation a) {
		_queue.put(a);
	}

	public static void addToQueue(Collection c) {
		_queue.putAll(c);
	}

	public static boolean queueEmpty() {
		return _queue.size() == 0;
	}

	private static void removeFromQueue(Operation a) {
		_queue.removeAllCopies(a);
	}

	public static boolean keepInQueue() {
		return _keepInQueue;
	}

	public static void setKeepInQueue(boolean value) {
		if (!_keepInQueue) {
			_keepInQueue = value;
		}
	}

	public static int getOperationsCount() {
		return _operationsCount;
	}

	public static void startOperation() {
		_operationsInProgress++;
	}

	public static void startOperation(Operation operation) {
		_operationsInProgress++;
		_operationsCount++;
		PromptListenerManager.fireOperationStarted(operation);
//		_queue.add(operation);
	}

	public static void completeOperation(Operation operation) {
		_operationsInProgress--;
		removeFromQueue(operation);
//		TabComponent.updateUI (operation);
		if (_queue.size() != 0 && _operationsInProgress == 0) {
			Operation next = (Operation) _queue.get();
			_queue.removeAllCopies(next);
			next.performOperation();
		} else if (operation.copyEverythingRelated()) {
			_keepInQueue = false;
		}

		if (_queue.size() == 0 && _operationsInProgress == 0) {
			TabComponent.updateUI(operation);
			SuggestionsAndConflicts.executePendingMoves();
		}
		PromptListenerManager.fireOperationCompleted(operation);
	}

	public static void completeOperation() {
		_operationsInProgress--;
	}

	@Override
	public void dispose() {
		Statistics.printStatistics();
		_mergingHasBeenSetUp = false;
		JMenuBar menuBar = getMainWindowMenuBar();
		menuBar.remove(_menu);

		PluginFacade.savePluginConfig();

		setDefaultFrameRenderers();
		
		PromptDiff promptDiff = getPromptDiff();
		if (promptDiff != null) {
		    promptDiff.dispose();
		}
		initializationScreen = null;
		ProjectsAndKnowledgeBases.dispose();
	}

	public static boolean mergingHasBeenSetUp() {
		return _mergingHasBeenSetUp;
	}

	public static void main(String[] args) {
		try {
			if (args.length == 3) {
				TVDiff.viewDirty(args[0], args[1], args[2]);
				TVDiff.viewChanged(args[0], args[1], args[2]);
			}
			edu.stanford.smi.protege.Application.main(args);
		} catch (Exception e) {
		}
	}

	public static boolean merging() {
		return _mode == PromptModes.MERGING_MODE;
	}

	public static boolean mapping() {
		return _mode == PromptModes.MAPPING_MODE;
	}

	public static boolean extracting() {
		return _mode == PromptModes.EXTRACTING_MODE;
	}

	public static boolean moving() {
		return _mode == PromptModes.MOVING_MODE;
	}

	public static boolean diff() {
		return _mode == PromptModes.DIFF_MODE;
	}

	public static void setMerging() {
		_mode = PromptModes.MERGING_MODE;
		_menu.setRemoveSuffixesEnabled(true);
		_menu.setConfigureEnabled(true);
	}

	public static void setMoving() {
		_mode = PromptModes.MOVING_MODE;
	}

	public static void setMapping() {
		_mode = PromptModes.MAPPING_MODE;
		_menu.setRunMappingInterpreterEnabled(true);
	}

	public static void setExtracting() {
		_mode = PromptModes.EXTRACTING_MODE;
		_menu.setRemoveSuffixesEnabled(true);
	}

	public static void setDiff() {
		_mode = PromptModes.DIFF_MODE;
		_menu.setDiffIconDialogEnabled(true);
	}

	public static PromptClsListener getClsListener() {
		return _clsListener;
	}

	public static PromptFrameListener getFrameListener() {
		return _frameListener;
	}

	public static void suggestionsPaneActive(boolean value) {
		_suggestionsPaneActive = value;
	}

	public static void conflictsPaneActive(boolean value) {
		_conflictsPaneActive = value;
	}

	public static boolean suggestionsPaneActive() {
		return _suggestionsPaneActive;
	}

	public static boolean conflictsPaneActive() {
		return _conflictsPaneActive;
	}

	public static TraversalDirectivesKnowledgeBase getTraversalDirectivesKb() {
		return ProjectsAndKnowledgeBases._traversalDirectivesKb;
	}

	public static void setTraversalDirectivesKb(TraversalDirectivesKnowledgeBase tdKb) {
		ProjectsAndKnowledgeBases._traversalDirectivesKb = tdKb;
	}

	public static boolean makeViewsExplicit() {
		return _makeViewsExplicit;
	}

	// more needs to be done here.
	public static void clearAll() {
		ProjectsAndKnowledgeBases.clearAll();

	}

	public static Project getTargetProject() {
		return _this.getProject();
	}

	public static String getPromptDirectory() {
		return (PluginUtilities.getPluginsDirectory().toString() + File.separatorChar + PROMPT_PLUGIN_DIRECTORY);
	}

	public static PromptDiff getPromptDiff() {
		return _promptDiff;
	}
}
