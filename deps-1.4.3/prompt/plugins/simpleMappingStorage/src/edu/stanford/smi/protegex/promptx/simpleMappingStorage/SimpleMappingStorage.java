/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.promptx.simpleMappingStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protegex.prompt.Mappings;
import edu.stanford.smi.protegex.prompt.actionLists.HashMapForCollections;
import edu.stanford.smi.protegex.prompt.explanation.MappingDefinition;
import edu.stanford.smi.protegex.prompt.operation.MergeFramesOperation;
import edu.stanford.smi.protegex.prompt.plugin.model.MappingStoragePlugin;
import edu.stanford.smi.protegex.prompt.plugin.ui.MappingStoragePluginConfigurationPanel;
import edu.stanford.smi.protegex.prompt.plugin.util.PluginUtilities;
import edu.stanford.smi.protegex.prompt.util.Util;

public class SimpleMappingStorage implements MappingStoragePlugin {
	private static final String SIMPLE_MAPPING_STORAGE_DIRECTORY = "edu.stanford.smi.protegex.promptx.simpleMappingStorage";
	private static final String JAR_NAME = "simpleMappingStorage.jar";

	private static final String MAPPING_CLASS = "One_to_one_mapping";
	private static final String TARGET_SLOT = "target";
	private static final String SOURCE_SLOT = "source";
	private static final String MAPPING_METADATA_SLOT = "mapping_metadata";
	private static final String MAPPING_COMPONENT_CLASS = "Mapping_Component";
	private static final String COMPONENT_NAME_SLOT = "component_name";
	private static final String COMPONENT_SOURCE_SLOT = "component_source";
	private static final String COMPONENT_TYPE_SLOT = "component_type";
	private static final String MAPPING_METADATA_CLASS = "Mapping_Metadata";
	private static final String AUTHOR_SLOT = "author";
	private static final String DATE_SLOT = "date";
	private static final String COMMENT_SLOT = "comment";
	private static final String CLASS_COMPONENT_TYPE = "Class";
	private static final String PROPERTY_COMPONENT_TYPE = "Property";
	private static final String INSTANCE_COMPONENT_TYPE = "Instance";

	//////////////////////////////////////////////////////////////////////////////////////////
	private Cls _mappingCls = null;
	private Slot _targetSlot = null;
	private Slot _sourceSlot = null;
	private Slot _mappingMetadataSlot = null;
	private Cls _mappingComponentCls = null;
	private Slot _componentNameSlot = null;
	private Slot _componentSourceSlot = null;
	private Slot _componentTypeSlot = null;
	private Cls _mappingMetadataCls = null;
	private Slot _authorSlot = null;
	private Slot _dateSlot = null;
	private Slot _commentSlot = null;

	//////////////////////////////////////////////////////////////////////////////////////////
	private KnowledgeBase _mappingKb = null;
	private Project _mappingProject = null;

	private KnowledgeBase _sourceKb = null;
	private KnowledgeBase _targetKb = null;
	private HashMapForCollections _mappingsMap; // <frame, collection of mappings>

	private boolean _performingSavedMerges = false;

	public void initialize(KnowledgeBase sourceKb, KnowledgeBase targetKb, Project mappingProject) {
		_mappingProject = mappingProject;
		if (_mappingProject == null) {
			_mappingKb = createNewMappingKb();
		} else {
			_mappingKb = _mappingProject.getKnowledgeBase();
		}
		init(sourceKb, targetKb);
	}

	private void init(KnowledgeBase sourceKb, KnowledgeBase targetKb) {
		_sourceKb = sourceKb;
		_targetKb = targetKb;
		_mappingsMap = new HashMapForCollections(sourceKb.getFrameCount() + targetKb.getFrameCount());
		initializeMappingClassesAndSlots();
	}

	private KnowledgeBase createNewMappingKb() {
		_mappingProject = Util.createNewClipsProject("-mappings-simple");
		Collection errors = new ArrayList();

		_mappingProject.includeProject(PluginUtilities.getMappingProjectURI(SIMPLE_MAPPING_STORAGE_DIRECTORY, JAR_NAME, "mappings.pprj"), errors);
		Util.displayErrors(errors);
		_mappingProject.mergeIncludedProjects();

		KnowledgeBase kb = _mappingProject.getKnowledgeBase();
		return kb;
	}

	private void addToMappingsMap(Frame f, Instance mapping) {
		if (f != null) {
			_mappingsMap.put(createKey(f), mapping);
		}
	}

	private String createKey(Frame f) {
		return ("" + f + " " + f.getKnowledgeBase());
	}

	public void save() {
		Collection errors = new ArrayList();
		_mappingProject.save(errors);
		Util.displayErrors(errors);
	}

	public String getPluginName() {
		return "Store mapping using a simple mapping ontology";
	}

	public void invokePlugin() {
	// TODO Auto-generated method stub

	}

