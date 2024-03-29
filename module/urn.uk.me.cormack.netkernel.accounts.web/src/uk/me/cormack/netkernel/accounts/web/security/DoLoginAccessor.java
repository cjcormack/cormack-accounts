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

package uk.me.cormack.netkernel.accounts.web.security;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernelroc.mod.layer2.Arg;
import org.netkernelroc.mod.layer2.ArgByValue;
import org.netkernelroc.mod.layer2.HttpLayer2AccessorImpl;
import org.netkernelroc.mod.layer2.HttpUtil;
import uk.me.cormack.netkernel.accounts.web.common.UrlUtil;

public class DoLoginAccessor extends HttpLayer2AccessorImpl {
  @Override
  public void onPost(INKFRequestContext aContext, HttpUtil util) throws Exception {
    String url;
    if (aContext.exists("session:/loginRedirect")) {
      url= aContext.source("session:/loginRedirect", String.class);
      if (url.endsWith("/user/login") ||
          url.endsWith("/user/activate") ||
          url.endsWith("/user/lostPassword") ||
          url.endsWith("/user/register")) {
        url= "/cormackAccounts/";
      }
    } else {
      url= "/cormackAccounts/";
    }
    
    if (util.issueExistsRequest("cormackAccounts:db:user:login",
                                new Arg("email", "httpRequest:/param/email"),
                                new Arg("password", "httpRequest:/param/password"))) {
      IHDSNode userId= util.issueSourceRequest("cormackAccounts:db:user:login",
                                               IHDSNode.class,
                                               new Arg("email", "httpRequest:/param/email"),
                                               new Arg("password", "httpRequest:/param/password"));
      
      long id= (Long)userId.getFirstValue("//id");

      aContext.sink("session:/currentUser", id);

      aContext.sink("session:/message/class", "success");
      aContext.sink("session:/message/title", "Login Success");
      aContext.sink("session:/message/content", "Your login was successful :-)");
      
      aContext.sink("httpResponse:/redirect", url);
      aContext.delete("session:/loginRedirect");
    } else {
      aContext.sink("session:/message/class", "error");
      aContext.sink("session:/message/title", "Login Failed");
      aContext.sink("session:/message/content", "Invalid email address and/or password.");
      aContext.sink("httpResponse:/redirect", UrlUtil.resolve(aContext, "meta:cormackAccounts:web:user:login"));
    }
  }
}
