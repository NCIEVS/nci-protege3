package gov.nih.nci.protegex.util;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 * This is an abstract class that can be subclassed to create a dialog
 * that contains Yes and No buttons.
 *
 * @author David Yee
 */
public class YesNoDialog extends JDialog {
    private static final long serialVersionUID = -1057537672191672468L;

    // Constants and enums:
    public static final String YES = "Yes";
    public static final String NO = "No";
    public static final String SUPPRESS = "In the future, do not show this message";

    // Member variables:
    private JLabel _buttonL;
    private JButton _yesB;
    private JButton _noB;
    private JCheckBox _suppressCB;
    private String _message = "";
    private ArrayList<ActionListener> _actionListeners =
        new ArrayList<ActionListener>();

    /**
     * Constructs this class.
     * @param frame The parent frame.
     * @param title The dialog's title.
     * @param message The message.
     * @param displaySupressFutureMessage if true, displays the "suppress
     *   future message" JCheckBox.
     * @param width The width of the dialog.
     * @param height The height of the dialog.
     */
    public YesNoDialog(JFrame frame, String title, String message,
            boolean displaySuppressFutureMessage, int width, int height) {
        super(frame, title);
        _message = message;
        create(displaySuppressFutureMessage, width, height);
    }

    /**
     * Constructs this class.
     * @param dialog The parent dialog.
     * @param title The dialog's title.
     * @param message The message.
     * @param displaySupressFutureMessage if true, displays the "suppress
     *   future message" JCheckBox.
     * @param width The width of the dialog.
     * @param height The height of the dialog.
     */
    public YesNoDialog(JDialog dialog, String title, String message,
            boolean displaySuppressFutureMessage, int width, int height) {
        super(dialog, title);
        _message = message;
        create(displaySuppressFutureMessage, width, height);
    }

    /**
     * Creates the components of this dialog.
     * @param displaySupressFutureMessage if true, displays the "suppress
     *   future message" JCheckBox.
     * @param width The width of the dialog.
     * @param height The height of the dialog.
     */
    private void create(boolean displaySuppressFutureMessage,
            int width, int height) {
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        JComponent component = newMainComponent();
        if (component != null)
            container.add(component, BorderLayout.CENTER);
        container.add(newButtonPanel(), BorderLayout.SOUTH);
        getRootPane().setDefaultButton(_yesB);
        displaySuppressFutureMessageCheckbox(displaySuppressFutureMessage);
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(width, height);
        setModal(true);
        UIUtil.centerWithRespectToParent(this);
    }
    
    /**
     * Creates the main component for this dialog.
     * @return The main component.
     */
    protected JComponent newMainComponent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(UIManager.getIcon("OptionPane.questionIcon")),
            BorderLayout.WEST);
        
        JTextArea textArea = new JTextArea(_message);
        textArea.setEditable(false); 
        textArea.setLineWrap(true); 
        textArea.setWrapStyleWord(false);
        textArea.setOpaque(false);
        
        panel.add(textArea, BorderLayout.CENTER);
        return panel;
    }

    protected JComponent add(JPanel panel, JComponent comp,
            GridBagConstraints c) {
        panel.add(comp, c);
        return comp;
    }

    /**
     * Adds button to the panel.
     * @param panel The panel.
     * @param button The button.
     * @return the button.
     */
    protected AbstractButton add(JPanel panel, AbstractButton button) {
        button.addActionListener(new ButtonHandler());
        panel.add(button);
        return button;
    }

    /**
     * Creates the button panel.
     * @return The button panel.
     */
    private JPanel newButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(_buttonL = new JLabel(""));
        _yesB = (JButton) add(buttonPanel, new JButton(YES));
        _noB = (JButton) add(buttonPanel, new JButton(NO));

        JPanel specialPanel = new JPanel(new FlowLayout());
        _suppressCB = (JCheckBox) add(specialPanel, new JCheckBox(SUPPRESS));
        _suppressCB.setVisible(false);
        
        BorderLayout layout = new BorderLayout();
        layout.setVgap(-8);
        JPanel panel = new JPanel(layout);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(specialPanel, BorderLayout.SOUTH);
        return panel;
    }
    
    /**
     * Sets the text to the right of the buttons.
     * @param text The text.
     */
    public void setButtonLabel(String text) {
        _buttonL.setText(text);
    }

    /**
     * Defaults the Yes button.
     */
    public void defaultYesButton() {
        getRootPane().setDefaultButton(_yesB);
    }

    /**
     * Defaults the No button.
     */
    public void defaultNoButton() {
        getRootPane().setDefaultButton(_noB);
    }

    /**
     * This method is called when the user selects the Yes button.
     * @return true is there is no problems.
     */
    private boolean yes() {
        fireEvent(new ActionEvent(this, 0, YES));
        dispose();
        return true;
    }

    /**
     * This method is called when the user selects the No button.
     * @return true is there is no problems.
     */
    protected boolean no() {
        fireEvent(new ActionEvent(this, 0, NO));
        dispose();
        return true;
    }

    /**
     * Adds the listener.  This event is triggered when the user selects
     *   any of the buttons.
     * @param listener The listener.
     */
    public void addActionListener(ActionListener listener) {
        _actionListeners.add(listener);
    }

    /**
     * Removes the listener.
     * @param listener The listener.
     */
    public void removeActionListener(ActionListener listener) {
        _actionListeners.remove(listener);
    }

    /**
     * Fires an event.
     * @param event The action event.
     */
    protected void fireEvent(ActionEvent event) {
        for (ActionListener listener : _actionListeners)
            listener.actionPerformed(event);
    }

    /**
     * This class handles button events.
     */
    private class ButtonHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Object obj = e.getSource();
            if (obj == _yesB)
                yes();
            else if (obj == _noB)
                no();
        }
    }
    
    /**
     * Displays the "suppress future message" JCheckBox.
     * @param display if true, displays JCheckBox.
     */
    public void displaySuppressFutureMessageCheckbox(boolean display) {
        _suppressCB.setVisible(display);
    }
    
    /**
     * Checks the "suppress future message" JCheckBox.
     * @param suppress if true checks the "suppress future message" JCheckBox.
     */
    public void setSuppressFutureMessagesOn(boolean suppress) {
        _suppressCB.setSelected(suppress);
    }
    
    /**
     * Returns true "suppress future message" JCheckBox is selected.
     * @return true "suppress future message" JCheckBox is selected.
     */
    public boolean isSuppressFutureMessages() {
        return _suppressCB.isSelected();
    }
    
    public static class ActionHandler implements ActionListener {
        // Member Variables:
        private boolean isYes = false;
        
        /**
         * Performs the action specified by the ActionEvent.
         * @param event The ActionEvent.
         */
        public void actionPerformed(ActionEvent event) {
            isYes = event.getActionCommand().equals(YES);
        }
        
        /**
         * Returns true if Yes was selected.
         * @return true if Yes was selected.
         */
        public boolean isYes() {
            return isYes;
        }
        
        /**
         * Returns true if No was selected.
         * @return true if No was selected.
         */
        public boolean isNo() {
            return ! isYes;
        }
    }
}
