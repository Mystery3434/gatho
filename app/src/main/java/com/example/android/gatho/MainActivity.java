package com.example.android.gatho;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.AutoTransition;
import android.transition.Slide;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private Boolean mLocationPermissionsGranted,callonTrue;
    private  ImageButton getStartedButton;
    private Button registerButton;
    private TextView register_name,register_email;

    private LinearLayout register_form;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initVars();

        mLocationPermissionsGranted = false;

        getLocationPermission();

        final AppPrefsDao pref = AppDatabase.getInstance(this).prefsDao();


        if (pref.getByKey("registered") != null && pref.getByKey("registered").getValue().equals("yes")){

            getStartedButton.setVisibility(View.VISIBLE);

        }else{
            register_form.setVisibility(View.VISIBLE);
                registerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String email = register_email.getText().toString();
                        String name = register_name.getText().toString();

                        if(email.contains("@") && name.trim() != ""){

                            pref.insert(new AppPrefs("registered","yes"));
                            pref.insert(new AppPrefs("email",email));
                            pref.insert(new AppPrefs("name",name));

                            register_form.setVisibility(View.GONE);

                            register_email.clearFocus();

                            (new ServerApi(getApplicationContext())).processUser(name,email);

                            Toast.makeText(getApplicationContext(),"Thank you for registering!"+name, Toast.LENGTH_SHORT).show();
                            if(mLocationPermissionsGranted)
                                getStartedButton.setVisibility(View.VISIBLE);
                            else {
                                Toast.makeText(getApplicationContext(), "Please wait!", Toast.LENGTH_SHORT).show();
                                callonTrue = true;
                                Intent loginIntent = new Intent(MainActivity.this, MapsActivity.class);
                                startActivity(loginIntent);
                                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                            }
                        }else{
                            Toast.makeText(getApplicationContext(),"Fill the fields correctly!", Toast.LENGTH_SHORT).show();

                        }


                    }
                });

        }



        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mLocationPermissionsGranted) {
                    Intent loginIntent = new Intent(MainActivity.this, MapsActivity.class);
                    startActivity(loginIntent);
                    overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                }

            }
        });
    }

    private void initVars() {
        register_form = (LinearLayout) findViewById(R.id.register_view);
        getStartedButton = (ImageButton) findViewById(R.id.get_started_button);

        registerButton = (Button) findViewById(R.id.register_button);
        register_name = (TextView) findViewById(R.id.register_name);
        register_email = (TextView) findViewById(R.id.register_email);
    }

    private void getLocationPermission() {
            String[] permissions = {FINE_LOCATION,COURSE_LOCATION};

            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionsGranted=true;
                } else {
                    Toast.makeText(this,"Please grant location permission",Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
                }
            } else {
                ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                }
            }
        }
        if(callonTrue){
            getStartedButton.setVisibility(View.VISIBLE);
        }
    }

    private static class InitDatabaseAsync extends AsyncTask<Context, Void, AppDatabase> {

        private Context ctx;

        public InitDatabaseAsync(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected AppDatabase doInBackground(Context... contexts) {
            AppDatabase.getInstance(ctx);
            return null;
        }

        @Override
        protected void onPostExecute(AppDatabase appDatabase) {
            super.onPostExecute(appDatabase);
            Log.v("ASYNC","Inited database");
        }

    }
}
