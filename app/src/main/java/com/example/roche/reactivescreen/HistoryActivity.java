package com.example.roche.reactivescreen;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


public class HistoryActivity extends AppCompatActivity {

    static final String NOTE = "note";
    static final String TITLES = "titles";
    static final String NEW = "new";
    private static final String TAG = "HistoryActivity";
    private static final int NEW_NOTE_CODE = 1;
    private DBHelper dbhelper;
    private SimpleCursorAdapter cursorAdapter;
    private ListView noteListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        // database
        dbhelper = new DBHelper(this);
        cursorAdapter = new SimpleCursorAdapter(
                this, R.layout.activity_list_item,
                dbhelper.getSelectAllExercisesCursor(),
                new String[]{DBHelper.STEPS, DBHelper.DATE},
                new int[]{R.id.text1, R.id.text2},
                0) {

        };

        GridLayout gridLayout = new GridLayout(this);
        gridLayout.setColumnCount(1);


        noteListView = new ListView(this);
        noteListView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        noteListView.setAdapter(cursorAdapter);
        gridLayout.addView(noteListView);
        setContentView(gridLayout);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            // Updates list view
            cursorAdapter.changeCursor(dbhelper.getSelectAllExercisesCursor());
        }
    }
}
