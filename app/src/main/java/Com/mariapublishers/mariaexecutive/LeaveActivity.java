package Com.mariapublishers.mariaexecutive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class LeaveActivity extends AppCompatActivity {
    String TAG = "LeaveActivity";
    Toolbar toolbar;
    ActionBar actionBar = null;
    RecyclerView recyclerView;
    TextView tvNoRecords;
    MovableFloatingActionButton fab;

    Utilis utilis;
    static SharedPreferences mPrefs;
    UserInfo obj;
    Dialog dialog;
    EditText dateTxt, todateTxt;
    String str_result="", str_message="";
    ArrayList<Leave> listValue = new ArrayList<Leave>();

    Spinner spinReason;
    String[] reasons = { "Sick", "Grief", "Pay leave", "Personal leave", "Maternity/Paternity", "Others"};
    String selectedItem="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave);

        utilis = new Utilis(LeaveActivity.this);
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

        recyclerView = findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(LeaveActivity.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);

        tvNoRecords = findViewById(R.id.tv_no_records);

        fab = findViewById(R.id.add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveDialog();
            }
        });

        if(Integer.parseInt(obj.getRoleId())==1) {
            fab.setVisibility(View.GONE);
            adminViewLeave();
        } else {
            fab.setVisibility(View.VISIBLE);
            exeViewLeave();
        }
    }

    private void adminViewLeave() {
        if (Utilis.isInternetOn()) {
            Utilis.showProgress(LeaveActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.adminViewLeave, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " adminViewLeave response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " adminViewLeave result " + str_result);

                        if (Integer.parseInt(str_result) == 0) {
                            listValue.clear();

                            recyclerView.setVisibility(View.VISIBLE);
                            tvNoRecords.setVisibility(View.GONE);

                            str_message = obj.getString("Message");

                            JSONArray json = obj.getJSONArray("result");
                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObject = json.getJSONObject(i);
                                Leave leave = new Leave(
                                        jsonObject.getString("Leaveid"),
                                        jsonObject.getString("Reason"),
                                        jsonObject.getString("Fromdate"),
                                        jsonObject.getString("Todate"),
                                        jsonObject.getString("Userid"),
                                        jsonObject.getString("Username"),
                                        jsonObject.getString("Status"));

                                listValue.add(leave);

                            }

                            AdminLeaveAdapter adapter = new AdminLeaveAdapter(LeaveActivity.this, listValue);
                            recyclerView.setAdapter(adapter);


                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");
                            recyclerView.setVisibility(View.GONE);
                            tvNoRecords.setVisibility(View.VISIBLE);
                        } else if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");
                            Toast.makeText(LeaveActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(LeaveActivity.this, LeaveActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
            Toast.makeText(this, LeaveActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void exeViewLeave() {
        if (Utilis.isInternetOn()) {
            Utilis.showProgress(LeaveActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.viewLeave, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " exeViewLeave response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " exeViewLeave result " + str_result);

                        if (Integer.parseInt(str_result) == 0) {

                            listValue.clear();

                            recyclerView.setVisibility(View.VISIBLE);
                            tvNoRecords.setVisibility(View.GONE);

                            str_message = obj.getString("Message");

                            JSONArray json = obj.getJSONArray("result");
                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObject = json.getJSONObject(i);
                                Leave leave = new Leave(
                                        jsonObject.getString("Leaveid"),
                                        jsonObject.getString("Reason"),
                                        jsonObject.getString("Fromdate"),
                                        jsonObject.getString("Todate"),
                                        jsonObject.getString("Status"));

                                listValue.add(leave);

                            }

                            LeaveAdapter adapter = new LeaveAdapter(LeaveActivity.this, listValue);
                            recyclerView.setAdapter(adapter);


                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");
                            recyclerView.setVisibility(View.GONE);
                            tvNoRecords.setVisibility(View.VISIBLE);
                        } else if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");
                            Toast.makeText(LeaveActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(LeaveActivity.this, LeaveActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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

                    System.out.println(TAG + " exeViewLeave inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
        } else {
            Toast.makeText(this, LeaveActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void leaveDialog() {
        dialog = new Dialog(LeaveActivity.this, android.R.style.Theme_Translucent_NoTitleBar);

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

//        Date c = Calendar.getInstance().getTime();
////        System.out.println("Current time => " + c);
////
////        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
////        String formattedDate = df.format(c);
////        dateTxt.setText(formattedDate);

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
                    Toast.makeText(LeaveActivity.this, "Select Start Date", Toast.LENGTH_SHORT).show();
                } else if (toDate.isEmpty() || toDate.equalsIgnoreCase("Select End Date")) {
                    Toast.makeText(LeaveActivity.this, "Select End Date", Toast.LENGTH_SHORT).show();
                } else {
                    if (selectedItem.equalsIgnoreCase("Others")) {
                        if (str_desc.isEmpty()) {
                            Toast.makeText(LeaveActivity.this, "Enter Reason for Leave", Toast.LENGTH_SHORT).show();
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

        DatePickerDialog dpd = new DatePickerDialog(LeaveActivity.this, new DatePickerDialog.OnDateSetListener() {

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

            Utilis.showProgress(LeaveActivity.this);

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

                            Toast.makeText(LeaveActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        } else if (Integer.parseInt(str_result) == 0) {

                            str_message = obj.getString("Message");
                            Toast.makeText(LeaveActivity.this, str_message, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            exeViewLeave();

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");

                            Toast.makeText(LeaveActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(LeaveActivity.this, LeaveActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
            Toast.makeText(this, LeaveActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void selectDate() {
        Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(LeaveActivity.this, new DatePickerDialog.OnDateSetListener() {

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        back();
    }

    private void back() {
        Intent iback = new Intent(LeaveActivity.this, MenuDashboardActivity.class);
        iback.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(iback);
        finish();

    }

    private class LeaveAdapter extends RecyclerView.Adapter<ViewHolder> {
        Activity mActivity;
        private ArrayList<Leave> arrayList;
        public LeaveAdapter(Activity leaveActivity, ArrayList<Leave> listValue) {
            mActivity = leaveActivity;
            arrayList = listValue;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leave_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvDate.setText(arrayList.get(position).getFromDate());
            holder.tvDesc.setText(arrayList.get(position).getReason());
            holder.tvEndDate.setText(arrayList.get(position).getToDate());
            holder.tvStatus.setText(arrayList.get(position).getStatus());
            if ("Approved".equalsIgnoreCase(arrayList.get(position).getStatus())) {
                holder.layStatus.setBackgroundColor(getResources().getColor(R.color.approved));
            } else if ("Pending".equalsIgnoreCase(arrayList.get(position).getStatus())) {
                holder.layStatus.setBackgroundColor(getResources().getColor(R.color.pending));
            } else {
                holder.tvStatus.setText("Rejected");
                holder.layStatus.setBackgroundColor(getResources().getColor(R.color.rejected));
            }
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvDesc, tvEndDate, tvStatus;
        LinearLayout layStatus;

        public ViewHolder(@NonNull View view) {
            super(view);
            tvDate = view.findViewById(R.id.tv_date);
            tvDesc = view.findViewById(R.id.tv_desc);
            tvEndDate = view.findViewById(R.id.tv_end_date);
            layStatus = view.findViewById(R.id.lay_status);
            tvStatus = view.findViewById(R.id.tv_status);
        }
    }

    private class AdminLeaveAdapter extends RecyclerView.Adapter<MyViewHolder> {
        Activity mActivity;
        private ArrayList<Leave> arrayList;
        public AdminLeaveAdapter(Activity leaveActivity, ArrayList<Leave> listValue) {
            mActivity = leaveActivity;
            arrayList = listValue;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_leave_list_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
holder.tvDate.setText(arrayList.get(position).getFromDate());
holder.tvName.setText(arrayList.get(position).getUserName());
holder.tvDesc.setText(arrayList.get(position).getReason());
holder.tvEndDate.setText(arrayList.get(position).getToDate());

if ("Pending".equalsIgnoreCase(arrayList.get(position).getStatus())) {
    holder.layPending.setVisibility(View.VISIBLE);
    holder.layStatus.setVisibility(View.GONE);
} else {
    holder.layPending.setVisibility(View.GONE);
    holder.layStatus.setVisibility(View.VISIBLE);
}

            if ("Approved".equalsIgnoreCase(arrayList.get(position).getStatus())) {
                holder.layStatus.setBackgroundColor(getResources().getColor(R.color.approved));
                holder.tvStatus.setText(arrayList.get(position).getStatus());
            } else if ("Reject".equalsIgnoreCase(arrayList.get(position).getStatus())){
                holder.layStatus.setBackgroundColor(getResources().getColor(R.color.rejected));
                holder.tvStatus.setText("Rejected");
            }

            holder.btnApprove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    approveLeave(arrayList.get(position).getLeaveId());
                }
            });


            holder.btnReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    rejectLeave(arrayList.get(position).getLeaveId());
                }
            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }

    private void rejectLeave(final String leaveId) {
        if (Utilis.isInternetOn()) {
            Utilis.showProgress(LeaveActivity.this);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.rejectleave, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " rejectLeave response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " rejectLeave result " + str_result);
                        if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");

                            Toast.makeText(LeaveActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        } else if (Integer.parseInt(str_result) == 0) {

                            str_message = obj.getString("Message");

                            mPrefs = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
                            Gson gson = new Gson();
                            String prefJson = mPrefs.getString("MyObject", "");
                            UserInfo userInfo = gson.fromJson(prefJson, UserInfo.class);

                            if(Integer.parseInt(userInfo.getRoleId())==1) {
                                adminViewLeave();
                            } else {
                                exeViewLeave();
                            }

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");

                            Toast.makeText(LeaveActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();

                    Toast.makeText(LeaveActivity.this, LeaveActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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

                    params.put("Leaveid", leaveId);

                    System.out.println(TAG + " rejectLeave inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);

        } else {
            Toast.makeText(this, LeaveActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void approveLeave(final String leaveId) {
        if (Utilis.isInternetOn()) {
            Utilis.showProgress(LeaveActivity.this);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.approveleave, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " approveLeave response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " approveLeave result " + str_result);
                        if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");

                            Toast.makeText(LeaveActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        } else if (Integer.parseInt(str_result) == 0) {

                            str_message = obj.getString("Message");

                            mPrefs = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
                            Gson gson = new Gson();
                            String prefJson = mPrefs.getString("MyObject", "");
                            UserInfo userInfo = gson.fromJson(prefJson, UserInfo.class);

                            if(Integer.parseInt(userInfo.getRoleId())==1) {
                                adminViewLeave();
                            } else {
                                exeViewLeave();
                            }

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");

                            Toast.makeText(LeaveActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();

                    Toast.makeText(LeaveActivity.this, LeaveActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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

                    params.put("Leaveid", leaveId);

                    System.out.println(TAG + " approveLeave inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);

        } else {
            Toast.makeText(this, LeaveActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvDesc, tvName, tvEndDate, tvStatus;
        LinearLayout layStatus, layPending;
        Button btnApprove, btnReject;

        public MyViewHolder(@NonNull View view) {
            super(view);
            tvDate = view.findViewById(R.id.tv_date);
            tvEndDate = view.findViewById(R.id.tv_end_date);
            tvName = view.findViewById(R.id.tv_name);
            tvDesc = view.findViewById(R.id.tv_desc);
            tvStatus = view.findViewById(R.id.tv_status);
            layStatus = view.findViewById(R.id.lay_status);
            layPending = view.findViewById(R.id.lay_pending);
            btnApprove = view.findViewById(R.id.btnApprove);
            btnReject = view.findViewById(R.id.btnReject);
        }
    }
}