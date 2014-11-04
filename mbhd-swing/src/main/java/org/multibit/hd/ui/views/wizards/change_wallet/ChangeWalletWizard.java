package org.multibit.hd.ui.views.wizards.change_wallet;

import com.google.common.base.Optional;
import org.multibit.hd.ui.views.wizards.AbstractWizard;
import org.multibit.hd.ui.views.wizards.AbstractWizardPanelView;

import java.util.Map;


/**
 * <p>Wizard to provide the following to UI for "credentials" wizard:</p>
 * <ol>
 * <li>Enter credentials. These could be a password, a Trezor PIN or no Trezor PIN if no credentials are required</li>
 * </ol>
 *
 * @since 0.0.1
 *  
 */
public class ChangeWalletWizard extends AbstractWizard<org.multibit.hd.ui.views.wizards.change_wallet.ChangeWalletWizardModel> {

    public ChangeWalletWizard(ChangeWalletWizardModel model, boolean isExiting) {
        super(model, isExiting, Optional.absent());
    }

    @Override
    protected void populateWizardViewMap(Map<String, AbstractWizardPanelView> wizardViewMap) {

        wizardViewMap.put(
                ChangeWalletState.CREDENTIALS_ENTER_PASSWORD.name(),
                new org.multibit.hd.ui.views.wizards.change_wallet.ChangeWalletPanelView(this, ChangeWalletState.CREDENTIALS_ENTER_PASSWORD.name()));

        wizardViewMap.put(
                ChangeWalletState.CREDENTIALS_ENTER_PIN.name(),
                new ChangeWalletPanelView(this, ChangeWalletState.CREDENTIALS_ENTER_PIN.name()));

        // TODO - no Trezor PIN panel

    }


    // TODO Ensure that restore buttons have somewhere to transition to before the Welcome wizard starts up

}

