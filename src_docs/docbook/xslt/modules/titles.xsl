<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:dbe="http://docbook.org/ns/docbook/errors"
                xmlns:f="http://docbook.org/ns/docbook/functions"
                xmlns:fp="http://docbook.org/ns/docbook/functions/private"
                xmlns:l="http://docbook.org/ns/docbook/l10n"
                xmlns:lt="http://docbook.org/ns/docbook/l10n/templates"
                xmlns:m="http://docbook.org/ns/docbook/modes"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                xmlns:mp="http://docbook.org/ns/docbook/modes/private"
                xmlns:tp="http://docbook.org/ns/docbook/templates/private"
                xmlns:v="http://docbook.org/ns/docbook/variables"
                xmlns:vp="http://docbook.org/ns/docbook/variables/private"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml"
                default-mode="m:docbook"
                exclude-result-prefixes="#all"
                version="3.0">

<xsl:variable name="v:user-title-groups" as="element()*"/>

<xsl:variable name="v:title-groups" as="element()+"
              xmlns:db="http://docbook.org/ns/docbook">
  <xsl:sequence select="$v:user-title-groups"/>

  <title xpath="self::db:section|self::db:sect1
                |self::db:sect2|self::db:sect3|self::db:sect4|self::db:sect5
                |self::db:refsection|self::db:refsect1|self::db:refsect2|self::db:refsect3"
         group="{if (f:is-true($section-numbers))
                 then 'title-numbered'
                 else 'title-unnumbered'}"/>

  <title xpath="self::db:article|self::db:preface|self::db:chapter|self::db:appendix"
         group="{if (f:is-true($component-numbers))
                 then 'title-numbered'
                 else 'title-unnumbered'}"/>

  <title xpath="self::db:set" group="title-unnumbered"/>

  <title xpath="self::db:book|self::db:part|self::db:reference"
         group="{if (f:is-true($division-numbers))
                 then 'title-numbered'
                 else 'title-unnumbered'}"/>

  <title xpath="self::db:figure[parent::db:formalgroup]
                |self::db:table[parent::db:formalgroup]
                |self::db:equation[parent::db:formalgroup]
                |self::db:example[parent::db:formalgroup]"
         group="subfigure-title"/>

  <title xpath="self::db:figure|self::db:table|self::db:equation|self::db:example|self::db:procedure"
         group="title-numbered"/>

  <title xpath="self::db:formalgroup"
         group="title-numbered"/>

  <title xpath="self::db:step|self::db:listitem[parent::db:orderedlist]"
         group="title-unnumbered"/>

  <title xpath="self::db:glosssee|self::db:glossseealso"
         group="title-unnumbered"/>

  <title xpath="self::db:see|self::db:seealso"
         group="title-unnumbered"/>

  <title xpath="self::db:question|self::db:answer"
         group="title-numbered"/>

  <title xpath="self::*"
         group="title-unnumbered"/>
</xsl:variable>

<!-- ============================================================ -->

<xsl:function name="fp:title-properties" as="element()?" cache="yes">
  <xsl:param name="this" as="element()"/>

  <xsl:iterate select="$v:title-groups">
    <xsl:variable name="test" as="element()*">
      <xsl:evaluate context-item="$this" xpath="@xpath"/>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$test">
        <xsl:sequence select="fp:title-properties-override($test, .)"/>
        <xsl:break/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:next-iteration/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:iterate>
</xsl:function>

<xsl:function name="fp:title-properties-override" as="element()" cache="yes">
  <xsl:param name="context" as="element()"/>
  <xsl:param name="properties" as="element()"/>

  <xsl:variable name="numbered-pi"
                select="($context/ancestor-or-self::* ! f:pi(., 'numbered'))[last()]"/>

  <!--
  <xsl:message select="node-name($context), $numbered-pi"/>
  -->

  <xsl:element name="{node-name($properties)}" namespace="{namespace-uri($properties)}">
    <xsl:copy select="$properties/@* except $properties/@group"/>
    <xsl:attribute name="group">
      <xsl:choose>
        <xsl:when test="empty($numbered-pi)">
          <xsl:sequence select="$properties/@group"/>
        </xsl:when>
        <xsl:when test="$numbered-pi[1] = 'true'">
          <xsl:sequence select="'title-numbered'"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:sequence select="'title-unnumbered'"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </xsl:element>
