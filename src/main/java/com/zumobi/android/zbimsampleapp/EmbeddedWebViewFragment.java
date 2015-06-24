package com.zumobi.android.zbimsampleapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zumobi.zbim.ObservableWebView;

public class EmbeddedWebViewFragment extends Fragment {

    // Member Variables
    private ObservableWebView mWebView;

    // Fragment Lifecycle callbacks
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        Activity parentActivity = getActivity();

        mWebView = new ObservableWebView(parentActivity, parentActivity);
        mWebView.loadUrl("http://www.zumobi.com");
        return mWebView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.resumeTimers();
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.pauseTimers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    public void goBack() {
        mWebView.goBack();
    }

    public void goForward() {
        mWebView.goForward();
    }
}
