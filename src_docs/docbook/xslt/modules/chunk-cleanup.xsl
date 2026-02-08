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
                xmlns:v="http://docbook.org/ns/docbook/variables"
                xmlns:vp="http://docbook.org/ns/docbook/variables/private"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml"
                default-mode="m:chunk-cleanup"
                exclude-result-prefixes="#all"
                version="3.0">

<xsl:key name="hid" match="*" use="@id"/>
<xsl:key name="hfootnote" match="h:db-footnote" use="@id"/>
<xsl:key name="hanno" match="h:db-annotation" use="@id"/>

<xsl:mode name="m:chunk-cleanup" on-no-match="shallow-copy"/>

<xsl:template match="/">
  <xsl:param name="docbook" as="document-node()" tunnel="yes" required="yes"/>
  <xsl:document>
    <xsl:apply-templates/>
  </xsl:document>
</xsl:template>

<xsl:template match="h:db-footnote|h:db-annotation|h:head
                     |h:db-annotation-script|h:db-xlink-script
                     |h:db-toc-script|h:db-pagetoc-script|h:db-fallback-script
                     |h:db-copy-verbatim-script
                     |h:db-mathml-script|h:db-script">
  <!-- discard -->
</xsl:template>

<xsl:template match="h:db-annotation-marker">
  <xsl:if test="not(@placement = 'before')">
    <xsl:apply-templates/>
  </xsl:if>
</xsl:template>

