<?xml version="1.0"?>
<!DOCTYPE stylesheet [
<!ENTITY space "<xsl:text> </xsl:text>">
<!ENTITY cr "<xsl:text>
</xsl:text>">
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns="http://maven.apache.org/POM/4.0.0" xmlns:m="http://maven.apache.org/POM/4.0.0" exclude-result-prefixes="m">
  <xsl:output method="xml" indent="yes"/>
  
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="m:dependencies">
<description>JiBX/WS is a framework for creating fast, simple web services.</description>
&cr;&space;&space;<licenses>
&cr;&space;&space;&space;&space;<license>
&cr;&space;&space;&space;&space;&space;&space;<name>BSD</name>
&cr;&space;&space;&space;&space;&space;&space;<url>http://jibx.sourceforge.net/jibx-license.html</url>
&cr;&space;&space;&space;&space;&space;&space;<distribution>repo</distribution>
&cr;&space;&space;&space;&space;</license>
&cr;&space;&space;</licenses>
&cr;&space;&space;
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
