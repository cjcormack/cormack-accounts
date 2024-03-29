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

package uk.me.cormack.netkernel.accounts.web.repeatingTransaction.apply;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernelroc.mod.layer2.AccessorUtil;
import org.netkernelroc.mod.layer2.ArgByValue;
import org.netkernelroc.mod.layer2.Layer2AccessorImpl;

public class ApplyAllAccessor extends Layer2AccessorImpl {
  @Override
  public void onSource(INKFRequestContext aContext, AccessorUtil util) throws Exception {
    // get list of accounts
    IHDSNode accountNodes= aContext.source("cormackAccounts:db:account:list", IHDSNode.class);

    for (IHDSNode accountNode: accountNodes.getNodes("//row")) {
      if ((Boolean)accountNode.getFirstValue("simple_account")) {
        aContext.logRaw(INKFRequestContext.LEVEL_INFO, "Ignoring account '" + accountNode.getFirstValue("name") + "' as it is a simple account");
      } else {
        long accountId= (Long)accountNode.getFirstValue("id");

        // get repeating transactions for account
        IHDSNode repeatingTransactionNodes= util.issueSourceRequest("cormackAccounts:db:repeatingTransaction:list",
                                                           IHDSNode.class,
                                                           new ArgByValue("id", accountId));

        for (IHDSNode repeatingTransactionNode: repeatingTransactionNodes.getNodes("//row")) {

          // only create new transaction for the repeating transaction if there isn't one for this month
          if (!(Boolean) repeatingTransactionNode.getFirstValue("apply_automatically")) {
            // ignore as this is not a repeating transaction to apply
          } else if (util.issueExistsRequest("cormackAccounts:db:transaction",
                                      new ArgByValue("repeatingTransactionId", repeatingTransactionNode.getFirstValue("id")))) {
            aContext.logRaw(INKFRequestContext.LEVEL_INFO, "Skipping Repeating Transaction '" + repeatingTransactionNode.getFirstValue("description") +
                            "' in account '" + accountNode.getFirstValue("name") + "' as it has already been added.");
          } else {
            aContext.logRaw(INKFRequestContext.LEVEL_INFO, "Adding transaction for Repeating Transaction '" + repeatingTransactionNode.getFirstValue("description") +
                            "' to account '" + accountNode.getFirstValue("name") + "'");
            util.issueNewRequest("cormackAccounts:db:transaction",
                                 Long.class,
                                 null,
                                 new ArgByValue("repeatingTransactionId", repeatingTransactionNode.getFirstValue("id")));
          }
        }
      }
    }
  }
}
