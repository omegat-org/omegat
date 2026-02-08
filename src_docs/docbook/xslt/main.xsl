<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:f="http://docbook.org/ns/docbook/functions"
                xmlns:fp="http://docbook.org/ns/docbook/functions/private"
                xmlns:h="http://www.w3.org/1999/xhtml"
                xmlns:m="http://docbook.org/ns/docbook/modes"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                xmlns:mp="http://docbook.org/ns/docbook/modes/private"
                xmlns:t="http://docbook.org/ns/docbook/templates"
                xmlns:v="http://docbook.org/ns/docbook/variables"
                xmlns:vp="http://docbook.org/ns/docbook/variables/private"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml"
                default-mode="m:docbook"
                exclude-result-prefixes="#all"
                version="3.0">

<xsl:import href="param.xsl"/>

<xsl:include href="VERSION.xsl"/>
<xsl:include href="modules/variable.xsl"/>
<xsl:include href="modules/space.xsl"/>
<xsl:include href="modules/unhandled.xsl"/>
<xsl:include href="modules/errors.xsl"/>
<xsl:include href="modules/head.xsl"/>
<xsl:include href="modules/titles.xsl"/>
<xsl:include href="modules/numbers.xsl"/>
<xsl:include href="modules/units.xsl"/>
<xsl:include href="modules/gentext.xsl"/>
<xsl:include href="modules/l10n.xsl"/>
<xsl:include href="modules/functions.xsl"/>
<xsl:include href="modules/toc.xsl"/>
<xsl:include href="modules/divisions.xsl"/>
<xsl:include href="modules/components.xsl"/>
<xsl:include href="modules/refentry.xsl"/>
<xsl:include href="modules/bibliography.xsl"/>
<xsl:include href="modules/biblio690.xsl"/>
<xsl:include href="modules/glossary.xsl"/>
<xsl:include href="modules/index.xsl"/>
<xsl:include href="modules/sections.xsl"/>
<xsl:include href="modules/templates.xsl"/>
<xsl:include href="modules/titlepage.xsl"/>
<xsl:include href="modules/info.xsl"/>
<xsl:include href="modules/lists.xsl"/>
<xsl:include href="modules/blocks.xsl"/>
<xsl:include href="modules/admonitions.xsl"/>
<xsl:include href="modules/programming.xsl"/>
<xsl:include href="modules/msgset.xsl"/>
<xsl:include href="modules/objects.xsl"/>
<xsl:include href="modules/footnotes.xsl"/>
<xsl:include href="modules/verbatim.xsl"/>
<xsl:include href="modules/tablecals.xsl"/>
<xsl:include href="modules/tablehtml.xsl"/>
<xsl:include href="modules/inlines.xsl"/>
<xsl:include href="modules/xlink.xsl"/>
<xsl:include href="modules/links.xsl"/>
<xsl:include href="modules/xref.xsl"/>
<xsl:include href="modules/attributes.xsl"/>
<xsl:include href="modules/publishers.xsl"/>
<xsl:include href="modules/annotations.xsl"/>

<xsl:import href="modules/chunk.xsl"/>
<xsl:import href="modules/chunk-cleanup.xsl"/>
<xsl:import href="modules/chunk-output.xsl"/>
<xsl:import href="modules/xform-locale.xsl"/>

<xsl:output method="xhtml" encoding="utf-8" indent="no" html-version="5"
            omit-xml-declaration="yes"/>

<xsl:key name="targetptr" match="*" use="@targetptr"/>

<xsl:param name="output-media" select="'screen'"/>

<xsl:template match="/" mode="m:docbook">
  <xsl:document>
    <html>
      <xsl:attribute name="xml:base" select="base-uri(/*)"/>
      <xsl:apply-templates select="(/*/db:info,/*)[1]" mode="m:html-head"/>

      <xsl:if test="f:is-true($persistent-toc)">
        <div db-persistent-toc="true">
          <xsl:apply-templates select="*" mode="m:persistent-toc"/>
        </div>
      </xsl:if>

      <!-- N.B. Any filename specified in a PI is ignored for the root -->
      <div db-chunk="{$chunk}"
           db-xlink="{f:xlink-style(/)}">
        <xsl:sequence select="fp:chunk-navigation(/*)"/>
        <xsl:apply-templates/>
      </div>

      <xsl:if test="f:is-true($theme-picker) and $vp:js-controls">
        <db-script>
          <script type="text/html" id="db-js-controls">
            <xsl:sequence select="$vp:js-controls"/>
          </script>
        </db-script>
      </xsl:if>

      <!-- These get copied into the chunks that need them... -->
      <xsl:if test="exists($resource-base-uri)">
        <db-annotation-script>
          <script type="text/html" class="annotation-close">
            <xsl:sequence select="$v:annotation-close"/>
          </script>
          <script src="{$resource-base-uri}{$annotations-js}" defer="defer"/>
        </db-annotation-script>
        <db-xlink-script>
          <xsl:if test="$xlink-icon-open">
            <script type="text/html" class="xlink-icon-open">
              <xsl:sequence select="$xlink-icon-open"/>
            </script>
          </xsl:if>
          <xsl:if test="$xlink-icon-closed">
            <script type="text/html" class="xlink-icon-closed">
              <xsl:sequence select="$xlink-icon-closed"/>
            </script>
          </xsl:if>
          <script src="{$resource-base-uri}{$xlink-js}" defer="defer"/>
        </db-xlink-script>
        <db-toc-script>
          <script src="{$resource-base-uri}{$persistent-toc-js}" defer="defer"/>
        </db-toc-script>
        <db-pagetoc-script>
          <script src="{$resource-base-uri}{$pagetoc-js}"
                  data-dynamic-pagetoc="{f:is-true($pagetoc-dynamic)}"
                  defer="defer"/>
        </db-pagetoc-script>
        <db-mathml-script>
          <script src="{if (starts-with($mathml-js, 'http:')
                            or starts-with($mathml-js, 'https:'))
                        then $mathml-js
                        else $resource-base-uri || $mathml-js}"
                  defer="defer"/>
        </db-mathml-script>
        <db-script>
          <xsl:if test="exists($chunk) and f:is-true($chunk-nav)">
            <script src="{$resource-base-uri}{$chunk-nav-js}" defer="defer"/>
          </xsl:if>
          <xsl:if test="f:is-true($theme-picker) and $vp:js-controls">
            <script src="{$resource-base-uri}{$control-js}" defer="defer"/>
          </xsl:if>
        </db-script>
        <db-copy-verbatim-script>
          <xsl:if test="normalize-space($copy-verbatim-js) != ''">
            <script src="{$resource-base-uri}{$copy-verbatim-js}" defer="defer"/>
          </xsl:if>
        </db-copy-verbatim-script>
        <db-fallback-script>
          <!-- NOT deferred! -->
          <xsl:if test="normalize-space($fallback-js) != ''">
            <script src="{$resource-base-uri}{$fallback-js}"/>
          </xsl:if>
        </db-fallback-script>
      </xsl:if>
    </html>
  </xsl:document>
</xsl:template>

<xsl:template match="text()">
  <xsl:copy/>
</xsl:template>

</xsl:stylesheet>
