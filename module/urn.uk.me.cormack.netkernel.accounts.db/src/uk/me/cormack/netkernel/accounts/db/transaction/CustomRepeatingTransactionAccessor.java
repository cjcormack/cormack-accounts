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
import uk.me.cormack.netkernel.accounts.db.AuditUtil;

import java.util.Date;


public class CustomRepeatingTransactionAccessor extends DatabaseAccessorImpl {
  @Override
  public void onNew(INKFRequestContext aContext, DatabaseUtil util) throws Exception {
    String nextIdSql= "SELECT nextval('accounts_transaction_id_seq') AS id;";
    IHDSNode nextIdNode= util.issueSourceRequest("active:sqlPSQuery",
                                                 IHDSNode.class,
                                                 new ArgByValue("operand", nextIdSql));
    Long nextId= (Long)nextIdNode.getFirstValue("//id");

    String insertSql= "INSERT INTO public.accounts_transaction (\n" +
                      "         id,\n" +
                      "         account_id,\n" +
                      "         repeating_transaction_id,\n" +
                      "         transaction_date,\n" +
                      "         transaction_type_id,\n" +
                      "         description,\n" +
                      "         amount\n" +
                      ") SELECT ?," +
                      "         account_id,\n" +
                      "         id,\n" +
                      "         ?,\n" +
                      "         ?,\n" +
                      "         ?,\n" +
                      "         ?\n" +
                      "  FROM   public.accounts_repeating_transaction\n" +
                      "  WHERE  id=?;";
    util.issueSourceRequest("active:sqlPSUpdate",
                            null,
                            new ArgByValue("operand", insertSql),
                            new ArgByValue("param", nextId),
                            new ArgByValue("param", new java.sql.Date(aContext.source("arg:date", Date.class).getTime())),
                            new ArgByValue("param", aContext.source("arg:transactionTypeId")),
                            new ArgByValue("param", aContext.source("arg:description")),
                            new ArgByValue("param", aContext.source("arg:amount")),
                            new ArgByValue("param", aContext.source("arg:repeatingTransactionId")));

    AuditUtil.logTransactionAudit(util, nextId, aContext.source("arg:userId", Long.class),
                                  AuditUtil.AuditOperation.ADD, "Transaction Created from Repeating Transaction");

    util.cutGoldenThread("cormackAccounts:accounts", "cormackAccounts:transactions");
  }
}
