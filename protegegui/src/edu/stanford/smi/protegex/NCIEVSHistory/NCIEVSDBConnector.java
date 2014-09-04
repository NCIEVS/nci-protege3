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
 */
package edu.stanford.smi.protegex.NCIEVSHistory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.storage.database.RobustConnection;
import edu.stanford.smi.protege.util.Log;



public class NCIEVSDBConnector {
    private static final transient Logger log = Log.getLogger(NCIEVSDBConnector.class);

    public String DB_DRIVER = "com.mysql.jdbc.Driver";

    private KnowledgeBase kb;
    
    private String user;
    private String dbName;
    private String ipAddress;
    private String password;
    private String table;

    

    private RobustConnection _conn = null;
    private String dbInsertCommand;
    private boolean dbIsUp = false;

    
    
    public Boolean recordRecord(String[] record) {
    	this.writeRecordEntrytoEVSDB(record);
    	return Boolean.TRUE;
    	
    	
    }
    
    private static NCIEVSDBConnector instance = null;

	public synchronized static NCIEVSDBConnector getInstance(KnowledgeBase kb) {
		if (instance != null) {

		} else {
			instance = new NCIEVSDBConnector(kb);
		}
		return instance;
	}
	
	private NCIEVSDBConnector(KnowledgeBase k) {
		this(k,EVSHistorySettings.getUserName(), 
                                EVSHistorySettings.getDatabaseName(), 
                                EVSHistorySettings.getIpAddr(), 
                                EVSHistorySettings.getPassword(), 
                                EVSHistorySettings.getEvsHistoryTable());
		
	}

    private NCIEVSDBConnector(KnowledgeBase kb,
                             String user,
                             String dbName,
                             String ipAddress,
                             String password,
                             String table) {
        this.kb = kb;
        
        this.user = user;
        this.dbName = dbName;
        this.ipAddress = ipAddress;
        this.password = password;
        this.table = table;

        initialize();
    }


    private void initialize() {
        String url = "jdbc:mysql://" + ipAddress + "/" + dbName;

        establishConnection(url);      
        prepareStatement (dbName);

        if (dbIsUp) {
            log.info("Database connection established");
        }
    }


    private void establishConnection(String url) {
        String randomQuery = new StringBuffer("SELECT COUNT(*) FROM ")
        .append(dbName)
        .append(".")
        .append(table)
        .toString();
        String createTable = new StringBuffer("CREATE TABLE ")
                                .append(table)
                                .append(" (historyid mediumint(22) NOT NULL auto_increment,")
                                .append("conceptcode varchar(32) NOT NULL default '',")
                                .append("conceptname varchar(255) NOT NULL default '',")
                                .append("action varchar(40) NOT NULL default '',")
                                .append("editdate timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,")
                                .append("editname varchar(40) NOT NULL default '',")
                                .append("host varchar(50) NOT NULL default '',")
                                .append("published mediumint(22) default '0',")
                                .append("referencecode varchar(32) default NULL,")
                                .append("PRIMARY KEY  (historyid)")
                                .append(") ENGINE=InnoDB DEFAULT CHARSET=utf8")
                                .toString();
        _conn = null;
        try {   
            if (log.isLoggable(Level.FINE)) {
                log.fine("opening db connection with url = " + url +", user = " + user + ", pwd = " + password);
            }
            _conn = new RobustConnection(DB_DRIVER, url, user, password, null, null);
            try {
                _conn.getPreparedStatement(randomQuery).execute();
            }
            catch (SQLException  e) {
                _conn.getPreparedStatement(createTable).execute();
                log.info("New EVS History Table created");
            }
            dbIsUp = true;
        } catch (SQLException e) {
            Log.getLogger().severe("Could not connect to the EVS history database: " + Log.toString (e));
            return;     
        }

    }

    public String getUserName () {
        return kb.getUserName();
    }


    public static String getIPAddress () {
        RemoteSession session = ServerFrameStore.getCurrentSession();
        if (session != null) {
            return session.getUserIpAddress();
        }
        else {
            return "127.0.0.1";
        }
    }


    private void prepareStatement (String dbName) {
        dbInsertCommand = new StringBuffer("INSERT INTO ")
                                    .append(dbName)
                                    .append(".")
                                    .append(EVSHistorySettings.getEvsHistoryTable())
                                    .append(" (conceptcode, conceptname, action, editname, host, referencecode) VALUES(?,?,?,?,?,?)")
                                    .toString();
    }


    private PreparedStatement createStatement (String conceptCode, 
                                               String conceptName, 
                                               String action, 
                                               String editName, 
                                               String host, 
                                               String reference)  {
        PreparedStatement insertCmd = null;
        try {
            insertCmd = _conn.getPreparedStatement(dbInsertCommand);
            insertCmd.setString(1, conceptCode);
            insertCmd.setString(2, conceptName);
            insertCmd.setString(3, action);
            insertCmd.setString(4, editName);
            insertCmd.setString(5, host);
            insertCmd.setString(6, reference);
        } catch (SQLException e) {
            Log.getLogger().warning("Could not create statement: " + conceptCode + ", " + conceptName + ", " + action + ", " + editName);
        }
        return insertCmd;
    }

    

    

    public void writeRecordEntrytoEVSDB(String[] record) {
    	String code = record[0];
    	String name = record[1];
    	String action = record[2];
    	String reference = record[3];
    	
        if (log.isLoggable(Level.FINE)) {
            log.fine("Writing record: ");
            log.fine("\tFrame = " + name);
            log.fine("\taction = " + action);
            log.fine("\treference = " + reference);
        }
        
        

        
        writeRecordEntrytoEVSDB(code, 
                                name, 
                                action, 
                                getUserName(), 
                                getIPAddress(), 
                                reference);
    }

    private void writeRecordEntrytoEVSDB(String conceptCode, 
                                         String conceptName, 
                                         String action, 
                                         String userName, 
                                         String host, 
                                         String reference)  {
        if (!dbIsUp) {
            return;
        }

        if (log.isLoggable(Level.FINE)) {
            log.fine("Logging EVS record");
            log.fine("\tConcept Code = " + conceptCode);
            log.fine("\tConcept Name = " + conceptName);
            log.fine("\tAction = " + action);
            log.fine("\tEdit Name = " + userName);
            log.fine("\tHost  = " + host);
            log.fine("\tReference = " + reference);
        }

        PreparedStatement insertCmd = createStatement (conceptCode, conceptName, action, userName, host, reference);
        try{
            if (insertCmd != null) {
                insertCmd.executeUpdate();
            }
        } catch (SQLException e) {
            Log.getLogger().info ("Write to history database failed:" + Log.toString (e));
        }
    }

}
