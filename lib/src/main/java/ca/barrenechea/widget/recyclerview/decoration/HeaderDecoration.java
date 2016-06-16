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

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

public class HeaderDecoration extends RecyclerView.ItemDecoration {

    /**
     * No top margin will be applied to the sticky-headers
     */
    public static final int NO_MARGIN_TOP = -1;

    /* default header */
    @NonNull
    protected HeaderAdapter headerAdapter;
    @NonNull
    protected Map<Long, RecyclerView.ViewHolder> mHeaderCache;

    /* sub-header */
    @Nullable
    private DoubleHeaderAdapter subHeaderAdapter;
    @Nullable
    private Map<Long, RecyclerView.ViewHolder> subHeaderCache;


    protected int marginTop = HeaderDecoration.NO_MARGIN_TOP;
    protected boolean renderInline = false;

    public HeaderDecoration(@NonNull final HeaderAdapter adapter) {
        this(adapter, false);
    }

    public HeaderDecoration(
            @NonNull final HeaderAdapter adapter,
            final boolean renderInline
    ) {

        // instantiate default adapter
        this.headerAdapter = adapter;
        this.mHeaderCache = new HashMap<>();

        // instantiate double header adapter
        if (adapter instanceof DoubleHeaderAdapter) {
            this.subHeaderAdapter = (DoubleHeaderAdapter) adapter;
            this.subHeaderCache = new HashMap<>();
        }

        // define if headers will be rendered inline
        this.renderInline = renderInline;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getItemOffsets(
            final Rect outRect,
            final View view,
            final RecyclerView parent,
            final RecyclerView.State state
    ) {
        int position = parent.getChildAdapterPosition(view);
        int headerHeight = 0;
        if (isInLayout(position)) {
            if (hasHeader(position)) headerHeight += getHeaderHeightForLayout(parent, position);
            if (hasSubHeader(position)) headerHeight += getHeight(getSubHeader(parent, position));
        }
        outRect.set(0, headerHeight, 0, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawOver(
            @NonNull final Canvas canvas,
            @NonNull final RecyclerView parent,
            @NonNull final RecyclerView.State state
    ) {

        boolean headerDrawn = false;
        final int count = parent.getChildCount();
        for (int layoutPos = 0; layoutPos < count; layoutPos++) {
            final View child = parent.getChildAt(layoutPos);
            boolean visible = getAnimatedTop(child) > -child.getHeight()/* && child.getTop() < parent.getHeight()*/;
            final int adapterPos = parent.getChildAdapterPosition(child);
            if (visible && isInLayout(adapterPos) && (!headerDrawn || hasSubHeader(adapterPos))) {

                // get viewHolder
                final RecyclerView.ViewHolder holder = getHeader(parent, adapterPos);
                final RecyclerView.ViewHolder subHolder = getSubHeader(parent, adapterPos);

                // draw subHeaders
                final View subHeader = getView(subHolder);
                final View header = getView(holder);

                if (subHeader != null) {
                    canvas.save();
                    final int left = child.getLeft();
                    final int top = getSubHeaderTop(parent, child, header, subHeader, adapterPos, layoutPos);
                    canvas.translate(left, top);
                    subHeader.setTranslationX(left);
                    subHeader.setTranslationY(top);
                    subHeader.draw(canvas);
                    canvas.restore();
                    headerDrawn = true;
                }
            }
        }

        headerDrawn = false;
        for (int layoutPos = 0; layoutPos < count; layoutPos++) {
            final View child = parent.getChildAt(layoutPos);
            final int adapterPos = parent.getChildAdapterPosition(child);
            if (isInLayout(adapterPos) && (!headerDrawn || hasHeader(adapterPos))) {

                // get viewHolder
                final RecyclerView.ViewHolder holder = getHeader(parent, adapterPos);
                final RecyclerView.ViewHolder subHolder = getSubHeader(parent, adapterPos);

                // draw subHeaders
                final View subHeader = getView(subHolder);
                final View header = getView(holder);

                if (header != null) {
                    canvas.save();
                    final int left = child.getLeft();
                    final int top = getHeaderTop(parent, child, header, subHeader, adapterPos, layoutPos);
                    canvas.translate(left, top);
                    header.setTranslationX(left);
                    header.setTranslationY(top);
                    header.draw(canvas);
                    canvas.restore();
                    headerDrawn = true;
                }
            }
        }
    }

    /**
     * Headers will be recreated and rebound on list scroll after this method has been called.
     */
    public void clearCache() {
        this.clearHeaderCache();
        this.clearSubHeaderCache();
    }

    /**
     * Clears the subHeader view cache. SubHeaders will be recreated and
     * rebound on list scroll after this method has been called.
     */
    public void clearSubHeaderCache() {
        if (this.subHeaderCache != null) this.subHeaderCache.clear();
    }

    /**
     * Clears the header view cache. Headers will be recreated and
     * rebound on list scroll after this method has been called.
     */
    public void clearHeaderCache() {
        this.mHeaderCache.clear();
    }

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

    protected boolean isInLayout(final int adapterPosition) {
        return adapterPosition != RecyclerView.NO_POSITION;
    }

    protected boolean hasMarginTop(@DimenRes final int marginTop) {
        return marginTop != HeaderDecoration.NO_MARGIN_TOP;
    }

    protected int getMarginTopPixels(@NonNull final ViewGroup viewGroup) {
        final Resources resources = viewGroup.getContext().getResources();
        return hasMarginTop(this.marginTop) ? resources.getDimensionPixelSize(this.marginTop) : 0;
    }

    protected int getAnimatedTop(@NonNull final View view) {
        return view.getTop() + (int) view.getTranslationY();
    }

    protected boolean isFirstValidChild(final int layoutPos, @NonNull final RecyclerView parent) {
        boolean isFirstValidChild = true;
        for (int otherLayoutPos = layoutPos - 1; otherLayoutPos >= 0; --otherLayoutPos) {
            final View otherChild = parent.getChildAt(otherLayoutPos);
            if (isInLayout(parent.getChildAdapterPosition(otherChild))) {
                boolean visible = getAnimatedTop(otherChild) > -getHeaderHeightForLayout(otherChild);
                if (visible) {
                    isFirstValidChild = false;
                    break;
                }
            }
        }
        return isFirstValidChild;
    }

    protected int getHeaderHeightForLayout(@NonNull final RecyclerView recyclerView, final int adapterPos) {
        final RecyclerView.ViewHolder viewHolder = getHeader(recyclerView, adapterPos);
        return getHeaderHeightForLayout(viewHolder);
    }

    protected int getHeaderHeightForLayout(@Nullable final RecyclerView.ViewHolder viewHolder) {
        if (viewHolder == null) return 0;
        return getHeaderHeightForLayout(viewHolder.itemView);
    }

    protected int getHeaderHeightForLayout(@NonNull final View header) {
        return this.renderInline ? 0 : header.getHeight();
    }

    private long getHeaderId(final int adapterPos) {
        return this.headerAdapter.getHeaderId(adapterPos);
    }

    private long getSubHeaderId(final int adapterPos) {
        if (this.subHeaderAdapter == null) return RecyclerView.NO_ID;
        return this.subHeaderAdapter.getSubHeaderId(adapterPos);
    }

    /**
     * check if the given item at a given adapterPosition has a header
     *
     * @param adapterPos the adapterPosition to check
     * @return true if the item has a header
     */
    protected boolean hasHeader(final int adapterPos) {
        final boolean hasId = this.headerAdapter.getHeaderId(adapterPos) != RecyclerView.NO_ID;
        if (adapterPos == 0 && hasId) {
            return true;
        }
        final int previous = adapterPos - 1;
        return hasId && this.headerAdapter.getHeaderId(adapterPos) != this.headerAdapter.getHeaderId(previous);
    }


    /**
     * check if the given item at a given adapterPosition has a subHeader
     *
     * @param adapterPos the adapterPosition to check
     * @return true if the item has a subHeader
     */
    private boolean hasSubHeader(final int adapterPos) {
        if (this.subHeaderAdapter == null) return false;
        final boolean hasId = this.subHeaderAdapter.getSubHeaderId(adapterPos) != RecyclerView.NO_ID;
        if (adapterPos == 0 && hasId) {
            return true;
        }
        final int previous = adapterPos - 1;
        return hasId && this.subHeaderAdapter.getSubHeaderId(adapterPos) != this.subHeaderAdapter.getSubHeaderId(previous);
    }

    protected int getHeaderTop(
            @NonNull final RecyclerView parent,
            @NonNull final View child,
            @NonNull final View header,
            @Nullable final View subHeader,
            final int adapterPos,
            final int layoutPos
    ) {
        int top = getAnimatedTop(child) - getHeight(header) - getHeight(subHeader);
        final int marginTop = getMarginTopPixels(parent);
        if (isFirstValidChild(layoutPos, parent)) {
            final int count = parent.getChildCount();
            final long currentId = this.headerAdapter.getHeaderId(adapterPos);
            // find next view with header and compute the offscreen push if needed
            for (int i = layoutPos + 1; i < count; i++) {
                final View next = parent.getChildAt(i);
                int nextAdapterPosition = parent.getChildAdapterPosition(next);
                if (isInLayout(nextAdapterPosition)) {
                    long nextId = this.headerAdapter.getHeaderId(nextAdapterPosition);
                    if (nextId != currentId) {
                        final int headersHeight = getHeaderHeightForLayout(header) + getHeaderHeightForLayout(parent, nextAdapterPosition);
                        int offset = getAnimatedTop(next) - headersHeight - getSubHeaderHeight(parent, nextAdapterPosition);
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

    private int getSubHeaderTop(
            @NonNull final RecyclerView parent,
            @NonNull final View child,
            @Nullable final View header,
            @Nullable final View subHeader,
            final int adapterPos,
            final int layoutPos
    ) {

        final int top = getAnimatedTop(child) - getHeight(subHeader);
        final int marginTop = getMarginTopPixels(parent);

        if (isFirstValidChild(layoutPos, parent)) {

            final int count = parent.getChildCount();
            final long currentHeaderId = this.getHeaderId(adapterPos);
            final long currentSubHeaderId = this.getSubHeaderId(adapterPos);

            // find next view with sub-header and compute the offscreen push if needed
            for (int i = layoutPos + 1; i < count; i++) {
                final View next = parent.getChildAt(i);
                int nextAdapterPosition = parent.getChildAdapterPosition(next);
                if (isInLayout(nextAdapterPosition)) {
                    final long nextHeaderId = this.getHeaderId(nextAdapterPosition);
                    final long nextSubHeaderId = this.getSubHeaderId(nextAdapterPosition);

                    if (nextSubHeaderId != currentSubHeaderId /*&& nextSubHeaderId != RecyclerView.NO_ID*/) {

                        int headersHeight = getHeight(subHeader) + getHeight(getSubHeader(parent, nextAdapterPosition));
                        if (nextHeaderId != currentHeaderId) {
                            headersHeight += getHeight(getHeader(parent, nextAdapterPosition)); // add subHolder height?
                        }

                        final int offset = getAnimatedTop(next) - headersHeight;// getAnimatedTop(next) - subHeadersHeight;
                        if (offset < marginTop + getHeight(header)) {
                            return offset;
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        return Math.max(marginTop + getHeight(header), top);

    }

    /**
     * @param parent     the recyclerView
     * @param adapterPos the adapterPosition
     * @return the header for a given adapter position if a header exists for that position
     */
    @Nullable
    protected RecyclerView.ViewHolder getHeader(@NonNull final RecyclerView parent, final int adapterPos) {
        final long id = headerAdapter.getHeaderId(adapterPos);
        if (id == RecyclerView.NO_ID) return null;

        if (mHeaderCache.containsKey(id)) {
            return mHeaderCache.get(id);
        } else {
            final RecyclerView.ViewHolder holder = this.headerAdapter.onCreateHeaderViewHolder(parent);
            final View header = holder.itemView;

            //noinspection unchecked
            this.headerAdapter.onBindHeaderViewHolder(holder, adapterPos);
            measureView(parent, header);
            mHeaderCache.put(id, holder);

            return holder;
        }
    }

    /**
     * @param parent     the recyclerView
     * @param adapterPos the adapterPosition
     * @return the subHeader for a given adapter position if a subHeader exists for that position
     */
    @Nullable
    private RecyclerView.ViewHolder getSubHeader(@NonNull final RecyclerView parent, final int adapterPos) {
        if (this.subHeaderAdapter == null || this.subHeaderCache == null) return null;
        final long id = this.getSubHeaderId(adapterPos);
        if (id == RecyclerView.NO_ID) return null;

        if (this.subHeaderCache.containsKey(id)) {
            return this.subHeaderCache.get(id);
        } else {
            final RecyclerView.ViewHolder holder = this.subHeaderAdapter.onCreateSubHeaderHolder(parent);
            final View header = holder.itemView;

            //noinspection unchecked
            this.subHeaderAdapter.onBindSubHeaderHolder(holder, adapterPos);
            this.measureView(parent, header);
            this.subHeaderCache.put(id, holder);

            return holder;
        }
    }

    /**
     * calculate the height for a given adapterPosition
     * if the adapterPosition does not contain a header, return 0
     *
     * @param adapterPos the adapterPos to get the header for
     * @return 0 if the item does not have a header, otherwise the height of the item's header
     */
    protected int getHeaderHeight(@NonNull final RecyclerView recyclerView, final int adapterPos) {
        return getHeaderHeightForLayout(getHeader(recyclerView, adapterPos));
    }

    /**
     * calculate the height for a given adapterPosition
     * if the adapterPosition does not contain a subHeader, return 0
     *
     * @param adapterPos the adapterPos to get the subHeader for
     * @return 0 if the item does not have a header, otherwise the height of the item's header
     */
    protected int getSubHeaderHeight(@NonNull final RecyclerView recyclerView, final int adapterPos) {
        return getHeight(getSubHeader(recyclerView, adapterPos));
    }

    /**
     * calculate the height for a given viewHolder if it's not null, otherwise return 0
     *
     * @param viewHolder the viewHolder, may be null
     * @return 0 if the viewHolder is null, otherwise the height of its itemView
     */
    protected int getHeight(@Nullable final RecyclerView.ViewHolder viewHolder) {
        if (viewHolder == null) return 0;
        return getHeight(viewHolder.itemView);
    }

    /**
     * calculate the height for a given view if it's not null, otherwise return 0
     *
     * @param view the view, may be null
     * @return 0 if the view is null, otherwise the height of the view
     */
    protected int getHeight(@Nullable final View view) {
        if (view == null) return 0;
        return view.getHeight();
    }

    @Nullable
    protected View getView(@Nullable final RecyclerView.ViewHolder viewHolder) {
        if (viewHolder == null) return null;
        return viewHolder.itemView;
    }


    protected void measureView(@NonNull final RecyclerView parent, @NonNull final View header) {

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);

        final int childWidth = ViewGroup.getChildMeasureSpec(
                widthSpec, parent.getPaddingLeft() + parent.getPaddingRight(),
                header.getLayoutParams().width
        );
        final int childHeight = ViewGroup.getChildMeasureSpec(
                heightSpec, parent.getPaddingTop() + parent.getPaddingBottom(),
                header.getLayoutParams().height
        );

        header.measure(childWidth, childHeight);
        header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());

    }

}
