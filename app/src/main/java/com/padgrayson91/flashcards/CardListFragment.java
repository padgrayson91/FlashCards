package com.padgrayson91.flashcards;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by patrickgrayson on 3/10/16.
 */
public class CardListFragment extends Fragment {
    private ArrayList<Card> mCards;
    private Deck mDeck;
    private ListView mCardList;
    private CardListAdapter mCardAdapter;

    public CardListFragment(){
        mCards = new ArrayList<Card>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_card_list, container, false);
        mCardList = (ListView) root.findViewById(R.id.card_list);
        mCardAdapter = new CardListAdapter();
        mCardList.setAdapter(mCardAdapter);
        return root;
    }

    public void setDeck(Deck d){
        mDeck = d;
        mCards = new ArrayList<Card>();
        HashMap<String, Card> temp = d.getCards();
        for(String s: temp.keySet()){
            mCards.add(temp.get(s));
        }
        Collections.sort(mCards);
        if(mCardAdapter != null){
            mCardAdapter.notifyDataSetChanged();
        }
    }

    //TODO: Would be really cool if the layout color changed based on ratio correct:incorrect
    class CardListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mCards.size();
        }

        @Override
        public Object getItem(int position) {
            return mCards.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout listItem = (LinearLayout) inflater.inflate(R.layout.list_item_card, parent, false);
            TextView cardQuestionView = (TextView) listItem.findViewById(R.id.view_card_question);
            TextView cardScoreView = (TextView) listItem.findViewById(R.id.view_card_score);
            cardQuestionView.setText(mCards.get(position).getQuestion());

            int score = mCards.get(position).getScore();
            String scoreText = String.format(getResources().getString(R.string.score_text), score);
            cardScoreView.setText(scoreText);

            return listItem;

        }
    }
}
