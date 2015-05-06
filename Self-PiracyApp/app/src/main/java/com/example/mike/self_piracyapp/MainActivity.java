package com.example.mike.self_piracyapp;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognitionService;
import android.speech.SpeechRecognizer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.speech.RecognizerIntent;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
    Main Activity of the Self-Piracy App
    Written by: Michael Santry
    resources used to help write/debug this code:
        http://www.jameselsey.co.uk/blogs/techblog/android-how-to-implement-voice-recognition-a-nice-easy-tutorial/
 */

public class MainActivity extends ActionBarActivity {

    private static final int REQUEST_CODE = 1;

    RelativeLayout background;
    Button StartRecordingButton;
    Button StatsButton;
    Button GetSimilarButton;
    Button NextButton;
    VoiceRecognitionClass VRClass;
    Intent intent;
    DatabaseHandler databaseHandler;
    TextView whatUserJustSaidBoxTextView;
    TextView similarSayingsTextView;
    TextView dateText;
    TextView warningText;
    ArrayList<String> lastSaidCleanWords;
    DataBaseWordChunk lastWordChunk;
    ArrayList<DataBaseWordChunk> lastSimilarWordChunks;
    Integer lastSimilarWordChunksCount;
    HashMap<Integer,Boolean> safeWords;


    /*
        On creation of main activity, this sets up buttons and onClick actions.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        background = (RelativeLayout) findViewById(R.id.mainBackground);
        databaseHandler = new DatabaseHandler(getApplicationContext());

        StartRecordingButton = (Button) findViewById(R.id.StartRecordingButton);
        StatsButton = (Button) findViewById(R.id.StatsButton);
        GetSimilarButton = (Button) findViewById(R.id.getSimilar);
        NextButton = (Button) findViewById(R.id.nextButton);

        lastSaidCleanWords = new ArrayList<String>();
        lastSimilarWordChunks = new ArrayList<DataBaseWordChunk>();
        lastSimilarWordChunksCount = 0;
        safeWords = new HashMap<Integer, Boolean>();

        MediaStore.Audio.Media media = new MediaStore.Audio.Media();
        MediaRecorder mediaRecorder = new MediaRecorder();
        VRClass = new VoiceRecognitionClass();

        whatUserJustSaidBoxTextView = (TextView) findViewById(R.id.WhatWasJustSaidTextBox);
        whatUserJustSaidBoxTextView.setMovementMethod(new ScrollingMovementMethod());

        similarSayingsTextView = (TextView) findViewById(R.id.SimilarSayingsTextbox);
        similarSayingsTextView.setMovementMethod(new ScrollingMovementMethod());

        dateText = (TextView) findViewById(R.id.dateText);
        warningText = (TextView) findViewById(R.id.WarningText);

//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        mediaRecorder.setOutputFile(fileName);
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        /*
        When StartRecordingButton is clicked this uses the google API to begin recording and
        then return results
         */
        StartRecordingButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                similarSayingsTextView.setText("");
                dateText.setText("");
                warningText.setText("");
                intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

