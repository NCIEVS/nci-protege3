/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import edu.stanford.smi.protege.event.ProjectAdapter;
import edu.stanford.smi.protege.event.ProjectEvent;
import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.StringUtilities;
import edu.stanford.smi.protegex.owl.jena.creator.OwlProjectFromUriCreator;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.util.ImportHelper;
import edu.stanford.smi.protegex.owl.repository.Repository;
import edu.stanford.smi.protegex.prompt.actionLists.SuggestionsAndConflicts;
import edu.stanford.smi.protegex.prompt.plugin.PluginManager;
import edu.stanford.smi.protegex.prompt.plugin.model.MappingStoragePlugin;
import edu.stanford.smi.protegex.prompt.ui.diffUI.AcceptChangesKnowledgeBase;
import edu.stanford.smi.protegex.prompt.util.Util;

public class ProjectsAndKnowledgeBases {

	private static HashMap _kbs;
	private static String[] _projectAliases;
	static private int _maxActiveKbs = 4;
	static private int[] _sourceKbIndices = null; // these kbs show up in the sources window.

	// static public Project _mappingsProject = null;
	static public MappingStoragePlugin[] _mappingStoragePlugins = null;
	static public boolean _mappingsProjectDefined = false;
	static public KnowledgeBaseInMerging[] _mappingsKbInMerging = null;

	// static private KnowledgeBase _currentKb = null;
	// static private Project _currentProject = null;

	// static public Project _traversalDirectivesProject = null;

	// DIFF_MODE
	// activeKbs = 2
	static public final int NEW_VERSION_INDEX = 1; // current
	static public final int OLD_VERSION_INDEX = 0;
	static public final int ACCEPT_CHANGES_INDEX = 2;
	static public final int[] VERSION_SOURCE_KBS_INDICES = new int[] { NEW_VERSION_INDEX, OLD_VERSION_INDEX };
	// static private final int DIFF_MODE_ACTIVE_KBS = 2;
	static private AcceptChangesKnowledgeBase _acceptChangesKb = null;
	static private boolean _acceptChangesProjectDefined = false;

	// MAPPING_MODE
	static public final int MAPPING_SOURCE_INDEX = 3; // current
	static public final int MAPPING_TARGET_INDEX = 0;
	static public final int MAPPING_PROJECT_INDEX = 1;
	static public final int MERGED_PROJECT_IN_MAPPING_INDEX = 2;
	static private final int MAPPING_MODE_NUMBER_OF_KBS = 2;
	static public final int[] MAPPING_SOURCE_KBS_INDICES = new int[] { MAPPING_SOURCE_INDEX, MAPPING_TARGET_INDEX };

	// MERGING_MODE
	static public final int MERGING_SOURCE1_INDEX = 0;
	static public final int MERGING_SOURCE2_INDEX = 1;
	static public final int MAPPING_PROJECT_IN_MERGING_INDEX = 2;
	static public final int MERGED_PROJECT_INDEX = 3; // current
	static private final int MERGING_MODE_NUMBER_OF_KBS = 3;
	static public final int[] MERGING_SOURCE_KBS_INDICES = new int[] { MERGING_SOURCE1_INDEX, MERGING_SOURCE2_INDEX };
	static private Project _currentMergedProject = null;

	// EXTRACT_MODE
	static public final int EXTRACT_SOURCE_INDEX = 0;
	static public final int EXTRACT_TARGET_INDEX = 2; // current
	static public final int EXTRACT_VIEW_DEFINITIONS_INDEX = 1;
	static public final int[] EXTRACT_SOURCE_KBS_INDICES = new int[] { EXTRACT_SOURCE_INDEX };

	// MOVIING_MODE
	static public final int INCLUDING_PROJECT_INDEX = 1; // current
	static public final int INCLUDED_PROJECT_INDEX = 0;
	static public final int[] MOVING_SOURCE_KBS_INDICES = new int[] { INCLUDED_PROJECT_INDEX };

	static public AcceptChangesKnowledgeBase getAcceptChangesKb() {
		return _acceptChangesKb;
	}
	static public boolean acceptChangesProjectDefined() {
		return _acceptChangesProjectDefined;
	}

