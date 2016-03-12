package com.padgrayson91.flashcards;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import static com.padgrayson91.flashcards.Constants.ERROR_DUPLICATE_NAME;
import static com.padgrayson91.flashcards.Constants.ERROR_EMPTY_NAME;
import static com.padgrayson91.flashcards.Constants.ERROR_WRITE_FAILED;
import static com.padgrayson91.flashcards.Constants.SUCCESS;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "FlashCards";


    private int currentMode;
    private static final String KEY_MODE = "mode";
    private static final int MODE_LIST = 0;
    private static final int MODE_PLAYER = 1;

    private static final String TAG_LIST = "display_list";
    private static final String TAG_PLAY = "play_deck";

    private Deck mSelectedDeck;

    private FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            currentMode = savedInstanceState.getInt(KEY_MODE);
        } else {
            currentMode = MODE_LIST;
        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText input = new EditText(MainActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getResources().getString(R.string.alert_name_deck))
                        .setView(input)
                        .setPositiveButton(getResources().getString(R.string.alert_name_accept), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Storage storage = new Storage(MainActivity.this);
                                String deckName = input.getText().toString();
                                int result = storage.storeDeck(deckName);
                                switch (result) {
                                    case ERROR_DUPLICATE_NAME:
                                        Toast.makeText(MainActivity.this, "Deck already exists!", Toast.LENGTH_LONG).show();
                                        break;
                                    case ERROR_EMPTY_NAME:
                                        Toast.makeText(MainActivity.this, "You must give your deck a name!", Toast.LENGTH_LONG).show();
                                        break;
                                    case ERROR_WRITE_FAILED:
                                        Toast.makeText(MainActivity.this, "Oops, somethings went wrong!", Toast.LENGTH_LONG).show();
                                        break;
                                    case SUCCESS:
                                        Toast.makeText(MainActivity.this, "Deck " + deckName + " created!", Toast.LENGTH_SHORT).show();
                                        MainActivityFragment maf = (MainActivityFragment) getSupportFragmentManager().findFragmentByTag(TAG_LIST);
                                        maf.updateDecks();
                                        break;
                                }
                            }
                        }).setNegativeButton(getResources().getString(R.string.alert_name_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        swapFragment(currentMode);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_MODE, currentMode);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //TODO: should let user change some settings
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onDeckSelected(Deck d){
        mSelectedDeck = d;
        swapFragment(MODE_PLAYER);
        FragmentManager fm = getSupportFragmentManager();
        PlayerFragment playerFragment = (PlayerFragment) fm.findFragmentByTag(TAG_PLAY);
        if(playerFragment == null){
            fm.executePendingTransactions();
            playerFragment = (PlayerFragment) fm.findFragmentByTag(TAG_PLAY);

        }
        if(playerFragment == null){
            //Give up if we're still null

        } else {
            playerFragment.loadCard(d.getNextCard());
        }
    }

    public void onPlayFinished(){
        swapFragment(MODE_LIST);
        Toast.makeText(MainActivity.this, "Deck complete!", Toast.LENGTH_SHORT).show();
    }

    private void swapFragment(int mode){
        currentMode = mode;
        Fragment fragment = null;
        String TAG = "";
        switch (mode){
            case MODE_PLAYER:
                fab.setVisibility(View.GONE);
                fragment = new PlayerFragment();
                ((PlayerFragment) fragment).setDeck(mSelectedDeck);
                TAG = TAG_PLAY;
                break;
            case MODE_LIST:
                fab.setVisibility(View.VISIBLE);
                fragment = new MainActivityFragment();
                TAG = TAG_LIST;
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_placeholder, fragment, TAG)
                .commit();

    }
}
