<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:f="http://docbook.org/ns/docbook/functions"
                xmlns:fp="http://docbook.org/ns/docbook/functions/private"
                xmlns:l="http://docbook.org/ns/docbook/l10n"
                xmlns:ls="http://docbook.org/ns/docbook/l10n/source"
                xmlns:lt="http://docbook.org/ns/docbook/l10n/templates"
                xmlns:m="http://docbook.org/ns/docbook/modes"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                xmlns:mp="http://docbook.org/ns/docbook/modes/private"
                xmlns:n="http://docbook.org/ns/docbook/l10n/number"
                xmlns:t="http://docbook.org/ns/docbook/l10n/title"
                xmlns:v="http://docbook.org/ns/docbook/variables"
                xmlns:vp="http://docbook.org/ns/docbook/variables/private"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml"
                default-mode="m:docbook"
                exclude-result-prefixes="#all"
                version="3.0">

<xsl:function name="f:languages" as="xs:string+" cache="yes">
  <xsl:param name="context" as="document-node()"/>

  <xsl:variable name="always"
                select="('en', $default-language, $gentext-language)"/>

  <xsl:sequence
      select="if (exists($gentext-language))
              then distinct-values($always)
              else distinct-values(($always, $context//@xml:lang))"/>
</xsl:function>  

<xsl:function name="f:in-scope-language" as="xs:string" cache="yes">
  <xsl:param name="target" as="node()"/>
  <xsl:sequence select="($target/ancestor-or-self::*[@xml:lang][1]/@xml:lang,
                         $default-language)[1]"/>
</xsl:function>  

<!-- ============================================================ -->

<xsl:function name="fp:localization" as="element(l:l10n)" cache="yes">
  <xsl:param name="language" as="xs:string"/>

  <xsl:variable name="fn-region" select="lower-case($language) =&gt; replace('-', '_')"/>
  <xsl:variable name="fn" select="if (contains($fn-region, '_'))
                                  then substring-before($fn-region, '_')
                                  else ()"/>

  <xsl:variable name="base-locale" as="element(l:l10n)">
    <xsl:choose>
      <xsl:when test="doc-available('../locale/' || $fn-region || '.xml')">
        <xsl:sequence select="doc('../locale/' || $fn-region || '.xml')/l:l10n"/>
      </xsl:when>
      <xsl:when test="exists($fn) and doc-available('../locale/' || $fn || '.xml')">
        <xsl:sequence select="doc('../locale/' || $fn || '.xml')/l:l10n"/>
      </xsl:when>
      <xsl:when test="$language != $default-language">
        <xsl:sequence select="fp:localization($default-language)"/>
      </xsl:when>
      <xsl:when test="doc-available('../locale/en.xml')">
        <xsl:sequence select="doc('../locale/en.xml')/l:l10n"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes"
                     select="'Failed to load localization or fallback localization'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="$v:custom-localizations/ls:locale[@language = $base-locale/@language]">
      <xsl:variable name="custom" as="element(l:l10n)">
        <xsl:apply-templates
            select="$v:custom-localizations/ls:locale[@language = $base-locale/@language]"
            mode="mp:transform-locale"/>
      </xsl:variable>
      <xsl:apply-templates select="$base-locale" mode="mp:merge-custom">
        <xsl:with-param name="custom" as="element(l:l10n)" tunnel="yes"
                        select="$custom"/>
      </xsl:apply-templates>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="$base-locale"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<!-- ============================================================ -->

<xsl:function name="fp:localization-template" as="item()*">
  <xsl:param name="node" as="element()"/>
  <xsl:param name="group" as="xs:string"/>

  <xsl:variable name="lang" select="f:l10n-language($node)"/>

  <xsl:choose>
    <xsl:when test="exists(fp:lookup-localization-template($node, $lang, $group))">
      <xsl:sequence select="fp:lookup-localization-template($node, $lang, $group)"/>
    </xsl:when>
    <xsl:when test="exists(fp:lookup-localization-template($node, $default-language, $group))">
      <xsl:if test="f:is-true($warn-about-missing-localizations)">
        <xsl:message expand-text="yes"
        >No localization for {$group}/{local-name($node)} in {$lang}, using {$default-language}</xsl:message>
      </xsl:if>
      <xsl:sequence select="fp:lookup-localization-template($node, $default-language, $group)"/>
    </xsl:when>
    <xsl:when test="exists(fp:lookup-localization-template($node, 'en', $group))">
      <xsl:if test="f:is-true($warn-about-missing-localizations)">
        <xsl:message expand-text="yes"
        >No localization for {$group}/{local-name($node)} in {$lang}, using en</xsl:message>
      </xsl:if>
      <xsl:sequence select="fp:lookup-localization-template($node, 'en', $group)"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:if test="f:is-true($warn-about-missing-localizations)">
        <xsl:message expand-text="yes"
        >No localization for {$group}/{local-name($node)} in {$lang}, using "MISSING"</xsl:message>
      </xsl:if>
      <lt:text>MISSING</lt:text>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<xsl:function name="fp:lookup-localization-template" as="element(l:template)?" cache="yes">
  <xsl:param name="node" as="element()"/>
  <xsl:param name="lang" as="xs:string"/>
  <xsl:param name="group" as="xs:string"/>

  <xsl:variable name="l10n" select="fp:localization($lang)"/>

  <xsl:variable name="templates"
                select="$l10n/l:group[@name=$group]"/>

  <!--
  <xsl:message select="'lookup:', local-name($node), $lang, $group"/>
  -->

  <xsl:iterate select="$templates/l:template">
    <xsl:variable name="result" as="item()*">
      <xsl:evaluate xpath="@match" context-item="$node"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="boolean($result)">
        <!--<xsl:message select="'Y:', @match/string()"/>-->
        <xsl:break select="."/>
      </xsl:when>
      <xsl:otherwise>
        <!--<xsl:message select="'N:', @match/string()"/>-->
        <xsl:next-iteration/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:iterate>
</xsl:function>

<!-- ============================================================ -->

<xsl:function name="fp:localization-list" as="item()*">
  <xsl:param name="node" as="element()"/>
  <xsl:param name="name" as="xs:string"/>

  <xsl:variable name="lang" select="f:l10n-language($node)"/>

  <xsl:choose>
    <xsl:when test="exists(fp:lookup-localization-list($node, $lang, $name))">
      <xsl:sequence select="fp:lookup-localization-list($node, $lang, $name)"/>
    </xsl:when>
    <xsl:when test="exists(fp:lookup-localization-list($node, $default-language, $name))">
      <xsl:if test="f:is-true($warn-about-missing-localizations)">
        <xsl:message expand-text="yes"
        >No localization list for {$name} in {$lang}, using {$default-language}</xsl:message>
      </xsl:if>
      <xsl:sequence select="fp:lookup-localization-list($node, $default-language, $name)"/>
    </xsl:when>
    <xsl:when test="exists(fp:lookup-localization-list($node, 'en', $name))">
      <xsl:if test="f:is-true($warn-about-missing-localizations)">
        <xsl:message expand-text="yes"
        >No localization list for {$name}in {$lang}, using en</xsl:message>
      </xsl:if>
      <xsl:sequence select="fp:lookup-localization-list($node, 'en', $name)"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:if test="f:is-true($warn-about-missing-localizations)">
        <xsl:message expand-text="yes"
        >No localization list for {$name} in {$lang}, using "MISSING"</xsl:message>
      </xsl:if>
      <lt:text>MISSING</lt:text>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<xsl:function name="fp:lookup-localization-list" as="element(l:list)?" cache="yes">
  <xsl:param name="node" as="element()"/>
  <xsl:param name="lang" as="xs:string"/>
  <xsl:param name="name" as="xs:string"/>

  <xsl:variable name="l10n" select="fp:localization($lang)"/>

  <xsl:choose>
    <xsl:when test="$l10n/l:list[@name=$name]">
      <xsl:sequence select="$l10n/l:list[@name=$name]"/>
    </xsl:when>
    <xsl:when test="$l10n/l:list[@name='_default']">
      <xsl:sequence select="$l10n/l:list[@name='_default']"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="()"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<!-- ============================================================ -->

<xsl:mode name="mp:merge-custom" on-no-match="shallow-copy"/>

<xsl:template match="l:l10n" mode="mp:merge-custom">
  <xsl:param name="custom" as="element(l:l10n)" tunnel="yes"/>

  <xsl:variable name="this" select="."/>
  <xsl:copy>
    <xsl:copy-of select="@*"/>
    <xsl:apply-templates mode="mp:merge-custom"/>

    <xsl:for-each select="$custom/l:group">
      <xsl:variable name="name" select="string(@name)"/>
      <xsl:if test="empty($this/l:group[@name = $name])">
        <xsl:message use-when="'localization' = $v:debug"
                     select="'Add localization group: ' || $name"/>
        <xsl:sequence select="."/>
      </xsl:if>
    </xsl:for-each>

    <xsl:for-each select="$custom/l:properties">
      <xsl:variable name="name" select="string(@name)"/>
      <xsl:if test="empty($this/l:properties[@name = $name])">
        <xsl:message use-when="'localization' = $v:debug"
                     select="'Add localization properties: ' || $name"/>
        <xsl:sequence select="."/>
      </xsl:if>
    </xsl:for-each>

    <xsl:for-each select="$custom/l:list">
      <xsl:variable name="name" select="string(@name)"/>
      <xsl:if test="empty($this/l:list[@name = $name])">
        <xsl:message use-when="'localization' = $v:debug"
                     select="'Add localization list: ' || $name"/>
        <xsl:sequence select="."/>
      </xsl:if>
    </xsl:for-each>
  </xsl:copy>
</xsl:template>

<xsl:template match="l:gentext" mode="mp:merge-custom">
  <xsl:param name="custom" as="element(l:l10n)" tunnel="yes"/>

  <xsl:variable name="this" select="."/>
  <xsl:copy>
    <xsl:copy-of select="@*"/>
    <xsl:for-each select="$custom/l:gentext/l:token">
      <xsl:variable name="key" select="string(@key)"/>
      <xsl:if test="empty($this/l:token[@key = $key])">
        <xsl:message use-when="'localization' = $v:debug"
                     select="'Add localization token: ' || $key"/>
        <xsl:sequence select="."/>
      </xsl:if>
    </xsl:for-each>
    <xsl:apply-templates mode="mp:merge-custom"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="l:gentext/l:token" mode="mp:merge-custom">
  <xsl:param name="custom" as="element(l:l10n)" tunnel="yes"/>

  <xsl:variable name="key" select="string(@key)"/>
  <xsl:variable name="override"
                select="$custom/l:gentext/l:token[@key=$key]"/>

  <xsl:choose>
    <xsl:when test="exists($override)">
      <xsl:message use-when="'localization' = $v:debug"
                   select="'Override localization token: ' || $key"/>
      <xsl:sequence select="$override"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:copy>
        <xsl:copy-of select="@*"/>
        <xsl:apply-templates mode="mp:merge-custom"/>
      </xsl:copy>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="l:properties" mode="mp:merge-custom">
  <xsl:param name="custom" as="element(l:l10n)" tunnel="yes"/>

  <xsl:variable name="this" select="."/>
  <xsl:copy>
    <xsl:copy-of select="@*"/>
    <xsl:for-each select="$custom/l:properties[@name = $this/@name]/l:property">
      <xsl:variable name="name" select="string(@name)"/>
      <xsl:if test="empty($this/l:property[@name = $name])">
        <xsl:message use-when="'localization' = $v:debug"
                     select="'Add localization property: ' || $this/@name || '/' || $name"/>
        <xsl:sequence select="."/>
      </xsl:if>
    </xsl:for-each>
    <xsl:apply-templates mode="mp:merge-custom"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="l:properties/l:property" mode="mp:merge-custom">
  <xsl:param name="custom" as="element(l:l10n)" tunnel="yes"/>

  <xsl:variable name="pname" select="string(../@name)"/>
  <xsl:variable name="name" select="string(@name)"/>
  <xsl:variable name="override"
                select="$custom/l:properties[@name=$pname]/l:property[@name=$name]"/>

  <xsl:choose>
    <xsl:when test="exists($override)">
      <xsl:message use-when="'localization' = $v:debug"
                   select="'Override localization property: ' || $pname || '/' || $name"/>
      <xsl:sequence select="$override"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:copy>
        <xsl:copy-of select="@*"/>
        <xsl:apply-templates mode="mp:merge-custom"/>
      </xsl:copy>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="l:group" mode="mp:merge-custom">
  <xsl:param name="custom" as="element(l:l10n)" tunnel="yes"/>

  <xsl:variable name="this" select="."/>
  <xsl:copy>
    <xsl:copy-of select="@*"/>
    <xsl:for-each select="$custom/l:group[@name = $this/@name]/l:template">
      <xsl:variable name="key" select="string(@key)"/>
      <xsl:if test="empty($this/l:template[@key = $key])">
        <xsl:message use-when="'localization' = $v:debug"
                     select="'Add localization template: ' || $this/@name || '/' || $key"/>
        <xsl:sequence select="."/>
      </xsl:if>
    </xsl:for-each>
    <xsl:apply-templates mode="mp:merge-custom"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="l:group/l:template" mode="mp:merge-custom">
  <xsl:param name="custom" as="element(l:l10n)" tunnel="yes"/>

  <xsl:variable name="pname" select="string(../@name)"/>
  <xsl:variable name="key" select="string(@key)"/>
  <xsl:variable name="override"
                select="$custom/l:group[@name=$pname]/l:template[@key=$key]"/>

  <xsl:choose>
    <xsl:when test="exists($override)">
      <xsl:message use-when="'localization' = $v:debug"
                   select="'Override localization template: ' || $pname || '/' || $key"/>
      <xsl:sequence select="$override"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:copy>
        <xsl:copy-of select="@*"/>
        <xsl:apply-templates mode="mp:merge-custom"/>
      </xsl:copy>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="l:l10n/l:list" mode="mp:merge-custom">
  <xsl:param name="custom" as="element(l:l10n)" tunnel="yes"/>

  <xsl:variable name="name" select="string(@name)"/>
  <xsl:variable name="override"
                select="$custom/l:list[@name=$name]"/>

  <xsl:choose>
    <xsl:when test="exists($override)">
      <xsl:message use-when="'localization' = $v:debug"
                   select="'Override localization list: ' || $name"/>
      <xsl:sequence select="$override"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:copy>
        <xsl:copy-of select="@*"/>
        <xsl:apply-templates mode="mp:merge-custom"/>
      </xsl:copy>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="l:letters/l:l[exists(node())]" mode="mp:merge-custom">
  <xsl:param name="custom" as="element(l:l10n)" tunnel="yes"/>

  <xsl:variable name="symbol" select="string(.)"/>
  <xsl:variable name="override"
                select="$custom/l:letters/l:l[string(.) = $symbol]"/>

  <xsl:choose>
    <xsl:when test="exists($override)">
      <xsl:message use-when="'localization' = $v:debug"
                   select="'Override localization symbol: ' || $symbol"/>
      <xsl:sequence select="$override"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:copy>
        <xsl:copy-of select="@*"/>
        <xsl:apply-templates mode="mp:merge-custom"/>
      </xsl:copy>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*" mode="mp:merge-custom">
  <xsl:copy>
    <xsl:copy-of select="@*"/>
    <xsl:apply-templates mode="mp:merge-custom"/>
  </xsl:copy>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="*" mode="m:gentext">
  <xsl:param name="group" as="xs:string"/>
  <xsl:param name="content" as="item()*" select="()"/>

  <xsl:variable name="template"
                select="fp:localization-template(., $group)"/>

  <!--
  <xsl:message select="'GENTEXT:', local-name(.), $group, exists($content), $template"/>>
  -->

  <xsl:apply-templates select="$template" mode="mp:localization">
    <xsl:with-param name="context" select="."/>
    <xsl:with-param name="content" select="$content"/>
    <xsl:with-param name="group" select="$group"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="db:question|db:answer" mode="m:gentext">
  <xsl:param name="group" as="xs:string"/>
  <xsl:param name="content" as="item()*" select="()"/>

  <!-- This is a weird special case because the default label
       ends in a colon. If we're looking for a label separator,
       and we're using the 'qanda' label style, and the label
       text ends with punctuation, just output a space.
       Note: there are some extra conditionals in here to
       avoid doing extra work if the conditions don't apply. -->

  <xsl:variable name="label" as="xs:string?"
                select="if ($group = 'label-separator')
                        then ancestor::db:qandaset[@defaultlabel][1]/@defaultlabel/string()
                        else ()"/>
  <xsl:variable name="label" as="xs:string"
                select="if ($label)
                        then $label
                        else $qandaset-default-label"/>
  <xsl:variable name="text"
                select="if ($group = 'label-separator' and $label = 'qanda')
                        then f:l10n-token(., local-name(.))
                        else string(db:label)"/>

  <xsl:choose>
    <xsl:when test="$group = 'label-separator' and exists($text)
                    and matches($text, '\p{Po}$')">
      <xsl:sequence select="' '"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:next-match>
        <xsl:with-param name="group" select="$group"/>
        <xsl:with-param name="content" select="$content"/>
      </xsl:next-match>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