	public Project getProject() {
		return _mappingProject;
	}

	public void performSavedMerges() {
		_performingSavedMerges = true;
		Collection<Instance> mappings = _mappingCls.getInstances();

		Iterator i = mappings.iterator();
		HashMapForCollections performedMerges = new HashMapForCollections(mappings.size());
		while (i.hasNext()) {
			Instance nextMapping = (Instance) i.next();
			Frame nextSource;
			nextSource = getSourceFrameInMapping(nextMapping);
			if (nextSource == null) {
				continue;
			}
			Frame nextTarget;
			nextTarget = getTargetFrameInMapping(nextMapping);
			if (nextTarget == null) {
				continue;
			}
			Collection existingMergesForSource = performedMerges.getValues(nextSource);
			if (existingMergesForSource != null && !existingMergesForSource.isEmpty() && existingMergesForSource.contains(nextTarget)) {
				continue;
			}
			MergeFramesOperation.selectMergeOperation(nextSource, nextTarget, new MappingDefinition()).performOperation();
			performedMerges.put(nextSource, nextTarget);
		}
		_performingSavedMerges = false;

	}

	private Frame getSourceFrameInMapping(Instance mapping) {
		return getFrameInMapping(mapping, true);
	}

	private Frame getTargetFrameInMapping(Instance mapping) {
		return getFrameInMapping(mapping, false);
	}

	private Frame getFrameInMapping(Instance mapping, boolean sourceOrTarget) {
		Slot slot = sourceOrTarget ? _sourceSlot : _targetSlot;
		KnowledgeBase kb = sourceOrTarget ? _sourceKb : _targetKb;
		Instance component = (Instance) mapping.getOwnSlotValue(slot);
		if (component == null) {
			return null;
		}

		Object frameName = component.getOwnSlotValue(_componentNameSlot);
		if (frameName == null) {
			return null;
		}

		return kb.getFrame((String) frameName);
	}

	public void createClassToClassMapping(Cls cls1, Cls cls2) {
		if (_performingSavedMerges) {
			return;
		}
		Cls source = (cls1.getKnowledgeBase() == _sourceKb) ? cls1 : cls2;
		Cls target = (source.equals(cls1)) ? cls2 : cls1;
		createMappingFromSourceToTarget(source, target);
	}

	public void createSlotToSlotMapping(Slot slot1, Slot slot2) {
		if (_performingSavedMerges) {
			return;
		}
		Slot source = (slot1.getKnowledgeBase() == _sourceKb) ? slot1 : slot2;
		Slot target = (source.equals(slot1)) ? slot2 : slot1;
		createMappingFromSourceToTarget(source, target);
	}

	private void createMappingFromSourceToTarget(Frame source, Frame target) {
		if (source == null || target == null) {
			return;
		}
		Instance mapping = _mappingCls.createDirectInstance(null);

		setSourceInMapping(source, mapping);
		addToMappingsMap(source, mapping);

		setTargetInMapping(target, mapping);
		addToMappingsMap(target, mapping);

		setMetadataInMapping(mapping);
	}

	private void setMetadataInMapping(Instance mapping) {
		Instance metadata = _mappingMetadataCls.createDirectInstance(null);

		metadata.setDirectOwnSlotValue(_authorSlot, SystemUtilities.getUserName());
		metadata.setDirectOwnSlotValue(_dateSlot, (new Date()).toString());
		//metadata.setDirectOwnSlotValue(_commentSlot, "");

		mapping.setDirectOwnSlotValue(_mappingMetadataSlot, metadata);
	}

	private void setSourceInMapping(Frame source, Instance mapping) {
		setFrameInMapping(source, mapping, true);
	}

	private void setTargetInMapping(Frame target, Instance mapping) {
		setFrameInMapping(target, mapping, false);
	}

	private void setFrameInMapping(Frame frame, Instance mapping, boolean sourceOrTarget) {
		Instance mappingComponent = _mappingComponentCls.createDirectInstance(null);

		String componentType = null;
		if (frame instanceof Cls) {
			componentType = CLASS_COMPONENT_TYPE;
		} else if (frame instanceof Slot) {
			componentType = PROPERTY_COMPONENT_TYPE;
		} else {
			componentType = INSTANCE_COMPONENT_TYPE;
		}

		mappingComponent.setDirectOwnSlotValue(_componentNameSlot, frame.getName());
		mappingComponent.setDirectOwnSlotValue(_componentSourceSlot, frame.getKnowledgeBase().toString());
		mappingComponent.setDirectOwnSlotValue(_componentTypeSlot, componentType);

		mapping.setDirectOwnSlotValue(sourceOrTarget ? _sourceSlot : _targetSlot, mappingComponent);
	}

	public Cls getClassForClassToClassMappings() {
		return _mappingCls;
	}

	public Cls getClassForSlotToSlotMappings() {
		return _mappingCls;
	}

	public Object[] getMappedClses(Instance mapping) {
		return getMappedFrames(mapping);
	}