<xsl:template match="*[@db-chunk]" priority="10">
  <xsl:param name="docbook" as="document-node()" tunnel="yes"/>

  <xsl:variable name="self" select="."/>

  <!-- Saxonica bug #4632 -->
  <xsl:sequence select="base-uri(root(.)/*)[. = '-no match-']"/>

  <xsl:if test="'chunk-cleanup' = $v:debug">
    <xsl:message select="'Chunk cleanup:', local-name(.), @db-chunk/string()"/>
  </xsl:if>

  <xsl:variable name="footnotes" as="element(h:db-footnote)*">
    <xsl:for-each select=".//h:db-footnote[not(ancestor::h:table)
                                           or ancestor::h:table[contains-token(@class,'verbatim')]]">
      <xsl:variable name="chunk" select="ancestor::*[@db-chunk][1]"/>
      <xsl:if test="$chunk is $self">
        <xsl:sequence select="."/>
      </xsl:if>
    </xsl:for-each>
  </xsl:variable>

  <xsl:variable name="annotations" as="xs:string*">
    <xsl:for-each select=".//h:db-annotation-marker">
      <xsl:variable name="chunk" select="ancestor::*[@db-chunk][1]"/>
      <xsl:if test="$chunk is $self">
        <xsl:sequence select="@target/string()"/>
      </xsl:if>
    </xsl:for-each>
  </xsl:variable>

  <xsl:variable name="annotations" select="distinct-values($annotations)"/>

  <xsl:variable name="head" select="/h:html/h:head"/>

  <xsl:variable name="heads" as="element(h:head)+">
    <xsl:for-each select="ancestor-or-self::*">
      <xsl:sequence select="h:head"/>
    </xsl:for-each>
  </xsl:variable>

  <!-- If the chunk selected for previous or next navigation is an
       annotation and annotations are being handled by JavaScript,
       then disregard the chunk identified. -->
  <xsl:variable name="prev"
                select="(ancestor::*[@db-chunk and fp:navigable(.)][1]
                         |preceding::*[@db-chunk and fp:navigable(.)][1])[last()]"/>
  <xsl:variable name="prev" as="element()?">
    <xsl:choose>
      <xsl:when test="$annotation-style = 'javascript'
                      and $prev/*[contains-token(@class, 'annotation-body')]">
        <xsl:sequence select="()"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$prev"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="next"
                select="((.//*[@db-chunk and fp:navigable(.)])[1]
                          |following::*[@db-chunk and fp:navigable(.)][1])[1]"/>
  <xsl:variable name="next" as="element()?">
    <xsl:choose>
      <xsl:when test="$annotation-style = 'javascript'
                      and $next/*[contains-token(@class, 'annotation-body')]">
        <xsl:sequence select="()"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$next"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="up"
                select="ancestor::*[@db-chunk and fp:navigable(.)][1]"/>
  <xsl:variable name="top"
                select="ancestor::*[@db-chunk and fp:navigable(.)][last()]"/>

  <xsl:variable name="rbu" select="fp:root-base-uri(.)"/>
  <xsl:variable name="cbu" select="fp:chunk-output-filename(.)"/>

  <xsl:variable name="classes" 
                select="if (@db-chunk = '')
                        then tokenize(*/@class)
                        else tokenize(@class)"/>
  <xsl:variable name="pagetoc"
                select="$classes = $vp:pagetoc-elements"/>

  <xsl:variable name="scripts" as="element(h:script)*">
    <xsl:if test="exists(.//mml:*)"
            xmlns:mml="http://www.w3.org/1998/Math/MathML">
      <xsl:apply-templates select="/h:html/h:db-mathml-script/*">
        <xsl:with-param name="rootbaseuri" select="$rbu"/>
        <xsl:with-param name="chunkbaseuri" select="$cbu"/>
      </xsl:apply-templates>
    </xsl:if>

    <!-- We save the annotation-style on the root div -->
    <xsl:if test="exists($annotations) and $annotation-style = 'javascript'">
      <xsl:apply-templates select="/h:html/h:db-annotation-script/*">
        <xsl:with-param name="rootbaseuri" select="$rbu"/>
        <xsl:with-param name="chunkbaseuri" select="$cbu"/>
      </xsl:apply-templates>
    </xsl:if>

    <!-- We save the xlink-style on the root div -->
    <xsl:if test="/h:html/h:div/@db-xlink/string() = 'javascript'">
      <xsl:apply-templates select="/h:html/h:db-xlink-script/*">
        <xsl:with-param name="rootbaseuri" select="$rbu"/>
        <xsl:with-param name="chunkbaseuri" select="$cbu"/>
      </xsl:apply-templates>
    </xsl:if>

    <xsl:if test="f:is-true($persistent-toc)">
      <xsl:apply-templates select="/h:html/h:db-toc-script/*">
        <xsl:with-param name="rootbaseuri" select="$rbu"/>
        <xsl:with-param name="chunkbaseuri" select="$cbu"/>
      </xsl:apply-templates>
    </xsl:if>

    <xsl:if test="$pagetoc">
      <xsl:apply-templates select="/h:html/h:db-pagetoc-script/*">
        <xsl:with-param name="rootbaseuri" select="$rbu"/>
        <xsl:with-param name="chunkbaseuri" select="$cbu"/>
      </xsl:apply-templates>
    </xsl:if>

    <xsl:if test=".//h:video|.//h:audio">
      <xsl:apply-templates select="/h:html/h:db-fallback-script/*">
        <xsl:with-param name="rootbaseuri" select="$rbu"/>
        <xsl:with-param name="chunkbaseuri" select="$cbu"/>
      </xsl:apply-templates>
    </xsl:if>

    <!-- The copy-verbatim script doesn't handle the highlight.js markup
         and highlight.js manages the selection correctly anyway. -->
    <xsl:if test="f:is-true($verbatim-embellishments)
                  and f:global-syntax-highlighter($docbook) = 'pygments'
                  and .//h:div[contains-token(@class, 'pre-wrap')]">
      <xsl:apply-templates select="/h:html/h:db-copy-verbatim-script/*">
        <xsl:with-param name="rootbaseuri" select="$rbu"/>
        <xsl:with-param name="chunkbaseuri" select="$cbu"/>
      </xsl:apply-templates>
    </xsl:if>

    <!-- Unconditionally add h:db-script children. -->
    <xsl:apply-templates select="/h:html/h:db-script/*">
      <xsl:with-param name="rootbaseuri" select="$rbu"/>
      <xsl:with-param name="chunkbaseuri" select="$cbu"/>
    </xsl:apply-templates>
  </xsl:variable>

  <!-- class=no-js is a hook for setting CSS styles when js isn't
       available; see the script element a few lines below. -->
  <html class="no-js" db-chunk="{fp:chunk-output-filename(.)}">
    <xsl:if test="normalize-space($default-theme) ne ''">
      <xsl:attribute name="class" select="$default-theme"/>
    </xsl:if>
    <head>
      <!-- When serialized, this always comes first, so make sure it's first
           here. (It doesn't really matter in practice, but the XSpec tests
           will fail if it isn't also first here.) -->
      <xsl:variable name="ctype" select="$head/h:meta[@http-equiv='Content-Type']"/>
      <xsl:apply-templates select="$ctype"/>

      <!-- If js is available, turn that no-js class into a js class, per
           https://www.paulirish.com/2009/avoiding-the-fouc-v3/ -->
      <script>
        <xsl:text>(function(H){H.className=H.className.replace(/\bno-js\b/,'js')})</xsl:text>
        <xsl:text>(document.documentElement)</xsl:text>
      </script>

      <xsl:variable name="title" select="$head/h:title"/>
      <title>
        <!-- because value-of introduces extra spaces -->
        <xsl:sequence select="f:chunk-title(.) ! ./descendant-or-self::text()"/>
      </title>

      <xsl:apply-templates select="$head/node() except ($ctype|$title)">
        <xsl:with-param name="rootbaseuri" select="$rbu"/>
        <xsl:with-param name="chunkbaseuri" select="$cbu"/>
      </xsl:apply-templates>

      <xsl:if test="$prev">
        <link rel="prev" href="{fp:relative-link(., $prev)}"/>
      </xsl:if>
      <xsl:if test="$next">
        <link rel="next" href="{fp:relative-link(., $next)}"/>
      </xsl:if>
      <xsl:if test="$up">
        <link rel="up" href="{fp:relative-link(., $up)}"/>
      </xsl:if>
      <xsl:if test="$top">
        <link rel="home" href="{fp:relative-link(., $top)}"/>
      </xsl:if>

      <xsl:sequence select="$scripts[not(@type) or contains(@type,'javascript')]"/>
      <xsl:apply-templates select="$heads[position() gt 1]/node()">
        <xsl:with-param name="rootbaseuri" select="$rbu"/>
        <xsl:with-param name="chunkbaseuri" select="$cbu"/>
      </xsl:apply-templates>
    </head>
    <body>
      <xsl:variable name="class-list" as="xs:string*">
        <xsl:if test="parent::h:html">
          <xsl:sequence select="'home'"/>
        </xsl:if>
        <xsl:sequence select="$docbook/*/@status/string()"/>
        <xsl:sequence select="tokenize(@class, '\s+')"/>
      </xsl:variable>
      <xsl:if test="exists($class-list)">
        <xsl:attribute name="class"
                       select="normalize-space(string-join(distinct-values($class-list), ' '))"/>
      </xsl:if>

      <nav class="top">
        <xsl:if test="(empty(@db-navigation)
                       or f:is-true(@db-navigation))
                      and (empty(@db-top-navigation)
                           or f:is-true(@db-top-navigation))">
          <xsl:call-template name="t:top-nav">
            <xsl:with-param name="chunk" select="exists($chunk)"/>
            <xsl:with-param name="node" select="$self"/>
            <xsl:with-param name="prev" select="$prev"/>
            <xsl:with-param name="next" select="$next"/>
            <xsl:with-param name="up" select="$up"/>
            <xsl:with-param name="top" select="$top"/>
          </xsl:call-template>
        </xsl:if>
      </nav>

      <xsl:variable name="main" as="element()">
        <main>
          <xsl:copy>
            <xsl:apply-templates select="@*,node()">
              <xsl:with-param name="rootbaseuri" select="$rbu" tunnel="yes"/>
              <xsl:with-param name="chunkbaseuri" select="$cbu" tunnel="yes"/>
            </xsl:apply-templates>
          </xsl:copy>

          <xsl:if test="$footnotes or exists($annotations)">
            <footer>
              <xsl:if test="$footnotes">
                <xsl:call-template name="t:chunk-footnotes">
                  <xsl:with-param name="footnotes" select="$footnotes"/>
                </xsl:call-template>
              </xsl:if>

              <xsl:if test="exists($annotations)">
                <xsl:variable name="style"
                              select="key('hanno', $annotations[1])[1]/@style/string()"/>
                <div class="annotations">
                  <div class="annotation-wrapper title"
                       >Annotations</div>
                  <xsl:for-each select="$annotations">
                    <xsl:apply-templates select="key('hanno', ., root($self))/node()"
                                         mode="m:docbook"/>
                  </xsl:for-each>
                </div>
              </xsl:if>
            </footer>
          </xsl:if>
        </main>
      </xsl:variable>

      <xsl:choose>
        <xsl:when test="$pagetoc">
          <div class="pagebody">
            <xsl:sequence select="$main"/>
            <nav class="pagetoc">
              <div class="tocwrapper">
              </div>
            </nav>
          </div>
        </xsl:when>
        <xsl:otherwise>
          <xsl:sequence select="$main"/>
        </xsl:otherwise>
      </xsl:choose>

      <nav class="bottom">
        <xsl:if test="(empty(@db-navigation)
                       or f:is-true(@db-navigation))
                      and (empty(@db-bottom-navigation)
                           or f:is-true(@db-bottom-navigation))">
          <xsl:call-template name="t:bottom-nav">
            <xsl:with-param name="chunk" select="exists($chunk)"/>
            <xsl:with-param name="node" select="$self"/>
            <xsl:with-param name="prev" select="$prev"/>
            <xsl:with-param name="next" select="$next"/>
            <xsl:with-param name="up" select="$up"/>
            <xsl:with-param name="top" select="$top"/>
          </xsl:call-template>
        </xsl:if>
      </nav>

      <xsl:sequence select="$scripts[@type and not(contains(@type,'javascript'))]"/>

      <xsl:variable name="more-scripts" as="element()*">
        <xsl:apply-templates select="." mode="m:html-body-script">
          <xsl:with-param name="rootbaseuri" select="$rbu"/>
          <xsl:with-param name="chunkbaseuri" select="$cbu"/>
        </xsl:apply-templates>
      </xsl:variable>

      <xsl:apply-templates select="$more-scripts">
        <xsl:with-param name="rootbaseuri" select="$rbu"/>
        <xsl:with-param name="chunkbaseuri" select="$cbu"/>
      </xsl:apply-templates>
    </body>
  </html>
