package Com.mariapublishers.mariaexecutive;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ExpenseActivity extends AppCompatActivity {
    String TAG = "ExpenseActivity";
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
    ArrayList<Expenses> listValue = new ArrayList<Expenses>();

    String userChosenTask = "";
    int REQUEST_CAMERA = 101;
    int SELECT_FILE = 102;
    ImageView imgExp;
    String base64img ="";
    ProgressDialog progressDialog;
    String str_exp_id="";
    LinearLayout filterLayout;
    Button btnSubmit, btnClear;
    LinearLayout dateLayout, toDateLayout;
    EditText dateTxt, toDateTxt;
    ImageView imgClear, toImgClear;

    Spinner spinExpense;
    String[] expenses = { "T A Dearness Allowances", "Room Rent", "Fuel Expenses", "Traveling Expenses", "Courier & Postage", "Printing & Stationary"};
    String selectedItem="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        utilis = new Utilis(ExpenseActivity.this);
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

        filterLayout = findViewById(R.id.layout_filter);

        dateLayout = findViewById(R.id.date_layout);
        dateTxt = findViewById(R.id.date_txt);
        imgClear = findViewById(R.id.ivClear);
        toDateLayout = findViewById(R.id.to_date_layout);
        toDateTxt = findViewById(R.id.to_date_txt);
        toImgClear = findViewById(R.id.ivToClear);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnClear = findViewById(R.id.btnClear);

        recyclerView = findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ExpenseActivity.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);

        tvNoRecords = findViewById(R.id.tv_no_records);

        fab = findViewById(R.id.add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expenseDialog();
            }
        });

        if (Integer.parseInt(obj.getRoleId()) == 1) {
            fab.setVisibility(View.GONE);
            adminViewExpenses();
        } else {
            fab.setVisibility(View.VISIBLE);
            exeViewExpenses();
        }

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
                if (dateTxt.getText().toString().equalsIgnoreCase("From Date")) {
                    Toast.makeText(ExpenseActivity.this, "Select From Date", Toast.LENGTH_SHORT).show();
                } else if (toDateTxt.getText().toString().equalsIgnoreCase("To Date")) {
                    Toast.makeText(ExpenseActivity.this, "Select To Date", Toast.LENGTH_SHORT).show();
                } else {
                    filterExpenseApi(obj.getIndexId(), dateTxt.getText().toString(), toDateTxt.getText().toString());
                }
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateTxt.setText("From Date");
                imgClear.setVisibility(View.GONE);
                toDateTxt.setText("To Date");
                toImgClear.setVisibility(View.GONE);

                if (Utilis.isInternetOn()) {
                    exeViewExpenses();
                } else {
                    Toast.makeText(ExpenseActivity.this, ExpenseActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
                }
            }
        });

        imgClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateTxt.setText("From Date");
                imgClear.setVisibility(View.GONE);
            }
        });

        toImgClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toDateTxt.setText("To Date");
                toImgClear.setVisibility(View.GONE);
            }
        });
    }

    private void filterExpenseApi(final String indexId, final String fromDate, final String toDate) {
        if (Utilis.isInternetOn()) {
            Utilis.showProgress(ExpenseActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.filterexpense, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " filterExpenseApi response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " filterExpenseApi result " + str_result);

                        if (Integer.parseInt(str_result) == 0) {
                            listValue.clear();

                            recyclerView.setVisibility(View.VISIBLE);
                            filterLayout.setVisibility(View.VISIBLE);
                            tvNoRecords.setVisibility(View.GONE);

                            str_message = obj.getString("Message");

                            JSONArray json = obj.getJSONArray("result");
                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObject = json.getJSONObject(i);
                                Expenses expenses = new Expenses(
                                        jsonObject.getString("Expenseid"),
                                        jsonObject.getString("Amount"),
                                        jsonObject.getString("Description"),
                                        jsonObject.getString("Datetime"),
                                        jsonObject.getString("image"));

                                listValue.add(expenses);
                            }

                            ExeExpenseAdapter adapter = new ExeExpenseAdapter(ExpenseActivity.this, listValue);
                            recyclerView.setAdapter(adapter);

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");
                            recyclerView.setVisibility(View.GONE);
                            filterLayout.setVisibility(View.VISIBLE);
                            tvNoRecords.setVisibility(View.VISIBLE);
                        } else if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");
                            Toast.makeText(ExpenseActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(ExpenseActivity.this, ExpenseActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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

                    params.put("ExecutiveId", indexId);
                    params.put("Fromdate", fromDate);
                    params.put("Todate", toDate);

                    System.out.println(TAG + " filterExpenseApi inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
        } else {
            Toast.makeText(this, ExpenseActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void selectToDate() {
        Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(ExpenseActivity.this, new DatePickerDialog.OnDateSetListener() {

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

        DatePickerDialog dpd = new DatePickerDialog(ExpenseActivity.this, new DatePickerDialog.OnDateSetListener() {

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

    private void adminViewExpenses() {
        if (Utilis.isInternetOn()) {
            Utilis.showProgress(ExpenseActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.adminViewExpense, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " adminViewExpenses response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " adminViewExpenses result " + str_result);

                        if (Integer.parseInt(str_result) == 0) {
                            listValue.clear();

                            recyclerView.setVisibility(View.VISIBLE);
                            filterLayout.setVisibility(View.VISIBLE);
                            tvNoRecords.setVisibility(View.GONE);

                            str_message = obj.getString("Message");

                            JSONArray json = obj.getJSONArray("result");
                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObject = json.getJSONObject(i);
                                Expenses expenses = new Expenses(
                                        jsonObject.getString("Expenseid"),
                                        jsonObject.getString("Amount"),
                                        jsonObject.getString("Description"),
                                        jsonObject.getString("Datetime"),
                                        jsonObject.getString("Userid"),
                                        jsonObject.getString("Username"),
                                        jsonObject.getString("image"));

                                listValue.add(expenses);
                            }

                            AdminExpenseAdapter adapter = new AdminExpenseAdapter(ExpenseActivity.this, listValue);
                            recyclerView.setAdapter(adapter);


                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");
                            recyclerView.setVisibility(View.GONE);
                            filterLayout.setVisibility(View.GONE);
                            tvNoRecords.setVisibility(View.VISIBLE);
                        } else if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");
                            Toast.makeText(ExpenseActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(ExpenseActivity.this, ExpenseActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
            Toast.makeText(this, ExpenseActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void exeViewExpenses() {
        if (Utilis.isInternetOn()) {
            Utilis.showProgress(ExpenseActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.viewExpense, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " exeViewExpenses response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " exeViewExpenses result " + str_result);

                        if (Integer.parseInt(str_result) == 0) {
                            listValue.clear();

                            recyclerView.setVisibility(View.VISIBLE);
                            filterLayout.setVisibility(View.VISIBLE);
                            tvNoRecords.setVisibility(View.GONE);

                            str_message = obj.getString("Message");

                            JSONArray json = obj.getJSONArray("result");
                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObject = json.getJSONObject(i);
                                Expenses expenses = new Expenses(
                                        jsonObject.getString("Expenseid"),
                                        jsonObject.getString("Amount"),
                                        jsonObject.getString("Description"),
                                        jsonObject.getString("Datetime"),
                                        jsonObject.getString("image"));

                                listValue.add(expenses);
                            }

                            ExeExpenseAdapter adapter = new ExeExpenseAdapter(ExpenseActivity.this, listValue);
                            recyclerView.setAdapter(adapter);

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");
                            recyclerView.setVisibility(View.GONE);
                            filterLayout.setVisibility(View.GONE);
                            tvNoRecords.setVisibility(View.VISIBLE);
                        } else if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");
                            Toast.makeText(ExpenseActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(ExpenseActivity.this, ExpenseActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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

                    System.out.println(TAG + " exeViewExpenses inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
        } else {
            Toast.makeText(this, ExpenseActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void expenseDialog() {
        dialog = new Dialog(ExpenseActivity.this, android.R.style.Theme_Translucent_NoTitleBar);

        dialog.setCancelable(false);
        dialog.getWindow().setContentView(R.layout.add_expense_alert_dialog);
        dialog.show();

        final EditText amtEt = dialog.findViewById(R.id.et_amt);
        final EditText descEt = dialog.findViewById(R.id.et_desc);
        Button submitBtn = dialog.findViewById(R.id.addbtn);
        Button cancelBtn = dialog.findViewById(R.id.closebtn);
        TextView tvPickImage = dialog.findViewById(R.id.tvPickImage);
        imgExp = dialog.findViewById(R.id.img_exp);

        spinExpense = dialog.findViewById(R.id.spin_expense);

        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,expenses);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spinExpense.setAdapter(aa);

        spinExpense.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedItem = expenses[i].toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        tvPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                base64img="";
                selectImage();
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_amt = amtEt.getText().toString();
                String str_desc = descEt.getText().toString();

                if (str_amt.isEmpty()) {
                    Toast.makeText(ExpenseActivity.this, "Enter Amount", Toast.LENGTH_SHORT).show();
                } else if (str_desc.isEmpty()) {
                    Toast.makeText(ExpenseActivity.this, "Enter Description", Toast.LENGTH_SHORT).show();
                } else {
                    sendExpense(str_amt, str_desc, dialog, selectedItem);
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

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Gallery",
                "Cancel"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ExpenseActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    userChosenTask = "Take Photo";
                    boolean result = Utilis.checkPermission(ExpenseActivity.this);
                    if (result)
                        cameraIntent();
                } else if (items[item].equals("Choose from Gallery")) {
                    userChosenTask = "Choose from Gallery";
                    boolean result = Utilis.checkPermission(ExpenseActivity.this);
                    if (result)
                        galleryIntent();
                } else {//if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent() {
        System.out.println("intent to gallery");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Utilis.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (userChosenTask.equals("Take Photo"))
                    cameraIntent();
                else if (userChosenTask.equals("Choose from Gallery"))
                    galleryIntent();
            } else {
                Toast.makeText(ExpenseActivity.this, "Grant Permission to update profile image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        assert thumbnail != null;
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        imgExp.setVisibility(View.VISIBLE);
        imgExp.setImageBitmap(thumbnail);

        byte[] byteArray = bytes .toByteArray();
        base64img = Base64.encodeToString(byteArray, Base64.DEFAULT);
        System.out.println("Camera image "+base64img);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                imgExp.setVisibility(View.VISIBLE);
                imgExp.setImageBitmap(bm);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        assert bm != null;
        bm.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        base64img = Base64.encodeToString(byteArray, Base64.DEFAULT);
        System.out.println("Gallery image "+base64img);
    }

    /* Get the real path from the URI */
    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    private void sendExpense(final String str_amt, final String str_desc, final Dialog dialog, final String selectedItem) {
        if (Utilis.isInternetOn()) {

            Utilis.showProgress(ExpenseActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.addExpense, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " sendExpense response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " sendExpense result " + str_result);

                        if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");

                            Toast.makeText(ExpenseActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        } else if (Integer.parseInt(str_result) == 0) {

                            str_message = obj.getString("Message");
                            str_exp_id = obj.getString("ExpenseId");
                            System.out.println(TAG +"sendExpense expid "+str_exp_id);
                            Toast.makeText(ExpenseActivity.this, str_message, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            if (base64img.equals("")) {
                                exeViewExpenses();
                            } else {
                                uploadImage(str_exp_id, base64img);
                            }

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");

                            Toast.makeText(ExpenseActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(ExpenseActivity.this, ExpenseActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
                    params.put("Amount", str_amt);
                    params.put("Description", str_desc);
                    params.put("Expense", selectedItem);

                    System.out.println(TAG + " sendExpense inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);

        } else {
            Toast.makeText(this, ExpenseActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage(final String exp_id, final String base64img) {
        progressDialog = ProgressDialog.show(ExpenseActivity.this, "Uploading",
                "Please wait...", true);

        //sending image to server
        StringRequest request = new StringRequest(Request.Method.POST, Utilis.Api+Utilis.uploadexpenseimage, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                progressDialog.dismiss();
                Toast.makeText(ExpenseActivity.this, "Image uploaded succesfully", Toast.LENGTH_SHORT).show();

                exeViewExpenses();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressDialog.dismiss();
                Toast.makeText(ExpenseActivity.this, getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();
            }
        }) {
            //adding parameters to send
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("Image", base64img);
                parameters.put("ExpenseId", exp_id);
                System.out.println(TAG + " ImageUploadTask inputs " + parameters);
                return parameters;
            }
        };

        RequestQueue rQueue = Volley.newRequestQueue(ExpenseActivity.this);
        rQueue.add(request);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        back();
    }

    private void back() {
        Intent iback = new Intent(ExpenseActivity.this, MenuDashboardActivity.class);
        iback.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(iback);
        finish();
    }

    private class ExeExpenseAdapter extends RecyclerView.Adapter<ViewHolder> {
        Activity mActivity;
        private ArrayList<Expenses> arrayList;

        public ExeExpenseAdapter(Activity expenseActivity, ArrayList<Expenses> listValue) {
            mActivity = expenseActivity;
            arrayList = listValue;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            holder.tvDateTime.setText(arrayList.get(position).getDateTime());
            holder.tvAmount.setText(arrayList.get(position).getAmount());
            holder.tvDesc.setText(arrayList.get(position).getDescription());
            holder.layDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(ExpenseActivity.this);

                    alertDialog.setTitle("Delete");

                    alertDialog.setMessage("Are you sure want to delete this expense?");


                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                            if (Utilis.isInternetOn()) {
                                deleteExpense(arrayList.get(position).getExpenseId());
                            } else {
                                Toast.makeText(ExpenseActivity.this, ExpenseActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
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

            holder.layViewImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("Exp Image onclick "+arrayList.get(position).getImage());
                    if (arrayList.get(position).getImage()!=null &&!arrayList.get(position).getImage().isEmpty()) {
                        showImage(arrayList.get(position).getImage(), arrayList.get(position).getExpenseId());
                    } else {
                        Toast.makeText(mActivity, "No Image Attached", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }

    private void deleteExpense(final String expenseId) {
        Utilis.showProgress(ExpenseActivity.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.deleteExpense, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    //converting response to json object
                    JSONObject obj = new JSONObject(response);

                    System.out.println(TAG + " deleteExpense response - " + response);

                    Utilis.dismissProgress();

                    str_result = obj.getString("errorCode");
                    System.out.print(TAG + " deleteExpense result " + str_result);

                    if (Integer.parseInt(str_result) == 0) {

                        str_message = obj.getString("Message");
                        Toast.makeText(ExpenseActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        exeViewExpenses();

                    } else if (Integer.parseInt(str_result) == 2) {
                        str_message = obj.getString("Message");
                        Toast.makeText(ExpenseActivity.this, str_message, Toast.LENGTH_SHORT).show();
                    } else if (Integer.parseInt(str_result) == 1) {
                        str_message = obj.getString("Message");
                        Toast.makeText(ExpenseActivity.this, str_message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Utilis.dismissProgress();
                Toast.makeText(ExpenseActivity.this, ExpenseActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
                params.put("Expenseid", expenseId);
                System.out.println(TAG + " deleteExpense inputs " + params);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(ExpenseActivity.this).addToRequestQueue(stringRequest);

    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateTime, tvAmount, tvDesc;
        LinearLayout layDelete, layViewImage;

        public ViewHolder(@NonNull View view) {
            super(view);
            tvDateTime = view.findViewById(R.id.tv_date_time);
            tvAmount = view.findViewById(R.id.tv_amount);
            tvDesc = view.findViewById(R.id.tv_desc);
            layDelete = view.findViewById(R.id.lay_delete);
            layViewImage = view.findViewById(R.id.lay_viewImage);
        }
    }

    private class AdminExpenseAdapter extends RecyclerView.Adapter<MyViewHolder> {
        Activity mActivity;
        private ArrayList<Expenses> arrayList;

        public AdminExpenseAdapter(Activity expenseActivity, ArrayList<Expenses> listValue) {
            mActivity = expenseActivity;
            arrayList = listValue;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_exp_list_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
            holder.tvDateTime.setText(arrayList.get(position).getDateTime());
            holder.tvAmount.setText(arrayList.get(position).getAmount());
            holder.tvName.setText(arrayList.get(position).getUserName());
            holder.tvDesc.setText(arrayList.get(position).getDescription());
            holder.layViewImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (arrayList.get(position).getImage()!=null &&!arrayList.get(position).getImage().isEmpty()) {
                        showImage(arrayList.get(position).getImage(), arrayList.get(position).getExpenseId());
                    } else {
                        Toast.makeText(mActivity, "No Image Attached", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }

    private void showImage(String image, String expenseId) {
        dialog = new Dialog(ExpenseActivity.this, android.R.style.Theme_Translucent_NoTitleBar);

        dialog.setCancelable(false);
        dialog.getWindow().setContentView(R.layout.expense_image_alert_dialog);
        dialog.show();

        ImageView imgDp = dialog.findViewById(R.id.img_dp);
        Button cancelBtn = dialog.findViewById(R.id.closebtn);

        System.out.println("Exp Image "+image);

        try {
            Picasso.with(this).load(Utilis.expImagePath+expenseId).skipMemoryCache().networkPolicy(NetworkPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_CACHE).into(imgDp);
        } catch (Resources.NotFoundException e) {
            System.out.println(TAG + " image upload err " + e.getMessage());
            e.printStackTrace();
        }

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateTime, tvAmount, tvName, tvDesc;
        LinearLayout layViewImage;

        public MyViewHolder(@NonNull View view) {
            super(view);
            tvDateTime = view.findViewById(R.id.tv_date_time);
            tvAmount = view.findViewById(R.id.tv_amount);
            tvName = view.findViewById(R.id.tv_name);
            tvDesc = view.findViewById(R.id.tv_desc);
            layViewImage = view.findViewById(R.id.lay_viewImage);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_download, menu);
        if(Integer.parseInt(obj.getRoleId()) == 1) {
            menu.findItem(R.id.ic_download).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.ic_download) {
            Gson gson1 = new Gson();
            String json1 = mPrefs.getString("MyObject", "");
            UserInfo userInfo = gson1.fromJson(json1, UserInfo.class);

            String urlString = Utilis.downloadExpenseReport + userInfo.getIndexId();
            Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
            urlIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            urlIntent.setPackage("com.android.chrome");
            try {
                startActivity(urlIntent);
            } catch (ActivityNotFoundException ex) {
                // Chrome browser presumably not installed so allow user to choose instead
                urlIntent.setPackage(null);
                startActivity(urlIntent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}