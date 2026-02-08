<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:array="http://www.w3.org/2005/xpath-functions/array"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:err="http://www.w3.org/2005/xqt-errors"
                xmlns:f="http://docbook.org/ns/docbook/functions"
                xmlns:fp="http://docbook.org/ns/docbook/functions/private"
                xmlns:l="http://docbook.org/ns/docbook/l10n"
                xmlns:m="http://docbook.org/ns/docbook/modes"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                xmlns:mp="http://docbook.org/ns/docbook/modes/private"
                xmlns:v="http://docbook.org/ns/docbook/variables"
                xmlns:vp="http://docbook.org/ns/docbook/variables/private"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml"
                default-mode="m:docbook"
                exclude-result-prefixes="array db f fp l m map mp v vp xs"
                version="3.0">

<xsl:include href="../standalone-functions.xsl"/>

<xsl:key name="id" match="*" use="@xml:id"/>
<xsl:key name="genid" match="*" use="generate-id(.)"/>

<xsl:variable name="vp:translate-suppress-elements"
              select="tokenize($translate-suppress-elements, '\s+')"/>

<xsl:function name="f:translate-attribute" as="xs:boolean?">
  <xsl:param name="node" as="element()"/>
  <xsl:if test="local-name($node) = $vp:translate-suppress-elements">
    <xsl:sequence select="false()"/>
  </xsl:if>
</xsl:function>

<xsl:function name="f:attributes" as="attribute()*">
  <xsl:param name="node" as="element()"/>
  <xsl:param name="attributes" as="attribute()*"/>
  <xsl:sequence select="f:attributes($node, $attributes, local-name($node), ())"/>
</xsl:function>

<xsl:function name="f:attributes" as="attribute()*">
  <xsl:param name="node" as="element()"/>
  <xsl:param name="attributes" as="attribute()*"/>
  <xsl:param name="extra-classes" as="xs:string*"/>
  <xsl:param name="exclude-classes" as="xs:string*"/>

  <!--
  <xsl:message>
    <xsl:text>f:attributes(</xsl:text>
    <xsl:value-of select="node-name($node)"/>
    <xsl:text>,</xsl:text>
    <xsl:sequence select="$attributes"/>
    <xsl:text>,</xsl:text>
    <xsl:sequence select="$extra-classes"/>
    <xsl:text>,</xsl:text>
    <xsl:sequence select="$exclude-classes"/>
    <xsl:text>)</xsl:text>
  </xsl:message>
  -->

  <!-- combine duplicates -->
  <xsl:variable name="names"
                select="distinct-values($attributes/node-name())"/>
  <xsl:for-each select="$names">
    <xsl:variable name="name" select="."/>
    <xsl:variable name="values" as="xs:string*"
                  select="$attributes[node-name()=$name]/string()"/>
    <xsl:if test="exists($values)">
      <xsl:attribute name="{$name}"
                     select="string-join($values, ' ')"/>
    </xsl:if>
  </xsl:for-each>

  <!-- if there isn't a class attribute, manufacture one -->
  <xsl:if test="not(QName('', 'class') = $attributes/node-name())">
    <xsl:variable name="roles"
                  select="(tokenize(normalize-space(string-join($extra-classes, ' '))),
                           tokenize(normalize-space($node/@role)),
                           if ($node/@revisionflag)
                           then 'rev'||$node/@revisionflag
                           else ())"/>
    <xsl:variable name="exclude" 
                  select="tokenize(normalize-space(string-join($exclude-classes, ' ')))"/>
    <!-- sort them and make them unique -->
    <xsl:variable name="classes" as="xs:string*">
      <xsl:for-each select="distinct-values($roles)">
        <xsl:sort select="."/>
        <xsl:if test="not(. = $exclude)">
          <xsl:sequence select="."/>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>

    <xsl:if test="exists($classes)">
      <xsl:attribute name="class" select="string-join($classes, ' ')"/>
    </xsl:if>
  </xsl:if>

  <xsl:variable name="translate" select="f:translate-attribute($node)"/>
  <xsl:if test="exists($translate)">
    <xsl:attribute name="translate" select="if ($translate) then 'yes' else 'no'"/>
  </xsl:if>
</xsl:function>

