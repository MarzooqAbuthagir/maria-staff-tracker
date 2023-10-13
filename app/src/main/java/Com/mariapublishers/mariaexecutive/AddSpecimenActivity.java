package Com.mariapublishers.mariaexecutive;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
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
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddSpecimenActivity extends AppCompatActivity {
    String TAG = "AddSpecimenActivity";
    Toolbar toolbar;
    ActionBar actionBar = null;

    Utilis utilis;
    static SharedPreferences mPrefs;
    UserInfo obj;

    EditText shipAddressDetails, bookingPlace, transport;
    EditText dateTxt, todateTxt;

    private ArrayList<SchoolData> categoryListValue = new ArrayList<SchoolData>();
    private List<String> categorySpinnerValue = new ArrayList<>();

    private ArrayList<SchoolData> bookListValue = new ArrayList<SchoolData>();
    private List<String> bookSpinnerValue = new ArrayList<>();

    private String strCategoryId = "", strCategoryName = "", strBookID = "", strBookName = "", strRup = "";
    String str_result = "", str_message = "";

    String strShipAddress = "", strBookingPlace = "", strTransport = "", strFromDate = "", strToDate = "";

    RecyclerView recyclerView;
    SpecimenAdapter specimenAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_specimen);

        utilis = new Utilis(AddSpecimenActivity.this);
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

        shipAddressDetails = findViewById(R.id.et_ship_addr_det);
        bookingPlace = findViewById(R.id.et_booking_place);
        transport = findViewById(R.id.et_transport);

        LinearLayout dateLayout = findViewById(R.id.date_layout);
        LinearLayout todateLayout = findViewById(R.id.to_date_layout);
        dateTxt = findViewById(R.id.date_txt);
        todateTxt = findViewById(R.id.to_date_txt);

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

        recyclerView = findViewById(R.id.recycler_view);

        // setting recyclerView layoutManager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(AddSpecimenActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        specimenAdapter = new SpecimenAdapter(specimenDataArrayList);
        recyclerView.setAdapter(specimenAdapter);

        Button btnSubmit = findViewById(R.id.btn_submit);
        Button btnCancel = findViewById(R.id.btn_cancel);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                strShipAddress = shipAddressDetails.getText().toString();
                strBookingPlace = bookingPlace.getText().toString();
                strFromDate = dateTxt.getText().toString();
                strToDate = todateTxt.getText().toString();
                strTransport = transport.getText().toString();

                if (strShipAddress.isEmpty()) {
                    Toast.makeText(AddSpecimenActivity.this, "Enter Ship Address Details", Toast.LENGTH_SHORT).show();
                } else if (strBookingPlace.isEmpty()) {
                    Toast.makeText(AddSpecimenActivity.this, "Enter Booking Place", Toast.LENGTH_SHORT).show();
                } else if (strFromDate.isEmpty()) {
                    Toast.makeText(AddSpecimenActivity.this, "Select Date", Toast.LENGTH_SHORT).show();
                } else if (strToDate.isEmpty()) {
                    Toast.makeText(AddSpecimenActivity.this, "Select Dispatch Date", Toast.LENGTH_SHORT).show();
                } else if (strTransport.isEmpty()) {
                    Toast.makeText(AddSpecimenActivity.this, "Enter Transport (Courier)", Toast.LENGTH_SHORT).show();
                } else if (specimenDataArrayList.size() == 0) {
                    Toast.makeText(AddSpecimenActivity.this, "Add Specimen Book Details", Toast.LENGTH_SHORT).show();
                } else {
                    saveForm();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shipAddressDetails.setText("");
                bookingPlace.setText("");
                transport.setText("");

                strShipAddress = "";
                strBookingPlace = "";
                strTransport = "";

                dateTxt.setText("");
                todateTxt.setText("");

                categoryListValue.clear();
                bookListValue.clear();
                spinBook.setEnabled(false);
                strCategoryId = "";
                strCategoryName = "";
                strBookID = "";
                strBookName = "";
                strRup = "";
                qtyEt.setEnabled(false);
                qtyEt.setText("");
                totAmtEt.setText("");

                strFromDate = "";
                strToDate = "";

                SchoolData initSchoolData = new SchoolData();
                initSchoolData.setId("-1");
                initSchoolData.setName("Category");
                categoryListValue.add(0, initSchoolData);

                categorySpinnerValue.clear();
                for (int i = 0; i < categoryListValue.size(); i++) {
                    categorySpinnerValue.add(categoryListValue.get(i).getName());
                }

                ArrayAdapter arrayAdapter1 = new ArrayAdapter(AddSpecimenActivity.this, R.layout.spinner_item, categorySpinnerValue);
                arrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //Setting the ArrayAdapter data on the Spinner
                spinCategory.setAdapter(arrayAdapter1);
                spinBook.setSelection(0);


                SchoolData initDistrictData = new SchoolData();
                initDistrictData.setId("-1");
                initDistrictData.setName("Book Name");
                bookListValue.add(0, initDistrictData);

                bookSpinnerValue.add(bookListValue.get(0).getName());

                ArrayAdapter arrayAdapter = new ArrayAdapter(AddSpecimenActivity.this, R.layout.spinner_item, bookSpinnerValue);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //Setting the ArrayAdapter data on the Spinner
                spinBook.setAdapter(arrayAdapter);
                spinBook.setSelection(0);
            }
        });

        specimenAdapter.setOnItemEditClickListener(new OnItemEditClickListener() {
            @Override
            public void onItemEditClick(View view, int position) {
                SpecimenData offerData = specimenDataArrayList.get(position);
                editOfferDialog(view, offerData, position);
            }
        });

        specimenAdapter.setOnItemDeleteClickListener(new OnItemDeleteClickListener() {
            @Override
            public void onItemDeleteClick(View view, final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddSpecimenActivity.this);
                builder.setTitle("Confirmation")
                        .setMessage("Are you sure want to delete?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                specimenDataArrayList.remove(position);
                                specimenAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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
        });
    }

    private void editOfferDialog(View view, SpecimenData offerData, final int position) {

        final Dialog dialog = new Dialog(AddSpecimenActivity.this, android.R.style.Theme_Translucent_NoTitleBar);

        dialog.setCancelable(false);
        dialog.getWindow().setContentView(R.layout.specimen_alert_dialog);
        dialog.show();

        getCategoryList();

        totAmtEt = dialog.findViewById(R.id.et_total_amt);
        qtyEt = dialog.findViewById(R.id.et_qty);
        spinBook = dialog.findViewById(R.id.spin_book);
        spinCategory = dialog.findViewById(R.id.spin_category);

        spinCategory.setTitle("Select Category");
        spinBook.setTitle("Select Book");

        spinCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    spinBook.setEnabled(true);
                    strCategoryId = categoryListValue.get(i).getId();
                    strCategoryName = categoryListValue.get(i).getName();

                    strBookID = "";
                    strBookName = "";
                    strRup = "";
                    qtyEt.setEnabled(false);
//                    qtyEt.setText("");

                    bookListValue.clear();
                    bookSpinnerValue.clear();
                    SchoolData initDistrictData = new SchoolData();
                    initDistrictData.setId("-1");
                    initDistrictData.setName("Book Name");
                    bookListValue.add(0, initDistrictData);

                    bookSpinnerValue.add(bookListValue.get(0).getName());

                    ArrayAdapter arrayAdapter = new ArrayAdapter(AddSpecimenActivity.this, R.layout.spinner_item, bookSpinnerValue);
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Setting the ArrayAdapter data on the Spinner
                    spinBook.setAdapter(arrayAdapter);
                    spinBook.setSelection(0);

                    getBookList();
                } else {
                    bookListValue.clear();
                    spinBook.setEnabled(false);
                    strCategoryId = "";
                    strCategoryName = "";
                    strBookID = "";
                    strBookName = "";
                    strRup = "";
                    qtyEt.setEnabled(false);
//                    qtyEt.setText("");
                    SchoolData initDistrictData = new SchoolData();
                    initDistrictData.setId("-1");
                    initDistrictData.setName("Book Name");
                    bookListValue.add(0, initDistrictData);

                    bookSpinnerValue.add(bookListValue.get(0).getName());

                    ArrayAdapter arrayAdapter = new ArrayAdapter(AddSpecimenActivity.this, R.layout.spinner_item, bookSpinnerValue);
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Setting the ArrayAdapter data on the Spinner
                    spinBook.setAdapter(arrayAdapter);
                    spinBook.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinBook.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    strBookID = bookListValue.get(i).getId();
                    strBookName = bookListValue.get(i).getName();
                    strRup = bookListValue.get(i).getPrice();
                    qtyEt.setEnabled(true);
                } else {
                    strBookID = "";
                    strBookName = "";
                    strRup = "";
//                    qtyEt.setText("");
                    qtyEt.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        totAmtEt.setText(offerData.getTotAmt());
        qtyEt.setText(offerData.getNoOfBooks());
        strRup = offerData.getTotAmt();

        qtyEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String value = editable.toString();
                Toast.makeText(AddSpecimenActivity.this, value, Toast.LENGTH_SHORT).show();
                if (value.isEmpty()) {
                    totAmtEt.setText("");
                } else if (!value.isEmpty() && Integer.parseInt(value) > 0) {
                    int totalAmount = Integer.parseInt(value) * Integer.parseInt(strRup);
                    totAmtEt.setText(String.valueOf(totalAmount));
                } else if (!value.isEmpty() && Integer.parseInt(value) == 0) {
                    Toast.makeText(AddSpecimenActivity.this, "Entered no. of Books should be greater than zero", Toast.LENGTH_SHORT).show();
                }
            }
        });

