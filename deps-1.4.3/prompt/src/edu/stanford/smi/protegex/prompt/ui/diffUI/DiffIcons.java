/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt.ui.diffUI;

import javax.swing.*;

import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.ui.*;

public class DiffIcons {
	private static String _metaclassPrefix = "Metaclass";
	private static String _ending = ".gif";
	private static String _definedOWLClsPrefix = "DefinedOWL";

//	public static Icon getTreeAddedIcon (boolean metaclass) {
//		return new DecoratedIcon (Icons.getClsIcon(), 
//				 ComponentUtilities.loadImageIcon(TabComponent.class, "images/treeAdded" + _ending),
//				 DecoratedIcon.RIGHT. DecoratedIcon.BOTTOM);
//	}

	public static Icon getTreeAddedIcon (boolean metaclass, boolean defined) {
		return getIconFromFile("TreeAdded", metaclass, defined);
	}

	public static Icon getTreeDeletedIcon (boolean metaclass, boolean defined) {
		return getIconFromFile("TreeDeleted", metaclass, defined);
	}

	public static Icon getTreeChangedIcon (boolean metaclass, boolean defined) {
		return getIconFromFile("TreeBold", metaclass, defined);
	}
	
	public static Icon getTreeMovedToIcon (boolean metaclass, boolean defined) {
		return getIconFromFile("TreeMovedTo", metaclass, defined);
	}
	
	public static Icon getTreeMovedFromIcon (boolean metaclass, boolean defined) {
		return getIconFromFile("TreeGray", metaclass, defined);
	}
	
	public static Icon getTreeWithWarningIcon (boolean metaclass, boolean defined) {
		return getIconFromFile("ClassWithWarning", metaclass, defined);
	}
	
	
	public static Icon getRegularTreeIcon (boolean metaclass, boolean defined) {
		return getIconFromFile("Class", metaclass, defined);
	}
		
	private static Icon getIconFromFile (String fileName, boolean metaclass, boolean defined) {
		String prefix = "";
		if (defined)
			prefix += _definedOWLClsPrefix;
		else if (metaclass)
			prefix += _metaclassPrefix;
		return ComponentUtilities.loadImageIcon(TabComponent.class, "images/" + prefix + fileName + _ending);
	}

}
