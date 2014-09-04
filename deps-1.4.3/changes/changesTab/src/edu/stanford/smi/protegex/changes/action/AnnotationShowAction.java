package edu.stanford.smi.protegex.changes.action;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JTable;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protegex.changes.AnnotationTableModel;

public class AnnotationShowAction implements MouseListener{

	private JTable aTable;
	private AnnotationTableModel aTableModel;
	private Project changes;
	
	
	public AnnotationShowAction(JTable aTable, AnnotationTableModel aTableModel, Project changes) {
		this.aTable = aTable;
		this.aTableModel = aTableModel;
		this.changes = changes;
	}

	// Show annotation instance on double click in annotation table
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount()==2) {
			int index = aTable.getSelectedRow();
			Instance changeInst = (Instance)aTableModel.getObjInRow(index);
			JFrame annotateAction = changes.show(changeInst);
			annotateAction.addWindowListener(new WindowListener() {
				
				public void windowClosed(WindowEvent arg0) {
				}

				public void windowClosing(WindowEvent arg0) {
				}
				
				public void windowOpened(WindowEvent arg0) {
				}
				
				public void windowIconified(WindowEvent arg0) {
				}

				public void windowDeiconified(WindowEvent arg0) {
				}
				
				public void windowActivated(WindowEvent arg0) {
				}
				
				public void windowDeactivated(WindowEvent arg0) {
				}
			});
			annotateAction.setVisible(true);
		}
	}

	public void mousePressed(MouseEvent arg0) {
	}

	public void mouseReleased(MouseEvent arg0) {
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}
}