	static public boolean setUpNewProjects(String[] projectFileNames, String[] projectAliases) {
		_kbs = new HashMap(_maxActiveKbs * 2);
		_projectAliases = new String[_maxActiveKbs];
		clearAll();
		switch (PromptTab.getMode()) {
		case PromptModes.DIFF_MODE:
			return setUpProjectsForDiff(projectFileNames, projectAliases);
		case PromptModes.MAPPING_MODE:
			return setUpProjectsForMapping(projectFileNames, projectAliases);
		case PromptModes.MERGING_MODE:
			return setUpProjectsForMerging(projectFileNames, projectAliases);
		case PromptModes.EXTRACTING_MODE:
			return setUpProjectsForExtracting(projectFileNames, projectAliases);
		case PromptModes.MOVING_MODE:
			return setUpProjectsForMoving(projectFileNames, projectAliases);
		}
		return false;
	}

	static private boolean setUpProjectsForDiff(String[] projectFileNames, String[] projectAliases) {
		PromptTab.setKbInOWL(Util.kbInOWL(ProjectManager.getProjectManager().getCurrentProject().getKnowledgeBase()));

		// process new version -- currently opened project
		Project newVersionProject = openFile(projectFileNames[NEW_VERSION_INDEX], projectAliases[NEW_VERSION_INDEX], true);
		KnowledgeBaseInMerging newVersionKbInMerging = new KnowledgeBaseInMerging(newVersionProject, projectAliases[NEW_VERSION_INDEX], true);

		// process old version
		Project oldVersionProject = openFile(projectFileNames[OLD_VERSION_INDEX], projectAliases[OLD_VERSION_INDEX], false);
		if (oldVersionProject == null) {
			ModalDialog.showMessageDialog(ProjectManager.getProjectManager().getCurrentProjectView(), 
					"Could not open old version of project from:\n" +
					projectFileNames[OLD_VERSION_INDEX], "Open old version");
			return false;
		}
		
		if (!sameKnowledgeBaseType(newVersionProject.getKnowledgeBase(), oldVersionProject.getKnowledgeBase())) return false;
		KnowledgeBaseInMerging oldVersionKbInMerging = new KnowledgeBaseInMerging(oldVersionProject, projectAliases[OLD_VERSION_INDEX], false);

		newVersionKbInMerging.getKnowledgeBase().setCallCachingEnabled(false);
		oldVersionKbInMerging.getKnowledgeBase().setCallCachingEnabled(false);

		_projectAliases[NEW_VERSION_INDEX] = projectAliases[NEW_VERSION_INDEX];
		_projectAliases[OLD_VERSION_INDEX] = projectAliases[OLD_VERSION_INDEX];
		_sourceKbIndices = VERSION_SOURCE_KBS_INDICES;

		Log.getLogger().info("Loaded knowledge bases: " + printProjectAliases(projectAliases));

		Project acceptsProject = createAcceptsProject(newVersionProject);
		if (acceptsProject == null)
			_acceptChangesKb = new AcceptChangesKnowledgeBase();
		else
			_acceptChangesKb = new AcceptChangesKnowledgeBase(acceptsProject);

		newVersionProject.addProjectListener(new ProjectAdapter() {
			@Override
			public void projectSaved(ProjectEvent event) {
				_acceptChangesKb.save();
			}
		});

		return true;
	}

	static private Project createAcceptsProject(Project targetProject) {
		String targetProjectName = targetProject.getProjectName();
		String acProjectName = targetProjectName + "-accepts";
		File f = new File(URI.create(targetProject.getProjectDirectoryURI() + "/" + acProjectName + ".pprj"));
		Project result = null;
		if (f.exists()) {
			_acceptChangesProjectDefined = true;
			Collection errors = new ArrayList();
			result = new Project(f.toString(), errors);
			Util.displayErrors(errors);
		}
		return result;
	}

