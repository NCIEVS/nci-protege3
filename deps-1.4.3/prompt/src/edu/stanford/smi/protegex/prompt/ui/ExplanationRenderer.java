 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.Component;

import javax.swing.JList;

import edu.stanford.smi.protegex.prompt.actionLists.Action;
import edu.stanford.smi.protegex.prompt.actionLists.ActionArgs;

public  class ExplanationRenderer extends ActionRenderer {
	// now works only for _numberOfLines <= 3
    private int _numberOfLines = 3;
  	private int _index;

    	public ExplanationRenderer (int numberOfLines) {
         	super ();
            _numberOfLines = numberOfLines;
        }

        public void load (Object o) {
			if (_index % _numberOfLines == 0)
            	loadFirstString ((Action)o);
			if (_index % _numberOfLines == 1)
            	loadSecondString ((Action)o);
			if (_index % _numberOfLines == 2)
            	loadThirdString ((Action)o);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index,
          boolean isSelected, boolean cellHasFocus) {
        	_index = index;
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }

        private void loadFirstString (Action c) {
        	setMainString (c.getShortName ());
            ActionArgs args = c.getArgs();
            if (args != null && args.size() > 0) {
            	setArgs (new Object [] {args.getArg (0)});
            } else {
             	setArgs (null);
                setMainString ("");
            }
            setTrailingString("");
        }

        private void loadSecondString (Action c) {
        	setMainString (c.getConnectorString());
            ActionArgs args = c.getArgs();
            if (args != null && args.size() > 1) {
            	setArgs (new Object [] {args.getArg (1)});
            } else {
             	setArgs (null);
                setMainString ("");
            }
            setTrailingString("");
        }

        private void loadThirdString (Action c) {
            ActionArgs args = c.getArgs();
            if (args != null && args.size() > 2) {
        		setMainString ("    " + c.getConnectorString2());
            	setArgs (new Object [] {args.getArg (2)});
            } else {
             	setArgs (null);
                setMainString ("");
            }
            setTrailingString(c.getTrailingString());
        }
}


