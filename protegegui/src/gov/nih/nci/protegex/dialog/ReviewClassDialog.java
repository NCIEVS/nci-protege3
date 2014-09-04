package gov.nih.nci.protegex.dialog;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;
import gov.nih.nci.protegex.edit.NCIAnnotationsTableModel;
import gov.nih.nci.protegex.edit.NCIConditionsTableItem;
import gov.nih.nci.protegex.edit.NCIConditionsTableModel;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.edit.NCIFULLSYNTableModel;
import gov.nih.nci.protegex.edit.OWLWrapper;
import gov.nih.nci.protegex.panel.EditPanel;
import gov.nih.nci.protegex.util.ComplexPropertyParser;
import gov.nih.nci.protegex.util.QuickSortVecStrings;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
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

public class ReviewClassDialog extends JFrame implements ActionListener
{
    private static final long serialVersionUID = 123456031L;
    private static final String NL = "\n";
    private JButton closeButton;
	private JTextArea textArea;
    private NCIEditTab tab;
    private OWLNamedClass cls = null;
    private OWLWrapper wrapper = null;
    private Vector<String> classList = null;
    private EditPanel editpanel = null;

	private NCIFULLSYNTableModel synonym_tablemodel;
	private NCIAnnotationsTableModel definition_tablemodel;
	private NCIAnnotationsTableModel property_tablemodel;
	private NCIAnnotationsTableModel association_tablemodel;
    private NCIAnnotationsTableModel complex_property_tablemodel;
	private NCIConditionsTableModel restriction_tablemodel;
	private NCIConditionsTableModel superconcept_tablemodel;

    public ReviewClassDialog(NCIEditTab tab, EditPanel editpanel, OWLNamedClass cls)
    {
    	//super();
    	
		super( "Review " + cls.getBrowserText());
		
    	//this.setTitle(public int getHeight() {
    	
		//this.setModalityType()		
		//this.setAlwaysOnTop(false);
		//this.setResizable(true);
		
		
		this.tab = tab;
		this.wrapper = tab.getWrapper();
		this.cls = cls;
		this.classList = new Vector<String>();

		//this.editpanel = tab.getEditPanel();
		this.editpanel = editpanel;

		this.synonym_tablemodel = editpanel.get_synonym_tablemodel();
		this.definition_tablemodel = editpanel.get_definition_tablemodel();
		this.property_tablemodel = editpanel.get_property_tablemodel();
		this.complex_property_tablemodel = editpanel.get_complex_property_tablemodel();

		this.association_tablemodel = editpanel.get_association_tablemodel();
		this.restriction_tablemodel = editpanel.get_restriction_tablemodel();
		this.superconcept_tablemodel = editpanel.get_superconcept_tablemodel();

		//this.cls = (OWLNamedClass) synonym_tablemodel.getSubject();
		init();
	}


