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

<xsl:template match="db:preface|db:chapter|db:appendix|db:article
                     |db:topic|db:acknowledgements|db:dedication|db:colophon">
  <xsl:variable name="gi" select="if (parent::*)
                                  then 'section'
                                  else 'article'"/>
  <xsl:element name="{$gi}" namespace="http://www.w3.org/1999/xhtml">
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates select="." mode="m:generate-titlepage"/>
    <xsl:apply-templates select="." mode="m:toc"/>

    <xsl:variable name="section" select="db:section[1] | db:sect1[1]"/>
    <xsl:choose>
      <xsl:when test="empty($section)">
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="nodes" select="$section/preceding-sibling::node()"/>
        <xsl:apply-templates select="$nodes/self::db:toc"/>
        <xsl:where-populated>
          <div class="db-bfs{f:conditional-orientation-class(.) ! concat(' ', .)}">
            <xsl:apply-templates select="$nodes except $nodes/self::db:toc"/>
          </div>
        </xsl:where-populated>
        <xsl:apply-templates select="$section, $section/following-sibling::node()"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:element>
</xsl:template>

</xsl:stylesheet>

