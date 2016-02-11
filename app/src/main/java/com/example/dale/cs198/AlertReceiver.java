package com.example.dale.cs198;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by DALE on 1/31/2016.
 */
public class AlertReceiver extends BroadcastReceiver {
    private static final String TAG = "testMessage";

    @Override
    public void onReceive(Context context, Intent intent) {
//        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
//            createNotification(context,"Present!","Class is about to start!","Get Ready");
//        }
        createNotification(context, "Present!", "Class is about to start!", "Get Ready");
    }

    public void createNotification(Context context,String message,String messageText,String messageAlert){

        PendingIntent notifIntent = PendingIntent.getActivity(context,0,new Intent(context, CardHome.class),0);

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(message)
                .setTicker(messageAlert)
                .setContentText(messageText);

        notifBuilder.setContentIntent(notifIntent);
        notifBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        notifBuilder.setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1,notifBuilder.build());

        Log.i(TAG, "NOTIFICATION BUILT");



    }


}
