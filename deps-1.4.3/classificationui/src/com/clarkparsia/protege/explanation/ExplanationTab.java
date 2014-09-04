package com.clarkparsia.protege.explanation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.util.SimpleURIShortFormProvider;
import org.semanticweb.owl.util.URIShortFormProvider;

import com.clarkparsia.dig20.client.DigReasoner;
import com.clarkparsia.dig20.client.admin.DigClientAdmin;
import com.clarkparsia.dig20.exceptions.DigClientException;
import com.clarkparsia.dig20.exceptions.ErrorResponseException;
import com.clarkparsia.explanation.io.manchester.html.HTMLManchesterSyntaxExplanationRenderer;
import com.clarkparsia.explanation.io.manchester.html.HTMLManchesterSyntaxExplanationRenderer.IrrelevantPartHandling;
import com.clarkparsia.protege.explanation.util.PrefsManager;
import com.clarkparsia.protege.explanation.util.TabProperties;
import com.clarkparsia.protege.reasoner.CustomProtegeOWLReasoner;
import com.clarkparsia.protege.reasoner.CustomReasonerProjectPlugin;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.stanford.smi.protege.exception.ProtegeIOException;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.ModelUtilities;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.impl.UnbackedOperationImpl;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SelectionEvent;
import edu.stanford.smi.protege.util.SelectionListener;
import edu.stanford.smi.protege.util.WaitCursor;
import edu.stanford.smi.protege.widget.AbstractSlotWidget;
import edu.stanford.smi.protegex.owl.inference.protegeowl.ReasonerManager;
import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.model.OWLNames;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.ui.actions.AbstractOWLModelAction;
import edu.stanford.smi.protegex.owl.ui.widget.AbstractTabWidget;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Apr 10, 2007 10:14:24 AM
 *
 * @author Michael Grove <mhgrove@hotmail.com>
 */
public class ExplanationTab extends AbstractTabWidget implements ActionListener, ChangeListener {
    private static Logger LOGGER = Log.getLogger(ExplanationTab.class);

    private static String CMD_NEXT = "CMD_NEXT";
    private static String CMD_PREVIOUS = "CMD_PREVIOUS";
    private static String CMD_SHOW_PREFS = "CMD_SHOW_PREFS";
    private static String CMD_RELOAD = "CMD_RELOAD";
    private static String CMD_SHUTDOWN = "CMD_SHUTDOWN";
    private static String CMD_LOAD = "CMD_LOAD";
    private static String CMD_INFO = "CMD_INFO";

    private AbstractSlotWidget mSubClassWidget;
    private AbstractSlotWidget mSuperClassWidget;
    private AbstractSlotWidget mEquivClassWidget;
    private AbstractSlotWidget mInferredSubClassWidget;
    private AbstractSlotWidget mInferredSuperClassWidget;
    
    private ExplanationPanel mExplanationPanel;

    private JRadioButton mShowIrrelevantParts;
    private JRadioButton mColorIrrelevantParts;
    private JRadioButton mHideIrrelevantParts;

    private HTMLManchesterSyntaxExplanationRenderer mExplanationRenderer;

    private ClassTree mClassTree;

	private enum ExplainType {
		SubClass, SuperClass, Equivalent
	}

    private static final int LIMIT = 10;

    private static final PrefsManager mPrefsManager = new PrefsManager("1.0");

    private TabProperties mTabProperties;

//    private Collection mLastExplanationSelection;
//    private int mLastExplanationType;
    
    private OWLAxiom mLastExplainedAxiom;
    private Set<Set<OWLAxiom>> mLastExplanations;

	private ExplainType mLastExplainType;
	private Collection mLastSelection;
	private Action mMoreExplanationsAction;
	private ThrobberIcon mThrobber;
	private JLabel mStatusMsg;
	
	private URIShortFormProvider shortFormProvider = new SimpleURIShortFormProvider() {		
		@Override
        public String getShortForm(URI theURI) {
	        String aName = getOWLModel().getResourceNameForURI(theURI.toString());
	        
	        RDFResource aResource = getOWLModel().getRDFResource(aName);

	        String browserText = (aResource == null) ? null : aResource.getBrowserText();
	        
	        return (browserText == null) ? super.getShortForm(theURI) : browserText;
        }		
	};

    private OWLDataFactory mFactory = CustomReasonerProjectPlugin.getOWLDataFactory();

	public ExplanationTab() {
        mTabProperties = new TabProperties(getClass().getResourceAsStream("explanation.properties"));

        installDefaultPreferences();

        updateExplanationRenderer();

        LOGGER.info("Clark & Parsia, LLC. Protege Explanation Tab created successfully");
    }
	
	private final static Operation EXPLANATION_SERVER_READ_OPERATION = new UnbackedOperationImpl("ExpServerRead", "ExpServerRead");
	private final static Operation EXPLANATION_SERVER_MODIFY_OPERATION = new UnbackedOperationImpl("ExpServerModify", "ExpServerModify");

	/**
	 * Checks whether the user is allowed to execute the operation.
	 *
	 * The code originates from edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore.isOperationAllowed(KnowledgeBase, Operation)
	 * It was modified to return "false" when the operation specified is not known to the frame store. (The original code
	 * returned "true" in such a case.)
	 * 
	 * @param kb the knowledge base
	 * @param op the operation
	 * @return true if the user is allowed to execute the operation
	 * @throws ProtegeIOException if an error should occur
	 */
	private static boolean isOperationAllowed(KnowledgeBase kb, Operation op)
	throws ProtegeIOException {
		DefaultKnowledgeBase dkb = (DefaultKnowledgeBase) kb;
		FrameStore terminalFS = dkb.getTerminalFrameStore();
		if (!(terminalFS instanceof RemoteClientFrameStore)) {
			return true;
		}
		RemoteClientFrameStore remoteFS = (RemoteClientFrameStore) terminalFS;
		return remoteFS.getKnownOperations().contains(op) &&
		remoteFS.getAllowedOperations().contains(op);
	}

