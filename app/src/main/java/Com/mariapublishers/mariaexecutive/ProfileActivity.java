package Com.mariapublishers.mariaexecutive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    String TAG = "ProfileActivity";
    Toolbar toolbar;
    ActionBar actionBar = null;

    Utilis utilis;
    static SharedPreferences mPrefs;
    UserInfo obj;

    EditText etMail, etPhone, etName;
    Button btnLogout;
    Button btnProfile;
    String str_result = "", str_message = "", str_indexId = "", str_mob_num = "", str_name = "", str_email = "", str_image = "", str_role = "", str_pwd = "";
    ImageView imgDp;
    String userChoosenTask = "";
    int REQUEST_CAMERA = 101;
    int SELECT_FILE = 102;
    String base64img ="";
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        utilis = new Utilis(ProfileActivity.this);
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

        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_mob);
        etMail = findViewById(R.id.et_mail);
        btnLogout = findViewById(R.id.btn_logout);

        etName.setText(obj.getName());
        etMail.setText(obj.getEmail());
        etPhone.setText(obj.getMobileNumber());

        imgDp = findViewById(R.id.img_dp);
        imgDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        loadImage();

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);

                builder.setTitle(ProfileActivity.this.getResources().getString(R.string.logout_title))
                        .setMessage(ProfileActivity.this.getResources().getString(R.string.logout_msg))
                        .setPositiveButton(ProfileActivity.this.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing but close the dialog
                                dialog.dismiss();
                                MyService.isServiceRunning = false;
                                Intent serviceIntent = new Intent(ProfileActivity.this, MyService.class);
                                stopService(serviceIntent);

                                LoginSharedPreference.setLoggedIn(ProfileActivity.this, false);

                                SharedPreferences preferences = getSharedPreferences("MY_SHARED_PREF", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.apply();

                                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                                finish();
                            }
                        })
                        .setNegativeButton(ProfileActivity.this.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
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

        btnProfile = findViewById(R.id.btn_profile);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etName.getText().toString().trim();
                String email = etMail.getText().toString().trim();

                if (name.isEmpty()) {
                    Toast.makeText(ProfileActivity.this, "Enter Name", Toast.LENGTH_SHORT).show();
                } else if (email.isEmpty()) {
                    Toast.makeText(ProfileActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
                } else if (email.length() > 0) {
                    boolean chkmail = Utilis.eMailValidation(email);
                    if (!chkmail) {
                        Toast.makeText(ProfileActivity.this, "Enter Valid Email", Toast.LENGTH_SHORT).show();
                    } else {
                        updateProfile(name, email);
                    }
                }
            }
        });
    }

    private void loadImage() {
        try {
            Picasso.with(this).load(Utilis.imagePath+obj.getIndexId()).skipMemoryCache().placeholder(getResources().getDrawable(R.drawable.header_profile)).transform(new CircleTransform()).networkPolicy(NetworkPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_CACHE).into(imgDp);
        } catch (Resources.NotFoundException e) {
            System.out.println(TAG + " image upload err " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Gallery",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";
                    boolean result = Utilis.checkPermission(ProfileActivity.this);
                    if (result)
                        cameraIntent();
                } else if (items[item].equals("Choose from Gallery")) {
                    userChoosenTask = "Choose from Gallery";
                    boolean result = Utilis.checkPermission(ProfileActivity.this);
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
                if (userChoosenTask.equals("Take Photo"))
                    cameraIntent();
                else if (userChoosenTask.equals("Choose from Gallery"))
                    galleryIntent();
            } else {
                Toast.makeText(ProfileActivity.this, "Grant Permission to update profile image", Toast.LENGTH_SHORT).show();
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
//        File destination = new File(Environment.getExternalStorageDirectory(),
//                System.currentTimeMillis() + ".jpg");
//        FileOutputStream fo;
//        try {
//            destination.createNewFile();
//            fo = new FileOutputStream(destination);
//            fo.write(bytes.toByteArray());
//            fo.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        imgDp.setImageBitmap(thumbnail);

        byte[] byteArray = bytes .toByteArray();
        base64img = Base64.encodeToString(byteArray, Base64.DEFAULT);
        System.out.println("Camera image "+base64img);

        uploadImage(base64img);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        imgDp.setImageBitmap(bm);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        assert bm != null;
        bm.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        base64img = Base64.encodeToString(byteArray, Base64.DEFAULT);
        System.out.println("Gallery image "+base64img);

        uploadImage(base64img);
    }

    private void uploadImage(final String base64img) {
        progressDialog = ProgressDialog.show(ProfileActivity.this, "Uploading",
                "Please wait...", true);

        //sending image to server
        StringRequest request = new StringRequest(Request.Method.POST, Utilis.Api+Utilis.uploadimage, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                progressDialog.dismiss();
                Picasso.with(ProfileActivity.this).load(Utilis.imagePath+obj.getIndexId()).skipMemoryCache().placeholder(getResources().getDrawable(R.drawable.header_profile)).transform(new CircleTransform()).networkPolicy(NetworkPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_CACHE).into(imgDp);
                Toast.makeText(ProfileActivity.this, "Image uploaded succesfully", Toast.LENGTH_SHORT).show();

                Intent iback = new Intent(ProfileActivity.this, MainActivity.class);
                iback.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(iback);
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();
            }
        }) {
            //adding parameters to send
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("Image", base64img);
                parameters.put("Userid", obj.getIndexId());
                System.out.println(TAG + " ImageUploadTask inputs " + parameters);
                return parameters;
            }
        };

        RequestQueue rQueue = Volley.newRequestQueue(ProfileActivity.this);
        rQueue.add(request);
    }

    private void updateProfile(final String name, final String email) {
        if (Utilis.isInternetOn()) {

            Utilis.showProgress(ProfileActivity.this);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.profileupdate, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " updateProfile response - " + response);

                        Utilis.dismissProgress();

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " updateProfile result " + str_result);

                        if (Integer.parseInt(str_result) == 1) {
                            str_message = obj.getString("Message");

                            Toast.makeText(ProfileActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        } else if (Integer.parseInt(str_result) == 0) {

                            str_message = obj.getString("Message");

                            JSONObject json = obj.getJSONObject("result");
                            str_indexId = json.getString("Userid");
                            str_mob_num = json.getString("Mobilenumber");
                            str_email = json.getString("Emailaddress");
                            str_image = json.getString("imageString");
                            str_name = json.getString("Name");
                            saveDetails();

                        } else if (Integer.parseInt(str_result) == 2) {
                            str_message = obj.getString("Message");

                            Toast.makeText(ProfileActivity.this, str_message, Toast.LENGTH_SHORT).show();

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Utilis.dismissProgress();
                    Toast.makeText(ProfileActivity.this, ProfileActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

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
                    params.put("Mobilenumber", obj.getMobileNumber());
                    params.put("Userid", obj.getIndexId());
                    params.put("Emailaddress", email);
                    params.put("imageString", base64img);
                    params.put("Name", name);

                    System.out.println(TAG + " updateProfile inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);

        } else {
            Toast.makeText(this, ProfileActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveDetails() {
        str_role = obj.getRoleId();
        str_pwd = obj.getPassword();

        UserInfo userInfo = new UserInfo();
        userInfo.setMobileNumber(str_mob_num);
        userInfo.setPassword(str_pwd);
        userInfo.setIndexId(str_indexId);
        userInfo.setEmail(str_email);
        userInfo.setRoleId(str_role);
        userInfo.setName(str_name);

        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(userInfo);
        prefsEditor.putString("MyObject", json);
        prefsEditor.apply();

        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        back();
    }

    private void back() {
        Intent iback = new Intent(ProfileActivity.this, MainActivity.class);
        iback.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(iback);
        finish();
    }
}