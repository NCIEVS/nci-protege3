/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */
package edu.stanford.smi.protegex.promptx.simpleMappingStorage;

import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.plugin.ui.*;
import edu.stanford.smi.protegex.prompt.ui.*;

public class SimpleMappingStoragePluginConfigurationPanel extends MappingStoragePluginConfigurationPanel {
	
	private SingleFilePanel _mappingProjectPanel; 
	
	public SimpleMappingStoragePluginConfigurationPanel () {
		_mappingProjectPanel = new SingleFilePanel (PromptModes.MAPPING_MODE, ProjectsAndKnowledgeBases.MAPPING_PROJECT_INDEX, false); 
		add(_mappingProjectPanel);
	}

	public String getMappingFileName() {
		return _mappingProjectPanel.getFileName();
	}
	

}
