/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.prompt.actionLists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.prompt.Mappings;
import edu.stanford.smi.protegex.prompt.ProjectsAndKnowledgeBases;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.conflict.Conflict;
import edu.stanford.smi.protegex.prompt.operation.MergeFramesOperation;
import edu.stanford.smi.protegex.prompt.operation.Operation;

public class ActionList extends ArrayList {
	static final int MAX_PRIORITY = 30;
	static final int MAX_ACTION_TYPES = 15;

	PriorityList priorityMap; // <Priority, [Action]>
	HashMapForCollections actionTypeMap; // <ActionType, [Action] >
	Stack lifoActionMap; // [Action]
	HashMapForCollections oldActionsByTypeMap; //<string, [Action]>

	public ActionList() {
		priorityMap = new PriorityList(MAX_PRIORITY);
		actionTypeMap = new HashMapForCollections(MAX_ACTION_TYPES);
		lifoActionMap = new Stack();
		oldActionsByTypeMap = new HashMapForCollections();
	}

	public boolean add(Action a) {
		return add(a, true);
	}

	public boolean add(Action a, boolean checkForDuplicates) {
		Object duplicate = null;
		if (PromptTab.mapping() && !(a instanceof MergeFramesOperation)) {
			Log.getLogger().warning("Mapping: add to action list; shouldn't be here");
		}

		if (PromptTab.mapping() && PromptTab.mergingHasBeenSetUp() && mergeArgumentsFromSameKb(a)) {
			return false;
		}

		if (checkForDuplicates) {
			duplicate = findValueInList(a);
		}

		if (duplicate == null) { // also true if we don't check for duplicates)
			super.add(a);
			priorityMap.put(new Integer(a.getPriority()), a);
			actionTypeMap.put(a.getType(), a);
			lifoActionMap.push(a);
			a.addToFrameActionsMap();
		} else {
			if (duplicate instanceof Operation) {
				Operation duplicateOperation = (Operation) duplicate;
				a.setPriority(priorityMap.getCurrentPriority());
//        Mappings.removeFromFrameActionsMap((Operation)a, false);

				priorityMap.remove(new Integer(duplicateOperation.getPriority()), duplicateOperation);
				duplicateOperation.setPriority(a.getPriority());
				duplicateOperation.setPriority(priorityMap.getCurrentPriority());
				priorityMap.put(new Integer(duplicateOperation.getPriority()), duplicateOperation);

				Collection conflicts = ((Operation) a).getConflictsItSolves();
				duplicateOperation.addConflictsItSolvesToOperation(conflicts);

				if (conflicts != null && !conflicts.isEmpty()) {
					Iterator i = conflicts.iterator();
					while (i.hasNext()) {
						Conflict next = (Conflict) i.next();
						next.removeSolution((Operation) a);
						next.addSolution(duplicateOperation);
					}
				}
			}
		}
		return true;
	}

	private boolean mergeArgumentsFromSameKb(Action a) {
		ActionArgs args = a.getArgs();
		Frame f1 = (Frame) args.getArg(0);
		Frame f2 = (Frame) args.getArg(1);
		KnowledgeBase kb1 = f1.getKnowledgeBase();
		KnowledgeBase kb2 = f2.getKnowledgeBase();

		// frames are from the same original kb
		if (kb1 == kb2 && kb1 != ProjectsAndKnowledgeBases.getTargetKnowledgeBase()) {
			return true;
		}

		Frame source1, source2;
		if (kb1 != ProjectsAndKnowledgeBases.getTargetKnowledgeBase()) {
			source1 = f1;
		} else {
			source1 = Mappings.getSingleSource(f1);
		}

		if (kb2 != ProjectsAndKnowledgeBases.getTargetKnowledgeBase()) {
			source2 = f2;
		} else {
			source2 = Mappings.getSingleSource(f2);
		}

		return (source1.getKnowledgeBase() == source2.getKnowledgeBase());

	}

	public boolean addAll(Collection c) {
		return addAll(c, true);
	}

