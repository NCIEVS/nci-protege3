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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.ReferenceImpl;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.ResultTable;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.TableRow;
import edu.stanford.smi.protegex.prompt.util.Util;


public class NCIDBSaver {
    private static final transient Logger log = Log.getLogger(NCIDBSaver.class);
    
    public final static String EMPTY_CODE = "0";
    public final static String EMPTY_REFERENCE = null;

    private ResultTable results;
    private int count = 0;
    private OntologyInfo ontologyInfo2;
    private NCIConceptDBConnector connector;

    private Map<Reference, List> hintsMap = new HashMap<Reference, List>(); 
    private boolean firstTime = true;
    private int rollbackLocation;
    private boolean saved = false;
    
    private static NCIDBSaver dbSaver = null;
    
    
    private NCIDBSaver() throws IOException {
        results = PromptTab.getPromptDiff().getResultsTable();
        ontologyInfo2 = new OntologyInfo(results.getKb2());
        connector = new NCIConceptDBConnector();
    }
    
    /*
     * TODO - multiple  prompt sessions? prompt results table is the key?
     */
    public static NCIDBSaver getNCIDBSaver() throws IOException {
    	if (dbSaver == null && PromptTab.getPromptDiff() != null) {
    		dbSaver = new NCIDBSaver();
    	}
    	return dbSaver;
    }

    public void save() throws IOException {
    	init();
        Iterator i = results.values().iterator();
        count = 1;
        int numRows = results.values().size();
        while (i.hasNext()) {
            TableRow nextRow = (TableRow) i.next();
            saveInNCIDB(nextRow);
            if (count % 1000 == 0) {
                log.warning(new StringBuffer().append("Log Message at ").append(new Date().toString()).append(": Parsed ").append(count).append(" rows of ").append(numRows).toString());
            }
            count++;
        }
        saved = true;
    }
    
    public boolean saved() {
    	return saved;
    }
    
    private void init() throws IOException {
    	try {
    		if (firstTime) {
    			rollbackLocation = connector.getSize();
    			firstTime = false;
    		}
    		else {
    			connector.rollback(rollbackLocation);
    		}
    	}
    	catch (SQLException sqle) {
    		log.log(Level.WARNING, "Exception caught trying to start concept history save operation");
    		IOException ioe = new IOException(sqle.getMessage());
    		ioe.initCause(sqle);
    		throw ioe;
    	}
    }
    
    private void saveInNCIDB(TableRow nextRow) throws IOException {
        Frame f1 = nextRow.getF1Value();
        Frame f2 = nextRow.getF2Value();
        String operation = nextRow.getOperationValue();
        String mapLevel = nextRow.getMappingLevel();

        if (skipCase(f1, f2, operation, mapLevel)) return;
        if (handleRetireOrMerge(nextRow, (Cls) f1, (Cls) f2, operation, mapLevel)) return;
        if (handleNewOrSplit(nextRow, (Cls) f1, (Cls) f2, operation, mapLevel)) return;
        handleGenericModify(nextRow, (Cls) f1, (Cls) f2, operation, mapLevel);
    }
    
