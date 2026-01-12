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
                xmlns:v="http://docbook.org/ns/docbook/variables"
                xmlns:vp="http://docbook.org/ns/docbook/variables/private"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml"
                default-mode="m:docbook"
                exclude-result-prefixes="#all"
                version="3.0">

<!-- ============================================================ -->

<xsl:function name="fp:number" as="xs:integer?" cache="yes">
  <xsl:param name="node" as="element()"/>
  <xsl:apply-templates select="$node" mode="mp:label-number"/>
</xsl:function>

<xsl:template match="*" mode="mp:label-number">
  <xsl:message select="'Error: no numeration scheme for ' || local-name(.)"/>
  <xsl:sequence select="0"/>
</xsl:template>

<xsl:template match="db:set" mode="mp:label-number">
  <xsl:choose>
    <xsl:when test="$sets-number-from = 'set'
                    and ancestor::db:set">
      <xsl:number from="db:set" level="single"/>
    </xsl:when>
    <xsl:when test="$sets-number-from = ('set', 'root')">
      <xsl:number level="any"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:message select="'Error: sets-number-from='||$sets-number-from"/>
      <xsl:sequence select="0"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:book" mode="mp:label-number">
  <xsl:choose>
    <xsl:when test="$books-number-from = 'set'
                    and ancestor::db:set">
      <xsl:number from="db:set" level="any"/>
    </xsl:when>
    <xsl:when test="$books-number-from = ('set', 'root')">
      <xsl:number level="any"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:message select="'Error: books-number-from='||$books-number-from"/>
      <xsl:sequence select="0"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:part|db:reference" mode="mp:label-number">
  <xsl:choose>
    <xsl:when test="$divisions-number-from = 'set'
                    and ancestor::db:set">
      <xsl:number from="db:set" level="any"/>
    </xsl:when>
    <xsl:when test="$divisions-number-from = 'set'">
      <xsl:number level="any"/>
    </xsl:when>
    <xsl:when test="$divisions-number-from = 'book'
                    and ancestor::db:book">
      <xsl:number from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$divisions-number-from = 'book'">
      <xsl:number level="any"/>
    </xsl:when>
    <xsl:when test="$divisions-number-from = 'root'">
      <xsl:number level="any"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:message select="'Error: divisions-number-from='||$divisions-number-from"/>
      <xsl:sequence select="0"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:preface|db:chapter|db:appendix|db:partintro
                     |db:dedication|db:colophon|db:acknowledgements
                     |db:article
                     |db:glossary|db:bibliography
                     |db:index|db:setindex" mode="mp:label-number">
  <xsl:choose>
    <xsl:when test="$components-number-from = 'set'
                    and ancestor::db:set">
      <xsl:number from="db:set" level="any"/>
    </xsl:when>
    <xsl:when test="$components-number-from = 'set'">
      <xsl:number level="any"/>
    </xsl:when>
    <xsl:when test="$components-number-from = 'book'
                    and ancestor::db:book">
      <xsl:number from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$components-number-from = 'book'">
      <xsl:number level="any"/>
    </xsl:when>
    <xsl:when test="$components-number-from = 'division'
                    and ancestor::db:part">
      <xsl:number from="db:part" level="any"/>
    </xsl:when>
    <xsl:when test="$components-number-from = 'division'
                    and ancestor::db:book">
      <xsl:number from="db:book" level="single"/>
    </xsl:when>
    <xsl:when test="$components-number-from = 'division'">
      <xsl:number level="any"/>
    </xsl:when>
    <xsl:when test="$components-number-from = 'root'">
      <xsl:number level="any"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:message select="'Error: components-number-from='||$components-number-from"/>
      <xsl:sequence select="0"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:refentry|db:refsection|db:refsect1|db:refsect2|db:refsect3"
              mode="mp:label-number">
  <!-- not usually numbered -->
  <xsl:sequence select="()"/>
</xsl:template>

