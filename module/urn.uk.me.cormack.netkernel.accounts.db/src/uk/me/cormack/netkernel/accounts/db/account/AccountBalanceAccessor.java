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

package uk.me.cormack.netkernel.accounts.db.account;


import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernelroc.mod.layer2.ArgByValue;
import org.netkernelroc.mod.layer2.DatabaseAccessorImpl;
import org.netkernelroc.mod.layer2.DatabaseUtil;

public class AccountBalanceAccessor extends DatabaseAccessorImpl {
  @Override
  public void onSink(INKFRequestContext aContext, DatabaseUtil util) throws Exception {
    Long id= aContext.source("arg:id", Long.class);

    String updateSql= "UPDATE public.accounts_account\n" +
                      "SET    current_balance=?\n" +
                      "WHERE  id=?";
    util.issueSourceRequest("active:sqlPSUpdate",
                            null,
                            new ArgByValue("operand", updateSql),
                            new ArgByValue("param", aContext.source("arg:balance")),
                            new ArgByValue("param", id));

    String registerSql= "INSERT INTO public.accounts_balance_change (\n" +
                        "    operation_time," +
                        "    account_id,\n" +
                        "    user_id,\n" +
                        "    balance\n" +
                        ") VALUES (\n" +
                        "    NOW()," +
                        "    ?," +
                        "    ?," +
                        "    ?" +
                        ")";
    util.issueSourceRequest("active:sqlPSUpdate",
                            null,
                            new ArgByValue("operand", registerSql),
                            new ArgByValue("param", id),
                            new ArgByValue("param", aContext.source("arg:userId")),
                            new ArgByValue("param", aContext.source("arg:balance")));

    util.cutGoldenThread("cormackAccounts:accounts");
  }
}
