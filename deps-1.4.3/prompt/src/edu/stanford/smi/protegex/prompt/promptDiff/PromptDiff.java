/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu,
 *                 Michel Klein michel.klein@cs.vu.nl
 *                 Abhita Chugh abhita@stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JTabbedPane;

import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.prompt.ProjectsAndKnowledgeBases;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.event.DiffEvent;
import edu.stanford.smi.protegex.prompt.event.DiffListener;
import edu.stanford.smi.protegex.prompt.event.PromptListenerManager;
import edu.stanford.smi.protegex.prompt.plugin.AlgorithmProgressMonitor;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.AlgorithmDependencyTable;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.FrameDifferenceElement;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.ResultTable;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.StackSet;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.TableRow;
import edu.stanford.smi.protegex.prompt.promptDiff.users.ChangeManagement;
import edu.stanford.smi.protegex.prompt.ui.diffUI.AcceptorRejector;
import edu.stanford.smi.protegex.prompt.ui.diffUI.DiffTableView;
import edu.stanford.smi.protegex.prompt.ui.diffUI.DiffViewSetUp;
import edu.stanford.smi.protegex.prompt.util.OWLUtil;
import edu.stanford.smi.protegex.server_changes.PostProcessorManager;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;
import edu.stanford.smi.protegex.server_changes.prompt.AuthorManagement;

public class PromptDiff extends Thread {
	private static  boolean _showIncluded = true;
	private  KnowledgeBase _kb1;
	private  KnowledgeBase _kb2;
	private  ResultTable _results;
	private  ChangeManagement cm;

	private  transient Vector<DiffListener> _listeners;

	private  AuthorManagement _authorManagement;

	public static final String NULL_VALUE = "NULL";

	private  DiffViewSetUp _viewSetUp;

	private static final String [] ALGORITHM_NAMES =
	{//"edu.stanford.smi.protegex.prompt.promptDiff.FindUnchangedEntries",
		"edu.stanford.smi.protegex.prompt.promptDiff.FramesWithSimilarNames",
		"edu.stanford.smi.protegex.prompt.promptDiff.LoneUnmatchedSibling",
		"edu.stanford.smi.protegex.prompt.promptDiff.CompareOWLAnonymousClasses",
		"edu.stanford.smi.protegex.prompt.promptDiff.InstancesWithSameSlotValues",
		"edu.stanford.smi.protegex.prompt.promptDiff.CompareFrameNamesAndTypes",
//		"edu.stanford.smi.protegex.prompt.promptDiff.CompareConceptIDs", // it's run only once, so no need to have it in the stack
		"edu.stanford.smi.protegex.prompt.promptDiff.LoneUnmatchedTemplateSlot",
		"edu.stanford.smi.protegex.prompt.promptDiff.UnmatchedSuperclass",
		"edu.stanford.smi.protegex.prompt.promptDiff.UnmatchedTemplateSlotsAtClass",
		"edu.stanford.smi.protegex.prompt.promptDiff.MultipleUnmatchedSiblings",
		"edu.stanford.smi.protegex.prompt.promptDiff.MultipleUnmatchedSiblingsWithSimilarNames",
		"edu.stanford.smi.protegex.prompt.promptDiff.SiblingsWithSameSuffixes",
		"edu.stanford.smi.protegex.prompt.promptDiff.ClassesWithSameSubclassAndSuperclass",
		"edu.stanford.smi.protegex.prompt.promptDiff.SplitClasses",
		"edu.stanford.smi.protegex.prompt.promptDiff.LoneUnmatchedRestrictionOfSameType",
		"edu.stanford.smi.protegex.prompt.promptDiff.SameRestrictionTypeForSamePropertyAtSameClass",
		"edu.stanford.smi.protegex.prompt.promptDiff.SingleUnmatchedAllowedClass",
		"edu.stanford.smi.protegex.prompt.promptDiff.SlotsWithSameAllowedClass",
		"edu.stanford.smi.protegex.prompt.promptDiff.CompareOWLIntersectionAndUnionClasses",
		"edu.stanford.smi.protegex.prompt.promptDiff.SingleUnmatchedDomain",
		"edu.stanford.smi.protegex.prompt.promptDiff.UnmatchedInverseSlot",
	"edu.stanford.smi.protegex.prompt.promptDiff.CompareOWLAnonymousIndividuals"};

