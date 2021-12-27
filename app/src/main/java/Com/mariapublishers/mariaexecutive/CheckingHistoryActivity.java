package Com.mariapublishers.mariaexecutive;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CheckingHistoryActivity extends AppCompatActivity {
    String TAG = "CheckingHistoryActivity";
    Toolbar toolbar;
    ActionBar actionBar = null;
    RecyclerView recyclerView;
    TextView tvNoRecords;

    Utilis utilis;
    static SharedPreferences mPrefs;
    UserInfo obj;

    String str_result = "", str_message = "";
    ArrayList<CheckIn> listValue = new ArrayList<CheckIn>();
    String keyIntent = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checking_history);
        utilis = new Utilis(CheckingHistoryActivity.this);
        mPrefs = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("MyObject", "");
        obj = gson.fromJson(json, UserInfo.class);

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
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(CheckingHistoryActivity.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);

        tvNoRecords = findViewById(R.id.tv_no_records);


        if (Utilis.isInternetOn()) {
            getHistory();
        } else {
            Toast.makeText(this, CheckingHistoryActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }

    }

    private void getHistory() {
        Utilis.showProgress(CheckingHistoryActivity.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.checkinHistory, new Response.Listener<String>() {
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
                        tvNoRecords.setVisibility(View.GONE);

                        str_message = obj.getString("Message");

                        JSONArray json = obj.getJSONArray("result");
                        for (int i = 0; i < json.length(); i++) {
                            JSONObject jsonObject = json.getJSONObject(i);
                            CheckIn checkIn = new CheckIn(
                                    jsonObject.getString("Datetime"),
                                    jsonObject.getString("Address"),
                                    jsonObject.getString("Userid"),
                                    jsonObject.getString("Date"),
                                    jsonObject.getString("CheckinId"),
                                    jsonObject.getString("Ischeckout"),
                                    jsonObject.getString("CheckoutDateTime"));

                            listValue.add(checkIn);

                        }

                        CheckInAdapter adapter = new CheckInAdapter(CheckingHistoryActivity.this, listValue);
                        recyclerView.setAdapter(adapter);

                    } else if (Integer.parseInt(str_result) == 2) {
                        str_message = obj.getString("Message");
                        recyclerView.setVisibility(View.GONE);
                        tvNoRecords.setVisibility(View.VISIBLE);
                    } else if (Integer.parseInt(str_result) == 1) {
                        str_message = obj.getString("Message");
                        Toast.makeText(CheckingHistoryActivity.this, str_message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Utilis.dismissProgress();
                Toast.makeText(CheckingHistoryActivity.this, CheckingHistoryActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
                System.out.println(TAG + " getHistory inputs " + params);
                return params;
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
        if (keyIntent.equalsIgnoreCase("Dash")) {
            Intent iback = new Intent(CheckingHistoryActivity.this, MenuDashboardActivity.class);
            iback.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(iback);
            finish();
        } else {
            Intent iback = new Intent(CheckingHistoryActivity.this, MainActivity.class);
            iback.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(iback);
            finish();
        }
    }

    private class CheckInAdapter extends RecyclerView.Adapter<ViewHolder> {
        Activity mActivity;
        private ArrayList<CheckIn> arrayList;

        Spinner spinCallType;
        String[] callType = {"Select Call Type", "General", "Order", "Payment Collection", "Returns", "Promotion"};
        String selectedItemCallType="";

        Spinner spinPayType;
        String[] payType = { "Select Payment Type", "Cheque", "Cash", "NEFT / RTGS"};
        String selectedItemPayType="";

        Spinner spinDrivingType;
        String[] drivingType = {"Select Driving Type", "Two Wheeler", "Car", "Public Transport"};
        String selectedItemDrivingType = "";

//        RadioButton radioButton;

        public CheckInAdapter(Activity activity, ArrayList<CheckIn> listValue) {
            this.mActivity = activity;
            this.arrayList = listValue;
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
            holder.tvName.setText(obj.getName());
            if (arrayList.get(position).getIscheckout().equals("0")) {
                holder.layCheckout.setVisibility(View.VISIBLE);
                holder.layCheckoutView.setVisibility(View.GONE);
            } else {
                holder.layCheckout.setVisibility(View.GONE);
                holder.layCheckoutView.setVisibility(View.VISIBLE);
                holder.tvState.setText(obj.getState());
                holder.tvCheckoutDateTime.setText(arrayList.get(position).getCheckoutDateTime());
            }
            holder.btnCheckout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog dialog = new Dialog(mActivity, android.R.style.Theme_Translucent_NoTitleBar);

                    dialog.setCancelable(false);
                    dialog.getWindow().setContentView(R.layout.check_out_alert_dialog);
                    dialog.show();

                    final EditText etAmount = dialog.findViewById(R.id.et_amt);

//                    final RadioGroup radioGroup = dialog.findViewById(R.id.radioGroup);

                    spinCallType = dialog.findViewById(R.id.spin_call_type);
                    spinPayType = dialog.findViewById(R.id.spin_payment_type);
                    spinDrivingType = dialog.findViewById(R.id.spin_driving_type);


                    //Creating the ArrayAdapter instance having the country list
                    ArrayAdapter callAdapter = new ArrayAdapter(mActivity,android.R.layout.simple_spinner_item,callType);
                    callAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Setting the ArrayAdapter data on the Spinner
                    spinCallType.setAdapter(callAdapter);


                    //Creating the ArrayAdapter instance having the country list
                    ArrayAdapter payAdapter = new ArrayAdapter(mActivity,android.R.layout.simple_spinner_item,payType);
                    payAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Setting the ArrayAdapter data on the Spinner
                    spinPayType.setAdapter(payAdapter);

                    //Creating the ArrayAdapter instance having the country list
                    ArrayAdapter drivingAdapter = new ArrayAdapter(mActivity,android.R.layout.simple_spinner_item,drivingType);
                    drivingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Setting the ArrayAdapter data on the Spinner
                    spinDrivingType.setAdapter(drivingAdapter);

                    spinCallType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            selectedItemCallType = callType[i].toString();
                            if (selectedItemCallType.equalsIgnoreCase("Select Call Type")) {
                                selectedItemCallType = "";
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    spinPayType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            selectedItemPayType = payType[i].toString();
                            if (selectedItemPayType.equalsIgnoreCase("Select Payment Type")) {
                               selectedItemPayType = "";
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    spinDrivingType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            selectedItemDrivingType = drivingType[i].toString();
                            if (selectedItemDrivingType.equalsIgnoreCase("Select Driving Type")) {
                                selectedItemDrivingType = "";
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    Button submitBtn = dialog.findViewById(R.id.submitbtn);
                    Button cancelBtn = dialog.findViewById(R.id.cancelbtn);
                    final EditText etDesc = dialog.findViewById(R.id.et_desc);

                    submitBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String checkOutDesc = etDesc.getText().toString().trim();
//                            int selectedId = radioGroup.getCheckedRadioButtonId();
//                            radioButton = (RadioButton) dialog.findViewById(selectedId);
                            String collectionAmt = etAmount.getText().toString().trim();

//                            if(selectedId==-1){
//                                Toast.makeText(mActivity,"Select Type of Driving", Toast.LENGTH_SHORT).show();
//                            }
                            if(selectedItemDrivingType.isEmpty()) {
                                Toast.makeText(mActivity,"Select Type of Driving", Toast.LENGTH_SHORT).show();
                            }
                            else if (checkOutDesc.isEmpty()) {
                                Toast.makeText(mActivity, "Enter Description", Toast.LENGTH_SHORT).show();
                            } else {
                                checkOut(mActivity, arrayList.get(position).getCheckinId(), checkOutDesc, dialog, selectedItemCallType, selectedItemPayType, collectionAmt, selectedItemDrivingType); //radioButton.getText().toString());
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
            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }

    private void checkOut(final Activity activity, final String checkinId, final String checkOutDesc, final Dialog dialog, final String CallType, final String PayType, final String collectionAmt, final String drivingMode) {
        if (Utilis.isInternetOn()) {

            Utilis.showProgress(activity);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.updatecheckout, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " checkOut response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " checkOut result " + str_result);

                        if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");

                            Toast.makeText(activity, str_message, Toast.LENGTH_SHORT).show();

                        } else if (Integer.parseInt(str_result) == 0) {

                            str_message = obj.getString("Message");
                            Toast.makeText(activity, str_message, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();

                            getHistory();

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");

                            Toast.makeText(activity, str_message, Toast.LENGTH_SHORT).show();

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(activity, activity.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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

                    params.put("CheckinId", checkinId);
                    params.put("Checkoutdesc", checkOutDesc);
                    params.put("Calltype", CallType);
                    params.put("Paymenttype", PayType);
                    params.put("Collectionamount", collectionAmt);
                    params.put("Drivingmode", drivingMode);

                    System.out.println(TAG + " checkOut inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);

        } else {
            Toast.makeText(activity, activity.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAddress, tvDateTime, tvName;
        LinearLayout layCheckout;
        Button btnCheckout;
        LinearLayout layCheckoutView;
        TextView tvState, tvCheckoutDateTime;

        public ViewHolder(@NonNull View view) {
            super(view);
            tvAddress = view.findViewById(R.id.tv_checkinaddress);
            tvDateTime = view.findViewById(R.id.tv_date_time);
            tvName = view.findViewById(R.id.tv_name);
            layCheckout = view.findViewById(R.id.lay_checkout);
            btnCheckout = view.findViewById(R.id.btn_checkout);
            layCheckoutView = view.findViewById(R.id.lay_checkout_view);
            tvState = view.findViewById(R.id.tv_state);
            tvCheckoutDateTime = view.findViewById(R.id.tv_checkout_date_time);
            view.setTag(itemView);
        }
    }
}