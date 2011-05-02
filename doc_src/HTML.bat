if %1.==. exit /b

call Xincludes %1
call classpath

cd ..\docs\
md %1
cd %1
md images
cd ..
cd ..\doc_src

java com.icl.saxon.StyleSheet %1\index.xml c:\dbk\html\chunk.xsl use.id.as.filename=1 base.dir=../docs/%1/  chunk.section.depth=0 chunk.first.sections=0 chunker.output.encoding=UTF-8 use.extensions=1 chapter.autolabel=1 section.autolabel=1 tablecolumns.extension=0 toc.max.depth=2 generate.toc="book toc,title,figure,table chapter toc appendix toc" generate.index=1 html.stylesheet=OmegaT.css saxon.character.representation=native;decimal

java com.icl.saxon.StyleSheet -o ..\docs\%1\instantStartGuideNoTOC.html %1\InstantStartGuide.xml docbook-utf8.xsl base.dir=../docs/%1/ chunk.section.depth=0 chunk.first.sections=0 use.extensions=1 chapter.autolabel=0 section.autolabel=1 tablecolumns.extension=0 toc.max.depth=0 generate.toc=0 generate.index=0 html.stylesheet=OmegaT.css

copy %1\images ..\docs\%1\images
copy %1\license.txt ..\docs\%1
copy %1\version.properties ..\docs\%1
copy %1\OmegaT.css ..\docs\%1