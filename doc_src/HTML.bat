if %1.==. exit /b
call Xincludes %1
cd %1
md html
cd html
md images
cd..
cd..
java com.icl.saxon.StyleSheet %1\index.xml c:\dbk\html\chunk.xsl use.id.as.filename=1 base.dir=%1/html/  chunk.section.depth=0 chunk.first.sections=0 use.extensions=1 chapter.autolabel=I section.autolabel=1 tablecolumns.extension=0 toc.max.depth=2 generate.toc="book toc,title,figure,table chapter toc appendix toc" generate.index=1
copy %1\images %1\html\images