	public void init()
	{
		Container contain = this.getContentPane();
		this.setLocation(360,300);
		contain.setLayout(new BorderLayout());

		textArea = new JTextArea(cls.getBrowserText());
		boolean wrap = true;
		textArea.setLineWrap(wrap);
		textArea.getDropTarget().setActive(false);

		String textAreaText="";
		String conceptDetail="";
		getCls(cls, 0);

		for(int i=0;i<classList.size();i++){
			conceptDetail=(String)classList.elementAt(i);
			if (tab.printable(conceptDetail))
			{
				textAreaText+=conceptDetail;
		    }
		}
		textArea.setText(textAreaText);
		textArea.setEditable(false);

		JScrollPane reportPane = new JScrollPane(textArea);
		reportPane.setPreferredSize(new Dimension (800, 500));

		//midPanel.add(reportPane, BorderLayout.CENTER);
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


	private void getCls(Cls superCls, int level) {

		if (superCls == null)
		{
			return;
		}

		String tabString="";

		for(int i=0;i<level;i++){
			tabString+="\t";
		}

        classList.clear();
		try{
			classList.add(NL);
			classList.add(tabString + "Annotation Properties:" + NL);

			Vector v = getOwnslotValues(NCIEditTab.CODE);
			for (int i=0; i<v.size(); i++)
			{
				String s = (String) v.elementAt(i);
				classList.add(s);
			}

			String pt = (String) editpanel.getPreferredName();
			String s = "\t" + "Preferred_Name:" + "\t" + pt + NL;
			classList.add(s);

			classList.add(NL);

			sortData();
		}
		catch(Exception e){
			Log.getLogger().log(Level.WARNING, "Exception caught", e);
		}

	}

    private Vector getOwnslotValues(String slotname)
    {
		Vector<String> v = new Vector<String>();
		RDFProperty slot = wrapper.getRDFProperty(slotname);
		if (slot == null) return v;
		Collection values = cls.getPropertyValues(slot);
		if(values == null || values.isEmpty()) return v;
		Iterator i = values.iterator();
		while(i.hasNext()) {
			Object obj = i.next();
			ValueType type = wrapper.getObjectValueType(obj);
			String entry = wrapper.convertObjecttoString(obj, type);
			try {
				String s = "\t" + slotname + "\t" + entry + NL;
				v.add(s);
			}
			catch(Exception e){
				Log.getLogger().log(Level.WARNING, "Exception caught", e);
				return new Vector();
			}
		}
        return v;
	}

	public void actionPerformed(ActionEvent event)
	{
		Object action = event.getSource();
		if (action == closeButton){
			dispose();
		}
	}

    private void sortData()
    {
        sortAnnotationData((ArrayList) property_tablemodel.getProperties(),
                 (ArrayList) property_tablemodel.getValues());

        sortAnnotationData((ArrayList) synonym_tablemodel.getProperties(),
                 (ArrayList) synonym_tablemodel.getValues());

        sortAnnotationData((ArrayList) definition_tablemodel.getProperties(),
                 (ArrayList) definition_tablemodel.getValues());

        sortAnnotationData((ArrayList) complex_property_tablemodel.getProperties(),
                 (ArrayList) complex_property_tablemodel.getValues());

		sortConditionData("Named Superclasses:", (ArrayList) superconcept_tablemodel.getItems());

        sortConditionData("Restrictions:", (ArrayList) restriction_tablemodel.getItems());

		sortObjectValuedAnnotationData((ArrayList) association_tablemodel.getProperties(),
		         (ArrayList) association_tablemodel.getValues());

	}

    private void sortAnnotationData(ArrayList properties, ArrayList values)
    {
        Vector<String> v = new Vector<String>();
        for (int i=0; i<properties.size(); i++)
        {
			RDFProperty property = (RDFProperty) properties.get(i);

			// 080306
			//String value = (String) values.get(i);
			Object value_obj = (Object) values.get(i);
			String value = "";
			if (value_obj instanceof RDFSLiteral) {
				value = ((RDFSLiteral) value_obj).getString();
            }
            else
            {
                if (value_obj != null)
                    value = value_obj.toString();
                else value = "NULL";
			}

			String s = property.getBrowserText() + ": " + value;
			v.add(s);
		}

        QuickSortVecStrings qsvs = new QuickSortVecStrings();
        try {
			qsvs.sort(v);
			for (int i=0; i<v.size(); i++)
			{
				String s = (String) v.elementAt(i);

				int pos = s.indexOf(":");
				String name = s.substring(0, pos);
				String value = s.substring(pos+1, s.length());

				HashMap<String, String> map = ComplexPropertyParser.parseXML(value);
				String text = "";
				if (map.containsKey("def-definition"))
				{
					text = (String) map.get("def-definition");
					classList.add("\t" + name + ": " + text + NL);

                    Iterator it = map.keySet().iterator();
                    while (it.hasNext())
                    {
					    String qName = (String) it.next();
					    if (qName.compareTo("def-definition") != 0 && qName.compareTo("root") != 0)
					    {
							String qValue = (String) map.get(qName);
							classList.add("\t\t" + qName + ": " + qValue + NL);
						}
					}

				}
				else if (map.containsKey("go-term"))
				{
					text = (String) map.get("go-term");
					classList.add("\t" + name + ": " + text + NL);

                    Iterator it = map.keySet().iterator();
                    while (it.hasNext())
                    {
					    String qName = (String) it.next();
					    if (qName.compareTo("go-term") != 0 && qName.compareTo("root") != 0)
					    {
							String qValue = (String) map.get(qName);
							classList.add("\t\t" + qName + ": " + qValue + NL);
						}
					}
				}
				else if (map.containsKey("def-definition"))
				{
					text = (String) map.get("def-definition");
					classList.add("\t" + name + ": " + text + NL);

                    Iterator it = map.keySet().iterator();
                    while (it.hasNext())
                    {
					    String qName = (String) it.next();
					    if (qName.compareTo("def-definition") != 0 && qName.compareTo("root") != 0)
					    {
							String qValue = (String) map.get(qName);
							classList.add("\t\t" + qName + ": " + qValue + NL);
						}
					}
				}
				else if (map.containsKey("term-name"))
				{
					text = (String) map.get("term-name");
					classList.add("\t" + name + ": " + text + NL);

                    Iterator it = map.keySet().iterator();
                    while (it.hasNext())
                    {
					    String qName = (String) it.next();
					    if (qName.compareTo("term-name") != 0 && qName.compareTo("root") != 0)
					    {
							String qValue = (String) map.get(qName);
							classList.add("\t\t" + qName + ": " + qValue + NL);
						}
					}
				}
				else
				{
					classList.add("\t" + s + NL);
				}
			}
			classList.add(NL);
	    } catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Exception caught", e);
		}
	}

    private void sortObjectValuedAnnotationData(ArrayList properties, ArrayList values)
    {
		String label = "Object-Valued Annotation Properties (Associations)";
		classList.add(NL);
		classList.add(label + NL);

        Vector<String> v = new Vector<String>();
        for (int i=0; i<properties.size(); i++)
        {
			RDFProperty property = (RDFProperty) properties.get(i);
			OWLNamedClass value = (OWLNamedClass) values.get(i);

			String s = property.getBrowserText() + "\t" + value.getBrowserText();
			v.add(s);
		}

        QuickSortVecStrings qsvs = new QuickSortVecStrings();
        try {
			qsvs.sort(v);
			for (int i=0; i<v.size(); i++)
			{
				String s = (String) v.elementAt(i);
				classList.add("\t" + s + NL);
			}
	    } catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Exception caught", e);
		}
	}

    private void sortConditionData(String label, ArrayList items)
    {
		classList.add(NL);
		classList.add(label + NL);

        Vector<String> v = new Vector<String>();
        for (int i=0; i<items.size(); i++)
        {
			NCIConditionsTableItem item = (NCIConditionsTableItem) items.get(i);
			String s = item.getDisplayText();
			if (item.isDefining()) s = s + "   [defining]";
			//121406
			if (item.isInherited()) s = s + "   [inherited]";

			v.add(s);
		}

        QuickSortVecStrings qsvs = new QuickSortVecStrings();
        try {
			qsvs.sort(v);
			for (int i=0; i<v.size(); i++)
			{
				String s = (String) v.elementAt(i);
				classList.add("\t" + s + NL);
			}
	    } catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Exception caught", e);
		}
	}
}
