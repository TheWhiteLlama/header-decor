/*
 * Copyright 2014 Eduardo Barrenechea
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.barrenechea.stickyheaders.ui;

import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import ca.barrenechea.stickyheaders.R;
import ca.barrenechea.stickyheaders.widget.StickyTestAdapter;
import ca.barrenechea.widget.recyclerview.decoration.HeaderDecoration;

public class StickyHeaderFragment extends BaseDecorationFragment {

    private RecyclerView list;

    private HeaderDecoration decor;

    @Override
    protected void setAdapterAndDecor(RecyclerView list) {
        final StickyTestAdapter adapter = new StickyTestAdapter(this.getActivity());
        decor = new HeaderDecoration(adapter);
        setHasOptionsMenu(true);

        this.list = list;
        this.list.setAdapter(adapter);
        this.list.addItemDecoration(decor, 1);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        final MenuItem item = menu.findItem(R.id.action_toggle_top_margin);
        item.setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear_cache:
                decor.clearCache();
                return true;
            case R.id.action_toggle_top_margin:
                if (decor.hasMarginTop()) {
                    decor.clearMarginTop();
                } else {
                    decor.setMarginTop(R.dimen.margin_top);
                }
                decor.clearCache();
                list.invalidate();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
