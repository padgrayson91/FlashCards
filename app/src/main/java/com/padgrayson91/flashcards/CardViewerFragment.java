package com.padgrayson91.flashcards;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by patrickgrayson on 3/11/16.
 */
public class CardViewerFragment extends Fragment {
    private Card mCurrentCard;
    private Deck mDeck;


    private TextView mQuestionText;
    private TextView mAnswerText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_card_viewer, container, false);
        mQuestionText = (TextView) root.findViewById(R.id.question_display_text);
        mAnswerText = (TextView) root.findViewById(R.id.answer_display_text);

        return root;
    }

    public void setCard(Card c){
        mCurrentCard = c;
        if(mQuestionText != null){
            mQuestionText.setText(c.getQuestion());
        }
        if(mAnswerText != null){
            mAnswerText.setText(c.getAnswer());
        }
    }

    public void setmDeck(Deck d){
        mDeck = d;
    }
}
