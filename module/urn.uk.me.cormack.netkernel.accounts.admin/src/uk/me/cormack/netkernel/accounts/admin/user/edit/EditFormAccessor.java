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

package uk.me.cormack.netkernel.accounts.admin.user.edit;

import net.sf.saxon.s9api.XdmNode;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernel.layer0.representation.impl.HDSBuilder;
import org.netkernelroc.mod.layer2.AccessorUtil;
import org.netkernelroc.mod.layer2.Arg;
import org.netkernelroc.mod.layer2.ArgByValue;
import org.netkernelroc.mod.layer2.Layer2AccessorImpl;

public class EditFormAccessor extends Layer2AccessorImpl {
  @Override
  public void onSource(INKFRequestContext aContext, AccessorUtil util) throws Exception {
    aContext.setCWU("res:/uk/me/cormack/netkernel/accounts/admin/user/edit/");

    Long id= aContext.source("arg:id", Long.class);

    IHDSNode params;

    if (aContext.exists("session:/formData/name") &&
        aContext.source("session:/formData/name", String.class).equals("user-edit")) {
      params= aContext.source("session:/formData/params", IHDSNode.class);
      aContext.delete("session:/formData/name");
      aContext.delete("session:/formData/params");
    } else {
      params= util.issueSourceRequest("cormackAccounts:db:user",
                                      IHDSNode.class,
                                      new ArgByValue("id", id));
    }
    
    XdmNode formNode= util.issueSourceRequest("active:xslt2",
                                              XdmNode.class,
                                              new Arg("operator", "edit.xsl"),
                                              new Arg("operand", "edit.xml"),
                                              new ArgByValue("userId", id + ""));
    formNode= util.issueSourceRequest("active:xslt2",
                                      XdmNode.class,
                                      new Arg("operator", "../../common/form-template.xsl"),
                                      new ArgByValue("operand", formNode),
                                      new ArgByValue("params", params));
    
    util.issueSourceRequestAsResponse("active:xrl2",
                                      new ArgByValue("template", formNode),
                                      new ArgByValue("id", id));
  }
}
