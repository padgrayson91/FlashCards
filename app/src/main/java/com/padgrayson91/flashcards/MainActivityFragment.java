package com.padgrayson91.flashcards;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
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
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.padgrayson91.flashcards.Constants.ACTION_BUILD_DECK;
import static com.padgrayson91.flashcards.Constants.ERROR_DUPLICATE_NAME;
import static com.padgrayson91.flashcards.Constants.ERROR_EMPTY_NAME;
import static com.padgrayson91.flashcards.Constants.ERROR_WRITE_FAILED;
import static com.padgrayson91.flashcards.Constants.EXTRA_DECK_NAME;
import static com.padgrayson91.flashcards.Constants.SUCCESS;

/**
 * A fragment containing the list of available decks
 */

//TODO: Search decks
public class MainActivityFragment extends GenericListFragment {
    private static final String TAG = "FlashCards";

    private ArrayList<Deck> mDecks;
    private ArrayList<Deck> mSelectedDecks;
    private TextView mEmptyText;
    private ListView mDeckList;
    private Storage mStorage;
    private DeckListAdapter mDeckAdapter;
    private Menu mMenu;
    private ActionMode mActionMode;

    //Deck creation modes
    public static final int CREATE_MODE_NEW = 0;
    public static final int CREATE_MODE_MERGE = 1;
    public static final int CREATE_MODE_RENAME = 2;

    private static final String KEY_SHOW_CHECKS = "show_checks";
    private static final String KEY_SELECTED_DECKS = "selected_decks";
    private static final String KEY_ACTION_MODE = "action_mode";
    private boolean doShowChecks;
    private boolean isActionMode;

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
        mDeckList.setOnItemLongClickListener(mItemLongClickListener);

