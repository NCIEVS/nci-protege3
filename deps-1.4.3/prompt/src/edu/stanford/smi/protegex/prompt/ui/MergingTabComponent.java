/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Collection;

import edu.stanford.smi.protegex.prompt.actionLists.SuggestionsAndConflicts;

public class MergingTabComponent extends TabComponent {

	public static final int LEFT_FRAME = 1;
	public static final int RIGHT_FRAME = 2;

	public MergingTabComponent(Dimension size, int pluginPerspectiveType) {
		super(size, pluginPerspectiveType);
	}

	public void revalidateTarget() {
		_target.revalidateClassDisplay();
	}

	public void conflictsListChanged(boolean changed) {
		if (changed && _conflictsPane != null) {
			_conflictsPane.postChange();
		}
		setConflictsPaneColor();
	}

	private static Color _defaultTabTitleColor = null;
	private static int _conflictsTabPosition;

	private void setConflictsPaneColor() {
		if (_defaultTabTitleColor == null && _workingPane != null) {
			_conflictsTabPosition = _workingPane.indexOfTab(_conflictsTabTitle);
			Color _defaultTabTitleColor = _workingPane.getForegroundAt(_workingPane.indexOfTab(_createTabTitle));
		}
		if (_conflictsTabPosition == -1) {
			return;
		}

		Collection currentConflicts = SuggestionsAndConflicts.getConflictsList();
		if (currentConflicts == null || currentConflicts.size() == 0) {
			_workingPane.setForegroundAt(_conflictsTabPosition, _defaultTabTitleColor);
		} else {
			_workingPane.setForegroundAt(_conflictsTabPosition, Color.red);
		}
	}

	public String toString() {
		return "MergingTabComponent";
	}

}
