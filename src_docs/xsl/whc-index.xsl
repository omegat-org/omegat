<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml"
                xmlns:htm="http://www.w3.org/1999/xhtml"
		        version="1.0">

    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>


    <xsl:template match="htm:div[@class='toc']/htm:ul">
        <div class="toc-chapters">
            <xsl:apply-templates select="htm:li[htm:span/@class='chapter']"/>
        </div>
        <div class="toc-appendices">
            <xsl:apply-templates select="htm:li[htm:span/@class='appendix']"/>
        </div>
    </xsl:template>

    <xsl:template match="htm:div[@class='toc']/htm:ul/htm:li">
        <div class="toc-panel toc-{htm:span/@class}">
            <h2><xsl:apply-templates select="htm:span"/></h2>
            <xsl:apply-templates select="htm:ul"/>
        </div>
    </xsl:template>

    <xsl:template match="htm:div[@class='toc']/htm:ul/htm:li/htm:span">
        <xsl:apply-templates/>
    </xsl:template>
</xsl:stylesheet>
