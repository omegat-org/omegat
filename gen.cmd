xjc src/schemas/srx20.xsd -d src -p gen.core.segmentation
xjc src/schemas/filters.xsd -d src -p gen.core.filters
xjc src/schemas/tbx.xsd -d src -p gen.core.tbx
xjc src/schemas/project_properties.xsd -d src -p gen.core.project

#xjc src/schemas/tmx11.xsd -dtd -d src -p gen.core.tmx11
# do not forget to change xml:lang:
#    @XmlAttribute(name = "lang", namespace="http://www.w3.org/XML/1998/namespace", required = true)
xjc src/schemas/tmx14.xsd -dtd -d src -p gen.core.tmx14 

pause
