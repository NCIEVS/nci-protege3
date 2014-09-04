/*
 * Contributor(s): Sean Falconer seanf@uvic.ca
 * 	               
 */

package edu.stanford.smi.protegex.prompt.ui.plugin;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.prompt.plugin.PluginFacade;
import edu.stanford.smi.protegex.prompt.plugin.PluginManager;
import edu.stanford.smi.protegex.prompt.plugin.model.PromptPlugin;

/**
 * Dialog for setting Prompt plugin visibilities
 * 
 * @author seanf
 */
public class PromptPluginManagerDialog extends JDialog {
	private static final long serialVersionUID = 4080636244468014066L;

	/** the table that displays all the UI plugins */
	private JTable pluginTable;

	/** buttons to close the dialog */
	private JButton okButton;
	private JButton cancelButton;

	public PromptPluginManagerDialog(Frame frame, String title, boolean modal) {
		super(frame, title, modal);

		try {
			setLayout(new BorderLayout());

			init();
			pack();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init() {
		pluginTable = new JTable();
		PluginManager pluginManager = PluginManager.getInstance();
		PluginTableModel pluginTableModel = new PluginTableModel(pluginManager.getPlugins(PluginManager.PLUGIN_UI_MAP));
		pluginTable.setModel(pluginTableModel);

		this.okButton = new JButton("OK");
		this.cancelButton = new JButton("Cancel");

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		this.initActions();

		JScrollPane scrollPane = new JScrollPane(pluginTable);
		this.add(scrollPane, BorderLayout.CENTER);
		//this.add(new JLabel("Plugin Activation"), BorderLayout.NORTH);
		this.add(buttonPanel, BorderLayout.SOUTH);
	}

	private void initActions() {
		final PromptPluginManagerDialog dialog = this;

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				savePluginVisibilities();
				dialog.setVisible(false);
				dialog.dispose();
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
	}

	private void savePluginVisibilities() {
		//PluginTableModel tableModel = (PluginTableModel)pluginTable.getModel();
		PluginFacade.savePluginConfig();
	}
}

class PluginTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -5580722045513641358L;

	private static final int COLUMN_COUNT = 2;

	private static final String[] COLUMN_NAMES = new String[] { "Plugin name", "Visible" };

	private LinkedList<PromptPlugin> pluginList;
	private PluginManager pluginManager;

	public PluginTableModel() {}

	public PluginTableModel(LinkedList<PromptPlugin> pluginList) {
		this.pluginList = pluginList;
		this.pluginManager = PluginManager.getInstance();
	}

	public LinkedList<PromptPlugin> getPluginList() {
		return pluginList;
	}

	public String getColumnName(int col) {
		return COLUMN_NAMES[col];
	}

	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	public int getRowCount() {
		return pluginList.size();
	}

	public void setValueAt(Object value, int row, int col) {
		PluginFacade.setPluginVisibility(getValueAt(row, 0).toString(), Boolean.parseBoolean(value.toString()));
	}

	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	public boolean isCellEditable(int row, int col) {
		Log.getLogger().info("" + col);
		if (col > 0) {
			return true;
		}
		return false;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return pluginList.get(rowIndex).getPluginName();
		}

		return new Boolean(PluginFacade.isPluginVisible(pluginList.get(rowIndex).getPluginName()));
	}
}