</xsl:template>

<xsl:template name="t:chunk-footnotes">
  <xsl:param name="footnotes" as="element()*"/>

  <div class="footnotes">
    <hr/>
    <xsl:for-each select="$footnotes">
      <xsl:apply-templates select="./node()"/>
    </xsl:for-each>
  </div>
</xsl:template>

<xsl:function name="fp:chunk-output-filename" as="xs:anyURI" cache="yes">
  <xsl:param name="node" as="element()"/>

  <xsl:variable name="pchunk" select="$node/ancestor::*[@db-chunk][1]"/>

  <xsl:variable name="fn" as="xs:anyURI">
    <xsl:choose>
      <xsl:when test="exists($pchunk)">
        <xsl:sequence
            select="fp:resolve-uri($node/@db-chunk,
                                fp:chunk-output-filename($pchunk))"/>
      </xsl:when>
      <xsl:when test="not($v:chunk)">
        <xsl:sequence select="base-uri(root($node)/*)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence
            select="fp:resolve-uri($node/@db-chunk,
                                $vp:chunk-output-base-uri)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <!--<xsl:message select="'COF:', $fn"/>-->
  <xsl:sequence select="$fn"/>
</xsl:function>

<xsl:template match="h:head/h:link[@href]">
  <xsl:param name="rootbaseuri" as="xs:anyURI" required="yes"/>
  <xsl:param name="chunkbaseuri" as="xs:anyURI" required="yes"/>

  <xsl:choose>
    <xsl:when test="ends-with(@href, 'css/pygments.css')
                    and empty(../..//h:div[contains-token(@class, 'highlight')])">
      <!-- We don't need this one. Note that this is a slightly
           under-zealous test. It will preserve the pygments
           stylesheet in all of the ancestors of a chunk that
           has a highlighted listing. -->
    </xsl:when>
    <xsl:otherwise>
      <xsl:copy>
        <xsl:attribute name="href"
                       select="fp:relative-uri($rootbaseuri, $chunkbaseuri, @href)"/>
        <xsl:apply-templates select="@* except @href"/>
        <xsl:apply-templates select="node()"/>
      </xsl:copy>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="h:script[@src]">
  <xsl:param name="rootbaseuri" as="xs:anyURI" required="yes"/>
  <xsl:param name="chunkbaseuri" as="xs:anyURI" required="yes"/>

  <xsl:copy>
    <xsl:attribute name="src"
                   select="fp:relative-uri($rootbaseuri, $chunkbaseuri, @src)"/>
    <xsl:apply-templates select="@* except @src"/>
    <xsl:apply-templates select="node()"/>
  </xsl:copy>
</xsl:template>

<!-- Helper function to alternate Saxon's resolve-uri for file:/ normalization -->
<xsl:function name="fp:resolve-uri" as="xs:anyURI">
  <xsl:param name="href" as="xs:string" required="yes"/>
  <xsl:param name="baseuri" as="xs:string" required="yes"/>
  <xsl:variable name="uri" as="xs:string">
    <xsl:sequence select="resolve-uri($href, $baseuri)"/>
  </xsl:variable>
  <xsl:variable name="fixed-uri" as="xs:string">
    <xsl:choose>
      <!-- Handle file: with no slashes -->
      <xsl:when test="$uri = 'file:'">
        <xsl:sequence select="'file:///'"/>
      </xsl:when>
      <!-- Handle file:/ (single slash) -->
      <xsl:when test="starts-with($uri, 'file:/') and not(starts-with($uri, 'file://'))">
        <xsl:sequence select="replace($uri, '^file:/', 'file:///')"/>
      </xsl:when>
      <!-- Handle file:// (double slash) -->
      <xsl:when test="starts-with($uri, 'file://') and not(starts-with($uri, 'file:///'))">
        <xsl:sequence select="replace($uri, '^file://', 'file:///')"/>
      </xsl:when>
      <!-- Already correct or not a file URI -->
      <xsl:otherwise>
        <xsl:sequence select="$uri"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:sequence select="xs:anyURI($fixed-uri)"/>
</xsl:function>

<xsl:function name="fp:relative-uri" as="xs:string">
  <xsl:param name="rootbaseuri" as="xs:string" required="yes"/>
  <xsl:param name="chunkbaseuri" as="xs:string" required="yes"/>
  <xsl:param name="href" as="xs:string" required="yes"/>

  <xsl:variable name="absuri"
                select="if ($v:chunk)
                        then fp:resolve-uri($href, $rootbaseuri)
                        else $rootbaseuri"/>

  <xsl:variable name="rchunk"
                select="fp:trim-common-prefix($chunkbaseuri, $absuri)"/>

  <xsl:choose>
    <!-- Attempt to leave absolute path references alone -->
    <xsl:when test="starts-with($href, '/')">
      <xsl:sequence select="$href"/>
    </xsl:when>

    <!-- if $rchunk = $chunkbaseuri, they have no common prefix -->
    <!-- if $rchunk doesn't contain a /, then it's at the root -->
    <xsl:when test="($rchunk ne $chunkbaseuri) and contains($rchunk, '/')">
      <xsl:variable name="rhref"
                    select="fp:trim-common-prefix($absuri, $chunkbaseuri)"/>
      <xsl:variable name="parts" as="xs:string+">
        <xsl:for-each select="2 to count(tokenize($rchunk, '/'))">
          <xsl:sequence select="'..'"/>
        </xsl:for-each>
      </xsl:variable>
      <xsl:sequence select="string-join($parts, '/') || '/' || $rhref"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="$href"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<xsl:template match="h:a[starts-with(@href, '#')]">
  <xsl:variable name="id" select="substring-after(@href, '#')"/>
  <xsl:variable name="target" select="key('hid', $id)"/>
  <xsl:variable name="pchunk" select="(ancestor::*[@db-chunk])[last()]"/>
  <xsl:variable name="tchunk" select="($target/ancestor-or-self::*[@db-chunk])[last()]"/>

  <xsl:if test="$target[1]/@db-chunk and count($target) != 1">
    <xsl:choose>
      <xsl:when test="count($target) = 0">
        <xsl:if test="$message-level gt 0">
          <xsl:message select="'Error: cannot find ' || $id || ' in document'"/>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <xsl:if test="$message-level gt 0">
          <xsl:message select="'Error: multiple elements match ' || $id || ' in document'"/>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:if>

  <xsl:choose>
    <xsl:when test="empty($target)">
      <xsl:if test="$message-level gt 0">
        <xsl:message select="'No id for #' || $id"/>
      </xsl:if>
      <span class="error broken-link">
        <xsl:copy>
          <xsl:apply-templates select="@*,node()"/>
        </xsl:copy>
      </span>
    </xsl:when>
    <xsl:when test="contains-token(@class, 'annomark')">
      <!-- Annotations are special, they're always local references because
           we'll copy the relevant annotations into this chunk. -->
      <xsl:if test="'intra-chunk-refs' = $v:debug">
        <xsl:message select="'Link:', @href/string(), 'is an annotation'"/>
      </xsl:if>
      <a>
        <xsl:copy-of select="@* except @db-annotation"/>
        <xsl:apply-templates/>
      </a>
    </xsl:when>
    <xsl:when test="$pchunk is $tchunk">
      <xsl:if test="'intra-chunk-refs' = $v:debug">
        <xsl:message select="'Link:', @href/string(), 'in same chunk as target'"/>
      </xsl:if>
      <a>
        <xsl:copy-of select="@*"/>
        <xsl:apply-templates/>
      </a>
    </xsl:when>
    <xsl:when test="ancestor::*[@db-persistent-toc]">
      <!-- ignore it, we'll clean up the persistent ToC later -->
      <xsl:sequence select="."/>
    </xsl:when>
    <xsl:when test="$tchunk/@id = $id">
      <xsl:if test="'intra-chunk-refs' = $v:debug">
        <xsl:message select="'Link:', @href/string(),
                             'to root of', $tchunk/@db-chunk/string()"/>
      </xsl:if>
      <a href="{fp:relative-link($pchunk, $tchunk)}">
        <xsl:copy-of select="@* except @href"/>
        <xsl:apply-templates/>
      </a>
    </xsl:when>
    <xsl:otherwise>
      <xsl:if test="'intra-chunk-refs' = $v:debug">
        <xsl:message select="'Link:', @href/string(),
                             'in chunk', $tchunk/@db-chunk/string()"/>
      </xsl:if>
      <a href="{fp:relative-link($pchunk, $tchunk)}{@href}">
        <xsl:copy-of select="@* except @href"/>
        <xsl:apply-templates/>
      </a>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="h:sup[@db-footnote]">
  <xsl:copy>
    <xsl:apply-templates select="@* except @db-footnote"/>
    <xsl:apply-templates/>
  </xsl:copy>
</xsl:template>

<!-- If we're renumbering footnotes and this is a text node in an h:a that's
     a child of an h:sup that identifies a footnote and there are no other
     nodes inside this h:a, then we'll try to renumber the footnote. -->
<xsl:template match="text()[$v:chunk-renumber-footnotes
                            and parent::h:a
                                /parent::h:sup[contains-token(@class, 'footnote-number')
                                               and not(contains-token(@class, 'table-footnote'))
                                               and @db-footnote]
                            and empty(preceding-sibling::node())
                            and empty(following-sibling::node())]"
              priority="100">
  <xsl:variable name="id" select="substring-after(../@href, '#')"/>
  <xsl:variable name="renumber" as="xs:string">
    <xsl:choose>
      <xsl:when test="key('hfootnote', $id)">
        <xsl:apply-templates select="key('hfootnote', $id)"
                             mode="mp:footnote-renumber"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- this must be the number in the footnote itself... -->
        <xsl:apply-templates select="ancestor::h:db-footnote[1]"
                             mode="mp:footnote-renumber"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:sequence select="$renumber"/>
