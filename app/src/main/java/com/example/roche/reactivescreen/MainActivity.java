package com.example.roche.reactivescreen;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    WallpaperService wp_service;
    boolean mServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startService(View view) {
        Intent intent = new Intent(this, WallpaperService.class);
        startService(intent);
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
//        unbindService(new Intent(getBaseContext(), WallpaperService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m){
        m.add("set wallpaper")
                .setOnMenuItemClickListener(this.setWallpaperClickListener)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return super.onCreateOptionsMenu(m);
    }

    OnMenuItemClickListener setWallpaperClickListener = new OnMenuItemClickListener(){
        @SuppressLint("ResourceType")
        public boolean onMenuItemClick(MenuItem i){
            WallpaperManager wpManager = WallpaperManager.getInstance(MainActivity.this);

            try {
                wpManager.setResource(R.drawable.green);
                Toast.makeText(MainActivity.this, "set wallpaper", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WallpaperService.MyBinder myBinder = (WallpaperService.MyBinder) service;
            wp_service = myBinder.getService();
            mServiceBound = true;
        }
    };
}
