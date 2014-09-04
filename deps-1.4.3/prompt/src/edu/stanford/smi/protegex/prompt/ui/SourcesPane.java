 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.*;
import java.lang.reflect.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;

public class SourcesPane extends JTabbedPane implements ShowSourcesInterface {
  static private final int CLSES_PANEL = 0;
  static private final int SLOTS_PANEL = 1;
  static private final int INSTANCES_PANEL = 2;

  static private boolean _allowSelection = true;
  static private boolean _isSource = false;
  private Dimension _size;

  public SourcesPane (Dimension size) {
  	initialize (size);
  }

  public SourcesPane (Dimension size, boolean allowSelection) {
    _allowSelection = allowSelection;
    _isSource = true;
  	initialize (size);
  }

//   	JTabbedPane pane = ComponentFactory.createTabbedPane(true);
  private void initialize (Dimension size) {
	  _size = size;
	  JPanel clsesPanel = ComponentFactory.createPanel ();
	  clsesPanel.setLayout(new BorderLayout ());
	  clsesPanel.add ( createClsesPane() , BorderLayout.CENTER);
	  add (clsesPanel, CLSES_PANEL);
	  setTitleAt (CLSES_PANEL, "Source classes");
	  
	  JPanel slotsPanel = ComponentFactory.createPanel ();
	  slotsPanel.setLayout(new BorderLayout ());
	  slotsPanel.add ( createSlotsPane() , BorderLayout.CENTER);
	  add (slotsPanel, SLOTS_PANEL);
	  setTitleAt (SLOTS_PANEL, "Source slots");
	  
	  JPanel instancesPanel = ComponentFactory.createPanel ();
	  instancesPanel.setLayout(new BorderLayout ());
	  
	  instancesPanel.add ( createInstancesPane() , BorderLayout.CENTER);
	  add (instancesPanel, INSTANCES_PANEL);
	  setTitleAt (INSTANCES_PANEL, "Source instances");
	  
	  setSelectedIndex(0);
  }

  public Class getSelectedTabFrameType () {
  	switch (getSelectedIndex()) {
     	case CLSES_PANEL:
        	return Cls.class;
        case SLOTS_PANEL:
        	return Slot.class;
        case INSTANCES_PANEL:
        default:
        	return Instance.class;
    }
  }


  private JComponent createClsesPane () {
    return createSourcesPane (SourceClsesPane.class);
  }

  private JComponent createSlotsPane () {
    return createSourcesPane (SourceSlotsPane.class);
  }

  private JComponent createInstancesPane () {
    return createSourcesPane (SourceInstancesPane.class);
  }

  private JComponent createSourcesPane (Class paneCls) {
    KnowledgeBaseInMerging [] kbs = ProjectsAndKnowledgeBases.getSourceKnowledgeBasesInMerging();
    try {
    SourceFramesPane sources [] = new SourceFramesPane [kbs.length];
  	Constructor constructor = paneCls.getConstructor
    				(new Class [] {Dimension.class, KnowledgeBaseInMerging.class, Boolean.class, Boolean.class});
    for (int i = 0; i < sources.length; i++) {
		KnowledgeBaseInMerging kbi = (KnowledgeBaseInMerging)kbs[sources.length - i - 1];  // reverse the order of the knowledge bases.
    	sources[i] = (SourceFramesPane)constructor.newInstance
    				(new Object [] {_size, kbi, new Boolean (_allowSelection), new Boolean (_isSource) });
	}

    if (kbs.length == 1) {
    	JPanel sourcesPane = new JPanel ();
        sourcesPane.setLayout(new BorderLayout ());
        sourcesPane.add (sources[0], BorderLayout.CENTER);
        return sourcesPane;
    } else {
		JSplitPane sourcesSplitPane = ComponentFactory.createLeftRightSplitPane(false);
    	sourcesSplitPane.setLeftComponent (sources[0]);
  		sourcesSplitPane.setRightComponent (sources[1]);
    	sourcesSplitPane.setDividerLocation(_size.width/2);
    	return sourcesSplitPane;
    }
    } catch (InvocationTargetException e) {
        e.getTargetException().printStackTrace();
    	return null;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public void selectTab (Frame frame) {
   	if (frame instanceof Cls)
		setSelectedIndex (CLSES_PANEL);
    else if (frame instanceof Slot)
    	setSelectedIndex (SLOTS_PANEL);
    else if (frame instanceof Instance)
    	setSelectedIndex (INSTANCES_PANEL);
  }

  public void unselectAll () {
  	ComponentUtilities.applyToDescendents(this, new UnaryFunction () {
		public Object apply (Object o) {
            if (o instanceof SourceFramesPane)
            	((SourceFramesPane)o).unselect();
            return null;
        }
    });
  }

  public String toString () {
    return "SourcesPane";
  }

}
