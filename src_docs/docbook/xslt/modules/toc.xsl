<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:dbe="http://docbook.org/ns/docbook/errors"
                xmlns:f="http://docbook.org/ns/docbook/functions"
                xmlns:fp="http://docbook.org/ns/docbook/functions/private"
                xmlns:h="http://www.w3.org/1999/xhtml"
                xmlns:m="http://docbook.org/ns/docbook/modes"
                xmlns:t="http://docbook.org/ns/docbook/templates"
                xmlns:tp="http://docbook.org/ns/docbook/templates/private"
                xmlns:v="http://docbook.org/ns/docbook/variables"
                xmlns:vp="http://docbook.org/ns/docbook/variables/private"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml"
                default-mode="m:docbook"
                exclude-result-prefixes="#all"
                version="3.0">

<xsl:mode name="m:toc" on-no-match="shallow-skip"/>

<xsl:template match="*" mode="m:toc">
  <xsl:param name="placed-by-author" select="false()"/>
  <xsl:if test="$placed-by-author">
    <xsl:call-template name="tp:toc">
      <xsl:with-param name="persistent" select="false()" tunnel="yes"/>
      <xsl:with-param name="root-element" select="." tunnel="yes"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template match="/db:article|db:set|db:book|db:part|db:reference"
              mode="m:toc">
  <xsl:param name="placed-by-author" select="false()"/>
  <xsl:if test="$placed-by-author
                or (f:is-true($auto-toc)
                    and not(db:toc|processing-instruction('db-toc')))">
    <xsl:call-template name="tp:toc">
      <xsl:with-param name="persistent" select="false()" tunnel="yes"/>
      <xsl:with-param name="root-element" select="." tunnel="yes"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!-- By default, the persistent ToC is the same as the regular ToC.
     Customize the m:persistent-toc templates to change this. -->
<xsl:template match="*" mode="m:persistent-toc"/>
<xsl:template match="text()|processing-instruction()|comment()" mode="m:persistent-toc"/>
<xsl:template match="/db:article|db:set|db:book|db:part|db:reference"
              mode="m:persistent-toc">
  <xsl:call-template name="tp:toc">
    <xsl:with-param name="persistent" select="true()" tunnel="yes"/>
    <xsl:with-param name="root-element" select="." tunnel="yes"/>
  </xsl:call-template>
</xsl:template>

