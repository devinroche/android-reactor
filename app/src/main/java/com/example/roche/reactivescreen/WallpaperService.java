package com.example.roche.reactivescreen;

import android.annotation.SuppressLint;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.provider.Settings;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
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
                    int set_brightness;
                    int min = Calendar.getInstance().get(Calendar.MINUTE);
                    System.out.println(min);
                    try {
                        if (min % 2 == 0) {
                            wpManager.setResource(R.drawable.red);
                            android.provider.Settings.System.putInt(getContentResolver(),
                                    android.provider.Settings.System.SCREEN_BRIGHTNESS, random);
                        } else {
                            wpManager.setResource(R.drawable.green);
                            android.provider.Settings.System.putInt(getContentResolver(),
                                    Settings.System.FONT_SCALE, 1);
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
