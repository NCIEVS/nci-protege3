/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt;

import java.io.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;

public class ResultsToTable {
    static final private String RESULTS_PROJECT =
    	"//Users//natasha//Work//Anchor-Prompt//researchProjectOnts//experiments//results//resultsUMD_ATLAS.pprj";
//    static final private String RESULTS_PROJECT =
//    	"d:\\Anchor-Prompt\\researchProjectOnts\\experiments\\results\\resultsUMD_SWRC.pprj";
//    static final private String RESULTS_PROJECT =
//    	"d:\\Anchor-Prompt\\researchProjectOnts\\experiments\\results\\resultsSWRC_ATLAS.pprj";
	static final private String RESULTS_FILE = "//Users//natasha//Work//Anchor-Prompt//researchProjectOnts//experiments//result";
	static private PrintStream _resultsFile = null;

    static private KnowledgeBase _resultsKb;
    static private Project _resultsProject;

    static private boolean outputToFile = true;
    static private boolean outputToProject = true;

    static {
        openProjectAndLogFile ();
    }

    static public void resultsToTable () {

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

//    static private Slot _resultsSlot = _resultsKb.getSlot ("experiment results");
//    static private Cls _analysisCls = _resultsKb.getCls ("Analysis");
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

    static private Slot _setupInstance = _resultsKb.getSlot ("experiment setup");

    // inst is an instance of Experiment class
    static private void processExperimentInstance (Instance inst) {
        printSetup ((Instance)inst.getOwnSlotValue(_setupInstance));
        printResults ((Instance)inst.getOwnSlotValue(_analysisSlot));
        _resultsFile.println();
    }

    static private final String SEPARATOR = "\t";
    // setup instance
    static private Slot _depthSlot = _resultsKb.getSlot ("depth");
    static private Slot _equivalenceThresholdSlot = _resultsKb.getSlot ("equiavalence threshold");
    static private Slot _highScoreSlot = _resultsKb.getSlot ("high score");
    static private Slot _lowScoreSlot = _resultsKb.getSlot ("low score");
    static private Slot _considerSubclassesSlot = _resultsKb.getSlot ("consider subclasses");
    static private Slot _anchorsSlot = _resultsKb.getSlot ("anchors");

    static private void printSetup (Instance setupInstance) {
    	_resultsFile.print (setupInstance.getOwnSlotValue (_depthSlot) + SEPARATOR);
    	_resultsFile.print (setupInstance.getOwnSlotValue (_equivalenceThresholdSlot) + SEPARATOR);
    	_resultsFile.print (setupInstance.getOwnSlotValue (_highScoreSlot) + SEPARATOR);
    	_resultsFile.print (setupInstance.getOwnSlotValue (_lowScoreSlot) + SEPARATOR);
    	_resultsFile.print (setupInstance.getOwnSlotValue (_considerSubclassesSlot) + SEPARATOR);
    	_resultsFile.print (setupInstance.getOwnSlotValues (_anchorsSlot).size() + SEPARATOR);
    }

    // analysis instance
    static private void printResults (Instance analysisInstance) {
        _resultsFile.print (analysisInstance.getOwnSlotValue (_numberOfResultsSlot) + SEPARATOR);

        _resultsFile.print (analysisInstance.getOwnSlotValue (_correctResultsSlot) + SEPARATOR);
        _resultsFile.print (analysisInstance.getOwnSlotValue (_equivalentResultsSlot) + SEPARATOR);

        _resultsFile.print (analysisInstance.getOwnSlotValue (_correctResultsAboveMedianSlot) + SEPARATOR);
        _resultsFile.print (analysisInstance.getOwnSlotValue (_equivalentResultsAboveMedianSlot) + SEPARATOR);
        _resultsFile.print (analysisInstance.getOwnSlotValue (_numberOfResultsAboveMedianSlot) + SEPARATOR);

        _resultsFile.print (analysisInstance.getOwnSlotValue (_ratioOfCorrectResultsSlot) + SEPARATOR);
        _resultsFile.print (analysisInstance.getOwnSlotValue (_ratioOfCorrectResultsAboveMedianSlot) + SEPARATOR);
        _resultsFile.print (analysisInstance.getOwnSlotValue (_ratioOfEquivalentResultsSlot) + SEPARATOR);
        _resultsFile.print (analysisInstance.getOwnSlotValue (_ratioOfEquivalentResultsAboveMedianSlot) + SEPARATOR);

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


