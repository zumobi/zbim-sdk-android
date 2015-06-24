package com.zumobi.android.zbimsampleapp;

import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import com.zumobi.zbim.ContentHubFragment;
import com.zumobi.zbim.ZBiM;
import com.zumobi.zbim.listeners.OnPageActionListener;
import com.zumobi.zbim.listeners.OnScrollListener;

import org.xwalk.core.XWalkView;

import java.io.Serializable;

public class ActivityFragmentHub extends FragmentActivity implements OnClickListener, OnPageActionListener, OnScrollListener {

	// Constants
	private final String TAG = this.getClass().getSimpleName();
    public final static String EXTRA_INTENT_URI = "ActivityFragmentHub.INTENT_EXTRA.URI";
	
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
    private ViewGroup mFragmentContainer;
    private boolean mShowingToolBar;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment_container);

        mButtonContainer = (ViewGroup) findViewById(R.id.button_container);
        mButtonContainer.animate().setListener(mButtonContainerAnimationListener);
        mFragmentContainer = (ViewGroup) findViewById(R.id.fragment_container);

        mToggleButton = (ToggleButton) findViewById(R.id.fragment_container_toggle);
        mToggleButton.setOnClickListener(this);
        mToggleButton.setChecked(true);

        // get references to buttons in the button bar below the content hub fragment
		mButtonBack = (Button) findViewById(R.id.btn_back);
		mButtonClose = (Button) findViewById(R.id.btn_close);
		
		// assign click listener to each button
		mButtonBack.setOnClickListener(this);
		mButtonClose.setOnClickListener(this);
		
		// start off with Back and Forward buttons disabled - these will be enabled by callback
		mButtonBack.setEnabled(false);
		
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

            // if a URI is passed-in, use it as a launch-pad for the contenthub
            String strURI = null;
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                strURI = extras.getString(EXTRA_INTENT_URI);
            }

            // the fragment must be returned by ZBiM
            mContentHubFragment = ZBiM.getInstance(this).getContentHubFragment(strURI);//NOTE: a specific URI can be passed here, or null to use default hub
            mContentHubFragment.setOnPageActionListener(this);
            mContentHubFragment.setOnScrollListener(this);

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, (Fragment)mContentHubFragment).commit();
            getSupportFragmentManager().executePendingTransactions();

        }

        LocalBroadcastManager.getInstance(this).registerReceiver(mContentNavigationReciever, new IntentFilter(ZBiM.CONTENT_TYPE_CHANGED_ACTION));
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
	 * OnClickListener Interface implementation
	 */
	@Override
	public void onClick(View view) {
		if (view == mToggleButton) {
			switchContainerItem();
			return;
		}

        if (view == mButtonBack) {
            mContentHubFragment.goBack();
            return;
        }

		// exit this activity
		if (view == mButtonClose) {
			finish();
		}
	}

    /**
	 * OnPageActionListener Interface implementation
	 */
	@Override
	public void onPageStarted(XWalkView view, String url) {
		Log.i(TAG,"onPageStarted");
	}

	@Override
	public void onPageFinished(XWalkView view, String url) {
		Log.i(TAG,"onPageFinished");
		
		final boolean canGoBack = view.getNavigationHistory().canGoBack();
		final boolean canGoForward = view.getNavigationHistory().canGoForward();
		
		// update the UI on the UI thread
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mButtonBack.setEnabled(canGoBack);
			}
		});
	}

	@Override
	public void onReceivedError(XWalkView view, int errorCode, String description, String failingUrl) {
		Log.i(TAG,"onReceivedError");
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
                /*int height = mButtonContainer.getLayoutParams().height;

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams()
                mButtonContainer.setLayoutParams().setMargins();
                mButtonContainer.requestLayout();
                mFragmentContainer.requestLayout();*/
            }
            else if(mButtonContainer.getVisibility() != View.VISIBLE) {
                mButtonContainer.setVisibility(View.VISIBLE);
                /*int height = mButtonContainer.getLayoutParams().height;

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams()
                mButtonContainer.setLayoutParams().setMargins();
                mButtonContainer.requestLayout();
                mFragmentContainer.requestLayout();*/
            }

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }

    };
}