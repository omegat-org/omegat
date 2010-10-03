if %1.==. exit /b
call Xincludes %1
call fo %1
copy %1\images %1\pdf\images
cd %1
cd pdf
fop -c ..\..\fop.xconf -fo OmegaT_documentation.fo -pdf OmegaT_documentation.pdf -dpi 1200 -r