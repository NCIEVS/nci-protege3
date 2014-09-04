

/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 *                 Sandhya Kunnatur kunnatur@smi.stanford.edu
 * 		   Abhita Chugh abhita@stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui.diffUI;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.FrameSlotCombination;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.SystemFrames;
import edu.stanford.smi.protege.ui.ClsInverseRelationshipPanel;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.ui.InstanceDisplay;
import edu.stanford.smi.protege.util.AbstractSelectableComponent;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.MutableSelectable;
import edu.stanford.smi.protege.util.RowTableModel;
import edu.stanford.smi.protege.util.Selectable;
import edu.stanford.smi.protege.util.SelectableList;
import edu.stanford.smi.protege.util.SelectionEvent;
import edu.stanford.smi.protege.util.SelectionListener;
import edu.stanford.smi.protege.widget.ClsWidget;
import edu.stanford.smi.protege.widget.SlotWidget;
import edu.stanford.smi.protege.widget.TemplateSlotsWidget;
import edu.stanford.smi.protegex.prompt.ProjectsAndKnowledgeBases;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.FrameDifferenceElement;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.ResultTable;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.TableRow;
import edu.stanford.smi.protegex.prompt.ui.TabComponent;
import edu.stanford.smi.protegex.prompt.util.Util;

public class DiffTreeView extends AbstractSelectableComponent implements MutableSelectable {
    protected DiffClsesPanel _clsesPanel;
    protected DiffDirectInstancesList _directInstancesList;
    protected ClsInverseRelationshipPanel _inverseRelationshipPanel;
    private SelectableList _clsList;
    private boolean _isUpdating;
    protected InstanceDisplay _instanceDisplay;
    protected Project _project;
    private DiffSubclassPane _subclassesPane;

    private ResultTable _diffTable;
//  private IndividualDiff _individualDiff;
    private DiffTablePanel _diffTablePanel;

    private DiffViewSetUp _viewSetup = null;
    private ClsWidget _currentClsWidget;
    private TemplateSlotsWidget _templateSlotsWidget;
    private boolean _customised;
    private boolean _isOwl;


    public DiffTreeView () {
        _project = ProjectsAndKnowledgeBases.getProject(PromptTab.getPromptDiff().getKb2());

        _diffTable = PromptTab.getPromptDiff().getResultsTable();
        _isOwl = PromptTab.kbInOWL();

        _viewSetup = PromptTab.getPromptDiff().getViewSetUp();
//      _viewSetup = new DiffViewSetUp (PromptTab.getProject(PromptDiff.getKb1()), _project, PromptDiff.getResultsTable());

        initialize();
        replayAcceptedChanges();

    }

    public DiffTreeView(Project oldVersion, Project newVersion, ResultTable diffTable){
        _project = newVersion;

        _diffTable = diffTable;
        _isOwl = PromptTab.kbInOWL();

        _viewSetup = new DiffViewSetUp (oldVersion, newVersion, diffTable);

        initialize();
        replayAcceptedChanges();
    }


    public void replayAcceptedChanges(){
        if(ProjectsAndKnowledgeBases.acceptChangesProjectDefined())
            ProjectsAndKnowledgeBases.getAcceptChangesKb().executeAcceptedChanges(this);
    }

    public DiffViewSetUp getViewSetup () {
        return _viewSetup; 
    }

    private JComponent createClsControlPanel() {
        if(_isOwl){
            return createClsesPanel();	
        }else{
            JSplitPane pane = ComponentFactory.createTopBottomSplitPane();
            pane.setDividerLocation(400);
            pane.setTopComponent(createClsesPanel());
            pane.setBottomComponent(createInverseRelationshipPanel());
            return pane;
        }
    }

