package com.example.android.gatho;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class ServerApi {
    private final String server_API_event = "http://91.187.102.181:2208/event/new";
    private final String server_API_user = "http://91.187.102.181:2208/userG/new";
    private Context context;

    public ServerApi(Context context) {
        this.context = context;
    }


    public void newEvent(Event event){
        AppPrefsDao prefs = AppDatabase.getInstance(context).prefsDao();

        if (prefs.getByKey("user_id")  != null){
            ServerReqEventAsync reqAsync = new ServerReqEventAsync(event,server_API_event,context);
            reqAsync.execute();
        }

    }

    public void processUser(String name,String email){
        (new ServerReqUserAsync(server_API_user,name,email,context)).execute();
    }


    private static class ServerReqEventAsync extends AsyncTask<URL, Void, Integer> {

        private String req_url;
        private Event newEvent;
        private Context context;

        public ServerReqEventAsync(Event event,String req_url,Context context) {
            this.req_url = req_url;
            this.context = context;
            this.newEvent = event;
        }

        /**
         * Returns new URL object from the given string URL.
         */
        private URL createUrl() {
            URL url = null;
            try {
                url = new URL(this.req_url);
            } catch (MalformedURLException exception) {
                return null;
            }
            return url;
        }


        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private Integer makeHttpRequest(URL url) throws IOException {

            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();

                JSONObject eventObj = new JSONObject();

                eventObj.put("title",newEvent.getTitle());
                eventObj.put("description",newEvent.getDescription());
                eventObj.put("lat",newEvent.getLocation().latitude);
                eventObj.put("category",newEvent.getCategory());
                eventObj.put("lng",newEvent.getLocation().longitude);
                eventObj.put("time",newEvent.getDate());
                eventObj.put("user_id",newEvent.getUser_id());

                OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
                Log.v("ServerApi: ", eventObj.toString());
                wr.write(eventObj.toString());
                wr.flush();
                int HttpResult = urlConnection.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {

                    String output = convertInputStreamToString(urlConnection.getInputStream());

                    Log.v("ServerApiRes ;" , output);

                    return Integer.parseInt(output);
                }


            } catch (IOException e) {
                // TODO: Handle the exception
             //   Log.e(LOG_TAG, "Failed to receive data from the requested source.", e);
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return 0;
        }


        /**
         * Convert the {@link InputStream} into a String which contains the
         * whole JSON response from the server.
         */
        private String convertInputStreamToString(InputStream inputStream) throws IOException {
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




        @Override
        protected Integer doInBackground(URL... urls) {
            // Create URL object
            URL url = createUrl();

            // Perform HTTP request to the URL and receive a JSON response back
            Integer event_id = 0;
            try {
                event_id = makeHttpRequest(url);
            } catch (IOException e) {
                // TODO Handle the IOException
                Log.e("ServerAPI", "Failed to make HTTP request", e);
            }


            return event_id;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            EventDao events = AppDatabase.getInstance(context).eventDao();
            newEvent.setSId(integer);
            events.updateSid(newEvent.getId(),integer);
        }
    }

    private static class ServerReqUserAsync extends AsyncTask<URL, Void, Integer> {

        private String user_name,user_email,req_url;
        private Context context;

        public ServerReqUserAsync(String url,String name,String email,Context ctx) {
            this.user_name = name;
            this.user_email = email;
            this.req_url = url;
            this.context = ctx;
        }

        /**
         * Returns new URL object from the given string URL.
         */
        private URL createUrl() {
            URL url = null;
            try {
                url = new URL(this.req_url);
            } catch (MalformedURLException exception) {
                return null;
            }
            return url;
        }


        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private Integer makeHttpRequest(URL url) throws IOException {

            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();

                JSONObject userObj = new JSONObject();

                userObj.put("name",user_name);
                userObj.put("email",user_email);

                OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
                Log.v("ServerApi: ", userObj.toString());
                wr.write(userObj.toString());
                wr.flush();
                int HttpResult = urlConnection.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {

                    String output = convertInputStreamToString(urlConnection.getInputStream());

                    Log.v("ServerApiRes ;" , output);

                    return Integer.parseInt(output);
                }


            } catch (IOException e) {
                // TODO: Handle the exception
                //   Log.e(LOG_TAG, "Failed to receive data from the requested source.", e);
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return 0;
        }


        /**
         * Convert the {@link InputStream} into a String which contains the
         * whole JSON response from the server.
         */
        private String convertInputStreamToString(InputStream inputStream) throws IOException {
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




        @Override
        protected Integer doInBackground(URL... urls) {
            // Create URL object
            URL url = createUrl();

            // Perform HTTP request to the URL and receive a JSON response back
            Integer event_id = 0;
            try {
                event_id = makeHttpRequest(url);
            } catch (IOException e) {
                // TODO Handle the IOException
                Log.e("ServerAPI", "Failed to make HTTP request", e);
            }


            return event_id;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            AppPrefsDao pref = AppDatabase.getInstance(context).prefsDao();
            pref.insert(new AppPrefs("user_id",integer.toString()));
        }
    }
}
