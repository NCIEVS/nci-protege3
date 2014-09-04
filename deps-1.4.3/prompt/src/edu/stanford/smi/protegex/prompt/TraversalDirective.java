/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt;

import java.util.*;

import edu.stanford.smi.protege.model.*;


// a collection of slot-level pairs

public class TraversalDirective {
	private HashMap _slotDirectives = new HashMap (101);
	
	public static final int NO_LEVEL_SET = -1;
	public static final int INFINITY = Integer.MAX_VALUE;
	public static final Integer _infinity = new Integer (INFINITY);
	
	public static final TraversalDirectivesKnowledgeBase _tdKB = PromptTab.getTraversalDirectivesKb ();
	
	protected static Collection _visited = new ArrayList ();
//	protected boolean _copyInstances = false;
//	protected boolean _copySubclasses = false;
//	
	
	protected boolean _copyEverythingRelated = false;
	protected boolean _lastLevel = false; // if we are extracting several levels around an argument class, we want only top-level
											 // slot information for these classes (i.e., do not look at facets at class or own slot values

	protected static TraversalDirective _nullDirective = new TraversalDirective (null, false);
	protected int _numberOfLevels = NO_LEVEL_SET;
	protected Frame _starterConcept = null;
	
	protected static KnowledgeBase _kb = ProjectsAndKnowledgeBases.getKnowledgeBase(ProjectsAndKnowledgeBases.EXTRACT_SOURCE_INDEX);
	protected static Slot _directSubclassesSlot = _kb.getSlot(Model.Slot.DIRECT_SUBCLASSES);
	protected static Slot _directSuperclassesSlot = _kb.getSlot(Model.Slot.DIRECT_SUPERCLASSES);
	protected static Slot _directInstancesSlot = _kb.getSlot(Model.Slot.DIRECT_INSTANCES);
	
	protected Instance _instanceInKb = null;	

	public static TraversalDirective createTestDirective (Frame f) {
		TraversalDirective td = new TraversalDirective (f, true);
		KnowledgeBase kb = ProjectsAndKnowledgeBases.getKnowledgeBase(ProjectsAndKnowledgeBases.EXTRACT_SOURCE_INDEX);
		Slot responsibleFor = kb.getSlot("responsible_for");
		Slot sections = kb.getSlot("sections");
		td.setLevelForSlot(responsibleFor, 3);
		td.setLevelForSlot(sections, 2);
//		td.setCopySubclasses(true);
		td.setCopyInstances(true);
//		td.setCopyEverythingRelated(true);
//		td.setNumberOfLevels(2);
		return td;
	}
	
	public TraversalDirective(Frame starter, boolean firstOne) {
 
		_starterConcept = starter;
		if (firstOne)
			_visited.clear();
		if (starter != null) 
			_visited.add (starter);
			
		if (firstOne && PromptTab.extracting())
			_instanceInKb = _tdKB.addTDtoKB (this);
	}

	public TraversalDirective  (Frame starter, HashMap slotLevels, boolean firstOne) {
		_starterConcept = starter;
		if (starter != null && !nullDirective()) {
			_visited.add (starter);
		}
		if (slotLevels != null)
			_slotDirectives = slotLevels;
		if (firstOne && PromptTab.extracting())
			_instanceInKb = _tdKB.addTDtoKB (this);
	}	

	public TraversalDirective  (Frame starter, boolean copyInstances, boolean copySubclasses) {
		_starterConcept = starter;
		setCopySubclasses (copySubclasses);
		setCopyInstances (copyInstances);
		if (starter != null && !nullDirective()) {
			_visited.add (starter);
		}
	}
	
	public void setLevelForSlot (Slot slot, int level) {
		if (level >= 1) {
			_slotDirectives.put(slot, new Integer (level));
		}
		if (_instanceInKb != null)
			_tdKB.updateSlotLevel (_instanceInKb, slot, level);
	}
	
	public int getLevelForSlot (Slot slot) {
		Object level = _slotDirectives.get(slot);
		if (level == null) 
			return NO_LEVEL_SET;
		else
			return ((Integer)level).intValue();
	}
	
	public boolean copyInstances () {
		return copyInstancesSubclassesOrSuperclasses (_directInstancesSlot);	
	}
	
	public boolean copySuperclasses () {
		return copyInstancesSubclassesOrSuperclasses (_directSuperclassesSlot);	
	}
	
	private boolean copyInstancesSubclassesOrSuperclasses (Slot slot) {
		if (_starterConcept == null || !(_starterConcept instanceof Cls)) return false;
		Object value = _slotDirectives.get(slot);
		return (value != null || _copyEverythingRelated || _numberOfLevels >=1);
	}
	
	public boolean copySubclasses (){
		return copyInstancesSubclassesOrSuperclasses (_directSubclassesSlot);	
	}
	
	public int numberOfLevels () {
		return _numberOfLevels;
	}
	
	public void setCopySubclasses (boolean value) {
		if (value) 
			_slotDirectives.put(_directSubclassesSlot, _infinity);
		if (_instanceInKb != null)
			_tdKB.setCopySubclasses (_instanceInKb, value);
	}
	
	public void setCopySubslots (boolean value) {
		if (value)
			setLevelForSlot(_kb.getSlot(Model.Slot.DIRECT_SUBSLOTS), INFINITY);
	}
	
	public void setCopySuperclasses (boolean value) {
		if (value) 
			_slotDirectives.put(_directSuperclassesSlot, _infinity);
		if (_instanceInKb != null)
			_tdKB.setCopySuperclasses (_instanceInKb, value);
	}
	
	public void setNumberOfLevels (int value) {
		_numberOfLevels = value;
		if (_instanceInKb != null)
			_tdKB.setNumberOfLevels (_instanceInKb, value);
	}
	
