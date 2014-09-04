/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protege.widget.*;
import edu.stanford.smi.protegex.anchorPrompt.ui.*;

public class AnchorPromptTab extends AbstractTabWidget
{
    static TabComponent _anchorPromptTab;
    static AbstractTabWidget _this;
   	static Collection _anchorPairs = new ArrayList();

    static final int ACTIVE_KBS = 3;
    static final String DEFAULT_MERGED_FILE_NAME = "merged.pprj";

/*
    static final String [] PROJECT_FILES =
            {"d:\\Merging\\Guideline ontologies\\ocelet\\rct.pprj",
             "d:\\Merging\\Guideline ontologies\\DaT\\ClinTrialOntology.pprj",
             "d:\\Anchor-Prompt\\examples\\merged.pprj"};
    static final String [] PROJECT_PRETTY_NAMES =
            {"rct", "cto", "merged"};
*/
/*
    static final String [] PROJECT_FILES =
            {"d:\\Anchor-Prompt\\researchProjectOnts\\umd\\umd-all.pprj",
             "d:\\Anchor-Prompt\\researchProjectOnts\\swrc\\swrc.pprj",
             "d:\\Anchor-Prompt\\examples\\merged.pprj"};
    static final String [] PROJECT_PRETTY_NAMES =
            {"umd", "swrc", "merged"};
*/
/*
    static final String [] PROJECT_FILES =
            {"d:\\Anchor-Prompt\\researchProjectOnts\\umd\\umd-all.pprj",
             "d:\\Anchor-Prompt\\researchProjectOnts\\atlas-cmu\\atlas.pprj",
             "d:\\Anchor-Prompt\\examples\\merged.pprj"};
    static final String [] PROJECT_PRETTY_NAMES =
            {"umd", "atlas", "merged"};
*/
    static final String [] PROJECT_FILES =
            {"//Users//natasha//Work//Anchor-Prompt//researchProjectOnts//swrc//swrc.pprj",
             "//Users//natasha//Work//Anchor-Prompt//researchProjectOnts//atlas-cmu//atlas.pprj",
             "//Users//natasha//Work//Anchor-Prompt//examples//merged.pprj"};
    static final String [] PROJECT_PRETTY_NAMES =
            {"swrc", "atlas", "merged"};

    private static HashMap _kbs;
    private static String [] _projectAliases;
    private static KnowledgeBase _targetKb = null;

    static private boolean testing = false;
    static private boolean _runExperiments = false;
    static private boolean _processResults = false;

    public AnchorPromptTab() {
    }

    public void initialize() {
      setLabel ("Anchor-Prompt");
      JButton initButton = new JButton ("Click here to initialize");
      setLayout (new BorderLayout());
      add (initButton, BorderLayout.CENTER);

      _this = this;

      initButton.addActionListener (new ActionListener () {
        public void actionPerformed (ActionEvent event) {
          setUpMerging ();
        }
      });
    }

    public void setUpMerging () {
	  OpenFileDialog files = null;
      if (testing)
      	setUpNewProjects (PROJECT_FILES, PROJECT_PRETTY_NAMES);
      else
      	files = new OpenFileDialog();


//      JMenuBar menuBar = getMainWindowMenuBar();

      if (!testing && files != null && !files.filesOpened()) {
        _targetKb = getKnowledgeBase();

        Log.getLogger().info ("Done!");
        return;
      }
      removeAll();

//      if (testing)
//      	setAnchors ();

      if (_runExperiments) {
      	RunExperiments.runExperiments ();
      } else if (_processResults) {
      	//ProcessResults.processResults ();
        ResultsToTable.resultsToTable();
      } else {
      	_anchorPromptTab = new TabComponent();
      	setLayout (new BorderLayout());
      	add (_anchorPromptTab, BorderLayout.CENTER);
      	revalidate();
      	repaint();
	    Log.getLogger().info ("Done!");
      }

  }

  static public boolean runningExperiments () { return _runExperiments; }

//  static private void setAnchors () {
//   	KnowledgeBase kb1 = (KnowledgeBase)_kbs.get ("umd");
//   	KnowledgeBase kb2 = (KnowledgeBase)_kbs.get ("swrc");
//    Cls publ = (Cls)kb1.getFrame ("Publication");
//    Cls pub2 = (Cls)kb2.getFrame("Publication");
//    Cls group1 = (Cls)kb1.getFrame ("ResearchGroup");
//    Cls group2 = (Cls)kb2.getFrame("ResearchGroup");
//    Cls project1 = (Cls)kb1.getFrame ("Research");
//    Cls project2 = (Cls)kb2.getFrame("Project");
//    Cls event1 = (Cls)kb1.getFrame ("Event");
//    Cls event2 = (Cls)kb2.getFrame("Event");
//    Cls organization1 = (Cls)kb1.getFrame ("Organization");
//    Cls organization2 = (Cls)kb2.getFrame("Organization");
//
//    _anchorPairs.add (new AnchorPair (publ, pub2));
//    _anchorPairs.add (new AnchorPair (group1, group2));
//    _anchorPairs.add (new AnchorPair (project1, project2));
//    _anchorPairs.add (new AnchorPair (event1, event2));
//    _anchorPairs.add (new AnchorPair (organization1, organization2));
//  }

