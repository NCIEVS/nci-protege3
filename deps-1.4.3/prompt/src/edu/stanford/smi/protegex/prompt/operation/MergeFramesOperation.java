/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 *                 Kyle Bruck kbruck@stanford.edu
 */


package edu.stanford.smi.protegex.prompt.operation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameSlotCombination;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.prompt.Mappings;
import edu.stanford.smi.protegex.prompt.Preferences;
import edu.stanford.smi.protegex.prompt.ProjectsAndKnowledgeBases;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.actionLists.Action;
import edu.stanford.smi.protegex.prompt.actionLists.SuggestionsAndConflicts;
import edu.stanford.smi.protegex.prompt.analysis.Analysis;
import edu.stanford.smi.protegex.prompt.conflict.Conflict;
import edu.stanford.smi.protegex.prompt.explanation.ApproximateMatch;
import edu.stanford.smi.protegex.prompt.explanation.Explanation;
import edu.stanford.smi.protegex.prompt.explanation.IdenticalNames;
import edu.stanford.smi.protegex.prompt.explanation.SameRole;
import edu.stanford.smi.protegex.prompt.ui.DisplayWarning;
import edu.stanford.smi.protegex.prompt.ui.Warning;
import edu.stanford.smi.protegex.prompt.util.CompareNames;
import edu.stanford.smi.protegex.prompt.util.ReplaceReferences;
import edu.stanford.smi.protegex.prompt.util.Util;

public class MergeFramesOperation extends Operation {
	static final int MERGE_OPERATION_ARITY  = 2;
	static final String TEMP_NAME = "_TEMPORARY";
	static final int REWARD_POINTS = 1;
	
//	static private GetValueWidget [] _argumentWidgets;
	
	protected Frame _preferredFrame = null;
	private boolean _duplicateNames = false;
	protected Object [] _originalArgs;
	private boolean _performAdditionalMergeWhenDone = false;
	private Frame _additionalMergeToPerform = null;
	private static final String _selectNamePrompt = "Choose the name for the merged class";
	private static final String _chooseOwnSlotTitle = "CONFLICT: different own slot values";
	private static final String _chooseNameTitle = "Choose frame name";
	
	private static final String _selectSlotAttachmentPrompt = "Choose facets for the slot at class ";
	private static final String _selectSlotAttachmentTitle = "Choose facets";
	
	private String _newName = "";
	
	private static final String GENERATE_NEW_NAME = "";
	
	protected boolean _deepCopy = false;
	
	public MergeFramesOperation () {
		super (MERGE_OPERATION_ARITY);
		initialize (null, null);
	}
	
	public MergeFramesOperation (Frame f1, Frame f2) {
		super (MERGE_OPERATION_ARITY);
		initialize (f1, f2);
	}
	
	public MergeFramesOperation (Frame f1, Frame f2, Explanation exp) {
		super (MERGE_OPERATION_ARITY, exp);
		initialize (f1, f2); 
	}
	
	public static MergeFramesOperation createOperation (Frame f1, Frame f2, int comparisonResult) {
		MergeFramesOperation result;
		/*
		 Collection operationsWithF1 = Mappings.getCurrentOperations(f1);
		 Collection operationsWithF2 = Mappings.getCurrentOperations(f2);
		 
		 
		 if (operationsWithF1 != null && operationsWithF2 != null) {
		 Collection operations = new ArrayList (operationsWithF1);
		 operations.retainAll(operationsWithF2);
		 if (operations != null && operations.size() >0) {
		 Iterator i = operations.iterator();
		 while (i.hasNext()) {
		 Action next = (Action)i.next();
		 if (next instanceof MergeFramesOperation)
		 return (MergeFramesOperation) next;
		 }
		 }
		 }
		 */
		if (comparisonResult == CompareNames.EQUAL)
			result = selectMergeOperation (f1, f2, new IdenticalNames ());
		else if (comparisonResult == CompareNames.APPROXIMATE_MATCH)
			result = selectMergeOperation (f1, f2, new ApproximateMatch (f1, f2));
		else
			result = selectMergeOperation (f1, f2, null);
		return result;
	}
	
