 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu,
  *                 Michel Klein michel.klein@cs.vu.nl
 * 		   Abhita Chugh abhita@stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff.structures;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.stanford.smi.protege.event.KnowledgeBaseAdapter;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.event.KnowledgeBaseListener;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.ArrayListMultiMap;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.MultiMap;
import edu.stanford.smi.protegex.prompt.plugin.PluginManager;
import edu.stanford.smi.protegex.prompt.util.Util;

public class ResultTable {
    private Collection<TableRow> _allRows = Collections.synchronizedSet(new HashSet<TableRow> ()); // a collection of table rows
    private MultiMap<String, TableRow> _sourceToRowsMap = new ArrayListMultiMap<String, TableRow> (101); // <f1, TableRow>
    private MultiMap<String, TableRow> _imageToRowsMap = new ArrayListMultiMap<String, TableRow> (101); // <f2, TableRow>


    private Collection<Frame> _unmatchedEntriesFromO1 = null;
    private Collection<Frame> _unmatchedEntriesFromO2 = null;
    private boolean _tableChanged = true;
    private boolean _traceOn = false;
    
    private KnowledgeBase _kb1;
    private KnowledgeBase _kb2;
    
    // default index value for a tab delimited file save
    private static int TAB_DELIMITED = 0;
    
//  private HashMap _mapForKb1;
    private HashMap<String, Frame> _systemFramesFromKb2;
    private Collection<Frame> _systemFramesFromKb1;
    
    private KnowledgeBaseListener replaceFrameListener = new KnowledgeBaseAdapter() {

        @Override
        public void frameNameChanged(KnowledgeBaseEvent event) {
            Frame oldFrame = event.getFrame();
            String oldName = event.getOldName();
            Frame newFrame = event.getNewFrame();
            Collection<TableRow> rows = _imageToRowsMap.getValues(oldName);
            if (rows != null && !rows.isEmpty()) {
                _imageToRowsMap.removeKey(oldName);
                _imageToRowsMap.addValues(newFrame.getName(), rows);
                for (TableRow row : rows) {
                    row.setF2Value(newFrame);
                }
            }
            if (_unmatchedEntriesFromO2.contains(oldFrame)) {
                _unmatchedEntriesFromO2.remove(oldFrame);
                _unmatchedEntriesFromO2.add(newFrame);
            }
        }
        
    };
    


	public ResultTable (KnowledgeBase kb1, KnowledgeBase kb2){
		_kb1 = kb1;
		_kb2 = kb2;
		_kb2.addKnowledgeBaseListener(replaceFrameListener);
	}

	public void initializeTable () {
	    Collection<Frame> frames1 = Util.getFrames (_kb1);
	    Collection<Frame> frames2 = Util.getFrames (_kb2);

	    initializeTableRows (frames1, 0);
	    initializeTableRows (frames2, 1);
	    addRowsWithSystemClasses ();
	}


    private void initializeTableRows (Collection<Frame> frames, int index) {
      if (index == 0) {
		_systemFramesFromKb1 = new ArrayList<Frame> ();
      } else {
		_systemFramesFromKb2 = new HashMap<String, Frame>();
      }
      Iterator<Frame> i = frames.iterator();
      while (i.hasNext()) {
        Frame next = i.next();
        if (!Util.isSystem(next))
         addElement (new TableRow (index==0 ? next : null,
                                  index==1 ? next : null));
        else {
			if (index == 0)
				_systemFramesFromKb1.add (next);
			else 
				_systemFramesFromKb2.put(next.getName(), next); // will need this for system frames later
        }
      }
    }

    private void addRowsWithSystemClasses () {
      Iterator i = _systemFramesFromKb1.iterator();
      while (i.hasNext()) {
        Frame next = (Frame)i.next();
        
        if (!Util.isSystem(next)) continue;
        addElement (new TableRow (next, (Frame)_systemFramesFromKb2.get(next.getName())));
      }
    }

