package Com.mariapublishers.mariaexecutive;

import android.Manifest;
import android.app.Activity;
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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import ir.mirrajabi.searchdialog.SimpleSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.BaseSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.SearchResultListener;
import ir.mirrajabi.searchdialog.core.Searchable;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener,
        LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    String TAG = "MainActivity";
    LocationTrack currentLocation;
    double latitude = 0.0;
    double longitude = 0.0;
    private static final int REQUEST_CODE = 101;

    LinearLayout myLocationLayout;
    TextView myAddressTxt;
    String address = "";

    public static final int notify = 10000;  //interval between two services(Here Service run every 10 seconds)
    private Handler mHandler = new Handler();   //run on another Thread to avoid crash

    Utilis utilis;
    static SharedPreferences mPrefs;
    UserInfo obj;
    Toolbar toolbar;
    int gpsAlertCount = 0;
    Dialog add_dialog;

    NavigationView navigationView;
    DrawerLayout drawer;
    View header;
    ImageView img_dp;
    TextView tv_mob;
    Dialog checkin_dialog;
    String str_result = "", str_message = "";
    ImageView nearbyImg;
    ArrayList<NearbyPlaces> listValue = new ArrayList<NearbyPlaces>();
    Dialog nearby_dialog;
    Timer mTimer = null;
    public static boolean isTimerRunning = true;

    public static GoogleMap mMap;
    GoogleApiClient gac;
    LocationRequest locationRequest;
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    static final int REQUEST_CODE_LOCATION = 1;

    //    ArrayList<CheckInCustomerName> checkInCustomerNameValue = new ArrayList<CheckInCustomerName>();
    List<String> checkInCustomerNameValue = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        utilis = new Utilis(MainActivity.this);
        mPrefs = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("MyObject", "");
        obj = gson.fromJson(json, UserInfo.class);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(MainActivity.this);

        header = navigationView.getHeaderView(0);
        img_dp = header.findViewById(R.id.img_dp);
        tv_mob = header.findViewById(R.id.tv_mob);

        tv_mob.setText(obj.getMobileNumber());

        myAddressTxt = findViewById(R.id.tv_address);

//        Intent serviceIntent = new Intent(this, MyService.class);
//        serviceIntent.putExtra("state", true);
//        ContextCompat.startForegroundService(this, serviceIntent);

