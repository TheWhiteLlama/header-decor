/*
 * Copyright 2014 Eduardo Barrenechea
 * Partial Copyright 2016 Christian Ringshofer
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

package ca.barrenechea.widget.recyclerview.decoration;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * A double sticky header decoration for android's RecyclerView.
 */
public class DoubleHeaderDecoration extends HeaderDecoration {

    private DoubleHeaderAdapter doubleHeaderAdapter;
    private Map<Long, RecyclerView.ViewHolder> mSubHeaderCache;

    /**
     * @param adapter the double header adapter to use
     */
    public DoubleHeaderDecoration(DoubleHeaderAdapter adapter) {
        super(adapter);
        this.doubleHeaderAdapter = adapter;
        this.mSubHeaderCache = new HashMap<>();
    }

    /**
     * Clears the subheader view cache. Subheaders will be recreated and
     * rebound on list scroll after this method has been called.
     */
    public void clearSubHeaderCache() {
        mSubHeaderCache.clear();
    }

    /**
     * Clears the header view cache. Headers will be recreated and
     * rebound on list scroll after this method has been called.
     */
    public void clearHeaderCache() {
        mHeaderCache.clear();
    }

    @Nullable
    private RecyclerView.ViewHolder getSubHeader(RecyclerView parent, int position) {
        final long key = this.doubleHeaderAdapter.getSubHeaderId(position);
        if (key == RecyclerView.NO_ID) return null;

        if (mSubHeaderCache.containsKey(key)) {
            return mSubHeaderCache.get(key);
        } else {
            final RecyclerView.ViewHolder holder =  this.doubleHeaderAdapter.onCreateSubHeaderHolder(parent);
            final View header = holder.itemView;

            //noinspection unchecked
            this.doubleHeaderAdapter.onBindSubHeaderHolder(holder, position);
            measureView(parent, header);
            mSubHeaderCache.put(key, holder);

            return holder;
        }
    }

    private RecyclerView.ViewHolder getHeader(RecyclerView parent, int position) {
        final long key = mAdapter.getHeaderId(position);

        if (mHeaderCache.containsKey(key)) {
            return mHeaderCache.get(key);
        } else {
            final RecyclerView.ViewHolder holder =  this.mAdapter.onCreateHeaderViewHolder(parent);
            final View header = holder.itemView;

            //noinspection unchecked
            this.mAdapter.onBindHeaderViewHolder(holder, position);
            measureView(parent, header);
            mHeaderCache.put(key, holder);

            return holder;
        }
    }

    private void measureView(RecyclerView parent, View header) {
        int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);

