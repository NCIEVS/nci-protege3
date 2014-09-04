package edu.stanford.smi.protegex.prompt.plugin.model;

import java.awt.Dimension;

import javax.swing.JComponent;

/**
 * Top level UI perspective interface. Instances of this interface must implement their own content
 * pane that will replace the default Prompt content pane.
 * 
 * @author seanf
 */
public interface PromptUIPerspective extends PromptUIPlugin {
	public JComponent createContentPane(Dimension size);
}
