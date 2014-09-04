package gov.nih.nci.protegex.util;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JDialog;

/**
 * This class helps the dialog remember its last size and location.
 * If the usePreviousSetting flag is set to false, it will display the
 * dialog in its default configuration.
 * 
 * @author David Yee
 */
public class DialogHelper {
    //Member Variables:
    private int _initWidth = -1, _initHeight = -1;
    private int _x = -1, _y = -1, _width = -1, _height = -1;
    private JDialog _dialog = null;
    private boolean _usePreviousSettings = false;
    
    /**
     * Constructs this class.
     * @param initWidth The dialog initial width;
     * @param initHeight The dialog initial height;
     */
    public DialogHelper(int initWidth, int initHeight) {
        _initWidth = initWidth;
        _initHeight = initHeight;
    }
    
    /**
     * Re-initialize to the new dialog.
     * @param dialog The dialog being manipulated.
     * @param usePreviousSettings if true, set the size and location
     *   with respect to the last instantiated dialog.
     */
    public void init(JDialog dialog, boolean usePreviousSettings) {
        _dialog = dialog;
        _usePreviousSettings = usePreviousSettings;
        if (_usePreviousSettings) {
            _dialog.setSize(getUpdatedSize(_width, _height));
            _dialog.setLocation(getUpdatedLocation(_x, _y));
        } else {
            _dialog.setSize(_initWidth, _initHeight);
            _dialog.setLocationRelativeTo(UIUtil.getFrame(_dialog));
        }
    }

    /**
     * Returns the updated size.
     * @param dimension The current size.
     * @return the updated size.
     */
    public Dimension getUpdatedSize(Dimension dimension) {
        if (! _usePreviousSettings)
            return dimension;
        
        _width = dimension.width >= 0 ? dimension.width : _initWidth;
        _height = dimension.height >= 0 ? dimension.height : _initHeight;
        return new Dimension(_width, _height);
    }
    
    /**
     * Returns the updated size.
     * @param width The current width.
     * @param height The current height.
     * @return the updated size.
     */
    public Dimension getUpdatedSize(int width, int height) {
        return getUpdatedSize(new Dimension(width, height));
    }
    
    /**
     * Returns the updated location.
     * @param location The current location.
     * @return the updated location.
     */
    public Point getUpdatedLocation(Point location) {
        if (! _usePreviousSettings)
            return location;

        if (location.x < 0 && location.y < 0) {
            _dialog.setLocationRelativeTo(UIUtil.getFrame(_dialog));
            return _dialog.getLocation();
        }
        _x = location.x;
        _y = location.y;
        return new Point(_x, _y);
    }
    
    /**
     * Returns the updated location.
     * @param x The current x location.
     * @param y The current y location.
     * @return the updated location.
     */
    public Point getUpdatedLocation(int x, int y) {
        return getUpdatedLocation(new Point(x, y));
    }
    
    /**
     * Cleans up and saves the current size and location values.
     */
    public void dispose() {
        if (_dialog == null || ! _usePreviousSettings)
            return;
        
        Point location = _dialog.getLocation();
        _x = location.x;
        _y = location.y;
        Dimension size = _dialog.getSize();
        _width = size.width;
        _height = size.height;
        _dialog = null;
    }
}
