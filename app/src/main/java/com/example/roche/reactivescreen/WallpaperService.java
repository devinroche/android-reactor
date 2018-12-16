package com.example.roche.reactivescreen;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import static android.os.Build.*;

@RequiresApi(api = VERSION_CODES.O)
public class WallpaperService extends Service implements SensorEventListener {
    private boolean isRunning = true;
    private final IBinder mBinder = new MyBinder();
    private Chronometer mChronometer;

    private int currentStepsDetected;
    private int tmpStepCounter = 1;
    private int stepCounter;

    private DBHelper dbhelper;
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private Sensor stepDetectorSensor;

    private final int ncode = 101;
    private final String ChannelID = "my_channel_01";
    private NotificationManager mNotific;
    private final CharSequence name = "Ragav";
    private final String desc = "this is notific";
    private final int imp = NotificationManager.IMPORTANCE_HIGH;
    private int goal;

    private static final String TAG = "BroadcastService";
    public static final String BROADCAST_ACTION = "com.example.roche.reactivescreen";
    private final Handler handler = new Handler();
    private Intent intent;

    public WallpaperService() {
        Log.i("HERE", "here I am!");
    }

    @Override
    public void onCreate(){
        mChronometer = new Chronometer(this);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
        currentStepsDetected = 1;
        dbhelper = new DBHelper(this);
        intent = new Intent(BROADCAST_ACTION);

        SharedPreferences settings = getSharedPreferences("settings", 0);
        goal = settings.getInt("steps", 2000);

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

    private final Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            handler.postDelayed(this, 1500);
            intent.putExtra("steps", String.valueOf(currentStepsDetected));
            sendBroadcast(intent);
        }
    };
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second

        final Timer t = new Timer();
        final Timer t2 = new Timer();
        final Timer t3 = new Timer();

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

        TimerTask task2 = new TimerTask() {
            @Override
            public void run() {
                if (isRunning) {
                    notificationManager();
                } else {
                    t2.cancel();
                    t2.purge();
                }
            }
        };

        TimerTask task3 = new TimerTask() {
            @Override
            public void run() {
                if (isRunning) {
                    saveToDB();
                } else {
                    t3.cancel();
                    t3.purge();
                }
            }
        };

        t.schedule(task, 0, 1000 * 60); // 10 seconds
        t2.schedule(task2, 0, 3600 * 1000); // run every hour 3600 * 1000
        t3.schedule(task3, 0, 1000*60*60*24); // once daily 1000*60*60*24

        Toast.makeText(this, "Step Counter Started!", Toast.LENGTH_LONG).show();
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
        currentStepsDetected = 1;
        tmpStepCounter = 1;
        mChronometer.stop();
        Toast.makeText(this, "Step Counter Ended", Toast.LENGTH_LONG).show();
    }

    public class MyBinder extends Binder {
        WallpaperService getService() {
            return WallpaperService.this;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int countSteps = (int) event.values[0];

            if (stepCounter == 0) {
                stepCounter = (int) event.values[0];
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            int detectSteps = (int) event.values[0];
            currentStepsDetected += detectSteps;
            tmpStepCounter += detectSteps;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void showNotification(String title, String content) {
        Notification n = new Notification.Builder(this, ChannelID)
                .setContentTitle(title)
                .setContentText(content)
                .setBadgeIconType(R.mipmap.ic_launcher)
                .setNumber(5)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setAutoCancel(true)
                .build();

        mNotific.notify(ncode, n);
    }

    private boolean muteAll() {
        Calendar calendar = Calendar.getInstance();
        return !(calendar.get(Calendar.HOUR_OF_DAY) < 6 || calendar.get(Calendar.HOUR_OF_DAY) > 22);
    }

    @SuppressLint("ResourceType")
    private void activityManager() {
        final WallpaperManager wpManager = WallpaperManager.getInstance(this);
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.HOUR_OF_DAY) == 23 && calendar.get(Calendar.MINUTE) == 0) {
            currentStepsDetected = 0;
        }
        try {
            if (muteAll()) { // dont be annoying between 11:00pm & 7:00am
                if(currentStepsDetected >= getGoal()){
                    wpManager.setResource(R.drawable.trophy);
                }
                else if (tmpStepCounter >= ((getGoal() / 24) / 6)) {
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

    private void notificationManager(){
        if (muteAll()) { // dont be annoying between 11:00pm & 7:00am
            if(currentStepsDetected >= getGoal()){
                showNotification(
                        getResources().getString(R.string.done_goal_head),
                        getResources().getString(R.string.done_goal_sub)
                );
            }
            else if (tmpStepCounter >= ((getGoal() / 24) / 6)) {
                showNotification(
                        getResources().getString(R.string.good_goal_head),
                        getResources().getString(R.string.good_goal_sub)
                );
            } else {
                showNotification(
                        getResources().getString(R.string.bad_goal_head),
                        getResources().getString(R.string.bad_goal_sub)
                );
            }
        }
    }

    private void saveToDB(){
        Exercise tmp = new Exercise();
        tmp.setDate(Exercise.dateToStr());
        tmp.setSteps(tmpStepCounter);
        dbhelper.insertExercise(tmp);
    }

    private int getGoal(){
        SharedPreferences settings = getSharedPreferences("settings", 0);
        return settings.getInt("steps", 2000);
    }
}
