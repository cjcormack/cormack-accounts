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

package uk.me.cormack.netkernel.accounts.web.balanceHistory.update;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernel.layer0.representation.impl.HDSBuilder;
import org.netkernelroc.mod.layer2.*;
import uk.me.cormack.netkernel.accounts.web.common.UrlUtil;

import java.math.BigDecimal;

public class DoUpdateBalanceAccessor extends HttpLayer2AccessorImpl {
  @Override
  public void onPost(INKFRequestContext aContext, HttpUtil util) throws Exception {
    String id= aContext.source("arg:id", String.class);
    String balanceString= aContext.source("httpRequest:/param/balance", String.class).trim();
    BigDecimal balance= null;

    if (util.issueExistsRequest("cormackAccounts:db:account",
                                new ArgByValue("id", id))) {

      IHDSNode accountDetails= util.issueSourceRequest("cormackAccounts:db:account",
                                                       IHDSNode.class,
                                                       new Arg("id", "arg:id"));

      boolean valid= true;
      HDSBuilder reasonsBuilder= new HDSBuilder();
      reasonsBuilder.pushNode("div");
      reasonsBuilder.addNode("p", "Updating account balance failed for the following reasons: ");
      reasonsBuilder.pushNode("ul");

      if (balanceString.equals("")) {
        valid= false;
        reasonsBuilder.addNode("li", "Balance must be supplied");
      } else {
        try {
          balance= new BigDecimal(balanceString);
        } catch (Exception e) {
          valid= false;
          reasonsBuilder.addNode("li", "Balance must be numeric");
        }
      }

      if (valid) {
        util.issueSinkRequest("cormackAccounts:db:account:balance",
                              null,
                              new ArgByValue("id", id),
                              new ArgByValue("balance", balance),
                              new ArgByValue("userId", aContext.source("session:/currentUser")));
        aContext.sink("session:/message/class", "success");
        aContext.sink("session:/message/title", "Account successfully updated");
        aContext.sink("session:/message/content", "Account balance for '" + accountDetails.getFirstValue("//name") +
                                                  "' has been successfully updated");

        aContext.sink("httpResponse:/redirect", UrlUtil.resolve(aContext,
                                                                "meta:cormackAccounts:web:account:view",
                                                                new Arg("id", id)));
      } else {
        aContext.sink("session:/message/class", "error");
        aContext.sink("session:/message/title", "Account balance update failed");
        aContext.sink("session:/message/content", reasonsBuilder.getRoot());

        if (aContext.exists("httpRequest:/params")) {
          aContext.sink("session:/formData/name", "account-update-balance");
          aContext.sink("session:/formData/params", aContext.source("httpRequest:/params"));
        }

        aContext.sink("httpResponse:/redirect", UrlUtil.resolve(aContext,
                                                "meta:cormackAccounts:web:account:updateBalance",
                                                new Arg("id", id)));
      }
    } else {
      aContext.sink("httpResponse:/redirect", UrlUtil.resolve(aContext,
                                                              "meta:cormackAccounts:web:account:view",
                                                              new Arg("id", id)));
    }
  }
}
