<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:db="http://docbook.org/ns/docbook"
                xmlns:err="http://www.w3.org/2005/xqt-errors" 
                xmlns:f="http://docbook.org/ns/docbook/functions"
                xmlns:fp="http://docbook.org/ns/docbook/functions/private"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                xmlns:mp="http://docbook.org/ns/docbook/modes/private"
                xmlns:v="http://docbook.org/ns/docbook/variables"
                xmlns:vp="http://docbook.org/ns/docbook/variables/private"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="#all"
                version="3.0">
    
<!-- ===================================================================================
|    Functions designed for use independent from the xsTNG Stylesheets 
|    e. g. in Schematron rules  
|=================================================================================== -->

<xsl:key name="glossary-entry" match="db:glossary//db:glossentry"
         use="fp:baseform(db:glossterm)"/>

<xsl:key name="glossary-ref" match="db:glossterm[not(parent::db:glossentry)]|db:firstterm"
         use="fp:baseform(.)"/>

<xsl:key name="citation" match="db:citation" use="normalize-space(.)"/>

<xsl:key name="bibliography-entry"
         match="db:biblioentry[db:abbrev]|db:bibliomixed[db:abbrev]"
         use="db:abbrev ! normalize-space(.)"/>

<!-- ============================================================ -->

<xsl:variable name="v:pi-db-attributes-are-uris" as="xs:string*"
              select="('glossary-collection', 'bibliography-collection',
                       'annotation-collection')"/>
  
<!-- ====================================================================================
|    Functions for processing instructions and their pseudo attributes
|===================================================================================  -->  

<xsl:variable name="vp:pi-match"
              select="'^.*?(\c+)=[''&quot;](.*?)[''&quot;](.*)$'"/>  
  
<xsl:function name="f:pi" as="xs:string?" visibility="public">
  <xsl:param name="context" as="node()?"/>
  <xsl:param name="property" as="xs:string"/>
  <xsl:sequence select="f:pi($context, $property, ())"/>
</xsl:function>  

<xsl:function name="f:pi" as="xs:string*" visibility="public">
  <xsl:param name="context" as="node()?"/>
  <xsl:param name="property" as="xs:string"/>
  <xsl:param name="default" as="xs:string*"/>
  
  <xsl:choose>
    <xsl:when test="empty($context)">
      <xsl:sequence select="$default"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="fp:pi-from-list($context/processing-instruction('db'),
                                            $property, $default)"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<xsl:function name="fp:pi-from-list" as="xs:string*">
  <xsl:param name="pis" as="processing-instruction()*"/>
  <xsl:param name="property" as="xs:string"/>
  <xsl:param name="default" as="xs:string*"/>

  <xsl:variable name="value"
                select="f:pi-attributes($pis)/@*[local-name(.) = $property]/string()"/>

  <xsl:sequence select="if (empty($value))
                        then $default
                        else $value"/>
</xsl:function>

<xsl:function name="f:pi-attributes" as="element()?">
  <xsl:param name="pis" as="processing-instruction()*"/>

  <xsl:variable name="attributes"
    select="fp:pi-attributes($pis, map { })"/>
  
  <xsl:element name="pis" namespace="">
    <xsl:for-each select="map:keys($attributes)">
      <xsl:attribute name="{.}" select="map:get($attributes, .)"/>
    </xsl:for-each>
  </xsl:element>
</xsl:function>  

<xsl:function name="fp:pi-attributes" as="map(*)?">
  <xsl:param name="pis" as="processing-instruction()*"/>
  <xsl:param name="pimap" as="map(*)"/>
  
  <xsl:choose>
    <xsl:when test="empty($pis)">
      <xsl:sequence select="$pimap"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:variable name="map"
                    select="fp:pi-pi-attributes($pimap, $pis[1], normalize-space($pis[1]))"/>
      <xsl:sequence select="fp:pi-attributes(subsequence($pis, 2), $map)"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>
  