<xsl:function name="f:is-true" as="xs:boolean" visibility="public">
  <xsl:param name="value"/>

  <xsl:choose>
    <xsl:when test="empty($value)">
      <xsl:value-of select="false()"/>
    </xsl:when>
    <xsl:when test="$value castable as xs:boolean">
      <xsl:value-of select="xs:boolean($value)"/>
    </xsl:when>
    <xsl:when test="$value castable as xs:integer">
      <xsl:value-of select="xs:integer($value) != 0"/>
    </xsl:when>
    <xsl:when test="string($value) = ('true', 'yes')">
      <xsl:value-of select="true()"/>
    </xsl:when>
    <xsl:when test="string($value) = ('false', 'no')">
      <xsl:value-of select="false()"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:message expand-text="yes" terminate="yes"
                   >Warning: interpreting ‘{$value}’ as true.</xsl:message>
      <xsl:value-of select="true()"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<xsl:function name="f:orderedlist-startingnumber" as="xs:integer">
  <xsl:param name="list" as="element(db:orderedlist)"/>

  <xsl:choose>
    <xsl:when test="not($list/@continuation = 'continues')">
      <xsl:sequence select="1"/>
    </xsl:when>
    <xsl:when test="empty($list/preceding::db:orderedlist)">
      <xsl:message>
        <xsl:text>Warning: orderedlist continuation=continues, </xsl:text>
        <xsl:text>but no preceding list</xsl:text>
      </xsl:message>
      <xsl:sequence select="1"/>
    </xsl:when>
    <xsl:otherwise>
      <!-- Hat tip to Gerrit for the fix here -->
      <xsl:variable name="plist"
                    select="$list/outermost(preceding::db:orderedlist)[last()]"/>

      <xsl:sequence select="f:orderedlist-startingnumber($plist)
                            + count($plist/db:listitem)"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<xsl:function name="f:l10n-language" as="xs:string" cache="yes">
  <xsl:param name="target" as="element()"/>

  <xsl:variable name="nearest-lang"
                select="$target/ancestor-or-self::*[@xml:lang][1]/@xml:lang"/>

  <xsl:variable name="mc-language" as="xs:string"
                select="if (exists($gentext-language))
                        then $gentext-language
                        else if (exists($nearest-lang) and $nearest-lang = '')
                             then $default-language
                             else ($nearest-lang, $default-language)[1]"/>

  <xsl:variable name="language" select="lower-case($mc-language)"/>

  <xsl:variable name="adjusted-language"
                select="if (contains($language, '-'))
                        then substring-before($language, '-')
                             || '_' || substring-after($language, '-')
                        else $language"/>

  <xsl:choose>
    <xsl:when test="doc-available(
                      resolve-uri($adjusted-language||'.xml', $v:localization-base-uri))">
      <xsl:sequence select="$adjusted-language"/>
    </xsl:when>
    <!-- try just the lang code without country -->
    <xsl:when test="doc-available(
                      resolve-uri(
                        substring-before($adjusted-language, '_')||'.xml', $v:localization-base-uri))">
      <xsl:sequence select="substring-before($adjusted-language,'_')"/>
    </xsl:when>
    <!-- or use the default -->
    <xsl:otherwise>
      <xsl:message>
        <xsl:text>No localization exists for "</xsl:text>
        <xsl:sequence select="$adjusted-language"/>
        <xsl:text>" or "</xsl:text>
        <xsl:sequence select="substring-before($adjusted-language,'_')"/>
        <xsl:text>". Using default "</xsl:text>
        <xsl:sequence select="$default-language"/>
        <xsl:text>".</xsl:text>
      </xsl:message>
      <xsl:sequence select="$default-language"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<xsl:function name="f:gentext-letters" as="element(l:letters)">
  <xsl:param name="node" as="element()"/>
  <xsl:sequence select="f:gentext-letters-for-language($node)"/>
</xsl:function>

<xsl:function name="f:gentext-letters-for-language" as="element(l:letters)">
  <xsl:param name="node" as="element()"/>

  <xsl:variable name="lang" select="f:l10n-language($node)"/>
  <xsl:variable name="l10n" select="fp:localization($lang)"/>

  <xsl:variable name="letters"
                select="$l10n/l:letters"/>

  <xsl:if test="empty($letters)">
    <xsl:message select="'No letters for', $lang"/>
  </xsl:if>

  <xsl:if test="count($letters) gt 1">
    <xsl:message
        select="'Multiple letters for localization:', $lang"/>
  </xsl:if>

  <xsl:sequence select="$letters[1]"/>
</xsl:function>

