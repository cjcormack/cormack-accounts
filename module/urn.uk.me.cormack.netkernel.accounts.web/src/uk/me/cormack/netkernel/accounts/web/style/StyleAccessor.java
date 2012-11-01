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

package uk.me.cormack.netkernel.accounts.web.style;

import net.sf.saxon.s9api.XdmNode;
import org.netkernel.layer0.nkf.INKFRequest;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.NKFException;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernel.layer0.representation.impl.HDSBuilder;
import org.netkernel.layer0.urii.SimpleIdentifierImpl;
import org.netkernel.request.IRequestScopeLevel;
import org.netkernel.request.impl.RequestScopeLevelImpl;
import org.netkernel.urii.IIdentifier;
import org.netkernel.urii.ISpace;
import org.netkernel.urii.impl.Version;
import org.netkernelroc.mod.layer2.AccessorUtil;
import org.netkernelroc.mod.layer2.Arg;
import org.netkernelroc.mod.layer2.ArgByValue;
import org.netkernelroc.mod.layer2.Layer2AccessorImpl;

public class StyleAccessor extends Layer2AccessorImpl {
  @Override
  public void onSource(INKFRequestContext aContext, AccessorUtil util) throws Exception {
    aContext.setCWU("res:/uk/me/cormack/netkernel/accounts/web/style/");

    boolean externalStyling;

    XdmNode template= aContext.source("template.xml", XdmNode.class);

    XdmNode content = aContext.source("arg:operand", XdmNode.class);

    XdmNode appliedTemplate= util.issueSourceRequest("active:xslt2",
                                                     XdmNode.class,
                                                     new Arg("operator", "style.xsl"),
                                                     new ArgByValue("operand", template),
                                                     new ArgByValue("content", content));
    XdmNode xrl= util.issueSourceRequest("active:xrl2",
                                         XdmNode.class,
                                         new ArgByValue("template", appliedTemplate));
    XdmNode tidied= util.issueSourceRequest("active:xslt2",
                                            XdmNode.class,
                                            new Arg("operator", "tidyHtml.xsl"),
                                            new ArgByValue("operand", xrl));

    HDSBuilder serializeSettings = new HDSBuilder();
    serializeSettings.pushNode("serialize");
    serializeSettings.addNode("indent", "yes");
    serializeSettings.addNode("method", "html");
    serializeSettings.addNode("mimeType", "text/html");
    serializeSettings.pushNode("doctype");
    serializeSettings.addNode("public", "-//W3C//DTD HTML 4.01 Transitional//EN");
    serializeSettings.addNode("system", "http://www.w3.org/TR/html4/loose.dtd");

    util.issueSourceRequestAsResponse("active:saxonSerialize",
                                      new ArgByValue("operand", tidied),
                                      new ArgByValue("operator", serializeSettings.getRoot()));
  }
}
