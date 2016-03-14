package com.padgrayson91.flashcards;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by patrickgrayson on 3/12/16.
 */
public class CardViewerActivity extends AppCompatActivity {
    private static final String TAG = "FlashCards";

    private static final String KEY_CARD_ID = "card_id";
    private static final String KEY_DECK_NAME = "deck_name";

    private Deck mDeck;
    private ArrayList<Card> mCards;
    private Storage mStorage;
    private String focusedCardId;

    private ViewPager mPager;
    private CardPageAdapter mPageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_viewer);
        mStorage = new Storage(CardViewerActivity.this);
        Intent intent = getIntent();
        //If we have a saved state, check that first
        if(savedInstanceState != null && !("".equals(savedInstanceState.getString(KEY_CARD_ID, "")))){
            Log.d(TAG, "Reading card/deck info from saved state");
            focusedCardId = savedInstanceState.getString(KEY_CARD_ID);
            mDeck = mStorage.readDeckFromFile(savedInstanceState.getString(KEY_DECK_NAME));
        //Next check to see if progress is in storage
        } else if((!"".equals(mStorage.getInProgressDeck()))) {
            Log.d(TAG, "Reading card/deck info from storage");
            mDeck = mStorage.readDeckFromFile(mStorage.getInProgressDeck());
            focusedCardId = mStorage.getInProgressCardId();
        } else if(intent != null){
            Log.d(TAG, "Reading card/deck info from intent");
            Bundle extras = intent.getExtras();
            if(extras != null){
                String deckName = extras.getString(Constants.EXTRA_DECK_NAME, "");
                if(!"".equals(deckName)){
                    mDeck = mStorage.readDeckFromFile(deckName);
                }
                focusedCardId = extras.getString(Constants.EXTRA_CARD_ID, "");
            }
        } else {
            Log.d(TAG, "Had no idea what was going on");
            //We have no idea where we are, so just close this activity
            finish();
        }
        mPager = (ViewPager) findViewById(R.id.viewpager);
        ArrayList<Fragment> cardFragments = new ArrayList<>();
        mCards = new ArrayList<>();
        int focusedIndex = 0;
        //First add cards to list so we can sort them
        if(mDeck != null) {
            setTitle(mDeck.getName());
            Card c;
            while ((c = mDeck.getNextCard()) != null) {
                mCards.add(c);
            }
        }
        Log.d(TAG, "Had " + mCards.size() + " cards");
        mPageAdapter = new CardPageAdapter(getSupportFragmentManager(), mCards.size());
        mPager.setAdapter(mPageAdapter);
        mPager.setCurrentItem(focusedIndex);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Pausing, storing card/deck info");
        mStorage.storeInProgressDeckName(mDeck.getName());
        mStorage.storeInProgressCardId(focusedCardId);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "Saving state");
        outState.putString(KEY_DECK_NAME, mDeck.getName());
        outState.putString(KEY_CARD_ID, focusedCardId);
        super.onSaveInstanceState(outState);
    }

    class CardPageAdapter extends PagerAdapter {
        private Fragment[] fragments;
        private FragmentManager fm;

        public CardPageAdapter(FragmentManager fm, int size) {
            this.fm = fm;
            this.fragments = new Fragment[size];
        }

        public Fragment getItem(int position) {
            assert(0 <= position && position < fragments.length);
            Card.setSortMode(mStorage.getCardSortMode());
            Collections.sort(mCards);
            Card c = mCards.get(position);
            if(fragments[position] == null){

                //Make a fragment for each card or find the existing one
                Bundle args = new Bundle();
                Log.d(TAG, "Card id for fragment: " + c.id);
                args.putString(Constants.EXTRA_CARD_ID, c.id);
                args.putString(Constants.EXTRA_DECK_NAME, mDeck.getName());
                fragments[position] = Fragment.instantiate(CardViewerActivity.this, CardViewerFragment.class.getName(), args);
            } else {
                ((CardViewerFragment) fragments[position]).setCard(c);
            }
            return fragments[position];
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            assert (0 <= position && position < fragments.length);
            FragmentTransaction trans = fm.beginTransaction();
            trans.remove(fragments[position]);
            trans.commit();
            fragments[position] = null;
        }

        @Override
        public Fragment instantiateItem(ViewGroup container, int position){
            Fragment fragment = getItem(position);
            ((CardViewerFragment) fragment).setCard(mCards.get(position));
            FragmentTransaction trans = fm.beginTransaction();
            trans.add(container.getId(),fragment,"fragment:"+position);
            trans.commit();
            return fragment;
        }

        @Override
        public int getCount() {
            return this.fragments.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object fragment) {
            return ((Fragment) fragment).getView() == view;
        }
    }
}
