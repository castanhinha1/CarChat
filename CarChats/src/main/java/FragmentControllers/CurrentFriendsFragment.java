package FragmentControllers;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.parse.starter.R;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ConfigClasses.ParseAdapterCustomList;
import Models.FollowTable;
import Models.User;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class CurrentFriendsFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener, LocationListener {

    //MapView
    private GoogleMap googleMap;
    private Marker marker;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private boolean mLocationUpdateState;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    MapView mMapView;

    //ListView
    ArrayList<String> currentFriends;
    ArrayList<Date> expirationDate;
    ArrayList<Date> createdAtDate;
    ListView listview;
    TextView emptytextview;
    CurrentClients adapter;
    User currentUser;
    SwipeRefreshLayout swipeContainer;
    OnAddNewUserButtonClicked activityCallback;
    OnProfileButtonClicked activityCallback2;
    ExpandableLayout expandableLayoutTop;
    ExpandableLayout expandableLayoutBottom;
    //Toolbar
    Toolbar toolbar;
    ImageButton leftToolbarButton;
    ImageButton rightToolbarbutton;
    //NavBar
    BottomNavigationView bottomNavigationView;
    CircleImageView myProfilePictureView;
    TextView nameLabel;
    TextView locationLabel;
    Bitmap profileBitmap;

    public interface OnAddNewUserButtonClicked {
        void onAddUserClicked();
    }

    public interface OnProfileButtonClicked {
        void onProfileButtonClicked();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            activityCallback = (OnAddNewUserButtonClicked) context;
            activityCallback2 = (OnProfileButtonClicked) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        currentUser = (User) ParseUser.getCurrentUser();
        //Toolbar top
        toolbar = (Toolbar) getActivity().findViewById(R.id.custom_toolbar);
        Button backButton = (Button) getActivity().findViewById(R.id.toolbar_left_button_text);
        backButton.setText("");
        backButton.setClickable(false);
        Button doneButton = (Button) getActivity().findViewById(R.id.toolbar_right_button_text);
        doneButton.setText("");
        doneButton.setClickable(false);
        TextView titleTextView = (TextView) getActivity().findViewById(R.id.toolbar_title);
        titleTextView.setText(currentUser.getFirstName() + "'s "+"Buddies");
        leftToolbarButton = (ImageButton) getActivity().findViewById(R.id.toolbar_left_button);
        leftToolbarButton.setImageResource(R.drawable.ic_add_user_green);
        leftToolbarButton.setVisibility(View.VISIBLE);
        leftToolbarButton.setOnClickListener(new AddNewClientButtonListener());
        rightToolbarbutton = (ImageButton) getActivity().findViewById(R.id.toolbar_right_button);
        rightToolbarbutton.setVisibility(View.INVISIBLE);
        //ListView that shows friends sharing location with you
        View rootView = inflater.inflate(R.layout.fragment_current_friends, container, false);
        emptytextview = (TextView) rootView.findViewById(R.id.emptyTextView);
        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);
        listview = (ListView) rootView.findViewById(R.id.current_client_list_view);
        expandableLayoutTop = (ExpandableLayout) rootView.findViewById(R.id.expandable_layout_top);
        expandableLayoutBottom = (ExpandableLayout) rootView.findViewById(R.id.expandable_layout_bottom);
        swipeContainer.setOnRefreshListener(new SwipeToRefresh());
        currentFriends = new ArrayList<>();
        expirationDate = new ArrayList<>();
        createdAtDate = new ArrayList<>();
        findPeopleFollowing();
        //NavBar
        bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation_navbar);
        bottomNavigationView.setOnClickListener(new BottomNavClickListener());
        myProfilePictureView = (CircleImageView) getActivity().findViewById(R.id.profile_picture_navbar);
        nameLabel = (TextView) getActivity().findViewById(R.id.nameLabel);
        locationLabel = (TextView) getActivity().findViewById(R.id.locationLabel);
        new GetPhotoAsync().execute();
        nameLabel.setText(currentUser.getFullName());
        locationLabel.setText(currentUser.getLocation());

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_green_light);
        //MapView
        mMapView = (MapView) rootView.findViewById(R.id.mapViewFragment);
        mMapView.onCreate(savedInstanceState);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                mMap.getUiSettings().setZoomControlsEnabled(true);
                LatLng currentUserLocation = null;
                if (currentUser.getLatitude() != 0) {
                    // For dropping a marker at a point on the Map
                    currentUserLocation = new LatLng(currentUser.getLatitude(), currentUser.getLongitude());
                    placeMarkerOnMap(currentUser);
                }else {
                    createLocationRequest();
                }
                if (currentUserLocation != null) {
                    // For zooming automatically to the location of the marker
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(currentUserLocation).zoom(12).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                } else {
                    createLocationRequest();
                }
            }
        });
        createLocationRequest();
        return rootView;
    }

    class GetPhotoAsync extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            profileBitmap = currentUser.getProfilePicture();
            return null;
        }
        @Override
        protected void onPostExecute(String s){
            myProfilePictureView.setImageBitmap(profileBitmap);
        }
    }


    private class BottomNavClickListener implements BottomNavigationView.OnClickListener{

        @Override
        public void onClick(View v) {
            activityCallback2.onProfileButtonClicked();
        }
    }

    //MAPVIEW METHODS

    private void setUpMap() {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]
                    {android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        googleMap.setMyLocationEnabled(true);

        LocationAvailability locationAvailability =
                LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient);
        if (null != locationAvailability && locationAvailability.isLocationAvailable()) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                LatLng currentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation
                        .getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom
                        (currentLocation, 12));
            }
        }
        googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                leftToolbarButton.setImageResource(R.drawable.ic_back_button);
                leftToolbarButton.setOnClickListener(new BackButtonListener());
                expandableLayoutBottom.collapse();
            }
        });
    }

    private class BackButtonListener implements ImageButton.OnClickListener{
        @Override
        public void onClick(View v) {
            expandableLayoutBottom.expand();
            leftToolbarButton.setImageResource(R.drawable.ic_add_user_green);
            leftToolbarButton.setOnClickListener(new AddNewClientButtonListener());
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop(){
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() ){
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        setUpMap();
        if (mLocationUpdateState) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (null != mLastLocation) {
            ParseGeoPoint point = new ParseGeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            currentUser.setGeopoint(point);
            currentUser.saveInBackground();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private String getAddress( LatLng latLng ) {
        Geocoder geocoder = new Geocoder( getActivity() );
        String addressText = "";
        List<Address> addresses = null;
        Address address = null;
        try {
            addresses = geocoder.getFromLocation( latLng.latitude, latLng.longitude, 1 );
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses.get(0);
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    addressText += (i == 0)?address.getAddressLine(i):("\n" + address.getAddressLine(i));
                }
            }
        } catch (IOException e ) {
        }
        return addressText;
    }

    public void placeMarkerOnMap(User user) {
        LatLng latLng = new LatLng(user.getLatitude(), user.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        String titleStr = user.getFullName();  // add these two lines
        markerOptions.title(titleStr);
        if (user.getProfilePicture() != null) {
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(user.getProfilePicture()));
        } else {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.com_facebook_profile_picture_blank_square));
        }
        if (marker != null) {
            marker.remove();
            marker = googleMap.addMarker(markerOptions);
            marker.showInfoWindow();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom
                    (latLng, 12));
        } else {
            marker = googleMap.addMarker(markerOptions);
            marker.showInfoWindow();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom
                    (latLng, 12));
        }
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,
                this);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        mLocationUpdateState = true;
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                mLocationUpdateState = true;
                startLocationUpdates();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        bottomNavigationView.setVisibility(View.VISIBLE);
        if (mGoogleApiClient.isConnected() && !mLocationUpdateState) {
            startLocationUpdates();
        }
    }

    //LISTVIEW METHODS

    private class SwipeToRefresh implements SwipeRefreshLayout.OnRefreshListener{

        @Override
        public void onRefresh() {
            adapter.loadObjects();
        }
    }

    private class AddNewClientButtonListener implements ImageButton.OnClickListener{
        @Override
        public void onClick(View v) {
            activityCallback.onAddUserClicked();
        }
    }

    public List findPeopleFollowing(){
        ParseQuery<FollowTable> query = ParseQuery.getQuery(FollowTable.class);
        query.whereEqualTo("following", currentUser);
        query.whereEqualTo("requestConfirmed", true);
        query.findInBackground(new FindCallback<FollowTable>() {
            @Override
            public void done(List<FollowTable> objects, ParseException e) {
                if (objects.size() != 0){
                    for (int i = 0; i < objects.size(); i++){
                        currentFriends.add(objects.get(i).getIsFollowed().getObjectId());
                        expirationDate.add(objects.get(i).getExpirationDate());
                        createdAtDate.add(objects.get(i).getCreatedAt());
                    }
                    adapter = new CurrentClients(getActivity());
                    listview.setAdapter(adapter);
                    listview.setEmptyView(emptytextview);
                } else {
                    Log.i("AppInfo", "coming here");
                    //Blank profile add
                }
            }
        });
        return currentFriends;
    }


    private class CurrentClients extends ParseAdapterCustomList implements ParseQueryAdapter.OnQueryLoadListener {
        private CurrentClients(final Context context){
            super(context, new ParseQueryAdapter.QueryFactory<User>(){
               public ParseQuery<User> create() {
                   ParseQuery<User> query = ParseQuery.getQuery(User.class);
                   Log.i("AppInfo", "User id: "+currentFriends);
                   query.whereContainedIn("objectId", currentFriends);
                   query.whereNotEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());

                   return query;

               }
            });
            addOnQueryLoadListener(this);
        }


        @Override
        public void onLoading() {
            swipeContainer.setRefreshing(true);
            Log.i("AppInfo", "Loading");
        }

        @Override
        public void onLoaded(List objects, Exception e) {
            swipeContainer.setRefreshing(false);
            Log.i("AppInfo", "Loaded");
        }

        public String calculateDistance(User user) {
            double distance = Math.round(user.getGeopoint().distanceInMilesTo(currentUser.getGeopoint()));
            String distanceInMiles = String.valueOf(distance);
            return distanceInMiles;
        }


        @Override
        public View getItemView(final User user, View v, ViewGroup parent){
            if (v == null){
                v = View.inflate(getContext(), R.layout.list_layout_current_friends, null);
            }
            super.getItemView(user, v, parent);

            //Add the title view
            Log.i("AppInfo", "User: "+user);
            TextView nameTextView = (TextView) v.findViewById(R.id.current_client_text_view_name);
            nameTextView.setText(user.getFullName());

            //Add the Location label
            TextView location = (TextView) v.findViewById(R.id.current_client_object_id);
            location.setText(user.getLocation());

            //Add the distance label
            TextView distanceLabel = (TextView) v.findViewById(R.id.distanceLabel);
            distanceLabel.setText(calculateDistance(user)+" miles");

            //Add the image
            CircleImageView imageView = (CircleImageView) v.findViewById(R.id.imageView3);
            imageView.setImageBitmap(user.getProfilePicture());

            //On click listener for selection
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    placeMarkerOnMap(user);
                }
            });
            return v;
        }
    }
}

