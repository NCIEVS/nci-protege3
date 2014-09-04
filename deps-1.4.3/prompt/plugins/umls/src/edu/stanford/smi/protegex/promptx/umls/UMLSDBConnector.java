/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */
package edu.stanford.smi.protegex.promptx.umls;

import java.sql.*;
import java.util.*;

import edu.stanford.smi.protege.util.*;


public class UMLSDBConnector implements UMLSSourceWrapper {
	private Connection _conn = null;
	private PreparedStatement _dbStatement = null;
	private PreparedStatement _dbStatementForNormalizedTerm = null;
	private boolean _initialized = false;
	
	private static final String TERM_TO_CUI_QUERY_STRING = "SELECT CUI FROM mrconso where str = ?";
	private static final String NORMALIZED_TERM_TO_CUI_QUERY_STRING = "SELECT CUI FROM mrxns_eng where nstr = ?";
//	private static final String TERM_TO_CUI_QUERY_STRING = "SELECT CUI FROM mrconso where convert (str using utf8) = ?";

	public UMLSDBConnector() {
		initialize ();
	}
	
	private void initialize () {
		String user = DBParameters.getUserName();
		String url = DBParameters.getJdbcUrl();
		String password = DBParameters.getPassword();
		String driver = DBParameters.getDriver();

		establishConnection (driver, url, user, password);
		if (_conn == null)
			return;

		prepareStatement ();

		if (preparedToWrite()) {
			System.out.println("Database connection established");
			_initialized = true;
		}
	}

	private void prepareStatement() {
		_dbStatement = null;
		try {
			_dbStatement = _conn.prepareStatement(new StringBuffer().append(TERM_TO_CUI_QUERY_STRING).toString());
			_dbStatementForNormalizedTerm =  _conn.prepareStatement(new StringBuffer().append(NORMALIZED_TERM_TO_CUI_QUERY_STRING).toString());
		} catch (SQLException e) {
			Log.getLogger().warning("Could not prepare statement: " + Log.toString(e));
			return;
		}
	}

	private static final Class [] _emptyArray = {};
	
	private void establishConnection(String driverClassName, String url, String user, String password) {
		DriverManager.setLoginTimeout(5);
		_conn = null;
		Driver driver = null;
		try {
			driver = (Driver)Class.forName(driverClassName).getConstructor(_emptyArray).newInstance((Object[])_emptyArray);
		} catch (Exception e) {
			Log.getLogger().severe("No suitable driver: " + e);
			return;
		}
		
		Properties props = new Properties ();
		props.put("user", user);
		props.put("password", password);
		try {
			_conn = driver.connect(url, props);
		} catch (SQLException e) {
			Log.getLogger().severe("Could not connect to the UMLS database: " + Log.toString(e));
			return;
		}
		}

	private boolean preparedToWrite() {
		return (_conn != null && _dbStatement != null);
	}

	public String getCUIforTerm(String term) {
		if (!connectionOpen ()) {
			Log.getLogger().info ("UMLS database connection closed");
			initialize();
		}
		
		if (!preparedToWrite ()) {
			Log.getLogger ().info ("Could not query the database for " + term);
			return null;
		}
		createStatement (term);
		try{
    			ResultSet result = _dbStatement.executeQuery(); 
    			if (!result.first()) return getCUIforNormalizedTerm (term);
    			return result.getString(1);
    			
		} catch (SQLException e) {
    			Log.getLogger().info ("Read from UMLS Database failed:" + Log.toString (e));
    			if (!connectionOpen()) {
    				Log.getLogger ().info ("Could not query the database for " + term);
     		}
    			return null;
		}
	}
	
	private String getCUIforNormalizedTerm(String term) {
		String normalizedTerm = normalizeTerm(term);
		createStatementForNormalizedQuery(normalizedTerm);
		try {
			ResultSet result = _dbStatementForNormalizedTerm.executeQuery();
			if (!result.first())
				return null;
			return result.getString(1);

		} catch (SQLException e) {
			Log.getLogger().info(
					"Read from UMLS Database failed:" + Log.toString(e));
			if (!connectionOpen()) {
				Log.getLogger()
						.info("Could not query the database for " + term);
			}
			return null;
		}
	}
	
	private String normalizeTerm(String term) {
		
		String tokens[] = term.split("\\W");
		tokens = removeGenitives (tokens);
		tokens = lowerCase (tokens);
		
		Arrays.sort(tokens);
		
		String result = "";
		for (int i = 0; i < tokens.length; i++) {
			result += tokens[i];
			result += " ";
		}
		result = result.trim();
		
		return result;
	}
	
	private String [] lowerCase (String [] tokens) {
		for (int i = 0; i < tokens.length; i++)
			tokens [i] = tokens[i].toLowerCase();
		return tokens;
	}
	
	
	private String [] removeGenitives (String [] tokens) {
		int i;
		for (i = 0; i < tokens.length && !(tokens[i].equals ("s")); i++);
		if (i == tokens.length) return tokens;
		
		String [] newTokens = new String [tokens.length - 1];
		for (int j = 0; j < i; j++)
			newTokens[j] = tokens[j];
		for (int j = i; j < tokens.length - 1; j++)
			newTokens[j] = tokens[j+1];
		return newTokens;		
	}

	private void createStatementForNormalizedQuery(String term) {
	       try {
	           _dbStatementForNormalizedTerm.setString(1, term);
	          } catch (SQLException e) {
	          	 Log.getLogger().warning("Could not create statement: " + term);
	          }		
	}

	private boolean connectionOpen() {
		try {
			return !(_conn.isClosed());
		} catch (SQLException e) {
			Log.toString(e);
			return false;
		}
	}


    private void createStatement (String term)  {
        try {
         _dbStatement.setString(1, term);
        } catch (SQLException e) {
        	 Log.getLogger().warning("Could not create statement: " + term);
        }
    }

        public boolean initialized() {
		return _initialized;
        }

}