</xsl:function>


<xsl:template match="*" mode="mp:compute-headline-label" as="item()*">
  <xsl:param name="purpose" as="xs:string" required="yes"/>

  <xsl:variable name="prop" select="fp:title-properties(.)"/>

  <!--
  <xsl:message select="node-name(.), $purpose, $prop/@group/string()"/>
  -->

  <xsl:variable name="template"
                select="if ($purpose = 'lot')
                        then fp:localization-template(., 'list-of-titles')
                        else fp:localization-template(., $prop/@group)"/>

  <xsl:if test="$template/lt:label">
    <xsl:apply-templates select="." mode="m:headline-label">
      <xsl:with-param name="purpose" select="$purpose"/>
    </xsl:apply-templates>
  </xsl:if>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="*" mode="m:headline">
  <xsl:param name="purpose" as="xs:string" required="yes"/>

  <xsl:variable name="prop" select="fp:title-properties(.)"/>

  <!-- There's a little bit of a hack here. Turning off numbers
       (e.g., $division-numbers, $component-numbers, or $section-numbers)
       should (usually) effect the list-of-titles as well. But it
       doesn't unless you also override the list-of-titles templates in
       the localization, and that's more work. Surely the flags should
       apply?

       So to support that, if the property group contains 'unnumbered',
       we look for list-of-titles-unnumbered, otherwise we look for
       'list-of-titles'. It's a bit of a hack, but...those boolean
       params are arguably the hack, so...
   -->
  <!--
  <xsl:message select="local-name(.), $purpose, $prop/@group/string()"/>
  <xsl:message select="$prop"/>
  -->

  <xsl:variable name="template" as="element(l:template)">
    <xsl:choose>
      <xsl:when test="$purpose = 'lot' and contains($prop/@group, 'unnumbered')">
        <xsl:sequence select="fp:localization-template(., 'list-of-titles-unnumbered')"/>
      </xsl:when>
      <xsl:when test="$purpose = 'lot'">
        <xsl:sequence select="fp:localization-template(., 'list-of-titles')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="fp:localization-template(., $prop/@group)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <!--
  <xsl:message select="local-name(.), $purpose, $template"/>
  -->

  <xsl:variable name="label" as="item()*">
    <xsl:apply-templates select="." mode="mp:compute-headline-label">
      <xsl:with-param name="purpose" select="$purpose"/>
    </xsl:apply-templates>
  </xsl:variable>

  <xsl:if test="$vp:olinkdb">
    <xsl:attribute name="db-label" select="$label"/>
  </xsl:if>

  <xsl:variable name="title" as="node()*">
    <xsl:if test="$template/lt:content">
      <xsl:apply-templates select="." mode="m:headline-title">
        <xsl:with-param name="purpose" select="$purpose"/>
      </xsl:apply-templates>
    </xsl:if>
  </xsl:variable>

  <xsl:if test="f:is-true($titleabbrev-passthrough)
                and db:info/db:titleabbrev and not($purpose = 'lot')">
    <script type="text/html" class="titleabbrev">
      <xsl:apply-templates select="$template" mode="mp:localization">
        <xsl:with-param name="context" select="."/>
        <xsl:with-param name="label" select="$label"/>
        <xsl:with-param name="content">
          <xsl:apply-templates select="." mode="m:headline-title">
            <xsl:with-param name="purpose" select="'abbrev'"/>
          </xsl:apply-templates>
        </xsl:with-param>
      </xsl:apply-templates>
    </script>
  </xsl:if>

  <xsl:apply-templates select="$template" mode="mp:localization">
    <xsl:with-param name="context" select="."/>
    <xsl:with-param name="label" select="$label"/>
    <xsl:with-param name="content" select="$title"/>
  </xsl:apply-templates>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="*" mode="m:headline-label">
  <xsl:param name="purpose" as="xs:string" required="yes"/>
  <xsl:apply-templates select="." mode="m:headline-number">
    <xsl:with-param name="purpose" select="$purpose"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="db:appendix" mode="m:headline-label">
  <xsl:param name="purpose" as="xs:string" required="yes"/>
  <xsl:choose>
    <xsl:when test="not(f:is-true($number-single-appendix))
                    and empty(preceding-sibling::db:appendix)
                    and empty(following-sibling::db:appendix)">
      <xsl:sequence select="()"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:next-match>
        <xsl:with-param name="purpose" select="$purpose"/>
      </xsl:next-match>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:qandaentry" mode="m:headline-label">
  <xsl:param name="purpose" as="xs:string" required="yes"/>
  <xsl:apply-templates select="db:question" mode="m:headline-label">
    <xsl:with-param name="purpose" select="$purpose"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="db:question" mode="m:headline-label">
  <xsl:param name="purpose" as="xs:string" required="yes"/>

  <xsl:variable name="label"
                select="ancestor::db:qandaset[@defaultlabel][1]/@defaultlabel/string()"/>
  <xsl:variable name="label"
                select="if ($label)
                        then $label
                        else $qandaset-default-label"/>

  <xsl:choose>
    <xsl:when test="db:label">
      <xsl:apply-templates select="db:label"/>
    </xsl:when>
    <xsl:when test="$label = 'none'"/>
    <xsl:when test="$label = 'number'">
      <xsl:number from="db:qandaset" level="multiple" select=".."
                  count="db:qandaentry|db:qandadiv"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:if test="$label != 'qanda'">
        <xsl:message
            select="'Unexpected qandaset label: ' || $label || ', using qanda'"/>
      </xsl:if>

      <xsl:sequence select="f:l10n-token(., 'question')"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:answer" mode="m:headline-label">
  <xsl:param name="purpose" as="xs:string" required="yes"/>

  <xsl:variable name="label"
                select="ancestor::db:qandaset[@defaultlabel][1]/@defaultlabel/string()"/>
  <xsl:variable name="label"
                select="if ($label)
                        then $label
                        else $qandaset-default-label"/>

  <xsl:choose>
    <xsl:when test="db:label">
      <xsl:apply-templates select="db:label"/>
    </xsl:when>
    <xsl:when test="$label = 'none' or $label='number'"/>
    <xsl:when test="$label = 'qanda'">
      <xsl:text>A:</xsl:text>
    </xsl:when>
    <xsl:otherwise>
      <xsl:message
          select="'Unexpected qandaset label: ' || $label || ', using qanda'"/>
      <xsl:text>A:</xsl:text>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="*" as="item()*" mode="m:headline-number">
  <xsl:param name="purpose" as="xs:string" required="yes"/>
  <xsl:message use-when="$v:debug = 'numeration'"
               select="'No headline number for', local-name(.)"/>
  <!-- just a default... -->
  <xsl:number level="single"/>
