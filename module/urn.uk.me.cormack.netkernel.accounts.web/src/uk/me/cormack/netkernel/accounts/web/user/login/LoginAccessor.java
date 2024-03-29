/*
 * Copyright (C) 2010-2011 by Chris Cormack
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

package uk.me.cormack.netkernel.accounts.web.user.login;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernelroc.mod.layer2.AccessorUtil;
import org.netkernelroc.mod.layer2.Layer2AccessorImpl;

public class LoginAccessor extends Layer2AccessorImpl {
  @Override
  public void onSource(INKFRequestContext aContext, AccessorUtil util) throws Exception {
    aContext.setCWU("res:/uk/me/cormack/netkernel/accounts/web/user/login/");
    
    String url;
    if (aContext.exists("session:/loginRedirect")) {
      url= aContext.source("session:/loginRedirect", String.class);
    } else if (!aContext.source("httpRequest:/url", String.class).endsWith("login")) {
      url= aContext.source("httpRequest:/header/url", String.class);
    } else if (aContext.exists("httpRequest:/header/Referer") &&
        aContext.source("httpRequest:/header/Referer", String.class).contains("/cormackAccounts/") &&
        !aContext.source("httpRequest:/header/Referer", String.class).contains("login")) {
      url= aContext.source("httpRequest:/header/Referer", String.class);
    } else {
      url= "/cormackAccounts/";
    }
    
    aContext.sink("session:/loginRedirect", url);
    
    aContext.createResponseFrom(aContext.source("login.xml"));
  }
}