//        fetchLastLocation();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(this);

        if (Utilis.isGpsOn()) {
            getLocation();
        }

        getAttendance();

        myLocationLayout = findViewById(R.id.my_location_layout);
        myLocationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });

        nearbyImg = findViewById(R.id.img_my_nearby);
        nearbyImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listValue.size() > 0) {
                    nearby_dialog = new Dialog(MainActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
                    nearby_dialog.setCancelable(false);
                    nearby_dialog.getWindow().setContentView(R.layout.nearby_places_alert_dialog);
                    nearby_dialog.show();

                    RecyclerView recyclerView = nearby_dialog.findViewById(R.id.recycler_view);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setNestedScrollingEnabled(false);
                    recyclerView.setLayoutManager(layoutManager);

                    NearbyAdapter adapter = new NearbyAdapter(MainActivity.this, listValue);
                    recyclerView.setAdapter(adapter);

                    Button closeBtn = nearby_dialog.findViewById(R.id.closebtn);
                    closeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            nearby_dialog.cancel();
                        }
                    });
                }
            }
        });
    }

    private void getAttendance() {
        if (Utilis.isInternetOn()) {
            Utilis.showProgress(MainActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.checkattendancecheckout, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " getAttendance checkattendancecheckout response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " getAttendance checkattendancecheckout result " + str_result);

                        if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");

                            Toast.makeText(MainActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        } else if (Integer.parseInt(str_result) == 0) {

                            str_message = obj.getString("Message");
                            setMenuVisible(false);
                        } else if (Integer.parseInt(str_result) == 2) {

                            str_message = obj.getString("Message");
                            setMenuVisible(true);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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

                    params.put("ExecutiveId", obj.getIndexId());

                    System.out.println(TAG + " getAttendance checkattendancecheckout inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
        } else {
            Toast.makeText(this, MainActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void setMenuVisible(boolean isVisible) {
        Menu menu = navigationView.getMenu();
        for (int menuItemIndex = 0; menuItemIndex < menu.size(); menuItemIndex++) {
            MenuItem menuItem = menu.getItem(menuItemIndex);
            if (menuItem.getItemId() == R.id.nav_attendance_checkout) {
                menuItem.setVisible(isVisible);
            }
        }
    }

    private void fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        System.out.println("MainActivity running fetchLoc");
        if (Utilis.isGpsOn()) {
            try {
                SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
                supportMapFragment.getMapAsync(MainActivity.this);
            } catch (Exception e) {
                System.out.println("onSuccess Excep " + e.getMessage());
            }

            getAddress(latitude, longitude);
        }

    }

    private void getAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
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
                address = stringBuilder.toString();
                myAddressTxt.setText(address);
            }
        } catch (Exception e) {
            System.out.println("getAddress Exception " + e.getMessage());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng latLng = new LatLng(latitude, longitude);
//        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("I Am Here.").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("I Am Here.").icon(bitmapDescriptorFromVector(MainActivity.this, R.mipmap.marker));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
        mMap.addMarker(markerOptions);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                fetchLastLocation();
                getLocation();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isTimerRunning = true;
        mTimer = new Timer();   //recreate new
        mTimer.scheduleAtFixedRate(new MainTimeDisplay(), 0, notify);   //Schedule task
    }

    private void toLogoutCustomer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle(MainActivity.this.getResources().getString(R.string.logout_title))
                .setMessage(MainActivity.this.getResources().getString(R.string.logout_msg))
                .setPositiveButton(MainActivity.this.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog
                        dialog.dismiss();
                        MenuDashboardActivity.isDashTimerRunning = false;

                        isTimerRunning = false;
                        MyService.isServiceRunning = false;
                        Intent serviceIntent = new Intent(MainActivity.this, MyService.class);
                        stopService(serviceIntent);

                        LoginSharedPreference.setLoggedIn(MainActivity.this, false);

                        SharedPreferences preferences = getSharedPreferences("MY_SHARED_PREF", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.apply();

                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    }
                })
                .setNegativeButton(MainActivity.this.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {

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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_Home) {
            drawer.closeDrawers();
            isTimerRunning = false;
            startActivity(new Intent(MainActivity.this, MenuDashboardActivity.class));
            finish();
        } else if (id == R.id.nav_CheckIn) {
            drawer.closeDrawers();
            checkInAddress(address, latitude, longitude);
        } else if (id == R.id.nav_History) {
            drawer.closeDrawers();
            Intent intent = new Intent(MainActivity.this, CheckingHistoryActivity.class);
            intent.putExtra("key", "Main");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_Profile) {
            drawer.closeDrawers();
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            finish();
        } else if (id == R.id.nav_attendance_checkout) {
            drawer.closeDrawers();

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle("Confirmation")
                    .setMessage("Are you sure want to Attendance Checkout?")
                    .setPositiveButton(MainActivity.this.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing but close the dialog
                            dialog.dismiss();
                            toAttendanceCheckout();
                        }
                    })
                    .setNegativeButton(MainActivity.this.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {

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
        } else if (id == R.id.nav_Logout) {
            drawer.closeDrawers();
            toLogoutCustomer();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void toAttendanceCheckout() {
        if (Utilis.isInternetOn()) {
            Utilis.showProgress(MainActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.makeattendancecheckout, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " toAttendanceCheckout response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " toAttendanceCheckout result " + str_result);

                        if (Integer.parseInt(str_result) == 0) {
                            str_message = obj.getString("Message");

                            getAttendance();

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");
                            Toast.makeText(MainActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        } else if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");
                            Toast.makeText(MainActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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

                    params.put("Userid", obj.getIndexId());

                    System.out.println(TAG + " toAttendanceCheckout inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
        } else {
            Toast.makeText(this, MainActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }


    Spinner spinCusType;
    String[] cusType = {"Select Customer Type", "New Customer", "Follow up", "Head Office", "Branch Office"};
    String selectedItemCusType = "";

    String cusName = "";
    int customerTypePos = 0;

    Spinner spinContactType;
    String[] contactType = {"Select Contact Type", "Principal", "Head Master", "Correspondent", "Vice Principal", "Office Staff", "Secretary", "Teachers", "Others"};
    String selectedItemContactType = "";

    EditText contactPersonEt;
    EditText contactNumberEt;
    EditText emailEt;
    EditText websiteEt;
    EditText additionalCustomerEt, notesEt, textAreaEt;
    LinearLayout layContactType;
    boolean isRequired = false;

    private void checkInAddress(String checkinAddr, final double lat, final double lng) {

        checkin_dialog = new Dialog(MainActivity.this, android.R.style.Theme_Translucent_NoTitleBar);

        checkin_dialog.setCancelable(false);
        checkin_dialog.getWindow().setContentView(R.layout.check_in_address_alert_dialog);
        checkin_dialog.show();

        final EditText addrEt = checkin_dialog.findViewById(R.id.et_address);
        addrEt.setText(checkinAddr);
        notesEt = checkin_dialog.findViewById(R.id.et_desc);

        spinCusType = checkin_dialog.findViewById(R.id.spin_cus_type);
        spinContactType = checkin_dialog.findViewById(R.id.spin_contact_type);


        contactPersonEt = checkin_dialog.findViewById(R.id.et_contact_name);
        contactNumberEt = checkin_dialog.findViewById(R.id.et_contact_number);
        emailEt = checkin_dialog.findViewById(R.id.et_email);
        websiteEt = checkin_dialog.findViewById(R.id.et_website);
        additionalCustomerEt = checkin_dialog.findViewById(R.id.et_additional_customer);
        textAreaEt = checkin_dialog.findViewById(R.id.et_text_area);

        final EditText cusNameEt = checkin_dialog.findViewById(R.id.et_cus_name);

        final AutoCompleteTextView text = checkin_dialog.findViewById(R.id.autoCompleteTextView);
        layContactType = checkin_dialog.findViewById(R.id.lay_contact_type);

        isRequired = false;

        getCustomerName(cusNameEt, lat, lng);

        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter cusAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, cusType);
        cusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spinCusType.setAdapter(cusAdapter);

        spinCusType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedItemCusType = cusType[i].toString();
                if (selectedItemCusType.equalsIgnoreCase("Select Customer Type")) {
                    selectedItemCusType = "";
                }
                if (i == 0) {
                    customerTypePos = i;
                    cusNameEt.setVisibility(View.VISIBLE);
                    cusNameEt.setText("");
                    cusNameEt.setEnabled(false);
                    text.setVisibility(View.GONE);
                    contactPersonEt.setVisibility(View.VISIBLE);
                    textAreaEt.setVisibility(View.GONE);

                    isRequired = false;
                    contactNumberEt.setVisibility(View.GONE);
                    emailEt.setVisibility(View.GONE);
                    websiteEt.setVisibility(View.GONE);
                    layContactType.setVisibility(View.GONE);
                    additionalCustomerEt.setVisibility(View.GONE);
                    selectedItemContactType = "";

                } else if (i == 1) {
                    customerTypePos = i;
                    text.setVisibility(View.VISIBLE);
                    text.setText("");
                    contactPersonEt.setText("");
                    notesEt.setText("");
                    cusNameEt.setVisibility(View.GONE);
                    cusNameEt.setText("");
                    cusNameEt.setEnabled(true);
                    contactPersonEt.setVisibility(View.VISIBLE);
                    textAreaEt.setVisibility(View.GONE);

                    isRequired = false;
                    contactNumberEt.setVisibility(View.GONE);
                    emailEt.setVisibility(View.GONE);
                    websiteEt.setVisibility(View.GONE);
                    layContactType.setVisibility(View.GONE);
                    additionalCustomerEt.setVisibility(View.GONE);
                    selectedItemContactType = "";

                    ArrayAdapter<String> adapter = new
                            ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, checkInCustomerNameValue) {
                                @NonNull
                                @Override
                                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                    View row = super.getView(position, convertView, parent);
                                    row.setBackgroundColor(Color.WHITE);
                                    return row;
                                }
                            };

                    text.setAdapter(adapter);
                    text.setThreshold(1);
                } else if (i ==2){
                    customerTypePos = i;
                    cusNameEt.setVisibility(View.VISIBLE);
                    cusNameEt.setText("");
                    cusNameEt.setEnabled(false);
                    text.setVisibility(View.GONE);
                    contactPersonEt.setVisibility(View.VISIBLE);
                    textAreaEt.setVisibility(View.GONE);

                    isRequired = false;
                    contactNumberEt.setVisibility(View.GONE);
                    emailEt.setVisibility(View.GONE);
                    websiteEt.setVisibility(View.GONE);
                    layContactType.setVisibility(View.GONE);
                    additionalCustomerEt.setVisibility(View.GONE);
                    selectedItemContactType = "";

                    new SimpleSearchDialogCompat(MainActivity.this, "Customer Name ", "Search here...",
                            null, initData(), new SearchResultListener<Searchable>() {
                        @Override
                        public void onSelected(BaseSearchDialogCompat baseSearchDialogCompat, Searchable searchable, int i) {
                            System.out.println("search " + searchable.getTitle());
                            baseSearchDialogCompat.dismiss();
                            cusNameEt.setText(searchable.getTitle());
                            getRecentRecords(cusNameEt.getText().toString());
                        }
                    }).show();
                } else {
                    customerTypePos = i;
                    text.setVisibility(View.GONE);
                    cusNameEt.setVisibility(View.GONE);
                    cusNameEt.setText("");
                    cusNameEt.setEnabled(true);
                    contactPersonEt.setVisibility(View.GONE);
                    textAreaEt.setVisibility(View.VISIBLE);
                    textAreaEt.setText("");
                    notesEt.setText("");

                    isRequired = false;
                    contactNumberEt.setVisibility(View.GONE);
                    emailEt.setVisibility(View.GONE);
                    websiteEt.setVisibility(View.GONE);
                    layContactType.setVisibility(View.GONE);
                    additionalCustomerEt.setVisibility(View.GONE);
                    selectedItemContactType = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter conAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, contactType);
        conAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spinContactType.setAdapter(conAdapter);

        spinContactType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedItemContactType = contactType[i].toString();
                if (selectedItemContactType.equalsIgnoreCase("Select Contact Type")) {
                    selectedItemContactType = "";
                }
                if (selectedItemContactType.equalsIgnoreCase("Others")) {
                    additionalCustomerEt.setVisibility(View.VISIBLE);
                } else {
                    additionalCustomerEt.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final EditText etArea = checkin_dialog.findViewById(R.id.et_area);
        Button submitBtn = checkin_dialog.findViewById(R.id.submitbtn);
        Button cancelBtn = checkin_dialog.findViewById(R.id.cancelbtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_checkin_address = addrEt.getText().toString().trim();
                String str_notes = notesEt.getText().toString().trim();
                String str_area = etArea.getText().toString().trim();
                String strTextArea = textAreaEt.getText().toString().trim();

                if (customerTypePos == 1) {
                    cusName = text.getText().toString().trim();
                } else if (customerTypePos == 2) {
                    cusName = cusNameEt.getText().toString().trim();
                } else {
                    cusName = "";
                }
                String contactPerson = contactPersonEt.getText().toString().trim();
                String contactNumber = contactNumberEt.getText().toString().trim();
                String email = emailEt.getText().toString().trim();
                String website = websiteEt.getText().toString().trim();
                String additionalCus = additionalCustomerEt.getText().toString().trim();

                if (selectedItemCusType.equals("")) {
                    Toast.makeText(MainActivity.this, "Select Customer Type", Toast.LENGTH_SHORT).show();
                } else if ((selectedItemCusType.equalsIgnoreCase("New Customer") || selectedItemCusType.equalsIgnoreCase("Follow up")) && cusName.equals("")) {
                    Toast.makeText(MainActivity.this, "Customer Name required", Toast.LENGTH_SHORT).show();
                } else if (str_checkin_address.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Address should not be Empty", Toast.LENGTH_SHORT).show();
                } /*else if (str_area.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Area required", Toast.LENGTH_SHORT).show();
                }*/ else if (selectedItemCusType.equalsIgnoreCase("Follow up") && selectedItemContactType.equals("") && isRequired) {
                    Toast.makeText(MainActivity.this, "Select Contact Type", Toast.LENGTH_SHORT).show();
                } else if (selectedItemContactType.equalsIgnoreCase("Others") && additionalCus.isEmpty() && isRequired) {
                    Toast.makeText(MainActivity.this, "Additional Customer required", Toast.LENGTH_SHORT).show();
                } else if ((selectedItemCusType.equalsIgnoreCase("New Customer") || selectedItemCusType.equalsIgnoreCase("Follow up")) && contactPerson.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Contact Person required", Toast.LENGTH_SHORT).show();
                } else if (selectedItemCusType.equalsIgnoreCase("Follow up") && contactNumber.isEmpty() && isRequired) {
                    Toast.makeText(MainActivity.this, "Contact Number required", Toast.LENGTH_SHORT).show();
                } else if ((selectedItemCusType.equalsIgnoreCase("Head Office") || selectedItemCusType.equalsIgnoreCase("Branch Office")) && textAreaEt.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Text required", Toast.LENGTH_SHORT).show();
                } else if (notesEt.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Notes required", Toast.LENGTH_SHORT).show();
                } else {
                    System.out.println("name is " + cusName);
                    if (customerTypePos == 1 || customerTypePos == 2) {
                        sendCheckInAddress(str_checkin_address, lat, lng, checkin_dialog, str_notes, selectedItemCusType, cusName, contactPerson, contactNumber, email, website, additionalCus, selectedItemContactType, str_area);
                    } else {
                        sendNewCustomerType(str_checkin_address, lat, lng, checkin_dialog, str_notes, selectedItemCusType, strTextArea);
                    }
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkin_dialog.cancel();
            }
        });
    }

    private void sendNewCustomerType(final String str_checkin_address, final double lat, final double lng, final Dialog checkin_dialog, final String str_notes, final String selectedItemCusType, final String strTextArea) {
        if (Utilis.isInternetOn()) {

            Utilis.showProgress(MainActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.newcustomertype, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " sendNewCustomerType response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " sendNewCustomerType result " + str_result);

                        if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");

                            Toast.makeText(MainActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        } else if (Integer.parseInt(str_result) == 0) {

                            str_message = obj.getString("message");
                            Toast.makeText(MainActivity.this, str_message, Toast.LENGTH_SHORT).show();
                            checkin_dialog.dismiss();

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");

                            Toast.makeText(MainActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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

                    params.put("executiveId", obj.getIndexId());
                    params.put("latitude", String.valueOf(lat));
                    params.put("longitude", String.valueOf(lng));
                    params.put("customerAddress", str_checkin_address);
                    params.put("notes", str_notes);
                    params.put("customerType", selectedItemCusType);
                    params.put("textArea", strTextArea);

                    System.out.println(TAG + " sendNewCustomerType inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);

        } else {
            Toast.makeText(this, MainActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void getRecentRecords(final String cusName) {
        if (Utilis.isInternetOn()) {

            Utilis.showProgress(MainActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.getrecentrecords, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " getrecentrecords response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " getrecentrecords result " + str_result);

                        if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");
                            isRequired = false;
                            Toast.makeText(MainActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        } else if (Integer.parseInt(str_result) == 0) {

                            str_message = obj.getString("message");
                            JSONObject json = obj.getJSONObject("result");
                            isRequired=true;
                            contactNumberEt.setVisibility(View.VISIBLE);
                            emailEt.setVisibility(View.VISIBLE);
                            websiteEt.setVisibility(View.VISIBLE);
                            layContactType.setVisibility(View.VISIBLE);

                            contactPersonEt.setText(json.getString("ContactPerson"));
                            if (json.getString("ContactNum") == null || json.getString("ContactNum").equalsIgnoreCase("null")) {
                                contactNumberEt.setText("");
                            } else {
                                contactNumberEt.setText(json.getString("ContactNum"));
                            }
                            if (json.getString("Email") == null || json.getString("Email").equalsIgnoreCase("null")) {
                                emailEt.setText("");
                            } else {
                                emailEt.setText(json.getString("Email"));
                            }
                            if (json.getString("Website") == null || json.getString("Website").equalsIgnoreCase("null")) {
                                websiteEt.setText("");
                            } else {
                                websiteEt.setText(json.getString("Website"));
                            }
                            if (json.getString("Notes") == null || json.getString("Notes").equalsIgnoreCase("null")) {
                                notesEt.setText("");
                            } else {
                                notesEt.setText(json.getString("Notes"));
                            }
                            if (json.getString("AdditionalCustomer") == null || json.getString("AdditionalCustomer").equalsIgnoreCase("null")) {
                                additionalCustomerEt.setText("");
                            } else {
                                additionalCustomerEt.setText(json.getString("AdditionalCustomer"));
                            }
                            String strContactType = json.getString("ContactType");

                            int prefPos = 0;
                            for (int i = 0; i < contactType.length; i++) {
                                if (contactType[i].equalsIgnoreCase(strContactType)) {
                                    prefPos = i;
                                    break;
                                }
                            }

                            ArrayAdapter arrayAdapter = new ArrayAdapter(MainActivity.this, R.layout.spinner_item, contactType);
                            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            //Setting the ArrayAdapter data on the Spinner
                            spinContactType.setAdapter(arrayAdapter);
                            spinContactType.setSelection(prefPos);

                            if (strContactType.equalsIgnoreCase("Others")) {
                                additionalCustomerEt.setVisibility(View.VISIBLE);
                            } else {
                                additionalCustomerEt.setVisibility(View.GONE);
                                selectedItemContactType = "";
                            }

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("message");
                            isRequired = false;
//                            Toast.makeText(MainActivity.this, str_message, Toast.LENGTH_SHORT).show();
                            contactNumberEt.setVisibility(View.GONE);
                            emailEt.setVisibility(View.GONE);
                            websiteEt.setVisibility(View.GONE);
                            layContactType.setVisibility(View.GONE);
                            additionalCustomerEt.setVisibility(View.GONE);
                            selectedItemContactType = "";

                            contactPersonEt.setText("");
                            contactNumberEt.setText("");
                            emailEt.setText("");
                            websiteEt.setText("");
                            notesEt.setText("");
                            additionalCustomerEt.setText("");

                            //Creating the ArrayAdapter instance having the country list
                            ArrayAdapter conAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, contactType);
                            conAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            //Setting the ArrayAdapter data on the Spinner
                            spinContactType.setAdapter(conAdapter);
                            spinContactType.setSelection(0);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();
                    isRequired = false;
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
                    params.put("executiveId", obj.getIndexId());
                    params.put("customerName", cusName);
                    System.out.println(TAG + " inputs getrecentrecords "+ params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(MainActivity.this).addToRequestQueue(stringRequest);

        } else {
            Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void getCustomerName(final EditText cusNameEt, final double lat, final double lng) {
        if (Utilis.isInternetOn()) {

            Utilis.showProgress(MainActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.getcheckincustomername, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " getcheckincustomername response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " getcheckincustomername result " + str_result);

                        if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");

                            Toast.makeText(MainActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        } else if (Integer.parseInt(str_result) == 0) {

                            str_message = obj.getString("Message");

                            checkInCustomerNameValue.clear();

                            JSONArray json = obj.getJSONArray("result");
                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObject = json.getJSONObject(i);
                                System.out.println("onloop " + jsonObject.getString("Customername"));

                                checkInCustomerNameValue.add(jsonObject.getString("Customername"));
                            }

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");

                            Toast.makeText(MainActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
                    params.put("latitude", String.valueOf(lat));
                    params.put("longitude", String.valueOf(lng));
                    System.out.println(TAG + " getcheckincustomername inputs "+ params );
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(MainActivity.this).addToRequestQueue(stringRequest);

        } else {
            Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<SearchModel> initData() {
        ArrayList<SearchModel> items = new ArrayList<>();
        for (int i = 0; i < checkInCustomerNameValue.size(); i++) {
            items.add(new SearchModel(checkInCustomerNameValue.get(i)));
        }
        return items;
    }

    private void sendCheckInAddress(final String str_checkin_address, final double lat, final double lng, final Dialog checkin_dialog, final String str_notes, final String CustomerType, final String cusName, final String contactPerson, final String contactNumber, final String email, final String website, final String additionalCus, final String selectedItemContactType, final String str_area) {
        if (Utilis.isInternetOn()) {

            Utilis.showProgress(MainActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.checkinnew, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " sendCheckInAddress response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " sendCheckInAddress result " + str_result);

                        if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");

                            Toast.makeText(MainActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        } else if (Integer.parseInt(str_result) == 0) {

                            str_message = obj.getString("Message");
                            Toast.makeText(MainActivity.this, str_message, Toast.LENGTH_SHORT).show();
                            checkin_dialog.dismiss();

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");

                            Toast.makeText(MainActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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

                    params.put("UserIndexId", obj.getIndexId());
                    params.put("Latitude", String.valueOf(lat));
                    params.put("Longitude", String.valueOf(lng));
                    params.put("Address", str_checkin_address);
                    params.put("Description", str_notes);
                    params.put("Customertype", CustomerType);
                    params.put("Customername", cusName);
                    params.put("Contactperson", contactPerson);
                    params.put("Area", notesEt.getText().toString());
                    params.put("ContactType", selectedItemContactType);
                    params.put("ContactNum", contactNumber);
                    params.put("Email", email);
                    params.put("Website", website);
                    params.put("AdditionalCustomer", additionalCus);

                    System.out.println(TAG + " sendCheckInAddress inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);

        } else {
            Toast.makeText(this, MainActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }

    }

    private void getLocation() {
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

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location != null) {
            updateUI(location);
        }
    }

    private void updateUI(Location location) {
        mMap.clear();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("I Am Here.").icon(bitmapDescriptorFromVector(MainActivity.this, R.mipmap.marker));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
        mMap.addMarker(markerOptions);
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (Utilis.isGpsOn()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LOCATION) {
            getLocation();
        }
    }

    //class MainTimeDisplay for handling task
    class MainTimeDisplay extends TimerTask {
        @Override
        public void run() {
            if (isTimerRunning) {
                // run on another thread
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("MainActivity Timer running");
//                        currentLocation = new LocationTrack(MainActivity.this);
//                        System.out.println("MainActivity " + currentLocation.canGetLocation());
                        if (Utilis.isGpsOn()) {
                            gpsAlertCount = 1;
                            if (add_dialog != null && add_dialog.isShowing()) {
                                System.out.println("Dialog showing force to close");
                                add_dialog.dismiss();
                            }
//                            if (currentLocation.canGetLocation()) {
                            System.out.println("MainActivity running inside");
//                                longitude = currentLocation.getLongitude();
//                                latitude = currentLocation.getLatitude();
//                                fetchLastLocation();
                            getAddress(latitude, longitude);
                            getNearbyAddress();
//                            }
                        } else {
                            if (gpsAlertCount == 0) {
                                gpsAlertCount = 1;

                                //                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                //
                                //                            alertDialog.setTitle("GPS is not Enabled!");
                                //
                                //                            alertDialog.setMessage("Do you want to turn on GPS?");
                                //
                                //
                                //                            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                //                                public void onClick(DialogInterface dialog, int which) {
                                //                                    gpsAlertCount=0;
                                //                                    dialog.dismiss();
                                //                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                //                                    startActivity(intent);
                                //                                }
                                //                            });
                                //
                                //
                                //                            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                //                                public void onClick(DialogInterface dialog, int which) {
                                //                                    gpsAlertCount =0;
                                //                                    dialog.cancel();
                                //                                }
                                //                            });
                                //
                                //
                                //                            alertDialog.show();


                                add_dialog = new Dialog(MainActivity.this, android.R.style.Theme_Translucent_NoTitleBar);

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
                                        startActivity(intent);
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
                });
            } else {
                System.out.println("main timer stopped");
                mTimer.cancel();
            }
        }
    }

    private void getNearbyAddress() {
        if (Utilis.isInternetOn()) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.getaddress, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " getNearbyAddress response - " + response);

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " getNearbyAddress result " + str_result);

                        if (Integer.parseInt(str_result) == 0) {
                            nearbyImg.setVisibility(View.VISIBLE);
                            Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.blink);
                            nearbyImg.startAnimation(anim);
                            listValue.clear();

                            str_message = obj.getString("Message");
                            JSONArray json = obj.getJSONArray("result");
                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObject = json.getJSONObject(i);
                                NearbyPlaces nearbyPlaces = new NearbyPlaces(
                                        jsonObject.getString("Datetime"),
                                        jsonObject.getString("Address"),
                                        jsonObject.getString("Latitude"),
                                        jsonObject.getString("Longitude"),
                                        jsonObject.getString("Name"));
                                listValue.add(nearbyPlaces);
                            }

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");
                            nearbyImg.setVisibility(View.GONE);
                            nearbyImg.clearAnimation();
                            listValue.clear();
                        } else if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");
                            nearbyImg.setVisibility(View.GONE);
                            nearbyImg.clearAnimation();
                            listValue.clear();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
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
                    System.out.println(TAG + " getNearby inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_check_in, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.ic_check_in) {
            checkInAddress(address, latitude, longitude);
            return true;
        } else if (item.getItemId() == R.id.ic_home) {
            isTimerRunning = false;
            startActivity(new Intent(MainActivity.this, MenuDashboardActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private class NearbyAdapter extends RecyclerView.Adapter<ViewHolder> {
        Activity mActivity;
        private ArrayList<NearbyPlaces> arrayList;

        public NearbyAdapter(Activity mainActivity, ArrayList<NearbyPlaces> listValue) {
            mActivity = mainActivity;
            arrayList = listValue;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.nearby_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            holder.tvAddress.setText(arrayList.get(position).getAddress());
            holder.tvDateTime.setText(arrayList.get(position).getDateTime());
            holder.tvName.setText(obj.getName());
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (nearby_dialog != null && nearby_dialog.isShowing()) {
                        nearby_dialog.dismiss();
                        checkInAddress(arrayList.get(position).getAddress(), Double.parseDouble(arrayList.get(position).getLatitude()), Double.parseDouble(arrayList.get(position).getLongitude()));
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAddress, tvDateTime, tvName;
        RelativeLayout cardView;

        public ViewHolder(@NonNull View view) {
            super(view);
            cardView = view.findViewById(R.id.cardLayout);
            tvAddress = view.findViewById(R.id.tv_checkinaddress);
            tvDateTime = view.findViewById(R.id.tv_date_time);
            tvName = view.findViewById(R.id.tv_name);
            view.setTag(itemView);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(MainActivity.this, MenuDashboardActivity.class));
        finish();
    }
}