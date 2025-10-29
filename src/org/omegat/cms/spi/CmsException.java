package org.omegat.cms.spi;

/**
 * Exception thrown by CMS connectors for any operation failure.
 */
public class CmsException extends Exception {
    public CmsException() {
        super();
    }

    public CmsException(String message) {
        super(message);
    }

    public CmsException(String message, Throwable cause) {
        super(message, cause);
    }

    public CmsException(Throwable cause) {
        super(cause);
    }
}
