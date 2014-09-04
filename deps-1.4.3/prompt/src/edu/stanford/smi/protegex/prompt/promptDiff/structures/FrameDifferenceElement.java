 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu,
  *                 Michel Klein michel.klein@cs.vu.nl
 */

package edu.stanford.smi.protegex.prompt.promptDiff.structures;

import java.io.PrintStream;

import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Slot;

//Instances of this class represent single elements in the comparison of two classes;
//for example, there can be an instance stating that two superclasses of a class, c1 and c2, are non-isomorphic images of
//each other; there can be an instance saying that the type of f1 has no image at all.
public class FrameDifferenceElement implements Comparable<FrameDifferenceElement> {
  private Object _o1 = null;
  private Object _o2 = null;
  private Slot _slot = null;
  private Facet _facet = null;
  private String _mappingLevel = null;
  private String _relationshipToFrame = null;
  private int _operation;
  private int _changeLevel;
  private boolean _refersToFrame = false;

  public static final String [] COLUMN_NAMES = {"level", "operation", "slot", "facet", "old value", "new value"
  												 //, "mapping level", "operation explanation"
                                                  };
  public static int numberOfColumns () {return COLUMN_NAMES.length;}

//  private static ResultTable _results = PromptDiff.getResultsTable();

  public static final String TYPE = "type";
  public static final String META_CLASS = "meta-class";
  public static final String META_SLOT = "meta-slot";
  public static final String SUPERCLASS = "superclass";
  public static final String RESTRICTION = "restriction";
  public static final String PRIMITIVE_TO_DEFINED = "primitive to defined";
  public static final String DEFINED_TO_PRIMITIVE = "defined to primitive";
  public static final String SUBCLASS = "subclass";
  public static final String SUPERSLOT = "superslot";
  public static final String SUBSLOT = "subslot";
  public static final String INSTANCE = "instance";
  public static final String TEMPLATE_SLOT = "template slot";
  public static final String TEMPLATE_SLOT_VALUE = "template slot value";
  public static final String OWN_SLOT = "own slot";
  public static final String OWN_SLOT_VALUE = "own slot value";
  public static final String FACET_VALUE = "facet value";
  public static final String FACET = "facet";
  // Others
  public static final String RANGE = "range";
  public static final String SLOT_RESTRICTION_VALUE = "slot restriction filler";

  public static final int OP_ADDED = 10;
  public static final int OP_DELETED = 20;
  public static final int OP_CHANGED = 30;
  public static final int OP_CHANGED_SUPER = 31;
  public static final int OP_CHANGED_SUB = 32;
  public static final int OP_ALTERED = 40;

  // The definitions in the comments are not very precise
  public static final int LEVEL_DIRECT = 4;     // a change in the definition of a frame (ie. parent-relation, direct slots, overridden facets)
  public static final int LEVEL_CHANGED = 3;    // a change in another relation of the frame (instance, child)
  public static final int LEVEL_IMPLIED = 2;    // a change in the definition of a frame that is caused by the deletion of another frame
  public static final int LEVEL_ISOMORPHIC = 1; // a change in one of the frames that is referred by the frame
  public static final int LEVEL_UNCHANGED = 0;


  public FrameDifferenceElement (int changeLevel, int operation, String relationshipToFrame, Object o1, Object o2) {
	this(changeLevel, operation, relationshipToFrame, null, null, o1, o2);
  }

  public FrameDifferenceElement (int changeLevel, int operation, String relationshipToFrame, Slot slot, Object o1, Object o2) {
	this(changeLevel, operation, relationshipToFrame, slot, null, o1, o2);
  }

  public FrameDifferenceElement (int changeLevel, int operation, String relationshipToFrame, Slot slot, Facet facet, Object o1, Object o2) {
    _operation = operation;
    _slot = slot;
    _facet = facet;
    
    _changeLevel = changeLevel;
    _o1 = o1;
    _o2 = o2;
//    _mappingLevel = mappingLevel;
    _relationshipToFrame = relationshipToFrame;
   
	_refersToFrame = ((_o1!= null && _o1 instanceof Frame) || (_o2 != null && _o2 instanceof Frame)); 
  }
  
