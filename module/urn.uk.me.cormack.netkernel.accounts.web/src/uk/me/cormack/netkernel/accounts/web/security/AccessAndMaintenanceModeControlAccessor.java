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

import org.netkernel.layer0.nkf.INKFRequest;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFRequestReadOnly;
import org.netkernel.layer0.nkf.NKFException;
import org.netkernelroc.mod.layer2.*;
import uk.me.cormack.netkernel.accounts.web.common.UrlUtil;

public class AccessAndMaintenanceModeControlAccessor extends Layer2AccessorImpl {
  @Override
  public void onSource(INKFRequestContext aContext, AccessorUtil util) throws Exception {
    INKFRequestReadOnly request= aContext.source("arg:request", INKFRequestReadOnly.class);
    String name= request.getResolvedElementId();
    
    // check maintenance mode
    if (!name.startsWith("cormackAccounts:style") &&
        aContext.source("cormackAccounts:db:liquibase:updateAvailable", Boolean.class)) {
      maintenanceMode(aContext, util);
    } else {
      String requiredRole= util.issueSourceRequest("active:xpath2",
                                                   String.class,
                                                   new Arg("operand", "active:xrl2+template@res:/uk/me/cormack/netkernel/accounts/web/mapperConfig.xml"),
                                                   new ArgByValue("operator", "xs:string(//endpoint[id='" + name +"']/role)"));

      if (requiredRole == null) {
        allowRequest(request, aContext);
      } else if (requiredRole.equalsIgnoreCase("User") && aContext.exists("session:/currentUser")) {
        allowRequest(request, aContext);
      } else {
        denyRequest(aContext, util);
      }
    }
  }
  
  private void allowRequest(INKFRequestReadOnly request, INKFRequestContext aContext) throws NKFException {
    INKFRequest innerRequest= request.getIssuableClone();
    aContext.createResponseFrom(innerRequest);
  }
  
  private void denyRequest(INKFRequestContext aContext, AccessorUtil util) throws Exception {
    String url= aContext.source("httpRequest:/url", String.class);

    String query= aContext.source("httpRequest:/query", String.class);
    if (query != null && query.trim().length() > 0) {
      url+= "?" + query;
    }

    if (aContext.source("httpRequest:/url", String.class).endsWith("login")) {
      url= "/cormackAccounts/";
    }

    aContext.sink("session:/message/class", "info");
    aContext.sink("session:/message/title", "Login Required");
    aContext.sink("session:/message/content", "You need to login to access this page.");

    aContext.sink("session:/loginRedirect", url);

    aContext.createResponseFrom(util.createSinkRequest("httpResponse:/redirect",
                                                       new PrimaryArgByValue(UrlUtil.resolve(aContext, "meta:cormackAccounts:web:user:login"))));
  }
  
  private void maintenanceMode(INKFRequestContext aContext, AccessorUtil util) throws Exception {
    aContext.sink("session:/message/class", "error");
    aContext.sink("session:/message/title", "Maintenance Mode");
    aContext.sink("session:/message/content", "Accounts is currently in maintenance mode.");

    aContext.createResponseFrom(aContext.source("res:/uk/me/cormack/netkernel/accounts/web/exception/maintenanceMode.xml"));
  }
}
