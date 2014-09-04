/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.promptx.umls;
  
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.plugin.*;
import edu.stanford.smi.protegex.prompt.plugin.model.*;
import edu.stanford.smi.protegex.prompt.plugin.ui.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class UMLSPlugin implements ComparisonAlgorithmPlugin {
	private static final String PLUGIN_DIRECTORY = "edu.stanford.smi.protegex.promptx.umls";

	private UMLSPluginConfigurationPanel _configPanel = new UMLSPluginConfigurationPanel ();
	private KnowledgeBase _sourceKb = null;
	private KnowledgeBase _targetKb = null;
	
	UMLSSourceWrapper _umlsWrapper = null;
	
	public String getPluginName() {
		return "Using UMLS concept identifiers for matching";
	}
	
	public Collection performAlignment(AlgorithmProgressMonitor progress, KnowledgeBase sourceKnowledgeBase, KnowledgeBase targetKnowledgeBase) {
		Log.getLogger().info("Starting UMLS-based matching: " + new Date ());
		progress.setProgressText("Starting matching with using UMLS cuis");
		_configPanel.setDBConfigurationParameters();
		_umlsWrapper = new UMLSDBConnector ();
//		_umlsWrapper = new UMLSKSSConnector ();
		
		if (!_umlsWrapper.initialized ()) {
			progress.setCompleted (true);
			return null;
		}
		
		_sourceKb = sourceKnowledgeBase;
		_targetKb = targetKnowledgeBase;
		Collection clses1 = Util.getLocalClses(sourceKnowledgeBase);
		Collection clses2 = Util.getLocalClses(targetKnowledgeBase);
		
		progress.setProgressText("Finding candidates");
		
		Collection matches = findMappingCandidates (clses1, clses2);
		
		progress.setProgressText("Alignment complete");
		progress.setCompleted(true);
		
		return matches;
		
	}
	
	private Collection findMappingCandidates (Collection clses1, Collection clses2) {
 
		getCUIs (clses1);
		getCUIs (clses2);
		
		Collection result = compareCUIs ();
		
		return result;
	}
	
	private  CUIMap _cuisToClasses = new CUIMap (); // <cui; collection of classes from two sources>
	
	private  void getCUIs (Collection clses) {
		Log.getLogger().info("**** Getting CUIs for " + ((Cls)CollectionUtilities.getFirstItem(clses)).getKnowledgeBase() + " ****");
		int count = 0;
		Collection <String> classesWithNoCUIs = new ArrayList <String> ();
		
		Iterator i = clses.iterator();
		while (i.hasNext()) {
			Cls next = (Cls)i.next();
			String nextCui = getCUIforCls (next);
			if (count % 1000 == 0)
				Log.getLogger().info("[" + count + "] Processing class: " + next.getBrowserText() + "; cui: " + nextCui);
			if (nextCui != null)
				_cuisToClasses.addValue(nextCui, next);
			else
				classesWithNoCUIs.add (next.getBrowserText());
			count++;
		}
		Log.getLogger().info("**** Number of classes with no CUIs: " + classesWithNoCUIs.size() + "/" + count);
//		printStringCollection (classesWithNoCUIs);
	}
	
	private void printStringCollection (Collection strs) {
		Iterator i = strs.iterator();
		int count = 0;
		while (i.hasNext()) {
			String next = (String)i.next();
			System.out.println(next.replace('_', ' '));
			count++;
			if (count % 1000 == 0)
				SystemUtilities.pause();
		}
		SystemUtilities.pause();
	}
	
	private  String getCUIforCls (Cls cls) {
		String term = cls.getBrowserText();		
       	term = term.replace('_', ' ');
		
		String cui =  _umlsWrapper.getCUIforTerm (term);
		return cui;
	}
	

	
	private  Collection compareCUIs () {
		Collection <CandidateMapping> result = new ArrayList <CandidateMapping> ();
		Collection cuis = _cuisToClasses.getKeys();
		int count = 0;
		Iterator c = cuis.iterator();
		while (c.hasNext()) {
			String nextCui = (String)c.next();
			KBArray next = _cuisToClasses.getValue (nextCui);
			if (count % 1000 == 0) 
				Log.getLogger().info("[" + count + "] Comparing cuis: " + nextCui + "; terms: " + next.getClses(_sourceKb) + " from " + _sourceKb + " and " + next.getClses(_targetKb) + " from " + _targetKb);
			count++;
			
			Collection sourceClsesForNextCui = next.getClses(_sourceKb);
			Collection targetClsesForNextCui = next.getClses(_targetKb);
			
			if ((sourceClsesForNextCui == null) || (targetClsesForNextCui == null)) continue;
			
			Iterator s = sourceClsesForNextCui.iterator();
			while (s.hasNext()) {
				Cls nextSourceCls = (Cls)s.next();
				Iterator t = targetClsesForNextCui.iterator();
				while (t.hasNext()) {
					Cls nextTargetCls = (Cls)t.next();
					result.add(new CandidateMapping (nextSourceCls, nextTargetCls, new IdenticalUMLSCUI () ));
				}
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
	
	public class CUIMap {
		private  HashMap <String, KBArray> _map = new HashMap <String, KBArray> ();
		
		
		public void addValue (String key, Cls cls) {
			KBArray currentValue = (KBArray)_map.get(key);
			if (currentValue == null) 
				currentValue = new KBArray ();

			currentValue.add (cls);
			_map.put(key, currentValue);
		}
		
		public Collection getKeys () {
			return _map.keySet();
		}
		
		public KBArray getValue (String key) {
			return (KBArray)_map.get(key);
		}
		
	}
	
	// a pair of two lists -- classes from each of the two kbs with the same CUI
	public class KBArray {
		ListMultiMap _kbs = new ListMultiMap (2); //<kb, list of classes>
		
		public void add (Cls cls) {
			KnowledgeBase kb = cls.getKnowledgeBase();
			_kbs.addValue(kb, cls);
		}
		
		public Collection getClses (KnowledgeBase kb) {
			return _kbs.getValues(kb);
		}
		
	}

	public static String getPluginDirectoryName() {
		return PLUGIN_DIRECTORY;
	}


	
}
