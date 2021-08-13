package Com.mariapublishers.mariaexecutive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MenuDashboardActivity extends AppCompatActivity {
    String TAG = "MenuDashboardActivity";
    GridView gridView;
    List<RowItem> rowItems;
    String[] adminMenuNames = {"Executive Tracking",
            "Expense",
            "Complains/Inquiry",
            "Leave",
            "Send News",
            "Attendance List",
            "Checking History",
            "Logout"
    };
    Integer[] adminMenuImages = {R.mipmap.ic_exe_tracking_round,
            R.mipmap.ic_expense_round,
            R.mipmap.ic_complains_round,
            R.mipmap.ic_leave_round,
            R.mipmap.ic_news_round,
            R.mipmap.ic_attendacne_round,
            R.mipmap.ic_history_round,
            R.mipmap.ic_logout_round,
    };

    String[] exeMenuNames = {"Current Location",
//            "Checkin Address",
            "Expense",
            "Complains/Inquiry",
            "Leave",
            "Messages",
            "Attendance",
            "Checking History",
            "Logout"
    };

    Integer[] exeMenuImages = {R.mipmap.ic_current_location_round,
//            R.mipmap.ic_check_in_round,
            R.mipmap.ic_expense_round,
            R.mipmap.ic_complains_round,
            R.mipmap.ic_leave_round,
            R.mipmap.ic_news_round,
            R.mipmap.ic_attendacne_round,
            R.mipmap.ic_history_round,
            R.mipmap.ic_logout_round,
    };

    SharedPreferences mPrefs;
    Timer mTimer = null;
    public static final int notify = 30000;  //interval between two services(Here Service run every 30 seconds)
    private Handler mHandler = new Handler();   //run on another Thread to avoid crash
    public static boolean isDashTimerRunning = true;
    int gpsAlertCount = 0;
    Dialog add_dialog;
    private static final int REQUEST_CODE = 101;
    App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_dashboard);
        app = (App) getApplication();
        gridView = (GridView) findViewById(R.id.gridView);

        mPrefs = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
        if (LoginSharedPreference.getLoggedStatus(MenuDashboardActivity.this)) {
            Gson gson = new Gson();
            String json = mPrefs.getString("MyObject", "");
            UserInfo obj = gson.fromJson(json, UserInfo.class);
            if (Integer.parseInt(obj.getRoleId()) == 1) {
                rowItems = new ArrayList<RowItem>();
                for (int i = 0; i < adminMenuImages.length; i++) {
                    RowItem item = new RowItem(adminMenuImages[i], adminMenuNames[i]);
                    rowItems.add(item);
                }
            } else {
                rowItems = new ArrayList<RowItem>();
                for (int i = 0; i < exeMenuImages.length; i++) {
                    RowItem item = new RowItem(exeMenuImages[i], exeMenuNames[i]);
                    rowItems.add(item);
                }

                getRunTimePermission();

                Intent serviceIntent = new Intent(this, MyService.class);
                serviceIntent.putExtra("state", true);
                ContextCompat.startForegroundService(this, serviceIntent);
            }
        }

        GridAdapter adapter = new GridAdapter(MenuDashboardActivity.this,
                R.layout.grid_list_item, rowItems);
        gridView.setAdapter(adapter);


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = rowItems.get(position).getName();
                switch (name) {
                    case "Executive Tracking":
                        isDashTimerRunning = false;
                        startActivity(new Intent(MenuDashboardActivity.this, AdminActivity.class));
                        finish();
                        break;

                    case "Current Location":
                        isDashTimerRunning = false;
                        startActivity(new Intent(MenuDashboardActivity.this, MainActivity.class));
                        finish();
                        break;

                    case "Checking History":
                        Gson gson = new Gson();
                        String json = mPrefs.getString("MyObject", "");
                        UserInfo obj = gson.fromJson(json, UserInfo.class);
                        if (Integer.parseInt(obj.getRoleId()) == 1) {
                            isDashTimerRunning = false;
                            Intent intent = new Intent(MenuDashboardActivity.this, OthersCheckingHistoryActivity.class);
                            intent.putExtra("key", "Dash");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            isDashTimerRunning = false;
                            Intent intent = new Intent(MenuDashboardActivity.this, CheckingHistoryActivity.class);
                            intent.putExtra("key", "Dash");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                        break;

                    case "Logout":
                        toLogout();
                        break;

                    case "Expense":
                        isDashTimerRunning = false;
                        startActivity(new Intent(MenuDashboardActivity.this, ExpenseActivity.class));
                        finish();
                        break;

                    case "Complains/Inquiry":
                        isDashTimerRunning = false;
                        startActivity(new Intent(MenuDashboardActivity.this, ComplainActivity.class));
                        finish();
                        break;

                    case "Leave":
                        isDashTimerRunning = false;
                        startActivity(new Intent(MenuDashboardActivity.this, LeaveActivity.class));
                        finish();
                        break;

                    case "Send News":

                    case "Messages":
                        isDashTimerRunning = false;
                        startActivity(new Intent(MenuDashboardActivity.this, SendNewsActivity.class));
                        finish();
                        break;

                    case "Attendance":
                        isDashTimerRunning = false;
                        Intent intent = new Intent(MenuDashboardActivity.this, AttendanceActivity.class);
                        intent.putExtra("isAttendance", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        break;

                    case "Attendance List":
                        isDashTimerRunning = false;
                        startActivity(new Intent(MenuDashboardActivity.this, AttendanceListActivity.class));
                        finish();

                }
            }
        });
    }

    private void getRunTimePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getRunTimePermission();
            }
        }
    }

    private void toLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MenuDashboardActivity.this);

        builder.setTitle(MenuDashboardActivity.this.getResources().getString(R.string.logout_title))
                .setMessage(MenuDashboardActivity.this.getResources().getString(R.string.logout_msg))
                .setPositiveButton(MenuDashboardActivity.this.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog
                        dialog.dismiss();

                        isDashTimerRunning = false;

                        AdminActivity.isTimerRunning = false;
                        MainActivity.isTimerRunning = false;
                        MyService.isServiceRunning = false;
                        Intent serviceIntent = new Intent(MenuDashboardActivity.this, MyService.class);
                        stopService(serviceIntent);

                        LoginSharedPreference.setLoggedIn(MenuDashboardActivity.this, false);

                        SharedPreferences preferences = getSharedPreferences("MY_SHARED_PREF", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.apply();

                        startActivity(new Intent(MenuDashboardActivity.this, LoginActivity.class));
                        finish();
                    }
                })
                .setNegativeButton(MenuDashboardActivity.this.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {

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
    protected void onResume() {
        super.onResume();
        checkAppUpdate();
    }

    private void checkAppUpdate() {
        String str_ver_code = String.valueOf(BuildConfig.VERSION_CODE);
        String str_ver_name = BuildConfig.VERSION_NAME;

        toCheckVersion(str_ver_code, str_ver_name);
    }

    private void toCheckVersion(final String str_ver_code, final String str_ver_name) {
        if (Utilis.isInternetOn()) {

            Utilis.showProgress(MenuDashboardActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.checkversion, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " toCheckVersion response - " + response);

                        Utilis.dismissProgress();

                        String str_result = obj.getString("errorCode");
                        System.out.print(TAG + " toCheckVersion result" + str_result);

                        if (Integer.parseInt(str_result) == 1) {

                            // input param missing

                        } else if (Integer.parseInt(str_result) == 0) {

                            Gson gson = new Gson();
                            String json = mPrefs.getString("MyObject", "");
                            UserInfo userInfo = gson.fromJson(json, UserInfo.class);
                            if (Integer.parseInt(userInfo.getRoleId()) != 1) {
                                isDashTimerRunning = true;
                                mTimer = new Timer();   //recreate new
                                mTimer.scheduleAtFixedRate(new MyTimeDisplay(), 0, notify);   //Schedule task
                            }
                            oneSignal(userInfo.getIndexId());

                        } else if (Integer.parseInt(str_result) == 2) {

                            Intent it = new Intent(MenuDashboardActivity.this, AppUpdateActivity.class);
                            it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(it);
                            finish();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(MenuDashboardActivity.this, MenuDashboardActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

                    if (error instanceof NoConnectionError) {

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

                    params.put("Versioncode", str_ver_code);
                    params.put("Versionname", str_ver_name);

                    System.out.println(TAG + " toCheckVersion inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(MenuDashboardActivity.this).addToRequestQueue(stringRequest);

        } else {
            Toast.makeText(this, MenuDashboardActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void oneSignal(String indexId) {
        app.initMethod(indexId);
    }

    private class MyTimeDisplay extends TimerTask {
        @Override
        public void run() {
            if (isDashTimerRunning) {
                // run on another thread
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Dashboard Timer running");
                        if (Utilis.isGpsOn()) {
                        } else {
                            if (gpsAlertCount == 0) {
                                gpsAlertCount = 1;

                                add_dialog = new Dialog(MenuDashboardActivity.this, android.R.style.Theme_Translucent_NoTitleBar);

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
                System.out.println("my timer stopped");
                mTimer.cancel();
            }
        }
    }
}