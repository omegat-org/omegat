/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: https://omegat.org/support

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util;

import java.awt.Window;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import javax.swing.FocusManager;

import org.jasypt.exceptions.AlreadyInitializedException;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.BasicTextEncryptor;
import org.omegat.core.Core;
import org.omegat.gui.dialogs.PasswordEnterDialogController;
import org.omegat.gui.dialogs.PasswordSetDialogController;
import org.omegat.gui.main.IMainWindow;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * A class for storing and retrieving sensitive values such as login
 * credentials, API keys, etc., from the program-wide Preferences store.
 * <p>
 * Stored values are encrypted with a "master password" (=encryption key). If
 * this has not yet been supplied to the encryption engine, the user will be
 * prompted to create it. Upon creating a master password, a "canary" value is
 * saved to preferences; the canary is used to ensure that all values are
 * encrypted with the same master password (thus ensuring that the user only
 * needs to remember one password).
 * <p>
 * The user can choose not to set a master password; in this case a master
 * password is generated for the user and stored in Preferences in plain text.
 * Values stored with the CredentialsManager will still be encrypted, but
 * because the master password is readily accessible the actual security is
 * greatly diminished. This feature was deemed required for usability, despite
 * the drawbacks.
 *
 * @author Aaron Madlon-Kay
 */
public final class CredentialsManager {

    public interface IPasswordPrompt {
        Optional<char[]> getExistingPassword(String message);

        PasswordSetResult createNewPassword();
    }

    private static final Logger LOGGER = Logger.getLogger(CredentialsManager.class.getName());
    private static final String CREDENTIALS_MANAGER_CANARY = "credentials_manager_canary";
    private static final String CREDENTIALS_MASTER_PASSWORD = "credentials_master_password";

    private static class SingletonHelper {
        private static final CredentialsManager INSTANCE = new CredentialsManager();
    }

    public static CredentialsManager getInstance() {
        return SingletonHelper.INSTANCE;
    }

    private final IPasswordPrompt prompt;
    private BasicTextEncryptor textEncryptor;

    private CredentialsManager() {
        prompt = new GuiPasswordPrompt();
        textEncryptor = new BasicTextEncryptor();
    }

    /**
     * Securely store a key-value pair. If the master password is not stored and
     * has not been input, the user will be prompted to input it.
     *
     * @param key
     *            The key for the value to store (not encrypted)
     * @param value
     *            The value to store (encrypted)
     * @return True if the value was stored successfully; false if otherwise
     *         (e.g. the user canceled)
     */
    public boolean store(String key, String value) {
        if (value.isEmpty()) {
            clear(key);
            return true;
        }
        Optional<String> encrypted = encrypt(value);
        encrypted.ifPresent(ev -> Preferences.setPreference(key, ev));
        return encrypted.isPresent();
    }

    /**
     * Check to see if a value has been securely stored for the given key.
     * <p>
     * If the master password has not been set, this will return false for all
     * keys.
     *
     * @see #isMasterPasswordSet()
     */
    public boolean isStored(String key) {
        return isMasterPasswordSet() && !Preferences.getPreference(key).isEmpty();
    }

    private synchronized Optional<String> encrypt(String text) {
        while (true) {
            try {
                return Optional.of(textEncryptor.encrypt(text));
            } catch (EncryptionInitializationException e) {
                if (!onEncryptionFailed()) {
                    return Optional.empty();
                }
            }
        }
    }

    private void setEncryptionKey(char[] password) {
        try {
            textEncryptor.setPasswordCharArray(password);
        } catch (AlreadyInitializedException e) {
            textEncryptor = new BasicTextEncryptor();
            setEncryptionKey(password);
        }
    }

    private void setMasterPassword(char[] masterPassword) {
        setEncryptionKey(masterPassword);
        store(CREDENTIALS_MANAGER_CANARY, CREDENTIALS_MANAGER_CANARY);
    }

    /**
     * Check whether or not the master password has been set. This checks only
     * for the presence of the canary value.
     */
    public boolean isMasterPasswordSet() {
        return !Preferences.getPreference(CREDENTIALS_MANAGER_CANARY).isEmpty();
    }

    /**
     * Check whether or not the master password is stored in plain text so the user doesn't need to input it.
     * The master password is considered to not be stored if {@link #isMasterPasswordSet()} returns false.
     */
    public boolean isMasterPasswordStored() {
        return isMasterPasswordSet() && !Preferences.getPreference(CREDENTIALS_MASTER_PASSWORD).isEmpty();
    }

    /**
     * Clear the stored master password (if present) and the canary value.
     * Afterwards, any encrypted values will be considered to be not set
     * ({@link #isStored(String)} returns false; {@link #retrieve(String)}
     * returns {@link Optional#empty()}).
     */
    public void clearMasterPassword() {
        clear(CREDENTIALS_MANAGER_CANARY);
        clear(CREDENTIALS_MASTER_PASSWORD);
        synchronized (this) {
            textEncryptor = new BasicTextEncryptor();
        }
    }

