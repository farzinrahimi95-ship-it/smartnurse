package com.smartnurse.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "alarm_channel";
    public static final String EXTRA_MED_ID = "med_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        long medId = intent.getLongExtra(EXTRA_MED_ID, -1);
        if (medId == -1) return;

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        Medication med = dbHelper.getMedication(medId);
        if (med == null) return;

        // ارسال SMS
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(
                    med.getPhone(),
                    null,
                    "زمان مصرف دارو فرا رسید:\nنام دارو: " + med.getName() + "\nدستور مصرف: " + med.getInstructions(),
                    null,
                    null
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        // نمایش نوتیفیکیشن تمام‌صفحه
        createNotificationChannel(context);

        Intent fullScreenIntent = new Intent(context, AlarmActivity.class);
        fullScreenIntent.putExtra(EXTRA_MED_ID, medId);
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                (int) medId,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("زمان مصرف دارو")
                .setContentText(med.getName() + " - " + med.getInstructions())
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat.from(context).notify((int) medId, notification);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alarm",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for medication alarms");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
