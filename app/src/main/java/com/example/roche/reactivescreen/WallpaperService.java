package com.example.roche.reactivescreen;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.provider.Settings;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static android.os.Build.*;

@RequiresApi(api = VERSION_CODES.O)
public class WallpaperService extends Service implements SensorEventListener {
    private boolean isRunning = true;
    private IBinder mBinder = new MyBinder();
    private Chronometer mChronometer;
    final int ncode = 101;
    final String ChannelID = "my_channel_01";
    int currentStepsDetected = 1;
    int tmpStepCounter = 1;
    int stepCounter;
    int newStepCounter;
    DBHelper dbhelper;
    SensorManager sensorManager;
    Sensor stepCounterSensor;
    Sensor stepDetectorSensor;
    NotificationManager mNotific;
    CharSequence name = "Ragav";
    String desc = "this is notific";
    int imp = NotificationManager.IMPORTANCE_HIGH;
    private long delay = 100000; // 13.89 steps/10 mins
    private int goal = 2000;

    public WallpaperService() {
        Log.i("HERE", "here I am!");
    }

    @Override
    public void onCreate(){
        mChronometer = new Chronometer(this);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
        dbhelper = new DBHelper(this);


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        sensorManager.registerListener(this, stepCounterSensor, 0);
        sensorManager.registerListener(this, stepDetectorSensor, 0);
        mNotific = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (VERSION.SDK_INT >= 26) {
            NotificationChannel mChannel = new NotificationChannel(ChannelID, name,
                    imp);
            mChannel.setDescription(desc);
            mChannel.canShowBadge();
            mChannel.setShowBadge(true);
            mNotific.createNotificationChannel(mChannel);
        }
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

        final Timer t = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (isRunning) {
                    activityManager();
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
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        mChronometer.stop();
    }

    public class MyBinder extends Binder {
        WallpaperService getService() {
            return WallpaperService.this;
        }
    }

//    @Override
//    public void onLowMemory() {
//        super.onLowMemory();
//    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int countSteps = (int) event.values[0];

            if (stepCounter == 0) {
                stepCounter = (int) event.values[0];
            }
            newStepCounter = countSteps - stepCounter;
        }

        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            int detectSteps = (int) event.values[0];
            currentStepsDetected += detectSteps;
            tmpStepCounter += detectSteps;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    void showNotification(String title, String content) {
        Notification n = new Notification.Builder(this, ChannelID)
                .setContentTitle(getPackageName())
                .setContentText(Body)
                .setBadgeIconType(R.mipmap.ic_launcher)
                .setNumber(5)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setAutoCancel(true)
                .build();

        mNotific.notify(ncode, n);
    }

    boolean muteAll() {
        Calendar calendar = Calendar.getInstance();
        return !(calendar.get(Calendar.HOUR_OF_DAY) < 6 || calendar.get(Calendar.HOUR_OF_DAY) > 22);
    }

    public void activityManager() {
        final WallpaperManager wpManager = WallpaperManager.getInstance(this);
        showNotification();
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.HOUR_OF_DAY) == 23 && calendar.get(Calendar.MINUTE) == 0) {
            currentStepsDetected = 0;
        }
        try {
            Exercise tmp = new Exercise();
            tmp.setDate(Exercise.dateToStr());
            tmp.setSteps(tmpStepCounter);
            dbhelper.insertExercise(tmp);
            if (!muteAll()) { // dont be annoying between 11:00pm & 7:00am
                if (tmpStepCounter >= ((goal / 24) / 6)) {
                    wpManager.setResource(R.drawable.green);
                } else {
                    wpManager.setResource(R.drawable.red);
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                }
            }
            tmpStepCounter = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
