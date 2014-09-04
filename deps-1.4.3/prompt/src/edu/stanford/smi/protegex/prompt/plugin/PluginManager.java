/*
 * Contributor(s): Sean Falconer seanf@uvic.ca
 * 	               Natasha Noy noy@stanford.edu
 */

package edu.stanford.smi.protegex.prompt.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.plugin.PluginUtilities;
import edu.stanford.smi.protege.util.DirectoryClassLoader;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.plugin.model.ComparisonAlgorithmPlugin;
import edu.stanford.smi.protegex.prompt.plugin.model.DiffUIPlugin;
import edu.stanford.smi.protegex.prompt.plugin.model.DiffUIPluginPerspective;
import edu.stanford.smi.protegex.prompt.plugin.model.ExtractUIPlugin;
import edu.stanford.smi.protegex.prompt.plugin.model.ExtractUIPluginPerspective;
import edu.stanford.smi.protegex.prompt.plugin.model.MapUIPlugin;
import edu.stanford.smi.protegex.prompt.plugin.model.MapUIPluginPerspective;
import edu.stanford.smi.protegex.prompt.plugin.model.MappingStoragePlugin;
import edu.stanford.smi.protegex.prompt.plugin.model.MergeUIPlugin;
import edu.stanford.smi.protegex.prompt.plugin.model.MergeUIPluginPerspective;
import edu.stanford.smi.protegex.prompt.plugin.model.PromptPlugin;
import edu.stanford.smi.protegex.prompt.plugin.model.PromptUIPlugin;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.FrameDifferenceElement;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.TableRow;

/**
 * Singleton for managing plugin loading and communication.
 * 
 * @author seanf
 */
public class PluginManager {
	/** constant to represent a mapping UI plugin */
	public static final int PLUGIN_UI_MAP = 0;

	/** constant to represent a merge UI plugin */
	public static final int PLUGIN_UI_MERGE = 1;

	/** constant to represent a PromptDiff UI plugin */
	public static final int PLUGIN_UI_DIFF = 2;

	/** constant to represent a extracting UI plugin */
	public static final int PLUGIN_UI_EXTRACT = 3;

	/** constant to represent a comparison algorithm plugin */
	public static final int PLUGIN_ALG_COMPARISON = 4;

	/** constant to represent a mapping storage plugin */
	public static final int PLUGIN_MAPPING_STORAGE = 5;

	/** constant to represent a mapping ui perspective plugin */
	public static final int PLUGIN_UI_MAP_PERSPECTIVE = 6;

	/** constant to represent a merge ui perspective plugin */
	public static final int PLUGIN_UI_MERGE_PERSPECTIVE = 7;

	/** constant to represent a difference ui perspective plugin */
	public static final int PLUGIN_UI_DIFF_PERSPECTIVE = 8;

	/** constant to represent a extraction ui perspective plugin */
	public static final int PLUGIN_UI_EXTRACT_PERSPECTIVE = 9;

	/** extension points, these would be located in the manifest.mf for a plugin */
	private static final String MAP_UI_EXTENSION = "Map-Extension";
	private static final String MERGE_UI_EXTENSION = "Merge-Extension";
	private static final String DIFF_UI_EXTENSION = "Diff-Extension";
	private static final String EXTRACT_UI_EXTENSION = "Extract-Extension";
	private static final String COMPARISON_ALG_EXTENSION = "Comparison-Extension";
	private static final String MAPPING_STORAGE_EXTENSION = "Mapping-Storage-Extension";

	/** alternative perspectives, these would be located in the manifest.mf for a plugin */
	private static final String MAP_UI_PERSPECTIVE = "Map-Perspective";
	private static final String MERGE_UI_PERSPECTIVE = "Merge-Perspective";
	private static final String DIFF_UI_PERSPECTIVE = "Diff-Perspective";
	private static final String EXTRACT_UI_PERSPECTIVE = "Extract-Perspective";

