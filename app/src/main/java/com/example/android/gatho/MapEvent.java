package com.example.android.gatho;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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

public class MapEvent {

    private static final String LOG_TAG = "GOTHY_LOG";
    private static final String EVENTS_URL = "http://91.187.102.181:2208/event/all";

    private MarkerOptions markerOptions;
    private GoogleMap mMap;

    //Temporarily using global variables to add an info window to the pins
    //Will add a more elegant solution after the MVP phase.
    static String description;
    static String title;




    public MapEvent(GoogleMap mMap) {
        this.mMap = mMap;
        markerOptions = new MarkerOptions();
        mMap.setOnMarkerClickListener(new handleMarkerClick());
    }

    public MapEvent(Context context,OnEventListener<EventDao> callback){
        Log.v("ASYNC","dbAsync exec");

        if (isNetworkAvailable(context)) {
            LoadDataAsync loadData = new LoadDataAsync(context,callback);
            loadData.execute();
        }else {
            Toast.makeText(context.getApplicationContext(),"You're offline.\nCouldn't fetch pins.",Toast.LENGTH_SHORT).show();
        }


    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
            = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void addEvent(Event event){
        Log.v("MapEvent",""+event.getLocation().toString());
        markerOptions.position(event.getLocation());
        markerOptions.title(event.getTitle());
        String event_category = event.getCategory();

        Log.v("MAPEVNET",event_category);
        switch (event_category){
            case "sport":
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_sport));
                break;
            case "art":
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_art));
                break;
            case "food":
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_food));

                break;
            case "music":
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_music));

                break;
            case "chat":
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_chat));

                break;
            case "game":
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_game));
                break;
        }
        markerOptions.snippet(event.getDescription());
        Marker eventMarker = mMap.addMarker(markerOptions);
        eventMarker.setTag(event);
    }

    class handleMarkerClick implements GoogleMap.OnMarkerClickListener {
        @Override
        public boolean onMarkerClick(Marker marker) {
            if (marker.getTag() != null && marker.getTag() instanceof Event) {
                Event event = (Event) marker.getTag();
                Log.v("Event Clicked", event.getTitle() + "  " + event.getDate());
            }
            return false;
        }
    }




    private static class LoadDataAsync extends AsyncTask<URL, Void, EventDao> {

        private EventDao events_db;
        private Context ctx;
        private OnEventListener<EventDao> mCallBack;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(ctx,"Syncing with Server...",Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(EventDao aBoolean) {
            super.onPostExecute(aBoolean);
            mCallBack.onSuccess(aBoolean);
        }

        public LoadDataAsync(Context ctx,OnEventListener<EventDao> callback) {
            this.mCallBack = callback;
            this.ctx = ctx;
        }

        /**
         * Returns new URL object from the given string URL.
         */
        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }


        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            if (url == null)
                return jsonResponse;

            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setReadTimeout(10000 /* milliseconds */);
                    urlConnection.setConnectTimeout(15000 /* milliseconds */);
                    urlConnection.connect();

                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else
                    Log.e(LOG_TAG, "URL Response Code: " + urlConnection.getResponseCode());
            } catch (IOException e) {
                // TODO: Handle the exception
                Log.e(LOG_TAG, "Failed to receive data from the requested source.", e);
                jsonResponse = "";
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }


        /**
         * Convert the {@link InputStream} into a String which contains the
         * whole JSON response from the server.
         */
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }


        /**
         * Return an {@link Event} object by parsing out information
         * about the first earthquake from the input earthquakeJSON string.
         */

        private boolean jsonToDb(String eventJSON) {

            if (TextUtils.isEmpty(eventJSON))
                return false;
            try {
                JSONObject baseJsonResponse = new JSONObject(eventJSON);
                JSONArray dataArray = baseJsonResponse.getJSONArray("data");

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject dataObject = dataArray.getJSONObject(i);

                    // If there are results in the features array
                    if (dataObject != null) {
                        /*
                         Extract out the title, description and location coordinates from the JSON object
                         */

                        int sID = dataObject.getInt("id");
                        int user_ID = dataObject.getInt("user_id");
                        title = dataObject.getString("title");
                        String category = dataObject.getString("category");
                        boolean chat = dataObject.getBoolean("chat_enabled");
                        JSONObject locationObject = dataObject.getJSONObject("location");
                        JSONObject geomObject = locationObject.getJSONObject("geom");
                        JSONArray coordinateArray = geomObject.getJSONArray("coordinates");
                        String date = dataObject.getString("date");
                        String createdAtDate = (dataObject.getJSONObject("created_at")).getString("date");
                        double latitude = coordinateArray.getDouble(0);
                        double longitude = coordinateArray.getDouble(1);
                        description = dataObject.getString("description");
                        LatLng location = new LatLng(latitude, longitude);


                        // Create a new {@link Event} object
                        this.events_db.insert(new Event(sID, title,category,description, date, user_ID, chat, createdAtDate, location));
                    }
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the JSON results", e);
                return false;
            }
            return true;

        }


        @Override
        protected EventDao doInBackground(URL... urls) {

            events_db = AppDatabase.getInstance(ctx).eventDao();

            events_db.deleteAll();
            // Create URL object
            URL url = createUrl(EVENTS_URL);



            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                // TODO Handle the IOException
                Log.e(LOG_TAG, "Failed to make HTTP request", e);
            }

            // Extract relevant fields from the JSON response and create a list of Event objects
            jsonToDb(jsonResponse);


            // Return the list of Event objects
            return events_db;
        }


    }
}
