/*
 * Copyright (C) 2012 by Chris Cormack
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package uk.me.cormack.netkernel.accounts.db.transaction;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponse;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernelroc.mod.layer2.ArgByValue;
import org.netkernelroc.mod.layer2.DatabaseAccessorImpl;
import org.netkernelroc.mod.layer2.DatabaseUtil;

import java.util.Date;

public class TransactionAccessor extends DatabaseAccessorImpl {
  @Override
  public void onExists(INKFRequestContext aContext, DatabaseUtil util) throws Exception {
    String sql= "SELECT id\n" +
                "FROM   public.accounts_transaction\n" +
                "WHERE  id=?;";

    INKFResponse resp= util.issueSourceRequestAsResponse("active:sqlPSBooleanQuery",
                                                         new ArgByValue("operand", sql),
                                                         new ArgByValue("param", aContext.source("arg:id")));

    resp.setHeader("no-cache", null);
    util.attachGoldenThread("cormackAccounts:all", "cormackAccounts:transactions");
  }

  @Override
  public void onNew(INKFRequestContext aContext, DatabaseUtil util) throws Exception {
    String nextIdSql= "SELECT nextval('accounts_account_id_seq') AS id;";
    IHDSNode nextIdNode= util.issueSourceRequest("active:sqlPSQuery",
                                                 IHDSNode.class,
                                                 new ArgByValue("operand", nextIdSql));
    Long nextId= (Long)nextIdNode.getFirstValue("//id");

    String insertSql= "INSERT INTO public.accounts_transaction (\n" +
                      "     id,\n" +
                      "     account_id,\n" +
                      "     transaction_date,\n" +
                      "     transaction_type_id,\n" +
                      "     description,\n" +
                      "     amount\n" +
                      ") VALUES (\n" +
                      "     ?,\n" +
                      "     ?,\n" +
                      "     ?,\n" +
                      "     ?,\n" +
                      "     ?,\n" +
                      "     ?\n" +
                      ");";
    util.issueSourceRequest("active:sqlPSUpdate",
                            null,
                            new ArgByValue("operand", insertSql),
                            new ArgByValue("param", nextId),
                            new ArgByValue("param", aContext.source("arg:accountId")),
                            new ArgByValue("param", new java.sql.Date(aContext.source("arg:date", Date.class).getTime())),
                            new ArgByValue("param", aContext.source("arg:transactionTypeId")),
                            new ArgByValue("param", aContext.source("arg:description")),
                            new ArgByValue("param", aContext.source("arg:amount")));

    String updateBalanceSql= "UPDATE public.accounts_account\n" +
                             "SET    current_balance=(SELECT   ( SELECT opening_balance\n" +
                             "                                   FROM   public.accounts_account\n" +
                             "                                   WHERE  id=accounts_transaction.account_id)+\n" +
                             "                                 coalesce(\n" +
                             "                                     ( SELECT sum(amount)\n" +
                             "                                       FROM   public.accounts_transaction AS inner_transaction\n" +
                             "                                       WHERE  inner_transaction.account_id=accounts_transaction.account_id\n" +
                             "                                       AND    ( inner_transaction.transaction_date < accounts_transaction.transaction_date\n" +
                             "                                          OR    ( inner_transaction.transaction_date=accounts_transaction.transaction_date\n" +
                             "                                             AND  inner_transaction.id < accounts_transaction.id)\n" +
                             "                                       )\n" +
                             "                                     ),\n" +
                             "                                     0) + amount AS balance\n" +
                             "                        FROM     public.accounts_transaction\n" +
                             "                        WHERE    account_id=3\n" +
                             "                        ORDER BY transaction_date DESC,\n" +
                             "                                 id DESC\n" +
                             "                        LIMIT    1)\n" +
                             "WHERE  id=?;";
    util.issueSourceRequest("active:sqlPSUpdate",
                            null,
                            new ArgByValue("operand", updateBalanceSql),
                            new ArgByValue("param", aContext.source("arg:accountId")));

    String recordSql= "INSERT INTO public.accounts_transaction_audit (\n" +
                      "    operation_time," +
                      "    transaction_id,\n" +
                      "    user_id,\n" +
                      "    operation_id,\n" +
                      "    description\n" +
                      ") VALUES (\n" +
                      "    NOW(),\n" +
                      "    ?,\n" +
                      "    ?,\n" +
                      "    ( SELECT id\n" +
                      "      FROM   public.accounts_transaction_audit_operation\n" +
                      "      WHERE operation='ADD'),\n" +
                      "    'Transaction added'\n" +
                      ");";
    util.issueSourceRequest("active:sqlPSUpdate",
                            null,
                            new ArgByValue("operand", recordSql),
                            new ArgByValue("param", nextId),
                            new ArgByValue("param", aContext.source("arg:userId")));

    util.cutGoldenThread("cormackAccounts:accounts", "cormackAccounts:transactions");
  }

  @Override
  public void onSink(INKFRequestContext aContext, DatabaseUtil util) throws Exception {
    Long id= aContext.source("arg:id", Long.class);
    boolean checked= aContext.source("arg:checked", Boolean.class);

    String sql= "UPDATE public.accounts_transaction\n" +
                "SET    checked=?\n" +
                "WHERE  id=?;";

    util.issueSourceRequest("active:sqlPSUpdate",
                            null,
                            new ArgByValue("operand", sql),
                            new ArgByValue("param", checked),
                            new ArgByValue("param", id));

    String recordSql= "INSERT INTO public.accounts_transaction_audit (\n" +
                      "    operation_time," +
                      "    transaction_id,\n" +
                      "    user_id,\n" +
                      "    operation_id,\n" +
                      "    description\n" +
                      ") VALUES (\n" +
                      "    NOW(),\n" +
                      "    ?,\n" +
                      "    ?,\n" +
                      "    ( SELECT id\n" +
                      "      FROM   public.accounts_transaction_audit_operation\n" +
                      "      WHERE operation=?),\n" +
                      "    'Transaction added'\n" +
                      ");";
    util.issueSourceRequest("active:sqlPSUpdate",
                            null,
                            new ArgByValue("operand", recordSql),
                            new ArgByValue("param", id),
                            new ArgByValue("param", aContext.source("arg:userId")),
                            new ArgByValue("param", checked ? "CHECK" : "UNCHECK"));

    util.cutGoldenThread( "cormackAccounts:transactions");
  }
}