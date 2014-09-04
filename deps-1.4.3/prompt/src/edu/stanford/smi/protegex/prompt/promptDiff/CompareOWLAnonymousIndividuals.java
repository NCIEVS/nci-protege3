/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */
/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.ArrayListMultiMap;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.MultiMap;
import edu.stanford.smi.protegex.owl.model.OWLDataRange;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.RDFList;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.ResultTable;
import edu.stanford.smi.protegex.prompt.util.OWLUtil;
import edu.stanford.smi.protegex.prompt.util.Util;

public class CompareOWLAnonymousIndividuals implements DiffAlgorithm {
	public boolean usesClassImageInformationInTable () {return true;}
	public boolean usesSlotImageInformationInTable () {return true;}
	public boolean usesFacetImageInformationInTable () {return false;}
	public boolean usesInstanceImageInformationInTable () {return true;}

	public boolean usesClassOperationInformationInTable () {return false;}
	public boolean usesSlotOperationInformationInTable () {return false;}
	public boolean usesFacetOperationInformationInTable () {return false;}
	public boolean usesInstanceOperationInformationInTable () {return false;}

	public boolean modifiesClassImageInformationInTable () {return false;}
	public boolean modifiesSlotImageInformationInTable () {return false;}
	public boolean modifiesFacetImageInformationInTable () {return false;}
	public boolean modifiesInstanceImageInformationInTable () {return true;}

	public boolean modifiesClassOperationInformationInTable () {return false;}
	public boolean modifiesSlotOperationInformationInTable () {return false;}
	public boolean modifiesFacetOperationInformationInTable () {return false;}
	public boolean modifiesInstanceOperationInformationInTable () {return false;}

//-----------------------------------------------------------------------------

	private static ResultTable _resultsTable;
	
	private static KnowledgeBase _kb1 = null;
	private static KnowledgeBase _kb2 = null;
	
	private static boolean _changesMade = false;
	
	private static Map<KnowledgeBase, Map<Cls, MultiMap<String, OWLIndividual>>> _kbToBrowserTexts = new HashMap<KnowledgeBase, Map<Cls, MultiMap<String, OWLIndividual>>> (2);
	private static boolean _done = false;

	public static boolean run (ResultTable table, PromptDiff promptDiff) {
		if (!sourceKbsInOwl(promptDiff.getKb1(), promptDiff.getKb2())) return false;
		Log.getLogger().info ("*********** start CompareOWLAnonymousIndividuals ************");
		_resultsTable = table;
	
		_changesMade = false;
		_resultsTable.traceOn(false);

		findMatchingFrames ();
		return _changesMade;
	}
	
	private static boolean sourceKbsInOwl (KnowledgeBase kb1, KnowledgeBase kb2) {
		_kb1 =  kb1;
		_kb2 =  kb2;
		return (PromptTab.kbInOWL());
	}

	private static void findMatchingFrames() {
	  if (_done) return;
	  Collection<Frame> frames1 = _resultsTable.getUnmatchedEntriesFromO1 ();
	  Collection<Frame> frames2 = _resultsTable.getUnmatchedEntriesFromO2 ();
	  
	  List<OWLIndividual> anonymousFrames1 = OWLUtil.anonymousIndividualsFromCollection (frames1, _kb1);
	  List<OWLIndividual> anonymousFrames2 = OWLUtil.anonymousIndividualsFromCollection (frames2, _kb2);
	  
	  if (anonymousFrames1 == null || anonymousFrames1.isEmpty()) {
		  _done = true;
		  return;
	  }
	  if (anonymousFrames2 == null || anonymousFrames2.isEmpty()) {
		  _done = true;
		  return;
	  }
	  
	  Map<Cls, MultiMap<String, OWLIndividual>> classToBrowserTextToFrame1 = createClassToBrowserTextToFrameMap (anonymousFrames1, _kb1);
	  Map<Cls, MultiMap<String, OWLIndividual>> classToBrowserTextToFrame2 = createClassToBrowserTextToFrameMap (anonymousFrames2, _kb2);
	  
      Collection<Cls> classes1 = classToBrowserTextToFrame1.keySet();
      Iterator<Cls> i = classes1.iterator();
      while (i.hasNext()) {
      	Cls nextCls = i.next();
      	Cls nextClsImage = (Cls)_resultsTable.getFirstImage(nextCls);
      	if (nextClsImage == null) continue;
      	Map<Slot, Slot> slotMap = Util.createSlotMap(nextCls.getTemplateSlots(), nextClsImage.getTemplateSlots(), _resultsTable);
      	
      	findMatchesInClassInstances (classToBrowserTextToFrame1.get(nextCls), classToBrowserTextToFrame2.get(nextClsImage), slotMap);
      }
	  
	}
	
