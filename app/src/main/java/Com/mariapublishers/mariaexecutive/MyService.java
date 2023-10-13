package Com.mariapublishers.mariaexecutive;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static Com.mariapublishers.mariaexecutive.App.channelId;

public class MyService extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    String TAG = "MyService";

    public static final int notify = 50000;  //interval between two services(Here Service run every 30 seconds)

    private Handler mHandler = new Handler();   //run on another Thread to avoid crash
    private Timer mTimer = null;    //timer handling

    LocationTrack locationTrack;

    Utilis utilis;
    static SharedPreferences mPrefs;
    UserInfo obj;

    public static boolean isServiceRunning;
    String str_address = "";
    String str_result = "";
    String str_message = "";

    double latitude = 0.0;
    double longitude = 0.0;
    GoogleApiClient gac;
    LocationRequest locationRequest;
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    @Override
    public void onCreate() {
        super.onCreate();
        utilis = new Utilis(MyService.this);
        mPrefs = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("MyObject", "");
        obj = gson.fromJson(json, UserInfo.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isServiceRunning = intent.getBooleanExtra("state", true);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Capturing Location")
                .setContentText("")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        callApiToUpdateLatLng();

        return START_NOT_STICKY;
    }

    private void callApiToUpdateLatLng() {
        // using thread operation service will hit continuously
        //timer handling
        mTimer = new Timer();   //recreate new
        mTimer.scheduleAtFixedRate(new TimeDisplay(), 0, notify);   //Schedule task
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (Utilis.isGpsOn()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.getLastLocation(gac);
                LocationServices.FusedLocationApi.requestLocationUpdates(gac, locationRequest, this);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
    }

    //class TimeDisplay for handling task
    class TimeDisplay extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            if (isServiceRunning) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (Utilis.isInternetOn()) {
                            if (Utilis.isGpsOn()) {
//                                locationTrack = new LocationTrack(getApplicationContext());
//                                System.out.println("service " + locationTrack.canGetLocation());
//                                if (locationTrack.canGetLocation()) {
//
//                                    double longitude = locationTrack.getLongitude();
//                                    double latitude = locationTrack.getLatitude();

//                                    Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
                                System.out.println("service latitude " + latitude + " longitude " + longitude);

                                locationRequest = new LocationRequest();
                                locationRequest.setInterval(UPDATE_INTERVAL);
                                locationRequest.setFastestInterval(FASTEST_INTERVAL);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                gac = new GoogleApiClient.Builder(MyService.this)
                                        .addConnectionCallbacks(MyService.this)
                                        .addOnConnectionFailedListener(MyService.this)
                                        .addApi(LocationServices.API)
                                        .build();
                                gac.connect();

                                if (latitude != 0 && longitude != 0) {
                                    getAddress(latitude, longitude);
                                }
//                                }
                            } else {
                                Toast.makeText(MyService.this, "Please turn on location service", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MyService.this, "Please turn on internet connection ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                System.out.println("Service is Stopped");
                mTimer.cancel();
            }
        }
    }

    private void getAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(MyService.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null) {
                Address myAddress = addresses.get(0);
                StringBuilder stringBuilder = new StringBuilder("");
                for (int i = 0; i <= myAddress.getMaxAddressLineIndex(); i++) {
                    if (i == 0)
                        stringBuilder.append(myAddress.getAddressLine(i));
                    else
                        stringBuilder.append("\n").append(myAddress.getAddressLine(i));
                }
                str_address = stringBuilder.toString();
                sendAddressApi(latitude, longitude, str_address);
            }
        } catch (Exception e) {
            System.out.println("getAddress service Exception " + e.getMessage());
        }
    }

    private void sendAddressApi(final double latitude, final double longitude, final String address) {
        if (Utilis.isInternetOn()) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.updateLatLng, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " updateLatLng response - " + response);

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " updateLatLng result " + str_result);

                        if (Integer.parseInt(str_result) == 0) {

                            str_message = obj.getString("Message");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Toast.makeText(MyService.this, MyService.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

                    if (error instanceof NoConnectionError) {
                        System.out.println("NoConnectionError");
                    } else if (error instanceof TimeoutError) {
                        System.out.println("TimeoutError");

                    } else if (error instanceof ServerError) {
                        System.out.println("ServerError");

                    } else if (error instanceof AuthFailureError) {
                        System.out.println("AuthFailureError");

                    } else if (error instanceof NetworkError) {
                        System.out.println("NetworkError");
                    }
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();

                    params.put("Latitude", String.valueOf(latitude));
                    params.put("Longitude", String.valueOf(longitude));
                    params.put("Address", address);
                    params.put("UserIndexId", obj.getIndexId());
                    System.out.println(TAG + " updateLatLng inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);

        } else {
            Toast.makeText(this, MyService.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }
}
