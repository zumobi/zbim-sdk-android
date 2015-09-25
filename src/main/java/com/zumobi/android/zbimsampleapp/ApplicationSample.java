package com.zumobi.android.zbimsampleapp;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.support.multidex.MultiDex;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zumobi.zbim.interfaces.ContenthubStatusUiDelegate;

/**
 * This class maintains state for long-term objects that should not be destroyed when an activity closes
 */
public class ApplicationSample extends Application implements ContenthubStatusUiDelegate {

    private static ApplicationSample singleton;
    private ProgressBar mProgressBar;// keep reference for callback

    public static ApplicationSample getInstance(){
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }

    // required for android.support.multidex.MultiDexApplication support
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    //
    // Concrete implementation of ContenthubStatusUiDelegate interface follows
    //

    @Override
    public ViewGroup getErrorView(String optionalMessage, ViewGroup root) {

        // inflate your XML layout (or create it programmatically, if you wish)
        LayoutInflater inflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        //NOTE: no need to attach to root, ZBiM will do it, so pass "false" below
        final View inflatedview = inflater.inflate(R.layout.custom_ui_message, root, false);
        final RelativeLayout viewgroup = (RelativeLayout) inflatedview.findViewById(R.id.custom_user_message_container);
        viewgroup.setBackgroundColor(Color.RED);

        // example of using the suggested message from ZBiM - it can also be customized as desired
        final TextView textview = (TextView) inflatedview.findViewById(R.id.custom_usermessage_text_message);
        textview.setText(optionalMessage);

        // hide the progressbar - it is not relevant for this usecase
        final ProgressBar progressbar = (ProgressBar) inflatedview.findViewById(R.id.custom_usermessage_progressBar);
        progressbar.setVisibility(View.GONE);

        return viewgroup;
    }


    @Override
    public ViewGroup getCheckingForContentView(String optionalMessage, ViewGroup root) {

        // inflate your XML layout (or create it programmatically, if you wish)
        LayoutInflater inflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        //NOTE: no need to attach to root, ZBiM will do it, so pass "false" below
        final View inflatedview = inflater.inflate(R.layout.custom_ui_message, root, false);
        final RelativeLayout viewgroup = (RelativeLayout) inflatedview.findViewById(R.id.custom_user_message_container);
        viewgroup.setBackgroundColor(Color.GREEN);

        // example of changing the text message - note you can also show optionalMessage
        final TextView textview = (TextView) inflatedview.findViewById(R.id.custom_usermessage_text_message);
        textview.setText("Custom Content-Check Message");

        // hide the progressbar - it is not relevant for this usecase
        final ProgressBar progressbar = (ProgressBar) inflatedview.findViewById(R.id.custom_usermessage_progressBar);
        progressbar.setVisibility(View.GONE);

        return viewgroup;
    }


    @Override
    public ViewGroup getDownloadProgressView(ViewGroup root) {

        // inflate your XML layout (or create it programmatically, if you wish)
        LayoutInflater inflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        //NOTE: no need to attach to root, ZBiM will do it, so pass "false" below
        final View inflatedview = inflater.inflate(R.layout.custom_ui_message, root, false);
        final RelativeLayout viewgroup = (RelativeLayout) inflatedview.findViewById(R.id.custom_user_message_container);
        viewgroup.setBackgroundColor(Color.BLUE);

        // example of changing the text message - note you can also show optionalMessage
        final TextView textview = (TextView) inflatedview.findViewById(R.id.custom_usermessage_text_message);
        textview.setText("Custom Downloading Message");

        // show the progressbar
        mProgressBar = (ProgressBar) inflatedview.findViewById(R.id.custom_usermessage_progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        return viewgroup;
    }


    @Override
    public void downloadProgress(int percentComplete) {
        // note this is called on UI thread already
        if (mProgressBar != null) {
            mProgressBar.setProgress(percentComplete);
        }
    }
}
