package com.padgrayson91.flashcards;

import android.graphics.Color;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static com.padgrayson91.flashcards.Constants.ERROR_NO_ID;
import static com.padgrayson91.flashcards.Constants.KEY_CARDS;
import static com.padgrayson91.flashcards.Constants.KEY_CARD_CONTENTS;
import static com.padgrayson91.flashcards.Constants.KEY_CREATED_AT;
import static com.padgrayson91.flashcards.Constants.KEY_ID;
import static com.padgrayson91.flashcards.Constants.KEY_LAST_PLAYED;
import static com.padgrayson91.flashcards.Constants.KEY_NAME;
import static com.padgrayson91.flashcards.Constants.KEY_OWNER;
import static com.padgrayson91.flashcards.Constants.SUCCESS;

/**
 * Created by patrickgrayson on 3/10/16.
 */
public class Deck implements Comparable {
    private static final String TAG = "FlashCards";
    public static final int NEVER_PLAYED = 0;
    private static final double TIME_DECAY_CONSTANT = 10; //Number of hours after which one point is lost

    private JSONObject mJson;
    private ArrayList<Card> cards;
    private String name;
    private long lastPlayed; //Time last played in ms
    private long createdAt; //Time created in ms
    private String owner;
    private Iterator<Card> mCardIterator;

    public static final int SORT_MODE_SCORE = 0;
    public static final int SORT_MODE_ALPHA = 1;
    private static int SORT_MODE;

    private static final int ALPHA = 200;
    private static final int MIN_R = 50;
    private static final int MIN_G = 50;
    private static final int MIN_B = 80;
    private static final int BLUE_FACTOR = 7; //higher number = less blue
    private static final int RED_FACTOR = 10; // higher number = less red
    private static final int GREEN_FACTOR = 10; // higher number = less green
    public Deck(String name){
        mJson = new JSONObject();
        cards = new ArrayList<>();
        mCardIterator = cards.iterator();
        createdAt = System.currentTimeMillis();
        lastPlayed = NEVER_PLAYED;
        this.name = name;
        try {
            mJson.put(KEY_NAME, name);
        } catch (JSONException e) {
            //Should never get here
        }
    }

    public Deck(JSONObject deck){
        mJson = deck;
        //Check for keys to initialize deck
        try {
            if (mJson.has(KEY_NAME)) {
                name = mJson.getString(KEY_NAME);
            } else {
                //If we get here something weird is going on
                Log.d(TAG, "Deck had no name??");
            }
            cards = new ArrayList<>();
            if (mJson.has(KEY_CARDS)){
                JSONArray cardsArray = mJson.getJSONArray(KEY_CARDS);
                for(int i = 0; i < cardsArray.length(); i++){
                    JSONObject jobj = cardsArray.getJSONObject(i);
                    String id = jobj.getString(KEY_ID);
                    Card card = new Card(jobj.getJSONObject(KEY_CARD_CONTENTS), id);
                    cards.add(card);
                }
            }
            if(mJson.has(KEY_CREATED_AT)){
                createdAt = mJson.getLong(KEY_CREATED_AT);
            }
            if(mJson.has(KEY_LAST_PLAYED)){
                lastPlayed = mJson.getLong(KEY_LAST_PLAYED);
            } else {
                //if we don't know when it was last played
                lastPlayed = NEVER_PLAYED;
            }
            if(mJson.has(KEY_OWNER)){
                owner = mJson.getString(KEY_OWNER);
            }


        } catch (JSONException ex) {
            //Should never get here
        }
        mCardIterator = cards.iterator();

    }

    public void merge(Deck other){
        Card c;
        while((c= other.getNextCard()) != null){
            addCard(c);
        }
    }

