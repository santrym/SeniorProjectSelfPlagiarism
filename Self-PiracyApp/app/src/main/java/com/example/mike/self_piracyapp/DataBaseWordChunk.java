package com.example.mike.self_piracyapp;

/**
 * Created by Mike on 4/13/2015.
 * This object will be the entire string of speech spoken within one recording session.
 * This string is also stored with the date and time it was spoken.
 */
public class DataBaseWordChunk {

    private String _chunk;
    private int _id, _ampm, _minute, _hour, _day, _month, _second;

    /*
    Creates DataBaseWordChunk, ?Not sure if I need to give it an ID?
     */
    public DataBaseWordChunk(int id, String chunk, int second, int minute, int hour, int ampm, int day, int month){
        _id = id;
        _chunk = chunk;
        _second = second;
        _minute = minute;
        _hour = hour;
        _ampm = ampm;
        _day = day;
        _month = month;
    }

    public int getId(){return _id;}

    public String getChunk(){return _chunk;}

    public void setChunk(String a){
        _chunk = a;
    }

    public int getAmPm() {return _ampm;}

    public int getMinute() {return _minute;}

    public int getHour() {return _hour;}

    public int getDay() {return _day;}

    public int getMonth() {return _month;}

    public int getSecond() {return _second;}

}
