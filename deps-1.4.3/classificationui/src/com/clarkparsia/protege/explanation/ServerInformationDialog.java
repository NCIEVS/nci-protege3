package com.clarkparsia.protege.explanation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.clarkparsia.protege.explanation.util.TabProperties;

import edu.stanford.smi.protege.util.ComponentUtilities;


public class ServerInformationDialog extends JDialog {
	private ExplanationTab mApp;
	private Properties mProperties;

	private ArrayList<String> mSortedProperties;
	
	private static final int DEFAULT_WIDTH = 640;
	private static final int DEFAULT_HEIGHT = 480;

	public ServerInformationDialog(ExplanationTab theApp,
			Properties theProperties) {
		super((JFrame) null, theApp.getTabProperties().get(TabProperties.ResourceKey.DialogServerInformationTitle));
		mApp = theApp;
		mProperties = theProperties;

		mSortedProperties = new ArrayList<String>();
		
		for (Enumeration e = mProperties.propertyNames();
		     e.hasMoreElements(); ) {
			mSortedProperties.add((String) e.nextElement());
		}
		
		Collections.sort(mSortedProperties);

		initGUI();
		
		setSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		
		ComponentUtilities.center(this);
	}

	private void initGUI() {
		getContentPane().setLayout(new BorderLayout());
		
		JTable aTable = new JTable(new PropertyTableModel());
		aTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		aTable.setDefaultRenderer(String.class, new PropertyTableCellRenderer());
		
		JScrollPane aScrollPane = new JScrollPane(aTable);
		
		getContentPane().add(aScrollPane, BorderLayout.CENTER);
		
		JButton aCloseButton = new JButton(mApp.getTabProperties().get(TabProperties.ResourceKey.ButtonCloseLabel));
		aCloseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}			
		});
		
		JPanel aButtonPanel = new JPanel();
		aButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		aButtonPanel.add(aCloseButton);
		
		getContentPane().add(aButtonPanel, BorderLayout.SOUTH);
	}
	
	private class PropertyTableModel extends AbstractTableModel {
		private final String[] COLUMN_NAMES = new String[] { "Property name", "Value" }; 
		
		@Override
		public Class getColumnClass(int columnIndex) {
			return String.class;
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			return COLUMN_NAMES[columnIndex];
		}
		
		public int getColumnCount() {
			return COLUMN_NAMES.length;
		}

		public int getRowCount() {
			return mSortedProperties.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return mSortedProperties.get(rowIndex);
			} else if (columnIndex == 1) {
				return mProperties.get(mSortedProperties.get(rowIndex));
			}
			
			return null;
		}		
	}
	
	private class PropertyTableCellRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
			
			Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			if ((component instanceof JComponent) && (value instanceof String)) {
				JComponent aComponent = (JComponent) component;
				String stringValue = (String) value;
				
				aComponent.setToolTipText(stringValue);
			}
			
			return component;
		}
	}
}
