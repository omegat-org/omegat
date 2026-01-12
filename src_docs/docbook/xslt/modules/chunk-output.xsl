<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:dbe="http://docbook.org/ns/docbook/errors"
                xmlns:f="http://docbook.org/ns/docbook/functions"
                xmlns:fp="http://docbook.org/ns/docbook/functions/private"
                xmlns:h="http://www.w3.org/1999/xhtml"
                xmlns:m="http://docbook.org/ns/docbook/modes"
                xmlns:mp="http://docbook.org/ns/docbook/modes/private"
                xmlns:t="http://docbook.org/ns/docbook/templates"
                xmlns:tp="http://docbook.org/ns/docbook/templates/private"
                xmlns:v="http://docbook.org/ns/docbook/variables"
                xmlns:vp="http://docbook.org/ns/docbook/variables/private"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml"
                default-mode="m:chunk-output"
                exclude-result-prefixes="#all"
                version="3.0">

<xsl:template match="/" as="map(xs:string, item()*)">
  <xsl:choose>
    <xsl:when test="$v:chunk">
      <xsl:map>
        <xsl:apply-templates select="/h:html//h:html">
          <xsl:with-param name="map" select="true()"/>
        </xsl:apply-templates>
        <xsl:if test="normalize-space($persistent-toc-filename) != '' and f:is-true($persistent-toc)">
          <xsl:variable name="rootfn"
                        select="(.//*[@db-chunk])[1]/@db-chunk/string()"/>
          <xsl:map-entry key="string(resolve-uri($persistent-toc-filename, $rootfn))">
            <html>
              <head>
                <xsl:sequence select="/h:html/h:html[1]/h:head/h:title"/>
              </head>
              <body>
                <xsl:call-template name="tp:resolve-persistent-toc-uris">
                  <xsl:with-param name="html" select="."/>
                </xsl:call-template>
              </body>
            </html>
          </xsl:map-entry>
        </xsl:if>
      </xsl:map>
    </xsl:when>
    <xsl:otherwise>
      <xsl:variable name="result">
        <xsl:choose>
          <xsl:when test="/h:html/h:html">
            <xsl:apply-templates select="/h:html/h:html"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:sequence select="."/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:sequence select="map {'output': $result}"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="h:html[@db-chunk]">
  <xsl:param name="map" as="xs:boolean" select="false()"/>

  <xsl:variable name="copy-attributes"
                select="@* except @*[starts-with(local-name(.), 'db-')]"/>

  <xsl:choose>
    <xsl:when test="not($v:chunk)">
      <xsl:copy>
        <xsl:apply-templates select="$copy-attributes"/>
        <xsl:apply-templates/>
      </xsl:copy>
    </xsl:when>
    <xsl:when test="not($map)"/>
    <xsl:when test="@db-chunk != ''">
      <xsl:if test="'chunks' = $v:debug">
        <xsl:message select="'Chunk:', @db-chunk/string()"/>
      </xsl:if>
      <xsl:map-entry key="@db-chunk/string()">
        <xsl:copy>
          <xsl:apply-templates select="$copy-attributes"/>
          <xsl:apply-templates/>
        </xsl:copy>
      </xsl:map-entry>
    </xsl:when>
    <xsl:otherwise>
      <xsl:copy>
        <xsl:apply-templates select="$copy-attributes"/>
        <xsl:apply-templates/>
      </xsl:copy>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="h:div[@db-chunk]">
  <xsl:variable name="copy-attributes"
                select="@* except @*[starts-with(local-name(.), 'db-')]"/>

  <xsl:choose>
    <xsl:when test="empty($copy-attributes)">
      <xsl:apply-templates/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:copy>
        <xsl:apply-templates select="$copy-attributes"/>
        <xsl:apply-templates/>
      </xsl:copy>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="tp:resolve-persistent-toc-uris">
  <xsl:param name="html" as="document-node()"/>
  <xsl:param name="context" as="element()?"/>

  <xsl:variable name="toc"
                select="fp:resolve-persistent-toc(/*/*[@db-persistent-toc])"/>

  <xsl:sequence select="$toc/h:div/h:div[contains-token(@class, 'toc')]/h:ul"/>
  <xsl:for-each select="$toc/h:div/h:div[not(contains-token(@class, 'toc'))]">
    <ul class="nav-title">
      <li class="nav-title">
        <xsl:sequence select="h:div[contains-token(@class, 'title')]/node()"/>
      </li>
      <xsl:sequence select="h:ul/h:li"/>
    </ul>
  </xsl:for-each>
</xsl:template>