<xsl:template match="db:section|db:sect1|db:sect2|db:sect3|db:sect4|db:sect5
                     |db:simplesect"
              mode="mp:label-number">
  <xsl:choose>
    <xsl:when test="parent::db:section|parent::db:sect1|parent::db:sect2
                    | parent::db:sect3|parent::db:sect4|parent::db:sect5">
      <xsl:number from="db:section|db:sect1|db:sect2|db:sect3|db:sect4|db:sect5"
                  level="single"/>
    </xsl:when>
    <xsl:when test="$sections-number-from = 'set'
                    and ancestor::db:set">
      <xsl:number count="db:section[not(parent::db:section)]|db:sect1"
                  from="db:set" level="any"/>
    </xsl:when>
    <xsl:when test="$sections-number-from = 'set'">
      <xsl:number count="db:section[not(parent::db:section)]|db:sect1"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$sections-number-from = 'book'
                    and ancestor::db:book">
      <xsl:number count="db:section[not(parent::db:section)]|db:sect1"
                  from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$sections-number-from = 'book'">
      <xsl:number count="db:section[not(parent::db:section)]|db:sect1"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$sections-number-from = 'division'
                    and ancestor::db:part">
      <xsl:number count="db:section[not(parent::db:section)]|db:sect1"
                  from="db:part" level="any"/>
    </xsl:when>
    <xsl:when test="$sections-number-from = 'division'">
      <xsl:number count="db:section[not(parent::db:section)
                                    and not(ancestor::db:part)]
                         |db:sect1[not(ancestor::db:part)]"
                  from="db:book" level="any"/>
    </xsl:when>
    <!-- bibliography and glossary are special because they can be either
         ancestors or preceding elements which impacts out level=any works -->
    <xsl:when test="$sections-number-from = 'component'
                    and (ancestor::db:glossary|ancestor::db:bibliography)">
      <xsl:number count="db:section[not(parent::db:section)]|db:sect1"
                  from="db:glossary|db:bibliography"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$sections-number-from = 'component'
                    and (ancestor::db:preface|ancestor::db:chapter
                         |ancestor::db:appendix|ancestor::db:partintro
                         |ancestor::db:dedication|ancestor::db:colophon
                         |ancestor::db:acknowledgements
                         |ancestor::db:article|ancestor::db:refentry
                         |ancestor::db:index|ancestor::db:setindex)">
      <xsl:number count="db:section[not(parent::db:section)
                                    and not(ancestor::db:bibliography
                                            |ancestor::db:glossary)]
                         |db:sect1[not(ancestor::db:bibliography
                                       |ancestor::db:glossary)]"
                  from="db:preface|db:chapter|db:appendix|db:partintro
                        |db:dedication|db:colophon|db:acknowledgements
                        |db:article|db:refentry
                        |db:index|db:setindex"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$sections-number-from = 'component'">
      <xsl:number count="db:section[not(parent::db:section)]|db:sect1"
                  from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$sections-number-from = 'root'">
      <xsl:number count="db:section[not(parent::db:section)]|db:sect1"
                  level="any"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:message select="'Error: sections-number-from='||$sections-number-from"/>
      <xsl:sequence select="0"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:figure[not(parent::db:formalgroup)]|db:formalgroup[db:figure]"
              mode="mp:label-number">
  <xsl:choose>
    <xsl:when test="$formal-objects-number-from = 'set'
                    and ancestor::db:set">
      <xsl:number count="db:figure[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:figure]"
                  from="db:set" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'set'">
      <xsl:number count="db:figure[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:figure]"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'book'
                    and ancestor::db:book">
      <xsl:number count="db:figure[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:figure]"
                  from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'book'">
      <xsl:number count="db:figure[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:figure]"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'division'
                    and (ancestor::db:part|ancestor::db:reference)">
      <xsl:number count="db:figure[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:figure]"
                  from="db:part|db:reference" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'division'">
      <xsl:number count="db:figure[not(ancestor::db:formalgroup)
                                   and not(ancestor::db:part|ancestor::db:reference)]
                         |db:formalgroup[db:figure
                                         and not(ancestor::db:part|ancestor::db:reference)]"
                  from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'component'
                    and (ancestor::db:preface|ancestor::db:chapter
                         |ancestor::db:appendix|ancestor::db:partintro
                         |ancestor::db:dedication|ancestor::db:colophon
                         |ancestor::db:acknowledgements
                         |ancestor::db:article|ancestor::db:refentry
                         |ancestor::db:glossary|ancestor::db:bibliography
                         |ancestor::db:index|ancestor::db:setindex)">
      <xsl:number count="db:figure[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:figure]"
                  from="db:preface|db:chapter|db:appendix|db:partintro
                        |db:dedication|db:colophon|db:acknowledgements
                        |db:article|db:refentry
                        |db:glossary|db:bibliography
                        |db:index|db:setindex"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'component'">
      <xsl:number count="db:figure[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:figure]"
                  from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'section'
                    and (ancestor::db:section|ancestor::db:sect1
                         |ancestor::db:sect2|ancestor::db:sect3
                         |ancestor::db:sect4|ancestor::db:sect5)">
      <xsl:number count="db:figure[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:figure]"
                  from="db:section|db:sect1|db:sect2|db:sect3|db:sect4|db:sect5"
                  level="single"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'section'
                    and (ancestor::db:preface|ancestor::db:chapter
                         |ancestor::db:appendix|ancestor::db:partintro
                         |ancestor::db:dedication|ancestor::db:colophon
                         |ancestor::db:acknowledgements
                         |ancestor::db:article|ancestor::db:refentry
                         |ancestor::db:glossary|ancestor::db:bibliography
                         |ancestor::db:index|ancestor::db:setindex)">
      <xsl:number count="db:figure[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:figure]"
                  from="db:preface|db:chapter|db:appendix|db:partintro
                        |db:dedication|db:colophon|db:acknowledgements
                        |db:article|db:refentry
                        |db:glossary|db:bibliography
                        |db:index|db:setindex"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'section'">
      <xsl:number count="db:figure[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:figure]"
                  from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'root'">
      <xsl:number count="db:figure[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:figure]"
                  level="any"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:message select="'Error: formal-objects-number-from='||$formal-objects-number-from"/>
      <xsl:sequence select="0"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:table[not(parent::db:formalgroup)]|db:formalgroup[db:table]"
              mode="mp:label-number">
  <xsl:choose>
    <xsl:when test="$formal-objects-number-from = 'set'
                    and ancestor::db:set">
      <xsl:number count="db:table[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:table]"
                  from="db:set" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'set'">
      <xsl:number count="db:table[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:table]"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'book'
                    and ancestor::db:book">
      <xsl:number count="db:table[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:table]"
                  from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'book'">
      <xsl:number count="db:table[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:table]"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'division'
                    and (ancestor::db:part|ancestor::db:reference)">
      <xsl:number count="db:table[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:table]"
                  from="db:part|db:reference" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'division'">
      <xsl:number count="db:table[not(ancestor::db:formalgroup)
                                   and not(ancestor::db:part|ancestor::db:reference)]
                         |db:formalgroup[db:table
                                         and not(ancestor::db:part|ancestor::db:reference)]"
                  from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'component'
                    and (ancestor::db:preface|ancestor::db:chapter
                         |ancestor::db:appendix|ancestor::db:partintro
                         |ancestor::db:dedication|ancestor::db:colophon
                         |ancestor::db:acknowledgements
                         |ancestor::db:article|ancestor::db:refentry
                         |ancestor::db:glossary|ancestor::db:bibliography
                         |ancestor::db:index|ancestor::db:setindex)">
      <xsl:number count="db:table[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:table]"
                  from="db:preface|db:chapter|db:appendix|db:partintro
                        |db:dedication|db:colophon|db:acknowledgements
                        |db:article|db:refentry
                        |db:glossary|db:bibliography
                        |db:index|db:setindex"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'component'">
      <xsl:number count="db:table[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:table]"
                  from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'section'
                    and (ancestor::db:section|ancestor::db:sect1
                         |ancestor::db:sect2|ancestor::db:sect3
                         |ancestor::db:sect4|ancestor::db:sect5)">
      <xsl:number count="db:table[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:table]"
                  from="db:section|db:sect1|db:sect2|db:sect3|db:sect4|db:sect5"
                  level="single"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'section'
                    and (ancestor::db:preface|ancestor::db:chapter
                         |ancestor::db:appendix|ancestor::db:partintro
                         |ancestor::db:dedication|ancestor::db:colophon
                         |ancestor::db:acknowledgements
                         |ancestor::db:article|ancestor::db:refentry
                         |ancestor::db:glossary|ancestor::db:bibliography
                         |ancestor::db:index|ancestor::db:setindex)">
      <xsl:number count="db:table[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:table]"
                  from="db:preface|db:chapter|db:appendix|db:partintro
                        |db:dedication|db:colophon|db:acknowledgements
                        |db:article|db:refentry
                        |db:glossary|db:bibliography
                        |db:index|db:setindex"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'section'">
      <xsl:number count="db:table[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:table]"
                  from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'root'">
      <xsl:number count="db:table[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:table]"
                  level="any"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:message select="'Error: formal-objects-number-from='||$formal-objects-number-from"/>
      <xsl:sequence select="0"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:example[not(parent::db:formalgroup)]|db:formalgroup[db:example]"
              mode="mp:label-number">
  <xsl:choose>
    <xsl:when test="$formal-objects-number-from = 'set'
                    and ancestor::db:set">
      <xsl:number count="db:example[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:example]"
                  from="db:set" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'set'">
      <xsl:number count="db:example[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:example]"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'book'
                    and ancestor::db:book">
      <xsl:number count="db:example[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:example]"
                  from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'book'">
      <xsl:number count="db:example[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:example]"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'division'
                    and (ancestor::db:part|ancestor::db:reference)">
      <xsl:number count="db:example[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:example]"
                  from="db:part|db:reference" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'division'">
      <xsl:number count="db:example[not(ancestor::db:formalgroup)
                                   and not(ancestor::db:part|ancestor::db:reference)]
                         |db:formalgroup[db:example
                                         and not(ancestor::db:part|ancestor::db:reference)]"
                  from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'component'
                    and (ancestor::db:preface|ancestor::db:chapter
                         |ancestor::db:appendix|ancestor::db:partintro
                         |ancestor::db:dedication|ancestor::db:colophon
                         |ancestor::db:acknowledgements
                         |ancestor::db:article|ancestor::db:refentry
                         |ancestor::db:glossary|ancestor::db:bibliography
                         |ancestor::db:index|ancestor::db:setindex)">
      <xsl:number count="db:example[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:example]"
                  from="db:preface|db:chapter|db:appendix|db:partintro
                        |db:dedication|db:colophon|db:acknowledgements
                        |db:article|db:refentry
                        |db:glossary|db:bibliography
                        |db:index|db:setindex"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'component'">
      <xsl:number count="db:example[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:example]"
                  from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'section'
                    and (ancestor::db:section|ancestor::db:sect1
                         |ancestor::db:sect2|ancestor::db:sect3
                         |ancestor::db:sect4|ancestor::db:sect5)">
      <xsl:number count="db:example[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:example]"
                  from="db:section|db:sect1|db:sect2|db:sect3|db:sect4|db:sect5"
                  level="single"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'section'
                    and (ancestor::db:preface|ancestor::db:chapter
                         |ancestor::db:appendix|ancestor::db:partintro
                         |ancestor::db:dedication|ancestor::db:colophon
                         |ancestor::db:acknowledgements
                         |ancestor::db:article|ancestor::db:refentry
                         |ancestor::db:glossary|ancestor::db:bibliography
                         |ancestor::db:index|ancestor::db:setindex)">
      <xsl:number count="db:example[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:example]"
                  from="db:preface|db:chapter|db:appendix|db:partintro
                        |db:dedication|db:colophon|db:acknowledgements
                        |db:article|db:refentry
                        |db:glossary|db:bibliography
                        |db:index|db:setindex"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'section'">
      <xsl:number count="db:example[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:example]"
                  from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'root'">
      <xsl:number count="db:example[not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:example]"
                  level="any"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:message select="'Error: formal-objects-number-from='||$formal-objects-number-from"/>
      <xsl:sequence select="0"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:equation[db:info/db:title and not(parent::db:formalgroup)]
                     |db:formalgroup[db:equation]"
              mode="mp:label-number">
  <xsl:choose>
    <xsl:when test="$formal-objects-number-from = 'set'
                    and ancestor::db:set">
      <xsl:number count="db:equation[db:info/db:title and not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:equation]"
                  from="db:set" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'set'">
      <xsl:number count="db:equation[db:info/db:title and not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:equation]"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'book'
                    and ancestor::db:book">
      <xsl:number count="db:equation[db:info/db:title and not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:equation]"
                  from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'book'">
      <xsl:number count="db:equation[db:info/db:title and not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:equation]"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'division'
                    and (ancestor::db:part|ancestor::db:reference)">
      <xsl:number count="db:equation[db:info/db:title and not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:equation]"
                  from="db:part|db:reference" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'division'">
      <xsl:number count="db:equation[db:info/db:title
                                      and not(ancestor::db:formalgroup)
                                      and not(ancestor::db:part|ancestor::db:reference)]
                         |db:formalgroup[db:equation
                                         and not(ancestor::db:part|ancestor::db:reference)]"
                  from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'component'
                    and (ancestor::db:preface|ancestor::db:chapter
                         |ancestor::db:appendix|ancestor::db:partintro
                         |ancestor::db:dedication|ancestor::db:colophon
                         |ancestor::db:acknowledgements
                         |ancestor::db:article|ancestor::db:refentry
                         |ancestor::db:glossary|ancestor::db:bibliography
                         |ancestor::db:index|ancestor::db:setindex)">
      <xsl:number count="db:equation[db:info/db:title and not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:equation]"
                  from="db:preface|db:chapter|db:appendix|db:partintro
                        |db:dedication|db:colophon|db:acknowledgements
                        |db:article|db:refentry
                        |db:glossary|db:bibliography
                        |db:index|db:setindex"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'component'">
      <xsl:number count="db:equation[db:info/db:title and not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:equation]"
                  from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'section'
                    and (ancestor::db:section|ancestor::db:sect1
                         |ancestor::db:sect2|ancestor::db:sect3
                         |ancestor::db:sect4|ancestor::db:sect5)">
      <xsl:number count="db:equation[db:info/db:title and not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:equation]"
                  from="db:section|db:sect1|db:sect2|db:sect3|db:sect4|db:sect5"
                  level="single"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'section'
                    and (ancestor::db:preface|ancestor::db:chapter
                         |ancestor::db:appendix|ancestor::db:partintro
                         |ancestor::db:dedication|ancestor::db:colophon
                         |ancestor::db:acknowledgements
                         |ancestor::db:article|ancestor::db:refentry
                         |ancestor::db:glossary|ancestor::db:bibliography
                         |ancestor::db:index|ancestor::db:setindex)">
      <xsl:number count="db:equation[db:info/db:title and not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:equation]"
                  from="db:preface|db:chapter|db:appendix|db:partintro
                        |db:dedication|db:colophon|db:acknowledgements
                        |db:article|db:refentry
                        |db:glossary|db:bibliography
                        |db:index|db:setindex"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'section'">
      <xsl:number count="db:equation[db:info/db:title and not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:equation]"
                  from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'root'">
      <xsl:number count="db:equation[db:info/db:title and not(ancestor::db:formalgroup)]
                         |db:formalgroup[db:equation]"
                  level="any"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:message select="'Error: formal-objects-number-from='||$formal-objects-number-from"/>
      <xsl:sequence select="0"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:figure[parent::db:formalgroup]
                     |db:table[parent::db:formalgroup]
                     |db:example[parent::db:formalgroup]
                     |db:equation[parent::db:formalgroup]"
              mode="mp:label-number">
  <xsl:number from="db:formalgroup"/>
