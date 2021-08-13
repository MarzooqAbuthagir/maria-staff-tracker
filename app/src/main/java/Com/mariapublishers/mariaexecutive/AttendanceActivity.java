package Com.mariapublishers.mariaexecutive;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.google.gson.Gson;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AttendanceActivity extends AppCompatActivity {
    String TAG = "AttendanceActivity";
    Toolbar toolbar;
    ActionBar actionBar = null;

    Utilis utilis;
    static SharedPreferences mPrefs; //, sharedPreferences;
    UserInfo obj;

    EditText etName, etDate, etTime;
    ImageView imgDp;
    SwitchCompat mySwitch;
    String str_result = "", str_message = "";
    boolean isAttendance;
    Button leaveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        Intent intent = getIntent();
        isAttendance = intent.getBooleanExtra("isAttendance", false);


        utilis = new Utilis(AttendanceActivity.this);
        mPrefs = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("MyObject", "");
        obj = gson.fromJson(json, UserInfo.class);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDefaultDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        etName = findViewById(R.id.et_name);
        etDate = findViewById(R.id.date_txt);
        etTime = findViewById(R.id.time_txt);
        imgDp = findViewById(R.id.img_dp);
        mySwitch = findViewById(R.id.mySwitch);
//        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
//                if (isCheck) {
//                    sharedPreferences = getSharedPreferences("MY_ATTENDANCE",MODE_PRIVATE);
//
//                    Calendar cal = Calendar.getInstance();
//                    int currentDate = cal.get(Calendar.DATE);
//
//                    String prefDate = sharedPreferences.getString("currentDate", "");
//                    System.out.println("prefDate "+prefDate);
//                    if (!prefDate.isEmpty() && Integer.parseInt(prefDate) == currentDate) {
//                    } else {
//                        markAttendance();
//                    }
//                } else
//                    System.out.println("off");
//            }
//        });

//        sharedPreferences = getSharedPreferences("MY_ATTENDANCE",MODE_PRIVATE);

        Calendar cal = Calendar.getInstance();
        int currentDate = cal.get(Calendar.DATE);

//        String prefDate = sharedPreferences.getString("currentDate", "");
//        System.out.println("prefDate "+prefDate);
//        if (!prefDate.isEmpty() && Integer.parseInt(prefDate) == currentDate) {
//            mySwitch.setEnabled(false);
//            mySwitch.setChecked(true);
//        } else {
//            mySwitch.setEnabled(true);
//            mySwitch.setChecked(false);
//        }


        if (Utilis.isInternetOn()) {
            getAttendance();
        } else {
            Toast.makeText(this, AttendanceActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }

        etName.setText(obj.getName());
        try {
            Picasso.with(this).load(Utilis.imagePath + obj.getIndexId()).skipMemoryCache().placeholder(getResources().getDrawable(R.drawable.header_profile)).transform(new CircleTransform()).networkPolicy(NetworkPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_CACHE).into(imgDp);
        } catch (Resources.NotFoundException e) {
            System.out.println(TAG + " image err " + e.getMessage());
            e.printStackTrace();
        }

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
        etDate.setText(formattedDate);

        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        etTime.setText(currentTime);

        leaveBtn = findViewById(R.id.leaveBtn);
        leaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveDialog();
            }
        });
    }

    Dialog dialog;
    EditText dateTxt, todateTxt;
    ArrayList<Leave> listValue = new ArrayList<Leave>();

    Spinner spinReason;
    String[] reasons = { "Sick", "Grief", "Pay leave", "Personal leave", "Maternity/Paternity", "Others"};
    String selectedItem="";

    private void leaveDialog() {
        dialog = new Dialog(AttendanceActivity.this, android.R.style.Theme_Translucent_NoTitleBar);

        dialog.setCancelable(false);
        dialog.getWindow().setContentView(R.layout.add_leave_alert_dialog);
        dialog.show();

        LinearLayout dateLayout = dialog.findViewById(R.id.date_layout);
        LinearLayout todateLayout = dialog.findViewById(R.id.to_date_layout);
        dateTxt = dialog.findViewById(R.id.date_txt);
        todateTxt = dialog.findViewById(R.id.to_date_txt);
        final EditText descEt = dialog.findViewById(R.id.et_reason);
        Button submitBtn = dialog.findViewById(R.id.addbtn);
        Button cancelBtn = dialog.findViewById(R.id.closebtn);

        spinReason = dialog.findViewById(R.id.spin_reason);

        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,reasons);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spinReason.setAdapter(aa);

        spinReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedItem = reasons[i].toString();
                if (selectedItem.equalsIgnoreCase("Others")) {
                    descEt.setVisibility(View.VISIBLE);
                } else {
                    descEt.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        dateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDate();
            }
        });

        dateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDate();
            }
        });

        todateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toselectDate();
            }
        });

        todateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toselectDate();
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fromDate = dateTxt.getText().toString();
                String toDate = todateTxt.getText().toString();
                String str_desc = descEt.getText().toString();
                if (fromDate.isEmpty() || fromDate.equalsIgnoreCase("Select Start Date")) {
                    Toast.makeText(AttendanceActivity.this, "Select Start Date", Toast.LENGTH_SHORT).show();
                } else if (toDate.isEmpty() || toDate.equalsIgnoreCase("Select End Date")) {
                    Toast.makeText(AttendanceActivity.this, "Select End Date", Toast.LENGTH_SHORT).show();
                } else {
                    if (selectedItem.equalsIgnoreCase("Others")) {
                        if (str_desc.isEmpty()) {
                            Toast.makeText(AttendanceActivity.this, "Enter Reason for Leave", Toast.LENGTH_SHORT).show();
                        } else {
                            str_desc = descEt.getText().toString();
                            sendLeave(fromDate, toDate, str_desc, dialog);
                        }
                    } else {
                        str_desc = selectedItem;
                        sendLeave(fromDate, toDate, str_desc, dialog);
                    }
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
    }

    private void toselectDate() {
        Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(AttendanceActivity.this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // Display Selected date in textbox

                String strMonth = "";
                String strDay = "";
                String monthofYear = String.valueOf(monthOfYear + 1);

                try {
                    if (String.valueOf(monthOfYear + 1).length() == 1) {
                        strMonth = String.valueOf("0" + monthofYear);
                    } else {
                        strMonth = String.valueOf(monthofYear);
                    }

                    if (String.valueOf(dayOfMonth).length() == 1) {
                        strDay = String.valueOf("0" + dayOfMonth);
                    } else {
                        strDay = String.valueOf(dayOfMonth);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "onDateSet: " + e.getMessage(), e);
                }

                String date = year + "-" + strMonth + "-" + strDay;

                todateTxt.setText(date);

            }
        }, mYear, mMonth, mDay);
        dpd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //ignored
            }
        });

        dpd.getDatePicker().setMinDate(System.currentTimeMillis()-1000);
        dpd.show();
    }

    private void sendLeave(final String fromLeaveDate, final String toLeaveDate, final String str_desc, final Dialog dialog) {
        if (Utilis.isInternetOn()) {

            Utilis.showProgress(AttendanceActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.addLeave, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " sendLeave response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " sendLeave result " + str_result);

                        if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");

                            Toast.makeText(AttendanceActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        } else if (Integer.parseInt(str_result) == 0) {

                            str_message = obj.getString("Message");
                            Toast.makeText(AttendanceActivity.this, str_message, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");

                            Toast.makeText(AttendanceActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(AttendanceActivity.this, AttendanceActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
                    params.put("Fromdate", fromLeaveDate);
                    params.put("Todate", toLeaveDate);
                    params.put("Reason", str_desc);

                    System.out.println(TAG + " sendLeave inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);

        } else {
            Toast.makeText(this, AttendanceActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void selectDate() {
        Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(AttendanceActivity.this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // Display Selected date in textbox

                String strMonth = "";
                String strDay = "";
                String monthofYear = String.valueOf(monthOfYear + 1);

                try {
                    if (String.valueOf(monthOfYear + 1).length() == 1) {
                        strMonth = String.valueOf("0" + monthofYear);
                    } else {
                        strMonth = String.valueOf(monthofYear);
                    }

                    if (String.valueOf(dayOfMonth).length() == 1) {
                        strDay = String.valueOf("0" + dayOfMonth);
                    } else {
                        strDay = String.valueOf(dayOfMonth);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "onDateSet: " + e.getMessage(), e);
                }

                String date = year + "-" + strMonth + "-" + strDay;

                dateTxt.setText(date);

            }
        }, mYear, mMonth, mDay);
        dpd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //ignored
            }
        });

        dpd.getDatePicker().setMinDate(System.currentTimeMillis()-1000);
        dpd.show();
    }

    private void getAttendance() {
        Utilis.showProgress(AttendanceActivity.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.checkattendance, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    //converting response to json object
                    JSONObject obj = new JSONObject(response);

                    System.out.println(TAG + " getAttendance response - " + response);

                    Utilis.dismissProgress();

                    str_result = obj.getString("errorCode");
                    System.out.print(TAG + " getAttendance result " + str_result);

                    if (Integer.parseInt(str_result) == 1) {
                        str_message = obj.getString("Message");

                        Toast.makeText(AttendanceActivity.this, str_message, Toast.LENGTH_SHORT).show();

                    } else if (Integer.parseInt(str_result) == 0) {

                        str_message = obj.getString("Message");

                        mySwitch.setEnabled(false);
                        mySwitch.setChecked(true);

                    } else if (Integer.parseInt(str_result) == 2) {
                        str_message = obj.getString("Message");

                        mySwitch.setEnabled(true);
                        mySwitch.setChecked(false);

                        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
                                if (isCheck) {
                                    markAttendance();
                                } else
                                    System.out.println("off");
                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Utilis.dismissProgress();
                Toast.makeText(AttendanceActivity.this, AttendanceActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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

                System.out.println(TAG + " getAttendance inputs " + params);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void markAttendance() {
        if (Utilis.isInternetOn()) {
            Utilis.showProgress(AttendanceActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.makeattendance, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " markAttendance response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " markAttendance result " + str_result);

                        if (Integer.parseInt(str_result) == 0) {
                            str_message = obj.getString("Message");
                            mySwitch.setEnabled(false);

//                            Calendar cal = Calendar.getInstance();
//                            int currentDate = cal.get(Calendar.DATE);
//                            System.out.println("Current Date "+currentDate);
//                            SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
//                            prefsEditor.putString("currentDate", String.valueOf(currentDate));
//                            prefsEditor.apply();

                            Toast.makeText(AttendanceActivity.this, str_message, Toast.LENGTH_SHORT).show();
                            Intent iback = new Intent(AttendanceActivity.this, MenuDashboardActivity.class);
                            iback.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(iback);
                            finish();
                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");
                            Toast.makeText(AttendanceActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        } else if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");
                            Toast.makeText(AttendanceActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(AttendanceActivity.this, AttendanceActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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

                    System.out.println(TAG + " markAttendance inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
        } else {
            Toast.makeText(this, AttendanceActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        back();
    }

    private void back() {
        if (isAttendance) {
            Intent iback = new Intent(AttendanceActivity.this, MenuDashboardActivity.class);
            iback.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(iback);
            finish();
        } else {
            finish();
        }
    }
}