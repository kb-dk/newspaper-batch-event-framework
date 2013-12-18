<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"

        >

    <xsl:output omit-xml-declaration="yes" indent="yes"/>
    <xsl:strip-space elements="*"/>
    <xsl:param name="file.storage.path"/>

    <!--<xsl:template match="node()|@*" name="identity">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
-->

    <xsl:template match="//node[substring(@shortName, string-length(@shortName) - 3) = '.jp2']">
        <xsl:value-of select="$file.storage.path"/>
        <xsl:value-of select="translate(@name,'/','_')"/>
        <xsl:text>&#xa;</xsl:text>
    </xsl:template>

</xsl:stylesheet>