
/*
 * Contributor(s): Abhita Chugh abhita@stanford.edu
*/

package edu.stanford.smi.protegex.prompt.ui.diffUI;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.util.*;

public class ChangeStatisticsDialog{

	
	public ChangeStatisticsDialog(JPanel parent){

		ModalDialog.showDialog(parent, getChangeStatisticsPanel(), "Class Change Statistics", ModalDialog.MODE_CLOSE);
	}
	

	private JComponent getChangeStatisticsPanel(){
		JPanel result = new JPanel();
		result.setLayout(new BoxLayout(result, BoxLayout.PAGE_AXIS));
		ChangeStatistics cs = new ChangeStatistics();
		Font font = new Font("Serif",Font.BOLD,18);
		result.add (new JLabel ("        Additions: "+cs.getNumberOfAdditions()+"\n")).setFont(font);
		result.add (new JLabel ("         Deletions: "+cs.getNumberOfDeletions()+"\n")).setFont(font);
		result.add (new JLabel ("               Splits: "+cs.getNumberOfSplits()+"\n")).setFont(font);
		result.add (new JLabel ("            Merges: "+cs.getNumberOfMerges()+"\n")).setFont(font);
		result.add (new JLabel ("Direct Changes: "+cs.getNumberOfDirectChanges()+"\n")).setFont(font);
		result.add (new JLabel (" Total Changes: "+cs.getTotalChanges()+"\n")).setFont(font);
		result.setPreferredSize(new Dimension(250,180));
	
		return result;
	}

}
