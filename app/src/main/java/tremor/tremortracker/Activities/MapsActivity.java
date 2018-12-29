package tremor.tremortracker.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import tremor.tremortracker.Model.Earthquake;
import tremor.tremortracker.R;
import tremor.tremortracker.UI.CustomInfoWindow;
import tremor.tremortracker.Util.Constants;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private RequestQueue queue;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private DatabaseReference root, demo;

    private Button showListBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        root = FirebaseDatabase.getInstance().getReference();
        demo=root.child("demo");


        showListBtn = (Button) findViewById(R.id.showListBtn);


        showListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, QuakesListActivity.class));
            }
        });


          queue= Volley.newRequestQueue(this);
          getEarthquakes();


    }


        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera. In this case,
         * we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to install
         * it inside the SupportMapFragment. This method will only be triggered once the user has
         * installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap){
            mMap = googleMap;


            mMap.setInfoWindowAdapter(new CustomInfoWindow(getApplicationContext()));
            mMap.setOnInfoWindowClickListener(this);
            mMap.setOnMarkerClickListener(this);


            mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            mLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.d("Location", location.toString());

                    LatLng newLocation=new LatLng(location.getLatitude(),location.getLongitude());
                    LatLng sydney = new LatLng(-34, 151);
                    mMap.addMarker(new MarkerOptions().position(newLocation).title("New Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation));

                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };
            if (Build.VERSION.SDK_INT < 23) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

                mLocationManager.requestLocationUpdates(mLocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    mLocationManager.requestLocationUpdates(mLocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                    Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                            .title("Hello"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8));

                }

            }

        }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED )
            {
                mLocationManager.requestLocationUpdates(mLocationManager.GPS_PROVIDER, 0, 0, mLocationListener);


            }
        }
    }

    public void getEarthquakes() {

        final Earthquake earthQuake = new Earthquake();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.URL,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray features = response.getJSONArray("features");
                            for (int i = 0; i < Constants.LIMIT; i++) {
                                JSONObject properties = features.getJSONObject(i).getJSONObject("properties");
                                JSONObject geometry = features.getJSONObject(i).getJSONObject("geometry");

                                //get coordinates array
                                JSONArray coordinates = geometry.getJSONArray("coordinates");
                                double lon=coordinates.getDouble(0);
                                double lat=coordinates.getDouble(1);


                                earthQuake.setPlace(properties.getString("place"));
                                earthQuake.setType(properties.getString("type"));
                                earthQuake.setTime(properties.getLong("time"));
                                earthQuake.setLat(lat);
                                earthQuake.setLon(lon);
                                earthQuake.setMagnitude(properties.getDouble("mag"));
                                earthQuake.setDetailLink(properties.getString("detail"));


                                java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance();
                                String formattedDate =  dateFormat.format(new Date(Long.valueOf(properties.getLong("time")))
                                        .getTime());


                                MarkerOptions markerOptions = new MarkerOptions();
                                if (earthQuake.getMagnitude() >= 4.5) {

                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                }
                                else if(earthQuake.getMagnitude() >= 4.0){
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                                }
                                else if(earthQuake.getMagnitude() >= 3.5){
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                                }
                                else
                                {
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                }
                                markerOptions.title(earthQuake.getPlace());
                                markerOptions.position(new LatLng(lat,lon));
                                markerOptions.snippet("Magnitude: "+earthQuake.getMagnitude()+"\n"+
                                                       "Date: "+formattedDate);

                                //Add circle to markers that have mag > x
                                //Add circle to markers that have mag > x
                                if (earthQuake.getMagnitude() >= 4.5 ) {
                                    CircleOptions circleOptions = new CircleOptions();
                                    circleOptions.center(new LatLng(earthQuake.getLat(),
                                            earthQuake.getLon()));
                                    circleOptions.radius(30000);
                                    circleOptions.strokeWidth(2.5f);
                                    circleOptions.fillColor(Color.RED);

                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                                    mMap.addCircle(circleOptions);

                                }



                                Marker marker=mMap.addMarker((markerOptions));
                                marker.setTag(earthQuake.getDetailLink());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 1));
                               // Log.d("Quake:",lon +"," +lat);

                               // Log.d("Properties:",properties.getString("place"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(jsonObjectRequest);
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        //Toast.makeText(getApplicationContext(), marker.getTitle(), Toast.LENGTH_LONG).show();
         getQuakeDetails(marker.getTag().toString());
    }

    private void getQuakeDetails(String url) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                String detailsUrl = "";

                try {
                    JSONObject properties = response.getJSONObject("properties");
                    JSONObject products = properties.getJSONObject("products");
                    JSONArray geoserve = products.getJSONArray("geoserve");

                    for (int i = 0; i < geoserve.length(); i++) {
                        JSONObject geoserveObj = geoserve.getJSONObject(i);

                        JSONObject contentObj = geoserveObj.getJSONObject("contents");
                        JSONObject geoJsonObj = contentObj.getJSONObject("geoserve.json");

                        detailsUrl = geoJsonObj.getString("url");



                    }
                    Log.d("URL: ", detailsUrl);

                    getMoreDetails(detailsUrl);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);



    }


        public void getMoreDetails(String url) {

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                    url, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    dialogBuilder = new AlertDialog.Builder(MapsActivity.this);
                    View view = getLayoutInflater().inflate(R.layout.popup, null);

                    Button dismissButton = (Button) view.findViewById(R.id.dismissPop);
                    Button bookmarkid=(Button)view.findViewById(R.id.bookmarkid);
                    Button dismissButtonTop = (Button) view.findViewById(R.id.desmissPopTop);
                    TextView popList = (TextView) view.findViewById(R.id.popList);
                    WebView htmlPop = (WebView) view.findViewById(R.id.htmlWebview);

                    StringBuilder stringBuilder = new StringBuilder();


                    try {

                        if (response.has("tectonicSummary") && response.getString("tectonicSummary") != null) {

                            final JSONObject tectonic = response.getJSONObject("tectonicSummary");

                            if (tectonic.has("text") && tectonic.getString("text") != null) {

                                final String text = tectonic.getString("text");

                                htmlPop.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null);

                                Log.d("HTML", text);
                                bookmarkid.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        demo.push().setValue(text);

                                    }
                                });

                            }

                        }


                        JSONArray cities = response.getJSONArray("cities");

                        for (int i = 0; i < cities.length(); i++) {
                            JSONObject citiesObj = cities.getJSONObject(i);

                            stringBuilder.append("City: " + citiesObj.getString("name")
                                    + "\n" + "Distance: " + citiesObj.getString("distance")
                                    + "\n" + "Population: "
                                    + citiesObj.getString("population"));

                            stringBuilder.append("\n\n");

                        }

                        popList.setText(stringBuilder);




                        dismissButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        dismissButtonTop.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });


                        dialogBuilder.setView(view);
                        dialog = dialogBuilder.create();
                        dialog.show();




                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });

            queue.add(jsonObjectRequest);

        }
    }


