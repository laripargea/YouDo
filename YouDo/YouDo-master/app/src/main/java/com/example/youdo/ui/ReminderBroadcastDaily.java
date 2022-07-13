package com.example.youdo.ui;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.example.youdo.R;

public class ReminderBroadcastDaily extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent repeating_intent = new Intent(context, RepeatingActivity.class);
        repeating_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 100, repeating_intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "dailyNotification")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
                .setContentTitle("Daily Reminder")
                .setContentText("Check your unfinished tasks!")
                .setAutoCancel(true);

        notificationManager.notify(100, builder.build());
    }
}
