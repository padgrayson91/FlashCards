package com.padgrayson91.flashcards;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ListView;

/**
 * Created by patrickgrayson on 3/13/16.
 */
public abstract class GenericListFragment extends Fragment {
    //http://stackoverflow.com/questions/24811536/android-listview-get-item-view-by-position
    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    protected abstract void showChecks(boolean show);
}