    public void addElements (Collection<TableRow> c) {
     	if (c == null) return;
        Iterator<TableRow> i = c.iterator();
        while (i.hasNext()) {
         	TableRow next = i.next();
            addElement (next);
        }
        _tableChanged = true;
    }

    public void addElement (TableRow row) {
	if (row.getF1Value() != null && Util.isSystem((Frame)row.getF1Value()) &&
            row.getF2Value() != null && Util.isSystem((Frame)row.getF2Value())) {
        	row.setOperation(TableRow.OPERATION_MAP);
        	
//                row.setMappingLevel (TableRow.MAPPING_LEVEL_UNCHANGED);
        }

        _allRows.add(row);
	if (row.getF1Value() != null) {
            	_sourceToRowsMap.addValue(createKey(row.getF1Value()), row);
        }
	if (row.getF2Value() != null)
            	_imageToRowsMap.addValue(createKey(row.getF2Value()), row);
        _tableChanged = true;
    }

    public List<TableRow> sort () {
     	TableRow [] v = _allRows.toArray(new TableRow[0]);
    	Arrays.sort  (v, new TableRowComparator ());
        List<TableRow> sorted = new ArrayList<TableRow>();
        for (int i = 0; i < v.length; i++)
        	sorted.add(v[i]);
        return sorted;
    }

    public Collection<Frame> getImages (Frame f) {
        Collection<Frame> result = new ArrayList<Frame> ();
        if (Util.isSystem(f)) {
            handleSystemSourcesAndImages (f, result);
            return result;
        }

        Collection<TableRow> rows = _sourceToRowsMap.getValues (createKey(f)); //Collection of TableRows
        //Change Ontology
        //if (rows == null) return null;
        if (rows == null || rows.isEmpty()){
            //result.add(_kb2.getFrame(f.getName()));
            //return result;
            TableRow rowWithNoFrame = new TableRow(f,_kb2.getFrame(f.getName()),TableRow.RENAME_MINUS,TableRow.OPERATION_MAP);
            rowWithNoFrame.setMappingLevel(TableRow.MAPPING_LEVEL_UNCHANGED);
            addElement(rowWithNoFrame);
            rows = _sourceToRowsMap.getValues (createKey(f));
        }

        Iterator<TableRow> i = rows.iterator();
        while (i.hasNext()) {
            TableRow next = i.next();
            if (next.getF2Value() != null)
                result.add(next.getF2Value());
        }
        return result;
    }

    private void handleSystemSourcesAndImages (Frame f, Collection result) {
      KnowledgeBase kbx = f.getKnowledgeBase();
      if (kbx == _kb1)
        result.add(_kb2.getFrame(f.getName()));
      else
        result.add(_kb1.getFrame(f.getName()));
    }

    public Frame getFirstImage (Frame f) {
     	Collection images = getImages (f);
        if (images == null || images.isEmpty()) return null;
        return (Frame)CollectionUtilities.getFirstItem(images);
    }

    public Frame getSoleImage (Frame f) {
     	Collection images = getImages (f);
        if (images == null || images.isEmpty()) return null;
        return (Frame)CollectionUtilities.getSoleItem(images);
    }

    public Collection<Frame> getSources (Frame f) {
        Collection<Frame> result = new ArrayList<Frame> ();
        if (Util.isSystem(f)) {
          handleSystemSourcesAndImages (f, result);
          return result;
        }


      Collection<TableRow> rows = _imageToRowsMap.getValues (createKey(f)); //Collection of TableRows
        //Change Ontology
	    //if (rows == null) return null;
	    if (rows == null || rows.isEmpty()){
	    	//result.add(_kb1.getFrame(f.getName()));
	    	//return result;
	    	TableRow rowWithNoFrame = new TableRow(_kb1.getFrame(f.getName()),f,TableRow.RENAME_MINUS,TableRow.OPERATION_MAP);
        	rowWithNoFrame.setMappingLevel(TableRow.MAPPING_LEVEL_UNCHANGED);
        	addElement(rowWithNoFrame);
        	rows = _imageToRowsMap.getValues (createKey(f));
	    }
        Iterator<TableRow> i = rows.iterator();
        while (i.hasNext()) {
        	TableRow next = i.next();
            if (next.getF1Value() != null)
            	result.add(next.getF1Value());
        }
        return result;
    }
    
