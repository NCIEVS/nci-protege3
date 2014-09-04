/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */
package edu.stanford.smi.protegex.prompt.plugin.util;

import java.io.*;
import java.net.*;

import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.plugin.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class PluginUtilities {
	public static  URI getMappingProjectURI (String pluginDirectory, String jarName, String projectName) {
		try {
			File installationDir = new File (PluginManager.getPromptPluginsDirectory() + File.separatorChar + pluginDirectory);
			URI projectURI = Util.getProjectFromJar (installationDir, jarName, projectName);
			if (projectURI != null)
				return projectURI; 
			else 
				return Util.getProjectFromDirectory (installationDir, projectName);
		} catch (Exception e) {
			Log.getLogger().severe ("Cannot create a URI");
			return null;
		}
	}

}