<xsl:function name="fp:properties" as="map(*)">
  <xsl:param name="context" as="element()"/>
  <xsl:param name="properties" as="array(map(*))"/>

  <xsl:variable name="props" as="map(*)*">
    <xsl:for-each select="1 to array:size($properties)">
      <xsl:variable name="map" select="array:get($properties, .)"/>
      <xsl:variable name="nodes" as="node()*">
        <xsl:evaluate context-item="$context" xpath="$map?xpath"/>
      </xsl:variable>
      <xsl:if test="exists($nodes)">
        <xsl:sequence select="$map"/>
      </xsl:if>
    </xsl:for-each>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="empty($props)">
      <xsl:message use-when="'properties' = $v:debug"
          select="'No properties for ' || local-name($context)"/>
      <xsl:sequence select="map { }"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="$props[1]"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<xsl:function name="f:date-format" as="xs:string">
  <xsl:param name="context" as="element()"/>

  <xsl:variable name="format"
                select="f:pi($context, 'date-format')"/>

  <xsl:choose>
    <xsl:when test="$context/*">
      <xsl:sequence select="'apply-templates'"/>
    </xsl:when>
    <xsl:when test="string($context) castable as xs:dateTime">
      <xsl:choose>
        <xsl:when test="$format">
          <xsl:sequence select="$format"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:sequence select="$date-dateTime-format"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:when test="string($context) castable as xs:date">
      <xsl:choose>
        <xsl:when test="$format">
          <xsl:sequence select="$format"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:sequence select="$date-date-format"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="'apply-templates'"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<!-- ============================================================ -->

<xsl:function name="fp:replace-element" as="array(*)">
  <xsl:param name="lines" as="array(*)"/>
  <xsl:param name="elemno" as="xs:integer"/>
  <xsl:param name="new-elem" as="item()*"/>
  <xsl:sequence
      select="fp:replace-element($lines, $elemno, 1, $new-elem, [])"/>
</xsl:function>

<xsl:function name="fp:replace-element" as="array(*)">
  <!-- See https://saxonica.plan.io/issues/4500 -->
  <!-- reimplement this with array:join when that but is fixed -->
  <xsl:param name="array" as="array(*)"/>
  <xsl:param name="elemno" as="xs:integer"/>
  <xsl:param name="count" as="xs:integer"/>
  <xsl:param name="new-elem" as="item()*"/>
  <xsl:param name="newarray" as="array(*)"/>

  <xsl:choose>
    <xsl:when test="$count gt array:size($array)">
      <xsl:sequence select="$newarray"/>
    </xsl:when>
    <xsl:when test="$count = $elemno">
      <xsl:sequence
          select="fp:replace-element($array, $elemno, $count+1, $new-elem,
                                     array:append($newarray, $new-elem))"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence
          select="fp:replace-element($array, $elemno, $count+1, $new-elem,
                                     array:append($newarray, $array($count)))"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<xsl:function name="f:target" as="element()*" cache="yes">
  <xsl:param name="id" as="xs:string"/>
  <xsl:param name="context" as="node()"/>
  <xsl:sequence select="key('id', $id, root($context))"/>
</xsl:function>

<xsl:function name="f:href" as="xs:string" cache="yes">
  <xsl:param name="context" as="node()"/>
  <xsl:param name="node" as="element()"/>
  <xsl:sequence select="'#' || f:generate-id($node)"/>
</xsl:function>

<xsl:variable name="vp:gidmap" select="map {
  'acknowledgements': 'ack',
  'annotation': 'an',
  'appendix': 'ap',
  'article': 'art',
  'bibliodiv': 'bd',
  'bibliography': 'bi',
  'book': 'bo',
  'chapter': 'ch',
  'colophon': 'co',
  'dedication': 'ded',
  'equation': 'eq',
  'example': 'ex',
  'figure': 'fig',
  'glossary': 'g',
  'glossdiv': 'gd',
  'glossentry': 'ge',
  'glossterm': 'gt',
  'itemizedlist': 'il',
  'listitem': 'li',
  'orderedlist': 'ol',
  'part': 'part',
  'preface': 'p',
  'procedure': 'proc',
  'refentry': 're',
  'reference': 'ref',
  'refsect1': 'rs1_',
  'refsect2': 'rs2_',
  'refsect3': 'rs3_',
  'sect1': 's1_',
  'sect2': 's2_',
  'sect3': 's3_',
  'sect4': 's4_',
  'sect5': 's5_',
  'section': 's',
  'table': 'tab',
  'variablelist': 'vl',
  'varlistentry': 'vle'
  }"/>

