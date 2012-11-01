<?xml version="1.0" encoding="UTF-8"?>
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
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:accounts="http://cormack.me.uk/netkernel/accounts"
                exclude-result-prefixes="#all"
                version="2.0">
  
  <xsl:output method="xhtml"/>
  
  <xsl:param name="content"/>
  
  <xsl:template match="@* | node()" mode="#all">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="accounts:title">
    <xsl:value-of select="$content/accounts:page/accounts:title"/>
  </xsl:template>
  <xsl:template match="accounts:heading">
    <xsl:value-of select="$content/accounts:page/accounts:heading"/>
  </xsl:template>
  
  <xsl:template match="accounts:content" mode="#default">
    <xsl:apply-templates select="$content/accounts:page/accounts:content/*"/>
  </xsl:template>
  
  <xsl:template match="accounts:head" mode="#default">
    <xsl:apply-templates select="$content/accounts:page/accounts:head/*"/>
    <xsl:apply-templates select="$content//accounts:snippet/accounts:head/*"/>
  </xsl:template>
  
  <xsl:template match="accounts:snippet">
    <xsl:apply-templates select="accounts:content/*"/>
  </xsl:template>

  <xsl:function name="accounts:clean-replace-output" as="xs:string">
    <xsl:param name="input" as="xs:string"/>
    <xsl:value-of select="replace(replace($input, '\\', '\\\\'), '\$', '\\\$')"/>
  </xsl:function>
</xsl:stylesheet>