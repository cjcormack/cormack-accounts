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

package uk.me.cormack.netkernel.accounts.db.repeatingTransaction;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponse;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernelroc.mod.layer2.ArgByValue;
import org.netkernelroc.mod.layer2.DatabaseAccessorImpl;
import org.netkernelroc.mod.layer2.DatabaseUtil;
import uk.me.cormack.netkernel.accounts.db.AuditUtil;

public class RepeatingTransactionAccessor extends DatabaseAccessorImpl {
  @Override
  public void onExists(INKFRequestContext aContext, DatabaseUtil util) throws Exception {
    String sql= "SELECT id\n" +
                "FROM   public.accounts_repeating_transaction\n" +
                "WHERE  id=?;";

    INKFResponse resp= util.issueSourceRequestAsResponse("active:sqlPSBooleanQuery",
                                                         new ArgByValue("operand", sql),
                                                         new ArgByValue("param", aContext.source("arg:id")));

    resp.setHeader("no-cache", null);
    util.attachGoldenThread("cormackAccounts:all", "cormackAccounts:repeatingTransactions");
  }

  @Override
  public void onSource(INKFRequestContext aContext, DatabaseUtil util) throws Exception {
    String sql= "SELECT id,\n" +
                "       account_id,\n" +
                "       description,\n" +
                "       apply_automatically,\n" +
                "       transaction_type_id AS type,\n" +
                "       abs(amount) AS amount,\n" +
                "       CASE WHEN amount > 0 THEN 'credit'\n" +
                "            ELSE 'debit'\n" +
                "       END AS mode\n" +
                "FROM   public.accounts_repeating_transaction\n" +
                "WHERE  id=?;";

    INKFResponse resp= util.issueSourceRequestAsResponse("active:sqlPSQuery",
                                                         new ArgByValue("operand", sql),
                                                         new ArgByValue("param", aContext.source("arg:id")));

    resp.setHeader("no-cache", null);
    util.attachGoldenThread("cormackAccounts:all", "cormackAccounts:repeatingTransactions");
  }

  @Override
  public void onNew(INKFRequestContext aContext, DatabaseUtil util) throws Exception {
    String nextIdSql= "SELECT nextval('accounts_direct_debit_id_seq') AS id;";
    IHDSNode nextIdNode= util.issueSourceRequest("active:sqlPSQuery",
                                                 IHDSNode.class,
                                                 new ArgByValue("operand", nextIdSql));
    Long nextId= (Long)nextIdNode.getFirstValue("//id");

    String insertSql= "INSERT INTO public.accounts_repeating_transaction (\n" +
                      "     id,\n" +
                      "     account_id,\n" +
                      "     description,\n" +
                      "     amount,\n" +
                      "     apply_automatically,\n" +
                      "     transaction_type_id\n" +
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
                            new ArgByValue("param", aContext.source("arg:description")),
                            new ArgByValue("param", aContext.source("arg:amount")),
                            new ArgByValue("param", aContext.source("arg:applyAutomatically")),
                            new ArgByValue("param", aContext.source("arg:transactionTypeId")));

    AuditUtil.logRepeatingTransactionAudit(util, nextId, aContext.source("arg:userId", Long.class), AuditUtil.AuditOperation.ADD, "Repeating Transaction Created");

    util.cutGoldenThread("cormackAccounts:accounts", "cormackAccounts:repeatingTransactions");
  }

  @Override
  public void onSink(INKFRequestContext aContext, DatabaseUtil util) throws Exception {
    String updateSql= "UPDATE public.accounts_repeating_transaction\n" +
                      "SET    description=?,\n" +
                      "       amount=?,\n" +
                      "       apply_automatically=?,\n" +
                      "       transaction_type_id=?\n" +
                      "WHERE  id=?;";
    util.issueSourceRequest("active:sqlPSUpdate",
                            null,
                            new ArgByValue("operand", updateSql),
                            new ArgByValue("param", aContext.source("arg:description")),
                            new ArgByValue("param", aContext.source("arg:amount")),
                            new ArgByValue("param", aContext.source("arg:applyAutomatically")),
                            new ArgByValue("param", aContext.source("arg:transactionTypeId")),
                            new ArgByValue("param", aContext.source("arg:id")));

    AuditUtil.logRepeatingTransactionAudit(util, aContext.source("arg:id", Long.class), aContext.source("arg:userId", Long.class),
                                           AuditUtil.AuditOperation.MODIFY, "Repeating Transaction Updated");

    util.cutGoldenThread("cormackAccounts:accounts", "cormackAccounts:repeatingTransactions");
  }
}
