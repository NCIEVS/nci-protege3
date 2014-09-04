 /*
  * Contributor(s): Sean Falconer seanf@uvic.ca
  * 				  Natasha Noy noy@stanford.edu
  */

package edu.stanford.smi.protegex.prompt.plugin.model;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.explanation.*;
import edu.stanford.smi.protegex.prompt.operation.*;
import edu.stanford.smi.protegex.prompt.plugin.*;
import edu.stanford.smi.protegex.prompt.plugin.ui.*;

/**
 * Interface for plugin's to provide their own comparison algorithms.
 * @author seanf
 */
public interface ComparisonAlgorithmPlugin extends PromptAlgorithmPlugin {
	/**
	 * Called by the PluginManager's fireAlignmentEvent for the currently active comparison
	 * algorithm plugin.  This is where an algorithm plugin would process the knowledge bases
	 * and compute an alignment for them.
	 * @param progress The progress object
	 * @param sourceKnowledgeBase Source knowledge base
	 * @param targetKnowledgeBase Target knowledge base
	 * @param mappingKnowledgeBase The predefined mappings
	 * @return A collection of CandidateMapping objects
	 */
	public Collection performAlignment(AlgorithmProgressMonitor progress, KnowledgeBase sourceKnowledgeBase, KnowledgeBase targetKnowledgeBase);
	
	/**
	 * Gets the plugin's configuration panel.  This is where a plugin could provide their own
	 * configuration screen.
	 * @return The plugin's configuration screen.
	 */
	public ComparisonAlgorithmPluginConfigurationPanel getConfigurationComponent();
	
	/**
	 * Called to validate the configuration screen.  This will be called when a user clicks the button
	 * to perform the alignment.  The plugin can use this method to validate the configuration information
	 * they provide in the configuration screen.
	 * @return True if everything is ok and we should proceed with the alignment.
	 */
	public boolean validateConfigSettings();
	
	public class CandidateMapping {
		Frame _f1 = null;
		Frame _f2 = null;
		int _score = 1;
		Explanation _explanation = null;
		
		public CandidateMapping (Frame f1, Frame f2, int score, Explanation explanation) {
			_f1 = f1;
			_f2 = f2;
			_score = score;
			_explanation = explanation;
		}
		
		public CandidateMapping (Frame f1, Frame f2, Explanation explanation) {
			_f1 = f1;
			_f2 = f2;
			_explanation = explanation;
		}
		
		public CandidateMapping (Frame f1, Frame f2) {
			_f1 = f1;
			_f2 = f2;
		}
		
		public Operation convertToMergeOperation () {
			return MergeClsesOperation.selectMergeOperation(_f1, _f2, _explanation);
		}
	}
}