//        int prefPos = 0;
//        for (int i = 0; i < categoryListValue.size(); i++) {
//            if (offerData.getCategoryName().equalsIgnoreCase(categoryListValue.get(i).getName())) {
//                prefPos = i;
//                break;
//            }
//        }
//
//        ArrayAdapter arrayAdapter = new ArrayAdapter(AddSpecimenActivity.this, R.layout.spinner_item, categorySpinnerValue);
//        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        //Setting the ArrayAdapter data on the Spinner
//        spinCategory.setAdapter(arrayAdapter);
//        spinCategory.setSelection(prefPos);
//
//        int prefBookPos = 0;
//        for (int i = 0; i < bookListValue.size(); i++) {
//            if (offerData.getBookName().equalsIgnoreCase(bookListValue.get(i).getName())) {
//                prefBookPos = i;
//                break;
//            }
//        }
//
//        ArrayAdapter arrayAdapter1 = new ArrayAdapter(AddSpecimenActivity.this, R.layout.spinner_item, bookSpinnerValue);
//        arrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        //Setting the ArrayAdapter data on the Spinner
//        spinBook.setAdapter(arrayAdapter1);
//        spinBook.setSelection(prefBookPos);

        Button btnSubmit = dialog.findViewById(R.id.addbtn);
        Button btnCancel = dialog.findViewById(R.id.closebtn);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String noOfBooks = qtyEt.getText().toString();
                if (strCategoryId.isEmpty()) {
                    Toast.makeText(AddSpecimenActivity.this, "Select Category", Toast.LENGTH_SHORT).show();
                } else if (strBookID.isEmpty()) {
                    Toast.makeText(AddSpecimenActivity.this, "Select Book Name", Toast.LENGTH_SHORT).show();
                } else if (noOfBooks.isEmpty()) {
                    Toast.makeText(AddSpecimenActivity.this, "Enter no. of Books", Toast.LENGTH_SHORT).show();
                } else if (Integer.parseInt(noOfBooks) == 0) {
                    Toast.makeText(AddSpecimenActivity.this, "Entered no. of Books should be greater than zero", Toast.LENGTH_SHORT).show();
                } else {
                    specimenDataArrayList.remove(position);
                    SpecimenData specimenData = new SpecimenData(
                            strCategoryId,
                            strCategoryName,
                            strBookID,
                            strBookName,
                            noOfBooks,
                            totAmtEt.getText().toString()
                    );
                    specimenDataArrayList.add(specimenData);
                    specimenAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            }
        });


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void saveForm() {
        if (Utilis.isInternetOn()) {
            Utilis.showProgress(AddSpecimenActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.saveform, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " saveForm response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " saveForm result " + str_result);

                        if (Integer.parseInt(str_result) == 0) {

                            str_message = obj.getString("message");
//                            Toast.makeText(AddSpecimenActivity.this, str_message, Toast.LENGTH_SHORT).show();

                            String docNum = obj.getString("orderNum");
                            String orderIndex = obj.getString("orderIndex");

                            postSpecimenArrayListData(docNum, orderIndex);


//                            AlertDialog.Builder builder = new AlertDialog.Builder(AddSpecimenActivity.this);
//                            builder.setMessage("Your request has been placed successfully. And your Specimen document no is " + obj.getString("orderNum"))
//                                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            dialog.dismiss();
//                                            back();
//                                        }
//                                    });
//                            AlertDialog alert = builder.create();
//                            alert.show();
//                            Button btnOk = alert.getButton(DialogInterface.BUTTON_NEUTRAL);
//                            btnOk.setTextColor(Color.parseColor("#000000"));

                        } else {
                            Toast.makeText(AddSpecimenActivity.this, "Try again later", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(AddSpecimenActivity.this, AddSpecimenActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
                    params.put("shipAddress", strShipAddress);
                    params.put("bookingPlace", strBookingPlace);
                    params.put("bookingDate", strFromDate);
                    params.put("trichy_to_dispatch", strToDate);
                    params.put("transport", strTransport);

                    System.out.println(TAG + " saveForm inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
        } else {
            Toast.makeText(this, AddSpecimenActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void postSpecimenArrayListData(final String docNum, final String orderIndex) {
        if (Utilis.isInternetOn()) {
            Utilis.showProgress(AddSpecimenActivity.this);

            for (int i = 0; i < specimenDataArrayList.size(); i++) {
                final int currentPos = i;

                StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.updateorders, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            //converting response to json object
                            JSONObject obj = new JSONObject(response);

                            System.out.println(TAG + " postSpecimenArrayListData response - " + response);

                            str_result = obj.getString("errorCode");
                            System.out.print(TAG + " postSpecimenArrayListData result " + str_result);

                            if (Integer.parseInt(str_result) == 0) {

                                str_message = obj.getString("message");
//                            Toast.makeText(AddSpecimenActivity.this, str_message, Toast.LENGTH_SHORT).show();

                                if (currentPos + 1 == specimenDataArrayList.size()) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(AddSpecimenActivity.this);
                                    builder.setMessage("Your request has been placed successfully. And your Specimen document no is " + docNum)
                                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    back();
                                                }
                                            });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                    Button btnOk = alert.getButton(DialogInterface.BUTTON_NEUTRAL);
                                    btnOk.setTextColor(Color.parseColor("#000000"));

                                }

                            } else {
                                Utilis.dismissProgress();
                                Toast.makeText(AddSpecimenActivity.this, "Try again later", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Utilis.dismissProgress();
                        Toast.makeText(AddSpecimenActivity.this, AddSpecimenActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
                        params.put("orderNum", docNum);
                        params.put("orderIndex", orderIndex);
                        params.put("categoryId", specimenDataArrayList.get(currentPos).getCategoryID());
                        params.put("bookId", specimenDataArrayList.get(currentPos).getBookID());
                        params.put("totalAmount", specimenDataArrayList.get(currentPos).getTotAmt());
                        params.put("debit", specimenDataArrayList.get(currentPos).getNoOfBooks());

                        System.out.println(TAG + " postSpecimenArrayListData inputs " + params);
                        return params;
                    }
                };

                stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
            }

        } else {
            Toast.makeText(this, AddSpecimenActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void selectDate() {
        Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(AddSpecimenActivity.this, new DatePickerDialog.OnDateSetListener() {

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

        dpd.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dpd.show();
    }

    private void toselectDate() {
        Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(AddSpecimenActivity.this, new DatePickerDialog.OnDateSetListener() {

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

        dpd.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dpd.show();
    }

    private void getBookList() {
        if (Utilis.isInternetOn()) {

            Utilis.showProgress(AddSpecimenActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.getbook, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " getBookList response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " getBookList result " + str_result);

                        if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");

                            Toast.makeText(AddSpecimenActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        } else if (Integer.parseInt(str_result) == 0) {

                            str_message = obj.getString("message");

                            bookListValue.clear();
                            bookSpinnerValue.clear();

                            JSONArray json = obj.getJSONArray("result");
                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObject = json.getJSONObject(i);
                                SchoolData bookData = new SchoolData(
                                        jsonObject.getString("bookId"),
                                        jsonObject.getString("bookName"),
                                        jsonObject.getString("rupees"),
                                        "0");

                                bookListValue.add(bookData);
                            }

                            System.out.println("book id spec "+ specimenDataArrayList.size());
                            for (int a = 0; a < specimenDataArrayList.size(); a++) {
                                if (strCategoryId.equalsIgnoreCase(specimenDataArrayList.get(a).getCategoryID())) {
                                    for (int b = 0; b < bookListValue.size(); b++) {
                                        SchoolData bookData;
                                        System.out.println("book id temp "+ bookListValue.get(b).getId());
                                        System.out.println("book id spec "+ specimenDataArrayList.get(a).getBookID());
                                        if (bookListValue.get(b).getId().equalsIgnoreCase(specimenDataArrayList.get(a).getBookID())) {
                                            System.out.println("book id spec equals "+ specimenDataArrayList.get(a).getNoOfBooks());
                                            bookData = new SchoolData(
                                                    bookListValue.get(b).getId(),
                                                    bookListValue.get(b).getName(),
                                                    bookListValue.get(b).getPrice(),
                                                    specimenDataArrayList.get(a).getNoOfBooks());
                                            System.out.println("book id equals");
                                            bookListValue.remove(b);
                                            bookListValue.add(b, bookData);
                                        }
                                    }
                                }
                            }

//                            SchoolData initSchoolData = new SchoolData();
//                            initSchoolData.setId("-1");
//                            initSchoolData.setName("Book Name");
//                            bookListValue.add(0, initSchoolData);
//
//                            bookSpinnerValue.clear();
//                            for (int i = 0; i < bookListValue.size(); i++) {
//                                bookSpinnerValue.add(bookListValue.get(i).getName());
//                            }
//
//                            ArrayAdapter arrayAdapter = new ArrayAdapter(AddSpecimenActivity.this, R.layout.spinner_item, bookSpinnerValue);
//                            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                            //Setting the ArrayAdapter data on the Spinner
//                            spinBook.setAdapter(arrayAdapter);

                            recyclerViewBook.setVisibility(View.VISIBLE);

                            bookAdapter = new BookAdapter(AddSpecimenActivity.this, bookListValue);
                            recyclerViewBook.setAdapter(bookAdapter);

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("message");

                            Toast.makeText(AddSpecimenActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(AddSpecimenActivity.this, AddSpecimenActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
                    params.put("categoryId", strCategoryId);
                    System.out.println(TAG + "getBookList inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(AddSpecimenActivity.this).addToRequestQueue(stringRequest);

        } else {
            Toast.makeText(AddSpecimenActivity.this, AddSpecimenActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void getCategoryList() {
        if (Utilis.isInternetOn()) {
            Utilis.showProgress(AddSpecimenActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.GET, Utilis.Api + Utilis.categorylist, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " getCategoryList response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " getCategoryList result " + str_result);

                        if (Integer.parseInt(str_result) == 0) {
                            categoryListValue.clear();
                            str_message = obj.getString("message");

                            JSONArray json = obj.getJSONArray("result");
                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObject = json.getJSONObject(i);
                                SchoolData categoryData = new SchoolData(
                                        jsonObject.getString("categoryId"),
                                        jsonObject.getString("categoryName"));

                                categoryListValue.add(categoryData);
                            }

                            SchoolData initSchoolData = new SchoolData();
                            initSchoolData.setId("-1");
                            initSchoolData.setName("Category");
                            categoryListValue.add(0, initSchoolData);

                            categorySpinnerValue.clear();
                            for (int i = 0; i < categoryListValue.size(); i++) {
                                categorySpinnerValue.add(categoryListValue.get(i).getName());
                            }

                            ArrayAdapter arrayAdapter = new ArrayAdapter(AddSpecimenActivity.this, R.layout.spinner_item, categorySpinnerValue);
                            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            //Setting the ArrayAdapter data on the Spinner
                            spinCategory.setAdapter(arrayAdapter);

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("message");
                            Toast.makeText(AddSpecimenActivity.this, str_message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(AddSpecimenActivity.this, AddSpecimenActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
                protected Map<String, String> getParams() {
                    return new HashMap<>();
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(AddSpecimenActivity.this).addToRequestQueue(stringRequest);
        } else {
            Toast.makeText(AddSpecimenActivity.this, AddSpecimenActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void back() {
        Intent iback = new Intent(AddSpecimenActivity.this, StockActivity.class);
        iback.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(iback);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        back();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_specimen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.ic_add_specimen) {
            addSpecimenDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    EditText qtyEt, totAmtEt;
    SearchableSpinner spinBook, spinCategory;
    ArrayList<SpecimenData> specimenDataArrayList = new ArrayList<>();
    RecyclerView recyclerViewBook;
    BookAdapter bookAdapter;

    private void addSpecimenDialog() {
        final Dialog dialog = new Dialog(AddSpecimenActivity.this, android.R.style.Theme_Translucent_NoTitleBar);

        dialog.setCancelable(false);
        dialog.getWindow().setContentView(R.layout.specimen_alert_dialog);
        dialog.show();

        getCategoryList();

        totAmtEt = dialog.findViewById(R.id.et_total_amt);
        qtyEt = dialog.findViewById(R.id.et_qty);
        spinBook = dialog.findViewById(R.id.spin_book);
        spinCategory = dialog.findViewById(R.id.spin_category);
        recyclerViewBook = dialog.findViewById(R.id.recycler_view_book);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(AddSpecimenActivity.this);
        recyclerViewBook.setHasFixedSize(true);
        recyclerViewBook.setNestedScrollingEnabled(false);
        recyclerViewBook.setLayoutManager(layoutManager);

        recyclerViewBook.addItemDecoration(new DividerItemDecoration(AddSpecimenActivity.this, DividerItemDecoration.VERTICAL));

        // Get drawable object
        Drawable mDivider = ContextCompat.getDrawable(AddSpecimenActivity.this, R.drawable.divider_line);
        // Create a DividerItemDecoration whose orientation is Horizontal
        DividerItemDecoration hItemDecoration = new DividerItemDecoration(AddSpecimenActivity.this,
                DividerItemDecoration.VERTICAL);
        // Set the drawable on it
        hItemDecoration.setDrawable(mDivider);

        spinCategory.setTitle("Select Category");
        spinBook.setTitle("Select Book");

        spinCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    spinBook.setEnabled(true);
                    strCategoryId = categoryListValue.get(i).getId();
                    strCategoryName = categoryListValue.get(i).getName();

                    strBookID = "";
                    strBookName = "";
                    strRup = "";
                    qtyEt.setEnabled(false);
                    qtyEt.setText("");

                    bookListValue.clear();
                    bookSpinnerValue.clear();
                    SchoolData initDistrictData = new SchoolData();
                    initDistrictData.setId("-1");
                    initDistrictData.setName("Book Name");
                    bookListValue.add(0, initDistrictData);

                    bookSpinnerValue.add(bookListValue.get(0).getName());

                    ArrayAdapter arrayAdapter = new ArrayAdapter(AddSpecimenActivity.this, R.layout.spinner_item, bookSpinnerValue);
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Setting the ArrayAdapter data on the Spinner
                    spinBook.setAdapter(arrayAdapter);
                    spinBook.setSelection(0);

                    getBookList();
                } else {
                    bookListValue.clear();
                    spinBook.setEnabled(false);
                    strCategoryId = "";
                    strCategoryName = "";
                    strBookID = "";
                    strBookName = "";
                    strRup = "";
                    qtyEt.setEnabled(false);
                    qtyEt.setText("");
                    SchoolData initDistrictData = new SchoolData();
                    initDistrictData.setId("-1");
                    initDistrictData.setName("Book Name");
                    bookListValue.add(0, initDistrictData);

                    bookSpinnerValue.add(bookListValue.get(0).getName());

                    ArrayAdapter arrayAdapter = new ArrayAdapter(AddSpecimenActivity.this, R.layout.spinner_item, bookSpinnerValue);
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Setting the ArrayAdapter data on the Spinner
                    spinBook.setAdapter(arrayAdapter);
                    spinBook.setSelection(0);

                    recyclerViewBook.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinBook.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    strBookID = bookListValue.get(i).getId();
                    strBookName = bookListValue.get(i).getName();
                    strRup = bookListValue.get(i).getPrice();
                    qtyEt.setEnabled(true);
                } else {
                    strBookID = "";
                    strBookName = "";
                    strRup = "";
                    qtyEt.setText("");
                    qtyEt.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        qtyEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String value = editable.toString();
                if (value.isEmpty()) {
                    totAmtEt.setText("");
                } else if (!value.isEmpty() && Integer.parseInt(value) > 0) {
                    int totalAmount = Integer.parseInt(value) * Integer.parseInt(strRup);
                    totAmtEt.setText(String.valueOf(totalAmount));
                } else if (!value.isEmpty() && Integer.parseInt(value) == 0) {
                    Toast.makeText(AddSpecimenActivity.this, "Entered no. of Books should be greater than zero", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button btnSubmit = dialog.findViewById(R.id.addbtn);
        Button btnCancel = dialog.findViewById(R.id.closebtn);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String noOfBooks = qtyEt.getText().toString();
//                if (strCategoryId.isEmpty()) {
//                    Toast.makeText(AddSpecimenActivity.this, "Select Category", Toast.LENGTH_SHORT).show();
//                } else if (strBookID.isEmpty()) {
//                    Toast.makeText(AddSpecimenActivity.this, "Select Book Name", Toast.LENGTH_SHORT).show();
//                } else if (noOfBooks.isEmpty()) {
//                    Toast.makeText(AddSpecimenActivity.this, "Enter no. of Books", Toast.LENGTH_SHORT).show();
//                } else if (Integer.parseInt(noOfBooks) == 0) {
//                    Toast.makeText(AddSpecimenActivity.this, "Entered no. of Books should be greater than zero", Toast.LENGTH_SHORT).show();
//                } else {
//                    SpecimenData specimenData = new SpecimenData(
//                            strCategoryId,
//                            strCategoryName,
//                            strBookID,
//                            strBookName,
//                            noOfBooks,
//                            totAmtEt.getText().toString()
//                    );
//                    specimenDataArrayList.add(specimenData);
//                    specimenAdapter.notifyDataSetChanged();
//                    dialog.dismiss();
//                }
                dialog.dismiss();
            }
        });


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

    }

    private class SpecimenAdapter extends RecyclerView.Adapter<SpecimenAdapter.ViewHolder> {
        private List<SpecimenData> arrayList;
        private OnItemEditClickListener mItemEditClickListener;
        private OnItemDeleteClickListener mItemDeleteClickListener;

        public SpecimenAdapter(ArrayList<SpecimenData> specimenDataArrayList) {
            super();
            arrayList = specimenDataArrayList;
        }


        public void setOnItemEditClickListener(final OnItemEditClickListener mItemClickListener) {
            this.mItemEditClickListener = mItemClickListener;
        }

        public void setOnItemDeleteClickListener(final OnItemDeleteClickListener mItemClickListener) {
            this.mItemDeleteClickListener = mItemClickListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.specimen_list_item, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            holder.tvCategory.setText(arrayList.get(position).getCategoryName());
            holder.tvBook.setText(arrayList.get(position).getBookName());
            holder.tvQty.setText(arrayList.get(position).getNoOfBooks());
            holder.tvAmt.setText(arrayList.get(position).getTotAmt());

            holder.layEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemEditClickListener != null) {
                        mItemEditClickListener.onItemEditClick(view, holder.getAdapterPosition());
                    }
                }
            });

            holder.layDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemDeleteClickListener != null) {
                        mItemDeleteClickListener.onItemDeleteClick(view, holder.getAdapterPosition());
                    }
                }
            });
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout layEdit, layDelete;
            TextView tvCategory, tvBook, tvQty, tvAmt;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                layEdit = itemView.findViewById(R.id.lay_edit);
                layDelete = itemView.findViewById(R.id.lay_delete);
                tvCategory = itemView.findViewById(R.id.tv_category);
                tvBook = itemView.findViewById(R.id.tv_book);
                tvQty = itemView.findViewById(R.id.tv_qty);
                tvAmt = itemView.findViewById(R.id.tv_amt);
            }
        }
    }

    public interface OnItemEditClickListener {
        void onItemEditClick(View view, int position);
    }

    public interface OnItemDeleteClickListener {
        void onItemDeleteClick(View view, int position);
    }

    private class BookAdapter extends RecyclerView.Adapter<BookViewHolder> {
        Activity mActivity;
        private ArrayList<SchoolData> arrayList;

        public BookAdapter(Activity activity, ArrayList<SchoolData> bookListValue) {
            setHasStableIds(true);
            this.mActivity = activity;
            this.arrayList = bookListValue;
        }

        @Override
        public void onViewRecycled(@NonNull BookViewHolder holder) {
            super.onViewRecycled(holder);
        }

        @NonNull
        @Override
        public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_list_item, parent, false);
            return new BookViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final BookViewHolder holder, final int position) {
            holder.setIsRecyclable(false);

            if (arrayList.get(position).getName().startsWith(" ")) {
                holder.tvBookName.setText(arrayList.get(position).getName().replace(" ", ""));
            } else {
                holder.tvBookName.setText(arrayList.get(position).getName());
            }

            holder.etQty.setText(arrayList.get(position).getQty());

            holder.btnDec.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int qty = holder.etQty.getText().toString().isEmpty() ? 0 : Integer.parseInt(holder.etQty.getText().toString());
                    if (qty == 0 || holder.etQty.getText().toString().isEmpty()) {

                    } else {
                        qty = qty - 1;
                        holder.etQty.setText(qty + "");
                        int totalAmount = qty * Integer.parseInt(arrayList.get(position).getPrice());
                        saveList(
                                strCategoryId,
                                strCategoryName,
                                arrayList.get(position).getId(),
                                arrayList.get(position).getName(),
                                holder.etQty.getText().toString(),
                                String.valueOf(totalAmount)
                        );
                    }
                }
            });

            holder.btnInc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int qty = holder.etQty.getText().toString().isEmpty() ? 0 : Integer.parseInt(holder.etQty.getText().toString());
                    qty = qty + 1;
                    holder.etQty.setText(qty + "");
                    int totalAmount = qty * Integer.parseInt(arrayList.get(position).getPrice());
                    saveList(
                            strCategoryId,
                            strCategoryName,
                            arrayList.get(position).getId(),
                            arrayList.get(position).getName(),
                            holder.etQty.getText().toString(),
                            String.valueOf(totalAmount)
                    );
                }
            });

            holder.etQty.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String value = editable.toString();
                    if (value.isEmpty()) {
                        for (int i = 0; i < specimenDataArrayList.size(); i++) {
                            if (specimenDataArrayList.get(i).getCategoryID().equalsIgnoreCase(strCategoryId) &&
                                    specimenDataArrayList.get(i).getBookID().equalsIgnoreCase(arrayList.get(position).getId())) {
                                specimenDataArrayList.remove(i);
                                specimenAdapter.notifyDataSetChanged();
                                break;
                            }
                        }

                        for (int j=0; j< bookListValue.size(); j++) {
                            if (bookListValue.get(j).getId().equalsIgnoreCase(arrayList.get(position).getId())) {
                                SchoolData bookData = new SchoolData(
                                        bookListValue.get(j).getId(),
                                        bookListValue.get(j).getName(),
                                        bookListValue.get(j).getPrice(),
                                        value);
                                bookListValue.remove(j);
                                bookListValue.add(j, bookData);
                                break;
                            }
                        }
                    } else if (!value.isEmpty() && Integer.parseInt(value) > 0) {
                        int totalAmount = Integer.parseInt(value) * Integer.parseInt(arrayList.get(position).getPrice());
                        saveList(
                                strCategoryId,
                                strCategoryName,
                                arrayList.get(position).getId(),
                                arrayList.get(position).getName(),
                                holder.etQty.getText().toString(),
                                String.valueOf(totalAmount)
                        );
                    } else if (!value.isEmpty() && Integer.parseInt(value) == 0) {
                        for (int i = 0; i < specimenDataArrayList.size(); i++) {
                            if (specimenDataArrayList.get(i).getCategoryID().equalsIgnoreCase(strCategoryId) &&
                                    specimenDataArrayList.get(i).getBookID().equalsIgnoreCase(arrayList.get(position).getId())) {
                                specimenDataArrayList.remove(i);
                                specimenAdapter.notifyDataSetChanged();
                                break;
                            }
                        }

                        for (int j=0; j< bookListValue.size(); j++) {
                            if (bookListValue.get(j).getId().equalsIgnoreCase(arrayList.get(position).getId())) {
                                SchoolData bookData = new SchoolData(
                                        bookListValue.get(j).getId(),
                                        bookListValue.get(j).getName(),
                                        bookListValue.get(j).getPrice(),
                                        value);
                                bookListValue.remove(j);
                                bookListValue.add(j, bookData);
                                break;
                            }
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return (long) position;
        }
    }

    private void saveList(String strCategoryId, String strCategoryName, String bookId, String bookName, String qty, String amount) {
        for (int i = 0; i < specimenDataArrayList.size(); i++) {
            if (specimenDataArrayList.get(i).getCategoryID().equalsIgnoreCase(strCategoryId) &&
                    specimenDataArrayList.get(i).getBookID().equalsIgnoreCase(bookId)) {
                specimenDataArrayList.remove(i);
                specimenAdapter.notifyDataSetChanged();
                break;
            }
        }

        for (int j=0; j< bookListValue.size(); j++) {
            if (bookListValue.get(j).getId().equalsIgnoreCase(bookId)) {
               SchoolData bookData = new SchoolData(
                        bookListValue.get(j).getId(),
                        bookListValue.get(j).getName(),
                        bookListValue.get(j).getPrice(),
                        qty);
                bookListValue.remove(j);
                bookListValue.add(j, bookData);
               break;
            }
        }

        if (Integer.parseInt(qty) > 0) {
            SpecimenData specimenData = new SpecimenData(
                    strCategoryId,
                    strCategoryName,
                    bookId,
                    bookName,
                    qty,
                    amount
            );
            specimenDataArrayList.add(specimenData);
            specimenAdapter.notifyDataSetChanged();
        }
    }

    private class BookViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookName;
        Button btnDec, btnInc;
        EditText etQty;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookName = itemView.findViewById(R.id.tv_book_name);
            etQty = itemView.findViewById(R.id.et_qty);
            btnDec = itemView.findViewById(R.id.btn_dec);
            btnInc = itemView.findViewById(R.id.btn_inc);
        }
    }
}