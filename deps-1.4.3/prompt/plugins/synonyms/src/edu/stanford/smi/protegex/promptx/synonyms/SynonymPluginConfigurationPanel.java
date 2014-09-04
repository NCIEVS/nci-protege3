/*
 * Contributor(s): Natasha Noy noy@stanford.edu
 */
package edu.stanford.smi.protegex.promptx.synonyms;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.plugin.ui.*;

public class SynonymPluginConfigurationPanel extends ComparisonAlgorithmPluginConfigurationPanel{
	private JCheckBox _compareSourcesCheckBox;
	private JCheckBox _approximateMatchCheckBox;
	private JTextField _sourceSynonymSlot;
	private JTextField _targetSynonymSlot;
	
	
	public SynonymPluginConfigurationPanel () {
		setLayout (new BorderLayout ());
		JPanel insidePanel = new JPanel ();
		insidePanel.setLayout(new GridLayout (3, 0));
		insidePanel.add (synonymSlotsPanel ());
		insidePanel.add (compareSourcesCheckBox ());
		insidePanel.add (approximateMatchCheckBox ());
		add (insidePanel, BorderLayout.WEST);
		
	}

	public boolean compareSources () {
		return _compareSourcesCheckBox.isSelected();
	}
	
	public boolean approximateMatch () {
		return _approximateMatchCheckBox.isSelected();
	}
	
	public String sourceSynonymSlot () {
		if (_sourceSynonymSlot.getText().equals("")) return null;
		return _sourceSynonymSlot.getText();
	}
	
	public String targetSynonymSlot () {
		if (_targetSynonymSlot.getText().equals("")) return null;
		return _targetSynonymSlot.getText();
	}
	
	private JPanel synonymSlotsPanel () {
		JPanel result = new JPanel();
		result.setLayout(new GridLayout (0, 2, 10, 0));
//		new BoxLayout(result, BoxLayout.X_AXIS);
		_sourceSynonymSlot = ComponentFactory.createTextField("synonyms");
		_targetSynonymSlot = ComponentFactory.createTextField("synonyms");
		LabeledComponent source = new LabeledComponent ("Synonym slot in source", _sourceSynonymSlot);
		LabeledComponent target = new LabeledComponent ("Synonym slot in target", _targetSynonymSlot);
		result.add(source);
		result.add(target);
		return result;
	}
	
	private JCheckBox compareSourcesCheckBox () {
		_compareSourcesCheckBox = ComponentFactory.createCheckBox("Compare sources (can be slow if ontologies are very large)");
		_compareSourcesCheckBox.setSelected(true);
		return _compareSourcesCheckBox;
	}
	
	private JCheckBox approximateMatchCheckBox () {
		_approximateMatchCheckBox = ComponentFactory.createCheckBox("Approximate match for names (slower)");
		_approximateMatchCheckBox.setSelected(true);
		return _approximateMatchCheckBox;
	}
	

}
