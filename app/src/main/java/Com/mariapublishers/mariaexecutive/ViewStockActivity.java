package Com.mariapublishers.mariaexecutive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class ViewStockActivity extends AppCompatActivity {
    String TAG = "ViewStockActivity";
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

    String documentNo = "", orderIndex= "", orderDate ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stock);

        Intent intent = getIntent();
        documentNo = intent.getStringExtra("documentNo");
        orderIndex = intent.getStringExtra("orderIndex");
        orderDate = intent.getStringExtra("orderDate");

        utilis = new Utilis(ViewStockActivity.this);
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
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ViewStockActivity.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);

        tvNoRecords = findViewById(R.id.tv_no_records);
        getStockList();
    }

    private void getStockList() {
        if (Utilis.isInternetOn()) {
            Utilis.showProgress(ViewStockActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.viewstockdetails, new Response.Listener<String>() {
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
                                        jsonObject.getString("bookName"),
                                        jsonObject.getString("categoryName"),
                                        jsonObject.getString("amount"),
                                        jsonObject.getString("debit"));

                                listValue.add(leave);

                            }

                            StockAdapter adapter = new StockAdapter(ViewStockActivity.this, listValue);
                            recyclerView.setAdapter(adapter);


                        } else if (Integer.parseInt(str_result) == 2) {
                            recyclerView.setVisibility(View.GONE);
                            tvNoRecords.setVisibility(View.VISIBLE);
                        } else if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");
                            Toast.makeText(ViewStockActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(ViewStockActivity.this, ViewStockActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
                    params.put("documentNo", documentNo);
                    params.put("orderIndexId", orderIndex);
                    System.out.println(TAG + " getStockList inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
        } else {
            Toast.makeText(this, ViewStockActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        back();
    }

    private void back() {
        Intent iback = new Intent(ViewStockActivity.this, StockActivity.class);
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
            holder.layShipAddress.setVisibility(View.GONE);
            holder.layBookingPlace.setVisibility(View.GONE);
            holder.layView.setVisibility(View.GONE);

            holder.tvQty.setText(arrayList.get(position).getDebit());
            holder.tvCategory.setText(arrayList.get(position).getCategoryName());
            holder.tvBook.setText(arrayList.get(position).getBookName());
            holder.tvSchool.setText(documentNo);
            holder.tvOrderDate.setText(orderDate);

        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateTime, tvSchool, tvBook, tvQty, tvCategory, tvShipAddress, tvBookingPlace, tvOrderDate;
        LinearLayout layShipAddress, layBookingPlace, layCategory, layBook, layNoOfBook, layView;

        public ViewHolder(@NonNull View view) {
            super(view);
            tvDateTime = view.findViewById(R.id.tv_date_time);
            tvSchool = view.findViewById(R.id.tv_school);
            tvBook = view.findViewById(R.id.tv_book);
            tvCategory = view.findViewById(R.id.tv_category);
            tvQty = view.findViewById(R.id.tv_qty);
            tvShipAddress = view.findViewById(R.id.tv_ship_address);
            tvBookingPlace = view.findViewById(R.id.tv_booking_place);
            tvOrderDate = view.findViewById(R.id.tv_order_date);
            layShipAddress = view.findViewById(R.id.lay_ship_address);
            layBookingPlace = view.findViewById(R.id.lay_booking_place);
            layCategory = view.findViewById(R.id.lay_category);
            layBook = view.findViewById(R.id.lay_book);
            layNoOfBook = view.findViewById(R.id.lay_no_of_books);
            layView = view.findViewById(R.id.lay_view);
        }
    }
}