<?xml version="1.0" encoding="UTF-8" ?>
<!--
  ~ Copyright (C) 2012 by Chris Cormack
  ~
  ~ Permission is hereby granted, free of charge, to any person obtaining a copy
  ~ of this software and associated documentation files (the "Software"), to deal
  ~ in the Software without restriction, including without limitation the rights
  ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  ~ copies of the Software, and to permit persons to whom the Software is
  ~ furnished to do so, subject to the following conditions:
  ~
  ~ The above copyright notice and this permission notice shall be included in
  ~ all copies or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  ~ THE SOFTWARE.
  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xrl="http://netkernel.org/xrl"
                xmlns:accounts="http://cormack.me.uk/netkernel/accounts"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:xhtml="http://www.w3.org/1999/xhtml"
                version="2.0"
                exclude-result-prefixes="xrl accounts saxon">
  <xsl:strip-space elements="*"/>
  <xsl:output exclude-result-prefixes="xrl accounts saxon" />

  <xsl:template match="@* | node()">
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="title">
    <xsl:copy copy-namespaces="no">
      <xsl:value-of select="normalize-space()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="accounts:group">
    <xsl:apply-templates select="node()" mode="#current"/>
  </xsl:template>
  
  <!-- strip XRL attributes -->
  <xsl:template match="@xrl:*"/>
  
  <!-- strip XRL elements -->
  <xsl:template match="xrl:*"/>
  
  <!-- strip accounts attributes -->
  <xsl:template match="@accounts:*"/>
  
  <!-- strip accounts elements -->
  <xsl:template match="accounts:*"/>
  
  <xsl:template match="xhtml:*">
    <xsl:element name="{local-name()}">
      <xsl:apply-templates select="@* | node()"/>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
