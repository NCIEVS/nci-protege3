/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;

public class FrameWithSourceRenderer extends Component {
	private Icon theClsIcon = Icons.getClsIcon();
	private Icon theMetaClsIcon = Icons.getClsIcon(true, false, false, false);
	private Icon theReadonlyClsIcon = Icons.getClsIcon(false, false, true, false);
	private Icon theReadonlyMetaClsIcon = Icons.getClsIcon(true, false, true, false);
	private Icon theInstanceIcon = Icons.getInstanceIcon();
	private Icon theReadonlyInstanceIcon = Icons.getInstanceIcon(true, false);
	private Icon theSlotIcon = Icons.getSlotIcon();
	private Icon theReadonlySlotIcon = Icons.getSlotIcon(false, false, true, false);
	private Icon theFacetIcon = Icons.getFacetIcon();
	private Icon theReadonlyFacetIcon = Icons.getFacetIcon(true, false);
	private final int _iconTextGap = 3;
	
	private String _text = "";
	private Icon _frameIcon = null;
	private String _frameSource = null;
	private Frame _frame = null;
	
	private final String SOURCE_SEPARATOR_PREFIX = " ";
	private final String SOURCE_SEPARATOR_SUFFIX = "";
	
	private static Font _frameNameFont = null;
	private static FontMetrics _frameNameFontMetrics = null;
	private static Font _frameSourceFont = null;
	private static FontMetrics _frameSourceFontMetrics = null;
	
	FrameWithSourceRenderer (Object o) {
		if (o instanceof Frame) {
			_frame = (Frame)o;
			if (_frame.getFrameID () == null || _frame.isDeleted())
				_text = "*******";
			else {
				KnowledgeBase source = _frame.getKnowledgeBase ();
				if (PromptTab.mapping() && (source == ProjectsAndKnowledgeBases.getTargetKnowledgeBase())) {
					_frame = (Frame) CollectionUtilities.getFirstItem(Mappings.getSources(_frame));
					if(_frame != null) {
						source = _frame.getKnowledgeBase();
					}
				}
				_text = _frame.getBrowserText();
				if (_frame instanceof SimpleInstance)
					_text = _text + " (" + ((Instance)_frame).getDirectType().getBrowserText() + ")";
				if (source != ProjectsAndKnowledgeBases.getTargetKnowledgeBase() && (PromptTab.merging() || PromptTab.mapping()) )
					_frameSource = ProjectsAndKnowledgeBases.getKnowledgeBasePrettyName (source);
			}
			_frameIcon = getIcon (_frame);
		} else if (o != null)
			_text = o.toString();
		else
			_text = "";
	}
	
	public void paint (ActionRenderer renderer, Graphics g, Point p, FontMetrics fm) {
		Color frameColor = KnowledgeBaseInMerging.getFrameColor(_frame);
		Color oldColor = g.getColor();
		Font oldFont = g.getFont ();
		
		setFonts (g);
		if (_frameIcon != null) {
			int iconY = p.y + (renderer.getHeight() - _frameIcon.getIconHeight())/2;
			_frameIcon.paintIcon(renderer, g, p.x, iconY);
			p.x += _frameIcon.getIconWidth() + _iconTextGap;
		}
		if (_text != null) {
			if (frameColor != null)
				g.setColor (frameColor);
			g.setFont(_frameNameFont);
			g.drawString(_text, p.x, p.y + _frameNameFontMetrics.getAscent());
			p.x += fm.stringWidth(_text);
			g.setColor(oldColor);
		}
		if (_frameSource != null) {
			g.setFont (_frameSourceFont);
			String source = SOURCE_SEPARATOR_PREFIX;
			source += _frameSource;
			source += SOURCE_SEPARATOR_SUFFIX;
			g.drawString(source, p.x, p.y + _frameSourceFontMetrics.getAscent());
			p.x += fm.stringWidth(source);
		}
		g.setFont(oldFont);
		p.x += ActionRenderer.getTextGap ();
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
			d.width +=fm.stringWidth(_frameSource);
		}
		d.width += ActionRenderer.getTextGap ();
		return d;
	}
	
	private Icon getIcon (Frame f) {
		if (f.getFrameID() == null) return null;
		if (f instanceof Cls) {
			if (((Cls)f).isClsMetaCls()) {
				return f.isEditable() ?  theMetaClsIcon : theReadonlyMetaClsIcon;
			} else {
				return f.isEditable() ? theClsIcon : theReadonlyClsIcon;
			}
		} else if (f instanceof Slot) {
			return f.isEditable() ?  theSlotIcon : theReadonlySlotIcon;
		} else if (f instanceof Facet) {
			return f.isEditable() ? theFacetIcon : theReadonlyFacetIcon;
		} else if (f instanceof Instance) {
			return f.isEditable() ? theInstanceIcon : theReadonlyInstanceIcon;
		}
		return null;
	}
	
	private void setFonts (Graphics g) {
		_frameNameFont = g.getFont();
		_frameSourceFont = new Font (g.getFont().getName(), Font.ITALIC, g.getFont().getSize());;
		
		_frameNameFontMetrics = g.getFontMetrics (_frameNameFont);
		_frameSourceFontMetrics = g.getFontMetrics (_frameSourceFont);
		
	}
	
}