	static private boolean setUpProjectsForExtracting(String[] projectFileNames, String[] projectAliases) {
		PromptTab.setKbInOWL(Util.kbInOWL(ProjectManager.getProjectManager().getCurrentProject().getKnowledgeBase()));

		Project extractTargetProject = openFile(projectFileNames[EXTRACT_TARGET_INDEX], projectAliases[EXTRACT_TARGET_INDEX], true);
		KnowledgeBaseInMerging extractTargetKbInMerging = new KnowledgeBaseInMerging(extractTargetProject, projectAliases[EXTRACT_TARGET_INDEX], true);

		Project extractSourceProject = openFile(projectFileNames[EXTRACT_SOURCE_INDEX], projectAliases[EXTRACT_SOURCE_INDEX], false);
		if (!sameKnowledgeBaseType(extractSourceProject.getKnowledgeBase(), extractTargetKbInMerging.getKnowledgeBase())) return false;
		KnowledgeBaseInMerging extractSourceKbInMerging = new KnowledgeBaseInMerging(extractSourceProject, projectAliases[EXTRACT_SOURCE_INDEX], false);

		_projectAliases[EXTRACT_SOURCE_INDEX] = projectAliases[EXTRACT_SOURCE_INDEX];
		_projectAliases[EXTRACT_TARGET_INDEX] = projectAliases[EXTRACT_TARGET_INDEX];
		_sourceKbIndices = EXTRACT_SOURCE_KBS_INDICES;

		Log.getLogger().info("Loaded knowledge bases: " + printProjectAliases(projectAliases));

		if (!viewDefinitionsProjectDefined()) {
			_traversalDirectivesKb = new TraversalDirectivesKnowledgeBase();
		} else {
			Project traversalDirectivesProject = openFile(projectFileNames[EXTRACT_VIEW_DEFINITIONS_INDEX], projectAliases[EXTRACT_VIEW_DEFINITIONS_INDEX], false);
			_traversalDirectivesKb = new TraversalDirectivesKnowledgeBase(traversalDirectivesProject);
		}

		extractTargetProject.addProjectListener(new ProjectAdapter() {
			@Override
			public void projectSaved(ProjectEvent event) {
				_traversalDirectivesKb.save();
			}
		});

		return true;
	}

	static private boolean setUpProjectsForMoving(String[] projectFileNames, String[] projectAliases) {
		PromptTab.setKbInOWL(Util.kbInOWL(ProjectManager.getProjectManager().getCurrentProject().getKnowledgeBase()));

		Project includingProject = openFile(projectFileNames[INCLUDING_PROJECT_INDEX], projectAliases[INCLUDING_PROJECT_INDEX], true);
		KnowledgeBaseInMerging includingKbInMerging = new KnowledgeBaseInMerging(includingProject, projectAliases[INCLUDING_PROJECT_INDEX], true);

		Project includedProject = openFile(projectFileNames[INCLUDED_PROJECT_INDEX], projectAliases[INCLUDED_PROJECT_INDEX], false);
		KnowledgeBaseInMerging includedKbInMerging = new KnowledgeBaseInMerging(includedProject, projectAliases[INCLUDED_PROJECT_INDEX], false);

		_projectAliases[INCLUDING_PROJECT_INDEX] = projectAliases[INCLUDING_PROJECT_INDEX];
		_projectAliases[INCLUDED_PROJECT_INDEX] = projectAliases[INCLUDED_PROJECT_INDEX];
		_sourceKbIndices = MOVING_SOURCE_KBS_INDICES;

		Log.getLogger().info("Loaded knowledge bases: " + printProjectAliases(projectAliases));

		// need to create a copy of the source and include it in the target kb
		// and need to create a kb for traversal directives
		Collection alreadyIncluded = includingProject.getIncludedProjects();
		if (alreadyIncluded != null) {
			Iterator i = alreadyIncluded.iterator();
			while (i.hasNext()) {
				URI nextProject = (URI) i.next();
				if (nextProject.equals(projectFileNames[INCLUDED_PROJECT_INDEX])) return true;
			}
		}
		Collection errors = new ArrayList();
		includingProject.includeProject(projectFileNames[INCLUDED_PROJECT_INDEX], errors);

		includingProject.addProjectListener(new ProjectAdapter() {
			@Override
			public void projectSaved(ProjectEvent event) {
				saveProject(getKnowledgeBaseInMerging(INCLUDED_PROJECT_INDEX).getProject());
			}
		});

		return true;

	}

