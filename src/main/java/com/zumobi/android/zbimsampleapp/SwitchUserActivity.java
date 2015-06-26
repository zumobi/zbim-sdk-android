package com.zumobi.android.zbimsampleapp;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.zumobi.zbim.ZBiM;
import com.zumobi.zbim.exceptions.ZBiMStateException;

import java.util.ArrayList;

public class SwitchUserActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Button mCloseButton;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inflate UI
        setContentView(R.layout.activity_switch_user);

        // get references to UI views
        mCloseButton = (Button) findViewById(R.id.switch_user_close);
        mListView = (ListView) findViewById(R.id.switch_user_listView);

        // events
        mCloseButton.setOnClickListener(this);

        // collect data and put it in listview
        ArrayList<String> useridarray = ZBiM.getInstance(this).getUserIDs();
        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, useridarray);
        mListView.setAdapter(itemsAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.switch_user_close:
                finish();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final String strUserID = (String) parent.getItemAtPosition(position);

        try {
            ZBiM.getInstance(this).setActiveUser(strUserID);
            Toast.makeText(this, "Switched to user "+strUserID, Toast.LENGTH_LONG).show();
        } catch (ZBiMStateException e) {
            Log.d("SwitchUserActivity", e.toString());
            Toast.makeText(this, "Failed to switch to user "+strUserID, Toast.LENGTH_LONG).show();
        }


    }
}