</xsl:template>

<xsl:template match="h:img/@src
                     |h:video/h:source/@src
                     |h:audio/h:source/@src
                     |h:iframe/@src
                     |h:audio//h:a/@href
                     |h:video//h:a/@href
                     |h:source/@srcset">
  <xsl:param name="rootbaseuri" tunnel="yes"/>
  <xsl:param name="chunkbaseuri" tunnel="yes"/>

  <xsl:variable name="uri" as="xs:string">
    <xsl:choose>
      <xsl:when test="exists(f:uri-scheme(.)) and f:uri-scheme(.) != 'file'">
        <xsl:sequence select="."/>
      </xsl:when>
      <xsl:when test="exists($mediaobject-output-base-uri)">
        <xsl:choose>
          <xsl:when test="f:is-true($mediaobject-output-paths)">
            <xsl:sequence
                select="fp:relative-uri($rootbaseuri, $chunkbaseuri, '')
                        || $mediaobject-output-base-uri
                        || ."/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:sequence
                select="fp:relative-uri($rootbaseuri, $chunkbaseuri, '')
                        || $mediaobject-output-base-uri
                        || tokenize(., '/')[last()]"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="true()">
        <xsl:sequence
            select="fp:relative-uri($rootbaseuri, $chunkbaseuri, '') || ."/>
      </xsl:when>
      <xsl:otherwise>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="adjusted-uri" as="xs:string">
    <xsl:apply-templates select="." mode="m:mediaobject-output-adjust">
      <xsl:with-param name="adjusted-uri" select="$uri"/>
    </xsl:apply-templates>
  </xsl:variable>

  <xsl:message use-when="'mediaobject-uris' = $v:debug">
    <xsl:text>Cleanup </xsl:text>
    <xsl:value-of
        select="substring(local-name((parent::h:img,
                                      parent::h:iframe,
                                      parent::h:a,
                                      ../..)[1]), 1, 3)"/>
    <xsl:choose>
      <xsl:when test="$uri = $adjusted-uri">
        <xsl:value-of select="': ' || . || ' &#9;→ ' || $adjusted-uri"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="': ' || . || ' &#9;→ ' || $uri || ' → &#9;' || $adjusted-uri"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:message>

  <xsl:attribute name="{local-name(.)}" select="$adjusted-uri"/>