	static private boolean setUpProjectsForMerging(String[] projectFileNames, String[] projectAliases) {
		PromptTab.setKbInOWL(Util.kbInOWL(ProjectManager.getProjectManager().getCurrentProject().getKnowledgeBase()));

		URI u = ProjectManager.getProjectManager().getCurrentProject().getProjectURI();
		projectFileNames[MERGED_PROJECT_INDEX] = (u == null) ? "current" : u.toString();
		String currentProjectName = ProjectManager.getProjectManager().getCurrentProject().getKnowledgeBase().getName();
		projectAliases[MERGED_PROJECT_INDEX] = (currentProjectName == null) ? "current" : currentProjectName;

		Project mergedProject = openFile(projectFileNames[MERGED_PROJECT_INDEX], projectAliases[MERGED_PROJECT_INDEX], true);
		KnowledgeBaseInMerging mergedKbInMerging = new KnowledgeBaseInMerging(mergedProject, projectAliases[MERGED_PROJECT_INDEX], true);

		Project source1Project = openFile(projectFileNames[MERGING_SOURCE1_INDEX], projectAliases[MERGING_SOURCE1_INDEX], false);
		if (!sameKnowledgeBaseType(source1Project.getKnowledgeBase(), mergedKbInMerging.getKnowledgeBase())) return false;
		KnowledgeBaseInMerging kb1InMerging = new KnowledgeBaseInMerging(source1Project, projectAliases[MERGING_SOURCE1_INDEX], false);

		Project source2Project = openFile(projectFileNames[MERGING_SOURCE2_INDEX], projectAliases[MERGING_SOURCE2_INDEX], false);
		if (!sameKnowledgeBaseType(source2Project.getKnowledgeBase(), mergedKbInMerging.getKnowledgeBase())) return false;
		KnowledgeBaseInMerging kb2InMerging = new KnowledgeBaseInMerging(source2Project, projectAliases[MERGING_SOURCE2_INDEX], false);

		addIncludedOrImportedProjects(mergedProject, mergedKbInMerging, source1Project, kb1InMerging, source2Project, kb2InMerging);

		_projectAliases[MERGED_PROJECT_INDEX] = projectAliases[MERGED_PROJECT_INDEX];
		_projectAliases[MERGING_SOURCE1_INDEX] = projectAliases[MERGING_SOURCE1_INDEX];
		_projectAliases[MERGING_SOURCE2_INDEX] = projectAliases[MERGING_SOURCE2_INDEX];
		_sourceKbIndices = MERGING_SOURCE_KBS_INDICES;

		Log.getLogger().info("Loaded knowledge bases: " + printProjectAliases(projectAliases));

		KnowledgeBase mappingSourceKb = Preferences.preferredOntology();
		if (mappingSourceKb == null) mappingSourceKb = source1Project.getKnowledgeBase();

		KnowledgeBase mappingTargetKb = source2Project.getKnowledgeBase();
		_mappingStoragePlugins = PluginManager.getInstance().getActiveMappingStoragePlugins();

		Project mappingsProject = null;
		if (_mappingsProjectDefined) {
			mappingsProject = openFile(projectFileNames[MAPPING_PROJECT_IN_MERGING_INDEX], projectAliases[MAPPING_PROJECT_IN_MERGING_INDEX], false);
		}

		if (_mappingStoragePlugins != null) {
			_mappingsKbInMerging = new KnowledgeBaseInMerging[_mappingStoragePlugins.length];
			for (int i = 0; i < _mappingStoragePlugins.length; i++) {
				_mappingStoragePlugins[i].initialize(mappingSourceKb, mappingTargetKb, mappingsProject);
				_mappingsKbInMerging[i] = new KnowledgeBaseInMerging(_mappingStoragePlugins[i].getProject(), "mapping", false);
			}
		}

		Preferences.considerInheritedSlots(true);

		mergedProject.addProjectListener(new ProjectAdapter() {
			@Override
			public void projectSaved(ProjectEvent event) {
				if (_mappingStoragePlugins == null) return;
				for (int i = 0; i < _mappingStoragePlugins.length; i++)
					_mappingStoragePlugins[i].save();
			}
		});

		return true;
	}

