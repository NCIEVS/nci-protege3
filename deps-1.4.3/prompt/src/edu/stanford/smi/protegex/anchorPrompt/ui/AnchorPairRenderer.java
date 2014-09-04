/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt.ui;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.anchorPrompt.*;

public class AnchorPairRenderer extends JComponent implements ListCellRenderer {

    private Color _backgroundNormalColor;
    private Color _foregroundNormalColor;
    private Color _backgroundSelectionColor;
    private Color _foregroundSelectionColor;

    private FontMetrics _fontMetrics = null;

    private LookAndFeel _cachedLookAndFeel;
    private boolean _hasFocus;
    private boolean _isSelected;

    private FrameWithSourceRenderer [] _anchorRenders = new FrameWithSourceRenderer [2];
    private Cls _anchors [] = new Cls [2];

    static final int _textGap = 8;
    static final private int NO_SCORE = -1;
   	private int _score = NO_SCORE;


	public Component getListCellRendererComponent(JList list, Object value,
					int row, boolean selected, boolean hasFocus) {
		_foregroundSelectionColor = list.getSelectionForeground();
        _backgroundSelectionColor = list.getSelectionBackground();
        _foregroundNormalColor = list.getForeground();
        _backgroundNormalColor = list.getBackground();
        return setup(list, value, hasFocus, selected);
    }


    public Dimension getPreferredSize() {
        Dimension d = new Dimension(0, _fontMetrics.getHeight());
        if (_anchors != null && _anchors.length > 0) {
        	if (_score != NO_SCORE)
            	d.width += _fontMetrics.stringWidth ("" + _score + ":");
        	for (int i = 0; i < _anchors.length; i++) {
            	FrameWithSourceRenderer nextArg = (FrameWithSourceRenderer)_anchorRenders[i];
                d = nextArg.getPreferredSize (d, _fontMetrics);
            }
        }
       	d.width += _textGap;

        return d;
    }

    public void paint(Graphics g) {
		Dimension preferredSize = getPreferredSize();
		int height = getHeight();
		int width = getWidth();
		Point p = new Point();
		FontMetrics fm = _fontMetrics;
		int ascent = fm.getAscent();

		g.setColor(getBackgroundColor());
		g.fillRect(p.x, p.y, width, height);

		p.y += (height - preferredSize.height) / 2;
		g.setColor(getTextColor());

        if (_anchors != null && _anchors.length > 0) {
        	if (_score != NO_SCORE) {
	        	String scoreString = "" + _score + ":";
    	    	g.drawString(scoreString, p.x, p.y + ascent);
        		p.x += _fontMetrics.stringWidth(scoreString) + _textGap;
            }
        	for (int i = 0; i < _anchors.length; i++) {
             	FrameWithSourceRenderer nextArg = (FrameWithSourceRenderer)_anchorRenders[i];
				nextArg.paint (this, g, p, _fontMetrics);
         		p.x += _textGap;
            }
        }
	}

	public void load(Object o) {
       	if (o instanceof AnchorPair) {
	        AnchorPair anchor = (AnchorPair)o;
    	    _anchors[0] = anchor.getAnchor(0);
        	_anchors[1] = anchor.getAnchor(1);
       		for (int i = 0; i < _anchors.length; i++)
        		_anchorRenders[i] = new FrameWithSourceRenderer (_anchors[i]);
        } else if (o instanceof ScoreTableElement) {
        	ScoreTableElement elt = (ScoreTableElement)o;
        	_anchors[0] = elt.getFirstElement ();
       		_anchors[1] = elt.getSecondElement ();
        	_score = elt.getScore();
       		for (int i = 0; i < _anchors.length; i++)
        		_anchorRenders[i] = new FrameWithSourceRenderer (_anchors[i]);
        }

    }
	private Color getBackgroundColor() {
		return (_isSelected) ? _backgroundSelectionColor : _backgroundNormalColor;
    }

    private Color getTextColor() {
		return (_isSelected) ? _foregroundSelectionColor : _foregroundNormalColor;
    }

    private Component setup(Component c, Object value, boolean hasFocus, boolean isSelected) {
    	setFont(c.getFont());
        this._hasFocus = hasFocus;
        this._isSelected = isSelected;

        load(value);

        LookAndFeel _currentLookAndFeel = UIManager.getLookAndFeel();
        if (_fontMetrics == null) {
        	_fontMetrics = getFontMetrics(getFont());
        }
        if (_currentLookAndFeel != _cachedLookAndFeel) {
        	_cachedLookAndFeel = _currentLookAndFeel;
        }

        return this;
    }

    public static int getTextGap () {
		return _textGap;
    }

}