                try {
                    startActivityForResult(intent, REQUEST_CODE);
                } catch (ActivityNotFoundException a) {
                    Toast t = Toast.makeText(getApplicationContext(),
                            "Oops! Your device doesn't support Speech-to-Text",
                            Toast.LENGTH_SHORT);
                    t.show();
                }

            }
        });

        /*
        When StatsButton is clicked this takes the user to the StatsActivity Activity
         */
        StatsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(MainActivity.this, StatsActivity.class);
                startActivity(intent);
            }
        });

        /*
        When GetSimilarButton is clicked, this method provides a list of similar word chunks to be
        shown in the similar sayings text box on the main activity page.
         */
        GetSimilarButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                safeWords = new HashMap<Integer, Boolean>();
                lastSimilarWordChunks = getSimilarWordChunkResults(lastWordChunk);

                if(lastSimilarWordChunks.size()>0) {
                    DataBaseWordChunk bestChunk = lastSimilarWordChunks.get(0);
                    // Set warning of high similarity
                    if(!safeWords.get(bestChunk.getId())){
                        warningText.setText("WARNING: HIGH SIMILARITIES!");
                    }

                    similarSayingsTextView.setText(bestChunk.getChunk());
                    dateText.setText(getTimeString(bestChunk));
                    lastSimilarWordChunksCount = 1;
                }
                else{
                    similarSayingsTextView.setText("No similar sayings found");
                }
            }
        });

        /*
        When NextButton is clicked, this method allows the user to see the next closest saying via
        the similar sayings textbox found on the main activity page.
         */
        NextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(lastSimilarWordChunks.size()>lastSimilarWordChunksCount) {
                    similarSayingsTextView.setText(lastSimilarWordChunks.get(lastSimilarWordChunksCount).getChunk());
                    dateText.setText(getTimeString(lastSimilarWordChunks.get(lastSimilarWordChunksCount)));
                    if(!safeWords.get(lastSimilarWordChunks.get(lastSimilarWordChunksCount).getId())){
                        warningText.setText("WARNING: HIGH SIMILARITIES!");
                    }
                    else{
                        warningText.setText("");
                    }
                    lastSimilarWordChunksCount++;
                }
            }

        });
    }


    /*
    When the recognize_speech intent returns the best guesses for what the user says, this method
    adds the best guess to the database via the wordChunk table
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        if(resultCode == RESULT_OK) {

            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            ArrayList<String> cleanWords = VRClass.recognizedSpeechInitialAnalyzer(results.get(0));
            lastSaidCleanWords = cleanWords;
            Map<String, Integer> timeMap = VRClass.getAllTimeInfo();

            DataBaseWordChunk dbWordChunk = new DataBaseWordChunk(0, results.get(0), timeMap.get("second"),
                    timeMap.get("minute"), timeMap.get("hour"), timeMap.get("pmAm"), timeMap.get("dayOfMonth"),
                    timeMap.get("month")+1);
            databaseHandler.createWordChunk(dbWordChunk);


            lastWordChunk = databaseHandler.getDataBaseWordChunk(timeMap.get("month"), timeMap.get("dayOfMonth"),
                    timeMap.get("pmAm"), timeMap.get("hour"), timeMap.get("minute"), timeMap.get("second"));

            whatUserJustSaidBoxTextView.setText(results.get(0));

            databaseTheWords(cleanWords);
            lastSimilarWordChunksCount = 0;

        }

        super.onActivityResult(requestCode, resultCode, data);

    }


    /*
    Adds the individual words to the database. If word already exists, add to its count.
     */
    private void databaseTheWords(ArrayList<String> theBirds){
        for(String i : theBirds){

            DataBaseActualWord DBActWord = databaseHandler.getActualWordFromDatabase(i);

            if(DBActWord.getWord().equals("BUSTEDSTUFF")){
                DataBaseActualWord dbActWord = new DataBaseActualWord(0, i, 1);
                databaseHandler.createActualWord(dbActWord);
            }
            else{
                DataBaseActualWord dbActWord = DBActWord;
                dbActWord.setCount(dbActWord.getCount() + 1);
                databaseHandler.updateActualWord(dbActWord);
            }

        }
    }

    /*
    Tests to see if there is any exact chunk matches, list of all exact matches is returned.
     */
    public ArrayList<DataBaseWordChunk> findExactChunkMaskReplica(String chunk){
        ArrayList<DataBaseWordChunk> retList = new ArrayList<DataBaseWordChunk>();
        ArrayList<DataBaseWordChunk> allChunks = databaseHandler.getAllWordChunksFromDatabase();
        for(DataBaseWordChunk i : allChunks){
            if(i.getChunk().equals(chunk)){
                retList.add(i);
            }
        }
        return retList;
    }


    /*
    This looks for any nearly identical word chunks within the database.
     */
    public ArrayList<DataBaseWordChunk> getSimilarWordChunkResults(DataBaseWordChunk chunk){
        ArrayList<DataBaseWordChunk> allChunks = databaseHandler.getAllWordChunksFromDatabase();
        ArrayList<DataBaseWordChunk> resultChunks = new ArrayList<DataBaseWordChunk>();
        int sizeOfLastCleanWords = lastSaidCleanWords.size();

        // This section wil do the obvious eliminations.
        for(DataBaseWordChunk i : allChunks){
            ArrayList<String> individualWords = VRClass.getWords(i.getChunk());
            int count = 0;
            for(String j : individualWords){
                for(String k : lastSaidCleanWords){
                    if(k.equals(j)){
                        count++;
                    }
                }
            }
            if(count > 2){
                resultChunks.add(i);
            }
        }

        ArrayList<DataBaseWordChunk> safeList = new ArrayList<DataBaseWordChunk>();
        ArrayList<DataBaseWordChunk> notSafeList = new ArrayList<DataBaseWordChunk>();

        //Look for exact or close to exact results
        for(DataBaseWordChunk i : resultChunks){
            ArrayList<String> individualWordsTwo = VRClass.getWords(i.getChunk());
            int resultWordCount = individualWordsTwo.size();
            int similarCount = 0;
            for(String j : lastSaidCleanWords){
                for(String k : individualWordsTwo){
                    if(j.equals(k)){
                        similarCount++;
                    }
                }
            }
            if((similarCount > (resultWordCount/2)) && (similarCount > (sizeOfLastCleanWords/2))) {
                notSafeList.add(i);

            }else{
                safeList.add(i);
            }

        }

        notSafeList = flipList(notSafeList);
        safeList = flipList(safeList);
        ArrayList<DataBaseWordChunk> iAmCompleteList = new ArrayList<DataBaseWordChunk>();

        for(DataBaseWordChunk p : notSafeList){
            iAmCompleteList.add(p);
            safeWords.put(p.getId(), false);

        }
        for(DataBaseWordChunk q : safeList){
            iAmCompleteList.add(q);
            safeWords.put(q.getId(),true);

        }

        return iAmCompleteList;
    }

    /*
    Reorders or flips a list such that the last item appears first and the first appears last.
     */
    public ArrayList<DataBaseWordChunk> flipList(ArrayList<DataBaseWordChunk> list){
        ArrayList<DataBaseWordChunk> orderedResultChunks = new ArrayList<DataBaseWordChunk>();
        if(list.size()>1) {
            for (int i = list.size() - 2; i >= 0; i = i - 1) {
                orderedResultChunks.add(list.get(i));
            }
        }
        return orderedResultChunks;
    }


    /*
    Returns the time and date string that will be put in the date field on the main activity page.
     */
    public String getTimeString(DataBaseWordChunk chunk){

        String amPm = "";
        if(chunk.getAmPm()==0){
            amPm = "AM";
        }
        else{
            amPm = "PM";
        }
        String retString = "";
        if(chunk.getHour() == 0){
             retString = chunk.getMonth() + "/" + chunk.getDay() + "/2015  " + "12" + ":";
        }
        else{
            retString = chunk.getMonth() + "/" + chunk.getDay() + "/2015  " + chunk.getHour() + ":";
        }
        if(chunk.getMinute()<10) {
             retString = retString + "0" + chunk.getMinute() +  ":" + chunk.getSecond() + " " + amPm;
        }
        else {
            retString = retString + chunk.getMinute() + ":" + chunk.getSecond() + " " + amPm;
        }

        return retString;

    }

}