	static private boolean setUpProjectsForMapping(String[] projectFileNames, String[] projectAliases) {
		PromptTab.setKbInOWL(Util.kbInOWL(ProjectManager.getProjectManager().getCurrentProject().getKnowledgeBase()));

		projectFileNames[MAPPING_SOURCE_INDEX] = ProjectManager.getProjectManager().getCurrentProject().getProjectURI().toString();
		projectAliases[MAPPING_SOURCE_INDEX] = ProjectManager.getProjectManager().getCurrentProject().getKnowledgeBase().getName();

		Project mappingSourceProject = openFile(projectFileNames[MAPPING_SOURCE_INDEX], projectAliases[MAPPING_SOURCE_INDEX], true);
		KnowledgeBaseInMerging mappingSourceKbInMerging = new KnowledgeBaseInMerging(mappingSourceProject, projectAliases[MAPPING_SOURCE_INDEX], false);

		Project mappingTargetProject = openFile(projectFileNames[MAPPING_TARGET_INDEX], projectAliases[MAPPING_TARGET_INDEX], false);
		if (!sameKnowledgeBaseType(mappingTargetProject.getKnowledgeBase(), mappingSourceKbInMerging.getKnowledgeBase())) return false;
		KnowledgeBaseInMerging mappingTargetKbInMerging = new KnowledgeBaseInMerging(mappingTargetProject, projectAliases[MAPPING_TARGET_INDEX], false);

		Project mergedProject = Util.createNewProject(mappingSourceProject.getKnowledgeBaseFactory());
		projectAliases[MERGED_PROJECT_IN_MAPPING_INDEX] = "merged";
		KnowledgeBaseInMerging mergedKbInMerging = new KnowledgeBaseInMerging(mergedProject, projectAliases[MERGED_PROJECT_IN_MAPPING_INDEX], true);

		addIncludedOrImportedProjects(mergedProject, mergedKbInMerging, mappingSourceProject, mappingSourceKbInMerging, mappingTargetProject, mappingTargetKbInMerging);

		_projectAliases[MAPPING_SOURCE_INDEX] = projectAliases[MAPPING_SOURCE_INDEX];
		_projectAliases[MAPPING_TARGET_INDEX] = projectAliases[MAPPING_TARGET_INDEX];
		_projectAliases[MERGED_PROJECT_IN_MAPPING_INDEX] = projectAliases[MERGED_PROJECT_IN_MAPPING_INDEX];
		_sourceKbIndices = MAPPING_SOURCE_KBS_INDICES;

		Log.getLogger().info("Loaded knowledge bases: " + printProjectAliases(projectAliases));
		// mergedProject.getKnowledgeBase().addKnowledgeBaseListener(new PromptKnowledgeBaseListener
		// ());

		KnowledgeBase mappingSourceKb = Preferences.preferredOntology();
		if (mappingSourceKb == null) mappingSourceKb = mappingSourceProject.getKnowledgeBase();

		KnowledgeBase mappingTargetKb = mappingTargetProject.getKnowledgeBase();
		_mappingStoragePlugins = PluginManager.getInstance().getActiveMappingStoragePlugins();

		Project mappingsProject = null;
		if (_mappingsProjectDefined) {
			mappingsProject = openFile(projectFileNames[MAPPING_PROJECT_INDEX], projectAliases[MAPPING_PROJECT_INDEX], false);
		}

		if (_mappingStoragePlugins != null) {
			_mappingsKbInMerging = new KnowledgeBaseInMerging[_mappingStoragePlugins.length];
			for (int i = 0; i < _mappingStoragePlugins.length; i++) {
				_mappingStoragePlugins[i].initialize(mappingSourceKb, mappingTargetKb, mappingsProject);
				_mappingsKbInMerging[i] = new KnowledgeBaseInMerging(_mappingStoragePlugins[i].getProject(), "mapping", false);
			}
		}

		Preferences.considerInheritedSlots(true);

		mappingSourceProject.addProjectListener(new ProjectAdapter() {
			@Override
			public void projectSaved(ProjectEvent event) {
				if (_mappingStoragePlugins == null) return;
				for (int i = 0; i < _mappingStoragePlugins.length; i++)
					_mappingStoragePlugins[i].save();
			}
		});

		return true;
	}

	static public KnowledgeBaseInMerging getKnowledgeBaseInMerging(int index) {
		return (KnowledgeBaseInMerging) _kbs.get(_projectAliases[index]);
	}

	static public KnowledgeBase getKnowledgeBase(int index) {
		return ((KnowledgeBaseInMerging) _kbs.get(_projectAliases[index])).getKnowledgeBase();
	}

	static public void addToKbs(String prettyName, KnowledgeBaseInMerging kbInMerging) {
		_kbs.put(prettyName, kbInMerging); // <kbName, kbInMerging>
		_kbs.put(kbInMerging.getKnowledgeBase(), kbInMerging); // <kb, kbInMerging>

	}

	static private boolean sameKnowledgeBaseType(KnowledgeBase kb1, KnowledgeBase kb2) {
		if (kb1.getClass().equals(kb2.getClass())) return true;
		if (kb1 instanceof OWLModel && kb2 instanceof OWLModel) return true;
		if (kb1 instanceof OWLModel || kb2 instanceof OWLModel) return false;
		if ((StringUtilities.getClassName(kb2).equals(("OldJdbcDefaultKnowledgeBase"))) && (StringUtilities.getClassName(kb1).equals("DefaultKnowledgeBase"))) return true;
		if ((StringUtilities.getClassName(kb1).equals(("OldJdbcDefaultKnowledgeBase"))) && (StringUtilities.getClassName(kb2).equals("DefaultKnowledgeBase"))) return true;
		return false;
	}

