package com.zumobi.android.zbimsampleapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.zumobi.zbim.ContentWidget;
import com.zumobi.zbim.ContentWidgetQueryResultListener;
import com.zumobi.zbim.ZBiM;


public class ContentWidgetListActivity extends Activity implements ContentWidgetQueryResultListener, AdapterView.OnItemClickListener {

    private ListView mListViewContentWidgets;
    private ProgressBar mProgressBarLoading;
    private ContentWidget[] mContentWidgets;
    private ZBiM mZBiM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inflate UI
        setContentView(R.layout.activity_content_widget_list);

        // get references to UI elements
        mListViewContentWidgets = (ListView)findViewById(R.id.listView_article_links);
        mProgressBarLoading = (ProgressBar) findViewById(R.id.progress_loading);

        // generate the list of Content Widgets - the ContentFragmentQueryResultListener will receive results
        mZBiM = ZBiM.getInstance(this);
        mZBiM.generateContentWidgets("article", 0, null, true, ContentWidgetListActivity.this);
    }


    /*
    Concrete implementation of ContentFragmentQueryResultListener interface
    NOTE: code in here runs on the UI Thread
    */
    @Override
    public void onSuccess(ContentWidget[] contentWidgets) {

        //maintain reference to data for possible click event later on
        mContentWidgets = contentWidgets;

        // process the widgets data - but do not do this on the UI thread otherwise UI performance will suffer
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                // Generate an array of just views for use with the Listview Adapter
                final View[] arrayViews = new View[mContentWidgets.length];

                int index = 0;
                for(ContentWidget contentWidget : mContentWidgets) {
                    arrayViews[index++] = contentWidget.getContentWidget();
                }

                // Data processing is finished - show it all, but from UI thread
                ContentWidgetListActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        // now that data exists, instantiate the Listview Adapter
                        final ListViewAdapter listViewAdapter = new ListViewAdapter(ContentWidgetListActivity.this, arrayViews);
                        mListViewContentWidgets.setAdapter(listViewAdapter);
                        mListViewContentWidgets.setOnItemClickListener(ContentWidgetListActivity.this);

                        // fixes Android bug https://code.google.com/p/android/issues/detail?id=159739
                        mListViewContentWidgets.setOverScrollMode(View.OVER_SCROLL_NEVER);

                        // about to show the widgets in the listview - hide the progress bar
                        mProgressBarLoading.setVisibility(View.INVISIBLE);
                    }
                });

            }
        };
        new Thread(runnable).start();
    }


    @Override
    public void onFailure() {

    }


    /*
    Concrete implementation of OnItemClickListener
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // test if clicked view is a content widget or 3rd party view
        if (position % 2 == 0) {
            position = position/2;
        } else {
            return; // 3rd party handles the click event for their content
        }

        // if full screen is enabled show as full screen, otherwise fragment
        if (MainActivity.mScreenMode == MainActivity.ScreenMode.FULLSCREEN) {

            // Launch full-screen activity defined in ZBiM
            mContentWidgets[position].performAction();
        }
        else
        {
            // store the selected Content Widget
            mZBiM.selectContentWidget(mContentWidgets[position]);

            // Launch user-customizable fragment activity
            Intent intent = new Intent(this, ActivityFragmentHub.class);
            startActivity(intent);
        }
    }

}
