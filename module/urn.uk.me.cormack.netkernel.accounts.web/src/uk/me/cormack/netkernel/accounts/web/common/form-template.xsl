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
                xmlns:accounts="http://cormack.me.uk/netkernel/accounts"
                version="2.0">
  <xsl:param name="params"/>
  
  <xsl:template match="@* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="input[@type='checkbox'][@name][not(@accounts:overrideValue='false')]">
    <xsl:variable name="name" select="@name"/>
    <xsl:copy>
      <xsl:apply-templates select="@* except @checked"/>
      <xsl:if test="$params//*[local-name()=$name] and not($params//*[local-name()=$name]='false')">
        <xsl:attribute name="checked" select="'checked'"/>
      </xsl:if>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="input[not(@type='checkbox')][@name][not(@accounts:overrideValue='false')]">
    <xsl:variable name="name" select="@name"/>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:attribute name="value">
        <xsl:variable name="value" select="$params//*[local-name()=$name]"/>
        <xsl:choose>
          <xsl:when test="$value">
            <xsl:choose>
              <xsl:when test="@accounts:type='numeric'">
                <xsl:value-of select="if (matches($value, '[0-9]+(\.[0-9+])?')) then format-number($value, '0.00#########') else $value"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$value"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:when test="@accounts:defaultValueDateNow='true'">
            <xsl:value-of select="format-date(current-date(), '[D01]/[MNn,*-3]/[Y0001]', 'en', (), ())"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="textarea[@name][not(@accounts:overrideValue='false')]">
    <xsl:variable name="name" select="@name"/>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:value-of select="$params//*[local-name()=$name]"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="select[@name][not(@accounts:overrideValue='false')]/option">
    <xsl:variable name="name" select="../@name"/>
    <xsl:variable name="value" select="@value"/>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:if test="$params//*[local-name()=$name]/text()=$value">
        <xsl:attribute name="selected" select="'selected'"/>
      </xsl:if>
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
