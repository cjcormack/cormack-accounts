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

package uk.me.cormack.netkernel.accounts.db.account;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponse;
import org.netkernel.layer0.nkf.NKFException;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernel.layer0.representation.impl.HDSBuilder;
import org.netkernelroc.mod.layer2.ArgByValue;
import org.netkernelroc.mod.layer2.DatabaseAccessorImpl;
import org.netkernelroc.mod.layer2.DatabaseUtil;

import java.util.Calendar;

public class MonthDetailsAccessor extends DatabaseAccessorImpl {
  @Override
  public void onSource(INKFRequestContext aContext, DatabaseUtil util) throws Exception {
    Long id= aContext.source("arg:id", Long.class);

    Integer month= aContext.source("arg:month", Integer.class);
    Integer year= aContext.source("arg:year", Integer.class);

    HDSBuilder resultBuilder= new HDSBuilder();
    resultBuilder.pushNode("root");

    // does last month have a carried forward?
    Calendar lastMonth= Calendar.getInstance();
    lastMonth.set(Calendar.DAY_OF_MONTH, 1);
    lastMonth.set(Calendar.YEAR, year);
    lastMonth.set(Calendar.MONTH, month - 1);
    lastMonth.add(Calendar.MONTH, -1);

    if (hasCarriedForward(util, id, lastMonth.get(Calendar.MONTH) + 1, lastMonth.get(Calendar.YEAR))) {
      IHDSNode broughtForward = fetchCarriedForward(util, id, lastMonth.get(Calendar.MONTH) + 1, lastMonth.get(Calendar.YEAR));
      resultBuilder.addNode("broughtForward", broughtForward.getFirstValue("//balance"));
    } else {
      resultBuilder.addNode("broughtForward", null);
    }

    if (hasCarriedForward(util, id, month, year)) {
      IHDSNode carriedForward = fetchCarriedForward(util, id, month, year);
      resultBuilder.addNode("carriedForward", carriedForward.getFirstValue("//balance"));
    } else {
      resultBuilder.addNode("carriedForward", null);
    }

    final String totalSql= "SELECT coalesce((SELECT   sum(amount)\n" +
                           "                 FROM     public.accounts_transaction\n" +
                           "                 WHERE    account_id=?\n" +
                           "                 AND      date_part('month', transaction_date)=?\n" +
                           "                 AND      date_part('year', transaction_date)=?),\n" +
                           "                0) AS total;";
    IHDSNode totalNode= util.issueSourceRequest("active:sqlPSQuery",
                                                IHDSNode.class,
                                                new ArgByValue("operand", totalSql),
                                                new ArgByValue("param", id),
                                                new ArgByValue("param", aContext.source("arg:month", Integer.class).doubleValue()),
                                                new ArgByValue("param", aContext.source("arg:year", Integer.class).doubleValue()));
    resultBuilder.addNode("total", totalNode.getFirstValue("//total"));

    INKFResponse resp= aContext.createResponseFrom(resultBuilder.getRoot());
    
    resp.setHeader("no-cache", null);
    util.attachGoldenThread("cormackAccounts:all", "cormackAccounts:accounts", "cormackAccounts:transactions");
  }

  private boolean hasCarriedForward(DatabaseUtil util, Long id, int month, int year) throws NKFException {
    final String hasCarriedForwardSql= "SELECT  id\n" +
                                       "FROM    public.accounts_transaction\n" +
                                       "WHERE   account_id=?\n" +
                                       "AND     transaction_date > date (? || '-' || ? || '-' || '01') + interval '1 month' - interval '1 day'\n" +
                                       "LIMIT   1;";

    Calendar currentDate= Calendar.getInstance();
    int currentMonth= currentDate.get(Calendar.MONTH) + 1;
    int currentYear= currentDate.get(Calendar.YEAR);

    boolean isPastMonth= year < currentYear || (year == currentYear && month < currentMonth);

    return isPastMonth ||
           util.issueSourceRequest("active:sqlPSBooleanQuery",
                                   Boolean.class,
                                   new ArgByValue("operand", hasCarriedForwardSql),
                                   new ArgByValue("param", id),
                                   new ArgByValue("param", year + ""),
                                   new ArgByValue("param", month + ""));
  }

  private IHDSNode fetchCarriedForward(DatabaseUtil util, Long id, Integer month, Integer year) throws NKFException {
    String carriedForwardSql= "SELECT   balance\n" +
                              "FROM     public.accounts_transaction_with_balance\n" +
                              "WHERE    account_id=?\n" +
                              "AND      transaction_date < date (? || '-' || ? || '-' || '01') + interval '1 month'\n" +
                              "ORDER BY transaction_date DESC,\n" +
                              "         id DESC\n" +
                              "LIMIT    1;";

    return util.issueSourceRequest("active:sqlPSQuery",
                                   IHDSNode.class,
                                   new ArgByValue("operand", carriedForwardSql),
                                   new ArgByValue("param", id),
                                   new ArgByValue("param", year + ""),
                                   new ArgByValue("param", month + ""));
  }
}
