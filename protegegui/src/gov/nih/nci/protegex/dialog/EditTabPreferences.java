/**
 * 
 */
package gov.nih.nci.protegex.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.ValidatableTabComponent;
import edu.stanford.smi.protegex.owl.model.NamespaceManager;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.util.StringUtil;
import gov.nih.nci.protegex.util.UIUtil;

/**
 * @author bitdiddle
 * 
 */
public class EditTabPreferences extends ValidatableTabComponent {

	public static final long serialVersionUID = 1134568792L;

	private ConfigureEditTabPanels configureTabsPanel;

	private UISettingsPanel uiSettingsPanel;

	// these are specific to the workflow plugin
	private NamespaceManager namespaceMgr;
	private JComboBox namespacePrefixes;
	private JLabel namespaceSelected;
	private String wiki_url;
	private String namespacePrefix;

	public EditTabPreferences(NCIEditTab tab, String url, String nsPrefix) {
		namespaceMgr = tab.getOWLModel().getNamespaceManager();
		wiki_url = url;
		namespacePrefix = nsPrefix;
		addComponents(tab);

	}

	public void initNameSpaces(NCIEditTab tab) {

		namespacePrefixes = new JComboBox();
		namespacePrefixes.addActionListener(new UpdateNamespaceHandler());

		Collection<String> list = namespaceMgr.getPrefixes();

		ArrayList<Object> sortedList = new ArrayList<Object>(list);
		sortedList = UIUtil.sort(sortedList);
		Iterator<Object> iterator2 = sortedList.iterator();
		while (iterator2.hasNext()) {
			String pf = (String) iterator2.next();
			if (!(pf.length() == 0)) {

				namespacePrefixes.addItem(pf);
			} else {
				String cpf = tab.getSMWPrefix(namespaceMgr
						.getDefaultNamespace());
				namespacePrefixes.addItem(cpf);
			}
		}
		namespacePrefixes.setSelectedItem(namespacePrefix);
	}

	private void addComponents(NCIEditTab tab) {

		addTab("General", createGeneralTab(tab));
		configureTabsPanel = new ConfigureEditTabPanels(tab);
		addTab("Panels", configureTabsPanel);

	}

	private JPanel createGeneralTab(NCIEditTab tab) {
		JPanel generalTab = new JPanel();
		generalTab.setLayout(new BoxLayout(generalTab, BoxLayout.Y_AXIS));

		uiSettingsPanel = new UISettingsPanel();
		generalTab.add(uiSettingsPanel);

		JPanel reasonerPanel = new JPanel(new BorderLayout(0, 8));
		reasonerPanel.setBorder(BorderFactory.createTitledBorder("Workflow"));
		reasonerPanel.add(BorderLayout.NORTH, new LabeledComponent("WIKI URL",
				createValidatorField(), false));

		namespaceSelected = new JLabel();
		namespaceSelected.setBorder(new EtchedBorder());
		reasonerPanel.add(BorderLayout.CENTER, namespaceSelected);
		Box iconsPanel = Box.createHorizontalBox();
		iconsPanel.add(new JLabel("Namespace Prefixes: "));

		initNameSpaces(tab);

		iconsPanel.add(namespacePrefixes);
		iconsPanel.add(Box.createHorizontalGlue());
		reasonerPanel.add(BorderLayout.SOUTH, iconsPanel);

		generalTab.add(reasonerPanel);

		JPanel container = new JPanel(new BorderLayout());
		container.add(BorderLayout.NORTH, generalTab);
		// container.add(BorderLayout.CENTER, new JPanel());
		return container;
	}

	private JTextField createValidatorField() {

		final JTextField validatorField = new JTextField(wiki_url);

		validatorField.setPreferredSize(new Dimension(300, 25));

		validatorField.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent arg0) {
			}

			public void focusLost(FocusEvent arg0) {
				wiki_url = validatorField.getText();
				// now set te field in the edit tab
			}
		});

		return validatorField;
	}

	public boolean getRequiresReloadUI() {
		return configureTabsPanel.getRequiresReloadUI();
	}

	public void ok() {
		configureTabsPanel.saveContents();
	}

	/**
	 * This class handles updating namespace label.
	 * 
	 * @author David Yee
	 */
	private class UpdateNamespaceHandler implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			String prefix = (String) namespacePrefixes.getSelectedItem();
			String ns = namespaceMgr.getNamespaceForPrefix(prefix);
			namespaceSelected.setText(ns);
		}
	}

	/**
	 * Returns the Wiki URL.
	 * 
	 * @return the Wiki URL.
	 */
	public String getURL() {
		return StringUtil.stripSlashfromURL(wiki_url);
	}

	/**
	 * Returns the Wiki namespace prefix.
	 * 
	 * @return the Wiki namespace prefix.
	 */
	public String getNSPrefix() {
		String nsPrefix = (String) namespacePrefixes.getSelectedItem();
		if (nsPrefix != null) {
			nsPrefix = nsPrefix.toUpperCase();
		}
		return nsPrefix;
	}
}
