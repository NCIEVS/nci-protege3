/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.promptx.mappingDomainPSM;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protegex.prompt.actionLists.HashMapForCollections;
import edu.stanford.smi.protegex.prompt.explanation.MappingDefinition;
import edu.stanford.smi.protegex.prompt.operation.MergeFramesOperation;
import edu.stanford.smi.protegex.prompt.plugin.model.MappingStoragePlugin;
import edu.stanford.smi.protegex.prompt.plugin.ui.MappingStoragePluginConfigurationPanel;
import edu.stanford.smi.protegex.prompt.plugin.util.PluginUtilities;
import edu.stanford.smi.protegex.prompt.ui.DisplayWarning;
import edu.stanford.smi.protegex.prompt.util.Util;

public class MappingDomainPSM implements MappingStoragePlugin {
	private static final String MAPPING_DOMAIN_PSM_DIRECTORY = "edu.stanford.smi.protegex.promptx.mappingDomainPSM";
	private static final String JAR_NAME = "mappingDomainPSM.jar";

	private static String INSTANCE_MAPPING_CLASS = "instance-mapping";
	private static String TARGET_CLASS_SLOT = "target-class";
	private static String SOURCE_CLASS_SLOT = "source-class-desc";
	private static String ON_DEMAND_SLOT = "on-demand";

	private static String TARGET_CLASS_DESCRIPTION_CLASS = "target-class-description";
	private static String SOURCE_CLASS_DESCRIPTION_CLASS = "source-class-description";
	private static String NAME_SLOT = "name";

	private static String SLOT_MAPS_SLOT = "slot-maps";
	private static String SLOT_MAPPING_CLS = "slot-mapping";

	private static String RENAMING_SLOT_MAPPING_CLASS = "renaming-slot-mapping";
	private static String RECURSIVE_SLOT_MAPPING_CLASS = "recursive-slot-mapping";
	private static String RECURSIVE_MAPPING_SLOT = "mappings";

	private static String SLOT_MAPPING_SOURCE_SLOT = "source-slot";
	private static String SLOT_MAPPING_TARGET_SLOT = "target-slot";

	private static String SOURCE_SLOT_DESCRIPTION_CLASS = "source-slot-description";
	private static String TARGET_SLOT_DESCRIPTION_CLASS = "target-slot-description";

	private static String GLOBAL_SLOT_MAPPING_CLASS = "global-slot-mapping";

/////////////////////////////////////////////////////////////////////////////////////////	

	private Cls _instanceMappingCls = null;
	private Slot _targetClassSlot = null;
	private Slot _sourceClassSlot = null;
	private Slot _onDemandSlot = null;

	private Cls _targetClassDescriptionCls = null;
	private Cls _sourceClassDescriptionCls = null;
	private Slot _nameSlot = null;

	private Slot _slotMapsSlot = null;
	private Cls _slotMappingCls = null;

	private Cls _renamingSlotMappingCls = null;
	private Cls _recursiveSlotMappingCls = null;
	private Slot _recursiveMappingSlot = null;

	private Slot _slotMappingSourceSlot = null;
	private Slot _slotMappingTargetSlot = null;

	private Cls _sourceSlotDescriptionCls = null;
	private Cls _targetSlotDescriptionCls = null;

	private Cls _globalSlotMappingCls = null;

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
		_mappingProject = Util.createNewClipsProject("-mappings-dpsm");
		Collection errors = new ArrayList();

		_mappingProject.includeProject(PluginUtilities.getMappingProjectURI(MAPPING_DOMAIN_PSM_DIRECTORY, JAR_NAME, "mappings.pprj"), errors);
		Util.displayErrors(errors);
		_mappingProject.mergeIncludedProjects();

		KnowledgeBase kb = _mappingProject.getKnowledgeBase();

//		createTDOntology (kb);
		return kb;
	}

//	private static final String MAPPINGS_PROJECT_PATH = "projects" + File.separatorChar + "mappings.pprj";
	// ** should only be the following
	// private static final String MAPPINGS_PROJECT_PATH = "/projects/mappings.pprj";

