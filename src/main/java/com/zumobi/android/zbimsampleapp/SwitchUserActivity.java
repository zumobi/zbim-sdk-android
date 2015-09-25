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
        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ZBiM.getUserIDs());
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

        ZBiM.setActiveUser(strUserID);
        Toast.makeText(this, "Switched to user "+strUserID, Toast.LENGTH_LONG).show();
    }
}
