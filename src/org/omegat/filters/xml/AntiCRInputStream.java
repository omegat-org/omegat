package org.omegat.filters.xml;

import java.io.InputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: maxym
 * Date: 31.10.2004
 * Time: 2:27:18
 * To change this template use File | Settings | File Templates.
 */
class AntiCRInputStream extends InputStream
{
    private InputStream is;

    public AntiCRInputStream(InputStream is)
    {
        this.is = is;
    }

    public int available() throws IOException
    {
        return is.available();
    }
    public long skip(long n) throws IOException
    {
        return is.skip(n);
    }
    public void close() throws IOException
    {
        is.close();
    }

    public int read() throws IOException
    {
        int res = is.read();
        while( res=='\r' )
            res = is.read();
        return res;
    }

}
