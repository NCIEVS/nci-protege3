/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt;

import java.io.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.anchorPrompt.analysis.*;

public class RunExperiments {
    static final private String RESULTS_PROJECT = "d:\\Anchor-Prompt\\researchProjectOnts\\experiments\\experimentResults.pprj";
	static final private String RESULTS_FILE = "d:\\Anchor-Prompt\\researchProjectOnts\\experiments\\result";
	static private PrintStream _resultsFile = null;

   	static Collection _anchorPairs = new ArrayList();
    static private HashMap _uniqueResults = new HashMap (); // "cls1:cls2" -> ScoreTableElement
    static private HashMap _correctResults = new HashMap (); // "cls1:cls2" -> ScoreTableElement
       					                                     // with 1000 for equivalent
                                                             // and 2000 for subclass/superclass
                                                             // and -1000 for unrelated
	static private String _currentProject1;
	static private String _currentProject2;
	static private Collection _currentAnchorPairs;
    static private KnowledgeBase _resultsKb;
    static private Project _resultsProject;
    static private Instance _currentSetupInstance;

    static private boolean outputToFile = true;
    static private boolean outputToProject = true;
    static private boolean firstRun = false;


	static public void runExperiments () {
        openProjectAndLogFile ();
        if (!firstRun)
        	setUpResultsTable();

        Collection combinations = generateAnchorPairCombinations (setAnchors());
        Iterator i = combinations.iterator();
        while (i.hasNext()) {
        	Collection nextSet = (Collection)i.next();
  			saveSetup (nextSet);
    		while (! Parameters.doneWithExperiments()) {
       			Analysis.clearMaps();
        		Parameters.nextParameterSet(outputToFile ? _resultsFile : null);
                createProtegeSetupInstance ();
				Collection results = Analysis.analyze (nextSet);
                setResultCorrectness (results);
            	saveResults (results);
                if (firstRun)
	                createUniqueResultSet (results);
    		}
        }
        if (firstRun) {
	        _resultsFile.println ("UNIQUE RESULTS:" );
	        printUniqueResults (_uniqueResults.values());
        }
        closeProjectAndLogFile ();
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

    static private Collection generateAnchorPairCombinations (Collection sources) {
     	Collection allCombinations = generateCombinations (sources);
        Collection result = new ArrayList();
        Iterator i = allCombinations.iterator();
        while (i.hasNext()) {
         	Collection next = (Collection)i.next();
            if (next.size() >= 2)
            	result.add (next);
        }
        return result;
    }

    static private Collection generateCombinations (Collection sources) {
     	// generate all the possible collections of length > 1
        if (sources.size() == 0) return null;

        Object firstElement = CollectionUtilities.getFirstItem(sources);
        sources.remove(firstElement);
        Collection otherCombinations = generateCombinations (sources);

        if (otherCombinations == null)
        	return CollectionUtilities.createCollection
            	(CollectionUtilities.createCollection(firstElement));

        Collection result = new ArrayList(otherCombinations);
        result.add (CollectionUtilities.createCollection(firstElement));
        Iterator i = otherCombinations.iterator();
        while (i.hasNext()) {
			Collection next = new ArrayList ((Collection)i.next());
            next.add (firstElement);
            result.add (next);
        }
        return result;
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

    static private void saveSetup (Collection anchorPairs) {
     	if (outputToFile)
        	printSetup (anchorPairs);
        if (outputToProject)
        	createSetupFrames (anchorPairs);
    }

  	static private void printSetup (Collection anchorPairs) {
    	String [] projectAliases = AnchorPromptTab.getProjectsPrettyNames();
  		_resultsFile.println("PROJECTS:\t" + projectAliases[0] + "\t" + projectAliases[1]);
        _currentProject1 = projectAliases[0];
        _currentProject2 = projectAliases[1];
    		Iterator i = anchorPairs.iterator();
    	while (i.hasNext()) {
        	AnchorPair nextPair = (AnchorPair)i.next();
    		_resultsFile.println  (nextPair.toString());
        }
  	}


  	static private void createSetupFrames (Collection anchorPairs) {
    	String [] projectAliases = AnchorPromptTab.getProjectsPrettyNames();
  		_resultsFile.println("PROJECTS:\t" + projectAliases[0] + "\t" + projectAliases[1]);
        _currentProject1 = projectAliases[0];
        _currentProject2 = projectAliases[1];
        _currentAnchorPairs = new ArrayList (anchorPairs.size()) ;
   		Iterator i = anchorPairs.iterator();
        Cls anchorPairCls = _resultsKb.getCls("Anchor pair");
        Slot firstAnchorSlot = _resultsKb.getSlot("anchor pair element 1");
        Slot secondAnchorSlot = _resultsKb.getSlot("anchor pair element 2");
    	while (i.hasNext()) {
        	AnchorPair nextPair = (AnchorPair)i.next();
            Instance nextInstance =
            	_resultsKb.createInstance (null, anchorPairCls);
            nextInstance.setOwnSlotValue(firstAnchorSlot, nextPair.getAnchor(0).getName());
            nextInstance.setOwnSlotValue(secondAnchorSlot, nextPair.getAnchor(1).getName());
            _currentAnchorPairs.add (nextInstance);
        }
  	}

    static private void createProtegeSetupInstance () {
		if (!outputToProject) return;
     	Cls experimentSetupCls = _resultsKb.getCls("Experiment setup");
        _currentSetupInstance = _resultsKb.createInstance (null,  experimentSetupCls);
        _currentSetupInstance.setOwnSlotValue(_resultsKb.getSlot ("project 1"), _currentProject1 );
        _currentSetupInstance.setOwnSlotValue(_resultsKb.getSlot ("project 2"), _currentProject2 );
        _currentSetupInstance.setOwnSlotValues(_resultsKb.getSlot("anchors"), _currentAnchorPairs);
        _currentSetupInstance.setOwnSlotValue(_resultsKb.getSlot ("depth"), new Integer (Parameters.DEPTH).toString());
        _currentSetupInstance.setOwnSlotValue(_resultsKb.getSlot ("equiavalence threshold"), new Integer (Parameters.EQUIVALENCE_THRESHOLD).toString());
        _currentSetupInstance.setOwnSlotValue(_resultsKb.getSlot ("high score"), new Integer (Parameters.HIGH_SCORE).toString());
        _currentSetupInstance.setOwnSlotValue(_resultsKb.getSlot ("low score"), new Integer (Parameters.LOW_SCORE).toString());
        _currentSetupInstance.setOwnSlotValue(_resultsKb.getSlot ("consider subclasses"), new Boolean (Parameters.considerSubclasses()));
    }

    static private void saveResults (Collection results) {
    	if (outputToFile)
        	printResults (results);
        if (outputToProject)
        	saveResultInstances (results);
    }

    static private void printResults (Collection results) {
		if (results == null || results.size() == 0)  {
        	_resultsFile.println ("NONE");
            return;
        }
        Iterator i = results.iterator();
        while (i.hasNext()) {
            ScoreTableElement next = (ScoreTableElement)i.next();
            _resultsFile.println (next.toString());
        }
    }

    static private void saveResultInstances (Collection results) {
    	Cls experimentCls = _resultsKb.getCls("Experiment");
   		Instance experimentInstance = _resultsKb.createInstance (null, experimentCls);
        Slot setupSlot = _resultsKb.getSlot ("experiment setup");
        Slot resultsSlot = _resultsKb.getSlot ("experiment results");

        experimentInstance.setOwnSlotValue(setupSlot, _currentSetupInstance);
        Collection resultInstances = new ArrayList();

		if (results == null || results.size() == 0) {
        	resultInstances.add (createResultInstace (null));
        } else {
        	Iterator i = results.iterator();
        	while (i.hasNext()) {
           		ScoreTableElement next = (ScoreTableElement)i.next();
            	_resultsFile.println (next.toString());
            	resultInstances.add (createResultInstace (next));
        	}
        }
        experimentInstance.setOwnSlotValues(resultsSlot, resultInstances);
    }

    private static Instance createResultInstace (ScoreTableElement elt) {
     	Cls cls = _resultsKb.getCls("Score table element");
        Instance instance = _resultsKb.createInstance (null, cls);
        if (elt == null) {
			instance.setOwnSlotValue (_resultsKb.getSlot ("score"), new Integer (0));
         	return instance;
        }
        instance.setOwnSlotValue (_resultsKb.getSlot ("element 1"), elt.getFirstElement().getName());
        instance.setOwnSlotValue (_resultsKb.getSlot ("element 2"), elt.getSecondElement().getName());
        instance.setOwnSlotValue (_resultsKb.getSlot ("score"), new Integer (elt.getScore()));
        instance.setOwnSlotValue (_resultsKb.getSlot ("correctness"), elt.getCorretnessAsString());
        return instance;
    }

    static private void createUniqueResultSet (Collection results) {
        Iterator i = results.iterator();
        while (i.hasNext()) {
            ScoreTableElement next = (ScoreTableElement)i.next();
            String elt1 = next.getFirstElement().getName();
            String elt2 = next.getSecondElement().getName();
            String key = elt1 + ":" + elt2;
            boolean exists = _uniqueResults.containsKey(key);
            if (!exists)
            	_uniqueResults.put (key, next);
        }
    }

    static private void printUniqueResults (Collection results) {
		if (results == null || results.size() == 0)
        	_resultsFile.println ("NONE");
        Iterator i = results.iterator();
        while (i.hasNext()) {
            ScoreTableElement next = (ScoreTableElement)i.next();
            _resultsFile.println ("createCorrectResultsTableEntry (\""+
                                  next.getFirstElement().getName() +
                                  "\", \"" +
                                  next.getSecondElement().getName() +
                                  "\", ScoreTableElement.  );");
        }
    }

    static private Collection setAnchors () {
//	    return setAnchorsUMD_SWRC ();
//	    return setAnchorsUMD_ATLAS ();
	    return setAnchorsSWRC_ATLAS ();
    }

  	static private void setResultCorrectness (Collection results) {
     	if (results == null || results.size() == 0) return;
        Iterator i = results.iterator();
        while (i.hasNext()) {
            ScoreTableElement next = (ScoreTableElement)i.next();
         	setCorrectnessValue (next);
        }
    }

    static private void setCorrectnessValue (ScoreTableElement elt) {
     	Integer value = (Integer)_correctResults.get
        	(generateCorrectnessTableKey (elt.getFirstElement().getName(),
                                          elt.getSecondElement().getName()));
        if (value != null)
        	elt.setCorrectness(value.intValue());
    }

    static private void setUpResultsTable () {
//    	setUpResultsTableUMD_SWRC();
//        setUpResultsTableUMD_ATLAS ();
        setUpResultsTableSWRC_ATLAS();
    }

    static private void setUpResultsTableUMD_ATLAS () {
createCorrectResultsTableEntry ("Publication", "Project", ScoreTableElement.UNRELATED  );
createCorrectResultsTableEntry ("Employee", "Employee", ScoreTableElement.EQUIVALENT  );
createCorrectResultsTableEntry ("Person", "Employee", ScoreTableElement.SUBCLASS_SUPERCLASS  );
createCorrectResultsTableEntry ("Employee", "Research_Project", ScoreTableElement.UNRELATED  );
createCorrectResultsTableEntry ("Organization", "Research_Group", ScoreTableElement.SUBCLASS_SUPERCLASS  );
createCorrectResultsTableEntry ("Organization", "Employee", ScoreTableElement.UNRELATED  );
    }

    static private void setUpResultsTableSWRC_ATLAS () {
createCorrectResultsTableEntry ("Project", "Research_Group", ScoreTableElement.UNRELATED  );
createCorrectResultsTableEntry ("Organization", "Research_Project", ScoreTableElement.UNRELATED  );
createCorrectResultsTableEntry ("Project", "Research_Project", ScoreTableElement.SUBCLASS_SUPERCLASS  );
createCorrectResultsTableEntry ("Employee", "Research_Activity", ScoreTableElement.UNRELATED  );
createCorrectResultsTableEntry ("Project", "Employee", ScoreTableElement.UNRELATED  );
createCorrectResultsTableEntry ("Institute", "Project", ScoreTableElement.UNRELATED  );
createCorrectResultsTableEntry ("Organization", "Research_Group", ScoreTableElement.SUBCLASS_SUPERCLASS  );
createCorrectResultsTableEntry ("Employee", "Employee", ScoreTableElement.EQUIVALENT  );
createCorrectResultsTableEntry ("Employee", "Project", ScoreTableElement.UNRELATED  );
createCorrectResultsTableEntry ("AcademicStaff", "Employee", ScoreTableElement.SUBCLASS_SUPERCLASS  );
createCorrectResultsTableEntry ("AcademicStaff", "Project", ScoreTableElement.UNRELATED  );
createCorrectResultsTableEntry ("AcademicStaff", "Research_Project", ScoreTableElement.UNRELATED  );
createCorrectResultsTableEntry ("Institute", "Research_Project", ScoreTableElement.UNRELATED  );
createCorrectResultsTableEntry ("Organization", "Project", ScoreTableElement.UNRELATED  );
createCorrectResultsTableEntry ("Employee", "Research_Group", ScoreTableElement.UNRELATED  );
createCorrectResultsTableEntry ("Organization", "Publication", ScoreTableElement.UNRELATED  );
createCorrectResultsTableEntry ("ResearchTopic", "Research_Activity", ScoreTableElement.SUBCLASS_SUPERCLASS  );
createCorrectResultsTableEntry ("Employee", "Publication", ScoreTableElement.UNRELATED  );
createCorrectResultsTableEntry ("AcademicStaff", "Research_Group", ScoreTableElement.UNRELATED  );
createCorrectResultsTableEntry ("Project", "Publication", ScoreTableElement.UNRELATED  );
createCorrectResultsTableEntry ("ResearchTopic", "Research_Group", ScoreTableElement.UNRELATED  );
createCorrectResultsTableEntry ("PhDStudent", "Employee", ScoreTableElement.UNRELATED  );
createCorrectResultsTableEntry ("ResearchTopic", "Employee", ScoreTableElement.UNRELATED  );
createCorrectResultsTableEntry ("Organization", "Employee", ScoreTableElement.UNRELATED  );
createCorrectResultsTableEntry ("Person", "Employee", ScoreTableElement.SUBCLASS_SUPERCLASS  );
    }

    static private void setUpResultsTableUMD_SWRC () {
createCorrectResultsTableEntry ("Person", "ResearchTopic", ScoreTableElement.UNRELATED);
createCorrectResultsTableEntry ("Publication", "Employee", ScoreTableElement.UNRELATED);
createCorrectResultsTableEntry ("SocialGroup", "Article", ScoreTableElement.UNRELATED);
createCorrectResultsTableEntry ("Document", "Project", ScoreTableElement.UNRELATED) ;
createCorrectResultsTableEntry ("Employee", "Organization", ScoreTableElement.UNRELATED) ;
createCorrectResultsTableEntry ("Employee", "Person", ScoreTableElement.SUBCLASS_SUPERCLASS)    ;
createCorrectResultsTableEntry ("Document", "AcademicStaff", ScoreTableElement.UNRELATED)  ;
createCorrectResultsTableEntry ("Employee", "Project", ScoreTableElement.UNRELATED)   ;
createCorrectResultsTableEntry ("Person", "Organization", ScoreTableElement.UNRELATED) ;
createCorrectResultsTableEntry ("Organization", "ResearchGroup", ScoreTableElement.SUBCLASS_SUPERCLASS) ;
createCorrectResultsTableEntry ("Organization", "Project", ScoreTableElement.UNRELATED) ;
createCorrectResultsTableEntry ("SocialGroup", "Publication", ScoreTableElement.UNRELATED)   ;
createCorrectResultsTableEntry ("Document", "Organization", ScoreTableElement.UNRELATED) ;
createCorrectResultsTableEntry ("Organization", "Person", ScoreTableElement.UNRELATED) ;
createCorrectResultsTableEntry ("Agent", "Book", ScoreTableElement.UNRELATED) ;
createCorrectResultsTableEntry ("Organization", "ResearchTopic", ScoreTableElement.UNRELATED) ;
createCorrectResultsTableEntry (":THING", "Event", ScoreTableElement.SUBCLASS_SUPERCLASS)   ;
createCorrectResultsTableEntry ("Organization", "Organization", ScoreTableElement.EQUIVALENT)  ;
createCorrectResultsTableEntry ("Organization", "Employee", ScoreTableElement.UNRELATED)  ;
createCorrectResultsTableEntry ("Person", "Employee", ScoreTableElement.SUBCLASS_SUPERCLASS)  ;
    }

    private static void  createCorrectResultsTableEntry (String v1, String v2, int c) {
    	_correctResults.put (generateCorrectnessTableKey (v1, v2), new Integer (c));
    }

    private static String generateCorrectnessTableKey (String v1, String v2) {
     	return v1 + ":" + v2;
    }

  static private Collection <AnchorPair> setAnchorsUMD_SWRC () {
  	Collection <AnchorPair> result = new ArrayList<AnchorPair> ();
   	KnowledgeBase umdKb = AnchorPromptTab.getKnowledgeBase("umd");
   	KnowledgeBase swrcKb = AnchorPromptTab.getKnowledgeBase("swrc");
    Cls publ = (Cls)umdKb.getFrame ("Publication");
    Cls pub2 = (Cls)swrcKb.getFrame("Publication");
    Cls group1 = (Cls)umdKb.getFrame ("ResearchGroup");
    Cls group2 = (Cls)swrcKb.getFrame("ResearchGroup");
    Cls project1 = (Cls)umdKb.getFrame ("Research");
    Cls project2 = (Cls)swrcKb.getFrame("Project");
    Cls event1 = (Cls)umdKb.getFrame ("Event");
    Cls event2 = (Cls)swrcKb.getFrame("Event");
    Cls organization1 = (Cls)umdKb.getFrame ("Organization");
    Cls organization2 = (Cls)swrcKb.getFrame("Organization");

    result.add (new AnchorPair (publ, pub2));
    result.add (new AnchorPair (group1, group2));
    result.add (new AnchorPair (project1, project2));
    result.add (new AnchorPair (event1, event2));
    result.add (new AnchorPair (organization1, organization2));

    return result;
  }

  static private Collection<AnchorPair> setAnchorsUMD_ATLAS () {
  	Collection<AnchorPair> result = new ArrayList<AnchorPair>();
   	KnowledgeBase umdKb = AnchorPromptTab.getKnowledgeBase("umd");
   	KnowledgeBase atlasKb = AnchorPromptTab.getKnowledgeBase("atlas");
    Cls publ = (Cls)umdKb.getFrame ("Publication");
    Cls pub2 = (Cls)atlasKb.getFrame("Publication");
    Cls group1 = (Cls)umdKb.getFrame ("ResearchGroup");
    Cls group2 = (Cls)atlasKb.getFrame("Research_Group");
    Cls project1 = (Cls)umdKb.getFrame ("Research");
    Cls project2 = (Cls)atlasKb.getFrame("Project");
    Cls organization1 = (Cls)umdKb.getFrame ("Organization");
    Cls organization2 = (Cls)atlasKb.getFrame("Organisation");

    result.add (new AnchorPair (publ, pub2));
    result.add (new AnchorPair (group1, group2));
    result.add (new AnchorPair (project1, project2));
    result.add (new AnchorPair (organization1, organization2));

    return result;
  }

  static private Collection setAnchorsSWRC_ATLAS () {
  	Collection result = new ArrayList();
   	KnowledgeBase swrcKb = AnchorPromptTab.getKnowledgeBase("swrc");
   	KnowledgeBase atlasKb = AnchorPromptTab.getKnowledgeBase("atlas");
    Cls publ = (Cls)swrcKb.getFrame ("Publication");
    Cls pub2 = (Cls)atlasKb.getFrame("Publication");
    Cls group1 = (Cls)swrcKb.getFrame ("ResearchGroup");
    Cls group2 = (Cls)atlasKb.getFrame("Research_Group");
    Cls project1 = (Cls)swrcKb.getFrame ("Project");
    Cls project2 = (Cls)atlasKb.getFrame("Project");
    Cls organization1 = (Cls)swrcKb.getFrame ("Organization");
    Cls organization2 = (Cls)atlasKb.getFrame("Organisation");

    result.add (new AnchorPair (publ, pub2));
    result.add (new AnchorPair (group1, group2));
    result.add (new AnchorPair (project1, project2));
    result.add (new AnchorPair (organization1, organization2));

    return result;
  }

}


