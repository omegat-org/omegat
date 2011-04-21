if %1.==. exit /b
call Xincludes %1
cd %1
md html
cd html
md images
cd..
cd..
java com.icl.saxon.StyleSheet %1\index.xml c:\dbk\html\chunk.xsl use.id.as.filename=1 base.dir=%1/html/  chunk.section.depth=0 chunk.first.sections=0 use.extensions=1 chapter.autolabel=1 section.autolabel=1 tablecolumns.extension=0 toc.max.depth=2 generate.toc="book toc,title,figure,table chapter toc appendix toc" generate.index=1
java com.icl.saxon.StyleSheet -o %1\html\instantStartGuideNoTOC.html %1\InstantStartGuide.xml C:\dbk\html\docbook.xsl base.dir=%1/html/ chunk.section.depth=0 chunk.first.sections=0 use.extensions=1 chapter.autolabel=0 section.autolabel=1 tablecolumns.extension=0 toc.max.depth=0 generate.toc=0 generate.index=0

copy %1\images %1\html\images