    private JComponent createClsDisplay() {
        _clsList = ComponentFactory.createSingleItemList(null);
        _clsList.setCellRenderer(FrameRenderer.createInstance());
        _clsList.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent event) {
                if (!_isUpdating) {
                    try {
                        _isUpdating = true;
                        Collection selection = event.getSelectable().getSelection();
                        Instance firstSelection = (Instance) CollectionUtilities.getFirstItem(selection);
                        _directInstancesList.clearSelection();
                        //_instanceDisplay.setInstance(firstSelection);

                        if (firstSelection != null && firstSelection.getFrameID().equals(Model.ClsID.THING))
                            _instanceDisplay.setInstance(null);		  
                        else {		
                            //*** this is a stub for deleted classes;
                            // need to do something better later
                            if (firstSelection != null) {
                                if (firstSelection.getKnowledgeBase().equals(_viewSetup.getKb2())) {
                                    _instanceDisplay.setInstance(firstSelection);
                                    customiseInstanceDisplay((Cls)firstSelection);
                                    Collection rows = _diffTable.getRows(firstSelection);
                                    if (rows != null && !rows.isEmpty()){
                                        _diffTablePanel.setRow( (TableRow)CollectionUtilities.getFirstItem(rows),_customised);
                                        //_individualDiff.setIndividualDiffTableValues ((TableRow)CollectionUtilities.getFirstItem(rows));
                                    }
                                } else {
                                    //*** this is a stub for deleted classes;
                                    // need to do something better later
                                    _instanceDisplay.setInstance(null);
                                }
                            }
                        }
                    }
                    finally  {
                        _isUpdating = false;
                    }
                    notifySelectionListeners();
                }
            }
        });
        LabeledComponent c = new LabeledComponent("Class", _clsList);
        return c;
    }

    private JComponent createClsesPanel() {
        _clsesPanel = DiffClsesPanel.getInstance(_project,_viewSetup);
        _subclassesPane = (DiffSubclassPane)_clsesPanel.getSubclassPane();
        //FrameRenderer renderer = FrameRenderer.createInstance();
        //renderer.setDisplayDirectInstanceCount(true);
        //_clsesPanel.setRenderer(renderer);
        _clsesPanel.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent event) {
                transmitClsSelection();
            }
        });
        return _clsesPanel;
    }

    public DiffSubclassPane getSubclassPane () {
        return _subclassesPane;
    }


    private JComponent createClsSplitter() {
        JSplitPane pane = ComponentFactory.createLeftRightSplitPane();
        pane.setDividerLocation(250);
        pane.setLeftComponent(createClsControlPanel());
        pane.setRightComponent(createInstanceSplitter());
        return pane;
    }

    private JComponent createDirectInstancesList() {
        _directInstancesList = new DiffDirectInstancesList(_project,this);

        _directInstancesList.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent event) {
                if (!_isUpdating) {
                    _isUpdating = true;
                    Collection selection = _directInstancesList.getSelection();
                    Instance selectedInstance;
                    if (selection.size() == 1) {
                        selectedInstance = (Instance) CollectionUtilities.getFirstItem(selection);
                    } else {
                        selectedInstance = null;
                    }
                    _clsList.clearSelection();

                    if (selectedInstance != null) {
                        if (selectedInstance.getKnowledgeBase().equals(_viewSetup.getKb2())) {
                            _instanceDisplay.setInstance(selectedInstance);
                            //customiseInstanceDisplay(selectedCls);
                            Collection rows = _diffTable.getRows(selectedInstance);
                            if (rows != null && !rows.isEmpty()){
                                _diffTablePanel.setRow((TableRow)CollectionUtilities.getFirstItem(rows),_customised);

                                //_individualDiff.setIndividualDiffTableValues ((TableRow)CollectionUtilities.getFirstItem(rows));
                            }
                        } else {
                            //*** this is a stub for deleted classes;
                            // need to do something better later
                            _instanceDisplay.setInstance(null);
                            _diffTablePanel.setRow(null,false);

                        }
                    }

                    //_instanceDisplay.setInstance(selectedInstance);
                    _isUpdating = false;
                }
            }
        });
        return _directInstancesList;
    }

    private JComponent createInstanceDisplay() {

        JSplitPane pane = ComponentFactory.createTopBottomSplitPane ();
        _instanceDisplay = new InstanceDisplay(_project);
        pane.setTopComponent(_instanceDisplay);
        _diffTablePanel = new DiffTablePanel(this,_clsesPanel,_diffTable);

        pane.setBottomComponent(_diffTablePanel);
        pane.setDividerLocation(400);
        return pane;

    }

    private JComponent createInstancesPanel() {
        JPanel panel = ComponentFactory.createPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.add(createClsDisplay(), BorderLayout.NORTH);
        panel.add(createDirectInstancesList(), BorderLayout.CENTER);
        return panel;
    }

    private JComponent createInstanceSplitter() {
        JSplitPane pane = ComponentFactory.createLeftRightSplitPane();
        pane.setDividerLocation(200);
        pane.setLeftComponent(createInstancesPanel());
        pane.setRightComponent(createInstanceDisplay());
        return pane;
    }

    private JComponent createInverseRelationshipPanel() {
        _inverseRelationshipPanel = new ClsInverseRelationshipPanel(_project);
        _inverseRelationshipPanel.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent event) {
                Collection selection = _inverseRelationshipPanel.getSelection();
                if (selection.size() == 1) {
                    Cls cls = (Cls) selection.iterator().next();
                    _clsesPanel.setDisplayParent(cls);
                }
            }
        });
        return _inverseRelationshipPanel;
    }

    public void initialize() {
        //setIcon(Icons.getClsesAndInstancesIcon());
        //setLabel("Classes & Instances");
        setLayout (new BorderLayout());
        add (createClsSplitter(), BorderLayout.CENTER);

        //transmitClsSelection();
        //setClsTree(_clsesPanel.getClsesTree());
    }

    private void transmitClsSelection() {
        // Log.enter(this, "transmitSelection");
        Collection selection = _clsesPanel.getSelection();
        Instance selectedInstance = null;
        Cls selectedCls = null;
        Cls selectedParent = null;
        if (selection.size() == 1) {
            selectedInstance = (Instance) CollectionUtilities.getFirstItem(selection);
            if (selectedInstance instanceof Cls) {
                selectedCls = (Cls) selectedInstance;
                selectedParent = _clsesPanel.getDisplayParent();
            }
        }
        if(_inverseRelationshipPanel != null){
            _inverseRelationshipPanel.setCls(selectedCls, selectedParent);
        }
        _directInstancesList.setCls(selectedCls);

        ComponentUtilities.setListValues(_clsList, selection);
        if (!selection.isEmpty()) {
            _clsList.setSelectedIndex(0);
        }	 	  
    }

    //  private void 	initialize(){