    private String createKey (Frame f) {
    	if (f == null) return null;
    	KnowledgeBase kb = f.getKnowledgeBase();
    	return f.getName() + " from " + kb + "_" + (kb.equals(_kb1) ? "1" : "2");
    }

    public Frame getFirstSource (Frame f) {
     	Collection sources = getSources (f);
        if (sources == null || sources.isEmpty()) return null;
        return (Frame)CollectionUtilities.getFirstItem(sources);
    }

    public Frame getSoleSource (Frame f) {
     	Collection sources = getSources (f);
        if (sources == null || sources.isEmpty()) return null;
        return (Frame)CollectionUtilities.getSoleItem(sources);
    }

    public Collection<TableRow> getRows (Frame f) {
	    Collection<TableRow> images = _sourceToRowsMap.getValues(createKey(f));
        Collection<TableRow> sources = _imageToRowsMap.getValues(createKey(f));
        //Change ontology
        if((images == null || images.isEmpty())&&(sources == null || sources.isEmpty())){
        	//Collection result = new ArrayList();
        	TableRow rowWithNoFrame = new TableRow(_kb1.getFrame(f.getName()),_kb2.getFrame(f.getName()),TableRow.RENAME_MINUS,TableRow.OPERATION_MAP);
        	rowWithNoFrame.setMappingLevel(TableRow.MAPPING_LEVEL_UNCHANGED);
        	addElement(rowWithNoFrame);
        	images = _sourceToRowsMap.getValues(createKey(f));
            sources = _imageToRowsMap.getValues(createKey(f));
        	//return result;
        }
        if (images == null || images.isEmpty()) {
            return (sources==null)? new ArrayList<TableRow>() : new ArrayList<TableRow>(sources);
        }
        return new ArrayList<TableRow> (images);
    }

/*
public void showTables() {
Log.getLogger().info ("_sourceToRowsMap");
  Collection keys = _sourceToRowsMap.getKeys();
  Iterator i = keys.iterator();
  while (i.hasNext()) {
      Log.getLogger().info (CollectionUtilities.toString(_sourceToRowsMap.getValues(i.next())));
  }
Log.getLogger().info ("_imageToRowsMap");
  Collection keys2 = _imageToRowsMap.getKeys();
  Iterator i2 = keys.iterator();
  while (i2.hasNext()) {
      Log.getLogger().info (CollectionUtilities.toString(_imageToRowsMap.getValues(i2.next())));
  }
}
*/
    public void printResultsTable () {
        Iterator<TableRow> i = _allRows.iterator();
        while (i.hasNext()) {
        
         	 Log.getLogger().info ("" + i.next());
        }
    }

