package gov.nih.nci.protegex.tree;

import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.util.ComplexPropertyParser;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * @Author: NGIT, Kim Ong; Iris Guo
 */
public class TreeRenderer extends DefaultTreeCellRenderer {
    private static final long serialVersionUID = 5233371338809554109L;   
    

    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean isSelected, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        TreeNode node = (TreeNode) value;
        setForeground(isSelected ? Color.black : node.getColor());
        setBackgroundNonSelectionColor(node.getDiffColor());
        
        TreeItem ti = (TreeItem) node.getUserObject();
        if (ti.getValue().startsWith("<")) {
        	HashMap<String, String> hmap = ComplexPropertyParser.parseXML(ti.getValue());
        	if (ti.getName().equalsIgnoreCase(NCIEditTab.ALTLABEL)) {
            	
            	//String termName = ComplexPropertyParser.getPtNciTermName(ti.getValue());
            	setText(ti.getDisplayName() + " - (" + hmap.get("term-source") +
            			"/" + hmap.get("term-group") + "): " + hmap.get("term-name"));
            	
            } else if (ti.getName().equalsIgnoreCase(NCIEditTab.DEFINITION)) {
            	setText(ti.getDisplayName() + " - (" + hmap.get("def-source") +
            			"): " + hmap.get("def-definition"));
            	
            } else {
            	setText(node.getText());
            }
        } else {
        	setText(node.getText());
        }
        
      
        
        setIcon(node.getIcon());
        selected = isSelected;  
        
        return this;
    }
}
