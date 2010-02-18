xjc src/schemas/srx20.xsd -d src -p gen.core.segmentation
xjc src/schemas/filters.xsd -d src -p gen.core.filters
xjc -dtd src/schemas/TBXcoreStructV02.dtd -d src -p gen.core.tbx
pause
