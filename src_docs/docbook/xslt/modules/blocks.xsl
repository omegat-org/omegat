<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:f="http://docbook.org/ns/docbook/functions"
                xmlns:h="http://www.w3.org/1999/xhtml"
                xmlns:m="http://docbook.org/ns/docbook/modes"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                xmlns:math="http://www.w3.org/1998/Math/MathML"
                xmlns:t="http://docbook.org/ns/docbook/templates"
                xmlns:v="http://docbook.org/ns/docbook/variables"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml"
                default-mode="m:docbook"
                exclude-result-prefixes="db f h m map math t v xs"
                version="3.0">

<xsl:template match="db:informalfigure|db:informalequation|db:informalexample">
  <div>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </div>
</xsl:template>

<xsl:template match="db:formalgroup">
  <xsl:variable name="placement"
                select="if (map:get($v:formal-object-title-placement, local-name(.)))
                        then map:get($v:formal-object-title-placement, local-name(.))
                        else map:get($v:formal-object-title-placement, '_default')"/>
  <div>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:choose>
      <xsl:when test="$placement = 'before'">
        <xsl:apply-templates select="." mode="m:generate-titlepage"/>
        <div class="fgbody">
          <xsl:apply-templates/>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <div class="fgbody">
          <xsl:apply-templates/>
        </div>
        <xsl:apply-templates select="." mode="m:generate-titlepage"/>
      </xsl:otherwise>
    </xsl:choose>
  </div>
</xsl:template>

<xsl:template match="db:figure|db:equation|db:example|db:screenshot">
  <xsl:variable name="tp"
                select="if (parent::db:formalgroup)
                        then $v:formalgroup-nested-object-title-placement
                        else $v:formal-object-title-placement"/>
  <xsl:variable name="placement"
                select="if (map:get($tp, local-name(.)))
                        then map:get($tp, local-name(.))
                        else map:get($tp, '_default')"/>
  <div>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:choose>
      <xsl:when test="$placement = 'before'">
        <xsl:apply-templates select="." mode="m:generate-titlepage"/>
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates/>
        <xsl:apply-templates select="." mode="m:generate-titlepage"/>
      </xsl:otherwise>
    </xsl:choose>
  </div>
</xsl:template>

<xsl:template match="math:*">
  <xsl:element name="{local-name(.)}" namespace="http://www.w3.org/1998/Math/MathML">
    <xsl:copy-of select="@*"/>
    <xsl:apply-templates select="node()"/>
  </xsl:element>
</xsl:template>

<xsl:template match="db:para">
  <xsl:choose>
    <xsl:when test="empty(*) or not(f:is-true($unwrap-paragraphs))">
      <p>
        <xsl:apply-templates select="." mode="m:attributes"/>
        <xsl:apply-templates/>
      </p>
    </xsl:when>
    <xsl:otherwise>
      <xsl:iterate select="node()">
        <xsl:param name="p" as="element(h:p)"><p/></xsl:param>
        <xsl:choose>
          <xsl:when test="self::db:address|self::db:bibliolist|self::db:blockquote
                          |self::db:bridgehead|self::db:calloutlist|self::db:caution
                          |self::db:classsynopsis|self::db:cmdsynopsis|self::db:constraintdef
                          |self::db:constructorsynopsis|self::db:danger
                          |self::db:destructorsynopsis|self::db:enumsynopsis|self::db:epigraph
                          |self::db:equation|self::db:example|self::db:fieldsynopsis
                          |self::db:figure|self::db:formalgroup|self::db:funcsynopsis
                          |self::db:glosslist|self::db:important|self::db:indexterm
                          |self::db:informalequation|self::db:informalexample
                          |self::db:informalfigure|self::db:informaltable|self::db:itemizedlist
                          |self::db:literallayout|self::db:macrosynopsis|self::db:mediaobject
                          |self::db:methodsynopsis|self::db:msgset|self::db:note
                          |self::db:orderedlist|self::db:packagesynopsis|self::db:procedure
                          |self::db:productionset|self::db:programlisting
                          |self::db:programlistingco|self::db:qandaset|self::db:remark
                          |self::db:revhistory|self::db:screen|self::db:screenco
                          |self::db:screenshot|self::db:segmentedlist|self::db:sidebar
                          |self::db:simplelist|self::db:synopsis|self::db:table|self::db:task
                          |self::db:tip|self::db:typedefsynopsis|self::db:unionsynopsis
                          |self::db:variablelist|self::db:warning">
            <xsl:if test="not(empty($p/node()))
                          and not(normalize-space(string($p)) = '')">
              <xsl:sequence select="$p"/>
            </xsl:if>
            <xsl:apply-templates select="."/>
            <xsl:next-iteration>
              <xsl:with-param name="p" as="element(h:p)"><p/></xsl:with-param>
            </xsl:next-iteration>
          </xsl:when>
          <xsl:when test="not(following-sibling::node())">
            <xsl:variable name="last-p" as="element(h:p)">
              <p>
                <xsl:sequence select="$p/node()"/>
                <xsl:apply-templates select="."/>
              </p>
            </xsl:variable>
            <xsl:if test="not(empty($last-p/node()))
                          and not(normalize-space(string($last-p)) = '')">
              <xsl:sequence select="$last-p"/>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:next-iteration>
              <xsl:with-param name="p" as="element(h:p)">
                <p>
                  <xsl:sequence select="$p/node()"/>
                  <xsl:apply-templates select="."/>
                </p>
              </xsl:with-param>
            </xsl:next-iteration>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:iterate>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:simpara">
  <p>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </p>
