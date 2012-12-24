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

public class ListAccessor extends DatabaseAccessorImpl {
  @Override
  public void onSource(INKFRequestContext aContext, DatabaseUtil util) throws Exception {
    String sql= "SELECT   accounts_transaction.id,\n" +
                "         accounts_transaction.transaction_date,\n" +
                "         accounts_transaction.description,\n" +
                "         accounts_transaction.transaction_type_id,\n" +
                "         accounts_transaction.amount,\n" +
                "         accounts_transaction.checked,\n" +
                "         ( SELECT opening_balance\n" +
                "           FROM   public.accounts_account\n" +
                "           WHERE  id=accounts_transaction.account_id)+\n" +
                "         coalesce(\n" +
                "             ( SELECT sum(amount)\n" +
                "               FROM   public.accounts_transaction AS inner_transaction\n" +
                "               WHERE  inner_transaction.account_id=accounts_transaction.account_id\n" +
                "               AND    ( inner_transaction.transaction_date < accounts_transaction.transaction_date\n" +
                "                  OR    ( inner_transaction.transaction_date=accounts_transaction.transaction_date\n" +
                "                     AND  inner_transaction.id < accounts_transaction.id)\n" +
                "               )\n" +
                "             ),\n" +
                "             0) + accounts_transaction.amount AS balance\n" +
                "FROM     public.accounts_transaction\n" +
                "WHERE    account_id=?\n" +
                "ORDER BY transaction_date,\n" +
                "         id;";
    INKFResponse resp= util.issueSourceRequestAsResponse("active:sqlPSQuery",
                                                         IHDSNode.class,
                                                         new ArgByValue("operand", sql),
                                                         new ArgByValue("param", aContext.source("arg:id")));
    
    resp.setHeader("no-cache", null);
    util.attachGoldenThread("cormackAccounts:all", "cormackAccounts:accounts", "cormackAccounts:transactions");
  }
}
