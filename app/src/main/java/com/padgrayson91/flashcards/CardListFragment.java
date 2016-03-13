package com.padgrayson91.flashcards;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by patrickgrayson on 3/10/16.
 */
public class CardListFragment extends Fragment {
    private static final String TAG = "FlashCards";

    private ArrayList<Card> mCards;
    private Deck mDeck;
    private ListView mCardList;
    private CardListAdapter mCardAdapter;
    private TextView mEmptyText;
    private Storage mStorage;

    public CardListFragment(){
        mCards = new ArrayList<Card>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_card_list, container, false);
        mEmptyText = (TextView) root.findViewById(R.id.view_empty_text);
        mCardList = (ListView) root.findViewById(R.id.card_list);
        Bundle args = getArguments();
        Log.d(TAG, "Arguments: " + args + " Context: " + getContext());
        if(args != null && getContext() != null){
            mStorage = new Storage(getContext());
            String deckName = args.getString(Constants.EXTRA_DECK_NAME, "");
            if(!deckName.equals("")){
                Log.d(TAG, "Got Deck name!");
                setDeck(mStorage.readDeckFromFile(deckName));
            }
        }
        if(mCards.size() > 0){
            mCardList.setVisibility(View.VISIBLE);
            mEmptyText.setVisibility(View.GONE);
        } else{
            mEmptyText.setVisibility(View.VISIBLE);
            mCardList.setVisibility(View.GONE);
        }
        mCardAdapter = new CardListAdapter();
        mCardList.setAdapter(mCardAdapter);
        mCardList.setOnItemClickListener(mItemClickListener);
        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_card_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_delete){
            deleteSelectedCards();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void deleteSelectedCards(){
        for(int i = 0; i < mCards.size(); i++){
            boolean selected = ((CheckBox) mCardList.getChildAt(i).findViewById(R.id.selection_check)).isChecked();
            if(selected){
                mDeck.removeCard(mCards.get(i));
            }
        }
        Toast.makeText(getActivity(), "Cards deleted", Toast.LENGTH_SHORT).show();
        setDeck(mDeck);
    }

    public void setDeck(Deck d){
        mDeck = d;
        mCards = new ArrayList<Card>();
        HashMap<String, Card> temp = d.getCards();
        for(String s: temp.keySet()){
            mCards.add(temp.get(s));
        }
        sortCards();
        if(mCardAdapter != null){
            mCardAdapter.notifyDataSetChanged();
        }
    }

    private void sortCards(){
        //Always set sort mode before performing a sort
        try {
            Card.setSortMode(mStorage.getCardSortMode());
        } catch (NullPointerException ex){}
        Collections.sort(mCards);
    }

    AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Card c = mCards.get(position);
            Bundle extras = new Bundle();
            extras.putString(Constants.EXTRA_CARD_ID, c.id);
            extras.putString(Constants.EXTRA_DECK_NAME, mDeck.getName());
            Intent i = new Intent(getActivity(), CardViewerActivity.class);
            i.putExtras(extras);
            startActivity(i);
        }
    };

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
            listItem.setBackgroundColor(mCards.get(position).getColor());

            return listItem;

        }
    }
}