</xsl:template>

<xsl:template match="db:formalpara">
  <p>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates select="." mode="m:generate-titlepage"/>
    <span class="para">
      <xsl:apply-templates select="db:para" mode="m:attributes"/>
      <xsl:apply-templates select="db:para/node()"/>
    </span>
  </p>
</xsl:template>

<xsl:template match="db:blockquote|db:epigraph">
  <blockquote>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates select="." mode="m:generate-titlepage"/>
    <xsl:apply-templates select="* except db:attribution"/>
    <xsl:apply-templates select="db:attribution"/>
  </blockquote>
</xsl:template>

<xsl:template match="db:attribution">
  <div>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </div>
</xsl:template>

<xsl:template match="db:revhistory">
  <xsl:variable name="style"
                select="f:pi(., 'revhistory-style', $revhistory-style)"/>

  <div>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates select="." mode="m:generate-titlepage"/>
    <xsl:choose>
      <xsl:when test="$style = 'table'">
        <table>
          <colgroup>
            <col class="revnumber"/>
            <col class="date" style="text-align: right;"/>
            <col class="author"/>
            <col class="revremark"/>
          </colgroup>
          <tbody>
            <xsl:apply-templates select="db:revision" mode="m:revhistory-table"/>
          </tbody>
        </table>
      </xsl:when>
      <xsl:when test="$style = 'list'">
        <ul>
          <xsl:apply-templates select="db:revision" mode="m:revhistory-list"/>
        </ul>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message select="'Uknown revhistory-style: ' || $style"/>
        <ul>
          <xsl:apply-templates select="db:revision" mode="m:revhistory-list"/>
        </ul>
      </xsl:otherwise>
    </xsl:choose>
  </div>
</xsl:template>

<xsl:template match="db:revision" mode="m:revhistory-list">
  <li>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <div class="revnumber">
      <xsl:if test="db:revnumber">
        <xsl:apply-templates select="db:revnumber"/>
        <xsl:text>, </xsl:text>
      </xsl:if>
      <xsl:apply-templates select="db:date"/>
      <xsl:for-each select="db:author|db:authorinitials">
        <xsl:text>, </xsl:text>
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </div>
    <div class="description">
      <xsl:apply-templates select="db:revdescription|db:revremark"/>
    </div>
  </li>
</xsl:template>

<xsl:template match="db:revision" mode="m:revhistory-table">
  <tr>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <td>
      <xsl:apply-templates select="db:revnumber"/>
    </td>
    <td>
      <xsl:apply-templates select="db:date"/>
    </td>
    <td>
      <xsl:for-each select="db:author|db:authorinitials">
        <xsl:if test="position() gt 1">
          <xsl:text>, </xsl:text>
        </xsl:if>
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </td>
    <td>
      <xsl:apply-templates select="db:revdescription|db:revremark"/>
    </td>
  </tr>
</xsl:template>

<xsl:template match="db:revdescription">
  <div>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </div>
</xsl:template>