//  setLayout (new BorderLayout());
//  add (createMainSplitter(), BorderLayout.CENTER);

//  }

//  private JComponent createMainSplitter() {
//  JSplitPane pane = ComponentFactory.createLeftRightSplitPane ();
//  pane.setLeftComponent(createClsesSplitter());
//  pane.setRightComponent(createClsDisplay());
//  pane.setDividerLocation(250);
//  return pane;
//  }

//  protected JComponent createClsDisplay() {

//  JSplitPane pane = ComponentFactory.createTopBottomSplitPane ();
//  _instanceDisplay = new InstanceDisplay(_project);
//  pane.setTopComponent(_instanceDisplay);
//  //pane.setBottomComponent(createInstanceDiffDisplay());
//  _diffTablePanel = new DiffTablePanel(this,_clsesPanel,_diffTable);
//  pane.setBottomComponent(_diffTablePanel);
//  pane.setDividerLocation(400);
//  return pane;
//  }

    private void customiseInstanceDisplay(Cls selectedCls){

        ClsWidget clsWidget = _instanceDisplay.getFirstClsWidget();
        ClsWidget prevClsWidget = _currentClsWidget;

        _currentClsWidget = clsWidget;
        _customised = false;
        _templateSlotsWidget = null;

        if(clsWidget == null)
            return;

        SystemFrames systemFrames = _project.getKnowledgeBase().getSystemFrames();
        Slot directTemplateSlot = systemFrames.getDirectTemplateSlotsSlot();
        SlotWidget slotWidgetTmp = clsWidget.getSlotWidget(directTemplateSlot);

        if(slotWidgetTmp == null || ! (slotWidgetTmp instanceof TemplateSlotsWidget)){
            return;
        }

        TemplateSlotsWidget slotWidget = (TemplateSlotsWidget)clsWidget.getSlotWidget(directTemplateSlot);
        _templateSlotsWidget = slotWidget;

        if(_currentClsWidget != prevClsWidget) {
            slotWidget.addButton(new AcceptSlotAction("Accept Change",slotWidget,_instanceDisplay));
            slotWidget.addButton(new RejectSlotAction("Reject Change",slotWidget,_instanceDisplay));

            _currentClsWidget = clsWidget;
        }

        JTable table = slotWidget.getTable();
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setCellRenderer(new DiffSlotPairRenderer(_diffTable));

        if(selectedCls != null){
            addDeletedTemplateSlots(table,selectedCls);
        }
        _customised = true;
    }

    private void addDeletedTemplateSlots(JTable table,Cls cls){

        TableRow row = (TableRow)CollectionUtilities.getSoleItem(_diffTable.getRows(cls));
        Cls clsMapping = (Cls)row.getF1Value();

        if(clsMapping == null)
            return;

        RowTableModel tableModel = (RowTableModel)table.getModel();
        Collection diffElements = row.getOperationExplanation();
        for(Iterator iter = diffElements.iterator(); iter.hasNext(); ){
            FrameDifferenceElement diffEl = (FrameDifferenceElement)iter.next();

            if(diffEl.getChangeLevel() == FrameDifferenceElement.LEVEL_DIRECT &&
                    diffEl.getRelationshipToFrame() == FrameDifferenceElement.TEMPLATE_SLOT &&
                    diffEl.getOperation() == FrameDifferenceElement.OP_DELETED){

                FrameSlotCombination o = new FrameSlotCombination(clsMapping,(Slot)diffEl.getO1Value());
                tableModel.addRow(o);	
            }
        }
    }

