package com.padgrayson91.flashcards;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by patrickgrayson on 3/11/16.
 */
public class PlayerFragment extends Fragment {
    private static final String TAG = "FlashCards";

    private static final String KEY_CARD_ID = "card_id";
    private static final String KEY_DECK_NAME = "deck_name";
    private static long CARD_DELAY = 2000;

    private Deck mDeck;
    private Card mCurrentCard;
    private int mCurrentAnswerIndex;
    private TextView mQuestionText;
    private ArrayList<Button> mAnswerButtons;

    private Storage mStorage;

    public PlayerFragment(){
        mAnswerButtons = new ArrayList<Button>();
        Log.d(TAG, "Player fragment initialized!");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_player, container, false);
        mQuestionText = (TextView) root.findViewById(R.id.question_display_text);
        mAnswerButtons.add((Button) root.findViewById(R.id.answer_button_1));
        mAnswerButtons.add((Button) root.findViewById(R.id.answer_button_2));
        mAnswerButtons.add((Button) root.findViewById(R.id.answer_button_3));
        mAnswerButtons.add((Button) root.findViewById(R.id.answer_button_4));
        mStorage = new Storage(getActivity());

        for(Button b: mAnswerButtons){
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(((Button) v).getText().equals(mCurrentCard.getAnswer())){
                        //Question Right, Yay!
                        ((Button) v).setTextColor(Color.GREEN);
                        mCurrentCard.incrementCorrectCount();
                        Toast.makeText(getActivity(), "Correct!", Toast.LENGTH_SHORT).show();
                    } else {
                        mCurrentCard.incrementIncorrectCount();
                        ((Button) v).setTextColor(Color.RED);
                        mAnswerButtons.get(mCurrentAnswerIndex).setTextColor(Color.GREEN);
                        Toast.makeText(getActivity(), "Sorry, that's not correct", Toast.LENGTH_SHORT).show();
                        //Question Wrong, Aww
                    }
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            boolean hasNext = loadNextCard();
                            if(!hasNext){
                                playFinished();
                            }
                        }
                    }, CARD_DELAY);
                }
            });
        }
        Log.d(TAG, "Player fragment: view created!");
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "Activity Created!");
        mStorage = new Storage(getActivity());
        if(savedInstanceState != null){
            Log.d(TAG, "Had saved state, getting info from that");
            String deckName = savedInstanceState.getString(KEY_DECK_NAME, "");
            String cardId = savedInstanceState.getString(KEY_CARD_ID, "");
            setDeck(mStorage.readDeckFromFile(deckName));
            if(mDeck != null){
                Card current = mDeck.iterateToCard(cardId);
                if(current != null){
                    loadCard(current);
                }
            }
        } else {
            Log.d(TAG, "No saved state, pulling from storage");
            String deckName = mStorage.getInProgressDeck();
            if(!"".equals(deckName)) {
                Log.d(TAG, "Found a deck saved in storage " + deckName);
                //We were in progress, so pick up where we left
                //off
                setDeck(mStorage.readDeckFromFile(deckName));
                String cardId = mStorage.getInProgressCardId();
                mCurrentCard = mDeck.iterateToCard(cardId);
                Log.d(TAG, "Set card to " + mCurrentCard.toString());
            }
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Resuming");
        if(mCurrentCard != null){
            Log.d(TAG, "Had a card so we're loading it");
            loadCard(mCurrentCard);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "Saving state");
        if(mCurrentCard != null){
            outState.putString(KEY_CARD_ID, mCurrentCard.id);
        }
        if(mDeck != null){
            outState.putString(KEY_DECK_NAME, mDeck.getName());
        }
        //Save the deck to file so we don't lose card scores
        Storage storage = new Storage(getActivity());
        storage.writeDeckToFile(mDeck);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "Pausing, writing state to storage");
        if(mCurrentCard != null && mDeck != null) {
            Log.d(TAG, "Card: " + mCurrentCard.toString());
            Log.d(TAG, "Deck: " + mDeck.getName());
            mStorage.storeInProgressCardId(mCurrentCard.id);
            mStorage.storeInProgressDeckName(mDeck.getName());

            //Save the deck to file so we don't lose card scores
            Storage storage = new Storage(getActivity());
            storage.writeDeckToFile(mDeck);
        }
        super.onPause();
    }

    public void playFinished(){
        //Update the deck so scores persist
        Storage storage = new Storage(getActivity());
        mDeck.setLastPlayed(System.currentTimeMillis());
        storage.writeDeckToFile(mDeck);
        Log.d(TAG, "Play finished, nulling deck and card");
        setDeck(null);
        loadCard(null);
        mStorage.clearInProgressPlay();

        ((MainActivity) getActivity()).onPlayFinished();
    }

    public void setDeck(Deck d){
        mDeck = d;
    }

    /***
     * Checks to see if another card is available, and loads it if so
     * @return ture if card was loaded, false otherwise
     */
    public boolean loadNextCard(){
        if(mDeck != null){
            Card c = mDeck.getNextCard();
            if(c == null){
                return false;
            } else {
                loadCard(c);
                return true;
            }
        } else {
            return false;
        }
    }

    /***
     * Loads a card directly into the UI
     * @param c the card to load.  If null, UI will not be updated
     */
    public void loadCard(@Nullable Card c){
        //When we load  a new card, we need to reset all the button colors
        for(Button b: mAnswerButtons){
            b.setTextColor(Color.BLACK);
        }
        mCurrentCard = c;
        if(c == null){
            return;
        }
        String question = c.getQuestion();
        mQuestionText.setText(question);

        String answer = c.getAnswer();
        ArrayList<String> options = c.getOptions();
        try {
            Collections.shuffle(options);
        } catch (NullPointerException ex) {
            Log.d(TAG, "Options couldn't be shuffled");
        }
        Random random = new Random(SystemClock.currentThreadTimeMillis());
        int correctPos = random.nextInt(options.size() + 1);
        Log.d(TAG, "Adding correct answer at index " + correctPos);
        Log.d(TAG, "There are " + options.size() + "options");
        mCurrentAnswerIndex = correctPos;
        Button current;
        //i = index for buttons, j = index for options
        int i = 0;
        for(int  j = 0; j < options.size() ; i++, j++){
            Log.d(TAG, "Index: " + i);
            if(i == correctPos){
                Log.d(TAG, "Inserting correct answer and making button visible");
                current = mAnswerButtons.get(i);
                current.setText(answer);
                current.setVisibility(View.VISIBLE);
                j--; //need to cancel out the increment of j
            }
            else {
                current = mAnswerButtons.get(i);
                current.setText(options.get(j));
                current.setVisibility(View.VISIBLE);
            }
        }
        while(i < mAnswerButtons.size()){
            if(i == correctPos) {
                Log.d(TAG, "Inserting correct answer and making button visible");
                current = mAnswerButtons.get(i);
                current.setText(answer);
                current.setVisibility(View.VISIBLE);
            } else {
                mAnswerButtons.get(i).setVisibility(View.GONE);
            }
            i++;
        }
    }
}
