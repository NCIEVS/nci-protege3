package gov.nih.nci.protegex.edit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.ui.FrameComparator;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.widget.ClsWidget;
import edu.stanford.smi.protegex.owl.model.OWLAllValuesFrom;
import edu.stanford.smi.protegex.owl.model.OWLHasValue;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLNames;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.OWLRestriction;
import edu.stanford.smi.protegex.owl.model.OWLSomeValuesFrom;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLAllValuesFrom;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLHasValue;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLSomeValuesFrom;
import edu.stanford.smi.protegex.owl.model.impl.OWLUtil;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.dialogs.ModalDialogFactory;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import edu.stanford.smi.protegex.owl.ui.profiles.ProfilesManager;
import edu.stanford.smi.protegex.owl.ui.resourcedisplay.InstanceNameComponent;
import edu.stanford.smi.protegex.owl.ui.resourceselection.ResourceSelectionAction;
import edu.stanford.smi.protegex.owl.ui.restrictions.RestrictionKindRenderer;
import edu.stanford.smi.protegex.owl.ui.search.ResourceListFinder;
import gov.nih.nci.protegex.util.MsgDialog;

/**
 * A panel that allows to completely define a restriction. This displays a searchable propertyList of slots,
 * radiobuttons to select the restriction kind, and a textfield that is supported by a symbol panel.
 * 
 * @author Holger Knublauch <holger@knublauch.com>
 */
public class NCICreateRolePanel extends JComponent implements ActionListener {

    private Logger logger = Log.getLogger();

    private static final long serialVersionUID = 123456036L;

    private Action createRDFPropertyAction;

    private JList modifierList;

    private OWLModel owlModel;

    private OWLWrapper wrapper = OWLWrapper.getInstance();

    private JList propertyList;

    private RDFSClass targetClass;

    private Action viewAction;

    private JButton okButton;

    private JComponent parentComponent;

    private edu.stanford.smi.protege.model.Cls metaCls = null;

    private JTextField filler_field;

    private Cls selectedCls = null;

    private String selectedResourceName = "";

    private RDFProperty property;

    private JButton selectClsButton;

    private JButton selectIndividualButton;

    private OWLRestriction restriction = null;

    private String fillerText;

    JCheckBox definingCheckBox = null;

    public boolean getIsDefining() {
        return definingCheckBox.isSelected();
    }

    public void setIsDefining(boolean b) {
        definingCheckBox.setSelected(b);
    }

    public NCICreateRolePanel(JComponent parent, edu.stanford.smi.protege.model.Cls metaCls, RDFProperty property,
            String fillerText, RDFSClass targetClass, OWLRestriction r, boolean allowIsDefining) {

        this.owlModel = NCIEditTab.getActiveOWLModel();
        this.parentComponent = parent;
        this.targetClass = targetClass;
        this.metaCls = metaCls;
        this.property = property;
        this.restriction = r;
        // Note: This fillerText is the browserText as opposed to internalName.
        // This fillerText is not needed if the restriction is set.
        this.fillerText = fillerText;

        initialize(allowIsDefining);
    }

    private void debug(String text) {
        logger.log(Level.FINE, "NCICreateRolePanel: " + text);
    }

