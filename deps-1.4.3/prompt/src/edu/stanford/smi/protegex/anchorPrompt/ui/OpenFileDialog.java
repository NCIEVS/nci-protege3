/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.anchorPrompt.*;


public class OpenFileDialog  {
    static int _numberOfProjects;
    static boolean _filesOpened = false;
    static File _tempLog = null;

    public OpenFileDialog () {
       _numberOfProjects = AnchorPromptTab.getNumberOfActiveKbs() ;
       OpenFilesPanel openDialog = new OpenFilesPanel();
       int confirmation = ModalDialog.showDialog (AnchorPromptTab.getMainWindow(),
                          openDialog, "Open", ModalDialog.MODE_OK_CANCEL);
       if (confirmation == ModalDialog.OPTION_OK) {
        String [] projectFiles = new String [_numberOfProjects];
        String [] projectNames = new String [_numberOfProjects];
        _filesOpened = openDialog.collectInformation (projectFiles, projectNames);

        if (_filesOpened == false) return;

        String filePath = AnchorPromptTab.getTargetProject().getProjectDirectoryURI().toString();

		try {
        if (filePath != null)
	        projectFiles [_numberOfProjects-1] = AnchorPromptTab.getTargetProject().getProjectDirectoryURI().toString();
        else {
            _tempLog = File.createTempFile("temp", ".mlog");
            projectFiles [_numberOfProjects-1] = _tempLog.getPath();
        }
        } catch (IOException e) {
           Log.getLogger().severe(e.toString());
        }
        projectNames [_numberOfProjects-1] = "merged";

		AnchorPromptTab.setUpNewProjects (projectFiles, projectNames);
        _filesOpened = true;
       }
    }

	public boolean filesOpened () {return _filesOpened;}

    public class OpenFilesPanel extends JPanel{
      SingleFilePanel [] _filePanels;
      OpenFilesPanel () {
        super ();
        setLayout (new BoxLayout (this, BoxLayout.Y_AXIS));
        _filePanels = new SingleFilePanel [_numberOfProjects];
        for (int i = 0; i < _numberOfProjects -1; i++) {
          if (i == _numberOfProjects - 1)
             _filePanels [i] = new SingleFilePanel ();
          else
             _filePanels [i] = new SingleFilePanel (i);
          add (_filePanels[i]);
        }
      }

    public boolean collectInformation (String [] names, String [] aliases) {
      for (int i = 0; i < _numberOfProjects-1; i++) {
         names [i] = _filePanels[i].getFileName();
         if (names[i] == null) return false;
         aliases [i] = _filePanels[i].getFileAlias ();
      }
      return true;
    }

    public class SingleFilePanel extends JPanel {
      JTextField _fileName;
      JTextField _fileAlias;
      SingleFilePanel _panel;

      SingleFilePanel (int number) {
        super ();
        if (number == 0)
          initialize ("Choose the first source");
        else if (number == 1)
          initialize ("Choose the second source");
        else
          initialize ("Choose the next source");
      }

      SingleFilePanel () {
        super ();
        initialize ("Target");
      }

      private void initialize (String label) {
        setLayout(new BoxLayout (this, BoxLayout.X_AXIS));
        _fileName = ComponentFactory.createTextField ();
        _fileAlias = ComponentFactory.createTextField ();
        _panel = this;
        LabeledComponent nameField = new LabeledComponent (label,  _fileName);
        LabeledComponent aliasField = new LabeledComponent ("Alias",  _fileAlias);
        Action findAction = new AbstractAction ("FindFile", Icons.getAddIcon()) {
 //             Component _parent = _panel;
              public void actionPerformed(ActionEvent e) {
               JFileChooser chooser =  ComponentFactory.createFileChooser ("Select file", ".pprj");
               int returnVal = chooser.showOpenDialog (_panel);
               if (returnVal == JFileChooser.APPROVE_OPTION) {
                 File chosen = chooser.getSelectedFile();
                 _fileName.setText (chosen.getPath());
                 _fileAlias.setText (getAlias(chosen));
               }
             }
        };

        nameField.addHeaderButton (findAction);

        nameField.setPreferredSize(new Dimension (350, (int)nameField.getPreferredSize().getHeight()));
        add (nameField);

        add (Box.createRigidArea(new Dimension (10, 0)));

        aliasField.setPreferredSize(new Dimension (100, (int)aliasField.getPreferredSize().getHeight()));
        add (aliasField);
      }

      private String getAlias (File file) {
        if (_fileAlias.getText().equals("")) {
            String alias = file.getName();
            return alias.substring (0, alias.indexOf ('.'));
        } else
          return _fileAlias.getText();
      }

      private String getAlias () {
        if (_fileAlias.getText().equals("") && _fileName.getText() != null) {
            String alias = getFileName(_fileName.getText());
            return alias.substring (0, alias.indexOf ('.'));
        } else
          return _fileAlias.getText();
      }

      private String getFileName (String name) {
        int begin = name.lastIndexOf(File.separatorChar);
        if (begin == -1) begin = 0;
        return name.substring(begin + 1);
      }

      public String  getFileName () { return _fileName.getText(); }

      public String  getFileAlias () {
      	return getAlias ();
      }

    }
  }

}

