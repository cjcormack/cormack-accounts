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

package uk.me.cormack.netkernel.accounts.db.user;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponse;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernelroc.mod.layer2.ArgByValue;
import org.netkernelroc.mod.layer2.DatabaseAccessorImpl;
import org.netkernelroc.mod.layer2.DatabaseUtil;
import uk.me.cormack.netkernel.accounts.db.ConfigUtil;

public class UserAccessor extends DatabaseAccessorImpl {
  public UserAccessor() {
    declareInhibitCheckForBadExpirationOnMutableResource();
  }

  @Override
  public void onSource(INKFRequestContext aContext, DatabaseUtil util) throws Exception {
    String sql= "SELECT   id,\n" +
                "         email\n" +
                "FROM     accounts_user\n" +
                "WHERE    id=?;";
    INKFResponse resp= util.issueSourceRequestAsResponse("active:sqlPSQuery",
                                                         IHDSNode.class,
                                                         new ArgByValue("operand", sql),
                                                         new ArgByValue("param", aContext.source("arg:id")));

    resp.setHeader("no-cache", null);
    util.attachGoldenThread("cormackAccounts:all", "cormackAccounts:users");
  }

  @Override
  public void onNew(INKFRequestContext aContext, DatabaseUtil util) throws Exception {
    String email= aContext.source("arg:email", String.class);
    String password= aContext.source("arg:password", String.class);

    String nextIdSql= "SELECT nextval('accounts_user_id_seq') AS id;";
    IHDSNode nextIdNode= util.issueSourceRequest("active:sqlPSQuery",
                                                 IHDSNode.class,
                                                 new ArgByValue("operand", nextIdSql));
    Long nextId= (Long)nextIdNode.getFirstValue("//id");

    String encryptedPassword= PasswordUtil.generateSaltedPassword(util,
                                                                  password,
                                                                  nextId + "",
                                                                  ConfigUtil.getSiteSalt(util));

    String newUserSql= "INSERT INTO accounts_user\n" +
                       "(\n" +
                       "    id,\n" +
                       "    email,\n" +
                       "    password\n" +
                       ") VALUES (\n" +
                       "    ?,\n" +
                       "    ?,\n" +
                       "    ?\n" +
                       ");";
    
    util.issueSourceRequest("active:sqlPSUpdate",
                            null,
                            new ArgByValue("operand", newUserSql),
                            new ArgByValue("param", nextId),
                            new ArgByValue("param", email),
                            new ArgByValue("param", encryptedPassword));
    
    aContext.createResponseFrom(nextId);
    
    util.cutGoldenThread("cormackAccounts:users");
  }

  @Override
  public void onSink(INKFRequestContext aContext, DatabaseUtil util) throws Exception {
    long id= aContext.source("arg:id", Long.class);
    String email= aContext.source("arg:email", String.class);
    String password= aContext.source("arg:password", String.class);

    String encryptedPassword= PasswordUtil.generateSaltedPassword(util,
                                                                  password,
                                                                  id + "",
                                                                  ConfigUtil.getSiteSalt(util));

    String updateAccountSql= "UPDATE accounts_user\n" +
                             "SET    email=?,\n" +
                             "       password=?\n" +
                             "WHERE  id=?";
    util.issueSourceRequest("active:sqlPSUpdate",
                            null,
                            new ArgByValue("operand", updateAccountSql),
                            new ArgByValue("param", email),
                            new ArgByValue("param", encryptedPassword),
                            new ArgByValue("param", id));

    util.cutGoldenThread("cormackAccounts:users");
  }
}
