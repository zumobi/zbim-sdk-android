package com.zumobi.android.zbimsampleapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.Toast;

import com.zumobi.android.zbimsampleapp.customViews.SegmentedRadioGroup;
import com.zumobi.zbim.ZBiM;
import com.zumobi.zbim.ZBiMColorScheme;
import com.zumobi.zbim.exceptions.ZBiMErrorType;
import com.zumobi.zbim.exceptions.ZBiMStateException;

/**
 * The Launch activity that is parent of all other views in this project
 */
public class MainActivity extends Activity implements OnClickListener, OnCheckedChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String ERROR_DIALOG_TITLE = "title";
    public static final String ERROR_DIALOG_MESSAGE = "message";

    public enum ScreenMode {FULLSCREEN, FRAGMENT}
    public enum StatusMode {DEFAULT, CUSTOM}

    private Button mShowStatusDefaultButton;
    private Button mShowStatusCustomButton;
	private Button mShowContentHubButton;
    private Button mShowContentWidgetButton;
	private Button mCreateNewUserButton;
	private Button mSwitchUserButton;
    private Switch mThemeSwitch;
    static public ScreenMode mScreenMode = ScreenMode.FULLSCREEN;
    static public StatusMode mStatusMode = StatusMode.DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inflate UI
        setContentView(R.layout.activity_main);
        
        // get references to UI elements
        setupUIElements();

        // Initialize ZBiM
        ZBiM.start(getApplication());

        // Set Logger
        ZBiM.setLoggingHandler(new ZBiMSDKLogger());

        this.setupActiveUser();
    }


    private void setupUIElements() {

        // Setup buttons
        mShowContentHubButton = (Button)findViewById(R.id.buttonShowContentHub);
        if (mShowContentHubButton != null) {
        	mShowContentHubButton.setOnClickListener(this);
        }

        mShowContentWidgetButton = (Button)findViewById(R.id.buttonShowContentWidgets);
        if (mShowContentWidgetButton != null) {
            mShowContentWidgetButton.setOnClickListener(this);
        }

        mShowStatusDefaultButton = (Button)findViewById(R.id.button_default_status);
        if (mShowStatusDefaultButton != null) {
            mShowStatusDefaultButton.setOnClickListener(this);
        }

        mShowStatusCustomButton = (Button)findViewById(R.id.button_custom_status);
        if (mShowStatusCustomButton != null) {
            mShowStatusCustomButton.setOnClickListener(this);
        }

        mCreateNewUserButton = (Button)findViewById(R.id.buttonCreateNewUser);
        if (mCreateNewUserButton != null) {
            mCreateNewUserButton.setOnClickListener(this);
        }

        mSwitchUserButton = (Button)findViewById(R.id.buttonSwitchUser);
        if (mSwitchUserButton != null) {
            mSwitchUserButton.setOnClickListener(this);
        }

        // Setup switches
        mThemeSwitch = (Switch)findViewById(R.id.switchTheme);

        // set up radio group that selects UI mode
        SegmentedRadioGroup radioUImode = (SegmentedRadioGroup)findViewById(R.id.segment_mode_group);
        radioUImode.setOnCheckedChangeListener(this);

        if (mScreenMode == ScreenMode.FULLSCREEN)
            radioUImode.check(R.id.button_one);
        else
            radioUImode.check(R.id.button_two);

        // set up radio group that selects Status mode
        SegmentedRadioGroup radioStatusmode = (SegmentedRadioGroup)findViewById(R.id.segment_status_group);
        radioStatusmode.setOnCheckedChangeListener(this);

        if (mStatusMode == StatusMode.DEFAULT)
            radioStatusmode.check(R.id.button_default_status);
        else
            radioStatusmode.check(R.id.button_custom_status);
    }

    private void setupActiveUser() {

        // If there is no active user already set, create a new
        // one and set it as the current active user.
        String userId = ZBiM.getActiveUser();

        if (userId == null){
            // Have the SDK generate a user ID on the app's behalf.
            userId = ZBiM.generateDefaultUserID();
            ZBiM.createUser(userId, null);

            // Set the newly created user as the active user.
            ZBiM.setActiveUser(userId);
        }
    }

    @Override
    public void onClick(View view) {

        // required initialization for fragment or fullscreen content hubs
        if ((view == mShowContentWidgetButton) || (view == mShowContentHubButton)) {
            // REQUIRED: set the Color Scheme
            if (mThemeSwitch.isChecked()) {
                ZBiM.setColorScheme(ZBiMColorScheme.Dark);
            } else {
                ZBiM.setColorScheme(ZBiMColorScheme.Light);
            }
        }

        if (view == mShowStatusCustomButton) {
            // register callback
            ZBiM.setContenthubStatusUiDelegate((ApplicationSample) getApplication());
        }

        if (view == mShowStatusDefaultButton) {
            // unregister callback
            ZBiM.setContenthubStatusUiDelegate(null);
        }

        if (view == mShowContentWidgetButton) {
            Intent intent = new Intent(this, ContentWidgetListActivity.class);
            startActivityForResult(intent, 0);
            //Note: the mScreenMode is tested from within ContentWidgetListActivity
            return;
        }

    	if (view == mShowContentHubButton) {

            if(mScreenMode == ScreenMode.FULLSCREEN) {
                try {
                    ZBiM.setReferrerTracking("FullScreen ContentHub").launchContentHubActivity();
                } catch (ZBiMStateException ex) {

                    showError(this, ex.getUserTitle(), ex.getUserMessage());

                    if (ex.getErrorType() == ZBiMErrorType.SDKDisabledState){
                        // Typically no action taken here. On next App launch, we will try reset and can try again.
                        Log.d(TAG, "SDK is disabled");
                    }
                    else if (ex.getErrorType() == ZBiMErrorType.RemovedFromPilotProgram || ex.getErrorType() == ZBiMErrorType.DeniedAccessToPilotProgram){
                        // The user has been removed from the Pilot Program, the app can decide to disable or remove the button
                        // On next App launch, we will try again or next DB download.
                        // mShowContentHubButton.setEnabled(false);
                        Log.d(TAG, "Removed from Pilot Program");
                    }
                }
            } else if(mScreenMode == ScreenMode.FRAGMENT) {
                Intent intent = new Intent(this, ActivityFragmentHub.class);
                startActivityForResult(intent, 0);
            }

    		return;
    	}

        // Create new user
        if (view == mCreateNewUserButton) {
            Intent intent = new Intent(this, NewUserActivity.class);
            startActivity(intent);
            return;
        }

        // Switch User
        if (view == mSwitchUserButton) {
            Intent intent = new Intent(this, SwitchUserActivity.class);
            startActivity(intent);
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Shows an activity to let a developer / tester access URI entrypoints in the db
        if (id == R.id.id_menu_showuripage) {
            Intent intent = new Intent(this, ShowUriActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Implementation of OnCheckedChangeListener interface
     * @param group RadioGroup
     * @param checkedId ID of button
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.button_one) {
            mScreenMode = ScreenMode.FULLSCREEN;
        }

        else if (checkedId == R.id.button_two) {
            mScreenMode = ScreenMode.FRAGMENT;
        }

        else if (checkedId == R.id.button_default_status) {
            mStatusMode = StatusMode.DEFAULT;
        }

        else if (checkedId == R.id.button_custom_status) {
            mStatusMode = StatusMode.CUSTOM;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data != null) {
            showError(this, data.getStringExtra(ERROR_DIALOG_TITLE), data.getStringExtra(ERROR_DIALOG_MESSAGE));
        }
    }

    protected static void showError(Context context, String title, String message) {
        // Create the dialog
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(message);

        // Get app icon ID
        try {
            final String packageName = context.getPackageName();
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            final int iconId = packageInfo.applicationInfo.icon;
            alertBuilder.setIcon(iconId);
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            Log.d(TAG, "Failed to obtain packageName and packageInfo");
        }

        alertBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertBuilder.show();
    }
}
