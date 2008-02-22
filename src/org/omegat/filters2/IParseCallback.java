package org.omegat.filters2;

/**
 * Callback for all processing all entries for filter.
 * 
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public interface IParseCallback {
    String processEntry(String entry);
}
