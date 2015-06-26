package com.zumobi.android.zbimsampleapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.Color;
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

import org.xwalk.core.XWalkPreferences;

/**
 * The Launch activity that is parent of all other views in this project
 */
public class MainActivity extends Activity implements OnClickListener, OnCheckedChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    public enum ScreenMode {FULLSCREEN, FRAGMENT}

	private Button mShowContentHubButton;
    private Button mShowContentWidgetButton;
	private Button mCreateNewUserButton;
	private Button mSwitchUserButton;
    private Switch mThemeSwitch;
    static public ScreenMode mScreenMode = ScreenMode.FULLSCREEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // inflate UI
        setContentView(R.layout.activity_main);
        
        // get references to UI elements
        setupUIElements();

        // Enable xwalkview debugging on debug builds
        if (0 != ( getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE))
        {
            XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);
        }

        // initialize ZBiM
        ZBiM.getInstance(this);

        // Whitelist the scheme
        ZBiM.getInstance(this).whitelistScheme("zbimsampleapp");//required
        // ZBiM.getInstance(this).whitelistScheme("your_company_scheme");//optional additional schemes

        // Set Logger
        ZBiM.getInstance(this).setLoggingHandler(new ZBiMSDKLogger());
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
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Check for a current user
        String userId = ZBiM.getInstance(this).getActiveUser();
        if(userId == null){

            // create a random user ID without tags
            try {
                userId = ZBiM.getInstance(this).generateDefaultUserID();
                ZBiM.getInstance(this).createUser(userId, null);// by default, don't create tags
            }
            catch (UnsupportedOperationException uoe){
                Log.d(TAG, "Failed creating userId:" + userId);

                //disable button and grey it out without using a selector
                mShowContentHubButton.setEnabled(false);
                mShowContentHubButton.setAlpha(0.5f);
                return;
            }

            // set new user to active user
            try {
                ZBiM.getInstance(this).setActiveUser(userId);
            } catch (Exception e) {
                e.printStackTrace();

                //disable button and grey it out without using a selector
                mShowContentHubButton.setEnabled(false);
                mShowContentHubButton.setAlpha(0.5f);
                return;
            }

            // success !
            Toast.makeText(this, "No User existed. Automatically created one.", Toast.LENGTH_LONG).show();

        } else {
            // re-enable button in case there was an error before
            mShowContentHubButton.setEnabled(true);
            mShowContentHubButton.setAlpha(1.0f);
        }
    }


    @Override
    public void onClick(View view) {

        // required initialization for fragment or fullscreen content hubs
        if ((view == mShowContentWidgetButton) || (view == mShowContentHubButton)) {
            // REQUIRED: set the Color Scheme
            if (mThemeSwitch.isChecked()) {
                ZBiM.getInstance(this).setColorScheme(ZBiMColorScheme.Dark);
            }
            else {
                ZBiM.getInstance(this).setColorScheme(ZBiMColorScheme.Light);
            }

            // OPTIONAL: set the background color of the content Hub
            if (mThemeSwitch.isChecked()) {
                ZBiM.getInstance(this).setContentHubBackgroundColor(Color.BLACK);
            }
            else {
                ZBiM.getInstance(this).setContentHubBackgroundColor(Color.WHITE);
            }
        }


        if (view == mShowContentWidgetButton) {
            Intent intent = new Intent(this, ContentWidgetListActivity.class);
            startActivity(intent);
            //Note: the mScreenMode is tested from within ContentWidgetListActivity
            return;
        }


    	if (view == mShowContentHubButton) {

            if(mScreenMode == ScreenMode.FULLSCREEN) {
                try {
                    ZBiM.getInstance(this).launchContentHubActivity();
                } catch (ZBiMStateException ex) {

                    // Create the Dialog ...
                    AlertDialog.Builder alertbuilder = new AlertDialog.Builder(this);
                    alertbuilder.setTitle(ex.getUserTitle());
                    alertbuilder.setMessage(ex.getUserMessage());

                    // Get app icon ID
                    try {
                        final String packageName = getPackageName();
                        PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, 0);
                        final int iconid = packageInfo.applicationInfo.icon;
                        alertbuilder.setIcon(iconid);
                    } catch (android.content.pm.PackageManager.NameNotFoundException e) {
                        Log.d(TAG, "Failed to obtain packageName and packageInfo");
                    }

                    alertbuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    alertbuilder.show();

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
                startActivity(intent);
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

        } else if (checkedId == R.id.button_two) {
            mScreenMode = ScreenMode.FRAGMENT;

        }
    }
}
