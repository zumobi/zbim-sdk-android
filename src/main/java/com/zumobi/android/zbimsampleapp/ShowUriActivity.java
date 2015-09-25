package com.zumobi.android.zbimsampleapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.zumobi.zbim.ZBiM;
import com.zumobi.zbim.exceptions.ZBiMStateException;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;


/**
 * An Activity that is intended for developers
 * It shows all the entry points into the content hub
 */
public class ShowUriActivity extends Activity {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ShowUriActivity.class);

    private ArrayList<String> mArrayListUris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_uri);

        // Initialization
        ArrayList<String> arrayListTitles = new ArrayList<String>();
        mArrayListUris = new ArrayList<String>();

        // get list of URI's from DB
        // 1) try to open the DB
        String existingChecksum = readFromSharedPreferences(this, "dbChecksum");
        String existingDBFilename = existingChecksum != null ? generateDBFilenameFromChecksum(existingChecksum) : null;
        String existingDBFullPath = existingDBFilename != null ? generateDBFullPathFromFilename(this, existingDBFilename) : null;

        SQLiteDatabase database = SQLiteDatabase.openDatabase(existingDBFullPath, null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);

        // 2) collect the data
        String sqlQuery = "SELECT title, uri FROM resources WHERE (resource_type='hub' OR resource_type='article' OR resource_type='channel') AND length(content) > 0 AND length(title) > 0 ORDER BY position ASC";
        Cursor cursor = database.rawQuery(sqlQuery, null);

        while (cursor.moveToNext()) {
            // title can be null - skip it if it is
            final String strTitle = cursor.getString(0);
            if (strTitle != null) {
                arrayListTitles.add(strTitle);
                mArrayListUris.add(cursor.getString(1));
            }
        }
        cursor.close();

        // 3) Adapter to display the data
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, arrayListTitles);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        // 4) item click listener to launch the content hub
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String strUri = mArrayListUris.get(position);
                try {
                    ZBiM.launchContentHubActivity(strUri);
                } catch (ZBiMStateException ex) {
                    Toast.makeText(getApplicationContext(), "Error Launching Contenthub - see logcat", Toast.LENGTH_LONG).show();
                    logger.error("Exception {}", ex.getMessage());
                    ex.printStackTrace();
                }

            }
        });
    }


    /**
     * Utility method readFromSharedPreferences()
     * @param context Context
     * @param key String
     * @return sharedPreferences.getString(key)
     */
    public String readFromSharedPreferences(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("com.zumobi.zbim", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    /**
     * Utility method generateDBFilenameFromChecksum()
     * @param checksum String
     * @return checksum + ".db"
     */
    public String generateDBFilenameFromChecksum(String checksum) {
        return checksum + ".db";
    }

    /**
     * Utility method generateDBFullPathFromFilename()
     * @param context Context
     * @param filename String
     * @return getFilesDir() + "/" + filename
     */
    public String generateDBFullPathFromFilename(Context context, String filename) {
        return context.getFilesDir() + "/" + filename;
    }

}