	public void setCopyInstances (boolean value) {
		if (value)
			_slotDirectives.put(_directInstancesSlot, _infinity);
		if (_instanceInKb != null)
			_tdKB.setCopyInstances (_instanceInKb, value);
	}
	
	public TraversalDirective sameDirective (Frame starter) {
//Log.trace ("_visited: = " + _visited, this, "sameDirective");
//Log.trace ("starter: = " + starter, this, "sameDirective");
//Log.trace ("_visited.contains(starter): = " + _visited.contains(starter), this, "sameDirective");

		if (_visited.contains(starter)) return _nullDirective;
		TraversalDirective newTD = new TraversalDirective (starter, false);
		newTD._lastLevel = _lastLevel;
		newTD._slotDirectives = _slotDirectives;
		newTD.setCopyEverythingRelated (_copyEverythingRelated);
		newTD.setNumberOfLevels (_numberOfLevels);
		return newTD;
	}
	
	
	public TraversalDirective nextDirective (Frame starter){
//		TraversalDirective newTD = new TraversalDirective (_copyInstances, _copySubclasses);
		if (_visited.contains(starter)) return _nullDirective;
		TraversalDirective newTD = new TraversalDirective (starter, false);
		newTD.setCopyEverythingRelated(_copyEverythingRelated);

		if (_numberOfLevels == 1) {
			newTD.setLastLevel(true);
			newTD.setNumberOfLevels (NO_LEVEL_SET);	
		}
		else if (_lastLevel) {
			newTD.setLastLevel(true);
//			Log.trace ("Requesting next directive for the last level directive", this, "nextDirective");
		} else
			newTD.setLastLevel(false);

		if (_numberOfLevels != NO_LEVEL_SET)
			newTD.setNumberOfLevels(_numberOfLevels - 1);
			
		createNewSlotDirectives (newTD);
			
		if (newTD.nullDirective()) return _nullDirective;
		return newTD;
	}
	
	public Collection getSlotsInDirective () {
		if (_copyEverythingRelated && _starterConcept != null) return _kb.getSlots();
		if (_numberOfLevels > 0) return _kb.getSlots();
		return _slotDirectives.keySet();
	}
	
	public  boolean nullDirective () {
		return (!_copyEverythingRelated && _numberOfLevels == NO_LEVEL_SET && noSlotDirectives());
//		return (!_copyInstances && !_copySubclasses && !_copyEverythingRelated && _numberOfLevels == NO_LEVEL_SET && noSlotDirectives());
	}
	
	private boolean noSlotDirectives () {
		return _slotDirectives.keySet().isEmpty();
	}
	
	private void createNewSlotDirectives (TraversalDirective newTD) {
		Set keys = _slotDirectives.keySet();
		if (keys.isEmpty()) return;
		
		Iterator i = keys.iterator();
		while (i.hasNext()) {
			Object nextKey = i.next();
			Integer nextValue = (Integer)_slotDirectives.get(nextKey);
			int nextValueInt = nextValue.intValue();
			if (nextValueInt > 1)
				newTD.setLevelForSlot((Slot)nextKey, (nextValueInt == INFINITY) ? INFINITY : (nextValueInt - 1));
		}
	}
	
	public boolean copyEverythingRelated () {
		return _copyEverythingRelated;
	}

//	if we are extracting several levels around an argument class, we want only top-level
//											  // slot information for these classes (i.e., do not look at facets at class or own slot values
	public boolean lastLevel () {
		return _lastLevel;
	}
	
	public static TraversalDirective createCopyLevelOnlyDirective (Frame starter) {
		TraversalDirective newTD = new TraversalDirective (starter, false);
		newTD.setLastLevel(true);
		return newTD;
	}
	
	// create a new instance with same values for copyInstances and copySubclasses
//	public static TraversalDirective sameInstanceSameSubclassesValues (Frame starter, TraversalDirective old) {
//		TraversalDirective newTD = new TraversalDirective (starter, old.copyInstances(), old.copySubclasses());
//		return newTD;
//	}
//	
	public void setCopyEverythingRelated (boolean value) {
		_copyEverythingRelated = value;
		if (_instanceInKb != null)
			_tdKB.setCopyEverythingRelated (_instanceInKb, value);
	}

	public void setLastLevel (boolean value) {
		_lastLevel = value;
	}
	
	public static TraversalDirective getNullDirective () {
		return _nullDirective;
	}
	
	public String toString () {
		String result = "";
		
//		result = "copyInstances = " + copyInstances() + ", copySubclasses = " + copySubclasses() + 
//				", numberOfLevels = " + _numberOfLevels + ", copyEverythingRelated = " + _copyEverythingRelated + slotDirectivesToString();
		
//		result = "starter = " + _starterConcept + ", levels  = " + _numberOfLevels + "copyInstances = " + copyInstances() + ", copySubclasses = " + copySubclasses() + ", copyEverythingRelated = " + _copyEverythingRelated + slotDirectivesToString();
		result = "starter = " + _starterConcept + "; slot directives: " + slotDirectivesToString();
		return result;
	}
	
	public String printDirectives() {
		return slotDirectivesToString();
	}

	private String slotDirectivesToString () {
		String result = "";
		Set keys = _slotDirectives.keySet();
		if (keys.isEmpty()) return result;
		
		Iterator i = keys.iterator();
		while (i.hasNext()) {
			Slot nextSlot = (Slot)i.next();
			Object nextLevel = _slotDirectives.get(nextSlot);
			String nextLevelString = (nextLevel == null || nextLevel == _infinity) ? "inf" : nextLevel.toString();
			result += ", " + nextSlot + ":" + nextLevelString;
		}
		return result;
	}
	
	public Frame getStarterConcept () {
		return _starterConcept;
	}

         
}
