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
                xmlns:xrl="http://netkernel.org/xrl"
                exclude-result-prefixes="xs"
                version="2.0">
  <xsl:param name="account"/>
  <xsl:param name="isCurrent" as="xs:boolean"/>
  <xsl:param name="month" as="xs:string"/>
  <xsl:param name="year" as="xs:string"/>

  <xsl:template match="@* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="accounts:id">
    <xsl:value-of select="$account//id"/>
  </xsl:template>

  <xsl:template match="accounts:name">
    <xsl:value-of select="$account//name"/>
  </xsl:template>

  <xsl:template match="accounts:balance">
    <xsl:value-of select="format-number($account//current_balance, '£#,##0.00')"/>
  </xsl:template>

  <xsl:template match="accounts:accountType">
    <xsl:choose>
      <xsl:when test="xs:boolean($account//simple_account/text())">
        <xsl:text>Simple Account</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>Normal Account</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@*[contains(., '${accounts:id}')]">
    <xsl:attribute name="{name()}" select="replace(., '\$\{accounts:id\}', $account//id)"/>
  </xsl:template>

  <xsl:template match="@*[contains(., '${accounts:month}')]">
    <xsl:attribute name="{name()}" select="replace(., '\$\{accounts:month\}', $month)"/>
  </xsl:template>

  <xsl:template match="@*[contains(., '${accounts:year}')]">
    <xsl:attribute name="{name()}" select="replace(., '\$\{accounts:year\}', $year)"/>
  </xsl:template>

  <xsl:template match="accounts:simpleAccount">
    <xsl:if test="xs:boolean($account//simple_account/text())">
      <xsl:apply-templates select="*"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="accounts:normalAccount">
    <xsl:if test="not(xs:boolean($account//simple_account/text()))">
      <xsl:apply-templates select="node()"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="accounts:notCurrent">
    <xsl:if test="not($isCurrent)">
      <xsl:apply-templates select="node()"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="accounts:monthName">
    <xsl:variable name="normalizedMonth" select="concat(if (string-length($month)=1) then '0' else '', $month)"/>

    <xsl:value-of select="format-date(xs:date(concat($year, '-', $normalizedMonth, '-01')), '[MNn] [Y0001]', 'en', (), ())"/>
  </xsl:template>
</xsl:stylesheet>
