/**
 *
 */
package edu.stanford.smi.protegex.changes;



public enum ChangeTableColumn {
	//slot name is hard coded
	CHANGE_COLNAME_ACTION("Action", "Type of Change", "action"),
    CHANGE_COLNAME_DESCRIPTION("Description", "Details of the action", "context"),
    CHANGE_COLNAME_AUTHOR("Author", "Person who made the change", "author"),
    CHANGE_COLNAME_CREATED("Created", "Date and time the change was made", "timestamp");

    private String name;
    private String search_slotName;
    private String heading;
    private ChangeTableColumn(String name,
                              String heading,
                              String search_slot) {
      this.name = name;
      this.heading = heading;
      this.search_slotName = search_slot;
    }

    public String getName() {
      return name;
    }

    public String getHeading() {
        return heading;
    }

    public String getSearchSlotName() {
        return search_slotName;
    }
    
    public static ChangeTableColumn getColumnFromName(String name) {
    	for (ChangeTableColumn col : ChangeTableColumn.values()) {
    		if (col.getName().equals(name)) {
    			return col;
    		}
    	}
    	throw new IllegalArgumentException("No such field");
    }
}