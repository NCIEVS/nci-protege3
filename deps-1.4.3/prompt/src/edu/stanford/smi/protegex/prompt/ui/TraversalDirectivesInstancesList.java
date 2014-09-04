/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt.ui;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

//same as the parent, except there are no buttons
public class TraversalDirectivesInstancesList extends DirectInstancesList {

	protected void addButtons(Action viewAction, LabeledComponent c) {
		c.addHeaderButton(viewAction);
		createCreateAction();
		createCopyAction();
		createReferencersAction();
		createDeleteAction();
	}


	public TraversalDirectivesInstancesList(Project project) {
		super(project);
	}

}
