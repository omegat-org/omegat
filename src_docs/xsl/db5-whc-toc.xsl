<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:f="http://docbook.org/ns/docbook/functions"
                xmlns:m="http://docbook.org/ns/docbook/modes"
                xmlns:t="http://docbook.org/ns/docbook/templates"
                xmlns:whc="http://www.xmlmind.com/whc/schema/whc"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="db f m t xs"
                version="3.0">


    <xsl:import href="../docbook/xslt/docbook.xsl"/>
    <xsl:param name="chunk" select="__index.html"/>
    <xsl:output method="xhtml"/>

  <!-- Suppress xslTNG's default HTML output; note that this template must return a document node.  -->
    <xsl:template match="/" mode="m:docbook">
        <xsl:document>
            <xsl:apply-templates select="." mode="TOC"/>
        </xsl:document>
    </xsl:template>

   <xsl:template match="/" mode="TOC">
       <whc:toc>
           <xsl:apply-templates mode="TOC"/>
       </whc:toc>
   </xsl:template>

    <xsl:template match="db:section" mode="TOC">
        <whc:entry href="{concat(f:generate-id(..), '.html#', f:generate-id(.))}">
            <whc:title><xsl:value-of select="db:info/db:title"/></whc:title>
        </whc:entry>
    </xsl:template>

   <xsl:template match="db:part|db:article|db:chapter" mode="TOC">
       <whc:entry href="{concat(f:generate-id(.), '.html')}">
           <whc:title><xsl:value-of select="db:info/db:title"/></whc:title>
           <xsl:apply-templates select="db:section" mode="TOC"/>
       </whc:entry>
   </xsl:template>
</xsl:stylesheet>
