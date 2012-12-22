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
  <xsl:param name="balanceList"/>
  <xsl:param name="accountDetails"/>

  <xsl:template match="@* | node()" mode="#default">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="accounts:eachBalance">
    <xsl:apply-templates select="$balanceList//row" mode="balanceList">
      <xsl:with-param name="balanceTemplate" select="."/>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="row" mode="balanceList">
    <xsl:param name="balanceTemplate"/>
    <xsl:apply-templates select="$balanceTemplate/*" mode="balance">
      <xsl:with-param name="currentBalance" select="."/>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="@* | node()" mode="balance">
    <xsl:param name="currentBalance" as="node()"/>
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" mode="#current">
        <xsl:with-param name="currentBalance" select="$currentBalance"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="accounts:date" mode="balance">
    <xsl:param name="currentBalance" as="node()"/>
    <xsl:value-of select="format-dateTime($currentBalance//operation_time, '[D01]/[MNn,*-3]/[Y0001]', 'en', (), ())"/>
  </xsl:template>

  <xsl:template match="accounts:userId" mode="balance">
    <xsl:param name="currentBalance" as="node()"/>
    <xsl:value-of select="$currentBalance//user_id"/>
  </xsl:template>

  <xsl:template match="accounts:balance" mode="balance">
    <xsl:param name="currentBalance" as="node()"/>
    <xsl:value-of select="format-number($currentBalance//balance, '£#,##0.00')"/>
  </xsl:template>

  <xsl:template match="accounts:currentBalance">
    <xsl:value-of select="format-number($accountDetails//current_balance, '£#,##0.00')"/>
  </xsl:template>

  <xsl:template match="@*[contains(., '${accounts:accountId}')]">
    <xsl:attribute name="{name()}" select="replace(., '\$\{accounts:accountId\}', $accountDetails//id)"/>
  </xsl:template>
</xsl:stylesheet>
