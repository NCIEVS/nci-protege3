 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JList;
import javax.swing.JPanel;

import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protegex.prompt.conflict.Conflict;

public class ExplanationPane extends JPanel {
  	JList _content;
    final private int NUMBER_OF_LINES = 3;
    ExplanationRenderer _renderer = new ExplanationRenderer (NUMBER_OF_LINES);
  	private int _index;

    ExplanationPane () {
    	_content = ComponentFactory.createList (null);
        _content.setCellRenderer(_renderer);

    	setLayout (new BorderLayout ());
        add (_content, BorderLayout.CENTER);
    }

    public void setValue (Conflict c) {
        if (c != null) {
        	Collection listValues = new ArrayList ();
        	for (int i = 0; i < NUMBER_OF_LINES; i++) {
             	listValues.add (c);
            }
            ComponentUtilities.setListValues (_content, listValues);
        }
    }

    private  class ConflictExplanationRenderer extends ActionRenderer {
        public void load (Object o) {
			if (_index % NUMBER_OF_LINES == 0)
            	loadFirstString ((Conflict)o);
			if (_index % NUMBER_OF_LINES == 1)
            	loadSecondString ((Conflict)o);
			if (_index % NUMBER_OF_LINES == 2)
            	loadThirdString ((Conflict)o);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index,
          boolean isSelected, boolean cellHasFocus) {
        	_index = index;
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }

        private void loadFirstString (Conflict c) {
        	setMainString (c.getShortName ());
            Object [] args = c.getArgs().toArray();
            if (args != null && args.length > 0) {
            	setArgs (new Object [] {args[0]});
            	setTrailingString (c.getConnectorString());
            }
        }

        private void loadSecondString (Conflict c) {
        	setMainString ("     ");
            Object [] args = c.getArgs().toArray();
            if (args != null && args.length > 1) {
            	setArgs (new Object [] {args[1]});
            	setTrailingString (c.getConnectorString2());
            }
        }

        private void loadThirdString (Conflict c) {
            Object [] args = c.getArgs().toArray();
            if (args != null && args.length > 2) {
        		setMainString ("     ");
            	setArgs (new Object [] {args[2]});
            }
        }
    }

}