	private static final String COMPARE_CONCEPT_IDS = "edu.stanford.smi.protegex.prompt.promptDiff.CompareConceptIDs";
	private static final String COMPARE_OWL_ANONYMOUS_CLASSES =
		"edu.stanford.smi.protegex.prompt.promptDiff.CompareOWLAnonymousClasses";

	private AlgorithmProgressMonitor _progressMonitor = null;

	private boolean _fromUI = false;
	private AcceptorRejector _changeAcceptorRejector;

	public void runDiff (boolean fromUI) {
		_kb1 = ProjectsAndKnowledgeBases.getKnowledgeBase(ProjectsAndKnowledgeBases.OLD_VERSION_INDEX);
		_kb2 = ProjectsAndKnowledgeBases.getKnowledgeBase(ProjectsAndKnowledgeBases.NEW_VERSION_INDEX);
		_fromUI = fromUI;

		if (_fromUI) {
			_progressMonitor = new AlgorithmProgressMonitor();
			PromptTab.displayProgressMonitor(_progressMonitor);
		}
		//runFixedPoint();

		//PromptListenerManager.fireTaskComplete(false);
	}

	public void runDiff (KnowledgeBase kb1, KnowledgeBase kb2) {
		_kb1 = kb1;
		_kb2 = kb2;

		runFixedPoint();

		//PromptListenerManager.fireTaskComplete(false);
	}

	@Override
	public void run() {
		if (_fromUI) {
			_progressMonitor.setProgressText("Computing ontology differences");
		}

		runFixedPoint();

		if (_fromUI) {
			_progressMonitor.setProgressText("Complete");
		}

		PromptListenerManager.fireTaskComplete(false);
	}


	private  void generateResultTableFromChangeOntology(){
		Log.getLogger().info ("Starting generation from Change Ontology:  " + new Date());
		_results = new ResultTable (_kb1, _kb2);
		//_results.initializeTable();
//		Log.getLogger().info("KB1: "+_kb1.getProject().getName());
//		Log.getLogger().info("KB2: "+_kb2.getProject().getName());
		//Collection originalKBFrames = _kb1.getFrames();
		//ChangeManagement cm = new ChangeManagement();
		Collection changes;
		Iterator i;
		Collection allChangedClasses = cm.getAllChangedClasses();

		changes = cm.getClassDeletedChanges();

		i = changes.iterator();
		while (i.hasNext()) {
			Instance nextChange = (Instance)i.next();
			String deletedClsName = cm.getChangedClassName(nextChange);
			if(_kb1.getFrame(deletedClsName)!= null){
				_results.addElement(new TableRow(_kb1.getFrame(deletedClsName),null,TableRow.RENAME_MINUS,TableRow.OPERATION_DELETE));
				//Log.getLogger().info("Added frame "+deletedClsName);
				allChangedClasses.remove(deletedClsName);
			}
			//originalKBFrames.remove(_kb1.getFrame(deletedClsName));
		}

		changes = cm.getClassRenamedChanges();

		i = changes.iterator();
		while (i.hasNext()) {
			Instance nextChange = (Instance)i.next();
			String newClsName = cm.getChangedClassName(nextChange);
			String oldClsName = cm.getOldClassName(nextChange);
			if(_kb2.getFrame(newClsName)!= null && _kb1.getFrame(oldClsName)!=null){
				_results.addElement(new TableRow(_kb1.getFrame(oldClsName),_kb2.getFrame(newClsName),TableRow.RENAME_PLUS,TableRow.OPERATION_MAP));
				//Log.getLogger().info("Added frame "+newClsName );
				allChangedClasses.remove(newClsName);
				allChangedClasses.remove(oldClsName);
			}
			//originalKBFrames.remove(_kb1.getFrame(oldClsName));
		}

		changes = cm.getClassCreatedChanges();

		i = changes.iterator();
		while (i.hasNext()) {
			Instance nextChange = (Instance)i.next();
			String createdClsName = cm.getChangedClassName(nextChange);
			if(_kb2.getFrame(createdClsName)!= null){
				_results.addElement(new TableRow(null,_kb2.getFrame(createdClsName),TableRow.RENAME_MINUS,TableRow.OPERATION_ADD));
				allChangedClasses.remove(createdClsName);
				//Log.getLogger().info("Adding frame "+createdClsName);
			}

		}


		/*i = originalKBFrames.iterator();
		 while (i.hasNext()) {
		 Frame nextFrame = (Frame)i.next();

		 _results.addElement(new TableRow(nextFrame,_kb2.getFrame(nextFrame.getName()),TableRow.RENAME_MINUS,TableRow.OPERATION_MAP));

		 }*/


		i = allChangedClasses.iterator();
		while (i.hasNext()) {
			String changedClass = (String)i.next();
			if(_kb2.getFrame(changedClass)!= null && _kb1.getFrame(changedClass)!=null){
				_results.addElement(new TableRow(_kb1.getFrame(changedClass),_kb2.getFrame(changedClass),TableRow.RENAME_MINUS,TableRow.OPERATION_MAP));
				//Log.getLogger().info("Adding frame "+changedClass);
			}

		}


	}

