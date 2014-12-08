/**
 * 
 */
package gov.nih.nci.protegex.panel;

import static gov.nih.nci.protegex.tree.TreePanel.PanelType.TYPE_PRERETIRE;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.widget.OWLUI;
import gov.nih.nci.protegex.tree.TreePanel;
import gov.nih.nci.protegex.util.MsgDialog;
import gov.nih.nci.protegex.dialog.NoteDialog;
import gov.nih.nci.protegex.edit.*;

/**
 * @author bitdiddle
 * 
 */
public class PreRetirePanel extends JPanel implements ActionListener,
        PanelDirty, ConceptChangedListener {

    private Logger logger = Log.getLogger(getClass());

    public static final long serialVersionUID = 123452792L;

    private NCIEditTab tab = null;

    private OWLWrapper wrapper = null;

    private OWLModel owlModel = null;

    private EditPanel subrefClsPanel = null;

    private Instance subrefClsSelected = null;

    private JTabbedPane tabbedPane_Preretire;

    private JScrollPane preRetireUpperPane;

    public JScrollPane getScrollPane() {
        return preRetireUpperPane;
    }

    private JScrollPane SubRefClsPanelContainier = null;

    private JList preRetireReferenceList; // preretire

    private TreePanel leftTreePanel_Preretire = null;

    private JButton saveButton_Preretire, clearButton_Preretire,
            preretireButton_Preretire;

    private SubMouseListener submouseListener = null;

    private ReferenceMouseListener referencemouseListener = null;

    private JList subClsList;

    private DefaultListModel referenceListModel, subclassListModel;

    private Vector<String> subVec, restrictionVec;

    private Collection subClassCollection, referringClasses = null;

    private boolean unretireButtonPressed;

    private Cls droppedCls = null;

    public void conceptChanged(OWLNamedClass cls, String msg) {

        // check if cls need to be removed as a dependency to clean up

        Collection c1 = wrapper.getNamedSubclasses((OWLNamedClass) droppedCls);

        Collection c2 = wrapper.getReferringClasses((OWLNamedClass) droppedCls);

        if (c1.contains(cls) || c2.contains(cls)) {

        } else {

            removeListeners();
            this.resetSubRefTabbedPane();

        }
    }

    public PreRetirePanel(NCIEditTab t) {
        super(false);
        tab = t;
        wrapper = tab.getOWLWrapper();
        owlModel = tab.getOWLModel();

        init();
    }

    private void init() {

        setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());

        JPanel subRefTabbedPane = null;
        subRefTabbedPane = createSubRefTabbedPane(); // upperleft

        leftPanel.add(subRefTabbedPane, BorderLayout.NORTH);

        JPanel treePanel = new JPanel();
        JLabel clsLabel = new JLabel("Retiring Class");

        preRetireUpperPane = new JScrollPane();
        leftTreePanel_Preretire = new TreePanel(tab, null, tab.getOWLModel());
        leftTreePanel_Preretire.setType(TYPE_PRERETIRE);
        preRetireUpperPane.setViewportView(leftTreePanel_Preretire);

        treePanel.setLayout(new BorderLayout());
        treePanel.add(clsLabel, BorderLayout.NORTH);
        treePanel.add(preRetireUpperPane, BorderLayout.CENTER);

        leftPanel.add(treePanel, BorderLayout.CENTER); // lowerleft treePanel

        // ///////////////////////////////////////////////////////////////////
        // subrefClsPanel = createResourcePanel(); // right panel
        OWLNamedClass nullCls = null;

        subrefClsPanel = tab.createEditPanel(nullCls);

        JPanel rightPanel = new JPanel(false);
        rightPanel.setLayout(new BorderLayout());

        SubRefClsPanelContainier = new JScrollPane();
        SubRefClsPanelContainier.setViewportView((Component) subrefClsPanel);

        rightPanel.add(SubRefClsPanelContainier, BorderLayout.CENTER);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                              leftPanel, rightPanel);
        splitPane.setDividerLocation(220);

        splitPane.setOneTouchExpandable(true);

        splitPane.resetToPreferredSizes();
        add(splitPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        preretireButton_Preretire = new JButton("PreRetire");
        preretireButton_Preretire.addActionListener(this);
        preretireButton_Preretire.setEnabled(false);

        saveButton_Preretire = new JButton("Save");
        saveButton_Preretire.addActionListener(this);

        clearButton_Preretire = new JButton("Clear");
        clearButton_Preretire.addActionListener(this);

        // buttonPanel.add(unretireButton_Preretire);
        buttonPanel.add(preretireButton_Preretire);
        buttonPanel.add(saveButton_Preretire);
        buttonPanel.add(clearButton_Preretire);

        add(buttonPanel, BorderLayout.SOUTH);

        preretireButton_Preretire.setEnabled(false);
        saveButton_Preretire.setEnabled(false);
        clearButton_Preretire.setEnabled(false);

        subVec = new Vector<String>();
        restrictionVec = new Vector<String>();
    }

    public boolean isDirty() {
        return this.saveButton_Preretire.isEnabled();
    }

    public void reset() {
        this.resetPreretirePanel();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == clearButton_Preretire) {
            try {
                if (tab.checkNoSavedContinueMsg())
                    return;
                resetPreretirePanel();
                // resetAll();
            } catch (Exception ex) {
                Log.getLogger().log(Level.WARNING, "Exception caught", ex);
            }
        }

        else if (e.getSource() == preretireButton_Preretire) {
            preretireButton_Preretire.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            if (droppedCls == null) {
                preretireButton_Preretire
                                         .setCursor(new Cursor(
                                                               Cursor.DEFAULT_CURSOR));
                String text = "Retiring concept is not specified.";
                ModalDialog.showMessageDialog(null, text);
                logger.info(text);
                return;
            }

            resetSubRefTabbedPane();

            DefaultListModel listModel1 = submouseListener
                                                          .getDefaultListModel();
            DefaultListModel listModel2 = referencemouseListener
                                                                .getDefaultListModel();
            if (listModel1.getSize() > 0 || listModel2.getSize() > 0) {
                preretireButton_Preretire
                                         .setCursor(new Cursor(
                                                               Cursor.DEFAULT_CURSOR));
                String text = "Please resolve all subclasses and referencing classes.";

                ProtegeUI.getModalDialogFactory()
                         .showErrorMessageDialog(owlModel, text);
                logger.info(text);
                return;
            }

            try {
                String editornote = "";
                String designnote = "";
                String prefix = "preretire_annotation";

                preretireButton_Preretire
                                         .setCursor(new Cursor(
                                                               Cursor.DEFAULT_CURSOR));
                NoteDialog dlg = new NoteDialog(tab, editornote, designnote,
                                                prefix);

                editornote = dlg.getEditorNote();
                if (tab.getUserName() != null) {
                    editornote += ", " + tab.getUserName();
                }
                designnote = dlg.getDesignNote();

                if (dlg.OKBtnPressed()) {
                    String prop_name = NCIEditTab.EDITORIALNOTE;
                    JTree edit_tree = leftTreePanel_Preretire.getTree();
                    leftTreePanel_Preretire.addProperty(prop_name, editornote,
                                                        null);
                    prop_name = NCIEditTab.SCOPENOTE;
                    leftTreePanel_Preretire.addProperty(prop_name, designnote,
                                                        null);
                    
                    this.tab.attributes2Properties(leftTreePanel_Preretire, (RDFSClass) droppedCls);

                    

                    String prop_value = NCIEditTab.PRERETIRED_CONCEPTS;

                    Cls preretiredCls = wrapper.getCls(prop_value);

                    leftTreePanel_Preretire.addParent(preretiredCls, false);

                    Vector inboundRoles = leftTreePanel_Preretire
                                                                 .getInboundRoles();
                    for (int i = 0; i < inboundRoles.size(); i++) {
                        String inboundRole = (String) inboundRoles.elementAt(i);
                        leftTreePanel_Preretire
                                               .addProperty(
                                                            NCIEditTab.PREDEPRECATIONSOURCEROLE,
                                                            inboundRole, null);
                        leftTreePanel_Preretire
                                               .add_old_source_role(inboundRole);
                    }
                    
                    Vector inboundAssocs = leftTreePanel_Preretire
                                                                  .getInboundAssocs();
                    for (int i = 0; i < inboundAssocs.size(); i++) {
                        String inboundAssoc = (String) inboundAssocs.elementAt(i);
                        leftTreePanel_Preretire
                                               .addProperty(
                                                            NCIEditTab.PREDEPRECATIONSOURCEASSOC,
                                                            inboundAssoc, null);
                        leftTreePanel_Preretire
                                               .add_old_source_assoc(inboundAssoc);
                    }

                    Vector oldSubclasses = leftTreePanel_Preretire
                                                                  .getOldSubclasses();
                    for (int i = 0; i < oldSubclasses.size(); i++) {
                        Cls oldSubclass = (Cls) oldSubclasses.elementAt(i);

                        leftTreePanel_Preretire
                                               .addProperty(
                                                            NCIEditTab.PREDEPRECATIONCHILDCONCEPT,
                                                            wrapper
                                                                   .getInternalName(oldSubclass),
                                                            oldSubclass);

                        leftTreePanel_Preretire
                                               .add_old_child(wrapper
                                                                     .getInternalName(oldSubclass));
                    }

                    preretireButton_Preretire.setEnabled(false);
                    saveButton_Preretire.setEnabled(true);
                    // unretireButton_Preretire.setEnabled(false);
                    clearButton_Preretire.setEnabled(true);

                    edit_tree.repaint();

                }

            } catch (Exception ex) {
                logger.warning("Exception caught" + ex.toString());
                OWLUI.handleError(owlModel, ex);
            }

            preretireButton_Preretire
                                     .setCursor(new Cursor(
                                                           Cursor.DEFAULT_CURSOR));

        }

        else if (e.getSource() == saveButton_Preretire) {

            String fromClsName = leftTreePanel_Preretire.getLocalName();
            Cls fromCls = wrapper.getCls(fromClsName);
            tab.removeFromListenedToClses((OWLNamedClass) fromCls, this);
            saveButton_Preretire.setCursor(new Cursor(Cursor.WAIT_CURSOR));

            DataHandler.Status status = tab
                                           .getDataHandler()
                                           .canSaveData(
                                                        leftTreePanel_Preretire,
                                                        fromCls);
            if (status != DataHandler.Status.SUCCESSFUL) {
                if (status == DataHandler.Status.FAILURE) {
                    tab.showError(leftTreePanel_Preretire.getDisplayName());
                    logger.info("Save is incomplete.");
                }
            } else {
                try {
                    owlModel.beginTransaction("Preretire " + fromClsName,
                                              fromCls.getName());

                    if (tab.saveConcept(leftTreePanel_Preretire) == DataHandler.Status.SUCCESSFUL) {
                        owlModel.commitTransaction();

                    } else {
                        owlModel.rollbackTransaction();
                    }

                } catch (Exception ex) {
                    owlModel.rollbackTransaction();
                    OWLUI.handleError(owlModel, ex);
                }

                String name = leftTreePanel_Preretire.getDisplayName();

                saveButton_Preretire
                                    .setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                if (unretireButtonPressed) {
                    MsgDialog.ok(tab, name + " has been unretired");
                } else {
                    MsgDialog
                             .ok(tab, name + " has been flagged for retirement");
                }

                resetPreretirePanel();

            }

            tab.addToListenedToClses((OWLNamedClass) fromCls, this);
            saveButton_Preretire.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            tab.ensureClassSelected(fromCls);
        }
        // TODO Auto-generated method stub

    }

    private JPanel createSubRefTabbedPane() {
        JPanel panel = new JPanel(false);
        ImageIcon icon = new ImageIcon("images/middle.gif");

        tabbedPane_Preretire = new JTabbedPane();

        Component batchLoaderPanel = createSubclassPanel();
        tabbedPane_Preretire.addTab("Subclasses", icon, batchLoaderPanel, "");

        Component batchEditorPanel = createReferenceClsesPanel();
        tabbedPane_Preretire.addTab("Referencing Classes", icon,
                                    batchEditorPanel, "");

        panel.setLayout(new GridLayout(1, 1));
        panel.add(tabbedPane_Preretire);

        tabbedPane_Preretire.setSelectedIndex(0);

        return panel;
    }

    private Component createSubclassPanel() {
        subClassCollection = new HashSet();
        /**
         * OWLNamedClass cls = null; if (droppedCls != null) { cls =
         * (OWLNamedClass) droppedCls; subClassCollection =
         * wrapper.getNamedSubclasses(cls); }
         */

        JPanel panel = new JPanel();

        submouseListener = new SubMouseListener();
        submouseListener.getJList().addMouseListener(submouseListener);

        // DefaultListModel listModel = submouseListener.getDefaultListModel();
        /**
         * Iterator itr = subClassCollection.iterator(); while (itr.hasNext()) {
         * listModel.addElement(wrapper.getInternalName(((Cls) itr.next()))); }
         */

        JScrollPane scrollPane = new JScrollPane(submouseListener.getJList());
        panel.setLayout(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void resetPreretirePanel() {
        if (subclassListModel != null) {
            subclassListModel.removeAllElements();
        }
        if (referenceListModel != null) {
            referenceListModel.removeAllElements();
        }

        subclassListModel = submouseListener.getDefaultListModel();
        subclassListModel.clear();

        referenceListModel = referencemouseListener.getDefaultListModel();
        referenceListModel.clear();

        initializeSubRefClsPanel();

        leftTreePanel_Preretire = new TreePanel(tab, null, owlModel);
        leftTreePanel_Preretire.setType(TYPE_PRERETIRE);

        preRetireUpperPane.setViewportView(leftTreePanel_Preretire);

        preretireButton_Preretire.setEnabled(false);
        saveButton_Preretire.setEnabled(false);
        clearButton_Preretire.setEnabled(false);

        droppedCls = null;
        tab.clearListenedToClses();
    }

    private Component createReferenceClsesPanel() {
        /**
         * referringClasses = new HashSet(); Cls cls = null; if (droppedCls !=
         * null) { cls = (Cls) droppedCls; referringClasses =
         * wrapper.getReferringClasses(cls); }
         */

        JPanel panel = new JPanel();

        referencemouseListener = new ReferenceMouseListener();
        referencemouseListener.getJList()
                              .addMouseListener(referencemouseListener);

        // DefaultListModel listModel = referencemouseListener
        // .getDefaultListModel();
        /**
         * Iterator itr = referringClasses.iterator(); while (itr.hasNext()) { //
         * listModel.addElement(((Cls) itr.next()).getBrowserText());
         * listModel.addElement(wrapper.getInternalName(((Cls) itr.next()))); }
         */

        JScrollPane scrollPane = new JScrollPane(
                                                 referencemouseListener
                                                                       .getJList());
        panel.setLayout(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private class SubMouseListener extends MouseAdapter {

        public SubMouseListener() {
            subclassListModel = new DefaultListModel();
            subClsList = new JList(subclassListModel);
            DefaultListCellRenderer renderer0 = new DefaultListCellRenderer();
            subClsList.setCellRenderer(renderer0);
            subClsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }

        public void mouseClicked(MouseEvent e) {
            if (subclassListModel.isEmpty())
                return;

            int selectIndex = subClsList.locationToIndex(e.getPoint());
            if (selectIndex >= 0) {
                String clsName = (String) subclassListModel
                                                           .getElementAt(selectIndex);
                Cls selectedCls = owlModel.getRDFSNamedClass(clsName);
                subrefClsSelected = selectedCls;
                updateSubRefClsPanel((Instance) selectedCls);
            }

        }

        public DefaultListModel getDefaultListModel() {
            return subclassListModel;
        }

        public JList getJList() {
            return subClsList;
        }
    }

    private class ReferenceMouseListener extends MouseAdapter {
        public ReferenceMouseListener() {
            referenceListModel = new DefaultListModel();
            preRetireReferenceList = new JList(referenceListModel);
            DefaultListCellRenderer renderer1 = new DefaultListCellRenderer();
            preRetireReferenceList.setCellRenderer(renderer1);
        }

        public void mouseClicked(MouseEvent e) {
            if (referenceListModel.isEmpty())
                return;

            int selectIndex = preRetireReferenceList
                                                    .locationToIndex(e
                                                                      .getPoint());
            if (selectIndex >= 0) {
                String clsName = (String) referenceListModel
                                                            .getElementAt(selectIndex);
                Cls selectedCls = owlModel.getRDFSNamedClass(clsName);
                subrefClsSelected = selectedCls;
                updateSubRefClsPanel((Instance) selectedCls);
            }
        }

        public DefaultListModel getDefaultListModel() {
            return referenceListModel;
        }

        public JList getJList() {
            return preRetireReferenceList;
        }
    }

    public void setDroppedCls(Cls cls) {
        this.droppedCls = cls;
        resetSubRefTabbedPane();
        preRetireUpperPane.repaint();
    }

    private void addListeners(Collection coll) {
        Iterator it = coll.iterator();
        while (it.hasNext()) {
            tab.addToListenedToClses((OWLNamedClass) it.next(), this);
        }
    }

    private void removeListeners(Collection coll) {
        Iterator it = coll.iterator();
        while (it.hasNext()) {
            tab.removeFromListenedToClses((OWLNamedClass) it.next(), this);
        }

    }

    private void removeListeners() {
        removeListeners(this.subClassCollection);
        removeListeners(this.referringClasses);
    }

    private void resetSubRefTabbedPane() {
        if (droppedCls == null) {
            logger.info("resetSubRefTabbedPane == null -- nothing done.");
            subVec.clear();
            restrictionVec.clear();
            DefaultListModel listModel = submouseListener.getDefaultListModel();
            listModel.clear();
            listModel = referencemouseListener.getDefaultListModel();
            listModel.clear();
            tab.clearListenedToClses();
            return;
        }

        OWLNamedClass cls = (OWLNamedClass) droppedCls;

        subClassCollection = wrapper.getNamedSubclasses(cls);
        addListeners(subClassCollection);
        referringClasses = wrapper.getReferringClasses(cls);
        addListeners(referringClasses);

        DefaultListModel listModel = submouseListener.getDefaultListModel();
        listModel.clear();

        Vector<String> v = new Vector<String>();
        Iterator itr = subClassCollection.iterator();
        while (itr.hasNext()) {
            Cls nextcls = (Cls) itr.next();
            v.addElement(wrapper.getInternalName(nextcls));
        }
        if (v.size() > 0) {
            try {
                if (wrapper.getSortUtility() == null) {
                    logger.info("QuickSort utility not available...");
                } else {
                    if (v.size() > 1) {
                        wrapper.getSortUtility().quickSort(v, 0, v.size() - 1);
                    }
                }
                for (int i = 0; i < v.size(); i++) {
                    listModel.addElement((String) v.elementAt(i));
                    subVec.add((String) v.elementAt(i));
                }
            } catch (Exception ex) {
                Log.getLogger().log(Level.WARNING, "Exception caught", ex);
            }
        }

        listModel = referencemouseListener.getDefaultListModel();
        listModel.clear();
        itr = referringClasses.iterator();
        v.clear();
        while (itr.hasNext()) {
            Cls nextcls = (Cls) itr.next();
            // v.addElement(nextcls.getBrowserText());
            v.addElement(wrapper.getInternalName(nextcls));
        }

        if (v.size() > 0) {
            try {
                if (wrapper.getSortUtility() == null) {
                    logger.info("QuickSort utility not available...");
                }

                else {
                    if (v.size() > 1) {
                        wrapper.getSortUtility().quickSort(v, 0, v.size() - 1);
                    }
                }
                for (int i = 0; i < v.size(); i++) {
                    listModel.addElement((String) v.elementAt(i));
                    restrictionVec.add((String) v.elementAt(i));
                }
            } catch (Exception ex) {
                Log.getLogger().log(Level.WARNING, "Exception caught", ex);
            }
        }

        initializeSubRefClsPanel();
        
        preretireButton_Preretire.setEnabled(true);
    }

    public void enableButton(String buttonLabel, boolean state) {
        if (buttonLabel.compareTo("saveButton_Preretire") == 0)
            saveButton_Preretire.setEnabled(state);
        else if (buttonLabel.compareTo("clearButton_Preretire") == 0)
            clearButton_Preretire.setEnabled(state);
        else if (buttonLabel.compareTo("preretireButton_Preretire") == 0)
            preretireButton_Preretire.setEnabled(state);
    }

    public void initializeSubRefClsPanel() {
        
        subrefClsPanel = tab.createEditPanel(null);
        SubRefClsPanelContainier.setViewportView((Component) subrefClsPanel);
        /**
        subrefClsPanel.setInstance(null);
        subrefClsPanel.setFocusClass(null);
        subrefClsPanel.updateAll();
        
        //subrefClsPanel.reInitialize();
        
        subrefClsPanel.disableAll();
        SubRefClsPanelContainier.setViewportView((Component) subrefClsPanel);
        **/
    }

    public void updateSubRefClsPanel(Instance instance) {
        subrefClsPanel.setFocusClass((OWLNamedClass) instance);
        subrefClsPanel.setAdvancedQuery("AdvancedQuery");
        if (instance != null) {
            subrefClsPanel.updateAll();
            subrefClsPanel.enableAddButtons();
            tab.addToListenedToClses((OWLNamedClass) instance, subrefClsPanel);
        }
    }

}