</xsl:template>

<xsl:template match="db:procedure[db:info/db:title]"
              mode="mp:label-number">
  <xsl:choose>
    <xsl:when test="$formal-objects-number-from = 'set'
                    and ancestor::db:set">
      <xsl:number count="db:procedure[db:info/db:title]"
                  from="db:set" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'set'">
      <xsl:number count="db:procedure[db:info/db:title]"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'book'
                    and ancestor::db:book">
      <xsl:number count="db:procedure[db:info/db:title]"
                  from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'book'">
      <xsl:number count="db:procedure[db:info/db:title]"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'division'
                    and (ancestor::db:part|ancestor::db:reference)">
      <xsl:number count="db:procedure[db:info/db:title]"
                  from="db:part|db:reference" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'division'">
      <xsl:number count="db:procedure[db:info/db:title
                                      and not(ancestor::db:part|ancestor::db:reference)]"
                  from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'component'
                    and (ancestor::db:preface|ancestor::db:chapter
                         |ancestor::db:appendix|ancestor::db:partintro
                         |ancestor::db:dedication|ancestor::db:colophon
                         |ancestor::db:acknowledgements
                         |ancestor::db:article|ancestor::db:refentry
                         |ancestor::db:glossary|ancestor::db:bibliography
                         |ancestor::db:index|ancestor::db:setindex)">
      <xsl:number count="db:procedure[db:info/db:title]"
                  from="db:preface|db:chapter|db:appendix|db:partintro
                        |db:dedication|db:colophon|db:acknowledgements
                        |db:article|db:refentry
                        |db:glossary|db:bibliography
                        |db:index|db:setindex"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'component'">
      <xsl:number count="db:procedure[db:info/db:title]"
                  from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'section'
                    and (ancestor::db:section|ancestor::db:sect1
                         |ancestor::db:sect2|ancestor::db:sect3
                         |ancestor::db:sect4|ancestor::db:sect5)">
      <xsl:number count="db:procedure[db:info/db:title]"
                  from="db:section|db:sect1|db:sect2|db:sect3|db:sect4|db:sect5"
                  level="single"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'section'
                    and (ancestor::db:preface|ancestor::db:chapter
                         |ancestor::db:appendix|ancestor::db:partintro
                         |ancestor::db:dedication|ancestor::db:colophon
                         |ancestor::db:acknowledgements
                         |ancestor::db:article|ancestor::db:refentry
                         |ancestor::db:glossary|ancestor::db:bibliography
                         |ancestor::db:index|ancestor::db:setindex)">
      <xsl:number count="db:procedure[db:info/db:title]"
                  from="db:preface|db:chapter|db:appendix|db:partintro
                        |db:dedication|db:colophon|db:acknowledgements
                        |db:article|db:refentry
                        |db:glossary|db:bibliography
                        |db:index|db:setindex"
                  level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'section'">
      <xsl:number count="db:procedure[db:info/db:title]"
                  from="db:book" level="any"/>
    </xsl:when>
    <xsl:when test="$formal-objects-number-from = 'root'">
      <xsl:number count="db:procedure[db:info/db:title]"
                  level="any"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:message select="'Error: formal-objects-number-from='||$formal-objects-number-from"/>
      <xsl:sequence select="0"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- These are for backwards compatibility without adding a bunch of
     new conditions for qandasets. -->