    private boolean handleRetireOrMerge(TableRow row, 
                                        Cls conceptCls1, Cls conceptCls2, 
                                        String operation, String mapLevel) 
    throws IOException {
    	
        if (conceptCls2.hasSuperclass(ontologyInfo2.getRetiredConcepts())) {
        	String code2 = (String) conceptCls2.getDirectOwnSlotValue(ontologyInfo2.getCodeSlot());
            Object merge_into = getSlotValueAndOptionallyRemove(conceptCls2, ontologyInfo2.getMergeIntoSlot(), true);
            if (validCode(merge_into)) {
                connector.writeLogEntrytoDB(code2, EditActions.RETIRE, EMPTY_REFERENCE);
                connector.writeLogEntrytoDB(code2, EditActions.MERGE, (String) merge_into);
                connector.writeLogEntrytoDB((String) merge_into, EditActions.MERGE, (String) merge_into);
            }  else {
            	for (Object o : getSlotValuesAndOptionallyRemove(conceptCls2, ontologyInfo2.getOldParentSlot(), false)) {
                    if (o instanceof String) {
                        String cls_name = (String) o;
                        Frame frame = results.getKb2().getFrame(ontologyInfo2.getFullName(cls_name));
                        if (frame == null || !(frame instanceof Cls)) {
                            Log.getLogger().info("" + conceptCls2.getBrowserText() + " has deleted parents");
                            connector.writeLogEntrytoDB(code2, EditActions.RETIRE, null);
                        }
                        else {
                        	Object parentCode = ((Cls) frame).getOwnSlotValue(ontologyInfo2.getCodeSlot());
                        	if (validCode(parentCode)) {
                        		connector.writeLogEntrytoDB(code2, EditActions.RETIRE, (String) parentCode);
                        		if (log.isLoggable(Level.FINE)) log.fine("Successfully commited record to DB");
                        	}
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    private boolean handleNewOrSplit(TableRow row, 
                                     Cls conceptCls1, Cls conceptCls2, 
                                     String operation, String mapLevel) 
    throws IOException {
        if (conceptCls1 == null) {
            String new_code = (String) conceptCls2.getDirectOwnSlotValue(ontologyInfo2.getCodeSlot());
            connector.writeLogEntrytoDB(new_code, EditActions.CREATE, EMPTY_REFERENCE);
            Object split_from = getSlotValueAndOptionallyRemove(conceptCls2, ontologyInfo2.getSplitFromSlot(), true);
            if (validCode(split_from)) {
                connector.writeLogEntrytoDB((String) split_from, EditActions.SPLIT, (String) split_from);
                connector.writeLogEntrytoDB((String) split_from, EditActions.SPLIT, new_code);
            }
            return true;
        }
        return false;
    }
    
    private void handleGenericModify(TableRow row, 
                                     Cls conceptCls1, Cls conceptCls2, 
                                     String operation, String mapLevel) 
    throws IOException {
        String code = (String) conceptCls2.getDirectOwnSlotValue(ontologyInfo2.getCodeSlot());
        connector.writeLogEntrytoDB(code, EditActions.MODIFY, EMPTY_REFERENCE);
    }
    
    private boolean skipCase(Frame f1, Frame f2, String operation, String mapLevel) {
        if (f2 == null) return true;
        if (!(f2 instanceof Cls)) return true;
        if (Util.isSystem(f2)) return true;
        Object code2 = f2.getDirectOwnSlotValue(ontologyInfo2.getCodeSlot());
        if (!validCode(code2)) return true;
        if (mapLevel.equals(TableRow.MAPPING_LEVEL_ISOMORPHIC)) return true;
        if (mapLevel.equals(TableRow.MAPPING_LEVEL_UNCHANGED)) return true;
        return false;
    }
    
    public static boolean validCode(Object code) {
        return code != null && code instanceof String && !code.equals(EMPTY_CODE);
    }
    
    public void dispose() {
        if (connector != null) {
            connector.dispose();
            connector = null;
        }
    }
    
    @SuppressWarnings("unchecked")
	private Object getSlotValueAndOptionallyRemove(Frame frame, Slot slot, boolean remove) {
    	List results = getSlotValuesAndOptionallyRemove(frame, slot, remove);
    	if (results.isEmpty()) {
    		return null;
    	}
    	else {
    		return results.iterator().next();
    	}
    }
    
	@SuppressWarnings("unchecked")
	private List getSlotValuesAndOptionallyRemove(Frame frame, Slot slot, boolean remove) {
    	Reference r = new ReferenceImpl(frame, slot, null, false);
    	List result = hintsMap.get(r);
    	if (result == null) {
    		result = frame.getDirectOwnSlotValues(slot);
    		if (result == null) {
    			result = new ArrayList();
    		}
    		hintsMap.put(r, result);
    		if (!result.isEmpty() && remove) {
    			frame.setDirectOwnSlotValues(slot, Collections.emptyList());
    		}
    	}
    	return result;
    }


}



