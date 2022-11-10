package Com.mariapublishers.mariaexecutive;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
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
import com.google.android.gms.location.LocationListener;
import com.google.gson.Gson;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockActivity extends AppCompatActivity {
    String TAG = "StockActivity";
    Toolbar toolbar;
    ActionBar actionBar = null;
    RecyclerView recyclerView;
    TextView tvNoRecords;
    MovableFloatingActionButton fab;

    Utilis utilis;
    static SharedPreferences mPrefs;
    UserInfo obj;

    String str_result = "", str_message = "";
    ArrayList<Stock> listValue = new ArrayList<Stock>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        utilis = new Utilis(StockActivity.this);
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
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(StockActivity.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);

        tvNoRecords = findViewById(R.id.tv_no_records);

        fab = findViewById(R.id.add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iback = new Intent(StockActivity.this, AddSpecimenActivity.class);
                iback.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(iback);
                finish();
            }
        });

        getStockList();
    }

    private void getStockList() {
        if (Utilis.isInternetOn()) {
            Utilis.showProgress(StockActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.getstocklist, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " getStockList response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " getStockList result " + str_result);

                        if (Integer.parseInt(str_result) == 0) {
                            listValue.clear();

                            recyclerView.setVisibility(View.VISIBLE);
                            tvNoRecords.setVisibility(View.GONE);

                            JSONArray json = obj.getJSONArray("result");
                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObject = json.getJSONObject(i);
                                Stock leave = new Stock(
                                        jsonObject.getString("orderIndex"),
                                        jsonObject.getString("documentNo"),
                                        jsonObject.getString("orderNo"),
                                        jsonObject.getString("shipAddress"),
                                        jsonObject.getString("bookingPlace"),
                                        jsonObject.getString("bookingDate"),
                                        jsonObject.getString("trichyToDispatchDate"));

                                listValue.add(leave);

                            }

                            StockAdapter adapter = new StockAdapter(StockActivity.this, listValue);
                            recyclerView.setAdapter(adapter);


                        } else if (Integer.parseInt(str_result) == 2) {
                            recyclerView.setVisibility(View.GONE);
                            tvNoRecords.setVisibility(View.VISIBLE);
                        } else if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");
                            Toast.makeText(StockActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(StockActivity.this, StockActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
                    System.out.println(TAG + " getStockList inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
        } else {
            Toast.makeText(this, StockActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        back();
    }

    private void back() {
        Intent iback = new Intent(StockActivity.this, MenuDashboardActivity.class);
        iback.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(iback);
        finish();

    }

    private class StockAdapter extends RecyclerView.Adapter<ViewHolder> {
        Activity mActivity;
        private ArrayList<Stock> arrayList;

        public StockAdapter(Activity expenseActivity, ArrayList<Stock> listValue) {
            mActivity = expenseActivity;
            arrayList = listValue;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            holder.layNoOfBook.setVisibility(View.GONE);
            holder.layCategory.setVisibility(View.GONE);
            holder.layBook.setVisibility(View.GONE);

            holder.tvSchool.setText(arrayList.get(position).getDocumentNo());
            holder.tvShipAddress.setText(arrayList.get(position).getShipAddress());
            holder.tvBookingPlace.setText(arrayList.get(position).getBookingPlace());

            holder.btnView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent iback = new Intent(StockActivity.this, ViewStockActivity.class);
                    iback.putExtra("documentNo", arrayList.get(position).getDocumentNo());
                    iback.putExtra("orderIndex", arrayList.get(position).getOrderIndex());
                    iback.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(iback);
                    finish();
                }
            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateTime, tvSchool, tvBook, tvQty, tvCategory, tvShipAddress, tvBookingPlace;
        LinearLayout layShipAddress, layBookingPlace, layCategory, layBook, layNoOfBook, layView;
        Button btnView;

        public ViewHolder(@NonNull View view) {
            super(view);
            tvDateTime = view.findViewById(R.id.tv_date_time);
            tvSchool = view.findViewById(R.id.tv_school);
            tvBook = view.findViewById(R.id.tv_book);
            tvCategory = view.findViewById(R.id.tv_category);
            tvQty = view.findViewById(R.id.tv_qty);
            tvShipAddress = view.findViewById(R.id.tv_ship_address);
            tvBookingPlace = view.findViewById(R.id.tv_booking_place);
            layShipAddress = view.findViewById(R.id.lay_ship_address);
            layBookingPlace = view.findViewById(R.id.lay_booking_place);
            layCategory = view.findViewById(R.id.lay_category);
            layBook = view.findViewById(R.id.lay_book);
            layNoOfBook = view.findViewById(R.id.lay_no_of_books);
            layView = view.findViewById(R.id.lay_view);
            btnView = view.findViewById(R.id.btn_view);
        }
    }
}