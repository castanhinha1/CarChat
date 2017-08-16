package com.parse.starter.ViewControllers;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.design.widget.NavigationView;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.parse.ParseUser;
import com.parse.starter.R;

import net.alhazmy13.mediapicker.Image.ImagePicker;

import java.util.List;

import ConfigClasses.MyProfilePictureView;
import FragmentControllers.EditProfileFragment;
import FragmentControllers.ProfileFragment;
import Models.User;


public class NewNavigationController extends AppCompatActivity implements ProfileFragment.OnRowSelected {

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private NavigationView nvDrawer;
    private View headerLayout;
    private MyProfilePictureView headerPhoto;
    private TextView headerLabel;

    User currentUser;
    Bundle savedInstanceState1;

    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_navigation_controller);
        currentUser = (User) ParseUser.getCurrentUser();
        this.savedInstanceState1 = savedInstanceState;

        // Set a Toolbar to replace the ActionBar. And hide default title but get custom title
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();

        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(nvDrawer);

        //Header View
        //References
        headerLayout = nvDrawer.inflateHeaderView(R.layout.nav_header);
        headerPhoto = (MyProfilePictureView) headerLayout.findViewById(R.id.header_photo);
        headerLabel = (TextView) headerLayout.findViewById(R.id.header_label);

        //Setting photo and name
        headerPhoto.setImageBitmap(headerPhoto.getRoundedBitmap(currentUser.getProfilePicture()));
        headerLabel.setText(currentUser.getFullName());

        //Status bar very top
        Window window = this.getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.palette_lightprimarycolorlighter));

        headerLayout.setOnClickListener(new ProfileClickListener());

        getFragmentManager().addOnBackStackChangedListener(new SetNavigationIconListener());

    }

    private class SetNavigationIconListener implements FragmentManager.OnBackStackChangedListener {

        @Override
        public void onBackStackChanged() {
            Log.i("AppInfo", "Count: "+getFragmentManager().getBackStackEntryCount());
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true); // show back button
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
            } else {
                //show hamburger
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                drawerToggle.syncState();
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDrawer.openDrawer(GravityCompat.START);
                    }
                });
            }
        }
    }

    //Profile details row selector
    @Override
    public void onRowSelected(int position) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if (findViewById(R.id.flContent) != null) {
            if (savedInstanceState1 != null) {
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            EditProfileFragment editProfileFragment = new EditProfileFragment();
            editProfileFragment.setArguments(bundle);
            fragmentTransaction
                    .setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right, R.animator.slide_in_right, R.animator.slide_out_left)
                    .replace(R.id.flContent, editProfileFragment)
                    .addToBackStack("profileFragment")
                    .commit();
        }

    }

    private class ProfileClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            if (findViewById(R.id.flContent) != null) {
                if (savedInstanceState1 != null) {
                    return;
                }

                // Create a new Fragment to be placed in the activity layout
                ProfileFragment profileFragment = new ProfileFragment();
                // Add the fragment to the 'fragment_container' FrameLayout
                fragmentTransaction
                        .setCustomAnimations(R.animator.slide_in_up, R.animator.slide_out_up, R.animator.slide_in_down, R.animator.slide_out_down)
                        .replace(R.id.flContent, profileFragment, "profileFragment")
                        .commit();
            }
            //Change title of toolbar
            toolbarTitle.setText("Profile");
            // Close the navigation drawer
            mDrawer.closeDrawers();

        }
    }

    public ActionBarDrawerToggle setupDrawerToggle() {
        // NOTE: Make sure you pass in a valid toolbar reference.  ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass;
        switch(menuItem.getItemId()) {
            case R.id.chats_fragment:
                //fragmentClass = ChatFragment.class;
                break;
            case R.id.contacts_fragment:
                //fragmentClass = ContactsFragment.class;
                break;
            case R.id.settings_fragment:
                //fragmentClass = SettingsFragment.class;
                break;
            default:
                //fragmentClass = SettingsFragment.class;
        }

        try {
            //fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        //FragmentManager fragmentManager = getSupportFragmentManager();
        //fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }

    //Overidden for hamburger icon
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getFragmentManager().findFragmentByTag("profileFragment");
        fragment.onActivityResult(requestCode, resultCode, data);
    }

}