<xsl:function name="f:generate-id" as="xs:string" cache="yes">
  <xsl:param name="node" as="element()"/>
  <xsl:sequence select="f:generate-id($node, true())"/>
</xsl:function>

<xsl:function name="f:generate-id" as="xs:string" cache="yes">
  <xsl:param name="node" as="element()"/>
  <xsl:param name="use-xml-id" as="xs:boolean"/>
  <xsl:choose>
    <xsl:when test="$use-xml-id and $node/@xml:id">
      <xsl:sequence select="$node/@xml:id/string()"/>
    </xsl:when>
    <xsl:when test="empty($node/parent::*)">
      <xsl:sequence select="$generated-id-root"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:variable name="aid" select="f:generate-id($node/parent::*, $use-xml-id)"/>
      <xsl:variable name="type" select="(map:get($vp:gidmap, local-name($node)),
                                         local-name($node))[1]"/>
      <xsl:variable name="prec"
                    select="$node/preceding-sibling::*[node-name(.)=node-name($node)]"/>
      <xsl:sequence
          select="$aid || $generated-id-sep || $type || string(count($prec)+1)"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<xsl:function name="f:id" as="xs:string" cache="yes">
  <xsl:param name="node" as="element()"/>
  <xsl:sequence select="if ($node/@xml:id)
                        then $node/@xml:id/string()
                        else f:generate-id($node)"/>
</xsl:function>

<xsl:function name="f:unique-id" as="xs:string" cache="yes">
  <xsl:param name="node" as="element()"/>
  <xsl:sequence select="f:generate-id($node, false())"/>
</xsl:function>

<xsl:function name="fp:css-properties" as="attribute()?">
  <xsl:param name="context" as="element()?"/>

  <xsl:variable name="generic-attributes" as="map(*)">
    <xsl:map>
      <xsl:for-each select="$context/@css:*" xmlns:css="https://xsltng.docbook.org/ns/css">
        <xsl:map-entry key="local-name(.)" select="string(.)"/>
      </xsl:for-each>
    </xsl:map>
  </xsl:variable>

  <xsl:variable name="media-attributes" as="map(*)">
    <xsl:map>
      <xsl:for-each select="$context/@*">
        <xsl:if test="namespace-uri(.) = 'https://xsltng.docbook.org/ns/css#' || $output-media">
          <xsl:map-entry key="local-name(.)" select="string(.)"/>
        </xsl:if>
      </xsl:for-each>
    </xsl:map>
  </xsl:variable>

  <xsl:variable name="attributes" as="map(*)"
                select="map:merge(($generic-attributes, $media-attributes),
                                  map { 'duplicates': 'use-last' })"/>

  <xsl:if test="map:size($attributes) != 0">
    <xsl:iterate select="map:keys($attributes)">
      <xsl:param name="css" select="''"/>
      <xsl:on-completion>
        <xsl:attribute name="style" select="$css"/>
      </xsl:on-completion>
      <xsl:variable name="name" select="."/>
      <xsl:variable name="value" select="map:get($attributes, .)"/>
      <xsl:next-iteration>
        <xsl:with-param name="css" select="$css || $name || ':' || $value || ';'"/>
      </xsl:next-iteration>
    </xsl:iterate>
  </xsl:if>
</xsl:function>

<xsl:function name="f:spaces" as="xs:string?">
  <xsl:param name="length" as="item()*"/>

  <xsl:choose>
    <xsl:when test="empty($length)"/>
    <xsl:when test="count($length) gt 1">
      <xsl:sequence
          select="f:spaces(string-join($length ! string(.), ''))"/>
    </xsl:when>
    <xsl:when test="$length castable as xs:integer">
      <xsl:variable name="length" select="xs:integer($length)"/>
      <xsl:choose>
        <xsl:when test="$length lt 0"/>
        <xsl:when test="$length lt 10">
          <xsl:sequence select="substring('          ', 1, $length)"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:sequence select="'          ' || f:spaces($length - 10)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="f:spaces(string-length(string($length)))"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<!-- ============================================================ -->

