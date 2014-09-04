/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 *                 Kyle Bruck kbruck@stanford.edu
 *                 Sean Falconer seanf@uvic.ca
 */

package edu.stanford.smi.protegex.prompt.actionLists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.prompt.Preferences;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.TraversalDirective;
import edu.stanford.smi.protegex.prompt.event.PromptListenerManager;
import edu.stanford.smi.protegex.prompt.explanation.IdenticalNames;
import edu.stanford.smi.protegex.prompt.explanation.SimilarNames;
import edu.stanford.smi.protegex.prompt.operation.KeepClsOperation;
import edu.stanford.smi.protegex.prompt.operation.MergeClsesOperation;
import edu.stanford.smi.protegex.prompt.operation.MergeFramesOperation;
import edu.stanford.smi.protegex.prompt.operation.Operation;
import edu.stanford.smi.protegex.prompt.plugin.AlgorithmProgressMonitor;
import edu.stanford.smi.protegex.prompt.plugin.PluginManager;
import edu.stanford.smi.protegex.prompt.plugin.model.ComparisonAlgorithmPlugin.CandidateMapping;
import edu.stanford.smi.protegex.prompt.util.CompareNames;
import edu.stanford.smi.protegex.prompt.util.FrameNameComparator;
import edu.stanford.smi.protegex.prompt.util.Util;

public class InitialToDoList extends Thread {
	private ArrayList _matches;

	private KnowledgeBase _kb1;
	private KnowledgeBase _kb2;

	private HashSet _keepers1;
	private HashSet _keepers2;

	private HashSet _roots1;
	private HashSet _roots2;

	/**
	 * progress monitor passed to the progress dialog and used by plugins and Prompt algorithms to
	 * notify the user about the current progress
	 */
	private AlgorithmProgressMonitor _progressMonitor;

	public InitialToDoList() {}

	public InitialToDoList(KnowledgeBase kb1, KnowledgeBase kb2) {
		_kb1 = kb1;
		_kb2 = kb2;
		_matches = new ArrayList();

		/*
		 * KB--Used to run the plugin alignment matching. Collection
		 * alignmentResults = doPluginAlignment(); if(true) _matches =
		 * (ArrayList)processAlignmentResults(clses1, clses2, alignmentResults);
		 * else _matches = (ArrayList)findCandidatesForMerging (clses1, clses2);
		 */

		// show the progress dialog
		_progressMonitor = new AlgorithmProgressMonitor();
		if(!PromptTab.initializeSilently) {
			PromptTab.displayProgressMonitor(_progressMonitor);
		}
	}

	public void run() {
		Collection clses1 = Util.getLocalClses(_kb1);
		Collection clses2 = Util.getLocalClses(_kb2);

		_progressMonitor.setProgressText("Finding candidates");

		_matches = (ArrayList) findCandidatesForMerging(clses1, clses2);

		// if the matches are null, kill this thread
		if (_matches == null) {
			PromptListenerManager.fireTaskComplete(true);
		}
		Log.getLogger().info("Number of initial mapping candidates: " + _matches.size());

		SuggestionsAndConflicts.addSuggestions(_matches, false);

		if (PromptTab.merging()) {
			Collection keepOperations1, keepOperations2, keepTreeOperations1, keepTreeOperations2;

			findTreesToKeep(_kb1);
			findTreesToKeep(_kb2);

			keepTreeOperations1 = keepTheTrees(_roots1);
			keepTreeOperations2 = keepTheTrees(_roots2);

			SuggestionsAndConflicts.addSuggestions(keepTreeOperations1, false);
			SuggestionsAndConflicts.addSuggestions(keepTreeOperations2, false);

			if (_keepers1 != null) {
				keepOperations1 = keepTheKeepers(_keepers1);
				SuggestionsAndConflicts.addSuggestions(keepOperations1, false);
			}
			if (_keepers2 != null) {
				keepOperations2 = keepTheKeepers(_keepers2);
				SuggestionsAndConflicts.addSuggestions(keepOperations2, false);
			}
		}

		_progressMonitor.setProgressText("Alignment complete");

		// let the UI thread know that we're done executing
		PromptListenerManager.fireTaskComplete(false);
	}

	Collection get() {
		return _matches;
	}

	Collection _treesToKeep;

	private void findTreesToKeep(KnowledgeBase kb) {
		Cls root = kb.getRootCls();
		HashSet roots = new HashSet();
		Collection inTrees = new HashSet();

		Collection keepers;
		Collection visited = new ArrayList();
		if (kb == _kb1) {
			keepers = _keepers1;
		} else {
			keepers = _keepers2;
		}

		if (keepers != null) {
			findTreesToKeep(root, keepers, roots, inTrees, visited);
			keepers.removeAll(roots);
			keepers.removeAll(inTrees);
		}

		if (kb == _kb1) {
			_roots1 = roots;
		} else {
			_roots2 = roots;
		}
	}

	/**
	 * Fires the alignment event for an algorithm plugin.
	 * 
	 * @return A collection of suggested operations for the user to perform.
	 */
	private Collection doPluginAlignment() {
		Collection candidates = PluginManager.getInstance().fireAlignmentEvent(_progressMonitor, _kb1, _kb2);
		if (candidates == null) {
			return null;
		}
		return convertCandidateMappingsToMergeOperations(candidates);
	}

