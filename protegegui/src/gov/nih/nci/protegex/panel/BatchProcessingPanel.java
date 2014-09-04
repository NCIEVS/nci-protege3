/**
 * 
 */
package gov.nih.nci.protegex.panel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import edu.stanford.smi.protege.util.Log;

import gov.nih.nci.protegex.batch.BatchProcessingDialog;
import gov.nih.nci.protegex.edit.*;

/**
 * @author bitdiddle
 *
 */
public class BatchProcessingPanel extends JPanel implements ActionListener,
		BatchPanel, PanelDirty {
	
	public static final long serialVersionUID = 123456221L;
	
	private NCIEditTab tab = null;
	
	JTextArea loaderTextArea = null;
	
	public JTextArea getTextArea() {
		return loaderTextArea;
	}
	
	private JButton inputButton, saveButton, clearButton;
	
	public boolean isDirty() {
		return this.clearButton.isEnabled();
	}
	
	public void reset() {
		// currently noop
	}
	
	public BatchProcessingPanel(NCIEditTab t) {
		super(new BorderLayout());
		tab = t;		
		init();
	}
	
	private void init() {
		JTextField fileName = new JTextField();
		fileName.setColumns(45);

		inputButton = new JButton("Input");
		inputButton.addActionListener(this);

		saveButton = new JButton("Save");
		saveButton.addActionListener(this);

		clearButton = new JButton("Clear");
		clearButton.addActionListener(this);

		JPanel textAreaPanel = new JPanel(new BorderLayout());
		loaderTextArea = new JTextArea(25, 45);
		loaderTextArea.setEditable(false);

		loaderTextArea.setTabSize(2);
		JPanel buttonPanel = new JPanel();

		buttonPanel.add(inputButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(clearButton);

		textAreaPanel.add(new JScrollPane(loaderTextArea), BorderLayout.CENTER);
		textAreaPanel.add(buttonPanel, BorderLayout.SOUTH);

		Box box = new Box(BoxLayout.Y_AXIS);
		box.add(textAreaPanel);

		textAreaPanel.setBorder(BorderFactory.createTitledBorder("Log"));

		
		add(box);

		inputButton.setEnabled(true);
		saveButton.setEnabled(false);
		clearButton.setEnabled(false);

		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == inputButton) {

			try {
				new BatchProcessingDialog(this, tab);

			} catch (Exception ex) {
				Log.getLogger().log(Level.WARNING, "Exception caught", ex);

			}
		}

		else if (e.getSource() == clearButton) {
			clearButton.setEnabled(false);
			inputButton.setEnabled(true);
			saveButton.setEnabled(false);
			loaderTextArea.setText("");
		}

		else if (e.getSource() == saveButton) {
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				tab.writeToFile(loaderTextArea, fc.getSelectedFile());
				return;
			}
		}
		// TODO Auto-generated method stub

	}

	
	
	public void enableButton(String buttonLabel, boolean state) {
		if (buttonLabel.compareTo("inpurButton") == 0)
			inputButton.setEnabled(state);
		else if (buttonLabel.compareTo("saveButton") == 0)
			saveButton.setEnabled(state);
		else if (buttonLabel.compareTo("clearButton") == 0)
			clearButton.setEnabled(state);

	}

}