//  private JComponent createInstanceDiffDisplay () {
//  JPanel result = new JPanel (new BorderLayout());
//  _individualDiff = new IndividualDiff (_diffTable);
//  result.add (_individualDiff);
//  return result;
//  }

//  private JComponent createClsesSplitter() {

//  if(!_isOwl){ 	
//  JSplitPane pane = ComponentFactory.createTopBottomSplitPane();
//  _clsesPanel = createClsesPanel();
//  pane.setTopComponent(_clsesPanel);
//  _inverseRelationshipPanel = createInverseRelationshipPanel();
//  pane.setBottomComponent(_inverseRelationshipPanel);
//  pane.setDividerLocation(400);
//  return pane;
//  }else{
//  _clsesPanel = createClsesPanel();
//  _inverseRelationshipPanel = null;
//  return _clsesPanel;
//  }
//  }

//  protected DiffClsesPanel createClsesPanel() {
//  DiffClsesPanel panel = new DiffClsesPanel(_project, _viewSetup);
//  panel.addSelectionListener(new SelectionListener() {
//  public void selectionChanged(SelectionEvent event) {
//  transmitSelection();
//  }
//  });
//  return panel;
//  }

//  protected ClsInverseRelationshipPanel createInverseRelationshipPanel() {
//  final ClsInverseRelationshipPanel panel = new ClsInverseRelationshipPanel(_project);
//  panel.addSelectionListener(new SelectionListener() {
//  public void selectionChanged(SelectionEvent event) {
//  Collection selection = panel.getSelection();
//  if (selection.size() == 1) {
//  Cls cls = (Cls) CollectionUtilities.getFirstItem(selection);
//  _clsesPanel.setDisplayParent(cls);
//  }
//  }
//  });
//  return panel;
//  }

//  protected void transmitSelection() {
//  // Log.enter(this, "transmitSelection");
//  Collection selection = _clsesPanel.getSelection();
//  Instance selectedInstance = null;
//  Cls selectedCls = null;
//  Cls selectedParent = null;
//  if (selection.size() == 1) {
//  selectedInstance = (Instance) CollectionUtilities.getFirstItem(selection);
//  if (selectedInstance instanceof Cls) {
//  selectedCls = (Cls) selectedInstance;
//  selectedParent = _clsesPanel.getDisplayParent();
//  }
//  }
//  if(_inverseRelationshipPanel != null)
//  _inverseRelationshipPanel.setCls(selectedCls, selectedParent);

//  //*** this is a stub for deleted classes;
//  // need to do something better later
//  if (selectedInstance != null) {
//  if (selectedInstance.getKnowledgeBase().equals(_viewSetup.getKb2())) {
//  _instanceDisplay.setInstance(selectedInstance);
//  customiseInstanceDisplay(selectedCls);
//  Collection rows = _diffTable.getRows(selectedInstance);
//  if (rows != null && !rows.isEmpty())
//  _diffTablePanel.setRow((TableRow)CollectionUtilities.getFirstItem(rows),_customised);
//  //_individualDiff.setIndividualDiffTableValues ((TableRow)CollectionUtilities.getFirstItem(rows));
//  } else {
//  //*** this is a stub for deleted classes;
//  // need to do something better later
//  _instanceDisplay.setInstance(null);
//  }
//  }

