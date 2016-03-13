package com.padgrayson91.flashcards;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.Set;

import static com.padgrayson91.flashcards.Constants.ACTION_BUILD_DECK;
import static com.padgrayson91.flashcards.Constants.ERROR_EMPTY_NAME;
import static com.padgrayson91.flashcards.Constants.ERROR_WRITE_FAILED;
import static com.padgrayson91.flashcards.Constants.EXTRA_DECK_NAME;
import static com.padgrayson91.flashcards.Constants.REQUEST_CODE_BUILD_DECK;
import static com.padgrayson91.flashcards.Constants.SUCCESS;

/**
 * A fragment containing the list of available decks
 */

//TODO: Search decks
public class MainActivityFragment extends Fragment {
    private static final String TAG = "FlashCards";

    private ArrayList<Deck> mDecks;
    private ArrayList<Deck> mSelectedDecks;
    private TextView mEmptyText;
    private ListView mDeckList;
    private Storage mStorage;
    private DeckListAdapter mDeckAdapter;
    private Menu mMenu;

    public MainActivityFragment() {
        mDecks = new ArrayList<Deck>();
        mSelectedDecks = new ArrayList<Deck>();
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
        mMenu = menu;
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_fragment_main, menu);
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
            promptDeleteDecks();
            return true;
        } else if(id == R.id.action_merge){
            promptCreateDeck(true);
        }

        return super.onOptionsItemSelected(item);
    }

    public void promptCreateDeck(final boolean isMerge){
        final Context context = getActivity();
        if(context == null){
            return;
        }
        final EditText input = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(getResources().getString(R.string.alert_name_deck))
                .setView(input)
                .setPositiveButton(getResources().getString(R.string.alert_name_accept), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Storage storage = new Storage(context);
                        String deckName = input.getText().toString();
                        int result = storage.storeDeck(deckName, isMerge);
                        switch (result) {
                            case ERROR_EMPTY_NAME:
                                Toast.makeText(context, "You must give your deck a name!", Toast.LENGTH_LONG).show();
                                break;
                            case ERROR_WRITE_FAILED:
                                Toast.makeText(context, "Oops, somethings went wrong!", Toast.LENGTH_LONG).show();
                                break;
                            case SUCCESS:
                                if (isMerge) {
                                    mergeSelectedDecks(deckName);
                                    Toast.makeText(context, "Decks merged!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Deck created!", Toast.LENGTH_SHORT).show();
                                }
                                updateDecks();
                                break;
                        }
                    }
                }).setNegativeButton(getResources().getString(R.string.alert_name_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    public void promptDeleteDecks(){
        Context context = getActivity();
        if(context == null){
            return;
        }
        Formatter formatter = new Formatter();
        String promptText = getResources().getQuantityString(R.plurals.alert_delete_deck, mSelectedDecks.size(), mSelectedDecks.size());
        new AlertDialog.Builder(context).setCancelable(true)
                .setMessage(promptText)
                .setPositiveButton(getResources().getString(R.string.alert_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteSelectedDecks();
                    }
                }).setNegativeButton(getResources().getString(R.string.alert_name_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    public void deleteSelectedDecks(){
        for(Deck d: mSelectedDecks){
            mStorage.removeDeck(d.getName());
        }
        //clear the list of selected decks
        mSelectedDecks = new ArrayList<>();
        Toast.makeText(getActivity(), "Decks deleted", Toast.LENGTH_SHORT).show();
        updateDecks();
    }

    public void mergeSelectedDecks(String newName){
        if(mSelectedDecks.size() <= 1)
            return;
        Deck primary = mSelectedDecks.get(0);
        for(int i = 1; i < mSelectedDecks.size(); i++){
            Deck secondary = mSelectedDecks.get(i);
            primary.merge(secondary);
        }
        for(Deck d: mSelectedDecks){
            mStorage.removeDeck(d.getName());
        }
        mStorage.storeDeck(newName);
        primary.rename(newName);
        mStorage.writeDeckToFile(primary);
        //clear the list of selected decks
        mSelectedDecks = new ArrayList<>();
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
                    if (d.getSize() > 0) {
                        ((MainActivity) getActivity()).onDeckSelected(d);
                    } else {
                        Toast.makeText(getContext(), "You need to add cards first!", Toast.LENGTH_SHORT).show();
                        startBuilder(d);
                    }
                }
            });
            CheckBox selectionCheck = (CheckBox) listItem.findViewById(R.id.selection_check);
            //NOTE: this isn't thread safe, so may need a lock on mSelectedDecks;
            selectionCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Deck d = mDecks.get(position);
                    int oldSize = mSelectedDecks.size();
                    if(isChecked){
                        if(!mSelectedDecks.contains(d)){
                            mSelectedDecks.add(d);
                        }
                    } else {
                        if(mSelectedDecks.contains(d)){
                            mSelectedDecks.remove(d);
                        }
                    }
                    int newSize = mSelectedDecks.size();
                    if(oldSize < 2 && newSize >= 2){
                        mMenu.setGroupVisible(R.id.group_actions_two_or_more, true);
                    }
                    if(oldSize == 0 && newSize >= 1){
                        mMenu.setGroupVisible(R.id.group_actions_one_or_more, true);
                    }
                    if(oldSize >= 2 && newSize <= 1){
                        mMenu.setGroupVisible(R.id.group_actions_two_or_more, false);
                    }
                    if(oldSize >= 1 && newSize == 0){
                        mMenu.setGroupVisible(R.id.group_actions_one_or_more, false);
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
