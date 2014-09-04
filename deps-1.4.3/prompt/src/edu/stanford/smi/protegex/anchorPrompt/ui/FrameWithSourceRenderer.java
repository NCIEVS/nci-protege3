/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt.ui;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protegex.anchorPrompt.*;

public class FrameWithSourceRenderer {
   	private Icon theClsIcon = Icons.getClsIcon();
	private Icon theMetaClsIcon = Icons.getClsIcon(true, false, false, false);
	private Icon theReadonlyClsIcon = Icons.getClsIcon(false, false, true, false);
	private Icon theReadonlyMetaClsIcon = Icons.getClsIcon(true, false, true, false);
   	private final int _iconTextGap = 3;

	private String _text = "";
    private Icon _frameIcon = null;
    private String _frameSource = null;

    FrameWithSourceRenderer (Object o) {
    	if (o instanceof Frame) {
        	Frame f = (Frame)o;
        	_text = f.getBrowserText();
            _frameIcon = getIcon (f);
            KnowledgeBase source = f.getKnowledgeBase ();
            if (source != AnchorPromptTab.getTargetKnowledgeBase())
            	_frameSource = AnchorPromptTab.getKnowledgeBasePrettyName (source);
        } else {
           	_text = o.toString();
        }
    }

	public void paint (AnchorPairRenderer renderer, Graphics g, Point p, FontMetrics fm) {
        	if (_frameIcon != null) {
            	int iconY = p.y + (renderer.getHeight() - _frameIcon.getIconHeight())/2;
                _frameIcon.paintIcon(renderer, g, p.x, iconY);
                p.x += _frameIcon.getIconWidth() + _iconTextGap;
            }
            if (_text != null) {
                g.drawString(_text, p.x, p.y + fm.getAscent());
                p.x += fm.stringWidth(_text);
            }
            if (_frameSource != null) {
            	String source = ", ";
                source += _frameSource;
                g.drawString(source, p.x, p.y + fm.getAscent());
                p.x += fm.stringWidth(source);
            }
            p.x += AnchorPairRenderer.getTextGap ();
    }

    public Dimension getPreferredSize (Dimension d, FontMetrics fm) {
            if (_frameIcon != null) {
                d.width += _frameIcon.getIconWidth() + _iconTextGap;
                d.height = Math.max(d.height, _frameIcon.getIconHeight());
            }
            if (_text != null) {
                d.width += fm.stringWidth(_text);
            }
            if (_frameSource != null) {
                d.width += fm.stringWidth(_frameSource);
            }
            d.width += AnchorPairRenderer.getTextGap ();
            return d;
    }

    private Icon getIcon (Frame f) {
    		if (f instanceof Cls) {
	        	if (((Cls)f).isClsMetaCls()) {
            		return f.isEditable() ?  theMetaClsIcon : theReadonlyMetaClsIcon;
        		} else {
            		return f.isEditable() ? theClsIcon : theReadonlyClsIcon;
        		}
        	}
            return null;
    }
}

