package com.padgrayson91.flashcards;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.padgrayson91.flashcards.Constants.KEY_CARDS;
import static com.padgrayson91.flashcards.Constants.KEY_CARD_CONTENTS;
import static com.padgrayson91.flashcards.Constants.KEY_ID;
import static com.padgrayson91.flashcards.Constants.KEY_NAME;

/**
 * Created by patrickgrayson on 3/10/16.
 */
public class Deck implements Comparable {
    private static final String TAG = "FlashCards";

    private JSONObject mJson;
    private HashMap<String, Card> cards;
    private String name;

    public Deck(String name){
        mJson = new JSONObject();
        cards = new HashMap<String, Card>();
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

    protected JSONObject getJson(){
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

    @Override
    public int compareTo(Object other){
        if(other instanceof Deck){
            return this.getName().compareTo(((Deck) other).getName());
        }
        else return 0;

    }
}
