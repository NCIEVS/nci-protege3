

package gov.nih.nci.protegex.dialog;

import edu.stanford.smi.protege.util.FileField;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import gov.nih.nci.protegex.panel.*;
import gov.nih.nci.protegex.edit.OWLWrapper;
import gov.nih.nci.protegex.util.MsgDialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;


public class ReportWriterDialog extends JDialog implements ActionListener
{
    private static final long serialVersionUID = 123456034L;

    boolean btnPressed;

	int windowClosingTag=0;

	JButton fInputButton, fOutputButton;
	JButton continueButton, cancelButton;

	JTextField fInputFile, fOutputFile;

    ReportWriterPanel tab;
    OWLModel owlModel;

	String infile;// = fInputTf.getText();
	String outfile;// = fOutputFile.getText();

	OWLWrapper wrapper = null;
	OWLClass selectedCls;

	FileField outputFileField = null;
	JComboBox levelComboBox;
	JRadioButton yesRadio;
	JRadioButton noRadio;

	boolean withAttributes = true;

	public ReportWriterDialog(ReportWriterPanel tab, OWLClass kb)
	{
		super((JFrame)tab.getTopLevelAncestor(), "Report Writer", true);

		this.tab = tab;
		this.selectedCls = kb;
		//this.owlModel = kb;
		//this.wrapper = tab.getWrapper();

		this.infile = "";
		this.outfile = "";

		initialize();
	}


    public File getOutputFile()
    {
		return outputFileField.getFilePath();
	}

    public boolean getWithAttributes()
    {
		return withAttributes;
	}

	public void initialize()
	{
		Container container = this.getContentPane();
		//setLocation(360,300);
		setLocation(450,300);
		setSize(new Dimension(450,240));
		container.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

		String label = "Output File";
		String path = "";
		String extension = "txt";
		String description = "";
		outputFileField = new FileField(label, path, extension, description);
		//Note: use public File getFilePath() to get the file path

		inputPanel.add(outputFileField, BorderLayout.NORTH);

        JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BorderLayout());
		JTextField rootConcept = new JTextField();
		if (selectedCls !=null){
			rootConcept.setText(selectedCls.getPrefixedName());
		}
		rootPanel.add(rootConcept, BorderLayout.CENTER);

		LabeledComponent lc2
		   = new LabeledComponent("Root Concept", rootPanel);

		inputPanel.add(lc2, BorderLayout.CENTER);

		String[] levels = new String[12];
		levels[0] = "All";
		for (int i=1; i<levels.length; i++)
		{
			Integer int_obj = new Integer(i-1);
			levels[i] = int_obj.toString();
		}

		levelComboBox = new JComboBox(levels);
		levelComboBox.setSelectedIndex(0);

		LabeledComponent lc3
		   = new LabeledComponent("Hierarchy Level", levelComboBox);

		inputPanel.add(lc3, BorderLayout.SOUTH);


		container.add(inputPanel, BorderLayout.NORTH);


		ButtonGroup yesnoGroup = new ButtonGroup();
		JPanel yesnoPanel = new JPanel();
		
		yesRadio = new JRadioButton("Yes");
		yesRadio.setSelected(true);
		yesRadio.addActionListener(this);

		noRadio = new JRadioButton("No");
		noRadio.addActionListener(this);
		yesnoGroup.add(yesRadio);
		yesnoGroup.add(noRadio);
		yesnoPanel.add(yesRadio);
		yesnoPanel.add(noRadio);


		LabeledComponent lc4
		   = new LabeledComponent("With Attributes", yesnoPanel);
		container.add(lc4, BorderLayout.CENTER);

		continueButton = new JButton("Continue");
		continueButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		JPanel okcancelPanel = new JPanel();
		okcancelPanel.add(continueButton);
		okcancelPanel.add(cancelButton);

        container.add(okcancelPanel, BorderLayout.SOUTH);

        pack();

        tab.enableReportButton(false);
		setVisible(true);

	}


    public boolean getOKBtnPressed()
    {
		return btnPressed;
	}

    public int getLevel()
    {
		int level = levelComboBox.getSelectedIndex();
		return level-1;
	}

	public void actionPerformed(ActionEvent event)
	{
		Object action = event.getSource();
		if (action == continueButton){
			File outputFile = getOutputFile();
			if (outputFile == null)
			{
				MsgDialog.warning(this, "Input and output files are required.");
				return;
			}

			btnPressed = true;
			tab.enableReportButton(true);
			dispose();

		}else if (action == cancelButton){
			btnPressed = false;
			tab.enableReportButton(true);
			dispose();
		}else if (action == yesRadio){
            withAttributes = true;
		}else if (action == noRadio){
            withAttributes = false;
		}
	}

	public void beginOutput()
	{
		continueButton.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		continueButton.removeActionListener(this);
		System.out.println("Report writer in progress. Please wait...");
	}

}



