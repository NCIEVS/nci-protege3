/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.SelectableContainer;
import edu.stanford.smi.protegex.prompt.Mappings;
import edu.stanford.smi.protegex.prompt.ProjectsAndKnowledgeBases;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.actionLists.HashMapForCollections;
import edu.stanford.smi.protegex.prompt.operation.Operation;
import edu.stanford.smi.protegex.prompt.plugin.PluginFacade;
import edu.stanford.smi.protegex.prompt.plugin.model.PromptUIPerspective;

public class TabComponent extends JDesktopPane {
	protected static ListSplitPane _conflictsPane = null;
	protected static ListSplitPane _todoPane = null;
	protected static BuildPane _buildPane;
	protected static JTabbedPane _workingPane = null;
	protected static TargetDisplayPane _target;
	protected static JComponent _mainComponent;

	protected static JInternalFrame _sourcesWindow = null;
	protected static ShowSourcesInterface _sourcesPane = null;

	public static final int LEFT_FRAME = 1;
	public static final int RIGHT_FRAME = 2;

//	private static MultiMap kbToBuildTreesMap = new ListMultiMap ();  // <kb, all the trees>
//	private static MultiMap kbToSourceTreesMap = new ListMultiMap ();  // <kb, all the trees>
	protected static HashMapForCollections kbToBuildTreesMap = new HashMapForCollections(); // <kb, all the trees>
	protected static HashMapForCollections kbToSourceTreesMap = new HashMapForCollections(); // <kb, all the trees>
	protected Dimension _size = null;

	public TabComponent() {

	}

	public TabComponent(Dimension size, int pluginPerspectiveType) {
		_size = size;
		if (PromptTab.getMainWindow() != null) {
			setBackground(PromptTab.getMainWindow().getBackground());
		}

		if(!PromptTab.initializeSilently) {
			//_mainComponent = createContentPane();
			setPerspective(PluginFacade.getDefaultPerspective(pluginPerspectiveType));
	
			setVisible(true);
		}
	}

	/**
	 * Sets the current perspective by swapping the mainComponent control and refreshing the view.
	 * 
	 * @param promptPlugin
	 */
	public void setPerspective(PromptUIPerspective promptPlugin) {
		if (_mainComponent != null) {
			remove(_mainComponent);
		}

		if (promptPlugin == null) {
			_mainComponent = createContentPane();
			add(_mainComponent);
		} else {
			createContentPane();
			promptPlugin.afterLoad();
			_mainComponent = promptPlugin.createContentPane(_size);
			add(_mainComponent);
		}

		invalidate();
		validate();
		repaint();
	}

	protected JComponent createContentPane() {
		PromptTab.conflictsPaneActive(false);
		PromptTab.suggestionsPaneActive(false);
		if (PromptTab.merging()) {
			_conflictsPane = new ConflictsPane();
			PromptTab.conflictsPaneActive(true);
		}
		if ((PromptTab.merging() || PromptTab.mapping() || PromptTab.extracting())) {
			_todoPane = getTodoPane();
			PromptTab.suggestionsPaneActive(true);
		}
		if (PromptTab.moving()) {
			_conflictsPane = new PartitionConflictsListPane();
			PromptTab.conflictsPaneActive(true);
		}

		double buildPaneWidth = _size.width * 3 / 5;
		int buildPaneWidthIntValue = (new Double(buildPaneWidth)).intValue();

		_buildPane = new BuildPane(new Dimension(buildPaneWidthIntValue, _size.height));
		_target = new TargetDisplayPane(new Dimension(_size.width - buildPaneWidthIntValue, _size.height), ProjectsAndKnowledgeBases.getTargetKnowledgeBaseInMerging());
		// this of course would change
		_mainComponent = createMainPane(buildPaneWidthIntValue);
		return _mainComponent;
	}

	public static ListSplitPane getTodoPane() {
		if(_todoPane == null) {
			_todoPane = new SuggestionListPane();
		}
		return _todoPane;
	}

	private JComponent createMainPane(int buildPaneWidthIntValue) {
		JSplitPane pane = ComponentFactory.createLeftRightSplitPane(true);
		pane.setLeftComponent(createMainLeftPane());
		pane.setRightComponent(createMainRightPane());
		pane.setDividerLocation(buildPaneWidthIntValue);
		if (buildPaneWidthIntValue == 0) {
			pane.setDividerLocation(0.6);
		}
		return pane;
	}

	private JComponent createMainRightPane() {
		JPanel pane = ComponentFactory.createPanel();
		pane.setLayout(new BorderLayout());
		pane.add(_target, BorderLayout.CENTER);
		return pane;
	}

	protected static final String _conflictsTabTitle = "Conflicts";
	protected static final String _suggestionsTabTitle = "Suggestions";
	protected static final String _createTabTitle = "New operations";

