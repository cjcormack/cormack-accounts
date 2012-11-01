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
import org.netkernelroc.mod.layer2.Arg;
import org.netkernelroc.mod.layer2.ArgByValue;
import org.netkernelroc.mod.layer2.DatabaseAccessorImpl;
import org.netkernelroc.mod.layer2.DatabaseUtil;
import uk.me.cormack.netkernel.accounts.db.ConfigUtil;

public class LoginAccessor extends DatabaseAccessorImpl {
  @Override
  public void onExists(INKFRequestContext aContext, DatabaseUtil util) throws Exception {
    INKFResponse resp;

    if (util.issueExistsRequest("cormackAccounts:db:user:email",
                                new Arg("email", "arg:email"))) {
      long id= util.issueSourceRequest("cormackAccounts:db:user:email",
                                        Long.class,
                                        new Arg("email", "arg:email"));

      String saltedPassword= PasswordUtil.generateSaltedPassword(util,
                                                                  aContext.source("arg:password", String.class),
                                                                  id + "",
                                                                  ConfigUtil.getSiteSalt(util));

      String saltedSql= "SELECT id\n" +
                        "FROM   accounts_user\n" +
                        "WHERE  email=?\n" +
                        "AND    password=?;";
      if (util.issueSourceRequest("active:sqlPSBooleanQuery",
                                  Boolean.class,
                                  new ArgByValue("operand", saltedSql),
                                  new ArgByValue("param", aContext.source("arg:email")),
                                  new ArgByValue("param", saltedPassword))) {
        resp= aContext.createResponseFrom(true);
      } else {
        resp= aContext.createResponseFrom(false);
      }
    } else {
      resp= aContext.createResponseFrom(false);
    }
    
    resp.setHeader("no-cache", null);
    util.attachGoldenThread("cormackAccounts:all", "cormackAccounts:user");
  }
  
  @Override
  public void onSource(INKFRequestContext aContext, DatabaseUtil util) throws Exception {
    long id = util.issueSourceRequest("cormackAccounts:db:user:email",
                                      Long.class,
                                      new Arg("email", "arg:email"));
    String saltedPassword= PasswordUtil.generateSaltedPassword(util,
                                                               aContext.source("arg:password", String.class),
                                                               id + "",
                                                               ConfigUtil.getSiteSalt(util));

    String sql= "SELECT id\n" +
                "FROM   accounts_user\n" +
                "WHERE  email=?\n" +
                "AND    password=?;";
    INKFResponse resp= util.issueSourceRequestAsResponse("active:sqlPSQuery",
                                                         IHDSNode.class,
                                                         new ArgByValue("operand", sql),
                                                         new ArgByValue("param", aContext.source("arg:email")),
                                                         new ArgByValue("param", saltedPassword));

    resp.setHeader("no-cache", null);
    util.attachGoldenThread("cormackAccounts:all", "cormackAccounts:user");
  }
}
