package Com.mariapublishers.mariaexecutive;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
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
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class OthersCheckingHistoryActivity extends AppCompatActivity {
    String TAG = "OthersCheckingHistoryActivity";
    Toolbar toolbar;
    ActionBar actionBar = null;
    RecyclerView recyclerView;
    CheckInAdapter adapter;
    TextView tvNoRecords;
    LinearLayout listLayout;
    Spinner spinExecutive, spinState;
    Button btnSubmit, btnClear;
    LinearLayout dateLayout, toDateLayout;
    EditText dateTxt, toDateTxt;
    ImageView imgClear, toImgClear;

    Utilis utilis;

    String str_result = "", str_message = "";
    ArrayList<AdminCheckInHistory> listValue = new ArrayList<AdminCheckInHistory>();
    ArrayList<LatLngTracker> latLngTrackerArrayList = new ArrayList<LatLngTracker>();
    List<String> executiveName = new ArrayList<>();
    List<AdminCheckInHistory> executiveListValue = new ArrayList<>();
    ArrayList<String> values=new ArrayList<String>();
    String selectedUserId="", selectedState="";
    String keyIntent = "";
    String[] stateList = {"Select State", "Tamil Nadu", "Kerala", "Andhra Pradesh", "Karnataka", "Pondicherry"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_others_checking_history);

        utilis = new Utilis(OthersCheckingHistoryActivity.this);

        Intent intent = getIntent();
        keyIntent = intent.getStringExtra("key");

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
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(OthersCheckingHistoryActivity.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);

        tvNoRecords = findViewById(R.id.tv_no_records);
        listLayout = findViewById(R.id.layout_filter);
        spinExecutive = findViewById(R.id.spin_executive);
        spinState = findViewById(R.id.spin_state);
        dateLayout = findViewById(R.id.date_layout);
        dateTxt = findViewById(R.id.date_txt);
        imgClear = findViewById(R.id.ivClear);
        toDateLayout = findViewById(R.id.to_date_layout);
        toDateTxt = findViewById(R.id.to_date_txt);
        toImgClear = findViewById(R.id.ivToClear);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnClear = findViewById(R.id.btnClear);

        if (Utilis.isInternetOn()) {
            getHistory(true);
        } else {
            Toast.makeText(this, OthersCheckingHistoryActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }

        spinExecutive.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedUserId = executiveListValue.get(i).getUserId();
//                adapter.filter(selectedUserId, dateTxt.getText().toString());
//                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedState = stateList[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        dateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDate();
            }
        });

        dateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDate();
            }
        });

        toDateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectToDate();
            }
        });

        toDateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectToDate();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                adapter.filter(selectedUserId, dateTxt.getText().toString());
//                adapter.notifyDataSetChanged();
                if(selectedUserId.equalsIgnoreCase("-1") && selectedState.equalsIgnoreCase("Select State")) {
                    Toast.makeText(OthersCheckingHistoryActivity.this, "Select Executive / State", Toast.LENGTH_SHORT).show();
                } else if (dateTxt.getText().toString().equalsIgnoreCase("From Date")) {
                    Toast.makeText(OthersCheckingHistoryActivity.this, "Select From Date", Toast.LENGTH_SHORT).show();
                } else if (toDateTxt.getText().toString().equalsIgnoreCase("To Date")) {
                    Toast.makeText(OthersCheckingHistoryActivity.this, "Select To Date", Toast.LENGTH_SHORT).show();
                } else {
                    selectedUserId = selectedUserId.equalsIgnoreCase("-1") ? "" : selectedUserId;
                    selectedState = selectedState.equalsIgnoreCase("Select State") ? "" : selectedState;
                    filterDataApi(selectedUserId, selectedState, dateTxt.getText().toString(), toDateTxt.getText().toString());
                }
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedUserId="";
                selectedState="";
                spinExecutive.setSelection(0);
                spinState.setSelection(0);
                dateTxt.setText("From Date");
                imgClear.setVisibility(View.GONE);
                toDateTxt.setText("To Date");
                toImgClear.setVisibility(View.GONE);

                if (Utilis.isInternetOn()) {
                    getHistory(false);
                } else {
                    Toast.makeText(OthersCheckingHistoryActivity.this, OthersCheckingHistoryActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
                }
            }
        });

        imgClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateTxt.setText("From Date");
                imgClear.setVisibility(View.GONE);
//                adapter.filter(selectedUserId, dateTxt.getText().toString());
//                adapter.notifyDataSetChanged();
            }
        });

        toImgClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toDateTxt.setText("To Date");
                toImgClear.setVisibility(View.GONE);