	public void performOperation () {
		super.performOperation();
		if (_performAdditionalMergeWhenDone) {
			selectMergeOperation (_additionalMergeToPerform, _newFrame, null).performOperation();
		}
	}
	
	public static MergeFramesOperation selectMergeOperation (Frame f1, Frame f2, Explanation exp) {
		MergeFramesOperation result;
		if (f1 instanceof Cls)
			result = new MergeClsesOperation (f1, f2, exp);
		else if (f1 instanceof Slot)
			result = new MergeSlotsOperation (f1, f2, exp);
		else if (f1 instanceof Instance)
			result = new MergeInstancesOperation (f1, f2, exp);
		else
			result = new MergeFramesOperation (f1, f2, exp);
		return result;
	}
	
	private void initialize (Frame f1, Frame f2) {
		String mergeOrMap = Util.mergeOrMapString ();
		_name = mergeOrMap + "-frames";
		_shortName = mergeOrMap;
		_connectorString = "and";
		_prettyName = _name;
		setArgs (f1, f2);
//		setArgumentWidgets ();
	}
	
	private void setArgs (Frame f1, Frame f2) {
		if (f1 == null || f2 == null) {
			_args.setArg (0, f1);
			_args.setArg (1, f2);
			return;
		}
//		KnowledgeBaseInMerging [] kbs = PromptTab.getSourceKnowledgeBasesInMerging();
		KnowledgeBase kb1 = f1.getKnowledgeBase();
		
		if (kb1.equals (ProjectsAndKnowledgeBases.getTargetKnowledgeBase())) {
			kb1 = Util.getSingleSourceKb (Mappings.getSources(f1));
		}
		
		if (kb1 != null && kb1.equals (ProjectsAndKnowledgeBases.getMappingSource1())) {
			_args.setArg (0, f1);
			_args.setArg (1, f2);
		} else {
			_args.setArg (0, f2);
			_args.setArg (1, f1);
		}
	}
	
	public void actualOperation () {
		Frame f1 = (Frame)_args.getArg(0);
		Frame f2 = (Frame)_args.getArg(1);
		
		_originalArgs = new Object [_arity];
		_originalArgs[0] = _args.getArg(0);
		_originalArgs[1] = _args.getArg(1);
		
		if (f1.equals (f2) && f1.getKnowledgeBase().equals(f2.getKnowledgeBase())) return;
		
		if (!checkMapping (f1, 0)) return;
		if (!checkMapping (f2, 1)) return;
		f1 = (Frame)_args.getArg(0);
		f2 = (Frame)_args.getArg(1);
		KnowledgeBase source1 = f1.getKnowledgeBase();
		KnowledgeBase source2 = f2.getKnowledgeBase();
		
		if (source1 != _targetKb) {
			if (_deepCopy)
//				(DeepCopyFrameOperation.createOperation (f1, _copyInstances, _copySubclasses, true)).performOperation();
				(DeepCopyFrameOperation.createOperation (f1, _traversalDirective.sameDirective (f1), true)).performOperation();
			else
				// (KeepFrameOperation.createOperation (f1, _copyInstances, _copySubclasses, true)).performOperation();
				(KeepFrameOperation.createOperation (f1, _traversalDirective.sameDirective (f1), true)).performOperation();
			_args.setArg (0, (Frame)Mappings.getWhatBecameOfIt (f1));
		}
		
		if (source2 != _targetKb){
			if (_deepCopy)
				//  (DeepCopyFrameOperation.createOperation (f2, _copyInstances, _copySubclasses, true)).performOperation();
				(DeepCopyFrameOperation.createOperation (f2, _traversalDirective.sameDirective (f2), true)).performOperation();
			else
//				(KeepFrameOperation.createOperation (f2, _copyInstances, _copySubclasses, true)).performOperation();
				(KeepFrameOperation.createOperation (f2, _traversalDirective.sameDirective (f2), true)).performOperation();
			_args.setArg(1, (Frame)Mappings.getWhatBecameOfIt (f2));
		}
		
		f1 = (Frame)_args.getArg(0);
		f2 = (Frame)_args.getArg(1);
		
		setPreferredFrame (f1, f2, source1, source2);
		mergeFrames (f1, f2, true);
		
		
		if (_newFrame instanceof Cls) {
			Analysis.considerMergingSlots ((Cls)_newFrame);
			Cls arg1 = (Cls)_originalArgs[0];
			Cls arg2 = (Cls)_originalArgs[1];
			if (arg1.getFrameID() != null && arg2.getFrameID() != null)
				Analysis.classesMerged((Cls)_originalArgs[0], (Cls)_originalArgs[1]);
		}
		if (PromptTab.merging () || PromptTab.mapping())
			createMapping ();
	}
	