</xsl:template>

<xsl:template match="@*" mode="m:mediaobject-output-adjust">
  <xsl:param name="adjusted-uri" as="xs:string"/>
  <xsl:sequence select="$adjusted-uri"/>
</xsl:template>

<xsl:template match="element()">
  <xsl:copy>
    <xsl:apply-templates select="@*"/>
    <!-- annotations inside the constructed db-bfs div are moved to the top
         so ignore them when we're *inside* the db-bfs div -->
    <xsl:if test="not(contains-token(@class, 'db-bfs'))">
      <xsl:sequence select="h:db-annotation-marker[@placement='before']/node()
                            |h:div[@class='db-bfs']/h:db-annotation-marker[@placement='before']/node()"/>
    </xsl:if>
    <xsl:apply-templates select="node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="attribute()|text()|comment()|processing-instruction()">
  <xsl:copy/>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="*" mode="mp:footnote-renumber">
  <xsl:sequence
      select="error($dbe:INTERNAL-RENUMBER-ERROR,
                    'Attempt to renumber ' || local-name(.)
                    || ': ' || @id/string())"/>
</xsl:template>

<xsl:template match="h:db-footnote" mode="mp:footnote-renumber">
  <xsl:variable name="new-number" as="xs:string">
    <xsl:number from="*[@db-chunk]"
                count="h:db-footnote[not(ancestor::h:table)
                                     or ancestor::h:table[contains-token(@class, 'verbatim')]]"
                level="any"/>
  </xsl:variable>
  <xsl:sequence select="fp:footnote-mark(xs:integer($new-number), $footnote-numeration)"/>
