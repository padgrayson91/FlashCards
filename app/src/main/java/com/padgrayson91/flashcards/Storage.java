package com.padgrayson91.flashcards;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
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


    public Storage(Context context){
        mContext = context;
        mPrefs = mContext.getSharedPreferences(Storage.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    //Get methods
    protected Set<String> getDecks(){
        return mPrefs.getStringSet(PROPERTY_DECKS, new HashSet<String>());
    }


    //Store methods

    /***
     * In addition to adding the name to list of decks, this method will create the flashcard data
     * folder and initial file so that it can be accessed later
     * @param deckName The name of the deck to be stored
     * @return either Constants.SUCCESS, Constants.ERROR_DUPLICATE_NAME, or Constants.ERROR_WRITE_FAILED
     *
     */
    protected int storeDeck(String deckName){
        Log.d(TAG, "Storing deck " + deckName);
        //need to make a copy or sharedprefs won't hang on to changes...
        Set<String> temp = getDecks();
        Set<String> decks = new HashSet<>();
        for(String s: temp){
            decks.add(s);
        }
        if(decks.contains(deckName)){
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



}