	protected Collection getSourcesForMappingKb () {
		Collection sources = Mappings.getSources(_newFrame);
		return sources;
	}
	
	private void setPreferredFrame (Frame f1, Frame f2, KnowledgeBase source1, KnowledgeBase source2) {
		// copy the slots from the system frame to the non-system one
		if (Util.isSystem(f1)) {
			_preferredFrame = f2;
			return;
		}
		
		if (Util.isSystem(f2)) {
			_preferredFrame = f1;
			return;
		}
		
		KnowledgeBase preferred = Preferences.preferredOntology();
		if (preferred == null) return;
		
		if (source1.equals (_targetKb))
			source1 = findSourceKb (f1);
		
		if (source2.equals (_targetKb))
			source2 = findSourceKb (f2);
		
		if (source1.equals (preferred))
			_preferredFrame = f1;
		if (source2.equals (preferred))  {
			if (_preferredFrame == null) {
				_preferredFrame = f2;
			}
			else
				_preferredFrame = null; // both frames come from the preferred kb
		}
	}
	
	private KnowledgeBase findSourceKb (Frame f) {
		Collection sources = Mappings.getSources(f);
		if (sources == null || sources.size() == 0) return f.getKnowledgeBase();
		Iterator i = sources.iterator();
		KnowledgeBase sourceKb = ((Frame)i.next()).getKnowledgeBase();
		while (i.hasNext()) {
			KnowledgeBase nextSourceKb = ((Frame) i.next()).getKnowledgeBase();
			if (nextSourceKb != sourceKb) // sources comes from different kbs
				return  f.getKnowledgeBase();
		}
		return sourceKb;
	}
	
	private Frame mergeFrames (Frame f1, Frame f2) {
		return mergeFrames (f1, f2, false);
	}
	
	private Frame mergeFrames (Frame f1, Frame f2, boolean firstTime) {
		Frame [] oldFrames = {f1, f2};
		
		String newName = pickName (f1, f2);
		String tempName = TEMP_NAME;
		
		if (newName == null) return null;
		if (newName == GENERATE_NEW_NAME) {
			newName = null;
			tempName = null;
		}
//		Log.trace ("1", this, "mergeFrames");
		Frame existing = _targetKb.getFrame(newName);
		if (existing != null && existing != f1 && existing != f2) {
			if (Warning.confirmToContinue(Warning.ASK_TO_COPY_SLOT,
					newName, existing)){
				_performAdditionalMergeWhenDone = true;
				_additionalMergeToPerform = existing;
			} else {
				_duplicateNames = true;
			}
			newName = Mappings.createNameWithSource (newName, _targetKb);
		}
		if (f1.equals (f2)) return f1;
//		Log.trace ("2", this, "mergeFrames");
		
		Frame newFrame;
		
		mergeDirectTypes ((Instance)f1, (Instance)f2);
//		Log.trace ("3", this, "mergeFrames");
		
		if (_preferredFrame != null) {
			newFrame = _preferredFrame;
			if (_preferredFrame.equals (f1))
				finishUpCreateFrame (_preferredFrame, new Frame[] {f2});
			else
				finishUpCreateFrame (_preferredFrame, new Frame[] {f1});
		}
		else
			newFrame = createNewFrame (oldFrames, tempName, ((Instance)f1).getDirectType());
		
		if (firstTime) {
			_newFrame = newFrame;
			_newName = newName;
		}
		
		if (newName != null)
//			newFrame.setName (newName);
			ReplaceReferences.replaceAllReferences (f1, newFrame, _targetKb);
		ReplaceReferences.replaceAllReferences (f2, newFrame, _targetKb);
		
		if (_duplicateNames && PromptTab.merging())
			_currentFoundConflicts.add
			(Conflict.duplicateFrameNames (_targetKb.getFrame (newName), newFrame, null));
		if (this instanceof MergeClsesOperation) {
			mergeOwnSlots (f1, f2);
			mergeTemplateSlots (f1, f2);
			mergeInstances ((Cls)f1, (Cls)f2);
		}
		
		if (this instanceof MergeInstancesOperation) {
			mergeOwnSlots (f1, f2);
		}
		
		if (this instanceof MergeSlotsOperation) //first check attachments before merging slots
			((MergeSlotsOperation)this).mergeSlots ();
		
		if (newFrame instanceof Cls &&
				f1 instanceof Cls &&
				f2 instanceof Cls) {
			mergeSuperclasses ((Cls)f1, (Cls)f2);
			mergeSubclasses ((Cls)f1, (Cls)f2);
		}
		
		if (f1 != _preferredFrame)
			Util.removeFrame(f1);
		
		if (f2 != _preferredFrame)
			Util.removeFrame(f2);
		
		if (newName != null) {
			newFrame = newFrame.rename(newName);
		}
		return newFrame;
	}
	
