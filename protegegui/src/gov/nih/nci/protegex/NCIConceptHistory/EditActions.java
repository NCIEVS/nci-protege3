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
 * The Original Code is PROMPT NCI-EVS
 *
 * The Initial Developer of the Original Code is Stanford University. Portions
 * created by Stanford University are Copyright (C) 2004.  All Rights Reserved.
 *
 * PROMPT was developed by Stanford Medical Informatics
 * (http//www.smi.stanford.edu) at the Stanford University School of Medicine
 * with support from the the Defense Advanced Research Projects Agency and National
 * Cancer Institute.  Current information about PROMPT can be obtained at
 * http//protege.stanford.edu

 * Created by IntelliJ IDEA.
 * User: prashr
 * Date: Dec 1, 2004
 * Time: 12:00:08 PM
 * To change this template use File | Settings | File Templates.
 */
package edu.stanford.smi.protegex.NCIConceptHistory;

public enum EditActions {
    CREATE("create"), MODIFY("modify"), PRERETIRE("modify"),
        RETIRE("retire"), MERGE("merge"), SPLIT("split");

    private final String myName; // for debug only

    private EditActions(String name) {
        myName = name;
    }

    public String toString() {
        return myName;
    }
}
