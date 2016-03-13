package com.padgrayson91.flashcards;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by patrickgrayson on 3/12/16.
 */
public class LayoutPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference_layout);
    }

}
