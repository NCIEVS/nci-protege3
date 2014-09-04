 /*
  * Contributor(s): Sean Falconer seanf@uvic.ca
  */

package edu.stanford.smi.protegex.prompt.plugin.util.mapping;

import java.awt.Component;

import javax.swing.JTabbedPane;

import edu.stanford.smi.protegex.prompt.plugin.util.SelectableContainer;
import edu.stanford.smi.protegex.prompt.plugin.util.SuggestionPluginUtility;
import edu.stanford.smi.protegex.prompt.ui.MappingTabComponent;
import edu.stanford.smi.protegex.prompt.ui.TargetDisplayPane;

/**
 * Utility class for extending parts of the mapping user interface.
 * @author seanf
 *
 */
public abstract class MapPluginFacade {
	public static final int UI_UTILITY_SUGGESTION = 0;
	           
	private static SelectableContainer suggestionPluginUtility = new SuggestionPluginUtility();
	   
	/**
	 * Adds a tab to the target display tab control.
	 * @param tabContainer
	 * @param tabTitle
	 */
	public static void addTargetDisplayTab(Component tabContainer, String tabTitle) {
		TargetDisplayPane targetKbPane = MappingTabComponent.getTargetKbPane();
		targetKbPane.addTab(tabTitle, tabContainer);
	}
	           
	/**
	 * Adds a tab to the source display tab control.
	 * @param tabContainer
	 * @param tabTitle
	 */
	public static void addSourceDisplayTab(Component tabContainer, String tabTitle) {
		TargetDisplayPane sourceKbPane = MappingTabComponent.getSourceKbPane();
		sourceKbPane.addTab(tabTitle, tabContainer);     
	}
	
	/**
	 * Adds a tab to the mapping display tab control.
	 * @param tabContainer
	 * @param tabTitle
	 */
	public static void addMappingDisplayTab(Component tabContainer, String tabTitle) {
		JTabbedPane tabbedPane = MappingTabComponent.getMappingPane();
		tabbedPane.addTab(tabTitle, tabContainer);
	}
	
	//public static void addPanelToSourceDisplay(Component panel) {
	//	TargetDisplayPane sourceKbPane = MappingTabComponent.getSourceKbPane();
	//}
	
	public static SelectableContainer getSelectableContainer(int pluginUtilityType) {
		switch(pluginUtilityType) {
			case UI_UTILITY_SUGGESTION:
				return suggestionPluginUtility;
		}
		
		return null;
	}
}
