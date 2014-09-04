
 /*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu,
 *                 Michel Klein michel.klein@cs.vu.nl
 * 		   Abhita Chugh abhita@stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff.structures;

import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.table.TableCellRenderer;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.DefaultRenderer;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.prompt.ProjectsAndKnowledgeBases;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.ui.FrameInMergingRenderer;
import edu.stanford.smi.protegex.prompt.ui.diffUI.DiffClsesPanel;
import edu.stanford.smi.protegex.prompt.ui.diffUI.ResultsPane.MappingLevelRenderer;
import edu.stanford.smi.protegex.prompt.util.Util;

public class TableRow {
    private Logger logger = Log.getLogger(TableRow.class);
    private Frame _f1 = null;
    private Frame _f2 = null;
    private String _rename = null;
    private String _operation = null;
    private String _mappingLevel = null;
    private String _renameExplanation = null;
    private boolean _explanationSorted = false;
    private Collection<FrameDifferenceElement> _operationExplanation 
                 = new ArrayList<FrameDifferenceElement> (); 
    public enum Column {
        F1("f1") {
            public TableCellRenderer createRenderer() { return new FrameInMergingRenderer (); }
        }, 
        F2("f2") { 
            public TableCellRenderer createRenderer() { return new FrameInMergingRenderer (); }
        }, 
        RENAMED("renamed") {
            public TableCellRenderer createRenderer() { return new DefaultRenderer (); }
        },
        OPERATION("operation") {
            public TableCellRenderer createRenderer() { return new DefaultRenderer (); }
        },
        MAP_LEVEL("map level") {
            public TableCellRenderer createRenderer() { return new MappingLevelRenderer (); }
        },
        RENAME_EXPLANATION("rename explanation") {
            public TableCellRenderer createRenderer() { return new DefaultRenderer (); }
        };
        
        private String name;
        private Column(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        public abstract TableCellRenderer createRenderer();
        
    }
    /**
     * The original declaration was 
     *   COLUMN_NAMES =     {"f1", "f2", "renamed", "operation", "map level",
     *                        "rename explanation",
     *                                             // "operation explanation"
     *                                             };
     * 
     * @deprecated use the TableRow.Column Enum
     */
    @Deprecated
    public static final String [] COLUMN_NAMES = new String[Column.values().length];
    static {
        for (Column col : Column.values()) {
            COLUMN_NAMES[col.ordinal()] = col.getName();
        }
    }
    
    


    private boolean _changeAccepted = false;
    private boolean _checked = false;
    
	// rename column values
    public static final String RENAME_MINUS = "No";
    public static final String RENAME_PLUS = "Yes";

    // operation column values
    public static final String OPERATION_SPLIT = "Split";
    public static final String OPERATION_MERGE = "Merge";
    public static final String OPERATION_ADD = "Add";
    public static final String OPERATION_DELETE = "Delete";
    public static final String OPERATION_MAP = "Map";
    public static final String OPERATION_RETIRE = "Retire";

    public static final String MAPPING_LEVEL_UNCHANGED = "Unchanged";
    public static final String MAPPING_LEVEL_DIRECTLY_CHANGED = "Directly-changed"; //only if there is no image for directly attached slots or superclass
    public static final String MAPPING_LEVEL_CHANGED = "Changed"; // if any of the referenced frames has no image
    public static final String MAPPING_LEVEL_STRONG_ISOMORPHIC = "Strong-isomorphic"; // only a rename, rest identical
    public static final String MAPPING_LEVEL_WEAK_ISOMORPHIC = "Weak-isomorphic"; // some of the references frames are not images
    public static final String MAPPING_LEVEL_ISOMORPHIC = "Isomorphic"; // if all referenced frames have images, but any of those are changed
    public static final String MAPPING_LEVEL_NOT_SET = "not set";

    public TableRow (Frame f1, Frame f2) {
     	initialize (f1, f2, null, null);
    }

    public TableRow (Frame f1, Frame f2, String rename) {
    	initialize (f1, f2, rename, null);
    }


    public TableRow (Frame f1, Frame f2, String rename, String operation) {
    	initialize (f1, f2, rename, operation);
    }

    private void initialize (Frame f1, Frame f2, String rename, String operation) {
     	_f1 = f1;
        _f2 = f2;
        _rename = (rename == null) ? RENAME_MINUS : rename;
        _operation = operation;
        if (f1 != null && Util.isSystem(f1))
        	_mappingLevel = MAPPING_LEVEL_UNCHANGED;
        else
        	_mappingLevel = MAPPING_LEVEL_NOT_SET;
    }
 
     public Object [] createArrayFromEntries () {
      	Object [] result;
        if (_f1 == null || _f2 == null)
        	result = new Object [1];
        else
        	result = new Object [2];
        if (_f1 != null) {
        	result [0] = _f1;
         	if (_f2 != null)
            	result [1] = _f2;
            return result;
        }
        result[0] = _f2;
        return result;
     }


    void setOperation (String operation) {
    	if (_f1 != null && _f2 != null && Util.isSystem(_f1) && Util.isSystem(_f2))
    		_operation = OPERATION_MAP;
        else
     		_operation = operation;
    }

    public void setMappingLevel (String level) {
     		_mappingLevel = level;
    }
    
    void setRenameValue (String rename) {
    	if (Util.isSystem(_f1) && Util.isSystem(_f2))
        	_rename = RENAME_MINUS;
        else
     		_rename = rename;
    }

    void setF1Value (Frame f) {
     	_f1 = f;
    }

    void setF2Value (Frame f) {
     	_f2 = f;
    }

    public void setRenameExplanation (String exp) {
    	if (!Util.isSystem(_f1) || ! Util.isSystem(_f2))
     		_renameExplanation = exp;
    }

    public void removeOperationExplanation (FrameDifferenceElement exp, Frame parent) {
    	_operationExplanation.remove (exp);
    	setAllMappingLevels(true, parent);
    }
    
    public void removeOperationExplanation (FrameDifferenceElement exp) {
    	_operationExplanation.remove (exp);
    	setAllMappingLevels(true, null);
    }
    
    public void removeInstanceOperationExplanation (FrameDifferenceElement exp) {
		removeOperationExplanation (exp, null);
}

    public void removeSlotOperationExplanation (FrameDifferenceElement exp) {
		removeOperationExplanation (exp, null);
}
    
    public void removeAddOrMoveOrDelete () {
    		setAllMappingLevels(true, null);
    }

    public void appendOperationExplanation (Collection<FrameDifferenceElement> c) {
      if (c == null || c.isEmpty()) return;

      Iterator<FrameDifferenceElement> i = c.iterator();
      while (i.hasNext()) {
        appendOperationExplanation (i.next());
      }
    }

    public void appendOperationExplanation (FrameDifferenceElement exp) {
        if (_f1 != null && _f2 != null && Util.isSystem(_f1) && Util.isSystem(_f2)) return;
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("appending explanation, " + exp + " to rows " + this);
        }
        if (exp != null) {
     	  _operationExplanation.add(exp);
     	  _explanationSorted = false;
    	}
    }

    public void clearOperationExplanation () {
      	_operationExplanation.clear();
    }

	public void clearOperationExplanationsReferringToFrames() {
		Iterator<FrameDifferenceElement> i = _operationExplanation.iterator();
		while (i.hasNext()) {
			FrameDifferenceElement next =i.next();
			if (next.refersToFrame()) 
				i.remove();
		}
	}

    public void saveToFile (PrintStream out, boolean printFrameDifferences) {
    	out.println("" + ((_f1 == null) ? "\t" : _f1.getBrowserText()) + "\t" + ((_f2 == null) ? "\t" : _f2.getBrowserText()) + "\t" + _rename + "\t" + _operation + "\t" + _mappingLevel);
  //    out.println(_f1 + "\t" + _f2);
  	  if (printFrameDifferences) {
  	  	saveFrameDifferencesToFile (out);
  	  }
    }
    
    private void saveFrameDifferencesToFile (PrintStream out) {
    	Iterator<FrameDifferenceElement> i = _operationExplanation.iterator();
    	while (i.hasNext()) {
    		FrameDifferenceElement next = i.next();
    		next.saveToFile (out);
    	}
    }

    public void saveToRDF (PrintStream out, URL oldNS, URL newNS) {
      if (_f1 == null) {
      	out.println("<ver:Added rdf:resource=\""+newNS+"#"+_f2.getName()+"\"/>");
      } else if (_f2 == null) {
      	out.println("<ver:Deleted rdf:resource=\""+oldNS+"#"+_f1.getName()+"\"/>");
      }
      else {
      	String ch = "Change>";
      	if (_operationExplanation.size() == 1) ch = "AtomicChange>";
      	if (_operationExplanation.size() > 1) ch = "ComplexChange>";
      	out.println("<ver:"+ch);
      	out.println("  <ver:from rdf:resource=\""+oldNS+"#"+_f1.getName()+"\"/>");
      	out.println("  <ver:to rdf:resource=\""+newNS+"#"+_f2.getName()+"\"/>");

        Iterator<FrameDifferenceElement> i = _operationExplanation.iterator();
        while (i.hasNext()) {
          FrameDifferenceElement myFramDif = i.next();
          out.println("  <ver:Type rdf:resource=\"#"+myFramDif.getOperationName()+"\">");
          out.println("    <ver:old rdf:value=\""+myFramDif.getO1Value()+"\">");
          if (myFramDif.getO2Value() != null)
            out.println("    <ver:new rdf:value=\""+myFramDif.getO2Value()+"\">");
          out.println("  </ver:Type>");
        }
      	out.println("</ver:"+ch);
      }
      out.println("");
    }

    public Frame getF1Value () { return _f1;}
    public Frame getF2Value () { return _f2;}
    public String getRenameValue () { return _rename;}
    public String getRenameExplanation () { return _renameExplanation;}
    

    public String getOperationValue () {
    	if (_operation == null) {
            if (_f1 == null)
            	_operation = OPERATION_ADD;
            else if (_f2 == null)
            	_operation = OPERATION_DELETE;
            else
            	_operation = OPERATION_MAP;
    	}
    	return _operation;
    }

    public Collection<FrameDifferenceElement> getOperationExplanation () {
    	if (!_explanationSorted) {
    	    FrameDifferenceElement [] v = _operationExplanation.toArray(new FrameDifferenceElement[0]);
	    	Arrays.sort (v);
	        Collection<FrameDifferenceElement> sorted = new ArrayList<FrameDifferenceElement>();
	        for (int i = v.length-1; i >= 0; i--)
	        	sorted.add (v[i]);
	        _operationExplanation = sorted;
	        _explanationSorted = true;
	        
	    }
	    return _operationExplanation;
    }
    
    public FrameDifferenceElement getOperationExplanation(Frame frame)
    {
    	Collection<FrameDifferenceElement> explanation = getOperationExplanation();
    	
    	for(Iterator<FrameDifferenceElement> i = explanation.iterator();i.hasNext();)
    	{
    		FrameDifferenceElement diff = i.next();
    		if(diff.getO1Value() != null && diff.getO1Value().equals(frame) || 
			diff.getO2Value() != null && diff.getO2Value().equals(frame))
    			return diff;
    	}
    	return null;
    }    
    
    
    public FrameDifferenceElement getDifferenceElement(Object o1, Object o2, String slot, String facet, boolean refersToFrame)
    {
    	Collection<FrameDifferenceElement> explanation = getOperationExplanation();
    	boolean slotMatches, facetMatches, o1Matches, o2Matches, refersToFrameMatches;
    	for(Iterator<FrameDifferenceElement> i = explanation.iterator();i.hasNext();)
    	{
    		slotMatches = false;
    		facetMatches = false;
    		o1Matches = false;
    		o2Matches = false;
    		refersToFrameMatches = false;
    		FrameDifferenceElement diff = (FrameDifferenceElement)i.next();
    		if(diff.refersToFrame()!= refersToFrame)
    			continue;
    		else
    			refersToFrameMatches = true;
    		if((diff.getSlotValue()== null && slot!= null) || (diff.getSlotValue()!= null && slot== null))
    			continue; // 1 slot is null & 1 isn't
    		if( diff.getSlotValue()!= null && slot!=null){
    		  if( diff.getSlotValue().getName().equals(slot))
    			  slotMatches = true;
    		  else 
    			  continue;
    		}
    		else
    			slotMatches = true;  // both slots are null
    		
    		if((diff.getFacetValue()== null && facet!= null) || (diff.getFacetValue()!= null && facet== null))
    			continue; // 1 facet is null & 1 isn't
    		if( diff.getFacetValue()!= null && facet!=null){
    		  if( diff.getFacetValue().getName().equals(facet))
    			  facetMatches = true;
    		  else 
    			  continue;
    		}
    		else
    			facetMatches = true;  // both facets are null
    		
    		if((diff.getO1Value()== null && o1!= null) || (diff.getO1Value()!= null && o1== null))
    			continue; // 1 o1 is null & 1 isn't
    		if( diff.getO1Value()!= null && o1!=null){
    		  if(refersToFrame){	
    		   if( ((Frame)diff.getO1Value()).getName().equals(o1))  //diff.refersToFrame() will also be true!
    			  o1Matches = true;
    		   else 
    			  continue;
    		  }
    		  else{
    			if(diff.getO1Value().equals(o1))
    				o1Matches = true;
    			else 
    				continue;
    			  
    		  }
    		}
    		else
    			o1Matches = true;  // both o1's are null
    		
    		if((diff.getO2Value()== null && o2!= null) || (diff.getO2Value()!= null && o2== null))
    			continue; // 1 o2 is null & 1 isn't
    		if( diff.getO2Value()!= null && o2!=null){
    		  if(refersToFrame){	
    		   if( ((Frame)diff.getO2Value()).getName().equals(o2))  //diff.refersToFrame() will also be true!
    			  o2Matches = true;
    		   else 
    			  continue;
    		  }
    		  else{
    			if(diff.getO2Value().equals(o2))
    				o2Matches = true;
    			else 
    				continue;
    			  
    		  }
    		}
    		else
    			o2Matches = true;  // both o2's are null
    		
    		if( refersToFrameMatches && o1Matches && o2Matches && slotMatches && facetMatches )
    			return diff;
    	}
    	return null;
    } 

	public boolean slotChangedAtFrame(Slot slot) {
		Collection<FrameDifferenceElement> explanation = getOperationExplanation();
    	
		for(Iterator<FrameDifferenceElement> i = explanation.iterator();i.hasNext();)
		{
			FrameDifferenceElement diff = i.next();
			if (diff.getSlotValue() != null && diff.getSlotValue().equals(slot)) return true;
		}
		return false;
	}

    public String getMappingLevel () {
    	return _mappingLevel;	
    }	

	public boolean isChanged() {
		return (getMappingLevel() == MAPPING_LEVEL_CHANGED ||
        		getMappingLevel() == MAPPING_LEVEL_DIRECTLY_CHANGED);
	}
	
	public  void setDirectlyChanged () {
		if (!(_mappingLevel.equals (MAPPING_LEVEL_NOT_SET))) return;
		if (_operationExplanation == null || _operationExplanation.isEmpty()) return;
		Iterator<FrameDifferenceElement> i = _operationExplanation.iterator();
		while (i.hasNext()) {
			FrameDifferenceElement diff = i.next();
			if (diff.getChangeLevel() == FrameDifferenceElement.LEVEL_DIRECT) {
				_mappingLevel = MAPPING_LEVEL_DIRECTLY_CHANGED;		
				return;	
			}
		}
	}
	
	public void setAllMappingLevels () {
		setAllMappingLevels (false, null);
	}
	
	private void setAllMappingLevels (boolean removing, Frame parent) {
		if (!removing && !(_mappingLevel.equals (MAPPING_LEVEL_NOT_SET))) return;
		if (_operationExplanation == null || _operationExplanation.isEmpty()) {
			if (_operation == OPERATION_MAP && _mappingLevel == MAPPING_LEVEL_NOT_SET) { // we are setting up the table for the first time
				_mappingLevel = MAPPING_LEVEL_UNCHANGED;
				return;
			}
			if (_mappingLevel != MAPPING_LEVEL_UNCHANGED) {
				_mappingLevel = MAPPING_LEVEL_UNCHANGED;
				//if (_f2 instanceof Cls && parent != null) {
				if (_f2 instanceof Cls) {
					FrameStatus fs = PromptTab.getPromptDiff().getViewSetUp().getFrameStatuses();
					if (parent != null)
						fs.clearStatus ((Cls)_f2, parent);
					else {
						Iterator i = Util.getDirectSuperclasses((Cls)_f2).iterator();
						while (i.hasNext()) {
							fs.clearStatus((Cls)_f2, (Cls)i.next());
						}
					}
					reduceClassesCount();
				}
			}
			return;
		}
		Iterator<FrameDifferenceElement> i = _operationExplanation.iterator();
		while (i.hasNext()) {
			FrameDifferenceElement diff = i.next();
			if (diff.getChangeLevel() == FrameDifferenceElement.LEVEL_DIRECT) {
				_mappingLevel = MAPPING_LEVEL_DIRECTLY_CHANGED;		
				return;	
			} else if (diff.getChangeLevel() == FrameDifferenceElement.LEVEL_ISOMORPHIC ||
                    diff.getChangeLevel() == FrameDifferenceElement.LEVEL_IMPLIED){
				_mappingLevel = MAPPING_LEVEL_ISOMORPHIC;		
				return;	
			}
			Log.getLogger().severe ("Should not be here: TableRow.setMappingLevels");
		}
		
	}
	
	private void reduceClassesCount () {
		  DiffClsesPanel clsesPanel = DiffClsesPanel.getInstance(ProjectsAndKnowledgeBases.getProject(PromptTab.getPromptDiff().getKb2()),PromptTab.getPromptDiff().getViewSetUp());
		  clsesPanel._totalChanges = clsesPanel._totalChanges - 1;
		  //Log.getLogger().info("After decrement:" +clsesPanel._totalChanges);
		  setChecked(true);
		  clsesPanel._numberOfChanges.setText("    " + Integer.toString(clsesPanel._totalChanges));

	}
	
	public void upgradeMappingLevel () {
		if (_mappingLevel.equals (MAPPING_LEVEL_NOT_SET)) {
			_mappingLevel = MAPPING_LEVEL_UNCHANGED;
			if (_operationExplanation == null  || _operationExplanation.isEmpty()) {
				if (_rename == TableRow.RENAME_PLUS)
					_mappingLevel = TableRow.MAPPING_LEVEL_STRONG_ISOMORPHIC;
			} else {
				Iterator i = _operationExplanation.iterator();
				int max_change = FrameDifferenceElement.LEVEL_UNCHANGED;
				while (i.hasNext()) {
					FrameDifferenceElement diff = (FrameDifferenceElement)i.next();
					if ( diff.getChangeLevel() > max_change) max_change = diff.getChangeLevel();
				}
				if (max_change >= FrameDifferenceElement.LEVEL_DIRECT)
					_mappingLevel = MAPPING_LEVEL_DIRECTLY_CHANGED;
				else if (max_change >= FrameDifferenceElement.LEVEL_CHANGED)
					_mappingLevel = MAPPING_LEVEL_CHANGED;
				else if (max_change >= FrameDifferenceElement.LEVEL_ISOMORPHIC)
					_mappingLevel = MAPPING_LEVEL_WEAK_ISOMORPHIC;
			}
			// Ignore difference between weak and strong isomorphism
			//if (_mappingLevel == MAPPING_LEVEL_STRONG_ISOMORPHIC || _mappingLevel == MAPPING_LEVEL_WEAK_ISOMORPHIC)
			//    _mappingLevel = MAPPING_LEVEL_ISOMORPHIC;
		}
	}


//	public boolean isMappingDetected() {
//		return _mappingDetected;
//	}
//
//	public void setMappingDetected() {
//		_mappingDetected = true;
//	}
//
//
    public static int numberOfColumns () {return COLUMN_NAMES.length;}

/*
	public String toString () {
   		return "f1: " + _f1 + ", f2: " + _f2 + ", r: " + _rename + ", operation: " + _operation +
        ", explanation: " + _explanation;
  	}
*/
	public String toString () {
   		return "f1: " + _f1 + ", f2: " + _f2 + ", r: " + _rename + ", operation: " + _operation;
  	}

	public boolean isChangeAccepted() {
		return _changeAccepted;
	}

	public void setChangeAccepted(boolean b) {
		_changeAccepted = b;
	}
	
	public boolean isChecked() {
		return _checked;
	}

	public void setChecked(boolean b) {
		_checked = b;
	}
	
//	public void addToChangeHistory (ChangeHistoryElement e) {
//		_changeHistory.add (e);
//	}
//	
//	public void setChangeHistory (Collection c) {
//		_changeHistory.clear();
//		_changeHistory.addAll (c);
//	}
//	
//	public Collection getChangeHistory () {
//		return _changeHistory;
//	}

}
