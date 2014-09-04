/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class CompareOWLIntersectionAndUnionClasses implements DiffAlgorithm {
	public boolean usesClassImageInformationInTable () {return true;}
	public boolean usesSlotImageInformationInTable () {return false;}
	public boolean usesFacetImageInformationInTable () {return false;}
	public boolean usesInstanceImageInformationInTable () {return true;}

	public boolean usesClassOperationInformationInTable () {return false;}
	public boolean usesSlotOperationInformationInTable () {return false;}
	public boolean usesFacetOperationInformationInTable () {return false;}
	public boolean usesInstanceOperationInformationInTable () {return false;}

	public boolean modifiesClassImageInformationInTable () {return true;}
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

	public static boolean run (ResultTable table, PromptDiff promptDiff) {
		if (!sourceKbsInOwl(promptDiff.getKb1(), promptDiff.getKb2())) return false;
		Log.getLogger().info ("*********** start CompareOWLIntersectionAndUnionClasses ************");
//Log.getLogger().info ("Starting at: " + new Date());				
		_resultsTable = table;
	
		_changesMade = false;
//		boolean oldTraceValue = _resultsTable.traceOn();
		_resultsTable.traceOn(false);

		findMatchingFrames ();
//Log.getLogger().info ("Done at: " + new Date());				

//		_resultsTable.traceOn(oldTraceValue);
		return _changesMade;
	}
	
	private static boolean sourceKbsInOwl (KnowledgeBase kb1, KnowledgeBase kb2) {
		_kb1 =  kb1;
		_kb2 =  kb2;
		return (PromptTab.kbInOWL());
	}

	private static HashMap _kbToBrowserTexts = new HashMap (2);
	private static void findMatchingFrames() {
	  Collection frames1 = _resultsTable.getUnmatchedEntriesFromO1 ();
	  Collection frames2 = _resultsTable.getUnmatchedEntriesFromO2 ();
	  
	  LinkedList anonymousFrames1 = getOWLIntersectionAndUnionClasses (frames1);
	  LinkedList anonymousFrames2 = getOWLIntersectionAndUnionClasses (frames2);
	  
	  createMatchesBasedOnNamedSubclasses (anonymousFrames1, anonymousFrames2);
	}
	
	private static LinkedList getOWLIntersectionAndUnionClasses(Collection frames) {
		LinkedList result = new LinkedList ();
		Iterator i = frames.iterator();
		while (i.hasNext()) {
			Frame next = (Frame)i.next();
			if (OWLUtil.isIntersectionOrUnionClass (next))
				result.add(next);
		}
		
		return result;
	}
	
	private static void createMatchesBasedOnNamedSubclasses (Collection c1, Collection c2) {
		Iterator i = c1.iterator();
		while (i.hasNext()) {
			Cls next1 = (Cls)i.next();
			Collection next1Subclasses = Util.getDirectSubclasses(next1);
			if (next1Subclasses.isEmpty()) continue;
			
			Cls next1Subclass = null;
			Cls next1SubclassImage = null;
			if (!next1Subclasses.isEmpty()) {
				next1Subclass = (Cls)CollectionUtilities.getFirstItem(next1Subclasses);
				next1SubclassImage = (Cls)_resultsTable.getFirstImage(next1Subclass);
				if (next1SubclassImage == null || !(OWLUtil.isOWLNamedClass (next1SubclassImage))) continue;
			}
			
			Collection restrictions = OWLUtil.getAnonymousSuperclasses(next1SubclassImage);
			Iterator j = restrictions.iterator();
			
			while (j.hasNext()) {
				Cls next1SubclassImageSuper = (Cls)j.next();
				if (!OWLUtil.isIntersectionOrUnionClass (next1SubclassImageSuper)) continue;
				if (OWLUtil.sameOperandsForOWLNAryLogicalClass (next1, next1SubclassImageSuper)) {
					AlgorithmUtils.createNewMatch(next1, next1SubclassImageSuper, "intersection or union classes with the same operands", _resultsTable);
					_changesMade = true;
					break;
				}
			}
		}
	}
	
}
