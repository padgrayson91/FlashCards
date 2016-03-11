package com.padgrayson91.flashcards;

import android.R.drawable;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

import static com.padgrayson91.flashcards.Constants.EXTRA_DECK_NAME;

/**
 * Created by patrickgrayson on 3/10/16.
 */
public class DeckBuilderActivity extends Activity {

    private Deck mDeck;
    private Storage mStorage;
    private FloatingActionButton mFloatButton;

    private int currentMode;
    private static final String MODE_KEY = "mode";

    private static final int MODE_LIST = 0;
    private static final int MODE_CREATE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            currentMode = savedInstanceState.getInt(MODE_KEY);
        }
        setContentView(R.layout.activity_deck_builder);
        Intent intent = getIntent();
        String deckName = "";
        if(intent != null){
            if(intent.hasExtra(EXTRA_DECK_NAME)){
                deckName = intent.getStringExtra(EXTRA_DECK_NAME);
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
        mStorage = new Storage(this);
        mDeck = mStorage.readDeckFromFile(deckName);
        if(mDeck == null){
            setResult(RESULT_CANCELED);
            finish();
        }

        mFloatButton = (FloatingActionButton) findViewById(R.id.fab);

    }

    @Override
    protected void onResume(){
        super.onResume();
        Fragment fragment;
        if(currentMode == MODE_CREATE) {
            fragment = new CardBuilderFragment();
            mFloatButton.setImageResource(drawable.ic_media_play);
            mFloatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: tell fragment to validate stuff and save card
                }
            });
        }
        else {
            fragment = new CardListFragment();
            mFloatButton.setImageResource(drawable.ic_input_add);
            mFloatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment builderFragment = new CardBuilderFragment();
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_placeholder, builderFragment)
                            .commit();
                }
            });
        }
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_placeholder, fragment)
                .commit();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt(MODE_KEY, currentMode);
    }


}