    public void saveToFile (String fileName,
                            boolean printAdded, boolean printDeleted, boolean printRenamed, boolean printDirectlyChanged,
                            boolean printChanged,
                            boolean printIsomorphic, boolean printUnchanged,
                            boolean printFrameDifferences, int fileSaveType) {
      try {
		if (fileName == null || fileName.equals ("")) return;
		
        PrintStream log = null;
        if(fileSaveType == TAB_DELIMITED) {
        	log = new PrintStream(new BufferedOutputStream(new FileOutputStream(fileName)));
        }
        
		LinkedList listToSave = new LinkedList();
		LinkedList frameDifferences = new LinkedList();
        Iterator i = sort().iterator();
        while (i.hasNext()) {
          TableRow nextRow = (TableRow)i.next();
          Object f1 = nextRow.getF1Value();
          Object f2 = nextRow.getF2Value();
          if (f1 != null && f1 instanceof Frame && Util.displayFrameInDiffTable((Frame)f1) ||
		  	  f2 != null && f2 instanceof Frame && Util.displayFrameInDiffTable((Frame)f2)) {
          	if (f1 == null && printAdded ||
              	f2 == null && printDeleted ||
              	nextRow.getRenameValue() == TableRow.RENAME_PLUS && printRenamed ||
				nextRow.getMappingLevel() == TableRow.MAPPING_LEVEL_DIRECTLY_CHANGED && printDirectlyChanged ||
				nextRow.getMappingLevel() == TableRow.MAPPING_LEVEL_CHANGED && printChanged ||
              	nextRow.getMappingLevel() == TableRow.MAPPING_LEVEL_ISOMORPHIC && printIsomorphic ||
              	nextRow.getMappingLevel() == TableRow.MAPPING_LEVEL_UNCHANGED && printUnchanged) {
          	
          		if(fileSaveType != TAB_DELIMITED) {
	          		listToSave.add(nextRow);
	          		if(printFrameDifferences) {
	          			Iterator iter = nextRow.getOperationExplanation().iterator();
	          	    	while (iter.hasNext()) {
	          	    		frameDifferences.add(iter.next());
	          	    	}
	          		}
          		}
          		else {
          			nextRow.saveToFile(log, printFrameDifferences);
          		}
          	}
		  }
        }
        
        // flush the log if this is our native format, otherwise fire the plugin event
        if(fileSaveType == TAB_DELIMITED) {
        	log.flush();
        }
        else {
        	PluginManager.getInstance().fireDiffSaveEvent(fileName, listToSave, frameDifferences, fileSaveType-1);
        }
  
      } catch (IOException e) {
        e.printStackTrace();
      }
    }


    public void saveToRDF (String fileName, URL oldNS, URL newNS) {
      try {
        PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(fileName)));
        Iterator i = sort().iterator();

        out.println("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"");
        out.println("         xmlns:ver=\"http://ontoview.org/schema/ver/test/#\">");
        out.println("");

        while (i.hasNext()) {
          TableRow nextRow = (TableRow)i.next();
          if (nextRow.getF1Value() == null ||
              nextRow.getF2Value() == null ||
              nextRow.getRenameValue() == TableRow.RENAME_PLUS ||
              nextRow.getMappingLevel() == TableRow.MAPPING_LEVEL_CHANGED )
          nextRow.saveToRDF(out, oldNS, newNS);
        }
        out.println("</rdf:RDF>");
        out.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }


    public void printStatistics (PrintStream out) {
    	Collection unmatchedEntriesFromO1 = getUnmatchedEntriesFromO1 ();
        out.println ("Unmatched entries from ontology 1: " +
                            ((unmatchedEntriesFromO1 == null) ? 0 : unmatchedEntriesFromO1.size()));
    	Collection unmatchedEntriesFromO2 = getUnmatchedEntriesFromO2 ();
        out.println ("Unmatched entries from ontology 2: " +
                            ((unmatchedEntriesFromO2 == null) ? 0 : unmatchedEntriesFromO2.size()));
        Collection rowsWithRenameValue = getRowsWithRenameValue (TableRow.RENAME_MINUS);
        out.println ("Rows without rename in the table: " +
                            ((rowsWithRenameValue == null) ? 0 : rowsWithRenameValue.size()));
        rowsWithRenameValue = getRowsWithRenameValue (TableRow.RENAME_PLUS);
        out.println ("Rows with rename in the table: " +
                            ((rowsWithRenameValue == null) ? 0 : rowsWithRenameValue.size()));
        Collection rowsWithOperation = getRowsWithMappingLevel (TableRow.MAPPING_LEVEL_UNCHANGED);
        out.println ("Unchanged rows in the table: " +
                            ((rowsWithOperation == null) ? 0 : rowsWithOperation.size()));
        rowsWithOperation = getRowsWithMappingLevel (TableRow.MAPPING_LEVEL_ISOMORPHIC);
        out.println ("Isomorphic rows in the table: " +
                            ((rowsWithOperation == null) ? 0 : rowsWithOperation.size()));
        rowsWithOperation = getRowsWithMappingLevel (TableRow.MAPPING_LEVEL_CHANGED);
        out.println ("Changed rows in the table: " +
                            ((rowsWithOperation == null) ? 0 : rowsWithOperation.size()));
    }

