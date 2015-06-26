package com.zumobi.android.zbimsampleapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.zumobi.zbim.ZBiM;

import java.util.ArrayList;


public class NewUserActivity extends Activity implements View.OnClickListener {

    private static final String TAG = NewUserActivity.class.getSimpleName();

    private EditText mUserIdTextView;
    private Switch mBusinessSwitch;
    private Switch mHolidaysSwitch;
    private Switch mFamilySwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        Button createNewUserButton;
        createNewUserButton = (Button)findViewById(R.id.buttonCreateNewUser);
        if (createNewUserButton != null) {
            createNewUserButton.setOnClickListener(this);
        }

        mUserIdTextView = (EditText)findViewById(R.id.textViewUserId);
        mUserIdTextView.setText(ZBiM.getInstance(this).generateDefaultUserID());

        // Setup switches
        mBusinessSwitch = (Switch)findViewById(R.id.switchBusiness);
        mHolidaysSwitch = (Switch)findViewById(R.id.switchHolidays);
        mFamilySwitch = (Switch)findViewById(R.id.switchFamily);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        String userId = mUserIdTextView.getText().toString();
        if(userId.length() == 0) {
            //TODO Show error message
            return;
        }

        ArrayList<String> tags = new ArrayList<String>(3);
        if(mBusinessSwitch.isChecked()){
            tags.add(mBusinessSwitch.getText().toString());
        }
        if(mHolidaysSwitch.isChecked()){
            tags.add(mHolidaysSwitch.getText().toString());
        }
        if(mFamilySwitch.isChecked()){
            tags.add(mFamilySwitch.getText().toString());
        }

        try {
            ZBiM.getInstance(this).createUser(userId, tags.toArray(new String[tags.size()]));
        }
        catch (UnsupportedOperationException uoe){
            Log.d(TAG, "Failed creating userId:" + userId + " with tags: " + tags.toString());
            return;
        }

        try {
            ZBiM.getInstance(this).setActiveUser(userId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.finish();

    }
}