	public  void generateChangeOntology() {

        PostProcessorManager changes_db = new PostProcessorManager(_kb2);

        /*
		//Create the change project
		Collection errors = new ArrayList();
		String baseName = "annotation";
		String myNameSpace = "http://protege.stanford.edu/kb#";
		URI changeOntURI = null;
		try {
			changeOntURI = ChangesTab.class.getResource("/projects/changes.pprj").toURI();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		Project changes = Project.loadProjectFromURI(changeOntURI, errors);

		URI annotateURI = null;
		try {
			annotateURI = new URI(changes.getProjectURI().toString() +"/annotation.pprj");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		changes.setProjectURI(annotateURI);
        */
        Project changes = changes_db.getChangesProject();

		KnowledgeBase cKb = changes.getKnowledgeBase();

		/*
		RDFBackend.setSourceFiles(changes.getSources(), baseName + ".rdfs", baseName + ".rdf", myNameSpace);
        */
        List errors = new ArrayList();
		ProjectsAndKnowledgeBases.getProject(_kb2).save(errors);

		ChangeFactory changeFactory = new ChangeFactory(changes.getKnowledgeBase());

		boolean isOwl = PromptTab.kbInOWL();
		Iterator i = _results.values().iterator();
		while (i.hasNext()) {
			TableRow next = (TableRow)i.next();
			if(isOwl){
				if(next.getF1Value()!=null && next.getF1Value()instanceof Cls && OWLUtil.isOWLNamedClass ((Cls)next.getF1Value())||next.getF2Value()!=null && next.getF2Value()instanceof Cls&& OWLUtil.isOWLNamedClass ((Cls)next.getF2Value())){

					if (next.getOperationValue() == TableRow.OPERATION_ADD)
					{
						String clsCreated = next.getF2Value().getName();
						// Add instance of Class_created to Change Ontology
						ServerChangesUtil.createCreatedChange(changes_db, changeFactory.createCreated_Change(null), next.getF2Value(), clsCreated);
                    }
					else if(next.getOperationValue()== TableRow.OPERATION_DELETE)
					{
						String clsDeleted = next.getF1Value().getName();
						// Add instance of class_deleted to Change Ontology
						ServerChangesUtil.createDeletedChange(changes_db, changeFactory.createClass_Deleted(null), next.getF1Value(), clsDeleted);
					}
					else if(next.getOperationValue()== TableRow.OPERATION_MAP && next.getRenameValue()== TableRow.RENAME_PLUS)
					{
						String oldClsName = next.getF1Value().getName();
						String newClsName = next.getF2Value().getName();
						// Add instance of class_renamed to change ontology
                        ServerChangesUtil.createNameChange(changes_db, next.getF1Value(), oldClsName, newClsName);
					}
					else if(next.getOperationValue()== TableRow.OPERATION_MAP && next.getRenameValue()== TableRow.RENAME_MINUS && next.getMappingLevel()==TableRow.MAPPING_LEVEL_DIRECTLY_CHANGED)
					{
						Collection explanation = next.getOperationExplanation();
						for(Iterator iter = explanation.iterator();iter.hasNext();)
						{
							FrameDifferenceElement diff = (FrameDifferenceElement)iter.next();
							if(diff.getChangeDescription().equals("restriction added")) {
								ServerChangesUtil.createChangeStd(changes_db,
										changeFactory.createComposite_Change(null),
                                        next.getF1Value(),
                                        "Restriction Added: " +
                                        ((Frame)diff.getO2Value()).getBrowserText() +
                                         " to class " +
                                         next.getF1Value().getName());
                            }
							else if(diff.getChangeDescription().equals("restriction deleted")){
                                ServerChangesUtil.createChangeStd(changes_db,
                                									changeFactory.createComposite_Change(null),
                                                                  next.getF1Value(),
                                                                  ((Frame)diff.getO1Value()).getBrowserText());
                            }
						}
					}

				}
			}

			else{
				if(next.getF1Value()!=null && next.getF1Value()instanceof Cls||next.getF2Value()!=null && next.getF2Value()instanceof Cls){
					if (next.getOperationValue() == TableRow.OPERATION_ADD)
					{
						String clsCreated = next.getF2Value().getName();
						// Add instance of Class_created to Change Ontology
                        ServerChangesUtil.createCreatedChange(changes_db, changeFactory.createClass_Created(null), next.getF2Value(), clsCreated);
					}
					else if(next.getOperationValue()== TableRow.OPERATION_DELETE)
					{
						String clsDeleted = next.getF1Value().getName();
						// Add instance of class_deleted to Change Ontology
                        ServerChangesUtil.createDeletedChange(changes_db, changeFactory.createClass_Deleted(null), next.getF1Value(), clsDeleted);
					}
					else if(next.getOperationValue()== TableRow.OPERATION_MAP && next.getRenameValue()== TableRow.RENAME_PLUS)
					{
						String oldClsName = next.getF1Value().getName();
						String newClsName = next.getF2Value().getName();
						// Add instance of class_renamed to change ontology
                        ServerChangesUtil.createNameChange(changes_db, next.getF2Value(), oldClsName, newClsName);
					}
					else if(next.getOperationValue()== TableRow.OPERATION_MAP && next.getRenameValue()== TableRow.RENAME_MINUS && next.getMappingLevel()==TableRow.MAPPING_LEVEL_DIRECTLY_CHANGED)
					{
						Collection explanation = next.getOperationExplanation();
						for(Iterator iter = explanation.iterator();iter.hasNext();)
						{
							FrameDifferenceElement diff = (FrameDifferenceElement)iter.next();
							if(diff.getChangeDescription().equals("template slot added")) {
                                ServerChangesUtil.createChangeStd(changes_db,
                                		changeFactory.createTemplateSlot_Added(null),
                                                                  next.getF1Value(),
                                                                  "Template slot added " + ((Slot)diff.getO2Value()).getName());
							//Log.getLogger().info("Added template slot: "+((Slot)diff.getO2Value()).getName()+"to: "+next.getF1Value().getName());
                            }
							else if(diff.getChangeDescription().equals("template slot deleted")) {
                                ServerChangesUtil.createChangeStd(changes_db,
                                		changeFactory.createTemplateSlot_Removed(null),
                                                                  next.getF1Value(),
                                                                  "Template slot removed "+ ((Slot)diff.getO1Value()).getName());
                                //Log.getLogger().info("Removed template slot: "+((Slot)diff.getO1Value()).getName()+"from: "+next.getF1Value().getName());
                            }
						}
					}

				}// end if
			}//end else
		}// end while
		String projectName = ProjectsAndKnowledgeBases.getProject(_kb2).getProjectName();
		String changesName = "annotation_"+projectName;

		/*
		RDFBackend.setSourceFiles(changes.getSources(), changesName +".rdfs", changesName + ".rdf", myNameSpace);
		URI projUri;
		try {
			projUri = new URI(ProjectsAndKnowledgeBases.getProject(_kb2).getProjectDirectoryURI()+"/"+changesName +".pprj");
			changes.setProjectURI(projUri);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		*/
		changes.save(errors);




	}



