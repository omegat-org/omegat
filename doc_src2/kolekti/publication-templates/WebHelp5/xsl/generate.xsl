<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:html="http://www.w3.org/1999/xhtml"
  xmlns:exsl="http://exslt.org/common"
  xmlns:kfp="kolekti:extensions:functions:publication"

  extension-element-prefixes="exsl kfp"
  exclude-result-prefixes="html exsl kfp">
  <xsl:output method="html" indent="yes" />

  <xsl:include href="alphaindex.xsl"/>
  
  <xsl:param name="pubdir" />
  <xsl:param name="template" />
  <xsl:param name="css" />

  <xsl:variable name="helpname">WebHelp5</xsl:variable>
<!--
  <xsl:variable name="index" select="//html:div[starts-with(@class,'INDEX')]"/>
-->
<xsl:variable name="index" select="//html:ins[@class='index']|//html:span[@rel='index']" />
  <xsl:variable name="topics" select="//html:div[@class='topic']" />


  <xsl:variable name="lang">
    <xsl:value-of select="/html:html/html:body/@lang" />
  </xsl:variable>


  <xsl:variable name="translationfile">
    <xsl:choose>
      <xsl:when test="document($template)//html:span[@id='labels']">
        <xsl:value-of select="document($template)//html:span[@id='labels']" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$helpname" />
        <xsl:text>_labels</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="helptitle">
    <xsl:value-of select="kfp:replace_strvar(string(/html:html/html:head/html:title/text()))" />
  </xsl:variable>

  <xsl:variable name="starttopic">
    <xsl:choose>
      <xsl:when test="$topics/html:div[@class='topicinfo']//html:span[@class='infolabel']='hlpstart'">
        <xsl:for-each select="$topics[html:div[@class='topicinfo']//html:span[@class='infolabel']='hlpstart'][1]">
          <xsl:call-template name="modfile" />
        </xsl:for-each>
      </xsl:when>
      <xsl:when test="document($template)//html:span[@id='start_topic']!=''">
        <xsl:value-of select="document($template)//html:span[@id='start_topic']" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="modfile">
          <xsl:with-param name="modid" select="$topics[1]/@id" />
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <!-- main template -->

  <xsl:template match="/">
    <exsl:document href="{$pubdir}/js/modcodes.js" method='text'>
      var modcodes = new Object();
      <xsl:apply-templates select="html:html/html:body//html:div[@class='topic']" mode="modcodes" />
    </exsl:document>
    <exsl:document href="{$pubdir}/js/modtexts.js" method='text'>
      var modtexts = new Object();
      <xsl:apply-templates select="$topics" mode="textcontent" />
    </exsl:document>
    <xsl:apply-templates select="$topics" />
    <xsl:apply-templates select="//html:div[starts-with(@class,'INDEX')]" />
     <xsl:if test="$index">
       <exsl:document href="{$pubdir}/alphaindex.html"
                      method="html"
                      indent="yes"
                      encoding="utf-8">
         <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
         <html>
           <head>
             <xsl:call-template name="genhtmlheader" />
           </head>
           <body>
             <xsl:call-template name="gennavbar">
               <xsl:with-param name="topictitle" select="'Index alphabétique'"/>
             </xsl:call-template>
             <div class="container-fluid">
               <div class="row">
		 <div class="col-md-3 col-sm-12 col-xs-12" id="k-menu">
                   <xsl:call-template name="gentoc" />
		 </div>
                 <div class="col-md-9 sol-sm-12 col-xs-12" >
                   <div id="alphaindex">
                     <h2 id="alphaindextitle" class="page-header alphaindextitle">
                       Index alphabétique
                     </h2>
                     <div id="alphaindexcontent" class="alphaindexcontent">
                       <xsl:call-template name="alphabetical-index" />
                     </div>
                   </div>
                 </div>
               </div>
             </div>
             <xsl:call-template name="genhtmlfooter"/> 
           </body>
         </html>
       </exsl:document>
     </xsl:if>
  </xsl:template>



  <xsl:template match="html:div[@class='topic']" mode="modcodes">
    <xsl:variable name="modcode">
      <xsl:call-template name="modfile" />
    </xsl:variable>
    <xsl:variable name="modt">
      <xsl:call-template name="modtitle" />
    </xsl:variable>
    <xsl:variable name="modtitle">
      <xsl:for-each select="ancestor::html:div[@class='section']">
        <xsl:sort order="ascending" select="count(ancestor::*)"/>
        <xsl:if test="not(position()=1)">
          <xsl:copy-of select="html:*[1]/text()"/>
          <!--        <xsl:if test="position()!=last() or $modt!=''"> -->
          <xsl:text> / </xsl:text>
          <!--        </xsl:if>-->
        </xsl:if>
      </xsl:for-each>
      <xsl:value-of select="$modt"/>
    </xsl:variable>
    <xsl:text>modcodes['</xsl:text>
    <xsl:value-of select="$modcode" />
    <xsl:text>']='</xsl:text>
    <xsl:call-template name="trquot">
      <xsl:with-param name="text" select="$modtitle" />
    </xsl:call-template>
    <xsl:text>';</xsl:text>
  </xsl:template>

  <xsl:template match="html:div[@class='topic']" mode="textcontent">
    <xsl:variable name="filename">
      <xsl:call-template name="modfile" />
    </xsl:variable>
    <xsl:text>modtexts['</xsl:text>
    <xsl:value-of select="$filename" />
    <xsl:text>'] = '</xsl:text>
    <xsl:apply-templates mode="textcontent" select="node()[not(@class='topicinfo')]" />
    <xsl:text>';</xsl:text>
  </xsl:template>

  <xsl:template match="text()" mode="textcontent">
    <xsl:call-template name="trquot">
      <xsl:with-param name="text" select="normalize-space(.)" />
    </xsl:call-template>
    <xsl:text> </xsl:text>
  </xsl:template>

  <xsl:template match="html:span[@class='title_num']" mode="textcontent"/>
  <xsl:template match="html:div[@class='topicinfo']" mode="textcontent"/>

  <xsl:template match="html:div[@class='topic']">
    <xsl:variable name="filename">
      <xsl:call-template name="modfile" />
    </xsl:variable>
    <exsl:document href="{$pubdir}/{$filename}"
      method="html"
      indent="yes"
      encoding="utf-8">
      <xsl:variable name="modtitle">
        <xsl:call-template name="modtitle" />
      </xsl:variable>
      <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
      <html>
        <head>
          <xsl:call-template name="genhtmlheader" />
        </head>
        <body>
          <xsl:call-template name="gennavbar" />
          <div class="container-fluid">

            <div class="row">
              <div class="col-md-3 col-sm-12 col-xs-12" id="k-menu">
                <xsl:call-template name="gentoc">
                  <xsl:with-param name="modtitle" select="$modtitle"/>
                </xsl:call-template>
              </div>

              <div class="col-md-9 col-sm-12 col-xs-12" id="k-main">
                <div class="row-fluid" id="k-topic">
                  <div class="col-md-11">
                    <div id="k-topiccontent">
                      <xsl:apply-templates select="*[not(@class='topicinfo')]" mode="modcontent" />
                    </div>
                  </div>
                </div>

                <!-- topic bottom -->
                <a href="#" class="back-to-top">
                  <i class="glyphicon glyphicon-chevron-up"></i>
                </a>

              </div>
            </div>
          </div>
          <xsl:call-template name="genhtmlfooter" />
        </body>
      </html>
    </exsl:document>
  </xsl:template>

  <xsl:template match="html:divX[@class='INDEX' or starts-with(@class,'INDEX ')]">
    <xsl:variable name="filename" select="'alphaindex.html'"/>
    <exsl:document href="{$pubdir}/{$filename}"
      method="html"
      indent="yes"
      encoding="utf-8">
      <xsl:variable name="modtitle">
        <xsl:value-of select="kfp:variable(string($translationfile),'AlphaIndexTitre')" />
      </xsl:variable>
      <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
      <html>
        <head>
          <xsl:call-template name="genhtmlheader" />
        </head>
        <body>
          <xsl:call-template name="gennavbar" />
          <div class="container-fluid">

            <div class="row">
              <div class="col-md-3 col-sm-12 col-xs-12" id="k-menu">
                <xsl:call-template name="gentoc">
                  <xsl:with-param name="modtitle" select="$modtitle"/>
                  <xsl:with-param name="modid" select="'alphaindex'"/>
                </xsl:call-template>
              </div>

              <div class="col-md-9 col-sm-12 col-xs-12" id="k-main">
                <div class="row-fluid" id="k-topic">
                  <div class="col-md-11">
                    <div id="k-topiccontent">
                      <xsl:apply-templates mode="modcontent" />
                    </div>
                  </div>
                </div>

                <!-- topic bottom -->
                <a href="#" class="back-to-top">
                  <i class="glyphicon glyphicon-chevron-up"></i>
                </a>

              </div>
            </div>
          </div>
          <xsl:call-template name="genhtmlfooter" />
        </body>
      </html>
    </exsl:document>
  </xsl:template>



  <xsl:template match="html:div[@class='topic']" mode="gentoc">
    <xsl:param name="modid" />

    <xsl:variable name="modref">
      <xsl:call-template name="modfile" />
    </xsl:variable>

    <xsl:variable name="curtopic" select="boolean(@id = $modid)" />

    <li>
      <xsl:attribute name="class">
        <xsl:text>list-group-item</xsl:text>
        <xsl:if test="$curtopic"> active</xsl:if>
      </xsl:attribute>

      <a href="{$modref}">
        <i class="glyphicon glyphicon-file"></i>
        <xsl:call-template name="modtitle" />
      </a>
    </li>
  </xsl:template>

  <xsl:template match="html:div[@class='topic'][not(.//html:h1) and not(.//html:h2)]" mode="gentoc"/>

  <xsl:template match="html:div[@class='section']" mode="gentoc">
    <xsl:param name="modid" />
    <xsl:param name="modtitle" />

    <xsl:variable name="modref">
      <xsl:call-template name="modfile">
        <xsl:with-param name="modid" select="(.//html:div[@class='topic'])[1]/@id" />
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="cursection" select="boolean(.//html:div[@id = $modid])" />

    <li class="list-group-item">
      <a href="{$modref}" data-section="{generate-id()}">
        <i>
          <xsl:attribute name="class">
            <xsl:choose>
              <xsl:when test="$cursection"><xsl:text>glyphicon glyphicon-folder-open</xsl:text></xsl:when>
              <xsl:otherwise><xsl:text>glyphicon glyphicon-folder-close</xsl:text></xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
        </i>
      </a>

      <a href="{$modref}">
        <xsl:apply-templates select="html:*[1]/node()" mode="gentoc"/>
      </a>

      <ul data-section-content="{generate-id()}">
        <xsl:attribute name="class">
          <xsl:text>list-group</xsl:text>
          <xsl:if test="not($cursection)"> hidden</xsl:if>
        </xsl:attribute>
        <xsl:apply-templates select="html:div[@class='section' or @class='topic']" mode="gentoc">
          <xsl:with-param name="modid" select="$modid" />
          <xsl:with-param name="modtitle" select="$modtitle" />
        </xsl:apply-templates>
      </ul>
    </li>
  </xsl:template>

  <xsl:template match="html:div[@class='section'][@data-hidden='true']" mode="gentoc"/>
  
  <xsl:template match="html:a[@class='indexmq']" mode="gentoc" />
  <xsl:template match="html:span[@class='title_num']" mode="gentoc" />


  <!-- traitement du contenu du topic -->

  <xsl:template match="html:a[@href='#']" mode="modcontent">
    <xsl:apply-templates select="node()" mode="modcontent" />
  </xsl:template>


  <xsl:template match="html:a[@href!='#'][starts-with(@href,'#')]" mode="modcontent">
    <xsl:copy>
      <xsl:apply-templates select="@*" mode="modcontent" />
      <xsl:attribute name="href">
        <xsl:call-template name="modhref">
          <xsl:with-param name="modid" select="substring-after(@href,'#')" />
        </xsl:call-template>
      </xsl:attribute>
      
      <xsl:apply-templates select="node()" mode="modcontent" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="html:a[@href!='#'][starts-with(@href,'#idx_')]" mode="modcontent">
    <xsl:variable name="modid">
      <xsl:for-each select="//html:*[@id=substring-after(current()/@href,'#')]/ancestor::html:div[@class='topic']">
        <xsl:call-template name="modfile"/>
      </xsl:for-each>
    </xsl:variable>

    <xsl:variable name="libel">
      <xsl:for-each select="//html:*[@id=substring-after(current()/@href,'#')]/ancestor::html:div[@class='topic']">
        <xsl:call-template name="modtitle"/>
      </xsl:for-each>
    </xsl:variable>

    <xsl:copy>
      <a href="{$modid}{@href}">
        <xsl:value-of select="$libel"/>
      </a>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="html:a[@href!='#'][not(starts-with(@href,'#'))]" mode="modcontent">
    <xsl:copy>
      <xsl:apply-templates select="@*" mode="modcontent" />
      <xsl:attribute name="target">_blank</xsl:attribute>
      <xsl:apply-templates select="node()" mode="modcontent" />
    </xsl:copy>
  </xsl:template>



  <xsl:template match="html:div[@class='attention']" mode="modcontent">
    <div class="alert alert-danger" role="alert">
    <span class="label label-danger">
      <xsl:value-of select="kfp:variable(string($translationfile),'Important')" />
    </span>
    <xsl:apply-templates select="node()" mode="modcontent" />
    </div>
  </xsl:template>

  <xsl:template match="html:div[@class='danger']" mode="modcontent">
    <div class="alert alert-danger" role="alert">
    <span class="label label-danger">
      <xsl:value-of select="kfp:variable(string($translationfile),'Danger')" />
    </span>
      <xsl:apply-templates select="node()" mode="modcontent" />
    </div>
  </xsl:template>

  <xsl:template match="html:div[@class='remarque']" mode="modcontent">
    <div class="alert alert-success" role="alert">
      <span class="label label-success">
	<xsl:value-of select="kfp:variable(string($translationfile),'Remarque')" />
      </span>
      <xsl:apply-templates select="node()" mode="modcontent" />
    </div>
  </xsl:template>

  <xsl:template match="html:div[@class='exemple']" mode="modcontent">
    <div class="alert alert-info" role="alert">
      <span class="label label-info">
	<xsl:value-of select="kfp:variable(string($translationfile),'Exemple')" />
      </span>
      <xsl:apply-templates select="node()" mode="modcontent" />
    </div>
  </xsl:template>

  <xsl:template match="html:div[@class='voiraussi']" mode="modcontent">
    <div class="panel panel-info">
      <div class="panel-heading">
        <xsl:apply-templates select="*[1]" mode="modcontent" />
      </div>
      <div class="panel-body">
        <xsl:apply-templates select="*[position() &gt; 1]" mode="modcontent" />
      </div>
    </div>
  </xsl:template>

  <xsl:template match="html:div[@class='voiraussi']//html:ul" mode="modcontent">
    <ul class="list-unstyled">
      <xsl:apply-templates select="node()" mode="modcontent" />
    </ul>
  </xsl:template>

  <xsl:template match="html:div[@class='voiraussi']//html:li" mode="modcontent">
    <li>
      <span class="glyphicon glyphicon-circle-arrow-right"></span>
      <xsl:apply-templates select="node()" mode="modcontent" />
    </li>
  </xsl:template>

  <xsl:template match="html:img" mode="modcontent">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <!--
	  <xsl:attribute name="class">
	  <xsl:value-of select="class"/>
	  <xsl:text> img-responsive</xsl:text>
	  </xsl:attribute>
	  -->
      <xsl:attribute name="src">
	<xsl:value-of select="substring(@src,2)"/>
      </xsl:attribute>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="node()|@*" mode="modcontent">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*" mode="modcontent" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="html:ins[@class='index']" mode="modcontent" />


<xsl:template match="html:span[@class='title_num']" mode="TOCtitle" />
<xsl:template match="html:ins[@class='index']|html:span[@rel='index']" mode="TOCtitle" />





<!-- generate topic navbar -->
<xsl:template name="topicnavbar">
  <img src="usercss/{$css}.parts/logo.png" style="height:auto; width:220px; margin-top:10px; margin-right:20px"/>
  <div id="xs-button" class="visible-xs">
    <button type="button" data-target="#navbarCollapse" data-toggle="collapse" class="glyphicon glyphicon-list btn btn-default">
    </button>
  </div>
  <div class="btn-group pull-right hidden-xs">
    <a title="{kfp:variable(string($translationfile),'precedent')}">
      <xsl:attribute name="class">
        <xsl:text>btn btn-default</xsl:text>
        <xsl:if test="not(preceding::html:div[@class='topic'])">
          <xsl:text> disabled</xsl:text>
        </xsl:if>
      </xsl:attribute>
      <xsl:attribute name="href">
        <xsl:call-template name="modfile">
          <xsl:with-param name="modid" select="preceding::html:div[@class='topic'][1]/@id" />
        </xsl:call-template>
      </xsl:attribute>
      <i class="glyphicon glyphicon-chevron-left"></i>
    </a>

    <a title="{kfp:variable(string($translationfile),'suivant')}">
      <xsl:attribute name="class">
        <xsl:text>btn btn-default</xsl:text>
        <xsl:if test="not(following::html:div[@class='topic'])">
          <xsl:text> disabled</xsl:text>
        </xsl:if>
      </xsl:attribute>
      <xsl:attribute name="href">
        <xsl:call-template name="modfile">
          <xsl:with-param name="modid" select="following::html:div[@class='topic'][1]/@id" />
        </xsl:call-template>
      </xsl:attribute>
      <i class="glyphicon glyphicon-chevron-right"></i>
    </a>
    <span class="btn btn-default" title="{kfp:variable(string($translationfile),'imprimer')}" onclick="window.print();"><i class="glyphicon glyphicon-print"></i></span>
       <!-- 
    <a title="PDF" class="btn btn-default" href="ManuelUtilisation_Niveau{/html:html/html:head/html:meta[@scheme = 'condition'][@name='NIV']/@content}.pdf" >PDF</a>
  -->
  </div>
</xsl:template>





   <!--  generate html header -->
   <xsl:template name="genhtmlheader">
     <meta charset="utf-8" />
     <meta content="width=device-width, initial-scale=1.0" name="viewport" />
     <xsl:copy-of select="/html:html/html:head/html:title" />
     <xsl:copy-of select="/html:html/html:head/html:meta[@scheme='user_condition']"/>
     <xsl:copy-of select="/html:html/html:head/html:meta[@scheme='user_condition_label']"/>
     <link rel="stylesheet" href="lib/css/bootstrap.min.css" type="text/css"/>
     <link rel="stylesheet" href="lib/css/bootstrap-theme.min.css" type="text/css"/>
     <link rel="stylesheet" href="lib/css/WebHelp5.css" type="text/css"/>
     <xsl:if test="$css">
       <link rel="stylesheet" href="usercss/{$css}.css" type="text/css" />
     </xsl:if>

   <xsl:text disable-output-escaping='yes'>&lt;!--[if lt IE 9]&gt;</xsl:text>
   <script src="http://html5shim.googlecode.com/svn/trunk/html5.js" type="text/javascript">
     <xsl:comment> </xsl:comment>
   </script>
   <xsl:text disable-output-escaping='yes'>&lt;![endif]--&gt;</xsl:text>
   <style type="text/css">
     body {
     padding-top: 60px;
     padding-bottom: 40px;
     }
     .sidebar-nav {
     padding: 9px 0;
     }
   </style>
 </xsl:template>


 <xsl:template name="genhtmlfooter">
     <script src="lib/js/jquery-2.1.1.min.js" type="text/javascript">
       <xsl:comment> </xsl:comment>
     </script>
     <script src="lib/js/filteredview.js" type="text/javascript">
       <xsl:comment> </xsl:comment>
     </script>
     <script src="lib/js/bootstrap.min.js" type="text/javascript">
       <xsl:comment> </xsl:comment>
     </script>

     <script src="js/modcodes.js" type="text/javascript">
       <xsl:comment> </xsl:comment>
     </script>
     <script src="js/modtexts.js" type="text/javascript">
       <xsl:comment> </xsl:comment>
     </script>
     <script src="js/index.js" type="text/javascript">
       <xsl:comment> </xsl:comment>
     </script>
     <script src="lib/js/search.js" type="text/javascript">
       <xsl:comment> </xsl:comment>
     </script>


     <script type="text/javascript">
       var label_score="<xsl:call-template name="gettext"><xsl:with-param name="label" select="'score'" /></xsl:call-template>";
       var label_moreres="<xsl:call-template name="gettext"><xsl:with-param name="label" select="'plus10res'" /></xsl:call-template>";
   </script>

     <script src="lib/js/events.js" type="text/javascript">
       <xsl:comment> </xsl:comment>
     </script>

   <script type="text/javascript">
     var label_score="";
     var label_moreres="";
     
     jQuery(document).ready(function() {
       var offset = 220;
       var duration = 500;
       jQuery(window).scroll(function() {
         if (jQuery(this).scrollTop() > offset) {
           jQuery('.back-to-top').fadeIn(duration);
         } else {
           jQuery('.back-to-top').fadeOut(duration);
         }
       });
     
       jQuery('.back-to-top').click(function(event) {
         event.preventDefault();
         jQuery('html, body').animate({scrollTop: 0}, duration);
         return false;
       })
     });
   </script>
 </xsl:template>


 <!-- generate navbar -->
 <xsl:template name="gennavbar">
   <div id="header" class="navbar-nav navbar-fixed-top">
     <div class="navbar-inner">
       <div class="container-fluid">
         <a href="index.html" class="navbar-brand">
           <xsl:copy-of select="/html:html/html:head/html:title/text()" />
         </a>

         <div class="pull-right">
           <xsl:call-template name="topicnavbar"/>
         </div>
       </div>
     </div>
   </div>
 </xsl:template>

 <!-- generate TOC -->
 <xsl:template name="gentoc">
   <xsl:param name="modid" select="@id" />
   <xsl:param name="modtitle" select="''" />
   <!--
   <div class="btn-list col-xs-12">
     <button type="button" data-target="#navbarCollapse" data-toggle="collapse" class="glyphicon glyphicon-list visible-xs btn-lg col-xs-12">
     </button>
   </div>
   -->
   
   <div id="navbarCollapse" class="collapse navbar-collapse sidebar">
     <div id="navbarCollapse-in" class="col-xs-12">
       <div id="search" class="well col-sm-12">
	 <div class="input-group">
	   <input type="text" id="ksearchinput" placeholder=" {kfp:variable(string($translationfile),'TitreRecherche')}" class="col-sm-11 col-xs-11 col-md-9">
	   </input>
	   <span class="input-group-btn">
	     <button class="btn btn-info" id="search_btn" type="button" title="Rechercher">
               <i class="glyphicon glyphicon-search icon-white"></i>
	     </button>
	   </span>
	 </div>
     </div>
     
        
     <div id="search_results" class="well navbar-nav list-group list-unstyled col-sm-12" style="display:none">
          
       <h5>
         <i class="glyphicon glyphicon-search icon-white"></i>
         <xsl:value-of select="kfp:variable(string($translationfile),'ResRecherche')"/>
         <span class="pull-right">
           <a href="#" id="searchclose">
             <i class="glyphicon glyphicon-remove"></i>
           </a>
         </span>
       </h5>
            
       <ul id="ksearchmenu" class="col-sm-12"></ul>
          
     </div>
          
     <xsl:if test="/html:html/html:head/html:meta[@scheme='user_condition']">
       <div class="well navbar-nav  col-sm-12">
	 <h5><xsl:value-of select="kfp:variable(string($translationfile),'conditions')"/></h5>
	 <div id="userconditions">
	 </div>
       </div>
     </xsl:if>

     <div id="menu" class="well navbar-nav col-sm-12">
       <h5><xsl:value-of select="kfp:variable(string($translationfile),'TdmTitre')"/></h5>
       <ul class="menu-list list-group list-unstyled">
         <xsl:apply-templates select="/html:html/html:body/html:div[@class='section' or @class='topic']" mode="gentoc">
           <xsl:with-param name="modid" select="$modid" />
           <xsl:with-param name="modtitle" select="$modtitle" />
         </xsl:apply-templates>
       </ul>
       <xsl:if test="//html:div[starts-with(@class,'INDEX')]">
         <ul class="menu-list list-group list-unstyled">
           <li>
             <xsl:attribute name="class">
               <xsl:text>list-group-item</xsl:text>
               <xsl:if test="$modid = 'alphaindex'"> active</xsl:if>
             </xsl:attribute>
             <a href="alphaindex.html">
               <i class="glyphicon glyphicon-list-alt"></i>
               <xsl:value-of select="kfp:variable(string($translationfile),'AlphaIndexTitre')" />
             </a>
           </li>
         </ul>
       </xsl:if>
     </div>
   </div>
   </div>

 </xsl:template>




 <!-- TODO template menu selection criteres -->

 <xsl:template name="critsel">
             <!-- Dans le cas où il y a trois a-->
          <div id="os" class="well navbar-nav col-md-12 col-sm-12">
            <h5 class="col-md-12 col-sm-3">Système d'exploitation</h5>
              <ul class="col-md-12 col-sm-9 list-group list-unstyled">
                
                  <li class="list-group-item text-center col-md-4 col-sm-4 col-lg-4 col-xs-4">
                    <a href="index.html">
                      Windows
                    </a>
                  </li>
                  <li class="list-group-item text-center col-md-4 col-sm-4 col-lg-4 col-xs-4">
                    <a href="index.html">
                      Windows
                    </a>
                  </li>
                  <li class="list-group-item text-center col-md-4 col-sm-4 col-lg-4 col-xs-4">
                    <a href="index.html">
                      Windows
                    </a>
                  </li>                
              </ul>

              
            </div>

            <!-- Dans le cas où il y a deux a-->
            <div id="format" class="well navbar-nav col-md-12 col-sm-12">
            <h5 class="col-md-12 col-sm-3">Format</h5>
              <ul class="col-md-12 col-sm-9 list-group list-unstyled">
                
                  <li class="list-group-item text-center col-md-6 col-sm-6 col-lg-6 col-xs-6">
                    <a href="index.html">
                      .odt
                    </a>
                  </li>
                  <li class="list-group-item text-center col-md-6 col-sm-6 col-lg-6 col-xs-6">
                    <a href="index.html">
                      .doc
                    </a>
                  </li>
              </ul>
              
              
            </div>

            <!-- Dans le cas où il y a un seul a-->
            <div id="langue" class="well navbar-nav col-md-12 col-sm-12">
            <h5 class="col-md-12 col-sm-3">Langue</h5>
              <ul class="col-md-12 col-sm-9 list-group list-unstyled">
                
                  <li class="list-group-item  text-center col-md-12 col-sm-12 col-lg-12 col-xs-12">
                    <a href="index.html">
                      Français
                    </a>
                  </li>
                                 
              </ul>
              
              
            </div>


 </xsl:template>















 <!-- topic title -->
 <xsl:template name="modtitle">
   <xsl:param name="modid">
     <xsl:value-of select="@id" />
   </xsl:param>
   <xsl:variable name="mod" select="//html:div[@id = $modid]" />
   <xsl:apply-templates select="($mod//html:h1|$mod//html:h2|$mod//html:h3|$mod//html:h4|$mod//html:h5|$mod//html:h6|$mod//html:dt)[1]" mode="TOCtitle" />
 </xsl:template>

 <!-- topic filename -->
 <xsl:template name="modfile">
   <xsl:param name="modid">
     <xsl:value-of select="@id" />
   </xsl:param>
   <xsl:variable name="mod" select="//html:div[@id = $modid]" />
   <xsl:choose>
     <xsl:when test="not($mod/preceding::html:div[@class='topic'][1])">
       <xsl:text>index</xsl:text>
     </xsl:when>
     <xsl:when test="$mod/html:div[@class='topicinfo']/html:p[html:span[@class='infolabel']='topic_file']">
       <xsl:value-of select="$mod/html:div[@class='topicinfo']/html:p[html:span[@class='infolabel']='topic_file']/html:span[@class='infovalue']" />
     </xsl:when>
     <xsl:otherwise>
       <xsl:value-of select="$modid" />
     </xsl:otherwise>
   </xsl:choose>
   <xsl:text>.html</xsl:text>
 </xsl:template>

 <!--  topic link -->
 <xsl:template name="modhref">
   <xsl:param name="modid" />
   <xsl:variable name="topid">
     <xsl:choose>
       <xsl:when test="contains($modid,'_')">
         <xsl:value-of select="substring-before($modid,'_')" />
       </xsl:when>
       <xsl:otherwise>
         <xsl:value-of select="$modid" />
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>
   <xsl:variable name="mod" select="//html:div[@id = $topid]" />
   <xsl:choose>
     <xsl:when test="not($mod/preceding::html:div[@class='topic'][1])">
       <xsl:text>index</xsl:text>
     </xsl:when>
     <xsl:when test="$mod/html:div[@class='topicinfo']/html:p[html:span[@class='infolabel']='topic_file']">
       <xsl:value-of select="$mod/html:div[@class='topicinfo']/html:p[html:span[@class='infolabel']='topic_file']/html:span[@class='infovalue']" />
     </xsl:when>
     <xsl:otherwise>
       <xsl:value-of select="$topid" />
     </xsl:otherwise>
   </xsl:choose>
   <xsl:text>.html</xsl:text>
   <xsl:if test="contains($modid,'_')">
     <xsl:text>#</xsl:text>
     <xsl:value-of select="substring-after($modid,'_')" />
   </xsl:if>
 </xsl:template>

 <!-- fixed slashes -->
 <xsl:template name="slashes">
   <xsl:param name="text" />
   <xsl:choose>
     <xsl:when test="contains($text,'\')">
       <xsl:value-of select="substring-before($text,'\')" />
       <xsl:text>/</xsl:text>
       <xsl:call-template name="slashes">
         <xsl:with-param name="text" select="substring-after($text,'\')" />
       </xsl:call-template>
     </xsl:when>
     <xsl:otherwise>
       <xsl:value-of select="$text" />
     </xsl:otherwise>
   </xsl:choose>
 </xsl:template>

 <!-- quote text -->
 <xsl:template name="trquot">
   <xsl:param name="text" />
   <xsl:choose>
     <xsl:when test='contains($text,"&apos;")'>
       <xsl:value-of select='substring-before($text,"&apos;")' />
       <xsl:text>\&apos;</xsl:text>
       <xsl:call-template name="trquot">
         <xsl:with-param name="text" select='substring-after($text,"&apos;")' />
       </xsl:call-template>
     </xsl:when>
     <xsl:when test='contains($text, "&#xa;")'>
       <xsl:value-of select='substring-before($text,"&#xa;")' />
       <xsl:text> </xsl:text>
       <xsl:call-template name="trquot">
         <xsl:with-param name="text" select='substring-after($text,"&#xa;")' />
       </xsl:call-template>
     </xsl:when>
     <xsl:when test='contains($text,"&#x0A;")'>
       <xsl:value-of select='substring-before($text,"&#x0A;")' />
       <xsl:text> </xsl:text>
       <xsl:call-template name="trquot">
         <xsl:with-param name="text" select='substring-after($text,"&#x0A;")' />
       </xsl:call-template>
     </xsl:when>
     <xsl:otherwise>
       <xsl:value-of select="$text" />
     </xsl:otherwise>
   </xsl:choose>
 </xsl:template>

 <!-- get translations -->
 <xsl:template name="gettext">
   <xsl:param name="label" />
   <xsl:value-of select="kfp:variable(string($translationfile),string($label))" />
 </xsl:template>
</xsl:stylesheet>
