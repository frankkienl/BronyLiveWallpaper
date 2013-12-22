/*
 * Copyright (C) 2013 FrankkieNL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.frankkie.bronylivewallpaper;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import org.apache.http.NameValuePair;

/**
 * CUSTOM LOGGER
 *
 * Deze class zo gemaakt, 
 * dat hij zoveel mogelijk lijkt op het bestaande Log-systeem,
 * en dat hij op dezelfde manier kan worden aangeroepen.
 * Als je alle logs via deze class laat lopen,
 * kan je makkelijk logging in en uit schakelen.
 * Ook exceptions kan je hiermee printen mbv de writer.
 * Voor later is het ook handig, (aanpasbaarheid)
 * als voortaan de logs naar een file moeten of zo,
 * hoef je niet alle logs in de app aan te passen,
 * maar alleen deze class.
 *
 * Voorbeeld:
 * normaal:
 * Log.v("tag", "bericht");
 * voortaan:
 * CLog.v("bericht");
 * @author Frankkie
 */
public class CLog extends OutputStream {

    private ByteArrayOutputStream bos = new ByteArrayOutputStream();
    public static String TAG = "CLog";
    /**
     * Minimum errorlevel om te worden gelogd.
     * Waardes komen van android.util.Log.*;
     */
    public static int errorLevel = Log.VERBOSE;
    public static boolean shouldLog = true;
    /**
     * door deze printwriter kan je meteen zo doen:
     * exception.printStackTrace(CLog.writer);
     */
    public static PrintWriter writer = new PrintWriter(new CLog());

    public CLog(Context c) {
        setShouldLogByDebuggable(c);
    }

    public CLog(Context c, String tag) {
        setShouldLogByDebuggable(c);
        setTag(tag);
    }

    public static boolean checkDebuggable(Context c) {
        return (0 != (c.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
    }

    public static void setShouldLog(boolean bool){
        shouldLog = bool;
    }

    public static void setShouldLogByDebuggable(Context c) {
        shouldLog = checkDebuggable(c);
    }

    public CLog() {
        // le nothin'
    }

    public CLog(String tag) {
        setTag(tag);
    }

    @Override
    public void write(int b) throws IOException {
        if (b == (int) '\n') {
            String s = new String(this.bos.toByteArray());
            CLog.v(TAG, s);
            this.bos = new ByteArrayOutputStream();
        } else {
            this.bos.write(b);
        }
    }

    public static void setTag(String s) {
        TAG = s;
    }

    public static String getTag() {
        return TAG;
    }

    /**
     * print exceptions
     * @param e
     */
    public static void ex(Exception e) {
        if (shouldLog) {
            e.printStackTrace();
        }
    }

    /**
     * print exceptions
     * @param e
     */
    public static void exOLD(Exception e) {
        if (shouldLog) {
            e.printStackTrace(writer);
        }
    }

    public static void e(String tag, String msg) {
        if (shouldLog && errorLevel <= Log.ERROR) {
            Log.e(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (shouldLog && errorLevel <= Log.DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (shouldLog && errorLevel <= Log.INFO) {
            Log.i(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (shouldLog && errorLevel <= Log.VERBOSE) {
            Log.v(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (shouldLog && errorLevel <= Log.WARN) {
            Log.w(tag, msg);
        }
    }

    public static void e(Object msg) {
        e(TAG, msg.toString());
    }

    public static void d(Object msg) {
        d(TAG, msg.toString());
    }

    public static void i(Object msg) {
        i(TAG, msg.toString());
    }

    public static void v(Object msg) {
        v(TAG, msg.toString());
    }

    public static void w(Object msg) {
        w(TAG, msg.toString());
    }

    /**
     * Print all parameter of List<NameValuePair>
     * @param pairs the pairs to be printed
     */
    public static void printNameValuePairs(List<NameValuePair> pairs) {
        if (!shouldLog) {
            return;
        }
        Iterator i = pairs.iterator();
        while (i.hasNext()) {
            NameValuePair p = (NameValuePair) i.next();
            v(p.getName() + ":" + p.getValue());
        }
    }

    /**
     * Het sluiten van een dialog geeft veel te
     * vaak een exception, waar we toch niets mee doen.
     * @param d
     */
    public static void dismissDialog(Dialog d) {
        try {
            d.dismiss();
        } catch (Exception e) {
            //./ignore
        }
    }
}
