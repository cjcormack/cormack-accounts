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

package uk.me.cormack.netkernel.accounts.db;

import org.netkernel.layer0.nkf.NKFException;
import org.netkernelroc.mod.layer2.ArgByValue;
import org.netkernelroc.mod.layer2.DatabaseUtil;

public class AuditUtil {
  private AuditUtil() {}

  public static void logAccountAudit(DatabaseUtil util, long id, Long userId, AuditOperation operation, String description) throws NKFException {
    logGenericAudit(util, "account", id, userId, operation, description);
  }

  public static void logDirectDebitAudit(DatabaseUtil util, long id, Long userId, AuditOperation operation, String description) throws NKFException {
    logGenericAudit(util, "direct_debit", id, userId, operation, description);
  }

  public static void logTransactionAudit(DatabaseUtil util, long id, Long userId, AuditOperation operation, String description) throws NKFException {
    logGenericAudit(util, "transaction", id, userId, operation, description);
  }

  private static void logGenericAudit(DatabaseUtil util, String type, long id, Long userId, AuditOperation operation, String description) throws NKFException {
    String recordSql= "INSERT INTO public.accounts_" + type + "_audit (\n" +
                      "    operation_time," +
                      "    " + type + "_id,\n" +
                      "    user_id,\n" +
                      "    operation_id,\n" +
                      "    description\n" +
                      ") VALUES (\n" +
                      "    NOW(),\n" +
                      "    ?,\n" +
                      "    ?,\n" +
                      "    ( SELECT id\n" +
                      "      FROM   public.accounts_audit_operation\n" +
                      "      WHERE operation=?),\n" +
                      "    ?\n" +
                      ");";
    util.issueSourceRequest("active:sqlPSUpdate",
                            null,
                            new ArgByValue("operand", recordSql),
                            new ArgByValue("param", id),
                            new ArgByValue("param", userId),
                            new ArgByValue("param", operation.name()),
                            new ArgByValue("param", description));
  }

  public static enum AuditOperation {
    ADD,
    DELETE,
    MODIFY
  }
}
