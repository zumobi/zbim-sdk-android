package com.zumobi.android.zbimsampleapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Adapter to show ContentWidgets
 * @see ContentWidgetListActivity
 */
public class ListViewAdapter extends BaseAdapter {

    private View[] mViewsData;
    private LayoutInflater mLayoutInflater;

    public ListViewAdapter(Context context, View[] data) {
        mViewsData = data;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mViewsData.length*2;
    }

    @Override
    public Object getItem(int position) {
        return null;//not used in this sample code
    }

    @Override
    public long getItemId(int position) {
        return position;//not used in this sample code
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position % 2 == 0) {
            return mViewsData[position/2];
        } else {
            convertView = mLayoutInflater.inflate(R.layout.third_party_list_cell, parent, false);
            return convertView;
        }
    }

}