</xsl:template>

<!-- ============================================================ -->

<xsl:function name="fp:relative-link" as="xs:string">
  <xsl:param name="source" as="element()"/>
  <xsl:param name="target" as="element()"/>

  <xsl:variable name="suri" 
                select="fp:chunk-output-filename($source)"/>
  <xsl:variable name="turi"
                select="fp:chunk-output-filename($target)"/>

  <xsl:variable name="tsuri" select="fp:trim-common-prefix($suri, $turi)"/>
  <xsl:variable name="tturi" select="fp:trim-common-prefix($turi, $suri)"/>

  <xsl:variable name="path" as="xs:string">
    <xsl:choose>
      <xsl:when test="contains($tsuri, '/')">
        <xsl:variable name="parts" as="xs:string+">
          <xsl:for-each select="2 to count(tokenize($tsuri, '/'))">
            <xsl:sequence select="'..'"/>
          </xsl:for-each>
        </xsl:variable>
        <xsl:sequence select="string-join($parts, '/') || '/' || $tturi"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$tturi"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:if test="'intra-chunk-links' = $v:debug">
    <xsl:message select="$tsuri,'→',$tturi,':',$path"/>
  </xsl:if>

  <xsl:sequence select="$path"/>
</xsl:function>

<xsl:function name="fp:trim-common-prefix" as="xs:string" cache="yes">
  <xsl:param name="source" as="xs:string"/>
  <xsl:param name="target" as="xs:string"/>

  <xsl:variable name="tail"
                select="fp:trim-common-parts(
                           tokenize($source, '/'),
                           tokenize($target, '/'))"/>

  <xsl:sequence select="string-join($tail, '/')"/>
