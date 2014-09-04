package edu.stanford.smi.protegex.prompt.ui;

import java.awt.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.plugin.model.*;

public class MappingInstancesPane extends SourceInstancesPane {
	private Cls _instanceMappingCls = null;
	private Cls _slotMappingCls = null;
	private MappingStoragePlugin _mappingStoragePlugin = null;
	
 	public  MappingInstancesPane (Dimension size, KnowledgeBaseInMerging kbInMerging, MappingStoragePlugin mappingStoragePlugin, boolean allowSelection) {
 		super (size, kbInMerging, allowSelection, false);
 		_mappingStoragePlugin = mappingStoragePlugin;
		_instanceMappingCls = _mappingStoragePlugin.getClassForClassToClassMappings();
		_slotMappingCls = _mappingStoragePlugin.getClassForSlotToSlotMappings();
	}
	
 	public void synchronizeMappingSelection () {
		Cls cls = getSelectedCls();
		if (cls == null) return;
		Instance instance = getSelectedInstance ();
		if (instance == null) return;
		Object [] args = null;
		if (cls.equals(_instanceMappingCls)) {
			args = _mappingStoragePlugin.getMappedClses(instance);
		} else if (cls.equals(_slotMappingCls)) {
			args = _mappingStoragePlugin.getMappedSlots(instance);
		}
		//PromptTab.getTabComponent().selectArgumentsInTrees(args);
			
	}

}
