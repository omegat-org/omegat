<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:f="http://docbook.org/ns/docbook/functions"
                xmlns:m="http://docbook.org/ns/docbook/modes"
                xmlns:t="http://docbook.org/ns/docbook/templates"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml"
                default-mode="m:docbook"
                exclude-result-prefixes="db f m t xs"
                version="3.0">

<xsl:template match="db:set|db:book|db:part|db:reference|db:partintro">
  <article>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates select="." mode="m:generate-titlepage"/>
    <xsl:apply-templates select="db:partintro"/>
    <xsl:apply-templates select="." mode="m:toc"/>
    <xsl:apply-templates select="node() except db:partintro"/>
    <xsl:if test="self::db:set|self::db:book">
      <xsl:apply-templates select="." mode="m:back-cover"/>
    </xsl:if>
  </article>
</xsl:template>

<xsl:template match="*" mode="m:back-cover">
  <!--
  <section class="back-cover">
    <xsl:if test="not($output-media = 'print')">
      <xsl:attribute name="db-chunk" select="'back-cover' || $html-extension"/>
      <xsl:attribute name="db-navigable" select="'true'"/>
    </xsl:if>
    <p>Contents of back cover goes here</p>
  </section>
  -->
</xsl:template>

</xsl:stylesheet>