<xsl:template match="db:figure[not(parent::db:formalgroup) and ancestor::db:qandadiv]"
              mode="mp:label-number" priority="10">
  <xsl:number count="db:figure[not(ancestor::db:formalgroup)]
                     |db:formalgroup[db:figure]"
              from="db:qandadiv" level="any"/>
</xsl:template>

<xsl:template match="db:table[not(parent::db:formalgroup) and ancestor::db:qandadiv]"
              mode="mp:label-number" priority="10">
  <xsl:number count="db:table[not(ancestor::db:formalgroup)]
                     |db:formalgroup[db:table]"
              from="db:qandadiv" level="any"/>
</xsl:template>

<xsl:template match="db:example[not(parent::db:formalgroup) and ancestor::db:qandadiv]"
              mode="mp:label-number" priority="10">
  <xsl:number count="db:example[not(ancestor::db:formalgroup)]
                     |db:formalgroup[db:example]"
              from="db:qandadiv" level="any"/>
</xsl:template>

<xsl:template match="db:equation[db:info/db:title
                                 and not(parent::db:formalgroup)
                                 and ancestor::db:qandadiv]"
              mode="mp:label-number" priority="10">
  <xsl:number count="db:equation[db:info/db:title and not(ancestor::db:formalgroup)]
                     |db:formalgroup[db:equation]"
              from="db:qandadiv" level="any"/>
</xsl:template>

<xsl:template match="db:procedure[db:info/db:title and ancestor::db:qandadiv]"
              mode="mp:label-number" priority="10">
  <xsl:number count="db:procedure[db:info/db:title]"
              from="db:qandadiv" level="any"/>
</xsl:template>

<xsl:template match="db:step" mode="mp:label-number" as="xs:integer?">
  <xsl:number level="single"/>
</xsl:template>

<xsl:template match="db:qandaset" mode="mp:label-number" as="xs:integer?">
  <xsl:sequence select="()"/>
</xsl:template>

<xsl:template match="db:qandadiv" mode="mp:label-number" as="xs:integer?">
  <xsl:number level="single"/>
</xsl:template>

</xsl:stylesheet>