<xsl:function name="fp:lookup-string" as="node()*">
  <xsl:param name="context" as="element()"/>
  <xsl:param name="lookup" as="element()"/>
  <xsl:param name="table-name" as="xs:string"/>

  <xsl:variable name="value"
                select="$lookup/*[node-name(.)=node-name($context)]"/>

  <xsl:if test="count($value) gt 1">
    <xsl:message expand-text="yes"
                 >Duplicate {$table-name} for {node-name($context)}</xsl:message>
  </xsl:if>

  <xsl:sequence select="if (empty($value))
                        then $lookup/db:_default/node()
                        else $value[1]/node()"/>
</xsl:function>

<xsl:function name="fp:separator" as="node()*">
  <xsl:param name="node" as="element()"/>
  <xsl:param name="key" as="xs:string"/>
  <xsl:sequence select="fp:localization-template($node, 'separator')"/>
</xsl:function>

<xsl:function name="f:label-separator" as="node()*">
  <xsl:param name="node" as="element()"/>
  <xsl:sequence select="fp:separator($node, 'label-separator')"/>
</xsl:function>

<xsl:function name="fp:parse-key-value-pairs" as="map(xs:string,xs:string)">
  <xsl:param name="strings" as="xs:string*"/>
  <xsl:sequence select="fp:parse-key-value-pairs($strings, map { })"/>
</xsl:function>

<xsl:function name="fp:parse-key-value-pairs" as="map(xs:string,xs:string)">
  <xsl:param name="strings" as="xs:string*"/>
  <xsl:param name="map" as="map(xs:string,xs:string)"/>

  <xsl:variable name="car" select="$strings[1]"/>
  <xsl:variable name="cdr" select="subsequence($strings, 2)"/>

  <xsl:variable name="key" select="if (contains($car, ':'))
                                   then substring-before($car, ':')
                                   else '_default'"/>
  <xsl:variable name="value" select="if (contains($car, ':'))
                                     then substring-after($car, ':')
                                     else $car"/>

  <xsl:choose>
    <xsl:when test="empty($car)">
      <xsl:sequence select="$map"/>
    </xsl:when>
    <xsl:when test="map:contains($map, $key)">
      <xsl:message select="'Warning: ignoring duplicate key:', $key"/>
      <xsl:sequence select="fp:parse-key-value-pairs($cdr, $map)"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="fp:parse-key-value-pairs($cdr,
                               map:put($map, $key, $value))"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<xsl:function name="f:refsection" as="xs:boolean">
  <xsl:param name="node" as="element()"/>
  <xsl:sequence select="$node/self::db:refsection
                        or $node/self::db:refsect1
                        or $node/self::db:refsect2
                        or $node/self::db:refsect3"/>
</xsl:function> 

<xsl:function name="f:section" as="xs:boolean" visibility="public">
  <xsl:param name="node" as="element()"/>
  <xsl:sequence select="$node/self::db:section
                        or $node/self::db:sect1
                        or $node/self::db:sect2
                        or $node/self::db:sect3
                        or $node/self::db:sect4
                        or $node/self::db:sect5
                        or f:refsection($node)"/>
</xsl:function>

<xsl:function name="f:section-depth" as="xs:integer" visibility="public">
  <xsl:param name="node" as="element()?"/>
  <xsl:choose>
    <xsl:when test="empty($node)">
      <xsl:value-of select="0"/>
    </xsl:when>
    <xsl:when test="$node/self::db:section">
      <xsl:value-of select="count($node/ancestor::db:section) + 1"/>
    </xsl:when>
    <xsl:when test="$node/self::db:sect1 or $node/self::db:sect2
                    or $node/self::db:sect3 or $node/self::db:sect4
                    or $node/self::db:sect5">
      <xsl:value-of select="xs:integer(substring(local-name($node), 5))"/>
    </xsl:when>
    <xsl:when test="$node/self::db:refsection">
      <xsl:value-of select="count($node/ancestor::db:refsection)+1"/>
    </xsl:when>
    <xsl:when test="$node/self::db:refsect1 or $node/self::db:refsect2
                    or $node/self::db:refsect3">
      <xsl:value-of select="xs:integer(substring(local-name($node), 8))"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="f:section-depth($node/parent::*)"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<xsl:function name="f:step-number" as="xs:integer+">
  <xsl:param name="node" as="element(db:step)"/>
  <xsl:iterate select="reverse($node/ancestor-or-self::*)">
    <xsl:param name="number" select="()"/>
    <xsl:choose>
      <xsl:when test="self::db:procedure">
        <xsl:sequence select="$number"/>
        <xsl:break/>
      </xsl:when>
      <xsl:when test="self::db:step">
        <xsl:next-iteration>
          <xsl:with-param name="number"
                          select="(count(preceding-sibling::db:step)+1, $number)"/>
        </xsl:next-iteration>
      </xsl:when>
      <xsl:otherwise>
        <xsl:next-iteration>
          <xsl:with-param name="number" select="$number"/>
        </xsl:next-iteration>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:iterate>
