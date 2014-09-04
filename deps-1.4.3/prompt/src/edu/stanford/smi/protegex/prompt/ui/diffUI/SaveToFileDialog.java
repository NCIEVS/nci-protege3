 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui.diffUI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protegex.prompt.plugin.PluginManager;
import edu.stanford.smi.protegex.prompt.plugin.model.DiffUIPlugin;
import edu.stanford.smi.protegex.prompt.ui.DisplayUtilities;


public class SaveToFileDialog  {
    static boolean _saveFile = false;
    static SaveFilePanel _panel = null;

    public SaveToFileDialog (JPanel parent) {
      _panel = new SaveFilePanel();

      int confirmation = ModalDialog.showDialog (parent,
                          _panel, "Save to file", ModalDialog.MODE_OK_CANCEL);
      _saveFile = (confirmation == ModalDialog.OPTION_OK) ? true : false;
    }

    public boolean saveFile () {return _saveFile;}

    public String getFileName () { return _panel.getFileName(); }

    public boolean printAdded () { return _panel.printAdded(); }
    public boolean printDeleted () { return _panel.printDeleted(); }
    public boolean printRenamed () { return _panel.printRenamed(); }
	public boolean printDirectlyChanged () { return _panel.printDirectlyChanged(); }
	public boolean printChanged () { return _panel.printChanged(); }
    public boolean printIsomorphic () { return _panel.printIsomorphic(); }
	public boolean printUnchanged () { return _panel.printUnchanged(); }
	public boolean printFrameDifferences () { return _panel.printFrameDifferences(); }
	public int getFileSaveType() { return _panel.getFileSaveType(); }

    public class SaveFilePanel extends JPanel{
      private SingleFilePanel _fileName = new SingleFilePanel();
      private JLabel _label = new JLabel ("Include in the file:");
      private JCheckBox _printAdded = new JCheckBox ("Added frames", true);
      private JCheckBox _printDeleted = new JCheckBox ("Deleted frames", true);
      private JCheckBox _printRenamed = new JCheckBox ("Renamed frames", true);
	  private JCheckBox _printDirectlyChanged = new JCheckBox ("Directly changed frames");
	  private JCheckBox _printChanged = new JCheckBox ("Indirectly changed frames");
      private JCheckBox _printIsomorphic = new JCheckBox ("Isomorphic frames");
	  private JCheckBox _printUnchanged = new JCheckBox ("Unchanged frames");
	  private JCheckBox _printFrameDifferences = new JCheckBox ("Print differences for each pair of frames");
	  private JComboBox _diffPlugins = new JComboBox();
	  private JPanel _centerPanel = new JPanel(new GridLayout(0, 1));
	  
      SaveFilePanel () {
        super ();
        setLayout (new BorderLayout (0, 5));
        add (new JLabel ("Save to file:"), BorderLayout.NORTH);
        
        _centerPanel.add(_fileName);
        _centerPanel.add(createPluginBox());
        
        add (_centerPanel, BorderLayout.CENTER);
        add (createPanelWithCheckBoxes(), BorderLayout.SOUTH);
      }
      
      /**
       * Creates the file save options panel, which includes the list of plugin save options.
       * @return
       */
      private JPanel createPluginBox() {
    	  JPanel container = new JPanel();
    	  LinkedList pluginList = PluginManager.getInstance().getPlugins(PluginManager.PLUGIN_UI_DIFF);
    	  String[] names = new String[pluginList.size() + 1];
    	  names[0] = "Tab delimited";
    	  int count = 1;
    	  
    	  for(Iterator iter = pluginList.iterator(); iter.hasNext(); ) {
    		  DiffUIPlugin diffPlugin = (DiffUIPlugin)iter.next();
    		  if(diffPlugin.getFileFormatName().length() > 0) {
    			  names[count++] = diffPlugin.getFileFormatName();
    		  }
    	  }
    	  
    	  _diffPlugins.setPreferredSize(new Dimension(350, 25));
    	  _diffPlugins.setModel(new DefaultComboBoxModel(names));
    	  
    	  LabeledComponent nameField = new LabeledComponent ("File options:",  _diffPlugins);

    	  container.add(nameField);
    	  
    	  return container;
      }

      private JPanel createPanelWithCheckBoxes() {
        JPanel result = new JPanel(new GridLayout(0, 1));
        result.add(_printAdded);
        result.add(_printDeleted);
        result.add(_printRenamed);
		result.add(_printDirectlyChanged);
		result.add(_printChanged);
        result.add(_printIsomorphic);
		result.add(_printUnchanged);
		result.add(_printFrameDifferences);
        return result;
      }

      public String getFileName () {return _fileName.getFileName();}

      public boolean printAdded () {return _printAdded.isSelected();}
      public boolean printDeleted () {return _printDeleted.isSelected();}
      public boolean printRenamed () {return _printRenamed.isSelected();}
	  public boolean printDirectlyChanged () {return _printDirectlyChanged.isSelected();}
      public boolean printIsomorphic () {return _printIsomorphic.isSelected();}
	  public boolean printChanged () {return _printChanged.isSelected();}
	  public boolean printUnchanged () {return _printUnchanged.isSelected();}
	  public boolean printFrameDifferences () {return _printFrameDifferences.isSelected();}
	  public int getFileSaveType() { return _diffPlugins.getSelectedIndex(); }	  

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
               JFileChooser chooser =  DisplayUtilities.createFileChooser ("Select file", ".diff");
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