    private void initialize(boolean allowIsDefining) {
        createActions();

        propertyList = ComponentFactory.createList(null);
        propertyList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        propertyList.setCellRenderer(FrameRenderer.createInstance());
        propertyList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                changeProperty((RDFProperty) propertyList.getSelectedValue());
                enableActions();
            }
        });
        propertyList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && viewAction.isEnabled()) {
                    viewAction.actionPerformed(null);
                }
            }
        });

        updatePropertiesList();

        // initial selection of property
        propertyList.setSelectedValue(property, true);

        JPanel propertyPanel = new JPanel(new BorderLayout());
        propertyPanel.setLayout(new BorderLayout());
        JScrollPane propertyScrollPane = new JScrollPane(propertyList);
        propertyScrollPane.setPreferredSize(new Dimension(240, 150));
        propertyPanel.add(BorderLayout.CENTER, propertyScrollPane);
        propertyPanel.add(BorderLayout.SOUTH, new ResourceListFinder(propertyList, "Find"));

        edu.stanford.smi.protege.model.Cls[] metaClses = ProfilesManager.getSupportedRestrictionMetaClses(owlModel);
        modifierList = new JList(metaClses);
        modifierList.setCellRenderer(new RestrictionKindRenderer());
        modifierList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // initial selection of modifier
        modifierList.setSelectedValue(metaCls, true);

        JScrollPane modifierScrollPane = new JScrollPane(modifierList);
        modifierScrollPane.setPreferredSize(new Dimension(160, 150));
        modifierList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                changeMetaClsSelection((edu.stanford.smi.protege.model.Cls) modifierList.getSelectedValue());
            }
        });

        JPanel definingPanel = new JPanel();
        definingPanel.setLayout(new BorderLayout());

        JPanel kindPanel = new JPanel();
        kindPanel.setLayout(new BorderLayout());
        kindPanel.add(BorderLayout.NORTH, new LabeledComponent("Restriction", modifierScrollPane));

        if (allowIsDefining) {
            JLabel definingLabel = new JLabel("  Defining");
            definingCheckBox = new JCheckBox();
            definingCheckBox.setEnabled(true);

            definingCheckBox.setSelected(false);
            definingPanel.add(definingCheckBox, BorderLayout.WEST);
            definingPanel.add(definingLabel, BorderLayout.CENTER);
            kindPanel.add(BorderLayout.CENTER, definingPanel);
        }

        final OWLModel owl_model = owlModel;

        fillerText = trimFillerText(fillerText);
        filler_field = new JTextField("");

        setInitialFillerValue();

        filler_field.setEditable(false);
        filler_field.setPreferredSize(new Dimension(300, 20));
        LabeledComponent lc = new LabeledComponent("Filler", filler_field);

        Action SelectPropertyValueAction = new AbstractAction("Select a Named Class (filler)", OWLIcons
                .getAddIcon("PrimitiveClass")) {
            private static final long serialVersionUID = 1234560360L;

            public void actionPerformed(ActionEvent e) {
                // java.awt.TextField textfield = new TextField();
                String label = "Select a named class";

                //Collection clses = wrapper.getSelectableRoots();
                
                RDFProperty propsel = (RDFProperty) propertyList.getSelectedValue();
                
                Collection clses = propsel.getUnionRangeClasses();
                
                if ((clses.size() == 1) && clses.contains(owlModel.getOWLThingClass())) {
                    clses = wrapper.getSelectableRoots();
                }
                
                if (clses.size() == 0) {
                    clses = wrapper.getSelectableRoots();
                }

                final NCISelectClsesPanel p = new NCISelectClsesPanel(owl_model, clses);
                int result = ModalDialog.showDialog(parentComponent, p, label, ModalDialog.MODE_OK_CANCEL);
                if (result == ModalDialogFactory.OPTION_OK) {
                    Collection c = p.getSelection();
                    if (c != null && c.size() > 0) {
                        Iterator it = c.iterator();
                        Object obj = it.next();
                        selectedCls = (Cls) obj;

                        boolean invaldSelection = wrapper.isPremerged((OWLNamedClass) selectedCls);
                        if (invaldSelection) {
                            MsgDialog.ok(parentComponent, "Cannot select a pre-merged concept.");
                            return;
                        }
                        invaldSelection = wrapper.isPreretired((OWLNamedClass) selectedCls);
                        if (invaldSelection) {
                            MsgDialog.ok(parentComponent, "Cannot select a pre-retired concept.");
                            return;
                        }
                        invaldSelection = wrapper.isRetired((OWLNamedClass) selectedCls);
                        if (invaldSelection) {
                            MsgDialog.ok(parentComponent, "Cannot select a retired concept.");
                            return;
                        }

                        // String name = wrapper.getInternalName(selectedCls);
                        filler_field.setText(selectedCls.getBrowserText());
                    }
                }
            }
        };

        selectClsButton = lc.addHeaderButton(SelectPropertyValueAction);

        Action SelectIndividualAction = new InsertIndividualAction() {
            private static final long serialVersionUID = 1234560361L;

        };

        selectIndividualButton = lc.addHeaderButton(SelectIndividualAction);
        selectIndividualButton.setEnabled(false);

        setLayout(new BorderLayout(10, 10));
        LabeledComponent lc2 = new LabeledComponent("Restricted Property", propertyPanel);
        // lc.addHeaderButton(viewAction);
        if (OWLUtil.hasRDFProfile(owlModel)) {
            lc2.addHeaderButton(createRDFPropertyAction);
        }

        // lc2.addHeaderButton(createDatatypePropertyAction);
        // lc2.addHeaderButton(createObjectPropertyAction);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(BorderLayout.CENTER, lc2);
        panel.add(BorderLayout.EAST, kindPanel);
        panel.add(BorderLayout.SOUTH, lc);

        add(BorderLayout.CENTER, panel);

        if (metaCls != null && property != null) {
            filler_field.setEditable(false);
            selectClsButton.setEnabled(true);
            selectIndividualButton.setEnabled(false);

            if (metaCls.getName().equals(OWLNames.Cls.HAS_VALUE_RESTRICTION)) {
                if (property instanceof OWLObjectProperty) {
                    filler_field.setEditable(false);
                    selectClsButton.setEnabled(false);
                    selectIndividualButton.setEnabled(false);

                } else {
                    filler_field.setEditable(true);
                    filler_field.setText(fillerText);
                    selectClsButton.setEnabled(false);
                    selectIndividualButton.setEnabled(false);
                }
            } else if (metaCls.getName().equals(OWLNames.Cls.MAX_CARDINALITY_RESTRICTION)) {
                filler_field.setEditable(true);
                selectClsButton.setEnabled(false);
                selectIndividualButton.setEnabled(false);
            } else if (metaCls.getName().equals(OWLNames.Cls.MIN_CARDINALITY_RESTRICTION)) {
                filler_field.setEditable(true);
                selectClsButton.setEnabled(false);
                selectIndividualButton.setEnabled(false);
            } else if (metaCls.getName().equals(OWLNames.Cls.CARDINALITY_RESTRICTION)) {
                filler_field.setEditable(true);
                selectClsButton.setEnabled(false);
                selectIndividualButton.setEnabled(false);
            }
        }

        setLocation(260, 200);
        setPreferredSize(new Dimension(500, 320));

    }

    private String trimFillerText(String s) {
        if (s == null)
            return "";
        if (s.length() < 2)
            return s;
        char quote = '"';
        String quote_str = "" + quote;
        if (s.substring(0, 1).compareTo(quote_str) == 0) {
            s = s.substring(1, s.length());
        }
        if (s.substring(s.length() - 1, s.length()).compareTo(quote_str) == 0) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private void addNewProperty(RDFProperty newProperty) {
        newProperty.setFunctional(false);
        newProperty.setDomainDefined(false);
        showModalPropertyWidget(newProperty);
        updatePropertiesList();
        propertyList.setSelectedValue(newProperty, true);
    }

    public boolean canClose(int result) {
        if (result == ModalDialogFactory.OPTION_OK) {
            RDFProperty selectedProperty = (RDFProperty) propertyList.getSelectedValue();
            if (selectedProperty != null) {
                // 091506
                // String uniCodeText = fillerTextArea.getText();
                String uniCodeText = filler_field.getText();
                if (uniCodeText.length() == 0) {
                    // symbolPanel.displayError("Please enter a filler");
                    return false;
                }
            }
        }
        // else {
        return true;
        // }
    }

    public Cls getFillerCls() {
        return selectedCls;
    }

    private void createActions() {

        viewAction = new AbstractAction("View Property...", OWLIcons.getViewIcon()) {
            private static final long serialVersionUID = 1234560362L;

            public void actionPerformed(ActionEvent arg0) {
                RDFProperty property = (RDFProperty) propertyList.getSelectedValue();
                showModalPropertyWidget(property);
            }
        };
        viewAction.setEnabled(false);

        createRDFPropertyAction = new AbstractAction("Create RDF property...", OWLIcons
                .getCreatePropertyIcon(OWLIcons.RDF_PROPERTY)) {
            private static final long serialVersionUID = 1234560363L;

            public void actionPerformed(ActionEvent arg0) {
                RDFProperty newProperty = owlModel.createRDFProperty(null);
                addNewProperty(newProperty);
            }
        };
    }

    private OWLRestriction createRestriction(edu.stanford.smi.protege.model.Cls metaCls, RDFProperty property,
            String text) throws Exception {
        Collection parents = CollectionUtilities.createCollection(((KnowledgeBase) owlModel)
                .getCls(OWLNames.Cls.ANONYMOUS_ROOT));
        KnowledgeBase kb = owlModel;

        try {
            kb.beginTransaction("Creating local restriction on property " + property.getBrowserText() + " with filler "
                    + text + " of type " + metaCls.getBrowserText());

            OWLRestriction restriction = (OWLRestriction) kb.createCls(null, parents, metaCls);
            restriction.setOnProperty(property);

            // Note: restriction could be AbstractOWLCardinalityBase or
            // AbstractOWLQuantifierRestriction. When it is a cardinality
            // the text value could contain "NUMBER CONCEPT_NAME".

            restriction.setFillerText(text);
            /**
            if (restriction instanceof OWLHasValue) {
                RDFResource res = owlModel.getRDFResource(text);
                ((OWLHasValue) restriction).setHasValue(res);
            } else {
                restriction.setFillerText(text);
            }
            **/

            kb.commitTransaction();
            return restriction;
        } catch (Exception e) {
            kb.rollbackTransaction();
            // OWLUI.handleError(owlModel, e);
            MsgDialog.warning(this, "Filler value \"" + text + "\" is invalid.");
        }

        return null;
    }

    private void enableActions() {
        viewAction.setEnabled(true);
    }

    public OWLRestriction getResult() {
        String text = filler_field.getText().trim();
        if (selectedResourceName.length() > 0)
            text = selectedResourceName;

        try {

            if (text.compareTo("") == 0) {
                MsgDialog.ok(null, "No class is selected.");
                return null;
            }

            if (selectedCls != null) {
                return createRestriction(getSelectedMetaCls(), getSelectedProperty(), ((OWLNamedClass) selectedCls)
                        .getPrefixedName());
            } else {

                return createRestriction(getSelectedMetaCls(), getSelectedProperty(), text);
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Unable to create restricton with filler " + text, ex);
            return null;
        }
    }

    edu.stanford.smi.protege.model.Cls getSelectedMetaCls() {
        return (edu.stanford.smi.protege.model.Cls) modifierList.getSelectedValue();
    }

    RDFProperty getSelectedProperty() {
        return (RDFProperty) propertyList.getSelectedValue();
    }

    private void showModalPropertyWidget(RDFProperty property) {
        ClsWidget widget = owlModel.getProject().createRuntimeClsWidget(property);
        InstanceNameComponent nameComponent = new InstanceNameComponent();
        nameComponent.setInstance(property);

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(nameComponent, BorderLayout.NORTH);
        panel.add((JComponent) widget, BorderLayout.CENTER);

        ProtegeUI.getModalDialogFactory().showDialog(this, panel, "Property " + property.getBrowserText(),
                ModalDialogFactory.MODE_CLOSE);
    }

    private void updatePropertiesList() {
        Collection allProperties = owlModel.getVisibleUserDefinedRDFProperties();
        Collection<RDFProperty> selectableProperties = new ArrayList<RDFProperty>();
        Collection parents = ((OWLNamedClass) targetClass).getSuperclasses(true);
        for (Iterator it = allProperties.iterator(); it.hasNext();) {
            RDFProperty property = (RDFProperty) it.next();
            if (!property.isAnnotationProperty()) {
                Collection domains = property.getUnionDomain(true);  
                
                OWLNamedClass oc = (OWLNamedClass) targetClass;                
                 
                Iterator itd = domains.iterator();
                boolean ok = false;
                while (itd.hasNext()) {
                	RDFSClass tc = (RDFSClass) itd.next();
                	if (parents.contains(tc) || targetClass.equalsStructurally(tc)) {
                		ok = true;                        
                    }
                }
                if (ok) {
                	selectableProperties.add(property);
                }
                
                

            }
        }

        RDFSNamedClass dbrClass = owlModel.getSystemFrames().getDirectedBinaryRelationCls();
        if (targetClass != null && ((Cls) targetClass).hasSuperclass(dbrClass)) {
            RDFProperty fromProperty = owlModel.getRDFProperty(Model.Slot.FROM);
            if (fromProperty.isVisible()) {
                selectableProperties.add(fromProperty);
            }
            RDFProperty toProperty = owlModel.getRDFProperty(Model.Slot.TO);
            if (toProperty.isVisible()) {
                selectableProperties.add(toProperty);
            }
        }
        RDFProperty[] propertiesArray = selectableProperties.toArray(new RDFProperty[selectableProperties.size()]);
        Comparator<RDFProperty> c = new Comparator<RDFProperty>() {
            public int compare(RDFProperty o1, RDFProperty o2) {
                return o1.compareTo(o2);

            }

        };
        Arrays.sort(propertiesArray, c);
        java.util.List propertiesList = Arrays.asList(propertiesArray);
        propertyList.setListData(propertiesList.toArray());

        propertyList.setSelectedIndex(0);
    }

    private class InsertIndividualAction extends ResourceSelectionAction {
        private static final long serialVersionUID = 1234560363L;

        InsertIndividualAction() {
            super("Insert individual...", OWLIcons.getImageIcon(OWLIcons.RDF_INDIVIDUAL));
        }

        public void actionPerformed(ActionEvent e) {
            // Collection sels =
            // ProtegeUI.getSelectionDialogFactory().selectResourcesByType(SymbolPanel.this,
            // owlModel,
            selectedCls = null;
            selectedResourceName = "";
            boolean valid = false;
            while (!valid) {
                valid = true;
                Collection sels = ProtegeUI.getSelectionDialogFactory().selectResourcesByType(null, owlModel,
                        Collections.singleton(owlModel.getOWLThingClass()), "Select the resources to insert");
                // not sure why there's a loop as only one is selected
                for (Iterator it = sels.iterator(); it.hasNext();) {
                    Instance frame = (Instance) it.next();
                    if (frame instanceof RDFResource) {

                        if (frame instanceof OWLNamedClass) {
                            selectedCls = (Cls) frame;
                            boolean invaldSelection = wrapper.isPremerged((OWLNamedClass) selectedCls);
                            if (invaldSelection) {
                                MsgDialog.ok(parentComponent, "Cannot select a pre-merged concept.");
                                valid = false;

                            }
                            invaldSelection = wrapper.isPreretired((OWLNamedClass) selectedCls);
                            if (invaldSelection) {
                                MsgDialog.ok(parentComponent, "Cannot select a pre-retired concept.");
                                valid = false;

                            }
                            invaldSelection = wrapper.isRetired((OWLNamedClass) selectedCls);
                            if (invaldSelection) {
                                MsgDialog.ok(parentComponent, "Cannot select a retired concept.");
                                valid = false;

                            }
                        }

                        RDFResource rdf_resource = (RDFResource) frame;
                        resourceSelected((RDFResource) frame);

                        String name = rdf_resource.getBrowserText();
                        filler_field.setText(name);
                        selectedResourceName = rdf_resource.getPrefixedName();

                    }
                }
            }
        }

        public void resourceSelected(RDFResource resource) {
            // insertIndividual(resource);
        }

        public Collection getSelectableResources() {
            Collection frames = owlModel.getOWLIndividuals();
            java.util.List copy = new ArrayList(frames);
            Collections.sort(copy, new FrameComparator());
            return copy;
        }

        public RDFResource pickResource() {
            Collection resources = getSelectableResources();
            // return
            // ProtegeUI.getSelectionDialogFactory().selectResourceFromCollection(SymbolPanel.this,
            return ProtegeUI.getSelectionDialogFactory().selectResourceFromCollection(null, owlModel, resources,
                    "Select the resource to insert");
        }
    }

    public void actionPerformed(ActionEvent event) {
        Object action = event.getSource();
        if (action == okButton) {
            // rolegroupPanel.addRolegroup(this);
            debug("Method: actionPerformed: okButton pressed.");
            // dispose();
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void changeProperty(RDFProperty property) {
        // property is the current selected property

        if (modifierList == null)
            return;

        // modifier

        edu.stanford.smi.protege.model.Cls metaCls = (edu.stanford.smi.protege.model.Cls) modifierList
                .getSelectedValue();
        if (metaCls == null)
            return;

        if (!(property instanceof OWLObjectProperty)) // data type
        {
            modifierList.setSelectedIndex(2);
            if (!(metaCls.getName().equals(OWLNames.Cls.HAS_VALUE_RESTRICTION))) {
                filler_field.setEditable(false);
                selectClsButton.setEnabled(false);
                selectIndividualButton.setEnabled(false);
                return;
            }
            filler_field.setEditable(true);
            selectClsButton.setEnabled(false);
            selectIndividualButton.setEnabled(false);
        } else {
            modifierList.setSelectedIndex(1);
            filler_field.setEditable(false);
            selectClsButton.setEnabled(true);
            selectIndividualButton.setEnabled(false);
        }
    }

    private void changeMetaClsSelection(edu.stanford.smi.protege.model.Cls metaclass) {
        RDFProperty property = (RDFProperty) propertyList.getSelectedValue();
        if (property == null)
            return;
        if (property instanceof OWLObjectProperty) {
            if (metaclass.getName().equals(OWLNames.Cls.ALL_VALUES_FROM_RESTRICTION)) {
                filler_field.setEditable(false);
                selectClsButton.setEnabled(true);
                selectIndividualButton.setEnabled(false);
            } else if (metaclass.getName().equals(OWLNames.Cls.SOME_VALUES_FROM_RESTRICTION)) {
                filler_field.setEditable(false);
                selectClsButton.setEnabled(true);
                selectIndividualButton.setEnabled(false);
            } else if (metaclass.getName().equals(OWLNames.Cls.HAS_VALUE_RESTRICTION)) {
                this.setInitialFillerValue();
                filler_field.setEditable(false);
                selectClsButton.setEnabled(false);
                selectIndividualButton.setEnabled(false);
            } else if (metaclass.getName().equals(OWLNames.Cls.MAX_CARDINALITY_RESTRICTION)) {
                filler_field.setEditable(true);
                selectClsButton.setEnabled(false);
                selectIndividualButton.setEnabled(false);
            } else if (metaclass.getName().equals(OWLNames.Cls.MIN_CARDINALITY_RESTRICTION)) {
                filler_field.setEditable(true);
                selectClsButton.setEnabled(false);
                selectIndividualButton.setEnabled(false);
            } else {
                filler_field.setEditable(true);
                selectClsButton.setEnabled(false);
                selectIndividualButton.setEnabled(false);
            }
        } else { // not object valued

            if (metaclass.getName().equals(OWLNames.Cls.HAS_VALUE_RESTRICTION)) {
                filler_field.setEditable(true);
                selectClsButton.setEnabled(false);
                selectIndividualButton.setEnabled(false);
            } else {
                filler_field.setEditable(false);
                filler_field.setText("");
                selectClsButton.setEnabled(false);
                selectIndividualButton.setEnabled(false);
            }
        }
    }

    private void setInitialFillerValue() {
        if (restriction == null) {
            filler_field.setText("");
            return;
        }
        if (restriction instanceof OWLSomeValuesFrom) {
            DefaultOWLSomeValuesFrom cls = (DefaultOWLSomeValuesFrom) restriction;
            RDFResource res = (RDFResource) cls.getSomeValuesFrom();
            OWLNamedClass owl_cls = (OWLNamedClass) res;
            this.selectedCls = owl_cls;
            String name = owl_cls.getBrowserText();
            // wrapper.getInternalName(owl_cls);
            debug("Method: setInitialFillerValue: " + name + " (" + res.getBrowserText() + ")");
            filler_field.setText(name);
        } else if (restriction instanceof OWLAllValuesFrom) {
            DefaultOWLAllValuesFrom cls = (DefaultOWLAllValuesFrom) restriction;
            RDFResource res = (RDFResource) cls.getAllValuesFrom();
            OWLNamedClass owl_cls = (OWLNamedClass) res;
            this.selectedCls = owl_cls;
            String name = owl_cls.getBrowserText();
            // wrapper.getInternalName(owl_cls);
            debug("Method: setInitialFillerValue: " + name + " (" + res.getBrowserText() + ")");
            filler_field.setText(name);
        } else if (restriction instanceof OWLHasValue) {
            DefaultOWLHasValue cls = (DefaultOWLHasValue) restriction;
            this.selectedCls = cls;
            String s = cls.getFillerText();
            filler_field.setText(s);
        } else {

            filler_field.setText(fillerText);
        }
    }

}
