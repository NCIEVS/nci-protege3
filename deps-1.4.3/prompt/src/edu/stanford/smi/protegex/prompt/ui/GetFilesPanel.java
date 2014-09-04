/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 *                 Kyle Bruck kbruck@stanford.edu
 *                 Sean Falconer seanf@uvic.ca
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.plugin.*;
import edu.stanford.smi.protegex.prompt.plugin.model.*;
import edu.stanford.smi.protegex.prompt.plugin.ui.*;
import edu.stanford.smi.protegex.prompt.plugin.util.*;

public class GetFilesPanel extends JPanel {
	private static final String DEFAULT_ALGORITHM = "Lexical matching";
	
	private int _numberOfProjects;
	private SingleFilePanel [] _filePanels = null;
	private String _promptString = null;
	private JCheckBox _compareSourcesCheckBox = null;
	private JCheckBox _approximateMatchCheckBox = null;
	private JCheckBox _caseSensitiveCheckBox = null;
	private JCheckBox _showChangesInIncludedProjectsCheckBox = null;
	private JComboBox _algorithmChooserList = null;
	private JCheckBox [] _mappingStorageCheckBoxes = null;
	private MappingStoragePlugin [] _mappingStoragePlugins = null;
	private SelectIdSlotPanel _selectIdSlotPanel = null;
	private MappingStoragePluginConfigurationPanel [] _mappingStoragePluginsConfigurationPanels  = null;
	
	private JPanel _optionsPanel = null;
	private JComponent _configPanel = null;
	private JPanel _configBorderPanel = null;
	private JPanel _algorithmChooser = null;
	private JPanel _mappingStorageOptions = null;
	
	int _mode;
	
	GetFilesPanel (int numberOfProjects, int mode, String promptString) {
		super ();
		initialize (numberOfProjects, mode, promptString, null);
	}
	
	GetFilesPanel (int numberOfProjects, int mode, String promptString, String viewDefinitions) {
		super ();
		initialize (numberOfProjects, mode, promptString, viewDefinitions);
	}
	
	GetFilesPanel (int numberOfProjects, int mode) {
		super ();
		initialize (numberOfProjects, mode, null, null);
	}
	
	private void initialize (int numberOfProjects, int mode, String promptString, String viewDefinitions) { 
		_numberOfProjects = numberOfProjects;
		_mode = mode;
		_promptString = promptString;
		
		setLayout (new BorderLayout ());
		
		
		JTextArea details = new JTextArea ();
		details.setEditable(false);
		details.setRows(2);
		details.setText (PromptModes.getDetails(mode));
//		add (details, BorderLayout.NORTH);
		
		JPanel argumentsPanel = new JPanel(new BorderLayout());
		JPanel fileContainerPanel = new JPanel(new GridLayout(0, 1));
		//argumentsPanel.setLayout(new GridLayout (0, 1));
		
		_filePanels = new SingleFilePanel [_numberOfProjects];
		for (int i = 0; i < _numberOfProjects; i++) {
			if ((mode == PromptModes.MERGING_MODE || _mode == PromptModes.MAPPING_MODE) && (i == _numberOfProjects -1))
				break;
			if (mode == PromptModes.EXTRACTING_MODE)
				_filePanels[i] = (i == 0) ? new SingleFilePanel(_mode, _promptString) : new SingleFilePanel(_mode, viewDefinitions);
			else if ((mode == PromptModes.MERGING_MODE) || (mode == PromptModes.MAPPING_MODE))
				_filePanels[i] = new SingleFilePanel(_mode, i);
			else
				_filePanels[i] = new SingleFilePanel(_mode, _promptString);
			fileContainerPanel.add(_filePanels[i]);
		}
		
		argumentsPanel.add(fileContainerPanel, BorderLayout.NORTH);

		if(mode == PromptModes.MAPPING_MODE || mode == PromptModes.MERGING_MODE) {
			_optionsPanel = new JPanel();
			_optionsPanel.setLayout(new GridLayout (1, 2, 10, 0));
			
			_optionsPanel.add (createAlgorithmChooser());
			_optionsPanel.add (createMappingStorageChooser());
			
			argumentsPanel.add(_optionsPanel, BorderLayout.CENTER);
			createPreferredButtonsGroup();
			
			if (mode == PromptModes.MAPPING_MODE)
				_filePanels[0].setPreferred(true);
		}
		if (mode == PromptModes.DIFF_MODE) {
			_optionsPanel = new JPanel (new BorderLayout ());
			JPanel internal = new JPanel (new GridLayout(0, 1));
			internal.add (showChangesInIncludedProjectsCheckBox());
			internal.add (selectIdSlotWidget());
			_optionsPanel.add(internal, BorderLayout.WEST);
			argumentsPanel.add (_optionsPanel, BorderLayout.CENTER);
		}
//		if (mode == MOVE_OPTION)
//		setDefaultIncluded ((SingleFilePanel)_filePanels[0]);
		
		add (argumentsPanel, BorderLayout.CENTER);
	}
	
