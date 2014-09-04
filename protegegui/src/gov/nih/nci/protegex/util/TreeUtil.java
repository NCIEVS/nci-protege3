package gov.nih.nci.protegex.util;

import edu.stanford.smi.protege.util.Log;
import gov.nih.nci.protegex.tree.TreeItems;
import gov.nih.nci.protegex.tree.TreeNode;

import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;

/**
 * Contains utility methods for the JTree GUI component.
 *
 * @author David Yee
 */
public class TreeUtil {
	/**
     * Prints the text of the TreeNode and recursively calls all its children.
     * @param node The TreeNode.
     * @param indent Indent string.
     */
    public static void debugTreeNode(TreeNode node, String indent) {
        Log.getLogger().fine(indent + "* " + node.getText());
        Enumeration enumerator = node.children();
        while (enumerator.hasMoreElements()) {
            TreeNode child = (TreeNode) enumerator.nextElement();
            debugTreeNode(child, "  ");
        }
    }

    /**
     * Prints the text contained in all of the TreeNodes.
     * @param text The text message.
     * @param tree The Tree.
     */
    public static void debugTree(String text, JTree tree) {
        Log.getLogger().fine(text);
        TreeModel model = tree.getModel();
        TreeNode root = (TreeNode) model.getRoot();
        debugTreeNode(root, "");
    }

    /**
     * Resets the TreeNode's diff state and recursively calls all its children.
     * @param node The TreeNode.
     */
    public static void resetTreeNodeDiffState(TreeNode node) {
        node.setDiffState(TreeNode.DiffState.Unmodified);
        Enumeration enumerator = node.children();
        while (enumerator.hasMoreElements()) {
            TreeNode child = (TreeNode) enumerator.nextElement();
            resetTreeNodeDiffState(child);
        }
    }

    /**
     * Resets the all the tree's TreeNodes' diff state.
     * @param tree The Tree.
     */
    public static void resetTreeDiffState(JTree tree) {
        TreeModel model = tree.getModel();
        TreeNode root = (TreeNode) model.getRoot();
        resetTreeNodeDiffState(root);
    }

    /**
     * Sets the TreeNode's diff state based on the diffs vector.
     *   This diff state sets the background color of each tree node.
     * @param node The TreeNode.
     * @param diffs The diffs vector.
     * @param diffState The diff state.
     */
    public static void setTreeNodeDiffState(TreeNode node, TreeItems diffs,
            TreeNode.DiffState diffState) {
        boolean diff = diffs.contains(node.getText());
        if (diff)
            node.setDiffState(diffState);
        Enumeration enumerator = node.children();
        while (enumerator.hasMoreElements()) {
            TreeNode child = (TreeNode) enumerator.nextElement();
            setTreeNodeDiffState(child, diffs, diffState);
        }
    }

    /**
     * Sets the all the tree's TreeNodes' diff state based on the
     *   diffs vector.  This diff state sets the background color
     *   of each tree node.
     * @param tree The tree.
     * @param diffs The diffs vector.
     * @param diffState The diff state.
     */
    public static void setTreeDiffState(JTree tree, TreeItems diffs,
            TreeNode.DiffState diffState) {
        if (false) {
            if (diffs != null) {
                diffs.fyi("");
                diffs.fyi("------------------------------");
                diffs.fyi("diffState=" + diffState);
                diffs.fyi();
            }
            debugTree("tree", tree);
        }

        resetTreeDiffState(tree);
        TreeModel model = tree.getModel();
        TreeNode root = (TreeNode) model.getRoot();
        setTreeNodeDiffState(root, diffs, diffState);
        tree.repaint();
    }
}