	private void runFixedPoint () {

		Log.getLogger().info ("Diff started:  " + new Date());

		_results = new ResultTable (_kb1, _kb2);

		_results.initializeTable();
		_results.traceOn(false);
		Log.getLogger().info ("Loaded versions:  " + new Date());



		Log.getLogger().info ("Using id slot:  " + getIdSlotName() + "; checking id slot only: " + useIdSlotOnly());

		if (PromptDiff.getIdSlotName() != null) {
			// run compare concept ids
			try {
				runAlgorithm (Class.forName(COMPARE_CONCEPT_IDS));
			} catch (Exception e) {
				Log.getLogger().info("" + e);
			}
		}

		if (_useIdSlotOnly) {
			// no need for any heuristics, just compare ids
			try {
				runAlgorithm (Class.forName(COMPARE_OWL_ANONYMOUS_CLASSES));
			} catch (Exception e) {
				Log.getLogger().info("" + e);
			}
		} else {
			AlgorithmDependencyTable dependencyTable = new AlgorithmDependencyTable (ALGORITHM_NAMES);

			StackSet runningStack = new StackSet();
			Collection allAlgorithms = dependencyTable.getAllAlgorithms();
			Collection algorithmsThatDontAffectOtherAlgorithms = dependencyTable.getAlgorithmsThatDontAffectOtherAlgorithms();
			Collection algorithmsThatArentAffectedByOtherAlgorithms = dependencyTable.getAlgorithmsThatArentAffectedByOtherAlgorithms();

			allAlgorithms.removeAll(algorithmsThatDontAffectOtherAlgorithms);
			allAlgorithms.removeAll(algorithmsThatArentAffectedByOtherAlgorithms);

			runningStack.pushAll(algorithmsThatDontAffectOtherAlgorithms);
			runningStack.pushAll(allAlgorithms);
			runningStack.pushAll(algorithmsThatArentAffectedByOtherAlgorithms);

			while (!runningStack.isEmpty()) {
				Class nextToRun = (Class)runningStack.pop();
				if (runAlgorithm (nextToRun)) {
					runningStack.pushAll (dependencyTable.getAlgorithmsThatAreAffectedBy (nextToRun));
				}
			}
		}

		Log.getLogger().info ("Diff done:  " + new Date());

		ChangeAnalyzer analyzer = new ChangeAnalyzer(_results);
		analyzer.findChanges();

		Log.getLogger().info ("change analyzer done:  " + new Date());

		Log.getLogger().info ("start processing log:  " + new Date());
		_authorManagement = PromptAuthorManagement.getAuthorManagement(_kb1, _kb2, this);
		Log.getLogger().info ("processing log  done:  " + new Date());

		fireDiffDone();
		_results.printStatistics(System.out);

	}

