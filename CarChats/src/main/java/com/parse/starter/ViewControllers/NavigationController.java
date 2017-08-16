package com.parse.starter.ViewControllers;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.starter.R;


import net.alhazmy13.mediapicker.Image.ImagePicker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import FragmentControllers.AddFriendsFragment;
import FragmentControllers.CurrentFriendsFragment;
import FragmentControllers.EditProfileFragment;
import FragmentControllers.ProfileFragment;
import FragmentControllers.SearchForFriends;
import Models.User;
import cn.pedant.SweetAlert.SweetAlertDialog;


public class NavigationController extends AppCompatActivity implements SearchForFriends.OnUserSelected, CurrentFriendsFragment.OnAddNewUserButtonClicked, CurrentFriendsFragment.OnProfileButtonClicked, ProfileFragment.OnRowSelected {

    private Toolbar toolbar;
    private PopupMenu mPopupMenu;
    private DrawerLayout mDrawer;
    Bundle savedInstanceState1;
    User currentUser;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        this.savedInstanceState1 = savedInstanceState;
        currentUser = (User) ParseUser.getCurrentUser();
        //Toolbar (Top)
        toolbar = (Toolbar) findViewById(R.id.custom_toolbar);
        ImageButton menuButton = (ImageButton) findViewById(R.id.toolbar_right_button);
        menuButton.setImageResource(R.drawable.ic_menu_button);
        mPopupMenu = new PopupMenu(this, menuButton);
        MenuInflater menuInflater = mPopupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, mPopupMenu.getMenu());
        menuButton.setOnClickListener(new MenuButtonClickListener());
        setSupportActionBar(toolbar);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        //If user clicks popup, take them to appropriate fragment
        String selectedFragment = getIntent().getStringExtra("notification");
        Log.i("AppInfo", "String selectedFragment: "+selectedFragment);
        if (selectedFragment != null) {
            if (selectedFragment.equals("profilefragment")) {
                home(savedInstanceState);
                onProfileButtonClicked();
            }
        } else {
            home(savedInstanceState);
        }

        //Status bar very top
        Window window = this.getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.palette_lightprimarycolor));

        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        status.getPermissionStatus().getEnabled();
        currentUser.setOneSignalId(status.getSubscriptionStatus().getUserId());
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null){
                    Log.i("AppInfo", "One signal id saved");
                } else {
                    Log.i("AppInfo", e.getMessage());
                }
            }
        });


    }

    public void home(Bundle savedInstanceState){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            // Create a new Fragment to be placed in the activity layout
            CurrentFriendsFragment firstFragment = new CurrentFriendsFragment();
            // Add the fragment to the 'fragment_container' FrameLayout
            fragmentTransaction
                    .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                    .replace(R.id.fragment_container, firstFragment, "firstFragment")
                    .commit();
        }
    }

    @Override
    public void onAddUserClicked() {
        mDrawer.openDrawer(GravityCompat.START);
        /*FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState1 != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            SearchForFriends searchForFriends = new SearchForFriends();
            // Add the fragment to the 'fragment_container' FrameLayout
            fragmentTransaction
                    .setCustomAnimations(R.animator.slide_in_up, R.animator.slide_out_up, R.animator.slide_in_down, R.animator.slide_out_down)
                    .replace(R.id.fragment_container, searchForFriends)
                    .addToBackStack("firstFragment")
                    .commit();
        }*/
    }

    @Override
    public void onUserSelected(String userId) {

        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState1 != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            AddFriendsFragment addFriendsFragment = new AddFriendsFragment();
            Bundle arguments = new Bundle();
            arguments.putString("userid", userId);
            addFriendsFragment.setArguments(arguments);
            // Add the fragment to the 'fragment_container' FrameLayout
            fragmentTransaction
                    .setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right, R.animator.slide_in_right, R.animator.slide_out_left)
                    .replace(R.id.fragment_container, addFriendsFragment)
                    .addToBackStack("firstFragment")
                    .commit();
        }
    }

    @Override
    public void onProfileButtonClicked() {
        /*FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState1 != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            ProfileFragment profileFragment = new ProfileFragment();
            // Add the fragment to the 'fragment_container' FrameLayout
            fragmentTransaction
                    .setCustomAnimations(R.animator.slide_in_up, R.animator.slide_out_up, R.animator.slide_in_down, R.animator.slide_out_down)
                    .replace(R.id.fragment_container, profileFragment)
                    .addToBackStack("firstFragment")
                    .commit();
        }*/
    }

    @Override
    public void onRowSelected(int position) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState1 != null) {
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            EditProfileFragment editProfileFragment = new EditProfileFragment();
            editProfileFragment.setArguments(bundle);
            fragmentTransaction
                    .setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right, R.animator.slide_in_right, R.animator.slide_out_left)
                    .replace(R.id.fragment_container, editProfileFragment)
                    .addToBackStack(null)
                    .commit();

        }
    }

    private class MenuButtonClickListener implements ImageButton.OnClickListener{
        @Override
        public void onClick(View view) {
            mPopupMenu.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();
        if (count == 0){
            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Are you sure?")
                    .setCancelText("Cancel")
                    .setConfirmText("Logout")
                    .showCancelButton(true)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            ParseUser.getCurrentUser().logOut();
                            sweetAlertDialog.cancel();
                            Intent intent = new Intent(getApplicationContext(), LoginController.class);
                            startActivity(intent);
                        }
                    })
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.cancel();
                        }
                    })
                    .show();
        } else {
            getFragmentManager().popBackStack();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> mPaths = (List<String>) data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_PATH);
            try {
                currentUser.setProfilePicture(serialize(data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_PATH)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            currentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null){
                        Log.i("AppInfo", "saved!");
                    } else {
                        Log.i("AppInfo", e.getMessage());
                    }
                }
            });
        }


    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

}

