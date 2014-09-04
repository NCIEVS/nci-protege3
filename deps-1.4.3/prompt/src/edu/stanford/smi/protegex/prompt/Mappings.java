/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.actionLists.*;
import edu.stanford.smi.protegex.prompt.operation.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class Mappings {
	
	private static KnowledgeBase _targetKb = ProjectsAndKnowledgeBases.getTargetKnowledgeBase();
	private static KnowledgeBaseInMerging _targetKbInMerging = ProjectsAndKnowledgeBases.getKnowledgeBaseInMerging(_targetKb);
	
	static public MultiMap getFrameActionsMap (KnowledgeBase kb) {
		return (ProjectsAndKnowledgeBases.getKnowledgeBaseInMerging(kb)).getFrameActionsMap();
	}
	
	static public void updateWhatBecameOfItMap (Frame oldFrame, Frame newFrame) {
		KnowledgeBaseInMerging sourceKbInMerging =
			ProjectsAndKnowledgeBases.getKnowledgeBaseInMerging (oldFrame.getKnowledgeBase());
		sourceKbInMerging.createWhatBecameOfItBinding (oldFrame, newFrame);
		updateReferencesInOperations (oldFrame, newFrame);
	}
	
	static private void updateReferencesInOperations (Frame oldFrame, Frame newFrame) {
		if (DummyFrame.isDummyFrame(newFrame)) return;
		Collection operations = getCurrentOperations (oldFrame);
		if (operations != null && operations.size() != 0) {
			operations = new ArrayList (operations);
			Iterator i = operations.iterator();
			while (i.hasNext()) {
				Action next = (Action)i.next();
				next.replaceFrameReference(oldFrame, newFrame);
			}
		}
	}
	
	static public void removeFromFrameActionsMap (Frame f, Action a) {
		MultiMap frameActionsMap = getFrameActionsMap (f.getKnowledgeBase ());
		Collection currentValues = frameActionsMap.getValues(f);
		if (currentValues != null && currentValues.contains(a))
			frameActionsMap.removeValue(f, a);
	}
	
	static public void removeFromFrameActionsMap (Operation op) {
		removeFromFrameActionsMap (op, true);
	}
	
	static public void removeFromFrameActionsMap (Operation op, boolean removeExplanations) {
		removeFromFrameActionsMap_AllFrames (op);
		if (removeExplanations) {
			Collection exps = op.getReason();
			if (exps == null) return;
			Iterator i = exps.iterator();
			while (i.hasNext ()) {
				removeFromFrameActionsMap_AllFrames ((Action)i.next());
			}
		}
	}
	
	static private void removeFromFrameActionsMap_AllFrames (Action a) {
		ActionArgs args = a.getArgs();
		if (args == null) return;
		
		for (int i = 0; i < args.size(); i++) {
			Object next = args.getArg(i);
			if (next instanceof Frame)
				removeFromFrameActionsMap ((Frame)next, a);
		}
	}
	
	static public void addToFrameActionsMap (Frame f, Action a) {
		getFrameActionsMap (f.getKnowledgeBase ()).addValue(f, a);
	}
	
	/*
	 static public void updateFrameActionsMap (Frame f, String oldName) {
	 HashMapForCollections map = getFrameActionsMap (f.getKnowledgeBase ());
	 map.updateKey(f, oldName);
	 }
	 */
	
	
	static public Collection getCurrentOperations (Frame f) {
		KnowledgeBase kb = f.getKnowledgeBase();
		MultiMap operationsMap = getFrameActionsMap (kb);
		Collection allOperations;
		allOperations = (Collection)operationsMap.getValues (f);
		
		/*
		 if (allOperations == null) return null;
		 
		 
		 Collection result = new HashSet();
		 
		 Iterator i = allOperations.iterator();
		 Action next;
		 while (i.hasNext()) {
		 next = (Action) i.next();
		 //       if (next instanceof Operation)
		  result.add (next);
		  }
		  if (result.size() == 0) return null;
		  
		  return result;
		  */
		return allOperations;
	}
	
	/*
	 static private Collection findOperationsForDeletedFrame (HashMapForCollections map, KnowledgeBase kb) {
	 Collection keys = map.keySet();
	 Iterator i = keys.iterator();
	 while (i.hasNext()) {
	 String next = (String)i.next();
	 Collection operations = (Collection)map.get (next);
	 if (kb.getFrame (next) == null && operations.size() > 0)
	 return operations;
	 }
	 return null;
	 }
	 */
	static public void createBindingToDummyFrame (Frame oldFrame, Frame dummy) {
		updateWhatBecameOfItMap (oldFrame, dummy);
	}
	
	static private Frame getMapping (Frame f) {
		KnowledgeBase kb = f.getKnowledgeBase();
		if (kb == _targetKb || (PromptTab.extracting() && kb == PromptTab.getTraversalDirectivesKb().getKnowledgeBase()))
			return f;
		else {
			Frame mapping = (Frame) ProjectsAndKnowledgeBases.getKnowledgeBaseInMerging (kb).getWhatBecameOfIt(f);
			if (mapping == null) return null;
			if (mapping.getName() == null) {
				ProjectsAndKnowledgeBases.getKnowledgeBaseInMerging (kb).removeWhatBecameOfItBinding(f);
				return null;
			}
			return mapping;
		}
	}
	
	static public void removeWhatBecameOfIt (Frame f) {
		KnowledgeBase kb = f.getKnowledgeBase();
		if (kb == _targetKb)
			return;
		else {
			ProjectsAndKnowledgeBases.getKnowledgeBaseInMerging(kb).removeWhatBecameOfItBinding(f);
		}
	}
	
	static public Frame getWhatBecameOfIt (Frame f) {
		Frame mapping = getMapping (f);
		if (mapping == null || mapping.getFrameID() == null) return null;
		if (DummyFrame.isDummyFrame (mapping))
			return null;
		else
			return mapping;
	}
	
	static public Frame getMappingToDummyFrame (Frame f) {
		Frame mapping = getMapping (f);
		if (mapping == null || mapping.equals (f))
			return null;
		
		if (DummyFrame.isDummyFrame (mapping))
			return mapping;
		else
			return null;
	}
	
	static public Collection <Frame> getSources (Frame f) {
		if (f.getKnowledgeBase() != _targetKb)
			return null;
		else {
			return _targetKbInMerging.getSources (f);
		}
	}
	
	static public Frame getSingleSource (Frame f) {
		Collection sources = getSources (f);
		if (sources == null || sources.isEmpty()) return null;
		return (Frame)CollectionUtilities.getFirstItem(sources);
	}
	
	static public void setSource (Frame oldFrame, Frame newFrame) {
		_targetKbInMerging.setSource (oldFrame, newFrame);
	}
	
	static protected void initialWhatBecameOfItBindings (KnowledgeBaseInMerging kbMergedInMerging,
			KnowledgeBaseInMerging kbInMerging) {
		Collection systemFrames = Util.getSystemFrames ();
		
		HashMap map = kbInMerging.getWhatBecameOfItMap ();
		
		KnowledgeBase kb = kbInMerging.getKnowledgeBase();
		KnowledgeBase kbMerged = kbMergedInMerging.getKnowledgeBase();
		
		Iterator i = systemFrames.iterator();
		Frame next;
		while (i.hasNext()) {
			next = (Frame)i.next();
			String nextName = next.getName();
			map.put (kb.getFrame (nextName), kbMerged.getFrame (nextName));
		}
	}

	static private final String BEGIN_ONT_SUFFIX = "--";
	static private final String END_ONT_SUFFIX = "";
	
	public static String  getRealName (Frame f) {
		String fullName = f.getName();
		if (fullName == null) return null;
		int index = fullName.indexOf (BEGIN_ONT_SUFFIX);
		if (index == -1)
			return fullName;
		else
			return fullName.substring (0, index);
	}
	
	public static String  getOntologySuffix (Frame f) {
		if (getRealName (f).equals (f.getName())) return null;
		
		String fullName = f.getName();
		int index1 = fullName.indexOf (BEGIN_ONT_SUFFIX);
		int index2 = fullName.indexOf (END_ONT_SUFFIX);
		if (index1 == -1 || index2 == -1)
			return null;
		else
			return fullName.substring (index1 + BEGIN_ONT_SUFFIX.length(), index2);
	}
	
	public static String createNameWithSource (String frameName, KnowledgeBase sourceKb) {
		if (frameName == null) return null;
		String kbName = ProjectsAndKnowledgeBases.getKnowledgeBasePrettyName (sourceKb);
		return frameName + BEGIN_ONT_SUFFIX + kbName + END_ONT_SUFFIX;
	}
	
	public static  Slot [] findPrototypes (String realName, Collection c) {
		Slot [] result = new Slot [2];
		
		int index = 0;
		Iterator i = c.iterator();
		Slot next;
		while (i.hasNext()) {
			next = (Slot)i.next();
			if (getRealName(next).equalsIgnoreCase (realName))
				result [index++] = next;
		}
		return result;
	}
	
	public static  ArrayList getRealNames (Collection c) {
		ArrayList result = new ArrayList();
		Iterator i = c.iterator();
		while (i.hasNext())
			result.add (getRealName((Slot)i.next()));
		
		return result;
	}
	
	
}