	/**
	 * Checks whether the current user is allowed to read any information from the explanation server.
	 * 
	 * A user is allowed to read anything from the information server if the current project is either single-client
	 * (in such a case the client owns everything related to the project), or it has ExpServerRead permission.
	 * (Usually, this permission is given to everybody in the "World" group, so everybody can read properties of the
	 * explanation server.)
	 * 
	 * @return true if the current user is allowed to read any information from the explanation server.
	 */
	private boolean isAllowedToReadExplanationServer() {		
		Project aProject = getProject();
		
		if (aProject != null) {
			if (aProject.isMultiUserClient()) {
				
				return isOperationAllowed(getProject().getKnowledgeBase(), EXPLANATION_SERVER_READ_OPERATION);
			} else {
				// in single-user clients it really does not matter whether we display admin options or not
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks whether the current user is allowed to modify the explanation server
	 * 
	 * A user is allowed to modify the explanation server if the current project is either single-client
	 * (in such a case the client owns everything related to the project), or it has ExpServerModify permission.
	 * (Usually, this permission is given only to people in administration groups.)
	 * 
	 * @return true if the current user is allowed to modify the explanation server.
	 */
	private boolean isAllowedToModifyExplanationServer() {
		Project aProject = getProject();
		
		if (aProject != null) {
			if (aProject.isMultiUserClient()) {
				return isOperationAllowed(getProject().getKnowledgeBase(), EXPLANATION_SERVER_MODIFY_OPERATION);
			} else {
				// in single-user clients it really does not matter whether we display admin options or not
				return true;
			}
		}
		return false;
	}

    /**
     * @inheritDoc
     */
    @Override
    public void dispose() {
        super.dispose();

        uninstallMenu();

        setReasonerMenuItemsEnabled(true);
    }

    /**
     * Sets the reasoner menu items listed in the reasoning menu to enabled or disabled.  When theEnable is false, only
     * the CustomProtegeOWLReasoner item will be left enabled.  Otherwise, they're all enabled
     * @param theEnable whether or not to enable all items (true) or disable all but ours (false)
     */
    private void setReasonerMenuItemsEnabled(final boolean theEnable) {
        JMenu aMenu = ComponentUtilities.getMenu(getMainWindowMenuBar(), AbstractOWLModelAction.REASONING_MENU);

        if (aMenu == null || !aMenu.getText().equals(AbstractOWLModelAction.REASONING_MENU)) {
			// this little gem is here if the OWL plugin has not already been loaded, which i think is the only way
			// the menu is not in the menu bar.  knowing that protege is single threaded on the EDT, we can can take
			// advantage of that by re-trying this method after all the current swing events are processed, which includes
			// protege finishing the load of the plugins.
			//
			// There is a danger that this will endlessly call itself, but if it does, the application won't hang.  it
			// will probably run a little slower, but this shouldn't kill it.  and if the reasoning menu is never loaded,
			// the owl plugin either wasnt loaded, or failed to load.  and since we depend on that plugin, we're not
			// going to worry about this case.  Hoorah for swing hax.
			SwingUtilities.invokeLater(new Runnable() { public void run() { setReasonerMenuItemsEnabled(theEnable); } });
            return;
        }

        for (int aItemIndex = 0; aItemIndex < aMenu.getMenuComponentCount(); aItemIndex++) {
            Component aComp = aMenu.getMenuComponent(aItemIndex);
            if (aComp instanceof JMenuItem) {
                JMenuItem aItem = (JMenuItem) aComp;

                aItem.setEnabled(theEnable || aItem.getText().equals(CustomProtegeOWLReasoner.getReasonerName()));
            }

            if (aComp instanceof JSeparator) {
                // this is a little hackish, but the first separator encountered in the menu list is what
                // divides the menu items for the reasoners from the rest of the menu items.  so we know if
                // we've reached this component, we've iterated over all the menu items pertaining to reasoners.
                break;
            }
        }
    }

    /**
     * Removes the Explanation tab menu from the main menu bar
     * @see #installMenu
     */
    private void uninstallMenu() {
        JMenu aMenu = ComponentUtilities.getMenu(getMainWindowMenuBar(),
                                                 mTabProperties.get(TabProperties.ResourceKey.MenuLabel));

        if (aMenu != null) {
            getMainWindowMenuBar().remove(aMenu);
        }
    }

    /**
     * Initialize the preferences manager with the default preferences for keys that don't have a value yet.
     */
    private void installDefaultPreferences() {

        if (getPrefsManager().get(PrefsManager.PrefsKey.PREFS_CONCEPT_WRAPPING) == null) {
            getPrefsManager().set(PrefsManager.PrefsKey.PREFS_CONCEPT_WRAPPING, PrefsManager.PrefsValue.PREFS_CONCEPT_SMART_WRAP);
        }

        if (getPrefsManager().get(PrefsManager.PrefsKey.PREFS_IRRELEVANT_PARTS) == null) {
            getPrefsManager().set(PrefsManager.PrefsKey.PREFS_IRRELEVANT_PARTS, PrefsManager.PrefsValue.PREFS_COLOR_IRRELEVANT_PARTS);
        }
        
        if (getPrefsManager().get(PrefsManager.PrefsKey.PREFS_RENDER_SYNTAX) == null) {
            getPrefsManager().set(PrefsManager.PrefsKey.PREFS_RENDER_SYNTAX, PrefsManager.PrefsValue.PREFS_MANCHESTER_SYNTAX);
        }
    }

    /**
     * Force protege to set our reasoner to the current reasoner, the explanation tab depends on this being the case
     */
    private void forceReasoner() {
        ProtegeReasoner aReasoner = ReasonerManager.getInstance().getProtegeReasoner(getOWLModel());

        if (!(aReasoner instanceof CustomProtegeOWLReasoner)) {
            //ReasonerManager.getInstance().setProtegeReasonerClass(getOWLModel(), CustomProtegeOWLReasoner.class);

            // we're going to make our reasoner the current.  we can do this via the reasoner manager, but doing it
            // that way doesn't actually update the user interface, the old reasoner is still selected in the Reasoning
            // menu, which is not what we want.  So lets go through the menu, find the menu item that corresponds to
            // our reasoner, and select it like a user had clicked on it.
            JMenu aMenu = ComponentUtilities.getMenu(getMainWindowMenuBar(), AbstractOWLModelAction.REASONING_MENU);

            if (aMenu == null|| !aMenu.getText().equals(AbstractOWLModelAction.REASONING_MENU)) {
				// this little gem is here if the OWL plugin has not already been loaded, which i think is the only way
				// the menu is not in the menu bar.  knowing that protege is single threaded on the EDT, we can can take
				// advantage of that by re-trying this method after all the current swing events are processed, which includes
				// protege finishing the load of the plugins.
				//
				// There is a danger that this will endlessly call itself, but if it does, the application won't hang.  it
				// will probably run a little slower, but this shouldn't kill it.  and if the reasoning menu is never loaded,
				// the owl plugin either wasnt loaded, or failed to load.  and since we depend on that plugin, we're not
				// going to worry about this case.  Hoorah for swing hax.

				SwingUtilities.invokeLater(new Runnable() { public void run() { forceReasoner(); }});
                return;
            }

            for (int aItemIndex = 0; aItemIndex < aMenu.getMenuComponentCount(); aItemIndex++) {
                Component aComp = aMenu.getMenuComponent(aItemIndex);

                if (aComp instanceof JMenuItem) {
                    JMenuItem aItem = (JMenuItem) aComp;

                    if (aItem.getText().equals(CustomProtegeOWLReasoner.getReasonerName())) {
                        aItem.doClick();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Creates the UI for the explanation tab
     * @inheritDoc
     */
    public void initialize() {
        setReasonerMenuItemsEnabled(false);
        forceReasoner();

        setLabel(mTabProperties.get(TabProperties.ResourceKey.TabLabel));
        setShortDescription(mTabProperties.get(TabProperties.ResourceKey.TabDescription));
        setIcon(Icons.getInstanceIcon());

        // instantiate our custom class tree...basically a clone of the regular protege class tree, but without the
        // editing abilities, which i think we want to keep people from doing in the explanation tab
        mClassTree = new ClassTree(getProject());
        mClassTree.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent selectionEvent) {
                updateWidget();
            }
        });

        // this will set the above class tree widget as the underlying class tree for this tab.  each tab has a notion
        // of a class tree associated with it, so we want to tell the tab that his is our class tree
        setClsTree(mClassTree.getTree());

        // lets set up the UI
        JPanel aContent = new JPanel();

        // layout the columns we'll use on screen.
        FormLayout aLayout = new FormLayout("left:pref:none, 5dlu, left:pref:grow");

        DefaultFormBuilder aBuilder = new DefaultFormBuilder(aLayout, aContent);

        aBuilder.setColumn(1);
        aBuilder.setRow(1);

        CellConstraints c = new CellConstraints();

        // add the row that we're going to drop all the components into
        aBuilder.appendRow("top:default:grow");

        // add our components to the UI
        aBuilder.add(createAxiomPanel(), c.xywh(aBuilder.getColumn(), aBuilder.getRow(), 1, 1, "default, fill"));
        aBuilder.add(createExplanationPanel(), c.xywh(aBuilder.getColumn() + 2, aBuilder.getRow(), 1, 1,
                                                      CellConstraints.FILL, CellConstraints.FILL));

        JSplitPane aSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        aSplitPane.setDividerLocation(250);
        aSplitPane.setLeftComponent(mClassTree);
        aSplitPane.setRightComponent(aContent);

        // set the layout for the main container
        setLayout(new BorderLayout());

        // and add our stuff!
        add(aSplitPane, BorderLayout.CENTER);

        installMenu();
    }

    /**
     * Adds the explanation menu to the main menu bar
     * @see #uninstallMenu
     */
    private void installMenu() {
        JMenu aMenu = new JMenu(mTabProperties.get(TabProperties.ResourceKey.MenuLabel));
        
        JMenuItem aPrefsItem = new JMenuItem(mTabProperties.get(TabProperties.ResourceKey.MenuPrefsLabel));
        aPrefsItem.addActionListener(this);
        aPrefsItem.setActionCommand(CMD_SHOW_PREFS);

        aMenu.add(aPrefsItem);

        boolean aReadPermission = isAllowedToReadExplanationServer();
        boolean aModifyPermission = isAllowedToModifyExplanationServer();
        
        if (aReadPermission || aModifyPermission) {
        	aMenu.addSeparator();
        }
        	
        if (aModifyPermission) {
        	JMenuItem aReloadItem = new JMenuItem(mTabProperties.get(TabProperties.ResourceKey.MenuReloadLabel));
        	aReloadItem.addActionListener(this);
        	aReloadItem.setActionCommand(CMD_RELOAD);

        	aMenu.add(aReloadItem);

        	JMenuItem aShutdownItem = new JMenuItem(mTabProperties.get(TabProperties.ResourceKey.MenuShutdownLabel));
        	aShutdownItem.addActionListener(this);
        	aShutdownItem.setActionCommand(CMD_SHUTDOWN);

        	aMenu.add(aShutdownItem);
        }
        	
        /*
        	JMenuItem aLoadItem = new JMenuItem(mTabProperties.get(TabProperties.ResourceKey.MenuLoadLabel));
        	aLoadItem.addActionListener(this);
        	aLoadItem.setActionCommand(CMD_LOAD);

        	aMenu.add(aLoadItem);
        */

        if (aReadPermission) {
        	JMenuItem aInfoItem = new JMenuItem(mTabProperties.get(TabProperties.ResourceKey.MenuInfoLabel));
        	aInfoItem.addActionListener(this);
        	aInfoItem.setActionCommand(CMD_INFO);

        	aMenu.add(aInfoItem);
        }
        
        getMainWindowMenuBar().add(aMenu);
        
    }

	/**
	 * Create the main GUI panel for the explanation tab
	 * @return the explanation tab GUI
	 */
    private JPanel createExplanationPanel() {
		FormLayout aLayout = new FormLayout("min, 4dlu, min, 4dlu, min, 4dlu, min, center:default:grow, min, 4dlu, min, 10dlu, pref, 5dlu");

		DefaultFormBuilder aBuilder = new DefaultFormBuilder(aLayout);
		
		try {
			aBuilder.setColumn(1);
			aBuilder.setRow(1);

			aBuilder.appendSeparator(mTabProperties.get(TabProperties.ResourceKey.ExplanationPanelLabel));

			aBuilder.appendRow("top:10dlu:none");

			aBuilder.appendRow("top:default:grow");

			// set the correct starting point to build the form from
			aBuilder.setColumn(1);
			aBuilder.setRow(3);

			mExplanationPanel = new ExplanationPanel(this);
			mExplanationPanel.setText(mTabProperties.get(TabProperties.ResourceKey.DefaultExplanationText));

			CellConstraints c = new CellConstraints();

			aBuilder.add(new JScrollPane(mExplanationPanel), c.xywh(aBuilder.getColumn(), aBuilder.getRow(), 13, 1,
			                                                    CellConstraints.DEFAULT, CellConstraints.FILL));

			aBuilder.appendRow("top:pref:none");
			aBuilder.nextRow();
			
			mShowIrrelevantParts = new JRadioButton(getTabProperties().get(TabProperties.ResourceKey.LabelShowIrrelevantParts));
			mColorIrrelevantParts = new JRadioButton(getTabProperties().get(TabProperties.ResourceKey.LabelColorIrrelevantParts));
			mHideIrrelevantParts = new JRadioButton(getTabProperties().get(TabProperties.ResourceKey.LabelHideIrrelevantParts));
			
			mShowIrrelevantParts.addChangeListener( this );
			mColorIrrelevantParts.addChangeListener( this );
			mHideIrrelevantParts.addChangeListener( this );
			
			ButtonGroup aIrrelevantPartsGroup = new ButtonGroup();
			aIrrelevantPartsGroup.add(mShowIrrelevantParts);
			aIrrelevantPartsGroup.add(mColorIrrelevantParts);
			aIrrelevantPartsGroup.add(mHideIrrelevantParts);

	        String aIrrelevantPartsValue = ExplanationTab.getPrefsManager().get(PrefsManager.PrefsKey.PREFS_IRRELEVANT_PARTS);
	        if (aIrrelevantPartsValue.equals(PrefsManager.PrefsValue.PREFS_SHOW_IRRELEVANT_PARTS.value()))
	        	mShowIrrelevantParts.setSelected(true);
	        else if (aIrrelevantPartsValue.equals(PrefsManager.PrefsValue.PREFS_COLOR_IRRELEVANT_PARTS.value()))
	        	mColorIrrelevantParts.setSelected(true);
	        else if (aIrrelevantPartsValue.equals(PrefsManager.PrefsValue.PREFS_HIDE_IRRELEVANT_PARTS.value()))
	        	mHideIrrelevantParts.setSelected(true);
	        
			aBuilder.add(new JLabel(getTabProperties().get(TabProperties.ResourceKey.LabelIrrelevantParts)),
			    	c.xywh(1, aBuilder.getRow(), 1, 1, CellConstraints.LEFT, CellConstraints.CENTER));
			
			aBuilder.add(mShowIrrelevantParts,
			        c.xywh(3, aBuilder.getRow(), 1, 1, CellConstraints.LEFT, CellConstraints.CENTER));
			
			aBuilder.add(mColorIrrelevantParts,
			            c.xywh(5, aBuilder.getRow(), 1, 1, CellConstraints.LEFT, CellConstraints.CENTER));
		    
			aBuilder.add(mHideIrrelevantParts,
						c.xywh(7, aBuilder.getRow(), 1, 1, CellConstraints.LEFT, CellConstraints.CENTER));

			mStatusMsg = new JLabel();
			aBuilder.add(mStatusMsg,
						c.xywh(9, aBuilder.getRow(), 1, 1, CellConstraints.RIGHT, CellConstraints.CENTER));

			mMoreExplanationsAction = new GetMoreExplanationsAction();
			aBuilder.add(new JButton(mMoreExplanationsAction),
						c.xywh(11, aBuilder.getRow(), 1, 1, CellConstraints.RIGHT, CellConstraints.CENTER));

			mThrobber = ThrobberIcon.circleThrobber(7, 1f, 5f);
        	mThrobber.setOpaque(false);
        	mThrobber.setMinimumSize(new Dimension(20, 20));
        	mThrobber.setPreferredSize(new Dimension(20, 20));

			aBuilder.add(mThrobber,
						c.xywh(13, aBuilder.getRow(), 1, 1, CellConstraints.RIGHT, CellConstraints.FILL));

		}
		catch( RuntimeException e ) {
			e.printStackTrace();
		}

        return aBuilder.getPanel();
    }

    private JPanel createAssertedAxiomPanel() {

        
        // TODO: make all of these single selection
        mEquivClassWidget = new SimpleClsListWidget(new ResourceKey(mTabProperties.get(TabProperties.ResourceKey.EquivalentClassListLabel)));
        mEquivClassWidget.setup(this.getDescriptor(), false, getProject(), getOWLModel().getOWLThingClass(), null);
        mEquivClassWidget.setSlot(getKnowledgeBase().getSlot(OWLNames.Slot.EQUIVALENT_CLASS));
        mEquivClassWidget.initialize();
        mEquivClassWidget.setEditable(false);
        mEquivClassWidget.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent theEvent) {
                if (mEquivClassWidget.getSelection().size() > 0) {
                    clearSelections(mSuperClassWidget, mInferredSubClassWidget, mInferredSuperClassWidget, mSubClassWidget);
                    showInitialExplanation(mEquivClassWidget.getSelection(), ExplainType.Equivalent);
                }
            }
        });

        mSuperClassWidget = new SimpleClsListWidget(new ResourceKey(mTabProperties.get(TabProperties.ResourceKey.SuperClassListLabel)));
        mSuperClassWidget.setup(this.getDescriptor(), false, getProject(), getOWLModel().getOWLThingClass(), null);
        mSuperClassWidget.setSlot(getKnowledgeBase().getSlot(Model.Slot.DIRECT_SUPERCLASSES));
        mSuperClassWidget.initialize();
        mSuperClassWidget.setEditable(false);
        mSuperClassWidget.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent theEvent) {
                if (mSuperClassWidget.getSelection().size() > 0) {
                    clearSelections(mSubClassWidget, mInferredSubClassWidget, mInferredSuperClassWidget, mEquivClassWidget);
                    showInitialExplanation(mSuperClassWidget.getSelection(), ExplainType.SuperClass);
                }
            }
        });

