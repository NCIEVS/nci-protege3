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
 * The Original Code is PROMPT.
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
 */

/*
 * Contributor(s): Sandhya Kunnatur kunnatur@smi.stanford.edu
*/

package edu.stanford.smi.protegex.prompt.ui.diffUI;

import edu.stanford.smi.protege.model.*;

public interface DiffClsListener
{
	public void clsUpdated(Cls cls);
	public void childRemoved(Cls cls);
	public void childAdded(Cls cls);
}	