	public static JTabbedPane getWorkingPane() {
		return _workingPane;
	}

	public static JTabbedPane getTargetPane() {
		return _target;
	}

	private JComponent createMainLeftPane() {
		_workingPane = ComponentFactory.createTabbedPane(true);
		if (_todoPane != null) {
			_workingPane.addTab(_suggestionsTabTitle, _todoPane);
		}
		if (_conflictsPane != null) {
			_workingPane.addTab(_conflictsTabTitle, _conflictsPane);
		}
		_workingPane.addTab(_createTabTitle, _buildPane);
		if (PromptTab.merging() || PromptTab.mapping()) {
			_workingPane.setSelectedIndex(_workingPane.indexOfTab(_suggestionsTabTitle));
		} else {
			_workingPane.setSelectedIndex(_workingPane.indexOfTab(_createTabTitle));
		}
		return _workingPane;
	}

	public void showSourcesDialog(Object[] args, int position) {
		showSourcesDialog(null, args, position);
	}

	public void showSourcesDialog(SelectableContainer selectionPane, Object[] args, int position) {
		if (_sourcesWindow != null) {
			if (!_sourcesWindow.isVisible()) {
				_sourcesWindow.setVisible(true);
				setPosition(position);
			}
		} else {
			createSourcesWindow(selectionPane, position);
		}
		selectArgumentsInTrees(args);
	}

	static private void setPosition(int position) {
		TabComponent tab = PromptTab.getTabComponent();
		if (position == RIGHT_FRAME) {
			_sourcesWindow.setLocation(tab.getWidth() / 2, 50);
		}
		if (position == LEFT_FRAME) {
			_sourcesWindow.setLocation(5, 50);
		}
	}

