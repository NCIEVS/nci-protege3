 /*
  * Contributor(s): Sean Falconer seanf@uvic.ca
  */

package edu.stanford.smi.protegex.prompt.plugin.util.merging;

import java.awt.Component;

import javax.swing.JTabbedPane;

import edu.stanford.smi.protegex.prompt.plugin.util.SelectableContainer;
import edu.stanford.smi.protegex.prompt.plugin.util.SuggestionPluginUtility;
import edu.stanford.smi.protegex.prompt.ui.MappingTabComponent;
import edu.stanford.smi.protegex.prompt.ui.MergingTabComponent;
import edu.stanford.smi.protegex.prompt.ui.TargetDisplayPane;

/**
 * Utility class for extending parts of the mapping user interface.
 * @author seanf
 *
 */
public abstract class MergePluginFacade {
	public static final int UI_UTILITY_SUGGESTION = 0;
	
	private static SelectableContainer suggestionPluginUtility = new SuggestionPluginUtility();

	/**
	 * Adds a tab to the operations tab control where Suggestions, Conflicts and New Operations exist.
	 * @param tabContainer
	 * @param tabTitle
	 */
	public static void addOperationDisplayTab(Component tabContainer, String tabTitle) {
		JTabbedPane operationPane = MergingTabComponent.getWorkingPane();
		operationPane.addTab(tabTitle, tabContainer);
	}
	
	/**
	 * Adds a tab to the result tab container, where Result classes, Result slots, etc exist.
	 * @param tabContainer
	 * @param tabTitle
	 */
	public static void addResultDisplayTab(Component tabContainer, String tabTitle) {
		JTabbedPane resultPane = MergingTabComponent.getTargetPane();
		resultPane.addTab(tabTitle, tabContainer);
	}
	
	/**
	 * Gets the selectable container based on the utility type.
	 * @param pluginUtilityType
	 * @return
	 */
	public static SelectableContainer getSelectableContainer(int pluginUtilityType) {
		switch(pluginUtilityType) {
			case UI_UTILITY_SUGGESTION:
				return suggestionPluginUtility;
		}
		
		return null;
	}
}
