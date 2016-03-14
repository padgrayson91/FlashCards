package com.padgrayson91.flashcards;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by patrickgrayson on 3/11/16.
 */
public class CardViewerFragment extends Fragment {
    private static final String TAG = "FlashCards";

    private Card mCurrentCard;
    private Deck mDeck;
    private Storage mStorage;


    private TextView mQuestionText;
    private TextView mAnswerText;

    public CardViewerFragment(){
        Log.d(TAG, "Instantiating card viewer fragment");
        Bundle args = getArguments();
        if(args != null && getContext() != null){
            mStorage = new Storage(getContext());
            String deckName = args.getString(Constants.EXTRA_DECK_NAME, "");
            if(!"".equals(deckName)){
                setDeck(mStorage.readDeckFromFile(deckName));
                String cardId = args.getString(Constants.EXTRA_CARD_ID, "");
                if(!"".equals(cardId) && !(mDeck == null)){
                    Log.d(TAG, "Got card ID in fragment " + cardId);
                    setCard(mDeck.getCard(cardId));
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "Creating view for card " + mCurrentCard);
        View root = inflater.inflate(R.layout.fragment_card_viewer, container, false);
        mQuestionText = (TextView) root.findViewById(R.id.question_display_text);
        mAnswerText = (TextView) root.findViewById(R.id.answer_display_text);
        if(mCurrentCard != null){
            mQuestionText.setText(mCurrentCard.getQuestion());
            mAnswerText.setText(mCurrentCard.getAnswer());
        }

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void setCard(Card c){
        Log.d(TAG, "Setting card for fragment");
        mCurrentCard = c;
        if(mQuestionText != null){
            mQuestionText.setText(c.getQuestion());
        }
        if(mAnswerText != null){
            mAnswerText.setText(c.getAnswer());
        }
    }

    public void setDeck(Deck d){
        mDeck = d;
    }
}
