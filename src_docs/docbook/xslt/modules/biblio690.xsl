<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:f="http://docbook.org/ns/docbook/functions"
                xmlns:fp="http://docbook.org/ns/docbook/functions/private"
                xmlns:m="http://docbook.org/ns/docbook/modes"
                xmlns:mp="http://docbook.org/ns/docbook/modes/private"
                xmlns:t="http://docbook.org/ns/docbook/templates"
                xmlns:xlink='http://www.w3.org/1999/xlink'
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml"
                default-mode="m:biblio690"
                exclude-result-prefixes="#all"
                version="3.0">

<xsl:template match="db:biblioentry[contains-token(@role, 'monograph')]">
  <xsl:variable name="entry">
    <db:biblioentry role="{@role}">
      <xsl:sequence select="db:authorgroup|db:author"/>
      <xsl:sequence select="db:title"/>
      <xsl:sequence select="db:subtitle"/>
      <xsl:sequence select="db:bibliomisc[contains-token(@role, 'medium')]"/>
      <xsl:sequence select="db:bibliomisc[contains-token(@role, 'secondary')]"/>
      <xsl:sequence select="db:edition"/>
      <xsl:sequence select="db:publisher"/>
      <xsl:sequence select="db:pubdate"/>
      <xsl:sequence select="db:pagenums"/>
      <xsl:sequence select="db:date[contains-token(@role, 'cit')]"/>
      <xsl:sequence select="db:bibliomisc[contains-token(@role, 'serie')]"/>
      <xsl:sequence select="db:bibliomisc[not(@role)]"/>
      <xsl:sequence select="db:biblioid"/>
    </db:biblioentry>
  </xsl:variable>

  <p>
    <xsl:apply-templates select="$entry/db:biblioentry" mode="m:attributes">
      <xsl:with-param name="style" select="'iso690'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="$entry/db:biblioentry/*"/>
  </p>
</xsl:template>

<xsl:template match="db:biblioentry[contains-token(@role, 'serial')]">
  <xsl:variable name="entry">
    <db:biblioentry role="{@role}">
      <xsl:sequence select="db:title"/>
      <xsl:sequence select="db:subtitle"/>
      <xsl:sequence select="db:bibliomisc[contains-token(@role, 'medium')]"/>
      <xsl:sequence select="db:bibliomisc[contains-token(@role, 'secondary')]"/>
      <xsl:sequence select="db:edition"/>
      <xsl:sequence select="db:pubdate[contains-token(@role, 'issuing')]"/>
      <xsl:sequence select="db:issuenum"/>
      <xsl:sequence select="db:publisher"/>
      <xsl:sequence select="db:pubdate[not(@role)]"/>
      <xsl:sequence select="db:date[contains-token(@role, 'cit')]"/>
      <xsl:sequence select="db:bibliomisc[contains-token(@role, 'serie')]"/>
      <xsl:sequence select="db:bibliomisc[not(@role)]"/>
      <xsl:sequence select="db:biblioid"/>
    </db:biblioentry>
  </xsl:variable>

  <p>
    <xsl:apply-templates select="$entry/db:biblioentry" mode="m:attributes">
      <xsl:with-param name="style" select="'iso690'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="$entry/db:biblioentry/*"/>
  </p>
</xsl:template>

<xsl:template match="db:biblioentry[contains-token(@role, 'part')]">
  <xsl:variable name="entry">
    <db:biblioentry role="{@role}">
      <xsl:sequence select="db:authorgroup|db:author"/>
      <xsl:sequence select="db:title"/>
      <xsl:sequence select="db:subtitle"/>
      <xsl:sequence select="db:bibliomisc[contains-token(@role, 'medium')]"/>
      <xsl:sequence select="db:edition"/>
      <xsl:sequence select="db:volumenum"/>
      <xsl:sequence select="db:bibliomisc[contains-token(@role, 'secondary')]"/>
      <xsl:sequence select="db:publisher"/>
      <xsl:sequence select="db:pubdate"/>
      <xsl:sequence select="db:date[contains-token(@role, 'cit')]"/>
      <xsl:sequence select="db:bibliomisc[contains-token(@role, 'secnum')]"/>
      <xsl:sequence select="db:bibliomisc[contains-token(@role, 'sectitle')]"/>
      <xsl:sequence select="db:pagenums"/>
      <xsl:sequence select="db:biblioid"/>
    </db:biblioentry>
  </xsl:variable>

  <p>
    <xsl:apply-templates select="$entry/db:biblioentry" mode="m:attributes">
      <xsl:with-param name="style" select="'iso690'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="$entry/db:biblioentry/*"/>
  </p>
</xsl:template>

<xsl:template match="db:biblioentry[contains-token(@role, 'contribution')]">
  <xsl:variable name="part" select="db:biblioset[@relation='part']"/>
  <xsl:variable name="book" select="db:biblioset[@relation='book']"/>

  <xsl:variable name="entry">
    <db:biblioentry role="{@role}">
      <db:biblioset relation="part">
        <xsl:sequence select="$part/db:authorgroup|$part/db:author"/>
        <xsl:sequence select="$part/db:title"/>
        <xsl:sequence select="$part/db:subtitle"/>
      </db:biblioset>
      <db:biblioset relation="book">
        <xsl:sequence select="$book/db:authorgroup|$book/db:author"/>
        <xsl:sequence select="$book/db:title"/>
        <xsl:sequence select="$book/db:subtitle"/>
        <xsl:sequence select="$book/db:bibliomisc[contains-token(@role, 'medium')]"/>
        <xsl:sequence select="$book/db:edition"/>
        <xsl:sequence select="$book/db:publisher"/>
        <xsl:sequence select="$book/db:pubdate"/>
        <xsl:sequence select="$book/db:volumenum"/>
        <xsl:sequence select="$book/db:pagenums"/>
        <xsl:sequence select="$book/db:biblioid"/>
      </db:biblioset>
    </db:biblioentry>
  </xsl:variable>

  <p>
    <xsl:apply-templates select="$entry/db:biblioentry" mode="m:attributes">
      <xsl:with-param name="style" select="'iso690'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="$entry/db:biblioentry/*"/>
  </p>
</xsl:template>

<xsl:template match="db:biblioentry[contains-token(@role, 'article')]">
  <xsl:variable name="art" select="db:biblioset[@relation='article']"/>
  <xsl:variable name="jour" select="db:biblioset[@relation='journal']"/>

  <xsl:variable name="entry">
    <db:biblioentry role="{@role}">
      <db:biblioset relation="part">
        <xsl:sequence select="$art/db:authorgroup|$art/db:author"/>
        <xsl:sequence select="$art/db:title"/>
        <xsl:sequence select="$art/db:subtitle"/>
        <xsl:sequence select="$art/db:bibliomisc[contains-token(@role, 'secondary')]"/>
      </db:biblioset>
      <db:biblioset relation="journal">
        <xsl:sequence select="$jour/db:title"/>
        <xsl:sequence select="$jour/db:subtitle"/>
        <xsl:sequence select="$jour/db:bibliomisc[contains-token(@role, 'medium')]"/>
        <xsl:sequence select="$jour/db:pubdate"/>
        <xsl:sequence select="$jour/db:volumenum"/>
        <xsl:sequence select="$jour/db:issuenum"/>
        <xsl:sequence select="$jour/db:date[contains-token(@role, 'cit')]"/>
        <xsl:sequence select="$jour/db:pagenums"/>
        <xsl:sequence select="$jour/db:biblioid"/>
      </db:biblioset>
    </db:biblioentry>
  </xsl:variable>

  <p>
    <xsl:apply-templates select="$entry/db:biblioentry" mode="m:attributes">
      <xsl:with-param name="style" select="'iso690'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="$entry/db:biblioentry/*"/>
  </p>
</xsl:template>

<xsl:template match="db:biblioentry[contains-token(@role, 'patent')]">
  <xsl:variable name="entry">
    <db:biblioentry role="{@role}">
      <xsl:sequence select="db:authorgroup|db:author"/>
      <xsl:sequence select="db:title"/>
      <xsl:sequence select="db:subtitle"/>
      <xsl:sequence select="db:bibliomisc[contains-token(@role, 'secondary')]"/>
      <xsl:sequence select="db:bibliomisc[not(@role)]"/>
      <xsl:sequence select="db:address"/>
      <xsl:sequence select="db:bibliomisc[contains-token(@role, 'patenttype')]"/>
      <xsl:sequence select="db:biblioid[@otherclass='patentnum']"/>
      <xsl:sequence select="db:pubdate"/>
    </db:biblioentry>
  </xsl:variable>

  <p>
    <xsl:apply-templates select="$entry/db:biblioentry" mode="m:attributes">
      <xsl:with-param name="style" select="'iso690'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="$entry/db:biblioentry/*"/>
  </p>
</xsl:template>

<xsl:template match="db:biblioentry[contains-token(@role, 'messagesystem')]">
  <xsl:variable name="entry">
    <db:biblioentry role="{@role}">
      <xsl:sequence select="db:title"/>
      <xsl:sequence select="db:subtitle"/>
      <xsl:sequence select="db:bibliomisc[contains-token(@role, 'medium')]"/>
      <xsl:sequence select="db:publisher"/>
      <xsl:sequence select="db:pubdate"/>
      <xsl:sequence select="db:date[contains-token(@role, 'cit')]"/>
      <xsl:sequence select="db:biblioid"/>
    </db:biblioentry>
  </xsl:variable>

  <p>
    <xsl:apply-templates select="$entry/db:biblioentry" mode="m:attributes">
      <xsl:with-param name="style" select="'iso690'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="$entry/db:biblioentry/*"/>
  </p>
</xsl:template>

<xsl:template match="db:biblioentry[contains-token(@role, 'message')]">
  <xsl:variable name="part" select="db:biblioset[@relation='part']"/>
  <xsl:variable name="book" select="db:biblioset[@relation='book']"/>

  <xsl:variable name="entry">
    <db:biblioentry role="{@role}">
      <db:biblioset relation="part">
        <xsl:sequence select="$part/db:authorgroup|$part/db:author"/>
        <xsl:sequence select="$part/db:title"/>
        <xsl:sequence select="$part/db:subtitle"/>
      </db:biblioset>
      <db:biblioset relation="book">
        <xsl:sequence select="$book/db:title"/>
        <xsl:sequence select="$book/db:subtitle"/>
        <xsl:sequence select="$book/db:bibliomisc[contains-token(@role, 'medium')]"/>
        <xsl:sequence select="$book/db:publisher"/>
        <xsl:sequence select="$book/db:pubdate"/>
        <xsl:sequence select="$book/db:date[contains-token(@role, 'cit')]"/>
        <xsl:sequence select="$book/db:biblioid"/>
      </db:biblioset>
    </db:biblioentry>
  </xsl:variable>

  <p>
    <xsl:apply-templates select="$entry/db:biblioentry" mode="m:attributes">
      <xsl:with-param name="style" select="'iso690'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="$entry/db:biblioentry/*"/>
  </p>
</xsl:template>

<xsl:template match="db:biblioentry">
  <p>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </p>
</xsl:template>

<xsl:template match="db:biblioset">
  <xsl:if test="preceding-sibling::db:biblioset and @relation = 'book'">
    <xsl:sequence select="f:l10n-token(., 'In')"/>
    <xsl:text> </xsl:text>
  </xsl:if>
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </span>
</xsl:template>

<xsl:template match="db:authorgroup">
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates select="." mode="m:gentext-list">
      <xsl:with-param name="list" as="element()*">
        <xsl:apply-templates select="*"/>
      </xsl:with-param>
    </xsl:apply-templates>
  </span>
  <xsl:sequence select="fp:optional-sep(*[last()], 'primary.sep')"/>
</xsl:template>

<xsl:template match="db:author">
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates select="*"/>
  </span>
  <xsl:if test="not(parent::db:authorgroup)">
    <xsl:sequence select="fp:optional-sep(., 'primary.sep')"/>
  </xsl:if>
</xsl:template>

<xsl:template match="db:personname">
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates select="db:surname"/>
    <xsl:sequence select="fp:iso690(., 'lastfirst.sep')"/>
    <xsl:apply-templates select="db:firstname|db:givenname"/>
  </span>
</xsl:template>

<xsl:template match="db:title">
  <em>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </em>
  <xsl:apply-templates select="." mode="mp:biblio690-punct"/>
</xsl:template>

<xsl:template match="db:biblioset[@relation='article']/db:title
                     |db:biblioset[@relation='part']/db:title">
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </span>
  <xsl:apply-templates select="." mode="mp:biblio690-punct"/>
</xsl:template>

<xsl:template match="db:title" mode="mp:biblio690-punct">
  <xsl:choose>
    <xsl:when test="following-sibling::db:bibliomisc[contains-token(@role, 'medium')]">
      <!-- nop -->
    </xsl:when>
    <xsl:when test="following-sibling::db:subtitle">
      <xsl:sequence select="fp:optional-sep(., 'submaintitle.sep')"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="fp:optional-sep(., 'title.sep')"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:subtitle">
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </span>
  <xsl:sequence select="fp:optional-sep(., 'title.sep')"/>
</xsl:template>

<xsl:template match="db:bibliomisc[contains-token(@role, 'secondary')]">
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </span>
  <xsl:variable name="fs" select="following-sibling::*[1]"/>
  <xsl:choose>
    <xsl:when test="$fs/self::db:bibliomisc and contains-token($fs/@role, 'secondary')">
      <xsl:sequence select="fp:iso690(., 'secondary.person.sep')"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="fp:optional-sep(., 'secondary.sep')"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:bibliomisc[contains-token(@role, 'serie')]">
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </span>
  <xsl:sequence select="fp:optional-sep(., 'serie.sep')"/>
</xsl:template>

<xsl:template match="db:bibliomisc[contains-token(@role, 'patenttype')]">
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </span>
  <xsl:sequence select="fp:optional-sep(., 'pattype.sep')"/>
</xsl:template>

<xsl:template match="db:bibliomisc[contains-token(@role, 'medium')]">
  <xsl:sequence select="fp:iso690(., 'medium1')"/>
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </span>
  <xsl:sequence select="fp:iso690(., 'medium2')"/>
  <xsl:sequence select="fp:iso690(., 'title.sep')"/>
</xsl:template>

<xsl:template match="db:bibliomisc">
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </span>
  <xsl:sequence select="fp:optional-sep(., 'primary.sep')"/> <!-- ??? -->
</xsl:template>

<xsl:template match="db:edition">
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </span>
  <xsl:sequence select="fp:optional-sep(., 'edition.sep')"/>
</xsl:template>

<xsl:template match="db:volumenum">
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </span>
  <xsl:sequence select="if ((following-sibling::db:issuenum)
                            or (following-sibling::db:pagenums))
                        then fp:iso690(., 'edition.serial.sep')
                        else fp:optional-sep(., 'edition.sep')"/>
</xsl:template>

<xsl:template match="db:issuenum">
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </span>
  <xsl:choose>
    <xsl:when test="following-sibling::*[1]/self::db:date[contains-token(@role, 'cit')]">
      <!-- nop -->
    </xsl:when>
    <xsl:when test="following-sibling::db:pagenums">
      <xsl:sequence select="fp:iso690(., 'edition.serial.sep')"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="fp:optional-sep(., 'edition.sep')"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:publisher">
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:if test="db:address/db:city">
      <xsl:apply-templates select="db:address/db:city"/>
      <xsl:sequence select="fp:iso690(., 'placepubl.sep')"/>
    </xsl:if>
    <xsl:apply-templates select="db:publishername"/>
  </span>
  <xsl:choose>
    <xsl:when test="following-sibling::*[1]/self::db:pubdate">
      <xsl:sequence select="fp:iso690(., 'publyear.sep')"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="fp:optional-sep(., 'pubinfo.sep')"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:pubdate">
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
    <xsl:if test="ends-with(normalize-space(.), '-')
                  or ends-with(normalize-space(.), '—')">
      <xsl:text> </xsl:text>
    </xsl:if>
  </span>
  <xsl:choose>
    <xsl:when test="following-sibling::*[1]/self::db:date[contains-token(@role, 'cit')]">
      <!-- nop -->
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="if (following-sibling::db:volumenum)
                            then fp:iso690(., 'edition.serial.sep')
                            else fp:optional-sep(., 'pubinfo.sep')"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:date[contains-token(@role, 'cit')]">
  <xsl:sequence select="fp:iso690(., 'datecit1')"/>
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </span>
  <xsl:sequence select="fp:iso690(., 'datecit2')"/>
  <xsl:sequence select="if (following-sibling::db:pagenums)
                        then fp:iso690(., 'edition.serial.sep')
                        else fp:optional-sep(., 'pubinfo.sep')"/>
</xsl:template>

<xsl:template match="db:pagenums">
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </span>
  <xsl:sequence select="fp:optional-sep(., 'primary.sep')"/> <!-- ??? -->
</xsl:template>

<xsl:template match="db:biblioid[contains-token(@class, 'uri')]">
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:choose>
      <xsl:when test="preceding-sibling::db:biblioid[contains-token(@class, 'uri')]">
        <xsl:sequence select="fp:iso690(., 'acctoo')"/>
        <xsl:apply-templates/>
        <xsl:if test="empty(node())">
          <xsl:sequence select="if (starts-with(@xlink:href, 'http'))
                                then fp:iso690(., 'onwww')
                                else fp:iso690(., 'oninet')"/>
        </xsl:if>
        <xsl:text>: </xsl:text>
        <xsl:sequence select="fp:iso690(., 'link1')"/>
        <xsl:sequence select="string(@xlink:href)"/>
        <xsl:sequence select="fp:iso690(., 'link2')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="fp:iso690(., 'access')"/>
        <xsl:apply-templates/>
        <xsl:if test="empty(node())">
          <xsl:sequence select="if (starts-with(@xlink:href, 'http'))
                                then fp:iso690(., 'onwww')
                                else fp:iso690(., 'oninet')"/>
        </xsl:if>
        <xsl:text>: </xsl:text>
        <xsl:sequence select="fp:iso690(., 'link1')"/>
        <xsl:sequence select="string(@xlink:href)"/>
        <xsl:sequence select="fp:iso690(., 'link2')"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:sequence select="fp:optional-sep(., 'primary.sep')"/> <!-- ??? -->
  </span>
</xsl:template>

<xsl:template match="db:biblioid">
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:choose>
      <xsl:when test="@class = 'isbn'">
        <xsl:sequence select="fp:iso690(., 'isbn')"/>
      </xsl:when>
      <xsl:when test="@class = 'issn'">
        <xsl:sequence select="fp:iso690(., 'issn')"/>
      </xsl:when>
      <xsl:otherwise/>
    </xsl:choose>
    <xsl:apply-templates/>
  </span>
  <xsl:sequence select="if (contains-token(@otherclass, 'patentnum'))
                        then fp:optional-sep(., 'patnum.sep')
                        else fp:optional-sep(., 'primary.sep')"/> <!-- ??? -->
</xsl:template>

<xsl:template match="db:surname|db:firstname|db:givenname
                     |db:city|db:publishername">
  <span>
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates/>
  </span>
</xsl:template>

<xsl:template match="db:biblioentry[contains-token(@role, 'patent')]/db:address">
  <xsl:if test="db:country">
    <span>
      <xsl:apply-templates select="db:country" mode="m:attributes"/>
      <xsl:apply-templates select="db:country/node()"/>
    </span>
    <xsl:text> </xsl:text>
  </xsl:if>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="db:*">
  <xsl:apply-templates select="." mode="m:docbook"/>
</xsl:template>

<!-- ============================================================ -->

<xsl:function name="fp:iso690" as="item()*">
  <xsl:param name="context" as="element()"/>
  <xsl:param name="key" as="xs:string"/>

  <xsl:sequence select="f:l10n-token($context, 'iso690.'||$key)"/>
</xsl:function>

<xsl:function name="fp:optional-sep" as="item()*">
  <xsl:param name="context" as="element()"/>
  <xsl:param name="key" as="xs:string"/>

  <xsl:choose>
    <xsl:when test="ends-with(normalize-space($context), '.')">
      <xsl:text> </xsl:text>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="f:l10n-token($context, 'iso690.'||$key)"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:function>

</xsl:stylesheet>
