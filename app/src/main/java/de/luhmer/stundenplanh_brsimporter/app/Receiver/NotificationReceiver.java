package de.luhmer.stundenplanh_brsimporter.app.Receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.luhmer.stundenplanh_brsimporter.app.MainActivity;
import de.luhmer.stundenplanh_brsimporter.app.R;

public class NotificationReceiver extends BroadcastReceiver {
    public NotificationReceiver() {
    }

    public static final String TEXT_CONTENT = "TEXT_CONTENT";
    public static final String TEXT_TITLE = "TEXT_TITLE";

    @Override
    public void onReceive(Context context, Intent intent) {

        String contentText = intent.getStringExtra(TEXT_CONTENT);
        String titleText = intent.getStringExtra(TEXT_TITLE);

        BuildNotification(context, contentText, titleText);

    }

    private void BuildNotification(Context context, String content, String title) {
        // Prepare intent which is triggered if the
        // notification is selected
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Build notification
        // Actions are just fake
        Notification noti = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, noti);
    }
}
