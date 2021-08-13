package Com.mariapublishers.mariaexecutive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

public class SendNewsActivity extends AppCompatActivity {
    String TAG = "SendNewsActivity";
    Toolbar toolbar;
    ActionBar actionBar = null;
    RecyclerView recyclerView;
    TextView tvNoRecords;
    MovableFloatingActionButton fab;

    Utilis utilis;
    static SharedPreferences mPrefs;
    UserInfo obj;
    Dialog dialog;
    String str_result = "", str_message = "";
    ArrayList<News> listValue = new ArrayList<News>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_news);

        utilis = new Utilis(SendNewsActivity.this);
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
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(SendNewsActivity.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);

        tvNoRecords = findViewById(R.id.tv_no_records);

        fab = findViewById(R.id.add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newsDialog();
            }
        });

        if (Integer.parseInt(obj.getRoleId()) == 1) {
            fab.setVisibility(View.VISIBLE);
            toolbar.setTitle("Send News");
        } else {
            fab.setVisibility(View.GONE);
            toolbar.setTitle("Messages");
        }

        showNews();
    }

    private void showNews() {
        if (Utilis.isInternetOn()) {
            Utilis.showProgress(SendNewsActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.bothViewNews, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " showNews response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " showNews result " + str_result);

                        if (Integer.parseInt(str_result) == 0) {

                            listValue.clear();

                            recyclerView.setVisibility(View.VISIBLE);
                            tvNoRecords.setVisibility(View.GONE);

                            str_message = obj.getString("Message");

                            JSONArray json = obj.getJSONArray("result");
                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObject = json.getJSONObject(i);
                                News checkIn = new News(
                                        jsonObject.getString("Newsid"),
                                        jsonObject.getString("Title"),
                                        jsonObject.getString("Message"),
                                        jsonObject.getString("Created_at"));

                                listValue.add(checkIn);
                            }

                            NewsAdapter adapter = new NewsAdapter(SendNewsActivity.this, listValue);
                            recyclerView.setAdapter(adapter);

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");
                            recyclerView.setVisibility(View.GONE);
                            tvNoRecords.setVisibility(View.VISIBLE);
                        } else if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");
                            Toast.makeText(SendNewsActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(SendNewsActivity.this, SendNewsActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
            Toast.makeText(this, SendNewsActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void newsDialog() {
        dialog = new Dialog(SendNewsActivity.this, android.R.style.Theme_Translucent_NoTitleBar);

        dialog.setCancelable(false);
        dialog.getWindow().setContentView(R.layout.add_news_alert_dialog);
        dialog.show();

        final EditText titleEt = dialog.findViewById(R.id.et_title);
        final EditText msgEt = dialog.findViewById(R.id.et_msg);
        Button submitBtn = dialog.findViewById(R.id.addbtn);
        Button cancelBtn = dialog.findViewById(R.id.closebtn);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_title = titleEt.getText().toString();
                String str_news_msg = msgEt.getText().toString();

                if (str_title.isEmpty()) {
                    Toast.makeText(SendNewsActivity.this, "Enter Title", Toast.LENGTH_SHORT).show();
                } else if (str_news_msg.isEmpty()) {
                    Toast.makeText(SendNewsActivity.this, "Enter Message", Toast.LENGTH_SHORT).show();
                } else {
                    sendNews(str_title, str_news_msg, dialog);
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

    private void sendNews(final String str_title, final String str_news_msg, final Dialog dialog) {
        if (Utilis.isInternetOn()) {

            Utilis.showProgress(SendNewsActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.addNews, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " sendNews response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " sendNews result " + str_result);

                        if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");

                            Toast.makeText(SendNewsActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        } else if (Integer.parseInt(str_result) == 0) {

                            str_message = obj.getString("Message");
                            Toast.makeText(SendNewsActivity.this, str_message, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            showNews();

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");

                            Toast.makeText(SendNewsActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(SendNewsActivity.this, SendNewsActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
                    params.put("Message", str_news_msg);
                    params.put("Title", str_title);

                    System.out.println(TAG + " sendNews inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);

        } else {
            Toast.makeText(this, SendNewsActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        back();
    }

    private void back() {
        Intent iback = new Intent(SendNewsActivity.this, MenuDashboardActivity.class);
        iback.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(iback);
        finish();

    }

    private class NewsAdapter extends RecyclerView.Adapter<ViewHolder> {
        Activity mActivity;
        private ArrayList<News> arrayList;

        public NewsAdapter(Activity sendNewsActivity, ArrayList<News> listValue) {
            mActivity = sendNewsActivity;
            arrayList = listValue;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvDateTime.setText(arrayList.get(position).getDateTime());
            holder.tvTitle.setText(arrayList.get(position).getTitle());
            holder.tvMessage.setText(arrayList.get(position).getMessage());
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateTime, tvMessage, tvTitle;

        public ViewHolder(@NonNull View view) {
            super(view);
            tvDateTime = view.findViewById(R.id.tv_date_time);
            tvTitle = view.findViewById(R.id.tv_title);
            tvMessage = view.findViewById(R.id.tv_message);
        }
    }
}