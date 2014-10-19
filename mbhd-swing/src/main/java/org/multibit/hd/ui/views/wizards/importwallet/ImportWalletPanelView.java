package org.multibit.hd.ui.views.wizards.importwallet;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.*;
import net.miginfocom.swing.MigLayout;
import org.multibit.hd.core.concurrent.SafeExecutors;
import org.multibit.hd.core.dto.WalletId;
import org.multibit.hd.core.dto.WalletSummary;
import org.multibit.hd.core.events.SecurityEvent;
import org.multibit.hd.core.exceptions.ExceptionHandler;
import org.multibit.hd.core.exceptions.WalletLoadException;
import org.multibit.hd.core.managers.InstallationManager;
import org.multibit.hd.core.managers.WalletManager;
import org.multibit.hd.core.services.ContactService;
import org.multibit.hd.core.services.CoreServices;
import org.multibit.hd.ui.audio.Sounds;
import org.multibit.hd.ui.events.view.ViewEvents;
import org.multibit.hd.ui.languages.Languages;
import org.multibit.hd.ui.languages.MessageKey;
import org.multibit.hd.ui.views.components.*;
import org.multibit.hd.ui.views.components.display_security_alert.DisplaySecurityAlertModel;
import org.multibit.hd.ui.views.components.display_security_alert.DisplaySecurityAlertView;
import org.multibit.hd.ui.views.components.enter_password.EnterPasswordModel;
import org.multibit.hd.ui.views.components.enter_password.EnterPasswordView;
import org.multibit.hd.ui.views.components.panels.PanelDecorator;
import org.multibit.hd.ui.views.components.select_wallet.SelectWalletModel;
import org.multibit.hd.ui.views.components.select_wallet.SelectWalletView;
import org.multibit.hd.ui.views.fonts.AwesomeIcon;
import org.multibit.hd.ui.views.wizards.AbstractWizard;
import org.multibit.hd.ui.views.wizards.AbstractWizardPanelView;
import org.multibit.hd.ui.views.wizards.WizardButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.swing.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 * <p>View to provide the following to UI:</p>
 * <ul>
 * <li>Credentials: Enter password</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public class ImportWalletPanelView extends AbstractWizardPanelView<ImportWalletWizardModel,ImportWalletPanelModel> {

    private static final Logger log = LoggerFactory.getLogger(ImportWalletPanelView.class);

    // Panel specific components
    private ModelAndView<DisplaySecurityAlertModel, DisplaySecurityAlertView> displaySecurityPopoverMaV;
    private ModelAndView<EnterPasswordModel, EnterPasswordView> enterPasswordMaV;
    private ModelAndView<SelectWalletModel, SelectWalletView> selectWalletMaV;

    final ListeningExecutorService checkPasswordExecutorService = SafeExecutors.newSingleThreadExecutor("check-password");

    /**
     * @param wizard The wizard managing the states
     */
    public ImportWalletPanelView(AbstractWizard<ImportWalletWizardModel> wizard, String panelName) {

        super(wizard, panelName, MessageKey.PASSWORD_TITLE, AwesomeIcon.LOCK);

    }

    @Override
    public void newPanelModel() {

        displaySecurityPopoverMaV = Popovers.newDisplaySecurityPopoverMaV(getPanelName());
        enterPasswordMaV = Components.newEnterPasswordMaV(getPanelName());
        enterPasswordMaV.getView().setAddLabel(false);
        selectWalletMaV = Components.newSelectWalletMaV(getPanelName());

        // Configure the panel model
        final ImportWalletPanelModel panelModel = new ImportWalletPanelModel(
                getPanelName(),
                enterPasswordMaV.getModel(),
                selectWalletMaV.getModel()
        );
        setPanelModel(panelModel);

        // Bind it to the wizard model
        getWizardModel().setEnterPasswordPanelModel(panelModel);

        // Register components
        registerComponents(displaySecurityPopoverMaV, enterPasswordMaV, selectWalletMaV);

    }

    @Override
    public void initialiseContent(JPanel contentPanel) {

        contentPanel.setLayout(new MigLayout(
                Panels.migXLayout(),
                "[]", // Column constraints
                "[]0[]32[]0[]32[]" // Row constraints
        ));

        contentPanel.add(Labels.newPasswordNote(), "wrap");
        contentPanel.add(enterPasswordMaV.getView().newComponentPanel(), "wrap");

        contentPanel.add(Labels.newSelectWalletNote(), "wrap");
        contentPanel.add(selectWalletMaV.getView().newComponentPanel(), "wrap");

    }

    @Override
    protected void initialiseButtons(AbstractWizard<ImportWalletWizardModel> wizard) {

        PanelDecorator.addCancelApply(this, wizard);

    }

    @Override
    public void fireInitialStateViewEvents() {

        // Initialise with "Unlock" disabled to force users to enter a credentials
        ViewEvents.fireWizardButtonEnabledEvent(
                getPanelName(),
                WizardButton.APPLY,
                true
        );

    }


    @Override
    public boolean beforeShow() {

        List<WalletSummary> wallets = WalletManager.getWalletSummaries();

        selectWalletMaV.getModel().setWalletList(wallets);
        selectWalletMaV.getView().setEnabled(true);

        return true;
    }

    @Override
    public void afterShow() {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                enterPasswordMaV.getView().requestInitialFocus();

                // Check for any security alerts
                Optional<SecurityEvent> securityEvent = CoreServices.getApplicationEventService().getLatestSecurityEvent();
                if (securityEvent.isPresent()) {

                    displaySecurityPopoverMaV.getModel().setValue(securityEvent.get());

                    // Show the security alert as a popover
                    Panels.showLightBoxPopover(displaySecurityPopoverMaV.getView().newComponentPanel());

                }

                selectWalletMaV.getView().updateViewFromModel();

            }
        });

    }

    @Override
    public boolean beforeHide(boolean isExitCancel) {

        // Don't block an exit
        if (isExitCancel) {
            return true;
        }

        // Start the spinner (we are deferring the hide)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                // Ensure the view shows the spinner and disables components
                getApplyButton().setEnabled(false);
                getCancelButton().setEnabled(false);

                enterPasswordMaV.getView().setSpinnerVisibility(true);
                selectWalletMaV.getView().setEnabled(false);

            }
        });

        // Check the password (might take a while so do it asynchronously while showing a spinner)
        // Tar pit (must be in a separate thread to ensure UI updates)
        ListenableFuture<Boolean> passwordFuture = checkPasswordExecutorService.submit(new Callable<Boolean>() {

            @Override
            public Boolean call() {

                // Need a very short delay here to allow the UI thread to update
                Uninterruptibles.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
                return checkPassword();

            }
        });
        Futures.addCallback(passwordFuture, new FutureCallback<Boolean>() {

                    @Override
                    public void onSuccess(Boolean result) {
                        // Check the result
                        if (result) {

                            WalletId walletId = selectWalletMaV.getModel().getValue().getWalletId();
                            CharSequence password = enterPasswordMaV.getModel().getValue();
                            ContactService contactService = CoreServices.getCurrentContactService();
                            contactService.importContacts(password, walletId);

                            // Maintain the spinner while the initialisation continues

                            // Manually deregister the MaVs
                            CoreServices.uiEventBus.unregister(displaySecurityPopoverMaV);
                            CoreServices.uiEventBus.unregister(enterPasswordMaV);
                            CoreServices.uiEventBus.unregister(selectWalletMaV);

                            // Trigger the deferred hide
                            ViewEvents.fireWizardDeferredHideEvent(getPanelName(), false);

                        } else {

                            // Wait just long enough to be annoying (anything below 2 seconds is comfortable)
                            Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);

                            // Failed
                            Sounds.playBeep();

                            // Ensure the view hides the spinner and enables components
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {

                                    enterPasswordMaV.getView().incorrectPassword();

                                    getApplyButton().setEnabled(true);
                                    getCancelButton().setEnabled(true);
                                    enterPasswordMaV.getView().setSpinnerVisibility(false);

                                    enterPasswordMaV.getView().requestInitialFocus();
                                    selectWalletMaV.getView().setEnabled(true);

                                }
                            });

                        }

                    }

                    @Override
                    public void onFailure(Throwable t) {

                        // Ensure the view hides the spinner and enables components
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {

                                getApplyButton().setEnabled(true);
                                getCancelButton().setEnabled(true);
                                enterPasswordMaV.getView().setSpinnerVisibility(false);

                                enterPasswordMaV.getView().requestInitialFocus();
                                selectWalletMaV.getView().setEnabled(true);
                            }
                        });

                        // Should not have seen an error
                        ExceptionHandler.handleThrowable(t);
                    }
                }
        );

        // Defer the hide operation
        return false;
    }

    /**
     * @return True if the selected wallet can be opened with the given password.
     */
    private boolean checkPassword() {

        CharSequence password = enterPasswordMaV.getModel().getValue();

        if (!"".equals(password)) {
            // Restore current wallet ID and Password
            WalletId CurrentWalletID = WalletManager.INSTANCE.getCurrentWalletSummary().get().getWalletId();
            CharSequence CurrentWalletPassword = WalletManager.INSTANCE.getCurrentWalletSummary().get().getPassword();
            // Attempt to open the wallet
            WalletId walletId = selectWalletMaV.getModel().getValue().getWalletId();
            try {
                WalletManager.INSTANCE.open(InstallationManager.getOrCreateApplicationDataDirectory(), walletId, password);
            } catch (WalletLoadException wle) {
                WalletManager.INSTANCE.open(InstallationManager.getOrCreateApplicationDataDirectory(), CurrentWalletID, CurrentWalletPassword);
                // Mostly this will be from a bad
                log.error(wle.getMessage());
                // Assume bad credentials
                return false;
            }
            Optional<WalletSummary> currentWalletSummary = WalletManager.INSTANCE.getCurrentWalletSummary();
            if (currentWalletSummary.isPresent()) {

                WalletManager.INSTANCE.open(InstallationManager.getOrCreateApplicationDataDirectory(), CurrentWalletID, CurrentWalletPassword);
                CoreServices.logHistory(Languages.safeText(MessageKey.PASSWORD_VERIFIED));

                return true;
            }

        }

        // Must have failed to be here
        log.error("Failed attempt to import contact!");

        return false;

    }

    @Override
    public void updateFromComponentModels(Optional componentModel) {

        // No need to update the wizard it has the references

    }

}