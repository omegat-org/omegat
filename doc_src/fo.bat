if %1.==. exit /b
call classpath

cd %1
md pdf
cd pdf
md images
cd..
cd..
java com.icl.saxon.StyleSheet -o %1\pdf\OmegaT_documentation.fo %1\index.xml c:\dbk\fo\docbook.xsl paper.type=A4 page.margin.inner="18mm" page.margin.outer="12mm" page.margin.top="8mm"  page.margin.bottom="8mm" generate.toc="book toc,title,figure,table"  chapter.autolabel=1 section.autolabel=1  toc.max.depth=2 indent="no" fop.extension=1 insert.olink.pdf.frag=1