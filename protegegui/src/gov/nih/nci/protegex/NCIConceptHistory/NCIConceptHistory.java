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
 */
package edu.stanford.smi.protegex.NCIConceptHistory;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import edu.stanford.smi.protege.plugin.ProjectPluginAdapter;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.ui.ProjectMenuBar;
import edu.stanford.smi.protege.ui.ProjectToolBar;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.event.PromptAdapter;
import edu.stanford.smi.protegex.prompt.event.PromptEvent;
import edu.stanford.smi.protegex.prompt.event.PromptListenerManager;
import edu.stanford.smi.protegex.prompt.ui.diffUI.DiffTableView;

public class NCIConceptHistory extends ProjectPluginAdapter {
	private transient Logger log = Log.getLogger(getClass());

	@Override
    public void afterShow(ProjectView view, ProjectToolBar toolBar, ProjectMenuBar menuBar) {
    	if (log.isLoggable(Level.FINE)) {
    		log.fine("after Show ");
    	}
        try {
            Class cls = Class.forName("edu.stanford.smi.protegex.prompt.PromptTab");
        } catch (ClassNotFoundException ex) {
            return;
        }
        
        PromptListenerManager.addListener(new PromptAdapter() {
        	@Override
        	public void beforeClose(PromptEvent event) {
        		askAboutSavingNCIDB();
        	}
        	
        	@Override
            public void diffTableViewBuilt(DiffTableView view, PromptEvent event) {
                view.addHeaderButton(new SaveInNCIDBAction(view));
            }
        });
    }

    public void askAboutSavingNCIDB() {
    	try {
    		if (PromptTab.getPromptDiff() == null) {
    			return;
    		}
    		NCIDBSaver saver = NCIDBSaver.getNCIDBSaver();
    		if (saver == null) {
    			return;
    		}
    		String message = saver.saved() ? 
    				"The NCIConcept History Database was saved in the past. Does it need to be resaved?" :
    					"Saved the NCIConcept History Database (or lose your changes)?";
    		int save = ModalDialog.showMessageDialog(ProjectManager.getProjectManager().getMainPanel(),
    					message,
    					"Save NCIConceptHistory Dialog",
    					ModalDialog.MODE_YES_NO);
    		if (save == ModalDialog.OPTION_YES) {
    			saver.save();
    		}
    	}
    	catch (IOException ioe) {
    		log.log(Level.SEVERE, "Failed to save to NCI Database");
    	}
    	
    }
}
