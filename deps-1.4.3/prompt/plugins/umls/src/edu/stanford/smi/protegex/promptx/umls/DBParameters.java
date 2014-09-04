/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */
package edu.stanford.smi.protegex.promptx.umls;

import java.io.*;
import java.util.*;

import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.plugin.*;

public class DBParameters {
	private static final String DRIVER_PROPERTY = "driver";
	private static final String IP_ADDRESS_PROPERTY = "jdbcURL";
	private static final String USER_NAME_PROPERTY = "userName";
	private static final String PASSWORD_PROPERTY = "password";
	
	
	private static String _driver = "";
	private static String _jdbcURL = "";
	private static String _userName = "";
	private static String _password = "";
	
	private static boolean _initialized = false;
	private static Properties _props = null;
	private static String PROPERTIES_FILE_NAME = "PromptUMLSPlugin.properties";

	private static File propsFile = new File(PluginManager.getPromptPluginsDirectory() + UMLSPlugin.getPluginDirectoryName() + File.separatorChar + PROPERTIES_FILE_NAME);

	public static void loadParams() { // Loads a ResourceBundle and creates Properties from it
		if (_initialized )
			return;  

		try {
			if (!propsFile.exists()) {
				Log.getLogger().warning("No UMLS Plugin properties file exists");
				return ;
			}
			_props = new Properties();
			_props.clear();
			_props.load(new FileInputStream(propsFile));
			
			_driver = (String)_props.getProperty(DRIVER_PROPERTY);
			_jdbcURL = (String)_props.getProperty(IP_ADDRESS_PROPERTY);
			_userName = (String)_props.getProperty(USER_NAME_PROPERTY);
			_password = (String)_props.getProperty(PASSWORD_PROPERTY);
			
			_initialized = true;
			return ;
		} catch (IOException ex) {
			Log.getLogger().warning("Could not open UMLS Plugin properties file");
			return ;
		}
	}

	public static String getDriver() {
		return _driver;
	}

	public static void setDriver(String driver) {
		DBParameters._driver = driver;
	}

	public static String getJdbcUrl() {
		return _jdbcURL;
	}

	public static void setJdbcUrl(String address) {
		_jdbcURL = address;
	}

	public static String getPassword() {
		return _password;
	}

	public static void setPassword(String password) {
		_password = password;
	}

	public static String getUserName() {
		return _userName;
	}

	public static void setUserName(String name) {
		_userName = name;
	}

}
