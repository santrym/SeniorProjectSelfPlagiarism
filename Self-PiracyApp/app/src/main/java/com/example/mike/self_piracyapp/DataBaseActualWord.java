package com.example.mike.self_piracyapp;

/**
 * Created by Mike Santry on 4/12/2015.
 * This class is used to create and manipulate ActualWord objects which will be added and
 * retrieved from the ActualWordsTable.
 */
public class DataBaseActualWord {

    private String _word;
    private int _id, _count;

    /*
    Adds the actual word to the database.
     */
    public DataBaseActualWord(int id, String word, int count){
        _word = word;
        _id = id;
        _count = count;
    }

    public int getId(){return _id; }

    public String getWord(){return _word; }

    public int getCount() {return _count; }

    public void setCount(int a){_count = a;}


}
