package org.multibit.hd.ui.views.screens.tools;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import net.miginfocom.swing.MigLayout;
import org.multibit.hd.core.dto.BitcoinNetworkStatus;
import org.multibit.hd.core.dto.BitcoinNetworkSummary;
import org.multibit.hd.core.dto.WalletSummary;
import org.multibit.hd.core.events.BitcoinNetworkChangedEvent;
import org.multibit.hd.core.managers.InstallationManager;
import org.multibit.hd.core.managers.WalletManager;
import org.multibit.hd.core.services.CoreServices;
import org.multibit.hd.ui.MultiBitUI;
import org.multibit.hd.ui.events.view.WizardHideEvent;
import org.multibit.hd.ui.languages.MessageKey;
import org.multibit.hd.ui.views.components.Buttons;
import org.multibit.hd.ui.views.components.Panels;
import org.multibit.hd.ui.views.screens.AbstractScreenView;
import org.multibit.hd.ui.views.screens.Screen;
import org.multibit.hd.ui.views.wizards.Wizards;
import org.multibit.hd.ui.views.wizards.edit_wallet.EditWalletState;
import org.multibit.hd.ui.views.wizards.edit_wallet.EditWalletWizardModel;
import org.multibit.hd.ui.views.wizards.change_wallet.ChangeWalletRequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.multibit.hd.ui.views.screens.contacts.ContactsScreenModel;
import org.multibit.hd.ui.views.screens.contacts.ContactsScreenView;