<xsl:function name="fp:pi-pi-attributes" as="map(*)">
  <xsl:param name="pimap" as="map(*)"/>
  <xsl:param name="pi" as="processing-instruction()"/>
  <xsl:param name="text" as="xs:string?"/>

  <xsl:choose>
    <xsl:when test="matches($text, $vp:pi-match)">
      <xsl:variable name="aname" select="replace($text, $vp:pi-match, '$1')"/>
      <xsl:variable name="avalue" select="replace($text, $vp:pi-match, '$2')"/>
      <xsl:variable name="rest" select="replace($text, $vp:pi-match, '$3')"/>

      <xsl:variable name="avalue-resolved"
                    select="if ($aname = $v:pi-db-attributes-are-uris)
                            then tokenize(normalize-space($avalue), '\s+') ! resolve-uri(., base-uri($pi))
                            else $avalue"/>

      <xsl:variable name="list-value"
                    select="if (map:contains($pimap, $aname))
                            then (map:get($pimap, $aname), $avalue-resolved)
                            else $avalue-resolved"/>
      <xsl:sequence
          select="fp:pi-pi-attributes(map:put($pimap, $aname, $list-value), $pi, $rest)"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="$pimap"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<!-- ===================================================================================
|   Support for automatic glossary and glossary-collection
|=================================================================================== -->

<xsl:function name="fp:available-glossaries" as="document-node()" cache="yes">
  <xsl:param name="root" as="element()"/>
  <xsl:param name="collections" as="xs:string*"/>

  <xsl:variable name="gloss-uris" as="xs:string*">
    <xsl:sequence select="f:pi($root, 'glossary-collection')"/>
    <xsl:sequence select="$collections"/>
  </xsl:variable>

  <xsl:document>
    <!-- It doesn't *need* a single root element, but it feels cleaner -->
    <glossary-collection xmlns="http://docbook.org/ns/docbook">
      <xsl:sequence select="$root//db:glossary"/>
      <xsl:for-each select="tokenize(normalize-space(string-join($gloss-uris, ' ')), '\s+')">
        <xsl:try>
          <xsl:sequence select="doc(xs:anyURI(.))/db:glossary"/>
          <xsl:catch>
            <xsl:message select="'Failed to load glossary: ' || ."/>
            <xsl:message select="'    ' || $err:description"/>
          </xsl:catch>
        </xsl:try>
      </xsl:for-each>
    </glossary-collection>
  </xsl:document>
</xsl:function>

<xsl:function name="f:available-glossaries">
  <xsl:param name="term" as="element()"/>
  <xsl:sequence select="fp:available-glossaries(root($term)/*, ())"/>
</xsl:function>

<xsl:function name="f:available-glossaries">
  <xsl:param name="term" as="element()"/>
  <xsl:param name="collections" as="xs:string*"/>
  <xsl:sequence select="fp:available-glossaries(root($term)/*, $collections)"/>
</xsl:function>

<xsl:function name="f:glossentries" as="element(db:glossentry)*">
  <xsl:param name="term" as="element()"/>
  <xsl:sequence select="f:glossentries($term, ())"/>
</xsl:function>

<xsl:function name="f:glossentries" as="element(db:glossentry)*">
  <xsl:param name="term" as="element()"/>
  <xsl:param name="collections" as="xs:string*"/>

  <xsl:variable name="glossaries" select="f:available-glossaries($term, $collections)"/>

  <xsl:choose>
    <xsl:when test="$term/self::db:glossterm or $term/self::db:firstterm">
      <xsl:sequence select="key('glossary-entry', fp:baseform($term), $glossaries)"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:message
          select="'Warning: f:glossentries must not be called with '
                  || local-name($term) || ' as $term.'"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<xsl:function name="f:glossrefs" as="element()*">
  <xsl:param name="term" as="element()"/>
  <xsl:sequence select="f:glossrefs($term, root($term))"/>
</xsl:function>

