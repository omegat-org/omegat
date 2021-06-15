/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010-2015 Alex Buloichik
               2013 Didier Briel
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

package org.omegat.core.machinetranslators;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import javax.swing.JCheckBoxMenuItem;

import org.omegat.core.Core;
import org.omegat.gui.exttrans.IMTGlossarySupplier;
import org.omegat.gui.exttrans.IMachineTranslation;
import org.omegat.util.CredentialsManager;
import org.omegat.util.Language;
import org.omegat.util.PatternConsts;
import org.omegat.util.Preferences;
import org.openide.awt.Mnemonics;

/**
 * Base class for machine translation.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 */
public abstract class BaseTranslate implements IMachineTranslation {

    protected boolean enabled;
    protected IMTGlossarySupplier glossarySupplier;

    /**
     * Machine translation implementation can use this cache for skip requests twice. Cache will not be
     * cleared during OmegaT work, but it's okay - nobody will work weeks without exit.
     */
    private final Map<String, String> cache = Collections.synchronizedMap(new HashMap<String, String>());

    public BaseTranslate() {
        // Options menu item
        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem();
        Mnemonics.setLocalizedText(menuItem, getName());
        menuItem.addActionListener(e -> setEnabled(menuItem.isSelected()));
        enabled = Preferences.isPreference(getPreferenceName());
        menuItem.setState(enabled);
        Core.getMainWindow().getMainMenu().getMachineTranslationMenu().add(menuItem);
        // Preferences listener
        Preferences.addPropertyChangeListener(getPreferenceName(), e -> {
            boolean newValue = (Boolean) e.getNewValue();
            menuItem.setSelected(newValue);
            enabled = newValue;
        });
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        Preferences.setPreference(getPreferenceName(), enabled);
    }

    @Override
    public void setGlossarySupplier(IMTGlossarySupplier glossarySupplier) {
        this.glossarySupplier = glossarySupplier;
    }

    @Override
    public String getTranslation(Language sLang, Language tLang, String text) throws Exception {
        if (enabled) {
            return translate(sLang, tLang, text);
        } else {
            return null;
        }
    }

    @Override
    public String getCachedTranslation(Language sLang, Language tLang, String text) {
        if (enabled) {
            return getFromCache(sLang, tLang, text);
        } else {
            return null;
        }
    }

    protected abstract String getPreferenceName();

    protected abstract String translate(Language sLang, Language tLang, String text) throws Exception;

    /**
     * Attempt to clean spaces added around tags by machine translators. Do it by comparing spaces between the source
     * text and the machine translated text.
     * @param machineText The text returned by the machine translator
     * @param sourceText The original source segment
     * @return
     */
    protected String cleanSpacesAroundTags(String machineText, String sourceText) {

        // Spaces after
        Matcher tag = PatternConsts.OMEGAT_TAG_SPACE.matcher(machineText);
        while (tag.find()) {
            String searchTag = tag.group();
            if (sourceText.indexOf(searchTag) == -1) { // The tag didn't appear with a trailing space in the source text
                String replacement = searchTag.substring(0, searchTag.length() - 1);
                machineText = machineText.replace(searchTag, replacement);
            }
        }

        // Spaces before
        tag = PatternConsts.SPACE_OMEGAT_TAG.matcher(machineText);
        while (tag.find()) {
            String searchTag = tag.group();
            if (sourceText.indexOf(searchTag) == -1) { // The tag didn't appear with a leading space in the source text
                String replacement = searchTag.substring(1, searchTag.length());
                machineText = machineText.replace(searchTag, replacement);
            }
        }
        return machineText;
    }

    protected String getFromCache(Language sLang, Language tLang, String text) {
        return cache.get(sLang + "/" + tLang + "/" + text);
    }

    protected String putToCache(Language sLang, Language tLang, String text, String result) {
        return cache.put(sLang + "/" + tLang + "/" + text, result);
    }

    /**
     * Clear the machine translation cache.
     */
    protected void clearCache() {
        cache.clear();
    }

    /**
     * Retrieve a credential with the given ID. First checks temporary system properties, then falls back to
     * the program's persistent preferences. Store a credential with
     * {@link #setCredential(String, String, boolean)}.
     *
     * @param id
     *            ID or key of the credential to retrieve
     * @return the credential value in plain text
     */
    protected String getCredential(String id) {
        String property = System.getProperty(id);
        if (property != null) {
            return property;
        }
        return CredentialsManager.getInstance().retrieve(id).orElse("");
    }

    /**
     * Store a credential. Credentials are stored in temporary system properties and, if
     * <code>temporary</code> is <code>false</code>, in the program's persistent preferences encoded in
     * Base64. Retrieve a credential with {@link #getCredential(String)}.
     *
     * @param id
     *            ID or key of the credential to store
     * @param value
     *            value of the credential to store
     * @param temporary
     *            if <code>false</code>, encode with Base64 and store in persistent preferences as well
     */
    protected void setCredential(String id, String value, boolean temporary) {
        System.setProperty(id, value);
        CredentialsManager.getInstance().store(id, temporary ? "" : value);
    }

    /**
     * Determine whether a credential has been stored "temporarily" according to the definition in
     * {@link #setCredential(String, String, boolean)}. The result will be <code>false</code> if the
     * credential is not stored at all, or if it is stored permanently.
     *
     * @param id
     *            ID or key of credential
     * @return <code>true</code> only if the credential is stored temporarily
     * @see #setCredential(String, String, boolean)
     * @see #getCredential(String)
     */
    protected boolean isCredentialStoredTemporarily(String id) {
        return !CredentialsManager.getInstance().isStored(id) && !System.getProperty(id, "").isEmpty();
    }
}