	// reconsider this
	static public void clearAll() {
		SuggestionsAndConflicts.clearAll();
		if (_kbs != null) {
			_kbs.clear();
		}
	}
	
	static public void dispose() {
		clearAll();
		_acceptChangesKb = null;
		_acceptChangesProjectDefined = false;
		_currentMergedProject = null;
		_mappingsKbInMerging = null;
		_mappingsProjectDefined = false;
		_mappingStoragePlugins = null;
		_projectAliases = null;
		_sourceKbIndices = null;
		_traversalDirectivesKb = null;
	}

	private static void saveProject(Project project) {
		Collection errors = new ArrayList();
		boolean isReadonly = project.isReadonly();
		project.setIsReadonly(false);
		project.save(errors);
		project.setIsReadonly(isReadonly);
	}


	public static Project _includedProject = null; // used only for moving
	static public boolean _viewDefinitionsProjectDefined = false;
	public static TraversalDirectivesKnowledgeBase _traversalDirectivesKb = null;


	static private Project openFile(String projectFileName, String projectAlias, boolean processingTarget) {
		Project newProject;
		if (!processingTarget) {		
			Collection errors = new ArrayList();
			if (isOWLProjectPath(projectFileName)) {
				newProject = openOWLFile(projectFileName, errors);
			} else {
				newProject = new Project(projectFileName, errors);
			}
			Util.displayErrors(errors);
		} else {
			newProject = ProjectManager.getProjectManager().getCurrentProject();
		}

		return newProject;
	}
	
	static private boolean isOWLProjectPath(String projectFileName) {
		return 	projectFileName.endsWith(".owl") || 
				projectFileName.endsWith(".rdf") ||
				projectFileName.endsWith(".rdfs");
	}
	
	static private Project openOWLFile(String projectFileName, Collection errors) {
		OwlProjectFromUriCreator creator = new OwlProjectFromUriCreator();
		creator.setOntologyUri(projectFileName);
		try {
			creator.create(errors);
		} catch (OntologyLoadException e) {
			Log.getLogger().log(Level.SEVERE, "Prompt: Could not open file: " + projectFileName, e);
		}
		return creator.getProject();
	}
	

	private static String printProjectAliases(String[] projectAliases) {
		String result = "";
		for (int i = 0; i < projectAliases.length; i++) {
			if (projectAliases[i] != null) result = result + " " + projectAliases[i];
		}
		return result;
	}

	public static boolean viewDefinitionsProjectDefined() {
		return _viewDefinitionsProjectDefined;
	}

	public static void viewDefinitionsProjectDefined(boolean b) {
		if (PromptTab.makeViewsExplicit()) _viewDefinitionsProjectDefined = b;
	}

	public static void mappingProjectDefined(boolean b) {
		_mappingsProjectDefined = b;
	}

	public static boolean mappingProjectDefined() {
		return _mappingsProjectDefined;
	}
	public static int getNumberOfKbs() {
		if (PromptTab.mapping()) return MAPPING_MODE_NUMBER_OF_KBS;
		return MERGING_MODE_NUMBER_OF_KBS;
	}
	static public KnowledgeBaseInMerging[] getMappingKnowledgeBaseInMerging() {
		return _mappingsKbInMerging;
	}

	static public KnowledgeBaseInMerging getFirstMappingKnowledgeBaseInMerging() {
		if (_mappingsKbInMerging == null || _mappingsKbInMerging.length == 0) return null;
		return _mappingsKbInMerging[0];
	}

	// static public KnowledgeBaseInMerging getTargetKnowledgeBaseInMerging () {
	// return _targetKbInMerging;
	// }

	static public KnowledgeBaseInMerging getKnowledgeBaseInMerging(String prettyName) {
		return (KnowledgeBaseInMerging) _kbs.get(prettyName);
	}
	static public KnowledgeBaseInMerging getKnowledgeBaseInMerging(KnowledgeBase kb) {
		return (KnowledgeBaseInMerging) _kbs.get(kb);
	}

	static public MappingStoragePlugin[] getMappingStoragePlugins() {
		return _mappingStoragePlugins;
	}

