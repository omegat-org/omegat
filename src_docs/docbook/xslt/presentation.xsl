<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:v="http://docbook.org/ns/docbook/variables"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all"
                version="3.0">

<xsl:import href="docbook.xsl"/>

<xsl:param name="presentation-mode" select="'true'"/>
<xsl:param name="division-numbers" select="'false'"/>
<xsl:param name="component-numbers" select="'false'"/>

<xsl:variable name="v:custom-localizations">
  <locale xmlns="http://docbook.org/ns/docbook/l10n/source"
          language="en"
          english-language-name="English">
    <group name="title-unnumbered">
      <template match="self::db:part">%c</template>
      <template match="self::db:chapter">%c</template>
    </group>
    <group name="list-of-titles">
      <template match="self::db:part[parent::db:book]">%c</template>
    </group>
  </locale>
</xsl:variable>



</xsl:stylesheet>
