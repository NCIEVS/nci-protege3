 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JList;

import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.operation.Operation;
import edu.stanford.smi.protegex.prompt.ui.ActionListPane;

public  class PerformButtonActionListener implements ActionListener {
//    JList _list;
	private JList _list = null;
    private ActionListPane _table = null;
    public PerformButtonActionListener (JComponent list) {
      super();
      if (list instanceof ActionListPane)
      	_table = (ActionListPane)list;
      else
      	_list = (JList)list;
    }

    public void actionPerformed(ActionEvent e) {
		Collection selectedOperations = null;
    	if (_list != null) {
	         int index = _list.getSelectedIndex();
	  	     if (index == -1) return;
      	     Object [] selection = _list.getSelectedValues();
             selectedOperations = Arrays.asList(selection);
   		} else { //_table != null
         	selectedOperations = _table.getSelection ();
        }
        if (selectedOperations != null) {
        	PromptTab.addToQueue(selectedOperations);
            ((Operation)CollectionUtilities.getFirstItem(selectedOperations)).performOperation();
//         	Iterator i = selectedOperations.iterator();
//            while (i.hasNext()) {
//             	((Operation)i.next()).performOperation();
//            }
        }
    }
}


