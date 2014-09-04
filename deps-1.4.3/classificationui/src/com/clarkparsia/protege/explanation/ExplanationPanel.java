package com.clarkparsia.protege.explanation;

import java.awt.BorderLayout;
import java.net.URISyntaxException;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.StyledEditorKit;


/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Apr 17, 2007 10:07:12 PM
 *
 * @author Michael Grove <mhgrove@hotmail.com>
 */
public class ExplanationPanel extends JPanel implements HyperlinkListener {
    private JEditorPane mContent;

    private ExplanationTab mTab;

    public ExplanationPanel(ExplanationTab theTab) {
        mTab = theTab;

        initGUI();
    }

    private void initGUI() {
        mContent = new JEditorPane();

        mContent.setEditorKit(new StyledEditorKit());
        mContent.addHyperlinkListener(this);
        mContent.setContentType("text/html");

        mContent.setEditable(false);
        mContent.setCaretPosition( 0 );
        mContent.setHighlighter( null );

        setLayout(new BorderLayout());

        add(mContent, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    public void setText(String theText) {
        mContent.setText(theText);
        mContent.setCaretPosition( 0 );
        
        mContent.revalidate();
        mContent.repaint();
    }

    public void hyperlinkUpdate(HyperlinkEvent theHyperlinkEvent) {
        if (theHyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                mTab.show(theHyperlinkEvent.getURL().toURI());
            }
            catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
