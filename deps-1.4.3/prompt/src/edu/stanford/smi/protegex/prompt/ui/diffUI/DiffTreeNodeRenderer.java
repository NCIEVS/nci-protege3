 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui.diffUI;

import java.awt.*;

import javax.swing.*;
import javax.swing.tree.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;
import edu.stanford.smi.protegex.prompt.ui.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class DiffTreeNodeRenderer extends FrameInMergingRenderer {
    private static ResultTable _diffTable;
    private boolean _underline = false;
    private boolean _strikeOut = false;

    private int _frameStatus;
    private int _treeStatus;
    private boolean _childrenChangedStatus;

    private String _toolTipText = null;

    private Frame _parentNode = null;

    private JTree _tree;
    private int _row;
    
    private DiffViewSetUp _viewSetup;
// (1) for each node and (2) for each tree, need to determine one of 5 states:
// 1. Unchanged
// 2. Changed
// 3. Added
// 4. Deleted
// 5. Moved

	public DiffTreeNodeRenderer (DiffViewSetUp vs) {
		super();
		_viewSetup = vs;
		_diffTable = vs.getResultsTable();
	}

    public Color getTextColor() {
        Color result;
        _underline = false;
        _strikeOut = false;
    	if ((!_viewSetup.isStandAloneTreeView() && !PromptTab.mergingHasBeenSetUp()) || _frame == null)
        	return super.getTextColor();
        if (_frameStatus==FrameStatus.FRAME_DELETED) {
          result = Color.red;
        } else if (_frameStatus==FrameStatus.FRAME_ADDED) {
          result = Color.blue;
        } else if (_frameStatus==FrameStatus.FRAME_MOVED_FROM) {
            result = Color.gray;
        } else if (_frameStatus==FrameStatus.SUPERCLASS_REMOVED_FROM_FRAME) {
            result = Color.gray;
          } else if (_frameStatus==FrameStatus.FRAME_MOVED_TO) {
            result = Color.blue;
        } else if (_frameStatus==FrameStatus.FRAME_COPIED_TO_HERE) {
            result = Color.blue;
        } else
          result = _foregroundNormalColor;

        if (result == null)
          result = super.getTextColor();

        return result;
    }


    public Font getFont () {
      Font result = super.getFont ();
      if (_frame == null) return result;
      
      if(_frame instanceof Cls && _frameStatus != FrameStatus.FRAME_DELETED && _frameStatus != FrameStatus.FRAME_ADDED){
      	if(Util.haveInstancesChanged(_diffTable,(Cls)_frame)){
      		_underline = true;
      	}
      }
      
      switch (_frameStatus) {
        case FrameStatus.FRAME_DELETED: {
          _strikeOut = true;
          return result;
        }
        case FrameStatus.FRAME_ADDED: {
          _underline = true;
          return result;
        }
        case FrameStatus.FRAME_CHANGED: {
          result = result.deriveFont(Font.BOLD);
          return result;
        }
        case FrameStatus.FRAME_MOVED_FROM: {
//          result = result.deriveFont(Font.ITALIC);
          return result;
        }
        case FrameStatus.SUPERCLASS_REMOVED_FROM_FRAME: {
//          result = result.deriveFont(Font.ITALIC);
          return result;
        }
        case FrameStatus.FRAME_MOVED_TO: {
            result = result.deriveFont(Font.BOLD);
            return result;
          }
        case FrameStatus.FRAME_COPIED_TO_HERE: {
            result = result.deriveFont(Font.BOLD);
            return result;
          }
      }

      if (_treeStatus != FrameStatus.NO_STATUS && _treeStatus != FrameStatus.TREE_UNCHANGED && _treeStatus != FrameStatus.TREE_NO_CHILDREN)
        result = result.deriveFont(Font.BOLD);

      return result;
    }

//**** change this dependeing on the tree status
    public void setMainIcon() {
      boolean metaclass = ((Cls)_frame).isMetaCls();
      boolean defined = PromptTab.kbInOWL () && OWLUtil.isDefinedCls ((Cls) _frame);
//      if (_childrenChangedStatus)
//        ending = "WithWarning.gif";

	  Icon mainIcon = null;
//	  if (_frame instanceof Cls && ((Cls)_frame).getDirectSubclassCount() == 0) 
//	  	mainIcon = ComponentUtilities.loadImageIcon(TabComponent.class, "images/" + metaclassPrefix + "Class.gif");
//      else 
      if (_treeStatus==FrameStatus.TREE_ADDED)
        mainIcon = DiffIcons.getTreeAddedIcon (metaclass, defined); 
      else if (_treeStatus==FrameStatus.TREE_DELETED)
	  	mainIcon = DiffIcons.getTreeDeletedIcon (metaclass, defined); 
      else if (_treeStatus==FrameStatus.TREE_CHANGED)
	  	mainIcon = DiffIcons.getTreeChangedIcon (metaclass, defined); 
      else if (_treeStatus==FrameStatus.TREE_MOVED_TO)
        mainIcon = DiffIcons.getTreeMovedToIcon(metaclass, defined);
      else if (_treeStatus==FrameStatus.TREE_MOVED_FROM)
        mainIcon = DiffIcons.getTreeMovedFromIcon (metaclass, defined);
//      else if (((Cls)_frame).isMetaCls())
//        _mainIcon = ComponentUtilities.loadImageIcon(TabComponent.class, "images/Metaclass" + ending);
      else if (_childrenChangedStatus)
        mainIcon = DiffIcons.getTreeWithWarningIcon(metaclass, defined);
      else
        mainIcon = DiffIcons.getRegularTreeIcon(metaclass, defined);
    	if (mainIcon != null)
    		super.setMainIcon (mainIcon);
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        _tree = tree;
        _row = row;
        Component result =  super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        setToolTipText (_toolTipText);
        return result;
    }

    public void load(Object value) {
      super.load(value);

	  if (value instanceof Frame && _frame == null)
	  	_frame = (Frame)value;
	
	 if (_frame == null) return;
      findCurrentParent();
       _frameStatus = _viewSetup.getFrameStatuses().getFrameStatus(_frame, _parentNode);
      _treeStatus = _viewSetup.getFrameStatuses().getTreeStatus(_frame, _parentNode);
      _childrenChangedStatus = _viewSetup.getFrameStatuses().getChildrenChangedStatus(_frame);
//Log.trace ("_frame = " + _frame + "_parentNode = " + _parentNode + "_frameStatus = " + _frameStatus + ", _treeStatus = " + _treeStatus,
//this, "load");
      setMainIcon();
      setToolTipText(_frame, _parentNode);

    }

    private void setToolTipText(Frame frame, Frame parent) {
      switch (_frameStatus) {
        case FrameStatus.FRAME_ADDED: {
           _toolTipText = "class added";
           return;
        }
        case FrameStatus.FRAME_DELETED: {
          _toolTipText = "class deleted";
          return;
        }
        case FrameStatus.FRAME_MOVED_FROM: {
          Frame localFrame = (_frame.getKnowledgeBase()==_viewSetup.getKb2()) ? _frame : _diffTable.getFirstImage(_frame);
          Object to = CollectionUtilities.getFirstItem (Util.getDirectSuperclasses((Cls)localFrame));
          _toolTipText = "class moved to " +
              ((to==null) ? "" : ((Cls)to).getBrowserText());
          return;
        }
        case FrameStatus.FRAME_MOVED_TO: {
            Frame from = _viewSetup.getFrameStatuses().movedFromWhere (frame, parent);
            _toolTipText = "class moved from " +
                ((from == null)? "" : from.getBrowserText());
            return;
          }
        case FrameStatus.FRAME_COPIED_TO_HERE: {
            _toolTipText = "class copied";
            return;
          }
        case FrameStatus.SUPERCLASS_REMOVED_FROM_FRAME: {
            _toolTipText = "superclass removed";
            return;
          }
        case FrameStatus.FRAME_CHANGED: {
          String oldName = _viewSetup.getFrameStatuses().getOldName (frame, parent);
          if (oldName != null)
            _toolTipText = "class name changed from " + oldName;
          else
            _toolTipText = "class changed";
          return;
        }
      }
      _toolTipText = "class unchanged";
    }

    private void findCurrentParent () {
      TreePath path = _tree.getPathForRow(_row);
      if (path!=null) {
        LazyTreeNode node = (LazyTreeNode) path.getParentPath().getLastPathComponent();
        Object o = node.getUserObject();
        if (o != null && o instanceof Frame)
          _parentNode = (Frame)o;
      }
    }

// --------------------------------------------------_-----------------
// The rest is the same for all nodes (no if's)


   protected void paintString(Graphics graphics, String text, Point position, Color color, Dimension size) {
	if (color != null) {
		graphics.setColor(color);
	}

	graphics.setFont(getFont());
	int y = (size.height + _fontMetrics.getAscent())/2 -2;	// -2 is a bizarre fudge factor that makes it look better!
	graphics.drawString(text, position.x, y);
	drawLine (graphics, position.x, (_fontMetrics.getHeight())/2, position.x + _fontMetrics.stringWidth(text), (_fontMetrics.getHeight())/2);
	position.x += _fontMetrics.stringWidth(text);
  }

  private void drawLine (Graphics g, int x1, int y1, int x2, int y2) { 
    if (_frame == null) return;
    if (_strikeOut)
      g.drawLine(x1, y1+1, x2, y2+1);
    if (_underline) {
      g.drawLine(x1, y1*2, x2, y2*2);
    }
  }

  public String toString () {
     	return "DiffTreeNodeRenderer";
  }

}