	private void createPreferredButtonsGroup () {
		ButtonGroup group = new ButtonGroup ();
		for (int i = 0; i < _numberOfProjects; i++) {
			if((_mode != PromptModes.MAPPING_MODE && (_mode != PromptModes.MERGING_MODE)) || ((_mode == PromptModes.MAPPING_MODE || _mode == PromptModes.MERGING_MODE) && (i != _numberOfProjects - 1)))
				group.add(_filePanels[i].getPreferredButton());
		}
	}
	
	public int preferredOntologyIndex () {
		for (int i = 0; i < _numberOfProjects; i++) {
			if (_filePanels[i] != null && _filePanels[i].isPreferred()) return i;
		}
		return -1;
	}
	
	public boolean collectInformation (String [] names, String [] aliases) {
		for (int i = 0; i < _numberOfProjects; i++) {
			if ((_mode == PromptModes.MERGING_MODE || _mode == PromptModes.MAPPING_MODE) && (i == _numberOfProjects -1))
				break;
			names [i] = _filePanels[i].getFileName();
			if (_mode == PromptModes.MERGING_MODE && i == ProjectsAndKnowledgeBases.MAPPING_PROJECT_IN_MERGING_INDEX && (names[i] == null || names[i].length() == 0))
				break;
			if ((_mode == PromptModes.MAPPING_MODE) && i == ProjectsAndKnowledgeBases.MAPPING_PROJECT_INDEX && (names[i] == null || names[i].length() == 0))
				break;
			if (_mode == PromptModes.EXTRACTING_MODE && i == ProjectsAndKnowledgeBases.EXTRACT_VIEW_DEFINITIONS_INDEX && (names[i] == null || names[i].length() == 0))
				break;
			if (names[i] == null || names[i].length() == 0)  return false;
			aliases [i] = _filePanels[i].getFileAlias ();
		}
		
		if (_mode == PromptModes.MERGING_MODE || _mode == PromptModes.MAPPING_MODE) {
			names[_numberOfProjects - 1] = collectInformationOnMappingFile ();
			if (names[_numberOfProjects - 1] != null && names[_numberOfProjects - 1].length() > 0) {
				aliases [_numberOfProjects - 1] = FileUtilities.getBaseName (names[_numberOfProjects - 1]);
			}
		}
			
		
		return true;
	}
	
	// returns the specified mapping file for the first checked storage plugin
	// creates the set of active storage plugins
	private String collectInformationOnMappingFile () {
		String nextMappingFile = null;
		HashSet<MappingStoragePlugin> activePlugins = new HashSet (_mappingStorageCheckBoxes.length);
		
		for (int i = 0; i < _mappingStorageCheckBoxes.length; i++) {
			if (_mappingStorageCheckBoxes[i].isSelected()) {
				activePlugins.add(_mappingStoragePlugins[i]);
				if (nextMappingFile == null) {
					nextMappingFile = _mappingStoragePluginsConfigurationPanels[i].getMappingFileName ();
					if (nextMappingFile != null) {
						PluginManager.getInstance().setStoragePluginWithSavedMappings(_mappingStoragePlugins[i]);
					}
				}
			}
		}
		PluginManager.getInstance().setActiveMappingStoragePlugins(activePlugins);
		return nextMappingFile;
	}
	
