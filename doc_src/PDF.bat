if %1.==. exit /b
call Xincludes %1
call fo %1
ant -Dlanguage=%1