</xsl:function>

<xsl:function name="f:step-numeration" as="xs:string">
  <xsl:param name="node" as="element(db:step)"/>
  <xsl:variable name="depth"
                select="count(f:step-number($node))"/>
  <xsl:variable name="depth"
                select="$depth
                        mod string-length($procedure-step-numeration)"/>
  <xsl:variable name="depth"
                select="if ($depth eq 0)
                        then string-length($procedure-step-numeration)
                        else $depth"/>
  <xsl:sequence select="substring($procedure-step-numeration, $depth, 1)"/>
</xsl:function>

<xsl:function name="f:orderedlist-item-number" as="xs:integer+">
  <xsl:param name="node" as="element(db:listitem)"/>
  <xsl:iterate select="reverse($node/ancestor-or-self::*)">
    <xsl:param name="number" select="()"/>
    <xsl:on-completion select="$number"/>
    <xsl:choose>
      <xsl:when test="self::db:listitem[parent::db:orderedlist]">
        <xsl:next-iteration>
          <xsl:with-param name="number"
                          select="(count(preceding-sibling::db:listitem)
                                   + f:orderedlist-startingnumber(parent::db:orderedlist),
                                   $number)"/>
        </xsl:next-iteration>
      </xsl:when>
      <xsl:otherwise>
        <xsl:next-iteration>
          <xsl:with-param name="number" select="$number"/>
        </xsl:next-iteration>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:iterate>
</xsl:function>

<xsl:function name="f:orderedlist-item-numeration" as="xs:string">
  <xsl:param name="node" as="element(db:listitem)"/>

  <xsl:variable name="numeration" select="$node/parent::db:orderedlist/@numeration"/>

  <xsl:choose>
    <xsl:when test="exists($numeration)">
      <xsl:choose>
        <xsl:when test="$numeration = 'upperalpha'">
          <xsl:sequence select="'A'"/>
        </xsl:when>
        <xsl:when test="$numeration = 'loweralpha'">
          <xsl:sequence select="'a'"/>
        </xsl:when>
        <xsl:when test="$numeration = 'upperroman'">
          <xsl:sequence select="'I'"/>
        </xsl:when>
        <xsl:when test="$numeration = 'lowerroman'">
          <xsl:sequence select="'i'"/>
        </xsl:when>
        <xsl:otherwise> <!-- arabic -->
          <xsl:sequence select="'1'"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:otherwise>
      <xsl:variable name="depth"
                    select="count(f:orderedlist-item-number($node))"/>
      <xsl:variable name="depth"
                    select="$depth
                            mod string-length($orderedlist-item-numeration)"/>
      <xsl:variable name="depth"
                    select="if ($depth eq 0)
                            then string-length($orderedlist-item-numeration)
                            else $depth"/>
      <xsl:sequence select="substring($orderedlist-item-numeration, $depth, 1)"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<xsl:function name="f:tokenize-on-char" as="xs:string*">
  <xsl:param name="string" as="xs:string"/>
  <xsl:param name="char" as="xs:string"/>

  <xsl:variable name="ch" select="substring($char||' ', 1, 1)"/>

  <xsl:variable name="tchar"
                select="if ($ch = ('.', '?', '*', '{', '}', '\', '\[', '\]'))
                        then '\' || $ch
                        else $ch"/>

  <xsl:sequence select="tokenize($string, $tchar)"/>
</xsl:function>

<xsl:function name="f:uri-scheme" as="xs:string?">
  <xsl:param name="uri" as="xs:string"/>

  <xsl:if test="matches($uri, '^[-a-zA-Z0-9]+:')">
    <xsl:sequence select="replace($uri, '^([-a-zA-Z0-9]+):.*$', '$1')"/>
  </xsl:if>
</xsl:function>

