package com.example.roche.reactivescreen;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class WallpaperService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private boolean isRunning = true;
    private IBinder mBinder = new MyBinder();
    private Chronometer mChronometer;
    private long delay = 60000;

    public WallpaperService() {
        Log.i("HERE", "here I am!");
    }

    @Override
    public void onCreate(){
        mChronometer = new Chronometer(this);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @SuppressLint("ResourceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        final WallpaperManager wpManager = WallpaperManager.getInstance(this);

        final Timer t = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (isRunning) {
                    final int random = new Random().nextInt(101);
                    android.provider.Settings.System.putInt(getContentResolver(),
                            Settings.System.FONT_SCALE, 1);

                    int min = Calendar.getInstance().get(Calendar.MINUTE);
                    System.out.println(min);
                    try {
                        if (min % 2 == 0) {
                            wpManager.setResource(R.drawable.red);
                            showForegroundNotification("test");
//                            android.provider.Settings.System.putInt(getContentResolver(),
//                                    android.provider.Settings.System.SCREEN_BRIGHTNESS, random);
                        } else {
                            wpManager.setResource(R.drawable.green);
                            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                            r.play();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    t.cancel();
                    t.purge();
                }
            }
        };

        t.schedule(task, 0, delay);

        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    private void showForegroundNotification(String contentText) {
        Intent showTaskIntent = new Intent(getApplicationContext(), MainActivity.class);
        showTaskIntent.setAction(Intent.ACTION_MAIN);
        showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                showTaskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(contentText)
                .setContentIntent(contentIntent)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        System.out.println("Service dead af");
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        mChronometer.stop();
    }

    public class MyBinder extends Binder {
        WallpaperService getService() {
            return WallpaperService.this;
        }
    }
}
