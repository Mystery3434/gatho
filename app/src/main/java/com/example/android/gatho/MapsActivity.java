package com.example.android.gatho;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private static final float DEFAULT_ZOOM = 15f;


    private static final String TAG = "MapsActivity";

    //vars
    private GoogleMap mMap;
    private AppPrefsDao prefs;
    private String event_type="",dateString="";
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private RelativeLayout mapWrapper;
    private LinearLayout categories_wrapper;
    private FloatingActionButton floatingActionButton,refreshMap,goLocation;
    private Button createEvent;
    private BottomSheetBehavior bottom_slider;
    private ImageView location_pointer,sportsButton,artButton,chatButton,musicButton,foodButton,gamesButton;
    private EditText new_event_title,new_event_description;
    private TextView categoryText,new_event_time;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        initVars();

        initMap();


        //Making the category buttons clickable
        sportsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_event("sport");

            }
        });
        artButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_event("art");
            }
        });
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_event("chat");
            }
        });
        musicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_event("music");
            }
        });
        gamesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_event("game");
            }
        });
        foodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_event("food");

            }
        });



        bottom_slider.setPeekHeight(Math.round(convertDpToPixel(88.0f,this)));
        bottom_slider.setState(BottomSheetBehavior.STATE_HIDDEN);


        //new_event_time.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_av_timer_black_24dp,0,0,0);


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                floatingActionButton.hide();
                bottom_slider.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        prefs = AppDatabase.getInstance(getApplicationContext()).prefsDao();

        bottom_slider.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                ViewGroup.LayoutParams mapBox = mapWrapper.getLayoutParams();
                switch (newState){
                    case BottomSheetBehavior.STATE_HIDDEN:
                        floatingActionButton.show();
                        location_pointer.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        location_pointer.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        location_pointer.setVisibility(View.VISIBLE);
                        mapBox.height = bottomSheet.getTop()+Math.round(convertDpToPixel(30.0f,getApplicationContext()));
                        mapWrapper.setLayoutParams(mapBox);
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        mapBox = mapWrapper.getLayoutParams();
                        mapBox.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        mapWrapper.setLayoutParams(mapBox);

                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        new_event_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTime();
            }
        });

        createEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Event newEvent = new Event();

                if (new_event_title.getText().toString().length()==0 ||
                    new_event_description.getText().toString().length()==0){
                    Toast.makeText(getApplicationContext(),"Fill all the fields",Toast.LENGTH_SHORT).show();
                    return;
                }

                newEvent.setDate(dateString);
                newEvent.setChat(true);
                newEvent.setCreated_at("");
                newEvent.setCategory(event_type);
                newEvent.setUser_id(Integer.parseInt(prefs.getByKey("user_id").getValue()));
                newEvent.setTitle(new_event_title.getText().toString()+" User("+prefs.getByKey("name").getValue()+")");
                newEvent.setDescription(new_event_description.getText().toString());
                newEvent.setLocation(mMap.getCameraPosition().target);



                Log.v("Event CREATION",mMap.getCameraPosition().target.toString()+" niow title "+new_event_title.getText().toString());

                (AppDatabase.getInstance(getApplicationContext()).eventDao()).insert(newEvent);

                ServerApi servA = new ServerApi(getApplicationContext());

                servA.newEvent(newEvent);

                bottom_slider.setState(BottomSheetBehavior.STATE_HIDDEN);
                new_event_title.setText("");
                new_event_description.setText("");
                new_event_time.setText("Now");
                categoryText.setText("Category");
                event_type="";
                dateString="";

                refreshMap();
            }
        });

        refreshMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshMap();
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private void select_event(String event_type) {
        this.event_type=event_type;
        switch (event_type){
            case "sport":
                categoryText.setText("Sports");
                break;
            case "art":
                categoryText.setText("Art");

                break;
            case "food":
                categoryText.setText("Food");

                break;
            case "music":
                categoryText.setText("Music");

                break;
            case "chat":
                categoryText.setText("Chat");

                break;
            case "game":
                categoryText.setText("Games");
                break;


        }
        bottom_slider.setState(BottomSheetBehavior.STATE_EXPANDED);

    }

    private void initVars() {
        sportsButton = (ImageButton) findViewById(R.id.new_event_category_sport);
        artButton = (ImageButton) findViewById(R.id.new_event_category_art);
        musicButton = (ImageButton) findViewById(R.id.new_event_category_music);
        gamesButton = (ImageButton) findViewById(R.id.new_event_category_games);
        chatButton = (ImageButton) findViewById(R.id.new_event_category_chat);
        foodButton = (ImageButton) findViewById(R.id.new_event_category_food);
        categoryText = findViewById(R.id.new_event_category_display);


        floatingActionButton = findViewById(R.id.create_event_button);
        refreshMap = findViewById(R.id.refresh_map);
        goLocation = findViewById(R.id.go_to_location);

        bottom_slider =  BottomSheetBehavior.from(findViewById(R.id.bottom_map_slider));
        location_pointer = findViewById(R.id.location_pointer);
        mapWrapper = findViewById(R.id.map_wrapper);

        createEvent = (Button) findViewById(R.id.new_event_confirm);
        new_event_time = (TextView) findViewById(R.id.new_event_time);
        new_event_title = findViewById(R.id.new_event_title);
        new_event_description = findViewById(R.id.new_event_description);

        categories_wrapper = findViewById(R.id.new_event_category);
    }

    /**
     * Function to obtain the user's current location
     */
    private void getDeviceLocation() {
        Log.d(TAG, "Getting user's current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            final Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: found location!");
                        Location currentLocation = (Location) task.getResult();
                        if (currentLocation != null)
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                            DEFAULT_ZOOM);

                    } else {
                        Log.d(TAG, "onComplete: current location is null");
                        Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }


    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();
            add = add + "\n" + obj.getPremises();

            Log.v("IGA", "Address" + add);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }





    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        getDeviceLocation();

        refreshMap();
        try {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        } catch (SecurityException var3) {
            //TODO: Handle exception when the user does not give the app location access.
        }


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(final LatLng latLng) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                floatingActionButton.hide();
                bottom_slider.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
            drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void refreshMap(){
        final MapEvent mapify = new MapEvent(mMap);

        new MapEvent(this,new OnEventListener<EventDao>(){
            @Override
            public void onSuccess(EventDao events) {
                Log.v("MAPS","Populating pins");
                for (Event event : events.getAll()) {
                    mapify.addEvent(event);
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

        mMap.clear();

    }


    private void getTime() {
        final String[] dateAndTime = new String[2];
        DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

            // when dialog box is closed, below method will be called.
            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                String year1 = String.valueOf(selectedYear);
                String month1 = String.valueOf(selectedMonth + 1);
                String day1 = String.valueOf(selectedDay);
                dateAndTime[0] = day1 + "/" + month1 + "/" + year1;


                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(MapsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        dateAndTime[1] =  selectedHour + ":" + selectedMinute;
                        String prettyPrint = dateAndTime[0] + " at " + dateAndTime[1];
                        new_event_time.setText(prettyPrint);

                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        };
        final Calendar cal = Calendar.getInstance();
        final DatePickerDialog datePicker = new DatePickerDialog(MapsActivity.this,  R.style.Theme_AppCompat_DayNight_Dialog,
            datePickerListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH));
        datePicker.setCancelable(false);
        datePicker.setTitle("Select the date");
        datePicker.show();
    }


    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

}
