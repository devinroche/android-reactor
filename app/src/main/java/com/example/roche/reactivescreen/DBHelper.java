package com.example.roche.reactivescreen;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class DBHelper extends SQLiteOpenHelper {

    static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "exerciseDatabase";
    static final String TABLE_EXERCISE = "tableExercise";
    static final String ID = "_id";
    static final String STEPS = "steps";
    static final String DATE = "date";
    String TAG = "DBHelper";

    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        context.deleteDatabase(DATABASE_NAME); // clears db
    }

    private static Exercise toExercise(Cursor cursor) {
        Exercise note = null;
        if (cursor.moveToNext())
            note = new Exercise();

        note.setId(cursor.getInt(0));
        note.setSteps(cursor.getInt(1));
        note.setDate(cursor.getString(2));

        return note;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sqlCreateTable = "CREATE TABLE " + TABLE_EXERCISE +
                "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                STEPS + " INTEGER, " +
                DATE + " STRING)";

        sqLiteDatabase.execSQL(sqlCreateTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public void insertExercise(Exercise ex) {
        // SQL Statement to insert a new row with
        // values from a Note Object
        String insertString = "INSERT INTO " + TABLE_EXERCISE + " VALUES(null, " +
                "'" + ex.getSteps() + "', " +
                "'" + ex.getDate() + "')";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(insertString);
        db.close();
    }

    public Cursor getSelectAllExercisesCursor() {
        String selectSQLString = "SELECT * from " + TABLE_EXERCISE;
        return getReadableDatabase().rawQuery(selectSQLString, null);
    }

    public Exercise getNoteById(long id) {
        String selectByIdSQL = "SELECT * from " + TABLE_EXERCISE +
                " WHERE " + ID + " = " + id;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectByIdSQL, null);
        return toExercise(cursor);
    }

    public void deleteAllExercises() {
        // SQL Statement to delete all notes from TABLE_NOTES
        String deleteAllString = "DELETE from " + TABLE_EXERCISE;
        getWritableDatabase().execSQL(deleteAllString);
    }

    public ArrayList<String> getHistory(int id) {
        // SQL Statement to get all ids and titles from TABLE_NOTES
        String sqlSelectTitles = "SELECT " + ID + ", " + DATE + " from " + TABLE_EXERCISE;
        System.out.println(sqlSelectTitles);
        Cursor cursor = getReadableDatabase().rawQuery(sqlSelectTitles, null);
        ArrayList<String> titles = new ArrayList<>();
        while (cursor.moveToNext())
            if (cursor.getInt(0) != id)
                titles.add(cursor.getString(1));
        cursor.close();
        return titles;
    }

    public ArrayList<String> getHistory() {
        return getHistory(-1);
    }
}