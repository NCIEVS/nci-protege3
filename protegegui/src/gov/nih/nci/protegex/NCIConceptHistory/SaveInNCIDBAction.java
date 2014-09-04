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

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.JPanel;

import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.util.Log;

public class SaveInNCIDBAction extends AbstractAction {
    JPanel _parent;

    public SaveInNCIDBAction(JPanel parent) {
        super("save in NCI DB format", Icons.getSaveProjectIcon());
        _parent = parent;
    }

    public void actionPerformed(ActionEvent e) {
        NCIDBSaver saver = null;
        try {
            saver = NCIDBSaver.getNCIDBSaver();
            saver.save();
        }
        catch (IOException ioe) {
            Log.getLogger().log(Level.WARNING, "Could not write concept status database", ioe);
        }
        finally {
            if (saver != null) saver.dispose();
        }
    }
}


