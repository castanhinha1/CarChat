package FragmentControllers;

import android.app.Fragment;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.nex3z.togglebuttongroup.SingleSelectToggleGroup;
import com.onesignal.OneSignal;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.starter.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

import Models.FollowTable;
import Models.User;
import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


/**
 * Created by Dylan Castanhinha on 3/31/2017.
 */

public class AddFriendsFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    int lengthOfTime;
    String time;
    TextView nametv;
    TextView phonetv;
    TextView detailstv;
    CircleImageView profilepictureview;
    SingleSelectToggleGroup singleSelectToggleGroup;
    User currentUser;
    User selectedUser;
    boolean currentlyFollowing;

    //MapView
    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private boolean mLocationUpdateState;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    MapView mMapView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_friends, container, false);
        getActivity().invalidateOptionsMenu();
        //Get user id from selected contact
        Bundle arguments = getArguments();
        String selectedUserid = arguments.getString("userid");
        currentUser = (User) ParseUser.getCurrentUser();
        //Toolbar top
        final TextView titleTextView = (TextView) getActivity().findViewById(R.id.toolbar_title);
        titleTextView.setText("Provide Location To");
        ImageButton backButton = (ImageButton) getActivity().findViewById(R.id.toolbar_left_button);
        backButton.setImageResource(R.drawable.ic_back_button);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(new CancelButtonListener());
        Button cancelButton = (Button) getActivity().findViewById(R.id.toolbar_left_button_text);
        cancelButton.setVisibility(View.INVISIBLE);
        Button sendButton = (Button) getActivity().findViewById(R.id.toolbar_right_button_text);
        sendButton.setVisibility(View.VISIBLE);
        sendButton.setText("Send");
        sendButton.setOnClickListener(new SendButtonClickListener());
        //Hide bottom navigation view
        BottomNavigationView bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation_navbar);
        bottomNavigationView.setVisibility(View.INVISIBLE);
        //Instantiate textviews and photo
        nametv = (TextView) rootView.findViewById(R.id.add_friend_name_tv);
        phonetv = (TextView) rootView.findViewById(R.id.add_friend_phonenumber_tv);
        detailstv = (TextView) rootView.findViewById(R.id.add_friend_details_tv);
        detailstv.setText("1 Hour");
        profilepictureview = (CircleImageView) rootView.findViewById(R.id.add_friend_photo);
        singleSelectToggleGroup = (SingleSelectToggleGroup) rootView.findViewById(R.id.group_choices);
        singleSelectToggleGroup.setOnCheckedChangeListener(new SelectToggleListener());
        lengthOfTime = 0;
        time = "1 Hour";

        getContactDetails(selectedUserid);

        //MapView
        mMapView = (MapView) rootView.findViewById(R.id.addFriendsMapViewFragment);
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
                googleMap.getUiSettings().setAllGesturesEnabled(false);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                // For dropping a marker at a point on the Map
                LatLng currentUserLocation = new LatLng(currentUser.getGeopoint().getLatitude(), currentUser.getGeopoint().getLongitude());

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(currentUserLocation).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                googleMap.addCircle(new CircleOptions()
                        .center(currentUserLocation)
                        .radius(500)
                        .strokeColor(Color.BLUE)
                        .strokeWidth(3));
            }
        });


        createLocationRequest();

        return rootView;

    }

    //MapView Methods
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
        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mLocationUpdateState) {
            startLocationUpdates();
        }
    }

    //List View
    public void getContactDetails(final String selectedUserid) {
        ParseQuery<User> query = ParseQuery.getQuery("_User");
        query.whereEqualTo("objectId", selectedUserid);
        query.getFirstInBackground(new GetCallback<User>() {
            @Override
            public void done(User object, ParseException e) {
                if (e == null && object != null){
                    selectedUser = object;
                    nametv.setText(selectedUser.getFullName());
                    phonetv.setText("4847072605");
                    profilepictureview.setImageBitmap(selectedUser.getProfilePicture());
                    areUsersCurrentlyFollowing();
                } else {
                    Log.i("AppInfo", e.getMessage());
                }
            }
        });

    }

    public void areUsersCurrentlyFollowing(){
        ParseQuery<FollowTable> query = ParseQuery.getQuery(FollowTable.class);
        query.whereEqualTo("following", selectedUser);
        query.whereEqualTo("isFollowed", currentUser);
        query.getFirstInBackground(new GetCallback<FollowTable>() {
            @Override
            public void done(FollowTable object, ParseException e) {
                if (object != null && e == null){
                    Log.i("AppInfo", "Currently Following: ");
                    currentlyFollowing = true;
                } else{
                    Log.i("AppInfo", "Not following");
                    currentlyFollowing = false;
                }
            }
        });
    }

    private class SendButtonClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View v) {

            if (currentlyFollowing) {
                new SweetAlertDialog(getActivity())
                        .setTitleText(selectedUser.getFirstName() + " is already following you.")
                        .setConfirmText("Okay")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                getFragmentManager().popBackStack();
                                sweetAlertDialog.cancel();
                            }
                        })
                        .show();
            }else {
                new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Share Location With " + selectedUser.getFirstName())
                        .setContentText("For: " + time)
                        .setCancelText("Cancel")
                        .setConfirmText("Send")
                        .showCancelButton(true)
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(final SweetAlertDialog sweetAlertDialog) {
                                if (selectedUser != null) {
                                    if (currentlyFollowing == false) {
                                        FollowTable followTable = new FollowTable();
                                        followTable.setFollowing(selectedUser);
                                        followTable.setIsFollowed(currentUser);
                                        followTable.setRequestConfirmed(false);
                                        ParseACL acl = new ParseACL();
                                        acl.setPublicReadAccess(true);
                                        acl.setPublicWriteAccess(true);
                                        followTable.setACL(acl);
                                        switch (lengthOfTime) {
                                            case 0:
                                                Calendar cal = Calendar.getInstance(); // creates calendar
                                                cal.setTime(new Date()); // sets calendar time/date
                                                cal.add(Calendar.HOUR_OF_DAY, 1); // adds one hour
                                                followTable.setExpirationDate(cal.getTime());
                                                break;
                                            case 1:
                                                Calendar cal2 = Calendar.getInstance(); // creates calendar
                                                cal2.setTime(new Date()); // sets calendar time/date
                                                cal2.add(Calendar.HOUR_OF_DAY, 4); // adds 4 hour
                                                followTable.setExpirationDate(cal2.getTime());
                                                break;
                                            case 2:
                                                Calendar cal3 = Calendar.getInstance(); // creates calendar
                                                cal3.setTime(new Date()); // sets calendar time/date
                                                cal3.add(Calendar.HOUR_OF_DAY, 24); // adds 1 day
                                                followTable.setExpirationDate(cal3.getTime());
                                                break;
                                            case 3:
                                                break;
                                        }
                                        followTable.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    //Popback twice to go to home screen
                                                    sweetAlertDialog.cancel();
                                                    postPushNotificationToUser();
                                                    getFragmentManager().popBackStack();
                                                    getFragmentManager().popBackStack();
                                                } else {
                                                    Log.i("AppInfo", e.getMessage());
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    new SweetAlertDialog(getActivity())
                                            .setTitleText("Select a user.")
                                            .setConfirmText("Okay")
                                            .show();
                                }
                            }
                        })
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.cancel();
                            }
                        })
                        .show();
            }
        }
    }

    public void postPushNotificationToUser(){
        try {
            OneSignal.postNotification(new JSONObject("{'contents': {'en': \""+ currentUser.getFullName() +" wants you to follow them." +"\"}, 'include_player_ids': ['" + selectedUser.getOneSignalId() + "'], 'headings': {'en':'Location Request'} , 'data': {'intent':'profile'}}"),
                    new OneSignal.PostNotificationResponseHandler() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            Log.i("OneSignalExample", "postNotification Success: " + response.toString());
                        }

                        @Override
                        public void onFailure(JSONObject response) {
                            Log.e("OneSignalExample", "postNotification Failure: " + response.toString());
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class CancelButtonListener implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            getFragmentManager().popBackStack();
        }
    }

    private class SelectToggleListener implements SingleSelectToggleGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(SingleSelectToggleGroup group, int checkedId) {
            int id = singleSelectToggleGroup.getCheckedId();
            switch (id) {
                case R.id.a:
                    //1 Hour
                    lengthOfTime = 0;
                    time = "1 Hour";
                    detailstv.setText("1 Hour");
                    break;
                case R.id.b:
                    //4 Hours
                    lengthOfTime = 1;
                    time = "4 Hours";
                    detailstv.setText("4 Hours");
                    break;
                case R.id.c:
                    // 1 Day
                    lengthOfTime = 2;
                    time = "1 Day";
                    detailstv.setText("1 Day");
                    break;
                case R.id.d:
                    //Emergency Contact
                    lengthOfTime = 3;
                    time = "Indefinitely";
                    detailstv.setText("Emergency");
                    break;
                default:
                    //1 Hour
                    lengthOfTime = 0;
                    time = "1 Hour";
                    detailstv.setText("1 Hour");
                    break;
            }
        }
    }
}
