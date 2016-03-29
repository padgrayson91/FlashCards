package com.padgrayson91.flashcards;

import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.padgrayson91.flashcards.Constants.KEY_ANSWER;
import static com.padgrayson91.flashcards.Constants.KEY_ID;
import static com.padgrayson91.flashcards.Constants.KEY_LAST_CORRECT;
import static com.padgrayson91.flashcards.Constants.KEY_OPTIONS;
import static com.padgrayson91.flashcards.Constants.KEY_QUESTION;
import static com.padgrayson91.flashcards.Constants.KEY_TIMES_CORRECT;
import static com.padgrayson91.flashcards.Constants.KEY_TIMES_INCORRECT;

/**
 * Utility to create and edit individual flash cards
 * Created by patrickgrayson on 3/10/16.
 */
public class Card implements Comparable{

    private long timeLastCorrect;
    private JSONObject mJson;
    private String question;
    private String answer;
    private int incorrectCount;
    private int correctCount;
    private ArrayList<String> options;
    public String id;

    private static final int MIN_R = 50;
    private static final int MIN_G = 50;
    private static final int MIN_B = 80;
    private static final int BLUE_FACTOR = 7; //higher number = less blue
    private static final int RED_FACTOR = 10; // higher number = less red
    private static final int GREEN_FACTOR = 10; // higher number = less green
    private static final int MAX_SCORE = 20;
    private static final int MIN_SCORE = -10;
    private static final long NEVER_PLAYED = 0;
    private static final int TIME_WEIGHT_CONSTANT = 5; //number of hours after which a point is lost
    private static final int NEVER_PLAYED_DEDUCTION_CONSTANT = 1; //number of points deducted from the score of a brand new card


    public static final int SORT_MODE_SCORE = 0;
    public static final int SORT_MODE_ALPHA = 1;
    private static int SORT_MODE;

    /***
     * Constructor to be used when creating a brand new card.  Used to generate a unique Id
     *
     */
    public Card(){
        this.id = UUID.randomUUID().toString();
        this.mJson = new JSONObject();
        this.incorrectCount = 0;
        this.correctCount = 0;
        this.options = new ArrayList<String>();
    }

    /***
     * Constructor to be used when instantiating a new object for an existing card with all
     * values reset
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
        //TODO: change this to new format from web app
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
            if(mJson.has(KEY_LAST_CORRECT)){
                timeLastCorrect = mJson.getLong(KEY_LAST_CORRECT);
            }
        } catch (JSONException ex) {
            //Should never get here
        }

        //TODO: Card types
    }




    public JSONObject toJSON(){
        //TODO: change this to new format from web app
        try {
            mJson.put(KEY_ID, id);
            mJson.put(KEY_QUESTION, question);
            mJson.put(KEY_ANSWER, answer);
            mJson.put(KEY_TIMES_CORRECT, correctCount);
            mJson.put(KEY_TIMES_INCORRECT, incorrectCount);
            JSONArray options = new JSONArray(this.options);
            mJson.put(KEY_OPTIONS, options);
            mJson.put(KEY_LAST_CORRECT, timeLastCorrect);
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

    /***
     * Method to get the score associated with a card.  Cards with more correct responses will have
     * a higher score.  Cards lose points if they haven't been correctly answered in a while.
     * @return
     */
    public int getScore(){
        int score = correctCount - incorrectCount;
        if(timeLastCorrect != NEVER_PLAYED) {
            long timeGap = System.currentTimeMillis() - timeLastCorrect;
            long timeGapHours = TimeUnit.MILLISECONDS.toHours(timeGap);
            score -= timeGapHours / TIME_WEIGHT_CONSTANT;
        } else {
            //deduct points for a card that was never played
            score -= NEVER_PLAYED_DEDUCTION_CONSTANT;
        }
        score = Math.max(score, MIN_SCORE);
        score = Math.min(score, MAX_SCORE);
        return score;
    }

    public int getColor(){
        int score = getScore();

        if(score > 0){
            return Color.argb(255, Math.max(255 - RED_FACTOR*score, MIN_R), 255, Math.max(255 - BLUE_FACTOR*score, MIN_B));
        } else {
            return Color.argb(255, 255, Math.max(255 + GREEN_FACTOR*score, MIN_G), Math.max(255 + BLUE_FACTOR*score, MIN_B));
        }
    }

    public int getDarkColor(){
        int color = getColor();
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        if(green > red){
            //We're green, make us more green
            color = Color.argb(255, red - MIN_R, green, blue - MIN_B);
        } else if(green < red){
            //We're red, make us more red
            color = Color.argb(255, red, green - MIN_G, blue - MIN_B);
        } else {
            //We're white, so use gray
            color = Color.LTGRAY;
        }
        return color;
    }

    public static void setSortMode(int mode){
        SORT_MODE = mode;
    }

    @Override
    public String toString(){
        return mJson.toString();
    }


    @Override
    public int compareTo(Object other){
        if(other instanceof Card){
            int compareValue = 0;
            switch (SORT_MODE) {
                case SORT_MODE_SCORE:
                    if (this.getScore() == ((Card) other).getScore()) {
                        compareValue = this.id.compareTo(((Card) other).id);
                    } else {
                        compareValue = this.getScore() - ((Card) other).getScore();
                    }
                    break;
                case SORT_MODE_ALPHA:
                    compareValue = this.question.compareTo(((Card) other).getQuestion());
                    break;
            }
            return compareValue;
        }
        else {
            return 0;
        }
    }

    public long getTimeLastCorrect() {
        return timeLastCorrect;
    }

    public void setTimeLastCorrect(long timeLastCorrect) {
        this.timeLastCorrect = timeLastCorrect;
    }
}
