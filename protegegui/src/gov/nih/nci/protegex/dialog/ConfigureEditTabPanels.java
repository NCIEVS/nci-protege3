/**
 * 
 */
package gov.nih.nci.protegex.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.ui.CheckBoxRenderer;
import edu.stanford.smi.protege.util.AbstractValidatableComponent;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.LabeledComponent;
import gov.nih.nci.protegex.edit.NCIEditTab;

/**
 * @author bitdiddle
 *
 */
public class ConfigureEditTabPanels extends AbstractValidatableComponent {
	
	public static final long serialVersionUID = 123456762L;
	
	 private boolean dirty;	    

	    private JTable table;


	    private class MoveTabUp extends AbstractAction {
	    	public static final long serialVersionUID = 123456562L;

	        MoveTabUp() {
	            super("Move selected tab up", Icons.getUpIcon());
	        }


	        public void actionPerformed(ActionEvent event) {
	            int index = table.getSelectedRow();
	            if (canMoveUp(index)) {
	                getTabModel().moveRow(index, index, index - 1);
	                int n = index - 1;
	                table.getSelectionModel().setSelectionInterval(n, n);
	                dirty = true;
	            }
	        }
	    }

	    private class MoveTabDown extends AbstractAction {
	    	
	    	public static final long serialVersionUID = 123416562L;

	        MoveTabDown() {
	            super("Move selected tab down", Icons.getDownIcon());
	        }


	        public void actionPerformed(ActionEvent event) {
	            int index = table.getSelectedRow();
	            if (canMoveDown(index)) {
	                getTabModel().moveRow(index, index, index + 1);
	                int n = index + 1;
	                table.getSelectionModel().setSelectionInterval(n, n);
	                dirty = true;
	            }
	        }
	    }
	    
	    private NCIEditTab tab = null;


	    public ConfigureEditTabPanels(NCIEditTab t) {
	    	tab = t;
	        setLayout(new BorderLayout());
	        
	        table = ComponentFactory.createTable(getConfigureAction());
	        table.setModel(createTableModel());
	        ComponentUtilities.addColumn(table, new PanelDescriptorEnableRenderer());
	        table.getColumnModel().getColumn(0).setMaxWidth(50);
	        ComponentUtilities.addColumn(table, new DefaultTableCellRenderer());
	        table.addMouseListener(new ClickListener());
	        JScrollPane pane = ComponentFactory.createScrollPane(table);
	        pane.setColumnHeaderView(table.getTableHeader());
	        pane.setBackground(table.getBackground());
	        LabeledComponent c = new LabeledComponent("Tabs", pane);
	        c.addHeaderButton(new MoveTabUp());
	        c.addHeaderButton(new MoveTabDown());
	        add(c);
	    }


	    private boolean canMoveUp(int index) {
	        return index > 0 && isEnabled(index);
	    }


	    private boolean canMoveDown(int index) {
	        boolean canMoveDown = 0 <= index && index < table.getRowCount() - 1;
	        if (canMoveDown) {
	            canMoveDown = isEnabled(index) && canEnable(index + 1);
	        }
	        return canMoveDown;
	    }


	    public boolean getRequiresReloadUI() {
	        return dirty;
	    }


	    private boolean isEnabled(int row) {
	        Boolean b = (Boolean) getTabModel().getValueAt(row, 0);
	        return b.booleanValue();
	    }


	    private void setEnabled(int row, boolean enabled) {
	        getTabModel().setValueAt(Boolean.valueOf(enabled), row, 0);
	    }


	    private class ClickListener extends MouseAdapter {

	        public void mousePressed(MouseEvent event) {
	            Point p = event.getPoint();
	            int col = table.columnAtPoint(p);
	            if (col == 0) {
	                int row = table.rowAtPoint(p);
	                if (isEditable(row)) {
	                    boolean b = isEnabled(row);
	                    setEnabled(row, !b);
	                    dirty = true;
	                }
	            }
	        }
	    }


	    private boolean isEditable(int row) {
	        return true;
	    }


	    private TableModel createTableModel() {
	        DefaultTableModel model = new DefaultTableModel();
	        model.addColumn("Visible");
	        model.addColumn("Panel");
	        

	        String[] panels = tab.getConfigurablePanels();
	        for (int i = 0; i < panels.length; i++) {
	        	if (tab.canEnable(panels[i])) {
	        	model.addRow(new Object[]{Boolean.valueOf(tab.isPanelVisible(panels[i])), panels[i]});
	        	}
	        }
	        return model;
	    }	    

	    private DefaultTableModel getTabModel() {
	        return (DefaultTableModel) table.getModel();
	    }


	    public void saveContents() {
	        if (dirty) {
	            
	            for (int row = 0; row < getTabModel().getRowCount(); ++row) {
	                tab.setPanelEnabled(getPanelName(row), isEnabled(row));
	            }
	            
	        }
	    }

	    public boolean validateContents() {
	        return true;
	    }    


	    private String getPanelName(int row) {
	        return (String) getTabModel().getValueAt(row, 1);
	    }


	    private boolean canEnable(int row) {
	        String d = getPanelName(row);
	        return canEnable(d);
	    }


	    private boolean canEnable(String d) {
	        return tab.canEnable(d);
	    }
	    
	    class PanelDescriptorEnableRenderer extends CheckBoxRenderer {
	    	
	    	public static final long serialVersionUID = 123456769L;

	        private final Component EMPTY;


	        {
	            EMPTY = new JPanel() {
	            	public static final long serialVersionUID = 123256769L;
	                public boolean isOpaque() {
	                    return false;
	                }
	            };
	        }


	        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean b,
	                                                       int row, int col) {
	            Component c;
	            if (canEnable(row)) {
	                c = super.getTableCellRendererComponent(table, value, isSelected, b, row, col);
	            }
	            else {
	                c = EMPTY;
	            }
	            return c;
	        }
	    }
	    
	    private Action getConfigureAction() {
	        return new AbstractAction("Configure") {
	        	public static final long serialVersionUID = 193456769L;
	            public void actionPerformed(ActionEvent event) {
	            	// do nothing
	                
	            }
	        };
	    }


	

}
