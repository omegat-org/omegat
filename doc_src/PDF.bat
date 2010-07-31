if %1.==. exit /b
call Xincludes %1
call fo %1
fop -c fop.xconf -fo %1\pdf\OmegaT_documentation.fo -pdf %1\pdf\OmegaT_documentation.pdf -dpi 1200 -r