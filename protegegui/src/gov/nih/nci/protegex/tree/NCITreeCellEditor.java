package gov.nih.nci.protegex.tree;

import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;


class NCITreeCellEditor extends DefaultTreeCellEditor {
   public  NCITreeCellEditor (JTree tree, DefaultTreeCellRenderer renderer)
   {
       super(tree, renderer);
   }

   public boolean isCellEditable(EventObject event)
   {
       return false;
   }
}