/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt;

import java.io.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

public class ProcessResults {
//    static final private String RESULTS_PROJECT =
//    	"d:\\Anchor-Prompt\\researchProjectOnts\\experiments\\results\\resultsUMD_ATLAS.pprj";
//    static final private String RESULTS_PROJECT =
//    	"d:\\Anchor-Prompt\\researchProjectOnts\\experiments\\results\\resultsUMD_SWRC.pprj";
    static final private String RESULTS_PROJECT =
    	"d:\\Anchor-Prompt\\researchProjectOnts\\experiments\\results\\resultsSWRC_ATLAS.pprj";
	static final private String RESULTS_FILE = "d:\\Anchor-Prompt\\researchProjectOnts\\experiments\\result";
	static private PrintStream _resultsFile = null;

    static private KnowledgeBase _resultsKb;
    static private Project _resultsProject;

    static private boolean outputToFile = false;
    static private boolean outputToProject = true;

    static {
        openProjectAndLogFile ();
    }

	static public void processResults () {

        Cls experimentCls = _resultsKb.getCls("Experiment");
        Collection experimentInstances = experimentCls.getInstances();
        processExperimentInstances (experimentInstances);

        closeProjectAndLogFile ();
    }

    // all instances of the Experiment class
    static private void processExperimentInstances (Collection instances) {
     	if (instances == null) return;
        Iterator i = instances.iterator();
        while (i.hasNext()) {
         	Instance next = (Instance)i.next();
            processExperimentInstance (next);
        }
    }

    static private Slot _resultsSlot = _resultsKb.getSlot ("experiment results");
    static private Cls _analysisCls = _resultsKb.getCls ("Analysis");
    static private Slot _analysisSlot = _resultsKb.getSlot ("experiment analysis");

    static private Slot _numberOfResultsSlot = _resultsKb.getSlot ("number of results");
    static private Slot _correctResultsSlot = _resultsKb.getSlot ("correct results");
    static private Slot _equivalentResultsSlot = _resultsKb.getSlot ("equivalent results");

    static private Slot _correctResultsAboveMedianSlot = _resultsKb.getSlot ("correct results above median");
    static private Slot _equivalentResultsAboveMedianSlot = _resultsKb.getSlot ("equivalent results above median");
    static private Slot _numberOfResultsAboveMedianSlot = _resultsKb.getSlot ("results above median");

    static private Slot _ratioOfCorrectResultsSlot = _resultsKb.getSlot ("ratio of correct results");
    static private Slot _ratioOfCorrectResultsAboveMedianSlot = _resultsKb.getSlot ("ratio of correct results above median");
    static private Slot _ratioOfEquivalentResultsSlot = _resultsKb.getSlot ("ratio of equivalent results");
    static private Slot _ratioOfEquivalentResultsAboveMedianSlot = _resultsKb.getSlot ("ratio of equivalent results above median");


    // inst is an instance of Experiment class
    static private void processExperimentInstance (Instance inst) {
        Instance analysisInstance = _resultsKb.createInstance(null, _analysisCls);
        inst.setOwnSlotValue(_analysisSlot, analysisInstance);

    	if (noResults (inst)) {
       		processNoResults (inst);
            return;
        }

     	Collection results = inst.getOwnSlotValues (_resultsSlot);

        int numberOfResults = results.size();
        int correctResults = 0;
        int equivalentResults = 0;

        int correctResultsAboveMedian = 0;
        int equivalentResultsAboveMedian = 0;
        int numberOfResultsAboveMedian = 0;

        Integer median = findMedian (results);

        Iterator i = results.iterator();
        while (i.hasNext()) {
        	// instance of Score table element class
         	Instance next = (Instance)i.next();
            if (correctResult (next))
            	correctResults++;
            if (equivalentResult (next))
            	equivalentResults++;
            if (aboveMedian (next, median)) {
                numberOfResultsAboveMedian++;
            	if (correctResult (next))
            		correctResultsAboveMedian++;
            	if (equivalentResult (next))
            		equivalentResultsAboveMedian++;
            }
        }

        float ratioOfCorrectResults = ((float)correctResults)/numberOfResults;
		float ratioOfCorrectResultsAboveMedian =
        	(numberOfResultsAboveMedian == 0) ? 0 : ((float)correctResultsAboveMedian)/numberOfResultsAboveMedian;

        float ratioOfEquivalentResults = ((float)equivalentResults)/numberOfResults;
        float ratioOfEquivalentResultsAboveMedian =
        	(equivalentResultsAboveMedian == 0) ? 0 : ((float)equivalentResultsAboveMedian)/numberOfResultsAboveMedian;

        analysisInstance.setOwnSlotValue (_numberOfResultsSlot, new Integer (numberOfResults));
        analysisInstance.setOwnSlotValue (_correctResultsSlot, new Integer (correctResults));
        analysisInstance.setOwnSlotValue (_equivalentResultsSlot, new Integer (equivalentResults));

        analysisInstance.setOwnSlotValue (_correctResultsAboveMedianSlot, new Integer (correctResultsAboveMedian));
        analysisInstance.setOwnSlotValue (_equivalentResultsAboveMedianSlot, new Integer (equivalentResultsAboveMedian));
        analysisInstance.setOwnSlotValue (_numberOfResultsAboveMedianSlot, new Integer (numberOfResultsAboveMedian));

        analysisInstance.setOwnSlotValue (_ratioOfCorrectResultsSlot, new Float (ratioOfCorrectResults));
        analysisInstance.setOwnSlotValue (_ratioOfCorrectResultsAboveMedianSlot, new Float (ratioOfCorrectResultsAboveMedian));
        analysisInstance.setOwnSlotValue (_ratioOfEquivalentResultsSlot, new Float (ratioOfEquivalentResults));
        analysisInstance.setOwnSlotValue (_ratioOfEquivalentResultsAboveMedianSlot, new Float (ratioOfEquivalentResultsAboveMedian));

    }

