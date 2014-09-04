package gov.nih.nci.protegex.dialog;

import static gov.nih.nci.protegex.tree.TreeItem.TreeItemType.TYPE_RESTRICTION;
import edu.stanford.smi.protege.util.Log;
import gov.nih.nci.protegex.tree.TreeItem;
import gov.nih.nci.protegex.tree.TreeItems;
import gov.nih.nci.protegex.tree.TreePanel;
import gov.nih.nci.protegex.util.ComplexPropertyParser;
import gov.nih.nci.protegex.util.QuickSortVecStrings;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TreePanelReviewDialog extends JFrame implements ActionListener {
	private static final long serialVersionUID = 123456031L;

	private static final String NL = "\n";

	private JButton closeButton;

	private JTextArea textArea;

	private TreePanel _tree = null;

	private ArrayList<TreeItem> _ann_props = new ArrayList<TreeItem>();

	private ArrayList<TreeItem> _complex_props = new ArrayList<TreeItem>();

	private ArrayList<TreeItem> _parents = new ArrayList<TreeItem>();

	private ArrayList<TreeItem> _restrictions = new ArrayList<TreeItem>();

	private ArrayList<TreeItem> _associations = new ArrayList<TreeItem>();

	private Vector<String> classList = null;

	public TreePanelReviewDialog(TreePanel p) {
		super("Review " + p.getDisplayName());
		_tree = p;
		classList = new Vector<String>();
		init();
	}

	private void initTreeItems() {

		TreeItems items = _tree.getCurrentState();

		for (int i = 0; i < items.size(); i++) {
			TreeItem ti = (TreeItem) items.elementAt(i);
			switch (ti.getType()) {
			case TYPE_CONCEPT:
				break;
			case TYPE_PROPERTY:
				if (ti.getValue().startsWith("<")) {
					_complex_props.add(ti);
				} else {
					_ann_props.add(ti);
				}
				break;
			case TYPE_PARENT:
				_parents.add(ti);
				break;
			case TYPE_RESTRICTION:
				_restrictions.add(ti);
				break;
			case TYPE_ASSOCIATION:
				_associations.add(ti);
				break;
			default:
				System.out.println("Exception this should not happen");
				break;
			}
			// System.out.println(vf.elementAt(i));
		}
	}

	public void init() {
		Container contain = this.getContentPane();
		this.setLocation(360, 300);
		contain.setLayout(new BorderLayout());

		textArea = new JTextArea(_tree.getDisplayName());
		boolean wrap = true;
		textArea.setLineWrap(wrap);
		textArea.getDropTarget().setActive(false);

		String textAreaText = "";
		String conceptDetail = "";
		getCls(0);

		for (int i = 0; i < classList.size(); i++) {
			conceptDetail = (String) classList.elementAt(i);
			// if (_tab.printable(conceptDetail)) {
			textAreaText += conceptDetail;
			// }
		}
		textArea.setText(textAreaText);
		textArea.setEditable(false);

		JScrollPane reportPane = new JScrollPane(textArea);
		reportPane.setPreferredSize(new Dimension(800, 500));

		// midPanel.add(reportPane, BorderLayout.CENTER);
		contain.add(reportPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton);

		contain.add(buttonPanel, BorderLayout.SOUTH);
		pack();

		setLocation(100, 100);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent event) {
		Object action = event.getSource();
		if (action == closeButton) {
			dispose();
		}
	}

	private void getCls(int level) {

		initTreeItems();

		String tabString = "";

		for (int i = 0; i < level; i++) {
			tabString += "\t";
		}

		classList.clear();
		try {
			classList.add(NL);
			classList.add(tabString + "Annotation Properties:" + NL);

			ArrayList<TreeItem> propsToRemove = new ArrayList<TreeItem>();
			for (TreeItem ti : _ann_props) {
				if (ti.getName().equalsIgnoreCase("code")) {
					classList.add("\t" + "code :" + "\t" + ti.getValue() + NL);
					propsToRemove.add(ti);
				}
				if (ti.getName().equalsIgnoreCase("Preferred_Name")) {
					classList.add("\t" + "Preferred_Name:" + "\t"
							+ ti.getValue() + NL);
					propsToRemove.add(ti);

				}

				

			}
			_ann_props.removeAll(propsToRemove);

			classList.add(NL);

			sortData();
		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Exception caught", e);
		}

	}

	private void sortData() {
		sortAnnotationData(_ann_props);
		
		sortAnnotationData(_complex_props);

		sortConditionData("Named Superclasses:", _parents);

		sortConditionData("Restrictions:", _restrictions);

		sortObjectValuedAnnotationData();

	}

	private void sortAnnotationData(ArrayList<TreeItem> items) {

		Vector<String> v = new Vector<String>();

		for (TreeItem ti : items) {
			v.add(ti.getName() + ": " + ti.getValue());
		}

		QuickSortVecStrings qsvs = new QuickSortVecStrings();
		try {
			qsvs.sort(v);
			for (int i = 0; i < v.size(); i++) {
				String s = (String) v.elementAt(i);

				int pos = s.indexOf(":");
				String name = s.substring(0, pos);
				String value = s.substring(pos + 1, s.length());

				HashMap<String, String> map = ComplexPropertyParser.parseXML(value);
				String text = "";
				if (map.containsKey("def-definition")) {
					text = (String) map.get("def-definition");
					classList.add("\t" + name + ": " + text + NL);

					Iterator it = map.keySet().iterator();
					while (it.hasNext()) {
						String qName = (String) it.next();
						if (qName.compareTo("def-definition") != 0
								&& qName.compareTo("root") != 0) {
							String qValue = (String) map.get(qName);
							classList.add("\t\t" + qName + ": " + qValue + NL);
						}
					}

				} else if (map.containsKey("go-term")) {
					text = (String) map.get("go-term");
					classList.add("\t" + name + ": " + text + NL);

					Iterator it = map.keySet().iterator();
					while (it.hasNext()) {
						String qName = (String) it.next();
						if (qName.compareTo("go-term") != 0
								&& qName.compareTo("root") != 0) {
							String qValue = (String) map.get(qName);
							classList.add("\t\t" + qName + ": " + qValue + NL);
						}
					}
				} else if (map.containsKey("def-definition")) {
					text = (String) map.get("def-definition");
					classList.add("\t" + name + ": " + text + NL);

					Iterator it = map.keySet().iterator();
					while (it.hasNext()) {
						String qName = (String) it.next();
						if (qName.compareTo("def-definition") != 0
								&& qName.compareTo("root") != 0) {
							String qValue = (String) map.get(qName);
							classList.add("\t\t" + qName + ": " + qValue + NL);
						}
					}
				} else if (map.containsKey("term-name")) {
					text = (String) map.get("term-name");
					classList.add("\t" + name + ": " + text + NL);

					Iterator it = map.keySet().iterator();
					while (it.hasNext()) {
						String qName = (String) it.next();
						if (qName.compareTo("term-name") != 0
								&& qName.compareTo("root") != 0) {
							String qValue = (String) map.get(qName);
							classList.add("\t\t" + qName + ": " + qValue + NL);
						}
					}
				} else {
					classList.add("\t" + s + NL);
				}
			}
			classList.add(NL);
		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Exception caught", e);
		}
	}

	private void sortObjectValuedAnnotationData() {

		String label = "Object-Valued Annotation Properties (Associations)";
		classList.add(NL);
		classList.add(label + NL);

		Vector<String> v = new Vector<String>();

		for (TreeItem ti : _associations) {
			v.add(ti.getName() + "\t" + ti.getValue());
		}

		QuickSortVecStrings qsvs = new QuickSortVecStrings();
		try {
			qsvs.sort(v);
			for (int i = 0; i < v.size(); i++) {
				String s = (String) v.elementAt(i);
				classList.add("\t" + s + NL);
			}
		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Exception caught", e);
		}
	}

	private void sortConditionData(String label, ArrayList<TreeItem> items) {
		classList.add(NL);
		classList.add(label + NL);

		Vector<String> v = new Vector<String>();

		for (TreeItem ti : items) {
			if (ti.getType() == TYPE_RESTRICTION) {
				v.add(ti.getNameValue());
			} else {
				v.add(ti.getValue());
			}
		}

		QuickSortVecStrings qsvs = new QuickSortVecStrings();
		try {
			qsvs.sort(v);
			for (int i = 0; i < v.size(); i++) {
				String s = (String) v.elementAt(i);
				classList.add("\t" + s + NL);
			}
		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Exception caught", e);
		}
	}

}
