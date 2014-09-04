package com.clarkparsia.protege.explanation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import com.clarkparsia.protege.explanation.util.PrefsManager;
import com.clarkparsia.protege.explanation.util.TabProperties;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.WaitCursor;

/**
 * Title: A simple dialog for editing the set of preferences used by the ExplanationTab<br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Apr 24, 2007 2:06:43 PM
 *
 * @author Michael Grove <mhgrove@hotmail.com>
 * @see com.clarkparsia.protege.explanation.ExplanationTab
 */
public class PreferencesDialog extends JDialog implements ActionListener {
    private static final String CMD_OK = "CMD_OK";

    private ExplanationTab mApp;

    private JComboBox mExplanationSyntax;
    
    private JRadioButton mConceptsNoWrap;
    private JRadioButton mConceptsWrap;
    private JRadioButton mConceptsSmartWrap;

    private JRadioButton mShowIrrelevantParts;
    private JRadioButton mColorIrrelevantParts;
    private JRadioButton mHideIrrelevantParts;

    public PreferencesDialog(ExplanationTab theApp) {
        super((JFrame)null, theApp.getTabProperties().get(TabProperties.ResourceKey.DialogPrefsTitle), true);

        mApp = theApp;

        initGUI();

        pack();

        ComponentUtilities.center(this);
    }

    private void initGUI() {
        mShowIrrelevantParts = new JRadioButton(mApp.getTabProperties().get(TabProperties.ResourceKey.LabelShowIrrelevantParts));
        mColorIrrelevantParts = new JRadioButton(mApp.getTabProperties().get(TabProperties.ResourceKey.LabelColorIrrelevantParts));
        mHideIrrelevantParts = new JRadioButton(mApp.getTabProperties().get(TabProperties.ResourceKey.LabelHideIrrelevantParts));

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
        
        mConceptsNoWrap = new JRadioButton(mApp.getTabProperties().get(TabProperties.ResourceKey.LabelConceptsNoWrap));
        mConceptsWrap = new JRadioButton(mApp.getTabProperties().get(TabProperties.ResourceKey.LabelConceptsWrap));
        mConceptsSmartWrap = new JRadioButton(mApp.getTabProperties().get(TabProperties.ResourceKey.LabelConceptsSmartWrap));

        ButtonGroup aConceptWrappingGroup = new ButtonGroup();
        aConceptWrappingGroup.add(mConceptsNoWrap);
        aConceptWrappingGroup.add(mConceptsWrap);
        aConceptWrappingGroup.add(mConceptsSmartWrap);

        String aConceptWrappingValue = ExplanationTab.getPrefsManager().get(PrefsManager.PrefsKey.PREFS_CONCEPT_WRAPPING);
        if (aConceptWrappingValue.equals(PrefsManager.PrefsValue.PREFS_CONCEPT_NO_WRAP.value()))
        	mConceptsNoWrap.setSelected(true);
        else if (aConceptWrappingValue.equals(PrefsManager.PrefsValue.PREFS_CONCEPT_WRAP.value()))
        	mConceptsWrap.setSelected(true);
        else if (aConceptWrappingValue.equals(PrefsManager.PrefsValue.PREFS_CONCEPT_SMART_WRAP.value()))
        	mConceptsSmartWrap.setSelected(true);

        makeRendererCombo();

//        mSort = new JCheckBox();
//        mSort.setSelected(ExplanationTab.getPrefsManager().getSortExplanations());

        FormLayout aLayout = new FormLayout("left:pref:grow(.2), 10dlu, fill:pref:grow(.8)");

        DefaultFormBuilder aBuilder = new DefaultFormBuilder(aLayout);

        aBuilder.setDefaultDialogBorder();

        aBuilder.setRow(1);
        aBuilder.setColumn(1);
        
        aBuilder.appendRow(new RowSpec("top:pref:none"));

        CellConstraints c = new CellConstraints();
        
        aBuilder.add(new JLabel(mApp.getTabProperties().get(TabProperties.ResourceKey.LabelConceptWrapping)),
                	c.xywh(aBuilder.getColumn(), aBuilder.getRow(), 1, 1, "left, center"));

        aBuilder.add(mConceptsNoWrap,
	                c.xywh(aBuilder.getColumn() + 2, aBuilder.getRow(), 1, 1));
	
        aBuilder.appendRow(new RowSpec("bottom:pref"));
        aBuilder.nextRow(1);

        aBuilder.add(mConceptsWrap,
	                c.xywh(aBuilder.getColumn() + 2, aBuilder.getRow(), 1, 1));
	
        aBuilder.appendRow(new RowSpec("bottom:pref"));
        aBuilder.nextRow(1);
        
        aBuilder.add(mConceptsSmartWrap,
                	c.xywh(aBuilder.getColumn() + 2, aBuilder.getRow(), 1, 1));

        aBuilder.appendRow(new RowSpec("top:10dlu:grow"));
        aBuilder.appendRow(new RowSpec("bottom:pref"));
        aBuilder.nextRow(2);

        aBuilder.add(new JLabel(mApp.getTabProperties().get(TabProperties.ResourceKey.LabelExplanationSyntax)),
                     c.xywh(aBuilder.getColumn(), aBuilder.getRow(), 1, 1, "left, center"));

        aBuilder.add(mExplanationSyntax,
                     c.xywh(aBuilder.getColumn() + 2, aBuilder.getRow(), 1, 1));


        aBuilder.appendRow(new RowSpec("top:10dlu:grow"));
        aBuilder.appendRow(new RowSpec("bottom:pref"));
        aBuilder.nextRow(2);

        JButton aOkBtn = new JButton(mApp.getTabProperties().get(TabProperties.ResourceKey.ButtonOkLabel));
        aOkBtn.addActionListener(this);
        aOkBtn.setActionCommand(CMD_OK);

        ButtonBarBuilder aBtnBuilder = new ButtonBarBuilder();

        aBtnBuilder.addFixed(aOkBtn);

        aBuilder.add(aBtnBuilder.getPanel(),
                     c.xywh(aBuilder.getColumn(), aBuilder.getRow(), 3, 1, "center, bottom"));

        setContentPane(aBuilder.getPanel());
    }