</xsl:template>

<xsl:template match="db:orderedlist/db:listitem" as="item()*"
              mode="m:headline-number">
  <xsl:param name="purpose" as="xs:string" required="yes"/>

  <xsl:variable name="prefix" as="item()*">
    <xsl:apply-templates select="parent::*/ancestor::db:listitem[parent::db:orderedlist][1]"
                         mode="m:headline-number">
      <xsl:with-param name="purpose" select="$purpose"/>
    </xsl:apply-templates>
  </xsl:variable>

  <xsl:variable name="number" as="xs:integer"
                select="f:orderedlist-item-number(.)[last()]"/>

  <xsl:variable name="format"
                select="f:orderedlist-item-numeration(.)"/>

  <xsl:variable name="formatted-number" as="xs:string?">
    <xsl:if test="exists($format)">
      <xsl:number value="$number" format="{$format}"/>
    </xsl:if>
  </xsl:variable>

  <xsl:if test="exists($formatted-number)">
    <xsl:if test="exists($prefix)">
      <xsl:sequence select="$prefix"/>
      <span class="sep">
        <xsl:apply-templates select="." mode="m:gentext">
          <xsl:with-param name="group" select="'number-separator'"/>
        </xsl:apply-templates>
      </span>
    </xsl:if>
    <xsl:sequence select="$formatted-number"/>
  </xsl:if>
