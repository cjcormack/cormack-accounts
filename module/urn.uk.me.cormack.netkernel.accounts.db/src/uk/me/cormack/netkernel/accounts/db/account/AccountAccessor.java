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
import org.netkernel.layer0.nkf.INKFResponse;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernelroc.mod.layer2.ArgByValue;
import org.netkernelroc.mod.layer2.DatabaseAccessorImpl;
import org.netkernelroc.mod.layer2.DatabaseUtil;

public class AccountAccessor extends DatabaseAccessorImpl {
  public AccountAccessor() {
    declareInhibitCheckForBadExpirationOnMutableResource();
  }

  @Override
  public void onExists(INKFRequestContext aContext, DatabaseUtil util) throws Exception {
    String sql= "SELECT id\n" +
                "FROM   accounts_account\n" +
                "WHERE  id=?;";

    INKFResponse resp= util.issueSourceRequestAsResponse("active:sqlPSBooleanQuery",
                                                         new ArgByValue("operand", sql),
                                                         new ArgByValue("param", aContext.source("arg:id")));

    resp.setHeader("no-cache", null);
    util.attachGoldenThread("cormackAccounts:all", "cormackAccounts:accounts");
  }

  @Override
  public void onSource(INKFRequestContext aContext, DatabaseUtil util) throws Exception {
    String sql= "SELECT   id,\n" +
                "         name,\n" +
                "         description,\n" +
                "         opening_balance,\n" +
                "         current_balance,\n" +
                "         simple_account\n" +
                "FROM     accounts_account\n" +
                "WHERE    id=?;";
    INKFResponse resp= util.issueSourceRequestAsResponse("active:sqlPSQuery",
                                                         IHDSNode.class,
                                                         new ArgByValue("operand", sql),
                                                         new ArgByValue("param", aContext.source("arg:id")));
    
    resp.setHeader("no-cache", null);
    util.attachGoldenThread("cormackAccounts:all", "cormackAccounts:accounts");
  }

  @Override
  public void onNew(INKFRequestContext aContext, DatabaseUtil util) throws Exception {
    String nextIdSql= "SELECT nextval('accounts_account_id_seq') AS id;";
    IHDSNode nextIdNode= util.issueSourceRequest("active:sqlPSQuery",
                                                 IHDSNode.class,
                                                 new ArgByValue("operand", nextIdSql));
    Long nextId= (Long)nextIdNode.getFirstValue("//id");

    String insertSql= "INSERT INTO public.accounts_account\n" +
                      "(\n" +
                      "    id,\n" +
                      "    name\n," +
                      "    description,\n" +
                      "    opening_balance,\n" +
                      "    current_balance,\n" +
                      "    simple_account\n" +
                      ") VALUES (\n" +
                      "    ?,\n" +
                      "    ?,\n" +
                      "    ?,\n" +
                      "    ?,\n" +
                      "    ?\n" +
                      ");";
    util.issueSourceRequest("active:sqlPSUpdate",
                            null,
                            new ArgByValue("operand", insertSql),
                            new ArgByValue("param", nextId),
                            new ArgByValue("param", aContext.source("arg:name")),
                            new ArgByValue("param", aContext.source("arg:description")),
                            new ArgByValue("param", aContext.source("arg:balance")),
                            new ArgByValue("param", aContext.source("arg:balance")),
                            new ArgByValue("param", aContext.source("arg:simpleAccount")));

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
                            new ArgByValue("param", nextId),
                            new ArgByValue("param", aContext.source("arg:userId")),
                            new ArgByValue("param", aContext.source("arg:balance")));

    util.cutGoldenThread("cormackAccounts:accounts");

    aContext.createResponseFrom(nextId);
  }

  @Override
  public void onSink(INKFRequestContext aContext, DatabaseUtil util) throws Exception {
    Long id= aContext.source("arg:id", Long.class);

    String sql= "UPDATE accounts_account\n" +
                "SET    name=?,\n" +
                "       description=?\n" +
                "WHERE  id=?";
    util.issueSourceRequest("active:sqlPSUpdate",
                            null,
                            new ArgByValue("operand", sql),
                            new ArgByValue("param", aContext.source("arg:name")),
                            new ArgByValue("param", aContext.source("arg:description")),
                            new ArgByValue("param", id));

    util.cutGoldenThread("cormackAccounts:accounts");
  }
}
