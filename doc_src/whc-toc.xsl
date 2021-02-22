<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml"
                xmlns:whc="http://www.xmlmind.com/whc/schema/whc"
                xmlns:htm="http://www.w3.org/1999/xhtml"
		        version="1.1">

    <xsl:param name="language">en</xsl:param>
    <xsl:variable name="trfile" select="concat($language,'/messages.xml')"/>
    <xsl:variable name="translations">
        <xsl:choose>
            <xsl:when test="count(document($trfile))">
                <xsl:message>translations file : <xsl:value-of select="$trfile"/></xsl:message>
                <xsl:copy-of select="document($trfile)/entries/entry"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message>translations file : en/messages.xml</xsl:message>
                <xsl:copy-of select="document('en/messages.xml')/entries/entry"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <xsl:template match="/">
        <whc:toc>
            <xsl:apply-templates select="//htm:div[@class='toc']/htm:ul"/>
        </whc:toc>
    </xsl:template>

    <xsl:template name="toc.item">
        <whc:entry href="{htm:span/htm:a/@href}">
            <whc:title><xsl:value-of select="htm:span/htm:a"/></whc:title>
            <xsl:apply-templates select="htm:ul"/>
        </whc:entry>
    </xsl:template>

    <xsl:template match="htm:li">
        <xsl:call-template name="toc.item"/>
    </xsl:template>

    <xsl:template match="htm:li" mode="appendices">
        <xsl:call-template name="toc.item"/>
    </xsl:template>

    <xsl:template match="htm:li[htm:span[@class='appendix']]">
        <xsl:if test="not(preceding-sibling::htm:li[htm:span[@class='appendix']])">
            <whc:entry href="{htm:span/htm:a/@href}">
                <whc:title><xsl:value-of select="$translations/entry[@id='appendices']"/></whc:title>
                <whc:entry href="{htm:span/htm:a/@href}">
                    <whc:title><xsl:value-of select="htm:span/htm:a"/></whc:title>
                </whc:entry>
                <xsl:apply-templates select="following-sibling::htm:li[htm:span[@class='appendix']]" mode="appendices"/>
            </whc:entry>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
