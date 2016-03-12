package com.padgrayson91.flashcards;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by patrickgrayson on 3/12/16.
 */
public class CardViewerActivity extends AppCompatActivity {
    private Deck mDeck;
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
        if(intent != null){
            Bundle extras = intent.getExtras();
            if(extras != null){
                String deckName = extras.getString(Constants.EXTRA_DECK_NAME, "");
                if(!"".equals(deckName)){
                    mDeck = mStorage.readDeckFromFile(deckName);
                }
                focusedCardId = extras.getString(Constants.EXTRA_CARD_ID, "");
            }
        }

        mPager = (ViewPager) findViewById(R.id.viewpager);
        ArrayList<Fragment> cardFragments = new ArrayList<>();
        ArrayList<Card> cards = new ArrayList<>();
        int focusedIndex = 0;
        //First add cards to list so we can sort them
        if(mDeck != null) {
            Card c;
            while ((c = mDeck.getNextCard()) != null) {
                cards.add(c);
            }
        }
        Collections.sort(cards);
        for(Card c: cards){
                //Make a fragment for each card
                Bundle args = new Bundle();
                args.putString(Constants.EXTRA_CARD_ID, c.id);
                args.putString(Constants.EXTRA_DECK_NAME, mDeck.getName());
                Fragment f = CardViewerFragment.instantiate(this, CardViewerFragment.class.getName(), args);
                ((CardViewerFragment) f).setCard(c);
                cardFragments.add(f);
                if(c.id.equals(focusedCardId)){
                    focusedIndex = cardFragments.size() - 1;
                }

        }
        mPageAdapter = new CardPageAdapter(getSupportFragmentManager(), cardFragments);
        mPager.setAdapter(mPageAdapter);
        mPager.setCurrentItem(focusedIndex);
    }

    class CardPageAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;

        public CardPageAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }
    }
}
