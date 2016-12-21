package com.example.xu.rewardtask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Administrator on 2016/12/13.
 */
public class BootReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.BOOT_COMPLETED";

    private String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
            context.startService(new Intent(context, NoticeService.class));
            Log.i(TAG, "Service started");
        }
    }
}
