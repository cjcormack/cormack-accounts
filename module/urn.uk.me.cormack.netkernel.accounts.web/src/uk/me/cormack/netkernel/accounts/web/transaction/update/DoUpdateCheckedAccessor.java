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

package uk.me.cormack.netkernel.accounts.web.transaction.update;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponse;
import org.netkernelroc.mod.layer2.ArgByValue;
import org.netkernelroc.mod.layer2.HttpLayer2AccessorImpl;
import org.netkernelroc.mod.layer2.HttpUtil;

public class DoUpdateCheckedAccessor extends HttpLayer2AccessorImpl {
  @Override
  public void onPost(INKFRequestContext aContext, HttpUtil util) throws Exception {
    String id= aContext.source("arg:id", String.class);

    boolean checked= aContext.exists("httpRequest:/param/checked");
    aContext.source("httpRequest:/params");

    INKFResponse resp;
    if (util.issueExistsRequest("cormackAccounts:db:transaction",
                                new ArgByValue("id", id))) {

      util.issueSinkRequest("cormackAccounts:db:transaction",
                            null,
                            new ArgByValue("id", id),
                            new ArgByValue("checked", checked),
                            new ArgByValue("userId", aContext.source("session:/currentUser")));

      resp= aContext.createResponseFrom("success");
    } else {
      aContext.sink("httpResponse:/code", 404);
      resp= aContext.createResponseFrom("This transaction no longer exists");
    }
    resp.setMimeType("text/plain");
  }
}