	private String pickName (Frame f1, Frame f2) {
		if (f1 instanceof SimpleInstance && f2 instanceof SimpleInstance)
			return createNewInstanceName();
		
		String realName1 = Mappings.getRealName (f1);
		String realName2 = Mappings.getRealName (f2);
		
		if (realName1.equals (realName2))
			return realName1;
		
		if (_preferredFrame != null)
			return Mappings.getRealName (_preferredFrame);
		// user has to choose
		boolean done = false;
		String newName = DisplayWarning.selectValue (_chooseNameTitle, _selectNamePrompt, new String [] {realName1, realName2}, true);
		return newName;
	}
	
	private String createNewInstanceName () {
		return	GENERATE_NEW_NAME;
	}
	
	private void mergeInstances (Cls f1, Cls f2) {
		Collection allInstances = new ArrayList ();
		if (!f1.equals (_newFrame))
			allInstances.addAll(f1.getDirectInstances());
		if (!f2.equals (_newFrame))
			allInstances.addAll(f2.getDirectInstances());
		
		Iterator i = allInstances.iterator();
		while (i.hasNext()) {
			Instance next = (Instance)i.next();
			if (!Util.isSystem(next))
				next.setDirectType((Cls)_newFrame);
		}
	}
	
	private void mergeOwnSlots (Frame f1, Frame f2) {
		mergeSlots (f1, f2, false);
	}
	
	private void mergeTemplateSlots ( Frame f1, Frame f2) {
		mergeSlots (f1, f2, true);
	}
	
	private void mergeSlots (Frame f1, Frame f2, boolean templateOrOwnSlots) {
		Collection slots1;
		Collection slots2;
		
		if (Preferences.discardNonPreferredSlots()) {
			// do nothing - the preferred frame already has all the slots it should have
			return;
		}
		
		if (templateOrOwnSlots) {
			if (Preferences.considerInheritedSlots()) {
				slots1 = new ArrayList (((Cls)f1).getTemplateSlots());
				slots2 = new ArrayList (((Cls)f2).getTemplateSlots());
			} else {
				slots1 = new ArrayList (((Cls)f1).getDirectTemplateSlots());
				slots2 = new ArrayList (((Cls)f2).getDirectTemplateSlots());
			}
		} else {
			slots1 = new ArrayList (f1.getOwnSlots());
			slots2 = new ArrayList (f2.getOwnSlots());
		}
		
		Collection commonSlots = new ArrayList (slots1);
		commonSlots.retainAll (slots2);
		// commonSlots contains the slots that we need to take special care of
		
		if (commonSlots != null) {
			slots1.removeAll(commonSlots);
			slots2.removeAll(commonSlots);
		}
		
		copyUniqueLocalSlots (f1, slots1, templateOrOwnSlots);
		copyUniqueLocalSlots (f2, slots2, templateOrOwnSlots);
		
		if (commonSlots == null) return;
		
//		if (_preferredFrame != null)
//		copyUniqueLocalSlots (_preferredFrame, commonSlots, templateOrOwnSlots);
//		else
		copySlotsAndResolveConflicts (commonSlots, f1, f2, templateOrOwnSlots);
		
	}
	
