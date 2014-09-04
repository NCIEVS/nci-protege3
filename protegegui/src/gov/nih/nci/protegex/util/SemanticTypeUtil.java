package gov.nih.nci.protegex.util;

import edu.stanford.smi.protege.model.Cls;
import gov.nih.nci.protegex.tree.TreeItem;
import gov.nih.nci.protegex.tree.TreeItems;

import java.util.prefs.Preferences;

import javax.swing.JComponent;

/**
 * This utility class handles Semantic Type property.
 * @author David Yee
 */
public class SemanticTypeUtil {
    // Constant:
    private static final String SUPPRESS_MESSAGE = 
        "semantic_type_supress_message"; 
    
    // Member Variables:
    private static Preferences _preferences = 
        Preferences.userNodeForPackage(SemanticTypeUtil.class);
    private static boolean _suppressMessage = 
        _preferences.getBoolean(SUPPRESS_MESSAGE, false);
    
    public static final String SEMANTICTYPE = "Semantic_Type";

    /**
     * Returns true if SemanticType property is set.
     * @param finalState The finalState vector.
     * @return true if SemanticType property is set.
     */
    public static boolean isPropertySet(TreeItems finalState) {
        int n = finalState.size();
        for (int i=0; i<n; ++i) {
            TreeItem item = finalState.elementAt(i);
            if (item.getName().equals(SEMANTICTYPE))
                return true;
        }
        return false;
    }
    
    /**
     * Sets the suppress message flag.
     * @param suppress if true, suppress the SemanticType message.
     */
    public static void setSuppressMessage(boolean suppress) {
        _suppressMessage = suppress;
        _preferences.putBoolean(SUPPRESS_MESSAGE, _suppressMessage);
    }
    
    /**
     * Returns true if suppress SemanticType message is turned on. 
     * @return true if suppress SemanticType message is turned on.
     */
    public static boolean isSuppressMessage() {
        return _suppressMessage;
    }
    
    /**
     * Displays the "Not Set, Continue?" message when the suppress
     * flag is set to false.
     * @param parent The parent component.
     * @param cls The concept class.
     * @return true if the user wants to continue, else false.
     */
    public static boolean notSet_Continue(JComponent parent, Cls cls) {
        if (_suppressMessage)
            return true;
        
        // TODO: Dave if you call get ancestor it might be null?
        //        If so YesNoDialog chokes
        YesNoDialog dialog = new YesNoDialog(
            UIUtil.getFrame(parent), 
            cls.getBrowserText(), "Semantic_Type value is not set.",
            true, 350, 125);
        dialog.setButtonLabel("Continue?");
        dialog.setSuppressFutureMessagesOn(_suppressMessage);
        YesNoDialog.ActionHandler handler = new YesNoDialog.ActionHandler();
        dialog.addActionListener(handler);
        dialog.setVisible(true);
        setSuppressMessage(dialog.isSuppressFutureMessages());
        return handler.isYes();
    }
}
