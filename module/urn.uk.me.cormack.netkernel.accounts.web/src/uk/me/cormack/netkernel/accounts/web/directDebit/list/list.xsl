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
  <xsl:param name="directDebitList"/>
  <xsl:param name="accountDetails"/>

  <xsl:template match="@* | node()" mode="#default">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="accounts:empty">
    <xsl:if test="not($directDebitList//row)">
      <xsl:apply-templates select="node()"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="accounts:eachDirectDebit">
    <xsl:apply-templates select="$directDebitList//row" mode="directDebitList">
      <xsl:with-param name="directDebitTemplate" select="."/>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="row" mode="directDebitList">
    <xsl:param name="directDebitTemplate"/>
    <xsl:apply-templates select="$directDebitTemplate/*" mode="directDebit">
      <xsl:with-param name="currentDirectDebit" select="."/>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="@* | node()" mode="directDebit">
    <xsl:param name="currentDirectDebit" as="node()"/>
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" mode="#current">
        <xsl:with-param name="currentDirectDebit" select="$currentDirectDebit"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="accounts:description" mode="directDebit">
    <xsl:param name="currentDirectDebit" as="node()"/>
    <xsl:value-of select="$currentDirectDebit//description"/>
  </xsl:template>

  <xsl:template match="accounts:amount" mode="directDebit">
    <xsl:param name="currentDirectDebit" as="node()"/>
    <xsl:value-of select="format-number(abs($currentDirectDebit//amount), '£#,##0.00')"/>
  </xsl:template>

  <xsl:template match="accounts:totalAmount">
    <xsl:value-of select="format-number(sum($directDebitList//row/amount), '£#,##0.00')"/>
  </xsl:template>

  <xsl:template match="accounts:name">
    <xsl:value-of select="$accountDetails//name"/>
  </xsl:template>

  <xsl:template match="@*[contains(., '${accounts:accountId}')]">
    <xsl:attribute name="{name()}" select="replace(., '\$\{accounts:accountId\}', $accountDetails//id)"/>
  </xsl:template>
</xsl:stylesheet>
