package com.example.android.gatho;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

@Database(entities = {Event.class,AppPrefs.class}, version = 2)
@TypeConverters({Converters.class})

public abstract class AppDatabase extends RoomDatabase{

    private static final String DB_NAME = "gothy-alpha-rc.sql";
    private static AppDatabase instance;

    static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {

            instance = create(context.getApplicationContext());
        }
        return instance;
    }

    private static AppDatabase create(Context context) {
        return Room.databaseBuilder(
            context,
            AppDatabase.class,
            DB_NAME).allowMainThreadQueries().build();
    }

    private boolean hasInstance(){
        return (instance == null) ? false : true;
    }

    public abstract EventDao eventDao();

    public abstract AppPrefsDao prefsDao();

}