//	private URI getMappingProjectURI () {
////		File installationDir = PluginUtilities.getInstallationDirectory(PromptTab.class.getName()); 
////		URI installationDirURI = installationDir.toURI();
//
//		File pluginsDir= new File (PromptTab.getPromptDirectory()); 
//		URI pluginsDirURI = pluginsDir.toURI();
//		URI mappingsURI = null;
//	    try {
//	    		mappingsURI = new URI(pluginsDir.toString() + MAPPINGS_PROJECT_PATH);
//	    } catch (Exception e) {
//	    		Log.getLogger().info("Cannot resolve URI: " + pluginsDir.toString() + mappingsURI);
//	    }
//	    return mappingsURI;
//	}
//	
//	
	private void initializeMappingClassesAndSlots() {
		_instanceMappingCls = _mappingKb.getCls(INSTANCE_MAPPING_CLASS);
		_targetClassSlot = _mappingKb.getSlot(TARGET_CLASS_SLOT);
		_sourceClassSlot = _mappingKb.getSlot(SOURCE_CLASS_SLOT);
		_onDemandSlot = _mappingKb.getSlot(ON_DEMAND_SLOT);

		_targetClassDescriptionCls = _mappingKb.getCls(TARGET_CLASS_DESCRIPTION_CLASS);
		_sourceClassDescriptionCls = _mappingKb.getCls(SOURCE_CLASS_DESCRIPTION_CLASS);
		_nameSlot = _mappingKb.getSlot(NAME_SLOT);

		_slotMapsSlot = _mappingKb.getSlot(SLOT_MAPS_SLOT);
		_slotMappingCls = _mappingKb.getCls(SLOT_MAPPING_CLS);

		_renamingSlotMappingCls = _mappingKb.getCls(RENAMING_SLOT_MAPPING_CLASS);
		_recursiveSlotMappingCls = _mappingKb.getCls(RECURSIVE_SLOT_MAPPING_CLASS);
		_recursiveMappingSlot = _mappingKb.getSlot(RECURSIVE_MAPPING_SLOT);

		_slotMappingSourceSlot = _mappingKb.getSlot(SLOT_MAPPING_SOURCE_SLOT);
		_slotMappingTargetSlot = _mappingKb.getSlot(SLOT_MAPPING_TARGET_SLOT);

		_sourceSlotDescriptionCls = _mappingKb.getCls(SOURCE_SLOT_DESCRIPTION_CLASS);
		_targetSlotDescriptionCls = _mappingKb.getCls(TARGET_SLOT_DESCRIPTION_CLASS);

		_globalSlotMappingCls = _mappingKb.getCls(GLOBAL_SLOT_MAPPING_CLASS);
	}

	public void performSavedMerges() {
		_performingSavedMerges = true;
		Collection<Instance> instanceMappings = _instanceMappingCls.getInstances();
		Collection<Instance> slotMappings = _slotMappingCls.getInstances();

		removeMappingsFromSelf(instanceMappings, slotMappings);
		performMergeOperations(instanceMappings, true);
		performMergeOperations(slotMappings, false);
		_performingSavedMerges = false;
	}

	private void removeMappingsFromSelf(Collection<Instance> instanceMappings, Collection<Instance> slotMappings) {
		Iterator i = slotMappings.iterator();
		while (i.hasNext()) {
			Instance nextSlotMapping = (Instance) i.next();
			if (isSelfMapping(nextSlotMapping)) {
				Collection<Instance> recursiveMappings = nextSlotMapping.getDirectOwnSlotValues(_recursiveMappingSlot);
				if (recursiveMappings != null) {
					instanceMappings.removeAll(recursiveMappings);
				}
			}
		}
	}

	//the second argument is true if merging classes, false if merging slots
	private void performMergeOperations(Collection mappings, boolean clsesOrSlots) {
		Iterator i = mappings.iterator();
		HashMapForCollections performedMerges = new HashMapForCollections(mappings.size());
		while (i.hasNext()) {
			Instance nextMapping = (Instance) i.next();
			Frame nextSource;
			if (clsesOrSlots) {
				nextSource = getSourceClsInInstanceMapping(nextMapping);
			} else {
				nextSource = getSourceSlotInSlotMapping(nextMapping);
			}
			if (nextSource == null) {
				continue;
			}
			Frame nextTarget;
			if (clsesOrSlots) {
				nextTarget = getTargetClsInInstanceMapping(nextMapping);
			} else {
				nextTarget = getTargetSlotInSlotMapping(nextMapping);
			}
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
	}

	public void createOneSidedMapping(Frame f) {
		if (_performingSavedMerges) {
			return;
		}
		Collection existingMappings = _mappingsMap.getValues(createKey(f));
		if (existingMappings != null && !existingMappings.isEmpty()) {
			return;
		}
		KnowledgeBase kb = f.getKnowledgeBase();
		if (kb == _sourceKb) {
			if (f instanceof Cls) {
				createInstanceMappingFromSourceToTarget((Cls) f, null);
			} else if (f instanceof Slot) {
				createSlotMappingFromSourceToTarget((Slot) f, null);
			}
		} else { //f is in target
			if (f instanceof Cls) {
				createInstanceMappingFromSourceToTarget(null, (Cls) f);
			} else if (f instanceof Slot) {
				createSlotMappingFromSourceToTarget(null, (Slot) f);
			}
		}
	}

	public void createClassToClassMapping(Cls cls1, Cls cls2) {
		if (_performingSavedMerges) {
			return;
		}
		Cls source = (cls1.getKnowledgeBase() == _sourceKb) ? cls1 : cls2;
		Cls target = (source.equals(cls1)) ? cls2 : cls1;
		createInstanceMappingFromSourceToTarget(source, target);
	}

	private void createInstanceMappingFromSourceToTarget(Cls source, Cls target) {
		Instance instanceMapping = createInstanceMappingInstance(source, target);

		if (target == null || source == null) {
			return;
		}

		createRelatedRenameMappingsForGlobalSlotMappings(instanceMapping, source, target);
		createRecursiveMappingsFromInstanceMapping(instanceMapping, source, target);
	}

	private void createRecursiveMappingsFromInstanceMapping(Instance instanceMapping, Cls sourceCls, Cls targetCls) {
		Collection slots = sourceCls.getTemplateSlots();
		if (slots == null || slots.isEmpty()) {
			return;
		}

		Iterator i = slots.iterator();
		while (i.hasNext()) {
			Slot nextSlot = (Slot) i.next();
			Collection mappings = _mappingsMap.getValues(createKey(nextSlot));
			if (mappings == null || mappings.isEmpty()) {
				continue;
			}
			findRecursiveMappingsForSlot(mappings, instanceMapping, sourceCls, targetCls);
		}
	}

	private void findRecursiveMappingsForSlot(Collection slotMappings, Instance instanceMapping, Cls sourceCls, Cls targetCls) {
		Iterator i = slotMappings.iterator();
		while (i.hasNext()) {
			Instance nextSlotMapping = (Instance) i.next();
			Slot target = getTargetSlotInSlotMapping(nextSlotMapping);
			if (target != null) {
				findRecursiveMappingForCls(instanceMapping, targetCls, getSourceSlotInSlotMapping(nextSlotMapping), target);
			}
		}
	}

	private void setSourceClsInInstanceMapping(Instance instanceMapping, Cls source) {
		Instance sourceClassDescription = _sourceClassDescriptionCls.createDirectInstance(null);
		sourceClassDescription.setOwnSlotValue(_nameSlot, source.getName());
		instanceMapping.setOwnSlotValue(_sourceClassSlot, sourceClassDescription);
	}

	private void setTargetClsInInstanceMapping(Instance instanceMapping, Cls target) {
		Instance targetClassDescription = _targetClassDescriptionCls.createDirectInstance(null);
		targetClassDescription.setOwnSlotValue(_nameSlot, target.getName());
		instanceMapping.setOwnSlotValue(_targetClassSlot, targetClassDescription);
	}

	private void createRelatedRenameMappingsForGlobalSlotMappings(Instance instanceMapping, Cls source, Cls target) {
		Collection<Slot> sourceSlots = source.getTemplateSlots();
		Collection<Slot> targetSlots = target.getTemplateSlots();
		if (sourceSlots == null || targetSlots == null || sourceSlots.isEmpty() || targetSlots.isEmpty()) {
			return;
		}

		Collection<Instance> globalSlotMappings = new ArrayList<Instance>();
		Iterator i = sourceSlots.iterator();
		while (i.hasNext()) {
			Slot nextSourceSlot = (Slot) i.next();
			Collection<Instance> mappings = _mappingsMap.getValues(createKey(nextSourceSlot));
			if (mappings != null && !mappings.isEmpty()) {
				Iterator j = mappings.iterator();
				while (j.hasNext()) {
					Instance nextGlobalMapping = (Instance) j.next();
					Object nextTargetValue = getTargetSlotInSlotMapping(nextGlobalMapping);
					if (nextTargetValue != null && targetSlots.contains(nextTargetValue)) {
						globalSlotMappings.add(nextGlobalMapping);
						break;
					}
				}
			}
		}

		if (globalSlotMappings != null && !globalSlotMappings.isEmpty()) {
			Iterator g = globalSlotMappings.iterator();
			while (g.hasNext()) {
				Instance nextSlotMapping = (Instance) g.next();
				createRelatedRenameMapping(instanceMapping, getSourceSlotInSlotMapping(nextSlotMapping), getTargetSlotInSlotMapping(nextSlotMapping));
			}
		}

	}

//	private Collection<String> createCollectionWithFrameNames (Collection<Frame> frames) {
//		Collection<String> result = new ArrayList<String> ();
//		Iterator i = frames.iterator();
//		while (i.hasNext()) {
//			result.add (((Frame)i.next()).getName());
//		}
//		return result;
//	}

	private void removeOneSidedMapping(Frame f) {
		Collection<Instance> mappings = _mappingsMap.getValues(createKey(f));
		if (mappings == null || mappings.isEmpty()) {
			return;
		}

		KnowledgeBase kb = f.getKnowledgeBase();
		Slot otherSlot = null;

		if (kb == _sourceKb) {
			if (f instanceof Cls) {
				otherSlot = _targetClassSlot;
			} else if (f instanceof Slot) {
				otherSlot = _slotMappingTargetSlot;
			}
		} else { //f is in target kb
			if (f instanceof Cls) {
				otherSlot = _sourceClassSlot;
			} else if (f instanceof Slot) {
				otherSlot = _slotMappingSourceSlot;
			}
		}

		Iterator i = (new ArrayList<Instance>(mappings)).iterator();
		while (i.hasNext()) {
			Instance nextMapping = (Instance) i.next();
			if (nextMapping.getOwnSlotValue(otherSlot) == null) {
				_mappingsMap.remove(createKey(f), nextMapping);
				_mappingKb.deleteFrame(nextMapping);
			}
		}
	}

	public void createSlotToSlotMapping(Slot slot1, Slot slot2) {
		if (_performingSavedMerges) {
			return;
		}
		Slot sourceSlot = (slot1.getKnowledgeBase() == _sourceKb) ? slot1 : slot2;
		Slot targetSlot = (sourceSlot.equals(slot1)) ? slot2 : slot1;
		createSlotMappingFromSourceToTarget(sourceSlot, targetSlot);
	}

	private void createSlotMappingFromSourceToTarget(Slot sourceSlot, Slot targetSlot) {
		Instance slotMapping = _globalSlotMappingCls.createDirectInstance(null);

		if (sourceSlot != null) {
			setSourceSlotInSlotMapping(slotMapping, sourceSlot);
		}

		if (targetSlot != null) {
			setTargetSlotInSlotMapping(slotMapping, targetSlot);
		}

		if (sourceSlot != null && targetSlot != null) {
			removeOneSidedMapping(sourceSlot);
			removeOneSidedMapping(targetSlot);
		}
		addToMappingsMap(sourceSlot, slotMapping);
		addToMappingsMap(targetSlot, slotMapping);

		if (sourceSlot == null || targetSlot == null) {
			return; // if it's a one-sided mapping, no need to create slot-maps
		}

		Collection<Instance> mappingsForSourceDomain = findLocalMappingsFromClses(sourceSlot.getDirectDomain(), targetSlot, _targetKb);
		Collection<Instance> mappingsForTargetDomain = findLocalMappingsFromClses(targetSlot.getDirectDomain(), sourceSlot, _sourceKb);
		// find union without duplicates
		mappingsForTargetDomain.removeAll(mappingsForSourceDomain);
		mappingsForTargetDomain.addAll(mappingsForSourceDomain);

		createRenameMappings(mappingsForTargetDomain, sourceSlot, targetSlot);

		createRecursiveMappingsFromSlotMapping(sourceSlot, targetSlot, slotMapping);
		createLexicalMappings(sourceSlot, targetSlot);
	}

	private void createRecursiveMappingsFromSlotMapping(Slot sourceSlot, Slot targetSlot, Instance globalSlotMapping) {
		Collection sourceDomains = sourceSlot.getDirectDomain();
		if (sourceDomains == null || sourceDomains.isEmpty()) {
			return;
		}

		Iterator i = sourceDomains.iterator();
		while (i.hasNext()) {
			Cls nextDomain = (Cls) i.next();
			Collection<Instance> nextInstanceMappings = _mappingsMap.getValues(createKey(nextDomain));
			if (nextInstanceMappings == null || nextInstanceMappings.isEmpty()) {
				continue;
			}
			Iterator j = (new ArrayList<Instance>(nextInstanceMappings)).iterator();
			while (j.hasNext()) {
				Instance nextMapping = (Instance) j.next();
				Cls nextTargetCls = getTargetClsInInstanceMapping(nextMapping);
				if (nextTargetCls != null) {
					findRecursiveMappingForCls(nextMapping, nextTargetCls, sourceSlot, targetSlot);
				}
			}
		}
	}

	// does mappingTargetCls have a template slot T such that its range is in the domain of targetSlot?
	private void findRecursiveMappingForCls(Instance instanceMapping, Cls mappingTargetCls, Slot sourceSlot, Slot targetSlot) {
		Collection targetClsTemplateSlots = mappingTargetCls.getTemplateSlots();
		if (targetClsTemplateSlots == null || targetClsTemplateSlots.isEmpty()) {
			return;
		}
		Collection targetSlotDomains = targetSlot.getDirectDomain();
		Iterator i = targetClsTemplateSlots.iterator();
		while (i.hasNext()) {
			Slot nextSlot = (Slot) i.next();
			Collection ranges = mappingTargetCls.getTemplateSlotAllowedClses(nextSlot);
			// is any of the ranges in the domain of targetSlot?
			ranges.retainAll(targetSlotDomains);
			if (!ranges.isEmpty()) {
				Iterator r = ranges.iterator();
				while (r.hasNext()) {
					Cls nextRangeCls = (Cls) r.next();
					Collection existingMappings = findExistingMappings(getSourceClsInInstanceMapping(instanceMapping), nextRangeCls);
					if (existingMappings == null) {
						suggestRecursiveMapping(instanceMapping, nextSlot, nextRangeCls, sourceSlot, targetSlot);
					} else {
						Instance existingMapping = findExistingRecursiveMapping(existingMappings, instanceMapping);
						if (existingMapping == null) {
							suggestRecursiveMapping(instanceMapping, nextSlot, nextRangeCls, sourceSlot, targetSlot);
						} else {
							createRelatedRenameMapping(existingMapping, sourceSlot, targetSlot);
						}
					}
				}
			}
		}
	}

	// find out if any of the recursive mappings referred to by parentMapping is in fact one of the elements
	// of the recursiveMappings collection
	private Instance findExistingRecursiveMapping(Collection recursiveMappings, Instance parentMapping) {
		Collection currentSlotMaps = parentMapping.getDirectOwnSlotValues(_slotMapsSlot);
		if (currentSlotMaps == null || currentSlotMaps.isEmpty()) {
			return null;
		}

		Collection possibleRecursiveMappings = new ArrayList();
		Iterator i = currentSlotMaps.iterator();
		while (i.hasNext()) {
			Instance nextSlotMapping = (Instance) i.next();
			if (nextSlotMapping.getDirectType().equals(_recursiveSlotMappingCls)) {
				Collection recursiveMappingsForNextSlot = nextSlotMapping.getOwnSlotValues(_recursiveMappingSlot);
				if (recursiveMappingsForNextSlot != null) {
					Collection modifiableVersion = new ArrayList(recursiveMappingsForNextSlot);
					modifiableVersion.retainAll(recursiveMappings);
					if (!modifiableVersion.isEmpty()) {
						return (Instance) CollectionUtilities.getFirstItem(modifiableVersion);
					}
				}
			}
		}
		return null;
	}

	private void suggestRecursiveMapping(Instance instanceMapping, Slot recursiveSlot, Cls recursiveCls, Slot sourceSlot, Slot targetSlot) {
		if (recursiveCls.getFrameID().equals(Model.ClsID.THING)) {
			return;
		}
		String[] message = new String[] { "Create a recursive mapping from " + getSourceClsInInstanceMapping(instanceMapping).getBrowserText() + " to " + recursiveCls.getBrowserText() + "?,",
				"(using " + getTargetClsInInstanceMapping(instanceMapping).getBrowserText() + " and " + recursiveSlot.getBrowserText() + ")" };
		boolean createMapping = DisplayWarning.showYesOrNoDialog(message);
		if (createMapping) {
			createRecursiveMapping(instanceMapping, recursiveSlot, recursiveCls, sourceSlot, targetSlot);
		}
	}

	private void createRecursiveMapping(Instance instanceMapping, Slot recursiveSlot, Cls recursiveCls, Slot sourceSlot, Slot targetSlot) {
		Instance recursiveMapping = _recursiveSlotMappingCls.createDirectInstance(null);
		setSourceSlotInSlotMappingToSelf(recursiveMapping);
		setTargetSlotInSlotMapping(recursiveMapping, recursiveSlot);

		instanceMapping.addOwnSlotValue(_slotMapsSlot, recursiveMapping);

		Instance recursiveInstanceMapping = createInstanceMappingInstance(getSourceClsInInstanceMapping(instanceMapping), recursiveCls);
		recursiveInstanceMapping.setOwnSlotValue(_onDemandSlot, new Boolean(true));

		recursiveMapping.addOwnSlotValue(_recursiveMappingSlot, recursiveInstanceMapping);
		createRelatedRenameMapping(recursiveInstanceMapping, sourceSlot, targetSlot);
	}

	private void setSourceSlotInSlotMappingToSelf(Instance mapping) {
		setSourceSlotInSlotMapping(mapping, "<SELF>");
	}

	public Cls getClassForClassToClassMappings() {
		return _instanceMappingCls;
	}

	public Cls getClassForSlotToSlotMappings() {
		return _renamingSlotMappingCls;
	}

	private Instance createInstanceMappingInstance(Cls source, Cls target) {
		Instance instanceMapping = _instanceMappingCls.createDirectInstance(null);
		if (source != null) {
			setSourceClsInInstanceMapping(instanceMapping, source);
			addToMappingsMap(source, instanceMapping);
		}
		if (target != null) {
			setTargetClsInInstanceMapping(instanceMapping, target);
			addToMappingsMap(target, instanceMapping);
		}
		if (source != null && target != null) {
			removeOneSidedMapping(source);
			removeOneSidedMapping(target);
		}
		return instanceMapping;
	}

	public Object[] getMappedClses(Instance mappingInstance) {
		Cls sourceCls = getSourceClsInInstanceMapping(mappingInstance);
		Cls targetCls = getTargetClsInInstanceMapping(mappingInstance);
		return new Object[] { sourceCls, targetCls };
	}

	public Object[] getMappedSlots(Instance mappingInstance) {
		Slot sourceSlot = getSourceSlotInSlotMapping(mappingInstance);
		Slot targetSlot = getTargetSlotInSlotMapping(mappingInstance);
		return new Object[] { sourceSlot, targetSlot };
	}

	private Collection findExistingMappings(Frame source, Frame target) {
		Collection sourceMappings = _mappingsMap.getValues(createKey(source));
		Collection targetMappings = _mappingsMap.getValues(createKey(target));
		if (sourceMappings == null || targetMappings == null) {
			return null;
		}
		sourceMappings.retainAll(targetMappings);
		if (sourceMappings.isEmpty()) {
			return null;
		} else {
			return sourceMappings;
		}
	}

	private void createLexicalMappings(Slot sourceSlot, Slot targetSlot) {}

	private Cls getTargetClsInInstanceMapping(Instance instanceMapping) {
		Object targetClsDescription = instanceMapping.getOwnSlotValue(_targetClassSlot);
		if (targetClsDescription == null) {
			return null;
		}

		Object targetClsName = ((Instance) targetClsDescription).getOwnSlotValue(_nameSlot);
		if (targetClsName == null) {
			return null;
		}

		return _targetKb.getCls((String) targetClsName);
	}

	private Cls getSourceClsInInstanceMapping(Instance instanceMapping) {
		Object sourceClsDescription = instanceMapping.getOwnSlotValue(_sourceClassSlot);
		if (sourceClsDescription == null) {
			return null;
		}

		Object sourceClsName = ((Instance) sourceClsDescription).getOwnSlotValue(_nameSlot);
		if (sourceClsName == null) {
			return null;
		}

		return _sourceKb.getCls((String) sourceClsName);
	}

	private void createRenameMappings(Collection mappings, Slot source, Slot target) {
		if (mappings != null) {
			Iterator i = mappings.iterator();
			while (i.hasNext()) {
				Instance nextInstanceMapping = (Instance) i.next();
				createRelatedRenameMapping(nextInstanceMapping, source, target);
			}
		}
	}

	private void createRelatedRenameMapping(Instance instanceMapping, Slot source, Slot target) {
		Instance existingMapping = existingRenameMappingForInstanceMapping(instanceMapping, source, target);
		if (existingMapping != null) {
			return;
		}

		Instance renamingMapping = _renamingSlotMappingCls.createDirectInstance(null);
		setSourceSlotInSlotMapping(renamingMapping, source);
		setTargetSlotInSlotMapping(renamingMapping, target);

		instanceMapping.addOwnSlotValue(_slotMapsSlot, renamingMapping);
	}

	private Instance existingRenameMappingForInstanceMapping(Instance instanceMapping, Slot source, Slot target) {
		Collection slotMappings = instanceMapping.getDirectOwnSlotValues(_slotMapsSlot);
		if (slotMappings == null || slotMappings.isEmpty()) {
			return null;
		}

		Iterator i = slotMappings.iterator();
		while (i.hasNext()) {
			Instance nextMapping = (Instance) i.next();
			if (nextMapping.getDirectType().equals(_renamingSlotMappingCls)) {
				Slot nextMappingSource = getSourceSlotInSlotMapping(nextMapping);
				Slot nextMappingTarget = getTargetSlotInSlotMapping(nextMapping);
				if (source.equals(nextMappingSource) && target.equals(nextMappingTarget)) {
					return nextMapping;
				}
			}
		}
		return null;
	}

	private void setSourceSlotInSlotMapping(Instance slotMapping, Slot sourceSlot) {
		Instance sourceSlotDescriptionInstance = _sourceSlotDescriptionCls.createDirectInstance(null);
		sourceSlotDescriptionInstance.setOwnSlotValue(_nameSlot, sourceSlot.getName());
		slotMapping.setOwnSlotValue(_slotMappingSourceSlot, sourceSlotDescriptionInstance);
	}

	private boolean isSelfMapping(Instance slotMapping) {
		Object sourceSlotDescription = slotMapping.getOwnSlotValue(_slotMappingSourceSlot);
		if (sourceSlotDescription == null) {
			return false;
		}

		Object sourceSlotName = ((Instance) sourceSlotDescription).getOwnSlotValue(_nameSlot);
		if (sourceSlotName == null) {
			return false;
		} else {
			return sourceSlotName.equals("<SELF>");
		}
	}

	private Slot getSourceSlotInSlotMapping(Instance slotMapping) {
		Object sourceSlotDescription = slotMapping.getOwnSlotValue(_slotMappingSourceSlot);
		if (sourceSlotDescription == null) {
			return null;
		}

		Object sourceSlotName = ((Instance) sourceSlotDescription).getOwnSlotValue(_nameSlot);
		if (sourceSlotName == null) {
			return null;
		} else {
			return _sourceKb.getSlot((String) sourceSlotName);
		}
	}

	private Slot getTargetSlotInSlotMapping(Instance slotMapping) {
		Object targetSlotDescription = slotMapping.getOwnSlotValue(_slotMappingTargetSlot);
		if (targetSlotDescription == null) {
			return null;
		}

		Object targetSlotName = ((Instance) targetSlotDescription).getOwnSlotValue(_nameSlot);
		if (targetSlotName == null) {
			return null;
		} else {
			return _targetKb.getSlot((String) targetSlotName);
		}
	}

	// used just with "<SELF>"
	private void setSourceSlotInSlotMapping(Instance slotMapping, String str) {
		Instance sourceSlotDescriptionInstance = _sourceSlotDescriptionCls.createDirectInstance(null);
		sourceSlotDescriptionInstance.setOwnSlotValue(_nameSlot, str);
		slotMapping.setOwnSlotValue(_slotMappingSourceSlot, sourceSlotDescriptionInstance);
	}

	private void setTargetSlotInSlotMapping(Instance slotMapping, Slot targetSlot) {
		Instance targetSlotDescriptionInstance = _targetSlotDescriptionCls.createDirectInstance(null);
		targetSlotDescriptionInstance.setOwnSlotValue(_nameSlot, targetSlot.getName());
		slotMapping.setOwnSlotValue(_slotMappingTargetSlot, targetSlotDescriptionInstance);
	}

	private Collection<Instance> findLocalMappingsFromClses(Collection<Cls> clses, Slot otherSlot, KnowledgeBase otherClsKb) {
		Collection<Instance> result = new ArrayList<Instance>();
		if (clses == null) {
			return result;
		}

//		Slot otherClsSlot = (otherClsKb == _sourceKb ? _sourceClassSlot : _targetClassSlot);
		boolean processingSource = (otherClsKb == _sourceKb);

		Iterator i = clses.iterator();
		while (i.hasNext()) {
			Cls nextCls = (Cls) i.next();
			Collection<Instance> nextMappings = _mappingsMap.getValues(createKey(nextCls));
			if (nextMappings != null) {
				Iterator j = nextMappings.iterator();
				while (j.hasNext()) {
					Instance nextMapping = (Instance) j.next();
					Cls otherCls = processingSource ? getSourceClsInInstanceMapping(nextMapping) : getTargetClsInInstanceMapping(nextMapping);
					if (otherCls.hasTemplateSlot(otherSlot)) {
						result.add(nextMapping);
					}
				}
			}
		}
		return result;
	}

	public Project getProject() {
		return _mappingProject;
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
		return "Store mappings using the Domain_PSM ontology";
	}

	public void invokePlugin() {
	// TODO Auto-generated method stub

	}

	public boolean showMappingInstances() {
		return true;
	}

	public String getTabName() {
		return "Domain-PSM Mapping";
	}

	public MappingStoragePluginConfigurationPanel getConfigurationPanel() {
		MappingDomainPSMConfigurationPanel configPanel = new MappingDomainPSMConfigurationPanel();
		return configPanel;
	}

	public String getMappingFileName(MappingStoragePluginConfigurationPanel panel) {
		return panel.getMappingFileName();
	}

	/**
	 * Given the Cls objects involved in a mapping, this function removes the mapping from the mapping ontology, updates Prompt's knowledge of the mappings, and updates the UI.
	 */	
	public void removeClassToClassMapping(Cls cls1, Cls cls2) {
	// TODO : must remove mapping between cls1 and cls2
	}

}