        int childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
                parent.getPaddingLeft() + parent.getPaddingRight(), header.getLayoutParams().width);
        int childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
                parent.getPaddingTop() + parent.getPaddingBottom(), header.getLayoutParams().height);

        header.measure(childWidth, childHeight);
        header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());
    }

    private boolean hasSubHeader(int position) {
        if (position == 0 &&  this.doubleHeaderAdapter.getSubHeaderId(position) != RecyclerView.NO_ID) {
            return true;
        }

        int previous = position - 1;
        return  this.doubleHeaderAdapter.getSubHeaderId(position) != RecyclerView.NO_ID &&  this.doubleHeaderAdapter.getSubHeaderId(position) !=  this.doubleHeaderAdapter.getSubHeaderId(previous);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);

        int headerHeight = 0;

        if (position != RecyclerView.NO_POSITION && hasSubHeader(position)) {
            if (hasHeader(position)) {
                View header = getHeader(parent, position).itemView;
                headerHeight += header.getHeight();
            }

            final RecyclerView.ViewHolder holder = getSubHeader(parent, position);
            if (holder != null) {
                final View header = holder.itemView;
                headerHeight += header.getHeight();
            }
        }

        outRect.set(0, headerHeight, 0, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        final int count = parent.getChildCount();

        boolean headerDrawn = false;
        for (int layoutPos = 0; layoutPos < count; layoutPos++) {
            final View child = parent.getChildAt(layoutPos);
            boolean visible = getAnimatedTop(child) > -child.getHeight()/* && child.getTop() < parent.getHeight()*/;
            final int adapterPos = parent.getChildAdapterPosition(child);
            if (visible && adapterPos != RecyclerView.NO_POSITION && (!headerDrawn || hasSubHeader(adapterPos))) {
                int left, top;
                final View header = getHeader(parent, adapterPos).itemView;
                final RecyclerView.ViewHolder holder = getSubHeader(parent, adapterPos);
                final View subHeader = holder == null ? null : holder.itemView;

                if (subHeader != null) {
                    c.save();
                    left = child.getLeft();
                    top = getSubHeaderTop(parent, child, header, subHeader, adapterPos, layoutPos);
                    c.translate(left, top);
                    subHeader.draw(c);
                    c.restore();
                }

                if (!headerDrawn || hasHeader(adapterPos)) {
                    c.save();
                    left = child.getLeft();
                    top = getHeaderTop(parent, child, subHeader, header, adapterPos, layoutPos);
                    c.translate(left, top);
                    header.draw(c);
                    c.restore();
                }

                headerDrawn = true;
            }
        }
    }

    private int getSubHeaderTop(RecyclerView parent, View child, @Nullable View header, @Nullable View subHeader, int adapterPos, int layoutPos) {
        final int headerHeight = header != null ? header.getHeight() : 0;
        final int subHeaderHeight = subHeader != null ? subHeader.getHeight() : 0;
        final int top = getAnimatedTop(child) - subHeaderHeight;
        final int marginTop = this.marginTop == HeaderDecoration.NO_MARGIN_TOP ? 0 : parent.getContext().getResources().getDimensionPixelSize(this.marginTop);
        if (isFirstValidChild(layoutPos, parent)) {
            final int count = parent.getChildCount();
            final long currentHeaderId = mAdapter.getHeaderId(adapterPos);
            final long currentSubHeaderId =  this.doubleHeaderAdapter.getSubHeaderId(adapterPos);

            // find next view with sub-header and compute the offscreen push if needed
            for (int i = layoutPos + 1; i < count; i++) {
                final View next = parent.getChildAt(i);
                int adapterPosHere = parent.getChildAdapterPosition(next);
                if (adapterPosHere != RecyclerView.NO_POSITION) {
                    final long nextHeaderId = mAdapter.getHeaderId(adapterPosHere);
                    final long nextSubHeaderId =  this.doubleHeaderAdapter.getSubHeaderId(adapterPosHere);

                    if ((nextSubHeaderId != currentSubHeaderId)) {
                        final RecyclerView.ViewHolder holder = getSubHeader(parent, adapterPosHere);
                        final View subHeaderHere = holder != null ? holder.itemView : null;
                        int subHeaderHeightHere = subHeaderHere != null ? subHeaderHere.getHeight() : 0;
                        int headersHeight = subHeaderHeight + subHeaderHeightHere;
                        if (nextHeaderId != currentHeaderId) {
                            headersHeight += getHeader(parent, adapterPosHere).itemView.getHeight();
                        }

                        final int offset = getAnimatedTop(next) - headersHeight;
                        if (offset < marginTop + headerHeight) {
                            return offset;
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        return Math.max(marginTop + headerHeight, top);
    }

    private int getHeaderTop(RecyclerView parent, View child, @Nullable View header, @Nullable View subHeader, int adapterPos, int layoutPos) {
        final int headerHeight = header != null ? header.getHeight() : 0;
        final int subHeaderHeight = subHeader != null ? subHeader.getHeight() : 0;
        final int top = getAnimatedTop(child) - headerHeight - subHeaderHeight;
        final int marginTop = this.marginTop == HeaderDecoration.NO_MARGIN_TOP ? 0 : parent.getContext().getResources().getDimensionPixelSize(this.marginTop);
        if (isFirstValidChild(layoutPos, parent)) {
            final int count = parent.getChildCount();
            final long currentId = mAdapter.getHeaderId(adapterPos);

            // find next view with header and compute the offscreen push if needed
            for (int i = layoutPos + 1; i < count; i++) {
                View next = parent.getChildAt(i);
                int adapterPosHere = parent.getChildAdapterPosition(next);
                if (adapterPosHere != RecyclerView.NO_POSITION) {
                    long nextId = mAdapter.getHeaderId(adapterPosHere);
                    if (nextId != currentId) {
                        final int headersHeight = headerHeight + getHeader(parent, adapterPosHere).itemView.getHeight();
                        final int offset = getAnimatedTop(next) - headersHeight - subHeaderHeight;

                        if (offset < marginTop) {
                            return offset;
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        return Math.max(marginTop, top);
    }

    private boolean isFirstValidChild(int layoutPos, RecyclerView parent) {
        boolean isFirstValidChild = true;
        final int marginTop = this.marginTop == HeaderDecoration.NO_MARGIN_TOP ? 0 : parent.getContext().getResources().getDimensionPixelSize(this.marginTop);
        for (int otherLayoutPos = layoutPos - 1; otherLayoutPos >= 0; --otherLayoutPos) {
            final View otherChild = parent.getChildAt(otherLayoutPos);
            if (parent.getChildAdapterPosition(otherChild) != RecyclerView.NO_POSITION) {
                boolean visible = getAnimatedTop(otherChild) > -(marginTop + otherChild.getHeight());
                if (visible) {
                    isFirstValidChild = false;
                    break;
                }
            }
        }
        return isFirstValidChild;
    }

    private int getAnimatedTop(View child) {
        return child.getTop() + (int) child.getTranslationY();
    }

    @Override
    public void clearCache() {
        clearSubHeaderCache();
        clearHeaderCache();
    }

}