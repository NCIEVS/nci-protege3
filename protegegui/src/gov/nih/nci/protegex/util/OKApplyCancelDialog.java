package gov.nih.nci.protegex.util;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * This is an abstract class that can be subclassed to create a dialog
 * that contains OK, Apply and Cancel buttons.
 *
 * @author David Yee
 */
public abstract class OKApplyCancelDialog extends JDialog {
    // Constants and enums:
    public enum Button { OK, Apply, Cancel };
    public static final String APPLY = "Apply";
    public static final String CANCEL = "Cancel";

    // Member variables:
    protected JPanel _buttonPanel;
    protected JButton _okB;
    protected JButton _applyB;
    private JButton _cancelB;
    private ArrayList<ActionListener> _actionListeners =
        new ArrayList<ActionListener>();

    /**
     * Constructs this class.
     * @param frame The parent frame.
     */
    protected OKApplyCancelDialog(JFrame frame) {
        super(frame);
        create();
        showApplyButton(false);
        setModal(true);
    }

    /**
     * Constructs this class.
     * @param dialog The parent dialog.
     */
    protected OKApplyCancelDialog(JDialog dialog) {
        super(dialog);
        create();
    }

    /**
     * Creates the components of this dialog.
     */
    private void create() {
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        JComponent component = newMainComponent();
        if (component != null)
            container.add(component, BorderLayout.CENTER);
        _buttonPanel = newButtonPanel();
        container.add(_buttonPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(_okB);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    /**
     * Creates the main component for this dialog.
     * @return The main component.
     */
    protected abstract JComponent newMainComponent();

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
    protected JButton add(JPanel panel, JButton button) {
        button.addActionListener(new ButtonHandler());
        panel.add(button);
        return button;
    }

    /**
     * Creates the button panel.
     * @return The button panel.
     */
    private JPanel newButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        _okB = add(panel, new JButton("OK"));
        _applyB = add(panel, new JButton("Apply"));
        _cancelB = add(panel, new JButton("Cancel"));
        return panel;
    }
    
    /**
     * Show or hides the Apply button.
     * @param show If true, shows Apply button.
     */
    public void showApplyButton(boolean show) {
        _applyB.setVisible(show);
    }

    /**
     * This method is called when the user selects the OK button.
     * @return true is there is no problems.
     */
    private boolean ok() {
        if (! apply())
            return false;

        dispose();
        return true;
    }

    /**
     * This method is called when the user selects the Apply button.
     * @return true is there is no problems.
     */
    protected boolean apply() {
        fireEvent(new ActionEvent(this, 0, APPLY));
        return true;
    }

    /**
     * This method is called when the user selects the Cancel button.
     * @return true is there is no problems.
     */
    protected boolean cancel() {
        dispose();
        fireEvent(new ActionEvent(this, 0, CANCEL));
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
            if (obj == _okB)
                ok();
            else if (obj == _applyB)
                apply();
            else if (obj == _cancelB)
                cancel();
        }
    }
}
