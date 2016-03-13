package com.padgrayson91.flashcards;

import android.graphics.Color;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

import static com.padgrayson91.flashcards.Constants.ERROR_NO_ID;
import static com.padgrayson91.flashcards.Constants.KEY_CARDS;
import static com.padgrayson91.flashcards.Constants.KEY_CARD_CONTENTS;
import static com.padgrayson91.flashcards.Constants.KEY_ID;
import static com.padgrayson91.flashcards.Constants.KEY_NAME;
import static com.padgrayson91.flashcards.Constants.SUCCESS;

/**
 * Created by patrickgrayson on 3/10/16.
 */
public class Deck implements Comparable {
    private static final String TAG = "FlashCards";

    private JSONObject mJson;
    private HashMap<String, Card> cards;
    private String name;
    private Iterator<String> mCardIterator;

    public static final int SORT_MODE_SCORE = 0;
    public static final int SORT_MODE_ALPHA = 1;
    private static int SORT_MODE;

    public Deck(String name){
        mJson = new JSONObject();
        cards = new HashMap<String, Card>();
        mCardIterator = cards.keySet().iterator();
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
            cards = new HashMap<String, Card>();
            if (mJson.has(KEY_CARDS)){
                JSONArray cardsArray = mJson.getJSONArray(KEY_CARDS);
                for(int i = 0; i < cardsArray.length(); i++){
                    JSONObject jobj = cardsArray.getJSONObject(i);
                    String id = jobj.getString(KEY_ID);
                    Card card = new Card(jobj.getJSONObject(KEY_CARD_CONTENTS), id);
                    cards.put(id, card);
                }
            }

        } catch (JSONException ex) {
            //Should never get here
        }
        mCardIterator = cards.keySet().iterator();

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
        for(String s: cards.keySet()){
            totalScore += cards.get(s).getScore();


        }
        totalScore = totalScore/(Math.max(1, cards.keySet().size()));
        return totalScore;
    }

    public int getColor(){
        int totalScore = getScore();
        if(totalScore > 0){
            return Color.argb(255, 255 - 10 * totalScore, 255, 255 - 10 * totalScore);
        } else {
            return Color.argb(255, 255, 255 + 10*totalScore, 255 + 10*totalScore);
        }
    }

    public HashMap<String, Card> getCards(){
        return cards;
    }

    public int addCard(Card c){
        if(c.id == null || c.id.equals("")){
            return ERROR_NO_ID;
        } else {
            //TODO: should check if card already exists with that ID
            cards.put(c.id, c);

            return SUCCESS;
        }
    }

    public Card getCard(String id){
        return cards.get(id);
    }

    public int removeCard(Card c){
        if(c.id == null || c.id.equals("")){
            return ERROR_NO_ID;
        } else {
            cards.remove(c.id);
            return SUCCESS;
        }
    }

    public int updateCard(Card c){
        if(c.id == null || !cards.keySet().contains(c.id)){
            return ERROR_NO_ID;
        } else {
            cards.put(c.id, c);
            return SUCCESS;
        }
    }

    public Card iterateToCard(String id){
        mCardIterator = cards.keySet().iterator();
        Card requested = null;
        while(mCardIterator.hasNext()){
            String nextId = mCardIterator.next();
            if(nextId.equals(id)){
                requested = cards.get(nextId);
                break;
            }
        }
        return requested;
    }

    public Card getNextCard(){
        //TODO: More interesting method of getting cards
        if(mCardIterator.hasNext()) {
            return cards.get(mCardIterator.next());
        }
        else {
            return null;
        }
    }

    public void reset(){
        mCardIterator = cards.keySet().iterator();
    }

    protected JSONObject getJson(){
        JSONArray cards = new JSONArray();
        try {
            for (String s : this.cards.keySet()) {
                JSONObject outerCard = new JSONObject();
                outerCard.put(KEY_ID, s);
                JSONObject cardContents = this.cards.get(s).toJSON();
                outerCard.put(KEY_CARD_CONTENTS, cardContents);
                cards.put(outerCard);
            }
            mJson.put(KEY_NAME, name);
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
}
