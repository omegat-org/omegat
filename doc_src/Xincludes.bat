if %1.==. exit /b
xmllint --xinclude -o %1\index.xml "%1\OmegaTUsersManual_xinclude full.xml"
