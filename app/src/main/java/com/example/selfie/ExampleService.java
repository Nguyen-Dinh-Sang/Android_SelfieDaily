package com.example.selfie;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.io.IOException;

import static com.example.selfie.App.CHANNEL_ID;

public class ExampleService extends Service {
    CountDownTimer Timer;
    Integer NOTIF_ID = 2;
    MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaPlayer = MediaPlayer.create(this, R.raw.notification);

        startForeground(NOTIF_ID, getNotification("Bắt đầu đếm ngược"));

        Timer = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                Log.d("AAAAA", "onTick: " + millisUntilFinished);
                updateNotification("Lần chụp hình tiếp theo: " + millisUntilFinished/1000 + " s");
            }

            public void onFinish() {
                mediaPlayer.start();
                try {
                    updateNotification("Bạn cần chụp hình");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                start();
            }
        }.start();

        return START_NOT_STICKY;
    }

    private void updateNotification(String title) {
        Notification notification = getNotification(title);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIF_ID, notification);
    }

    private Notification getNotification(String title) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.a)
                .setContentIntent(pendingIntent).build();

        return notification;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timer.cancel();
    }
}