</xsl:function>

<xsl:function name="fp:trim-common-parts" as="xs:string*">
  <xsl:param name="source" as="xs:string*"/>
  <xsl:param name="target" as="xs:string*"/>

  <xsl:choose>
    <xsl:when test="empty($source) or empty($target)">
      <xsl:sequence select="$source"/>
    </xsl:when>
    <xsl:when test="$source[1] ne $target[1]">
      <xsl:sequence select="$source"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="fp:trim-common-parts(subsequence($source, 2),
                                                 subsequence($target, 2))"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<xsl:function name="fp:root-base-uri" as="xs:anyURI" cache="yes">
  <xsl:param name="node" as="element()"/>
  <!-- Saxonica bug #4632 -->
  <xsl:sequence select="base-uri(root($node)/*)[. = '-no match-']"/>
  <xsl:choose>
    <xsl:when test="not($v:chunk)">
      <xsl:sequence select="base-uri(root($node)/*)"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="$vp:chunk-output-base-uri"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<!-- ============================================================ -->

<xsl:function name="f:chunk-title" as="node()*" cache="yes">
  <xsl:param name="chunk" as="element()?"/>

  <xsl:choose>
    <xsl:when test="$chunk/h:div[contains-token(@class, 'refnamediv')]">
      <!-- refentry chunks are special -->
      <xsl:apply-templates
          select="(($chunk/h:div[contains-token(@class, 'refnamediv')])[1]
                   //h:span[contains-token(@class, 'refname')])[1]/node()"
          mode="m:chunk-title"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:variable name="header" select="($chunk/h:header
                                           |$chunk/*/h:header
                                           |$chunk/*/*/h:header)[1]"/>

      <xsl:variable name="hx" select="(($header//h:h1)[1],
                                       ($header//h:h2)[1],
                                       ($header//h:h3)[1],
                                       ($header//h:h4)[1],
                                       $header//h:h5)[1]"/>

      <xsl:apply-templates select="$hx/node()" mode="m:chunk-title"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<xsl:template match="h:db-footnote|h:db-annotation" mode="m:chunk-title"/>

<xsl:template match="h:a" mode="m:chunk-title">
  <xsl:apply-templates mode="m:chunk-title"/>
</xsl:template>

<xsl:template match="element()" mode="m:chunk-title">
  <xsl:copy>
    <xsl:apply-templates select="@*,node()" mode="m:chunk-title"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="attribute()|text()|comment()|processing-instruction()"
              mode="m:chunk-title">
  <xsl:copy/>
</xsl:template>

<!-- ============================================================ -->

<xsl:template name="t:top-nav">
  <xsl:param name="chunk" as="xs:boolean"/>
  <xsl:param name="node" as="element()"/>
  <xsl:param name="prev" as="element()?"/>
  <xsl:param name="next" as="element()?"/>
  <xsl:param name="up" as="element()?"/>
  <xsl:param name="top" as="element()?"/>

  <!-- We don't have the actual DocBook sources here, so ... -->
  <xsl:variable name="lang"
                select="($node/ancestor-or-self::*/@lang/string(), $default-language)[1]"/>

  <xsl:if test="$chunk">
    <div>
      <xsl:if test="$top">
        <a href="{fp:relative-link(., $top)}">
          <xsl:sequence select="fp:l10n-token($lang, 'nav-home')"/>
        </a>
      </xsl:if>
      <xsl:text> </xsl:text>
      <xsl:if test="$up">
        <a href="{fp:relative-link(., $up)}">
          <xsl:sequence select="fp:l10n-token($lang, 'nav-up')"/>
        </a>
      </xsl:if>
      <xsl:text> </xsl:text>
      <xsl:if test="$next">
        <a href="{fp:relative-link(., $next)}">
          <xsl:sequence select="fp:l10n-token($lang, 'nav-next')"/>
        </a>
      </xsl:if>
      <xsl:text> </xsl:text>
      <xsl:if test="$prev">
        <a href="{fp:relative-link(., $prev)}">
          <xsl:sequence select="fp:l10n-token($lang, 'nav-prev')"/>
        </a>
      </xsl:if>
    </div>
  </xsl:if>
</xsl:template>

<xsl:template name="t:bottom-nav">
  <xsl:param name="chunk" as="xs:boolean"/>
  <xsl:param name="node" as="element()"/>
  <xsl:param name="prev" as="element()?"/>
  <xsl:param name="next" as="element()?"/>
  <xsl:param name="up" as="element()?"/>
  <xsl:param name="top" as="element()?"/>

  <!-- We don't have the actual DocBook sources here, so ... -->
  <xsl:variable name="lang"
                select="($node/ancestor-or-self::*/@lang/string(), $default-language)[1]"/>

  <xsl:if test="$chunk">
    <table>
      <tr>
        <td class="previous">
          <xsl:if test="$prev">
            <a href="{fp:relative-link(., $prev)}">
              <xsl:sequence select="fp:l10n-token($lang, 'nav-prev')"/>
            </a>
          </xsl:if>
        </td>
        <td class="up">
          <xsl:if test="$up">
            <a href="{fp:relative-link(., $up)}">
              <xsl:sequence select="fp:l10n-token($lang, 'nav-up')"/>
            </a>
          </xsl:if>
        </td>
        <td class="next">
          <xsl:if test="$next">
            <a href="{fp:relative-link(., $next)}">
              <xsl:sequence select="fp:l10n-token($lang, 'nav-next')"/>
            </a>
          </xsl:if>
        </td>
      </tr>
      <tr>
        <td class="previous">
          <xsl:sequence select="f:chunk-title($prev)"/>
        </td>
        <td class="up">
          <xsl:if test="$top">
            <a href="{fp:relative-link(., $top)}">
              <xsl:sequence select="fp:l10n-token($lang, 'nav-home')"/>
            </a>
          </xsl:if>
        </td>
        <td class="next">
          <xsl:sequence select="f:chunk-title($next)"/>
        </td>
      </tr>
    </table>
  </xsl:if>
</xsl:template>

<!-- ============================================================ -->

<xsl:function name="fp:footnote-number" as="xs:integer" cache="yes">
  <xsl:param name="node" as="element(db:footnote)"/>
  <xsl:apply-templates select="$node" mode="mp:footnote-number"/>
</xsl:function>

<xsl:template match="db:footnote" as="xs:integer" mode="mp:footnote-number">
  <xsl:variable name="nearest"
                select="(ancestor::db:table
                        |ancestor::db:informaltable)[last()]"/>

  <xsl:variable name="fnum" as="xs:string">
    <xsl:choose>
      <xsl:when test="empty($nearest)">
        <xsl:variable name="pfoot" select="count(preceding::db:footnote)"/>
        <xsl:variable name="ptfoot"
              select="count(preceding::db:footnote[ancestor::db:table])
                      + count(preceding::db:footnote[ancestor::db:informaltable])"/>
        <xsl:value-of select="$pfoot - $ptfoot + 1"/>
      </xsl:when>
      <xsl:when test="$nearest/self::db:informaltable">
        <xsl:number format="1" from="db:informaltable" level="any"/>
      </xsl:when>
      <xsl:when test="$nearest/self::db:table">
        <xsl:number format="1" from="db:table" level="any"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:if test="$message-level gt 0">
          <xsl:message>Error: failed to enumerate footnote:</xsl:message>
          <xsl:message select="."/>
        </xsl:if>
        <xsl:sequence select="1"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:sequence select="xs:integer($fnum)"/>
</xsl:template>

<xsl:template match="db:footnote" mode="m:footnote-number">
  <xsl:variable name="nearest"
                select="(ancestor::db:table
                        |ancestor::db:informaltable)[last()]"/>

  <xsl:variable name="fnum" select="fp:footnote-number(.)"/>

  <xsl:variable name="marks"
                select="if (empty($nearest))
                        then $footnote-numeration
                        else $table-footnote-numeration"/>

  <xsl:sequence select="fp:footnote-mark($fnum, $marks)"/>
</xsl:template>

<xsl:function name="fp:footnote-mark" as="xs:string">
  <xsl:param name="number" as="xs:integer"/>
  <xsl:param name="marks" as="xs:string+"/>

  <xsl:choose>
    <xsl:when test="$number lt count($marks)">
      <xsl:sequence select="$marks[$number]"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:number value="$number" format="{$marks[count($marks)]}"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

<!-- ============================================================ -->

<xsl:function name="fp:navigable" as="xs:boolean" cache="yes">
  <xsl:param name="node" as="element()"/>
  <xsl:sequence select="$node/@db-chunk and not($node/@db-navigable='false')"/>
</xsl:function>

</xsl:stylesheet>
