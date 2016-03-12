package com.padgrayson91.flashcards;

import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.padgrayson91.flashcards.Constants.KEY_ANSWER;
import static com.padgrayson91.flashcards.Constants.KEY_ID;
import static com.padgrayson91.flashcards.Constants.KEY_OPTIONS;
import static com.padgrayson91.flashcards.Constants.KEY_QUESTION;
import static com.padgrayson91.flashcards.Constants.KEY_TIMES_CORRECT;
import static com.padgrayson91.flashcards.Constants.KEY_TIMES_INCORRECT;

/**
 * Utility to create and edit individual flash cards
 * Created by patrickgrayson on 3/10/16.
 */
public class Card implements Comparable{
    private JSONObject mJson;
    private String question;
    private String answer;
    private int incorrectCount;
    private int correctCount;
    private ArrayList<String> options;
    public String id;

    /***
     * Constructor to be used when building a card for the first time
     * @param id
     */
    public Card(String id){
        this.id = id;
        this.mJson = new JSONObject();
        this.incorrectCount = 0;
        this.correctCount = 0;
        this.options = new ArrayList<String>();
    }


    /***
     * Constructor to be used when retrieving a card from JSON
     * @param card
     * @param id should be the id already assigned to the card
     */
    public Card(JSONObject card, String id) {
        mJson = card;
        this.id = id;
        this.options = new ArrayList<String>();

        //Check for all the keys to initialize the card
        try {
            if (mJson.has(KEY_QUESTION)) {
                question = mJson.getString(KEY_QUESTION);
            }
            if (mJson.has(KEY_ANSWER)) {
                answer = mJson.getString(KEY_ANSWER);
            }
            if (mJson.has(KEY_TIMES_INCORRECT)) {
                incorrectCount = mJson.getInt(KEY_TIMES_INCORRECT);
            }
            if (mJson.has(KEY_TIMES_CORRECT)) {
                correctCount = mJson.getInt(KEY_TIMES_CORRECT);
            }
            if (mJson.has(KEY_OPTIONS)) {
                JSONArray temp = mJson.getJSONArray(KEY_OPTIONS);
                for(int i = 0; i < temp.length(); i++){
                    options.add(temp.getString(i));
                }
            }
        } catch (JSONException ex) {
            //Should never get here
        }

        //TODO: Card types
    }




    public JSONObject toJSON(){
        try {
            mJson.put(KEY_ID, id);
            mJson.put(KEY_QUESTION, question);
            mJson.put(KEY_ANSWER, answer);
            mJson.put(KEY_TIMES_CORRECT, correctCount);
            mJson.put(KEY_TIMES_INCORRECT, incorrectCount);
            JSONArray options = new JSONArray(this.options);
            mJson.put(KEY_OPTIONS, options);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mJson;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public void incrementCorrectCount(){
        correctCount++;
    }

    public int getIncorrectCount() {
        return incorrectCount;
    }

    public void setIncorrectCount(int incorrectCount) {
        this.incorrectCount = incorrectCount;
    }

    public void incrementIncorrectCount(){
        incorrectCount++;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<String> options) {
        this.options = new ArrayList<String>();
        for(String s: options){
            this.options.add(s);
        }
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    //TODO: make a more interesting scoring algorithm
    public int getScore(){
        return correctCount - incorrectCount;
    }

    public int getColor(){
        int score = getScore();

        if(score > 0){
            return Color.argb(255, 255 - 10*score, 255, 255 - 10*score);
        } else {
            return Color.argb(255, 255, 255 + 10*score, 255 + 10*score);
        }
    }

    @Override
    public String toString(){
        return mJson.toString();
    }


    //TODO: should have comparison modes so that we can sort in other ways
    @Override
    public int compareTo(Object other){
        if(other instanceof Card){
            if(this.getScore() == ((Card) other).getScore()){
                return this.id.compareTo(((Card) other).id);
            }
            else {
                return this.getScore() - ((Card) other).getScore();
            }
        }
        else {
            return 0;
        }
    }
}