	//currentList: <class; <browserText; individuals*>>
	private static Map<Cls, MultiMap<String, OWLIndividual>> createClassToBrowserTextToFrameMap (Collection<OWLIndividual> anonymousFrames, KnowledgeBase kb) {
		Map<Cls, MultiMap<String, OWLIndividual>> currentList = _kbToBrowserTexts.get(kb);
		if (currentList != null) return currentList;
		
		Map<Cls, MultiMap<String, OWLIndividual>> result = new HashMap<Cls, MultiMap<String, OWLIndividual>> ();
		for (OWLIndividual next : anonymousFrames) {
			Cls type = ((Instance)next).getDirectType();
			MultiMap<String, OWLIndividual> typeMap = result.get(type);
			if (typeMap == null) {
				typeMap = new ArrayListMultiMap<String, OWLIndividual> ();
				result.put(type, typeMap);
			}
			typeMap.addValue(Util.getLocalBrowserText(next), next);
		}
		_kbToBrowserTexts.put(kb, result);
		return result;
	}
	
	static boolean _temp = false;
	private static void findMatchesInClassInstances (MultiMap<String, OWLIndividual> instances1, 
													 MultiMap<String, OWLIndividual> instances2, 
													 Map<Slot, Slot> slotMap) {
		Collection<String> browserTexts1 = instances1.getKeys();
		for (String nextKey  : browserTexts1) {
			Collection<OWLIndividual> nextInstances1 =instances1.getValues(nextKey);
			if (nextInstances1 == null) continue;
			Collection<OWLIndividual> nextInstances2 = instances2.getValues(nextKey);
			if (nextInstances2 == null)
				continue;
			Collection<OWLIndividual> temp1 = new ArrayList<OWLIndividual> (nextInstances1);
			Collection<OWLIndividual> temp2 = new ArrayList<OWLIndividual> (nextInstances2);
			_temp = true;
			createMatches (nextInstances1, nextInstances2, slotMap);
			if (_temp == false)
				createMatches (temp1, temp2, slotMap);
		}
		
	}
	
	
	private static void createMatches (Collection<OWLIndividual> c1, Collection<OWLIndividual> c2, Map<Slot, Slot> slotMap) {
		if (c1 == null || c1.isEmpty()) return;
		if (c2 == null || c2.isEmpty()) return;
		
		boolean lookingatclass = false;
		_temp = true;
		Iterator<OWLIndividual> i = c1.iterator();
		while (i.hasNext()) {
		    OWLIndividual next1 = i.next();
			Collection<Reference> next1References = next1.getReferences();
			_temp = false;
		  Iterator<OWLIndividual> j = c2.iterator();
		  while (j.hasNext()) {
		      OWLIndividual next2 = j.next();
			  Collection<Reference> next2References = next2.getReferences();
			  if (compareReferences (next1References, next2References)) {
				  //*** a bit of a hack: for anonymous instances, assume that if they are referred by mapped objects
				  // and have the same browser key, they are he same; don't check the rest of the slots
				  //&&	  Util.compareInstances(next1, next2, slotMap, _resultsTable)) {
				  AlgorithmUtils.createNewMatch (next1, next2, "individuals are anonymous, identical, and they are referenced from the same resources", _resultsTable);
					j.remove();
					i.remove();
				  _changesMade = true;
				  _temp = true;
				  break;
			  }
		  }
		  if (lookingatclass && _temp == false)
			  Log.getLogger().info("Shouldn't be here");
	  }
	}

	private static boolean compareReferences (Collection<Reference> refs1, Collection<Reference> refs2) {
		MultiMap<Slot, Frame> map1 = createSlotToFramesMap (refs1);
		MultiMap<Slot, Frame> map2 = createSlotToFramesMap (refs2);
		Collection<Slot> slots1 = map1.getKeys();
		Iterator<Slot> i = slots1.iterator();
		while (i.hasNext()) {
			Slot nextSlot1 = i.next();
			Slot nextSlot1Image = (Slot)_resultsTable.getFirstImage(nextSlot1);
			if (nextSlot1Image == null)
				return false;
			if (map2.getValues(nextSlot1Image) == null) {
				return false;
			}
			if (!AlgorithmUtils.compareCollections(map1.getValues(nextSlot1), map2.getValues(nextSlot1Image), _resultsTable)) 
				return false;
		}
		return true;
	}
	
	private static MultiMap<Slot, Frame> createSlotToFramesMap (Collection refs) {
		MultiMap<Slot, Frame> result = new ArrayListMultiMap<Slot, Frame> ();
		Iterator i = refs.iterator();
		while (i.hasNext()) {
			Reference nextRef = (Reference)i.next();
			result.addValue(nextRef.getSlot(), nextRef.getFrame());
		}
		return result;
	}
	

}
