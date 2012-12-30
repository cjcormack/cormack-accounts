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

package uk.me.cormack.netkernel.accounts.web.transaction.add;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernel.layer0.representation.impl.HDSBuilder;
import org.netkernelroc.mod.layer2.Arg;
import org.netkernelroc.mod.layer2.ArgByValue;
import org.netkernelroc.mod.layer2.HttpLayer2AccessorImpl;
import org.netkernelroc.mod.layer2.HttpUtil;
import uk.me.cormack.netkernel.accounts.web.common.UrlUtil;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DoAddTransactionAccessor extends HttpLayer2AccessorImpl {
  @Override
  public void onPost(INKFRequestContext aContext, HttpUtil util) throws Exception {
    String id= aContext.source("arg:id", String.class);
    String dateString= aContext.source("httpRequest:/param/date", String.class).trim();
    String typeString= aContext.source("httpRequest:/param/type", String.class).trim();
    String descriptionString= aContext.source("httpRequest:/param/description", String.class).trim();
    String modeString= aContext.source("httpRequest:/param/mode", String.class).trim();
    String amountString= aContext.source("httpRequest:/param/amount", String.class).trim();

    Date date= null;
    BigDecimal amount= null;

    if (util.issueExistsRequest("cormackAccounts:db:account",
                                new ArgByValue("id", id))) {

      IHDSNode accountDetails= util.issueSourceRequest("cormackAccounts:db:account",
                                                       IHDSNode.class,
                                                       new Arg("id", "arg:id"));

      boolean valid= true;
      HDSBuilder reasonsBuilder= new HDSBuilder();
      reasonsBuilder.pushNode("div");
      reasonsBuilder.addNode("p", "Adding transaction failed for the following reasons: ");
      reasonsBuilder.pushNode("ul");

      if (dateString.equals("")) {
        valid= false;
        reasonsBuilder.addNode("li", "Date must be supplied");
      } else {
        SimpleDateFormat sdf= new SimpleDateFormat("dd/MMM/yyyy");
        try {
          date= sdf.parse(dateString);
        } catch (ParseException e) {
          valid= false;
          reasonsBuilder.addNode("li", "Date must be in form dd/MMM/yyyy");
        }
      }

      if (typeString.equals("")) {
        valid= false;
        reasonsBuilder.addNode("li", "Type must be selected");
      } else if (!util.issueExistsRequest("cormackAccounts:db:transactionType",
                                          new ArgByValue("id", typeString))) {
        valid= false;
        reasonsBuilder.addNode("li", "Type no longer exists, please choose another");
      }

      if (descriptionString.equals("")) {
        valid= false;
        reasonsBuilder.addNode("li", "Description must be supplied");
      }

      if (!(modeString.equals("debit") || modeString.equals("credit"))) {
        valid= false;
        reasonsBuilder.addNode("li", "Mode of 'credit' or 'debit' must be selected");
      }

      if (amountString.equals("")) {
        valid= false;
        reasonsBuilder.addNode("li", "Amount must be supplied");
      } else {
        try {
          amount= new BigDecimal(amountString);

          if (amount.signum() <= 0) {
            reasonsBuilder.addNode("li", "Amount must be positive");
          }
        } catch (Exception e) {
          valid= false;
          reasonsBuilder.addNode("li", "Amount must be numeric");
        }
      }

      if (valid) {
        if (modeString.equals("debit")) {
          amount= amount.negate();
        }

        util.issueNewRequest("cormackAccounts:db:transaction",
                             Long.class,
                             null,
                             new ArgByValue("accountId", id),
                             new ArgByValue("date", date),
                             new ArgByValue("transactionTypeId", typeString),
                             new ArgByValue("description", descriptionString),
                             new ArgByValue("amount", amount),
                             new ArgByValue("userId", aContext.source("session:/currentUser")));
        aContext.sink("session:/message/class", "success");
        aContext.sink("session:/message/title", "Transaction successfully added");
        aContext.sink("session:/message/content", "Transaction '" + descriptionString + "' in '" + accountDetails.getFirstValue("//name") +
                                                  "' has been successfully added");

        Calendar cal= Calendar.getInstance();
        cal.setTime(date);

        aContext.sink("httpResponse:/redirect", UrlUtil.resolve(aContext,
                                                                "meta:cormackAccounts:web:account:view",
                                                                new Arg("id", id),
                                                                new Arg("month", (cal.get(Calendar.MONTH) + 1) + ""),
                                                                new Arg("year", cal.get(Calendar.YEAR) + "")));
      } else {
        aContext.sink("session:/message/class", "error");
        aContext.sink("session:/message/title", "Add transaction failed");
        aContext.sink("session:/message/content", reasonsBuilder.getRoot());

        if (aContext.exists("httpRequest:/params")) {
          aContext.sink("session:/formData/name", "account-transaction-add");
          aContext.sink("session:/formData/params", aContext.source("httpRequest:/params"));
        }

        aContext.sink("httpResponse:/redirect", UrlUtil.resolve(aContext,
                                                "meta:cormackAccounts:web:transaction:add",
                                                new Arg("id", id)));
      }
    } else {
      aContext.sink("httpResponse:/redirect", UrlUtil.resolve(aContext,
                                                              "meta:cormackAccounts:web:account:view",
                                                              new Arg("id", id)));
    }
  }
}
