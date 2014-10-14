/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License");  you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * http//www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * The Original Code is PROMPT NCI CONCEPT History.
 *
 * The Initial Developer of the Original Code is Stanford University. Portions
 * created by Stanford University are Copyright (C) 2004.  All Rights Reserved.
 *
 * PROMPT was developed by Stanford Medical Informatics
 * (http//www.smi.stanford.edu) at the Stanford University School of Medicine
 * with support from the the Defense Advanced Research Projects Agency and National
 * Cancer Institute.  Current information about PROMPT can be obtained at
 * http//protege.stanford.edu
 *

 * Created by IntelliJ IDEA.
 * User: prashr
 * Date: Oct 13, 2004
 * Time: 8:18:28 PM
 * To change this template use File | Settings | File Templates.
 */
package edu.stanford.smi.protegex.NCIConceptHistory;
   
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import edu.stanford.smi.protege.util.ApplicationProperties;


public class NCIConceptHistorySettings {
    public final static String USERNAME_PROP  = "userName";
    public final static String IP_ADDR_PROP   = "ipAddress";
    public final static String DB_NAME_PROP   = "dataBaseName";
    public final static String PASSWORD_PROP  = "password";
    public final static String DB_DRIVER_PROP = "dbdriver";
    public final static String DB_URL_PROP    = "dburl";
    public final static String DB_TABLE_PROP  = "dbConceptHistoryTable";
    
    public static final String DEFAULT_DB_DRIVER = "com.mysql.jdbc.Driver";
    
    public String ipAddress;
    public String userName;
    public String password;
    public String dataBaseName;
    public static boolean initialized = false;
    
    private static Properties prop = null;
    private static String pathSuffixTestVariable = ApplicationProperties.getApplicationDirectory().getAbsolutePath();
    private static String path = pathSuffixTestVariable.endsWith("/") ? pathSuffixTestVariable : pathSuffixTestVariable + "/";
    private static String propsFileName = new StringBuffer().append(path).append("PromptNCIPlugin.properties").toString();

    public static Properties loadParams() throws IOException {        // Loads a ResourceBundle and creates Properties from it
        if (prop == null) {
            prop = new Properties();
        } 
        prop.clear();
        try {
            File filePointer = new File(propsFileName);
            if (!filePointer.exists()) {
                filePointer.createNewFile();
            }
            prop.load(new FileInputStream(propsFileName));

        } catch (IOException ex) {
        }
        initialized = true;
        return prop;
    }

    public static Properties getProperties() throws IOException {
        if (prop == null) {
            loadParams();
        }
        return prop;
    }
    
    public static String getUserName() throws IOException {
        return (String) getProperties().getProperty(USERNAME_PROP);
    }
    
    public static String getPassword() throws IOException {
        return (String) getProperties().getProperty(PASSWORD_PROP);
    }
    
    public static String getDbName() throws IOException {
        return (String) getProperties().getProperty(DB_NAME_PROP);
    }
    
    public static String getIpAddr() throws IOException {
        return (String) getProperties().getProperty(IP_ADDR_PROP);
    }
    
    public static String getDbDriver() throws IOException {
        return (String) getProperties().getProperty(DB_DRIVER_PROP, DEFAULT_DB_DRIVER);
    }
    
    public static String getDbURL() throws IOException {
        String url = getProperties().getProperty(DB_URL_PROP);
        if (url != null) return url;
        return "jdbc:mysql://" + getIpAddr() + "/" + getDbName();
    }
    
    public static String getTable() throws IOException {
        return getProperties().getProperty(DB_TABLE_PROP, "concept_history");
    }
    
}
 