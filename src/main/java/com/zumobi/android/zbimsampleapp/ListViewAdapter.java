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
//        ViewHolder viewHolder;
//
//        if (convertView == null) {
//            // instantiate a ViewHolder object
//            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
//            convertView = inflater.inflate(R.layout.content_widget_default, parent, false);
//            viewHolder = new ViewHolder();
//            viewHolder.mImageView = (ImageView)convertView.findViewById(R.id.imageView);
//            viewHolder.mTextViewTitle = (TextView)convertView.findViewById(R.id.textview_title);
//            viewHolder.mTextViewCaption = (TextView)convertView.findViewById(R.id.textview_caption);
//            convertView.setTag(viewHolder);
//        } else {
//            // re-use a ViewHolder object
//            viewHolder = (ViewHolder) convertView.getTag();
//
//        }
        return mViewsData[position];
    }

//    // ViewHolder pattern
//    static class ViewHolder {
//        public ImageView mImageView;
//        public TextView mTextViewTitle;
//        public TextView mTextViewCaption;
//    }
}
