package com.example.roche.reactivescreen;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;


public class HistoryActivity extends AppCompatActivity {

    static final String NOTE = "note";
    static final String TITLES = "titles";
    static final String NEW = "new";
    private static final String TAG = "HistoryActivity";
    private static final int NEW_NOTE_CODE = 1;
    private static final int EDIT_NOTE_CODE = 2;
    DBHelper dbhelper;
    SimpleCursorAdapter cursorAdapter;
    ListView noteListView;

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
    /**
     * After completing a note, insert/update data in database,
     * and update list view
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            // Updates list view
            cursorAdapter.changeCursor(dbhelper.getSelectAllExercisesCursor());
        }
    }
}
