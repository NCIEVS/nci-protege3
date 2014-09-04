 /*
  * Contributor(s): Sean Falconer seanf@uvic.ca
  */

package edu.stanford.smi.protegex.prompt.plugin.util;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.actionLists.*;
import edu.stanford.smi.protegex.prompt.plugin.model.*;

/**
 * Abstract class for exposing an API for plugins to access Prompt's internal data structures. 
 * @author seanf
 */
public abstract class KnowledgeBaseFacade {
	
	/**
	 * Gets the knowledge base that is constructed during a mapping/merging.
	 * @return A KnowledgeBaseInMerging object representing the mapped/merged project.
	 */
	public static KnowledgeBaseInMerging getMappingKnowledgeBaseInMerging () {
		return ProjectsAndKnowledgeBases.getFirstMappingKnowledgeBaseInMerging();
	}
	
	/**
	 * Gets the constructed mapping KnowledgeBase object.
	 * @return A KnowledgeBase object.
	 */
	public static MappingStoragePlugin getMappingKnowledgeBase () {
		return ProjectsAndKnowledgeBases.getFirstMappingStoragePlugin();
	}
	
	/**
	 * Gets the KnowledgeBaseInMerging based on the prettyName key.
	 * @param prettyName A key associated with each project.
	 * @return A KnowledgeBaseInMerging object associated with prettyName.
	 */
	public static KnowledgeBaseInMerging getKnowledgeBaseInMerging (String prettyName) {
		return ProjectsAndKnowledgeBases.getKnowledgeBaseInMerging(prettyName);
	}
	
	/**
	 * Gets the KnowledgeBaseInMerging based on the KnowledgeBase key.
	 * @param kb The KnowledgeBase associated with a KnowledgeBaseInMerging object.
	 * @return A KnowledgeBaseInMerging object associated with given KnowledgeBase.
	 */
	public static KnowledgeBaseInMerging getKnowledgeBaseInMerging(KnowledgeBase kb) {
		return ProjectsAndKnowledgeBases.getKnowledgeBaseInMerging(kb);
	}
	
	/**
	 * Gets the target knowledge base as a KnowledgeBaseInMerging object.
	 * @return A KnowledgeBaseInMerging object.
	 */
	public static KnowledgeBaseInMerging getMappingTargetKnowledgeBaseInMerging() {
		return ProjectsAndKnowledgeBases.getMappingTargetKnowledgeBaseInMerging();
	}

	/**
	 * Gets the source knowledge base as a KnowledgeBaseInMerging object.
	 * @return A KnowledgeBaseInMerging object.
	 */
	public static KnowledgeBaseInMerging getMappingSourceKnowledgeBaseInMerging() {
		return ProjectsAndKnowledgeBases.getMappingSourceKnowledgeBaseInMerging();
	}
	
	/**
	 * Gets the first mapping source that is used during a merge.
	 * @return A KnowledgeBaseInMerging object.
	 */
	public static KnowledgeBaseInMerging getMappingSource1KnowledgeBaseInMerging() {
		return ProjectsAndKnowledgeBases.getMappingSource1KnowledgeBaseInMerging();
	}
	
	/**
	 * Gets the second mapping source that is used during a merge.
	 * @return A KnowledgeBaseInMerging object.
	 */
	public static KnowledgeBaseInMerging getMappingSource2KnowledgeBaseInMerging() {
		return ProjectsAndKnowledgeBases.getMappingSource2KnowledgeBaseInMerging();
	}
	
	/**
	 * Gets the list of suggestions.
	 * @return A collection of suggestions to do
	 */
	public static Collection getToDoListInPriorityOrder() {
		return SuggestionsAndConflicts.getTodoListInPriorityOrder();
	}
}
