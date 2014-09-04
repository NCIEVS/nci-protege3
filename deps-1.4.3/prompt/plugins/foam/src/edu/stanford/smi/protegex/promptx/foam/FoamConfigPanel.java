package edu.stanford.smi.protegex.promptx.foam;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protegex.prompt.plugin.ui.*;
import edu.unika.aifb.foam.main.*;


public class FoamConfigPanel extends ComparisonAlgorithmPluginConfigurationPanel {
	private static final long serialVersionUID = 1L;
	
	private static final int MAXITERATIONS = 10;		
	private static final double MAXERROR = 0.9; 	
	private static final double CUTOFF = 0.9;  //0.25;0.31;0.35(0.7);0.9(0.95)
	private static final int EFFICIENT = 0;
	private static final int EXTERNAL = 0;
	
	private JTextField txtMaxIterations;
	private JTextField txtMaxError;
	private JTextField txtCutOff;
	private JComboBox cbStrategy;
	private JComboBox cbEfficiency;
	private JComboBox cbComparison;
	
	public FoamConfigPanel() {
		init();
	}
	
	private boolean isInteger(String s) {
		for(int i = 0; i < s.length(); i++) {
			if(!Character.isDigit(s.charAt(i))) return false;
		}
		return true;
	}
	
	private boolean isBetweenZeroAndOne(String s) {
		try {
			double value = Double.parseDouble(s);
			
			return value >= 0 && value <= 1;
		}
		catch(NumberFormatException e) {
			return false;
		}
	}
	
	public boolean hasValidConfiguration() {
		if(txtMaxIterations.getText().trim().length() == 0) {
			JOptionPane.showMessageDialog(this, "You must supply a value for the maximum number of iterations.", "Invalid input", JOptionPane.OK_OPTION);
			return false;
		}
		else if(!isInteger(txtMaxIterations.getText())) {
			JOptionPane.showMessageDialog(this, "Maximum number of iterations must be an integer.", "Invalid input", JOptionPane.OK_OPTION);
			return false;
		}
		else if(!isBetweenZeroAndOne(txtMaxError.getText())) {
			JOptionPane.showMessageDialog(this, "The maximum error must be between 0 and 1.", "Invalid input", JOptionPane.OK_OPTION);
			return false;
		}
		else if(!isBetweenZeroAndOne(txtCutOff.getText())) {
			JOptionPane.showMessageDialog(this, "The cutoff must be between 0 and 1.", "Invalid input", JOptionPane.OK_OPTION);
			return false;
		}
		
		return true;
	}
	
	private void init() {
		setLayout(new BorderLayout());
		JPanel configContainer = new JPanel(new GridLayout(3, 4));

		txtMaxIterations = new JTextField(MAXITERATIONS + "", 10);
		txtMaxError = new JTextField(MAXERROR + "", 10);		
		txtCutOff = new JTextField(CUTOFF + "", 10);
		
		// combobox values
		String[] strategy = new String[]{"Equal labels", "Only labels", "Manual Weighted", "Manual Sigmoid",
				"Machine", "Decision Tree"};
		String[] efficiency = new String[]{"Efficient", "Complete"};
		String[] comparison = new String[]{"External", "Internal"};
		
		cbStrategy = new JComboBox(strategy);
		cbStrategy.setPreferredSize(new Dimension(115, 25));
		cbStrategy.setSelectedIndex(Parameter.DECISIONTREE);
		
		cbEfficiency = new JComboBox(efficiency);
		cbEfficiency.setPreferredSize(new Dimension(115, 25));
		cbEfficiency.setSelectedIndex(EFFICIENT);
		
		cbComparison = new JComboBox(comparison);
		cbComparison.setPreferredSize(new Dimension(115, 25));
		cbComparison.setSelectedIndex(EXTERNAL);
		
		JPanel jpMaxIterations = new JPanel();		
		jpMaxIterations.add(txtMaxIterations);

		JPanel jpMaxError = new JPanel();		
		jpMaxError.add(txtMaxError);

		JPanel jpCutOff = new JPanel();		
		jpCutOff.add(txtCutOff);
		
		JPanel jpStrategy = new JPanel();		
		jpStrategy.add(cbStrategy);
		
		JPanel jpEfficiency = new JPanel();		
		jpEfficiency.add(cbEfficiency);
		
		JPanel jpComparison = new JPanel();		
		jpComparison.add(cbComparison);

		configContainer.add(getLabelPanel("Max iterations:"));
		configContainer.add(jpMaxIterations);
		configContainer.add(getLabelPanel("Max error:"));
		configContainer.add(jpMaxError);
		configContainer.add(getLabelPanel("Cut off:"));
		configContainer.add(jpCutOff);
		configContainer.add(getLabelPanel("Strategy:"));
		configContainer.add(jpStrategy);
		configContainer.add(getLabelPanel("Efficiency:"));
		configContainer.add(jpEfficiency);
		configContainer.add(getLabelPanel("Comparison:"));
		configContainer.add(jpComparison);
		
		add(configContainer, BorderLayout.WEST);
	}
	
	private JPanel getLabelPanel(String label) {
		JPanel jp = new JPanel();
		jp.add(new JLabel(label));
		
		return jp;
	}
	
	public boolean isEfficient() {
		return cbEfficiency.getSelectedIndex() == EFFICIENT;
	}
	
	public boolean isExternal() {
		return cbComparison.getSelectedIndex() == EXTERNAL;
	}
	
	public int getStrategy() {
		return cbStrategy.getSelectedIndex();
	}
	
	public int getMaxIterations() {
		return Integer.parseInt(txtMaxIterations.getText());
	}
	
	public double getMaxError() {
		return Double.parseDouble(txtMaxError.getText());
	}
	
	public double getCutOff() {
		return Double.parseDouble(txtCutOff.getText());
	}
}
