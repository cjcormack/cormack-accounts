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
                exclude-result-prefixes="xs"
                version="2.0">
  <xsl:param name="displayName"/>
  
  <xsl:template match="@* | node()" mode="#default">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@*[contains(., '${accounts:displayName}')]">
    <xsl:attribute name="{name()}">
      <xsl:if test="$displayName">
        <xsl:value-of select="replace(., '\$\{accounts:displayName\}', accounts:clean-replace-output($displayName))"/>
      </xsl:if>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="accounts:displayName">
    <xsl:value-of select="$displayName"/>
  </xsl:template>

  <xsl:function name="accounts:clean-replace-output" as="xs:string">
    <xsl:param name="input" as="xs:string"/>
    <xsl:value-of select="replace(replace($input, '\\', '\\\\'), '\$', '\\\$')"/>
  </xsl:function>
</xsl:stylesheet>
