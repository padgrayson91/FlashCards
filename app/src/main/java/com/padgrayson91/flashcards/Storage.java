package com.padgrayson91.flashcards;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;

import java.io.File;
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
        Set<String> decks = getDecks();
        if(decks.contains(deckName)){
            return ERROR_DUPLICATE_NAME;
        } else {
            decks.add(deckName);
        }
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putStringSet(PROPERTY_DECKS, decks);
        editor.commit();

        ContextWrapper cw = new ContextWrapper(mContext);
        File directory = new File(cw.getFilesDir(), "Decks");
        directory.mkdirs();
        // Create imageDir
        File mypath=new File(directory, deckName);
        mypath.mkdirs();

        File baseFile = new File(mypath, deckName + ".json");
        try {
            baseFile.createNewFile();
        } catch (IOException e) {
            return ERROR_WRITE_FAILED;
        }
        return SUCCESS;
    }

}