        mSubClassWidget = new SimpleClsListWidget(new ResourceKey(mTabProperties.get(TabProperties.ResourceKey.SubClassListLabel)));
        mSubClassWidget.setup(this.getDescriptor(), false, getProject(), getOWLModel().getOWLThingClass(), null);
        mSubClassWidget.setSlot(getKnowledgeBase().getSlot(Model.Slot.DIRECT_SUBCLASSES));
        mSubClassWidget.initialize();
        mSubClassWidget.setEditable(false);
        mSubClassWidget.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent theEvent) {
                if (mSubClassWidget.getSelection().size() > 0) {
                    clearSelections(mSuperClassWidget, mInferredSubClassWidget, mInferredSuperClassWidget, mEquivClassWidget);
                    showInitialExplanation(mSubClassWidget.getSelection(), ExplainType.SubClass);
                }
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout( new BoxLayout(panel,BoxLayout.Y_AXIS) );

        panel.add(mSuperClassWidget);
        panel.add(mSubClassWidget);
        
        return panel;
    }
    
    private JPanel createInferredAxiomPanel() {
        mInferredSubClassWidget = new SimpleClsListWidget(new ResourceKey(mTabProperties.get(TabProperties.ResourceKey.InferredSubClassListLabel)), true, "getInferredSubclasses");
        mInferredSubClassWidget.setup(this.getDescriptor(), false, getProject(), getOWLModel().getOWLThingClass(), null);
        mInferredSubClassWidget.setSlot(getOWLModel().getProtegeInferredSubclassesProperty());
        mInferredSubClassWidget.initialize();
        mInferredSubClassWidget.setEditable(false);
        mInferredSubClassWidget.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent theEvent) {
                if (mInferredSubClassWidget.getSelection().size() > 0) {
                    clearSelections(mSuperClassWidget, mSubClassWidget, mInferredSuperClassWidget, mEquivClassWidget);
                    showInitialExplanation(mInferredSubClassWidget.getSelection(), ExplainType.SubClass);
                }
            }
        });

        mInferredSuperClassWidget = new SimpleClsListWidget(new ResourceKey(mTabProperties.get(TabProperties.ResourceKey.InferredSuperClassListLabel)), true, "getInferredSuperclasses");
        mInferredSuperClassWidget.setup(this.getDescriptor(), false, getProject(), getOWLModel().getOWLThingClass(), null);
        mInferredSuperClassWidget.setSlot(getOWLModel().getProtegeInferredSuperclassesProperty());
        mInferredSuperClassWidget.initialize();
        mInferredSuperClassWidget.setEditable(false);
        mInferredSuperClassWidget.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent theEvent) {
                if (mInferredSuperClassWidget.getSelection().size() > 0) {
                    clearSelections(mSuperClassWidget, mSubClassWidget, mInferredSubClassWidget, mEquivClassWidget);
                    showInitialExplanation(mInferredSuperClassWidget.getSelection(), ExplainType.SuperClass);
                }
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout( new BoxLayout(panel,BoxLayout.Y_AXIS) );

        panel.add(mInferredSuperClassWidget);
        panel.add(mInferredSubClassWidget);
        
        return panel;
    }

	/**
	 * Setup and show an explanation.  This is called to display the first available explanation from when something
	 * is selected in the axiom panel.  The get more explanations button is enabled and the status is reset before
	 * firing off the explanation grab & render in a separate thread
	 * @param theSelection the selection in the GUI to build the axiom to be explained from
	 * @param theType the type of explanation needed
	 */
	private void showInitialExplanation(Collection theSelection, ExplainType theType) {
		mMoreExplanationsAction.setEnabled(true);
		mStatusMsg.setText("");

		mLastExplainType = theType;
		mLastSelection = theSelection;

		showExplanations(theSelection, theType, 1);
	}

	/**
	 * Create the GUI panel that shows the asserted and inferred axioms for selection in the explanation tab.
	 * @return the axiom panel GUI
	 */
    private JPanel createAxiomPanel() {
    	FormLayout aLayout = new FormLayout("left:pref:grow, left:pref:grow");

        DefaultFormBuilder aBuilder = new DefaultFormBuilder(aLayout);

        aBuilder.setColumn(1);
        aBuilder.setRow(1);

        aBuilder.appendSeparator(mTabProperties.get(TabProperties.ResourceKey.AxiomPanelLabel));
        
    	JTabbedPane tabbedPane = new JTabbedPane();
    	tabbedPane.add("Asserted", createAssertedAxiomPanel() );
    	tabbedPane.add("Inferred", createInferredAxiomPanel() );
    	
        aBuilder.appendRow("top:5dlu:none");
        
        aBuilder.appendRow("top:pref:grow");

        // set the proper starting location
        aBuilder.setColumn(1);
        aBuilder.setRow(3);

        CellConstraints c = new CellConstraints();

        aBuilder.add(tabbedPane, c.xywh(aBuilder.getColumn(), aBuilder.getRow(), 2, 1, "fill, fill"));


    	return aBuilder.getPanel();
    }

	/**
	 * @inheritDoc
	 */
    @Override
    public void synchronizeClsTree(Collection theList) {
        // this will use the underlying implementation to update the class tree widget
        super.synchronizeClsTree(theList);

        // now update ourself
        updateWidget();
    }

	/**
	 * Synchronize the selection of the class tree with the provided selected item.  This change will propogate to
	 * all other interested tabs.
	 * @param theClass the new selected class.
	 */
	public void setSelection(RDFSClass theClass) {
		ComponentUtilities.setSelectedObjectPath(mClassTree.getTree(), ModelUtilities.getPathToRoot(theClass));
	}
	
	/**
	 * @inheritDoc
	 */
    @Override
    public void synchronizeToInstances(Collection theList) {
        // we can ignore this for now, we dont care if the instance stuff is getting updated
    }

	/**
	 * Updates the state of the widgets in the explanation panel based on the current selection in the class tree.
	 */
    private void updateWidget() {
        
        if (mClassTree != null && !mClassTree.getSelection().isEmpty()) {
            mExplanationPanel.setText(mTabProperties.get(TabProperties.ResourceKey.DefaultExplanationText));
        	mLastExplainedAxiom = null;
        	mLastExplanations = null;
        	
            Cls aCls = (Cls) mClassTree.getSelection().iterator().next();

            mSubClassWidget.setInstance(aCls);
            mSuperClassWidget.setInstance(aCls);
            mEquivClassWidget.setInstance(aCls);
            mInferredSubClassWidget.setInstance(aCls);
            mInferredSuperClassWidget.setInstance(aCls);

            mSubClassWidget.setEditable(false);
            mSuperClassWidget.setEditable(false);
            mEquivClassWidget.setEditable(false);
            mInferredSubClassWidget.setEditable(false);
            mInferredSuperClassWidget.setEditable(false);
        }
    }

	/**
	 * Action method for when a hyperlink is clicked in an explanation rendering.  This will fire off the events to get
	 * the selected instance/class displayed in the main protege window.
	 * @param theURI the uri of the concept to display
	 */
    protected void show(URI theURI) {
        // so this is what we have to search by, looking based on the URI does not work, which is awfully confusing
        String aName = getOWLModel().getResourceNameForURI(theURI.toString());

        RDFSNamedClass aClass = getOWLModel().getOWLNamedClass(aName);

        // TODO: maybe show some sort of alert here to let them know that the link wont work because we cant find the class?
        if (aClass != null) {

            Collection path = ModelUtilities.getPathToRoot(aClass);
            ComponentUtilities.setSelectedObjectPath(mClassTree.getTree(), path);
        }
    }

    /////////////////////////////////////////////////////
    ////////////////// Event Handling ///////////////////
    /////////////////////////////////////////////////////

	/**
	 * @inheritDoc
	 */
    public void actionPerformed(ActionEvent theEvent) {
        String aCommand = theEvent.getActionCommand();

        if (aCommand.equals(CMD_SHOW_PREFS)) {
            doShowPreferences();
            updateRenderedExplanation();
        } else if (aCommand.equals(CMD_RELOAD)) {
        	doReloadServer();
        } else if (aCommand.equals(CMD_SHUTDOWN)) {
        	doShutdownServer();
        } else if (aCommand.equals(CMD_LOAD)) {
        	doLoadServer();
        } else if (aCommand.equals(CMD_INFO)) {
        	doInfoServer();
        }
    }

	/**
	 * Show the preferences dialog
	 */
    private void doShowPreferences() {
        new PreferencesDialog(this).setVisible(true);
    }
    
    /**
     * Triggers a reload of the explanation server, and shows a confirmation/error message when the operation finishes. 
     * The reload is done asynchronously, so this method will return before the reload finishes. (Any confirmation/error windows will be
     * executed in the Swing thread after this method finishes.)
     */
    private void doReloadServer() {
    	int confirm = JOptionPane.showConfirmDialog(this, 
    			mTabProperties.get(TabProperties.ResourceKey.ConfirmDialogReloadMessage), 
    			mTabProperties.get(TabProperties.ResourceKey.ConfirmDialogReloadTitle), 
    			JOptionPane.YES_NO_OPTION, 
    			JOptionPane.QUESTION_MESSAGE);
    	
    	if (confirm == JOptionPane.NO_OPTION) {
    		return;
    	}
    	
    	// start showing wait cursors and the progress bars as this operation is potentially lengthy and it will execute in the background thread
    	final WaitCursor waitCursor = new WaitCursor(this);

        waitCursor.show();
		mThrobber.startAnimationRunning();
		final LengthyOperationPrompt lengthyOperationPrompt = new LengthyOperationPrompt(mTabProperties.get(TabProperties.ResourceKey.DialogReloadInProgressTitle), 
				mTabProperties.get(TabProperties.ResourceKey.DialogReloadInProgressMessage)); 
		
    	// create a new thread to send the reload request to the server, and wait for the response 
    	new Thread(new Runnable() {
    		public void run() {
    			DigClientAdmin admin = CustomReasonerProjectPlugin.getDigClientAdmin(getProject());
    	    	
    	    	if (admin != null) {
    	    		try {
						admin.reload();
						
						// prepare and display completion notification in the Swing Thread
						SimpleAdminActionResult successfulCompletionNotification = new SimpleAdminActionResult(mTabProperties.get(TabProperties.ResourceKey.DialogReloadOperationSuccessfulMessage), waitCursor);
						successfulCompletionNotification.setLengthyOperationPrompt(lengthyOperationPrompt);
						SwingUtilities.invokeLater(successfulCompletionNotification);
					} catch (DigClientException e) {
						e.printStackTrace();
						
						// prepare and display failure notification in the Swing Thread
						SimpleAdminActionResult failureNotification = new SimpleAdminActionResult(mTabProperties.get(TabProperties.ResourceKey.DialogReloadOperationFailedMessage), e, waitCursor);
						failureNotification.setLengthyOperationPrompt(lengthyOperationPrompt);
						SwingUtilities.invokeLater(failureNotification);					
					} 
    	    	}		
    		}
    	}).start();    	
    }
    
    /**
     * Triggers a shutdown of the explanation server, and shows a confirmation/error message when the operation finishes. 
     * The shutdown is done asynchronously, so this method will return before the shutdown finishes. (Any confirmation/error windows will be
     * executed in the Swing thread after this method finishes.)
     */
    private void doShutdownServer() {
    	int confirm = JOptionPane.showConfirmDialog(this, 
    			mTabProperties.get(TabProperties.ResourceKey.ConfirmDialogShutdownMessage), 
    			mTabProperties.get(TabProperties.ResourceKey.ConfirmDialogShutdownTitle), 
    			JOptionPane.YES_NO_OPTION, 
    			JOptionPane.QUESTION_MESSAGE);
    	
    	if (confirm == JOptionPane.NO_OPTION) {
    		return;
    	}
    	
    	// start showing wait cursors as this operation may block because of network latency, and therefore, it will execute in the background thread
    	final WaitCursor waitCursor = new WaitCursor(this);

        waitCursor.show();
		mThrobber.startAnimationRunning();
		

    	// send the shutdown request to the server
    	new Thread(new Runnable() {
    		public void run() {
    			DigClientAdmin admin = CustomReasonerProjectPlugin.getDigClientAdmin(getProject());
    	    	
    	    	if (admin != null) {
    	    		try {
						admin.shutdown();						
						
						// prepare and display completion notification in the Swing Thread
						SwingUtilities.invokeLater(new SimpleAdminActionResult(mTabProperties.get(TabProperties.ResourceKey.DialogShutdownOperationSuccessfulMessage), waitCursor));
					} catch (DigClientException e) {
						e.printStackTrace();
						
						// prepare and display the error notification in the Swing Thread
						SwingUtilities.invokeLater(new SimpleAdminActionResult(mTabProperties.get(TabProperties.ResourceKey.DialogShutdownOperationFailedMessage), e, waitCursor));					
					}
    	    	}		
    		}
    	}).start();    	
    }
    
    /**
     * Loads a new ontology to the server. This method is not finished, as loading of ontologies to the explanation server
     * has not yet been fully specified. 
     */
    private void doLoadServer() {
    	DigClientAdmin admin = CustomReasonerProjectPlugin.getDigClientAdmin(getProject());
    	
    	// TODO
    	
    	if (admin != null) {
    		admin.load();
    	}
    }
    
    /**
     * Retrieves and displays information about the explanation server. (This method only triggers the process, and it will return before the actual
     * window is shown; the request to the server is done asynchronously in the background to avoid blocking of the Swing thread because of network latency,
     * then the window is displayed back in the Swing Thread.)
     */
    private void doInfoServer() {
    	final WaitCursor waitCursor = new WaitCursor(this);

    	// start displaying wait cursors when we initiate request to the server
        waitCursor.show();
		mThrobber.startAnimationRunning();

    	// send the info request to the server
    	new Thread(new Runnable() {
    		public void run() {
    			DigClientAdmin admin = CustomReasonerProjectPlugin.getDigClientAdmin(getProject());
    	    	
    	    	if (admin != null) {
    	    		try {
						final Properties properties = admin.info();
						
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								waitCursor.hide();
								mThrobber.stopAnimationRunning();

								new ServerInformationDialog(ExplanationTab.this, properties).setVisible(true);
							}
						});
					} catch (DigClientException e) {
						e.printStackTrace();
						SwingUtilities.invokeLater(new SimpleAdminActionResult(mTabProperties.get(TabProperties.ResourceKey.DialogInfoOperationFailedMessage), e, waitCursor));					
					}
    	    	}		
    		}
    	}).start(); 
    }

    /////////////////////////////////////////////////////
    /////////////////////////////////////////////////////
    /////////////////////////////////////////////////////

	/**
	 * Return the tab resource bundle utility
	 * @return the tab properties instance
	 */
    protected TabProperties getTabProperties() {
        return mTabProperties;
    }

	/**
	 * Return the tab user preferences manager
	 * @return the prefs manager
	 */
    public static PrefsManager getPrefsManager() {
        return mPrefsManager;
    }

	/**
	 * Show explanations in the explanation tab.  This should always be called from the EDT.
	 * @param theSelection the selection in the GUI we should build the axiom to be explained from
	 * @param theExplanationType the explanation type
	 * @param theNum the number of explanations to fetch from the explanation server
	 */
    private void showExplanations(Collection theSelection, ExplainType theExplanationType, int theNum) {
        if (theSelection.isEmpty())
            return;

		// this function is always called from the EDT...

		// we'll set the wait cursor...
        WaitCursor aCursor = new WaitCursor(this);

        aCursor.show();
		mThrobber.startAnimationRunning();

		Instance aSelected = (Instance)theSelection.iterator().next();

		OWLAxiom aAxiom = null;

		// this will do the conversion of the selected item to the OWLAPI.  this should be quick (i think) so its ok
		// to do this on the EDT.  it makes the error handling case easier since we're doing this on the EDT, we don't
		// have to jump back onto the EDT to show it.
		{
			try {
				OWLClass aSelectedClass = convert(aSelected);

				switch (theExplanationType) {
					case Equivalent: {
						OWLClass aEqClass = convert((Instance) mClassTree.getSelection().iterator().next());

						HashSet<OWLDescription> aEqSet = new HashSet<OWLDescription>();
						aEqSet.add(aEqClass);
						aEqSet.add(aSelectedClass);

						aAxiom = mFactory.getOWLEquivalentClassesAxiom(aEqSet);

						break;
					}
					case SubClass: {
						OWLClass aParentClass = convert((Instance) mClassTree.getSelection().iterator().next());

						aAxiom = mFactory.getOWLSubClassAxiom(aSelectedClass, aParentClass);

						break;
					}
					case SuperClass: {
						OWLClass aParentClass = convert((Instance) mClassTree.getSelection().iterator().next());

						aAxiom = mFactory.getOWLSubClassAxiom(aParentClass, aSelectedClass);

						break;
					}
				}
			}
			catch (OWLException ex) {
				ex.printStackTrace();

				LOGGER.log(Level.SEVERE, "An error while converting a protege concept in order to get an explanation", ex);

				JOptionPane.showMessageDialog( this,
											   "Cannot get the explanation. There was an error while converting the selected class for explanation",
											   "Error", JOptionPane.ERROR_MESSAGE );
				return;
			}
		}

		if (aAxiom != null) {
			new Thread(new ExplanationUpdater(aAxiom, theNum, aCursor)).start();
		}
    }

	/**
	 * Renders explanations into the GUI
	 * @param theAxiom the axiom being explained
	 * @param theExplanations the explanations for the axiom
	 */
    private void renderExplanations(OWLAxiom theAxiom, Set<Set<OWLAxiom>> theExplanations) {
    	mLastExplainedAxiom = theAxiom;
    	mLastExplanations = theExplanations;
    	
        StringWriter aWriter = new StringWriter();

		StringBuffer aHeader = new StringBuffer();
		int aSize = theExplanations.size();
		boolean aAll = !mMoreExplanationsAction.isEnabled();

		if (aSize == 0) {
			aHeader.append("No explanations available.");
		}
		else if (aSize == 1) {
			if (aAll) {
				aHeader.append("Only available explanation ");
			}
			else {
				aHeader.append("First available explanation");
			}
		}
		else {
			if (aAll && aSize < LIMIT) {
				aHeader.append("All ").append(aSize).append(" explanations");
			}
			else {
				aHeader.append("Limit Reached, First ").append(LIMIT).append(" explanations shown");
			}
		}

		mStatusMsg.setText(aHeader.toString());

        mExplanationRenderer.startRendering(aWriter);
        
    	try {
             mExplanationRenderer.render(theAxiom, theExplanations);

             mExplanationRenderer.endRendering();

             StringBuffer aExplanationText = new StringBuffer(aWriter.toString());

             mExplanationPanel.setText(aExplanationText.toString());

             revalidate();
             repaint();
         }
         catch (OWLException ex) {
             ex.printStackTrace();
             LOGGER.log(Level.SEVERE, "An error while rendering an explanation!", ex);

             JOptionPane.showMessageDialog( this,
                     "Cannot render the explanation. There was an error while rendering the explanation(s).",
                     "Error", JOptionPane.ERROR_MESSAGE );
         }
         catch (IOException ex) {
             ex.printStackTrace();
             LOGGER.log(Level.SEVERE, "An error while rendering an explanation!", ex);

             JOptionPane.showMessageDialog( this,
                     "Cannot render the explanation. There was an IO error while rendering.",
                     "Error", JOptionPane.ERROR_MESSAGE );
         }
    }

	/**
	 * Convert a Protege Instance into an OWLAPI object
	 * @param theInstance the instance to convert
	 * @return the instance as an OWLAPI object
	 * @throws OWLException thrown if there is an error while converting
	 */
    private OWLClass convert(Instance theInstance) throws OWLException {
        RDFResource aNamedClass = (RDFResource) theInstance;

        return mFactory.getOWLClass(URI.create(aNamedClass.getURI()));
    }

	/**
	 * Clear the selection of all the lists
	 * @param theLists the list whose selections should be cleared
	 */
    private void clearSelections(AbstractSlotWidget... theLists) {
        for (AbstractSlotWidget theList : theLists) {
            theList.clearSelection();
        }
    }

	/**
	 * Re-renders the explanation based on the latest explanation renderer
	 */
    public void updateRenderedExplanation() {
    	updateExplanationRenderer();

        if (mLastExplainedAxiom == null) {
            updateWidget();
        }
        else {
        	renderExplanations(mLastExplainedAxiom, mLastExplanations);
        }
    }

	/**
	 * Updates the renderer used to render explanation based on the currently selected preferences
	 */
    private void updateExplanationRenderer() {
    	mExplanationRenderer = new HTMLManchesterSyntaxExplanationRenderer();
    	
        try {
        	mExplanationRenderer.setShortFormProvider(shortFormProvider);
        	mExplanationRenderer.setSortingEnabled( true );
        	mExplanationRenderer.setVisibilityStyleSupported( false );
        	mExplanationRenderer.setIndentingEnabled( false );
        
            String aValue = getPrefsManager().get(PrefsManager.PrefsKey.PREFS_RENDER_SYNTAX);
            if (aValue.equals(PrefsManager.PrefsValue.PREFS_ABSTRACT_SYNTAX.value())) {
               
            }
            else if (aValue.equals(PrefsManager.PrefsValue.PREFS_MANCHESTER_SYNTAX.value())) {
            	
            }
            
            aValue = getPrefsManager().get(PrefsManager.PrefsKey.PREFS_CONCEPT_WRAPPING);
            if (aValue.equals(PrefsManager.PrefsValue.PREFS_CONCEPT_NO_WRAP.value())) {
            	mExplanationRenderer.setWrapLines( false );
            }
            else if (aValue.equals(PrefsManager.PrefsValue.PREFS_CONCEPT_WRAP.value())) {
            	mExplanationRenderer.setWrapLines( true );
            	mExplanationRenderer.setSmartIndent( false );
            }
            else if (aValue.equals(PrefsManager.PrefsValue.PREFS_CONCEPT_SMART_WRAP.value())) {
            	mExplanationRenderer.setWrapLines( true );
            	mExplanationRenderer.setSmartIndent( true );
            }
            else if (!aValue.equals( "" )){
            	LOGGER.log(Level.WARNING, "Invalid concept wrapping value " + aValue );
            }
            
            aValue = getPrefsManager().get(PrefsManager.PrefsKey.PREFS_IRRELEVANT_PARTS);

            if (aValue.equals(PrefsManager.PrefsValue.PREFS_SHOW_IRRELEVANT_PARTS.value())) {
            	mExplanationRenderer.setIrrelevantParts( IrrelevantPartHandling.SHOW );
            }
            else if (aValue.equals(PrefsManager.PrefsValue.PREFS_COLOR_IRRELEVANT_PARTS.value())) {
            	mExplanationRenderer.setIrrelevantParts( IrrelevantPartHandling.LOWLIGHT );
            }
            else if (aValue.equals(PrefsManager.PrefsValue.PREFS_HIDE_IRRELEVANT_PARTS.value())) {
            	mExplanationRenderer.setIrrelevantParts( IrrelevantPartHandling.HIDE );
            }
            else if (!aValue.equals( "" )) {
            	LOGGER.log(Level.WARNING, "Invalid preference value for irrelevant parts " + aValue );
            }        
        }
        catch (Exception ex) {
            ex.printStackTrace();

            LOGGER.log(Level.SEVERE, "An error while creating explanation renderer", ex);
        }
    }

	/**
	 * @inheritDoc
	 */
	public void stateChanged(ChangeEvent e) {
        if (mShowIrrelevantParts.isSelected()) {
            ExplanationTab.getPrefsManager().set(PrefsManager.PrefsKey.PREFS_IRRELEVANT_PARTS,
                                       PrefsManager.PrefsValue.PREFS_SHOW_IRRELEVANT_PARTS);
        }
        else if (mColorIrrelevantParts.isSelected()) {
            ExplanationTab.getPrefsManager().set(PrefsManager.PrefsKey.PREFS_IRRELEVANT_PARTS,
                                       PrefsManager.PrefsValue.PREFS_COLOR_IRRELEVANT_PARTS);
        }
        else if (mHideIrrelevantParts.isSelected()) {
            ExplanationTab.getPrefsManager().set(PrefsManager.PrefsKey.PREFS_IRRELEVANT_PARTS,
                                       PrefsManager.PrefsValue.PREFS_HIDE_IRRELEVANT_PARTS);
        }
        
        updateRenderedExplanation();
	}

	/**
	 * Thread for getting explanations from the explanation server.  This is a long process in most cases and should
	 * NEVER be run on the EDT.  UI updates will be fired on the EDT via invokeLater using {@link RenderUpdater}.
	 * @see RenderUpdater
	 */
	private class ExplanationUpdater implements Runnable {
		private OWLAxiom mAxiom;
		private int mNum;
		private WaitCursor mWaitCursor;

		public ExplanationUpdater(OWLAxiom theAxiom, int theNum, WaitCursor theCursor) {
			mAxiom = theAxiom;
			mNum = theNum;
			mWaitCursor = theCursor;
		}

		public void run() {
			Set<Set<OWLAxiom>> aExplanations = null;

			try {
				DigReasoner aReasoner = CustomReasonerProjectPlugin.getDigReasoner(getProject());
				aExplanations = aReasoner.getExplanations(mAxiom, mNum);

				SwingUtilities.invokeLater(new RenderUpdater(mAxiom, aExplanations, mWaitCursor));
			}
			catch (Exception ex) {
				SwingUtilities.invokeLater(new RenderUpdater(ex, mWaitCursor));
			}
		}
	}

	/**
	 * Thread for displaying the results of getting some explanations, either by rendering the explanations to the tab
	 * or by showing the error message.  This is intendted to be run *ON* the EDT, so run this via SwingUtilities methods
	 * of either invokeLater or invokeAndWait
	 */
	private class RenderUpdater implements Runnable {
		private OWLAxiom mAxiom;
		private Set<Set<OWLAxiom>> mExplanations;
		private Exception mException;
		private WaitCursor mWaitCursor;

		public RenderUpdater(Exception theException, WaitCursor theCursor) {
			mWaitCursor = theCursor;
			mException = theException;
		}

		public RenderUpdater(OWLAxiom theAxiom, Set<Set<OWLAxiom>> theExplanations, WaitCursor theCursor) {
			mWaitCursor = theCursor;
			mAxiom = theAxiom;
			mExplanations = theExplanations;
		}

		public void run() {
			mWaitCursor.hide();
			mThrobber.stopAnimationRunning();

			if (mException == null) {
				if (mExplanations != null) {
					renderExplanations(mAxiom, mExplanations);
				}
			}
			else {
				if (mException instanceof ErrorResponseException) {
					mException.printStackTrace();

					LOGGER.log(Level.SEVERE, "Error response received from explanation server", mException);

					JOptionPane.showMessageDialog(ExplanationTab.this,
												  "Cannot get the explanation.  An error response was received from the explanation server.\n" +
												  mException.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );

				}
				else if (mException instanceof OWLException) {
					mException.printStackTrace();

					LOGGER.log(Level.SEVERE, "An error while getting an explanation!", mException);

					JOptionPane.showMessageDialog(ExplanationTab.this,
												  "Cannot get the explanation. There was an error while generating the explanation.",
												  "Error", JOptionPane.ERROR_MESSAGE );
				}
				else {
					mException.printStackTrace();

					LOGGER.log(Level.SEVERE, "An error while getting an explanation!", mException);

					JOptionPane.showMessageDialog(ExplanationTab.this,
												  "Cannot get the explanation. There was a possible error communicating with the explanation server.\nPlease refer to the logs for more information",
												  "Error", JOptionPane.ERROR_MESSAGE );
				}
			}
		}
	}

	/**
	 * Action implementation for the Get More Explanations button"
	 */
	public class GetMoreExplanationsAction extends AbstractAction {
		public GetMoreExplanationsAction() {
			super("Get More Explanations");
		}
		
		public void actionPerformed(ActionEvent theEvent) {
			setEnabled(false);

			// this will fire the explanation stuff in a separate thread, so its safe to call here, it should not block the EDT
			showExplanations(mLastSelection, mLastExplainType, LIMIT);
		}
	}
	
	/**
	 * A prompt displayed during lengthy operations. Currently, it consists of a dialog box with a specified message
	 * and a progress bar (with indeterminate amount of time).
	 * 
	 * @author Blazej Bulka <blazej@clarkparsia.com>
	 */
	public static class LengthyOperationPrompt extends JFrame {
		/**
		 * The progress bar
		 */
		private JProgressBar progressBar;		
		
		/**
		 * Space between the components in the window and the external margins (in pixels).
		 */
		private static final int SPACING = 15;
		
		/**
		 * Creates a prompt displayed during lengthy operations in the form of a dialog window with a progress bar.
		 * 
		 * @param title the title of the window
		 * @param message the message displayed above the progress bar
		 */
		public LengthyOperationPrompt(String title, String message) {
			super(title);
			
			// panel with the actual contents (label with the message and the progress bar)
			JPanel panel = new JPanel();
			
			progressBar = new JProgressBar();
			progressBar.setIndeterminate(true);
			
			BorderLayout layout = new BorderLayout();
			// set the spacing between the label and progress bar
			layout.setVgap(SPACING);
			
			panel.setLayout(layout);
			panel.add(new JLabel(message), BorderLayout.NORTH);
			panel.add(progressBar, BorderLayout.CENTER);			
			
			// add the panel to the center of the window
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(panel, BorderLayout.CENTER);
			
			// some space around the main content (margins)
			getContentPane().add(Box.createHorizontalStrut(SPACING), BorderLayout.WEST);
			getContentPane().add(Box.createHorizontalStrut(SPACING), BorderLayout.EAST);
			
			getContentPane().add(Box.createVerticalStrut(SPACING), BorderLayout.NORTH);
			getContentPane().add(Box.createVerticalStrut(SPACING), BorderLayout.SOUTH);
			
			pack();
			setVisible(true);
			setAlwaysOnTop(true);
			
			ComponentUtilities.center(this);
		}
	}
	
	/**
	 * A convenience class to encapsulate the code displaying simple results of administrative actions.
	 * Since administrative actions are typically executed in background thread, any visual result must be 
	 * displayed back in the Swing Thread. This class displays simple messages (strings) after the operation completes
	 * (successfully or unsuccessfully). It also closes any LengthyOperationPrompts that may be displayed. 
	 * 
	 * @author Blazej Bulka <blazej@clarkparsia.com>
	 */
	public class SimpleAdminActionResult implements Runnable {
		/**
		 * Message to be displayed to the user
		 */
		private String message;
		
		/**
		 * Exception that caused the admin action to fail. (This field must be null
		 * for successful operations).
		 */
		private Exception exception;
		
		/**
		 * Any wait cursor that was displayed for this operation.
		 */
		private WaitCursor waitCursor;
		
		/**
		 * Any prompt that was displayed for this operation.
		 */
		private LengthyOperationPrompt lengthyOperationPrompt;
		
		/**
		 * Creates a completion message to be displayed for a successful admin operation.
		 * 
		 * @param message the message to be displayed
		 * @param waitCursor a wait cursor that was displayed for the operation
		 */
		public SimpleAdminActionResult(String message, WaitCursor waitCursor) {
			this.message = message;
			this.waitCursor = waitCursor;
		}
		
		/**
		 * Creates a failure message to be displayed for an unsuccessful admin operation.
		 * 
		 * @param message the message to be displayed
		 * @param exception the exception that caused the this object to be created
		 * @param waitCursor a wait cursor that was displayed for the operation
		 */
		public SimpleAdminActionResult(String message, Exception exception, WaitCursor waitCursor) {
			this.message = message;
			this.exception = exception;
			this.waitCursor = waitCursor;
		}
		
		/**
		 * Creates a failure message to be displayed for an unsuccessful admin operation.
		 * 
		 * @param exception the exception that caused the this object to be created
		 * @param waitCursor a wait cursor that was displayed for the operation
		 */
		public SimpleAdminActionResult(Exception exception, WaitCursor waitCursor) {
			this.exception = exception;
			this.waitCursor = waitCursor;
		}
	
		/**
		 * Sets the reference to the lengthy operation prompt that was displayed during the operation.
		 * Before any completion/failure notification is displayed in the Swing thread, this prompt will be made to close.
		 * 
		 * @param lengthyOperationPrompt the prompt for the lengthy operation
		 */
		public void setLengthyOperationPrompt(LengthyOperationPrompt lengthyOperationPrompt) {
			this.lengthyOperationPrompt = lengthyOperationPrompt;
		}
 		
		/**
		 * The code that will be run in the Swing Thread, closing any lengthy operation prompts, turning off wait cursors,
		 * and displaying completion/failure messages.
		 */
		public void run() {
			if (waitCursor != null) {
				waitCursor.hide();
			}
			mThrobber.stopAnimationRunning();

			if (lengthyOperationPrompt != null) {
				lengthyOperationPrompt.dispose();
			}
			
			if (exception == null) {
				// action was successful				
				JOptionPane.showMessageDialog(ExplanationTab.this, 
						message, 
						mTabProperties.get(TabProperties.ResourceKey.DialogAdminOperationSuccessfulTitle), 
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				// action was unsuccessful
				JOptionPane.showMessageDialog(ExplanationTab.this, 
						message, 
						mTabProperties.get(TabProperties.ResourceKey.DialogAdminOperationFailedTitle), 
						JOptionPane.ERROR_MESSAGE);				
			}
		}
		
	}
	
	
}
