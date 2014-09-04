/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.operation.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class TraversalDirectivesKnowledgeBase {
	private Cls _traversalDirectiveCls = null;
	private Slot _starterConceptSlot = null;
	private Cls _slotDirectiveCls = null;
	private Slot _slotSlot = null;
	private Slot _depthSlot = null;
	private Slot _slotDirectivesSlot = null;
	private Slot _copyInstancesSlot = null;
	private Slot _copySubclassesSlot = null;
	private Slot _copySuperclassesSlot = null;
	private Slot _numberOfLevelsSlot = null;
	private Slot _copyEverythingRelatedSlot = null;
	
	private KnowledgeBase _traversalDirectivesKB = null;
	private Project _project = null;
	
	private static KnowledgeBase _sourceKb = null;

	private boolean _executingSavedDirectives = false;
	
	public TraversalDirectivesKnowledgeBase () {
		_sourceKb = ProjectsAndKnowledgeBases.getKnowledgeBase(ProjectsAndKnowledgeBases.EXTRACT_SOURCE_INDEX);
		_traversalDirectivesKB = createNewKb ();
		initializeVariables ();
	}
	
	public TraversalDirectivesKnowledgeBase (Project project) {
		_sourceKb = ProjectsAndKnowledgeBases.getKnowledgeBase(ProjectsAndKnowledgeBases.EXTRACT_SOURCE_INDEX);
		_project = project;
		_traversalDirectivesKB = project.getKnowledgeBase();
		initializeVariables ();
	}
	
	public TraversalDirectivesKnowledgeBase (Project project, KnowledgeBase sourceKb) {
		_project = project;
		_traversalDirectivesKB = project.getKnowledgeBase();
		_sourceKb = sourceKb;
		initializeVariables ();
	}
	
	public void executeSavedTraversalDirectives () {
		_executingSavedDirectives = true;
		Collection directives = _traversalDirectiveCls.getDirectInstances();
		Iterator i = directives.iterator();
		while (i.hasNext()) {
			Instance nextDirective = (Instance)i.next();
			TraversalDirective td = createTraversalDirective  (nextDirective);
			(KeepFrameOperation.createOperation (getStarterConcept (nextDirective), td, false)).performOperation();
		}
		_executingSavedDirectives = false;
	}
	
	private TraversalDirective createTraversalDirective (Instance directive) {
		Frame starterConcept = getStarterConcept (directive); 
		TraversalDirective td = new TraversalDirective (starterConcept, true);
		
		Object copyEverythingRelated = directive.getOwnSlotValue(_copyEverythingRelatedSlot);
		if (copyEverythingRelated != null)
			td.setCopyEverythingRelated(((Boolean)copyEverythingRelated).booleanValue());
		
		Object copyInstances = directive.getOwnSlotValue(_copyInstancesSlot);
		if (copyInstances != null)
			td.setCopyInstances(((Boolean)copyInstances).booleanValue());
		
		Object copySubclasses = directive.getOwnSlotValue(_copySubclassesSlot);
		if (copySubclasses != null)
			td.setCopySubclasses(((Boolean)copySubclasses).booleanValue());
			
		Object copySuperclasses = directive.getOwnSlotValue(_copySuperclassesSlot);
		if (copySuperclasses != null)
			td.setCopySuperclasses(((Boolean)copySuperclasses).booleanValue());
			
		Object numberOfLevels = directive.getOwnSlotValue(_numberOfLevelsSlot);
		if (numberOfLevels != null)
			td.setNumberOfLevels(((Integer)numberOfLevels).intValue());
			
		setSlotDirectives (directive, td);
		return td;
	}
	
	private void setSlotDirectives (Instance directive, TraversalDirective td) {
		Collection slotDirectives = directive.getOwnSlotValues(_slotDirectivesSlot);
		Iterator i = slotDirectives.iterator();
		while (i.hasNext()) {
			Instance next = (Instance)i.next();
			Slot nextSlot = _sourceKb.getSlot((String)next.getOwnSlotValue (_slotSlot));
			int nextDepth = ((Integer)next.getOwnSlotValue (_depthSlot)).intValue();
			td.setLevelForSlot(nextSlot, nextDepth);
		}
	}

	public Frame getStarterConcept (Instance directive) {
		return _sourceKb.getFrame((String)directive.getOwnSlotValue(_starterConceptSlot));
	}
	
	public void setStarterConcept (Instance directive, String value) {
		directive.setOwnSlotValue(_starterConceptSlot, value);
	}
	
	public Collection getSlotDirectives (Instance directive) {
		return directive.getOwnSlotValues (_slotDirectivesSlot);
	}
	
	public Instance addTDtoKB (TraversalDirective td) {
		if (_executingSavedDirectives) return null;
		if (td.getStarterConcept() == null) return null;
		Instance tdInstance = _traversalDirectivesKB.createInstance(null, _traversalDirectiveCls);
		tdInstance.setEditable(false);

		tdInstance.setOwnSlotValue (_starterConceptSlot, td.getStarterConcept().getName());
		tdInstance.setOwnSlotValue(_copyInstancesSlot, new Boolean (td.copyInstances()));
		tdInstance.setOwnSlotValue(_copySubclassesSlot, new Boolean (td.copySubclasses()));
		tdInstance.setOwnSlotValue(_copySuperclassesSlot, new Boolean (td.copySuperclasses()));

		if (td.numberOfLevels() != TraversalDirective.NO_LEVEL_SET) {
			tdInstance.setOwnSlotValue(_numberOfLevelsSlot, new Integer (td.numberOfLevels()));
			return tdInstance;
		}
		
		if (td.copyEverythingRelated()) {
			tdInstance.setOwnSlotValue(_copyEverythingRelatedSlot, new Boolean (true));
			return tdInstance;
		}
		
		Collection slots = td.getSlotsInDirective();
		if (slots == null || slots.isEmpty()) return tdInstance;
		
		Iterator i = slots.iterator();
		while (i.hasNext()) {
			Slot nextSlot = (Slot)i.next();
			String nextSlotName = nextSlot.getName();
			Instance nextSlotDirective = _traversalDirectivesKB.createInstance(null, _slotDirectiveCls); 
			nextSlotDirective.setOwnSlotValue(_slotSlot, nextSlotName);
			nextSlotDirective.setOwnSlotValue(_depthSlot, new Integer (td.getLevelForSlot(nextSlot)));
			nextSlotDirective.setEditable(false);
			tdInstance.addOwnSlotValue(_slotDirectivesSlot, nextSlotDirective);
		}
		return tdInstance;
	}
	
	private KnowledgeBase createNewKb () {
		_project = Util.createNewClipsProject("-views");
		Collection errors = new ArrayList();
Log.getLogger().info("url: " + Util.getProjectURI ("views.pprj"));
		_project.includeProject(Util.getProjectURI ("views.pprj"), errors);
		Util.displayErrors(errors);
		
		_project.mergeIncludedProjects();
		
		KnowledgeBase kb = _project.getKnowledgeBase();
		
//		createTDOntology (kb);
		return kb;
	}
	
	
	public void save () {
		Collection errors = new ArrayList();
		_project.save(errors);
		Util.displayErrors(errors);
	}
	
	private static final String TRAVERSAL_DIRECTIVE_CLS_NAME = "TraversalDirective";
	private static final String STARTER_CONCEPT_SLOT_NAME = "starter_concept";
	
	private static final String SLOT_DIRECTIVE_SLOT_NAME = "slot_directives";
	private static final String SLOT_DIRECTIVE_CLS_NAME = "SlotDirective";
	private static final String SLOT_SLOT_NAME = "slot";
	private static final String DEPTH_SLOT_NAME = "depth";

	private static final String COPY_INSTANCES_SLOT_NAME = "instances";
	private static final String COPY_SUBCLASSES_SLOT_NAME = "subclasses";
	private static final String COPY_SUPERCLASSES_SLOT_NAME = "superclasses";
	private static final String NUMBER_OF_LEVELS_SLOT_NAME = "number_of_levels";
	private static final String COPY_EVERYTHING_RELATED_SLOT_NAME = "everything_related";

	private void initializeVariables () {
		_traversalDirectiveCls = _traversalDirectivesKB.getCls(TRAVERSAL_DIRECTIVE_CLS_NAME);
		_starterConceptSlot = _traversalDirectivesKB.getSlot(STARTER_CONCEPT_SLOT_NAME);

		_slotDirectivesSlot = _traversalDirectivesKB.getSlot(SLOT_DIRECTIVE_SLOT_NAME);
		_slotDirectiveCls = _traversalDirectivesKB.getCls(SLOT_DIRECTIVE_CLS_NAME);
		_slotSlot = _traversalDirectivesKB.getSlot(SLOT_SLOT_NAME);
		_depthSlot = _traversalDirectivesKB.getSlot(DEPTH_SLOT_NAME);

		_copyInstancesSlot = _traversalDirectivesKB.getSlot(COPY_INSTANCES_SLOT_NAME);
		_copySubclassesSlot = _traversalDirectivesKB.getSlot(COPY_SUBCLASSES_SLOT_NAME);
		_copySuperclassesSlot = _traversalDirectivesKB.getSlot(COPY_SUPERCLASSES_SLOT_NAME);
		_numberOfLevelsSlot = _traversalDirectivesKB.getSlot(NUMBER_OF_LEVELS_SLOT_NAME);
		_copyEverythingRelatedSlot = _traversalDirectivesKB.getSlot(COPY_EVERYTHING_RELATED_SLOT_NAME);
		setInstancesToNotEditable ();
	}
	
	private void setInstancesToNotEditable () {
		Collection instances = _traversalDirectiveCls.getInstances();
		instances.addAll(_slotDirectiveCls.getInstances());
		Iterator i = instances.iterator();
		while (i.hasNext()) {
			Instance next = (Instance)i.next();
			next.setEditable(false); 
		}
	}
	
	public boolean isSlotDirective (Frame frame) {
		return ((Instance)frame).hasDirectType(_slotDirectiveCls);
	}
	
	public String getSlotDirectiveBrowserText (Instance instance) {
		String slot = (String)instance.getOwnSlotValue(_slotSlot);
		int depth = ((Integer)instance.getOwnSlotValue(_depthSlot)).intValue();
		return "" + slot + ":" + ((depth == TraversalDirective.INFINITY) ? "no limit" : ("" + depth));
	}
	
//	private void createTDOntology (KnowledgeBase kb) {
//		_traversalDirectiveCls = kb.createCls(TRAVERSAL_DIRECTIVE_CLS_NAME, CollectionUtilities.createCollection(kb.getRootCls()));
//		_starterConceptSlot = kb.createSlot(STARTER_CONCEPT_SLOT_NAME); //string single is ok
//		_traversalDirectiveCls.addDirectTemplateSlot(_starterConceptSlot);
//	
//		_slotDirectiveCls = kb.createCls(SLOT_DIRECTIVE_CLS_NAME, CollectionUtilities.createCollection(kb.getRootCls()));
//		_slotSlot = kb.createSlot(SLOT_SLOT_NAME); // string single
//		_depthSlot = kb.createSlot(DEPTH_SLOT_NAME); //single
//		_depthSlot.setValueType(ValueType.INTEGER);
//		
//		_slotDirectiveCls.addDirectTemplateSlot(_slotSlot);
//		_slotDirectiveCls.addDirectTemplateSlot(_depthSlot);
//
//		_slotDirectivesSlot = kb.createSlot(SLOT_DIRECTIVE_SLOT_NAME);
//		_slotDirectivesSlot.setValueType(ValueType.INSTANCE);
//		_slotDirectivesSlot.setAllowedClses(CollectionUtilities.createCollection(_slotDirectiveCls));
//		_slotDirectivesSlot.setAllowsMultipleValues(true);
//		_traversalDirectiveCls.addDirectTemplateSlot(_slotDirectivesSlot);
//		
//		_copyInstancesSlot = kb.createSlot(COPY_INSTANCES_SLOT_NAME);
//		_copyInstancesSlot.setValueType(ValueType.BOOLEAN);
//		_traversalDirectiveCls.addDirectTemplateSlot(_copyInstancesSlot);
//		
//		_copySubclassesSlot = kb.createSlot(COPY_SUBCLASSES_SLOT_NAME);
//		_copySubclassesSlot.setValueType(ValueType.BOOLEAN);
//		_traversalDirectiveCls.addDirectTemplateSlot(_copySubclassesSlot);
//		
//		_copySuperclassesSlot = kb.createSlot(COPY_SUPERCLASSES_SLOT_NAME);
//		_copySuperclassesSlot.setValueType(ValueType.BOOLEAN);
//		_traversalDirectiveCls.addDirectTemplateSlot(_copySuperclassesSlot);
//		
//		_numberOfLevelsSlot = kb.createSlot(NUMBER_OF_LEVELS_SLOT_NAME);
//		_numberOfLevelsSlot.setValueType(ValueType.INTEGER);
//		_traversalDirectiveCls.addDirectTemplateSlot(_numberOfLevelsSlot);
//		
//		_copyEverythingRelatedSlot = kb.createSlot(COPY_EVERYTHING_RELATED_SLOT_NAME);
//		_copyEverythingRelatedSlot.setValueType(ValueType.BOOLEAN);
//		_traversalDirectiveCls.addDirectTemplateSlot(_copyEverythingRelatedSlot);
//		
//	}

	public KnowledgeBase getKnowledgeBase() {
		return _traversalDirectivesKB;
	}

	public Project getProject() {
		return _project;
	}
	
	public Cls getTraversalDirectiveCls () {
		return _traversalDirectiveCls;
	}

	public void updateSlotLevel(Instance instance, Slot slot, int level) {
		Log.getLogger().severe("Not implemented");		
	}

	public void setCopySubclasses(Instance instance, boolean value) {
		instance.setOwnSlotValue(_copySubclassesSlot, new Boolean (value));
		
	}

	public void setCopySuperclasses(Instance instance, boolean value) {
		instance.setOwnSlotValue(_copySuperclassesSlot, new Boolean (value));
		
	}

	public void setNumberOfLevels(Instance instance, int value) {
		instance.setOwnSlotValue(_numberOfLevelsSlot, new Integer (value));
	}

	public void setCopyInstances(Instance instance, boolean value) {
		instance.setOwnSlotValue(_copyInstancesSlot, new Boolean (value));
	}

	public void setCopyEverythingRelated(Instance instance, boolean value) {
		instance.setOwnSlotValue(_copyEverythingRelatedSlot, new Boolean (value));
	}

	public void setSlotInSlotDirective(Instance slotDirective, String slotName) {
		slotDirective.setOwnSlotValue(_slotSlot, slotName);
	}

	public Slot getSlotInSlotDirective(Instance slotDirective) {
		return _sourceKb.getSlot ((String)slotDirective.getOwnSlotValue(_slotSlot));
	}

	public void removeTraversalDirective(Instance td) {
		Collection slotDirectives = td.getOwnSlotValues(_slotDirectivesSlot);
		_traversalDirectivesKB.deleteFrame(td);
		Iterator i = slotDirectives.iterator();
		while (i.hasNext())
			_traversalDirectivesKB.deleteFrame((Frame)i.next());
	}

	public void removeSlotDirective(Instance slotDirective, Instance td) {
		_traversalDirectivesKB.deleteFrame(slotDirective);
	}


}
