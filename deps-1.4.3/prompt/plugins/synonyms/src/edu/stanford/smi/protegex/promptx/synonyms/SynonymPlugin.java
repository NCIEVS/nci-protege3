/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.promptx.synonyms;
  
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.plugin.*;
import edu.stanford.smi.protegex.prompt.plugin.model.*;
import edu.stanford.smi.protegex.prompt.plugin.ui.*;
import edu.stanford.smi.protegex.prompt.util.*;
import edu.stanford.smi.protegex.promptx.synonyms.explanation.*;

public class SynonymPlugin implements ComparisonAlgorithmPlugin {
	private static final String PLUGIN_DIRECTORY = "edu.stanford.smi.protegex.promptx.synonyms";
	
	private SynonymPluginConfigurationPanel _configPanel = new SynonymPluginConfigurationPanel ();
	private Slot _sourceSynonymSlot = null;
	private Slot _targetSynonymSlot = null;
	
	public String getPluginName() {
		return "Lexical matching with synonyms";
	}
	
	public Collection performAlignment(AlgorithmProgressMonitor progress, KnowledgeBase sourceKnowledgeBase, KnowledgeBase targetKnowledgeBase) {
		progress.setProgressText("Starting lexical matching with synonyms");
		Collection clses1 = Util.getLocalClses(sourceKnowledgeBase);
		Collection clses2 = Util.getLocalClses(targetKnowledgeBase);
		
		if (_configPanel.sourceSynonymSlot() != null) {
			_sourceSynonymSlot = sourceKnowledgeBase.getSlot(_configPanel.sourceSynonymSlot());
			if (_sourceSynonymSlot == null) {
				progress.setCompleted(true);
				JOptionPane.showMessageDialog(null, "Specified synonym slot in the source ontology does not exist", "Invalid synonym slot", JOptionPane.OK_OPTION);
				return null;
			}
		}
		if (_configPanel.targetSynonymSlot() != null) {
			_targetSynonymSlot = targetKnowledgeBase.getSlot(_configPanel.targetSynonymSlot());
			if (_targetSynonymSlot == null) {
				progress.setCompleted(true);	
				JOptionPane.showMessageDialog(null, "Specified synonym slot in the target ontology does not exist", "Invalid synonym slot", JOptionPane.OK_OPTION);
				return null;
			}
		}
		
		
		progress.setProgressText("Finding candidates");
		
		Collection matches = findMappingCandidates (clses1, clses2);
		
		progress.setProgressText("Alignment complete");
		progress.setCompleted(true);
		
		return matches;
		
	}
	
	private Collection findMappingCandidates (Collection clses1, Collection clses2) {
		Collection namesAndSynonyms1 = createNamesAndSynonyms (clses1, _sourceSynonymSlot);
		Collection namesAndSynonyms2 = createNamesAndSynonyms (clses2, _targetSynonymSlot);
		
		if (_configPanel.approximateMatch())
			return findMappingCandidates_ApproximateMatch (namesAndSynonyms1, namesAndSynonyms2);
		else
			return findMappingCandidates_ExactMatch (namesAndSynonyms1, namesAndSynonyms2);
			
	}
	
	private Collection<TermAndClass> createNamesAndSynonyms (Collection<Cls> clses, Slot synonymSlot) {
		Collection <TermAndClass> result = new ArrayList <TermAndClass>();
		Iterator i = clses.iterator();
		while (i.hasNext()) {
			Cls next = (Cls)i.next();
			result.add(new TermAndClass (next, Util.getLocalBrowserText(next)));
			if (synonymSlot != null)  {
				Collection nextSynonyms = next.getOwnSlotValues(synonymSlot);
				if (nextSynonyms != null) {
					Iterator s = nextSynonyms.iterator();
					while (s.hasNext()) {
						Object nextSynonym = s.next();
						if (nextSynonym instanceof Frame)
							result.add (new TermAndClass(next, Util.getLocalBrowserText ((Frame)nextSynonym)));
						else
							result.add(new TermAndClass (next, nextSynonym.toString()));
					}
				}
			}
		}
		return result;
	}
	
	public class TermAndClass {
		private Cls _cls;
		private String _term ;
		
		public TermAndClass (Cls cls, String term) {
			_cls = cls;
			_term = term;
		}
		
		public String getTerm () {
			return _term;
		}
		
		public Cls getCls () {
			return _cls;
		}
		
	}
	
	private Collection findMappingCandidates_ApproximateMatch (Collection namesAndSynonyms1, Collection namesAndSynonyms2) {
		Collection <CandidateMapping> result = new ArrayList <CandidateMapping> ();

		Iterator i1 = namesAndSynonyms1.iterator();
		Iterator i2;

		TermAndClass next1, next2;
		int comparisonResult;
		boolean foundMatch;

		while (i1.hasNext()) {
			next1 = (TermAndClass) i1.next();
			i2 = namesAndSynonyms2.iterator();
			foundMatch = false;
			while (i2.hasNext()) {
				next2 = (TermAndClass) i2.next();
				comparisonResult = CompareNames.compareNames(next1.getTerm(), next2.getTerm());
				if (comparisonResult == CompareNames.EQUAL) {
					result.add(new CandidateMapping (next1.getCls(), next2.getCls(), new IdenticalMatchForNameOrSynonym()));
					foundMatch = true;
				}
				if (comparisonResult == CompareNames.APPROXIMATE_MATCH) {
					result.add(new CandidateMapping (next1.getCls(), next2.getCls(), new SimilarNameOrSynonym()));
					foundMatch = true;
				}
			}
		}

		return result;
	
	}
		
	private Collection findMappingCandidates_ExactMatch (Collection namesAndSynonyms1, Collection namesAndSynonyms2) {
		Collection <CandidateMapping> result = new ArrayList <CandidateMapping> ();
		
		TermComparator tc = new TermComparator ();
		Object[] v1 = namesAndSynonyms1.toArray();
		Object[] v2 = namesAndSynonyms2.toArray();

		Arrays.sort(v1, tc);
		Arrays.sort(v2, tc);

		int i1 = 0;
		int i2 = 0;
		TermAndClass next1, next2;

		while (i1 < v1.length && i2 < v2.length) {
			next1 = (TermAndClass) v1[i1];
			next2 = (TermAndClass) v2[i2];

			int comp = CompareNames.compareNames(next1.getTerm(), next2.getTerm());
			if (comp < 0) {
				i1++;
			} else if (comp > 0) {
				i2++;
			} else if (next1.getTerm().equals(next2.getTerm())) { // they are equal
				result.add(new CandidateMapping (next1.getCls(), next2.getCls(), new IdenticalMatchForNameOrSynonym()));
				i1++;
				i2++;
			} else {// case or delimiters may be difirent
				result.add(new CandidateMapping (next1.getCls(), next2.getCls(), new SimilarNameOrSynonym()));
				i1++;
				i2++;
			}
		}
		return result;
	}

	
	public void invokePlugin()  {
		
	}
	
	
	public ComparisonAlgorithmPluginConfigurationPanel getConfigurationComponent() {
		return _configPanel;
	}
	
	
	public boolean validateConfigSettings() {
		return true;
	}
	
	public class TermComparator implements Comparator {
//	    private boolean _reverseStrings = false;
//	    private boolean _trueComparison = false;

	    public TermComparator () {
	    	super ();
	    }

	    public int compare (Object o1, Object o2) {
	            return CompareNames.compareNamesWithExactMatch (((TermAndClass)o1).getTerm(), ((TermAndClass)o2).getTerm());
	    }
	}

	public String getPluginDirectoryName() {
		return PLUGIN_DIRECTORY;
	}

}
