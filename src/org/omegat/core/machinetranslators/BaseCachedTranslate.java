/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010-2015 Alex Buloichik
               2013 Didier Briel
               2022,2023 Hiroshi Miura
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

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;

import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;

import org.omegat.core.CoreEvents;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.gui.exttrans.IMachineTranslation;
import org.omegat.util.Language;

/**
 * Base class for machine translation.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Hiroshi Miura
 */
@SuppressWarnings("deprecation")
public abstract class BaseCachedTranslate extends BaseTranslate implements IMachineTranslation {

    private static final int SIZE_OF_CACHE = 1_000;

    /**
     * Machine translation implementation can use this cache for skip requests
     * twice. Cache will be cleared when project change.
     */
    protected final Cache<String, String> cache;

    public BaseCachedTranslate() {
        super();
        cache = getCacheLayer(getName());
    }

    /**
     * Creat cache object.
     * <p>
     * MT connectors can override cache size and invalidate policy.
     *
     * @param name
     *            name of cache which should be unique among MT connectors.
     * @return Cache object
     */
    protected Cache<String, String> getCacheLayer(String name) {
        Cache<String, String> caffeineCache = getCaffeineCache(name, SIZE_OF_CACHE, Duration.ONE_DAY);
        CoreEvents.registerProjectChangeListener(eventType -> {
            if (eventType.equals(IProjectEventListener.PROJECT_CHANGE_TYPE.CLOSE)) {
                caffeineCache.clear();
            }
        });
        return caffeineCache;
    }

    /**
     * Common function to obtain CaffeineCache instance.
     *
     * @param name
     *            name of cache.
     * @param sizeOfCache
     *            size of cache.
     * @param duration
     *            duration before clear.
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

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getTranslation(Language sLang, Language tLang, String text) throws Exception {
        if (enabled) {
            return translate(sLang, tLang, text);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getCachedTranslation(Language sLang, Language tLang, String text) {
        if (enabled) {
            return getCache(sLang, tLang, text);
        } else {
            return null;
        }
    }

    protected abstract String getPreferenceName();

    protected abstract String translate(Language sLang, Language tLang, String text) throws Exception;


    private String getCache(Language sLang, Language tLang, String text) {
        return cache.get(sLang + "/" + tLang + "/" + text);
    }

    private String putCache(Language sLang, Language tLang, String text, String result) {
        if (result != null) {
            cache.put(sLang.toString() + "/" + tLang.toString() + "/" + text, result);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // @Deprecated(since="6.1")
    protected String getFromCache(Language sLang, Language tLang, String text) {
        return getCache(sLang, tLang, text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // @Deprecated(since="6.1")
    protected String putToCache(Language sLang, Language tLang, String text, String result) {
        return putCache(sLang, tLang, text, result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void clearCache() {
        cache.clear();
    }
}
