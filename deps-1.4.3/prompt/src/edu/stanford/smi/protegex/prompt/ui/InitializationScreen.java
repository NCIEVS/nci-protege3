
/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
   *                 Kyle Bruck kbruck@stanford.edu
   *                 Sean Falconer seanf@uvic.ca
*/

package edu.stanford.smi.protegex.prompt.ui;
  
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protegex.prompt.Preferences;
import edu.stanford.smi.protegex.prompt.ProjectsAndKnowledgeBases;
import edu.stanford.smi.protegex.prompt.PromptModes;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.plugin.PluginManager;
import edu.stanford.smi.protegex.prompt.plugin.model.ComparisonAlgorithmPlugin;
import edu.stanford.smi.protegex.prompt.promptDiff.PromptDiff;

public class InitializationScreen extends JPanel {
	static boolean _filesOpened = false;

	static File _tempLog = null;

	private JSplitPane _mainSplitter = new JSplitPane(
			JSplitPane.VERTICAL_SPLIT);

	private GetFilesPanel[] _filePanels = new GetFilesPanel[PromptModes
			.getNumberOfModes()];

	private GetFilesPanel _mergeFilesPanel;

	private GetFilesPanel _extractFilesPanel;

	private GetFilesPanel _moveFilesPanel;

	private GetFilesPanel _diffFilesPanel;

	private GetFilesPanel _mapFilesPanel;

	private int _selectedMode;

	private GetFilesPanel _selectedPanel = null;

	private JButton _initButton;

