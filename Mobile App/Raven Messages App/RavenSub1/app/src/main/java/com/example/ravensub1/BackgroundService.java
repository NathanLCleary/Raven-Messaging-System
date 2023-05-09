package com.example.ravensub1;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;

import java.util.Random;

public class BackgroundService extends Service {

     String CHANNEL_ID = "55";
     private int notificationID = 1;
     //private Looper serviceLooper;
     // private ServiceHandler serviceHandler;
     MQTTHandler mqttHandler = new MQTTHandler();
     Mqtt5BlockingClient client;
     private static String topic;

     public static String getTopic() {
        return topic;
     }

     // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.

            // set a callback that is called when a message is received (using the async API style)

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments");
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        //serviceLooper = thread.getLooper();
        //serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.raven_icon__2_)
                .setContentTitle("Raven process is running")
                .setContentText("For you to receive messages this notification needs to be here")
                .build();

        startForeground(1, notification);

        topic = intent.getStringExtra("topic");

        mqttHandler.setTopic(topic);
        mqttHandler.subscribe();
        client = mqttHandler.getClient();
        //Log.e("Test12", "onStartCommand: " + mqttHandler.getTopic());
        client.toAsync().publishes(ALL, publish -> {
                    // Create an Intent for the activity you want to start
                    Intent resultIntent = new Intent(this, ShowMessagesActivity.class);
                    // Create the TaskStackBuilder and add the intent, which inflates the back stack
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addNextIntentWithParentStack(resultIntent);
                    // Get the PendingIntent containing the entire back stack
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                    // create notification
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.raven_icon__2_)
                            .setContentTitle("You have received a new message!")
                            .setContentText(UTF_8.decode(publish.getPayload().get()));
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                    builder.setContentIntent(resultPendingIntent);
                    notificationManager.notify(notificationID, builder.build());

                    Intent intent1 = new Intent("android.intent.category.LAUNCHER");
                    intent1.setClassName("com.example.ravensub1", "com.example.ravensub1.ShowMessagesActivity");
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent1);

                    notificationID++;
                });
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        mqttHandler.mqttConnectionStop();
        Toast.makeText(this, "No longer Receiving messages", Toast.LENGTH_SHORT).show();
    }

}

