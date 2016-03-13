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
import java.util.Set;

import static com.padgrayson91.flashcards.Constants.ACTION_BUILD_DECK;
import static com.padgrayson91.flashcards.Constants.EXTRA_DECK_NAME;
import static com.padgrayson91.flashcards.Constants.REQUEST_CODE_BUILD_DECK;

/**
 * A fragment containing the list of available decks
 */

//TODO: Search decks
public class MainActivityFragment extends Fragment {
    private static final String TAG = "FlashCards";

    private ArrayList<Deck> mDecks;
    private TextView mEmptyText;
    private ListView mDeckList;
    private Storage mStorage;
    private DeckListAdapter mDeckAdapter;

    public MainActivityFragment() {
        mDecks = new ArrayList<Deck>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_main, container, false);

        mEmptyText = (TextView) root.findViewById(R.id.view_empty_text);
        mDeckList = (ListView) root.findViewById(R.id.view_decks_list);

        mStorage = new Storage(getContext());
        getDecksFromStorage();
        mDeckAdapter = new DeckListAdapter();
        mDeckList.setAdapter(mDeckAdapter);
        mDeckList.setOnItemClickListener(mItemClickListener);

        if(mDecks.size() != 0){
            mEmptyText.setVisibility(View.GONE);
        } else {
            mDeckList.setVisibility(View.GONE);
        }
        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "Fragment Resumed!");
        updateDecks();
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_fragment_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_delete){
            deleteSelectedDecks();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void deleteSelectedDecks(){
        Log.d(TAG, "Deleting decks");
        for(int i = 0; i < mDecks.size(); i++){
            boolean selected = ((CheckBox) mDeckList.getChildAt(i).findViewById(R.id.selection_check)).isChecked();
            if(selected){
                mStorage.removeDeck(mDecks.get(i).getName());
            }
        }
        Toast.makeText(getActivity(), "Decks deleted", Toast.LENGTH_SHORT).show();
        updateDecks();
    }

    public void updateDecks(){
        mDecks = new ArrayList<Deck>();
        getDecksFromStorage();
        if(mDecks.size() != 0){
            mEmptyText.setVisibility(View.GONE);
            mDeckList.setVisibility(View.VISIBLE);
        } else {
            mDeckList.setVisibility(View.GONE);
            mEmptyText.setVisibility(View.VISIBLE);
        }
        mDeckAdapter.notifyDataSetChanged();
    }

    private void getDecksFromStorage(){
        Set<String> temp = mStorage.getDecks();
        Log.d(TAG, "Decks " + temp.toString());
        for(String s: temp){
            Log.d(TAG, "Looking for deck " + s);
            if(!mDecks.contains(s)){
                Deck d = mStorage.readDeckFromFile(s);
                Log.d(TAG, "Deck generated! Size: " + d.getSize());
                if(d == null){
                    Toast.makeText(getContext(), "Oops, something went wrong!", Toast.LENGTH_LONG).show();
                    break;
                } else {
                    mDecks.add(d);
                }
            }
        }
        sortDecks();
    }

    //Need to makes sure sort mode is correct before sorting
    private void sortDecks(){
        try{
            Deck.setSortMode(mStorage.getDeckSortMode());
        } catch (NullPointerException ex) {}
        Collections.sort(mDecks);
    }

    private void startBuilder(Deck d){
        Intent deckBuilderIntent = new Intent(getContext(), DeckBuilderActivity.class);
        deckBuilderIntent.setAction(ACTION_BUILD_DECK);
        Bundle extras = new Bundle();
        extras.putString(EXTRA_DECK_NAME, d.getName());
        deckBuilderIntent.putExtras(extras);
        startActivityForResult(deckBuilderIntent, REQUEST_CODE_BUILD_DECK);
    }

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Deck d = mDecks.get(position);
            if(d.getSize() > 0){
                ((MainActivity) getActivity()).onDeckSelected(d);
            } else {
                startBuilder(d);
            }
        }
    };

    class DeckListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mDecks.size();
        }

        @Override
        public Object getItem(int position) {
            return mDecks.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Log.d(TAG, "Getting view for " + mDecks.get(position).getName());
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout listItem = (LinearLayout) inflater.inflate(R.layout.list_item_deck, parent, false);
            TextView deckNameView = (TextView) listItem.findViewById(R.id.view_deck_name);
            TextView deckSizeView = (TextView) listItem.findViewById(R.id.view_deck_size);
            LinearLayout viewCardsButton = (LinearLayout) listItem.findViewById(R.id.btn_view_cards);
            viewCardsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startBuilder(mDecks.get(position));
                }
            });
            LinearLayout playButton = (LinearLayout) listItem.findViewById(R.id.btn_play);
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Deck d = mDecks.get(position);
                    if(d.getSize() > 0){
                        ((MainActivity) getActivity()).onDeckSelected(d);
                    } else {
                        Toast.makeText(getContext(), "You need to add cards first!", Toast.LENGTH_SHORT).show();
                        startBuilder(d);
                    }
                }
            });
            deckNameView.setText(mDecks.get(position).getName());
            deckSizeView.setText(getResources().getQuantityString(R.plurals.card_count, mDecks.get(position).getSize(), mDecks.get(position).getSize()));
            listItem.setBackgroundColor(mDecks.get(position).getColor());

            return listItem;

        }
    }
}