  static public void setUpNewProjects (String [] projectFileNames, String [] projectAliases) {
      int n;

      _kbs = new HashMap (ACTIVE_KBS * 2);
      clearAll();
      KnowledgeBase nextKb;
      for (n = ACTIVE_KBS-1; n >= 0; n--) {

        boolean processingTarget = false;
        if (n == ACTIVE_KBS-1) processingTarget = true;

        nextKb = openFile (projectFileNames[n], projectAliases[n], processingTarget);
        _kbs.put (projectAliases[n], nextKb);  //<kbName, kb>
        _kbs.put (nextKb, projectAliases[n]);  //<kb, kbName>
      }

      _projectAliases = projectAliases;
      Log.getLogger().info ("Loaded knowledge Base");

      _targetKb = (KnowledgeBase)_kbs.get(projectAliases[2]);
//      _anchorPairs.add(new AnchorPair (((KnowledgeBase)_kbs.get(projectAliases[0])).getRootCls(),
//                                       ((KnowledgeBase)_kbs.get(projectAliases[1])).getRootCls()));
//
  }

  static private KnowledgeBase  openFile (String projectFileName, String projectAlias,
                                                         boolean processingTarget) {
    Project newProject;
    if (!processingTarget) {
//      Application.main (new String [] {projectFileName});
        	Collection errors = new ArrayList();
          newProject = new Project(projectFileName, errors);
    } else
       newProject = ProjectManager.getProjectManager().getCurrentProject();

    return newProject.getKnowledgeBase();
  }


  static public String getKnowledgeBasePrettyName (KnowledgeBase kb) {
    if (kb == null)
      return null;
    return (String)_kbs.get(kb);
  }

  static public KnowledgeBase getKnowledgeBase (String name) {
    if (name == null)
      return null;
    return (KnowledgeBase)_kbs.get(name);
  }

  static public KnowledgeBase getTargetKnowledgeBase () {
    return _targetKb;
  }

  static public Project getTargetProject () {
    return _this.getProject();
  }

  static public String[] getProjectsPrettyNames () {
    return _projectAliases;
  }

  public static int getNumberOfActiveKbs () {return ACTIVE_KBS;}

  public static Component getMainWindow () {
    return ComponentUtilities.getFrame(_this);
  }

  public static TabComponent getTabComponent () {
    return _anchorPromptTab;
  }

    static public void clearAll () {
	 _anchorPairs.clear();
      _kbs.clear();
      _targetKb = null;
  }

  public void dispose () {
  }

  public static String [] getDefaultFileNames () { return PROJECT_FILES;}

  public static String [] getDefaultAliases () { return PROJECT_PRETTY_NAMES;}

  public static String getDefaultMergedFile () { return PROJECT_FILES[ACTIVE_KBS-1];}

  public static String getDefaultMergedAlias () { return PROJECT_PRETTY_NAMES[ACTIVE_KBS-1];}

  public static int getNumberOfKbs () {return ACTIVE_KBS;}

    public static void main(String[] args) {
    	try {
    		edu.stanford.smi.protege.Application.main(args);
        } catch (Exception e) {
        }
    }

  public static Collection getCurrentAnchors () { return _anchorPairs;}

  public static void addAnchorPair (AnchorPair p) {
   	_anchorPairs.add (p);
    if (_anchorPromptTab != null)
      _anchorPromptTab.anchorsChanged (true);
  }

  public static void removeAnchorPair (AnchorPair p) {
   	_anchorPairs.remove(p);
  }

  public static void setResults (Collection results) {
    if (_anchorPromptTab != null)
      _anchorPromptTab.resultsChanged (true, results);

  }
/*
  public void removeAnchorPair (AnchorPair p) {
  	if (_anchorPairs == null || _anchorPairs.size() == 0) return;
    Iterator i = _anchorPairs.iterator();
    Object toRemove = null;
    while (i.hasNext()) {
    	Object next = i.next();
        if (next.equals (p)) {
        	toRemove = p;
            break;
        }
    }
    if (toRemove != null)
   		_anchorPairs.remove (toRemove);
  }
  */
}