<xsl:function name="f:glossrefs" as="element()*">
  <xsl:param name="term" as="element()"/>
  <xsl:param name="root" as="document-node()"/>
  <xsl:sequence select="key('glossary-ref', fp:baseform($term), $root)"/>
</xsl:function>

<xsl:function name="fp:baseform" as="xs:string">
  <xsl:param name="element" as="element()"/>
  <xsl:sequence select="($element/@baseform, string($element))[1] ! normalize-space(.)"/>
</xsl:function>

<!-- ===================================================================================
|   Support for automatic bibliography and bibliography-collection
|=================================================================================== -->

<xsl:function name="fp:available-bibliographies" as="document-node()" cache="yes">
  <xsl:param name="root" as="element()"/>
  <xsl:param name="collections" as="xs:string*"/>

  <xsl:variable name="bibl-uris" as="xs:string*">
    <xsl:sequence select="f:pi($root, 'bibliography-collection')"/>
    <xsl:sequence select="$collections"/>
  </xsl:variable>

  <xsl:document>
    <!-- It doesn't *need* a single root element, but it feels cleaner -->
    <bibliography-collection xmlns="http://docbook.org/ns/docbook">
      <!-- for internal bibliographies, don't include the empty entries -->
      <xsl:apply-templates select="$root//db:bibliography" mode="mp:strip-empty-biblioentries"/>
      <xsl:for-each select="tokenize(normalize-space(string-join($bibl-uris, ' ')), '\s+')">
        <xsl:try>
          <xsl:sequence select="doc(xs:anyURI(.))/db:bibliography"/>
          <xsl:catch>
            <xsl:message select="'Failed to load bibliography: ' || ."/>
            <xsl:message select="'    ' || $err:description"/>
          </xsl:catch>
        </xsl:try>
      </xsl:for-each>
    </bibliography-collection>
  </xsl:document>
</xsl:function>

<xsl:mode name="mp:strip-empty-biblioentries" on-no-match="shallow-copy"/>
<xsl:template match="db:bibliomixed[empty(node() except db:abbrev)]
                     |db:biblioentry[empty(node() except db:abbrev)]"
              mode="mp:strip-empty-biblioentries">
  <!-- discard -->
</xsl:template>

<xsl:function name="f:available-bibliographies">
  <xsl:param name="term" as="element()"/>
  <xsl:sequence select="fp:available-bibliographies(root($term)/*, ())"/>
</xsl:function>

<xsl:function name="f:available-bibliographies">
  <xsl:param name="term" as="element()"/>
  <xsl:param name="collections" as="xs:string*"/>
  <xsl:sequence select="fp:available-bibliographies(root($term)/*, $collections)"/>
</xsl:function>

<xsl:function name="f:biblioentries" as="element()*">
  <xsl:param name="term" as="element()"/>
  <xsl:sequence select="f:biblioentries($term, ())"/>
</xsl:function>

<xsl:function name="f:biblioentries" as="element()*">
  <xsl:param name="term" as="element()"/>
  <xsl:param name="collections" as="xs:string*"/>

  <xsl:variable name="bibliographies" select="f:available-bibliographies($term, $collections)"/>

  <xsl:choose>
    <xsl:when test="$term/self::db:citation or $term/self::db:abbrev">
      <xsl:sequence select="key('bibliography-entry', normalize-space($term), $bibliographies)"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:message
          select="'Warning: f:biblioentries must not be called with '
                  || local-name($term) || ' as $term.'"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<xsl:function name="f:citations" as="element()*">
  <xsl:param name="term" as="element()"/>
  <xsl:sequence select="f:citations($term, root($term))"/>
</xsl:function>

<xsl:function name="f:citations" as="element()*">
  <xsl:param name="term" as="element()"/>
  <xsl:param name="root" as="document-node()"/>
  <xsl:sequence select="key('citation', normalize-space($term), $root)"/>
</xsl:function>

</xsl:stylesheet>
