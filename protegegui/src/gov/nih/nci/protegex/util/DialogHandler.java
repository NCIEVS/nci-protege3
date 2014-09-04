package gov.nih.nci.protegex.util;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;

public class DialogHandler extends OKApplyCancelDialog {
	private static final long serialVersionUID = 5399757302427502804L;

	private boolean _applySuccessful = true;
    
    public DialogHandler(JFrame frame, JComponent comp) {
        super(frame);
        addMainComponent(comp);
    }

    public DialogHandler(JDialog dialog, JComponent comp) {
        super(dialog);
        addMainComponent(comp);
    }
    
    public void setup(String title, int width, int height) {
        setTitle(title);
        setSize(width, height);
        UIUtil.centerWithRespectToParent(this);
    }

    protected JComponent newMainComponent() {
        return null;
    }
    
    protected void addMainComponent(JComponent comp) {
        Container container = getContentPane();
        container.add(comp, BorderLayout.CENTER);
    }
    
    protected boolean apply() {
        fireEvent(new ActionEvent(this, 0, APPLY));
        return _applySuccessful;
    }
    
    public boolean getApplySuccessful() {
        return _applySuccessful;
    }

    public void setApplySuccessful(boolean value) {
        _applySuccessful = value;
    }
}
