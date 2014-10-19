package org.multibit.hd.ui.views.wizards.importwallet;

import org.multibit.hd.core.services.ContactService;
import org.multibit.hd.core.services.PersistentContactService;
import org.multibit.hd.ui.views.wizards.AbstractWizardModel;

/**
 * <p>Model object to provide the following to "credentials wizard":</p>
 * <ul>
 * <li>Storage of panel data</li>
 * <li>State transition management</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public class ImportWalletWizardModel extends AbstractWizardModel<ImportWalletState> {

    /**
     * The "enter password" panel model
     */
    private ImportWalletPanelModel enterPasswordPanelModel;

    /**
     * The "enter pin" panel model
     */
    private ImportWalletPanelModel enterPinPanelModel;

    /**
     * The type of credentials being requested password/ Trezor PIN / no Trezor PIN
     */
    private final ImportWalletRequestType credentialsRequestType;


    public ImportWalletWizardModel(ContactService contactService, ImportWalletState credentialsState, ImportWalletRequestType credentialsRequestType) {
        super(credentialsState);
        this.credentialsRequestType = credentialsRequestType;
    }

    @Override
    public String getPanelName() {
        return state.name();
    }

    /**
     * @return The credentials the user entered
     */
    public String getCredentials() {
        switch (credentialsRequestType) {
            case PASSWORD :
                return enterPasswordPanelModel.getEnterPasswordModel().getValue();
            case TREZOR_PIN:
                //return enterPinPanelModel.getEnterPinModel().getValue();
            case NO_TREZOR_PIN:
            default:
                return "";
        }
    }

    /**
     * <p>Reduced visibility for panel models only</p>
     *
     * @param enterPasswordPanelModel The "enter credentials" panel model
     */
    void setEnterPasswordPanelModel(ImportWalletPanelModel enterPasswordPanelModel) {
        this.enterPasswordPanelModel = enterPasswordPanelModel;
    }

    public ImportWalletRequestType getCredentialsRequestType() {
        return credentialsRequestType;
    }

    public ImportWalletPanelModel getEnterPinPanelModel() {
        return enterPinPanelModel;
    }

    public void setEnterPinPanelModel(ImportWalletPanelModel enterPinPanelModel) {
        this.enterPinPanelModel = enterPinPanelModel;
    }
}