    /**
     * Clear the value for the given key.
     */
    public void clear(String key) {
        Preferences.setPreference(key, "");
    }

    /**
     * Retrieve the securely stored value for the given key. If the master
     * password is not stored and has not been input, the user will be prompted
     * to input it.
     *
     * @param key
     *            The key for the value to store (not encrypted)
     * @return The Optional-wrapped value, which can be empty if the user
     *         declines to enter the master password or the master password is
     *         not the correct encryption key for the value
     */
    public Optional<String> retrieve(String key) {
        String encrypted = Preferences.getPreference(key);
        if (encrypted.isEmpty()) {
            return Optional.empty();
        }
        return decrypt(encrypted);
    }

    private Optional<String> decrypt(String text) {
        if (!isMasterPasswordSet()) {
            LOGGER.warning("Trying to retrieve encrypted credentials but no master password has been set.");
            return Optional.empty();
        }
        synchronized (this) {
            while (true) {
                try {
                    return Optional.of(textEncryptor.decrypt(text));
                } catch (EncryptionOperationNotPossibleException e) {
                    LOGGER.severe(
                            "Could not decrypt stored credential with supposedly correct master password.");
                    return Optional.empty();
                } catch (EncryptionInitializationException e) {
                    if (!onDecryptionFailed()) {
                        return Optional.empty();
                    }
                }
            }
        }
    }

    private boolean onEncryptionFailed() {
        if (isMasterPasswordSet()) {
            if (useStoredMasterPassword()) {
                return true;
            }
            return promptForExistingPassword();
        } else {
            return promptForCreatingPassword();
        }
    }

    private boolean useStoredMasterPassword() {
        String mp = Preferences.getPreference(CREDENTIALS_MASTER_PASSWORD);
        if (!mp.isEmpty()) {
            setEncryptionKey(mp.toCharArray());
            if (checkCanary()) {
                return true;
            }
        }
        return false;
    }

    private boolean promptForCreatingPassword() {
        PasswordSetResult result = prompt.createNewPassword();
        switch (result.responseType) {
        case USE_INPUT:
            setMasterPassword(result.password);
            Arrays.fill(result.password, '\0');
            return true;
        case GENERATE_AND_STORE:
            String pwd = UUID.randomUUID().toString();
            setMasterPassword(pwd.toCharArray());
            Preferences.setPreference(CREDENTIALS_MASTER_PASSWORD, pwd);
            return true;
        case CANCEL:
            return false;
        }
        throw new IllegalArgumentException("Unknown response: " + result.responseType);
    }

    private boolean onDecryptionFailed() {
        if (!isMasterPasswordSet()) {
            return false;
        }
        if (useStoredMasterPassword()) {
            return true;
        }
        return promptForExistingPassword();
    }

    private boolean promptForExistingPassword() {
        String message = OStrings.getString("PASSWORD_ENTER_MESSAGE");
        while (true) {
            Optional<char[]> result = prompt.getExistingPassword(message);
            if (result.isPresent()) {
                setEncryptionKey(result.get());
                if (checkCanary()) {
                    return true;
                } else {
                    message = OStrings.getString("PASSWORD_TRY_AGAIN_MESSAGE");
                }
            } else {
                LOGGER.info("User declined to input master password");
                return false;
            }
        }
    }

    private boolean checkCanary() {
        if (!isMasterPasswordSet()) {
            return false;
        }
        try {
            String decrypted = textEncryptor.decrypt(Preferences.getPreference(CREDENTIALS_MANAGER_CANARY));
            return CREDENTIALS_MANAGER_CANARY.equals(decrypted);
        } catch (Exception e) {
            return false;
        }
    }

    public enum ResponseType {
        USE_INPUT, GENERATE_AND_STORE, CANCEL
    }

    public static class PasswordSetResult {
        public final ResponseType responseType;
        public final char[] password;

        public PasswordSetResult(ResponseType responseType, char[] password) {
            this.responseType = responseType;
            this.password = password;
        }
    }

    private static class GuiPasswordPrompt implements IPasswordPrompt {

        @Override
        public Optional<char[]> getExistingPassword(String message) {
            return UIThreadsUtil.returnResultFromSwingThread(() -> {
                PasswordEnterDialogController dialog = new PasswordEnterDialogController();
                dialog.show(getParentWindow(), message);
                return dialog.getResult();
            });
        }

        @Override
        public PasswordSetResult createNewPassword() {
            return UIThreadsUtil.returnResultFromSwingThread(() -> {
                PasswordSetDialogController dialog = new PasswordSetDialogController();
                dialog.show(getParentWindow());
                return dialog.getResult();
            });
        }

        private Window getParentWindow() {
            Window window = FocusManager.getCurrentManager().getActiveWindow();
            if (window == null) {
                IMainWindow mw = Core.getMainWindow();
                if (mw != null) {
                    window = mw.getApplicationFrame();
                }
            }
            return window;
        }
    }
}
