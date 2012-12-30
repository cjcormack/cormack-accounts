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

package uk.me.cormack.netkernel.accounts.web.account.view;

import net.sf.saxon.s9api.XdmNode;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernel.layer0.representation.impl.HDSBuilder;
import org.netkernelroc.mod.layer2.AccessorUtil;
import org.netkernelroc.mod.layer2.Arg;
import org.netkernelroc.mod.layer2.ArgByValue;
import org.netkernelroc.mod.layer2.Layer2AccessorImpl;
import uk.me.cormack.netkernel.accounts.web.common.UrlUtil;

import java.util.Calendar;
import java.util.Date;

public class ViewAccessor extends Layer2AccessorImpl {
  @Override
  public void onSource(INKFRequestContext aContext, AccessorUtil util) throws Exception {
    if (util.issueExistsRequest("cormackAccounts:db:account",
                                new ArgByValue("id", aContext.source("arg:id")))) {
      IHDSNode account= util.issueSourceRequest("cormackAccounts:db:account",
                                                IHDSNode.class,
                                                new ArgByValue("id", aContext.source("arg:id")));

      IHDSNode params;

      if (aContext.exists("session:/formData/name") &&
          aContext.source("session:/formData/name", String.class).equals("account-update")) {
        params= aContext.source("session:/formData/params", IHDSNode.class);
        aContext.delete("session:/formData/name");
        aContext.delete("session:/formData/params");
      } else {
        params= account;
      }

      Calendar currentCal= Calendar.getInstance();

      if ((Boolean)account.getFirstValue("//simple_account") &&
            (aContext.exists("arg:month") ||
             aContext.exists("arg:year"))) {
        // Month or year parameters are not appropriate for simple accounts
        aContext.sink("httpResponse:/redirect",
                      UrlUtil.resolve(aContext,
                                      "meta:cormackAccounts:web:account:view",
                                      new Arg("id", aContext.source("arg:id", String.class))));
        return;
      }

      int month;
      int year;

      if (aContext.exists("arg:month")) {
        month= aContext.source("arg:month", Integer.class);
      } else {
        month= currentCal.get(Calendar.MONTH) + 1;
      }

      if (aContext.exists("arg:year")) {
        year= aContext.source("arg:year", Integer.class);
      } else {
        year= currentCal.get(Calendar.YEAR);
      }

      aContext.setCWU("res:/uk/me/cormack/netkernel/accounts/web/account/view/");
      XdmNode view= util.issueSourceRequest("active:xslt2",
                                            XdmNode.class,
                                            new Arg("operator", "view.xsl"),
                                            new Arg("operand", "view.xml"),
                                            new ArgByValue("account", account),
                                            new ArgByValue("month", month + ""),
                                            new ArgByValue("year", year + ""),
                                            new ArgByValue("isCurrent", !(aContext.exists("arg:month") ||
                                                                          aContext.exists("arg:year"))));

      XdmNode formNode= util.issueSourceRequest("active:xslt2",
                                                XdmNode.class,
                                                new Arg("operator", "../../common/form-template.xsl"),
                                                new ArgByValue("operand", view),
                                                new ArgByValue("params", params));

      util.issueSourceRequestAsResponse("active:xrl2",
                                        new ArgByValue("template", formNode),
                                        new ArgByValue("month", month),
                                        new ArgByValue("year", year));
    } else {
      if (aContext.exists("arg:month") ||
          aContext.exists("arg:year")) {
        aContext.sink("httpResponse:/redirect",
                      UrlUtil.resolve(aContext,
                                      "meta:cormackAccounts:web:account:view",
                                      new Arg("id", aContext.source("arg:id", String.class))));
      } else {
        aContext.setCWU("res:/uk/me/cormack/netkernel/accounts/web/account/view/");
        util.issueSourceRequestAsResponse("active:xrl2",
                                          new Arg("template", "accountNotFound.xml"));
        aContext.sink("httpResponse:/code", 404);
      }
    }
  }
}
