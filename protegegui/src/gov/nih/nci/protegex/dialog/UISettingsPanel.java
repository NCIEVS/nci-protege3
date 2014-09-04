/**
 * 
 */
package gov.nih.nci.protegex.dialog;

import gov.nih.nci.protegex.util.SemanticTypeUtil;
import gov.nih.nci.protegex.workflow.ExportPackageDialog;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * @author bitdiddle
 *
 */
public class UISettingsPanel extends JComponent {
	
	public static final long serialVersionUID = 123456792L;
	
//	 Member variables:
    private JCheckBox _semanticTypeCB;
    
    // these next two are workflow related
    private JCheckBox _packageNotExportableCB;
    private JCheckBox _canArchiveRowsCB;    


    public UISettingsPanel() {

        

       
         
        _semanticTypeCB = new JCheckBox("Suppress Semantic Type Warnings", 
                SemanticTypeUtil.isSuppressMessage());
        _semanticTypeCB.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		SemanticTypeUtil.setSuppressMessage(_semanticTypeCB.isSelected());
                
        	}
        });
        
        // workflow items if workflow enabled
        
        _packageNotExportableCB = new JCheckBox("Suppress Package Not Exportable Warnings", 
                ExportPackageDialog.isSuppressMessagePackageNotExportable());
        this._packageNotExportableCB.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		ExportPackageDialog.setSuppressMessagePackageNotExportable(
                        _packageNotExportableCB.isSelected());
                    
        	}
        });
        
        
        _canArchiveRowsCB = new JCheckBox("Suppress Can Archive Rows Warnings", 
                ExportPackageDialog.isSuppressMessageCanArchiveRows());
        
        this._canArchiveRowsCB.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		ExportPackageDialog.setSuppressMessageCanArchiveRows(
                        _canArchiveRowsCB.isSelected());
        	}
        });        
        
        JPanel leftPanel = new JPanel(new GridLayout(6, 1));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Editing Features"));
        leftPanel.add(this._semanticTypeCB);
        leftPanel.add(this._packageNotExportableCB);
        leftPanel.add(this._canArchiveRowsCB);            

        setLayout(new BorderLayout(8, 0));
        add(BorderLayout.CENTER, leftPanel);
        
        
    }


    

    

}
