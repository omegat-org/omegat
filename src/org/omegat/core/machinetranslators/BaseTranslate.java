/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010-2015 Alex Buloichik
               2013 Didier Briel
               2022 Hiroshi Miura
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

import java.util.OptionalLong;
import java.util.regex.Matcher;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import javax.swing.JCheckBoxMenuItem;

import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;
import org.openide.awt.Mnemonics;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.gui.exttrans.IMTGlossarySupplier;
import org.omegat.gui.exttrans.IMachineTranslation;
import org.omegat.util.CredentialsManager;
import org.omegat.util.HTMLUtils;
import org.omegat.util.Language;
import org.omegat.util.PatternConsts;
import org.omegat.util.Preferences;

/**
 * Base class for machine translation.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Hiroshi Miura
 */
public abstract class BaseTranslate implements IMachineTranslation {

    protected boolean enabled;
    protected IMTGlossarySupplier glossarySupplier;

    /**
     * Machine translation implementation can use this cache for skip requests
     * twice. Cache will be cleared when project change.
     */
    private final Cache<String, String> cache;

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
        cache = getCacheLayer(getName());
        setCacheClearPolicy();
    }

    /**
     * Creat cache object.
     * <p>
     * MT connectors can override cache size and invalidate policy.
     * @param name name of cache which should be unique among MT connectors.
     * @return Cache object
     */
    protected Cache<String, String> getCacheLayer(String name) {
        return getCaffeineCache(name, 1_000, Duration.ONE_DAY);
    }

    /**
     * Register cache clear policy.
     */
    protected void setCacheClearPolicy() {
        CoreEvents.registerProjectChangeListener(eventType -> {
            if (eventType.equals(IProjectEventListener.PROJECT_CHANGE_TYPE.CLOSE)) {
                cache.clear();
            }
        });
    }

    /**
     * Common function to obtain CaffeineCache instance.
     * @param name name of cache.
     * @param sizeOfCache size of cache.
     * @param duration duration before clear.
     * @return Cache object.
     */
    protected Cache<String, String> getCaffeineCache(String name, int sizeOfCache, Duration duration) {
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager manager = provider.getCacheManager();
        Cache<String, String> cache1 = manager.getCache(name);
        if (cache1 != null) {
            return cache1;
        }
        CaffeineConfiguration<String, String> config = new CaffeineConfiguration<>();
        config.setExpiryPolicyFactory(() -> new CreatedExpiryPolicy(duration));
        config.setMaximumSize(OptionalLong.of(sizeOfCache));
        return manager.createCache(name, config);
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
     * Attempt to clean spaces added around tags by machine translators. Do it
     * by comparing spaces between the source text and the machine translated
     * text.
     * 
     * @param machineText
     *            The text returned by the machine translator
     * @param sourceText
     *            The original source segment
     * @return replaced text
     */
    protected String cleanSpacesAroundTags(String machineText, String sourceText) {

        // Spaces after
        Matcher tag = PatternConsts.OMEGAT_TAG_SPACE.matcher(machineText);
        while (tag.find()) {
            String searchTag = tag.group();
            if (!sourceText.contains(searchTag)) { // The tag didn't appear
                                                       // with a trailing space
                                                       // in the source text
                String replacement = searchTag.substring(0, searchTag.length() - 1);
                machineText = machineText.replace(searchTag, replacement);
            }
        }

        // Spaces before
        tag = PatternConsts.SPACE_OMEGAT_TAG.matcher(machineText);
        while (tag.find()) {
            String searchTag = tag.group();
            if (!sourceText.contains(searchTag)) { // The tag didn't appear
                                                       // with a leading space
                                                       // in the source text
                String replacement = searchTag.substring(1);
                machineText = machineText.replace(searchTag, replacement);
            }
        }
        return machineText;
    }

    protected String getFromCache(Language sLang, Language tLang, String text) {
        return cache.get(sLang + "/" + tLang + "/" + text);
    }

    protected String putToCache(Language sLang, Language tLang, String text, String result) {
        cache.put(sLang.toString() + "/" + tLang.toString() + "/" + text, result);
        return null; // always return null, just for method compatibility
    }

    /**
     * Clear the machine translation cache.
     */
    protected void clearCache() {
        cache.clear();
    }

    /**
     * Retrieve a credential with the given ID. First checks temporary system
     * properties, then falls back to the program's persistent preferences.
     * Store a credential with {@link #setCredential(String, String, boolean)}.
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
     * Store a credential. Credentials are stored in temporary system properties
     * and, if <code>temporary</code> is <code>false</code>, in the program's
     * persistent preferences encoded in Base64. Retrieve a credential with
     * {@link #getCredential(String)}.
     *
     * @param id
     *            ID or key of the credential to store
     * @param value
     *            value of the credential to store
     * @param temporary
     *            if <code>false</code>, encode with Base64 and store in
     *            persistent preferences as well
     */
    protected void setCredential(String id, String value, boolean temporary) {
        System.setProperty(id, value);
        CredentialsManager.getInstance().store(id, temporary ? "" : value);
    }

    /**
     * Determine whether a credential has been stored "temporarily" according to
     * the definition in {@link #setCredential(String, String, boolean)}. The
     * result will be <code>false</code> if the credential is not stored at all,
     * or if it is stored permanently.
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

    /** Convert entities to character. Ex: "&#39;" to "'". */
    protected static String unescapeHTML(String text) {
        return HTMLUtils.entitiesToChars(text);
    }
}
