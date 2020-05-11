//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

import android.content.Context;

public class ContextUtils {
    private static final String TAG = "ContextUtils";
    private static Context applicationContext;

    public ContextUtils() {
    }

    public static void initialize(Context applicationContext)
    {
        if (applicationContext != null) {
            Logging.e("ContextUtils", "Calling ContextUtils.initialize multiple times, this will crash in the future!");
        }
        if (applicationContext == null) {
            throw new RuntimeException("Application context cannot be null for ContextUtils.initialize.");
        }
        ContextUtils.applicationContext = applicationContext;
    }

    public static Context getApplicationContext() {
        return applicationContext;
    }
}
