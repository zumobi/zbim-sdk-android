package com.zumobi.android.zbimsampleapp;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Adapter to show ContentWidget's
 */
public class ListViewAdapter extends BaseAdapter {

    private View[] mViewsData;

    public ListViewAdapter(View[] data) {
        mViewsData = data;
    }

    @Override
    public int getCount() {
        return mViewsData.length;
    }

    @Override
    public Object getItem(int position) {
        return mViewsData[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return mViewsData[position];
    }

}
