package com.ankush.wheatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IInterface;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    // Constants:
    final  int REquest_code=123;
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?q=London,uk&APPID=39baad228ad31f4a2b60e47c5bd64111dd222";
    // App ID to use OpenWeather data
    final String APP_ID = "39baad228ad31f4a2b60e47c5bd64111dd222";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    // Set LOCATION_PROVIDER here:
    String Location_provider = LocationManager.GPS_PROVIDER;


    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    //Declare a LocationManager and a LocationListener here:
    LocationManager mLocationManager;
    LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = findViewById(R.id.locationTV);
        mWeatherImage = findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = findViewById(R.id.tempTV);
        ImageButton changeCity=findViewById(R.id.changeCityButton);


        //  Add an OnClickListener to the changeCityButton here:
changeCity.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Intent my_internt=new Intent(MainActivity.this,ChangeCityController.class);
        startActivity(my_internt);
    }
});
    }


    //  Add onResume() here:
    protected void onResume() {

        super.onResume();
        Log.d("clima", "onresume() called");
        Log.d("clima", "getting weather for current location");
        Intent my_intent=getIntent();
        String city=my_intent.getStringExtra("City");
        if (city!=null)
        {
            getweatherforcityname(city);
        }
        else {
            getWeatherlocation();
        }
    }


    // Add getWeatherForNewCity(String city) here:
private void getweatherforcityname(String city)
{
RequestParams requestParams=new RequestParams();
requestParams.put("q",city);
requestParams.put("appid",APP_ID);
letsDoSomenetworking(requestParams);
}

    //  Add getWeatherForCurrentLocation() here:
    private void getWeatherlocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                Log.d("clima", "onlocationchanged recieved");
               String Longitude= String.valueOf(location.getLongitude());
               String latitude=String.valueOf(location.getLatitude());
               Log.d("clima","longitude "+Longitude);
               Log.d("clima","latitude "+latitude);
                RequestParams params=new RequestParams();
                params.put("lat",latitude);
                params.put("lon",Longitude);
                params.put("appid",APP_ID);
                letsDoSomenetworking(params);


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("clima", "onproviderDisabled");
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //  Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REquest_code);
            return;
        }
        mLocationManager.requestLocationUpdates(Location_provider, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==REquest_code)
        {
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Log.d("clima","onrequestpermisiionResult(): permission granted");
                getWeatherlocation();
            }
            else{
                Log.d("clima","permission denied");
            }
        }
    }
// Add letsDoSomeNetworking(RequestParams params) here:

private void letsDoSomenetworking(RequestParams params)
{
    AsyncHttpClient client=new AsyncHttpClient();
    client .get(WEATHER_URL,params,new JsonHttpResponseHandler(){
        @Override
        public  void  onSuccess(int statuscode, Header[] headers, JSONObject response){
        Log.d("clima","success JSON "+response.toString());

            WeatherDataModel weatherData=WeatherDataModel.fromJson(response);

            updateUI(weatherData);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            Log.e("clima","fail"+throwable.toString());
            Log.d("clima","statuscode"+statusCode);
            Toast.makeText(MainActivity.this,"request failed",Toast.LENGTH_LONG).show();

        }


    });
}

    // Add updateUI() here:
private void updateUI(WeatherDataModel weather)
{
   mTemperatureLabel.setText(weather.getTemperature());
   mCityLabel.setText(weather.getCity());

   int resourceID=getResources().getIdentifier(weather.getIconName(),"drawable",getPackageName());

   mWeatherImage.setImageResource(resourceID);


}


    // Add onPause() here:


    @Override
    protected void onPause() {
        super.onPause();
        if (mLocationManager!=null){
            mLocationManager.removeUpdates(mLocationListener);
        }
    }
}
