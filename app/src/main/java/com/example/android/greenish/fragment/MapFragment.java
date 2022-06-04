package com.example.android.greenish.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.android.greenish.MarkerInfo;
import com.example.android.greenish.R;
import com.example.android.greenish.dialog.AddTreeDialog;
import com.example.android.greenish.dialog.MarkerInfoDialog;
import com.example.android.greenish.fragment.callbacks.OnPlantTreeListener;
import com.example.android.greenish.model.User;
import com.example.android.greenish.util.DateUtils;
import com.example.android.greenish.util.DistanceUtils;
import com.example.android.greenish.util.GpsUtils;
import com.example.android.greenish.util.ZoomLevel;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapFragment extends Fragment  implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    // constants
    private static final String TAG = "log_trace";
    private static final int GEOFENCE_RADIUS = 10;
    private static final int[] vectors = {R.drawable.ic_greentree, R.drawable.ic_orangetree,
            R.drawable.ic_redtree};

    // vars
    private User user;
    private Context context;
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private Circle mCircle;
    private Marker mMarker;
    private LatLng mLatLng;
    private Location userLocation;
    private MarkerInfo markerInfo;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private final ArrayList<Marker> markers = new ArrayList<>();
    private final ArrayList<MarkerInfo> markerInfoArrayList = new ArrayList<>();
    DatabaseReference reference;

    /**
     * for testing purposes
     * public LatLng(@north-south double latitude, @east-west double longitude)
     */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reference = FirebaseDatabase.getInstance().getReference();
        Log.d(TAG, "onCreate: reference ...");
    }

    /**
     *
     */
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Log.d(TAG, "onLocationResult: - triggered");
            userLocation = locationResult.getLastLocation();
            mLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            addCircle(mLatLng, GEOFENCE_RADIUS);
            markerInfo = new MarkerInfo();
            markerInfo.setLatLng(mLatLng);
            markerInfo.setTitle(mLatLng.latitude + ", " + mLatLng.longitude);

        }


    };

    public MapFragment() {

    }


    /**
     * Changing in states of the fragment throughout lifecycle.
     * */
    @Override
    public void onAttach(@NonNull Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Initialize view
        Log.d(TAG, "onCreateView: triggered");
        View v = inflater.inflate(R.layout.fragment_map, container, false); // Get layout

        // Initialize map fragment
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) { // If layout couldn't  find/inflate  ( R.id.map ).
            Log.d(TAG, "onCreateView: mapFragment is null");
            FragmentManager fManager = getParentFragmentManager();
            FragmentTransaction fTransaction = fManager.beginTransaction();
            mapFragment = SupportMapFragment.newInstance(); // Creates a map fragment, using default options programmatically.
            fTransaction.replace(R.id.map, mapFragment).commit();
        }

        // Async map
        mapFragment.getMapAsync(this);
        initView(v);
        Log.d(TAG, "onCreateView: return");
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated:  triggered !");
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: return");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     *
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    /** @param googleMap, the GoogleMap is an object that is received on a onMapReady() event */
    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // When map is loaded
        this.mMap = googleMap;
        Log.d(TAG, "onMapReady: ... start > ");
        initMap();
        checkSettingsAndStartLocationUpdates();
        Log.d(TAG, "onMapReady: 1");
        mMap.setOnMarkerClickListener(this);

        // retrieve data from firebase
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        }).start();
        Log.d(TAG, "onMapReady: 2");

    }

    private void initMap()
    {
        Log.d(TAG, "initMap: triggered");
        // Disable toolBar
        this.mMap.getUiSettings().setMapToolbarEnabled(false);

        this.mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }

        Log.d(TAG, "initMap: Accessing location permitted");
        // Enable Focus on user location - relocate -
        this.mMap.setMyLocationEnabled(true);
        moveMyLocationButton();
        moveCompassButton();

    }

    /**
     * reset my position button
     * TODO: To get View of the map you've to ensure first it's MapAsync method been called and implemented.
     */
    private void moveMyLocationButton()
    {
        View mapView = mapFragment.getView();
        try {
            if (mapView != null) { // skip this if the mapView has not been set yet.
                Log.d(TAG, "moveMyLocationButtonButton()");

                View view = mapView.findViewWithTag("GoogleMapMyLocationButton");

                // move the location button to the bottom right corner.
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, // width
                        RelativeLayout.LayoutParams.WRAP_CONTENT); // height

                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                layoutParams.setMargins(0, 0, 30, 150);

                view.setLayoutParams(layoutParams);

            } else {
                Log.d(TAG, "moveMyLocationButton: mapView is null !");
            }
        } catch (Exception ex) {
            Log.d(TAG, "moveMyLocationButton() - failed: " + ex.getMessage()); // returns the name of the exception.
            ex.printStackTrace(); // diagnosing.
        }
    }

    /**
     * reset my position button
     */
    private void moveCompassButton()
    {
        View mapView = mapFragment.getView();
        try {
            if (mapView != null) { // skip this if the mapView has not been set yet.
                Log.d(TAG, "moveCompassButton()");

                View view = mapView.findViewWithTag("GoogleMapCompass");

                // move the compass button to the top right corner.
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, // width
                        RelativeLayout.LayoutParams.WRAP_CONTENT); // height

                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
                layoutParams.setMargins(0, 30, 30, 0);

                view.setLayoutParams(layoutParams);

            } else {
                Log.d(TAG, "moveCompassButton: mapView is null !");
            }
        } catch (Exception ex) {
            Log.d(TAG, "moveCompassButton() - failed: " + ex.getMessage()); // returns the name of the exception.
            ex.printStackTrace(); // diagnosing.
        }
    }

    /**
     * Retrieve all trees from firebase.
     */
    //   MarkerInfo markerInfo;
    private void loadData() {
        Log.d(TAG, "loadData: triggered");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                user = snapshot
                        .child("users")
                        .child(FirebaseAuth.getInstance().getUid())
                        .getValue(User.class);

                for (DataSnapshot child : snapshot.child(MarkerInfo.class.getSimpleName())
                        .getChildren()) {

                    // Each model holds info for a single tree.
                    MarkerInfo model = child.getValue(MarkerInfo.class);
                    Log.d(TAG, "onDataChange: child : " + child.getKey());

                    // load data
                    MarkerInfo info = new MarkerInfo();
                    info.setUser(model.getUser());
                    info.setKey(child.getKey());
                    info.setLatLng(new LatLng(model.getLatitude(),model.getLongitude()));
                    info.lastWatering = model.lastWatering;
                    info.plantDate = model.plantDate;

                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(new LatLng(info.getLatitude(), info.getLongitude()));

                    switch (DateUtils.compareDate(model.lastWatering)) {
                        case DateUtils.HIGH_NEED:
                            markerOptions.icon(getBitmapDescriptorFromVectorDrawable(context, vectors[2]));
                            break;

                        case DateUtils.NORMAL_NEED:
                            markerOptions.icon(getBitmapDescriptorFromVectorDrawable(context, vectors[1]));
                            break;

                        default: // DateUtils.LOW_NEED
                            markerOptions.icon(getBitmapDescriptorFromVectorDrawable(context, vectors[0]));
                    }

                    if (mMap != null)
                    {
                        Marker marker = mMap.addMarker(markerOptions);
                        markers.add(marker);
                        markerInfoArrayList.add(info);

                        if (marker != null) {
                            marker.setTag(markers.size() -1);
                        }
                    }
                }

                Log.d(TAG, "loadData: return .., ");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                //handle errors
            }
        });
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker)
    {
        // send Info back to activity
        // or open dialog directly from here ...
        boolean isUserIn = detectIfMarkerWithinBoundary(marker, mCircle);

        int tag = -1;
        if (marker.getTag() != null) {
            tag = (Integer) marker.getTag();
        }

        MarkerInfoDialog dialog;
        Log.d(TAG, "onMarkerClick: triggered");
        if (tag == -1) {
            Log.d(TAG, "onMarkerClick: tag = -1");
            dialog = MarkerInfoDialog.newInstance(
                    "~",
                    DateUtils.formatDateTo("YYYY/MM/dd", new Date()),
                    DateUtils.formatDateTo("YYYY/MM/dd", new Date()),
                    "High/Normal/Low", "\" ~ \"", isUserIn
            );
        } else {
            Log.d(TAG, "onMarkerClick: tag retrieved");
            MarkerInfo info = markerInfoArrayList.get(tag);
            dialog = MarkerInfoDialog.newInstance(
                    info.getKey(),
                    info.plantDate,
                    info.lastWatering,
                    DateUtils.compareDate(markerInfoArrayList.get(tag).lastWatering),
                    info.getUser().firstName,
                    isUserIn);
        }

        Log.d(TAG, "onMarkerClick: show dialog");
        dialog.show(getParentFragmentManager(), null);
        return false;
    }

    /**
     *
     * @param context
     * @param data
     * @param vectorResId
     */
    private void addMarker(Context context, MarkerInfo data, int vectorResId)
    {
        if (mMarker != null) {
            mMarker.remove();
        }
        mMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(data.getLatitude(), data.getLongitude()))
                .title(data.getTitle())
                .snippet(data.getSnippet())
                .icon(getBitmapDescriptorFromVectorDrawable(context, vectorResId))
        );

    }

    /**
     *
     * @param latLng
     * @param radius
     */
    private void addCircle(LatLng latLng, float radius)
    {
        if (mCircle != null) {
            mCircle.remove();
        }
        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(radius)
                .strokeColor(Color.argb(255, 255, 0, 0))
                .fillColor(Color.argb(46, 255, 0, 0))
                .strokeWidth(2);

        mCircle = mMap.addCircle(circleOptions);
    }


    /**
     *
     * @param context
     * @param vectorResId
     * @return
     */
    private BitmapDescriptor getBitmapDescriptorFromVectorDrawable(Context context, int vectorResId)
    {
        Drawable drawable = ContextCompat.getDrawable(context, vectorResId);
        if (drawable != null) {
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.draw(canvas);
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        } else {
            Log.d(TAG, "getBitmapDescriptorFromVectorDrawable: NullPointerException: drawable is null ... ");
        }

        return BitmapDescriptorFactory.defaultMarker();
    }

    private boolean detectIfMarkerWithinBoundary(Marker marker, Circle circle)
    {
        if (circle == null)
            return false;
        double distance = DistanceUtils.getDistanceInMetersUsingAndroidLibrary(circle.getCenter().latitude, circle.getCenter().longitude,
                marker.getPosition().latitude, marker.getPosition().longitude);
        if (distance <= circle.getRadius()) {
            circle.setFillColor(Color.argb(50, 0, 255, 0));
            Toast.makeText(context, "_ Inside :: ", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            circle.setFillColor(Color.argb(50, 255, 0, 0));
            Toast.makeText(context, "_ Outside :: ", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * The moveCamera method repositions the camera according to the instructions defined in the update.
     * @param data
     * @param zoom
     */
    private void moveCamera (MarkerInfo data, float zoom)
    {
        if (mMap != null) {
            Log.d(TAG, "moveCamera: moving the camera to: lat: " + data.getLatitude() + ", lng: " + data.getLongitude());
            mMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(new LatLng(data
                            .getLatitude(), data.getLongitude()), zoom));

        } else {
            Log.d(TAG, "moveCamera: map hasn't been initialized _ !");
        }
    }


    /**
     * Access user location
     */
    private void checkSettingsAndStartLocationUpdates()
    {
        Log.d(TAG, "checkSettingsAndStartLocationUpdates: triggered");
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(GpsUtils.getLocationRequest()).build();

        SettingsClient client = LocationServices.getSettingsClient(context);

        Task<LocationSettingsResponse> responseTask = client.checkLocationSettings(request);
        responseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // Settings of device are satisfied and we can start location updates.
                startLocationUpdates();
            }
        });

        responseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException apiException = (ResolvableApiException) e;
                    try {
                        if (getActivity() != null) // returns the Activity the fragment is associated with.
                            apiException.startResolutionForResult(getActivity(), GpsUtils.REQUEST_CHECK_SETTING);
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        sendIntentException.printStackTrace();
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void startLocationUpdates()
    {
        Log.d(TAG, "startLocationUpdates: triggered");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            boolean isOk = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);
            if (!isOk)
                return;
        }
        if (mFusedLocationProviderClient == null) {
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        }
        mFusedLocationProviderClient.requestLocationUpdates(GpsUtils.getLocationRequest(),
                mLocationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates()
    {
        if (mFusedLocationProviderClient != null)
        {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
    }

    /**
     * callback from AddTreeDialog,
     * triggered when user press "Add Tree" Button.
     * */
    private final OnPlantTreeListener onPlantTreeListener = new OnPlantTreeListener() {
        @Override
        public void onPlantTree(boolean ok, MarkerOptions markerOptions) {
            if (ok)
            {
                if (checkPlantCondition()) {
                    markerOptions.icon(getBitmapDescriptorFromVectorDrawable(context, vectors[0]));
                    Marker marker = MapFragment.this.mMap.addMarker(markerOptions);
                    markers.add(marker);
                    MarkerInfo markerInfo = new MarkerInfo();
                    markerInfo.setLatLng(markerOptions.getPosition());
                    postTreeData(markerInfo);
                } else {
                    Toast.makeText(context, "There is a tree at the current location", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    private boolean checkPlantCondition()
    {
        for (int i = 0; i < markers.size(); i++) {
            if (!checkDuplicateTree(markers.get(i), mCircle))
                    return false;
        }
        return true;
    }

    private boolean checkDuplicateTree(Marker marker, Circle circle) {

        double distance = DistanceUtils.getDistanceInMetersUsingAndroidLibrary(circle.getCenter().latitude,
                circle.getCenter().longitude, marker.getPosition().latitude, marker.getPosition().longitude);
        if (distance <= circle.getRadius()) {
            circle.setFillColor(Color.argb(50, 0, 255, 0));
            return false;
        } else {
            circle.setFillColor(Color.argb(50, 255, 0, 0));
            return true;
        }
    }

    private void postTreeData(MarkerInfo markerInfo)
    {
        String uid = FirebaseAuth.getInstance().getUid();

        markerInfo.lastWatering = DateUtils.dateHelper(DateUtils.DATE_USE_SLASH);

        markerInfo.plantDate = new Date(System.currentTimeMillis()).toString();
        markerInfo.setUser(user);
        final String pathString = String.valueOf(System.currentTimeMillis());
        reference.child(MarkerInfo.class.getSimpleName()).child(pathString)
                .setValue(markerInfo);

        markerInfo.plantDate = new Date(System.currentTimeMillis()).toString();
        user.plant = user.plant+1;
        reference.child("users").child(uid).child("plant").setValue(user.plant);
        reference.child(MarkerInfo.class.getSimpleName()).child(pathString)
                .setValue(markerInfo);

        ///////////////////////

        markerInfoArrayList.add(markerInfo);
    }

    private void initView(View view)
    {
        Log.d(TAG, "initView: triggered");
        ImageButton actionButton = view.findViewById(R.id.addTreeFloatingActionButton);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userLocation != null) {
                    // get current location
                    if (markerInfo == null)
                        return;

                    double latitude = markerInfo.getLatitude(); // can be replaced with userLocation.getLatitude();
                    double longitude = markerInfo.getLongitude(); // can be replaced with userLocation.getLongitude();
                    MarkerInfo markerInfo = new MarkerInfo();
                    markerInfo.setLatLng(mLatLng);
                    markerInfo.setIcon(vectors[0]);
                    moveCamera(markerInfo, ZoomLevel.BUILDINGS_LEVEL);

                    // popup dialog
                    ArrayList<String> info = geocode(latitude, longitude);
                    AddTreeDialog dialog = AddTreeDialog.newInstance(onPlantTreeListener, info);

                    dialog.show(getParentFragmentManager(), "AddTreeDialog");

                } else {
                    Toast.makeText(context, "Plz, wait a bit", Toast.LENGTH_SHORT).show();
                    checkSettingsAndStartLocationUpdates();
                }
            }
        });
    }

    private ArrayList<String> geocode(double lat, double lng)
    {
        Geocoder geocoder = new Geocoder(context);
        List<Address> list = new ArrayList<>();
        ArrayList<String> addressInfo = new ArrayList<>();
        try {
            list = geocoder.getFromLocation(lat, lng, 1);
        } catch (java.io.IOException e) {
            Log.d(TAG, "onComplete: failed to know address from coordinates");
        } finally {

            if (list != null && list.size() > 0) {

                Address address = list.get(0);
                addressInfo.add(String.valueOf(lat));
                addressInfo.add(String.valueOf(lng));
                addressInfo.add(address.getCountryName());
                addressInfo.add(address.getAdminArea());
                addressInfo.add(address.getSubAdminArea());
            }
        }
        return addressInfo;
    }


}