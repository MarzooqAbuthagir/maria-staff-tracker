package Com.mariapublishers.mariaexecutive;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AppUpdateActivity extends AppCompatActivity {
    String TAG = "AppUpdateActivity";
    Button btnUpdate;
    String url="https://digimaria.info/Track/uploads/Apk/Maria.apk";
    static final int REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_update);
        btnUpdate = findViewById(R.id.btn_update);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (ActivityCompat.checkSelfPermission(AppUpdateActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(AppUpdateActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AppUpdateActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE);
                    return;
                }
                new AutoUpdateApk(AppUpdateActivity.this, url).execute();*/

                String appPackageName = AppUpdateActivity.this.getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });
    }

/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            new AutoUpdateApk(AppUpdateActivity.this, url).execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new AutoUpdateApk(AppUpdateActivity.this, url).execute();
            }
        }
    }*/

    private static class AutoUpdateApk extends AsyncTask<String, Integer, String> {
        String TAG = "AutoUpdateApk";
        @SuppressLint("StaticFieldLeak")
        Context con;
        String downnloadApkUrl;
        Utilis utilis;
        public static final String NEW_APK_LOCATION = "Android/data/" + BuildConfig.APPLICATION_ID + "/temp";
        public AutoUpdateApk(Context context, String url) {
            con = context;
            downnloadApkUrl = url;
            utilis = new Utilis(con);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Utilis.showProgress(con);
        }

        @Override
        protected String doInBackground(String... params) {
            FileOutputStream fos = null;
            InputStream is = null;
            try {
                URL url = new URL(downnloadApkUrl);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.connect();

                File sdcard = Environment.getExternalStorageDirectory();
                File myDir = new File(sdcard, NEW_APK_LOCATION);
                myDir.mkdirs();
                File outputFile = new File(myDir, "Maria.apk");
                if (outputFile.exists()) {
                    boolean isDelete = outputFile.delete();
                    if(isDelete)
                        Log.i(TAG, "doInBackground: deleted successfully");
                }


                int lengthOfFile = c.getContentLength();
                is = c.getInputStream();
                byte[] buffer = new byte[1024];
                int len1;
                long total = 0;
                fos = new FileOutputStream(outputFile);
                while ((len1 = is.read(buffer)) != -1) {
                    total += len1;
                    // publishing the progress....
                    publishProgress((int) ((total * 100) / lengthOfFile));
                    fos.write(buffer, 0, len1);
                }
                fos.flush();
                fos.close();
                is.close();
                return "0";
            } catch (FileNotFoundException fnfe) {
                Log.e("File", "FileNotFoundException! " + fnfe);
                return "1";
            } catch (Exception e) {
                Log.e("UpdateAPP", "Exception " + e);
                return "1";
            } finally {
                try {
                    if (fos != null)
                        fos.close();
                    if (is != null)
                        is.close();
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: "+e.getMessage(),e);
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Utilis.dismissProgress();
            if (result.equalsIgnoreCase("0")) {
                installNewApk(con);
            } else {
                Toast.makeText(con, "Sorry, Apk download failed", Toast.LENGTH_SHORT).show();
            }
        }

        private void installNewApk(Context con) {
            String INSTALL_NEW_APK_LOCATION = "Android/data/" + BuildConfig.APPLICATION_ID + "/temp/Maria.apk";
            ComponentName comp;
            if (android.os.Build.VERSION.SDK_INT < 23) {
                comp = new
                        ComponentName("com.android.packageinstaller",
                        "com.android.packageinstaller.PackageInstallerActivity");

            } else {
                comp = new
                        ComponentName("com.google.android.packageinstaller",
                        "com.android.packageinstaller.PackageInstallerActivity");
            }
            Intent intent;
            if (android.os.Build.VERSION.SDK_INT > 25) {
                intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            } else {
                intent = new Intent(Intent.ACTION_VIEW);
            }

            if (android.os.Build.VERSION.SDK_INT >= 24) {

                Uri contentUri = FileProvider.getUriForFile(con, BuildConfig.APPLICATION_ID + ".fileprovider", createImageFile());
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                File sdcard = Environment.getExternalStorageDirectory();
                intent.setDataAndType(Uri.fromFile(new File(sdcard, INSTALL_NEW_APK_LOCATION)),
                        "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!

            }

            if (android.os.Build.VERSION.SDK_INT < 25) {
                intent.setComponent(comp);
            }

            con.startActivity(intent);
        }

        private File createImageFile() {
            File sdcard = Environment.getExternalStorageDirectory();
            File myDir = new File(sdcard, NEW_APK_LOCATION);
            return new File(myDir, "Maria.apk");
        }
    }

    @Override
    public void onBackPressed() {
    }
}