<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:f="http://docbook.org/ns/docbook/functions"
                xmlns:fp="http://docbook.org/ns/docbook/functions/private"
                xmlns:h="http://www.w3.org/1999/xhtml"
                xmlns:l="http://docbook.org/ns/docbook/l10n"
                xmlns:lt="http://docbook.org/ns/docbook/l10n/templates"
                xmlns:m="http://docbook.org/ns/docbook/modes"
                xmlns:t="http://docbook.org/ns/docbook/templates"
                xmlns:tp="http://docbook.org/ns/docbook/templates/private"
                xmlns:v="http://docbook.org/ns/docbook/variables"
                xmlns:vp="http://docbook.org/ns/docbook/variables/private"
                xmlns:xlink='http://www.w3.org/1999/xlink'
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml"
                default-mode="m:docbook"
                exclude-result-prefixes="db f fp h l lt m t tp v vp xlink xs"
                version="3.0">

<xsl:template match="db:anchor">
  <xsl:call-template name="t:inline"/>
</xsl:template>

<xsl:template match="db:biblioref">
  <xsl:call-template name="tp:xref"/>
</xsl:template>

<xsl:template match="db:link">
  <xsl:choose>
    <xsl:when test="@linkend and empty(node())">
      <xsl:call-template name="tp:xref"/>
    </xsl:when>
    <xsl:when test="@linkend">
      <xsl:variable name="linkend"
                    select="(@linkend,
                            if (starts-with(@xlink:href, '#'))
                            then substring-after(@xlink:href, '#')
                            else ())[1]"/>
      <xsl:variable name="target"
                    select="if ($linkend)
                            then key('id', $linkend)[1]
                            else ()"/>
      <xsl:choose>
        <xsl:when test="empty($target)">
          <xsl:message select="'Link to non-existent ID: ' || $linkend"/>
          <xsl:sequence select="'[???' || $linkend || '???]'"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="tp:link">
            <xsl:with-param name="href" select="f:href(., $target)"/>
            <xsl:with-param name="content" as="node()*">
              <xsl:apply-templates select="node()"/>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="tp:link">
        <xsl:with-param name="content" as="item()*">
          <xsl:choose>
            <xsl:when test="empty(node())">
              <xsl:sequence select="@xlink:href/string()"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="node()"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*" mode="m:link">
  <xsl:param name="primary-markup" select="false()"/>
  <xsl:param name="content" as="item()*"/>

  <xsl:choose>
    <xsl:when test="@linkend">
      <xsl:call-template name="tp:link">
        <xsl:with-param name="primary-markup" select="$primary-markup"/>
        <xsl:with-param name="href" select="'#' || @linkend"/>
        <xsl:with-param name="content" select="$content"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="@xlink:href">
      <xsl:call-template name="tp:link">
        <xsl:with-param name="primary-markup" select="$primary-markup"/>
        <xsl:with-param name="href" select="@xlink:href/string()"/>
        <xsl:with-param name="content" select="$content"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="t:xlink">
        <xsl:with-param name="content" select="$content"/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:firstterm|db:glossterm" mode="m:link">
  <xsl:variable name="target"
                select="key('glossterm', (@baseform,normalize-space(.))[1])"/>

  <xsl:choose>
    <xsl:when test="empty($target)">
      <xsl:message select="'Gloss term has no entry:',
                           (@baseform/string(), normalize-space(.))[1]"/>
      <xsl:apply-templates/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:if test="count($target) gt 1">
        <xsl:message select="'Gloss term has multiple entries:',
                             (@baseform/string(), normalize-space(.))[1]"/>
      </xsl:if>
      <xsl:call-template name="tp:link">
        <xsl:with-param name="primary-markup" select="false()"/>
        <xsl:with-param name="href" select="f:href(., $target[1])"/>
        <xsl:with-param name="content" as="item()*">
          <xsl:apply-templates/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="tp:link">
  <xsl:param name="title" select="db:alt[1]/string()" as="xs:string?"/>
  <xsl:param name="href" select="iri-to-uri(@xlink:href)" as="xs:string"/>
  <xsl:param name="content" as="item()*" required="yes"/>
  <xsl:param name="primary-markup" as="xs:boolean" select="true()"/>

  <xsl:choose>
    <xsl:when test="$href != ''">
      <a href="{$href}">
        <xsl:if test="$primary-markup">
          <xsl:apply-templates select="." mode="m:attributes"/>
        </xsl:if>
        <xsl:if test="fp:pmuj-enabled(/)">
          <xsl:attribute name="id" select="f:generate-id(.)"/>
        </xsl:if>
        <xsl:if test="exists($title)">
          <xsl:attribute name="title" select="$title"/>
        </xsl:if>
        <xsl:sequence select="$content"/>
      </a>
    </xsl:when>
    <xsl:when test="@linkend">
      <xsl:variable name="target" select="f:target(@linkend, .)"/>
      <xsl:choose>
        <xsl:when test="empty($target)">
          <xsl:message select="'Link to undefined ID:', string(@linkend)"/>
          <span class="markup-error">
            <xsl:if test="$primary-markup">
              <xsl:apply-templates select="." mode="m:attributes"/>
            </xsl:if>
            <xsl:if test="exists($title)">
              <xsl:attribute name="title" select="$title"/>
            </xsl:if>
            <xsl:sequence select="'@@LINKEND: ' || @linkend || '@@'"/>
          </span>
        </xsl:when>
        <xsl:otherwise>
          <a href="{f:href(., $target)}">
            <xsl:if test="$primary-markup">
              <xsl:apply-templates select="." mode="m:attributes"/>
            </xsl:if>
            <xsl:if test="exists($title)">
              <xsl:attribute name="title" select="$title"/>
            </xsl:if>
            <xsl:sequence select="$content"/>
          </a>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:otherwise>
      <xsl:message select="'Link element with no target?', ."/>
      <span class="markup-error">
        <xsl:if test="$primary-markup">
          <xsl:apply-templates select="." mode="m:attributes"/>
        </xsl:if>
        <xsl:if test="exists($title)">
          <xsl:attribute name="title" select="$title"/>
        </xsl:if>
        <xsl:sequence select="$content"/>
      </span>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:xref" name="tp:xref">
  <xsl:param name="linkend"
             select="(@linkend,
                     if (starts-with(@xlink:href, '#'))
                     then substring-after(@xlink:href, '#')
                     else ())[1]"/>
  <xsl:param name="target"
             select="if ($linkend)
                     then key('id', $linkend)[1]
                     else ()"/>

  <xsl:choose>
    <xsl:when test="empty($target)">
      <xsl:message select="'Link to non-existent ID: ' || $linkend"/>
      <span class='error'>???</span>
    </xsl:when>
    <xsl:otherwise>
      <xsl:variable name="content" as="item()*">
        <xsl:choose>
          <xsl:when test="@endterm">
            <xsl:variable name="label" select="key('id', @endterm)[1]"/>
            <xsl:choose>
              <xsl:when test="empty($label)">
                <xsl:message select="'Endterm to non-existent ID: '
                                     || @endterm/string()"/>
                <xsl:apply-templates select="$target" mode="m:crossref"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:apply-templates select="$label/node()"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:when test="$target/@xreflabel">
            <xsl:sequence select="$target/@xreflabel/string()"/>
          </xsl:when>
          <xsl:when test="@xrefstyle">
            <xsl:call-template name="tp:apply-localization-template">
              <xsl:with-param name="target" select="$target"/>
              <xsl:with-param name="localization-template"
                              select="fp:localization-template-from-xrefstyle(., $target)"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="$target" mode="m:crossref"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="vp:pmuj" as="xs:boolean" select="fp:pmuj-enabled(/)"/>

      <!-- page number needs its own html:a element -->
      <xsl:for-each-group select="$content"
                          group-adjacent=". instance of node() and local-name() = ('pagenum')">
        <xsl:choose>
          <xsl:when test="boolean(current-grouping-key())">
            <a href="#{f:id($target)}" class="xref xref-{local-name($target)} xref-{local-name(.)}"/>
          </xsl:when>
          <xsl:otherwise>
            <a href="#{f:id($target)}" class="xref xref-{local-name($target)}">
              <xsl:if test="$vp:pmuj">
                <xsl:attribute name="id" select="f:generate-id(.)"/>
              </xsl:if>
              <xsl:sequence select="current-group()"/>
            </a>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each-group>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>
  
