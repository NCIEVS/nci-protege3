package gov.nih.nci.protegex.dialog;

import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.util.MsgDialog;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.util.*;

/**
 *@Author: NGIT, Kim Ong; Iris Guo
 */

public class NoteDialog extends JDialog implements ActionListener
{
    private static final long serialVersionUID = 123456032L;
    JButton okButton, cancelButton;
	JTextField fEditorNote, fDesignNote;
	String editornote, designnote;
	NCIEditTab tab;
	String prefix;

	boolean btnPressed;

// prefix:
//    premerge: premerge_annotation
//    preretire: preretire_annotation

	public NoteDialog(NCIEditTab tab, String editornote, String designnote, String prefix){
		super((JFrame)tab.getTopLevelAncestor(), "Enter Notes", true);
		this.editornote = editornote;
		this.designnote = designnote;
		this.tab = tab;
		this.prefix = prefix;
		init();
	}

	public void init()
	{
		Container contain = this.getContentPane();
		setLocation(360,300);
		setSize(new Dimension(360,200));
		//contain.setLayout(new GridBagLayout());
		contain.setLayout(new GridLayout(3,1));

		JPanel editorPanel = new JPanel();
		JLabel editorLabel = new JLabel("Editor's Note: ");
		fEditorNote = new JTextField(30);
		fEditorNote.setText(editornote);
		editorPanel.add(editorLabel);
		editorPanel.add(fEditorNote);
		//GridBagConstraints gridbagconstraints0 = new GridBagConstraints();
		//gridbagconstraints0.gridx = 0;
		//gridbagconstraints0.gridy = 0;
		//contain.add(editorPanel, gridbagconstraints0);
		contain.add(editorPanel);

		JPanel designPanel = new JPanel();
		JLabel designLabel = new JLabel("Design Note: ");
		fDesignNote = new JTextField(30);
		fDesignNote.setText(designnote);
		designPanel.add(designLabel);
		designPanel.add(fDesignNote);
		//GridBagConstraints gridbagconstraints1 = new GridBagConstraints();
		//gridbagconstraints1.gridx = 0;
		//gridbagconstraints1.gridy = 1;
		//contain.add(designPanel, gridbagconstraints1);
		contain.add(designPanel);

		JPanel buttonPanel = new JPanel();
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		//GridBagConstraints gridbagconstraints2 = new GridBagConstraints();
		//gridbagconstraints2.gridx = 0;
		//gridbagconstraints2.gridy = 2;
		//contain.add(buttonPanel, gridbagconstraints2);
		contain.add(buttonPanel);

		this.setVisible(true);
   	}

   	public String getEditorNote()
   	{
		return editornote;
	}

   	public String getDesignNote()
   	{
		return designnote;
	}

	public boolean OKBtnPressed()
	{
		return btnPressed;
	}

	public void actionPerformed(ActionEvent event)
	{
		Object action = event.getSource();
		if (action == okButton){
			editornote = fEditorNote.getText();
			designnote = fDesignNote.getText();
			if (editornote.trim().equals("") || designnote.trim().equals(""))
			{
                MsgDialog.warning(this, "Warning", "Editor Note and Design Note are required.");
				return;
			}
			editornote = prefix + "|" + (new Date()).toString() + " - " + fEditorNote.getText().trim();
			designnote = prefix + "|" + (new Date()).toString() + " - " + fDesignNote.getText().trim();

			btnPressed = true;
			dispose();

		}else if (action == cancelButton){
			editornote = "";
			designnote = "";

			btnPressed = false;
			dispose();
		}
	}
}
