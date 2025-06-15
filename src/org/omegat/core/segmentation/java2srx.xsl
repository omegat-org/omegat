<?xml version="1.0" encoding="UTF-8"?> 
<srx xmlns="http://www.lisa.org/srx20" version="2.0"  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xsl:version="1.0">
    <header segmentsubflows="yes" cascade="yes">
        
    </header>
    <body>
        <languagerules>
            <xsl:for-each select="//object[@class='org.omegat.core.segmentation.MapRule']">
                <languagerule>
                    <xsl:attribute name="languagerulename"><xsl:value-of select="./void[@property='language']/string/text()" /></xsl:attribute>
                    <xsl:for-each select="./void[@property='rules']/object/void[@method='add']">
                        <rule>
                            <xsl:attribute name="break">
                                <xsl:choose>
                                    <xsl:when test=".//void[@property='breakRule']/boolean/text() = 'true'">yes</xsl:when>
                                    <xsl:otherwise>no</xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>
                            
                            <beforebreak><xsl:value-of select=".//void[@property='beforebreak']/string/text()" /></beforebreak>
                            <afterbreak><xsl:value-of select=".//void[@property='afterbreak']/string/text()" /></afterbreak>
                        </rule>
                    </xsl:for-each>        
                </languagerule>
            </xsl:for-each>        
        </languagerules>
        <maprules>
            <xsl:for-each select="//object[@class='org.omegat.core.segmentation.MapRule']">
                <languagemap> 
                    <xsl:attribute name="languagepattern"><xsl:value-of select="./void[@property='pattern']/string/text()" /></xsl:attribute>
                    <xsl:attribute name="languagerulename"><xsl:value-of select="./void[@property='language']/string/text()" /></xsl:attribute>
                </languagemap>
            </xsl:for-each>
        </maprules>
    </body>
</srx>
