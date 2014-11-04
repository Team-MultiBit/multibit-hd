package org.multibit.hd.ui.views.wizards.change_wallet;

import org.multibit.hd.ui.views.wizards.AbstractWizardModel;

/**
 * <p>Model object to provide the following to "change wallet wizard":</p>
 * <ul>
 * <li>Storage of panel data</li>
 * <li>State transition management</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public class ChangeWalletWizardModel extends AbstractWizardModel<ChangeWalletState> {

    /**
     * The "enter password" panel model
     */
    private ChangeWalletPanelModel enterPasswordPanelModel;

    /**
     * The "enter pin" panel model
     */
    private ChangeWalletPanelModel enterPinPanelModel;

    /**
     * The type of credentials being requested password/ Trezor PIN / no Trezor PIN
     */
    private final ChangeWalletRequestType credentialsRequestType;

//    public ChangeWalletWizardModel(ContactService contactService, ChangeWalletState credentialsState, ChangeWalletRequestType credentialsRequestType) {
    public ChangeWalletWizardModel(ChangeWalletState credentialsState, ChangeWalletRequestType credentialsRequestType) {
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
    void setEnterPasswordPanelModel(ChangeWalletPanelModel enterPasswordPanelModel) {
        this.enterPasswordPanelModel = enterPasswordPanelModel;
    }

    public ChangeWalletRequestType getCredentialsRequestType() {
        return credentialsRequestType;
    }

    public ChangeWalletPanelModel getEnterPinPanelModel() {
        return enterPinPanelModel;
    }

    public void setEnterPinPanelModel(ChangeWalletPanelModel enterPinPanelModel) {
        this.enterPinPanelModel = enterPinPanelModel;
    }
}
