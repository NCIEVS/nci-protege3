/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class CompareFrameNamesAndTypes implements DiffAlgorithm {
	public boolean usesClassImageInformationInTable () {return CompareConceptIDs.idSlotExists();}
	public boolean usesSlotImageInformationInTable () {return CompareConceptIDs.idSlotExists();}
	public boolean usesFacetImageInformationInTable () {return CompareConceptIDs.idSlotExists();}
	public boolean usesInstanceImageInformationInTable () {return CompareConceptIDs.idSlotExists();}
	
	public boolean usesClassOperationInformationInTable () {return false;}
	public boolean usesSlotOperationInformationInTable () {return false;}
	public boolean usesFacetOperationInformationInTable () {return false;}
	public boolean usesInstanceOperationInformationInTable () {return false;}
	
	public boolean modifiesClassImageInformationInTable () {return true;}
	public boolean modifiesSlotImageInformationInTable () {return true;}
	public boolean modifiesFacetImageInformationInTable () {return true;}
	public boolean modifiesInstanceImageInformationInTable () {return true;}
	
	public boolean modifiesClassOperationInformationInTable () {return false;}
	public boolean modifiesSlotOperationInformationInTable () {return false;}
	public boolean modifiesFacetOperationInformationInTable () {return false;}
	public boolean modifiesInstanceOperationInformationInTable () {return false;}
	
//	-----------------------------------------------------------------------------
	
	private static ResultTable _results;
	private static boolean _changesMade = false;
	
	private static Collection [] _clses = {new ArrayList (), new ArrayList ()};
	
	private static Collection [] _slots = {new ArrayList (), new ArrayList ()};
	
	private static Collection [] _facets = {new ArrayList (), new ArrayList ()};
	
	private static Collection [] _instances = {new ArrayList (), new ArrayList ()};
	
	
	public static boolean run (ResultTable table, PromptDiff promptDiff) {
		Log.getLogger().info ("*********** start CompareFrameNamesAndTypes ************");
		_results = table;
		boolean oldTraceValue = _results.traceOn();
		_results.traceOn(false);
		_changesMade = false;
		
		initializeFrameLists(_results.getUnmatchedEntriesFromO1 (), 0);
		initializeFrameLists(_results.getUnmatchedEntriesFromO2 (), 1);
		findMatchingFramePairs (_clses[0], _clses[1]);
		findMatchingFramePairs (_slots[0], _slots[1]);
		findMatchingFramePairs (_facets[0], _facets[1]);
		findMatchingFramePairs (_instances[0], _instances[1]);
		
		_results.traceOn(oldTraceValue);
		return _changesMade;
	}
	
	private static void initializeFrameLists (Collection unmatchedFrames, int index) {
		if (unmatchedFrames == null || unmatchedFrames.isEmpty()) return;
		
		_clses[index].clear();
		_slots[index].clear();
		_facets[index].clear();
		_instances[index].clear();
		Iterator i = unmatchedFrames.iterator();
		while (i.hasNext()) {
			Frame next = (Frame)i.next();
			if (PromptTab.kbInOWL () && OWLUtil.isOWLAnonymousFrame (next))
				continue;
			else if (next instanceof Cls)
				_clses[index].add (next);
			else if (next instanceof Slot)
				_slots[index].add (next);
			else if (next instanceof Facet)
				_facets[index].add (next);
			else
				_instances[index].add (next);
		}
	}
	
	private static void findMatchingFramePairs (Collection c1, Collection c2) {
		FrameNameComparator fc = new FrameNameComparator(false, true);
		
		ArrayList v1 = new ArrayList (c1);
		ArrayList v2 = new ArrayList (c2);
		
		Collections.sort (v1, fc);
		Collections.sort (v2, fc);
//		Log.trace ("v1 = " + v1, CompareFrameNamesAndTypes.class, "findMatchingFramePairs");
//		Log.trace ("v2 = " + v2, CompareFrameNamesAndTypes.class, "findMatchingFramePairs");
		Frame next1, next2;
		
		while (!v1.isEmpty() && !v2.isEmpty()) {
			next1 = (Frame) v1.get(v1.size()-1);
			next2 = (Frame) v2.get(v2.size()-1);
			
			int comp = CompareNames.compareNamesWithExactMatch(Util.getLocalBrowserText(next1), Util.getLocalBrowserText(next2));
			
			if (comp > 0) {
				v1.remove(v1.size()-1);
			}
			else if (comp < 0) {
				v2.remove(v2.size()-1);
			} else {
				if (next1.getName().equals (next2.getName())) { // they are equal
					AlgorithmUtils.createNewMatch(next1, next2, "frame name and type are the same", _results);
					v1.remove(v1.size()-1);
					v2.remove(v2.size()-1);
				} else {// case or delimiters may be difirent
					//see if there is a frame with exactly the same name as next1 in kb2
					Frame frameWithSameName =
						((Frame)v2.get(v2.size()-1)).getKnowledgeBase().getFrame(((Frame)v1.get (v1.size()-1)).getName());
					if (frameWithSameName != null) {
						int index = v2.indexOf(frameWithSameName);
//						if (index >= 0) {
							AlgorithmUtils.createNewMatch(next1, (Frame)v2.get(index), "frame name and type are the same", _results);
							v1.remove(v1.size()-1);
							v2.remove(index);
//						}
					} else {
						// see if there is a frame with exactly the same name as next2 in kb1
						frameWithSameName =
							((Frame)v1.get(v1.size()-1)).getKnowledgeBase().getFrame(((Frame)v2.get (v2.size()-1)).getName());
						if (frameWithSameName != null) {
							int index = v1.indexOf(frameWithSameName);
//							if (index >= 0) {
								AlgorithmUtils.createNewMatch((Frame)v1.get(index), next2, "frame name and type are the same", _results);
								v1.remove(index);
								v2.remove(v2.size()-1);
//							}
						} else {
							TableRow newMatch = AlgorithmUtils.createNewMatch(next1, next2, "frame name and type are the same", _results);
							newMatch.setRenameExplanation ("different delimiters");
							v1.remove(v1.size()-1);
							v2.remove(v2.size()-1);
						}
					}
				}
			}
		}
		
	}
	
	public String toString () {
		return "CompareFrameNamesAndTypes";
	}
	
	private static Collection toCollection (Object [] v) {
		Collection result = new ArrayList ( );
		for (int i = 0; i < v.length; i++)
			result.add (v[i]);
		return result;
	}
	
}