	private static final Class [] _emptyArray = {};
	private boolean runAlgorithm (Class algorithm) {
		try{
			Method runMethod = algorithm.getMethod ("run", new Class [] {_results.getClass(), this.getClass()});
			return ((Boolean)runMethod.invoke(null, new Object [] {_results, this})).booleanValue ();
		} catch (InvocationTargetException e) {
			Log.getLogger().severe
			("Exception occurred for class " + algorithm + ": " + e.getTargetException());
			e.getTargetException().printStackTrace();
		} catch (Exception e) {
			Log.getLogger().severe("Exception occurred for class " + algorithm + ": " + e);
		}
		return false;
	}

	public List<TableRow> getResults () { return _results.sort ();}

	public ResultTable getResultsTable () { return _results;}

	public void setResultsTable (ResultTable diffTable) {
		_results = diffTable;
	}

	public KnowledgeBase getKb1 () {return _kb1;}

	public boolean showIncluded () {return _showIncluded;}

	public static void showIncluded (boolean value) {_showIncluded = value;}

	public KnowledgeBase getKb2 () {return _kb2;}

	@Override
	public String toString () {
		return "PromptDiff";
	}


	private static String _idSlotName = null;
	public static String getIdSlotName() {
		return _idSlotName;
	}

	public static void setIdSlotName(String idSlotName) {
		_idSlotName = idSlotName;
	}

