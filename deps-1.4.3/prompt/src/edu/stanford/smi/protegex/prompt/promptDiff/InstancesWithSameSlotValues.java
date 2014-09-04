/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.actionLists.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class InstancesWithSameSlotValues implements DiffAlgorithm {
  public boolean usesClassImageInformationInTable () {return true;}
  public boolean usesSlotImageInformationInTable () {return true;}
  public boolean usesFacetImageInformationInTable () {return true;}
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

  public static ResultTable _results;
  private static boolean _changesMade = false;

  public static boolean run (ResultTable currentResults, PromptDiff promptDiff) {
	  Log.getLogger().info ("*********** start InstancesWithSameSlotValues ************");
	  _changesMade = false;
	  if (currentResults == null) return false;
	  _results = currentResults;
	  startAlgorithm ();
	  return _changesMade;
  }

  private static void startAlgorithm () {
	Collection unmatchedFromO1 = _results.getUnmatchedEntriesFromO1 ();
	Collection unmatchedFromO2 = _results.getUnmatchedEntriesFromO2 ();

	if (unmatchedFromO1 == null || unmatchedFromO1.isEmpty() || unmatchedFromO2 == null || unmatchedFromO2.isEmpty()) return;
	HashMapForCollections classesAndInstances1 = createMapForClassesAndInstances (unmatchedFromO1);
	HashMapForCollections classesAndInstances2 = createMapForClassesAndInstances (unmatchedFromO2);

	findMatches (classesAndInstances1, classesAndInstances2);
  }
  
  private static void findMatches (HashMapForCollections classesAndInstances1, HashMapForCollections classesAndInstances2) {
  	Collection classes1 = classesAndInstances1.keySet();
  	Iterator i = classes1.iterator();
  	while (i.hasNext()) {
  		Cls nextCls = (Cls)i.next();
  		Frame nextClsImage = _results.getFirstImage(nextCls);
  		if (nextClsImage == null || !(nextClsImage instanceof Cls)) continue;
  		Collection instances1 = classesAndInstances1.getValues(nextCls);
  		Set<Slot> slots1 = new HashSet<Slot> (nextCls.getTemplateSlots());
		Collection instances2collection = classesAndInstances2.getValues(nextClsImage);
		if (instances2collection == null || instances2collection.isEmpty()) continue;
		HashSet instances2 = new HashSet (instances2collection);
		Collection<Slot> slots2 = ((Cls)nextClsImage).getTemplateSlots();
		compareInstanceSets (instances1, instances2, Util.createSlotMap (slots1, slots2, _results));
  	}
  }
  
  private static void compareInstanceSets (Collection instances1, Collection instances2, Map<Slot, Slot> slotsMap) {
   	FrameNameComparator fc = new FrameNameComparator(false, true);

   	ArrayList v1 = new ArrayList (instances1);
   	ArrayList v2 = new ArrayList (instances2);

   	Collections.sort (v1, fc);
   	Collections.sort (v2, fc);
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
    				boolean compareInstances = Util.compareInstances ((Instance)next1, (Instance)next2, slotsMap, _results);
   				if (compareInstances) {
  					AlgorithmUtils.createNewMatch(next1, next2, "Instances have the same type and slot values", _results);
	  				_changesMade = true;
   				}
   				v1.remove(v1.size()-1);
   				v2.remove(v2.size()-1);
   			}
   	}
  }
  
//  private static void compareInstanceSets (Collection instances1, Collection instances2, HashMap slotsMap) {
//	Log.getLogger().info ("Need to compare " + instances1.size() + "instances");
//	int count = 0;
//	if (instances1 == null || instances2 == null) return;
//	if (slotsMap.isEmpty()) return;
//  	Iterator i = instances1.iterator ();
//  	while (i.hasNext()) {
//  		Instance nextInstance1 = (Instance)i.next();
//  		Iterator j = instances2.iterator();
//  		Instance matchingInstance2 = null;
//  		while (j.hasNext()) {
//  			Instance nextInstance2 = (Instance)j.next();
//  			boolean compareInstances = compareInstances (nextInstance1, nextInstance2, slotsMap);
//  			if (compareInstances) {
//  				AlgorithmUtils.createNewMatch(nextInstance1, nextInstance2, "Instances have the same type and slot values");
//  				_changesMade = true;
//  				matchingInstance2 = nextInstance2;
//  				break;
//  			}
//  		}
//  		if (matchingInstance2 != null)
//  			instances2.remove(matchingInstance2);
//  		if (count % 1000 == 0) 
//  			Log.getLogger().info("count = " + count);
//  		count++;
//  	}
//  }
  
  private static HashMapForCollections createMapForClassesAndInstances (Collection c) {
	HashMapForCollections result = new HashMapForCollections ();
  	Iterator i = c.iterator();
  	while (i.hasNext()) {
  		Frame next = (Frame)i.next();
		if (next instanceof Cls) continue;  // run teh matcher only for simple instances
		if (PromptTab.kbInOWL() && OWLUtil.isOWLAnonymousIndividual(next)) continue;
 		if (PromptTab.kbInOWL() && next instanceof Cls && OWLUtil.isOWLAnonymousClassFrame((Cls)next)) continue;
 		// this is a temporary hack -- need to figure out why all the extraneous rdf:lists are there,
 		if (PromptTab.kbInOWL() && OWLUtil.isRDFList (next)) continue;
  		Cls nextType = ((Instance)next).getDirectType();
  		result.put(nextType, next);
  	}
  	return result;
  }

  public String toString () {
	  return "InstancesWithSameSlotValues";
  }

}