	/** array of extension point types */
	private static final String[] PROMPT_EXTENSION_POINTS = { MAP_UI_EXTENSION, MERGE_UI_EXTENSION, DIFF_UI_EXTENSION, EXTRACT_UI_EXTENSION, COMPARISON_ALG_EXTENSION, MAPPING_STORAGE_EXTENSION,
			MAP_UI_PERSPECTIVE, MERGE_UI_PERSPECTIVE, DIFF_UI_PERSPECTIVE, EXTRACT_UI_PERSPECTIVE };

	/** array of interfaces that may be inherited by a plugin */
	private static final Class[] PROMPT_EXTENSION_POINTS_CLASS = { MapUIPlugin.class, MergeUIPlugin.class, DiffUIPlugin.class, ExtractUIPlugin.class, ComparisonAlgorithmPlugin.class,
			MappingStoragePlugin.class, MapUIPluginPerspective.class, MergeUIPluginPerspective.class, DiffUIPluginPerspective.class, ExtractUIPluginPerspective.class };

	/** used for singleton implementation */
	private static PluginManager pluginManager = null;

	/** map to link plugin package to class loader for each plugin */
	private Map<File, ClassLoader> pluginPackageToClassLoaderMap = new HashMap<File, ClassLoader>();

	/** Plugin directory location */
	private File pluginsDir;

	/** Collection of manifest URLs */
	private Collection<URL> manifestURLs = new HashSet<URL>();

	/** collections of different plugins */
	private Map<String, LinkedList<PromptPlugin>> plugins = new HashMap<String, LinkedList<PromptPlugin>>();

	/** reference ot the current algorithm plugin used for initial ontology comparison */
	private ComparisonAlgorithmPlugin activeComparisonPlugin;

	/** reference to the current mapping store plugin being used */
	private MappingStoragePlugin[] activeMappingStorage = null;
	private MappingStoragePlugin storagePluginWithSavedMappings = null;