	static public MappingStoragePlugin getFirstMappingStoragePlugin() {
		if (_mappingStoragePlugins == null || _mappingStoragePlugins.length == 0) return null;
		return _mappingStoragePlugins[0];
	}

	static public String getKnowledgeBasePrettyName(KnowledgeBase kb) {
		if (kb == null) return null;
		KnowledgeBaseInMerging kbTemp = (KnowledgeBaseInMerging) _kbs.get(kb);
		return kbTemp.getPrettyName();
	}
	static public KnowledgeBase getKnowledgeBase(String prettyName) {
		KnowledgeBaseInMerging kb = (KnowledgeBaseInMerging) _kbs.get(prettyName);
		if (kb == null)
			return null;
		else
			return kb.getKnowledgeBase();
	}
	static public Project getProject(KnowledgeBase kb) {
		KnowledgeBaseInMerging kbTemp = (KnowledgeBaseInMerging) _kbs.get(kb);
		if (kb == null)
			return null;
		else
			return kbTemp.getProject();
	}

	public static KnowledgeBaseInMerging getMappingTargetKnowledgeBaseInMerging() {
		return (KnowledgeBaseInMerging) _kbs.get(_projectAliases[MAPPING_TARGET_INDEX]);
	}

	public static KnowledgeBaseInMerging getMappingSourceKnowledgeBaseInMerging() {
		return (KnowledgeBaseInMerging) _kbs.get(_projectAliases[MAPPING_SOURCE_INDEX]);
	}

	public static KnowledgeBase getOtherSourceKnowledgeBase(KnowledgeBase knowledgeBase) {
		KnowledgeBase kb = getKnowledgeBase(MERGING_SOURCE1_INDEX);
		if (kb.equals(knowledgeBase)) return getKnowledgeBase(MERGING_SOURCE2_INDEX);
		return kb;
	}

	public static String[] getSourceProjectsPrettyNames() {
		if (PromptTab.merging()) return new String[] { _projectAliases[MERGING_SOURCE1_INDEX], _projectAliases[MERGING_SOURCE2_INDEX], _projectAliases[MERGED_PROJECT_INDEX] };
		if (PromptTab.mapping()) return new String[] { _projectAliases[MAPPING_SOURCE_INDEX], _projectAliases[MAPPING_TARGET_INDEX] };
		if (PromptTab.extracting()) return new String[] { _projectAliases[EXTRACT_SOURCE_INDEX], _projectAliases[EXTRACT_TARGET_INDEX] };
		return _projectAliases;
	}

	public static KnowledgeBaseInMerging[] getSourceKnowledgeBasesInMerging() {
		KnowledgeBaseInMerging[] result = new KnowledgeBaseInMerging[_sourceKbIndices.length];
		for (int i = 0; i < _sourceKbIndices.length; i++)
			result[i] = getKnowledgeBaseInMerging(_sourceKbIndices[i]);
		return result;
	}
	public static TraversalDirectivesKnowledgeBase getTraversalDirectivesKb() {
		return _traversalDirectivesKb;
	}

	public static KnowledgeBase getTargetKnowledgeBase() {
		if (PromptTab.merging()) return getKnowledgeBase(MERGED_PROJECT_INDEX);
		if (PromptTab.mapping()) return getKnowledgeBase(MERGED_PROJECT_IN_MAPPING_INDEX);
		return ProjectManager.getProjectManager().getCurrentProject().getKnowledgeBase();
	}

	public static KnowledgeBaseInMerging getTargetKnowledgeBaseInMerging() {
		return getKnowledgeBaseInMerging(getTargetKnowledgeBase());
	}

	public static KnowledgeBase getMappingSource1() {
		if (PromptTab.merging()) return getKnowledgeBase(MERGING_SOURCE1_INDEX);
		return getKnowledgeBase(MAPPING_SOURCE_INDEX);
	}

	public static KnowledgeBase getMappingSource2() {
		if (PromptTab.merging()) return getKnowledgeBase(MERGING_SOURCE2_INDEX);
		return getKnowledgeBase(MAPPING_TARGET_INDEX);
	}

	public static KnowledgeBaseInMerging getMappingSource1KnowledgeBaseInMerging() {
		return getKnowledgeBaseInMerging(getMappingSource1());
	}

	public static KnowledgeBaseInMerging getMappingSource2KnowledgeBaseInMerging() {
		return getKnowledgeBaseInMerging(getMappingSource2());
	}

