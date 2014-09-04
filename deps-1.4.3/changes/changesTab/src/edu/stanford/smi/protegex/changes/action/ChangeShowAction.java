package edu.stanford.smi.protegex.changes.action;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JTable;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protegex.changes.ChangeTableModel;
import edu.stanford.smi.protegex.changes.ui.JTreeTable;

public class ChangeShowAction implements MouseListener{

	JTreeTable cTable;
	ChangeTableModel cTableModel;
	Project changes;
	
	public ChangeShowAction(JTreeTable cTable, ChangeTableModel cTableModel, Project changes) {
		this.cTable = cTable;
		this.cTableModel = cTableModel;
		this.changes = changes;
	}
	
	// Show change instance on double click in change table
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount()==2) {
			int index = cTable.getSelectedRow();
			Instance changeInst = (Instance)cTableModel.getObjInRow(index);
			JFrame changeAction = changes.show(changeInst);
			changeAction.addWindowListener(new WindowListener() {
				
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
			changeAction.setVisible(true);
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