	private void mergeDirectTypes (Instance f1, Instance f2) {
		// f1 and f2 are in the same kb
		Cls type1 = f1.getDirectType();
		Cls type2 = f2.getDirectType();
		
		if (type1.equals (type2)) {
			_frameType = type1;
		} else if (Util.isSystem(type1)) {
			_frameType = type2;
		} else if (Util.isSystem(type2)) {
			_frameType = type1;
		} else
			(new MergeClsesOperation (type1, type2)).performOperation();
	}
	
	private void copyUniqueLocalSlots (Frame oldFrame, Collection slots, boolean templateOrOwnSlots) {
		if (oldFrame.equals (_preferredFrame)) return;
		Iterator i = slots.iterator();
		Slot next;
		Set<Slot> ownSlotsToIgnore = getOwnSlotsToIgnore (oldFrame.getKnowledgeBase());
		
		while (i.hasNext()) {
			next = (Slot)i.next();
			if (templateOrOwnSlots || !ownSlotsToIgnore.contains (next.getName())) {
//				if (oldFrame instanceof Cls )
				copySlotAttachmentInformation (oldFrame, next, _newFrame, next, templateOrOwnSlots);
			}
		}
	}
	
	private void copySlotsAndResolveConflicts (Collection slots, Frame f1, Frame f2, boolean templateOrOwnSlots) {
		if (slots.isEmpty()) return;
		Iterator i = slots.iterator();
		Slot next;
		Collection value1, value2;
		
		Collection reallyConflictingSlots = new  ArrayList();
		Set<Slot> ownSlotsToIgnore = getOwnSlotsToIgnore (((Slot)CollectionUtilities.getFirstItem(slots)).getKnowledgeBase());
		
		while (i.hasNext()) {
			next = (Slot)i.next();
			
			if (! ownSlotsToIgnore.contains (next)) {
				if (sameValuesOrFacets (new FrameSlotCombination (f1, next), new FrameSlotCombination (f2, next), templateOrOwnSlots))
					copySlotAttachmentInformation (f1, next, _newFrame, next, templateOrOwnSlots);
				else if (f1.equals (_preferredFrame))
					copySlotAttachmentInformation (f1, next, _newFrame, next, templateOrOwnSlots);
				else if  (f2.equals(_preferredFrame))
					copySlotAttachmentInformation (f2, next, _newFrame, next, templateOrOwnSlots);
				else {
					Frame nullAlternative = getNullAlternative (f1, f2, next, templateOrOwnSlots);
					if (nullAlternative != null)
						copySlotAttachmentInformation (nullAlternative, next, _newFrame, next, templateOrOwnSlots);
					else
						reallyConflictingSlots.add (next);
				}
			}
		}
		askForHelp (reallyConflictingSlots, f1, f2, templateOrOwnSlots);
	}
	
	private Frame getNullAlternative (Frame f1, Frame f2, Slot s, boolean templateOrOwnSlots) {
		if (templateOrOwnSlots) return null;
		
		Collection value1 = f1.getDirectOwnSlotValues(s);
		Collection value2 = f2.getDirectOwnSlotValues(s);
		
		if (emptyValue (value1)) return f2;
		if (emptyValue (value2)) return f1;
		
		return null;
	}
	
	private boolean emptyValue (Collection c) {
		return (c == null ||
				c.size() == 0 ||
				c.size() == 1 && CollectionUtilities.getFirstItem (c).toString().equals (""));
	}
	
	
	private boolean sameValuesOrFacets (FrameSlotCombination o1, FrameSlotCombination o2, boolean templateOrOwnSlots) {
		if (templateOrOwnSlots)
			return sameFacets (o1, o2);
		else
			return sameValues (o1, o2);
		
	}
	
