<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:f="http://docbook.org/ns/docbook/functions"
                xmlns:fp="http://docbook.org/ns/docbook/functions/private"
                xmlns:m="http://docbook.org/ns/docbook/modes"
                xmlns:t="http://docbook.org/ns/docbook/templates"
                xmlns:vp="http://docbook.org/ns/docbook/variables/private"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml"
                default-mode="m:docbook"
                exclude-result-prefixes="db f fp m t vp xs"
                version="3.0">

<xsl:key name="glossterm" match="db:glossentry"
         use="(db:glossterm/@baseform, db:glossterm/normalize-space(.))[1]"/>

<xsl:template match="db:glossary|db:glossdiv|db:glosslist">
  <xsl:variable name="gi" select="if (parent::*)
                                  then 'div'
                                  else 'article'"/>

  <xsl:variable name="make-divisions"
                select="if (empty(f:pi(., 'glossary-divisions', ())))
                        then f:is-true($glossary-automatic-divisions)
                        else f:is-true(f:pi(., 'glossary-divisions', 'false'))"/>

  <xsl:element name="{$gi}" namespace="http://www.w3.org/1999/xhtml">
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates select="." mode="m:generate-titlepage"/>
    <xsl:apply-templates select="* except (db:glossentry|db:bibliography)"/>
    <xsl:choose>
      <xsl:when test="self::db:glossary and db:glossentry
                      and f:is-true($glossary-sort-entries) and $make-divisions">
        <xsl:call-template name="t:glossary-divisions">
          <xsl:with-param name="terms" as="element(db:glossentry)+">
            <xsl:for-each select="db:glossentry">
              <xsl:sort select="(@sortas, normalize-space(db:glossterm[1]))[1]"
                        collation="{$sort-collation}"/>
              <xsl:sequence select="."/>
            </xsl:for-each>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="db:glossentry and f:is-true($glossary-sort-entries)">
        <dl class="{local-name(.)}">
          <xsl:apply-templates select="db:glossentry">
            <xsl:sort select="(@sortas, normalize-space(db:glossterm[1]))[1]"
                      collation="{$sort-collation}"/>
          </xsl:apply-templates>
        </dl>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="db:glossentry"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="db:bibliography"/>
  </xsl:element>
</xsl:template>

<xsl:template name="t:glossary-divisions">
  <xsl:param name="terms" as="element(db:glossentry)+"/>

  <xsl:variable name="gid" select="f:id(.)"/>

  <xsl:for-each-group
      select="$terms"
      group-by="upper-case(substring((db:glossterm[1]/@baseform, string(db:glossterm[1]))[1], 1, 1))">
    <xsl:choose>
      <!-- Don't bother with the group in the unlikely event that there's only one. -->
      <xsl:when test="last() gt 1">
        <div id="{$gid}_{current-grouping-key()}" class="glossdiv">
          <header>
            <div class="title">
              <xsl:sequence select="current-grouping-key()"/>
            </div>
          </header>
          <dl>
            <xsl:apply-templates select="current-group()"/>
          </dl>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <dl>
          <xsl:apply-templates select="current-group()"/>
        </dl>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:for-each-group>
</xsl:template>

<xsl:template match="db:glossentry">
  <dt>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates select="db:glossterm|db:indexterm"/>
  </dt>
  <xsl:apply-templates select="db:glosssee|db:glossdef"/>
</xsl:template>

<xsl:template match="db:glossterm">
  <xsl:call-template name="t:inline"/>
</xsl:template>

<xsl:template match="db:glossentry/db:glossterm">
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </span>
  <xsl:if test="following-sibling::db:glossterm">
    <xsl:apply-templates select="." mode="m:gentext">
      <xsl:with-param name="group" select="'separator'"/>
    </xsl:apply-templates>
  </xsl:if>
</xsl:template>

<xsl:template match="db:glossentry/db:acronym">
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </span>
  <xsl:if test="following-sibling::db:acronym
                |following-sibling::db:abbrev">
    <xsl:apply-templates select="." mode="m:gentext">
      <xsl:with-param name="group" select="'separator'"/>
    </xsl:apply-templates>
  </xsl:if>
</xsl:template>

<xsl:template match="db:glossentry/db:abbrev">
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </span>
  <xsl:if test="following-sibling::db:acronym
                |following-sibling::db:abbrev">
    <xsl:apply-templates select="." mode="m:gentext">
      <xsl:with-param name="group" select="'separator'"/>
    </xsl:apply-templates>
  </xsl:if>
</xsl:template>

<xsl:template match="db:glosssee">
  <xsl:variable name="target"
                select="(key('id', @otherterm),
                         key('glossterm', normalize-space(.)))[1]"/>

  <dd>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <p>
      <xsl:apply-templates select="." mode="m:gentext">
        <xsl:with-param name="group" select="'see'"/>
        <xsl:with-param name="content">
          <xsl:choose>
            <xsl:when test="$target">
              <a href="{f:href(., $target)}">
                <xsl:apply-templates/>
              </a>
            </xsl:when>
            <xsl:when test="@otherterm and not($target)">
              <xsl:message>
                <xsl:text>Warning: </xsl:text>
                <xsl:text>glosssee @otherterm reference not found: </xsl:text>
                <xsl:value-of select="@otherterm"/>
              </xsl:message>
              <xsl:apply-templates/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:with-param>
      </xsl:apply-templates>
      <xsl:text>.</xsl:text>
    </p>
  </dd>
</xsl:template>

<xsl:template match="db:glossentry/db:glossdef">
  <dd>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates select="*[not(self::db:glossseealso)]"/>
  </dd>
  <xsl:apply-templates select="db:glossseealso"/>
</xsl:template>

<xsl:template match="db:glossseealso[preceding-sibling::db:glossseealso]"/>
<xsl:template match="db:glossseealso">
  <dd>
    <p>
      <xsl:variable name="targets" as="element()+">
        <xsl:for-each select="(., following-sibling::db:glossseealso)">
          <xsl:variable name="target"
                        select="if (key('id', @otherterm))
                                then key('id', @otherterm)[1]
                                else key('glossterm', string(.))"/>
          <xsl:choose>
            <xsl:when test="$target">
              <a href="{f:href(/,$target)}">
                <xsl:apply-templates select="$target" mode="m:crossref"/>
              </a>
            </xsl:when>
            <xsl:when test="@otherterm and not($target)">
              <xsl:message>
                <xsl:text>Warning: </xsl:text>
                <xsl:text>glossseealso @otherterm reference not found: </xsl:text>
                <xsl:value-of select="@otherterm"/>
              </xsl:message>
              <span>
                <xsl:apply-templates/>
              </span>
            </xsl:when>
            <xsl:otherwise>
              <span>
                <xsl:apply-templates/>
              </span>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:variable>

      <xsl:apply-templates select="." mode="m:gentext-list">
        <xsl:with-param name="list" select="$targets"/>
        <xsl:with-param name="name" select="'glossary-seealso'"/>
      </xsl:apply-templates>
    </p>
  </dd>
</xsl:template>

<!-- ============================================================ -->

</xsl:stylesheet>
