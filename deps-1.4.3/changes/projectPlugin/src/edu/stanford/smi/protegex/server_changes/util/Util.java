package edu.stanford.smi.protegex.server_changes.util;

import java.util.ArrayList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.stanford.smi.protege.model.CommandManager;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.framestore.undo.MacroCommand;
import edu.stanford.smi.protege.model.framestore.undo.UndoFrameStore;
import edu.stanford.smi.protegex.owl.model.OWLModel;

public class Util {



	private static String OWL_KB_INDICATOR = "OWL";
	
	private Util() {}
	
	public static boolean kbInOwl(KnowledgeBase kb) {
	    return kb instanceof OWLModel;
	}

	// Undo framestore possible use
	private static void ufsReg(KnowledgeBase currKB) {

		currKB.getCommandManager();
		CommandManager cm = currKB.getCommandManager();
		cm.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent arg0) {
				UndoFrameStore ufs = (UndoFrameStore) arg0.getSource();
				ArrayList doneCmds = new ArrayList(ufs.getDoneCommands());
				Object element = doneCmds.get(doneCmds.size()-1);
				
				if (element instanceof MacroCommand) {
					MacroCommand mc = (MacroCommand) element;
					//lastMacroCmd = mc.getDescription();
				}
			}
			
		});
			 
	}
}
