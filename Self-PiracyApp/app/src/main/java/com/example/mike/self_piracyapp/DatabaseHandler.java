package com.example.mike.self_piracyapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mike on 4/12/2015.
 * This class contains all methods that are used to communicate with the database.
 */
public class DatabaseHandler extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;

    private static final String
    DATABASE_NAME = "wordCollectionDB",
    TABLE_ACTUALWORDS = "actualWords",

    KEY_ACTUALWORDID = "id",
    KEY_ACTUALWORD = "actualWord",
    KEY_COUNT = "count",

    TABLE_WORDCHUNKS = "wordChunks",
    KEY_CHUNKID = "chunkID",
    KEY_WORDCHUNK = "wordChunk",
    KEY_AMPM = "ampm",
    KEY_MINUTE = "minute",
    KEY_HOUR = "hour",
    KEY_DAY = "day",
    KEY_MONTH = "month",
    KEY_SECOND = "second";


    /*
    The Constructor for the DatabaseHandler class.
     */
    public DatabaseHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /*
    Overridden onCreate method. When this class is created, this method sets up the tables.
     */
    @Override
    public void onCreate(SQLiteDatabase database){
        database.execSQL("CREATE TABLE " + TABLE_ACTUALWORDS + "(" + KEY_ACTUALWORDID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_ACTUALWORD + " TEXT," + KEY_COUNT + " INTEGER)");
        database.execSQL("CREATE TABLE " + TABLE_WORDCHUNKS + "(" + KEY_CHUNKID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_WORDCHUNK + " TEXT," + KEY_MINUTE + " INTEGER," + KEY_HOUR + " INTEGER," +
                KEY_AMPM + " INTEGER," + KEY_DAY + " INTEGER," + KEY_MONTH + " INTEGER," + KEY_SECOND + " INTEGER)");
    }

    /*
    Necessary method. Used when upgrades to tables are required, and you don't want to lose
    previously stored table entries.
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion){
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTUALWORDS);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_WORDCHUNKS);

        onCreate(database);
    }


    /*
    Add a word that isn't already in the database.
     */
    public void createActualWord(DataBaseActualWord actualWord){

        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_ACTUALWORD, actualWord.getWord());
        values.put(KEY_COUNT, actualWord.getCount());

        database.insert(TABLE_ACTUALWORDS, null, values);
        database.close();
    }


    /*
    Add a WordChunk to the database
     */
    public void createWordChunk(DataBaseWordChunk wordChunk){

        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_WORDCHUNK, wordChunk.getChunk());
        values.put(KEY_AMPM, wordChunk.getAmPm());
        values.put(KEY_SECOND, wordChunk.getSecond());
        values.put(KEY_MINUTE, wordChunk.getMinute());
        values.put(KEY_HOUR, wordChunk.getHour());
        values.put(KEY_DAY, wordChunk.getDay());
        values.put(KEY_MONTH, wordChunk.getMonth());

        database.insert(TABLE_WORDCHUNKS, null, values);
        database.close();
    }

    /*
    Retrieves all the WordChunks in the database
     */
    public ArrayList<DataBaseWordChunk> getAllWordChunksFromDatabase(){

        ArrayList<DataBaseWordChunk> retList = new ArrayList<DataBaseWordChunk>();
        SQLiteDatabase database = getReadableDatabase();
        Cursor theCursor = database.rawQuery("SELECT * FROM " + TABLE_WORDCHUNKS, null);

        if(theCursor.moveToFirst()){
            while(theCursor.moveToNext()){
                DataBaseWordChunk DBWordChunk = new DataBaseWordChunk(Integer.parseInt(theCursor.getString(0)), theCursor.getString(1),
                        Integer.parseInt(theCursor.getString(7)),Integer.parseInt(theCursor.getString(2)),
                        Integer.parseInt(theCursor.getString(3)), Integer.parseInt(theCursor.getString(4)),
                        Integer.parseInt(theCursor.getString(5)), Integer.parseInt(theCursor.getString(6)));
                retList.add(DBWordChunk);
            }

        }

        database.close();
        theCursor.close();
        return retList;
    }

    /*
    Retrieves an array of all the ActualWords stored in the ActualWordTable
     */
    public ArrayList<DataBaseActualWord> getAllActualWords(){

        ArrayList<DataBaseActualWord> retList = new ArrayList<DataBaseActualWord>();
        SQLiteDatabase database = getReadableDatabase();
        Cursor theCursor = database.rawQuery("SELECT * FROM " + TABLE_ACTUALWORDS, null);

        if(theCursor.moveToFirst()){
            while(theCursor.moveToNext()){
                DataBaseActualWord DBActualWord = new DataBaseActualWord(Integer.parseInt(theCursor.getString(0)), theCursor.getString(1), Integer.parseInt(theCursor.getString(2)));
                retList.add(DBActualWord);
            }
        }
        database.close();
        theCursor.close();
        return retList;
    }


    /*
    Retrieves the DataBaseWordChunk object for the unique key of listed parameters.
     */
    public DataBaseWordChunk getDataBaseWordChunk(int month, int day, int ampm, int hour, int minute, int second){

        SQLiteDatabase database = getReadableDatabase();
        Cursor retCursor = database.rawQuery("SELECT * FROM " + TABLE_WORDCHUNKS + " WHERE " + KEY_MONTH + " = ?  AND " + KEY_DAY + " = ?  AND " + KEY_AMPM + " = ?  AND " + KEY_HOUR + " = ?  AND " + KEY_MINUTE + " = ?  AND " + KEY_SECOND + " = ?" ,  new String[] {String.valueOf(month), String.valueOf(day), String.valueOf(ampm), String.valueOf(hour), String.valueOf(minute), String.valueOf(second)});

        if(retCursor != null && (retCursor.getCount() != 0)){
            DataBaseWordChunk retChunk = new DataBaseWordChunk(Integer.parseInt(retCursor.getString(0)), retCursor.getString(1),
                    Integer.parseInt(retCursor.getString(2)), Integer.parseInt(retCursor.getString(3)),
                    Integer.parseInt(retCursor.getString(4)), Integer.parseInt(retCursor.getString(5)),
                    Integer.parseInt(retCursor.getString(6)), Integer.parseInt(retCursor.getString(7)));
            return retChunk;
        }
        return null;
    }


    /*
    returns the DataBaseActualWord for the given input string if it exists
     */
    public DataBaseActualWord getActualWordFromDatabase(String word){

        SQLiteDatabase database = getReadableDatabase();
        Cursor retCursor = database.rawQuery("SELECT * FROM " + TABLE_ACTUALWORDS + " WHERE " +
                KEY_ACTUALWORD + " = ?",  new String[] {word});
        DataBaseActualWord DBActualWord = new DataBaseActualWord(0,"BUSTEDSTUFF",0);

        if(retCursor != null && (retCursor.getCount() != 0)){
            retCursor.moveToFirst();
            DBActualWord = new DataBaseActualWord(Integer.parseInt(retCursor.getString(0)), retCursor.getString(1),
                    Integer.parseInt(retCursor.getString(2)));
        }

        database.close();
        retCursor.close();
        return DBActualWord;
    }

    /*
    deletes a word from TABLE_ACTUALWORDS
     */
    public void deleteActualWordFromDatabase(DataBaseActualWord theBird){
        SQLiteDatabase database = getWritableDatabase();
        database.delete(TABLE_ACTUALWORDS, KEY_ACTUALWORDID + "=?", new String[] {String.valueOf(theBird.getId())});
        database.close();
    }


    /*
    Updates an ActualWord from the TABLE_ACTUALWORDS.
     */
    public void updateActualWord(DataBaseActualWord theBird){
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_ACTUALWORDID, theBird.getId());
        values.put(KEY_ACTUALWORD, theBird.getWord());
        values.put(KEY_COUNT, theBird.getCount());

        int res = database.update(TABLE_ACTUALWORDS, values, KEY_ACTUALWORDID + "=?", new String[] { String.valueOf(theBird.getId())});

        if(res != 1){
            System.out.println("Mr. President you better have a look at this");
        }

        database.close();
    }


    /*
    This returns the int value for teh number of unique words int he TABLE_ACTUALWORDS.
     */
    public int getTotalActualWordCount(){

        int retValue;
        SQLiteDatabase database = getWritableDatabase();
        Cursor theCursor = database.rawQuery("SELECT * FROM " + TABLE_ACTUALWORDS, null);
        retValue = theCursor.getCount();
        database.close();
        theCursor.close();
        return retValue;

    }


    /*
    Returns the average amount of times each word in the ActualWordsTable is used.
     */
    public int getActualWordsUsageAverage(){
        int average = (getNumberOfAllActualWordUses() * 10) / getTotalActualWordCount();
        return average;
    }


    /*
    Returns the cumulative sum of all word usage counts.
     */
    public int getNumberOfAllActualWordUses(){
        ArrayList<DataBaseActualWord> list = getAllActualWords();
        int count = 0;
        for(DataBaseActualWord i : list){
            count = count + i.getCount();
        }
        return count;
    }

}