	static private void createSourcesWindow(SelectableContainer selectionPane, int position) {
		String title = "Source knowledge bases";
		TabComponent tab = PromptTab.getTabComponent();

		_sourcesWindow = new JInternalFrame(title, true, true, false, true);
		_sourcesWindow.setFrameIcon(Icons.getLogoIcon());
		if (tab != null) {// the tab exists alredady
			_sourcesWindow.setPreferredSize(new Dimension(tab.getWidth() / 2, tab.getHeight() * 2 / 3));
			_sourcesWindow.setSize(_sourcesWindow.getPreferredSize());
		}

		if (PromptTab.moving()) {
			_sourcesPane = new TargetDisplayPane(_sourcesWindow.getSize(), ProjectsAndKnowledgeBases.getKnowledgeBaseInMerging(ProjectsAndKnowledgeBases.INCLUDING_PROJECT_INDEX), false);
		} else {
			_sourcesPane = new SourcesPane(_sourcesWindow.getSize(), false);
		}

		_sourcesWindow.getContentPane().add((Component) _sourcesPane);
		setPosition(position);
		_sourcesWindow.setVisible(true);
		_sourcesWindow.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

//		_sourcesWindow.setBorder(LineBorder.createGrayLineBorder());
		_sourcesWindow.pack();
		if (tab != null) {
			tab.add(_sourcesWindow);
		}
//		tab.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		try {
			_sourcesWindow.setSelected(true);
		} catch (java.beans.PropertyVetoException e) {
		}
		addSelectionListeners(selectionPane);
	}

	protected static void addSelectionListeners(SelectableContainer selectionPane) {
		if (_todoPane != null) {
			_todoPane.addSelectionListener();
		}
		if (_conflictsPane != null && PromptTab.moving()) {
			_conflictsPane.addSelectionListener();
		}
		if (_target != null) {
		    _target.addSelectionListener();
		}
	}

	public void reshape(int x, int y, int w, int h) {

		super.reshape(x, y, w, h);

		_mainComponent.reshape(0, 0, w, h);

	}

	public void suggestionsListChanged(boolean changed, Operation toSelect) {
		if (changed && _todoPane != null) {
			_todoPane.postChange(toSelect);
		}
	}

	public void suggestionsListChanged(boolean changed) {
		if (changed && _todoPane != null) {
			_todoPane.postChange();
		}
	}

	static public void addArgumentToOperation(Object o) {
		_buildPane.addArgumentToOperation(o);
	}

	static public void addToKbToBuildTreesMap(KnowledgeBase kb, SourceFramesPane tree) {
		kbToBuildTreesMap.put(kb, tree);
//		kbToBuildTreesMap.addValue(kb, tree);
	}

	static public void addToKbToSourceTreesMap(KnowledgeBase kb, SourceFramesPane tree) {
//		kbToSourceTreesMap.addValue(kb, tree);
		kbToSourceTreesMap.put(kb, tree);
	}

	static public Frame getSelectionFromTree(KnowledgeBase kb, Class frameType) {
		SourceFramesPane tree = getTree(kbToBuildTreesMap.getValues(kb), frameType);
		if (tree == null) {
			return null;
		}
		if (Cls.class.isAssignableFrom(frameType) && !tree.isShowing()) {
			tree = getTree(kbToBuildTreesMap.getValues(kb), Instance.class);
			SourceInstancesPane instancesTree = (SourceInstancesPane) tree;
			return instancesTree.getClsSelection();
		} else {
			return tree.getFirstSelection();
		}
	}

	static public boolean isSelected(Class frameType, KnowledgeBase kb) {
		SourceFramesPane tree = getTree(kbToBuildTreesMap.getValues(kb), frameType);
		if (tree == null) {
			return false;
		}
		return tree.isShowing();
	}

	static protected SourceFramesPane getSourceTree(Frame frame) {
		return getTree(kbToSourceTreesMap.getValues(frame.getKnowledgeBase()), frame.getClass());
	}

	static private SourceFramesPane getBuildTree(Frame frame) {
		SourceFramesPane result = getTree(kbToBuildTreesMap.getValues(frame.getKnowledgeBase()), frame.getClass());
		return result;
	}

	static private SourceFramesPane getTree(Collection trees, Class frameType) {
		if (trees == null) {
			return null;
		}
		SourceFramesPane instanceTree = null;
		Iterator i = trees.iterator();
		while (i.hasNext()) {
			SourceFramesPane next = (SourceFramesPane) i.next();
			if (Cls.class.isAssignableFrom(frameType) && next instanceof SourceClsesPane || Slot.class.isAssignableFrom(frameType) && next instanceof SourceSlotsPane) {
				return next;
			} else if (next instanceof SourceInstancesPane) {
				instanceTree = next;
			}
		}
		return instanceTree;
	}

	public void selectArgumentsInTrees(Object[] args) {
		if (_sourcesWindow == null || _sourcesWindow.isVisible() == false) {
			return;
		}

		_sourcesPane.unselectAll();

		Frame lastFrame = selectArgumentsInTree_common(args);
		if (lastFrame != null) {
			_sourcesPane.selectTab(lastFrame);
		} else {
			((Component) _sourcesPane).getToolkit().beep();
		}
	}

	protected Frame selectArgumentsInTree_common(Object args[]) {
		if (args == null) {
			((Component) _sourcesPane).getToolkit().beep();
			return null;
		}
		Frame lastFrame = null;
		for (int i = 0; i < args.length; i++) {
			Object next = args[i];
			if (next instanceof Frame) {
				Frame nextFrame = (Frame) next;
				if (nextFrame.getKnowledgeBase() == ProjectsAndKnowledgeBases.getTargetKnowledgeBase()) {
					Collection sourcesOfNextFrame = Mappings.getSources(nextFrame);
					if (sourcesOfNextFrame != null && !sourcesOfNextFrame.isEmpty()) {
						nextFrame = (Frame) CollectionUtilities.getFirstItem(sourcesOfNextFrame);
					}
				}
//				nextFrame = PromptTab.getTargetKnowledgeBase().getFrame(nextFrame.getName());
				SourceFramesPane nextTree = getSourceTree(nextFrame);
				if (nextTree != null) {
					nextTree.addSelection(nextFrame);
					lastFrame = nextFrame;
				}
			}
		}
		return lastFrame;
	}

	public static void updateUI(Operation operation) {
		Collection allTrees = new ArrayList(kbToSourceTreesMap.values());
		allTrees.addAll(kbToBuildTreesMap.values());
		Iterator i = allTrees.iterator();
		if (_target != null) {
			_target.postKnowledgeBaseChanged();
		}
		while (i.hasNext()) {
			Object next = i.next();
			if (next instanceof Collection) {
				Iterator j = ((Collection) next).iterator();
				while (j.hasNext()) {
					updateDisplay((SourceFramesPane) j.next());
				}
			} else {
				updateDisplay((SourceFramesPane) next);
			}
		}
		//  	ActionArgs args = operation.getArgs();
//		for (int i = 0; i < args.size(); i++)
//		if (args.getArg (i) instanceof Frame)
//		updateDisplay ((Frame) args.getArg (i));
	}

	private static void updateDisplay(Frame frame) {
		SourceFramesPane sourceTree = getSourceTree(frame);
		SourceFramesPane buildTree = getBuildTree(frame);
		_target.postKnowledgeBaseChanged();
		updateDisplay(sourceTree);
		updateDisplay(buildTree);
	}

	private static void updateDisplay(SourceFramesPane tree) {
		if (tree == null) {
			return;
		}
//		update the nodes.................
		tree.updateDisplay();
	}

	public static void postTargetTabChanged(Class frameType) {
		_buildPane.postTargetTabChanged(frameType);
	}

	public String toString() {
		return "TabComponent";
	}

}