	private static boolean collectionsEqual (Collection c1, Collection c2) {
		
		boolean equals;
		if (c1 == null) {
			equals = (c2 == null);
		} else if (c2 == null) {
			equals = false;
		} else if (c1.size() == c2.size()) {
			equals = true;
			Iterator iterC1 = c1.iterator();
			Iterator iterC2 = c2.iterator();
			while (equals && iterC1.hasNext() && iterC2.hasNext()) {
				Object o1 = iterC1.next();
				Object o2 = iterC2.next();
				equals = o1.equals(o2);
			}
		} else {
			equals = false;
		}
		return equals;
	}
	
	
	public boolean sameFacets (Slot s1, Slot s2) {
		Collection ownSlots = s1.getOwnSlots();
		Iterator i = ownSlots.iterator();
		while (i.hasNext()) {
			Slot next = (Slot)i.next();
			if (next.getName() == Model.Slot.NAME) continue;
			if (! collectionsEqual (s1.getDirectOwnSlotValues(next), s2.getDirectOwnSlotValues(next)))
				return false;
		}
		return true;
	}
	
	public boolean sameFacets (FrameSlotCombination o1, FrameSlotCombination o2) {
//		if (!((Cls)o1.getFrame()).hasTemplateSlot (o1.getSlot()) ||
//		!((Cls)o2.getFrame()).hasTemplateSlot (o2.getSlot())) return false;
		Collection facets1 = ((Cls)o1.getFrame()).getTemplateFacets (o1.getSlot());
		Collection facets2 = ((Cls)o2.getFrame()).getTemplateFacets (o2.getSlot());
		
		if (facets1 == null && facets2 == null) return true;
		
		if (facets1 == null || facets2 == null) return false;
		
		Iterator i = facets2.iterator();
		Facet next;
		Collection values1, values2;
		while (i.hasNext()) {
			next = (Facet) i.next();
			values1 = ((Cls)o1.getFrame()).getTemplateFacetValues(o1.getSlot(), next);
			values2 = ((Cls)o2.getFrame()).getTemplateFacetValues(o2.getSlot(), next);
			if (!collectionsEqual(values1, values2)) return false;
		}
		return true;
	}
	
	protected boolean sameValues (FrameSlotCombination o1, FrameSlotCombination o2) {
		Collection  value1 = ((Instance)o1.getFrame()).getDirectOwnSlotValues(o1.getSlot());
		Collection value2 = ((Instance)o2.getFrame()).getDirectOwnSlotValues(o2.getSlot());
		return (collectionsEqual(value1, value2));
	}
	
	private void askForHelp (Collection slots, Frame f1, Frame f2, boolean templateOrOwnSlots) {
		if (slots == null || slots.isEmpty()) return;
		Set<Slot> ownSlotsToIgnore = getOwnSlotsToIgnore (((Slot)CollectionUtilities.getFirstItem(slots)).getKnowledgeBase());
		
		Collection alternatives = new ArrayList();
		Iterator i = slots.iterator();
		Slot next;
		while (i.hasNext()) {
			next = (Slot)i.next();
			if (templateOrOwnSlots)  {
				FrameSlotCombination [] combinationChoices = {new FrameSlotCombination (f1, next),
						new FrameSlotCombination (f2, next)};
				int choice = DisplayWarning.selectSlotFacets
				(_selectSlotAttachmentTitle, _selectSlotAttachmentPrompt, combinationChoices );
				if (!((Cls)_newFrame).hasTemplateSlot (next))
					((Cls)_newFrame).addDirectTemplateSlot (next);
				if (choice < 2 && choice >= 0)
					Util.copyTemplateFacetValues (combinationChoices[choice], new FrameSlotCombination (_newFrame, next));
			}
			else {
				if (ownSlotsToIgnore.contains (next)) continue;
				if (next.getMaximumCardinality() > 1)
					_newFrame.setOwnSlotValues(next, mergeSlotValues (f1, next, f2, next));
				else {
					String prompt = "Choose the value for " + next.getName();
					Object newValue = DisplayWarning.selectValue
					(_chooseOwnSlotTitle, prompt, getValueAlternatives (next, f1, f2), true);
					if (newValue != null)
						_newFrame.setOwnSlotValue(next, newValue);
				}
			}
		}
	}
	
