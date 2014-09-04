/*
 * Contributor(s): Sean Falconer seanf@uvic.ca
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import edu.stanford.smi.protegex.prompt.event.ProgressUpdateEvent;
import edu.stanford.smi.protegex.prompt.event.ProgressUpdateListener;
import edu.stanford.smi.protegex.prompt.event.PromptListenerManager;
import edu.stanford.smi.protegex.prompt.plugin.AlgorithmProgressMonitor;

/**
 * This dialog displays the current progress of a running algorithm.
 * 
 * @author seanf
 * @date 16-Feb-07
 */
public class AlgorithmProgressDialog extends JDialog {
	private AlgorithmProgressMonitor progressMonitor;
	
	private static final int TITLE_FONT_SIZE = 14;
    private static final int OTHER_FONT_SIZE = 12;
    
	private JButton btnCancel = new JButton("Cancel");
	private JLabel lblNote = new JLabel("");
	private JPanel pnlContent = new JPanel();
	private JLabel lblTitle = new JLabel("");
	private JProgressBar jpbProgressBar = new JProgressBar(0, 100);
	
	public AlgorithmProgressDialog() {}
	
	public AlgorithmProgressDialog(JFrame parent, AlgorithmProgressMonitor progressMonitor) {
		super(parent);
		
		this.progressMonitor = progressMonitor;
		
		this.progressMonitor.addProgressUpdateListener(new ProgressUpdateListener() {
			public void progressUpdateReceived(ProgressUpdateEvent event) {
				final AlgorithmProgressMonitor progressMonitor = event.getProgressMonitor();
				if(progressMonitor.isCompleted()) {
					disposeProgressMonitor();
				}
				else {
					if(progressMonitor.getProgressTitle().length() > 0) {
						lblTitle.setText(progressMonitor.getProgressTitle());
					}
					lblNote.setText(progressMonitor.getProgressText());
				}
			}
		});
		
		init();
	}
	
	public void disposeProgressMonitor() {
		this.setVisible(false);
		this.dispose();
	}
	
	private void init() {
		this.setMinimumSize(new Dimension(360, 200));
		this.setPreferredSize(new Dimension(360, 200));
		this.setSize(new Dimension(360, 200));

		setUndecorated(true);
		lblTitle.setSize(new java.awt.Dimension(360, 20));
		lblTitle.setLocation(new java.awt.Point(20, 20));
		lblTitle.setVisible(true);

		Font bigFont = new Font (lblTitle.getFont().getName(), Font.BOLD, TITLE_FONT_SIZE);
		Font smallFont = new Font (lblTitle.getFont().getName(), Font.PLAIN, OTHER_FONT_SIZE);
		lblTitle.setFont(bigFont);
		lblTitle.setText("Processing ...");

		lblNote.setSize(new java.awt.Dimension(360, 20));
		lblNote.setLocation(new java.awt.Point(20, 60));
		lblNote.setVisible(true);
		lblNote.setFont(smallFont);
		
		jpbProgressBar.setValue(0);
		jpbProgressBar.setSize(new Dimension(230, 20));
		jpbProgressBar.setLocation(new java.awt.Point(60, 100));
		jpbProgressBar.setStringPainted(true);
		jpbProgressBar.setString("");
		jpbProgressBar.setIndeterminate(true);
		jpbProgressBar.setVisible(true);
		
		btnCancel.setSize(new java.awt.Dimension(90, 30));
		btnCancel.setLocation(new java.awt.Point(135, 140));
		btnCancel.setVisible(true);
		
		pnlContent.setLayout(null);
		pnlContent.add(lblTitle);
		pnlContent.add(lblNote);
		pnlContent.add(jpbProgressBar);
		pnlContent.add(btnCancel);
		
		pnlContent.setPreferredSize(new Dimension(360,160));
		pnlContent.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		getContentPane().add(pnlContent, BorderLayout.CENTER);
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				btnCancel.doClick();
			}
		});
		
		btnCancel.addActionListener ( new ActionListener () {
			public void actionPerformed (ActionEvent e) {
				lblNote.setText("Cancelling...");
				
				PromptListenerManager.fireTaskComplete(true);
			}
		});
	}
}