import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * <p>View to provide the following to application:</p>
 * <ul>
 * <li>Provision of components and layout for the tools detail display</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public class ToolsScreenView extends AbstractScreenView<ToolsScreenModel> {

  private static final Logger log = LoggerFactory.getLogger(ToolsScreenView.class);

  private JButton showEmptyWalletButton;

  private Optional<BitcoinNetworkChangedEvent> unprocessedBitcoinNetworkChangedEvent = Optional.absent();

  /**
   * @param panelModel The model backing this panel view
   * @param screen     The screen to filter events from components
   * @param title      The key to the main title of this panel view
   */
  public ToolsScreenView(ToolsScreenModel panelModel, Screen screen, MessageKey title) {
    super(panelModel, screen, title);
  }

  @Override
  public void newScreenModel() {

  }

  @Override
  public JPanel initialiseScreenViewPanel() {

    MigLayout layout = new MigLayout(
      Panels.migXYDetailLayout(),
      "6[]6[]6[]6", // Column constraints
      "6[]6[]6[]6" // Row constraints
    );

    JPanel contentPanel = Panels.newPanel(layout);

    JButton primaryButton = Buttons.newShowEditWalletButton(getShowWalletDetailsAction());

    showEmptyWalletButton = Buttons.newShowEmptyWalletButton(getShowEmptyWalletAction());

    // Initially show the button disabled - it is enabled when the network is synced
    showEmptyWalletButton.setEnabled(false);

    // Check for any Bitcoin network events that may have occurred before this screen
    // is initialised
    unprocessedBitcoinNetworkChangedEvent = CoreServices.getApplicationEventService().getLatestBitcoinNetworkChangedEvent();

    contentPanel.add(primaryButton, MultiBitUI.LARGE_BUTTON_MIG + ",align center,push");
    contentPanel.add(showEmptyWalletButton, MultiBitUI.LARGE_BUTTON_MIG + ",align center,push");
    contentPanel.add(Buttons.newShowRepairWalletButton(getShowRepairWalletAction()), MultiBitUI.LARGE_BUTTON_MIG + ",align center,push,wrap");

    JButton changePasswordButton = Buttons.newShowChangePasswordButton(getShowChangePasswordAction());
    contentPanel.add(changePasswordButton, MultiBitUI.LARGE_BUTTON_MIG + ",align center,push");
    contentPanel.add(Buttons.newShowVerifyNetworkButton(getShowVerifyNetworkAction()), MultiBitUI.LARGE_BUTTON_MIG + ",align center,push");
    contentPanel.add(Buttons.newShowAboutButton(getShowAboutAction()), MultiBitUI.LARGE_BUTTON_MIG + ",align center,push,wrap");

    contentPanel.add(Buttons.newLargeShowSignMessageWizardButton(getShowSignMessageWizardAction()), MultiBitUI.LARGE_BUTTON_MIG + ",align center,push");
    contentPanel.add(Buttons.newShowVerifyMessageWizardButton(getShowVerifyMessageWizardAction()), MultiBitUI.LARGE_BUTTON_MIG + ",align center,push");
    contentPanel.add(Buttons.newShowChangeWalletButton(getShowChangeWalletAction()), MultiBitUI.LARGE_BUTTON_MIG + ",align center,push");

    setInitialised(true);
    return contentPanel;
  }

  @Override
  public void afterShow() {

    // Ensure any unprocessed bitcoin network change events are dealt with
    if (isInitialised() && unprocessedBitcoinNetworkChangedEvent.isPresent()) {
      updateEmptyButton(unprocessedBitcoinNetworkChangedEvent.get());
      unprocessedBitcoinNetworkChangedEvent = Optional.absent();
    }
  }

  /**
   * @param event The "Bitcoin network changed" event - one per block downloaded during synchronization
   */
  @Subscribe
  public void onBitcoinNetworkChangeEvent(final BitcoinNetworkChangedEvent event) {

    if (!isInitialised()) {
      // Remember the last bitcoin change event if the panel is not initialised
      unprocessedBitcoinNetworkChangedEvent = Optional.of(event);
      log.trace("Not initialised so remembering the the unprocessed Bitcoin network change event " + event.getSummary());
      return;
    }

    Preconditions.checkNotNull(event, "'event' must be present");
    Preconditions.checkNotNull(event.getSummary(), "'summary' must be present");

    BitcoinNetworkSummary summary = event.getSummary();

    Preconditions.checkNotNull(summary.getSeverity(), "'severity' must be present");
    Preconditions.checkNotNull(summary.getMessageKey(), "'errorKey' must be present");
    Preconditions.checkNotNull(summary.getMessageData(), "'errorData' must be present");

    // Keep the UI response to a minimum due to the volume of these events
    updateEmptyButton(event);

  }


  /**
   * @return An action to show the "welcome wizard"
   */
  private AbstractAction getShowAboutAction() {
    return new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {

        // Show the About screen
        Panels.showLightBox(Wizards.newAboutWizard().getWizardScreenHolder());

      }
    };
  }

  /**
   * @return An action to show the "sign message" wizard
   */
  private AbstractAction getShowSignMessageWizardAction() {
    return new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {

        Panels.showLightBox(Wizards.newSignMessageWizard().getWizardScreenHolder());
      }
    };
  }

  /**
   * @return An action to show the "verify message" wizard
   */
  private AbstractAction getShowVerifyMessageWizardAction() {
    return new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {

        Panels.showLightBox(Wizards.newVerifyMessageWizard().getWizardScreenHolder());
      }
    };
  }

  /**
   * @return An action to show the "wallet details" tool
   */
  private AbstractAction getShowWalletDetailsAction() {
    return new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {

        Panels.showLightBox(Wizards.newEditWalletWizard().getWizardScreenHolder());
      }
    };
  }

  /**
   * @return An action to show the "change wallet" tool
   */
  private AbstractAction getShowChangeWalletAction() {
      return new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {

              Panels.showLightBox(Wizards.newChangeWalletWizard(ChangeWalletRequestType.PASSWORD).getWizardScreenHolder());
          }
      };
  }

  /**
   * @return An action to show the "change credentials" tool
   */
  private AbstractAction getShowChangePasswordAction() {
    return new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {

        Panels.showLightBox(Wizards.newChangePasswordWizard().getWizardScreenHolder());
      }
    };
  }

  /**
   * @return An action to show the "verify network" tool
   */
  private AbstractAction getShowVerifyNetworkAction() {
    return new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {

        Panels.showLightBox(Wizards.newVerifyNetworkWizard().getWizardScreenHolder());
      }
    };
  }

  /**
   * @return An action to show the "repair wallet" tool
   */
  private AbstractAction getShowRepairWalletAction() {
    return new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {

        Panels.showLightBox(Wizards.newRepairWalletWizard().getWizardScreenHolder());
      }
    };
  }

  /**
   * @return An action to show the "empty wallet" tool
   */
  private AbstractAction getShowEmptyWalletAction() {
    return new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {

        Panels.showLightBox(Wizards.newEmptyWalletWizard().getWizardScreenHolder());
      }
    };
  }

  /**
   * <p>Handle the transfer of data from a closing wizard</p>
   *
   * @param event The "wizard hide" event
   */
  @Subscribe
  public void onWizardHideEvent(WizardHideEvent event) {

    // Filter other events
    if (event.isExitCancel()) {
      return;
    }
    if (event.getPanelName().equals(EditWalletState.EDIT_WALLET.name())) {

      // Persist the data from the wizard model into wallet
      WalletSummary walletSummary = ((EditWalletWizardModel) event.getWizardModel()).getWalletSummary();

      File applicationDataDirectory = InstallationManager.getOrCreateApplicationDataDirectory();
      final File currentWalletSummaryFile = WalletManager.INSTANCE.getCurrentWalletSummaryFile(applicationDataDirectory).get();
      WalletManager.updateWalletSummary(currentWalletSummaryFile, walletSummary);

    }


  }

  /**
   * <p>Ensure that a wallet can only be emptied once synchronization has completed</p>
   *
   * @param event The "Bitcoin network changed" event - one per block downloaded during synchronization
   */
  private void updateEmptyButton(BitcoinNetworkChangedEvent event) {

    boolean currentEnabled = showEmptyWalletButton.isEnabled();

    final boolean newEnabled;

    // NOTE: Show empty wallet is disabled when the network is not available
    // because it is possible that a second wallet is generating transactions using
    // addresses that this one has not displayed yet. This would lead to the same
    // address being used twice.
    log.trace("Empty button status is " + currentEnabled);
    switch (event.getSummary().getSeverity()) {
      case RED:
        // Always disabled on RED
        newEnabled = false;
        log.trace("Severity = red");
        break;
      case AMBER:
        // Enable on AMBER only if unrestricted
        newEnabled = InstallationManager.unrestricted;
        log.trace("Severity = AMBER, newEnabled = " + newEnabled);
        break;
      case GREEN:
        // Enable on GREEN only if synchronized or unrestricted
        newEnabled = BitcoinNetworkStatus.SYNCHRONIZED.equals(event.getSummary().getStatus()) || InstallationManager.unrestricted;
        log.trace("Severity = GREEN, newEnabled = " + newEnabled);
        break;
      default:
        // Unknown status
        throw new IllegalStateException("Unknown event severity " + event.getSummary().getStatus());
    }

    // Test for a change in condition
    if (currentEnabled != newEnabled) {

      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          log.trace("Changing button enable state, newEnabled = " + newEnabled);

          showEmptyWalletButton.setEnabled(newEnabled);
        }
      });

    }

  }

}
