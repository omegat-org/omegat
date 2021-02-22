<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml"
                xmlns:htm="http://www.w3.org/1999/xhtml"
		        version="1.0">


    <xsl:template match="/">
        <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <meta charset="UTF-8" />

                <title>Header</title>

            </head>

            <body>
                <div class="logo">
                    <a href="https://omegat.org/">
                        <img alt="OmegaT"
                             height="50" width="100"
                             src="images/omegat.svg"
                             style="border-style: none;"  />
                    </a>
                </div>

                <div class="title">
                    <a href="index.html">
                        <xsl:value-of select="book/bookinfo/title"/>
                    </a>
                </div>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
