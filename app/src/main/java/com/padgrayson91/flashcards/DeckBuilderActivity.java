package com.padgrayson91.flashcards;

import android.R.drawable;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import static com.padgrayson91.flashcards.Constants.ERROR_EMPTY_ANSWER;
import static com.padgrayson91.flashcards.Constants.ERROR_EMPTY_QUESTION;
import static com.padgrayson91.flashcards.Constants.ERROR_NO_OPTIONS;
import static com.padgrayson91.flashcards.Constants.ERROR_WRITE_FAILED;
import static com.padgrayson91.flashcards.Constants.EXTRA_DECK_NAME;
import static com.padgrayson91.flashcards.Constants.SUCCESS;

/**
 * Created by patrickgrayson on 3/10/16.
 */
public class DeckBuilderActivity extends AppCompatActivity {

    private Deck mDeck;
    private Storage mStorage;
    private FloatingActionButton mFloatButton;

    private int currentMode;
    private static final String KEY_MODE = "mode";
    private static final String KEY_DECK_NAME = "deck_name";

    private static final int MODE_LIST = 0;
    private static final int MODE_CREATE = 1;
    private static final String TAG_BUILDER = "BUILDER";
    private static final String TAG_LIST = "LIST";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String deckName = "";
        if(savedInstanceState != null){
            currentMode = savedInstanceState.getInt(KEY_MODE);
            deckName = savedInstanceState.getString(KEY_DECK_NAME);
        } else {
            currentMode = MODE_LIST;
        }
        setContentView(R.layout.activity_deck_builder);
        Intent intent = getIntent();
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
        if(currentMode == MODE_CREATE) {
            swapFragment(MODE_CREATE);
        }
        else {
            swapFragment(MODE_LIST);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_MODE, currentMode);
    }

    private void updateButton(int resource){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mFloatButton.setImageDrawable(getResources().getDrawable(resource, getTheme()));
        } else {
            mFloatButton.setImageDrawable(getResources().getDrawable(resource));
        }
    }

    private void swapFragment(int mode){
        currentMode = mode;
        Fragment fragment = null;
        String TAG = "";
        switch (mode){
            case MODE_CREATE:
                fragment = new CardBuilderFragment();
                TAG = TAG_BUILDER;
                updateButton(drawable.ic_media_play);
                mFloatButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CardBuilderFragment builderFragment = (CardBuilderFragment) getSupportFragmentManager().findFragmentByTag(TAG_BUILDER);
                        int result = builderFragment.validateAndSave(mDeck);
                        switch (result){
                            case SUCCESS:
                                //switch back to list and update
                                swapFragment(MODE_LIST);
                                break;
                            case ERROR_NO_OPTIONS:
                                Toast.makeText(DeckBuilderActivity.this, "You need at least one other answer choice!", Toast.LENGTH_LONG).show();
                                break;
                            case ERROR_WRITE_FAILED:
                                Toast.makeText(DeckBuilderActivity.this, "Oops, something went wrong!", Toast.LENGTH_LONG).show();
                                break;
                            case ERROR_EMPTY_ANSWER:
                                Toast.makeText(DeckBuilderActivity.this, "You need to give an answer!", Toast.LENGTH_LONG).show();
                                break;
                            case ERROR_EMPTY_QUESTION:
                                Toast.makeText(DeckBuilderActivity.this, "You need to give a question!", Toast.LENGTH_LONG).show();
                                break;
                        }

                    }
                });
                break;
            case MODE_LIST:
                fragment = new CardListFragment();
                ((CardListFragment) fragment).setDeck(mDeck);
                TAG = TAG_LIST;
                updateButton(drawable.ic_input_add);
                mFloatButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        swapFragment(MODE_CREATE);
                    }
                });
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_placeholder, fragment, TAG)
                .commit();

    }


}
