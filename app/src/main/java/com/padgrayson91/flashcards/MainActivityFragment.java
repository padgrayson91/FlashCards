package com.padgrayson91.flashcards;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
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

import static com.padgrayson91.flashcards.Constants.*;

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
        return root;
    }

    public void deleteSelectedDecks(){
        for(int i = 0; i < mDecks.size(); i++){
            boolean selected = ((CheckBox) mDeckList.getChildAt(i).findViewById(R.id.selection_check)).isChecked();
            if(selected){
                mStorage.removeDeck(mDecks.get(i).getName());
            }
        }
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
            final Deck d = mDecks.get(position);
            //TODO: Make a custom dialog fragment because this looks awful
            if(d.getSize() > 0){
                new AlertDialog.Builder(getActivity()).setMessage(getResources().getString(R.string.dialog_play_or_edit))
                        .setPositiveButton(getResources().getString(R.string.dialog_button_play), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((MainActivity) getActivity()).onDeckSelected(d);
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.dialog_button_edit), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startBuilder(d);
                            }
                        }).setCancelable(true).show();

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
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(TAG, "Getting view for " + mDecks.get(position).getName());
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout listItem = (LinearLayout) inflater.inflate(R.layout.list_item_deck, parent, false);
            TextView deckNameView = (TextView) listItem.findViewById(R.id.view_deck_name);
            TextView deckSizeView = (TextView) listItem.findViewById(R.id.view_deck_size);
            deckNameView.setText(mDecks.get(position).getName());
            deckSizeView.setText(getResources().getQuantityString(R.plurals.card_count, mDecks.get(position).getSize(), mDecks.get(position).getSize()));
            listItem.setBackgroundColor(mDecks.get(position).getColor());

            return listItem;

        }
    }
}