<xsl:function name="fp:localization-template-from-xrefstyle" as="element(l:template)">
  <xsl:param name="xref" as="element()"/>
  <xsl:param name="target" as="element()"/>

  <xsl:variable name="content" as="item()*">
    <xsl:choose>
      <xsl:when test="starts-with($xref/@xrefstyle, 'template:')">
        <!-- Legacy XSLT 1.0 Stylesheets,
             see http://www.sagehill.net/docbookxsl/CustomXrefs.html#UsingTemplate -->
        <xsl:analyze-string select="substring-after($xref/@xrefstyle, 'template:')" regex="%n|%t">
          <xsl:matching-substring>
            <xsl:choose>
              <xsl:when test=". eq '%n'"><lt:label/></xsl:when>
              <xsl:when test=". eq '%t'"><lt:content/></xsl:when>
            </xsl:choose>
          </xsl:matching-substring>
          <xsl:non-matching-substring>
            <lt:text><xsl:sequence select="."/></lt:text>
          </xsl:non-matching-substring>
        </xsl:analyze-string>
      </xsl:when>
      <xsl:when test="starts-with($xref/@xrefstyle, 'select:')">
        <!-- Legacy XSLT 1.0 Stylesheets,
             see http://www.sagehill.net/docbookxsl/CustomXrefs.html#UsingSelect -->
        <xsl:message select="'Warning: xref/@xrefstyle with select: Syntax is not supported'"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- See xslTNG Reference Table 4.1. Template %-letter substitutions -->
        <xsl:analyze-string select="$xref/@xrefstyle" regex="%label|%l|%c|%p">
          <xsl:matching-substring>
            <xsl:choose>
              <xsl:when test=". eq '%label'">
                <xsl:sequence select="fp:localization-template($target,'xref-number')/*"/>
              </xsl:when>
              <xsl:when test=". eq '%l'"><lt:label/></xsl:when>
              <xsl:when test=". eq '%c'"><lt:content/></xsl:when>
              <xsl:when test=". eq '%p'"><lt:pagenum/></xsl:when>
            </xsl:choose>
          </xsl:matching-substring>
          <xsl:non-matching-substring>
            <lt:text><xsl:sequence select="."></xsl:sequence></lt:text>
          </xsl:non-matching-substring>
        </xsl:analyze-string>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <l:template>
    <xsl:sequence select="$content"/>
  </l:template>