    public Collection<TableRow> getRowsWithOperation (String operation) {
        Collection<TableRow> result = new ArrayList<TableRow>();
        Iterator<TableRow> i = _allRows.iterator();
        while (i.hasNext()) {
            TableRow next = i.next();
            if (next.getOperationValue() == operation)
                result.add(next);
        }
        return result;
    }

    
 
    
    public Collection<TableRow> getRowsWithMappingLevel (String level) {
        Collection<TableRow> result = new ArrayList<TableRow>();
        Iterator<TableRow> i = _allRows.iterator();
        while (i.hasNext()) {
            TableRow next = i.next();
            if (next.getMappingLevel() == level)
                result.add(next);
        }
        return result;
    }

    private Collection<TableRow> getRowsWithRenameValue (String value) {
        Collection<TableRow> result = new ArrayList<TableRow>();
     	Iterator<TableRow> i = _allRows.iterator();
        while (i.hasNext()) {
         	TableRow next = i.next();
			if (next.getF1Value() != null && next.getF2Value() != null &&
            	next.getRenameValue() == value)
            	result.add(next);
        }
        return result;
    }

    public Collection<Frame> getUnmatchedEntriesFromO1 () {
    		getAllUnmatchedEntries ();
        return _unmatchedEntriesFromO1;
    }

    public Collection<Frame> getUnmatchedEntriesFromO2 () {
    	getAllUnmatchedEntries ();
        return _unmatchedEntriesFromO2;
    }

    private void getAllUnmatchedEntries () {
    	if (!_tableChanged) return;
     	_unmatchedEntriesFromO1 = new ArrayList<Frame> ();
     	_unmatchedEntriesFromO2 = new ArrayList<Frame> ();
        Iterator<TableRow> i = _allRows.iterator();
        while (i.hasNext()) {
            TableRow nextRow = i.next();
            if (nextRow.getF1Value() == null)
            	_unmatchedEntriesFromO2.add (nextRow.getF2Value());
            else if (nextRow.getF2Value() == null)
            	_unmatchedEntriesFromO1.add (nextRow.getF1Value());
        }
		_tableChanged = false;
    }

    public void removeRowsForFrame (Frame key) {
		Collection<TableRow> images = _sourceToRowsMap.getValues(createKey(key));
        if (images != null && !images.isEmpty())
        	removeElements (images);

		Collection<TableRow> sources = _imageToRowsMap.getValues(createKey(key));
        if (sources != null && !sources.isEmpty())
        	removeElements (sources);

//            if (_traceOn)
//        		Log.getLogger().info ("removed:            " + removed);
    }

    public void removeElements (Collection<TableRow> rows) {
		Collection<TableRow> c = new ArrayList<TableRow> (rows);
    	Iterator<TableRow> i = c.iterator();
        while (i.hasNext())
        	removeElement (i.next());
    }

    public void removeElement (TableRow row) {
        if (row == null) return;
     	_allRows.remove(row);
        if (row.getF1Value() != null)
        	_sourceToRowsMap.removeValue (createKey(row.getF1Value()), row);
        if (row.getF2Value() != null)
        	_imageToRowsMap.removeValue (createKey(row.getF2Value()), row);
       _tableChanged = true;
    }

    public Collection<TableRow> values () {
    	return _allRows;
    }

    public void traceOn (boolean value) {
     	_traceOn = value;
	}

    public boolean traceOn () {
     	return _traceOn;
	}

    public void appendOperationExplanation (TableRow row, FrameDifferenceElement explanation) {
     	row.appendOperationExplanation(explanation);
    }

    public void appendOperationExplanation (TableRow row, Collection explanation) {
      if (explanation == null) return;
      Iterator i = explanation.iterator();
      while (i.hasNext()) {
     	row.appendOperationExplanation((FrameDifferenceElement)i.next());
      }
    }

    public void setRenameExplanation (TableRow row, String explanation) {
     	row.setRenameExplanation(explanation);
    }

