<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:f="http://docbook.org/ns/docbook/functions"
                xmlns:fp="http://docbook.org/ns/docbook/functions/private"
                xmlns:l="http://docbook.org/ns/docbook/l10n"
                xmlns:ls="http://docbook.org/ns/docbook/l10n/source"
                xmlns:lt="http://docbook.org/ns/docbook/l10n/templates"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                xmlns:mp="http://docbook.org/ns/docbook/modes/private"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://docbook.org/ns/docbook"
                default-mode="mp:transform-locale"
                exclude-result-prefixes="f fp ls map mp xs"
                expand-text="yes"
                version="3.0">

<xsl:output method="xml" encoding="utf-8" indent="yes"/>

<xsl:key name="gentext" match="ls:gentext" use="@key"/>

<xsl:template match="ls:locale">
  <l:l10n language="{@language}"
          english-language-name="{@english-language-name}">
    <xsl:apply-templates select="* except ls:info"/>
  </l:l10n>
</xsl:template>

<xsl:template match="ls:mappings">
  <l:gentext>
    <xsl:apply-templates select="ls:gentext">
      <xsl:sort select="lower-case(@key)" case-order="lower-first"/>
    </xsl:apply-templates>
  </l:gentext>
</xsl:template>

<xsl:template match="ls:gentext">
  <l:token key="{@key}">
    <xsl:sequence select="node()"/>
  </l:token>
</xsl:template>

<xsl:template match="ls:group">
  <l:group>
    <xsl:copy-of select="@*,namespace::*[local-name(.) != '']"/>
    <xsl:apply-templates select="ls:template"/>
  </l:group>
</xsl:template>

<xsl:template match="ls:ref">
  <l:ref>
    <xsl:copy-of select="@*,namespace::*[local-name(.) != '']"/>
  </l:ref>
</xsl:template>

<xsl:template match="ls:list">
  <l:list>
    <xsl:copy-of select="@*,namespace::*[local-name(.) != '']"/>
    <xsl:apply-templates select="ls:items"/>
  </l:list>
</xsl:template>

<xsl:template match="ls:template|ls:repeat|ls:items">
  <xsl:variable name="expanded" as="node()*">
    <xsl:apply-templates mode="mp:expand-l10n-template"/>
  </xsl:variable>

  <xsl:element name="l:{local-name(.)}" 
               namespace="http://docbook.org/ns/docbook/l10n">
    <xsl:copy-of select="@*,namespace::*[local-name(.) != '']"/>
    <xsl:iterate select="$expanded">
      <xsl:param name="result" select="()"/>
      <xsl:param name="last" select="()"/>
      <xsl:on-completion select="$result"/>
      <xsl:choose>
        <xsl:when test="./self::lt:text and $last/self::lt:text">
          <xsl:variable name="text" as="element()">
            <xsl:element name="lt:text"
                         namespace="http://docbook.org/ns/docbook/l10n/templates">
              <xsl:sequence select="string($last) || string(.)"/>
            </xsl:element>
          </xsl:variable>
          <xsl:next-iteration>
            <xsl:with-param name="result" select="($result[position() lt last()], $text)"/>
            <xsl:with-param name="last" select="$text"/>
          </xsl:next-iteration>
        </xsl:when>
        <xsl:otherwise>
          <xsl:next-iteration>
            <xsl:with-param name="result" select="($result, .)"/>
            <xsl:with-param name="last" select="."/>
          </xsl:next-iteration>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:iterate>
  </xsl:element>
</xsl:template>

<xsl:template match="ls:letters">
  <l:letters>
    <xsl:copy-of select="@*,namespace::*[local-name(.) != '']"/>
    <xsl:apply-templates select="*"/>
  </l:letters>
</xsl:template>

<xsl:template match="ls:l">
  <l:l>
    <xsl:copy-of select="@*,namespace::*[local-name(.) != '']"/>
    <xsl:sequence select="string(.)"/>
  </l:l>
</xsl:template>

<xsl:template match="*">
  <xsl:message terminate="yes">No template for {local-name(.)}</xsl:message>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="ls:ref" mode="mp:expand-l10n-template">
  <lt:ref>
    <xsl:copy-of select="@*"/>
  </lt:ref>
