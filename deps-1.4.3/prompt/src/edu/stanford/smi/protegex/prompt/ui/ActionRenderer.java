 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.prompt.actionLists.Action;

public class ActionRenderer extends JComponent implements ListCellRenderer, TableCellRenderer {

    private Color _backgroundNormalColor;
    private Color _foregroundNormalColor;
    private Color _backgroundSelectionColor;
    private Color _foregroundSelectionColor;

    private Font _textFont = null;
    private FontMetrics _textFontMetrics;

    private LookAndFeel _cachedLookAndFeel;
    private boolean _hasFocus;
    private boolean _isSelected;

	private String _mainString = null;
	private String _trailingString = null;
    private String _actionPriority = null;
    private FrameWithSourceRenderer [] _actionArgRenders = null;
    private Object [] _actionArgs = null;
    private String _connectorString = null;
    private String _connectorString2 = null;

    static final int _textGap = 8;

	public Component getListCellRendererComponent(JList list, Object value,
					int row, boolean selected, boolean hasFocus) {
		_foregroundSelectionColor = list.getSelectionForeground();
        _backgroundSelectionColor = list.getSelectionBackground();
        _foregroundNormalColor = list.getForeground();
        _backgroundNormalColor = list.getBackground();
        return setup(list, value, hasFocus, selected);
    }


	public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean hasFocus, int row, int col) {
		_foregroundSelectionColor = table.getSelectionForeground();
        _backgroundSelectionColor = table.getSelectionBackground();
        _foregroundNormalColor = table.getForeground();
        _backgroundNormalColor = table.getBackground();
        return setup(table, value, hasFocus, selected);
    }


    public Dimension getPreferredSize() {
        Dimension d = new Dimension(0, _textFontMetrics.getHeight());
        if (_mainString != null && _mainString != "") {
        	d.width += _textFontMetrics.stringWidth(_mainString);
            d.width += _textGap;
        }
        if (_actionArgRenders != null && _actionArgRenders.length > 0) {
        	for (int i = 0; i < _actionArgRenders.length; i++) {
            	FrameWithSourceRenderer nextArg = (FrameWithSourceRenderer)_actionArgRenders[i];
                d = nextArg.getPreferredSize (d, _textFontMetrics);
                if (_connectorString != null) {
                  	d.width += _textFontMetrics.stringWidth(_connectorString) + _textGap;
                }
                if (_connectorString2 != null) {
                  	d.width += _textFontMetrics.stringWidth(_connectorString2) + _textGap;
                }
            }
        }
        if (_trailingString != null && _trailingString != "") {
        	d.width += _textFontMetrics.stringWidth(_trailingString);
            d.width += _textGap;
        }

        return d;
    }

    public void paint(Graphics g) {
		Dimension preferredSize = getPreferredSize();
		int height = getHeight();
		int width = getWidth();
		Point p = new Point();
		FontMetrics fm = _textFontMetrics;
		int ascent = fm.getAscent();

		g.setColor(getBackgroundColor());
		g.fillRect(p.x, p.y, width, height);

		p.y += (height - preferredSize.height) / 2;
		g.setColor(getTextColor());
		g.setFont (_textFont);

		if (_mainString != null && _mainString != "") {
            Font currentFont = g.getFont();
			if (_textFont != null)
				g.setFont (_textFont);
        	g.drawString(_mainString, p.x, p.y + ascent);
        	p.x += fm.stringWidth(_mainString) + _textGap;
            g.setFont(currentFont);
        }
        if (_actionArgRenders != null && _actionArgRenders.length > 0) {
        	for (int i = 0; i < _actionArgRenders.length; i++) {
      	        FrameWithSourceRenderer nextArg = (FrameWithSourceRenderer)_actionArgRenders[i];
				nextArg.paint (this, g, p, _textFontMetrics);
                if (_connectorString != null && i == 0) {
        			g.drawString(_connectorString, p.x, p.y + ascent);
        			p.x += fm.stringWidth(_connectorString) + _textGap;
                }
                if (_connectorString2 != null && i == 1) {
        			g.drawString(_connectorString2, p.x, p.y + ascent);
        			p.x += fm.stringWidth(_connectorString2) + _textGap;
                }
            }
        }
		if (_trailingString != null && _trailingString != "") {
        	g.drawString(_trailingString, p.x, p.y + ascent);
        	p.x += fm.stringWidth(_trailingString) + _textGap;
        }
	}

	public void load(Object o) {
       	if (!(o instanceof Action)) return;

        Action action = (Action)o;
    	setPriority (action.getPriority());
        setMainString (action.getShortName());
        setConnectorStrings (action);
        try {
        	setArgs (action.getArgs().toArray());

        } catch (Exception e) {
            Log.getLogger().severe ("action: " + action);
       	   	 e.printStackTrace();
    	}
    }

    private void setPriority (int priority) {
    	_actionPriority = "" + priority + ":";
    }

    protected void setMainString (String name) {
    	_mainString = name;
    }

    private void setConnectorStrings (Action action) {
		_connectorString = action.getConnectorString();
		_connectorString2 = action.getConnectorString2();
    }

    public void setTrailingString (String text) {
		_trailingString = text;
    }

    protected void setArgs (Object [] args) {
    	if (args == null)  {
	    	_actionArgRenders = null;
            _actionArgs = null;
            return;
        }

    	_actionArgs = new Object [args.length];
    	_actionArgRenders = new FrameWithSourceRenderer [args.length];
    	for (int i = 0; i < args.length; i++){
	    	_actionArgRenders[i] = new FrameWithSourceRenderer (args[i]);
            _actionArgs[i] = args[i];
        }
    }

/*
	public void setUI(javax.swing.plaf.ComponentUI ui) {
    	super.setUI(ui);
        Log.enter(this, "setUI", ui);
    }
*/
	private Color getBackgroundColor() {
		return (_isSelected) ? _backgroundSelectionColor : _backgroundNormalColor;
    }

    private Color getTextColor() {
		return (_isSelected) ? _foregroundSelectionColor : _foregroundNormalColor;
    }

    public void setTextFont (Font font) {
        _textFont = font;
		_textFontMetrics = getFontMetrics(_textFont);
    }

    public Component setup(Component c, Object value, boolean hasFocus, boolean isSelected) {
    	setFont(c.getFont());
        _hasFocus = hasFocus;
        _isSelected = isSelected;

        load(value);

/*
        _frameFont = getFont();
		_frameFontMetrics = getFontMetrics(_frameFont);

        _textFont = new Font (getFont().getName(), Font.BOLD, getFont().getSize());
		_textFontMetrics = getFontMetrics(_textFont);

        _frameFont = new Font (getFont().getName(), Font.BOLD, getFont().getSize());
		_frameFontMetrics = getFontMetrics(_frameFont);



*/
        if (_textFont == null) {
	        _textFont = getFont();
			_textFontMetrics = getFontMetrics(_textFont);
        }

        LookAndFeel _currentLookAndFeel = UIManager.getLookAndFeel();
        if (_currentLookAndFeel != _cachedLookAndFeel) {
        	_cachedLookAndFeel = _currentLookAndFeel;
        }
        return this;
    }

    public static int getTextGap () {
		return _textGap;
    }

    public String toString () {
     	return "ActionRenderer";
    }

}

