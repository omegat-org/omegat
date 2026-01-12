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

    <xsl:import href="https://cdn.docbook.org/release/xsltng/2.5.0/xslt/docbook.xsl"/>
    <xsl:param name="chunk" select="__index.xml"/>
    <xsl:output method="xml" indent="yes"/>

    <!-- Suppress xslTNG's default HTML output; note that this template must return a document node. -->
    <xsl:template match="/" mode="m:docbook">
        <xsl:document>
            <xsl:apply-templates select="." mode="INDEX"/>
        </xsl:document>
    </xsl:template>

    <xsl:template match="/" mode="INDEX">
        <whc:index mergeAndSort="true">
            <xsl:apply-templates select="//db:indexterm" mode="INDEX"/>
        </whc:index>
    </xsl:template>

    <xsl:template match="db:indexterm" mode="INDEX">
        <whc:entry>
            <xsl:apply-templates select="db:primary" mode="INDEX"/>
            <xsl:apply-templates select="db:secondary" mode="INDEX"/>
            <xsl:apply-templates select="db:tertiary" mode="INDEX"/>
            <whc:anchor>
                <xsl:attribute name="href">
                    <xsl:value-of select="concat(f:generate-id(../..), '.html')"/>
                </xsl:attribute>
            </whc:anchor>
        </whc:entry>
    </xsl:template>

    <xsl:template match="db:primary" mode="INDEX">
        <whc:term>
            <xsl:value-of select="."/>
        </whc:term>
    </xsl:template>

    <xsl:template match="db:secondary" mode="INDEX">
        <whc:term>
            <xsl:value-of select="."/>
        </whc:term>
    </xsl:template>

    <xsl:template match="db:tertiary" mode="INDEX">
        <whc:term>
            <xsl:value-of select="."/>
        </whc:term>
    </xsl:template>

</xsl:stylesheet>