    private void makeRendererCombo() {
    	mExplanationSyntax = new JComboBox();

    	mExplanationSyntax.addItem(mApp.getTabProperties().get(TabProperties.ResourceKey.LabelManchesterSyntax));
//    	mExplanationSyntax.addItem(mApp.getTabProperties().get(TabProperties.ResourceKey.LabelAbstractSyntax));

    	mExplanationSyntax.setSelectedItem(mApp.getTabProperties().get(TabProperties.ResourceKey.LabelExplanationSyntax));
    }

    //////////////////////////////////////////////////////
    //////////////////////////////////////////////////////
    //////////////////////////////////////////////////////

    public void actionPerformed(ActionEvent theEvent) {
        String aCommand = theEvent.getActionCommand();

        if (aCommand.equals(CMD_OK)) {
            doOk();
        }
    }

    private void doOk() {
        WaitCursor aCursor = new WaitCursor(mApp);

        aCursor.show();
        
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

        if (mConceptsNoWrap.isSelected()) {
            ExplanationTab.getPrefsManager().set(PrefsManager.PrefsKey.PREFS_CONCEPT_WRAPPING,
                                       PrefsManager.PrefsValue.PREFS_CONCEPT_NO_WRAP);
        }
        else if (mConceptsWrap.isSelected()) {
            ExplanationTab.getPrefsManager().set(PrefsManager.PrefsKey.PREFS_CONCEPT_WRAPPING,
                                       PrefsManager.PrefsValue.PREFS_CONCEPT_WRAP);
        }
        else if (mConceptsSmartWrap.isSelected()) {
            ExplanationTab.getPrefsManager().set(PrefsManager.PrefsKey.PREFS_CONCEPT_WRAPPING,
                                       PrefsManager.PrefsValue.PREFS_CONCEPT_SMART_WRAP);
        }
        
        dispose();
        setVisible(false);

        aCursor.hide();
    }
}
