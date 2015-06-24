package com.zumobi.android.zbimsampleapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.zumobi.zbim.ContentFragment;
import com.zumobi.zbim.ContentFragmentQueryResultListener;
import com.zumobi.zbim.ZBiM;


public class ContentWidgetActivity extends Activity implements ContentFragmentQueryResultListener, AdapterView.OnItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ListView mListViewArticleLinks;
    private ContentFragment[] mContentFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inflate UI
        setContentView(R.layout.activity_article_link);

        // get references to UI elements
        mListViewArticleLinks = (ListView)findViewById(R.id.listView_article_links);

        // generate the list of Content Widgets - the ContentFragmentQueryResultListener will receive results
        final ZBiM zbim = ZBiM.getInstance(this);
        zbim.generateContentWidgets("article", 0, null, false, this);
    }

    /*
    Concrete implementation of ContentFragmentQueryResultListener interface
     */
    @Override
    public void onSuccess(ContentFragment[] contentFragments) {

        mContentFragments = contentFragments;//maintain reference for click event

        // Generate an array of just views for use with the Listview Adapter
        View[] arrayViews = new View[contentFragments.length];

        int index = 0;

        for(ContentFragment contentWidget : contentFragments) {

            arrayViews[index++] = contentWidget.getContentWidget();
        }

        // now that data exists, instantiate the Listview Adapter
        final ListViewAdapter listViewAdapter = new ListViewAdapter(arrayViews);
        mListViewArticleLinks.setAdapter(listViewAdapter);
        mListViewArticleLinks.setOnItemClickListener(this);
    }

    @Override
    public void onFailure() {

    }

    /*
    Concrete implementation of OnItemClickListener
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //TODO temporary demo code
        mContentFragments[position].performAction();
    }
}