  public boolean refersToFrame () {return _refersToFrame;}
  public Object getO1Value () {return _o1;}
  public Object getO2Value () {return _o2;}
  public Slot getSlotValue () {return _slot;}
  public Facet getFacetValue () {return _facet;}

  //we will determine the mapping level the first time we access the individual diff;
/*  public String getMappingLevel () {
    if (! (_o1 instanceof Frame)) return null;
    if (_mappingLevel != null) return _mappingLevel;
    Collection rowsWithO1 = _results.getRows((Frame)_o1);
    Iterator i = rowsWithO1.iterator();
    while (i.hasNext()) {
      TableRow next = (TableRow)i.next();
      if (_o2 != null && _o2.equals (next.getF2Value())) {
        _mappingLevel = next.getMappingLevel();
        return _mappingLevel;
      }
    }
    //** should never get here
    return null;
  } */

  public String getRelationshipToFrame() {return _relationshipToFrame;}

  public void setRelationshipToFrame (String rel) {
  	_relationshipToFrame = rel;
  }

  public void setOperation (int op) {
  	_operation = op;
  }

  public void setFacetValue (Facet fc) {
  	_facet = fc;
  }

  public void setSlotValue (Slot sl) {
  	_slot = sl;
  }

  public int getOperation() {return _operation;}

  public String getOperationName() {
  	switch (_operation) {
  	  case OP_ADDED: return "added";
  	  case OP_DELETED: return "deleted";
  	  case OP_CHANGED: return "changed";
  	  case OP_CHANGED_SUPER: return "changed to superclass";
  	  case OP_CHANGED_SUB: return "changed to subclass";
  	  case OP_ALTERED: return "altered";
    }
    return "What's this: "+_operation+"??";
  }

  public String getChangeDescription() {
  	return getRelationshipToFrame()+" "+getOperationName();
  }

  public int getChangeLevel() {return _changeLevel;}

  public String getChangeLevelName() {
  	switch (_changeLevel) {
  	  case LEVEL_UNCHANGED: return "Unchanged";
  	  case LEVEL_ISOMORPHIC: return "Isomorphic";
  	  case LEVEL_CHANGED: return "Change";
  	  case LEVEL_IMPLIED: return "Change (implicit)";
  	  case LEVEL_DIRECT: return "Change (direct)";
    }
    return "What's this: "+_changeLevel+"??";
  }

  public int compareTo(FrameDifferenceElement ob) {
	Integer myself = new Integer(_changeLevel);
	Integer other = new Integer(ob.getChangeLevel());
	return myself.compareTo(other);
  }
  
  public void saveToFile (PrintStream out) {
	out.println("\t"  + getChangeLevelName() + "\t" + getOperationName() + "\t" + 
			   ((_slot == null) ? "\t" : _slot.getBrowserText())  + "\t" + 
			   ((_facet == null) ? "\t" : _facet.getBrowserText())  + "\t" + 
			   ((_o1 == null || !(_o1 instanceof Frame)) ? "\t" : ((Frame)_o1).getBrowserText())  + "\t" + 
			   ((_o2 == null || !(_o2 instanceof Frame)) ? "\t" : ((Frame)_o2).getBrowserText()) );
  }


 // public void setMappingLevel (String level) {_mappingLevel = level;}

  public String toString () {
    return "_o1 = " + ((_o1 instanceof Frame) ? ((Frame)_o1).getBrowserText() : _o1)+ ", _o2 = " + ((_o2 instanceof Frame) ? ((Frame)_o2).getBrowserText() : _o2)+ ", _changeLevel = " + _changeLevel + ", _relationshipToFrame = " + _relationshipToFrame + ", _changeLevel = " + _changeLevel;
  }

}