	private FilenameFilter pluginPackageFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return new File(dir, name).isDirectory() && !name.equalsIgnoreCase("meta-inf") && !name.startsWith(".");
		}
	};

	private PluginManager() {
		init();
	}

	/**
	 * Gets the static instance of the PluginManager
	 * 
	 * @return Instance of the PluginManager
	 */
	public static PluginManager getInstance() {
		if (pluginManager == null) {
			pluginManager = new PluginManager();
		}

		return pluginManager;
	}

	public ComparisonAlgorithmPlugin getActiveComparisonAlgorithm() {
		return activeComparisonPlugin;
	}

	public void setActiveComparisonPlugin(ComparisonAlgorithmPlugin activeComparisonPlugin) {
		this.activeComparisonPlugin = activeComparisonPlugin;
	}

	public MappingStoragePlugin[] getActiveMappingStoragePlugins() {
		return activeMappingStorage;
	}

	public MappingStoragePlugin getStoragePluginWithSavedMappings() {
		return storagePluginWithSavedMappings;
	}

	public void setStoragePluginWithSavedMappings(MappingStoragePlugin pluginInstance) {
		this.storagePluginWithSavedMappings = pluginInstance;
	}

	public MappingStoragePlugin getFirstActiveMappingStoragePlugin() {
		if (activeMappingStorage == null || activeMappingStorage.length == 0) {
			return null;
		} else {
			return activeMappingStorage[0];
		}
	}

	public void setActiveMappingStoragePlugins(Collection storagePlugins) {
		if (storagePlugins == null) {
			return;
		}
		Iterator i = storagePlugins.iterator();

		activeMappingStorage = new MappingStoragePlugin[storagePlugins.size()];
		int index = 0;
		while (i.hasNext()) {
			MappingStoragePlugin nextStoragePlugin = (MappingStoragePlugin) i.next();
			activeMappingStorage[index] = nextStoragePlugin;
			index++;
		}
	}

	/**
	 * Gets all the Prompt UI plugins and returns them in a single list
	 * 
	 * @return A linked list of UI plugins
	 */
	public LinkedList<PromptPlugin> getAllUIPlugins() {
		LinkedList<PromptPlugin> uiPlugins = new LinkedList<PromptPlugin>();

		uiPlugins.addAll(getPlugins(PLUGIN_UI_MAP));
		uiPlugins.addAll(getPlugins(PLUGIN_UI_MERGE));
		uiPlugins.addAll(getPlugins(PLUGIN_UI_DIFF));
		uiPlugins.addAll(getPlugins(PLUGIN_UI_EXTRACT));

		return uiPlugins;
	}

	/**
	 * Gets a collection of plugins based on the type requested.
	 * 
	 * @param pluginType
	 * @return
	 */
	public LinkedList<PromptPlugin> getPlugins(int pluginType) {
		LinkedList<PromptPlugin> listToReturn = null;
		switch (pluginType) {
		case PLUGIN_UI_MAP:
			listToReturn = plugins.get(MAP_UI_EXTENSION);
			break;
		case PLUGIN_UI_MERGE:
			listToReturn = plugins.get(MERGE_UI_EXTENSION);
			break;
		case PLUGIN_UI_DIFF:
			listToReturn = plugins.get(DIFF_UI_EXTENSION);
			break;
		case PLUGIN_UI_EXTRACT:
			listToReturn = plugins.get(EXTRACT_UI_EXTENSION);
			break;
		case PLUGIN_ALG_COMPARISON:
			listToReturn = plugins.get(COMPARISON_ALG_EXTENSION);
			break;
		case PLUGIN_MAPPING_STORAGE:
			listToReturn = plugins.get(MAPPING_STORAGE_EXTENSION);
			break;
		case PLUGIN_UI_MAP_PERSPECTIVE:
			listToReturn = plugins.get(MAP_UI_PERSPECTIVE);
			break;
		case PLUGIN_UI_MERGE_PERSPECTIVE:
			listToReturn = plugins.get(MERGE_UI_PERSPECTIVE);
			break;
		case PLUGIN_UI_DIFF_PERSPECTIVE:
			listToReturn = plugins.get(DIFF_UI_PERSPECTIVE);
			break;
		case PLUGIN_UI_EXTRACT_PERSPECTIVE:
			listToReturn = plugins.get(EXTRACT_UI_PERSPECTIVE);
			break;
		}

		if (listToReturn == null) {
			return new LinkedList<PromptPlugin>();
		}
		return listToReturn;
	}

	/**
	 * Retrieves the PromptPlugin based on the type and the name of the plugin.
	 * 
	 * @param pluginType
	 * @param pluginName
	 * @return A PromptPlugin object.
	 */
	public PromptPlugin getPlugin(int pluginType, String pluginName) {
		LinkedList<PromptPlugin> plugins = getPlugins(pluginType);

		if (plugins == null) {
			return null;
		}

		for (PromptPlugin plugin : plugins) {
			if (plugin.getPluginName().equals(pluginName)) {
				return plugin;
			}
		}

		return null;
	}

	/**
	 * Retrieves the PromptPlugin based on the type and the index in the list.
	 * 
	 * @param pluginType
	 * @param pluginIndex
	 * @return A PromptPlugin object.
	 */
	public PromptPlugin getPlugin(int pluginType, int pluginIndex) {
		LinkedList<PromptPlugin> plugins = getPlugins(pluginType);

		if (plugins == null) {
			return null;
		}

		return plugins.get(pluginIndex);
	}

	/**
	 * Fires the save event for the DiffUIPlugin that is indicated by the index.
	 * 
	 * @param fileName The file the user selected to save to
	 * @param tableRows The table rows to save
	 * @param frameDifferences The frame differences to save
	 * @param pluginIndex The index of the plugin that we want to use.
	 */
	@SuppressWarnings("unchecked")
	public void fireDiffSaveEvent(String fileName, LinkedList tableRows, LinkedList frameDifferences, int pluginIndex) {
		DiffUIPlugin diffUIPlugin = (DiffUIPlugin) getPlugin(PLUGIN_UI_DIFF, pluginIndex);

		TableRow[] tableRowArray = (TableRow[]) tableRows.toArray(new TableRow[tableRows.size()]);
		FrameDifferenceElement[] frameDifferenceArray = (FrameDifferenceElement[]) frameDifferences.toArray(new FrameDifferenceElement[frameDifferences.size()]);

		diffUIPlugin.saveToFile(fileName, tableRowArray, frameDifferenceArray);
	}

	/**
	 * Fired when a selection list has had a selection event occur.
	 * 
	 * @param selectionListeners A list of custom selection listeners.
	 * @param selectedObject The object selected in the list.
	 */
	public void fireSelectionPerformed(LinkedList<SelectionListener> selectionListeners, Object selectedObject) {
		for (SelectionListener listener : selectionListeners) {
			listener.selectionPerformed(selectedObject);
		}
	}

	/**
	 * Fires the afterLoad() event for the given plugin type.
	 * 
	 * @param pluginType
	 */
	public void fireAfterLoad(int pluginType) {
		LinkedList<PromptPlugin> pluginList = getPlugins(pluginType);

		if (pluginList == null) {
			return;
		}

		try {
			for (PromptPlugin plugin : pluginList) {
				// check that the plugin is configured to be visible
				if (PluginFacade.isPluginVisible(plugin.getPluginName())) {
					PromptUIPlugin uiPlugin = (PromptUIPlugin) plugin;
					uiPlugin.afterLoad();
				}
			}
		} catch (ClassCastException e) {
			Log.getLogger().warning(e.getMessage());
		} catch (Exception e) {
			Log.getLogger().warning(e.getMessage());
		}
	}

	/**
	 * Fires the beforeClose() event for the given plugin type.
	 * 
	 * @param pluginType
	 */
	public void fireBeforeClose(int pluginType) {
		LinkedList<PromptPlugin> pluginList = getPlugins(pluginType);

		if (pluginList == null) {
			return;
		}

		try {
			for (PromptPlugin plugin : pluginList) {
				// check that the plugin is configured to be visible
				if (PluginFacade.isPluginVisible(plugin.getPluginName())) {
					PromptUIPlugin uiPlugin = (PromptUIPlugin) plugin;
					uiPlugin.beforeClose();
				}
			}
		} catch (ClassCastException e) {
			Log.getLogger().warning(e.getMessage());
		} catch (Exception e) {
			Log.getLogger().warning(e.getMessage());
		}
	}

	/**
	 * Calls the performAlignment function for the active comparison plugin.
	 * 
	 * @param progress The progress object
	 * @param sourceKnowledgeBase Source KnowledgeBase
	 * @param targetKnowledgeBase Target KnowledgeBase
	 * @param mappingKnowledgeBase The predefined mappings
	 * @return A collection of operations.
	 */
	public Collection fireAlignmentEvent(AlgorithmProgressMonitor progress, KnowledgeBase sourceKnowledgeBase, KnowledgeBase targetKnowledgeBase) {
		if (activeComparisonPlugin == null) {
			throw new NullPointerException("The active comparison plugin is null");
		}
		return activeComparisonPlugin.performAlignment(progress, sourceKnowledgeBase, targetKnowledgeBase);
	}

	/**
	 * Begin the initializing and loading of plugins.
	 */
	private void init() {
		try {
			loadPlugins();
		} catch (Exception e) {
			Log.getLogger().severe(Log.toString(e));
		}
	}

	/**
	 * Loads plugins located directly within the dir directory.
	 * 
	 * @param dir
	 */
	private void loadPlugins(File dir) {
		createClassLoaderAndLoadPlugins(dir, PluginUtilities.class.getClassLoader());
	}

	/**
	 * Loads plugins that are located in jar files directly below the plugins directory.
	 */
	private void loadLegacyPlugins() {
		loadPlugins(pluginsDir);
	}

	/**
	 * Loads all the Prompt plugins.
	 */
	private void loadPlugins() {
		findPluginsDirectory();

		if (pluginsDir != null) {
			loadLegacyPlugins();
			loadPluginPackages(pluginsDir);
		}
	}

	/**
	 * Loads the plugin packages within the plugin directory.
	 */
	private void loadPluginPackages(File dir) {
		File[] packages = dir.listFiles(pluginPackageFilter);
		Collection list = orderPackages(packages);
		Iterator i = list.iterator();
		while (i.hasNext()) {
			File packageDir = (File) i.next();
			if (!isLoaded(packageDir)) {
//            	Log.getLogger().info(packageDir.getName());
				createClassLoaderAndLoadPlugins(packageDir, getParentLoader(packageDir));
			}
		}
	}

	private ClassLoader getParentLoader(File packageDir) {
		return PluginManager.class.getClassLoader();
	}

	private ClassLoader createClassLoader(File directory, ClassLoader parentLoader) {
		return (directory == null) ? parentLoader : new DirectoryClassLoader(directory, parentLoader);
	}

	/**
	 * Gets all the manifest files for the given ClassLoader.
	 * 
	 * @param loader
	 * @return
	 */
	private Collection<Manifest> getNewManifests(ClassLoader loader) {
		Collection<Manifest> manifests = new ArrayList<Manifest>();
		try {
			Enumeration e = loader.getResources("META-INF/MANIFEST.MF");
			while (e.hasMoreElements()) {
				URL url = (URL) e.nextElement();
				addNew(url, manifests);
			}
		} catch (IOException e) {
			Log.getLogger().severe(Log.toString(e));
		}
		return manifests;
	}

	private void addNew(URL manifestURL, Collection<Manifest> newManifests) throws IOException {
		if (manifestURL != null && manifestURLs.add(manifestURL)) {
			newManifests.add(new Manifest(manifestURL.openStream()));
		}
	}

	private void loadPluginsWithClassLoader(File file, ClassLoader classLoader) {
		Collection manifests = getNewManifests(classLoader);

		Iterator i = manifests.iterator();
		while (i.hasNext()) {
			Manifest manifest = (Manifest) i.next();
			processManifest(manifest, classLoader);
		}

		// recursively load sub-directories in case there's jar files
		if (file.isDirectory()) {
			loadPluginPackages(file);
		}
	}

	/**
	 * Converts the attributeName to the class name.
	 * 
	 * @param attributeName
	 * @return
	 */
	private String attributeNameToClassName(String attributeName) {
		String className;
		if (attributeName.endsWith(".class")) {
			className = attributeName.substring(0, attributeName.length() - 6);
		} else {
			className = attributeName;
		}
		className = className.replace('/', '.');
		return className;
	}

	/**
	 * Checks whether the attributes has the key name and whether it's set to true.
	 * 
	 * @param attributes
	 * @param name
	 * @return
	 */
	private boolean isSet(Attributes attributes, String name) {
		boolean isSet = false;
		String s = attributes.getValue(name);
		if (s != null) {
			isSet = s.equalsIgnoreCase("true");
		}
		return isSet;
	}

	/**
	 * Checks whether the class is a loadable class, that is, implements one of our Prompt plugin
	 * extensions.
	 * 
	 * @param className
	 * @param loader
	 * @param interfac
	 * @return
	 */
	private boolean isLoadableClass(String className, ClassLoader loader, Class interfac) {
		boolean loadable = false;
		try {
			if (loader == null) {
				loader = SystemUtilities.class.getClassLoader();
			}

			Class clas = Class.forName(className, true, loader);
			if (clas != null) {
				loadable = interfac.isAssignableFrom(clas);

				if (!loadable) {
					Log.getLogger().warning(className + " does not implement " + interfac);
				}
			}
			//ESCA-JAVA0170 
		} catch (Throwable e) {
			Log.getLogger().warning(e.toString());
		}

		return loadable;
	}

	/**
	 * Sets the current thread context to use this ClassLoader.
	 * 
	 * @param loader
	 */
	private void setContextClassLoader(ClassLoader loader) {
		try {
			Thread.currentThread().setContextClassLoader(loader);
		} catch (SecurityException e) {
			// fails in applets, but plugins don't work there anyway
		}
	}

	/**
	 * Converts the class name into a Class object using the ClassLoader.
	 * 
	 * @param className
	 * @param loader
	 * @return
	 */
	private Class forName(String className, ClassLoader loader) {
		Class clas = null;
		ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();

		try {
			setContextClassLoader(loader);
			clas = Class.forName(className, true, loader);
		} catch (ClassNotFoundException e) {
			Log.getLogger().warning(e.getMessage());
		} catch (Throwable e) {
			Log.getLogger().warning(e.getMessage());
		}

		setContextClassLoader(oldLoader);
		return clas;
	}

	/**
	 * Adds the plugin to our collection of plugins based on the type.
	 * 
	 * @param pluginType
	 * @param plugin
	 */
	private void addPlugin(String pluginType, PromptPlugin plugin) {
		LinkedList<PromptPlugin> pluginList = plugins.get(pluginType);
		if (pluginList == null) {
			pluginList = new LinkedList<PromptPlugin>();
		}

		pluginList.add(plugin);
		plugins.put(pluginType, pluginList);
	}

	private void loadPlugin(Attributes attributes, String className, ClassLoader loader) {
		for (int i = 0; i < PROMPT_EXTENSION_POINTS.length; i++) {
			if (isSet(attributes, PROMPT_EXTENSION_POINTS[i])) {
				if (isLoadableClass(className, loader, PROMPT_EXTENSION_POINTS_CLASS[i])) {
					Class c = forName(className, loader);
					try {
						PromptPlugin promptPlugin = (PromptPlugin) c.newInstance();
						promptPlugin.invokePlugin();

						Log.getLogger().config("PROMPT: Loaded plugin " + className + " - " + promptPlugin.getPluginName());

						addPlugin(PROMPT_EXTENSION_POINTS[i], promptPlugin);
					} catch (Exception e) {
						Log.getLogger().warning(e.getMessage());
					}
				}
			}
		}
	}

	/**
	 * Processes the manifest and loads the plugin.
	 * 
	 * @param manifest
	 * @param loader
	 */
	private void processManifest(Manifest manifest, ClassLoader loader) {
		Iterator i = manifest.getEntries().keySet().iterator();
		while (i.hasNext()) {
			String attributeName = (String) i.next();
			Attributes attributes = manifest.getAttributes(attributeName);
			String className = attributeNameToClassName(attributeName);

			loadPlugin(attributes, className, loader);
		}
	}

	private void createClassLoaderAndLoadPlugins(File dir, ClassLoader parentLoader) {
		ClassLoader loader = createClassLoader(dir, parentLoader);
		pluginPackageToClassLoaderMap.put(dir, loader);
		loadPluginsWithClassLoader(dir, loader);
	}

	private boolean isLoaded(File packageDir) {
		return pluginPackageToClassLoaderMap.get(packageDir) != null;
	}

	private Collection orderPackages(File[] packages) {
		return Arrays.asList(packages);
	}

	private void findPluginsDirectory() {
		File dir = new File(PromptTab.getPromptDirectory());

		if (dir == null) {
			Log.getLogger().warning("Application directory not specified");
		} else {
			File file = new File(dir, "plugins");
			if (file.exists()) {
				pluginsDir = file;
			} else {
				Log.getLogger().warning("Plugins directory not found: " + file);
			}
		}
	}

	public static String getPromptPluginsDirectory() {
		return PromptTab.getPromptDirectory() + File.separatorChar + "plugins" + File.separatorChar;
	}
}