//  }

    public void reloadRHS(){
        Instance selectedInstance = (Instance)CollectionUtilities.getFirstItem(_directInstancesList.getSelection());

        transmitClsSelection();

        if(selectedInstance != null)
            _directInstancesList.select(selectedInstance);
    }

    public ClsWidget getCurrentClsWidget(){
        return _instanceDisplay.getFirstClsWidget();
    }

    public void clearSelectedTemplateSlot(){
        if(_templateSlotsWidget != null){
            _templateSlotsWidget.getTable().clearSelection();
        }
    }

    public void selectTemplateSlot(Slot slot){
        if(_templateSlotsWidget == null){
            return;
        }

        Slot slotMap = (Slot)Util.getMap(slot,_diffTable);

        JTable table = _templateSlotsWidget.getTable();
        int nRows = table.getRowCount();
        for(int i = 0; i < nRows; i++){
            FrameSlotCombination fsc = (FrameSlotCombination)table.getValueAt(i,0);
            if(fsc.getSlot() == slot || fsc.getSlot() == slotMap){
                table.setRowSelectionInterval(i,i);
                break;
            }
        }
    }

    public ResultTable getDiffTable() {
        return _diffTable;
    }

    public String toString () {

        return "DiffTreeView";
    }

    private  class AcceptSlotAction extends AllowableAction {

        private InstanceDisplay _instanceDisplay;

        public AcceptSlotAction (String prompt,Selectable selectable,InstanceDisplay display) {
            super(prompt, ComponentUtilities.loadImageIcon(TabComponent.class, "images/OK.gif"),selectable);
            _instanceDisplay = display;
        }

        public void actionPerformed(ActionEvent e) {
//          Log.getLogger().info("accept template slot");

            AcceptorRejector acceptorRejector = PromptTab.getPromptDiff().getAcceptorRejector();
            Collection selectedSlots = this.getSelection();

            for(Iterator iter = selectedSlots.iterator(); iter.hasNext(); ){

                FrameSlotCombination fsCombination = (FrameSlotCombination)iter.next();
                acceptorRejector.acceptSlotChange(getBoundCls(),fsCombination.getSlot());

                ProjectsAndKnowledgeBases.getAcceptChangesKb().addToKB(AcceptChangesKnowledgeBase.ACCEPT_SLOT,getBoundCls().getName(),null,fsCombination.getSlot().getName(),null);
            }
            ((DiffTabComponent)PromptTab.getTabComponent()).reset();
        }

        public void onSelectionChange() {

            Object o = CollectionUtilities.getFirstItem(this.getSelection());

            if(o == null){
                setAllowed(false);
                return;
            }
            FrameSlotCombination combination = (FrameSlotCombination) o;
            Slot slot = (combination == null) ? (Slot) null : combination.getSlot();
            int status = AcceptorRejector.getSlotStatus(_diffTable,(Cls)combination.getFrame(),slot); 

            setAllowed((status !=0 ));
        }

        private Cls getBoundCls(){
            return (Cls)_instanceDisplay.getCurrentInstance();
        }
    }




    private  class RejectSlotAction extends AllowableAction {

        private InstanceDisplay _instanceDisplay;

        public RejectSlotAction (String prompt,Selectable selectable,InstanceDisplay display) {
            super(prompt, ComponentUtilities.loadImageIcon(TabComponent.class, "images/Cancel.gif"),selectable);
            _instanceDisplay = display;
        }

        public void actionPerformed(ActionEvent e) {
//          Log.getLogger().info("accept template slot");

            AcceptorRejector acceptorRejector = PromptTab.getPromptDiff().getAcceptorRejector();
            Collection selectedSlots = this.getSelection();

            for(Iterator iter = selectedSlots.iterator(); iter.hasNext(); ){

                FrameSlotCombination fsCombination = (FrameSlotCombination)iter.next();
                acceptorRejector.rejectSlotChange(getBoundCls(),fsCombination.getSlot());
            }
            ((DiffTabComponent)PromptTab.getTabComponent()).reset();
        }

        public void onSelectionChange() {

            Object o = CollectionUtilities.getFirstItem(this.getSelection());

            if(o == null){
                setAllowed(false);
                return;
            }
            FrameSlotCombination combination = (FrameSlotCombination) o;
            Slot slot = (combination == null) ? (Slot) null : combination.getSlot();
            int status = AcceptorRejector.getSlotStatus(_diffTable,(Cls)combination.getFrame(),slot); 

            setAllowed((status !=0 ));
        }

        private Cls getBoundCls(){
            return (Cls)_instanceDisplay.getCurrentInstance();
        }

    }

    @Override
    public void notifySelectionListeners() {
        super.notifySelectionListeners();
    }

    public void clearSelection() {
        if (_clsesPanel.getSelectable() != null) {
            _clsesPanel.clearSelection();
        }
    }

    public Collection getSelection() {
        return _clsesPanel.getSelection();
    }

    public void setSelection(Collection objects) {
        if (objects != null) {
            _clsesPanel.getSubclassPane().setSelectedClses(objects);
        }
        else {
            clearSelection();
        }
    }
}

