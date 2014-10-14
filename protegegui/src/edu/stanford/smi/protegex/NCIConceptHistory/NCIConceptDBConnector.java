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

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.util.Log;


public class NCIConceptDBConnector {
    private static final transient Logger log = Log.getLogger(NCIConceptDBConnector.class);
    
    
    private Connection conn = null;
    private PreparedStatement dbStatement;

    public NCIConceptDBConnector() throws IOException {
        String user      = NCIConceptHistorySettings.getUserName();
        String password  = NCIConceptHistorySettings.getPassword();
        String url       = NCIConceptHistorySettings.getDbURL();
        String dbName    = NCIConceptHistorySettings.getDbName();
        String table     = NCIConceptHistorySettings.getTable();
        try {
            Class.forName(NCIConceptHistorySettings.getDbDriver()).newInstance();
        }
        catch (Exception e) {
            log.log(Level.WARNING,
                    "Exception caught trying to load database driver.  Will try to continue...", e);
        }
        DriverManager.setLoginTimeout(5);
        try {   
            conn = DriverManager.getConnection(url, user, password);
            dbStatement = conn.prepareStatement(new StringBuffer().append("INSERT INTO ")
                                                      .append(dbName).append(".concept_history (concept, editaction, editdate, reference) VALUES(?,?,?,?)")
                                                      .toString());
            PreparedStatement randomStatement = conn.prepareStatement( new StringBuffer("SELECT COUNT(*) FROM ")
                                                                                        .append(dbName)
                                                                                        .append(".")
                                                                                        .append(table)
                                                                                        .toString());
            PreparedStatement createTableStatement = null;
            try {
                randomStatement.execute();
            }
            catch (SQLException sqle) {
                createTableStatement = conn.prepareStatement(new StringBuffer("CREATE TABLE concept_history (")
                                                             .append("historyid mediumint(9) NOT NULL auto_increment,")
                                                             .append("concept varchar(45) NOT NULL default '',")
                                                             .append("editaction varchar(10) NOT NULL default '',")
                                                             .append("editdate datetime NOT NULL default '0000-00-00',")
                                                             .append("reference varchar(45) default NULL,")
                                                             .append("PRIMARY KEY  (historyid)")
                                                             .append(") ENGINE=InnoDB DEFAULT CHARSET=utf8")
                                                             .toString());
                createTableStatement.execute();
                log.info("New NCI Concept History table created");
            }
            finally {
                randomStatement.close();
                if (createTableStatement != null) {
                    createTableStatement.close();
                }
            }
        } catch (SQLException e) {
            Log.getLogger().warning("Could not connect to the EVS history database");
            IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }
        Log.getLogger().info("Database connection established");
    }

    public void writeLogEntrytoDB(String concept, EditActions editaction, String reference) 
    throws IOException {
        if (conn == null) return;
        try {
            dbStatement.setString(1, concept);
            dbStatement.setString(2, editaction.toString());
            dbStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            if (reference == null) {
            	dbStatement.setNull(4, Types.VARCHAR);
            }
            else {
            	dbStatement.setString(4, reference);
            }
            dbStatement.executeUpdate();
        }
        catch (SQLException sqle) {
            log.log(Level.WARNING, "Could not write to Concept History database.  Aborting...", sqle);
            IOException ioe = new IOException(sqle.getMessage());
            ioe.initCause(sqle);
            throw ioe;
        }
    }
    
    public int getSize() throws SQLException {
    	int size = 0;
    	Statement countingStmt = null;
    	try {
    		countingStmt = conn.createStatement();
    		ResultSet r = countingStmt.executeQuery("SELECT MAX(historyid) FROM concept_history");
    		if (r.next()) {
    			size = r.getInt(1);
    		}
    	}
    	finally {
    		if (countingStmt != null) {
    			countingStmt.close();
    		}
    	}
    	return size;
    }
    
    public void rollback(int size) throws SQLException {
    	Statement deleteEditsStmt = null;
    	try {
    		deleteEditsStmt = conn.createStatement();
    		deleteEditsStmt.executeUpdate("DELETE FROM concept_history WHERE historyid > " + size);
    	}
    	finally {
    		if (deleteEditsStmt != null) {
    			deleteEditsStmt.close();
    		}
    	}
    }
    
    public void dispose() {
        try {
            if (conn != null)  {
                conn.close();
            }
        }
        catch (SQLException sqle) {
            Log.getLogger().warning("Could not close database connection -- " + sqle);
        }
    }


}