	protected Collection mergeSlotValues (Frame f1, Slot slot1, Frame f2, Slot slot2) {
		Collection result = new ArrayList();
		result.addAll(f1.getOwnSlotValues(slot1));
		result.addAll(f2.getOwnSlotValues(slot2));
		return result;
	}
	/*
	 private void chooseCombination (FrameSlotCombination [] c, boolean templateOrOwnSlots) {
	 if (c == null) return;
	 
	 Collection alternatives = new ArrayList();
	 if (templateOrOwnSlots)  {
	 int choice = DisplayWarning.selectSlotFacets
	 (_selectSlotAttachmentTitle, _selectSlotAttachmentPrompt, c );
	 if (!((Cls)_newFrame).hasTemplateSlot (next.getSlot() ))
	 ((Cls)_newFrame).addDirectTemplateSlot (next.getSlot());
	 if (choice < 2 && choice >= 0)
	 Util.copyTemplateFacetValues (combinationChoices[choice],
	 new FrameSlotCombination (_newFrame, next.getSlot()));
	 }
	 else {
	 if (member (next.getSlot().getName(), ownSlotsToIgnore)) continue;
	 String prompt = "Choose the value for " + next.getSlot().getName();
	 String newValue = DisplayWarning.selectValue (_chooseOwnSlotTitle, prompt,
	 getValueAlternatives (c), true);
	 if (newValue != null)
	 _newFrame.setOwnSlotValue(next, (Object)newValue);
	 }
	 }
	 }
	 */
	
	
	protected Object [] getValueAlternatives  (Slot next, Frame f1, Frame f2) {
		return new Object [] {f1.getOwnSlotValue(next), f2.getOwnSlotValue(next)};
	}
	
	protected Object [] getValueAlternatives  (FrameSlotCombination [] combinations) {
		Object [] result = new Object [combinations.length];
		for (int i = 0; i < combinations.length; i++) {
			FrameSlotCombination next = (FrameSlotCombination)combinations[i];
			result[i] = next.getFrame().getOwnSlotValue(next.getSlot());
		}
		return result;
	}
	
	private void mergeSuperclasses (Cls f1, Cls f2) {
		Set allParents = new HashSet (Util.getDirectSuperclasses(f1));
		allParents.addAll(Util.getDirectSuperclasses(f2));
		allParents.remove (_newFrame);
		
		addDirectSuperclasses ((Cls)_newFrame, allParents);
		
		Collection newParents = Util.getDirectSuperclasses((Cls)_newFrame);
		if (newParents.size() > 1 && newParents.contains (_targetKb.getRootCls()))
			((Cls)_newFrame).removeDirectSuperclass (_targetKb.getRootCls());
		
		upgradeMergeOperations (Util.getDirectSuperclasses((Cls)_originalArgs[0]),
				Util.getDirectSuperclasses((Cls)_originalArgs[1]),
		"superclasses");
	}
	
	private void mergeSubclasses (Cls f1, Cls f2) {
		Set allChildren = new HashSet (Util.getDirectSubclasses(f1));
		allChildren.addAll (Util.getDirectSubclasses(f2));
		allChildren.remove(_newFrame);
		
		Iterator i = allChildren.iterator();
		Cls next;
		while (i.hasNext()) {
			next = (Cls)i.next();
			addDirectSubclass (next, (Cls)_newFrame, _targetKb);
		}
		
		removeDirectSubclasses (f1);
		removeDirectSubclasses (f2);
		
		upgradeMergeOperations (Util.getDirectSubclasses((Cls)_originalArgs[0]),
				Util.getDirectSubclasses((Cls)_originalArgs[1]),
		"subclasses");
		
	}
	
	public void removeDirectSubclasses (Cls cls) {
		if (cls.equals (_newFrame)) return;
		Collection subs = new ArrayList(cls.getDirectSubclasses());
		if (subs == null || subs.size () == 0) return;
		Iterator i = subs.iterator();
		while (i.hasNext()) {
			Cls next = (Cls)i.next();
			next.removeDirectSuperclass(cls);
		}
	}
	