	private static boolean _useIdSlotOnly = false;
	public static boolean useIdSlotOnly () {
		return _useIdSlotOnly;
	}

	public static void useIdSlotOnly (boolean value) {
		_useIdSlotOnly = value;
	}

	/**
	 * @return
	 */
	public DiffViewSetUp getViewSetUp() {
		return _viewSetUp;
	}

	/**
	 * @param up
	 */
	public void setViewSetUp(DiffViewSetUp vs) {

		_viewSetUp = vs;
		_results = vs.getResultsTable();
	}

	synchronized public void addDiffListener(DiffListener l) {
		if (_listeners == null) {
			_listeners = new Vector<DiffListener>();
		}
		_listeners.addElement(l);
	}

	synchronized public void removeDiffListener(DiffListener l) {
		if (_listeners == null) {
			_listeners = new Vector<DiffListener>();
		}
		_listeners.removeElement(l);
	}

	public void fireTableBuilt (DiffTableView view) {
		PromptListenerManager.fireTableBuilt(view);
		if (_listeners != null && !_listeners.isEmpty()) {
			DiffEvent event =
				new DiffEvent(PromptDiff.class);

			Vector<DiffListener> targets;
			synchronized (PromptDiff.class) {
				targets = (Vector<DiffListener>) _listeners.clone();
			}

			Enumeration<DiffListener> e = targets.elements();
			while (e.hasMoreElements()) {
				DiffListener l = e.nextElement();
				l.diffTableViewBuilt(view, event);
			}
		}
	}

	public void fireUIBuilt (JTabbedPane mainPane) {
		if (_listeners != null && !_listeners.isEmpty()) {
			DiffEvent event =
				new DiffEvent(PromptDiff.class);

			Vector<DiffListener> targets;
			synchronized (PromptDiff.class) {
				targets = (Vector<DiffListener>) _listeners.clone();
			}

			Enumeration<DiffListener> e = targets.elements();
			while (e.hasMoreElements()) {
				DiffListener l = e.nextElement();
				l.diffUIBuilt(mainPane, event);
			}
		}
	}

	protected void fireDiffDone () {
		PromptListenerManager.fireDiffDone();
		if (_listeners != null && !_listeners.isEmpty()) {
			DiffEvent event =
				new DiffEvent(PromptDiff.class);

			Vector<DiffListener> targets;
			synchronized (PromptDiff.class) {
				targets = (Vector<DiffListener>) _listeners.clone();
			}

			Enumeration<DiffListener> e = targets.elements();
			while (e.hasMoreElements()) {
				DiffListener l = e.nextElement();
				l.diffDone(event);
			}
		}
	}

	public AuthorManagement getAuthorManagement () { return _authorManagement; }

	public AcceptorRejector getAcceptorRejector () {
	    return _changeAcceptorRejector;
	}

	public void setAcceptorRejector (AcceptorRejector changeAcceptorRejector) {
	    _changeAcceptorRejector = changeAcceptorRejector;
	}

	public void dispose() {
	    if (_results != null) {
	        _results.dispose();
	    }
	}


}