<xsl:template name="tp:toc">
  <xsl:param name="persistent" as="xs:boolean" tunnel="yes"/>
  <xsl:param name="root-element" as="element()" tunnel="yes"/>

  <xsl:variable name="entries" as="element()*">
    <xsl:apply-templates mode="m:toc-entry">
      <xsl:with-param name="persistent" select="$persistent" tunnel="yes"/>
      <xsl:with-param name="root-element" select="$root-element" tunnel="yes"/>
    </xsl:apply-templates>
  </xsl:variable>

  <xsl:variable name="lists-of-titles" as="element()*">
    <xsl:apply-templates select="." mode="m:list-of-figures"/>
    <xsl:apply-templates select="." mode="m:list-of-tables"/>
    <xsl:apply-templates select="." mode="m:list-of-examples"/>
    <xsl:apply-templates select="." mode="m:list-of-equations"/>
    <xsl:apply-templates select="." mode="m:list-of-procedures"/>
  </xsl:variable>

  <xsl:variable name="size" select="count($entries/descendant-or-self::h:li)
                                    + count($lists-of-titles//h:li)"/>
  
  <xsl:where-populated>
    <div class="list-of-titles">
      <xsl:if test="$size gt 1 or (f:is-true($generate-trivial-toc) and $size eq 1)">
        <div class="lot toc">
          <div class="title">
            <xsl:apply-templates select="." mode="m:gentext">
              <xsl:with-param name="group" select="'table-of-contents'"/>
            </xsl:apply-templates>
          </div>
          <ul class="toc">
            <xsl:sequence select="$entries"/>
          </ul>
        </div>
        <xsl:sequence select="$lists-of-titles"/>
      </xsl:if>
    </div>
  </xsl:where-populated>
</xsl:template>  

<xsl:template match="*" mode="m:toc-entry"/>
<xsl:template match="text()|processing-instruction()|comment()" mode="m:toc-entry"/>
<xsl:template mode="m:toc-entry"
              match="db:set|db:book|db:part|db:reference

                     |db:preface|db:chapter|db:appendix|db:article
                     |db:topic|db:part|db:reference|db:dedication
                     |db:bibliography|db:index|db:glossary
                     |db:acknowledgements

                     |db:article
                     |db:section|db:sect1|db:sect2|db:sect3|db:sect4|db:sect5
                     |db:topic
                     ">
  <xsl:param name="persistent" as="xs:boolean" tunnel="yes"/>
  <xsl:param name="root-element" as="element()" tunnel="yes"/>
  <li>
    <a href="#{f:id(.)}">
      <xsl:apply-templates select="." mode="m:headline">
        <xsl:with-param name="purpose" select="'lot'"/>
      </xsl:apply-templates>
    </a>
    <xsl:where-populated>
      <ul class="toc">
        <xsl:apply-templates mode="m:toc-nested">
          <xsl:with-param name="persistent" select="$persistent" tunnel="yes"/>
          <xsl:with-param name="root-element" select="$root-element" tunnel="yes"/>
        </xsl:apply-templates>
      </ul>
    </xsl:where-populated>
  </li>
</xsl:template>

<xsl:template match="db:refentry" mode="m:toc-entry" priority="100">
  <xsl:variable name="refmeta" select=".//db:refmeta"/>
  <xsl:variable name="refentrytitle" select="$refmeta//db:refentrytitle"/>
  <xsl:variable name="refnamediv" select=".//db:refnamediv"/>
  <xsl:variable name="refname" select="$refnamediv//db:refname"/>

  <xsl:variable name="title">
    <xsl:choose>
      <xsl:when test="$refentrytitle">
        <xsl:apply-templates select="$refentrytitle[1]">
          <xsl:with-param name="purpose" select="'lot'"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="$refnamediv/db:refdescriptor">
        <xsl:apply-templates select="($refnamediv/db:refdescriptor)[1]">
          <xsl:with-param name="purpose" select="'lot'"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="$refname">
        <xsl:apply-templates select="$refname[1]">
          <xsl:with-param name="purpose" select="'lot'"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <li>
    <span class='refentrytitle'>
      <a href="#{f:id(.)}">
        <xsl:sequence select="$title"/>
      </a>
    </span>
    <xsl:if test="f:is-true($annotate-toc)">
      <xsl:apply-templates select="(db:refnamediv/db:refpurpose)[1]">
        <xsl:with-param name="purpose" select="'lot'"/>
      </xsl:apply-templates>
    </xsl:if>
  </li>
</xsl:template>

<xsl:template match="*[not(db:info/db:title)]" mode="m:toc-entry"
              priority="10">
  <!-- things without titles don't appear in the, uh, lists of titles -->
  <!-- preface, dedication, acknowledgements, colophon, equation,
       and procedure spring to mind... -->
</xsl:template>

<xsl:template match="db:colophon|db:bibliodiv|db:glossdiv|db:indexdiv"
              mode="m:toc-entry">
  <!-- by default, these don't appear in the ToC -->
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="*" mode="m:toc-nested">
  <xsl:apply-templates select="." mode="m:toc-entry"/>
</xsl:template>
<xsl:template match="text()|processing-instruction()|comment()" mode="m:toc-nested"/>

<xsl:template match="db:formalgroup
                     |db:figure|db:table|db:example|db:equation|db:procedure"
              mode="m:toc-nested">
  <!-- these don't nest -->
</xsl:template>

<xsl:template match="db:section|db:sect1|db:sect2|db:sect3|db:sect4|db:sect5
                     |db:refsection|db:refsect1|db:refsect2|db:refsect3"
              mode="m:toc-nested">
  <xsl:param name="persistent" as="xs:boolean" tunnel="yes"/>
  <xsl:param name="root-element" as="element()" tunnel="yes"/>

  <xsl:variable name="depth" as="xs:integer">
    <xsl:choose>
      <xsl:when test="self::db:section">
        <xsl:sequence select="count(ancestor::db:section)+1"/>
      </xsl:when>
      <xsl:when test="self::db:refsection">
        <xsl:sequence select="count(ancestor::db:refsection)+1"/>
      </xsl:when>
      <xsl:when test="self::db:sect5">
        <xsl:sequence select="5"/>
      </xsl:when>
      <xsl:when test="self::db:sect4">
        <xsl:sequence select="4"/>
      </xsl:when>
      <xsl:when test="self::db:sect3 or self::db:refsect3">
        <xsl:sequence select="3"/>
      </xsl:when>
      <xsl:when test="self::db:sect2 or self::db:refsect2">
        <xsl:sequence select="2"/>
      </xsl:when>
      <xsl:when test="self::db:sect1 or self::db:refsect1">
        <xsl:sequence select="1"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message select="'ERROR: no section matched in m:toc-nested: ' || node-name(.)"/>
        <xsl:sequence select="0"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:if test="$vp:section-toc-depth ge $depth">
    <xsl:apply-templates select="." mode="m:toc-entry"/>
  </xsl:if>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="*" mode="m:list-of-figures"/>
<xsl:template match="db:set|db:book" mode="m:list-of-figures">
  <xsl:if test="f:is-true($lists-of-figures)">
    <xsl:call-template name="tp:list-of-titles">
      <xsl:with-param name="elements"
                      select=".//db:figure[not(ancestor::db:formalgroup)]
                              |.//db:formalgroup[db:figure]"/>
      <xsl:with-param name="class" select="'list-of-figures'"/>
      <xsl:with-param name="group" select="'list-of-figures'"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template match="*" mode="m:list-of-tables"/>
<xsl:template match="db:set|db:book" mode="m:list-of-tables">
  <xsl:if test="f:is-true($lists-of-tables)">
    <xsl:call-template name="tp:list-of-titles">
      <xsl:with-param name="elements"
                      select=".//db:table[not(ancestor::db:formalgroup)]
                              |.//db:formalgroup[db:table]"/>
      <xsl:with-param name="class" select="'list-of-tables'"/>
      <xsl:with-param name="group" select="'list-of-tables'"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template match="*" mode="m:list-of-examples"/>
<xsl:template match="db:set|db:book" mode="m:list-of-examples">
  <xsl:if test="f:is-true($lists-of-examples)">
    <xsl:call-template name="tp:list-of-titles">
      <xsl:with-param name="elements"
                      select=".//db:example[not(ancestor::db:formalgroup)]
                              |.//db:formalgroup[db:example]"/>
      <xsl:with-param name="class" select="'list-of-examples'"/>
      <xsl:with-param name="group" select="'list-of-examples'"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template match="*" mode="m:list-of-equations"/>
<xsl:template match="db:set|db:book" mode="m:list-of-equations">
  <xsl:if test="f:is-true($lists-of-equations)">
    <xsl:call-template name="tp:list-of-titles">
      <xsl:with-param
          name="elements"
          select=".//db:equation[db:info/db:title and not(ancestor::db:formalgroup)]
                  |.//db:formalgroup[db:equation]"/>
      <xsl:with-param name="class" select="'list-of-equations'"/>
      <xsl:with-param name="group" select="'list-of-equations'"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template match="*" mode="m:list-of-procedures"/>
<xsl:template match="db:set|db:book" mode="m:list-of-procedures">
  <xsl:if test="f:is-true($lists-of-procedures)">
    <xsl:call-template name="tp:list-of-titles">
      <xsl:with-param name="elements" select=".//db:procedure[db:info/db:title]"/>
      <xsl:with-param name="class" select="'list-of-procedures'"/>
      <xsl:with-param name="group" select="'list-of-procedures'"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template name="tp:list-of-titles">
  <xsl:param name="elements" as="element()*" required="yes"/>
  <xsl:param name="class" as="xs:string" required="yes"/>
  <xsl:param name="group" as="xs:string" required="yes"/>

  <xsl:if test="$elements">
    <div class="{$class} lot">
      <div class="title">
        <xsl:apply-templates select="." mode="m:gentext">
          <xsl:with-param name="group" select="$group"/>
        </xsl:apply-templates>
      </div>
      <ul class="toc">
        <xsl:apply-templates select="$elements" mode="m:list-of-titles"/>
      </ul>
    </div>
  </xsl:if>
</xsl:template>

<xsl:template match="*" mode="m:list-of-titles">
  <li>
    <a href="#{f:id(.)}">
      <xsl:apply-templates select="." mode="m:headline">
        <xsl:with-param name="purpose" select="'lot'"/>
      </xsl:apply-templates>
    </a>
  </li>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="db:toc[not(*)] | processing-instruction('db-toc')">
  <xsl:apply-templates select=".." mode="m:toc">
    <xsl:with-param name="placed-by-author" select="true()"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="db:toc[*]">
  <div>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <div class="lot toc">
      <xsl:apply-templates select="." mode="m:generate-titlepage"/>
      <xsl:where-populated>
        <ul>
          <xsl:apply-templates select="db:tocdiv|db:tocentry"/>
        </ul>
      </xsl:where-populated>
    </div>
  </div>
</xsl:template>

<xsl:template match="db:tocdiv">
  <li>
    <xsl:call-template name="tp:tocentry-link">
      <xsl:with-param name="content" as="node()">
        <xsl:apply-templates select="db:info/db:title/node()"/>
      </xsl:with-param>
    </xsl:call-template>
    <xsl:where-populated>
      <ul>
        <xsl:apply-templates select="db:tocdiv|db:tocentry"/>
      </ul>
    </xsl:where-populated>
  </li>
</xsl:template>

<xsl:template match="db:tocentry">
  <li>
    <xsl:call-template name="tp:tocentry-link">
      <xsl:with-param name="content" as="node()">
        <xsl:apply-templates/>
      </xsl:with-param>
    </xsl:call-template>
  </li>
</xsl:template>

<xsl:template name="tp:tocentry-link">
  <xsl:param name="content" as="node()*"/>

  <xsl:variable name="target" select="if (@linkend)
                                      then key('id', @linkend)
                                      else ()"/>
  <xsl:choose>
    <xsl:when test="not(@linkend)">
      <xsl:sequence select="$content"/>
    </xsl:when>
    <xsl:when test="empty($target)">
      <xsl:message select="'Link to non-existent ID: ' || @linkend"/>
      <xsl:sequence select="$content"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="tp:link">
        <xsl:with-param name="href" select="f:href(., $target)"/>
        <xsl:with-param name="content" select="$content"/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