    public void setOperation (TableRow row, String operation) {
        if (operation.equals(row.getOperationValue())) return;
        if (_traceOn)
          Log.getLogger().info ("Changing row. From: " + row);
     	row.setOperation(operation);
        if (_traceOn)
	  Log.getLogger().info ("to:                 " + row + "\n");
        _tableChanged = true;
    }

/*
    public void setMappingLevel (TableRow row, String level) {
        if (level.equals(row.getMappingLevel())) return;
        if (_traceOn)
          Log.getLogger().info ("Changing row. From: " + row);
     	row.setMappingLevel(level);
        if (_traceOn)
	  Log.getLogger().info ("to:                 " + row + "\n");
        _tableChanged = true;
    }
*/

    public void setF2Value (TableRow row, Frame value) {
        if (value.equals(row.getF2Value())) return;
        _imageToRowsMap.removeValue(createKey(row.getF2Value()), row);
        if (_traceOn)
	  Log.getLogger().info ("Changing row. From: " + row);
     	row.setF2Value(value);
        _imageToRowsMap.addValue(createKey(value), row);
        if (_traceOn)
	  Log.getLogger().info ("to:                 " + row + "\n");
	if (row.getF1Value() != null && Util.isSystem((Frame)row.getF1Value()) &&
            row.getF2Value() != null && Util.isSystem((Frame)row.getF2Value())) {
        	row.setOperation(TableRow.OPERATION_MAP);//
//                row.setMappingLevel (TableRow.MAPPING_LEVEL_UNCHANGED);
        }
        _tableChanged = true;
    }

    public void setRenameValue (TableRow row, String value) {
        if (value.equals(row.getRenameValue())) return;
//		if (_traceOn)
//			Log.getLogger().info ("Changing row. From: " + row);
     	row.setRenameValue(value);
//		if (_traceOn)
//			Log.getLogger().info ("\nto:                 " + row + "\n");
        _tableChanged = true;
    }

/*
    public void setAddDeleteOperations () {
    	boolean oldTraceValue = _traceOn;
        _traceOn = false;
    	Iterator i = _allRows.iterator();
        while (i.hasNext()) {
            TableRow next = (TableRow)i.next();
            if (next.getF1Value() == null)
            	next.setOperation(TableRow.OPERATION_ADD);
            if (next.getF2Value() == null)
            	next.setOperation(TableRow.OPERATION_DELETE);
            if (next.getOperationValue() == null)
            	next.setOperation(TableRow.OPERATION_MAP);
            if (next.getMappingLevel() == null)
                next.setMappingLevel (TableRow.MAPPING_LEVEL_CHANGED);
            if (next.getMappingLevel() == TableRow.MAPPING_LEVEL_STRONG_ISOMORPHIC ||
            	next.getMappingLevel() == TableRow.MAPPING_LEVEL_WEAK_ISOMORPHIC)
                next.setMappingLevel(TableRow.MAPPING_LEVEL_ISOMORPHIC);
        }
        _traceOn = oldTraceValue;
    }
*/

    public void fillInMapOperation () {
    	boolean oldTraceValue = _traceOn;
        _traceOn = false;
    	Iterator<TableRow> i = _allRows.iterator();
        while (i.hasNext()) {
            TableRow next = i.next();
            if (next.getOperationValue() == null)
              next.setOperation (TableRow.OPERATION_MAP);
        }
    }

	public String toString () {
   		return "ResultTable";
  	}

	public KnowledgeBase getKb1() {
		return _kb1;
	}

	public KnowledgeBase getKb2() {
		return _kb2;
	}

	public void dispose() {
	    _allRows.clear();
	    _sourceToRowsMap.clear();
	    _imageToRowsMap.clear();
	    _unmatchedEntriesFromO1.clear();
	    _unmatchedEntriesFromO2.clear();
	    _systemFramesFromKb1.clear();
	    _systemFramesFromKb2.clear();
	    if (_kb2 != null) {
	        _kb2.removeKnowledgeBaseListener(replaceFrameListener);
	    }
	    _kb1 = _kb2 = null;
	}
}

