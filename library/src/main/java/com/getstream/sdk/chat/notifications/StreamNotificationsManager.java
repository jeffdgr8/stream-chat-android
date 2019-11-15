package com.getstream.sdk.chat.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.notifications.options.NotificationOptions;
import com.getstream.sdk.chat.notifications.options.StreamNotificationOptions;
import com.getstream.sdk.chat.rest.interfaces.CompletableCallback;
import com.getstream.sdk.chat.rest.response.CompletableResponse;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/*
 * Created by Anton Bevza on 2019-11-14.
 */
public class StreamNotificationsManager implements NotificationsManager {
    private static final String TAG = StreamNotificationsManager.class.getSimpleName();
    private static final String TITLE_KEY = "title";
    private static final String BODY_KEY = "body";

    private NotificationOptions notificationOptions;

    public StreamNotificationsManager(NotificationOptions notificationOptions) {
        this.notificationOptions = notificationOptions;
    }

    public StreamNotificationsManager() {
        this(new StreamNotificationOptions());
    }

    @Override
    public void setFirebaseToken(@NotNull String firebaseToken, @NotNull Context context) {
        StreamChat.getInstance(context).addDevice(firebaseToken, new CompletableCallback() {
            @Override
            public void onSuccess(CompletableResponse response) {
                // device is now registered!
            }

            @Override
            public void onError(String errMsg, int errCode) {
                // something went wrong registering this device, ouch!
            }
        });
    }

    @Override
    public void onReceiveFirebaseMessage(@NotNull RemoteMessage remoteMessage, @NotNull Context context) {
        NotificationCompat.Builder builder = notificationOptions.getNotificationBuilder(context);
        Map<String, String> payload = remoteMessage.getData();

        Log.d(TAG, "onMessageReceived: " + remoteMessage.toString() + " data: " + payload);

        if (!payload.isEmpty()) {
            builder.setContentTitle(remoteMessage.getData().get(TITLE_KEY))
                    .setContentText(remoteMessage.getData().get(BODY_KEY));
            showNotification(builder.build(), context);
        }
    }

    @Override
    public void onReceiveWebSocketEvent(@NotNull Event event, @NotNull Context context) {
        NotificationCompat.Builder builder = notificationOptions.getNotificationBuilder(context);

        if (event.getMessage() != null) {
            builder.setContentTitle(event.getCid())
                    .setContentText(event.getMessage().getText());
            //TODO add method for getting appropriate intent for launch activity on notification tap
            showNotification(builder.build(), context);
        }
    }

    @Override
    public void showNotification(@NotNull Notification notification, @NotNull Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                    notificationOptions.getNotificationChannel(context));
        }

        notificationManager.notify((int) System.currentTimeMillis(), notification);
    }
}