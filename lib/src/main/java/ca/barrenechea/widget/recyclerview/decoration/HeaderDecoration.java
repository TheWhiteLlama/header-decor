/*
 * Copyright 2016 Christian Ringshofer
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.barrenechea.widget.recyclerview.decoration;

import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.HashMap;
import java.util.Map;

public abstract class HeaderDecoration extends RecyclerView.ItemDecoration {

    /**
     * No top margin will be applied to the sticky-headers
     */
    public static final int NO_MARGIN_TOP = -1;

    protected HeaderAdapter mAdapter;
    protected int marginTop = HeaderDecoration.NO_MARGIN_TOP;
    protected Map<Long, RecyclerView.ViewHolder> mHeaderCache;

    public HeaderDecoration(@NonNull final HeaderAdapter adapter) {
        this.mAdapter = adapter;
        this.mHeaderCache = new HashMap<>();
    }

    /**
     * Headers will be recreated and rebound on list scroll after this method has been called.
     */
    public abstract void clearCache();

    /**
     * check if the adapter is using a top margin for its sticky-headers
     *
     * @return true if the adapter is using top margin
     */
    public boolean hasMarginTop() {
        return this.marginTop != HeaderDecoration.NO_MARGIN_TOP;
    }

    /**
     * clear the top margin
     */
    public void clearMarginTop() {
        this.marginTop = HeaderDecoration.NO_MARGIN_TOP;
    }

    /**
     * set the top margin of the headers so that they will stop before reaching the top border of the view.
     * This is very useful when implementing sticky-headers combined with smooth-app-bar-layout:
     * <a href="https://github.com/henrytao-me/smooth-app-bar-layout">Smooth-App-Bar-Layout by henrytao-me</a>
     *
     * @param marginTop the margin top dimension
     */
    public void setMarginTop(@DimenRes int marginTop) {
        this.marginTop = marginTop;
    }


    protected boolean hasHeader(int position) {
        if (position == 0 && mAdapter.getHeaderId(position) != RecyclerView.NO_ID) {
            return true;
        }

        int previous = position - 1;
        return mAdapter.getHeaderId(position) != RecyclerView.NO_ID && mAdapter.getHeaderId(position) != mAdapter.getHeaderId(previous);
    }

}