</xsl:template>

<xsl:template match="db:step" as="item()*"
              mode="m:headline-number">
  <xsl:param name="purpose" as="xs:string" required="yes"/>

  <xsl:variable name="prefix" as="item()*">
    <xsl:apply-templates select="ancestor::db:step[1]"
                         mode="m:headline-number">
      <xsl:with-param name="purpose" select="$purpose"/>
    </xsl:apply-templates>
  </xsl:variable>

  <xsl:variable name="formatted-number" as="item()*">
    <xsl:number value="fp:number(.)" format="{f:step-numeration(.)}"/>
  </xsl:variable>

  <xsl:if test="exists($formatted-number)">
    <xsl:if test="exists($prefix)">
      <xsl:sequence select="$prefix"/>
      <span class="sep">
        <xsl:apply-templates select="." mode="m:gentext">
          <xsl:with-param name="group" select="'number-separator'"/>
        </xsl:apply-templates>
      </span>
    </xsl:if>
    <xsl:sequence select="$formatted-number"/>
  </xsl:if>
</xsl:template>

<xsl:template match="db:qandadiv" as="item()*"
              mode="m:headline-number">
  <xsl:param name="purpose" as="xs:string" required="yes"/>

  <xsl:variable name="prefix" as="item()*">
    <xsl:apply-templates select="parent::*"
                         mode="m:headline-number">
      <xsl:with-param name="purpose" select="$purpose"/>
    </xsl:apply-templates>
  </xsl:variable>

  <xsl:variable name="number" select="fp:number(.)"/>

  <xsl:variable name="format">
    <xsl:apply-templates select="." mode="m:gentext">
      <xsl:with-param name="group" select="'number-format'"/>
    </xsl:apply-templates>
  </xsl:variable>

  <xsl:if test="exists($number) and exists($format)">
    <xsl:if test="exists($prefix)">
      <xsl:sequence select="$prefix"/>
      <span class="sep">
        <xsl:apply-templates select="." mode="m:gentext">
          <xsl:with-param name="group" select="'number-separator'"/>
        </xsl:apply-templates>
      </span>
    </xsl:if>
    <xsl:number value="$number" format="{$format}"/>
  </xsl:if>
</xsl:template>