</xsl:template>

<xsl:template match="ls:items" mode="mp:expand-l10n-template">
  <l:items>
    <xsl:apply-templates mode="mp:expand-l10n-template"/>
  </l:items>
</xsl:template>

<xsl:template match="ls:repeat" mode="mp:expand-l10n-template">
  <l:repeat>
    <xsl:apply-templates mode="mp:expand-l10n-template"/>
  </l:repeat>
</xsl:template>

<xsl:template match="*" mode="mp:expand-l10n-template">
  <xsl:message select="."/>

  <lt:token key="{local-name(.)}"/>
  <xsl:if test="not(key('gentext', local-name(.)))">
    <xsl:message>Warning: no gentext for {local-name(.)}</xsl:message>
  </xsl:if>
</xsl:template>

<xsl:template match="text()" mode="mp:expand-l10n-template">
  <xsl:sequence select="fp:fix-text(.)"/>
</xsl:template>

<!-- ============================================================ -->

<xsl:function name="fp:fix-text" as="node()*">
  <xsl:param name="text" as="xs:string"/>

  <xsl:variable name="bpos"
                select="if (contains($text, '{'))
                        then string-length(substring-before($text, '{'))
                        else -1"/>
  <xsl:variable name="ppos"
                select="if (contains($text, '%'))
                        then string-length(substring-before($text, '%'))
                        else -1"/>
  <xsl:choose>
    <xsl:when test="$text = ''"/>
    <xsl:when test="$bpos lt 0 and $ppos lt 0">
      <xsl:element name="lt:text"
                   namespace="http://docbook.org/ns/docbook/l10n/templates">
        <xsl:value-of select="$text"/>
      </xsl:element>
    </xsl:when>
    <xsl:when test="$bpos lt 0 or ($bpos ge 0 and $ppos ge 0 and $ppos lt $bpos)">
      <xsl:if test="$ppos gt 0">
        <xsl:element name="lt:text"
                     namespace="http://docbook.org/ns/docbook/l10n/templates">
          <xsl:value-of select="substring-before($text, '%')"/>
        </xsl:element>
      </xsl:if>

      <xsl:variable name="perc" select="substring($text, $ppos+2, 1)"/>
      <xsl:variable name="rest" select="substring($text, $ppos+3)"/>

      <!--
      <xsl:message select="'p:', '[' || $perc || ']', $rest, '::', $text"/>
      -->

      <xsl:choose>
        <xsl:when test="$perc = 'c'"><lt:content/></xsl:when>
        <xsl:when test="$perc = 'l'"><lt:label/></xsl:when>
        <xsl:when test="$perc = '%'"><lt:percent/></xsl:when>
        <xsl:when test="$perc = '.'"><lt:separator/></xsl:when>
        <xsl:when test="$perc = 'p'"><lt:page/></xsl:when>
        <xsl:when test="$perc = 'o'"><lt:olink-title/></xsl:when>
        <xsl:otherwise>
          <xsl:message terminate="yes"
                       select="'Unexpected percent code: %'||$perc"/>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:sequence select="fp:fix-text($rest)"/>
    </xsl:when>
    <xsl:when test="$ppos lt 0 or ($bpos ge 0 and $ppos ge 0 and $bpos lt $ppos)">
      <xsl:if test="$bpos gt 0">
        <xsl:element name="lt:text"
                     namespace="http://docbook.org/ns/docbook/l10n/templates">
          <xsl:value-of select="substring-before($text, '{')"/>
        </xsl:element>
      </xsl:if>
      <xsl:variable name="text"
                    select="substring($text, $bpos+2)"/>

      <xsl:variable name="epos" select="string-length(substring-before($text, '}'))"/>
      <xsl:variable name="token" select="substring($text, 1, $epos)"/>
      <xsl:variable name="rest" select="substring($text, $epos+2)"/>

      <!--
      <xsl:message select="'b:', $bpos, $epos, '|' || $token || '|', $rest, '::', $text"/>
      -->

      <lt:token key="{$token}"/>

      <xsl:sequence select="fp:fix-text($rest)"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:message terminate="yes">This can’t happen.</xsl:message>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

</xsl:stylesheet>