        if(mDecks.size() != 0){
            mEmptyText.setVisibility(View.GONE);
        } else {
            mDeckList.setVisibility(View.GONE);
        }
        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mStorage = new Storage(getActivity());
        if(savedInstanceState != null){
            doShowChecks = savedInstanceState.getBoolean(KEY_SHOW_CHECKS, false);
            if((isActionMode = savedInstanceState.getBoolean(KEY_ACTION_MODE, false))){
                mActionMode = getActivity().startActionMode(mActionModeCallback);
            }
            ArrayList<String> deckNames = savedInstanceState.getStringArrayList(KEY_SELECTED_DECKS);
            mSelectedDecks = new ArrayList<>();
            if(deckNames != null){
                for(String s: deckNames){
                    Deck d = mStorage.readDeckFromFile(s);
                    if(d != null) {
                        mSelectedDecks.add(d);
                    }
                }
            }
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "Fragment Resumed!");
        updateDecks();
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_SHOW_CHECKS, doShowChecks);
        outState.putBoolean(KEY_ACTION_MODE, isActionMode);
        ArrayList<String> deckNames = new ArrayList<>();
        for(Deck d: mSelectedDecks){
            deckNames.add(d.getName());
        }
        outState.putStringArrayList(KEY_SELECTED_DECKS, deckNames);
        super.onSaveInstanceState(outState);
    }

    public void promptCreateDeck(final int mode){
        final Context context = getActivity();
        if(context == null){
            return;
        }
        final boolean overwrite = (mode == CREATE_MODE_MERGE);
        final EditText input = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        String promptText = getResources().getString(R.string.alert_name_deck);
        if(mode == CREATE_MODE_MERGE){
            promptText = getResources().getString(R.string.alert_merge_deck);
        }
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(promptText)
                .setView(input)
                .setPositiveButton(getResources().getString(R.string.alert_name_accept), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Storage storage = new Storage(context);
                        String deckName = input.getText().toString();
                        int result = storage.storeDeck(deckName, overwrite);
                        switch (result) {
                            case ERROR_DUPLICATE_NAME:
                                Toast.makeText(context, "A deck with that name already exists!", Toast.LENGTH_LONG).show();
                                break;
                            case ERROR_EMPTY_NAME:
                                Toast.makeText(context, "You must give your deck a name!", Toast.LENGTH_LONG).show();
                                break;
                            case ERROR_WRITE_FAILED:
                                Toast.makeText(context, "Oops, somethings went wrong!", Toast.LENGTH_LONG).show();
                                break;
                            case SUCCESS:
                                if (mode == CREATE_MODE_MERGE) {
                                    mergeSelectedDecks(deckName);
                                    Toast.makeText(context, "Decks merged!", Toast.LENGTH_SHORT).show();
                                } else if (mode == CREATE_MODE_RENAME) {
                                    renameSelectedDeck(deckName);
                                    Toast.makeText(context, "Deck renamed!", Toast.LENGTH_SHORT).show();
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
        showChecks(false);
        mActionMode.finish();
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
        mActionMode.finish();
        showChecks(false);
    }

    public void renameSelectedDeck(String newName){
        if(mSelectedDecks.size() < 1)
            return;
        Deck d = mSelectedDecks.get(0);
        mStorage.removeDeck(d.getName());
        d.rename(newName);
        mStorage.writeDeckToFile(d);
        //clear the list of selected decks
        mSelectedDecks = new ArrayList<>();
        mActionMode.finish();
        showChecks(false);

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
        startActivity(deckBuilderIntent);
    }

    protected void showChecks(boolean show){
        doShowChecks = show;
        for(int i = 0; i < mDeckAdapter.getCount(); i++){
            View v = getViewByPosition(i, mDeckList);
            LinearLayout selectionLayout = (LinearLayout) v.findViewById(R.id.checkbox_layout);
            if(selectionLayout == null){
                return;
            }
            if(show) {
                selectionLayout.setVisibility(View.VISIBLE);
            } else {
                selectionLayout.setVisibility(View.GONE);
            }

        }
    }

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(mActionMode != null){
                mActionMode.finish();
            }
            Deck d = mDecks.get(position);
            if(d.getSize() > 0){
                ((MainActivity) getActivity()).onDeckSelected(d);
            } else {
                startBuilder(d);
            }
        }
    };

    private AdapterView.OnItemLongClickListener mItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            showChecks(true);
            mActionMode = getActivity().startActionMode(mActionModeCallback);
            CheckBox selectionCheck = (CheckBox) view.findViewById(R.id.selection_check);
            if(selectionCheck != null)
                selectionCheck.setChecked(true);

            return true;
        }
    };

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_context_deck, menu);
            mMenu = menu;
            isActionMode = true;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    promptDeleteDecks();
                    return true;
                case R.id.action_merge:
                    promptCreateDeck(CREATE_MODE_MERGE);
                    return true;
                case R.id.action_edit:
                    promptCreateDeck(CREATE_MODE_RENAME);
                    return true;
                case R.id.action_view:
                    startBuilder(mSelectedDecks.get(0));
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            isActionMode = false;
            mActionMode = null;
            showChecks(false);
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
            final Deck rowDeck = mDecks.get(position);
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout listItem = (LinearLayout) inflater.inflate(R.layout.list_item_deck, parent, false);
            TextView deckNameView = (TextView) listItem.findViewById(R.id.view_deck_name);
            TextView lastPlayedView = (TextView) listItem.findViewById(R.id.last_played_text);
            if(!(rowDeck.getLastPlayed() == Deck.NEVER_PLAYED)){
                String timeGapText = "";
                long timeGapMs = System.currentTimeMillis() - rowDeck.getLastPlayed();
                //Only display the largest time unit
                long days = TimeUnit.MILLISECONDS.toDays(timeGapMs);
                long hours = TimeUnit.MILLISECONDS.toHours(timeGapMs);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(timeGapMs);
                if(days > 0){
                    timeGapText = timeGapText + days + " d";
                } else if(hours > 0){
                    timeGapText = timeGapText + hours + " h";
                } else {
                    //Don't care if it's 0 minutes, not going higher resolution than this
                    timeGapText = timeGapText + minutes + " m";
                }
                lastPlayedView.setText(timeGapText);

            }

            try {
                GradientDrawable lastPlayedBg = (GradientDrawable) getResources().getDrawable(R.drawable.time_indicator_bubble);
                lastPlayedBg.setColor(rowDeck.getDarkColor());
                Log.d(TAG, "Setting background: " + lastPlayedBg.toString());
                lastPlayedView.setBackground(lastPlayedBg);
            } catch (ClassCastException ex) {
                Log.d(TAG, "Unable to cast drawable to gradient drawable");
            }
            TextView deckSizeView = (TextView) listItem.findViewById(R.id.view_deck_size);
            if(doShowChecks) {
                LinearLayout selectionLayout = (LinearLayout) listItem.findViewById(R.id.checkbox_layout);
                selectionLayout.setVisibility(View.VISIBLE);
            }
            CheckBox selectionCheck = (CheckBox) listItem.findViewById(R.id.selection_check);
            if(mSelectedDecks.contains(rowDeck)){
                selectionCheck.setChecked(true);
            }
            //NOTE: this isn't thread safe, so may need a lock on mSelectedDecks;
            selectionCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        if(!mSelectedDecks.contains(rowDeck)){
                            mSelectedDecks.add(rowDeck);
                        }
                    } else {
                        if(mSelectedDecks.contains(rowDeck)){
                            mSelectedDecks.remove(rowDeck);
                        }
                    }
                    if(mMenu == null){
                        //If there's no menu just hide the checks since they aren't useful without menu
                        showChecks(false);
                        return;
                    }
                    int newSize = mSelectedDecks.size();
                    if(newSize >= 2){
                        Log.d(TAG, "Two items are checked");
                        mMenu.setGroupVisible(R.id.group_actions_two_or_more, true);
                    }
                    if(newSize >= 1){
                        Log.d(TAG, "An item is checked");
                        mMenu.setGroupVisible(R.id.group_actions_one_or_more, true);
                    }
                    if(newSize <= 1){
                        mMenu.setGroupVisible(R.id.group_actions_two_or_more, false);
                    }
                    if(newSize == 0){
                        mMenu.setGroupVisible(R.id.group_actions_one_or_more, false);
                        mActionMode.finish();
                        showChecks(false);
                    }
                    if(newSize != 1){
                        mMenu.setGroupVisible(R.id.group_actions_only_one, false);
                    }
                    if(newSize == 1){
                        mMenu.setGroupVisible(R.id.group_actions_only_one, true);
                    }
                }
            });

            deckNameView.setText(rowDeck.getName());
            deckSizeView.setText(getResources().getQuantityString(R.plurals.card_count, rowDeck.getSize(), rowDeck.getSize()));
            listItem.setBackgroundColor(rowDeck.getColor());

            return listItem;

        }
    }
}