	private static void addIncludedOrImportedProjects(Project mergedProject, KnowledgeBaseInMerging mergedKbInMerging, Project source1, KnowledgeBaseInMerging kbInMerging1, Project source2,
			KnowledgeBaseInMerging kbInMerging2) {
		if (PromptTab.kbInOWL())
			addImportedProjects(mergedProject, mergedKbInMerging, source1, kbInMerging1, source2, kbInMerging2);
		else
			addIncludedProjects(mergedProject, mergedKbInMerging, source1, kbInMerging1, source2, kbInMerging2);
	}

	private static void addImportedProjects(Project mergedProject, KnowledgeBaseInMerging mergedKbInMerging, Project source1, KnowledgeBaseInMerging kbInMerging1, Project source2,
			KnowledgeBaseInMerging kbInMerging2) {
		OWLModel source1OwlModel = (OWLModel) source1.getKnowledgeBase();
		Set<String> source1Imports = source1OwlModel.getAllImports();
		List<Repository> repositories = source1OwlModel.getRepositoryManager().getAllRepositories();

		OWLModel source2OwlModel = (OWLModel) source2.getKnowledgeBase();
		Set<String> source2Imports = source2OwlModel.getAllImports();
		repositories.addAll(source2OwlModel.getRepositoryManager().getAllRepositories());

		Set<String> sourcesImports = source1Imports;
		sourcesImports.addAll(source2Imports);

		OWLModel mergedOwlModel = (OWLModel) mergedProject.getKnowledgeBase();
		Iterator<Repository> r = repositories.iterator();
		while (r.hasNext()) {
			Repository nextRepository = r.next();
			mergedOwlModel.getRepositoryManager().addProjectRepository(nextRepository);
		}

		ImportHelper importHelper = new ImportHelper(mergedOwlModel);

		Iterator<String> i = sourcesImports.iterator();

		try {
			while (i.hasNext()) {
				String nextImport = i.next();
				importHelper.addImport(new URI(nextImport));
			}

			importHelper.importOntologies();

			kbInMerging1.setWhatBecameOfItForImportedOrIncludedFrames(mergedProject);
			kbInMerging2.setWhatBecameOfItForImportedOrIncludedFrames(mergedProject);
		} catch (Exception e) {}
	}

	private static void addIncludedProjects(Project mergedProject, KnowledgeBaseInMerging mergedKbInMerging, Project source1, KnowledgeBaseInMerging kbInMerging1, Project source2,
			KnowledgeBaseInMerging kbInMerging2) {
		Collection<URI> source1IncludedProjects = source1.getDirectIncludedProjectURIs();
		Collection<URI> source2IncludedProjects = source2.getDirectIncludedProjectURIs();
		Collection<URI> allIncludedProjects = new HashSet<URI>();
		allIncludedProjects.addAll(source1IncludedProjects);
		allIncludedProjects.addAll(source2IncludedProjects);

		allIncludedProjects.removeAll(mergedProject.getDirectIncludedProjectURIs());

		if (allIncludedProjects.size() == 0) return;

		String projectName = mergedProject.getProjectName();
		if (projectName == null) {
			String tempdir = System.getProperty("java.io.tmpdir");
			if (!(tempdir.endsWith("/") || tempdir.endsWith("\\"))) tempdir = tempdir + System.getProperty("file.separator");
			String projectFileName = tempdir + "merged_temp.pprj";
			URI tempURI = new File(projectFileName).toURI();
			mergedProject.setProjectURI(tempURI);
			mergedProject.setProjectFilePath(projectFileName);
		}

		mergedProject.setDirectIncludedProjectURIs(allIncludedProjects);

		Collection errors = new ArrayList();
		mergedProject.save(errors);
		Util.displayErrors(errors);
		Project newMergedProject = new Project(mergedProject.getProjectURI().toString(), errors);
		_currentMergedProject = newMergedProject;

		mergedProject.addProjectListener(new ProjectAdapter() {
			@Override
			public void projectSaved(ProjectEvent event) {
				Collection savingErrors = new ArrayList();
				_currentMergedProject.save(savingErrors);
				Util.displayErrors(savingErrors);
			}
		});

		mergedKbInMerging.updateProjectAndKb(newMergedProject);

		kbInMerging1.setWhatBecameOfItForImportedOrIncludedFrames(newMergedProject);
		kbInMerging2.setWhatBecameOfItForImportedOrIncludedFrames(newMergedProject);

	}	
}