<xsl:template match="db:figure[not(parent::db:formal-group)]
                     |db:example[not(parent::db:formal-group)]
                     |db:table[not(parent::db:formal-group)]
                     |db:equation[not(parent::db:formal-group)]
                     |db:procedure
                     |db:qandaset
                     |db:formalgroup"
              as="item()*"
              mode="m:headline-number">
  <xsl:param name="purpose" as="xs:string" required="yes"/>
  <xsl:param name="inherit-from" select="tokenize($formal-objects-inherit-from, '\s+')"/>

  <xsl:apply-templates select="."
                       mode="mp:format-headline-number">
    <xsl:with-param name="purpose" select="$purpose"/>
    <xsl:with-param name="inherit-from" select="$inherit-from"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="db:section|db:sect1|db:sect2|db:sect3|db:sect4|db:sect5"
              as="item()*"
              mode="m:headline-number">
  <xsl:param name="purpose" as="xs:string" required="yes"/>
  <xsl:param name="inherit-from" select="tokenize($sections-inherit-from, '\s+')"/>

  <xsl:apply-templates select="."
                       mode="mp:format-headline-number">
    <xsl:with-param name="purpose" select="$purpose"/>
    <xsl:with-param name="inherit-from" select="$inherit-from"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="db:preface|db:chapter|db:appendix|db:partintro
                     |db:dedication|db:colophon|db:acknowledgements
                     |db:article
                     |db:glossary|db:bibliography
                     |db:index|db:setindex"
              mode="m:headline-number"
              as="item()*">
  <xsl:param name="purpose" as="xs:string" required="yes"/>
  <xsl:param name="inherit-from" select="tokenize($components-inherit-from, '\s+')"/>

  <!--
  <xsl:message select="local-name(.), $inherit-from,
                       '============================================================'"/>
  -->

  <xsl:apply-templates select="."
                       mode="mp:format-headline-number">
    <xsl:with-param name="purpose" select="$purpose"/>
    <xsl:with-param name="inherit-from" select="$inherit-from"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="db:part|db:reference" as="item()*"
              mode="m:headline-number">
  <xsl:param name="purpose" as="xs:string" required="yes"/>
  <xsl:param name="inherit-from" select="tokenize($divisions-inherit-from, '\s+')"/>

  <xsl:apply-templates select="."
                       mode="mp:format-headline-number">
    <xsl:with-param name="purpose" select="$purpose"/>
    <xsl:with-param name="inherit-from" select="$inherit-from"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="db:book" as="item()*"
              mode="m:headline-number">
  <xsl:param name="purpose" as="xs:string" required="yes"/>
  <xsl:param name="inherit-from" select="tokenize($books-inherit-from, '\s+')"/>
  <xsl:apply-templates select="."
                       mode="mp:format-headline-number">
    <xsl:with-param name="purpose" select="$purpose"/>
    <xsl:with-param name="inherit-from" select="$inherit-from"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="db:set" as="item()*"
              mode="m:headline-number">
  <xsl:param name="purpose" as="xs:string" required="yes"/>
  <xsl:param name="inherit-from" select="tokenize($sets-inherit-from, '\s+')"/>
  <xsl:apply-templates select="."
                       mode="mp:format-headline-number">
    <xsl:with-param name="purpose" select="$purpose"/>
    <xsl:with-param name="inherit-from" select="$inherit-from"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="db:figure[parent::db:formalgroup]
                     |db:table[parent::db:formalgroup]
                     |db:example[parent::db:formalgroup]
                     |db:equation[parent::db:formalgroup]"
              mode="mp:format-headline-number">
  <xsl:param name="purpose" as="xs:string" required="yes"/>
  <xsl:param name="inherit-from" as="xs:string*"/>

  <xsl:variable name="prefix" as="item()*">
    <xsl:apply-templates select=".." mode="mp:format-headline-number">
      <xsl:with-param name="purpose" select="$purpose"/>
      <xsl:with-param name="inherit-from" select="$inherit-from"/>
    </xsl:apply-templates>
  </xsl:variable>

  <xsl:variable name="number" as="item()*"
                select="fp:number(.)"/>

  <xsl:variable name="format">
    <xsl:apply-templates select="." mode="m:gentext">
      <xsl:with-param name="group" select="'number-format'"/>
    </xsl:apply-templates>
  </xsl:variable>

  <xsl:if test="exists($number) and exists($format)">
    <xsl:if test="exists($prefix)">
      <xsl:sequence select="$prefix"/>
      <span class="sep">
        <xsl:apply-templates select="." mode="m:gentext">
          <xsl:with-param name="group" select="'number-separator'"/>
        </xsl:apply-templates>
      </span>
    </xsl:if>
    <xsl:number value="$number" format="{$format}"/>
  </xsl:if>
</xsl:template>

