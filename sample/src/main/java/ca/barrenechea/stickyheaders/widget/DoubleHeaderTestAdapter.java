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

package ca.barrenechea.stickyheaders.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ca.barrenechea.stickyheaders.R;
import ca.barrenechea.widget.recyclerview.decoration.DoubleHeaderAdapter;

public class DoubleHeaderTestAdapter extends RecyclerView.Adapter<DoubleHeaderTestAdapter.ViewHolder> implements DoubleHeaderAdapter<DoubleHeaderTestAdapter.HeaderHolder, DoubleHeaderTestAdapter.SubHeaderHolder> {

    private LayoutInflater mInflater;

    public DoubleHeaderTestAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final View view = mInflater.inflate(R.layout.item_test, viewGroup, false);
        return new ViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.item.setText(String.format("Item %d", i));
    }

    @Override
    public int getItemCount() {
        return 100;
    }

    @Override
    public long getHeaderId(int position) {
        if (position < 5) return RecyclerView.NO_ID; // test leaving out headers
        return position / 18;
    }

    @Override
    public long getSubHeaderId(int position) {
        if (position < 18) return RecyclerView.NO_ID; // test leaving out headers
        return position / 3;
    }

    @Override
    public HeaderHolder onCreateHeaderViewHolder(ViewGroup parent) {
        final View view = mInflater.inflate(R.layout.super_header_test, parent, false);
        return new HeaderHolder(view);
    }

    @Override
    public SubHeaderHolder onCreateSubHeaderHolder(ViewGroup parent) {
        final View view = mInflater.inflate(R.layout.header_test, parent, false);
        return new SubHeaderHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindHeaderViewHolder(HeaderHolder viewHolder, int position) {
        viewHolder.timeline.setText(String.format("Header %d", getHeaderId(position)));
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindSubHeaderHolder(SubHeaderHolder viewholder, int position) {
        viewholder.date.setText(String.format("Sub-header %d", getSubHeaderId(position)));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView item;

        public ViewHolder(View itemView) {
            super(itemView);

            item = (TextView) itemView;
        }
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        public TextView timeline;

        public HeaderHolder(View itemView) {
            super(itemView);

            timeline = (TextView) itemView;
        }
    }

    static class SubHeaderHolder extends RecyclerView.ViewHolder {
        public TextView date;

        public SubHeaderHolder(View itemView) {
            super(itemView);

            date = (TextView) itemView;
        }
    }
}