<xsl:function name="fp:resolve-persistent-toc-prefix" as="xs:string?">
  <xsl:param name="html" as="document-node()"/>
  <xsl:param name="context" as="element()?"/>

  <!-- If this chunk and the "root" chunk are in different directories,
       work out what prefix (how many '../') is required to get back
       to the root level. Prefix all of the relative URIs in the ToC
       with that prefix so the links will work.
  -->
  <xsl:variable name="docroot" select="$html/h:html/h:html/@db-chunk/string()"/>
  <xsl:variable name="chroot"
                select="$context/ancestor-or-self::h:html[@db-chunk][1]/@db-chunk/string()"/>
  <xsl:variable name="rel"
                select="fp:trim-common-prefix(($chroot, '')[1], $docroot)"/>

  <xsl:variable name="parts" select="tokenize($rel, '/')"/>
  <xsl:variable name="ancestors" as="xs:string*">
    <xsl:for-each select="2 to count($parts)">
      <xsl:sequence select="'..'"/>
    </xsl:for-each>
  </xsl:variable>
  <xsl:sequence select="if (exists($ancestors))
                        then string-join($ancestors, '/') || '/'
                        else ()"/>
</xsl:function>

<xsl:template match="h:nav[contains-token(@class, 'bottom')]">
  <xsl:copy>
    <xsl:apply-templates select="@*,node()"/>

    <xsl:if test="f:is-true($persistent-toc)">
      <!-- N.B. This is cheating slightly. The completely clean way to do
           this would be to add the ToC elements during the chunk-cleanup pass,
           but on big document, that can result in tens of megabytes of extra
           data as the whole ToC is repeated in every chunk. Since we know what's
           in the ToC, we know it'll be safe to cheat...
      -->

      <nav class="tocopen"/>
      <nav class="toc"/>
      <xsl:comment> Hide ToC details from user agents that don’t support JS </xsl:comment>
      <script type="text/html" class="tocopen">
        <xsl:sequence select="$v:toc-open"/>
      </script>
      <script type="text/html" class="toc">
        <header>
          <span>
            <xsl:apply-templates select="." mode="m:gentext">
              <xsl:with-param name="group" select="'table-of-contents'"/>
            </xsl:apply-templates>
          </span>
          <span class="close">
            <xsl:sequence select="$v:toc-close"/>
          </span>
          <xsl:if test="$persistent-toc-search">
            <p class="ptoc-search">
              <input class="ptoc-search" placeholder="Search" style="width: 80%"/>
            </p>
          </xsl:if>
        </header>
        <xsl:choose>
          <xsl:when test="$v:chunk and normalize-space($persistent-toc-filename) != ''">
            <div db-persistent-toc="{$persistent-toc-filename}">
              <xsl:attribute name="db-prefix"
                             select="fp:resolve-persistent-toc-prefix(/, .)"/>
              <xsl:sequence select="f:l10n-token(., 'loading')"/>
            </div>
          </xsl:when>
          <xsl:otherwise>
            <div db-prefix="{fp:resolve-persistent-toc-prefix(/, .)}">
              <xsl:call-template name="tp:resolve-persistent-toc-uris">
                <xsl:with-param name="html" select="/"/>
                <xsl:with-param name="context" select="."/>
              </xsl:call-template>
            </div>
          </xsl:otherwise>
        </xsl:choose>
      </script>
    </xsl:if>
  </xsl:copy>
</xsl:template>

<xsl:function name="fp:resolve-persistent-toc" cache="yes" as="element(h:div)">
  <xsl:param name="toc" as="element(h:div)"/>
  <xsl:apply-templates select="$toc" mode="mp:copy-patch-toc"/>
</xsl:function>

<xsl:template match="element()">
  <xsl:copy>
    <xsl:apply-templates select="@*,node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="attribute()|text()|comment()|processing-instruction()"
             >
  <xsl:copy/>
</xsl:template>

<!-- ============================================================ -->

<xsl:mode name="mp:copy-patch-toc" on-no-match="shallow-copy"/>

<xsl:template match="h:a[@href]" mode="mp:copy-patch-toc">
  <xsl:variable name="href" as="xs:string">
    <xsl:choose>
      <xsl:when test="starts-with(@href, '#') and $v:chunk">
        <xsl:variable name="id" select="substring-after(@href, '#')"/>
        <xsl:variable name="target" select="key('hid', $id, root(.))"/>
        <xsl:variable name="chunk"
                      select="$target/ancestor-or-self::h:html[@db-chunk][1]"/>
        <xsl:variable name="href"
                      select="if ($chunk/h:body/h:main/h:div/@id = $id)
                              then ''
                              else @href/string()"/>

        <xsl:if test="count($target/ancestor-or-self::h:html[@db-chunk][1]/@db-chunk) gt 1">
          <xsl:message select="'Multiple chunks identified for ''' || $id || ''''"/>
        </xsl:if>

        <xsl:sequence
            select="substring-after(
                      $target/ancestor-or-self::h:html[@db-chunk][1]/@db-chunk/string(),
                      $vp:chunk-output-base-uri) || $href"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="@href/string()"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:copy>
    <xsl:copy-of select="@* except @href"/>
    <xsl:attribute name="href" select="$href"/>
    <xsl:apply-templates select="node()" mode="mp:copy-patch-toc"/>
  </xsl:copy>
</xsl:template>

<!-- ============================================================ -->

</xsl:stylesheet>
