package gov.nih.nci.protegex.util;

import java.awt.Component;

import javax.swing.JOptionPane;

/**
 * Contains utility methods for displaying dialog messages.
 *
 * @author David Yee
 */
public class MsgDialog {
    /** No response. */
    public static int NO_OPTION = JOptionPane.NO_OPTION;

    /** Yes response. */
    public static int YES_OPTION = JOptionPane.YES_OPTION;

    /**
     * Prompts the user with a message and returns a yes or no response.
     * @param parent Parent component.
     * @param title Message title.
     * @param message Message.
     * @return the user's yes or no response.
     */
    public static int yesOrNoOrig(Component parent, String title, String message) {
        Object[] options = { "Yes", "No" };
        return JOptionPane.showOptionDialog(parent, message, title,
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);
    }

    /**
     * Prompts the user with a message and returns a yes or no response.
     * @param parent Parent component.
     * @param title Message title.
     * @param message Message.
     * @return the user's yes or no response.
     */
    public static int yesOrNo(Component parent, String title, String message) {
        return JOptionPane.showConfirmDialog(parent, message, title,
                JOptionPane.YES_NO_OPTION);
    }

    /**
     * Prompts the user with a message and returns a yes or no response.
     * @param parent Parent component.
     * @param message Message.
     * @return the user's yes or no response.
     */
    public static int yesOrNo(Component parent, String message) {
        return yesOrNo(parent, "Confirmation", message);
    }

    /**
     * Displays an information message to the user.
     * @param parent Parent component.
     * @param title Message title.
     * @param message Message.
     * @param messageType type of message.  This affects how the
     *   icon is displayed.
     */
    public static void showMessage(Component parent, String title, 
        String message, int messageType) {
        Object[] options = { "OK" };
        JOptionPane.showOptionDialog(parent, message, title,
                JOptionPane.DEFAULT_OPTION, messageType,
                null, options, options[0]);
    }

    /**
     * Displays an information message to the user.
     * @param parent Parent component.
     * @param title Message title.
     * @param message Message.
     */
    public static void ok(Component parent, String title, String message) {
        showMessage(parent, title, message, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Displays an information message to the user.
     * @param parent Parent component.
     * @param message Message.
     */
    public static void ok(Component parent, String message) {
        ok(parent, "Confirmation", message);
    }

    /**
     * Displays a warning message to the user.
     * @param parent Parent component.
     * @param title Message title.
     * @param message Message.
     */
    public static void warning(Component parent, String title, String message) {
        showMessage(parent, title, message, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Displays a warning message to the user.
     * @param parent Parent component.
     * @param message Message.
     */
    public static void warning(Component parent, String message) {
        warning(parent, "Warning", message);
    }

    /**
     * Displays an error message to the user.
     * @param parent Parent component.
     * @param title Message title.
     * @param message Message.
     */
    public static void error(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title,
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Displays an error message to the user.
     * @param parent Parent component.
     * @param message Message.
     */
    public static void error(Component parent, String message) {
        error(parent, "Error", message);
    }

    /**
     * Displays a debug message to the user.
     * @param parent Parent component.
     * @param title Message title.
     * @param message Message.
     */
    public static void debug(Component parent, String title, String message) {
        ok(parent, title, message);
    }

    /**
     * Displays a debug message to the user.
     * @param parent Parent component.
     * @param message Message.
     */
    public static void debug(Component parent, String message) {
        warning(parent, "Debug", message);
    }
}
