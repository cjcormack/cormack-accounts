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
  <xsl:param name="transactionList"/>
  <xsl:param name="accountDetails"/>

  <xsl:template match="@* | node()" mode="#default">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="accounts:eachTransaction">
    <xsl:apply-templates select="$transactionList//row" mode="transactionList">
      <xsl:with-param name="transactionTemplate" select="."/>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="row" mode="transactionList">
    <xsl:param name="transactionTemplate"/>
    <xsl:apply-templates select="$transactionTemplate/*" mode="transaction">
      <xsl:with-param name="currentTransaction" select="."/>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="@* | node()" mode="transaction">
    <xsl:param name="currentTransaction" as="node()"/>
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" mode="#current">
        <xsl:with-param name="currentTransaction" select="$currentTransaction"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="accounts:date" mode="transaction">
    <xsl:param name="currentTransaction" as="node()"/>
    <xsl:value-of select="format-date($currentTransaction//transaction_date, '[D01]/[MNn,*-3]/[Y0001]', 'en', (), ())"/>
  </xsl:template>

  <xsl:template match="accounts:type" mode="transaction">
    <xsl:param name="currentTransaction" as="node()"/>
    <xsl:value-of select="$currentTransaction//transaction_type_id"/>
  </xsl:template>

  <xsl:template match="accounts:description" mode="transaction">
    <xsl:param name="currentTransaction" as="node()"/>
    <xsl:value-of select="$currentTransaction//description"/>
  </xsl:template>

  <xsl:template match="accounts:userId" mode="transaction">
    <xsl:param name="currentTransaction" as="node()"/>
    <xsl:value-of select="$currentTransaction//user_id"/>
  </xsl:template>

  <xsl:template match="accounts:debitAmount" mode="transaction">
    <xsl:param name="currentTransaction" as="node()"/>
    <xsl:if test="not(number($currentTransaction//amount) lt 0)">
      <xsl:value-of select="format-number($currentTransaction//amount, '£#,##0.00')"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="accounts:creditAmount" mode="transaction">
    <xsl:param name="currentTransaction" as="node()"/>
    <xsl:if test="number($currentTransaction//amount) lt 0">
      <xsl:value-of select="format-number(abs($currentTransaction//amount), '£#,##0.00')"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="accounts:balance" mode="transaction">
    <xsl:param name="currentTransaction" as="node()"/>
    <xsl:value-of select="format-number($currentTransaction//balance, '£#,##0.00')"/>
  </xsl:template>

  <xsl:template match="input[@name='checked']" mode="transaction">
    <xsl:param name="currentTransaction" as="node()"/>
    <xsl:copy>
      <xsl:if test="xs:boolean($currentTransaction//checked)">
        <xsl:attribute name="checked" select="'checked'"/>
      </xsl:if>
      <xsl:apply-templates select="@*, node()" mode="#current">
        <xsl:with-param name="currentTransaction" select="$currentTransaction"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@*[contains(., '${accounts:id}')]" mode="transaction">
    <xsl:param name="currentTransaction" as="node()"/>
    <xsl:attribute name="{name()}" select="replace(., '\$\{accounts:id\}', $currentTransaction//id)"/>
  </xsl:template>

  <xsl:template match="accounts:currentBalance">
    <xsl:value-of select="format-number($accountDetails//current_balance, '£#,##0.00')"/>
  </xsl:template>

  <xsl:template match="accounts:openingBalance">
    <xsl:value-of select="format-number($accountDetails//opening_balance, '£#,##0.00')"/>
  </xsl:template>

  <xsl:template match="@*[contains(., '${accounts:accountId}')]">
    <xsl:attribute name="{name()}" select="replace(., '\$\{accounts:accountId\}', $accountDetails//id)"/>
  </xsl:template>
</xsl:stylesheet>
