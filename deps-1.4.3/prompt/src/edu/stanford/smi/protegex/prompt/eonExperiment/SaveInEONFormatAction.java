/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt.eonExperiment;

import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;

	public class SaveInEONFormatAction  extends AbstractAction {
	   JPanel _parent;

	   public SaveInEONFormatAction(JPanel parent) {
		super("save in EON format", Icons.getSaveProjectIcon());
		_parent = parent;
	  }

	  public void actionPerformed(ActionEvent e) {
		  SaveInEONFormatDialog input = new SaveInEONFormatDialog(_parent);
		  if (input.saveFile())
			  SaveInEONFormat.saveInEONFormat(input.getFileName(), input.getOldNS(), input.getNewNS());
	   }
	}
