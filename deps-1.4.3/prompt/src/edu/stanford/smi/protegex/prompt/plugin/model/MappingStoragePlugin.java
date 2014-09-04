/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */
package edu.stanford.smi.protegex.prompt.plugin.model;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protegex.prompt.plugin.ui.MappingStoragePluginConfigurationPanel;

public interface MappingStoragePlugin extends PromptAlgorithmPlugin {

	public Project getProject();

	public void save();

	public void performSavedMerges();

	public void createClassToClassMapping(Cls cls1, Cls cls2);

	public void createSlotToSlotMapping(Slot slot1, Slot slot2);

	public void removeClassToClassMapping(Cls cls1, Cls cls2);

	public Cls getClassForClassToClassMappings();

	public Cls getClassForSlotToSlotMappings();

	public Object[] getMappedClses(Instance instance);

	public Object[] getMappedSlots(Instance instance);

	public void initialize(KnowledgeBase sourceKb, KnowledgeBase targetKb, Project mappingProject);

	public boolean showMappingInstances();

	public String getTabName();

	public MappingStoragePluginConfigurationPanel getConfigurationPanel();

	public String getMappingFileName(MappingStoragePluginConfigurationPanel configPanel);
}