<xsl:template match="db:revremark">
  <p>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </p>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="db:sidebar">
  <xsl:element name="{if ($sidebar-as-aside) then 'aside' else 'div'}"
               namespace="http://www.w3.org/1999/xhtml">
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates select="." mode="m:generate-titlepage"/>
    <xsl:apply-templates/>
  </xsl:element>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="db:qandaset">
  <xsl:variable name="pis"
                select="f:pi-attributes(processing-instruction('db'))"/>
  <div>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates select="." mode="m:generate-titlepage"/>

    <xsl:if test="(exists($pis/@toc) and f:is-true($pis/@toc))
                   or (not($pis/@toc) and f:is-true($qandaset-default-toc))">
      <xsl:apply-templates select="." mode="m:toc"/>
    </xsl:if>

    <xsl:choose>
      <xsl:when test="db:qandadiv">
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:otherwise>
        <div class="qandalist">
          <xsl:apply-templates/>
        </div>
      </xsl:otherwise>
    </xsl:choose>
  </div>
</xsl:template>

<xsl:template match="db:qandadiv">
  <div>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates select="." mode="m:generate-titlepage"/>
    <xsl:choose>
      <xsl:when test="db:qandadiv">
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:otherwise>
        <div class="qandalist">
          <xsl:apply-templates/>
        </div>
      </xsl:otherwise>
    </xsl:choose>
  </div>
</xsl:template>

<xsl:template match="db:qandaentry">
  <div>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates select="." mode="m:generate-titlepage"/>
    <div class="qanda">
      <xsl:apply-templates/>
    </div>
  </div>
</xsl:template>

<xsl:template match="db:question|db:answer">
  <div>
    <xsl:apply-templates select="." mode="m:attributes"/>

    <!-- This is a bit of a hack... -->
    <xsl:variable name="label">
      <xsl:apply-templates select="." mode="m:headline-label">
        <xsl:with-param name="purpose" select="'title'"/>
      </xsl:apply-templates>
    </xsl:variable>

    <div class="label">
      <xsl:sequence select="$label"/>
      <xsl:if test="normalize-space($label) != ''">
        <xsl:apply-templates select="." mode="m:gentext">
          <xsl:with-param name="group" select="'label-separator'"/>
        </xsl:apply-templates>
      </xsl:if>
    </div>
    <div class="body">
      <xsl:apply-templates select="node() except db:label"/>
    </div>
  </div>
</xsl:template>

<xsl:template match="db:qandaset" mode="m:toc">
  <ul class="toc">
    <xsl:apply-templates select="db:qandadiv|db:qandaentry" mode="m:toc"/>
  </ul>
</xsl:template>

<xsl:template match="db:qandadiv" mode="m:toc">
  <li>
    <a href="#{f:id(.)}">
      <xsl:apply-templates select="." mode="m:headline">
        <xsl:with-param name="purpose" select="'lot'"/>
      </xsl:apply-templates>
    </a>

    <xsl:variable name="toc" select="f:pi(., 'toc')"/>

    <xsl:if test="($toc and f:is-true($toc))
                   or (empty($toc) and f:is-true($qandadiv-default-toc))">
      <ul>
        <xsl:apply-templates select="db:qandadiv|db:qandaentry" mode="m:toc"/>
      </ul>
    </xsl:if>
  </li>
</xsl:template>

<xsl:template match="db:qandaentry" mode="m:toc">
  <li>
    <a href="#{f:id(.)}">
      <xsl:apply-templates select="db:question" mode="m:headline">
        <xsl:with-param name="purpose" select="'lot'"/>
      </xsl:apply-templates>
    </a>
  </li>
</xsl:template>

<xsl:template match="db:question" mode="m:toc">
  <xsl:choose>
    <xsl:when test="(* except db:label)[1]/self::db:para">
      <xsl:apply-templates select="(* except db:label)[1]/node()"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:message select="'No question title for ' || local-name(*[1])"/>
      <xsl:variable name="question" as="node()">
        <xsl:apply-templates select="."/>
      </xsl:variable>
      <xsl:variable name="question" select="string($question)"/>
      <xsl:sequence select="$question"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:label">
  <xsl:call-template name="t:inline"/>
</xsl:template>

<xsl:template match="db:remark">
  <xsl:if test="f:is-true($show-remarks)">
    <xsl:choose>
      <xsl:when test="ancestor::db:para">
        <!-- Assume it's an inline; this is not a foolproof test! -->
        <span>
          <xsl:apply-templates select="." mode="m:attributes"/>
          <xsl:apply-templates/>
        </span>
      </xsl:when>
      <xsl:otherwise>
        <div>
          <xsl:apply-templates select="." mode="m:attributes"/>
          <xsl:apply-templates/>
        </div>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:if>
</xsl:template>

</xsl:stylesheet>
