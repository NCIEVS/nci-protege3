package edu.stanford.smi.protegex.prompt.ui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protegex.prompt.KnowledgeBaseInMerging;
import edu.stanford.smi.protegex.prompt.ProjectsAndKnowledgeBases;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.plugin.model.MappingStoragePlugin;

public class MappingTabComponent extends TabComponent {
	private static SourceInstancesPane[] _mappingKbPanes = null;
	private static String[] _mappingKbPanesNames = null;
	private static TargetDisplayPane _sourceKbPane = null;
	private static TargetDisplayPane _targetKbPane = null;
	private static JTabbedPane _middleMappingPane = null;

	private static JSplitPane _leftPane = null;
	private static JSplitPane _rightPane = null;

	private static Dimension _componentSize;

	private static final String _suggestionsTabTitle = "Suggestions";
	private static final String _createTabTitle = "User-defined mappings";

	public MappingTabComponent(Dimension size, int pluginPerspectiveType) {
		super(size, pluginPerspectiveType);
	}

	public static Dimension getComponentSize() {
		return _componentSize;
	}

	//public static SourceInstancesPane getMappingKbPane() {
///		return _mappingKbPane;
	//}

	public static TargetDisplayPane getSourceKbPane() {
		return _sourceKbPane;
	}

	public static TargetDisplayPane getTargetKbPane() {
		return _targetKbPane;
	}

	public static JTabbedPane getMappingPane() {
		return _middleMappingPane;
	}

	public static void addLeftScreenPanel(JComponent panel) {
		_leftPane.setBottomComponent(panel);
		_leftPane.setDividerSize(10);
	}

	public static void addRightScreenPanel(JComponent panel) {
		_rightPane.setBottomComponent(panel);
		_rightPane.setDividerSize(10);
	}

	protected JComponent createContentPane() {
//		PromptTab.conflictsPaneActive (false);
//		PromptTab.suggestionsPaneActive (false);

		kbToBuildTreesMap.clear();
		kbToSourceTreesMap.clear();

		_todoPane = getTodoPane();
		PromptTab.suggestionsPaneActive(true);

		double buildPaneWidth = _size.width * 3 / 5;
		int buildPaneWidthIntValue = (new Double(buildPaneWidth)).intValue();

		_buildPane = new BuildPane(new Dimension(buildPaneWidthIntValue, _size.height));

		_leftPane = ComponentFactory.createTopBottomSplitPane();
		_rightPane = ComponentFactory.createTopBottomSplitPane();

		_sourceKbPane = new TargetDisplayPane(new Dimension((int) (_size.width * 0.35), _size.height / 2), ProjectsAndKnowledgeBases
				.getKnowledgeBaseInMerging(ProjectsAndKnowledgeBases.MAPPING_SOURCE_INDEX), true, "Source", true);
		_targetKbPane = new TargetDisplayPane(new Dimension((int) (_size.width * 0.35), _size.height / 2), ProjectsAndKnowledgeBases
				.getKnowledgeBaseInMerging(ProjectsAndKnowledgeBases.MAPPING_TARGET_INDEX), true, "Target", true);

		MappingStoragePlugin[] mappingStoragePlugins = ProjectsAndKnowledgeBases.getMappingStoragePlugins();
		if (mappingStoragePlugins != null && mappingStoragePlugins.length != 0) {
			_mappingKbPanes = new MappingInstancesPane[mappingStoragePlugins.length];
			_mappingKbPanesNames = new String[mappingStoragePlugins.length];
			KnowledgeBaseInMerging[] mappingKbsInMerging = ProjectsAndKnowledgeBases.getMappingKnowledgeBaseInMerging();
			for (int i = 0; i < mappingStoragePlugins.length; i++) {
				if (mappingStoragePlugins[i].showMappingInstances()) {
					_mappingKbPanes[i] = new MappingInstancesPane(new Dimension((int) (_size.width * 0.3), _size.height), mappingKbsInMerging[i], mappingStoragePlugins[i], true);
					_mappingKbPanesNames[i] = mappingStoragePlugins[i].getTabName();
				} else {
					_mappingKbPanes[i] = null;
				}
			}
		}

		// add the source to the left pane and create a dummy bottom component
		_leftPane.setTopComponent(_sourceKbPane);
		_leftPane.setBottomComponent(new JPanel());
		_leftPane.setDividerSize(0);

		// add the target to the right pane and create a dummy bottom component
		_rightPane.setTopComponent(_targetKbPane);
		_rightPane.setBottomComponent(new JPanel());
		_rightPane.setDividerSize(0);

		_mainComponent = createMainPane(buildPaneWidthIntValue);
		_todoPane.addSelectionListener();

		return _mainComponent;
	}

	private JComponent createMainPane(int buildPaneWidthIntValue) {
		JSplitPane leftpane = ComponentFactory.createLeftRightSplitPane(true);
		// set preferred sizes on the window based on this tab's total size in order to make
		// each pane have a consistent width
		_sourceKbPane.setPreferredSize(new Dimension((int) (_size.getWidth() * 0.35), (int) _size.getHeight() / 2));
		//leftpane.setLeftComponent(_sourceKbPane);
		JSplitPane rightPane = ComponentFactory.createLeftRightSplitPane(true);

		_middleMappingPane = (JTabbedPane) createMiddlePane();
		_middleMappingPane.setPreferredSize(new Dimension((int) (_size.getWidth() * 0.3), (int) _size.getHeight()));
		_targetKbPane.setPreferredSize(new Dimension((int) (_size.getWidth() * 0.35), (int) _size.getHeight() / 2));
		rightPane.setLeftComponent(_middleMappingPane);
		rightPane.setRightComponent(_rightPane);
		//rightPane.setRightComponent(_targetKbPane);

		leftpane.setLeftComponent(_leftPane);
		leftpane.setRightComponent(rightPane);
		return leftpane;
	}

	private JComponent createMiddlePane() {
		JTabbedPane middlePane = ComponentFactory.createTabbedPane(true);
		middlePane.addTab(_suggestionsTabTitle, _todoPane);
		middlePane.addTab(_createTabTitle, _buildPane);
		if (_mappingKbPanes != null) {
			for (int i = 0; i < _mappingKbPanes.length; i++) {
				middlePane.addTab(_mappingKbPanesNames[i], _mappingKbPanes[i]);
			}
		}
		return middlePane;
	}

	public void selectArgumentsInTrees(Object[] args) {
		_sourceKbPane.unselectAll();
		_targetKbPane.unselectAll();
		if (args == null) {
			((Component) _sourceKbPane).getToolkit().beep();
			return;
		}
		Frame lastFrame = selectArgumentsInTree_common(args);
		if (lastFrame != null) {
			//_sourceKbPane.selectTab (lastFrame);
			//_targetKbPane.selectTab(lastFrame);
		} else {
			((Component) _sourceKbPane).getToolkit().beep();
		}
	}

	public String toString() {
		return "MappingTabComponent";
	}

}
