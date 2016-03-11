package com.padgrayson91.flashcards;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
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


    private Deck mDeck;
    private Card mCurrentCard;
    private int mCurrentAnswerIndex;
    private TextView mQuestionText;
    private ArrayList<Button> mAnswerButtons;

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
                    }, 5000);
                }
            });
        }
        Log.d(TAG, "Player fragment: view created!");
        return root;
    }

    public void playFinished(){
        //Update the deck so scores persist
        Storage storage = new Storage(getActivity());
        storage.writeDeckToFile(mDeck);

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
     * @param c
     */
    public void loadCard(Card c){
        //When we load  a new card, we need to reset all the button colors
        for(Button b: mAnswerButtons){
            b.setTextColor(Color.BLACK);
        }
        mCurrentCard = c;
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
            mAnswerButtons.get(i).setVisibility(View.GONE);
            i++;
        }
    }
}
