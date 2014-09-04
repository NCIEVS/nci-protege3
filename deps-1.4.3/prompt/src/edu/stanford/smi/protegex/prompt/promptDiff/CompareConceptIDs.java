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

// use this matcher if there is a unique concept ID slot
public class CompareConceptIDs implements DiffAlgorithm {
    public boolean usesClassImageInformationInTable () {return false;}
    public boolean usesSlotImageInformationInTable () {return false;}
    public boolean usesFacetImageInformationInTable () {return false;}
    public boolean usesInstanceImageInformationInTable () {return false;}

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

//-----------------------------------------------------------------------------

    private static ResultTable _resultsTable;
    private static ArrayList _results = new ArrayList ();
    private static Slot _sourceIDSlot = null;
    private static Slot _targetIDSlot = null;
    
    private static KnowledgeBase _kb1, _kb2;
  
    public static boolean run (ResultTable table, PromptDiff promptDiff) {
    		Log.getLogger().info ("*********** start CompareConceptIDs ************");
    		_kb1 = promptDiff.getKb1();
    		_kb2 = promptDiff.getKb2();
		_resultsTable = table;
	
	   	boolean oldTraceValue = _resultsTable.traceOn();
     	_resultsTable.traceOn(false);
     	_results.clear();

        setIDSlots();

        if (_sourceIDSlot == null || _targetIDSlot == null)
          return false;

        findMatchingFrames ();

		_resultsTable.traceOn(oldTraceValue);
        if (_results.isEmpty())
          return false;
        else {
          _resultsTable.addElements(_results);
          return true;
        }
    }

    private static void findMatchingFrames() {

      Collection frames1 = _resultsTable.getUnmatchedEntriesFromO1 ();
      Collection frames2 = _resultsTable.getUnmatchedEntriesFromO2 ();

      int count = 0;

      FrameIDComparator fc1 = new FrameIDComparator(_sourceIDSlot);
     FrameIDComparator fc2 = new FrameIDComparator(_targetIDSlot);

      LinkedList [] v = new LinkedList[2];
      v[0] = new LinkedList (frames1);
      v[1] = new LinkedList (frames2);

//    Log.getLogger().info ("Start sort 1");
      Collections.sort (v[0], fc1);
//    Log.getLogger().info ("Start sort 2");
      Collections.sort (v[1], fc2);

      Frame [] next = new Frame [] {null, null};
      String [] id = new String [] {null, null};
      Frame [] prevFrame = new Frame [] {null, null};
      String [] prevId = new String [] {null, null};
      
      Slot [] idSlots = new Slot [] {_sourceIDSlot, _targetIDSlot};
      
      boolean firstTime = true;
 //Log.trace ("Done sorting", CompareConceptIDs.class, "findMatchingFrames");
	while (!v[0].isEmpty() && !v[1].isEmpty()) {
		for (int i = 0; i < 2; i++) {
			next[i] = (Frame) v[i].getLast();
       		id[i] = (String)next[i].getOwnSlotValue(idSlots[i]);
       		if (firstTime) { //first time in the loop
           		firstTime = false;
           	} else {
           		if (id[i] != null && id[i].equals(prevId[i]))
           			Log.getLogger().warning("ID is not unique for concepts: " + prevFrame[i] + " and " + next[i] + "; id = " + id[i]);
            	}
            if (id[i] == null || id[i].equals ("")) {
            		return; // reached the end of comparable ids
            }
		}
		
//Log.getLogger().info ("next[0] = " + next[0] + ", id[0] = " + id[0] + ", next[1] = " + next[1] + ", id[1] = " + id[1]);
       	
        if (id[0].compareTo(id[1]) > 0) {
          v[0].removeLast();
          prevId[0] = id[0];
          prevFrame[0] = next[0];
       }
        else if (id[0].compareTo(id[1]) < 0) {
          v[1].removeLast();
          prevId[1] = id[1];
          prevFrame[1] = next[1];
        }
        else {//id1==id2
          AlgorithmUtils.createNewMatch(next[0], next[1], "frame ids are the same", _resultsTable);
          v[0].removeLast();
          v[1].removeLast();
          prevId[0] = id[0];
          prevFrame[0] = next[0];
          prevId[1] = id[1];
          prevFrame[1] = next[1];
        }
      }
    }

    private static void setIDSlots () {
      if (PromptDiff.getIdSlotName() == null) return;
      _sourceIDSlot =  _kb1.getSlot( PromptTab.getPromptDiff().getIdSlotName());
      _targetIDSlot =  _kb2.getSlot( PromptTab.getPromptDiff().getIdSlotName());
    }

    public static boolean idSlotExists () {
      setIDSlots ();
      return (_sourceIDSlot != null && _targetIDSlot != null);
    }
}