<xsl:template match="*" mode="mp:format-headline-number">
  <xsl:param name="purpose" as="xs:string" required="yes"/>
  <xsl:param name="inherit-from" as="xs:string*"/>

  <xsl:variable name="from" as="element()?">
    <xsl:choose>
      <xsl:when test="$inherit-from = 'section'
                      and (ancestor::db:section|ancestor::db:sect1|ancestor::db:sect2
                           |ancestor::db:sect3|ancestor::db:sect4|ancestor::db:sect5)">
        <xsl:sequence
            select="(ancestor::db:section|ancestor::db:sect1|ancestor::db:sect2
                     |ancestor::db:sect3|ancestor::db:sect4|ancestor::db:sect5)[last()]"/>
      </xsl:when>
      <xsl:when test="$inherit-from = 'component'
                      and (ancestor::db:preface|ancestor::db:chapter
                           |ancestor::db:appendix|ancestor::db:partintro
                           |ancestor::db:dedication|ancestor::db:colophon
                           |ancestor::db:acknowledgements
                           |ancestor::db:article|ancestor::db:refentry
                           |ancestor::db:glossary|ancestor::db:bibliography
                           |ancestor::db:index|ancestor::db:setindex)">
        <xsl:sequence select="(ancestor::db:preface|ancestor::db:chapter
                               |ancestor::db:appendix|ancestor::db:partintro
                               |ancestor::db:dedication|ancestor::db:colophon
                               |ancestor::db:acknowledgements
                               |ancestor::db:article|ancestor::db:refentry
                               |ancestor::db:glossary|ancestor::db:bibliography
                               |ancestor::db:index|ancestor::db:setindex)[last()]"/>
      </xsl:when>
      <xsl:when test="$inherit-from = 'division'
                      and (ancestor::db:part|ancestor::db:reference)">
        <xsl:sequence select="(ancestor::db:part|ancestor::db:reference)[last()]"/>
      </xsl:when>
      <xsl:when test="$inherit-from = 'book' and ancestor::db:book">
        <xsl:sequence select="ancestor::db:book[1]"/>
      </xsl:when>
      <xsl:when test="$inherit-from = 'set' and ancestor::db:set">
        <xsl:sequence select="ancestor::db:set[1]"/>
      </xsl:when>
      <xsl:otherwise>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="label" as="item()*">
    <xsl:apply-templates select="$from" mode="mp:compute-headline-label">
      <xsl:with-param name="purpose" select="$purpose"/>
    </xsl:apply-templates>
  </xsl:variable>

  <xsl:variable name="prefix" as="item()*">
    <xsl:apply-templates select="$from" mode="mp:format-headline-number">
      <xsl:with-param name="purpose" select="$purpose"/>
      <xsl:with-param name="inherit-from" select="$inherit-from"/>
    </xsl:apply-templates>
  </xsl:variable>

  <xsl:variable name="number" as="item()*"
                select="fp:number(.)"/>

  <!--
  <xsl:message select="'FHN:', local-name(.), 'F:&quot;'||local-name($from)||'&quot;', 
                       'L:&quot;'||string-join($label, ' ')|| '&quot;',
                       'P:', $prefix, ' N:', $number, ':', $inherit-from"/>
  -->

  <xsl:variable name="format">
    <xsl:apply-templates select="." mode="m:gentext">
      <xsl:with-param name="group" select="'number-format'"/>
    </xsl:apply-templates>
  </xsl:variable>

  <xsl:if test="exists($number) and exists($format)">
    <xsl:if test="exists($label) and exists($prefix)">
      <xsl:sequence select="$prefix"/>
      <span class="sep">
        <xsl:apply-templates select="." mode="m:gentext">
          <xsl:with-param name="group" select="'number-separator'"/>
        </xsl:apply-templates>
      </span>
    </xsl:if>
    <xsl:number value="$number" format="{$format}"/>
  </xsl:if>
</xsl:template>

