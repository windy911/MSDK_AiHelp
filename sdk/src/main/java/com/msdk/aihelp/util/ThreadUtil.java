package com.msdk.aihelp.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtil {

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor();

    public static void runOnMain(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            MAIN_HANDLER.post(runnable);
        }
    }

    public static void runOnDb(Runnable runnable) {
        DB_EXECUTOR.execute(runnable);
    }

    public static Handler getMainHandler() {
        return MAIN_HANDLER;
    }
}
