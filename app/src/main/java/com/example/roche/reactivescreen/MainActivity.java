package com.example.roche.reactivescreen;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private boolean mServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, WallpaperService.class);
    }

    OnMenuItemClickListener setWallpaperClickListener = new OnMenuItemClickListener() {
        @SuppressLint("ResourceType")
        public boolean onMenuItemClick(MenuItem i) {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            // Starts Note Activity
            startActivityForResult(intent, 1);
            return true;
        }
    };

    public void startService(View view) {
        Intent intent = new Intent(this, WallpaperService.class);
        startService(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(broadcastReceiver, new IntentFilter(WallpaperService.BROADCAST_ACTION));
        }
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    // Method to stop the service
    public void stopService(View view) {
        if (mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
        Intent intent = new Intent(MainActivity.this,
                WallpaperService.class);
        stopService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        Toast.makeText(MainActivity.this, "Service Closed", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_bar, m);
        return true;
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WallpaperService.MyBinder myBinder = (WallpaperService.MyBinder) service;
            WallpaperService wp_service = myBinder.getService();
            mServiceBound = true;
        }
    };

    private void updateUI(Intent intent) {
        String steps = intent.getStringExtra("steps");
        TextView progress = findViewById(R.id.curr_progress);
        SharedPreferences settings = getSharedPreferences("settings", 0);
        int goal = settings.getInt("steps", 2000);
        progress.setText(steps + " / " + String.valueOf(goal));
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(intent);
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.view_all:
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivityForResult(intent, 1);
                return true;

            case R.id.settings:
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(i, 1);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