	private JPanel createMappingStorageChooser () {	
		JPanel mappingStorageOptions = new JPanel ();

		LinkedList listOfStorageOptions = PluginManager.getInstance().getPlugins(PluginManager.PLUGIN_MAPPING_STORAGE);
		MappingStoragePluginOrder.putSimplePluginFirst (listOfStorageOptions);
		mappingStorageOptions.setLayout(new GridLayout (listOfStorageOptions.size(), 1));
		mappingStorageOptions.setBorder (BorderFactory.createTitledBorder(""));
		
		
//		String [] storageOptions = new String [listOfStorageOptions.size()];
		_mappingStorageCheckBoxes = new JCheckBox [listOfStorageOptions.size()];
		_mappingStoragePlugins = new MappingStoragePlugin [listOfStorageOptions.size()];
		_mappingStoragePluginsConfigurationPanels = new MappingStoragePluginConfigurationPanel [listOfStorageOptions.size()];
	
		int index = 0;
		Iterator i = listOfStorageOptions.iterator();
		while (i.hasNext()) {
			MappingStoragePlugin nextPlugin = (MappingStoragePlugin)i.next();
			String pluginName = nextPlugin.getPluginName();
			if (pluginName.length() > 0) {
				_mappingStorageCheckBoxes [index] = ComponentFactory.createCheckBox(pluginName);
				_mappingStoragePlugins [index] = nextPlugin;
				if (index == 0)
					_mappingStorageCheckBoxes[index].setSelected(true);
				else
					_mappingStorageCheckBoxes[index].setSelected(false);
				JPanel nextOption = new JPanel ();
				nextOption.setLayout(new BorderLayout ());
				nextOption.add(_mappingStorageCheckBoxes [index], BorderLayout.NORTH);
				_mappingStoragePluginsConfigurationPanels [index] = nextPlugin.getConfigurationPanel ();
				if (_mappingStoragePluginsConfigurationPanels [index] != null) 
					nextOption.add(_mappingStoragePluginsConfigurationPanels [index], BorderLayout.CENTER);
				
				mappingStorageOptions.add(nextOption);
			}
			index++;
		}
		
		LabeledComponent lc = new LabeledComponent ("Choose the options for storing mappings", mappingStorageOptions);
		_mappingStorageOptions = new JPanel (new BorderLayout ());
		_mappingStorageOptions.add(lc, BorderLayout.CENTER);
		
		return _mappingStorageOptions;
		
	}
	
	/**
	 * Creates the algorithm choosing panel that a user can use to select a plugin
	 * as their algorithm of choice.
	 */
	private JPanel createAlgorithmChooser() {
		
		_algorithmChooser = new JPanel ();
		_algorithmChooser.setLayout (new BorderLayout ());
		initAlgorithmList();

		_configBorderPanel = new JPanel(new BorderLayout());
		_configBorderPanel.setBorder(BorderFactory.createTitledBorder("Algorithm configuration:"));

		_configPanel = new JPanel(new GridLayout(5, 1));
		_configPanel.add(analyzeCheckBox());
		_configPanel.add(approximateMatchCheckBox());
		_configBorderPanel.add(_configPanel, BorderLayout.CENTER);

		_algorithmChooser.add(_configBorderPanel, BorderLayout.CENTER);

		LabeledComponent lc = new LabeledComponent("Choose the algorithm to use in initial comparison", _algorithmChooserList);
		_algorithmChooser.add(lc, BorderLayout.NORTH);
		return _algorithmChooser;
	}	
	
