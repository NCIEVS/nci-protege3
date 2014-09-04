
/*
 * Contributor(s): Abhita Chugh abhita@stanford.edu
*/



package edu.stanford.smi.protegex.prompt.ui.diffUI;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.promptDiff.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class ChangeStatistics {
	private ResultTable _diffTable;
    private int _countAdditions, _countDeletions, _countSplits, _countMerges, _countDirectChanges,_totalChanges;
    private boolean isOwl;
   
    
	public ChangeStatistics(){
	    _countAdditions = 0;
	    _countDeletions = 0;
	    _countSplits = 0;
	    _countMerges = 0;
	    _countDirectChanges = 0;
	    _totalChanges = 0;
	    _diffTable = PromptTab.getPromptDiff().getResultsTable();
	    isOwl = PromptTab.kbInOWL();
	    getChangeStatistics();
		
	}


	
	private void getChangeStatistics()
	{
	   _countAdditions = getRowsWithOperation(TableRow.OPERATION_ADD).size();
	   _countDeletions = getRowsWithOperation(TableRow.OPERATION_DELETE).size();
	   _countSplits = (getRowsWithOperation(TableRow.OPERATION_SPLIT).size())/2;
	   _countMerges = (getRowsWithOperation(TableRow.OPERATION_MERGE).size())/2;
	   _countDirectChanges = getRowsWithMappingLevel(TableRow.MAPPING_LEVEL_DIRECTLY_CHANGED).size();
	   _totalChanges = _countAdditions + _countDeletions + _countSplits + _countMerges + _countDirectChanges;
	    	
	}
	
    public Collection getRowsWithOperation (String operation) {
    	Collection result = new ArrayList();
         	Iterator i = _diffTable.values().iterator();
            while (i.hasNext()) {
             	TableRow next = (TableRow)i.next();
             	if(isOwl){
                	if((next.getF1Value()!=null && next.getF1Value()instanceof Cls && OWLUtil.isOWLNamedClass ((Cls)next.getF1Value()))||(next.getF2Value()!=null && next.getF2Value()instanceof Cls&& OWLUtil.isOWLNamedClass ((Cls)next.getF2Value()))){
                     	
            			if (next.getOperationValue() == operation)
                        	result.add(next);
                	}
             	}
             		
                else{ 			
             	if((next.getF1Value()!=null && next.getF1Value()instanceof Cls)||(next.getF2Value()!=null && next.getF2Value()instanceof Cls)){
             	
    			if (next.getOperationValue() == operation)
                	result.add(next);
             	}
                }
            }
            return result;
        }

        
     
        
    public Collection getRowsWithMappingLevel (String level) {
    	Collection result = new ArrayList();
         	Iterator i = _diffTable.values().iterator();
            while (i.hasNext()) {
             	TableRow next = (TableRow)i.next();
            	if(isOwl){
                	if((next.getF1Value()!=null && next.getF1Value()instanceof Cls && OWLUtil.isOWLNamedClass ((Cls)next.getF1Value()))||(next.getF2Value()!=null && next.getF2Value()instanceof Cls&& OWLUtil.isOWLNamedClass ((Cls)next.getF2Value()))){
                     	
            			if (next.getMappingLevel() == level)
                        	result.add(next);
                	}
             	}
            	else{
             	if((next.getF1Value()!=null && next.getF1Value()instanceof Cls)||(next.getF2Value()!=null && next.getF2Value()instanceof Cls)){
    		if (next.getMappingLevel() == level)
                	  result.add(next);
             	}
            }
            } 	
            return result;
        }

	
	public int getNumberOfAdditions()
	{
		return _countAdditions;
	}
	
	public int getNumberOfDeletions()
	{
		return _countDeletions;
	}
	
	public int getNumberOfSplits()
	{
		return _countSplits;
	}
	
	public int getNumberOfMerges()
	{
		return _countMerges;
	}
	
	public int getNumberOfDirectChanges()
	{
		return _countDirectChanges;
	}
	
	public int getTotalChanges()
	{
		return _totalChanges;
	}
	

}
