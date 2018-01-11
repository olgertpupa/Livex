/*
 * @(#)IPrinter.java	2018-01-10
 *
 * Copyright 2010 Fiberhome. All rights reserved.
 */
package com.jlwan.livex;

/**
 *
 * @author jlwan
 * @version 1.0, 2018-01-10
 * @since 1.0
 */
public interface IMsgHandler {

    public void print(String msg);

    public void onProgress(int done, int all);

    public void onSuccess(String url);

    public void onFailed();
}