	public Object[] getMappedSlots(Instance mapping) {
		return getMappedFrames(mapping);
	}

	private Object[] getMappedFrames(Instance mapping) {
		Frame sourceFrame = getSourceFrameInMapping(mapping);
		Frame targetFrame = getTargetFrameInMapping(mapping);
		return new Object[] { sourceFrame, targetFrame };

	}

	private void initializeMappingClassesAndSlots() {
		_mappingCls = _mappingKb.getCls(MAPPING_CLASS);
		_targetSlot = _mappingKb.getSlot(TARGET_SLOT);
		_sourceSlot = _mappingKb.getSlot(SOURCE_SLOT);
		_mappingMetadataSlot = _mappingKb.getSlot(MAPPING_METADATA_SLOT);

		_mappingComponentCls = _mappingKb.getCls(MAPPING_COMPONENT_CLASS);
		_componentNameSlot = _mappingKb.getSlot(COMPONENT_NAME_SLOT);
		_componentSourceSlot = _mappingKb.getSlot(COMPONENT_SOURCE_SLOT);
		_componentTypeSlot = _mappingKb.getSlot(COMPONENT_TYPE_SLOT);

		_mappingMetadataCls = _mappingKb.getCls(MAPPING_METADATA_CLASS);
		_authorSlot = _mappingKb.getSlot(AUTHOR_SLOT);
		_dateSlot = _mappingKb.getSlot(DATE_SLOT);
		_commentSlot = _mappingKb.getSlot(COMMENT_SLOT);
	}

	public boolean showMappingInstances() {
		return true;
	}

	public String getTabName() {
		return "Simple mapping";
	}

	public MappingStoragePluginConfigurationPanel getConfigurationPanel() {
		SimpleMappingStoragePluginConfigurationPanel configPanel = new SimpleMappingStoragePluginConfigurationPanel();
		return configPanel;
	}

	public String getMappingFileName(MappingStoragePluginConfigurationPanel panel) {
		return panel.getMappingFileName();
	}

	/**
	 * Given the Cls objects involved in a mapping, this function removes the mapping from the
	 * mapping ontology, updates Prompt's knowledge of the mappings, and updates the UI.
	 */
	public void removeClassToClassMapping(Cls cls1, Cls cls2) {
		// remove references from the mapping ontology
		removeMappingInstance(cls1, cls2);
		removeMappingComponentInstance(cls1);
		removeMappingComponentInstance(cls2);

		// remove information in mapping hashtables
		Mappings.removeWhatBecameOfIt(cls1);
		Mappings.removeWhatBecameOfIt(cls2);
	}

	/**
	 * Finds the mapping instance associated with the two Cls objects and deletes the instance.
	 * 
	 * @param cls1
	 * @param cls2
	 */
	private void removeMappingInstance(Cls cls1, Cls cls2) {
		for (Instance instance : _mappingCls.getDirectInstances()) {
			Frame nextSource = getSourceFrameInMapping(instance);
			if (nextSource == null) {
				continue;
			}
			Frame nextTarget;
			nextTarget = getTargetFrameInMapping(instance);
			if (nextTarget == null) {
				continue;
			}

			if (nextSource.equals(cls1) && nextTarget.equals(cls2)) {
				for (Slot slot : cls1.getTemplateSlots()) {
					if (!slot.isSystem()) {
						Slot slot2 = _mappingKb.getSlot(slot.getName());
						if (slot2 != null) {
							_mappingKb.deleteSlot(slot2);
						}
					}
				}

				for (Slot slot : cls2.getTemplateSlots()) {
					if (!slot.isSystem()) {
						Slot slot2 = _mappingKb.getSlot(slot.getName());
						if (slot2 != null) {
							_mappingKb.deleteSlot(slot2);
						}
					}
				}

				Instance metadataInstance = (Instance) instance.getOwnSlotValue(_mappingMetadataSlot);

				_mappingMetadataCls.getKnowledgeBase().deleteInstance(metadataInstance);
				_mappingCls.getKnowledgeBase().deleteInstance(instance);
			}
		}
	}

	/**
	 * Finds the mapping component instance associated with the given Cls object and removes it.
	 * 
	 * @param cls1
	 */
	private void removeMappingComponentInstance(Cls cls1) {
		for (Instance instance : _mappingComponentCls.getDirectInstances()) {
			Object nameValue = getSlotInstanceValue(instance, _componentNameSlot);
			Object sourceValue = getSlotInstanceValue(instance, _componentSourceSlot);

			if (nameValue.equals(cls1.getBrowserText()) && sourceValue.equals(cls1.getKnowledgeBase().toString())) {
				_mappingCls.getKnowledgeBase().deleteInstance(instance);
			}
		}
	}

	private Object getSlotInstanceValue(Instance instance, Slot slot) {
		return instance.getOwnSlotValue(slot);
	}
}
