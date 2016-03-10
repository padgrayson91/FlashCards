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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

/**
 * A fragment containing the list of available decks
 */

//TODO: Search decks
public class MainActivityFragment extends Fragment {

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

        if(mDecks.size() != 0){
            mEmptyText.setVisibility(View.GONE);
        } else {
            mDeckList.setVisibility(View.GONE);
        }
        return root;
    }

    private void getDecksFromStorage(){
        Set<String> temp = mStorage.getDecks();
        for(String s: temp){
            if(!mDecks.contains(s)){
                Deck d = mStorage.readDeckFromFile(s);
                if(d == null){
                    Toast.makeText(getContext(), "Oops, something went wrong!", Toast.LENGTH_LONG).show();
                    break;
                } else {
                    mDecks.add(d);
                }
            }
        }
    }

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
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout listItem = (LinearLayout) inflater.inflate(R.layout.list_item_deck, parent, false);
            TextView deckNameView = (TextView) listItem.findViewById(R.id.view_deck_name);
            TextView deckSizeView = (TextView) listItem.findViewById(R.id.view_deck_size);
            deckNameView.setText(mDecks.get(position).getName());
            deckSizeView.setText(getResources().getQuantityString(R.plurals.card_count, mDecks.get(position).getSize()));

            return listItem;

        }
    }
}
