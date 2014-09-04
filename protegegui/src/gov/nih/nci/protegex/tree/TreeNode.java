package gov.nih.nci.protegex.tree;

import gov.nih.nci.protegex.edit.NCIOWLIcons;

import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.*;
import gov.nih.nci.protegex.tree.TreeItem.TreeItemType;

import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @Author: NGIT, Kim Ong; Iris Guo
 */
public class TreeNode extends DefaultMutableTreeNode {
    private static final long serialVersionUID = 3510671207995016589L;
    private static final Color UNMODIFIED_COLOR = null;
    private static final Color MODIFIED_COLOR = new Color(255, 250, 205); // Yellow
    private static final Color INSERTED_COLOR = new Color(193, 255, 193); // Green
    private static final Color DELETED_COLOR = new Color(255, 228, 225); // Red

    public enum DiffState {
        Unmodified, Modified, Inserted, Deleted
    };

    private TreeItemType _nodeType;
    private DiffState _diffState = DiffState.Unmodified;

    public TreeNode(TreeItem item, TreeItemType nodeType) {
        super(item);
        _nodeType = nodeType;
    }

    public String getText() {
        return toString();
    }

    public Color getColor() {
        Color color = Color.orange.darker();
        if (_nodeType == TYPE_CONCEPT)
            color = Color.black;
        else if (_nodeType == TYPE_PROPERTY)
            color = Color.black;
        else if (_nodeType == TYPE_PARENT)
            color = Color.magenta.darker().darker();
        else if (_nodeType == TYPE_RESTRICTION)
            color = Color.blue.darker();
        else if (_nodeType == TYPE_ASSOCIATION)
            color = Color.cyan.darker();
        //else if ((_nodeType == 5) || (_nodeType == 6))
            //color = Color.yellow.darker();
        return color;
    }

    public ImageIcon getIcon() {
        if (_nodeType == TYPE_CONCEPT) // code, id
            return NCIOWLIcons.getImageIcon("blue-ball.gif");
        else if (_nodeType == TYPE_ASSOCIATION) // code, id
            return NCIOWLIcons.getImageIcon("orange-ball.gif");
        else if (_nodeType == TYPE_RESTRICTION) // role, property
            return NCIOWLIcons.getImageIcon("magenta-ball.gif");
        else if (_nodeType == TYPE_PARENT) // super
            return NCIOWLIcons.getImageIcon("singleprimitiveP.gif");
        // else if (nodeType == TYPE_PROPERTY) // kind
        // return NCIOWLIcons.getImageIcon("cyan-ball.gif");
        else if ((_nodeType == TYPE_PROPERTY)) // primitive, defined
            return NCIOWLIcons.getImageIcon("yellow-ball.gif");
        return NCIOWLIcons.getImageIcon("orange-ball.gif");
    }

    public void setDiffState(DiffState diffState) {
        this._diffState = diffState;
    }

    public DiffState getDiffState() {
        return _diffState;
    }

    public Color getDiffColor() {
        switch (_diffState) {
        case Modified: return MODIFIED_COLOR;
        case Inserted: return INSERTED_COLOR;
        case Deleted: return DELETED_COLOR;
        default: return UNMODIFIED_COLOR;
        }
    }
}
