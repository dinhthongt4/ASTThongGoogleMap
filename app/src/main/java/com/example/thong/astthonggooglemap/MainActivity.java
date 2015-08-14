package com.example.thong.astthonggooglemap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Log;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.thong.astthonggooglemap.adapter.RecyclerViewAdapter;

import com.example.thong.astthonggooglemap.model.LocationSearch;
import com.example.thong.astthonggooglemap.parse.RestClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@EActivity(R.layout.activity_main)
public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    //private String url = "https://api.foursquare.com/v2/venues/search?intent=global&ll=16.0677948,108.2207947&query=cau%20song%20han&oauth_token=ESP5PMNAAH4ZCSMGYWCYHC52L0NDL21A3LHHUIEJY3ML2F5T&v=20141127";
    // https://maps.googleapis.com/maps/api/directions/json?origin=16.0,108.0&destination=16.0533805880463,108.20274876462453&sensor=false

    private GoogleMap mMap;
    private ArrayList<LocationSearch> mLocationSearches = new ArrayList<>();
    private RecyclerViewAdapter mRecyclerViewAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private LatLng mStart;
    private LatLng mEnd;
    private Marker mMarker;
    private ArrayList<Marker> mMarkers;
    private float zoom = 15;
    private Marker mMarkerMaster;
    private boolean mDrawAll;

    @ViewById(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @ViewById(R.id.edtSearch)
    EditText mEdtSearch;

    @ViewById(R.id.imgSearch)
    ImageView mImgSearch;

    @Click(R.id.imgSearch)
    public void searchPlace() {

        mRecyclerView.setVisibility(View.VISIBLE);
        if (mLocationSearches != null) {
            mLocationSearches.clear();

        }
        String search = mEdtSearch.getText().toString();
        try {
            search = URLEncoder.encode(search, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (search != null && !search.trim().equals("")) {
            loadPlace(search);
        }
    }

    @Click(R.id.btnNavigation)
    public void intentNavigation() {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://ditu.google.cn/maps?f=d&source=s_d" +
                        "&saddr=31.249351,121.45905&daddr=31.186371,121.489885&hl=zh&t=m&dirflg=d"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
    }

    @Click(R.id.btnPolygons)
    public void drawPolygonsExample() {
        PolygonOptions rectOptions = new PolygonOptions()
                .add(new LatLng(37.35, -122.0),
                        new LatLng(37.45, -122.0),
                        new LatLng(37.45, -122.2),
                        new LatLng(37.35, -122.2),
                        new LatLng(37.35, -122.0));
        rectOptions.fillColor(Color.RED);
        Polygon polygon = mMap.addPolygon(rectOptions);
    }


    @AfterViews
    public void init() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setVisibility(View.GONE);


    }

    // load place in foursquare API
    @Background
    public void loadPlace(String search) {
        RestClient restClient = new RestClient();
        mLocationSearches = restClient.getApiService().getLocationSearches("global", "16.0677948,108.2207947", search, "ESP5PMNAAH4ZCSMGYWCYHC52L0NDL21A3LHHUIEJY3ML2F5T", "20141127");
        LocationSearch locationSearch = new LocationSearch();
        locationSearch.setName("All");
        mLocationSearches.add(locationSearch);
        setPlace();
    }


    @UiThread
    public void setPlace() {
        mRecyclerViewAdapter = new RecyclerViewAdapter(mLocationSearches);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        mRecyclerViewAdapter.onItemRecyclerViewListener(new RecyclerViewAdapter.ItemOnClickListener() {
            @Override
            public void onClick(LocationSearch locationSearch) {

                mRecyclerView.setVisibility(View.GONE);
                if (locationSearch.getName().equals("All")) {

                    if (mMap.getCameraPosition().zoom >= 14) {
                        drawAllMarker();
                    } else {
                        mMarkerMaster = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(mLocationSearches.get(0).getLocation().getLat()
                                        , mLocationSearches.get(0).getLocation().getLng()))
                                .icon(BitmapDescriptorFactory.fromBitmap(createMarker())));
                    }

                    mDrawAll = true;

                } else {
                    mEnd = new LatLng(locationSearch.getLocation().getLat(), locationSearch.getLocation().getLng());
                    mMap.addMarker(new MarkerOptions().position(mEnd).title("Place"));
                    String url = " https://maps.googleapis.com/maps/api/directions/json?origin=" + mEnd.latitude + "," + mEnd.longitude + "&destination=" + mStart.latitude + "," + mStart.longitude + "&sensor=false";
                    Log.v("Main", url);
                    downloadPositionMap(url);
                    mDrawAll = false;
                    if (mMarkerMaster != null) {
                        mMarkerMaster.remove();
                        mMarkerMaster = null;
                    }
                    if (mMarkers != null) {
                        for (int i = 0; i < mLocationSearches.size() - 1; i++) {
                            if (mMarkers.get(i) != null) {

                                mMarkers.get(i).remove();
                            }
                        }
                    }
                }
            }
        });
    }

    // load list position between two place
    @Background
    public void downloadPositionMap(String mUrl) {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        ArrayList<LatLng> latLngs = new ArrayList<>();
        try {
            URL url = new URL(mUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            br.close();

        } catch (Exception e) {
            Log.d("Exception while downloading url", e.toString());
        } finally {
            urlConnection.disconnect();
        }

        try {
            JSONObject rootObject = new JSONObject(data);
            JSONArray jsonArrayRoutes = rootObject.getJSONArray("routes");
            for (int i = 0; i < jsonArrayRoutes.length(); i++) {
                JSONArray jsonArrayLegs = ((JSONObject) jsonArrayRoutes.get(i)).getJSONArray("legs");
                for (int j = 0; j < jsonArrayLegs.length(); j++) {
                    JSONArray jsonArraySteps = ((JSONObject) jsonArrayLegs.get(j)).getJSONArray("steps");
                    for (int k = 0; k < jsonArraySteps.length(); k++) {
                        String polyline = (String) ((JSONObject) ((JSONObject) jsonArraySteps.get(k)).get("polyline")).get("points");
                        latLngs.addAll((ArrayList<LatLng>) decodePoly(polyline));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        setPolyline(latLngs);
    }

    // Draw line between two place
    @UiThread
    public void setPolyline(ArrayList<LatLng> latLngs) {

        PolylineOptions lineOptions = new PolylineOptions();
        lineOptions.addAll(latLngs);
        lineOptions.width(2);
        lineOptions.color(Color.RED);
        mMap.addPolyline(lineOptions);
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    // Get my location
    private LatLng getLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        Location myLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (myLocation != null) {
            double dLatitude = myLocation.getLatitude();
            double dLongitude = myLocation.getLongitude();
            Log.v("main", "success");
            return new LatLng(dLatitude, dLongitude);
        } else {
            Log.v("main", "error");
            return new LatLng(16, 108);
        }
    }

    // Draw add marker
    private void drawAllMarker() {

        mMarkers = new ArrayList<>();
        for (int i = 0; i < mLocationSearches.size() - 1; i++) {
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(mLocationSearches.get(i).getLocation().getLat(), mLocationSearches.get(i).getLocation().getLng())).title(mLocationSearches.get(i).getName());
            Marker marker = mMap.addMarker(markerOptions);
            mMarkers.add(marker);
        }

    }

    private Bitmap createMarker() {

        RelativeLayout relativeLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(100, 100);

        ImageView imageView = new ImageView(this);
        RelativeLayout.LayoutParams layoutParamsImg = new RelativeLayout.LayoutParams(100, 100);
        imageView.setLayoutParams(layoutParamsImg);
        imageView.setImageResource(R.drawable.maker);
        relativeLayout.addView(imageView);

        TextView textView = new TextView(this);
        RelativeLayout.LayoutParams layoutParamsTxt = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParamsTxt.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        textView.setLayoutParams(layoutParamsTxt);
        textView.setText("" + mLocationSearches.size());
        imageView.setLayoutParams(layoutParams);
        relativeLayout.addView(textView);

        relativeLayout.setLayoutParams(layoutParams);

        relativeLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        relativeLayout.layout(0, 0, relativeLayout.getMeasuredWidth(), relativeLayout.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(relativeLayout.getMeasuredWidth(), relativeLayout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(b);
        relativeLayout.draw(c);

        return b;
    }

    public void setListener() {

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

                if (mDrawAll) {

                    if (cameraPosition.zoom < 14 && zoom >= 14 && mLocationSearches.size() > 0) {

                        if (mMarkers != null) {
                            for (int i = 0; i < mLocationSearches.size() - 1; i++) {
                                if (mMarkers.get(i) != null) {

                                    mMarkers.get(i).remove();
                                }
                            }
                        }

                        mMarkerMaster = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(mLocationSearches.get(0).getLocation().getLat()
                                        , mLocationSearches.get(0).getLocation().getLng()))
                                .icon(BitmapDescriptorFactory.fromBitmap(createMarker())));
                        zoom = cameraPosition.zoom;


                    } else if (cameraPosition.zoom >= 14 && zoom < 14 && mLocationSearches.size() > 0) {

                        if (mMarkerMaster != null) {
                            mMarkerMaster.remove();
                            mMarkerMaster = null;
                        }
                        drawAllMarker();
                        zoom = cameraPosition.zoom;
                    }
                    Log.v("Zom", String.valueOf(cameraPosition.zoom));
                }
            }
        });

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (mMarker != null) {
                    mMarker.remove();
                    mMarker = null;
                }

                mStart = new LatLng(location.getLatitude(), location.getLongitude());

                mMarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .title("My location"));
                // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17));
            }
        });


    }

    public void createThread() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });
        thread.start();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mStart = getLocation();
        mMarker = mMap.addMarker(new MarkerOptions().position(mStart).title("Mylocation"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mStart, 15));
        setListener();
    }
}
