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
  <xsl:param name="totalBalance" as="xs:decimal"/>
  <xsl:param name="accountList"/>
  
  <xsl:template match="@* | node()" mode="#default">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="accounts:totalBalance">
    <xsl:value-of select="format-number($totalBalance, '£#,##0.00')"/>
  </xsl:template>

  <xsl:template match="accounts:noAccounts">
    <xsl:if test="count($accountList//row)=0">
      <xsl:apply-templates select="*"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="accounts:account">
    <xsl:apply-templates select="$accountList//row" mode="accountList">
      <xsl:with-param name="accountTemplate" select="."/>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="row" mode="accountList">
    <xsl:param name="accountTemplate"/>
    <xsl:apply-templates select="$accountTemplate/*" mode="account">
      <xsl:with-param name="currentAccount" select="."/>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="@* | node()" mode="account">
    <xsl:param name="currentAccount"/>
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" mode="#current">
        <xsl:with-param name="currentAccount" select="$currentAccount"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="accounts:id" mode="account">
    <xsl:param name="currentAccount" as="node()"/>
    <xsl:value-of select="$currentAccount/id"/>
  </xsl:template>

  <xsl:template match="accounts:name" mode="account">
    <xsl:param name="currentAccount" as="node()"/>
    <xsl:value-of select="$currentAccount//name"/>
  </xsl:template>

  <xsl:template match="accounts:description" mode="account">
    <xsl:param name="currentAccount" as="node()"/>
    <xsl:value-of select="$currentAccount//description"/>
  </xsl:template>

  <xsl:template match="accounts:balance" mode="account">
    <xsl:param name="currentAccount" as="node()"/>
    <xsl:value-of select="format-number($currentAccount//balance, '£#,##0.00')"/>
  </xsl:template>

  <xsl:template match="@*[contains(., '${accounts:id}')]" mode="account">
    <xsl:param name="currentAccount" as="node()"/>
    <xsl:attribute name="{name()}">
      <xsl:if test="$currentAccount//id">
        <xsl:value-of select="replace(., '\$\{accounts:id\}', $currentAccount//id)"/>
      </xsl:if>
    </xsl:attribute>
  </xsl:template>
</xsl:stylesheet>
