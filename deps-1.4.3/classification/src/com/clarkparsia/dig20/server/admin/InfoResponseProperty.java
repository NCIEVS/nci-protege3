package com.clarkparsia.dig20.server.admin;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Oct 28, 2009
 *
 * @author Blazej Bulka <blazej@clarkparsia.com>
 */
public enum InfoResponseProperty {
	START_TIME("Start time"), 
	RUN_TIME("Run time"), 
	HOST("Host"), 
	PORT("Port"), 
	ONTOLOGY_COUNT("Number of ontologies"),
	ONTOLOGIES("Ontologies"), 
	AXIOMS("Axioms"), 
	LOGICAL_AXIOMS("Logical axioms"), 
	CLASS_AXIOMS("Class axioms"),
	REGULAR_CLASSIFICATION_TIME("Regular classification time (ms)"),
	INCREMENTAL_CLASSIFICATION_TIME("Incremental classification time (ms)"),
	TOTAL_CLASSIFICATION_TIME("Total classification time (ms)"),
	SERVER_KB_URI("URI of the Server's Knowledge Base"),
	LAST_RELOAD_TIME("Last reload time"),
	LAST_PERSIST_TIME("Last time server state persisted"),
	LAST_PERSIST_DURATION("Last persist duration (ms)"),
	CURRENT_STATE_PERSISTED("Current state of the server persisted");
	
	
	private String name;
	
	private InfoResponseProperty(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}
}