    static private Slot _scoreSlot = _resultsKb.getSlot("score");

    // instance of Score Table Element
    static private boolean aboveMedian (Instance inst, Integer median) {
    	int score = ((Integer)inst.getOwnSlotValue(_scoreSlot)).intValue();
        return score >= median.intValue();
    }

    static private Slot _correctnessSlot = _resultsKb.getSlot("correctness");

    // instance of Score Table Element
    static private boolean correctResult (Instance inst) {
    	String correctness = (String)inst.getOwnSlotValue(_correctnessSlot);
        return (ScoreTableElement.isEquivalentValue (correctness) ||
                ScoreTableElement.isSubclassSuperclassValue (correctness));
    }

     // instance of Score Table Element
    static private boolean equivalentResult (Instance inst) {
    	String correctness = (String)inst.getOwnSlotValue(_correctnessSlot);
        return (ScoreTableElement.isEquivalentValue (correctness));
    }

   // collection of instances of Score Table Element
    static private Integer findMedian (Collection results) {
		HashSet scores = new HashSet ();
        Iterator i = results.iterator();
        while (i.hasNext()) {
         	Instance next = (Instance)i.next();
            scores.add (next.getOwnSlotValue(_scoreSlot));
        }
        return median (scores);
    }

    // a set of numbers
    static private Integer median (Set scores) {
    	Object [] scoresOrdered = scores.toArray();
        Arrays.sort(scoresOrdered);
        for (int i = 0, j = scoresOrdered.length - 1; ; i++, j--) {
         	if (i >= j)
            	return  (Integer)scoresOrdered [i];
        }
//        return 0;
    }

    static private boolean noResults (Instance inst) {
        Integer zero = new Integer (0);
     	Collection results = inst.getOwnSlotValues (_resultsSlot);
        Instance firstResult = (Instance)CollectionUtilities.getFirstItem(results);
        return (firstResult.getOwnSlotValue (_scoreSlot)).equals (zero);
    }

    static private void processNoResults (Instance inst) {
     	// do nothing - default values are ok
    }

    static private void  closeProjectAndLogFile () {
    	if (outputToProject)
			_resultsProject.save(new ArrayList());
        if (outputToFile)
        	_resultsFile.close();
  	}


    static private void  openProjectAndLogFile () {
    	if (outputToProject)
        	openResultsProject();
        if (outputToFile)
    		openResultsFile ();
    }


    static private void openResultsProject () {
     	_resultsProject = new Project (RESULTS_PROJECT, new ArrayList());
        _resultsKb = _resultsProject.getKnowledgeBase();
    }

  	static private void openResultsFile () {
    	try {
        	int index = 0;
            while ((new File (RESULTS_FILE + index + ".log")).exists())
            	index++;
			_resultsFile = new PrintStream(new BufferedOutputStream(new FileOutputStream(RESULTS_FILE + index + ".log")));
    	} catch (IOException e) {
      		e.printStackTrace();
    	}
  	}

}


