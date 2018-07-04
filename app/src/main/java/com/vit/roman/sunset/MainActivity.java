package com.vit.roman.sunset;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private final String URL = "https://api.sunrise-sunset.org/";
    private FusedLocationProviderClient mFusedLocationClient;
    private String TAG = "MainActivity";
    private TextView mTextView;
    private TextView mTextViewInfo;
    private Button mButton;
    private Button mLocationButton;
    private float mLatitude;
    private float mLongtitide;

    private Gson gson = new GsonBuilder().create();

    private Retrofit mRetrofit = new Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(URL)
            .build();

    private Service intrf = mRetrofit.create(Service.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.text_view);
        mButton = findViewById(R.id.button);
        mLocationButton = findViewById(R.id.button_location);
        mTextViewInfo = findViewById(R.id.text_view_info);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        mButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (mLatitude != 0 || mLongtitide != 0) {

                    Call<JsonObject> call = intrf.getSunset(mLatitude, mLongtitide);

                    try {
                        Response<JsonObject> response = call.execute();

                        String in = response.body().toString();
                        JSONObject jsonObj = new JSONObject(in);
                        String results = jsonObj.getString("results");
                        JSONObject resultsJson = new JSONObject(results);
                        String sunset = resultsJson.getString("sunset");
                        String sunrise = resultsJson.getString("sunrise");
                        mTextViewInfo.setText("Sunset at: " + sunset + " \n Sunrise at: " + sunrise);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        getLastLocation();

        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLastLocation();
            }
        });



        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
                String resultText = "Your location: " + place.getName();
                mLatitude = (float) place.getLatLng().latitude;
                mLongtitide = (float) place.getLatLng().longitude;
                mTextView.setText(resultText);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 123);
        } else {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                Toast.makeText(getApplicationContext(), "Your location has been found", Toast.LENGTH_SHORT).show();
                                Double latitude = location.getLatitude();
                                Double longtitude = location.getLongitude();
                                //String resultText = "Your location: " + String.valueOf(latitude) + String.valueOf(longtitude);
                                mLatitude = latitude.floatValue();
                                mLongtitide = longtitude.floatValue();


                                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                                List<Address> addresses = null;
                                try {
                                    addresses = geocoder.getFromLocation(mLatitude, mLongtitide, 1);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                assert addresses != null;
                                String cityName = addresses.get(0).getAddressLine(0);
                                String stateName = addresses.get(0).getAddressLine(1);
                                String countryName = addresses.get(0).getAddressLine(2);

                                String resultText = "Your location: " + cityName;
                                mTextView.setText(resultText);
                            }
                        }
                    });
        }
    }
}





