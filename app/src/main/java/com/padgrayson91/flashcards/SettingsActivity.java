package com.padgrayson91.flashcards;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by patrickgrayson on 3/12/16.
 */
public class SettingsActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new LayoutPreferenceFragment())
                .commit();
    }
}