<xsl:template name="tp:format-number" as="item()*">
  <xsl:param name="prefix" as="item()*"/>

  <xsl:variable name="number" select="fp:number(.)"/>

  <xsl:variable name="format">
    <xsl:apply-templates select="." mode="m:gentext">
      <xsl:with-param name="group" select="'number-format'"/>
    </xsl:apply-templates>
  </xsl:variable>

  <xsl:if test="exists($prefix) and exists($number)">
    <xsl:sequence select="$prefix"/>
    <span class="sep">
      <xsl:apply-templates select="." mode="m:gentext">
        <xsl:with-param name="group" select="'number-separator'"/>
      </xsl:apply-templates>
    </span>
  </xsl:if>

  <xsl:if test="exists($number) and exists($format)">
    <xsl:number value="$number" format="{$format}"/>
  </xsl:if>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="*" as="item()*"
              mode="mp:headline-number-prefix">
  <xsl:param name="inherit-from" select="()"/>
  <xsl:message>No headline number prefix for <xsl:value-of select="local-name(.)"/></xsl:message>
  <xsl:sequence select="()"/>
</xsl:template>

<xsl:template match="db:section|db:sect1|db:sect2|db:sect3|db:sect4|db:sect5"
              as="item()*" mode="mp:headline-number-prefix">
  <xsl:param name="inherit-from" select="$sections-inherit-from"/>
  <xsl:call-template name="tp:format-number">
    <xsl:with-param name="prefix" as="item()*">
      <xsl:if test="$inherit-from = ('section', 'component', 'division', 'book', 'set')">
        <xsl:apply-templates select="parent::*" mode="mp:headline-number-prefix">
          <xsl:with-param name="inherit-from" select="$inherit-from"/>
        </xsl:apply-templates>
      </xsl:if>
    </xsl:with-param>
  </xsl:call-template>
</xsl:template>

<xsl:template match="db:preface|db:chapter|db:appendix|db:partintro
                     |db:dedication|db:colophon|db:acknowledgements
                     |db:article
                     |db:glossary|db:bibliography
                     |db:index|db:setindex"
              mode="mp:headline-number-prefix"
              as="item()*">
  <xsl:param name="inherit-from" select="$components-inherit-from"/>
  <xsl:if test="$inherit-from = ('component', 'division', 'book', 'set')">
    <xsl:apply-templates select="parent::*" mode="mp:headline-number-prefix">
      <xsl:with-param name="inherit-from" select="$inherit-from"/>
    </xsl:apply-templates>
    <xsl:value-of select="fp:number(.)"/>
  </xsl:if>
</xsl:template>

<xsl:template match="db:part|db:reference"
              mode="mp:headline-number-prefix"
              as="item()*">
  <xsl:param name="inherit-from" select="$divisions-inherit-from"/>
  <xsl:if test="$inherit-from = ('division', 'book', 'set')">
    <xsl:apply-templates select="parent::*" mode="mp:headline-number-prefix">
      <xsl:with-param name="inherit-from" select="$inherit-from"/>
    </xsl:apply-templates>
    <xsl:value-of select="fp:number(.)"/>
  </xsl:if>
</xsl:template>

<xsl:template match="db:book"
              mode="mp:headline-number-prefix"
              as="item()*">
  <xsl:param name="inherit-from" select="$divisions-inherit-from"/>
  <xsl:if test="$inherit-from = ('book', 'set')">
    <xsl:apply-templates select="parent::*" mode="mp:headline-number-prefix">
      <xsl:with-param name="inherit-from" select="$inherit-from"/>
    </xsl:apply-templates>
    <xsl:value-of select="fp:number(.)"/>
  </xsl:if>
</xsl:template>

<xsl:template match="db:set"
              mode="mp:headline-number-prefix"
              as="item()*">
  <xsl:param name="inherit-from" select="$divisions-inherit-from"/>
  <xsl:if test="$inherit-from = ('set')">
    <xsl:apply-templates select="parent::*" mode="mp:headline-number-prefix">
      <xsl:with-param name="inherit-from" select="$inherit-from"/>
    </xsl:apply-templates>
    <xsl:value-of select="fp:number(.)"/>
  </xsl:if>
</xsl:template>

<!-- ============================================================ -->

