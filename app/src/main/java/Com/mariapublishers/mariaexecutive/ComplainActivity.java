package Com.mariapublishers.mariaexecutive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.HashMap;
import java.util.Map;

public class ComplainActivity extends AppCompatActivity {
    String TAG = "ComplainActivity";
    Toolbar toolbar;
    ActionBar actionBar = null;
    RecyclerView recyclerView;
    TextView tvNoRecords;
    MovableFloatingActionButton fab;

    Utilis utilis;
    static SharedPreferences mPrefs;
    UserInfo obj;
    Dialog dialog;
    String str_result="", str_message="";
    ArrayList<Complains> listValue = new ArrayList<Complains>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complain);

        utilis = new Utilis(ComplainActivity.this);
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
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ComplainActivity.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);

        tvNoRecords = findViewById(R.id.tv_no_records);

        fab = findViewById(R.id.add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                complainDialog();
            }
        });

        if(Integer.parseInt(obj.getRoleId())==1) {
            fab.setVisibility(View.GONE);
            adminViewComplains();
        } else {
            fab.setVisibility(View.VISIBLE);
            exeViewComplains();
        }
    }

    private void exeViewComplains() {
        if (Utilis.isInternetOn()) {
            Utilis.showProgress(ComplainActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.viewEnquiry, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " exeViewComplains response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " exeViewComplains result " + str_result);

                        if (Integer.parseInt(str_result) == 0) {
                            listValue.clear();

                            recyclerView.setVisibility(View.VISIBLE);
                            tvNoRecords.setVisibility(View.GONE);

                            str_message = obj.getString("Message");

                            JSONArray json = obj.getJSONArray("result");
                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObject = json.getJSONObject(i);
                                Complains complains = new Complains(
                                        jsonObject.getString("Enquiryid"),
                                        jsonObject.getString("Description"),
                                        jsonObject.getString("Datetime"));

                                listValue.add(complains);
                            }

                            ExeComplainAdapter adapter = new ExeComplainAdapter(ComplainActivity.this, listValue);
                            recyclerView.setAdapter(adapter);


                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");
                            recyclerView.setVisibility(View.GONE);
                            tvNoRecords.setVisibility(View.VISIBLE);
                        } else if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");
                            Toast.makeText(ComplainActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(ComplainActivity.this, ComplainActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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

                    System.out.println(TAG + " exeViewComplains inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
        } else {
            Toast.makeText(this, ComplainActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void adminViewComplains() {
        if (Utilis.isInternetOn()) {
            Utilis.showProgress(ComplainActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.adminViewEnquiry, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " adminViewComplains response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " adminViewComplains result " + str_result);

                        if (Integer.parseInt(str_result) == 0) {
listValue.clear();
                            recyclerView.setVisibility(View.VISIBLE);
                            tvNoRecords.setVisibility(View.GONE);

                            str_message = obj.getString("Message");

                            JSONArray json = obj.getJSONArray("result");
                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObject = json.getJSONObject(i);
                                Complains complains = new Complains(
                                        jsonObject.getString("Enquiryid"),
                                        jsonObject.getString("Description"),
                                        jsonObject.getString("Datetime"),
                                        jsonObject.getString("Userid"),
                                        jsonObject.getString("Username"));

                                listValue.add(complains);
                            }

                            AdminComplainAdapter adapter = new AdminComplainAdapter(ComplainActivity.this, listValue);
                            recyclerView.setAdapter(adapter);


                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");
                            recyclerView.setVisibility(View.GONE);
                            tvNoRecords.setVisibility(View.VISIBLE);
                        } else if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");
                            Toast.makeText(ComplainActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(ComplainActivity.this, ComplainActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
            Toast.makeText(this, ComplainActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void complainDialog() {
        dialog = new Dialog(ComplainActivity.this, android.R.style.Theme_Translucent_NoTitleBar);

        dialog.setCancelable(false);
        dialog.getWindow().setContentView(R.layout.add_complains_alert_dialog);
        dialog.show();

        final EditText descEt = dialog.findViewById(R.id.et_desc);
        Button submitBtn = dialog.findViewById(R.id.addbtn);
        Button cancelBtn = dialog.findViewById(R.id.closebtn);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_desc = descEt.getText().toString();
                if (str_desc.isEmpty()) {
                    Toast.makeText(ComplainActivity.this, "Enter Complains/Inquiry", Toast.LENGTH_SHORT).show();
                } else {
                    sendComplain(str_desc, dialog);
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

    private void sendComplain(final String str_desc, final Dialog dialog) {
        if (Utilis.isInternetOn()) {

            Utilis.showProgress(ComplainActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.addEnquiry, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " sendComplain response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " sendComplain result " + str_result);

                        if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");

                            Toast.makeText(ComplainActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        } else if (Integer.parseInt(str_result) == 0) {

                            str_message = obj.getString("Message");
                            Toast.makeText(ComplainActivity.this, str_message, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            exeViewComplains();

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");

                            Toast.makeText(ComplainActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(ComplainActivity.this, ComplainActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
                    params.put("Description", str_desc);

                    System.out.println(TAG + " sendComplain inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);

        } else {
            Toast.makeText(this, ComplainActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        back();
    }

    private void back() {
        Intent iback = new Intent(ComplainActivity.this, MenuDashboardActivity.class);
        iback.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(iback);
        finish();

    }

    private class ExeComplainAdapter extends RecyclerView.Adapter<ViewHolder> {
        Activity mActivity;
        private ArrayList<Complains> arrayList;
        public ExeComplainAdapter(Activity complainActivity, ArrayList<Complains> listValue) {
            mActivity = complainActivity;
            arrayList = listValue;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.complain_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            holder.tvDateTime.setText(arrayList.get(position).getDateTime());
            holder.tvDesc.setText(arrayList.get(position).getDescription());
            holder.layDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(ComplainActivity.this);

                    alertDialog.setTitle("Delete");

                    alertDialog.setMessage("Are you sure want to delete this expense?");


                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                            if (Utilis.isInternetOn()) {
                                deleteComplains(arrayList.get(position).getEnquiryId());
                            } else {
                                Toast.makeText(ComplainActivity.this, ComplainActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                    alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.cancel();
                        }
                    });


                    alertDialog.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }

    private void deleteComplains(final String enquiryId) {
        Utilis.showProgress(ComplainActivity.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.deleteEnquiry, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    //converting response to json object
                    JSONObject obj = new JSONObject(response);

                    System.out.println(TAG + " deleteComplains response - " + response);

                    Utilis.dismissProgress();

                    str_result = obj.getString("errorCode");
                    System.out.print(TAG + " deleteComplains result " + str_result);

                    if (Integer.parseInt(str_result) == 0) {

                        str_message = obj.getString("Message");
                        Toast.makeText(ComplainActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        exeViewComplains();

                    } else if (Integer.parseInt(str_result) == 2) {
                        str_message = obj.getString("Message");
                        Toast.makeText(ComplainActivity.this, str_message, Toast.LENGTH_SHORT).show();
                    } else if (Integer.parseInt(str_result) == 1) {
                        str_message = obj.getString("Message");
                        Toast.makeText(ComplainActivity.this, str_message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Utilis.dismissProgress();
                Toast.makeText(ComplainActivity.this, ComplainActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
                params.put("Enquiryid", enquiryId);
                System.out.println(TAG + " deleteComplains inputs " + params);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(ComplainActivity.this).addToRequestQueue(stringRequest);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateTime, tvDesc;
        LinearLayout layDelete;

        public ViewHolder(@NonNull View view) {
            super(view);
            tvDateTime = view.findViewById(R.id.tv_date_time);
            tvDesc = view.findViewById(R.id.tv_desc);
            layDelete = view.findViewById(R.id.lay_delete);
        }
    }

    private class AdminComplainAdapter extends RecyclerView.Adapter<MyViewHolder> {
        Activity mActivity;
        private ArrayList<Complains> arrayList;
        public AdminComplainAdapter(Activity complainActivity, ArrayList<Complains> listValue) {
            mActivity = complainActivity;
            arrayList = listValue;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_complain_list_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
holder.tvDateTime.setText(arrayList.get(position).getDateTime());
holder.tvDesc.setText(arrayList.get(position).getDescription());
holder.tvName.setText(arrayList.get(position).getUserName());
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateTime, tvName, tvDesc;

        public MyViewHolder(@NonNull View view) {
            super(view);
            tvDateTime = view.findViewById(R.id.tv_date_time);
            tvDesc = view.findViewById(R.id.tv_desc);
            tvName = view.findViewById(R.id.tv_name);
        }
    }
}