	public InitializationScreen() {
		_initButton = new JButton("Click here to begin");
		_initButton.setEnabled(false);
		_initButton.setIcon(Icons.getOkIcon());

		setLayout(new BorderLayout());

		add(new JLabel("MANAGING MULTIPLE ONTOLOGIES"), BorderLayout.NORTH);
		add(createContentPane(), BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(_initButton);
		add(buttonPanel, BorderLayout.SOUTH);

		_initButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (Preferences.pluginMatch()) {
					ComparisonAlgorithmPlugin activePlugin = PluginManager.getInstance().getActiveComparisonAlgorithm();
					if (!activePlugin.validateConfigSettings())
						return;
				}

				PluginManager.getInstance().setActiveMappingStoragePlugins(null);
				_initButton.setEnabled(false);
				try {
					initializeTask();	
				} catch (Throwable t) {
					Log.getLogger().log(Level.SEVERE, "Error at performing Prompt operation", t);
				} 
				if (!_filesOpened)
					return;
			}
		});
	}

	private JSplitPane createContentPane() {
		JComponent choices = createChoiceList();
		choices.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

		//     JPanel top = new JPanel (new BorderLayout ());
		//     top.add (choices, BorderLayout.CENTER);
		//     top.add (ComponentFactory.createScrollPane(choices), BorderLayout.CENTER);
		JScrollPane top = ComponentFactory.createScrollPane(choices);
		_mainSplitter.setTopComponent(top);
		createFilePanels();

		_mainSplitter.setBottomComponent(new JPanel(new BorderLayout()));

		_mainSplitter.setDividerLocation((int) top.getPreferredSize()
				.getHeight());
		return _mainSplitter;
	}

	@Override
	public void reshape(int x, int y, int w, int h) {
		super.reshape(x, y, w, h);

		JComponent top = (JComponent) _mainSplitter.getTopComponent();
		_mainSplitter.setDividerLocation((int) top.getPreferredSize()
				.getHeight());
		top.setMaximumSize(top.getPreferredSize());

	}

	private void createFilePanels() {
		_mergeFilesPanel = new GetFilesPanel(3, PromptModes.MERGING_MODE);
		if (PromptTab.makeViewsExplicit())
			_extractFilesPanel = new GetFilesPanel(2, PromptModes.EXTRACTING_MODE,
					"Choose the ontology to extract concepts from",
					"Choose the project with view definitions (optional, must have been generated by Prompt)");
		else
			_extractFilesPanel = new GetFilesPanel(1, PromptModes.EXTRACTING_MODE, "Choose the ontology to extract concepts from");
		
		_moveFilesPanel = new GetFilesPanel(1, PromptModes.MOVING_MODE, "Choose the included project that will be modified");
		_diffFilesPanel = new GetFilesPanel(1, PromptModes.DIFF_MODE, "Choose the version to compare with the current project");
		_mapFilesPanel = new GetFilesPanel(2, PromptModes.MAPPING_MODE);
	}

	private JPanel createChoiceList() {
		JPanel choices = new JPanel(new GridLayout(0, 1, 0, 10));

		int[] modes = PromptModes.getModes();
		ButtonGroup buttonGroup = new ButtonGroup();
		JRadioButton[] buttons = new JRadioButton[modes.length];
		for (int i = 0; i < modes.length; i++) {
			JPanel buttonPanel = new JPanel(new BorderLayout());

			buttons[i] = new JRadioButton("");
			buttons[i].setActionCommand(PromptModes.getModeName(modes[i]));
			buttons[i].addActionListener(createButtonActionListener());

			buttonGroup.add(buttons[i]);

			buttonPanel.add(buttons[i], BorderLayout.WEST);
			buttonPanel.add(createButtonLabel(modes[i]), BorderLayout.CENTER);

			choices.add(buttonPanel);
		}
		return choices;
	}

	private JComponent createButtonLabel(int mode) {
		JLabel label = new JLabel();
		String fontName = label.getFont().getFontName();
		String fontFamily = label.getFont().getFamily();
		String labelText = "<html><font face = \"" + fontName
				+ "\" family = \"" + fontFamily + "\"><font color = blue><b>"
				+ "   " + PromptModes.getModeName(mode) + "</b></font> "
				+ PromptModes.getDetails(mode) + "</font></html>";
		label.setText(labelText);
		return label;
	}

	private ActionListener createButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selectedButton = e.getActionCommand();
				_selectedMode = PromptModes.getModeFromName(selectedButton);
				_initButton.setEnabled(true);

				// need to make sure this is turned off as it gets set when the user selects to use
				// an algorith plugin
				Preferences.pluginMatch(false);

				switch (_selectedMode) {
				case PromptModes.DIFF_MODE: {
					_selectedPanel = _diffFilesPanel;
					_mainSplitter.setBottomComponent(ComponentFactory.createScrollPane(_diffFilesPanel));
					break;
				}
				case PromptModes.EXTRACTING_MODE: {
					_selectedPanel = _extractFilesPanel;
					_mainSplitter.setBottomComponent(ComponentFactory.createScrollPane(_extractFilesPanel));
					break;
				}
				case PromptModes.MERGING_MODE: {
					_selectedPanel = _mergeFilesPanel;
					_mainSplitter.setBottomComponent(ComponentFactory.createScrollPane(_mergeFilesPanel));
					break;
				}
				case PromptModes.MAPPING_MODE: {
					_selectedPanel = _mapFilesPanel;
					_mainSplitter.setBottomComponent(ComponentFactory.createScrollPane(_mapFilesPanel));
					break;
				}
				case PromptModes.MOVING_MODE: {
					_selectedPanel = _moveFilesPanel;
					_mainSplitter.setBottomComponent(ComponentFactory.createScrollPane(_moveFilesPanel));
				}
				}
			};
		};
	}

	private void initializeTask() {
		if (_selectedMode == PromptModes.MERGING_MODE && ProjectManager.getProjectManager().getCurrentProject().getProjectURI() == null) {
            ModalDialog.showMessageDialog(this, "Cannot start merge until project has been saved");
            return;
        } 
		
		int numberOfProjects = (_selectedMode == PromptModes.MAPPING_MODE) ? (_selectedPanel.getNumberOfProjects() + 2) : (_selectedPanel.getNumberOfProjects() + 1);

		String[] projectFiles = new String[numberOfProjects];
		String[] projectNames = new String[numberOfProjects];
		_filesOpened = _selectedPanel.collectInformation(projectFiles,	projectNames);
		if (_filesOpened == false)
			return;

		setMode();

		// we are merging and a mapping project is specified
		if ((PromptTab.merging())
				&& (projectNames[ProjectsAndKnowledgeBases.MAPPING_PROJECT_IN_MERGING_INDEX] != null && projectNames[ProjectsAndKnowledgeBases.MAPPING_PROJECT_IN_MERGING_INDEX].length() != 0)) {
			ProjectsAndKnowledgeBases.mappingProjectDefined(true);
		}

		if ((PromptTab.mapping())
				&& (projectNames[ProjectsAndKnowledgeBases.MAPPING_PROJECT_INDEX] != null && projectNames[ProjectsAndKnowledgeBases.MAPPING_PROJECT_INDEX].length() != 0)) {
			ProjectsAndKnowledgeBases.mappingProjectDefined(true);
		}

		//we have a view definitions file specified
		if (PromptTab.extracting()
				&& (projectNames[ProjectsAndKnowledgeBases.EXTRACT_VIEW_DEFINITIONS_INDEX] != null && projectNames[ProjectsAndKnowledgeBases.EXTRACT_VIEW_DEFINITIONS_INDEX].length() != 0)) {
			ProjectsAndKnowledgeBases.viewDefinitionsProjectDefined(true);
		}
	
		_filesOpened = true;

		if (_selectedMode == PromptModes.MERGING_MODE) {
			PromptTab.setCompareSources(_mergeFilesPanel.compareSources());
			Preferences.approximateMatch(_mergeFilesPanel.approximateMatch());
			//      Preferences.caseSensitiveConflicts(_mergeFilesPanel.caseSensitive());
		}

		if (_selectedMode == PromptModes.MAPPING_MODE) {
			PromptTab.setCompareSources(_mapFilesPanel.compareSources());
			Preferences.approximateMatch(_mapFilesPanel.approximateMatch());
			//      Preferences.caseSensitiveConflicts(_mapFilesPanel.caseSensitive());
		}

		if (_selectedMode == PromptModes.DIFF_MODE) {
			PromptDiff.showIncluded(_diffFilesPanel.showChangesInIncludedProjects());
			PromptDiff.setIdSlotName(_diffFilesPanel.idSlotName());
			PromptDiff.useIdSlotOnly(_diffFilesPanel.useIdSlotOnly());
		}

                int preferredOntologyIndex = PromptTab.merging() ? _mergeFilesPanel.preferredOntologyIndex() : 0;
		boolean mergingSetUp = PromptTab.setUpMerging(projectFiles,projectNames, (_mergeFilesPanel == null) ? -1: preferredOntologyIndex, false);
		if (!mergingSetUp)
			Warning.inform("<html>For Prompt to operate properly, all projects (including the current one) <br>"
							+ "must use the same format (for example, <it>Standard text files, RDF, OWL </it>.<br>"
							+ "Please, use <bf>Project|Save in Format...</bf> to convert your projects to the same format.</html>");

	}

	private void setMode() {
		switch (_selectedMode) {
		case PromptModes.MERGING_MODE: {
			PromptTab.setMerging();
			break;
		}
		case PromptModes.MOVING_MODE: {
			PromptTab.setMoving();
			break;
		}
		case PromptModes.EXTRACTING_MODE: {
			PromptTab.setExtracting();
			PromptTab.setCompareSources(false);
			break;
		}

		case PromptModes.MAPPING_MODE: {
			PromptTab.setMapping();
			PromptTab.setCompareSources(false);
			break;
		}
		case PromptModes.DIFF_MODE: {
			PromptTab.setDiff();
			PromptTab.setCompareSources(true);
			break;
		}
		}
	}

	public static JTextArea createExplanationText() {
		JTextArea _text = new JTextArea();

		_text
				.append("You can perform the following operations using the tab:\n");

		int[] modes = PromptModes.getModes();

		for (int i = 0; i < modes.length; i++) {
			_text.append("" + (i + 1) + ". " + modes[i] + ". \n\t"
					+ PromptModes.getDetails(modes[i]) + "\n");
		}

		_text.setEditable(false);
		return _text;
	}

	public void setStartButtonEnabled(boolean enabled) {
		_initButton.setEnabled(enabled);
	}
	
	@Override
	public String toString() {
		return "InitializationScreen";
	}

}
