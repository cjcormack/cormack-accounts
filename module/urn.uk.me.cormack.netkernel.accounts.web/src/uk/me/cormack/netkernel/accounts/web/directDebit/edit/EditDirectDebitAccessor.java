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

package uk.me.cormack.netkernel.accounts.web.directDebit.edit;

import net.sf.saxon.s9api.XdmNode;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernelroc.mod.layer2.AccessorUtil;
import org.netkernelroc.mod.layer2.Arg;
import org.netkernelroc.mod.layer2.ArgByValue;
import org.netkernelroc.mod.layer2.Layer2AccessorImpl;

public class EditDirectDebitAccessor extends Layer2AccessorImpl {
  @Override
  public void onSource(INKFRequestContext aContext, AccessorUtil util) throws Exception {
    aContext.setCWU("res:/uk/me/cormack/netkernel/accounts/web/directDebit/edit/");

    long id= aContext.source("arg:id", Long.class);
    if (util.issueExistsRequest("cormackAccounts:db:directDebit",
                                new ArgByValue("id", id))) {
      IHDSNode directDebit= util.issueSourceRequest("cormackAccounts:db:directDebit",
                                                    IHDSNode.class,
                                                    new ArgByValue("id", id));
      IHDSNode account= util.issueSourceRequest("cormackAccounts:db:account",
                                                IHDSNode.class,
                                                new ArgByValue("id", directDebit.getFirstValue("//account_id")));

      IHDSNode params;

      if (aContext.exists("session:/formData/name") &&
          aContext.source("session:/formData/name", String.class).equals("account-directDebit-" + id + "-edit")) {
        params= aContext.source("session:/formData/params", IHDSNode.class);
        aContext.delete("session:/formData/name");
        aContext.delete("session:/formData/params");
      } else if (aContext.exists("httpRequest:/params") && aContext.source("httpRequest:/params", IHDSNode.class).getValues("//*").length > 1) {
        params= aContext.source("httpRequest:/params", IHDSNode.class);
      } else {
        params= directDebit;
      }

      XdmNode formNode= util.issueSourceRequest("active:xslt2",
                                                XdmNode.class,
                                                new Arg("operator", "editDirectDebit.xsl"),
                                                new Arg("operand", "editDirectDebit.xml"),
                                                new ArgByValue("account", account),
                                                new ArgByValue("directDebit", directDebit));

      formNode= util.issueSourceRequest("active:xrl2",
                                        XdmNode.class,
                                        new ArgByValue("template", formNode));

      formNode= util.issueSourceRequest("active:xslt2",
                                        XdmNode.class,
                                        new Arg("operator", "../../common/form-template.xsl"),
                                        new ArgByValue("operand", formNode),
                                        new ArgByValue("params", params));

      util.issueSourceRequestAsResponse("active:xrl2",
                                        new ArgByValue("template", formNode));
    } else {
      util.issueSourceRequestAsResponse("active:xrl2",
                                        new Arg("template", "directDebitNotFound.xml"));
      aContext.sink("httpResponse:/code", 404);
    }
  }
}
