package com.zumobi.android.zbimsampleapp;

import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.zumobi.zbim.ContentWidget;
import com.zumobi.zbim.ContentHubFragment;
import com.zumobi.zbim.ZBiM;
import com.zumobi.zbim.exceptions.ZBiMStateException;
import com.zumobi.zbim.interfaces.ContentWidgetDelegate;
import com.zumobi.zbim.listeners.OnPageActionListener;
import com.zumobi.zbim.listeners.OnScrollListener;

import java.io.Serializable;

public class ActivityFragmentHub extends FragmentActivity implements OnClickListener, OnScrollListener, ContentWidgetDelegate {

	// Constants
	private final String TAG = this.getClass().getSimpleName();

	// Member Variables
    private ToggleButton mToggleButton;

    private ViewGroup mButtonContainer;
    private Button mButtonBack;
	private Button mButtonClose;
	private ContentHubFragment mContentHubFragment;
	private EmbeddedWebViewFragment mEmbeddedWebViewFragment;
    private boolean mHideToolBar;
    private boolean mShowToolBar;
    private int mStartingX;
    private boolean mShowingToolBar;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment_container);

        mButtonContainer = (ViewGroup) findViewById(R.id.button_container);
        mButtonContainer.animate().setListener(mButtonContainerAnimationListener);

        mToggleButton = (ToggleButton) findViewById(R.id.fragment_container_toggle);
        mToggleButton.setOnClickListener(this);
        mToggleButton.setChecked(false);

        // get references to buttons in the button bar below the content hub fragment
		mButtonBack = (Button) findViewById(R.id.btn_back);
		mButtonClose = (Button) findViewById(R.id.btn_close);
		
		// assign click listener to each button
		mButtonBack.setOnClickListener(this);
		mButtonClose.setOnClickListener(this);
		
		// Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create the other fragment
            mEmbeddedWebViewFragment = new EmbeddedWebViewFragment();

            try {
                // if there exists a stored Content Widget, then show that article/channel/hub fragment, otherwise show the default hub
                ContentWidget contentWidget = ZBiM.getSelectedContentWidget();
                if (contentWidget == null) {
                    attachFragment(null);
                } else {
                    ZBiM.setContentWidgetDelegate(this);
                    contentWidget.performAction();
                }
            } catch (ZBiMStateException ex) {

                Intent data = new Intent();
                data.putExtra(MainActivity.ERROR_DIALOG_TITLE, ex.getUserTitle());
                data.putExtra(MainActivity.ERROR_DIALOG_MESSAGE, ex.getUserMessage());

                this.setResult(0, data);
                this.finish();
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(mContentNavigationReciever, new IntentFilter(ZBiM.CONTENT_TYPE_CHANGED_ACTION));

        ZBiM.setReferrerTracking("Fragment ContentHub");
	}

    private void switchContainerItem() {
        // Switch the fragment to the 'fragment_container' FrameLayout
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if(mToggleButton.isChecked()) {
            ft.replace(R.id.fragment_container, mEmbeddedWebViewFragment).commit();
        }
        else{
            ft.replace(R.id.fragment_container, mContentHubFragment).commit();
        }
        getSupportFragmentManager().executePendingTransactions();
    }

    /**
     * this is used to prevent the Contenthub from being destroyed, otherwise video playback is drastically interrupted.
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
	 * OnClickListener Interface implementation
	 */
	@Override
	public void onClick(View view) {
		if (view == mToggleButton) {
			switchContainerItem();
		} else if (view == mButtonBack) {
            mContentHubFragment.goBack();
        } else if (view == mButtonClose) {
            // exit this activity
			finish();
		}
	}

    @Override
    protected void onDestroy() {

        // Important: this is required cleanup
        ZBiM.setContentWidgetDelegate(null);
        ZBiM.selectContentWidget(null);

        super.onDestroy();
    }

    /**
     * ContentNavigation Receiver
     */

    private BroadcastReceiver mContentNavigationReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra(ZBiM.CONTENT_TYPE_CHANGED_ACTION_TITLE_EXTRA);
            Serializable uri = intent.getSerializableExtra(ZBiM.CONTENT_TYPE_CHANGED_ACTION_URL_EXTRA);
            String url = uri.toString();
            String type = intent.getStringExtra(ZBiM.CONTENT_TYPE_CHANGED_ACTION_TYPE_EXTRA);

            updateUI(title, url, type);
        }
    };

    private void updateUI(final String title, final String url, final String type) {
        ActivityFragmentHub.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(ActivityFragmentHub.this, "Content Navigation Action :: \nTitle: " + title + "; \nType: " + type + "; \nURI: " + url, Toast.LENGTH_LONG );
                toast.show();
            }
        });
    }

    /**
     * OnPageActionListener Interface implementation
     */
    @Override
    public void onScrollStarting(int horizontalScrollPosition, int verticalScrollPosition) {
        mStartingX = verticalScrollPosition;
        mHideToolBar = true;
        mShowToolBar = true;
    }

    @Override
    public void onScroll(int horizontalScrollPosition, int verticalScrollPosition) {
        if( mStartingX - verticalScrollPosition < -10){
            if(mHideToolBar && mButtonContainer.getVisibility() == View.VISIBLE){
                // Hide the Button Container
                TranslateAnimation tr = new TranslateAnimation(0,0,0,50);
                tr.setDuration(1000);
                mButtonContainer.setAnimation(tr);
                mButtonContainer.animate().start();

                mShowingToolBar = false;
            }
            mHideToolBar = false;
        }
        else if( mStartingX - verticalScrollPosition > 10){
            if(mShowToolBar && mButtonContainer.getVisibility() != View.VISIBLE){
                // Show the Button Container
                mButtonContainer.setVisibility(View.VISIBLE);

                TranslateAnimation tr = new TranslateAnimation(0,0,50,0);
                tr.setFillEnabled(true);
                tr.setFillBefore(true);
                tr.setDuration(1000);
                mButtonContainer.setAnimation(tr);
                mButtonContainer.animate().start();
                mShowingToolBar = true;
            }
            mShowToolBar = false;
        }
    }

    @Override
    public void onScrollEnding(int horizontalScrollPosition, int verticalScrollPosition) {
    }

    private Animator.AnimatorListener mButtonContainerAnimationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {

            if(!mShowingToolBar) {
                mButtonContainer.setVisibility(View.GONE);
                mShowingToolBar = false;
            }
            else if(mButtonContainer.getVisibility() != View.VISIBLE) {
                mButtonContainer.setVisibility(View.VISIBLE);
            }

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }

    };

    /*
    Concrete implementation of ContentWidgetDelegate
     */
    @Override
    public void attachFragment(String strUri) throws ZBiMStateException {
        // the fragment must be returned by ZBiM
        mContentHubFragment = ZBiM.getContentHubFragment(strUri);//NOTE: a specific URI can be passed here, or null to use default hub
        mContentHubFragment.setOnScrollListener(this);

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, mContentHubFragment).commit();
        getSupportFragmentManager().executePendingTransactions();
    }
}