    /***
     * Renames the deck. Note: this does not rename the folder used for the deck
     * A new folder should be created and the old one deleted
     * @param name
     */
    public void rename(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public int getSize(){
        if(cards != null) {
            return cards.size();
        } else {
            return 0;
        }
    }

    public int getScore(){
        int totalScore = 0;
        for(Card c: cards){
            totalScore += c.getScore();


        }
        totalScore = totalScore/(Math.max(1, cards.size()));
        if(lastPlayed != NEVER_PLAYED) {
            long timeGapMs = System.currentTimeMillis() - lastPlayed;
            long timeGapHours = TimeUnit.MILLISECONDS.toHours(timeGapMs);
            totalScore -= timeGapHours/TIME_DECAY_CONSTANT;
        }

        return totalScore;
    }

    public int getColor(){
        int totalScore = getScore();
        if(totalScore > 0){
            return Color.argb(ALPHA, Math.max(255 - RED_FACTOR*totalScore, MIN_R), 255, Math.max(255 - BLUE_FACTOR*totalScore, MIN_B));
        } else {
            return Color.argb(ALPHA, 255, Math.max(255 + GREEN_FACTOR*totalScore, MIN_G), Math.max(255 + BLUE_FACTOR*totalScore, MIN_B));
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

    public ArrayList<Card> getCards(){
        return cards;
    }

    public int addCard(Card c){
        if(c.id == null || c.id.equals("")){
            return ERROR_NO_ID;
        } else {
            //TODO: should check if card already exists with that ID
            cards.add(c);

            return SUCCESS;
        }
    }

    public int addCards(ArrayList<Card> toAdd){
        for(Card c: toAdd){
            int result = addCard(c);
            if(result != SUCCESS){
                return result;
            }
        }
        return SUCCESS;
    }

    public Card getCard(String id){
        int index = cards.indexOf(id);
        if(index >= 0) {
            return cards.get(index);
        } else {
            return null;
        }
    }

    public int removeCard(Card c){
        if(c.id == null || c.id.equals("")){
            return ERROR_NO_ID;
        } else {
            cards.remove(c);
            return SUCCESS;
        }
    }

    public int updateCard(Card c){
        if(c.id == null || !cards.contains(c)){
            return ERROR_NO_ID;
        } else {
            cards.set(cards.indexOf(c), c);
            return SUCCESS;
        }
    }

    public Card iterateToCard(String id){
        mCardIterator = cards.iterator();
        Card requested = null;
        while(mCardIterator.hasNext()){
            Card nextCard = mCardIterator.next();
            if(nextCard.id.equals(id)){
                requested = nextCard;
                break;
            }
        }
        return requested;
    }

    public Card getNextCard(){
        //TODO: More interesting method of getting cards
        if(mCardIterator.hasNext()) {
            return mCardIterator.next();
        }
        else {
            return null;
        }
    }

    public void reset(){
        mCardIterator = cards.iterator();
    }

    protected JSONObject getJson(){
        JSONArray cards = new JSONArray();
        try {
            for (Card c : this.cards) {
                JSONObject outerCard = new JSONObject();
                outerCard.put(KEY_ID, c.id);
                JSONObject cardContents = c.toJSON();
                outerCard.put(KEY_CARD_CONTENTS, cardContents);
                cards.put(outerCard);
            }
            mJson.put(KEY_NAME, name);
            mJson.put(KEY_OWNER, owner);
            mJson.put(KEY_LAST_PLAYED, lastPlayed);
            mJson.put(KEY_CREATED_AT, createdAt);
            mJson.put(KEY_CARDS, cards);
        } catch (JSONException ex){
            //Should never get here
        }
        return mJson;
    }

    @Override
    public boolean equals(Object toCompare){
        if(toCompare instanceof Deck){
            return ((Deck) toCompare).name.equals(this.name);
        } else if(toCompare instanceof String){
            return toCompare.equals(this.name);
        } else {
            return false;
        }
    }

    public static void setSortMode(int mode){
        SORT_MODE = mode;
    }

    @Override
    public int compareTo(Object other){
        if(other instanceof Deck){
            int returnValue = 0;
            switch (SORT_MODE) {
                case SORT_MODE_ALPHA:
                    returnValue = this.getName().compareTo(((Deck) other).getName());
                    break;
                case SORT_MODE_SCORE:
                    returnValue = this.getScore() - ((Deck) other).getScore();
                    break;
            }
            return returnValue;
        }
        else return 0;

    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getLastPlayed() {
        return lastPlayed;
    }

    public void setLastPlayed(long lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
