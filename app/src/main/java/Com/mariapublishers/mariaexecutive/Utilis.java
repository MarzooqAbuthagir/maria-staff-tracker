package Com.mariapublishers.mariaexecutive;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;

public class Utilis {
    @SuppressLint("StaticFieldLeak")
    static Context con;
    // Custom Progress Dialog
    public static ProgressDialog mProgressDialog;

    // Base Url
    public static String Api = "https://digimaria.info/Track/api/";
    public static String login = "userlogin";
    public static String updateLatLng = "updatelatlng";
    public static String checkin = "checkin";
    public static String checkinHistory = "checkinhistory";
    public static String adminviewuser = "adminviewuser";
    public static String profileupdate = "profileupdate";
    public static String uploadimage = "uploadimage";
    public static String getaddress = "getaddress";
    public static String adminviewcheckin = "adminviewcheckin";
    public static String getlatlng = "getlatlng";
    public static String addExpense = "addexpense";
    public static String addEnquiry = "addenquiry";
    public static String addLeave = "addleave";
    public static String addNews = "addnews";
    public static String viewExpense = "viewexpense";
    public static String viewEnquiry = "viewenquiry";
    public static String viewLeave = "viewleave";
    public static String adminViewExpense = "adminviewexpense";
    public static String adminViewEnquiry = "adminviewenquiry";
    public static String adminViewLeave = "adminviewleave";
    public static String bothViewNews = "executiveviewnews";
    public static String deleteExpense = "deleteexpense";
    public static String deleteEnquiry = "deleteenquiry";
    public static String adminviewcheckinsearch = "adminviewcheckinsearch";
    public static String makeattendance = "makeattendance";
    public static String approveleave = "approveleave";
    public static String rejectleave = "rejectleave";
    public static String updatedeviceid = "updatedeviceid";
    public static String checkversion = "checkversion";
    public static String viewattendance = "viewattendance";
    public static String updatecheckout = "updatecheckout";
    public static String uploadexpenseimage = "uploadexpenseimage";
    public static String checkattendance = "checkattendance";
    public static String getcheckincustomername = "getcheckincustomername";
    public static String filterexpense = "expensefilter";

    public static String imagePath = "https://digimaria.info/Track/uploads/track_user/";
    public static String expImagePath = "https://digimaria.info/Track/uploads/expense/";

    public Utilis(Context con) {
        Utilis.con = con;
    }

    public static boolean isInternetOn() {
        ConnectivityManager conMgr = (ConnectivityManager) con
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conMgr.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    public static boolean isGpsOn() {
        LocationManager manager = (LocationManager) con
                .getSystemService(Context.LOCATION_SERVICE);
        return !(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    public static void showProgress(Context context) {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage(context.getResources().getString(R.string.progresstitle));
        mProgressDialog.show();
    }

    public static void dismissProgress() {
        try {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean eMailValidation(CharSequence stringemailaddress1) {
        // TODO Auto-generated method stub

        //final String EMAIL_PATTERN = "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,4}$";

        final String EMAIL_PATTERN = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(stringemailaddress1);

        return matcher.matches();

    }

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkPermission(final Context context)
    {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=android.os.Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
}