	/**
	 * Initializes the combobox that displays the algorithm list.
	 */
	private void initAlgorithmList() {
		_algorithmChooserList = ComponentFactory.createComboBox();
		
		LinkedList listOfAlgorithms = PluginManager.getInstance().getPlugins(PluginManager.PLUGIN_ALG_COMPARISON);
		
		String[] algorithms = new String[listOfAlgorithms.size() + 1];
		algorithms[0] = DEFAULT_ALGORITHM;
		
		int i = 1;
		for(Iterator iter = listOfAlgorithms.iterator(); iter.hasNext();) {
			String pluginName = ((PromptPlugin)iter.next()).getPluginName();
			// only display plugin's with valid names
			if(pluginName.length() > 0) {
				algorithms[i++] = pluginName;
			}
		}
		
		_algorithmChooserList.setModel(new DefaultComboBoxModel(algorithms));
		_algorithmChooserList.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
		        JComboBox cb = (JComboBox)e.getSource();
		        String selectedAlgorithm = (String)cb.getSelectedItem();
		        
		        addConfigurationPanel(selectedAlgorithm);
		    }			
		});
	}
	
	/**
	 * Adds a custom configuration panel based on the algorithm that was chosen.
	 * @param selectedAlgorithm The selected algorithm.
	 */
	private void addConfigurationPanel(String selectedAlgorithm) {
		_configBorderPanel.remove(_configPanel);
		_algorithmChooser.remove(_configBorderPanel);
        
		// handle native prompt's algorithm configuration differently than a plugin
        if(selectedAlgorithm.equals(DEFAULT_ALGORITHM)) {
        	_configPanel = new JPanel(new GridLayout(3, 0));
        	_configPanel.add(analyzeCheckBox());
        	_configPanel.add(approximateMatchCheckBox());
        	_configBorderPanel.add(_configPanel, BorderLayout.CENTER);
        	_algorithmChooser.add(_configBorderPanel, BorderLayout.CENTER);
        	
        	// reset the active plugin to be null
        	PluginManager.getInstance().setActiveComparisonPlugin(null);
        	Preferences.pluginMatch(false);
        }
        else {
        	// get the custom configuration panel for the selected algorithm
        	ComparisonAlgorithmPlugin algorithmPlugin = (ComparisonAlgorithmPlugin)PluginManager.getInstance().getPlugin(PluginManager.PLUGIN_ALG_COMPARISON, selectedAlgorithm);
        	_configPanel = algorithmPlugin.getConfigurationComponent();
        	
        	// set the selected algorithm as the active plugin
        	PluginManager.getInstance().setActiveComparisonPlugin(algorithmPlugin);
        	Preferences.pluginMatch(true);
        	
        	if(_configPanel == null) {
        		_configBorderPanel.setBorder(null);
        		_configPanel = new JPanel();
        	}
        	else {
        		_configBorderPanel.setBorder(BorderFactory.createTitledBorder("Algorithm configuration:"));
        	}
        	
        	_configBorderPanel.add(_configPanel, BorderLayout.CENTER);
        	_algorithmChooser.add(_configBorderPanel, BorderLayout.CENTER);
        }
        
        // refresh the GUI
        SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				_algorithmChooser.invalidate();
				_algorithmChooser.validate();
			}
		});
	}
	
	private JCheckBox analyzeCheckBox () {
		_compareSourcesCheckBox = ComponentFactory.createCheckBox("Compare sources (can be slow if ontologies are very large)");
		_compareSourcesCheckBox.setSelected(true);
		return _compareSourcesCheckBox;
	}
	
	private JCheckBox approximateMatchCheckBox () {
		_approximateMatchCheckBox = ComponentFactory.createCheckBox("Approximate match for names (slower)");
		_approximateMatchCheckBox.setSelected(true);
		return _approximateMatchCheckBox;
	}
	
	private JCheckBox caseSensitiveCheckBox () {
		_caseSensitiveCheckBox = ComponentFactory.createCheckBox("Case-sensitive comparison");
		_caseSensitiveCheckBox.setSelected(false);
		return _caseSensitiveCheckBox;
	}
	
	private JCheckBox showChangesInIncludedProjectsCheckBox () {
		_showChangesInIncludedProjectsCheckBox = ComponentFactory.createCheckBox("Display changes for included frames");
		_showChangesInIncludedProjectsCheckBox.setSelected(true);
		return _showChangesInIncludedProjectsCheckBox;
	}
	
	private SelectIdSlotPanel selectIdSlotWidget () {
		_selectIdSlotPanel = new SelectIdSlotPanel ();
		return _selectIdSlotPanel;
	}
	
	public boolean compareSources () {
		return _compareSourcesCheckBox.isSelected();
	}
	
	public boolean approximateMatch () {
		return _approximateMatchCheckBox.isSelected();
	}
	
	public boolean caseSensitive () {
		return _caseSensitiveCheckBox.isSelected();
	}
	
	public boolean showChangesInIncludedProjects () {
		return _showChangesInIncludedProjectsCheckBox.isSelected();
	}
	
	public boolean useIdSlotOnly () {
		return _selectIdSlotPanel.useIdSlotOnly();
	}
	
	public String idSlotName () {
		Object value = _selectIdSlotPanel.getIdSlot();
		if (value == null) return null;
		return ((Slot)value).getName();
	}
	
	public int getNumberOfProjects () {
//		if (_useTarget) return _numberOfProjects + 1;
//		else
		return _numberOfProjects;
	}
	
	
	
	

}
