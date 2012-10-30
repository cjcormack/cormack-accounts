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

package uk.me.cormack.netkernel.accounts.admin.user.add;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernelroc.mod.layer2.AccessorUtil;
import org.netkernelroc.mod.layer2.ArgByValue;
import org.netkernelroc.mod.layer2.Layer2AccessorImpl;

public class DoAddAccessor extends Layer2AccessorImpl
{
  @Override
  public void onSource(INKFRequestContext aContext, AccessorUtil util) throws Exception {
    String email= aContext.source("httpRequest:/param/email", String.class).trim();
    String password= aContext.source("httpRequest:/param/password", String.class).trim();
    String confirm= aContext.source("httpRequest:/param/confirm", String.class).trim();

    boolean valid= true;
    String reason= "";

    if (email.equals("")) {
      valid= false;
      reason+= "<li>Email must be supplied</li>";
    } else if (util.issueExistsRequest("cormackAccounts:db:user:email",
                                       new ArgByValue("email", email))) {
      valid= false;
      reason+= "<li>An account already exists with this email address</li>";
    }

    if (password.equals("")) {
      valid= false;
      reason+= "<li>Password must be supplied</li>";
    }
    if (confirm.equals("")) {
      valid= false;
      reason+= "<li>Confirm password must be supplied</li>";
    }
    if (!password.equals(confirm)) {
      valid= false;
      reason+= "<li>Password and confirm password must match</li>";
    }

    if (valid) {
      long id= util.issueNewRequest("cormackAccounts:db:user",
                                    Long.class,
                                    null,
                                    new ArgByValue("email", email),
                                    new ArgByValue("password", password));
      aContext.sink("session:/message/title", "User Added");
      aContext.sink("session:/message/content", "<p>User has been successfully added</p>");

      aContext.sink("httpResponse:/redirect", id + "/edit");
    }

    if (!valid) {
      aContext.sink("session:/formData/name", "user-add");
      aContext.sink("session:/formData/params", aContext.source("httpRequest:/params"));
      aContext.sink("session:/message/title", "Adding User Failed");
      aContext.sink("session:/message/content", "<div><p>Failed to validate for the following reasons:</p>\n" +
                                                "<ul>" + reason + "</ul></div>");
      aContext.sink("httpResponse:/redirect", "add");
    } else {

    }
  }
}
