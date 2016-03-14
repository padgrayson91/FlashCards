package com.padgrayson91.flashcards;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by patrickgrayson on 3/10/16.
 */
public class CardListFragment extends GenericListFragment {
    private static final String TAG = "FlashCards";

    private ArrayList<Card> mCards;
    private ArrayList<Card> mSelectedCards;
    private Deck mDeck;
    private ListView mCardList;
    private CardListAdapter mCardAdapter;
    private TextView mEmptyText;
    private Storage mStorage;

    private Menu mMenu;

    public CardListFragment(){
        mCards = new ArrayList<Card>();
        mSelectedCards = new ArrayList<>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_card_list, container, false);
        mEmptyText = (TextView) root.findViewById(R.id.view_empty_text);
        mCardList = (ListView) root.findViewById(R.id.card_list);
        Bundle args = getArguments();
        Log.d(TAG, "Arguments: " + args + " Context: " + getActivity());
        if(args != null && getActivity() != null){
            mStorage = new Storage(getActivity());
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
    public void onActivityCreated(Bundle savedInstanceState) {
        mStorage = new Storage(getActivity());
        mStorage.clearInProgressPlay();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mMenu = menu;
        inflater.inflate(R.menu.menu_fragment_card_list, menu);
    }

    @Override
    public void onDestroyOptionsMenu() {
        mMenu = null;
        super.onDestroyOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_delete){
            promptDeleteCards();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void deleteSelectedCards(){
        for(Card c: mSelectedCards){
            mDeck.removeCard(c);
        }
        Toast.makeText(getActivity(), "Cards deleted", Toast.LENGTH_SHORT).show();
        mSelectedCards = new ArrayList<>();
        mStorage.writeDeckToFile(mDeck);
        setDeck(mDeck);
    }

    public void promptDeleteCards(){
        Context context = getActivity();
        if(context == null){
            return;
        }
        String promptText = getResources().getQuantityString(R.plurals.alert_delete_card, mSelectedCards.size(), mSelectedCards.size());
        new AlertDialog.Builder(context).setCancelable(true)
                .setMessage(promptText)
                .setPositiveButton(getResources().getString(R.string.alert_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteSelectedCards();
                    }
                }).setNegativeButton(getResources().getString(R.string.alert_name_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    public void setDeck(Deck d){
        mDeck = d;
        mCards = new ArrayList<Card>();
        mCards = d.getCards();
        sortCards();
        if(mCardAdapter != null){
            mCardAdapter.notifyDataSetChanged();
        }
    }

    protected void showChecks(boolean show){
        //TODO: need to update layout so checks can be hidden
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout listItem = (LinearLayout) inflater.inflate(R.layout.list_item_card, parent, false);
            TextView cardQuestionView = (TextView) listItem.findViewById(R.id.view_card_question);
            TextView cardScoreView = (TextView) listItem.findViewById(R.id.view_card_score);
            cardQuestionView.setText(mCards.get(position).getQuestion());
            int score = mCards.get(position).getScore();
            String scoreText = String.format(getResources().getString(R.string.score_text), score);
            cardScoreView.setText(scoreText);
            listItem.setBackgroundColor(mCards.get(position).getColor());
            CheckBox selectionCheck = (CheckBox) listItem.findViewById(R.id.selection_check);
            selectionCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Card c = mCards.get(position);
                    int oldSize = mSelectedCards.size();
                    if(isChecked){
                        if(!mSelectedCards.contains(c)){
                            mSelectedCards.add(c);
                        }
                    } else {
                        if(mSelectedCards.contains(c)){
                            mSelectedCards.remove(c);
                        }
                    }
                    int newSize = mSelectedCards.size();
                    if(oldSize == 0 && newSize >= 1){
                        mMenu.setGroupVisible(R.id.group_actions_one_or_more, true);
                    }
                    if(oldSize >= 1 && newSize == 0){
                        mMenu.setGroupVisible(R.id.group_actions_one_or_more, false);
                        showChecks(false);
                    }
                }
            });

            return listItem;

        }
    }
}