	public boolean addAll(Collection c, boolean checkForDuplicates) {
		if (c != null && c.size() != 0) {
			Iterator i = c.iterator();
			while (i.hasNext()) {
				add((Action) i.next(), checkForDuplicates);
			}
		}
		return true;
	}

	public Object remove(Action a) {
		Action found = null;
		if (this == null) {
			return null;
		}

		Iterator i = this.iterator();
		while (i.hasNext() && found == null) {
			Object next = i.next();
			if (a.equals(next)) {
				found = (Action) next;
			}
		}

		if (found != null) {
			super.remove(found);
			priorityMap.remove(new Integer(found.getPriority()), found);
			actionTypeMap.remove(found.getType(), found);

			String stringAction = createStringFromAction(found);
			oldActionsByTypeMap.put(stringAction, stringAction);
			lifoActionMap.remove(found);
			found.removeFromFrameActionsMap();
			return found;
		} else {
			return null;
		}
	}

	private String createStringFromAction(Action a) {
		//creates a string version of an action with reference to strings rather
		//than frame names for further reference

		String result = a.getType();
		ActionArgs args = a.getArgs();
		Object[] newArgs = args.toArray();
		if (newArgs == null) {
			return result;
		}
		ArrayList stringArgs = new ArrayList();

		for (int i = 0; i < newArgs.length; i++) {
			Object next = newArgs[i];
			if (next instanceof Frame) {
				stringArgs.add(((Frame) next).getName());
			} else if (next instanceof String) {
				stringArgs.add(next);
			}
		}
		Collections.sort(stringArgs);
		Iterator i = stringArgs.iterator();
		while (i.hasNext()) {
			result += (String) i.next();
		}
		return result;
	}

	public String toString() {
		return "";
	}

	public boolean removeAll(Collection c) {
		if (c != null && c.size() != 0) {
			Iterator i = c.iterator();
			while (i.hasNext()) {
				remove((Action) i.next());
			}
		}
		return true;
	}

	public Object findValueInList(Action a) {
		Collection allValues = findValuesInList(a);
		if (allValues == null) {
			return null;
		} else {
			return CollectionUtilities.getFirstItem(allValues);
		}
	}

	public Collection findValuesInList(Action a) {
		Collection result = new ArrayList();
		Collection actionsOfNeededType = (Collection) actionTypeMap.get(a.getType());
		if (actionsOfNeededType == null || actionsOfNeededType.size() == 0) {
			return null;
		}

		Iterator i = actionsOfNeededType.iterator();
		Action next;
		while (i.hasNext()) {
			next = (Action) i.next();
			if (next.equals(a)) {
				result.add(next);
			}
		}
		if (result.isEmpty()) {
			String stringAction = createStringFromAction(a);
			result = (Collection) oldActionsByTypeMap.get(stringAction);
		}
		if (result == null || result.isEmpty()) {
			return null;
		} else {
			return result;
		}
	}

	public void removeOldAction(Action a) {
		String stringAction = createStringFromAction(a);
		oldActionsByTypeMap.remove(stringAction);
	}

	public Collection getActionsOfType(String type) {
		return (Collection) actionTypeMap.get(type);
	}

/*
  public Map displayInPriorityOrder () {
    return priorityMap.displayNumbered ();
  }
*/
	public ArrayList listInPriorityOrder() {
		return priorityMap.listInPriorityOrder();
	}

/*
  public Map displayInLifoOrder () {
    Collections.reverse ((AbstractList)lifoActionMap);
    return DisplayUtilities.displayNumbered (lifoActionMap);
  }
*/
	public int getNumberOfActionsInList() {
		return lifoActionMap.size();
	}

	public int getCurrentPriority() {
		return priorityMap.getCurrentPriority();
	}

	public void incrementCurrentPriority() {
		priorityMap.incrementCurrentPriority();
	}

	public Object changePriorityToCurrent(Action a) {
		return priorityMap.changePriorityToCurrent(a);
	}

	public void setPriority(Action a, int value) {
		priorityMap.setPriority(a, value);
	}
}
