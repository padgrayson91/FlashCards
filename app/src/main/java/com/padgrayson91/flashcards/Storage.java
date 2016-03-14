package com.padgrayson91.flashcards;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.padgrayson91.flashcards.Constants.ERROR_DUPLICATE_NAME;
import static com.padgrayson91.flashcards.Constants.ERROR_WRITE_FAILED;
import static com.padgrayson91.flashcards.Constants.SUCCESS;

/**
 * Created by patrickgrayson on 3/10/16.
 */
public class Storage {
    private static final String TAG = "FlashCards";

    private Context mContext;
    private SharedPreferences mPrefs;

    //Date to be stored
    private static final String PROPERTY_DECKS = "decks_info";
    private static final String PROPERTY_CARD_SORT = "pref_sort_cards";
    private static final String PROPERTY_DECK_SORT = "pref_sort_decks";
    private static final String PROPERTY_IN_PROGRESS_QUESTION = "in_progress_question";
    private static final String PROPERTY_IN_PROGRESS_ANSWER = "in_progress_answer";
    private static final String PROPERTY_IN_PROGRESS_OPTION_1 ="in_progress_option_1";
    private static final String PROPERTY_IN_PROGRESS_OPTION_2 ="in_progress_option_2";
    private static final String PROPERTY_IN_PROGRESS_OPTION_3 ="in_progress_option_3";
    private static final String PROPERTY_IN_PROGRESS_DECK_NAME = "in_progress_deck";
    private static final String PROPERTY_IN_PROGRESS_CARD_ID = "in_progress_card";


    public Storage(Context context){
        mContext = context;
        mPrefs = mContext.getSharedPreferences(Storage.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    //Get methods
    protected Set<String> getDecks(){
        return mPrefs.getStringSet(PROPERTY_DECKS, new HashSet<String>());
    }

    protected int getCardSortMode(){
        try {
            Log.d(TAG, "Card sort mode " + PreferenceManager.getDefaultSharedPreferences(mContext).getString(PROPERTY_CARD_SORT, "BLAH"));
            return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(mContext).getString(PROPERTY_CARD_SORT, "0"));
        } catch (NumberFormatException ex) {
            Log.d(TAG, "Card sort mode wasn't number " + PreferenceManager.getDefaultSharedPreferences(mContext).getString(PROPERTY_CARD_SORT, "0"));
            return 0;
        }
    }

    protected int getDeckSortMode(){
        try {
            return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(mContext).getString(PROPERTY_DECK_SORT, "1"));
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    protected String getInProgressQuestion(){
        return mPrefs.getString(PROPERTY_IN_PROGRESS_QUESTION, "");
    }

    protected String getInProgressAnswer(){
        return mPrefs.getString(PROPERTY_IN_PROGRESS_ANSWER, "");
    }

    protected ArrayList<String> getInPorgressOptions(){
        ArrayList<String> options = new ArrayList<>();
        options.add(mPrefs.getString(PROPERTY_IN_PROGRESS_OPTION_1, ""));
        options.add(mPrefs.getString(PROPERTY_IN_PROGRESS_OPTION_2, ""));
        options.add(mPrefs.getString(PROPERTY_IN_PROGRESS_OPTION_3, ""));
        return options;
    }

    protected String getInProgressDeck(){
        return mPrefs.getString(PROPERTY_IN_PROGRESS_DECK_NAME, "");
    }

    protected String getInProgressCardId(){
        return mPrefs.getString(PROPERTY_IN_PROGRESS_CARD_ID, "");
    }


    //Store and remove methods

    //Convenience method, will never overwrite
    protected int storeDeck(String deckName){
        return storeDeck(deckName, false);
    }

    /***
     * In addition to adding the name to list of decks, this method will create the flashcard data
     * folder and initial file so that it can be accessed later
     * @param deckName The name of the deck to be stored
     * @param overWrite If true, a previous deck with this name can be overwritten
     * @return either Constants.SUCCESS, Constants.ERROR_DUPLICATE_NAME, or Constants.ERROR_WRITE_FAILED
     *
     */
    protected int storeDeck(String deckName, boolean overWrite){
        Log.d(TAG, "Storing deck " + deckName);
        //need to make a copy or sharedprefs won't hang on to changes...
        Set<String> temp = getDecks();
        Set<String> decks = new HashSet<>();
        for(String s: temp){
            decks.add(s);
        }
        if(decks.contains(deckName) && !overWrite){
            return ERROR_DUPLICATE_NAME;
        } else {
            Log.d(TAG, "Added to set");
            decks.add(deckName);
            Log.d(TAG, "Decks: " + decks.toString());
        }

        if(!writeDeckToFile(new Deck(deckName))){
            return ERROR_WRITE_FAILED;
        }

        //Only commit if we know the write worked
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putStringSet(PROPERTY_DECKS, decks);
        editor.commit();
        Log.d(TAG, "committed");
        return SUCCESS;
    }

    protected void storeInProgressQuestion(String question){
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PROPERTY_IN_PROGRESS_QUESTION, question);
        editor.commit();
    }

    protected  void storeInProgressAnswer(String answer){
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PROPERTY_IN_PROGRESS_ANSWER, answer);
        editor.commit();
    }

