package edu.stanford.smi.protegex.NCIEVSHistory;
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
 * The Original Code is PROMPT NCI-EVS
 *
 * The Initial Developer of the Original Code is Stanford University. Portions
 * created by Stanford University are Copyright (C) 2004.  All Rights Reserved.
 *
 * PROMPT was developed by Stanford Medical Informatics
 * (http//www.smi.stanford.edu) at the Stanford University School of Medicine
 * with support from the the Defense Advanced Research Projects Agency and National
 * Cancer Institute.  Current information about PROMPT can be obtained at
 * http//protege.stanford.edu

 * Created by IntelliJ IDEA.
 * User: prashr
 * Date: Nov 20, 2004
 * Time: 11:20:43 PM
 * To change this template use File | Settings | File Templates.
 */
import java.io.*;
import java.util.*;

import edu.stanford.smi.protege.util.*;

public class EVSHistorySettings {
    public static final String PROP_USER     = "userName";
    public static final String PROP_DATABASE = "dataBaseName";
    public static final String PROP_IPADDR   = "ipAddress";
    public static final String PROP_PASSWD   = "password";
    public static final String PROP_TABLE    = "dbEvsHistoryTable";

    private static Properties prop = null;

    static  {        // Loads a ResourceBundle and creates Properties from it
        if (prop == null) {
            try {
                prop = new Properties();
                prop.clear();
                String pathSuffixTestVariable 
                         = ApplicationProperties.getApplicationDirectory().getAbsolutePath();
                String path = pathSuffixTestVariable.endsWith("/") ? pathSuffixTestVariable : pathSuffixTestVariable + "/";
                String propsFileName = new StringBuffer().append(path).append("PromptNCIPlugin.properties").toString();

                File filePointer = new File(propsFileName);
                if (!filePointer.exists()) {
                    filePointer.createNewFile();
                }
                prop.load(new FileInputStream(propsFileName));

            } catch (IOException ex) {
                Log.getLogger().warning("Error loading evs history " + ex);
            }
        }
    }


    public static String getUserName() {
        return prop.getProperty(PROP_USER);
    }

    public static String getDatabaseName() {
        return prop.getProperty(PROP_DATABASE);
    }
    
    public static String getIpAddr() {
        return prop.getProperty(PROP_IPADDR);
    }

    public static String getPassword() {
        return prop.getProperty(PROP_PASSWD);
    }
    
    public static String getEvsHistoryTable() {
        return prop.getProperty(PROP_TABLE, "evs_history");
    }
}

