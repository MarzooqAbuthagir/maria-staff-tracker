package Com.mariapublishers.mariaexecutive;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

public class LoginActivity extends AppCompatActivity {
    String TAG = "LoginActivity";

    Utilis utilis;
    App app;
    SharedPreferences mPrefs;

    EditText etMobNum, etPassword;
    Button btnLogin;
    String strMobNum = "", strPwd = "", str_result = "", str_message = "", str_indexId = "", str_name = "", str_email = "", str_role = "", str_state = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);

        if (LoginSharedPreference.getLoggedStatus(LoginActivity.this)) {
            Gson gson = new Gson();
            String json = mPrefs.getString("MyObject", "");
            UserInfo obj = gson.fromJson(json, UserInfo.class);
//            if (Integer.parseInt(obj.getRoleId()) == 1) {
//                startActivity(new Intent(LoginActivity.this, AdminActivity.class));
//                finish();
//            } else {
//                startActivity(new Intent(LoginActivity.this, MainActivity.class));
//                finish();
//            }

            if (Integer.parseInt(obj.getRoleId()) == 1) {
                startActivity(new Intent(LoginActivity.this, MenuDashboardActivity.class));
                finish();
            } else {
                getAttendance(obj.getIndexId());
            }
        } else {
            setContentView(R.layout.activity_login);

            app = (App) getApplication();
            utilis = new Utilis(LoginActivity.this);
            etMobNum = findViewById(R.id.et_mob);
            etPassword = findViewById(R.id.et_pwd);
            btnLogin = findViewById(R.id.btnLogin);

            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    strMobNum = etMobNum.getText().toString().trim();
                    strPwd = etPassword.getText().toString().trim();

                    if (strMobNum.equals("")) {
                        Toast.makeText(LoginActivity.this, "Enter mobile number", Toast.LENGTH_SHORT).show();
                    } else if (strPwd.equals("")) {
                        Toast.makeText(LoginActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
                    } else {
                        checkAppUpdate();
                    }
                }
            });
        }
    }

    private void checkAppUpdate() {
        String str_ver_code = String.valueOf(BuildConfig.VERSION_CODE);
        String str_ver_name = BuildConfig.VERSION_NAME;

        toCheckVersion(str_ver_code, str_ver_name);
    }

    private void toCheckVersion(final String str_ver_code, final String str_ver_name) {
        if (Utilis.isInternetOn()) {

            Utilis.showProgress(LoginActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.checkversion, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " toCheckVersion response - " + response);

                        Utilis.dismissProgress();

                        String str_result = obj.getString("errorCode");
                        System.out.print(TAG + " toCheckVersion result" + str_result);

                        if (Integer.parseInt(str_result) == 1) {

                            // input param missing

                        } else if (Integer.parseInt(str_result) == 0) {

                            loginAPI();

                        } else if (Integer.parseInt(str_result) == 2) {

                            Intent it = new Intent(LoginActivity.this, AppUpdateActivity.class);
                            it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(it);
                            finish();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(LoginActivity.this, LoginActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

                    if (error instanceof NoConnectionError) {

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

                    params.put("Versioncode", str_ver_code);
                    params.put("Versionname", str_ver_name);

                    System.out.println(TAG + " toCheckVersion inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(LoginActivity.this).addToRequestQueue(stringRequest);

        } else {
            Toast.makeText(this, LoginActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void loginAPI() {
        if (Utilis.isInternetOn()) {

            Utilis.showProgress(LoginActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.login, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " loginAPI response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " loginAPI result " + str_result);

                        if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");

                            Toast.makeText(LoginActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        } else if (Integer.parseInt(str_result) == 0) {

                            str_message = obj.getString("Message");

                            JSONObject json = obj.getJSONObject("result");
                            str_indexId = json.getString("UserIndexId");
                            System.out.println("indexid " + str_indexId);
                            str_name = json.getString("Username");
                            str_email = json.getString("Emailaddress");
                            str_role = json.getString("RoleId");
                            str_state = json.getString("State");
                            saveDetails();

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");

                            Toast.makeText(LoginActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(LoginActivity.this, LoginActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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

                    params.put("Mobilenumber", strMobNum);
                    params.put("Password", strPwd);

                    System.out.println(TAG + " loginAPI inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);

        } else {
            Toast.makeText(this, LoginActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveDetails() {
        UserInfo userInfo = new UserInfo();
        userInfo.setMobileNumber(strMobNum);
        userInfo.setPassword(strPwd);
        userInfo.setIndexId(str_indexId);
        userInfo.setEmail(str_email);
        userInfo.setRoleId(str_role);
        userInfo.setName(str_name);
        userInfo.setState(str_state);

        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(userInfo);
        prefsEditor.putString("MyObject", json);
        prefsEditor.apply();

        LoginSharedPreference.setLoggedIn(LoginActivity.this, true);
//        if (Integer.parseInt(str_role) == 1) {
//            startActivity(new Intent(LoginActivity.this, AdminActivity.class));
//            finish();
//        } else {
//            startActivity(new Intent(LoginActivity.this, MainActivity.class));
//            finish();
//        }

        if (Integer.parseInt(str_role) == 1) {
            startActivity(new Intent(LoginActivity.this, MenuDashboardActivity.class));
            finish();
        } else {
            getAttendance(str_indexId);
        }
    }

    private void getAttendance(final String myIndexId) {
        if (Utilis.isInternetOn()) {
            Utilis.showProgress(LoginActivity.this);

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

                            Toast.makeText(LoginActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        } else if (Integer.parseInt(str_result) == 0) {

                            str_message = obj.getString("Message");

                            startActivity(new Intent(LoginActivity.this, MenuDashboardActivity.class));
                            finish();

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");

                            Intent intent = new Intent(LoginActivity.this, AttendanceActivity.class);
                            intent.putExtra("isAttendance", false);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(LoginActivity.this, LoginActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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

                    params.put("ExecutiveId", myIndexId);

                    System.out.println(TAG + " getAttendance inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
        } else {
            Toast.makeText(this, LoginActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }
}