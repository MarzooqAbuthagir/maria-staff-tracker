package Com.mariapublishers.mariaexecutive;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class OthersCheckingHistoryMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    String TAG = "CheckingHistoryMapActivity";
    Toolbar toolbar;
    ActionBar actionBar = null;

    private String str_origin = "";
    private GoogleMap mMap;

    ArrayList<LatLngTracker> arraylist = new ArrayList<>();
    private ArrayList<LatLng> markerPoints = new ArrayList<LatLng>();
    private DownloadTask downloadTask;
    String mapkey = "AIzaSyCzHIJusa16-YdPzDlBuAguRZjqxAW6TMU";
    private ArrayList<HashMap<String, String>> location = new ArrayList<HashMap<String, String>>();
    String keyIntent = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checking_history_map);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        arraylist = bundle.getParcelableArrayList("mylist");

        Intent intent = getIntent();
        keyIntent = intent.getStringExtra("key");

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

        try {
            SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
            supportMapFragment.getMapAsync(OthersCheckingHistoryMapActivity.this);
        } catch (Exception e) {
            System.out.println("onMap Excep " + e.getMessage());
        }
    }

    private void setMarkerPoints(ArrayList<LatLngTracker> arraylist) {
        double latitude = 0.0;
        double longitude = 0.0;
        markerPoints.clear();
        location.clear();
        System.out.println("array size " + arraylist.size());
        if (arraylist.size() > 0) {
            for (int i = 0; i < arraylist.size(); i++) {
                latitude = arraylist.get(i).getLatitude();
                longitude = arraylist.get(i).getLongitude();
                markerPoints.add(new LatLng(latitude, longitude));

                HashMap<String, String> mapvalue;
                mapvalue = new HashMap<>();
                mapvalue.put("Latitude", String.valueOf(arraylist.get(i).getLatitude()));
                mapvalue.put("Longitude", String.valueOf(arraylist.get(i).getLongitude()));
                mapvalue.put("LocationName", arraylist.get(i).getName());
                location.add(mapvalue);
            }
        }

        System.out.println("location size " + location.size());

        if (location.size() > 0) {
            for (int i = 0; i < location.size(); i++) {
                double latti = Double.parseDouble(Objects.requireNonNull(location.get(i).get("Latitude")));
                double longi = Double.parseDouble(Objects.requireNonNull(location.get(i).get("Longitude")));
                if (i == 0 || i == location.size() - 1) {
                    LatLng latLng = new LatLng(latti, longi);
                    MarkerOptions markerOptions = new MarkerOptions().position(latLng).icon(bitmapDescriptorFromVector(OthersCheckingHistoryMapActivity.this, R.mipmap.marker));
                    mMap.addMarker(markerOptions);
                }
            }
        }

        System.out.println("marker size " + markerPoints.size());
        if (markerPoints.size() > 0) {
            for (int i = 0; i < markerPoints.size(); i++) {

                int markersize = markerPoints.size();
                int lastsize = markersize - 1;

                if (i != lastsize) {
                    LatLng origin = markerPoints.get(i);
                    LatLng dest = markerPoints.get(i + 1);
                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(origin, dest);
                    downloadTask = new DownloadTask();
                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                } else {
                    LatLng origin = null;
                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(origin, markerPoints.get(i));
                    downloadTask = new DownloadTask();
                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);

                }
            }
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int marker) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, marker);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=true";

        String key = "key=" + mapkey;
        String parameters = "";
        String str_origin1 = "";

        if (origin != null) {

            // Origin of route
            str_origin1 = "origin=" + origin.latitude + "," + origin.longitude;
            // Building the parameters to the web service
            parameters = str_origin1 + "&" + str_dest + "&" + key;
        } else {

            System.out.println("str_origin" + str_origin);

            parameters = str_origin + "&" + str_dest + "&" + key;
        }

        System.out.println("directionpoints---> " + str_origin1 + " " + str_dest);


        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        System.out.println("directionurl---> " + url);


        return url;
    }


    /**
     * A method to download json data from url
     */
    @SuppressLint("LongLogTag")
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }

        //catch
        catch (Exception e) {
            Log.d("Exception while downloading url", e.toString());
        } finally {
            if (iStream != null) {
                iStream.close();
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return data;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        str_origin = "origin=" + arraylist.get(0).getLatitude() + "," + arraylist.get(0).getLongitude();

        LatLng latLng = new LatLng(arraylist.get(0).getLatitude(), arraylist.get(0).getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));

        setMarkerPoints(arraylist);
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    @SuppressLint("StaticFieldLeak")
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                // Starts parsing data
                routes = parser.parse(jObject);
                System.out.println("json object " + jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = new ArrayList<LatLng>();
            PolylineOptions lineOptions = new PolylineOptions();
            lineOptions.width(4);
            lineOptions.color(Color.BLUE);
            MarkerOptions markerOptions = new MarkerOptions();
            System.out.println("hash results " + result);
            if (result != null) {
                // Traversing through all the routes
                for (int i = 0; i < result.size(); i++) {

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);

                    // Fetching all the points in i-th route
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);
                        double lat = Double.parseDouble(Objects.requireNonNull(point.get("lat")));
                        double lng = Double.parseDouble(Objects.requireNonNull(point.get("lng")));
                        LatLng position = new LatLng(lat, lng);
                        points.add(position);
                    }

                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                }

                // Drawing polyline in the Google Map for the i-th route

                if (points.size() != 0)
                    mMap.addPolyline(lineOptions);
            }
        }
    }

    private void back() {
        Intent iback = new Intent(OthersCheckingHistoryMapActivity.this, OthersCheckingHistoryActivity.class);
        iback.putExtra("key", keyIntent);
        iback.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(iback);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        back();
    }
}