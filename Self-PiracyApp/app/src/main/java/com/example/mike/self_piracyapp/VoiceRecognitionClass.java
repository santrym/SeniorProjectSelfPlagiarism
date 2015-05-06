package com.example.mike.self_piracyapp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Mike on 3/24/2015.
 */
public class VoiceRecognitionClass extends Activity{

    /**
     * Class that handles voice recognition when button is pressed.
     */

        private static final int REQUEST_CODE = 1;
        private ListView wordsList;
        private Intent intent;
        private ArrayList<String> recognitionResults;
        private Button speechButton;
        private ArrayList<String> garbageWords;
        private DatabaseHandler databaseHandler;


    /**
     * Performs acts on the returned list of results from Speech Recognizer action
     */
    public ArrayList<String> recognizedSpeechInitialAnalyzer(String chunk)
    {

        ArrayList<String> individualWords = getWords(chunk);
        individualWords = removeGarbageWords(individualWords);
        return individualWords;

    }

    /*
    This class will return a map of time and date elements. Used later to store chunks.
     */
    public Map<String, Integer> getAllTimeInfo(){

        Calendar cal = Calendar.getInstance();
        Map<String, Integer> timeMap = new HashMap<String, Integer>();

        timeMap.put("month", cal.get(Calendar.MONTH));
        timeMap.put("dayOfMonth", cal.get(Calendar.DAY_OF_MONTH));
        timeMap.put("minute", cal.get(Calendar.MINUTE));
        timeMap.put("hour", cal.get(Calendar.HOUR));
        timeMap.put("pmAm", cal.get(Calendar.AM_PM));
        timeMap.put("second", cal.get(Calendar.SECOND));

        return timeMap;

    }


    /*
    This method takes a String, and separates individual words, returning them in an arrayList.
     */
    public ArrayList<String> getWords(String theString){

        ArrayList<String> retList = new ArrayList<>();
        String theStringLowered = lowerLetters(theString);

        if(theStringLowered.length() == 0){
            return retList;
        }
        else {
            int beginningIndex = 0;
            int endIndex = 0;
            while (endIndex < theStringLowered.length()) {
                if (theStringLowered.substring(endIndex, endIndex + 1).equals(" ")) {
                    retList.add(theStringLowered.substring(beginningIndex, endIndex));
                    beginningIndex = endIndex + 1;
                    endIndex = endIndex++;
                }
                endIndex++;
            }
            retList.add(theStringLowered.substring(beginningIndex, theStringLowered.length()));
        }
        return retList;
    }


    /*
    Removes the non crucial words within the list of words. Words like "a" and "the"
     */
    public ArrayList<String> removeGarbageWords(ArrayList<String> list){

        setGarbageWords();
        ArrayList<String> retList = new ArrayList<String>();
        ArrayList<String> gWords = getGarbageWords();
        for(String a : list){
            boolean clear = true;
            for(String b : gWords){
                if(a.equals(b)){
                    clear = false;
                }
            }
            if(clear){
                retList.add(a);
            }
        }
        return retList;
    }


    /*
    Adds common and unimportant words to garbageWord list.
     */
    public void setGarbageWords(){

        garbageWords = new ArrayList<String>();

        garbageWords.add("the");
        garbageWords.add("a");
        garbageWords.add("was");
        garbageWords.add("is");
        garbageWords.add("hey");
        garbageWords.add("yes");
        garbageWords.add("no");
        garbageWords.add("if");
        garbageWords.add("and");
        garbageWords.add("or");
        garbageWords.add("either");

    }

    /*
    returns a list of non-useful words.
     */
    public ArrayList<String> getGarbageWords(){

        return garbageWords;

    }

    /*
    Lowers any upper case letters down to lower case.
     */
    public String lowerLetters(String stringer){
        String retString = "";
        for(int i = 0; i < stringer.length(); i++){
            if(Character.isUpperCase(stringer.charAt(i))){
                retString = retString + Character.toLowerCase(stringer.charAt(i));
            }
            else{
                retString = retString + stringer.charAt(i);
            }
        }
        return retString;
    }


}