</xsl:function>

<xsl:template match="db:olink">
  <xsl:variable name="targetdoc" select="@targetdoc/string()"/>
  <xsl:variable name="targetptr" select="@targetptr/string()"/>
  <xsl:variable name="targetdb" select="($v:olink-databases[@targetdoc = $targetdoc])[1]"/>
  <xsl:variable name="obj" select="key('targetptr', $targetptr, root($targetdb))"/>

  <xsl:choose>
    <xsl:when test="empty($targetdb)">
      <xsl:message select="'olink: no targetdoc:', $targetdoc"/>
      <span class="error">
        <xsl:sequence select="'olink: ' || $targetdoc || '/' || $targetptr"/>
      </span>
    </xsl:when>
    <xsl:when test="empty($obj)">
      <xsl:message select="'olink: no targetptr: ' || $targetdoc || '/' || $targetptr"/>
      <span class="error">
        <xsl:sequence select="'olink: ' || $targetdoc || '/' || $targetptr"/>
      </span>
    </xsl:when>
    <xsl:when test="empty(node())">
      <a href="{$obj/@href}">
        <xsl:sequence select="$obj/h:xreftext/node()"/>
      </a>
    </xsl:when>
    <xsl:otherwise>
      <a href="{$obj/@href}">
        <xsl:apply-templates/>
      </a>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
