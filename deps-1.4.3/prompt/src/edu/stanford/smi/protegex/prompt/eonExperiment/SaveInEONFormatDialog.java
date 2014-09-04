 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.eonExperiment;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.ui.*;


public class SaveInEONFormatDialog  {
    static boolean _saveFile = false;
	static SaveFilePanel _panel = null;

    public SaveInEONFormatDialog (JPanel parent) {
		_panel = new SaveFilePanel();

		int confirmation = ModalDialog.showDialog (parent,
							_panel, "Save to file", ModalDialog.MODE_OK_CANCEL);
		_saveFile = (confirmation == ModalDialog.OPTION_OK) ? true : false;
    }

    public boolean saveFile () {return _saveFile;}

    public String getFileName () { return _panel.getFileName(); }

	public String getOldNS () { return _panel.getOldNS(); }
	public String getNewNS () { return _panel.getNewNS(); }

    public class SaveFilePanel extends JPanel{
      private SingleFilePanel _fileName = new SingleFilePanel();
	  private JTextField _oldNS = new JTextField ("file://localhost/Volumes/Phata/Web/html/co4/align/Contest/101/onto.rdf");
	  private JTextField _newNS = new JTextField ("file://localhost/Volumes/Phata/Web/html/co4/align/Contest/101/onto.rdf");

      SaveFilePanel () {
        super ();
        setLayout (new GridLayout (3, 0));
		add (new LabeledComponent ("Save to file:", _fileName));
		add (new LabeledComponent ("Old namespace:", _oldNS));
		add (new LabeledComponent ("New namespace:", _newNS));
      }


      public String getFileName () {return _fileName.getFileName();}

	  public String getOldNS () {return _oldNS.getText();}
	  public String getNewNS () {return _newNS.getText();}
 
      public class SingleFilePanel extends JPanel {
        JTextField _fileName = ComponentFactory.createTextField ();
        JPanel _this;

        SingleFilePanel ( ) {
          super ();
          _this = this;
          setLayout(new FlowLayout());
          LabeledComponent nameField = new LabeledComponent ("Choose file name:",  _fileName);
          Action findAction = new AbstractAction ("FindFile", Icons.getAddIcon()) {
              Component _parent = _this;
              public void actionPerformed(ActionEvent e) {
               JFileChooser chooser =  DisplayUtilities.createFileChooser ("Select file", ".rdf");
               int returnVal = chooser.showOpenDialog (_panel);
               if (returnVal == JFileChooser.APPROVE_OPTION) {
                 File chosen = chooser.getSelectedFile();
                 _fileName.setText (chosen.getPath());
               }
             }
        };

        nameField.addHeaderButton (findAction);

        nameField.setPreferredSize(new Dimension (350, (int)nameField.getPreferredSize().getHeight()));
        add (nameField);
      }

      private String getFileName (String name) {
      	if (name.length() == 0) {
        	return "";
        }

        int begin = name.lastIndexOf(File.separatorChar);
        if (begin == -1) begin = 0;
        return name.substring(begin + 1);
      }

      public String  getFileName () { return _fileName.getText(); }

    }
  }

}

