package com.example.xu.rewardtask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

public class NoticeReceiver extends BroadcastReceiver {
    private String TAG = "NoticeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Receive");

        Bundle bundle = intent.getExtras();
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), bundle.getInt("Icon"));

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context);

        builder.setContentTitle(bundle.getString("Missionname"))
                .setContentText(bundle.getString("Comment"))
                .setTicker("AskMe")
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setLargeIcon(bm)
                .setSmallIcon(R.mipmap.app_icon)
                .setAutoCancel(true);

        Intent mIntent = new Intent(context, MissionDetailActivity.class);
        mIntent.putExtra("UserName", bundle.getString("Username"));
        mIntent.putExtra("MissionName", bundle.getString("Missionname"));

        PendingIntent mPendingIntent = PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(mPendingIntent);

        Notification notify = builder.build();

        manager.notify(0, notify);
    }
}
