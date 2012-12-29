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

package uk.me.cormack.netkernel.accounts.web.directDebit.list;

import net.sf.saxon.s9api.XdmNode;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernelroc.mod.layer2.AccessorUtil;
import org.netkernelroc.mod.layer2.Arg;
import org.netkernelroc.mod.layer2.ArgByValue;
import org.netkernelroc.mod.layer2.Layer2AccessorImpl;
import uk.me.cormack.netkernel.accounts.web.common.UrlUtil;

public class ListAccessor extends Layer2AccessorImpl {
  @Override
  public void onSource(INKFRequestContext aContext, AccessorUtil util) throws Exception {
    aContext.setCWU("res:/uk/me/cormack/netkernel/accounts/web/directDebit/list/");

    if (util.issueExistsRequest("cormackAccounts:db:account",
                                new ArgByValue("id", aContext.source("arg:id")))) {
      XdmNode directDebitList= util.issueSourceRequest("cormackAccounts:db:directDebit:list",
                                                       XdmNode.class,
                                                       new ArgByValue("id", aContext.source("arg:id")));

      IHDSNode accountDetails= util.issueSourceRequest("cormackAccounts:db:account",
                                                       IHDSNode.class,
                                                       new Arg("id", "arg:id"));

      XdmNode styledBalanceList= util.issueSourceRequest("active:xslt2",
                                                         XdmNode.class,
                                                         new Arg("operator", "list.xsl"),
                                                         new Arg("operand", "list.xml"),
                                                         new ArgByValue("directDebitList", directDebitList),
                                                         new ArgByValue("accountDetails", accountDetails));

      util.issueSourceRequestAsResponse("active:xrl2",
                                        new ArgByValue("template", styledBalanceList));
    } else {
      aContext.sink("httpResponse:/redirect",
                    UrlUtil.resolve(aContext,
                                    "meta:cormackAccounts:web:account:view",
                                    new Arg("id", aContext.source("arg:id", String.class))));
    }
  }
}