<!-- Is there a clever XPath I'm overlooking? -->
<xsl:function name="fp:nearest-relevant-ancestor" as="element()">
  <xsl:param name="elem" as="element()"/>
  <xsl:choose>
    <xsl:when test="$elem/self::db:chapter|$elem/self::db:appendix
                    |$elem/self::db:sect1|$elem/self::db:sect2|$elem/self::db:sect3
                    |$elem/self::db:sect4|$elem/self::db:sect5|$elem/self::db:section">
      <xsl:sequence select="$elem"/>
    </xsl:when>
    <xsl:when test="empty($elem/parent::*)">
      <xsl:sequence select="$elem"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="fp:nearest-relevant-ancestor($elem/parent::*)"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<!-- ============================================================ -->

<xsl:template match="*" mode="m:headline-title">
  <xsl:param name="purpose" as="xs:string" select="'title'"/>

  <xsl:choose>
    <xsl:when test="$purpose = 'title' or not(db:info/db:titleabbrev)">
      <xsl:apply-templates select="db:info/db:title" mode="m:title">
        <xsl:with-param name="purpose" select="$purpose"/>
      </xsl:apply-templates>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates select="db:info/db:titleabbrev" mode="m:title">
        <xsl:with-param name="purpose" select="$purpose"/>
      </xsl:apply-templates>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:refentry" mode="m:headline-title">
  <xsl:param name="purpose" as="xs:string" select="'title'"/>
  <xsl:choose>
    <xsl:when test="db:refmeta">
      <xsl:apply-templates select="db:refmeta" mode="m:headline-title">
        <xsl:with-param name="purpose" select="$purpose"/>
      </xsl:apply-templates>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates select="db:refnamediv/db:refname[1]" mode="m:headline-title">
        <xsl:with-param name="purpose" select="$purpose"/>
      </xsl:apply-templates>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:refmeta" mode="m:headline-title">
  <xsl:param name="purpose" as="xs:string" select="'title'"/>
  <xsl:apply-templates select="db:refentrytitle/node()"/>
  <xsl:apply-templates select="db:manvolnum"/>
</xsl:template>

<xsl:template match="db:refnamediv" mode="m:headline-title">
  <xsl:param name="purpose" as="xs:string" select="'title'"/>
  <xsl:apply-templates select="db:refname[1]" mode="m:headline-title">
    <xsl:with-param name="purpose" select="$purpose"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="db:refname" mode="m:headline-title">
  <xsl:param name="purpose" as="xs:string" select="'title'"/>
  <xsl:apply-templates mode="m:title">
    <xsl:with-param name="purpose" select="$purpose"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="db:question" mode="m:headline-title">
  <xsl:param name="purpose" as="xs:string" select="'title'"/>
  <xsl:apply-templates mode="m:title"
      select="(* except (db:label|db:info|db:tip|db:note|db:danger|db:important
                         |db:caution|db:sidebar|db:figure|db:example
                         |db:procedure|db:table|db:equation)
              )[1]">
    <xsl:with-param name="purpose" select="$purpose"/>
  </xsl:apply-templates>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="db:title|db:titleabbrev" mode="m:title">
  <xsl:param name="purpose" as="xs:string" required="yes"/>

  <xsl:choose>
    <xsl:when test="$purpose = 'title'">
      <xsl:apply-templates/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:variable name="title" as="item()*">
        <xsl:apply-templates/>
      </xsl:variable>
      <xsl:apply-templates select="$title" mode="mp:strip-links"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- ============================================================ -->

<xsl:template xmlns:h="http://www.w3.org/1999/xhtml"
              match="h:db-footnote|h:db-annotation" mode="mp:strip-links"/>

<xsl:template xmlns:h="http://www.w3.org/1999/xhtml"
              match="h:a" mode="mp:strip-links">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="element()" mode="mp:strip-links">
  <xsl:copy>
    <xsl:apply-templates select="@*,node()" mode="mp:strip-links"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="attribute()|text()|comment()|processing-instruction()"
              mode="mp:strip-links">
  <xsl:copy/>
</xsl:template>

</xsl:stylesheet>
