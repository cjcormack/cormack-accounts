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

package uk.me.cormack.netkernel.accounts.web.account.add;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.representation.impl.HDSBuilder;
import org.netkernelroc.mod.layer2.*;
import uk.me.cormack.netkernel.accounts.web.common.UrlUtil;

import java.math.BigDecimal;

public class DoAddAccessor extends HttpLayer2AccessorImpl {
  @Override
  public void onPost(INKFRequestContext aContext, HttpUtil util) throws Exception {
    aContext.setCWU("res:/uk/me/cormack/netkernel/accounts/web/account/add/");

    String name= aContext.source("httpRequest:/param/name", String.class).trim();
    String description= aContext.source("httpRequest:/param/description", String.class).trim();
    String balanceString= aContext.source("httpRequest:/param/balance", String.class).trim();
    boolean simpleAccount= !aContext.exists("httpRequest:/param/simple_account");
    BigDecimal balance= null;

    boolean valid= true;
    HDSBuilder reasonsBuilder= new HDSBuilder();
    reasonsBuilder.pushNode("div");
    reasonsBuilder.addNode("p", "Adding account failed for the following reasons: ");
    reasonsBuilder.pushNode("ul");

    if (name.equals("")) {
      valid= false;
      reasonsBuilder.addNode("li", "Name must be supplied");
    } else if (util.issueExistsRequest("cormackAccounts:db:account",
                                       new ArgByValue("name", name))) {
      valid= false;
      reasonsBuilder.addNode("li", "An account already exists with this name");
    }

    if (description.equals("")) {
      valid= false;
      reasonsBuilder.addNode("li", "Description must be supplied");
    }

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
      long accountId= util.issueNewRequest("cormackAccounts:db:account",
                                           Long.class,
                                           null,
                                           new ArgByValue("name", name),
                                           new ArgByValue("description", description),
                                           new ArgByValue("balance", balance),
                                           new ArgByValue("simpleAccount", simpleAccount));

      aContext.sink("session:/message/class", "success");
      aContext.sink("session:/message/title", "Account successfully added");
      aContext.sink("session:/message/content", "Your account '" + name + "' has been successfully added");

      aContext.sink("httpResponse:/redirect",
                    UrlUtil.resolve(aContext, "meta:cormackAccounts:web:account:view", new Arg("id", accountId + "")));
    } else {
      aContext.sink("session:/message/class", "error");
      aContext.sink("session:/message/title", "Add account failed");
      aContext.sink("session:/message/content", reasonsBuilder.getRoot());

      if (aContext.exists("httpRequest:/params")) {
        aContext.sink("session:/formData/name", "account-add");
        aContext.sink("session:/formData/params", aContext.source("httpRequest:/params"));
      }

      aContext.sink("httpResponse:/redirect", UrlUtil.resolve(aContext, "meta:cormackAccounts:web:account:add"));
    }
  }
}