    protected  void storeInProgressCardId(String id){
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PROPERTY_IN_PROGRESS_CARD_ID, id);
        editor.commit();
    }

    protected void storeInProgressDeckName(String name){
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PROPERTY_IN_PROGRESS_DECK_NAME, name);
        editor.commit();
    }

    /***
     * Expects an ArrayList with exactly 3 options
     * @param options
     */
    protected void storeInProgressOptions(ArrayList<String> options){
        SharedPreferences.Editor editor = mPrefs.edit();
        try {
            editor.putString(PROPERTY_IN_PROGRESS_OPTION_1, options.get(0));
            editor.putString(PROPERTY_IN_PROGRESS_OPTION_2, options.get(1));
            editor.putString(PROPERTY_IN_PROGRESS_OPTION_3, options.get(2));
        } catch (IndexOutOfBoundsException ex){
            //Only need to add the options we actually set
        }
        editor.commit();
    }

    protected void clearInProgressCard(){
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.remove(PROPERTY_IN_PROGRESS_QUESTION);
        editor.remove(PROPERTY_IN_PROGRESS_ANSWER);
        editor.remove(PROPERTY_IN_PROGRESS_OPTION_1);
        editor.remove(PROPERTY_IN_PROGRESS_OPTION_2);
        editor.remove(PROPERTY_IN_PROGRESS_OPTION_3);
        editor.commit();
    }

    protected void clearInProgressPlay(){
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.remove(PROPERTY_IN_PROGRESS_CARD_ID);
        editor.remove(PROPERTY_IN_PROGRESS_DECK_NAME);
        editor.commit();
    }

    protected void removeDeck(String deckName){
        //need to make a copy or sharedprefs won't hang on to changes...
        Set<String> temp = getDecks();
        Set<String> decks = new HashSet<>();
        for(String s: temp){
            decks.add(s);
        }
        decks.remove(deckName);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putStringSet(PROPERTY_DECKS, decks);
        editor.commit();
        deleteDeck(deckName);
    }

    //File I/O methods

    protected boolean writeDeckToFile(Deck d){
        ContextWrapper cw = new ContextWrapper(mContext);
        File directory = new File(cw.getFilesDir(), "Decks");
        directory.mkdirs();
        // Create directory for deck
        File mypath=new File(directory, d.getName());
        mypath.mkdirs();

        File baseFile = new File(mypath, d.getName() + ".json");
        try {
            if(!(baseFile.createNewFile() || baseFile.isFile())){
                return false;
            }
            FileWriter fw = new FileWriter(baseFile);
            BufferedWriter bw = new BufferedWriter(fw);
            Log.d(TAG, "Writing deck to file: " + d.getJson().toString());
            bw.write(d.getJson().toString());
            bw.flush();
            bw.close();



        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /***
     *
     * @param deckName The name of the deck to be accessed
     * @return a Deck object with the contents of the deck, or null if it doesn't exist or if there
     * is an error
     */
    protected Deck readDeckFromFile(String deckName){
        ContextWrapper cw = new ContextWrapper(mContext);
        File directory = new File(cw.getFilesDir(), "Decks");
        if(!directory.isDirectory()){
            return null;
        }
        File mypath=new File(directory, deckName);
        if(!mypath.isDirectory()){
            return null;
        }
        File deckFile = new File(mypath, deckName + ".json");
        if(!deckFile.isFile()){
            return null;
        }

        try {
            JSONObject jobj;
            FileReader fr = new FileReader(deckFile);
            BufferedReader br = new BufferedReader(fr);
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null){
                sb.append(line);
            }
            br.close();
            jobj = new JSONObject(sb.toString());
            return new Deck(jobj);
        } catch (FileNotFoundException e) {
            return null; //Should never get here
        } catch (IOException e) {
            return null;
        } catch (JSONException e) {
            return null;
        }
    }

    //Private because we should never be deleting a deck without removing it from sharedprefs
    private void deleteDeck(String deckName){
        ContextWrapper cw = new ContextWrapper(mContext);
        File dir = new File(cw.getFilesDir() + "Decks/" + deckName);
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
        }
    }



}