//                adapter.filter(selectedUserId, dateTxt.getText().toString());
//                adapter.notifyDataSetChanged();
            }
        });
    }

    private void filterDataApi(final String selectedUserId, final String selectedState, final String fromDate, final String toDate) {
        if (Utilis.isInternetOn()) {
            Utilis.showProgress(OthersCheckingHistoryActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.adminviewcheckinsearch, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " filterDataApi response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " filterDataApi result " + str_result);

                        if (Integer.parseInt(str_result) == 0) {
                            listValue.clear();
                            recyclerView.setVisibility(View.VISIBLE);
                            listLayout.setVisibility(View.VISIBLE);
                            tvNoRecords.setVisibility(View.GONE);

                            str_message = obj.getString("Message");

                            JSONArray json = obj.getJSONArray("result");
                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObject = json.getJSONObject(i);
                                AdminCheckInHistory checkIn = new AdminCheckInHistory(
                                        jsonObject.getString("Name"),
                                        jsonObject.getString("Userid"),
                                        jsonObject.getString("Latitude"),
                                        jsonObject.getString("Longitude"),
                                        jsonObject.getString("Address"),
                                        jsonObject.getString("Description"),
                                        jsonObject.getString("Datetime"),
                                        jsonObject.getString("Date"),
                                        jsonObject.getString("CheckoutDateTime"),
                                        jsonObject.getString("Ischeckout"),
                                        jsonObject.getString("State"),
                                        jsonObject.getString("Checkoutdesc"));

                                listValue.add(checkIn);
                            }

                            adapter = new CheckInAdapter(OthersCheckingHistoryActivity.this, listValue);
                            recyclerView.setAdapter(adapter);


                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");
                            recyclerView.setVisibility(View.GONE);
                            listLayout.setVisibility(View.VISIBLE);
                            tvNoRecords.setVisibility(View.VISIBLE);
                        } else if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");
                            Toast.makeText(OthersCheckingHistoryActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(OthersCheckingHistoryActivity.this, OthersCheckingHistoryActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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

                    params.put("Userid", selectedUserId);
                    params.put("State", selectedState);
                    params.put("Fromdate", fromDate);
                    params.put("Todate", toDate);

                    System.out.println(TAG + " filterDataApi inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
        } else {
            Toast.makeText(this, OthersCheckingHistoryActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void selectToDate() {
        Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(OthersCheckingHistoryActivity.this, new DatePickerDialog.OnDateSetListener() {

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

                toDateTxt.setText(date);
                toImgClear.setVisibility(View.VISIBLE);
//                adapter.filter(selectedUserId, dateTxt.getText().toString());
//                adapter.notifyDataSetChanged();

            }
        }, mYear, mMonth, mDay);
        dpd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //ignored
            }
        });
        dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
        dpd.show();
    }

    private void selectDate() {
        Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(OthersCheckingHistoryActivity.this, new DatePickerDialog.OnDateSetListener() {

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
                imgClear.setVisibility(View.VISIBLE);
//                adapter.filter(selectedUserId, dateTxt.getText().toString());
//                adapter.notifyDataSetChanged();

            }
        }, mYear, mMonth, mDay);
        dpd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //ignored
            }
        });
        dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
        dpd.show();
    }

    private void getHistory(final boolean myFlag) {
        Utilis.showProgress(OthersCheckingHistoryActivity.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.adminviewcheckin, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    //converting response to json object
                    JSONObject obj = new JSONObject(response);

                    System.out.println(TAG + " getHistory response - " + response);

                    Utilis.dismissProgress();

                    str_result = obj.getString("errorCode");
                    System.out.print(TAG + " getHistory result " + str_result);

                    if (Integer.parseInt(str_result) == 0) {
                        listValue.clear();
                        recyclerView.setVisibility(View.VISIBLE);
                        listLayout.setVisibility(View.VISIBLE);
                        tvNoRecords.setVisibility(View.GONE);

                        str_message = obj.getString("Message");

                        JSONArray json = obj.getJSONArray("result");
                        for (int i = 0; i < json.length(); i++) {
                            JSONObject jsonObject = json.getJSONObject(i);
                            AdminCheckInHistory checkIn = new AdminCheckInHistory(
                                    jsonObject.getString("Name"),
                                    jsonObject.getString("Userid"),
                                    jsonObject.getString("Latitude"),
                                    jsonObject.getString("Longitude"),
                                    jsonObject.getString("Address"),
                                    jsonObject.getString("Description"),
                                    jsonObject.getString("Datetime"),
                                    jsonObject.getString("Date"),
                                    jsonObject.getString("CheckoutDateTime"),
                                    jsonObject.getString("Ischeckout"),
                                    jsonObject.getString("State"),
                                    jsonObject.getString("Checkoutdesc"));

                            listValue.add(checkIn);
                            values.add(jsonObject.getString("Userid"));
                        }

                        adapter = new CheckInAdapter(OthersCheckingHistoryActivity.this, listValue);
                        recyclerView.setAdapter(adapter);


                        if (myFlag) {
                            HashSet<String> hashSet = new HashSet<String>();
                            hashSet.addAll(values);
                            values.clear();
                            values.addAll(hashSet);

                            for (AdminCheckInHistory adminCheckInHistory : listValue) {
                                for (int i =0; i < values.size(); i++) {
                                    if (adminCheckInHistory.getUserId().equals(values.get(i))) {
                                        if (!executiveName.contains(adminCheckInHistory.getName())) {
                                            executiveName.add(adminCheckInHistory.getName());
                                            executiveListValue.add(adminCheckInHistory);
                                        }
                                    }
                                }
                            }

                            AdminCheckInHistory adminCheckInHistory = new AdminCheckInHistory();
                            adminCheckInHistory.setName("Select Executive");
                            adminCheckInHistory.setUserId("-1");
                            executiveListValue.add(0, adminCheckInHistory);
                            executiveName.add(0, "Select Executive");

                            //Creating the ArrayAdapter instance having the country list
                            ArrayAdapter<String> aa = new ArrayAdapter<>(OthersCheckingHistoryActivity.this,android.R.layout.simple_spinner_item,executiveName);
                            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            //Setting the ArrayAdapter data on the Spinner
                            spinExecutive.setAdapter(aa);
                            spinExecutive.setSelection(0);

                            //Creating the ArrayAdapter instance having the country list
                            ArrayAdapter arrayAdapter = new ArrayAdapter(OthersCheckingHistoryActivity.this,android.R.layout.simple_spinner_item,stateList);
                            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            //Setting the ArrayAdapter data on the Spinner
                            spinState.setAdapter(arrayAdapter);
                            spinState.setSelection(0);
                        }

                    } else if (Integer.parseInt(str_result) == 2) {
                        str_message = obj.getString("Message");
                        recyclerView.setVisibility(View.GONE);
                        listLayout.setVisibility(View.GONE);
                        tvNoRecords.setVisibility(View.VISIBLE);
                    } else if (Integer.parseInt(str_result) == 1) {
                        str_message = obj.getString("Message");
                        Toast.makeText(OthersCheckingHistoryActivity.this, str_message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Utilis.dismissProgress();
                Toast.makeText(OthersCheckingHistoryActivity.this, OthersCheckingHistoryActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        back();
    }

    private void back() {
        if(keyIntent.equalsIgnoreCase("Dash")) {
            Intent iback = new Intent(OthersCheckingHistoryActivity.this, MenuDashboardActivity.class);
            iback.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(iback);
            finish();
        } else {
            Intent iback = new Intent(OthersCheckingHistoryActivity.this, AdminActivity.class);
            iback.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(iback);
            finish();
        }
    }

    private class CheckInAdapter extends RecyclerView.Adapter<ViewHolder> {
        Activity mActivity;
        private ArrayList<AdminCheckInHistory> arrayList;
        private List<AdminCheckInHistory> filterDataItem = new ArrayList<>();

        public CheckInAdapter(Activity activity, ArrayList<AdminCheckInHistory> listValue) {
            this.mActivity = activity;
            this.arrayList = listValue;
            this.filterDataItem.addAll(arrayList);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.checking_history_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            holder.tvAddress.setText(arrayList.get(position).getAddress());
            holder.tvDateTime.setText(arrayList.get(position).getDateTime());
            holder.tvName.setText(arrayList.get(position).getName());

            holder.layDesc.setVisibility(View.VISIBLE);
            holder.layDesc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog dialog = new Dialog(mActivity, android.R.style.Theme_Translucent_NoTitleBar);

                    dialog.setCancelable(false);
                    dialog.getWindow().setContentView(R.layout.desc_layout);
                    dialog.show();


                    Button cancelBtn = dialog.findViewById(R.id.closebtn);
                    TextView tvCheckInDesc = dialog.findViewById(R.id.tv_checkindesc);
                    TextView tvCheckOutDesc = dialog.findViewById(R.id.tv_checkoutdesc);

                    String checkInDesc = arrayList.get(position).getDescription();
                    String checkOutDesc = arrayList.get(position).getCheckoutDesc();

                    if (!checkInDesc.equals("") && !checkInDesc.equals("null"))
                        tvCheckInDesc.setText(checkInDesc);
                    else
                        tvCheckInDesc.setText("-");

                    if (!checkOutDesc.equals("") && !checkOutDesc.equals("null"))
                        tvCheckOutDesc.setText(checkOutDesc);
                    else
                        tvCheckOutDesc.setText("-");

                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                        }
                    });
                }
            });

            if (arrayList.get(position).getIscheckout().equals("1")) {
                holder.layCheckoutView.setVisibility(View.VISIBLE);
                holder.tvState.setText(arrayList.get(position).getState());
                holder.tvCheckoutDateTime.setText(arrayList.get(position).getCheckoutDateTime());
            } else {
                holder.layCheckoutView.setVisibility(View.GONE);
            }

            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String clickedUser = arrayList.get(position).getUserId();
                    String clickedDate = arrayList.get(position).getDate();
                    if (Utilis.isInternetOn()) {
                        getMapView(clickedUser, clickedDate);
                    } else {
                        Toast.makeText(OthersCheckingHistoryActivity.this, OthersCheckingHistoryActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }

        private void getMapView(final String clickedUser, final String clickedDate) {

            Utilis.showProgress(OthersCheckingHistoryActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.getlatlng, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " getMapView response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " getMapView result " + str_result);

                        if (Integer.parseInt(str_result) == 0) {

                            str_message = obj.getString("Message");

                            JSONArray json = obj.getJSONArray("result");
                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObject = json.getJSONObject(i);
                                LatLngTracker latLngTracker = new LatLngTracker(
                                        jsonObject.getDouble("Latitude"),
                                        jsonObject.getDouble("Longitude"),
                                        jsonObject.getString("Address"));

                                latLngTrackerArrayList.add(latLngTracker);

                            }

                            System.out.println("getMapView array size " + latLngTrackerArrayList.size());

                            Intent intent = new Intent(OthersCheckingHistoryActivity.this, OthersCheckingHistoryMapActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putParcelableArrayList("mylist", latLngTrackerArrayList);
                            intent.putExtras(bundle);
                            intent.putExtra("key", keyIntent);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");
                            Toast.makeText(OthersCheckingHistoryActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        } else if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");
                            Toast.makeText(OthersCheckingHistoryActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(OthersCheckingHistoryActivity.this, OthersCheckingHistoryActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
                    params.put("Userid", clickedUser);
                    params.put("Date", clickedDate);
                    System.out.println(TAG + " getMapView inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(OthersCheckingHistoryActivity.this).addToRequestQueue(stringRequest);

        }

        public void filter(String selectedUserId, String selectedDate) {
            arrayList.clear();
            if (selectedUserId.equals("-1") && selectedDate.equals("Select Date")) {
                arrayList.addAll(filterDataItem);
            } else if(!selectedUserId.equals("-1") && selectedDate.equals("Select Date")) {
                for (AdminCheckInHistory adminCheckInHistory : filterDataItem) {
                    if(adminCheckInHistory.getUserId().contains(selectedUserId)) {
                        arrayList.add(adminCheckInHistory);
                    }
                }
            } else if(selectedUserId.equals("-1") && !selectedDate.equals("Select Date")) {
                for (AdminCheckInHistory adminCheckInHistory : filterDataItem) {
                    if(adminCheckInHistory.getDate().contains(selectedDate)) {
                        arrayList.add(adminCheckInHistory);
                    }
                }
            } else {
                for (AdminCheckInHistory adminCheckInHistory : filterDataItem) {
                    if(adminCheckInHistory.getUserId().contains(selectedUserId) &&
                            adminCheckInHistory.getDate().contains(selectedDate)) {
                        arrayList.add(adminCheckInHistory);
                    }
                }
            }

            for (AdminCheckInHistory adminCheckInHistory : filterDataItem) {
                if(adminCheckInHistory.getUserId().contains(selectedUserId) &&
                        adminCheckInHistory.getDate().contains(selectedDate)) {
                    arrayList.add(adminCheckInHistory);
                }
            }
            notifyDataSetChanged();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAddress, tvDateTime, tvName;
        CardView cardView;
        LinearLayout layCheckoutView, layDesc;
        TextView tvState, tvCheckoutDateTime;

        public ViewHolder(@NonNull View view) {
            super(view);
            tvAddress = view.findViewById(R.id.tv_checkinaddress);
            tvDateTime = view.findViewById(R.id.tv_date_time);
            tvName = view.findViewById(R.id.tv_name);
            cardView = view.findViewById(R.id.cardLayout);
            layCheckoutView = view.findViewById(R.id.lay_checkout_view);
            tvState = view.findViewById(R.id.tv_state);
            tvCheckoutDateTime  = view.findViewById(R.id.tv_checkout_date_time);
            layDesc  = view.findViewById(R.id.lay_desc);
            view.setTag(itemView);
        }
    }
}