	private void  upgradeMergeOperations (Collection clsSet1, Collection clsSet2, String role) {
		Collection operations = getAllOperationsForSet (clsSet1);
		if (operations == null) return; // because MERGE is symmetric, there won't be any in set 2 either
		
		operations.retainAll (getAllOperationsForSet (clsSet2));
		
		if (operations == null) return;
		int currentPriority = SuggestionsAndConflicts.getCurrentTodoPriority();
//		currentPriority++;
		Iterator i = operations.iterator();
		Action next;
		while (i.hasNext()) {
			next = (Action) i.next();
			if (next instanceof MergeFramesOperation) {
				SuggestionsAndConflicts.incrementPriorityOfCurrentAction((Operation)next, REWARD_POINTS);
				((Operation)next).addExplanation(new SameRole(role, _newFrame));
			}
		}
	}
	
	private Collection getAllOperationsForSet (Collection set) {
		if (set == null) return null;
		
		Collection result = new ArrayList ();
		
		Iterator i = set.iterator();
		Collection result2;
		
		while (i.hasNext()) {
			result2 = Mappings.getCurrentOperations ((Frame)i.next());
			if (result2 != null)
				result.addAll (result2);
		}
		
		return result;
	}
	
	private boolean sameFrameType (Frame f1, Frame f2) {
		if (f1 instanceof Cls && f2 instanceof Slot ||
				f1 instanceof Slot && f2 instanceof Cls) {
			Warning.differentFrameTypesForMerge (f1, f2);
			return false;
		}
		else
			return true;
	}
	
	private boolean checkMapping (Frame f, int index) {
		Frame mapping = (Frame)Mappings.getWhatBecameOfIt (f);
		if (mapping != null && !mapping.equals(f))
			if (Warning.confirmToContinue (Warning.MAPPING_EXISTS, Warning.USE_MAPPING, f, mapping)) {
				_args.setArg (index, mapping);
				return true;
			}
			else
				return false;
		return true;
	}
	
	protected String getNewName () {return _newName;}
	
	/*
	 static public JPanel createActionBox () {
	 return Operation.createActionBox (_argumentWidgets);
	 }
	 
	 public void collectData () {
	 super.collectData (_argumentWidgets);
	 }
	 
	 public JPanel createEditBox () {
	 return createEditBox (_argumentWidgets);
	 }
	 
	 public void collectData (GetValueWidget [] argWidgets) {
	 super.collectData (argWidgets);
	 }
	 */
	public void printCurrentFrame (Frame newFrame) {
		if (newFrame instanceof Slot) {
			Slot s = (Slot)_newFrame;
			Collection attachedTo = s.getDirectDomain();
			Iterator i = attachedTo.iterator();
			while (i.hasNext ()) {
				Cls cls = (Cls)i.next ();
				Log.getLogger().info ("cls" + cls + " slot " + s + " value type " + cls.getTemplateSlotValueType(s));
			}
		}
	}
	
	public boolean deepCopy () {return _deepCopy;}
	
	public void replaceFrameReference (Frame from, Frame to) {
		super.replaceFrameReference(from, to);
		Frame f1 = (Frame)_args.getArg(0);
		Frame f2 = (Frame)_args.getArg(1);
		if (f1.equals(f2) && f1.getKnowledgeBase().equals(f2.getKnowledgeBase()))
			SuggestionsAndConflicts.removeSuggestionFromList(this);
		
	}
	/*
	 public JPanel createEditBox () {
	 GetValueWidget [] argWidgets = Dispatcher.createValuesWidgetsImplementation(this);
	 Object [] args = new Object [argWidgets.length];
	 int length = args.length;
	 args[0] = _args[0];
	 args[1] = _args[1];
	 if (length >= 2)
	 args[2] = new Boolean (_deepCopy);
	 if (length >= 3)
	 args[3] = new Boolean (_copySubclasses);
	 if (length >= 4)
	 args[4] = new Boolean (_copyInstances);
	 JPanel panel = Operation.createEditBox(argWidgets, args);
	 return panel;
	 }
	 */
	
}