<xsl:function name="f:relative-path" as="xs:string">
  <xsl:param name="base" as="xs:string"/>
  <xsl:param name="path" as="xs:string"/>

  <xsl:choose>
    <xsl:when test="exists(f:uri-scheme($path)) and f:uri-scheme($path) ne 'file'">
      <!-- It starts with a non-file: scheme, just assume it's absolute. -->
      <xsl:sequence select="$path"/>
    </xsl:when>
    <xsl:otherwise>
      <!-- Strip away variant forms of file: (file:/, file://, file:///, ...) -->
      <!-- In particular, file:/C:/path vs. file:///C:/path on Windows -->
      <xsl:variable name="bpath" select="replace($base, '^file:/+', '')"/>
      <xsl:variable name="ppath" select="replace($path, '^file:/+', '')"/>

      <xsl:variable name="base-parts" select="tokenize($bpath, '/')[position() lt last()]"/>
      <xsl:variable name="path-parts" select="tokenize($ppath, '/')"/>

      <!--
          <xsl:message select="'BP:', $base-parts"/>
          <xsl:message select="'PP:', $path-parts"/>
      -->

      <xsl:variable name="common-prefix" as="xs:string*">
        <xsl:iterate select="$base-parts">
          <xsl:param name="pos" select="1"/>
          <xsl:param name="common" select="()"/>
          <xsl:on-completion select="$common"/>
          <xsl:if test="$base-parts[$pos] = $path-parts[$pos]">
            <xsl:next-iteration>
              <xsl:with-param name="pos" select="$pos + 1"/>
              <xsl:with-param name="common" select="($common, $base-parts[$pos])"/>
            </xsl:next-iteration>
          </xsl:if>
        </xsl:iterate>
      </xsl:variable>

      <!--
          <xsl:message select="'CP:', $common-prefix"/>
      -->

      <xsl:variable name="base-tail"
                    select="$base-parts[position() gt count($common-prefix)]"/>
      <xsl:variable name="path-tail"
                    select="$path-parts[position() gt count($common-prefix)]"/>

      <!--
          <xsl:message select="'BT:', $base-tail"/>
          <xsl:message select="'PT:', $path-tail"/>
      -->

      <xsl:variable name="final-parts" as="xs:string*">
        <xsl:for-each select="1 to count($base-tail)">
          <xsl:sequence select="'..'"/>
        </xsl:for-each>
        <xsl:sequence select="$path-tail"/>
      </xsl:variable>

      <xsl:sequence select="string-join($final-parts, '/')"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>  

<xsl:function name="f:orientation-class" as="xs:string?">
  <xsl:param name="node" as="element()"/>

  <xsl:variable name="nearest"
                select="($node/ancestor-or-self::*[contains-token(@role,'landscape')
                                                   or contains-token(@role,'portrait')]
                         | $node/ancestor-or-self::db:table[@orient]
                         | $node/ancestor-or-self::db:informaltable[@orient])[last()]"/>

  <xsl:choose>
    <xsl:when test="$output-media != 'print'"/>
    <xsl:when test="$nearest/@orient and $nearest/@orient = 'land'">
      <xsl:sequence select="'landscape'"/>
    </xsl:when>
    <xsl:when test="$nearest/@orient">
      <xsl:sequence select="'portrait'"/>
    </xsl:when>
    <xsl:when test="contains-token($nearest/@role, 'landscape')">
      <xsl:sequence select="'landscape'"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="'portrait'"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<xsl:function name="f:conditional-orientation-class" as="xs:string?">
  <xsl:param name="node" as="element()"/>

  <xsl:variable name="parent" select="$node/parent::element() ! f:orientation-class(.)"/>
  <xsl:variable name="orient" select="f:orientation-class($node)"/>

<!--
  <xsl:message select="node-name($node), 'P:', $parent, ' O:', $orient"/>

      <xsl:sequence select="$orient"/>
-->
  <xsl:choose>
    <xsl:when test="exists($parent) and $parent eq $orient"/>
    <xsl:otherwise>
      <xsl:sequence select="$orient"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<xsl:function name="f:global-syntax-highlighter" as="xs:string" cache="yes">
  <xsl:param name="context" as="node()"/>
  <xsl:choose>
    <xsl:when test="f:pi($context/root()/*, 'syntax-highlighter')">
      <xsl:sequence select="f:pi($context/root()/*, 'syntax-highlighter')"/>
    </xsl:when>
    <xsl:when test="f:pi($context/root()/*/db:info, 'syntax-highlighter')">
      <xsl:sequence select="f:pi($context/root()/*/db:info, 'syntax-highlighter')"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="$verbatim-syntax-highlighter"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>
  
</xsl:stylesheet>
