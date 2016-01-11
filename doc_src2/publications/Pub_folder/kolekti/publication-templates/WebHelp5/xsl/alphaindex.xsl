<?xml version="1.0" encoding="utf-8"?>

<!DOCTYPE xsl:stylesheet [
   <!ENTITY mark "normalize-space(translate(string(.),'abcdefghijklmnopqrstuvwxyzÉÀÇÈÙËÊÎÏÔÖÛÜÂÄéàçèùëêïîöôüûäâ','ABCDEFGHIJKLMNOPQRSTUVWXYZEACEUEEIIOOUUAAACEUEEIIOOUUAA'))">
   <!ENTITY key "normalize-space(string(.))">
  ]>

<xsl:stylesheet version="1.0"
        xmlns:html="http://www.w3.org/1999/xhtml"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        exclude-result-prefixes="html"
        >
  <xsl:key name="indexmarks" match="html:ins[@class='index']|html:span[@rel='index']" use="''"/>
  <xsl:key name="indexmarksentries" match="html:ins[@class='index']|html:span[@rel='index']" use="text()"/>

  <xsl:template name="alphabetical-index">
    <p class="text-center" id="indexlinks">
      <xsl:apply-templates select="//html:p[@class='alphaindex letter']" mode="alphaindexlinks"/>
    </p>
    <div class="row" id="indexinner">
      <xsl:apply-templates select="//html:p[@class='alphaindex letter']" mode="alphaindex"/>
    </div>
  </xsl:template>
 
  <xsl:template match="html:ins[@class='index']|html:span[@rel='index']" mode="modcontent">
    <a name="markindex" id="indx_{generate-id()}"><xsl:comment> </xsl:comment></a>
  </xsl:template>


  <xsl:template match="html:p" mode="alphaindexlinks">
    <a href="#index{.}"><xsl:value-of select="."/></a>
    <xsl:text>&#xA0;</xsl:text>
  </xsl:template>

  <xsl:template match="node()" mode="alphaindex">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*" mode="alphaindex"/>
    </xsl:copy>
  </xsl:template>

  
  <xsl:template match="*" mode="alphaindex">
    <xsl:element name="{local-name()}">
      <xsl:apply-templates select="node()|@*" mode="alphaindex"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="@*" mode="alphaindex">
    <xsl:copy/>
  </xsl:template>
  
  <xsl:template match="html:p[@class='alphaindex letter']" mode="alphaindex">
    <div class="col-md-4" id="index{.}">
      <p class="alphaindex letter"><xsl:value-of select="."/></p>
      <div class="alphaindex lettergroup">
        <xsl:apply-templates select="following-sibling::html:div[1]" mode="alphaindex"/>
      </div>
    </div>
    <xsl:if test="position() mod 3 = 0">
      <div class="clearfix visible-md-block"></div>
      <hr/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="html:p[@class='alphaindex entry level0']|html:p[@class='alphaindex entry level1']" mode="alphaindex">
    <xsl:copy>
      <xsl:apply-templates select="@*" mode="alphaindex"/>
      <xsl:choose>
	<xsl:when test="count(html:a) = 0">
	  <xsl:apply-templates mode="alphaindex"/>
	</xsl:when>
	<xsl:when test="count(html:a) = 1">
	  <xsl:apply-templates select="html:a" mode="alphaindexlink">
	    <xsl:with-param name="linktext">
	      <xsl:apply-templates mode="alphaindex"/>
	    </xsl:with-param>
	  </xsl:apply-templates>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:apply-templates mode="alphaindex"/>
	  <xsl:text> </xsl:text>
	  <xsl:apply-templates select="html:a" mode="alphaindexlink"/>
	  <xsl:text></xsl:text>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="html:a" mode="alphaindex"/>
  <xsl:template match="html:span" mode="alphaindex"/>

  <xsl:template match="html:a" mode="alphaindexlink">
    <xsl:param name="linktext">
      <xsl:text>[</xsl:text>
      <xsl:value-of select="position()"/>
      <xsl:text>]</xsl:text>
    </xsl:param>

    <xsl:variable name="modref">
      <xsl:value-of select="//html:div[@class='topic'][.//html:a[@class='indexmq'][@name=substring-after(current()/@href,'#')]]/@id"/>
    </xsl:variable>
    
    <a>
      <xsl:attribute name="href">
	<xsl:call-template name="modhref">
          <xsl:with-param name="modid" select="concat($modref, '_', substring-after(@href,'#'))" />
        </xsl:call-template>	
      </xsl:attribute>
      <xsl:attribute name="title">
	<xsl:call-template name="modtitle">
          <xsl:with-param name="modid" select="$modref" />
        </xsl:call-template>

      </xsl:attribute>
      <xsl:value-of select="$linktext"/>
    </a>
    <xsl:if test="not(position()=last())">, </xsl:if>
  </xsl:template>


</xsl:stylesheet>
