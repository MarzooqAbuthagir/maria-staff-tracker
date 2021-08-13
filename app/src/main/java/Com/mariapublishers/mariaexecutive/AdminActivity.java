package Com.mariapublishers.mariaexecutive;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AdminActivity extends AppCompatActivity implements OnMapReadyCallback,
        LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    String TAG = "AdminActivity";
    LocationTrack currentLocation;
    double latitude = 0.0;
    double longitude = 0.0;
    private static final int REQUEST_CODE = 101;

    LinearLayout myLocationLayout;

    public static final int notify = 10000;  //interval between two services(Here Service run every 10 seconds)
    private Handler mHandler = new Handler();   //run on another Thread to avoid crash

    Utilis utilis;
    static SharedPreferences mPrefs;
    UserInfo obj;
    Toolbar toolbar;
    int gpsAlertCount = 0;
    Dialog add_dialog;
    String str_result = "", str_message = "";
    ArrayList<LatLngTracker> listValue = new ArrayList<LatLngTracker>();
    GoogleMap mMap;
    Timer mTimer = null;
    public static boolean isTimerRunning = true;
    LinearLayout goHomeLayout;

    GoogleApiClient gac;
    LocationRequest locationRequest;
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    static final int REQUEST_CODE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        utilis = new Utilis(AdminActivity.this);
        mPrefs = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("MyObject", "");
        obj = gson.fromJson(json, UserInfo.class);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        fetchLastLocation();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(this);

        fetchLastLocation();

        myLocationLayout = findViewById(R.id.my_location_layout);
        myLocationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchLastLocation();
            }
        });

        goHomeLayout = findViewById(R.id.bottom_layout);
        goHomeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminActivity.this, MenuDashboardActivity.class));
                finish();
            }
        });
    }

    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    private void fetchLastLocation() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
//            return;
//        }
        System.out.println("AdminActivity running fetchLoc");
        if (Utilis.isGpsOn()) {
//            currentLocation = new LocationTrack(AdminActivity.this);
            gpsAlertCount = 1;
            if (add_dialog != null && add_dialog.isShowing()) {
                System.out.println("Dialog showing force to close");
                add_dialog.dismiss();
            }
//            if (currentLocation.canGetLocation()) {
//                System.out.println("MainActivity running inside");
//                longitude = currentLocation.getLongitude();
//                latitude = currentLocation.getLatitude();
//            }
//            try {
//                SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
//                supportMapFragment.getMapAsync(AdminActivity.this);
//            } catch (Exception e) {
//                System.out.println("onSuccess Excep " + e.getMessage());
//            }

            isGooglePlayServicesAvailable();
            locationRequest = new LocationRequest();
            locationRequest.setInterval(UPDATE_INTERVAL);
            locationRequest.setFastestInterval(FASTEST_INTERVAL);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            gac = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            gac.connect();

        } else {
            if (gpsAlertCount == 0) {
                gpsAlertCount = 1;


                add_dialog = new Dialog(AdminActivity.this, android.R.style.Theme_Translucent_NoTitleBar);

                add_dialog.setCancelable(false);
                add_dialog.getWindow().setContentView(R.layout.gps_alert_dialog);
                add_dialog.show();

                Button yesBtn = add_dialog.findViewById(R.id.yesbtn);
                Button noBtn = add_dialog.findViewById(R.id.nobtn);

                yesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        gpsAlertCount = 0;
                        add_dialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, REQUEST_CODE);
                    }
                });

                noBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        gpsAlertCount = 0;
                        add_dialog.cancel();
                    }
                });

            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Permission Granted");
                fetchLastLocation();
            }
        } else if (requestCode == REQUEST_CODE_LOCATION) {
            fetchLastLocation();
        }
     }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE || requestCode == REQUEST_CODE_LOCATION) {
            fetchLastLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isTimerRunning = true;
        mTimer = new Timer();   //recreate new
        mTimer.scheduleAtFixedRate(new TimeDisplay(), 0, notify);   //Schedule task
    }

    private void toLogoutCustomer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this);

        builder.setTitle(AdminActivity.this.getResources().getString(R.string.logout_title))
                .setMessage(AdminActivity.this.getResources().getString(R.string.logout_msg))
                .setPositiveButton(AdminActivity.this.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog
                        dialog.dismiss();
                        isTimerRunning = false;
                        MenuDashboardActivity.isDashTimerRunning = false;

                        LoginSharedPreference.setLoggedIn(AdminActivity.this, false);

                        SharedPreferences preferences = getSharedPreferences("MY_SHARED_PREF", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.apply();

                        startActivity(new Intent(AdminActivity.this, LoginActivity.class));
                        finish();
                    }
                })
                .setNegativeButton(AdminActivity.this.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();

        Button btn_yes = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        Button btn_no = alert.getButton(DialogInterface.BUTTON_NEGATIVE);

        btn_no.setTextColor(Color.parseColor("#000000"));
        btn_yes.setTextColor(Color.parseColor("#000000"));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (Utilis.isGpsOn()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AdminActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_LOCATION);
                return;
            }
            LocationServices.FusedLocationApi.getLastLocation(gac);
            LocationServices.FusedLocationApi.requestLocationUpdates(gac, locationRequest, this);
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
            mMap.clear();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
    }

    private class TimeDisplay extends TimerTask {
        @Override
        public void run() {
            if (isTimerRunning) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (Utilis.isGpsOn()) {
                            updateMarkeronMap();
                        }
                    }
                });
            } else {
                System.out.println("Timer stopped");
                mTimer.cancel();
            }
        }
    }

    private void updateMarkeronMap() {
        if (Utilis.isInternetOn()) {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, Utilis.Api + Utilis.adminviewuser, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " updateMarkeronMap response - " + response);

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " updateMarkeronMap result " + str_result);

                        if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");

                            Toast.makeText(AdminActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        } else if (Integer.parseInt(str_result) == 0) {

                            str_message = obj.getString("Message");

                            JSONArray json = obj.getJSONArray("result");
                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObject = json.getJSONObject(i);
                                LatLngTracker latLngTracker = new LatLngTracker(
                                        jsonObject.getDouble("Latitude"),
                                        jsonObject.getDouble("Longitude"),
                                        jsonObject.getString("Name"));

                                listValue.add(latLngTracker);
                            }

                            setMarker(listValue);

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");

                            Toast.makeText(AdminActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {


                    Toast.makeText(AdminActivity.this, AdminActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
                    return new HashMap<>();
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);

        } else {
            Toast.makeText(this, AdminActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void setMarker(ArrayList<LatLngTracker> listValue) {
        if (mMap != null) {
            mMap.clear();
            for (int i = 0; i < listValue.size(); i++) {
                LatLng latLng = new LatLng(listValue.get(i).getLatitude(), listValue.get(i).getLongitude());
//                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(listValue.get(i).getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(listValue.get(i).getName()).icon(bitmapDescriptorFromVector(AdminActivity.this, R.mipmap.marker));
                mMap.addMarker(markerOptions);
            }
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int marker) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, marker);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.ic_logout) {
            toLogoutCustomer();
            return true;
        } else if (item.getItemId() == R.id.ic_history) {
            Intent intent = new Intent(AdminActivity.this, OthersCheckingHistoryActivity.class);
            intent.putExtra("key", "Admin");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(AdminActivity.this, MenuDashboardActivity.class));
        finish();
    }
}