	private void findTreesToKeep(Cls root, Collection keepers, Collection roots, Collection inTrees, Collection visited) {
		visited.add(root);
		Collection subclasses = Util.getDirectSubclasses(root);
		if (roots.contains(root) || inTrees.contains(root)) {
			return;
		}
		if (subclasses == null || subclasses.size() == 0) {
			//it's a leaf
			if (keepers.contains(root)) {
				roots.add(root);
			}
		}
		Iterator i = subclasses.iterator();
		boolean shouldCopyTree = keepers.contains(root) && !Util.isSystem(root);
		while (i.hasNext()) {
			Cls next = (Cls) i.next();
			if (visited.contains(next)) {
				continue;
			}
			findTreesToKeep(next, keepers, roots, inTrees, visited);
			if (!(roots.contains(next) || inTrees.contains(next))) {
				shouldCopyTree = false;
			}
		}
		if (shouldCopyTree) {
			roots.add(root);
			roots.removeAll(subclasses);
			inTrees.addAll(subclasses);
		}
	}

	private Collection findCandidatesForMerging(Collection c1, Collection c2) {
		if (Preferences.pluginMatch()) {
			return doPluginAlignment();
		} else if (Preferences.approximateMatch()) {
			_progressMonitor.setProgressText("Computing approximate match");

			return findCandidatesForMergingWithApproximateMatch(c1, c2);
		} else {
			_progressMonitor.setProgressText("Computing exact match");

			return findCandidatesForMergingWithExactMatch(c1, c2);
		}
	}

	private Collection convertCandidateMappingsToMergeOperations(Collection candidateMappings) {
		Iterator i = candidateMappings.iterator();
		Collection result = new ArrayList();
		while (i.hasNext()) {
			result.add(((CandidateMapping) i.next()).convertToMergeOperation());
		}
		return result;
	}

	private Collection findCandidatesForMergingWithApproximateMatch(Collection c1, Collection c2) {
		if (c1 == null) {
			_keepers1 = null;
			_keepers2 = new HashSet(c2);
			return null;
		}

		if (c2 == null) {
			_keepers1 = new HashSet(c1);
			_keepers2 = null;
			return null;
		}

		Collection result = new ArrayList();

		_keepers1 = new HashSet();

		Iterator i1 = c1.iterator();
		Iterator i2;

		Cls next1, next2;
		int comparisonResult;
		Collection temp = new HashSet();
		boolean foundMatch;

		while (i1.hasNext()) {
			next1 = (Cls) i1.next();
			i2 = c2.iterator();
			foundMatch = false;
			while (i2.hasNext()) {
				next2 = (Cls) i2.next();

				comparisonResult = CompareNames.compareNames(Util.getLocalBrowserText(next1), Util.getLocalBrowserText(next2));
				if (comparisonResult == CompareNames.EQUAL || comparisonResult == CompareNames.APPROXIMATE_MATCH) {
					result.add(MergeClsesOperation.createOperation(next1, next2, comparisonResult));
					foundMatch = true;
					temp.add(next2);
				}
			}
			if (!foundMatch) {
				_keepers1.add(next1);
			}
		}

		c2.removeAll(temp);
		_keepers2 = new HashSet(c2);
		return result;
	}

	Collection findCandidatesForMergingWithExactMatch(Collection c1, Collection c2) {
		Collection result = new ArrayList();

		_keepers1 = new HashSet();
		_keepers2 = new HashSet();

		FrameNameComparator fc = new FrameNameComparator();

		Object[] v1 = c1.toArray();
		Object[] v2 = c2.toArray();

		Arrays.sort(v1, fc);
		Arrays.sort(v2, fc);

		int i1 = 0;
		int i2 = 0;
		Frame next1, next2;

		while (i1 < v1.length && i2 < v2.length) {
			next1 = (Frame) v1[i1];
			next2 = (Frame) v2[i2];

			int comp = CompareNames.compareNames(Util.getLocalBrowserText(next1), Util.getLocalBrowserText(next2));

			if (comp < 0) {
				_keepers1.add(next1);
				i1++;
			} else if (comp > 0) {
				_keepers2.add(next2);
				i2++;
			} else if (Util.getLocalBrowserText(next1).equals(Util.getLocalBrowserText(next2))) { // they are equal
				result.add(MergeFramesOperation.selectMergeOperation(next1, next2, new IdenticalNames()));
				i1++;
				i2++;
			} else {// case or delimiters may be difirent
				result.add(MergeFramesOperation.selectMergeOperation(next1, next2, new SimilarNames()));
				i1++;
				i2++;
			}
		}

		for (; i1 < v1.length; i1++) {
			_keepers1.add(v1[i1]);
		}

		for (; i2 < v2.length; i2++) {
			_keepers2.add(v2[i2]);
		}

		return result;

	}

	Collection keepTheKeepers(Collection keepers) {
		Collection result = new ArrayList();
		Iterator i = keepers.iterator();
		while (i.hasNext()) {
			result.add(new KeepClsOperation((Frame) i.next()));
		}
		return result;
	}

	Collection keepTheTrees(HashSet roots) {
		Collection result = new ArrayList();
		if (roots != null && !roots.isEmpty()) {
			List rootsSorted = new ArrayList(roots);
			Collections.sort(rootsSorted, new FrameNameComparator());
			Iterator i = rootsSorted.iterator();
			while (i.hasNext()) {
				Cls next = (Cls) i.next();
				boolean copyInstances = (Util.getDirectSubclasses(next).size() != 0);
				Operation nextOperation = new KeepClsOperation(next, new TraversalDirective(next, false, copyInstances), false);
				result.add(nextOperation);
			}
		}
		return result;
	}

	private void matchesForLog() {
		Iterator i = _matches.iterator();
		Operation next;
		while (i.hasNext()) {
			next = (Operation) i.next();
			Log.getLogger().info("" + next);
			Log.getLogger().info("" + next.getReason());
		}

	}
}
