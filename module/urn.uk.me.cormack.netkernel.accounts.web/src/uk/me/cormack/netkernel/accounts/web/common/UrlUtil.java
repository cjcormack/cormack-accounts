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

package uk.me.cormack.netkernel.accounts.web.common;

import org.netkernel.layer0.nkf.INKFRequest;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernelroc.mod.layer2.Arg;

public class UrlUtil {
  private UrlUtil() {}

  private static String dropScheme(String aIdentifier) {
    String result;
    int i= aIdentifier.indexOf(':');
    if (i >= 0) {
      result= aIdentifier.substring(i + 1);
    } else {
      result= aIdentifier;
    }
    return result;
  }

  public static String resolve(INKFRequestContext aContext, String uri, Arg...args) throws Exception {
    INKFRequest req= aContext.createRequest(uri);

    for (Arg arg : args) {
      req.addArgument(arg.getArgName(), arg.getArgValue());
    }

    return dropScheme(req.getIdentifier());
  }
}
