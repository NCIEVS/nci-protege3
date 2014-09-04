package com.clarkparsia.protege.explanation.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Apr 24, 2007 1:21:46 PM
 *
 * @author Michael Grove <mhgrove@hotmail.com>
 */
public class TabProperties {
    public enum ResourceKey {
        PrefsErrorURLMsg("PREFS_ERROR_URL_MSG"),
        PrefsErrorTitle("PREFS_ERROR_TITLE"),
        ButtonOkLabel("BUTTON_OK_LABEL"),
        ButtonCloseLabel("BUTTON_CLOSE_LABEL"),
        ToolTipServerURL("TOOLTIP_SERVER_URL"),
        LabelServerURL("LABEL_SERVER_URL"),
        LabelSynchRealTime("LABEL_SYNCH_REALTIME"),
        LabelSynchOnQuery("LABEL_SYNCH_ONQUERY"),
        LabelIrrelevantParts("LABEL_IRRELEVANT_PARTS"),
        LabelShowIrrelevantParts("LABEL_SHOW_IRRELEVANT_PARTS"),
        LabelColorIrrelevantParts("LABEL_COLOR_IRRELEVANT_PARTS"),
        LabelHideIrrelevantParts("LABEL_HIDE_IRRELEVANT_PARTS"),
        LabelConceptWrapping("LABEL_CONCEPT_WRAPPING"),
        LabelConceptsNoWrap("LABEL_CONCEPTS_NO_WRAP"),
        LabelConceptsWrap("LABEL_CONCEPTS_WRAP"),
        LabelConceptsSmartWrap("LABEL_CONCEPTS_SMART_WRAP"),
        LabelExplanationSyntax("LABEL_EXPLANATION_SYNTAX"),
        LabelManchesterSyntax("LABEL_MANCHESTER_SYNTAX"),
        LabelAbstractSyntax("LABEL_ABSTRACT_SYNTAX"),
        DialogPrefsLabel("DIALOG_PREFS_LABEL"),
        DialogPrefsTitle("DIALOG_PREFS_TITLE"),
        MenuLabel("MENU_LABEL"),
        MenuPrefsLabel("MENU_PREFS_LABEL"),
        MenuReloadLabel("MENU_RELOAD_LABEL"),
        MenuShutdownLabel("MENU_SHUTDOWN_LABEL"),
        MenuLoadLabel("MENU_LOAD_LABEL"),
        MenuInfoLabel("MENU_INFO_LABEL"),
        LimitMessage("LIMIT_MESSAGE"),
        ExplanationPanelLabel("EXPLANATION_PANEL_LABEL"),
        AxiomPanelLabel("AXIOM_PANEL_LABEL"),
        SubClassListLabel("SUBCLASS_LIST_LABEL"),
        SuperClassListLabel("SUPERCLASS_LIST_LABEL"),
        InferredSubClassListLabel("INFERRED_SUBCLASS_LIST_LABEL"),
        InferredSuperClassListLabel("INFERRED_SUPERCLASS_LIST_LABEL"),
        EquivalentClassListLabel("EQUIVALENT_CLASS_LIST_LABEL"),
        ButtonPreviousLabel("BUTTON_PREVIOUS_LABEL"),
        ButtonPreviousToolTip("BUTTON_PREVIOUS_TOOLTIP"),
        ButtonNextLabel("BUTTON_NEXT_LABEL"),
        ButtonNextToolTip("BUTTON_NEXT_TOOLTIP"),
        TabLabel("TAB_LABEL"),
        TabDescription("TAB_DESCRIPTION"),
        DefaultExplanationText("DEFAULT_EXPLANATION_TEXT"),
        DefaultServerURL("DEFAULT_SERVER_URL"),
        ConfirmDialogReloadTitle("CONFIRM_DIALOG_RELOAD_TITLE"),
        ConfirmDialogReloadMessage("CONFIRM_DIALOG_RELOAD_MESSAGE"),
        ConfirmDialogShutdownTitle("CONFIRM_DIALOG_SHUTDOWN_TITLE"),
        ConfirmDialogShutdownMessage("CONFIRM_DIALOG_SHUTDOWN_MESSAGE"),
        DialogAdminOperationSuccessfulTitle("DIALOG_ADMIN_OPERATION_SUCCESSFUL_TITLE"),
        DialogAdminOperationFailedTitle("DIALOG_ADMIN_OPERATION_FAILED_TITLE"),
        DialogReloadOperationSuccessfulMessage("DIALOG_RELOAD_OPERATION_SUCCESSFUL_MESSAGE"),
        DialogReloadOperationFailedMessage("DIALOG_RELOAD_OPERATION_FAILED_MESSAGE"),
        DialogShutdownOperationSuccessfulMessage("DIALOG_SHUTDOWN_OPERATION_SUCCESSFUL_MESSAGE"),
        DialogShutdownOperationFailedMessage("DIALOG_SHUTDOWN_OPERATION_FAILED_MESSAGE"),
        DialogInfoOperationFailedMessage("DIALOG_INFO_OPERATION_FAILED_MESSAGE"),
        DialogServerInformationTitle("DIALOG_SERVER_INFORMATION_TITLE"),
        DialogReloadInProgressTitle("DIALOG_RELOAD_IN_PROGRESS_TITLE"),
        DialogReloadInProgressMessage("DIALOG_RELOAD_IN_PROGRESS_MESSAGE")
        ;

        private String mKey;

        ResourceKey(String theKey) {
            mKey = theKey;
        }
        String key() { return mKey; }
    }

    private Properties mProps;

    public TabProperties(InputStream theProps) {
        mProps = new Properties();

        try {
            mProps.load(theProps);
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public String get(ResourceKey theKey) {
        return mProps.getProperty(theKey.key());
    }

    public URL getURL(ResourceKey theKey) throws MalformedURLException {
        return new URL(get(theKey));
    }
}
