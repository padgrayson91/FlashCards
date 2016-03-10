package com.padgrayson91.flashcards;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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

        if(!writeDeckToFile(new Deck(deckName))){
            return ERROR_WRITE_FAILED;
        }
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
            if(!baseFile.createNewFile() || baseFile.isFile()){
                return false;
            }
            //TODO: ensure file is empty
            FileWriter fw = new FileWriter(baseFile);
            fw.write(d.getJson().toString());
            fw.flush();
            fw.close();



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
            JSONParser jparse = new JSONParser();
            JSONObject jobj = (JSONObject) jparse.parse(new FileReader(deckFile));
            return new Deck(jobj);
        } catch (FileNotFoundException e) {
            return null; //Should never get here
        } catch (ParseException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }



}
