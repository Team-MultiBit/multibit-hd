package org.multibit.hd.ui.views.wizards.change_wallet;

/**
 * <p>Enum to provide the following to "change_wallet" wizard model:</p>
 * <ul>
 * <li>State identification</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public enum ChangeWalletState {

    /**
     * Enter a password
     */
    CREDENTIALS_ENTER_PASSWORD,

    /**
     * Enter a Trezor PIN
     */
    CREDENTIALS_ENTER_PIN,

    /**
     * No Trezor PIN required
     */
    CREDENTIALS_NO_PIN_REQUIRED,

}
