package com.tuisongbao.android.engine.util;

import java.util.Iterator;
import java.util.TreeMap;

import android.annotation.SuppressLint;

@SuppressLint("DefaultLocale")
public class SignUtils
{
    static {
        System.loadLibrary("push-ndk");
    }

    /* A native method that is implemented by the
     * 'push-ndk' native library, which is packaged
     * with this application.
     */
    private static native String signRequest(String str);

    /**
     *
     * @param secret Assigned APP_SECRET
     */
    public static String generateSignature(TreeMap<String, String> params)
    {
        String result = null;
        StringBuffer orgin = getBeforeSign(params, new StringBuffer());
        if (orgin == null) return result;

        try
        {
            result = signRequest(orgin.toString());
        }
        catch (Exception e)
        {
            throw new java.lang.RuntimeException("sign error !");
        }
        return result;
    }

    /**
     * Encapsulate params.
     */
    private static StringBuffer getBeforeSign(TreeMap<String, String> params, StringBuffer orgin)
    {
        if (params == null) return null;
        Iterator<String> it = params.keySet().iterator();
        while (it.hasNext())
        {
            String key = it.next();
            orgin.append(key).append(params.get(key));
        }
        return orgin;
    }
}